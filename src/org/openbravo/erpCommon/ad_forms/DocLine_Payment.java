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

import org.apache.log4j.Logger;

public class DocLine_Payment extends DocLine {
  static Logger log4jDocLine_Payment = Logger.getLogger(DocLine_Payment.class);

  String Line_ID = "";
  String Amount = "";
  String WriteOffAmt = "";
  String isManual = "";
  String isReceipt = "";
  String isPaid = "";
  String C_Settlement_Cancel_ID = "";
  String C_Settlement_Generate_ID = "";
  String C_GLItem_ID = "";
  String IsDirectPosting = "";
  String dpStatus = "";
  String C_Currency_ID_From;
  String conversionDate;
  String C_INVOICE_ID = "";
  String C_BPARTNER_ID = "";
  String C_WITHHOLDING_ID = "";

  /**
   * @return the isReceipt
   */
  public String getIsReceipt() {
    return isReceipt;
  }

  /**
   * @return the amount
   */
  public String getAmount() {
    return Amount;
  }

  /**
   * @return the log4jDocLine_Payment
   */
  public static Logger getLog4jDocLine_Payment() {
    return log4jDocLine_Payment;
  }

  /**
   * @param log4jDocLine_Payment
   *          the log4jDocLine_Payment to set
   */
  public static void setLog4jDocLine_Payment(Logger log4jDocLine_Payment) {
    DocLine_Payment.log4jDocLine_Payment = log4jDocLine_Payment;
  }

  /**
   * @return the line_ID
   */
  public String getLine_ID() {
    return Line_ID;
  }

  /**
   * @param line_ID
   *          the line_ID to set
   */
  public void setLine_ID(String line_ID) {
    Line_ID = line_ID;
  }

  /**
   * @return the writeOffAmt
   */
  public String getWriteOffAmt() {
    return WriteOffAmt;
  }

  /**
   * @param writeOffAmt
   *          the writeOffAmt to set
   */
  public void setWriteOffAmt(String writeOffAmt) {
    WriteOffAmt = writeOffAmt;
  }

  /**
   * @return the isManual
   */
  public String getIsManual() {
    return isManual;
  }

  /**
   * @param isManual
   *          the isManual to set
   */
  public void setIsManual(String isManual) {
    this.isManual = isManual;
  }

  /**
   * @return the isPaid
   */
  public String getIsPaid() {
    return isPaid;
  }

  /**
   * @param isPaid
   *          the isPaid to set
   */
  public void setIsPaid(String isPaid) {
    this.isPaid = isPaid;
  }

  /**
   * @return the c_Settlement_Cancel_ID
   */
  public String getC_Settlement_Cancel_ID() {
    return C_Settlement_Cancel_ID;
  }

  /**
   * @param settlement_Cancel_ID
   *          the c_Settlement_Cancel_ID to set
   */
  public void setC_Settlement_Cancel_ID(String settlement_Cancel_ID) {
    C_Settlement_Cancel_ID = settlement_Cancel_ID;
  }

  /**
   * @return the c_Settlement_Generate_ID
   */
  public String getC_Settlement_Generate_ID() {
    return C_Settlement_Generate_ID;
  }

  /**
   * @param settlement_Generate_ID
   *          the c_Settlement_Generate_ID to set
   */
  public void setC_Settlement_Generate_ID(String settlement_Generate_ID) {
    C_Settlement_Generate_ID = settlement_Generate_ID;
  }

  /**
   * @return the c_GLItem_ID
   */
  public String getC_GLItem_ID() {
    return C_GLItem_ID;
  }

  /**
   * @param item_ID
   *          the c_GLItem_ID to set
   */
  public void setC_GLItem_ID(String item_ID) {
    C_GLItem_ID = item_ID;
  }

  /**
   * @return the isDirectPosting
   */
  public String getIsDirectPosting() {
    return IsDirectPosting;
  }

  /**
   * @param isDirectPosting
   *          the isDirectPosting to set
   */
  public void setIsDirectPosting(String isDirectPosting) {
    IsDirectPosting = isDirectPosting;
  }

  /**
   * @return the dpStatus
   */
  public String getDpStatus() {
    return dpStatus;
  }

  /**
   * @param dpStatus
   *          the dpStatus to set
   */
  public void setDpStatus(String dpStatus) {
    this.dpStatus = dpStatus;
  }

