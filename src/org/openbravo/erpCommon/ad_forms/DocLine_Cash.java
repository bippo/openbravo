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

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class DocLine_Cash extends DocLine {
  static Logger log4jDocLine_Cash = Logger.getLogger(DocLine_Cash.class);

  public DocLine_Cash(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  /** Cash Type */
  public String m_CashType = "";

  // AD_Reference_ID=217
  public static final String CASHTYPE_GLITEM = "G";
  public static final String CASHTYPE_CHARGE = "C";
  public static final String CASHTYPE_DIFFERENCE = "D";
  public static final String CASHTYPE_EXPENSE = "E";
  public static final String CASHTYPE_INVOICE = "I";
  public static final String CASHTYPE_RECEIPT = "R";
  public static final String CASHTYPE_TRANSFER = "T";
  public static final String CASHTYPE_DEBTPAYMENT = "P";
  public static final String CASHTYPE_ORDER = "O";

  // References
  public String m_C_BankAccount_ID = "";
  public String m_C_Invoice_ID = "";
  // public String m_C_BPartner_ID = "";
  public String m_C_Order_Id = "";
  public String m_C_Debt_Payment_Id = "";

  // Amounts
  public String m_Amount = ZERO.toString();
  public String m_DiscountAmt = ZERO.toString();
  public String m_WriteOffAmt = ZERO.toString();

  public String Line_ID = "";

  /**
   * Set Cash Type
   * 
   * @param CashType
   *          see CASHTYPE_*
   */
  public void setCashType(String CashType) {
    if (CashType != null && !CashType.equals(""))
      m_CashType = CashType;
  } // setCashType

  /**
   * Set References
   */
  public void setReference(String C_Order_ID, String C_Debt_Payment_ID, ConnectionProvider conn) {
    m_C_Order_Id = C_Order_ID;
    m_C_Debt_Payment_Id = C_Debt_Payment_ID;
    setReferenceInfo(conn);
  } // setReference

  /**
   * Get Reference Info - Organization - Currency - BPartner
   */
  private void setReferenceInfo(ConnectionProvider conn) {
    m_C_Currency_ID = "";
    m_AD_Org_ID = "";
    m_C_BPartner_ID = "";
    DocLineCashData[] data = null;
    log4jDocLine_Cash.debug("DocLineCash - setReferenceInfo - BankAccount_id = "
        + m_C_BankAccount_ID + "Invoice_ID = " + m_C_Invoice_ID);
    try {
      // Bank Account Info
      if (!m_C_Debt_Payment_Id.equals("")) {
        data = DocLineCashData.selectPayment(conn, m_C_Debt_Payment_Id);
      } else if (!m_C_Order_Id.equals("")) {
        data = DocLineCashData.selectOrder(conn, m_C_Order_Id);
      } else
        return;
    } catch (ServletException e) {
      log4jDocLine_Cash.warn(e);
    }

    if (data != null && data.length != 0) {
      m_AD_Org_ID = data[0].adOrgId;
      m_C_Currency_ID = data[0].cCurrencyId;
      m_C_BPartner_ID = data[0].cBpartnerId;
    }
    log4jDocLine_Cash.debug("DocLineCash - setReferenceInfo - C_CURRENCY_ID = " + m_C_Currency_ID);
  } // setReferenceInfo

  /**
   * Set Amounts
   * 
   * @param Amount
   *          payment amount
   * @param DiscountAmt
   *          discount
   * @param WriteOffAmt
   *          wrire-off
   */
  public void setAmount(String Amount, String DiscountAmt, String WriteOffAmt) {
    if (!Amount.equals(""))
      m_Amount = Amount;
    if (!DiscountAmt.equals(""))
      m_DiscountAmt = DiscountAmt;
    if (!WriteOffAmt.equals(""))
      m_WriteOffAmt = WriteOffAmt;
    //
    setAmount(Amount);
  } // setAmount

  /**
   * Get Amount
   * 
   * @return Payment Amount
   */
  public String getAmount() {
    return m_Amount;
  }

  /**
   * Get GL Item Account
   * 
   * @param as
   *          account schema
   * @param amount
   *          amount for expense(+)/revenue(-)
   * @return Charge Account or null
   */
  public Account getGlitemAccount(AcctSchema as, BigDecimal amount, ConnectionProvider conn) {
    if (m_C_Glitem_ID.equals(""))
      return null;
    String Account_ID = "";
    DocLineCashData[] data = null;
    Account acct = null;
    try {
      data = DocLineCashData.selectGlitem(conn, m_C_Glitem_ID, as.getC_AcctSchema_ID());
      if (data.length > 0) {
        Account_ID = data[0].glitemDebitAcct;
        if (amount != null && amount.signum() < 0)
          Account_ID = data[0].glitemCreditAcct;
      }
      // No account
      if (Account_ID.equals("")) {
        log4jDocLine.warn("getChargeAccount - NO account for m_C_Glitem_ID=" + m_C_Glitem_ID);
        return null;
      }
      // Return Account
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4jDocLine.warn(e);
    }
    return acct;
  } // getGlitemAccount

  /**
   * Get Currency of invoice or bank
   * 
   * @return C_Currency_ID
   */
  public String getC_Currency_ID(ConnectionProvider conn) {
    if ((m_C_BankAccount_ID == null || m_C_BankAccount_ID.equals(""))
        && (m_C_Invoice_ID == null || m_C_Invoice_ID.equals("")))
      return m_C_Currency_ID;// call to the upper class...

    if (m_C_Currency_ID == null || m_C_Currency_ID.equals(""))
      setReferenceInfo(conn);
    return m_C_Currency_ID;
  } // getC_Currency_ID

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
