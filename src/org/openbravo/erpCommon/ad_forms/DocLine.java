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
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;

public class DocLine {
  static Logger log4jDocLine = Logger.getLogger(DocLine.class);

  public BigDecimal ZERO = new BigDecimal("0");

  public String p_DocumentType = "";
  /** ID - Trx Header */
  public String m_TrxHeader_ID = "";
  /** ID - Line ID */
  public String m_TrxLine_ID = "";

  public String getM_C_Tax_ID() {
    return m_C_Tax_ID;
  }

  public void setM_C_Tax_ID(String tax_ID) {
    m_C_Tax_ID = tax_ID;
  }

  /** Line */
  public String m_Line = "";

  /** Qty UOM */
  public String m_C_UOM_ID = "";
  /** Qty */
  public String m_qty = "";

  /** Currency */
  public String m_C_Currency_ID = "";

  // -- GL Amounts
  /** Debit Journal Amt */
  public String m_AmtSourceDr = "0";
  /** Credit Journal Amt */
  public String m_AmtSourceCr = "0";

  /** Converted Amounts */
  public String m_AmtAcctDr = "";
  public String m_AmtAcctCr = "";
  public String m_C_AcctSchema_ID = "";

  public ProductInfo p_productInfo = null;

  /** Account used only for GL Journal */
  public Account m_account = null;

  // Dimensions
  public String m_AD_Org_ID = "";
  public String m_A_Asset_ID = "";
  public String m_C_BPartner_ID = "";
  public String m_M_Product_ID = "";
  public String m_C_Glitem_ID = "";
  public String m_AD_OrgTrx_ID = "";
  public String m_C_SalesRegion_ID = "";
  public String m_C_Project_ID = "";
  public String m_C_Campaign_ID = "";
  public String m_C_Activity_ID = "";
  public String m_C_LocFrom_ID = "";
  public String m_C_LocTo_ID = "";
  public String m_User1_ID = "";
  public String m_User2_ID = "";
  //
  public String m_C_Charge_ID = "";
  public String m_ChargeAmt = "";
  /** Description */
  public String m_description = "";
  /** Tax ID */
  public String m_C_Tax_ID = "";
  /** WithHolding ID */
  public String m_C_WithHolding_ID = "";

  public String m_DateAcct = "";
  public String m_DateDoc = "";
  public String m_Record_Id2 = "";

  public DocLine(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    if (DocumentType == null)
      throw new IllegalArgumentException("DocLine - DocumentType is null");
    p_DocumentType = DocumentType;
    m_TrxHeader_ID = TrxHeader_ID;
    m_TrxLine_ID = TrxLine_ID;
  } // DocLine

  public void loadAttributes(FieldProvider data, AcctServer vo) {
    m_Record_Id2 = data.getField("cDebtpaymentId");
    m_AD_Org_ID = data.getField("adOrgId");
    m_C_BPartner_ID = data.getField("cBpartnerId");
    m_M_Product_ID = data.getField("mProductId");
    m_C_Glitem_ID = data.getField("mCGlitemId");
    m_AD_OrgTrx_ID = data.getField("adOrgtrxId");
    m_C_SalesRegion_ID = data.getField("cSalesregionId");
    m_C_Project_ID = data.getField("cProjectId");
    m_A_Asset_ID = data.getField("aAssetId");
    m_C_Campaign_ID = data.getField("cCampaignId");
    m_C_Activity_ID = data.getField("cActivityId");
    m_C_LocFrom_ID = data.getField("cLocfromId");
    m_C_LocTo_ID = data.getField("cLoctoId");
    m_User1_ID = data.getField("user1Id");
    m_User2_ID = data.getField("user2Id");
    // Line, Description, Currency
    m_Line = data.getField("line");
    m_description = data.getField("description");
    m_C_Currency_ID = data.getField("cCurrencyId");
    log4jDocLine.debug("loadAttributes - C_CURRENCY_ID = " + m_C_Currency_ID);
    // Qty
    m_C_UOM_ID = data.getField("cUomId");
    m_qty = data.getField("qty");
    //
    m_C_Tax_ID = data.getField("cTaxId");
    m_C_WithHolding_ID = data.getField("cWithholdingId");
    m_C_Charge_ID = data.getField("cChargeId");
    m_ChargeAmt = data.getField("chargeamt");
    //
    m_DateAcct = data.getField("dateacct");
    m_DateDoc = data.getField("datedoc");
    //
    // Product Info
    p_productInfo = new ProductInfo(m_M_Product_ID, vo.getConnectionProvider());

    // Document Consistency
    if (m_AD_Org_ID != null && m_AD_Org_ID.equals(""))
      m_AD_Org_ID = vo.AD_Org_ID;
    if (m_C_Currency_ID != null && m_C_Currency_ID.equals(""))
      m_C_Currency_ID = vo.C_Currency_ID;
  } // loadAttributes

