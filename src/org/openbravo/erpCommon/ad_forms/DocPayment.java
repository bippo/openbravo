/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2001-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;

public class DocPayment extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(DocPayment.class);

  private String SeqNo = "0";
  private String SettlementType = "";
  static final BigDecimal ZERO = BigDecimal.ZERO;

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocPayment(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id)
      throws ServletException {
    setObjectFieldProvider(DocPaymentData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Specific Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DateDoc = data[0].getField("DateTrx");
    ChargeAmt = data[0].getField("ChargedAmt");
    SettlementType = data[0].getField("settlementtype");
    // Contained Objects
    p_lines = loadLines(conn);
    if (log4j.isDebugEnabled())
      log4j.debug("DocPayment - loadDocumentDetails - Lines=" + p_lines.length);
    return false;
  } // loadDocumentDetails

  /**
   * Load Payment Line. Settlement Cancel
   * 
   * @return DocLine Array
   */
  private DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLinePaymentData[] data = null;
    try {
      data = DocLinePaymentData.select(connectionProvider, Record_ID);
      for (int i = 0; i < data.length; i++) {
        String Line_ID = data[i].cDebtPaymentId;
        DocLine_Payment docLine = new DocLine_Payment(DocumentType, Record_ID, Line_ID);
        docLine.Amount = data[i].getField("amount");
        docLine.WriteOffAmt = data[i].getField("writeoffamt");
        docLine.isReceipt = data[i].getField("isreceipt");
        docLine.isManual = data[i].getField("ismanual");
        docLine.isPaid = data[i].getField("ispaid");
        docLine.loadAttributes(data[i], this);
        docLine.m_Record_Id2 = data[i].cDebtPaymentId;
        docLine.C_Settlement_Generate_ID = data[i].getField("cSettlementGenerateId");
        docLine.C_Settlement_Cancel_ID = data[i].getField("cSettlementCancelId");
        docLine.C_GLItem_ID = data[i].getField("cGlitemId");
        docLine.IsDirectPosting = data[i].getField("isdirectposting");
        docLine.C_Currency_ID_From = data[i].getField("cCurrencyId");
        docLine.conversionDate = data[i].getField("conversiondate");
        docLine.C_INVOICE_ID = data[i].getField("C_INVOICE_ID");
        docLine.C_BPARTNER_ID = data[i].getField("C_BPARTNER_ID");
        docLine.C_WITHHOLDING_ID = data[i].getField("C_WITHHOLDING_ID");
        docLine.WithHoldAmt = data[i].getField("withholdingamount");
        docLine.C_BANKSTATEMENTLINE_ID = data[i].getField("C_BANKSTATEMENTLINE_ID");
        docLine.C_CASHLINE_ID = data[i].getField("C_CASHLINE_ID");
        try {
          docLine.dpStatus = DocLinePaymentData.getDPStatus(connectionProvider, Record_ID,
              data[i].getField("cDebtPaymentId"));
        } catch (ServletException e) {
          log4j.error(e);
          docLine.dpStatus = "";
        }
        if (log4j.isDebugEnabled())
          log4j.debug("DocPayment - loadLines - docLine.IsDirectPosting - "
              + docLine.IsDirectPosting);
        list.add(docLine);
      }
    } catch (ServletException e) {
      log4j.warn(e);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Source Currency Balance - always zero
   * 
   * @return Zero (always balanced)
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for STT, APP.
   * 
   * <pre>
   * 
   *  Flow:
   *    1. Currency conversion variations
   *    2. Non manual DPs in settlement
   *       2.1 Cancelled
   *       2.2 Generated
   *    3. Manual DPs in settlement
   *       3.1 Transitory account
   *    4. Conceptos contables (manual sett and cancelation DP)
   *    5. Writeoff
   *    6. Bank in transit
   * 
   * </pre>
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Select specific definition
    String strClassname = AcctServerData
        .selectTemplateDoc(conn, as.m_C_AcctSchema_ID, DocumentType);
    if (strClassname.equals(""))
      strClassname = AcctServerData.selectTemplate(conn, as.m_C_AcctSchema_ID, AD_Table_ID);
    if (!strClassname.equals("")) {
      try {
        DocPaymentTemplate newTemplate = (DocPaymentTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocPaymentTemplate - " + e);
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("DocPayment - createFact - p_lines.length - " + p_lines.length);
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();

    // Loop to cover C_Debt_Payment in settlement (SttType != 'I' ||
    // directPosting=Y)
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_Payment line = (DocLine_Payment) p_lines[i];

      if (log4j.isDebugEnabled())
        log4j.debug("DocPayment - createFact - line.conversionDate - " + line.conversionDate);
      // For manual payment with direct posting = 'N' (no posting occurred at payment creation so no
      // conversion, for currency gain-loss, is needed)
      String convertedAmt = "";
      if (line.isManual.equals("Y") && line.IsDirectPosting.equals("N")
          && line.C_Settlement_Cancel_ID.equals(Record_ID))
        convertedAmt = line.Amount;
      else
        // 1* Amount is calculated and if there is currency conversion
        // variations between dates this change is accounted
        convertedAmt = convertAmount(line.Amount, line.isReceipt.equals("Y"), DateAcct,
            line.conversionDate, line.C_Currency_ID_From, C_Currency_ID, line, as, fact,
            Fact_Acct_Group_ID, conn);
      String convertWithHold = convertAmount(line.WithHoldAmt, line.isReceipt.equals("Y"),
          DateAcct, line.conversionDate, line.C_Currency_ID_From, C_Currency_ID, line, as, fact,
          Fact_Acct_Group_ID, conn);
      BigDecimal convertTotal = new BigDecimal(convertedAmt).add(new BigDecimal(convertWithHold));

      if (line.isManual.equals("N")) { // 2* Normal debt-payments
        String finalConvertedAmt = "";
        if (!C_Currency_ID.equals(as.m_C_Currency_ID)) {
          this.MultiCurrency = true;
          // Final conversion needed when currency of the document and currency of the accounting
          // schema are different
          finalConvertedAmt = convertAmount(convertTotal.toString(), line.isReceipt.equals("Y"),
              DateAcct, line.conversionDate, C_Currency_ID, as.m_C_Currency_ID, null, as, fact,
              Fact_Acct_Group_ID, conn);
        } else
          finalConvertedAmt = convertTotal.toString();
        if (!line.C_Settlement_Generate_ID.equals(Record_ID)) { // 2.1*
          // Cancelled
          // DP
          finalConvertedAmt = getConvertedAmt(finalConvertedAmt, as.m_C_Currency_ID, C_Currency_ID,
              DateAcct, "", AD_Client_ID, AD_Org_ID, conn);
          fact.createLine(
              line,
              getAccountBPartner(line.m_C_BPartner_ID, as, line.isReceipt.equals("Y"),
                  line.dpStatus, conn), C_Currency_ID, (line.isReceipt.equals("Y") ? ""
                  : finalConvertedAmt), (line.isReceipt.equals("Y") ? finalConvertedAmt : ""),
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        } else { // 2.2* Generated DP
          if (log4j.isDebugEnabled())
            log4j.debug("Genenarted DP");
          if (!line.isPaid.equals("Y")
              || !(line.C_Settlement_Cancel_ID == null || line.C_Settlement_Cancel_ID.equals(""))) {
            if (log4j.isDebugEnabled())
              log4j.debug("Not paid");
            fact.createLine(
                line,
                getAccountBPartner(line.m_C_BPartner_ID, as, line.isReceipt.equals("Y"),
                    line.dpStatus, conn), C_Currency_ID,
                (line.isReceipt.equals("Y") ? convertTotal.toString() : ""), (line.isReceipt
                    .equals("Y") ? "" : convertTotal.toString()), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
          }
        }

        if (log4j.isDebugEnabled())
          log4j.debug("DocPayment - createFact - No manual  - isReceipt: " + line.isReceipt);
      } else {// 3* MANUAL debt-payments (generated in a Manual stt)
        if (log4j.isDebugEnabled())
          log4j.debug("Manual DP - DirectPosting: " + line.IsDirectPosting + " - SettType:"
              + SettlementType);
        if (line.IsDirectPosting.equals("Y")) { // Direct posting:
          // transitory Account
          BigDecimal amount = ZERO;
          DocPaymentData[] data = DocPaymentData.selectDirectManual(conn, as.m_C_AcctSchema_ID,
              line.Line_ID);
          if (log4j.isDebugEnabled())
            log4j.debug("data[0].amount:" + data[0].amount + " - convertedAmt:" + convertedAmt);

          if (convertedAmt != null && !convertedAmt.equals(""))
            amount = new BigDecimal(convertedAmt);
          boolean changeGenerate = (!SettlementType.equals("I"));
          if (changeGenerate)
            amount = amount.negate();
          BigDecimal transitoryAmount = new BigDecimal(convertedAmt);
          if (log4j.isDebugEnabled())
            log4j.debug("Manual DP - amount:" + amount + " - transitoryAmount:" + transitoryAmount
                + " - Receipt:" + line.isReceipt);
          // Line is cloned to add withholding and/or tax info
          DocLine_Payment lineAux = DocLine_Payment.clone(line);
          lineAux.setM_C_WithHolding_ID(data[0].cWithholdingId);
          lineAux.setM_C_Tax_ID(data[0].cTaxId);
          // Depending on the stt type and the signum of DP it will be
          // posted on credit or debit
          if (amount.signum() == 1) {
            fact.createLine(lineAux, new Account(conn,
                (lineAux.isReceipt.equals("Y") ? data[0].creditAcct : data[0].debitAcct)),
                C_Currency_ID, (lineAux.isReceipt.equals("Y") ? transitoryAmount.abs().toString()
                    : "0"), (lineAux.isReceipt.equals("Y") ? "0" : transitoryAmount.abs()
                    .toString()), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if ((!changeGenerate && line.isReceipt.equals("N"))
                || (changeGenerate && line.isReceipt.equals("Y")))
              amount = amount.negate();
          } else {
            fact.createLine(lineAux, new Account(conn,
                (lineAux.isReceipt.equals("Y") ? data[0].creditAcct : data[0].debitAcct)),
                C_Currency_ID, (lineAux.isReceipt.equals("Y") ? "0" : transitoryAmount.abs()
                    .toString()), (lineAux.isReceipt.equals("Y") ? transitoryAmount.abs()
                    .toString() : "0"), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if ((!changeGenerate && line.isReceipt.equals("Y"))
                || (changeGenerate && line.isReceipt.equals("N")))
              amount = amount.negate();
          }
        }
        // 4 Manual Sett + Cancelation Sett (no direct posting)
        // G/L items
        if (SettlementType.equals("I") || line.IsDirectPosting.equals("N")) {
          DocPaymentData[] data = DocPaymentData.selectManual(conn, as.m_C_AcctSchema_ID,
              line.Line_ID);
          for (int j = 0; data != null && j < data.length; j++) {
            String amountdebit = convertAmount(data[j].amountdebit, line.isReceipt.equals("Y"),
                DateAcct, line.conversionDate, line.C_Currency_ID_From, as.m_C_Currency_ID, line,
                as, fact, Fact_Acct_Group_ID, conn);
            String amountcredit = convertAmount(data[j].amountcredit, line.isReceipt.equals("Y"),
                DateAcct, line.conversionDate, line.C_Currency_ID_From, as.m_C_Currency_ID, line,
                as, fact, Fact_Acct_Group_ID, conn);
            amountdebit = getConvertedAmt(amountdebit, as.m_C_Currency_ID, C_Currency_ID, DateAcct,
                "", AD_Client_ID, AD_Org_ID, conn);
            amountcredit = getConvertedAmt(amountcredit, as.m_C_Currency_ID, C_Currency_ID,
                DateAcct, "", AD_Client_ID, AD_Org_ID, conn);
            if (log4j.isDebugEnabled())
              log4j.debug("DocPayment - createFact - Conceptos - AmountDebit: " + amountdebit
                  + " - AmountCredit: " + amountcredit);
            // Line is cloned to add withholding and/or tax info
            DocLine_Payment lineAux = DocLine_Payment.clone(line);
            lineAux.setM_C_WithHolding_ID(data[j].cWithholdingId);
            lineAux.setM_C_Tax_ID(data[j].cTaxId);
            fact.createLine(lineAux, new Account(conn,
                (lineAux.isReceipt.equals("Y") ? data[j].creditAcct : data[j].debitAcct)),
                C_Currency_ID, (amountdebit.equals("0") ? "" : amountdebit), (amountcredit
                    .equals("0") ? "" : amountcredit), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, conn);
          }
        }
      } // END debt-payment conditions

      // 5* WRITEOFF calculations
      if (line.C_Settlement_Cancel_ID.equals(Record_ID)) { // Cancelled
        // debt-payments
        if (line.WriteOffAmt != null && !line.WriteOffAmt.equals("")
            && !line.WriteOffAmt.equals("0")) {
          String Account_ID = "";
          if ("Y".equals(line.isReceipt)) {
            AcctServerData[] acctData = AcctServerData.selectWriteOffAcct(conn, line.C_BPARTNER_ID,
                as.getC_AcctSchema_ID());
            if (acctData != null && acctData.length != 0) {
              Account_ID = acctData[0].accountId;
            } else {
              acctData = AcctServerData.selectWriteOffDefault(conn, as.getC_AcctSchema_ID());
              if (acctData != null && acctData.length != 0) {
                Account_ID = acctData[0].accountId;
              }
            }
          } else {
            AcctServerData[] acctData = AcctServerData.selectWriteOffAcctRevenue(conn,
                line.C_BPARTNER_ID, as.getC_AcctSchema_ID());
            if (acctData != null && acctData.length != 0) {
              Account_ID = acctData[0].accountId;
            } else {
              acctData = AcctServerData.selectWriteOffDefaultRevenue(conn, as.getC_AcctSchema_ID());
              if (acctData != null && acctData.length != 0) {
                Account_ID = acctData[0].accountId;
              }
            }
          }
          Account acct = null;
          if (!Account_ID.equals("")) {
            try {
              acct = Account.getAccount(conn, Account_ID);
            } catch (ServletException e) {
              log4j.warn(e);
              e.printStackTrace();
            }
          } else {
            log4j.warn("AcctServer - getAccount - NO account Type=" + AcctServer.ACCTTYPE_WriteOff
                + ", Record=" + Record_ID);
          }
          fact.createLine(line, acct, C_Currency_ID, (line.isReceipt.equals("Y") ? line.WriteOffAmt
              : ""), (line.isReceipt.equals("Y") ? "" : line.WriteOffAmt), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
        }
      }

      // 6* PPA - Bank in transit default, paid DPs, (non manual and
      // manual non direct posting)
      if ((line.isPaid.equals("Y") || new BigDecimal(line.Amount).compareTo(new BigDecimal(
          line.WriteOffAmt)) == 0)
          && ((line.C_Settlement_Cancel_ID == null || line.C_Settlement_Cancel_ID.equals("")) || (line.C_Settlement_Cancel_ID
              .equals(Record_ID)))) {
        BigDecimal finalLineAmt = new BigDecimal(line.Amount);
        String idSchema = as.getC_AcctSchema_ID();
        if (line.C_WITHHOLDING_ID != null && !line.C_WITHHOLDING_ID.equals("")) {
          String IdAccount = WithholdingManualData.select_accounts(conn, line.C_WITHHOLDING_ID,
              idSchema);
          //
          String sWithHoldAmt = getConvertedAmt(line.WithHoldAmt, line.C_Currency_ID_From,
              C_Currency_ID, DateAcct, "", AD_Client_ID, AD_Org_ID, conn);

          fact.createLine(line, Account.getAccount(conn, IdAccount), C_Currency_ID, (line.isReceipt
              .equals("Y") ? sWithHoldAmt : ""), (line.isReceipt.equals("Y") ? "" : sWithHoldAmt),
              Fact_Acct_Group_ID, "999999", DocumentType, conn);
        }
        if (line.isPaid.equals("Y")
            && ((line.C_Settlement_Cancel_ID == null || line.C_Settlement_Cancel_ID.equals("")) || (line.C_Settlement_Cancel_ID
                .equals(Record_ID)))) {
          if (line.WriteOffAmt != null && !line.WriteOffAmt.equals("")
              && !line.WriteOffAmt.equals("0"))
            finalLineAmt = finalLineAmt.subtract(new BigDecimal(line.WriteOffAmt));
          String finalAmtTo = "";
          String strcCurrencyId = "";
          if (line.isManual.equals("N")) {
            finalAmtTo = getConvertedAmt(finalLineAmt.toString(), line.C_Currency_ID_From,
                C_Currency_ID, DateAcct, "", AD_Client_ID, AD_Org_ID, conn);
            strcCurrencyId = C_Currency_ID;
          } else { // For manual payment with direct posting = 'N' (no posting occurred at payment
            // creation so no conversion, for currency gain-loss, is needed)
            finalAmtTo = finalLineAmt.toString();
            strcCurrencyId = line.C_Currency_ID_From;
          }
          finalLineAmt = new BigDecimal(finalAmtTo);
          if (finalLineAmt.compareTo(new BigDecimal("0.00")) != 0) {
            if (line.C_BANKSTATEMENTLINE_ID != null && !line.C_BANKSTATEMENTLINE_ID.equals("")) {
              fact.createLine(line,
                  getAccountBankStatementLine(line.C_BANKSTATEMENTLINE_ID, as, conn),
                  strcCurrencyId, (line.isReceipt.equals("Y") ? finalAmtTo : ""),
                  (line.isReceipt.equals("Y") ? "" : finalAmtTo), Fact_Acct_Group_ID, "999999",
                  DocumentType, conn);
            }// else if(line.C_CASHLINE_ID!=null &&
             // !line.C_CASHLINE_ID.equals("")) fact.createLine(line,
             // getAccountCashLine(line.C_CASHLINE_ID,
             // as,conn),strcCurrencyId,
             // (line.isReceipt.equals("Y")?finalAmtTo:""),(line.isReceipt.equals("Y")?"":finalAmtTo),
             // Fact_Acct_Group_ID, "999999", DocumentType,conn);
            else
              fact.createLine(line, getAccount(AcctServer.ACCTTYPE_BankInTransitDefault, as, conn),
                  strcCurrencyId, (line.isReceipt.equals("Y") ? finalAmtTo : ""),
                  (line.isReceipt.equals("Y") ? "" : finalAmtTo), Fact_Acct_Group_ID, "999999",
                  DocumentType, conn);
          }
        }
      }
    } // END of the C_Debt_Payment loop
    SeqNo = "0";
    if (log4j.isDebugEnabled())
      log4j.debug("DocPayment - createFact - finish");
    return fact;
  }

  /**
   * @return the log4j
   */
  public static Logger getLog4j() {
    return log4j;
  }

  /**
   * @param log4j
   *          the log4j to set
   */
  public static void setLog4j(Logger log4j) {
    DocPayment.log4j = log4j;
  }

  /**
   * @return the seqNo
   */
  public String getSeqNo() {
    return SeqNo;
  }

  /**
   * @param seqNo
   *          the seqNo to set
   */
  public void setSeqNo(String seqNo) {
    SeqNo = seqNo;
  }

  /**
   * @return the settlementType
   */
  public String getSettlementType() {
    return SettlementType;
  }

  /**
   * @param settlementType
   *          the settlementType to set
   */
  public void setSettlementType(String settlementType) {
    SettlementType = settlementType;
  }

  /**
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  /**
   * @return the zERO
   */
  public static BigDecimal getZERO() {
    return ZERO;
  }

  public String convertAmount(String Amount, boolean isReceipt, String DateAcct,
      String conversionDate, String C_Currency_ID_From, String C_Currency_ID, DocLine line,
      AcctSchema as, Fact fact, String Fact_Acct_Group_ID, ConnectionProvider conn)
      throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Amount:" + Amount + " curr from:" + C_Currency_ID_From + " Curr to:"
          + C_Currency_ID + " convDate:" + conversionDate + " DateAcct:" + DateAcct);
    if (Amount == null || Amount.equals(""))
      Amount = "0";
    if (C_Currency_ID_From.equals(C_Currency_ID))
      return Amount;
    else
      MultiCurrency = true;
    String Amt = getConvertedAmt(Amount, C_Currency_ID_From, C_Currency_ID, conversionDate, "",
        AD_Client_ID, AD_Org_ID, conn);
    if (log4j.isDebugEnabled())
      log4j.debug("Amt:" + Amt);

    String AmtTo = getConvertedAmt(Amount, C_Currency_ID_From, C_Currency_ID, DateAcct, "",
        AD_Client_ID, AD_Org_ID, conn);
    if (log4j.isDebugEnabled())
      log4j.debug("AmtTo:" + AmtTo);

    BigDecimal AmtDiff = (new BigDecimal(AmtTo)).subtract(new BigDecimal(Amt));
    if (log4j.isDebugEnabled())
      log4j.debug("AmtDiff:" + AmtDiff);

    if (log4j.isDebugEnabled()) {
      log4j.debug("curr from:" + C_Currency_ID_From + " Curr to:" + C_Currency_ID + " convDate:"
          + conversionDate + " DateAcct:" + DateAcct);
      log4j.debug("Amt:" + Amt + " AmtTo:" + AmtTo + " Diff:" + AmtDiff.toString());
    }

    if ((isReceipt && AmtDiff.compareTo(new BigDecimal("0.00")) == 1)
        || (!isReceipt && AmtDiff.compareTo(new BigDecimal("0.00")) == -1)) {
      fact.createLine(line, getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, as, conn),
          C_Currency_ID, "", AmtDiff.abs().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    } else {
      fact.createLine(line, getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, as, conn),
          C_Currency_ID, AmtDiff.abs().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    }

    return Amt;
  }

  /**
   * Get the account for Accounting Schema
   * 
   * @param cBPartnerId
   *          business partner id
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountBPartner(String cBPartnerId, AcctSchema as, boolean isReceipt,
      String dpStatus, ConnectionProvider conn) {
    DocPaymentData[] data = null;
    try {
      if (log4j.isDebugEnabled())
        log4j.debug("DocPayment - getAccountBPartner - DocumentType = " + DocumentType);
      if (isReceipt) {
        data = DocPaymentData.selectBPartnerCustomerAcct(conn, cBPartnerId,
            as.getC_AcctSchema_ID(), dpStatus);
      } else {
        data = DocPaymentData.selectBPartnerVendorAcct(conn, cBPartnerId, as.getC_AcctSchema_ID(),
            dpStatus);
      }
    } catch (ServletException e) {
      log4j.warn(e);
    }
    // Get Acct
    String Account_ID = "";
    if (data != null && data.length != 0) {
      Account_ID = data[0].accountId;
    } else
      return null;
    // No account
    if (Account_ID.equals("")) {
      log4j.warn("DocPayment - getAccountBPartner - NO account BPartner=" + cBPartnerId
          + ", Record=" + Record_ID + ", status " + dpStatus);
      return null;
    }
    // Return Account
    Account acct = null;
    try {
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4j.warn(e);
    }
    return acct;
  } // getAccount

  /**
   * Get the account for Accounting Schema
   * 
   * @param strcBankstatementlineId
   *          Line
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountBankStatementLine(String strcBankstatementlineId, AcctSchema as,
      ConnectionProvider conn) {
    DocPaymentData[] data = null;
    try {
      data = DocPaymentData.selectBankStatementLineAcct(conn, strcBankstatementlineId,
          as.getC_AcctSchema_ID());
    } catch (ServletException e) {
      log4j.warn(e);
    }
    // Get Acct
    String Account_ID = "";
    if (data != null && data.length != 0) {
      Account_ID = data[0].accountId;
    } else
      return null;
    // No account
    if (Account_ID.equals("")) {
      log4j.warn("DocPayment - getAccountBankStatementLine - NO account BankStatementLine="
          + strcBankstatementlineId + ", Record=" + Record_ID);
      return null;
    }
    // Return Account
    Account acct = null;
    try {
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4j.warn(e);
    }
    return acct;
  } // getAccount

  /**
   * Get the account for Accounting Schema
   * 
   * @param strcCashlineId
   *          Line Id
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountCashLine(String strcCashlineId, AcctSchema as,
      ConnectionProvider conn) {
    DocPaymentData[] data = null;
    try {
      data = DocPaymentData.selectCashLineAcct(conn, strcCashlineId, as.getC_AcctSchema_ID());
    } catch (ServletException e) {
      log4j.warn(e);
    }
    // Get Acct
    String Account_ID = "";
    if (data != null && data.length != 0) {
      Account_ID = data[0].accountId;
    } else
      return null;
    // No account
    if (Account_ID.equals("")) {
      log4j.warn("DocPayment - getAccountCashLine - NO account CashLine=" + strcCashlineId
          + ", Record=" + Record_ID);
      return null;
    }
    // Return Account
    Account acct = null;
    try {
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4j.warn(e);
    }
    return acct;
  } // getAccount

  public String nextSeqNo(String oldSeqNo) {
    if (log4j.isDebugEnabled())
      log4j.debug("DocPayment - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    if (log4j.isDebugEnabled())
      log4j.debug("DocPayment - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    DocPaymentData[] data;
    try {
      data = DocPaymentData.paymentsInformation(conn, strRecordId);
    } catch (ServletException e) {
      log4j.error(e.getMessage(), e);
      setStatus(STATUS_Error);
      return false;
    }
    if (data.length > 0) {
      for (DocPaymentData row : data) {
        if (row.ismanual.equals("N") || row.isdirectposting.equals("Y"))
          return true;
      }
      setStatus(STATUS_DocumentDisabled);
      return false;
    }
    return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
