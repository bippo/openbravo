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
 * All portions are Copyright (C) 2008-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
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

public class DocProduction extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocProduction = Logger.getLogger(DocProduction.class);

  private String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocProduction(String AD_Client_ID, String AD_Org_ID, ConnectionProvider conn) {
    super(AD_Client_ID, AD_Org_ID, conn);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocProductionData.selectRegistro(conn, stradClientId, Id));
  }

  /**
   * Load Specific Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_MatProduction;
    C_Currency_ID = NO_CURRENCY;
    DateDoc = data[0].getField("Movementdate");
    DateAcct = data[0].getField("Movementdate");
    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn);
    log4jDocProduction.debug("Record_ID = " + Record_ID + " - Lines=" + p_lines.length);
    return false;
  } // loadDocumentDetails

  /**
   * Load Lines.
   * 
   * @return DocLine Array
   */
  private DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineProductionData[] data = null;

    try {
      data = DocLineProductionData.select(conn, Record_ID);
      log4jDocProduction.debug("LoadLines: data.len" + data.length + " record_ID " + Record_ID);
    } catch (ServletException e) {
      log4jDocProduction.warn(e);
    }

    //
    for (int i = 0; data != null && i < data.length; i++) {
      String Line_ID = data[i].getField("M_PRODUCTIONLINE_ID");
      DocLine_Material docLine = new DocLine_Material(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.setQty(data[i].getField("MOVEMENTQTY"), conn); // sets Trx
      // and
      // Storage
      // Qty
      docLine.m_Productiontype = data[i].getField("PRODUCTIONTYPE");
      docLine.m_M_Warehouse_ID = data[i].getField("M_WAREHOUSE_ID");
      list.add(docLine);
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
   * Create Facts (the accounting logic) for
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
        DocProductionTemplate newTemplate = (DocProductionTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4jDocProduction.error("Error while creating new instance for DocProductionTemplate - "
            + e);
      }
    }
    log4jDocProduction.debug("createFact - Inicio");
    // create Fact Header
    Fact fact = null;
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    log4jDocProduction.debug("createFact - object created");
    String costCurrencyId = as.getC_Currency_ID();
    OBContext.setAdminMode(false);
    try {
      costCurrencyId = OBDal.getInstance().get(Client.class, AD_Client_ID).getCurrency().getId();
    } finally {
      OBContext.restorePreviousMode();
    }
    // Lines
    fact = new Fact(this, as, Fact.POST_Actual);
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_Material line = (DocLine_Material) p_lines[i];
      String costs = line.getProductCosts(DateAcct, as, conn, con);
      BigDecimal dCosts = new BigDecimal(costs);
      if (BigDecimal.ZERO.compareTo(dCosts) == 0
          && DocInOutData.existsCost(conn, DateAcct, line.m_M_Product_ID).equals("0")) {
        Map<String, String> parameters = getInvalidCostParameters(
            OBDal.getInstance().get(Product.class, line.m_M_Product_ID).getIdentifier(), DateAcct);
        setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
        throw new IllegalStateException();
      }
      log4jDocProduction.debug("DocProduction - createFact - line.m_Productiontype - "
          + line.m_Productiontype);
      if (line.m_Productiontype.equals("+")) {
        fact.createLine(line, line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            costCurrencyId, costs, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        fact.createLine(line, getAccountWarehouse(line.m_M_Warehouse_ID, as, conn), costCurrencyId,
            "", costs, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      } else {
        fact.createLine(line, line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            costCurrencyId, "", costs, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        fact.createLine(line, getAccountWarehouse(line.m_M_Warehouse_ID, as, conn), costCurrencyId,
            costs, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      }
    }
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * @return the log4j
   */
  public static Logger getLog4j() {
    return log4jDocProduction;
  }

  /**
   * @param log4j
   *          the log4j to set
   */
  public static void setLog4j(Logger log4j) {
    DocProduction.log4jDocProduction = log4j;
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
    log4jDocProduction.debug("DocAmortization - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocProduction.debug("DocAmortization - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get the account for Accounting Schema
   * 
   * @param strmWarehouseId
   *          warehouse
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountWarehouse(String strmWarehouseId, AcctSchema as,
      ConnectionProvider conn) throws ServletException {
    AcctServerData[] data = null;
    data = AcctServerData.selectWDifferencesAcct(conn, strmWarehouseId, as.getC_AcctSchema_ID());
    // Get Acct
    String Account_ID = "";
    if (data != null && data.length != 0) {
      Account_ID = data[0].accountId;
    } else
      return null;
    // No account
    if (Account_ID.equals("")) {
      return null;
    }
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - *******************************getAccount 4");
    // Return Account
    Account acct = null;
    try {
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4jDocProduction.warn(e);
    }
    return acct;
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