  public String getM_C_WithHolding_ID() {
    return m_C_WithHolding_ID;
  }

  public void setM_C_WithHolding_ID(String withHolding_ID) {
    m_C_WithHolding_ID = withHolding_ID;
  }

  public void setAmount(String amtSourceDr, String amtSourceCr) {
    m_AmtSourceDr = (amtSourceDr == null || amtSourceDr.equals("")) ? "0" : amtSourceDr;
    m_AmtSourceCr = (amtSourceCr == null || amtSourceCr.equals("")) ? "0" : amtSourceCr;
  } // setAmounts

  /**
   * Set Converted Amounts
   * 
   * @param C_AcctSchema_ID
   *          acct schema
   * @param amtAcctDr
   *          acct amount dr
   * @param amtAcctCr
   *          acct amount cr
   */
  public void setConvertedAmt(String C_AcctSchema_ID, String amtAcctDr, String amtAcctCr) {
    m_C_AcctSchema_ID = C_AcctSchema_ID;
    m_AmtAcctDr = amtAcctDr;
    m_AmtAcctCr = amtAcctCr;
  } // setConvertedAmt

  /**
   * Set Journal Account
   * 
   * @param acct
   *          account
   */
  public void setAccount(Account acct) {
    m_account = acct;
  } // setAccount

  /**
   * Set AD_Org_ID. For GL Journal overwrite
   * 
   * @param AD_Org_ID
   *          org
   */
  public void setAD_Org_ID(String AD_Org_ID) {
    m_AD_Org_ID = AD_Org_ID;
  } // setAD_Org_ID

  /**
   * Get Charge Account
   * 
   * @param as
   *          account schema
   * @param amount
   *          amount for expense(+)/revenue(-)
   * @return Charge Account or null
   */
  public Account getChargeAccount(AcctSchema as, BigDecimal amount, ConnectionProvider conn) {
    if (m_C_Charge_ID.equals(""))
      return null;
    String Account_ID = "";
    DocLineData[] data = null;
    Account acct = null;
    try {
      data = DocLineData.select(conn, m_C_Charge_ID, as.getC_AcctSchema_ID());
      if (data.length > 0) {
        Account_ID = data[0].expense; // Expense (positive amt)
        if (amount != null && amount.signum() < 0)
          Account_ID = data[0].revenue; // Revenue (negative amt)
      }
      // No account
      if (Account_ID.equals("")) {
        log4jDocLine.warn("getChargeAccount - NO account for C_Charge_ID=" + m_C_Charge_ID);
        return null;
      }
      // Return Account
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4jDocLine.warn(e);
    }
    return acct;
  } // getChargeAccount

  /**
   * Get (Journal) AcctSchema
   * 
   * @return C_AcctSchema_ID
   */
  public String getC_AcctSchema_ID() {
    return m_C_AcctSchema_ID;
  } // getC_AcctSchema_ID

  /**
   * Get Journal Account
   * 
   * @return account
   */
  public Account getAccount() {
    return m_account;
  } // getAccount

  /**
   * Get (Journal) Line Source Dr Amount
   * 
   * @return DR source amount
   */
  public String getAmtSourceDr() {
    return m_AmtSourceDr;
  } // getAmtSourceDr

  /**
   * Get (Journal) Line Source Cr Amount
   * 
   * @return CR source amount
   */
  public String getAmtSourceCr() {
    return m_AmtSourceCr;
  } // getAmtSourceCr

  /**
   * Quantity
   * 
   * @param qty
   *          transaction Qty
   */
  public void setQty(String qty) {
    m_qty = qty.equals("") ? "0" : qty;
  } // getQty

  /**
   * Line Net Amount or Dr-Cr
   * 
   * @return balance
   */
  public String getAmount() {
    BigDecimal AmtSourceDr = new BigDecimal(m_AmtSourceDr);
    BigDecimal AmtSourceCr = new BigDecimal(m_AmtSourceCr);
    return AmtSourceDr.subtract(AmtSourceCr).toString();
  } // getAmount

  /**
   * Set Amount (DR)
   * 
   * @param sourceAmt
   *          source amt
   */
  public void setAmount(String sourceAmt) {
    m_AmtSourceDr = (sourceAmt == null || sourceAmt.equals("")) ? ZERO.toString() : sourceAmt;
    m_AmtSourceCr = ZERO.toString();
  } // setAmounts

  /**
   * @return the m_description
   */
  public String getM_description() {
    return m_description;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
