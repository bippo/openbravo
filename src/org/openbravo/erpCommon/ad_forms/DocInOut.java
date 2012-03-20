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
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;

public class DocInOut extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInOut = Logger.getLogger(DocInOut.class);

  /** AD_Table_ID */
  private String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocInOut(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocInOutData.selectRegistro(conn, stradClientId, Id));
  }

  /**
   * Load Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    C_Currency_ID = NO_CURRENCY;
    log4jDocInOut.debug("loadDocumentDetails - C_Currency_ID : " + C_Currency_ID);
    DateDoc = data[0].getField("MovementDate");
    C_BPartner_Location_ID = data[0].getField("C_BPartner_Location_ID");

    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn);
    log4jDocInOut.debug("Lines=" + p_lines.length);
    return true;
  } // loadDocumentDetails

  /**
   * Load Invoice Line
   * 
   * @return DocLine Array
   */
  public DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineInOutData[] data = null;
    try {
      data = DocLineInOutData.select(conn, Record_ID);
    } catch (ServletException e) {
      log4jDocInOut.warn(e);
    }
    //
    for (int i = 0; data != null && i < data.length; i++) {
      String Line_ID = data[i].getField("M_INOUTLINE_ID");
      DocLine_Material docLine = new DocLine_Material(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.setQty(data[i].getField("MOVEMENTQTY"), conn); // sets Trx
      // and
      // Storage
      // Qty
      docLine.m_M_Locator_ID = data[i].getField("M_LOCATOR_ID");
      //
      if (docLine.m_M_Product_ID.equals(""))
        log4jDocInOut.debug(" - No Product - ignored");
      else
        list.add(docLine);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Balance
   * 
   * @return Zero (always balanced)
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for MMS, MMR.
   * 
   * <pre>
   *  Shipment
   *      CoGS            DR
   *      Inventory               CR
   *  Shipment of Project Issue
   *      CoGS            DR
   *      Project                 CR
   *  Receipt
   *      Inventory       DR
   *      NotInvoicedReceipt      CR
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
        DocInOutTemplate newTemplate = (DocInOutTemplate) Class.forName(strClassname).newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocInOutTemplate - " + e);
      }
    }
    C_Currency_ID = as.getC_Currency_ID();
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    // Line pointers
    FactLine dr = null;
    FactLine cr = null;
    String strScale = DocInOutData.selectClientCurrencyPrecission(conn, vars.getClient());

    // Sales
    if (DocumentType.equals(AcctServer.DOCTYPE_MatShipment)) {
      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        DocLine_Material line = (DocLine_Material) p_lines[i];
        Account cogsAccount = line.getAccount(ProductInfo.ACCTTYPE_P_Cogs, as, conn);
        if (cogsAccount == null) {
          Product product = OBDal.getInstance().get(Product.class, line.m_M_Product_ID);
          org.openbravo.model.financialmgmt.accounting.coa.AcctSchema schema = OBDal.getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.m_C_AcctSchema_ID);
          log4j.error("No Account COGS for product: " + product.getName()
              + " in accounting schema: " + schema.getName());
        }
        Account assetAccount = line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn);
        if (assetAccount == null) {
          Product product = OBDal.getInstance().get(Product.class, line.m_M_Product_ID);
          org.openbravo.model.financialmgmt.accounting.coa.AcctSchema schema = OBDal.getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.m_C_AcctSchema_ID);
          log4j.error("No Account Asset for product: " + product.getName()
              + " in accounting schema: " + schema.getName());
        }
        String costs = line.getProductCosts(DateAcct, as, conn, con);
        log4jDocInOut.debug("(MatShipment) - DR account: "
            + line.getAccount(ProductInfo.ACCTTYPE_P_Cogs, as, conn));
        log4jDocInOut.debug("(MatShipment) - DR costs: " + costs);
        BigDecimal b_Costs = new BigDecimal(costs).setScale(new Integer(strScale),
            RoundingMode.HALF_UP);
        String strCosts = b_Costs.toString();
        if (b_Costs.compareTo(BigDecimal.ZERO) == 0
            && DocInOutData.existsCost(conn, DateAcct, line.m_M_Product_ID).equals("0")) {
          setStatus(STATUS_InvalidCost);
          break;
        }
        // CoGS DR
        dr = fact.createLine(line, cogsAccount, as.getC_Currency_ID(), strCosts, "",
            Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        if (dr != null) {
          dr.setM_Locator_ID(line.m_M_Locator_ID);
          dr.setLocationFromLocator(line.m_M_Locator_ID, true, conn); // from
          // Loc
          dr.setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to
        }
        // Loc
        log4jDocInOut.debug("(MatShipment) - CR account: "
            + line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn));
        log4jDocInOut.debug("(MatShipment) - CR costs: " + strCosts);
        // Inventory CR
        cr = fact.createLine(line, assetAccount, as.getC_Currency_ID(), "", strCosts,
            Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        if (cr != null) {
          cr.setM_Locator_ID(line.m_M_Locator_ID);
          cr.setLocationFromLocator(line.m_M_Locator_ID, true, conn); // from
          // Loc
          cr.setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to
          // Loc
        }
      }
    }
    // Purchasing
    else if (DocumentType.equals(AcctServer.DOCTYPE_MatReceipt)) {
      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        DocLine_Material line = (DocLine_Material) p_lines[i];
        String costs = line.getProductCosts(DateAcct, as, conn, con);
        BigDecimal b_Costs = new BigDecimal(costs).setScale(new Integer(strScale),
            RoundingMode.HALF_UP);
        String strCosts = b_Costs.toString();
        if (b_Costs.compareTo(BigDecimal.ZERO) == 0
            && DocInOutData.existsCost(conn, DateAcct, line.m_M_Product_ID).equals("0")) {
          setStatus(STATUS_InvalidCost);
          break;
        }
        Account notInvoicedReceiptsAccount = getAccount(AcctServer.ACCTTYPE_NotInvoicedReceipts,
            as, conn);
        if (notInvoicedReceiptsAccount == null) {
          Product product = OBDal.getInstance().get(Product.class, line.m_M_Product_ID);
          org.openbravo.model.financialmgmt.accounting.coa.AcctSchema schema = OBDal.getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.m_C_AcctSchema_ID);
          log4j.error("No Account Not Invoiced Receipts for product: " + product.getName()
              + " in accounting schema: " + schema.getName());
        }
        Account assetAccount = line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn);
        if (assetAccount == null) {
          Product product = OBDal.getInstance().get(Product.class, line.m_M_Product_ID);
          org.openbravo.model.financialmgmt.accounting.coa.AcctSchema schema = OBDal.getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.m_C_AcctSchema_ID);
          log4j.error("No Account Asset for product: " + product.getName()
              + " in accounting schema: " + schema.getName());
        }
        // If there exists cost for the product, but it is equals to zero, then no line is added,
        // but no error is thrown. If this is the only line in the document, yes an error will be
        // thrown
        if (!costs.equals("0")
            || DocInOutData.existsCost(conn, DateAcct, line.m_M_Product_ID).equals("0")) {

          log4jDocInOut.debug("(matReceipt) - DR account: " + assetAccount);
          log4jDocInOut.debug("(matReceipt) - DR costs: " + strCosts);
          // Inventory DR
          dr = fact.createLine(line, assetAccount, as.getC_Currency_ID(), strCosts, "",
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (!getStatus().equals("i")) {
            if (dr != null) {
              dr.setM_Locator_ID(line.m_M_Locator_ID);
              dr.setLocationFromBPartner(C_BPartner_Location_ID, true, conn); // from
              // Loc
              dr.setLocationFromLocator(line.m_M_Locator_ID, false, conn); // to
              // Loc
            }
            log4jDocInOut.debug("(matReceipt) - CR account: "
                + line.getAccount(AcctServer.ACCTTYPE_NotInvoicedReceipts, as, conn));
            log4jDocInOut.debug("(matReceipt) - CR costs: " + strCosts);
            // NotInvoicedReceipt CR
            cr = fact.createLine(line, notInvoicedReceiptsAccount, as.getC_Currency_ID(), "",
                strCosts, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (cr != null) {
              cr.setM_Locator_ID(line.m_M_Locator_ID);
              cr.setLocationFromBPartner(C_BPartner_Location_ID, true, conn); // from
              // Loc
              cr.setLocationFromLocator(line.m_M_Locator_ID, false, conn); // to
              // Loc
            }
          }
        }
      }
    } else {
      log4jDocInOut.warn("createFact - " + "DocumentType unknown: " + DocumentType);
      return null;
    }
    //
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * @return the log4jDocInOut
   */
  public static Logger getLog4jDocInOut() {
    return log4jDocInOut;
  }

  /**
   * @param log4jDocInOut
   *          the log4jDocInOut to set
   */
  public static void setLog4jDocInOut(Logger log4jDocInOut) {
    DocInOut.log4jDocInOut = log4jDocInOut;
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
    log4jDocInOut.debug("DocInOut - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocInOut.debug("DocInOut - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    try {
      DocLineInOutData[] data = DocLineInOutData.select(conn, Record_ID);
      ShipmentInOut inOut = OBDal.getInstance().get(ShipmentInOut.class, strRecordId);
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      String strDateAcct = outputFormat.format(inOut.getAccountingDate());
      int validLines = 0;
      for (int i = 0; i < data.length; i++) {
        if (DocInOutData.existsCost(conn, strDateAcct, data[i].getField("mProductId")).equals("0")) {
          setStatus(STATUS_InvalidCost);
          return false;
        } else if (!ProductInfoData.selectProductAverageCost(conn, data[i].getField("mProductId"),
            strDateAcct).equals("0")) {
          validLines++;
        }
      }
      if (validLines == 0) {
        setStatus(STATUS_DocumentDisabled);
        return false;
      }
    } catch (ServletException e) {
      log4j.error("Servlet Exception in document confirmation", e);
      return false;
    }
    return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method

}
