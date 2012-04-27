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
 * Contributions are Copyright (C) 2001-2012 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.plm.Product;

public class DocMovement extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocMovement = Logger.getLogger(DocMovement.class);

  private String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          client
   */
  public DocMovement(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocMovementData.select(conn, stradClientId, Id));
  }

  /**
   * Load Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_MatMovement;
    C_Currency_ID = NO_CURRENCY;
    DateDoc = data[0].getField("MovementDate");

    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn);
    log4jDocMovement.debug("Lines=" + p_lines.length);
    return true;
  } // loadDocumentDetails

  /**
   * Load Invoice Line
   * 
   * @return document lines (DocLine_Material)
   */
  public DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineMovementData[] data = null;
    try {
      data = DocLineMovementData.select(conn, Record_ID);
      for (int i = 0; i < data.length; i++) {
        String Line_ID = data[i].getField("mMovementlineId");
        DocLine_Material docLine = new DocLine_Material(DocumentType, Record_ID, Line_ID);
        docLine.loadAttributes(data[i], this);
        docLine.setQty(data[i].getField("MovementQty"), conn);
        docLine.m_M_Locator_ID = data[i].getField("M_Locator_ID");
        docLine.m_M_LocatorTo_ID = data[i].getField("M_LocatorTo_ID");
        //
        log4jDocMovement.debug("Movement line: " + Line_ID + " loaded.");
        list.add(docLine);
      }
    } catch (ServletException e) {
      log4jDocMovement.warn(e);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Balance
   * 
   * @return balance (ZERO) - always balanced
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for MMM.
   * 
   * <pre>
   *  Movement
   *      Inventory       DR      CR
   *      InventoryTo     DR      CR
   * </pre>
   * 
   * @param as
   *          account schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    C_Currency_ID = as.getC_Currency_ID();
    // Select specific definition
    String strClassname = AcctServerData
        .selectTemplateDoc(conn, as.m_C_AcctSchema_ID, DocumentType);
    if (strClassname.equals(""))
      strClassname = AcctServerData.selectTemplate(conn, as.m_C_AcctSchema_ID, AD_Table_ID);
    if (!strClassname.equals("")) {
      try {
        DocMovementTemplate newTemplate = (DocMovementTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocInvoiceTemplate - " + e);
      }
    }
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();

    // Line pointers
    FactLine dr = null;
    FactLine cr = null;
    log4jDocMovement.debug("DocMovement - Before the loop");
    String costCurrencyId = as.getC_Currency_ID();
    OBContext.setAdminMode(false);
    try {
      costCurrencyId = OBDal.getInstance().get(Client.class, AD_Client_ID).getCurrency().getId();
    } finally {
      OBContext.restorePreviousMode();
    }
    for (int i = 0; i < p_lines.length; i++) {
      DocLine_Material line = (DocLine_Material) p_lines[i];
      log4jDocMovement.debug("DocMovement - Before calculating the costs for line i = " + i);
      String costs = line.getProductCosts(DateAcct, as, conn, con);
      BigDecimal b_Costs = new BigDecimal(costs);
      if (b_Costs.compareTo(BigDecimal.ZERO) == 0
          && DocInOutData.existsCost(conn, DateAcct, line.m_M_Product_ID).equals("0")) {
        Map<String, String> parameters = getInvalidCostParameters(
            OBDal.getInstance().get(Product.class, line.m_M_Product_ID).getIdentifier(), DateAcct);
        setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
        throw new IllegalStateException();
      }
      // Inventory DR CR
      dr = fact.createLine(line, line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
          costCurrencyId, (b_Costs.negate()).toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn); // from
      // (-)
      // CR
      dr.setM_Locator_ID(line.m_M_Locator_ID);
      // InventoryTo DR CR
      cr = fact.createLine(line, line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
          costCurrencyId, costs, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn); // to
      // (+)
      // DR
      cr.setM_Locator_ID(line.m_M_LocatorTo_ID);
    }
    log4jDocMovement.debug("DocMovement - After the loop");
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * @return the log4jDocMovement
   */
  public static Logger getLog4jDocMovement() {
    return log4jDocMovement;
  }

  /**
   * @param log4jDocMovement
   *          the log4jDocMovement to set
   */
  public static void setLog4jDocMovement(Logger log4jDocMovement) {
    DocMovement.log4jDocMovement = log4jDocMovement;
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

  public String nextSeqNo(String oldSeqNo) {
    log4jDocMovement.debug("DocMovement - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocMovement.debug("DocMovement - nextSeqNo = " + SeqNo);
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
    return "Servlet for accounting";
  } // end of getServletInfo() method
}
