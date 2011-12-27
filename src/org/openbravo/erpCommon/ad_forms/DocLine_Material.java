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

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class DocLine_Material extends DocLine {
  static Logger log4jDocLine_Material = Logger.getLogger(DocLine_Material.class);

  /**
   * Constructor
   * 
   * @param DocumentType
   *          document type
   * @param TrxHeader_ID
   *          trx header id
   * @param TrxLine_ID
   *          trx line id
   */
  public DocLine_Material(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  /** Locator */
  public String m_M_Locator_ID = "";
  public String m_M_LocatorTo_ID = "";
  public String m_M_Warehouse_ID = "";
  /** Production */
  public String m_Productiontype = "";

  /**
   * Set Trasaction Quantity and Storage Qty
   * 
   * @param qty
   *          qty
   */
  public void setQty(String qty, ConnectionProvider conn) {
    log4jDocLine_Material.debug(" setQty - qty= " + qty);
    super.setQty(qty); // save TrxQty
    p_productInfo.setQty(qty, p_productInfo.m_C_UOM_ID, conn);
    log4jDocLine_Material.debug(" setQty - productInfo.qty = " + p_productInfo.m_qty);
  } // setQty

  /**
   * Get Total Product Costs
   * 
   * @param as
   *          accounting schema
   * @return costs
   */
  public String getProductCosts(String date, AcctSchema as, ConnectionProvider conn, Connection con) {
    return p_productInfo.getProductCosts(date, "", as, conn, con);
  } // getProductCosts

  /**
   * Line Account from Product
   * 
   * @param AcctType
   *          see ProoductInfo.ACCTTYPE_* (0..3)
   * @param as
   *          accounting schema
   * @return Requested Product Account
   */
  public Account getAccount(String AcctType, AcctSchema as, ConnectionProvider conn) {
    return p_productInfo.getAccount(AcctType, as, conn);
  } // getAccount

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
