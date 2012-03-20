/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2011 Openbravo S.L.U.
 ******************************************************************************
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

public class DocInvoice extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInvoice = Logger.getLogger(DocInvoice.class);

  DocTax[] m_taxes = null;
  DocLine_FinPaymentSchedule[] m_payments = null;

  public DocLine_FinPaymentSchedule[] getM_payments() {
    return m_payments;
  }

  String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocInvoice(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocInvoiceData.selectRegistro(conn, stradClientId, Id));
  }

  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DateDoc = data[0].getField("DateInvoiced");
    TaxIncluded = data[0].getField("IsTaxIncluded");
    C_BPartner_Location_ID = data[0].getField("C_BPartner_Location_ID");
    // Amounts
    Amounts[AMTTYPE_Gross] = data[0].getField("GrandTotal");
    if (Amounts[AMTTYPE_Gross] == null)
      Amounts[AMTTYPE_Gross] = "0";
    Amounts[AMTTYPE_Net] = data[0].getField("TotalLines");
    if (Amounts[AMTTYPE_Net] == null)
      Amounts[AMTTYPE_Net] = "0";
    Amounts[AMTTYPE_Charge] = data[0].getField("ChargeAmt");
    if (Amounts[AMTTYPE_Charge] == null)
      Amounts[AMTTYPE_Charge] = "0";

    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines();
    m_taxes = loadTaxes();
    m_payments = loadPayments();
    m_debt_payments = loadDebtPayments();
    return true;

  }

  private DocLine[] loadLines() {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineInvoiceData[] data = null;
    try {
      log4jDocInvoice.debug("############### groupLines = " + groupLines);
      if (groupLines.equals("Y"))
        data = DocLineInvoiceData.selectTotal(connectionProvider, Record_ID);
      else
        data = DocLineInvoiceData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].cInvoicelineId;
      DocLine_Invoice docLine = new DocLine_Invoice(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      String strQty = data[i].qtyinvoiced;
      docLine.setQty(strQty);
      String LineNetAmt = data[i].linenetamt;
      String PriceList = data[i].pricelist;
      docLine.setAmount(LineNetAmt, PriceList, strQty);

      list.add(docLine);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  private DocTax[] loadTaxes() {
    ArrayList<Object> list = new ArrayList<Object>();
    DocInvoiceData[] data = null;
    try {
      data = DocInvoiceData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    log4jDocInvoice.debug("############### Taxes.length = " + data.length);
    for (int i = 0; i < data.length; i++) {
      String C_Tax_ID = data[i].cTaxId;
      String name = data[i].name;
      String rate = data[i].rate;

      String taxBaseAmt = data[i].taxbaseamt;
      String amount = data[i].taxamt;
      boolean isTaxDeductable = false;
      boolean isTaxUndeductable = ("Y".equals(data[i].ratetaxundeductable))
          || ("Y".equals(data[i].orgtaxundeductable));
      if ("Y".equals(data[i].orgtaxundeductable)) {
        /*
         * If any tax line level has tax deductable flag then override isTaxUndeductable flag for
         * intracommunity non tax deductible organization
         */
        if ("Y".equals(data[i].istaxdeductable)) {
          isTaxUndeductable = false;
          isTaxDeductable = true;
        }
      } else {
        // configured for intracommunity with tax liability
        if ("Y".equals(data[i].istaxdeductable)) {
          isTaxDeductable = true;
        }
      }

      DocTax taxLine = new DocTax(C_Tax_ID, name, rate, taxBaseAmt, amount, isTaxUndeductable,
          isTaxDeductable);
      list.add(taxLine);
    }
    // Return Array
    DocTax[] tl = new DocTax[list.size()];
    list.toArray(tl);
    return tl;
  } // loadTaxes

  private DocLine_Payment[] loadDebtPayments() {
    ArrayList<Object> list = new ArrayList<Object>();
    DocInvoiceData[] data = null;
    try {
      data = DocInvoiceData.selectDebtPayments(connectionProvider, Record_ID);
      log4jDocInvoice.debug("############### DebtPayments.length = " + data.length);
      for (int i = 0; i < data.length; i++) {
        //
        String Line_ID = data[i].cDebtPaymentId;
        DocLine_Payment dpLine = new DocLine_Payment(DocumentType, Record_ID, Line_ID);
        log4jDocInvoice.debug(" dpLine.m_Record_Id2 = " + data[i].cDebtPaymentId);
        dpLine.m_Record_Id2 = data[i].cDebtPaymentId;
        dpLine.C_Currency_ID_From = data[i].cCurrencyId;
        dpLine.dpStatus = data[i].status;
        dpLine.isReceipt = data[i].isreceipt;
        dpLine.isPaid = data[i].ispaid;
        dpLine.isManual = data[i].ismanual;
        dpLine.WriteOffAmt = data[i].writeoffamt;
        dpLine.Amount = data[i].amount;
        list.add(dpLine);
      }
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }

    // Return Array
    DocLine_Payment[] tl = new DocLine_Payment[list.size()];
    list.toArray(tl);
    return tl;
  } // loadDebtPayments

  private DocLine_FinPaymentSchedule[] loadPayments() {
    ArrayList<Object> list = new ArrayList<Object>();
    DocInvoiceData[] data = null;
    try {
      data = DocInvoiceData.selectPayments(connectionProvider, Record_ID);
      log4jDocInvoice.debug("############### DebtPayments.length = " + data.length);
      for (int i = 0; i < data.length; i++) {
        //
        String Line_ID = data[i].finPaymentScheduleId;
        DocLine_FinPaymentSchedule dpLine = new DocLine_FinPaymentSchedule(DocumentType, Record_ID,
            Line_ID);
        log4jDocInvoice.debug(" dpLine.m_Record_Id2 = " + data[i].finPaymentScheduleId);
        dpLine.m_Record_Id2 = data[i].finPaymentScheduleId;
        dpLine.C_Currency_ID_From = data[i].cCurrencyId;
        dpLine.isPaid = data[i].ispaid;
        dpLine.Amount = data[i].amount;
        dpLine.PrepaidAmount = data[i].prepaidamt;

        list.add(dpLine);
      }
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }

    // Return Array
    DocLine_FinPaymentSchedule[] tl = new DocLine_FinPaymentSchedule[list.size()];
    list.toArray(tl);
    return tl;
  } // loadPayments

  /**
   * Create Facts (the accounting logic) for ARI, ARC, ARF, API, APC.
   * 
   * <pre>
   *  ARI, ARF
   *      Receivables     DR
   *      Charge                  CR
   *      TaxDue                  CR
   *      Revenue                 CR
   *  ARC
   *      Receivables             CR
   *      Charge          DR
   *      TaxDue          DR
   *      Revenue         RR
   *  API
   *      Payables                CR
   *      Charge          DR
   *      TaxCredit       DR
   *      Expense         DR
   *  APC
   *      Payables        DR
   *      Charge                  CR
   *      TaxCredit               CR
   *      Expense                 CR
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
        DocInvoiceTemplate newTemplate = (DocInvoiceTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocInvoiceTemplate - " + e);
      }
    }
    log4jDocInvoice.debug("Starting create fact");
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    // Cash based accounting
    if (!as.isAccrual())
      return null;

    /** @todo Assumes TaxIncluded = N */

    // ARI, ARF
    if (DocumentType.equals(AcctServer.DOCTYPE_ARInvoice)
        || DocumentType.equals(AcctServer.DOCTYPE_ARProForma)) {
      log4jDocInvoice.debug("Point 1");
      // Receivables DR
      if (m_payments == null || m_payments.length == 0)
        for (int i = 0; m_debt_payments != null && i < m_debt_payments.length; i++) {
          if (m_debt_payments[i].isReceipt.equals("Y"))
            fact.createLine(
                m_debt_payments[i],
                getAccountBPartner(C_BPartner_ID, as, true, m_debt_payments[i].dpStatus, conn),
                this.C_Currency_ID,
                getConvertedAmt(m_debt_payments[i].Amount, m_debt_payments[i].C_Currency_ID_From,
                    this.C_Currency_ID, DateAcct, "", AD_Client_ID, AD_Org_ID, conn), "",
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          else
            fact.createLine(
                m_debt_payments[i],
                getAccountBPartner(C_BPartner_ID, as, false, m_debt_payments[i].dpStatus, conn),
                this.C_Currency_ID,
                "",
                getConvertedAmt(m_debt_payments[i].Amount, m_debt_payments[i].C_Currency_ID_From,
                    this.C_Currency_ID, DateAcct, "", AD_Client_ID, AD_Org_ID, conn),
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        }
      else
        for (int i = 0; m_payments != null && i < m_payments.length; i++) {
          fact.createLine(m_payments[i], getAccountBPartner(C_BPartner_ID, as, true, false, conn),
              this.C_Currency_ID, m_payments[i].Amount, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
          if (m_payments[i].C_Currency_ID_From.equals(as.m_C_Currency_ID)
              && new BigDecimal(m_payments[i].PrepaidAmount).compareTo(ZERO) != 0) {
            fact.createLine(m_payments[i], getAccountBPartner(C_BPartner_ID, as, true, true, conn),
                this.C_Currency_ID, m_payments[i].PrepaidAmount, "", Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
          } else {
            try {
              DocInvoiceData[] prepayments = DocInvoiceData.selectPrepayments(connectionProvider,
                  m_payments[i].Line_ID);
              for (int j = 0; j < prepayments.length; j++) {
                BigDecimal prePaymentAmt = convertAmount(new BigDecimal(prepayments[j].prepaidamt),
                    true, DateAcct, TABLEID_Payment, prepayments[j].finPaymentId,
                    m_payments[i].C_Currency_ID_From, as.m_C_Currency_ID, m_payments[i], as, fact,
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn);
                fact.createLine(m_payments[i],
                    getAccountBPartner(C_BPartner_ID, as, true, true, conn),
                    m_payments[i].C_Currency_ID_From, prePaymentAmt.toString(), "",
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              }
            } catch (ServletException e) {
              log4jDocInvoice.warn(e);
            }
          }
        }
      if ((m_payments == null || m_payments.length == 0)
          && (m_debt_payments == null || m_debt_payments.length == 0)) {
        fact.createLine(null, getAccountBPartner(C_BPartner_ID, as, true, false, conn),
            this.C_Currency_ID, Amounts[AMTTYPE_Gross], "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      }
      // Charge CR
      log4jDocInvoice.debug("The first create line");
      fact.createLine(null, getAccount(AcctServer.ACCTTYPE_Charge, as, conn), C_Currency_ID, "",
          getAmount(AcctServer.AMTTYPE_Charge), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
          conn);
      // TaxDue CR
      log4jDocInvoice.debug("m_taxes.length: " + m_taxes);
      for (int i = 0; m_taxes != null && i < m_taxes.length; i++) {
        // New docLine created to assign C_Tax_ID value to the entry
        DocLine docLine = new DocLine(DocumentType, Record_ID, "");
        docLine.m_C_Tax_ID = m_taxes[i].m_C_Tax_ID;
        if (IsReversal.equals("Y"))
          fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue, as, conn),
              C_Currency_ID, m_taxes[i].m_amount, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
        else
          fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue, as, conn),
              C_Currency_ID, "", m_taxes[i].m_amount, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
      }
      // Revenue CR
      if (p_lines != null && p_lines.length > 0) {
        for (int i = 0; i < p_lines.length; i++)
          if (IsReversal.equals("Y"))
            fact.createLine(
                p_lines[i],
                ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Revenue, as, conn),
                this.C_Currency_ID, p_lines[i].getAmount(), "", Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
          else
            fact.createLine(
                p_lines[i],
                ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Revenue, as, conn),
                this.C_Currency_ID, "", p_lines[i].getAmount(), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
      }
      // Set Locations
      FactLine[] fLines = fact.getLines();
      for (int i = 0; i < fLines.length; i++) {
        if (fLines[i] != null) {
          fLines[i].setLocationFromOrg(fLines[i].getAD_Org_ID(conn), true, conn); // from Loc
          fLines[i].setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to Loc
        }
      }
    }
    // ARC
    else if (this.DocumentType.equals(AcctServer.DOCTYPE_ARCredit)) {
      log4jDocInvoice.debug("Point 2");
      // Receivables CR
      if (m_payments == null || m_payments.length == 0)
        for (int i = 0; m_debt_payments != null && i < m_debt_payments.length; i++) {
          BigDecimal amount = new BigDecimal(m_debt_payments[i].Amount);
          // BigDecimal ZERO = BigDecimal.ZERO;
          fact.createLine(
              m_debt_payments[i],
              getAccountBPartner(C_BPartner_ID, as, true, m_debt_payments[i].dpStatus, conn),
              this.C_Currency_ID,
              "",
              getConvertedAmt(((amount.negate())).toPlainString(),
                  m_debt_payments[i].C_Currency_ID_From, this.C_Currency_ID, DateAcct, "",
                  AD_Client_ID, AD_Org_ID, conn), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
        }
      else
        for (int i = 0; m_payments != null && i < m_payments.length; i++) {
          BigDecimal amount = new BigDecimal(m_payments[i].Amount);
          BigDecimal prepaidAmount = new BigDecimal(m_payments[i].PrepaidAmount);
          fact.createLine(m_payments[i], getAccountBPartner(C_BPartner_ID, as, true, false, conn),
              this.C_Currency_ID, "", amount.negate().toString(), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          // Pre-payment: Probably not needed as at this point we can not generate pre-payments
          // against ARC. Amount is negated
          if (m_payments[i].C_Currency_ID_From.equals(as.m_C_Currency_ID)
              && prepaidAmount.compareTo(ZERO) != 0) {
            fact.createLine(m_payments[i], getAccountBPartner(C_BPartner_ID, as, true, true, conn),
                this.C_Currency_ID, "", prepaidAmount.negate().toString(), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
          } else {
            try {
              DocInvoiceData[] prepayments = DocInvoiceData.selectPrepayments(connectionProvider,
                  m_payments[i].Line_ID);
              for (int j = 0; j < prepayments.length; j++) {
                BigDecimal prePaymentAmt = convertAmount(
                    new BigDecimal(prepayments[j].prepaidamt).negate(), true, DateAcct,
                    TABLEID_Payment, prepayments[j].finPaymentId, m_payments[i].C_Currency_ID_From,
                    as.m_C_Currency_ID, m_payments[i], as, fact, Fact_Acct_Group_ID,
                    nextSeqNo(SeqNo), conn);
                fact.createLine(m_payments[i],
                    getAccountBPartner(C_BPartner_ID, as, true, true, conn),
                    m_payments[i].C_Currency_ID_From, "", prePaymentAmt.toString(),
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              }
            } catch (ServletException e) {
              log4jDocInvoice.warn(e);
            }
          }
        }
      if ((m_payments == null || m_payments.length == 0)
          && (m_debt_payments == null || m_debt_payments.length == 0)) {
        fact.createLine(null, getAccountBPartner(C_BPartner_ID, as, true, false, conn),
            this.C_Currency_ID, "", Amounts[AMTTYPE_Gross], Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      }
      // Charge DR
      fact.createLine(null, getAccount(AcctServer.ACCTTYPE_Charge, as, conn), this.C_Currency_ID,
          getAmount(AcctServer.AMTTYPE_Charge), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
      // TaxDue DR
      for (int i = 0; m_taxes != null && i < m_taxes.length; i++) {
        // New docLine created to assign C_Tax_ID value to the entry
        DocLine docLine = new DocLine(DocumentType, Record_ID, "");
        docLine.m_C_Tax_ID = m_taxes[i].m_C_Tax_ID;
        fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue, as, conn),
            this.C_Currency_ID, m_taxes[i].getAmount(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      }
      // Revenue CR
      for (int i = 0; p_lines != null && i < p_lines.length; i++)
        fact.createLine(p_lines[i],
            ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Revenue, as, conn),
            this.C_Currency_ID, p_lines[i].getAmount(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      // Set Locations
      FactLine[] fLines = fact.getLines();
      for (int i = 0; fLines != null && i < fLines.length; i++) {
        if (fLines[i] != null) {
          fLines[i].setLocationFromOrg(fLines[i].getAD_Org_ID(conn), true, conn); // from Loc
          fLines[i].setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to Loc
        }
      }
    }
    // API
    else if (this.DocumentType.equals(AcctServer.DOCTYPE_APInvoice)) {
      log4jDocInvoice.debug("Point 3");
      // Liability CR
      if (m_payments == null || m_payments.length == 0)
        for (int i = 0; m_debt_payments != null && i < m_debt_payments.length; i++) {
          if (m_debt_payments[i].isReceipt.equals("Y"))
            fact.createLine(
                m_debt_payments[i],
                getAccountBPartner(C_BPartner_ID, as, true, m_debt_payments[i].dpStatus, conn),
                this.C_Currency_ID,
                getConvertedAmt(m_debt_payments[i].Amount, m_debt_payments[i].C_Currency_ID_From,
                    this.C_Currency_ID, DateAcct, "", AD_Client_ID, AD_Org_ID, conn), "",
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          else
            fact.createLine(
                m_debt_payments[i],
                getAccountBPartner(C_BPartner_ID, as, false, m_debt_payments[i].dpStatus, conn),
                this.C_Currency_ID,
                "",
                getConvertedAmt(m_debt_payments[i].Amount, m_debt_payments[i].C_Currency_ID_From,
                    this.C_Currency_ID, DateAcct, "", AD_Client_ID, AD_Org_ID, conn),
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        }
      else
        for (int i = 0; m_payments != null && i < m_payments.length; i++) {
          fact.createLine(m_payments[i], getAccountBPartner(C_BPartner_ID, as, false, false, conn),
              this.C_Currency_ID, "", m_payments[i].Amount, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
          if (m_payments[i].C_Currency_ID_From.equals(as.m_C_Currency_ID)
              && new BigDecimal(m_payments[i].PrepaidAmount).compareTo(ZERO) != 0) {
            fact.createLine(m_payments[i],
                getAccountBPartner(C_BPartner_ID, as, false, true, conn), this.C_Currency_ID, "",
                m_payments[i].PrepaidAmount, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
                conn);
          } else {
            try {
              DocInvoiceData[] prepayments = DocInvoiceData.selectPrepayments(connectionProvider,
                  m_payments[i].Line_ID);
              for (int j = 0; j < prepayments.length; j++) {
                BigDecimal prePaymentAmt = convertAmount(new BigDecimal(prepayments[j].prepaidamt),
                    false, DateAcct, TABLEID_Payment, prepayments[j].finPaymentId,
                    m_payments[i].C_Currency_ID_From, as.m_C_Currency_ID, m_payments[i], as, fact,
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn);
                fact.createLine(m_payments[i],
                    getAccountBPartner(C_BPartner_ID, as, false, true, conn),
                    m_payments[i].C_Currency_ID_From, "", prePaymentAmt.toString(),
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              }
            } catch (ServletException e) {
              log4jDocInvoice.warn(e);
            }
          }
        }
      if ((m_payments == null || m_payments.length == 0)
          && (m_debt_payments == null || m_debt_payments.length == 0)) {
        fact.createLine(null, getAccountBPartner(C_BPartner_ID, as, false, false, conn),
            this.C_Currency_ID, "", Amounts[AMTTYPE_Gross], Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      }
      // Charge DR
      fact.createLine(null, getAccount(AcctServer.ACCTTYPE_Charge, as, conn), this.C_Currency_ID,
          getAmount(AcctServer.AMTTYPE_Charge), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
      // TaxCredit DR
      for (int i = 0; m_taxes != null && i < m_taxes.length; i++) {
        // New docLine created to assign C_Tax_ID value to the entry
        DocLine docLine = new DocLine(DocumentType, Record_ID, "");
        docLine.m_C_Tax_ID = m_taxes[i].m_C_Tax_ID;

        if (m_taxes[i].m_isTaxUndeductable) {
          computeTaxUndeductableLine(conn, as, fact, docLine, Fact_Acct_Group_ID,
              m_taxes[i].m_C_Tax_ID, m_taxes[i].getAmount());
        } else {
          if (IsReversal.equals("Y")) {
            fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn),
                this.C_Currency_ID, "", m_taxes[i].getAmount(), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);

          } else {
            fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn),
                this.C_Currency_ID, m_taxes[i].getAmount(), "", Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
          }
        }
      }
      // Expense DR
      for (int i = 0; p_lines != null && i < p_lines.length; i++)
        if (IsReversal.equals("Y"))
          fact.createLine(p_lines[i],
              ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn),
              this.C_Currency_ID, "", p_lines[i].getAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
        else
          fact.createLine(p_lines[i],
              ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn),
              this.C_Currency_ID, p_lines[i].getAmount(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
      // Set Locations
      FactLine[] fLines = fact.getLines();
      for (int i = 0; fLines != null && i < fLines.length; i++) {
        if (fLines[i] != null) {
          fLines[i].setLocationFromBPartner(C_BPartner_Location_ID, true, conn); // from Loc
          fLines[i].setLocationFromOrg(fLines[i].getAD_Org_ID(conn), false, conn); // to Loc
        }
      }
      updateProductInfo(as.getC_AcctSchema_ID(), conn, con); // only API
    }
    // APC
    else if (this.DocumentType.equals(AcctServer.DOCTYPE_APCredit)) {
      log4jDocInvoice.debug("Point 4");
      // Liability DR
      if (m_payments == null || m_payments.length == 0)
        for (int i = 0; m_debt_payments != null && i < m_debt_payments.length; i++) {
          BigDecimal amount = new BigDecimal(m_debt_payments[i].Amount);
          // BigDecimal ZERO = BigDecimal.ZERO;
          fact.createLine(
              m_debt_payments[i],
              getAccountBPartner(C_BPartner_ID, as, false, m_debt_payments[i].dpStatus, conn),
              this.C_Currency_ID,
              getConvertedAmt(((amount.negate())).toPlainString(),
                  m_debt_payments[i].C_Currency_ID_From, this.C_Currency_ID, DateAcct, "",
                  AD_Client_ID, AD_Org_ID, conn), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
        }
      else
        for (int i = 0; m_payments != null && i < m_payments.length; i++) {
          BigDecimal amount = new BigDecimal(m_payments[i].Amount);
          BigDecimal prepaidAmount = new BigDecimal(m_payments[i].PrepaidAmount);
          fact.createLine(m_payments[i], getAccountBPartner(C_BPartner_ID, as, false, false, conn),
              this.C_Currency_ID, amount.negate().toString(), "", Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          // Pre-payment: Probably not needed as at this point we can not generate pre-payments
          // against APC. Amount is negated
          if (m_payments[i].C_Currency_ID_From.equals(as.m_C_Currency_ID)
              && prepaidAmount.compareTo(ZERO) != 0) {
            fact.createLine(m_payments[i],
                getAccountBPartner(C_BPartner_ID, as, false, true, conn), this.C_Currency_ID,
                prepaidAmount.negate().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, conn);
          } else {
            try {
              DocInvoiceData[] prepayments = DocInvoiceData.selectPrepayments(connectionProvider,
                  m_payments[i].Line_ID);
              for (int j = 0; j < prepayments.length; j++) {
                BigDecimal prePaymentAmt = convertAmount(
                    new BigDecimal(prepayments[j].prepaidamt).negate(), false, DateAcct,
                    TABLEID_Payment, prepayments[j].finPaymentId, m_payments[i].C_Currency_ID_From,
                    as.m_C_Currency_ID, m_payments[i], as, fact, Fact_Acct_Group_ID,
                    nextSeqNo(SeqNo), conn);
                fact.createLine(m_payments[i],
                    getAccountBPartner(C_BPartner_ID, as, false, true, conn),
                    m_payments[i].C_Currency_ID_From, prePaymentAmt.toString(), "",
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
              }
            } catch (ServletException e) {
              log4jDocInvoice.warn(e);
            }
          }
        }
      if ((m_payments == null || m_payments.length == 0)
          && (m_debt_payments == null || m_debt_payments.length == 0)) {
        fact.createLine(null, getAccountBPartner(C_BPartner_ID, as, false, false, conn),
            this.C_Currency_ID, Amounts[AMTTYPE_Gross], "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      }
      // Charge CR
      fact.createLine(null, getAccount(AcctServer.ACCTTYPE_Charge, as, conn), this.C_Currency_ID,
          "", getAmount(AcctServer.AMTTYPE_Charge), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
      // TaxCredit CR
      for (int i = 0; m_taxes != null && i < m_taxes.length; i++) {
        // New docLine created to assign C_Tax_ID value to the entry
        DocLine docLine = new DocLine(DocumentType, Record_ID, "");
        docLine.m_C_Tax_ID = m_taxes[i].m_C_Tax_ID;
        if (m_taxes[i].m_isTaxUndeductable) {
          computeTaxUndeductableLine(conn, as, fact, docLine, Fact_Acct_Group_ID,
              m_taxes[i].m_C_Tax_ID, m_taxes[i].getAmount());

        } else {
          fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn),
              this.C_Currency_ID, "", m_taxes[i].getAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
        }
      }
      // Expense CR
      for (int i = 0; p_lines != null && i < p_lines.length; i++)
        fact.createLine(p_lines[i],
            ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn),
            this.C_Currency_ID, "", p_lines[i].getAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      // Set Locations
      FactLine[] fLines = fact.getLines();
      for (int i = 0; fLines != null && i < fLines.length; i++) {
        if (fLines[i] != null) {
          fLines[i].setLocationFromBPartner(C_BPartner_Location_ID, true, conn); // from Loc
          fLines[i].setLocationFromOrg(fLines[i].getAD_Org_ID(conn), false, conn); // to Loc
        }
      }
    } else {
      log4jDocInvoice.warn("Doc_Invoice - DocumentType unknown: " + this.DocumentType);
      fact = null;
    }
    SeqNo = "0";
    return fact;
  }// createFact

  /**
   * Update Product Info. - Costing (PriceLastInv) - PO (PriceLastInv)
   * 
   * @param C_AcctSchema_ID
   *          accounting schema
   */
  public void updateProductInfo(String C_AcctSchema_ID, ConnectionProvider conn, Connection con) {
    log4jDocInvoice.debug("updateProductInfo - C_Invoice_ID=" + this.Record_ID);

    /**
     * @todo Last.. would need to compare document/last updated date would need to maintain
     *       LastPriceUpdateDate on _PO and _Costing
     */

    // update Product PO info
    // should only be once, but here for every AcctSchema
    // ignores multiple lines with same product - just uses first
    int no = 0;
    try {
      no = DocInvoiceData.updateProductPO(con, conn, Record_ID);
      log4jDocInvoice.debug("M_Product_PO - Updated=" + no);

    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
  } // updateProductInfo

  /**
   * Get Source Currency Balance - subtracts line and tax amounts from total - no rounding
   * 
   * @return positive amount, if total invoice is bigger than lines
   */
  public BigDecimal getBalance() {
    // BigDecimal ZERO = new BigDecimal("0");
    BigDecimal retValue = ZERO;
    StringBuffer sb = new StringBuffer(" [");
    // Total
    retValue = retValue.add(new BigDecimal(getAmount(AcctServer.AMTTYPE_Gross)));
    sb.append(getAmount(AcctServer.AMTTYPE_Gross));
    // - Charge
    retValue = retValue.subtract(new BigDecimal(getAmount(AcctServer.AMTTYPE_Charge)));
    sb.append("-").append(getAmount(AcctServer.AMTTYPE_Charge));
    // - Tax
    for (int i = 0; i < m_taxes.length; i++) {
      retValue = retValue.subtract(new BigDecimal(m_taxes[i].getAmount()));
      sb.append("-").append(m_taxes[i].getAmount());
    }
    // - Lines
    for (int i = 0; i < p_lines.length; i++) {
      retValue = retValue.subtract(new BigDecimal(p_lines[i].getAmount()));
      sb.append("-").append(p_lines[i].getAmount());
    }
    sb.append("]");
    //
    log4jDocInvoice.debug("Balance=" + retValue + sb.toString());
    return retValue;
  } // getBalance

  public String nextSeqNo(String oldSeqNo) {
    log4jDocInvoice.debug("DocInvoice - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocInvoice.debug("DocInvoice - nextSeqNo = " + SeqNo);
    return SeqNo;
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
        log4j.debug("DocInvoice - getAccountBPartner - DocumentType = " + DocumentType);
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
      log4j.warn("DocInvoice - getAccountBPartner - NO account BPartner=" + cBPartnerId
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
   * @return the log4jDocInvoice
   */
  public static Logger getLog4jDocInvoice() {
    return log4jDocInvoice;
  }

  /**
   * @param log4jDocInvoice
   *          the log4jDocInvoice to set
   */
  public static void setLog4jDocInvoice(Logger log4jDocInvoice) {
    DocInvoice.log4jDocInvoice = log4jDocInvoice;
  }

  /**
   * @return the m_taxes
   */
  public DocTax[] getM_taxes() {
    return m_taxes;
  }

  /**
   * @param m_taxes
   *          the m_taxes to set
   */
  public void setM_taxes(DocTax[] m_taxes) {
    this.m_taxes = m_taxes;
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
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  /**
   * Get Document Confirmation
   * 
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    DocInvoiceData[] data = null;
    FieldProvider dataFP[] = getObjectFieldProvider();

    if (ZERO.compareTo(new BigDecimal(dataFP[0].getField("GrandTotal"))) == 0) {
      strMessage = "@TotalGrossIsZero@";
      setStatus(STATUS_DocumentDisabled);
      return false;
    }

    try {
      data = DocInvoiceData.selectRegistro(conn, AD_Client_ID, strRecordId);
      AcctSchema[] m_acctSchemas = reloadLocalAcctSchemaArray(data[0].adOrgId);

      AcctSchema acct = null;
      for (int i = 0; i < m_acctSchemas.length; i++) {
        acct = m_acctSchemas[i];
        data = DocInvoiceData.selectFinInvCount(conn, strRecordId, acct.m_C_AcctSchema_ID);
        int countFinInv = Integer.parseInt(data[0].fininvcount);
        int countGLItemAcct = Integer.parseInt(data[0].finacctcount);
        // For any GL Item used in financial invoice lines debit/credit accounts must be defined
        if (countFinInv != 0 && (countFinInv != countGLItemAcct)) {
          log4jDocInvoice.debug("DocInvoice - getDocumentConfirmation - GL Item used in financial "
              + "invoice lines debit/credit accounts must be defined.");
          setStatus(STATUS_InvalidAccount);
          return false;
        }
      }
    } catch (Exception e) {
      log4jDocInvoice.error("Exception in getDocumentConfirmation method.", e);
    }

    return true;
  }

  private AcctSchema[] reloadLocalAcctSchemaArray(String adOrgId) throws ServletException {
    AcctSchema acct = null;
    ArrayList<Object> new_as = new ArrayList<Object>();
    // We reload again all the acct schemas of the client
    AcctSchema[] m_aslocal = AcctSchema.getAcctSchemaArray(connectionProvider, AD_Client_ID,
        adOrgId);
    // Filter the right acct schemas for the organization
    for (int i = 0; i < m_aslocal.length; i++) {
      acct = m_aslocal[i];
      if (AcctSchemaData.selectAcctSchemaTable2(connectionProvider, acct.m_C_AcctSchema_ID,
          AD_Table_ID, adOrgId)) {
        new_as.add(new AcctSchema(connectionProvider, acct.m_C_AcctSchema_ID));
      }
    }
    AcctSchema[] retValue = new AcctSchema[new_as.size()];
    new_as.toArray(retValue);
    m_aslocal = retValue;
    return m_aslocal;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method

  private void computeTaxUndeductableLine(ConnectionProvider conn, AcctSchema as, Fact fact,
      DocLine docLine, String Fact_Acct_Group_ID, String taxId, String strTaxAmount) {
    int invoiceLineTaxCount = 0;
    int totalInvoiceLineTax = getTaxLineCount(conn, taxId);
    BigDecimal cumulativeTaxLineAmount = new BigDecimal(0);
    BigDecimal taxAmount = new BigDecimal(strTaxAmount.equals("") ? "0.00" : strTaxAmount);
    DocInvoiceData[] data = null;
    try {

      // We can have some lines from product or some lines from general ledger
      data = DocInvoiceData.selectProductAcct(conn, as.getC_AcctSchema_ID(), taxId, Record_ID);
      cumulativeTaxLineAmount = createLineForTaxUndeductable(invoiceLineTaxCount,
          totalInvoiceLineTax, cumulativeTaxLineAmount, taxAmount, data, conn, fact, docLine,
          Fact_Acct_Group_ID);
      invoiceLineTaxCount = data.length;
      // check whether gl item is selected instead of product in invoice line
      data = DocInvoiceData.selectGLItemAcctForTaxLine(conn, as.getC_AcctSchema_ID(), taxId,
          Record_ID);
      createLineForTaxUndeductable(invoiceLineTaxCount, totalInvoiceLineTax,
          cumulativeTaxLineAmount, taxAmount, data, conn, fact, docLine, Fact_Acct_Group_ID);
    } catch (ServletException e) {
      log4jDocInvoice.error("Exception in computeTaxUndeductableLine method: " + e);
    }
  }

  private int getTaxLineCount(ConnectionProvider conn, String taxId) {
    DocInvoiceData[] data = null;
    try {
      data = DocInvoiceData.getTaxLineCount(conn, taxId, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.error("Exception in getTaxLineCount method: " + e);
    }
    if (data.length > 0) {
      return Integer.parseInt(data[0].totallines);
    }
    return 0;
  }

  private BigDecimal createLineForTaxUndeductable(int invoiceLineTaxCount, int totalInvoiceLineTax,
      BigDecimal cumulativeTaxLineAmount, BigDecimal taxAmount, DocInvoiceData[] data,
      ConnectionProvider conn, Fact fact, DocLine docLine, String Fact_Acct_Group_ID) {
    for (int j = 0; j < data.length; j++) {
      invoiceLineTaxCount++;
      // We have to adjust the amount in last line of tax
      if (invoiceLineTaxCount == totalInvoiceLineTax) {
        data[j].taxamt = taxAmount.subtract(cumulativeTaxLineAmount).toPlainString();
      }
      try {
        // currently applicable for API and APC
        if (this.DocumentType.equals(AcctServer.DOCTYPE_APInvoice)) {
          if (IsReversal.equals("Y")) {
            fact.createLine(docLine, Account.getAccount(conn, data[j].pExpenseAcct),
                this.C_Currency_ID, "", data[j].taxamt, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, conn);

          } else {
            fact.createLine(docLine, Account.getAccount(conn, data[j].pExpenseAcct),
                this.C_Currency_ID, data[j].taxamt, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, conn);
          }
        } else if (this.DocumentType.equals(AcctServer.DOCTYPE_APCredit)) {
          fact.createLine(docLine, Account.getAccount(conn, data[j].pExpenseAcct),
              this.C_Currency_ID, "", data[j].taxamt, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
        }
        cumulativeTaxLineAmount = cumulativeTaxLineAmount.add(new BigDecimal(data[j].taxamt));
      } catch (ServletException e) {
        log4jDocInvoice.error("Exception in createLineForTaxUndeductable method: " + e);
      }
    }
    return cumulativeTaxLineAmount;
  }

}