  /**
   * @return the conversionDate
   */
  public String getConversionDate() {
    return conversionDate;
  }

  /**
   * @param conversionDate
   *          the conversionDate to set
   */
  public void setConversionDate(String conversionDate) {
    this.conversionDate = conversionDate;
  }

  /**
   * @return the c_INVOICE_ID
   */
  public String getC_INVOICE_ID() {
    return C_INVOICE_ID;
  }

  /**
   * @param c_invoice_id
   *          the c_INVOICE_ID to set
   */
  public void setC_INVOICE_ID(String c_invoice_id) {
    C_INVOICE_ID = c_invoice_id;
  }

  /**
   * @return the c_BPARTNER_ID
   */
  public String getC_BPARTNER_ID() {
    return C_BPARTNER_ID;
  }

  /**
   * @param c_bpartner_id
   *          the c_BPARTNER_ID to set
   */
  public void setC_BPARTNER_ID(String c_bpartner_id) {
    C_BPARTNER_ID = c_bpartner_id;
  }

  /**
   * @return the c_WITHHOLDING_ID
   */
  public String getC_WITHHOLDING_ID() {
    return C_WITHHOLDING_ID;
  }

  /**
   * @param c_withholding_id
   *          the c_WITHHOLDING_ID to set
   */
  public void setC_WITHHOLDING_ID(String c_withholding_id) {
    C_WITHHOLDING_ID = c_withholding_id;
  }

  /**
   * @return the withHoldAmt
   */
  public String getWithHoldAmt() {
    return WithHoldAmt;
  }

  /**
   * @param withHoldAmt
   *          the withHoldAmt to set
   */
  public void setWithHoldAmt(String withHoldAmt) {
    WithHoldAmt = withHoldAmt;
  }

  /**
   * @return the c_BANKACCOUNT_ID
   */
  public String getC_BANKACCOUNT_ID() {
    return C_BANKACCOUNT_ID;
  }

  /**
   * @param c_bankaccount_id
   *          the c_BANKACCOUNT_ID to set
   */
  public void setC_BANKACCOUNT_ID(String c_bankaccount_id) {
    C_BANKACCOUNT_ID = c_bankaccount_id;
  }

  /**
   * @return the c_BANKSTATEMENTLINE_ID
   */
  public String getC_BANKSTATEMENTLINE_ID() {
    return C_BANKSTATEMENTLINE_ID;
  }

  /**
   * @param c_bankstatementline_id
   *          the c_BANKSTATEMENTLINE_ID to set
   */
  public void setC_BANKSTATEMENTLINE_ID(String c_bankstatementline_id) {
    C_BANKSTATEMENTLINE_ID = c_bankstatementline_id;
  }

  /**
   * @return the c_CASHBOOK_ID
   */
  public String getC_CASHBOOK_ID() {
    return C_CASHBOOK_ID;
  }

  /**
   * @param c_cashbook_id
   *          the c_CASHBOOK_ID to set
   */
  public void setC_CASHBOOK_ID(String c_cashbook_id) {
    C_CASHBOOK_ID = c_cashbook_id;
  }

  /**
   * @return the c_CASHLINE_ID
   */
  public String getC_CASHLINE_ID() {
    return C_CASHLINE_ID;
  }

  /**
   * @param c_cashline_id
   *          the c_CASHLINE_ID to set
   */
  public void setC_CASHLINE_ID(String c_cashline_id) {
    C_CASHLINE_ID = c_cashline_id;
  }

  /**
   * @param amount
   *          the amount to set
   */
  public void setAmount(String amount) {
    Amount = amount;
  }

  /**
   * @return the c_Currency_ID_From
   */
  public String getC_Currency_ID_From() {
    return C_Currency_ID_From;
  }

  /**
   * @param currency_ID_From
   *          the c_Currency_ID_From to set
   */
  public void setC_Currency_ID_From(String currency_ID_From) {
    C_Currency_ID_From = currency_ID_From;
  }

  /**
   * @param isReceipt
   *          the isReceipt to set
   */
  public void setIsReceipt(String isReceipt) {
    this.isReceipt = isReceipt;
  }

  String WithHoldAmt = "";
  String C_BANKACCOUNT_ID = "";
  String C_BANKSTATEMENTLINE_ID = "";
  String C_CASHBOOK_ID = "";
  String C_CASHLINE_ID = "";

