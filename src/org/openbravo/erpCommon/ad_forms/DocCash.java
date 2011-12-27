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
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
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

public class DocCash extends AcctServer {
  private static final long serialVersionUID = 1L;

  /**
   * @return the log4jDocCash
   */
  public static Logger getLog4jDocCash() {
    return log4jDocCash;
  }

  /**
   * @param log4jDocCash
   *          the log4jDocCash to set
   */
  public static void setLog4jDocCash(Logger log4jDocCash) {
    DocCash.log4jDocCash = log4jDocCash;
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

  static Logger log4jDocCash = Logger.getLogger(DocCash.class);

  private String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocCash(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id)
      throws ServletException {
    log4jDocCash.debug("************************** DocCash - loadObjectFieldProvider - ID - " + Id);
    setObjectFieldProvider(DocCashData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Specific Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = DOCTYPE_CashJournal;
    log4jDocCash.debug("data.length = " + data.length + " - DocumentType = " + DocumentType);
    DateDoc = data[0].getField("StatementDate");

    // Amounts
    Amounts[AcctServer.AMTTYPE_Gross] = data[0].getField("StatementDifference");
    if (Amounts[AcctServer.AMTTYPE_Gross] == null)
      Amounts[AcctServer.AMTTYPE_Gross] = ZERO.toString();

    // Set CashBook Org & Currency
    setCashBookInfo();
    loadDocumentType(); // lines require doc type

    // Contained Objects
    p_lines = loadLines(conn);
    log4jDocCash.debug("Lines=" + p_lines.length);
    return true;
  } // loadDocumentDetails

  /**
   * Set CashBook info. - CashBook_ID - Organization - Currency
   */
  private void setCashBookInfo() {
    DocCashData[] data = null;
    try {
      data = DocCashData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocCash.warn(e);
    }
    if (data != null && data.length != 0) {
      C_CashBook_ID = data[0].cCashbookId;
      if (AD_Org_ID == null || AD_Org_ID.equals(""))
        AD_Org_ID = data[0].adOrgId;
      C_Currency_ID = data[0].cCurrencyId;
    }
    log4jDocCash.debug("setCashBookInfo - C_Currency_ID = " + C_Currency_ID + " - AD_Org_ID = "
        + AD_Org_ID + " - C_CashBook_ID = " + C_CashBook_ID);
  } // setCashBookInfo

  /**
   * Load Cash Line
   * 
   * @return DocLine Array
   */
  private DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineCashData[] data = null;
    try {
      data = DocLineCashData.select(connectionProvider, Record_ID);
      for (int i = 0; data != null && i < data.length; i++) {
        String t_Line_ID = data[i].cCashlineId;
        DocLine_Cash docLine = new DocLine_Cash(DocumentType, Record_ID, t_Line_ID);
        docLine.Line_ID = t_Line_ID;
        docLine.loadAttributes(data[i], this);
        docLine.setCashType(data[i].cashtype);
        docLine.m_C_Order_Id = data[i].cOrderId;
        docLine.m_C_Debt_Payment_Id = data[i].cDebtPaymentId;
        docLine.m_Record_Id2 = data[i].cDebtPaymentId;
        docLine.m_C_BPartner_ID = DocLineCashData.selectDebtBPartner(connectionProvider,
            docLine.m_C_Debt_Payment_Id);
        docLine.m_C_Glitem_ID = data[i].cGlitemId;
        docLine.setReference(data[i].cOrderId, data[i].cDebtPaymentId, conn);
        docLine.setAmount(data[i].amount, data[i].discountamt, data[i].writeoffamt);
        list.add(docLine);
      }
    } catch (ServletException e) {
      log4jDocCash.warn(e);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Source Currency Balance - subtracts line amounts from total - no rounding
   * 
   * @return positive amount, if total invoice is bigger than lines
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    StringBuffer sb = new StringBuffer(" [");
    // Total
    retValue = retValue.add(new BigDecimal(getAmount(AcctServer.AMTTYPE_Gross)));
    sb.append(getAmount(AcctServer.AMTTYPE_Gross));
    // - Lines
    for (int i = 0; i < p_lines.length; i++) {
      retValue = retValue.subtract(new BigDecimal(p_lines[i].getAmount()));
      sb.append("-").append(p_lines[i].getAmount());
    }
    sb.append("]");
    //
    log4jDocCash.debug(" Balance=" + retValue + sb.toString());
    // return retValue;
    return ZERO; // Lines are balanced
  } // getBalance

  /**
   * Create Facts (the accounting logic) for CMC.
   * 
   * <pre>
   *  Expense
   *          CashExpense     DR
   *          CashAsset               CR
   *  Receipt
   *          CashAsset       DR
   *          CashReceipt             CR
   *  Charge
   *          Charge          DR
   *          CashAsset               CR
   * jarenor
   *  G/L Item
   *          Charge          DR
   *          CashAsset               CR
   *  Difference
   *          CashDifference  DR
   *          CashAsset               CR
   *  Invoice
   *          CashAsset       DR
   *          CashTransfer            CR
   *  Transfer
   *          BankInTransit   DR
   *          CashAsset               CR
   * </pre>
   * 
   * @param as
   *          account schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Need to have CashBook
    if (C_CashBook_ID.equals("")) {
      log4jDocCash.warn("createFact - C_CashBook_ID not set");
      return null;
    }
    // Select specific definition
    String strClassname = AcctServerData
        .selectTemplateDoc(conn, as.m_C_AcctSchema_ID, DocumentType);
    if (strClassname.equals(""))
      strClassname = AcctServerData.selectTemplate(conn, as.m_C_AcctSchema_ID, AD_Table_ID);
    if (!strClassname.equals("")) {
      try {
        DocCashTemplate newTemplate = (DocCashTemplate) Class.forName(strClassname).newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocCashTemplate - " + e);
      }
    }
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    // Header posting amt as Invoices and Transfer could be differenet
    // currency
    // CashAsset Total
    BigDecimal assetAmt = ZERO;
    // Lines
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_Cash line = (DocLine_Cash) p_lines[i];
      String CashType = line.m_CashType;
      BigDecimal amount = new BigDecimal(line.getAmount());
      log4jDocCash.debug("antes del creteline, line.getAmount(): " + line.getAmount()
          + " - CashType: " + CashType);
      if (CashType.equals(DocLine_Cash.CASHTYPE_EXPENSE)) {
        // amount is negative
        // CashExpense DR
        // CashAsset CR
        log4jDocCash.debug("********** DocCash - factAcct - account - "
            + getAccount(AcctServer.ACCTTYPE_CashExpense, as, conn).C_ValidCombination_ID);
        fact.createLine(line, getAccount(AcctServer.ACCTTYPE_CashExpense, as, conn), C_Currency_ID,
            amount.negate().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
            conn);
        // fact.createLine(line,
        // getAccount(AcctServer.ACCTTYPE_CashAsset, as),
        // p_vo.C_Currency_ID, null, line.getAmount().negate());
        assetAmt = assetAmt.subtract(amount.negate());
      } else if (CashType.equals(DocLine_Cash.CASHTYPE_RECEIPT)) {
        // amount is positive
        // CashAsset DR
        // CashReceipt CR
        // fact.createLine(line, getAccount(Doc.ACCTTYPE_CashAsset, as),
        // p_vo.C_Currency_ID, line.getAmount(), null);
        assetAmt = assetAmt.add(amount);
        log4jDocCash.debug("********** DocCash - factAcct - account - "
            + getAccount(AcctServer.ACCTTYPE_CashReceipt, as, conn).C_ValidCombination_ID);
        fact.createLine(line, getAccount(AcctServer.ACCTTYPE_CashReceipt, as, conn), C_Currency_ID,
            "", amount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      } else if (CashType.equals(DocLine_Cash.CASHTYPE_CHARGE)) {
        // amount is negative
        // Charge DR
        // CashAsset CR
        log4jDocCash.debug("********** DocCash - factAcct - account - "
            + line.getChargeAccount(as, new BigDecimal(getAmount()), conn).C_ValidCombination_ID);
        fact.createLine(line, line.getChargeAccount(as, new BigDecimal(getAmount()), conn),
            C_Currency_ID, amount.negate().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
        // fact.createLine(line, getAccount(Doc.ACCTTYPE_CashAsset, as),
        // p_vo.C_Currency_ID, null, line.getAmount().negate());
        assetAmt = assetAmt.subtract(amount.negate());
      } else if (CashType.equals(DocLine_Cash.CASHTYPE_GLITEM)) {
        // amount is negative
        // Charge DR
        // CashAsset CR
        log4jDocCash.debug("********** DocCash - factAcct - account - "
            + line.getGlitemAccount(as, amount, conn).C_ValidCombination_ID);
        fact.createLine(line, line.getGlitemAccount(as, amount, conn), C_Currency_ID, amount
            .negate().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        assetAmt = assetAmt.subtract(amount.negate());
      } else if (CashType.equals(DocLine_Cash.CASHTYPE_DIFFERENCE)) {
        // amount is pos/neg
        // CashDifference DR
        // CashAsset CR
        log4jDocCash.debug("********** DocCash - factAcct - account - "
            + getAccount(AcctServer.ACCTTYPE_CashDifference, as, conn).C_ValidCombination_ID);
        fact.createLine(line, getAccount(AcctServer.ACCTTYPE_CashDifference, as, conn),
            C_Currency_ID, amount.negate().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
        // fact.createLine(line,
        // getAccount(AcctServer.ACCTTYPE_CashAsset, as),
        // p_vo.C_Currency_ID, line.getAmount());
        assetAmt = assetAmt.add(amount);
      } else if (CashType.equals(DocLine_Cash.CASHTYPE_INVOICE)) {
        // amount is pos/neg
        // CashAsset DR dr -- Invoice is in Invoice Currency !
        // CashTransfer cr CR
        log4jDocCash.debug("********** DocCash - factAcct - account - "
            + getAccount(AcctServer.ACCTTYPE_CashAsset, as, conn).C_ValidCombination_ID);
        if (line.getC_Currency_ID(conn) == C_Currency_ID)
          assetAmt = assetAmt.add(amount);
        else
          fact.createLine(null, getAccount(AcctServer.ACCTTYPE_CashAsset, as, conn),
              line.getC_Currency_ID(conn), amount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
        fact.createLine(line, getAccount(AcctServer.ACCTTYPE_CashTransfer, as, conn),
            line.getC_Currency_ID(conn), amount.negate().toString(), Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, conn);
      } else if (CashType.equals(DocLine_Cash.CASHTYPE_DEBTPAYMENT)
          || CashType.equals(DocLine_Cash.CASHTYPE_ORDER)) {
        if (amount.signum() == 1) {
          log4jDocCash.debug("********** DocCash - factAcct - amount - " + amount.toString()
              + " - debit");
          log4jDocCash
              .debug("********** DocCash - factAcct - account - "
                  + getAccount(AcctServer.ACCTTYPE_BankInTransitDefault, as, conn).C_ValidCombination_ID);
          // fact.createLine(line,getAccountCashInTransit(line.m_TrxLine_ID,
          // as, conn),line.getC_Currency_ID(conn), "",
          // amount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          // DocumentType, conn);
          fact.createLine(line, getAccount(AcctServer.ACCTTYPE_BankInTransitDefault, as, conn),
              line.getC_Currency_ID(conn), "", amount.toString(), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          fact.createLine(null, getAccount(AcctServer.ACCTTYPE_CashAsset, as, conn), C_Currency_ID,
              amount.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          // fact.createLine(line,getAccount(AcctServer.ACCTTYPE_CashReceipt,
          // as, conn),line.getC_Currency_ID(conn), "",
          // amount.negate().toString(), conn);
          // assetAmt = assetAmt.add(amount);
        } else {
          log4jDocCash.debug("********** DocCash - factAcct - amount - " + amount.toString()
              + " - credit");
          // fact.createLine(line,getAccount(AcctServer.ACCTTYPE_CashExpense,
          // as, conn),line.getC_Currency_ID(conn), "",
          // amount.toString(), conn);
          fact.createLine(line, getAccount(AcctServer.ACCTTYPE_BankInTransitDefault, as, conn),
              line.getC_Currency_ID(conn), amount.negate().toString(), "", Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          // fact.createLine(line,getAccountCashInTransit(line.m_TrxLine_ID,
          // as, conn),line.getC_Currency_ID(conn),
          // amount.negate().toString(), "", Fact_Acct_Group_ID,
          // nextSeqNo(SeqNo), DocumentType, conn);
          fact.createLine(null, getAccount(AcctServer.ACCTTYPE_CashAsset, as, conn), C_Currency_ID,
              "", amount.negate().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              conn);
          // assetAmt = assetAmt.subtract(amount.negate());
        }
      }/*
        * else if (CashType.equals(DocLine_Cash.CASHTYPE_ORDER)){
        * log4jDocCash.debug("********************* pasa por aqui " + CashType); String BPartner =
        * ""; String isSOTrx = ""; DocCashData [] data = null; try{ data =
        * DocCashData.selectBPartner(conn, line.Line_ID); } catch (ServletException e){
        * log4jDocCash.warn(e); } if (data!=null && data.length > 0){ BPartner =
        * data[0].cBpartnerId; isSOTrx = data[0].issotrx; }
        * log4jDocCash.debug("DocCash CASHTYPE_ORDER - C_CURRENCY_ID = " +
        * line.getC_Currency_ID(conn)); if (isSOTrx.equals("Y")){
        * fact.createLine(line,getAccountBPartner(true,BPartner, as,
        * conn),line.getC_Currency_ID(conn), "", amount.toString(), Fact_Acct_Group_ID,
        * nextSeqNo(SeqNo), DocumentType, conn); //fact
        * .createLine(line,getAccount(AcctServer.ACCTTYPE_CashReceipt, as,
        * conn),line.getC_Currency_ID(conn), "", amount.negate().toString(), conn); assetAmt =
        * assetAmt.add(amount); }else{ //fact.createLine(line,getAccount(AcctServer
        * .ACCTTYPE_CashExpense, as, conn),line.getC_Currency_ID(conn), "", amount.toString(),
        * conn); log4jDocCash.debug("********** DocCash - factAcct - account - " +
        * getAccountBPartner(false,BPartner, as, conn).C_ValidCombination_ID);
        * fact.createLine(line,getAccountBPartner(false,BPartner, as,
        * conn),line.getC_Currency_ID(conn), amount.negate().toString(), "", Fact_Acct_Group_ID,
        * nextSeqNo(SeqNo), DocumentType, conn); assetAmt = assetAmt.subtract(amount.negate()); } }
        */else if (CashType.equals(DocLine_Cash.CASHTYPE_TRANSFER)) {
        // amount is pos/neg
        // BankInTransit DR dr -- Transfer is in Bank Account Currency
        // CashAsset dr CR
        String temp = C_BankAccount_ID;
        C_BankAccount_ID = line.m_C_BankAccount_ID;
        log4jDocCash.debug("********** DocCash - factAcct - account - "
            + getAccount(AcctServer.ACCTTYPE_BankInTransit, as, conn).C_ValidCombination_ID);
        fact.createLine(line, getAccountBankInTransit(C_BankAccount_ID, as, conn),
            line.getC_Currency_ID(conn), amount.negate().toString(), Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, conn);
        C_BankAccount_ID = temp;
        if (line.getC_Currency_ID(conn) == C_Currency_ID)
          assetAmt = assetAmt.add(amount);
        else
          fact.createLine(null, getAccount(AcctServer.ACCTTYPE_CashAsset, as, conn),
              line.getC_Currency_ID(conn), amount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
      }
    } // lines

    // Cash Asset
    log4jDocCash.debug("********** DocCash - factAcct - account2 - "
        + getAccount(AcctServer.ACCTTYPE_CashAsset, as, conn).C_ValidCombination_ID);
    if (!assetAmt.toString().equals("0"))
      fact.createLine(null, getAccount(AcctServer.ACCTTYPE_CashAsset, as, conn), C_Currency_ID,
          assetAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * Get the account for Accounting Schema
   * 
   * @param AcctType
   *          see ACCTTYPE_*
   * @param as
   *          accounting schema
   * @return Account
   * 
   *         public final Account getAccountCashInTransit(String strcCashlineId, AcctSchema as,
   *         ConnectionProvider conn){ DocCashData [] data=null; try{ data =
   *         DocCashData.selectCashLineAcct(conn, strcCashlineId, as.getC_AcctSchema_ID());
   *         }catch(ServletException e){ log4j.warn(e); } // Get Acct String Account_ID = ""; if
   *         (data != null && data.length!=0){ Account_ID = data[0].accountId; }else return null; //
   *         No account if (Account_ID.equals("")){ log4j.warn(
   *         "DocCash - getAccountCashInTransit - NO account CashLine=" + strcCashlineId +
   *         ", Record=" + Record_ID); return null; } // Return Account Account acct = null; try{
   *         acct = Account.getAccount(conn, Account_ID); }catch(ServletException e){ log4j.warn(e);
   *         } return acct; } // getAccount
   */

  /**
   * Get the account for Accounting Schema
   * 
   * @param strcBankAccountId
   *          Account
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountBankInTransit(String strcBankAccountId, AcctSchema as,
      ConnectionProvider conn) {
    DocCashData[] data = null;
    try {
      data = DocCashData.selectBankInTransitAcct(conn, strcBankAccountId, as.getC_AcctSchema_ID());
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
      log4j.warn("DocCash - getAccountBankInTransit - NO account strcBankAccountId="
          + strcBankAccountId + ", Record=" + Record_ID);
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
    log4jDocCash.debug("DocCash - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocCash.debug("DocCash - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
