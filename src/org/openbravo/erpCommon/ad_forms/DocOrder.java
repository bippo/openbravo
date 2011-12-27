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

public class DocOrder extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocOrder = Logger.getLogger(DocOrder.class);

  /** Contained Optional Tax Lines */
  private DocTax[] m_taxes = null;

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          client
   */
  public DocOrder(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id)
      throws ServletException {
    setObjectFieldProvider(DocOrderData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Specific Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DateDoc = data[0].getField("DateOrdered");
    TaxIncluded = data[0].getField("IsTaxIncluded");

    // Amounts
    Amounts[AcctServer.AMTTYPE_Gross] = data[0].getField("GrandTotal");
    if (Amounts[AcctServer.AMTTYPE_Gross] == null)
      Amounts[AcctServer.AMTTYPE_Gross] = ZERO.toString();
    Amounts[AcctServer.AMTTYPE_Net] = data[0].getField("TotalLines");
    if (Amounts[AcctServer.AMTTYPE_Net] == null)
      Amounts[AcctServer.AMTTYPE_Net] = ZERO.toString();
    Amounts[AcctServer.AMTTYPE_Charge] = data[0].getField("ChargeAmt");
    if (Amounts[AcctServer.AMTTYPE_Charge] == null)
      Amounts[AcctServer.AMTTYPE_Charge] = ZERO.toString();
    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn);
    m_taxes = loadTaxes(conn);
    // Log.trace(Log.l5_DData, "Lines=" + p_lines.length + ", Taxes=" +
    // m_taxes.length);
    return true;
  } // loadDocumentDetails

  /**
   * Load Invoice Line
   * 
   * @return DocLine Array
   */
  public DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineOrderData[] data = null;
    try {
      data = DocLineOrderData.select(conn, Record_ID);

      //
      for (int i = 0; i < data.length; i++) {
        String Line_ID = data[i].getField("cOrderlineId");
        DocLine docLine = new DocLine(DocumentType, Record_ID, Line_ID);
        docLine.loadAttributes(data[i], this);
        String Qty = data[i].getField("qtyordered");
        docLine.setQty(Qty);
        String LineNetAmt = data[i].getField("linenetamt");
        // BigDecimal PriceList = rs.getBigDecimal("PriceList");
        docLine.setAmount(LineNetAmt);
        list.add(docLine);
      }
      //
    } catch (ServletException e) {
      log4jDocOrder.warn(e);
    }

    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Load Invoice Taxes
   * 
   * @return DocTax Array
   */
  public DocTax[] loadTaxes(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();

    DocOrderData[] data = null;
    try {
      data = DocOrderData.select(conn, Record_ID);
      //
      for (int i = 0; i < data.length; i++) {
        String C_Tax_ID = data[i].getField("cTaxId");
        String name = data[i].getField("name");
        String rate = data[i].getField("rate");
        String taxBaseAmt = data[i].getField("taxbaseamt");
        String amount = data[i].getField("taxamt");
        //
        DocTax taxLine = new DocTax(C_Tax_ID, name, rate, taxBaseAmt, amount);
        list.add(taxLine);
      }
    } catch (ServletException e) {
      log4jDocOrder.warn(e);
    }

    // Return Array
    DocTax[] tl = new DocTax[list.size()];
    list.toArray(tl);
    return tl;
  } // loadTaxes

  /**
   * Get Source Currency Balance - subtracts line and tax amounts from total - no rounding
   * 
   * @return positive amount, if total invoice is bigger than lines
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = new BigDecimal("0");
    StringBuffer sb = new StringBuffer(" [");
    // Total
    retValue = retValue.add(new BigDecimal(getAmount(AcctServer.AMTTYPE_Gross)));
    sb.append(getAmount(AcctServer.AMTTYPE_Gross));
    // - Charge
    retValue = retValue.subtract(new BigDecimal(getAmount(AcctServer.AMTTYPE_Charge)));
    sb.append("-").append(getAmount(AcctServer.AMTTYPE_Charge));
    // - Tax
    if (m_taxes != null) {
      for (int i = 0; i < m_taxes.length; i++) {
        retValue = retValue.subtract(new BigDecimal(m_taxes[i].getAmount()));
        sb.append("-").append(m_taxes[i].getAmount());
      }
    }
    // - Lines
    if (p_lines != null) {
      for (int i = 0; i < p_lines.length; i++) {
        retValue = retValue.subtract(new BigDecimal(p_lines[i].getAmount()));
        sb.append("-").append(p_lines[i].getAmount());
      }
      sb.append("]");
    }
    //
    log4jDocOrder.debug(" Balance=" + retValue + sb.toString());
    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for SOO, POO, POR.
   * 
   * <pre>
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
        DocOrderTemplate newTemplate = (DocOrderTemplate) Class.forName(strClassname).newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocOrderTemplate - " + e);
      }
    }
    // Purchase Order
    if (DocumentType.equals(AcctServer.DOCTYPE_POrder))
      updateProductInfo(as.getC_AcctSchema_ID(), conn, con);

    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    return fact;
  } // createFact

  /**
   * @return the log4jDocOrder
   */
  public static Logger getLog4jDocOrder() {
    return log4jDocOrder;
  }

  /**
   * @param log4jDocOrder
   *          the log4jDocOrder to set
   */
  public static void setLog4jDocOrder(Logger log4jDocOrder) {
    DocOrder.log4jDocOrder = log4jDocOrder;
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
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  /**
   * Update Product Info. - Costing (PriceLastPO) - PO (PriceLastPO)
   * 
   * @param C_AcctSchema_ID
   *          accounting schema
   */
  private void updateProductInfo(String C_AcctSchema_ID, ConnectionProvider conn, Connection con) {
    log4jDocOrder.debug("updateProductInfo - C_Order_ID=" + Record_ID);

    /**
     * @todo Last.. would need to compare document/last updated date would need to maintain
     *       LastPriceUpdateDate on _PO and _Costing
     */

    try {
      // update Product PO info
      // should only be once, but here for every AcctSchema
      // ignores multiple lines with same product - just uses first
      int no = DocOrderData.updateProductPO(con, conn, Record_ID);
      log4jDocOrder.debug("M_Product_PO - Updated=" + no);

    } catch (ServletException e) {
      log4jDocOrder.warn(e);
    }

  } // updateProductInfo

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