  public DocLine_Payment(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
    Line_ID = TrxLine_ID;
    m_Record_Id2 = Line_ID;
  }

  /**
   * Clone DocLine (DocLine)
   * 
   * @param docLine
   */
  public static DocLine_Payment clone(DocLine_Payment docLine) {
    DocLine_Payment lineAux = new DocLine_Payment(docLine.p_DocumentType, docLine.m_TrxHeader_ID,
        docLine.m_TrxLine_ID);
    lineAux.m_Record_Id2 = docLine.m_Record_Id2;
    lineAux.m_AD_Org_ID = docLine.m_AD_Org_ID;
    lineAux.m_C_BPartner_ID = docLine.m_C_BPartner_ID;
    lineAux.m_M_Product_ID = docLine.m_M_Product_ID;
    lineAux.m_AD_OrgTrx_ID = docLine.m_AD_OrgTrx_ID;
    lineAux.m_C_SalesRegion_ID = docLine.m_C_SalesRegion_ID;
    lineAux.m_C_Project_ID = docLine.m_C_Project_ID;
    lineAux.m_A_Asset_ID = docLine.m_A_Asset_ID;
    lineAux.m_C_Campaign_ID = docLine.m_C_Campaign_ID;
    lineAux.m_C_Activity_ID = docLine.m_C_Activity_ID;
    lineAux.m_C_LocFrom_ID = docLine.m_C_LocFrom_ID;
    lineAux.m_C_LocTo_ID = docLine.m_C_LocTo_ID;
    lineAux.m_User1_ID = docLine.m_User1_ID;
    lineAux.m_User2_ID = docLine.m_User2_ID;
    // Line, Description, Currency
    lineAux.m_Line = docLine.m_Line;
    lineAux.m_description = docLine.m_description;
    lineAux.m_C_Currency_ID = docLine.m_C_Currency_ID;
    // Qty
    lineAux.m_C_UOM_ID = docLine.m_C_UOM_ID;
    lineAux.m_qty = docLine.m_qty;
    //
    lineAux.m_C_Tax_ID = docLine.m_C_Tax_ID;
    lineAux.m_C_WithHolding_ID = docLine.m_C_WithHolding_ID;
    lineAux.m_C_Charge_ID = docLine.m_C_Charge_ID;
    lineAux.m_ChargeAmt = docLine.m_ChargeAmt;
    //
    lineAux.m_DateAcct = docLine.m_DateAcct;
    lineAux.m_DateDoc = docLine.m_DateDoc;
    //
    lineAux.m_AmtSourceDr = docLine.m_AmtSourceDr;
    lineAux.m_AmtSourceCr = docLine.m_AmtSourceCr;
    //
    lineAux.m_AmtAcctDr = docLine.m_AmtAcctDr;
    lineAux.m_AmtAcctCr = docLine.m_AmtAcctCr;
    //
    lineAux.m_C_AcctSchema_ID = docLine.m_C_AcctSchema_ID;
    //
    lineAux.m_account = docLine.m_account;
    //
    // Product Info
    lineAux.p_productInfo = docLine.p_productInfo;

    lineAux.Line_ID = docLine.Line_ID;
    lineAux.Amount = docLine.Amount;
    lineAux.WriteOffAmt = docLine.WriteOffAmt;
    lineAux.isManual = docLine.isManual;
    lineAux.isReceipt = docLine.isReceipt;
    lineAux.isPaid = docLine.isPaid;
    lineAux.C_Settlement_Cancel_ID = docLine.C_Settlement_Cancel_ID;
    lineAux.C_Settlement_Generate_ID = docLine.C_Settlement_Generate_ID;
    lineAux.C_GLItem_ID = docLine.C_GLItem_ID;
    lineAux.IsDirectPosting = docLine.IsDirectPosting;
    lineAux.dpStatus = docLine.dpStatus;
    lineAux.C_Currency_ID_From = docLine.C_Currency_ID_From;
    lineAux.conversionDate = docLine.conversionDate;
    lineAux.C_INVOICE_ID = docLine.C_INVOICE_ID;
    lineAux.C_BPARTNER_ID = docLine.C_BPARTNER_ID;
    lineAux.C_WITHHOLDING_ID = docLine.C_WITHHOLDING_ID;

    return lineAux;
  } // clone

  public String getServletInfo() {
    return "Servlet for accounting";
  } // end of getServletInfo() method
}
