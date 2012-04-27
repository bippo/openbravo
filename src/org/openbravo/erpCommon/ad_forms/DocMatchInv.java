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
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.system.Client;

public class DocMatchInv extends AcctServer {

  private static final long serialVersionUID = 1L;
  static Logger log4jDocMatchInv = Logger.getLogger(DocMatchInv.class);

  /** AD_Table_ID */
  private String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocMatchInv(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn,
      @SuppressWarnings("hiding") String AD_Client_ID, String Id) throws ServletException {
    setObjectFieldProvider(DocMatchInvData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    C_Currency_ID = NO_CURRENCY;
    DocumentType = AcctServer.DOCTYPE_MatMatchInv;
    log4jDocMatchInv.debug("loadDocumentDetails - C_Currency_ID : " + C_Currency_ID);
    DateDoc = data[0].getField("DateTrx");
    C_BPartner_ID = data[0].getField("C_Bpartner_Id");

    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn, data[0].getField("C_InvoiceLine_Id"));
    return true;
  } // loadDocumentDetails

  /**
   * Load Invoice Line
   * 
   * @return DocLine Array
   */
  public DocLine[] loadLines(ConnectionProvider conn, String strCInvoiceLineId) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocMatchInvData[] data = null;
    try {
      log4jDocMatchInv.debug("############### groupLines = " + groupLines);
      if (groupLines.equals("Y"))
        data = DocMatchInvData.selectInvoiceLineTotal(connectionProvider, strCInvoiceLineId);
      else
        data = DocMatchInvData.selectInvoiceLine(connectionProvider, strCInvoiceLineId);
    } catch (ServletException e) {
      log4jDocMatchInv.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      DocLine_Invoice docLine = new DocLine_Invoice(DocumentType, Record_ID, strCInvoiceLineId);
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
        DocMatchInvTemplate newTemplate = (DocMatchInvTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocMatchInvTemplate - " + e);
      }
    }
    C_Currency_ID = as.getC_Currency_ID();
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    // Line pointers
    FactLine dr = null, cr = null, diff = null;

    // Entry to build has the form:
    // Account......................................Debit.......................... Credit
    // Not Invoiced Receipts........... Cost in the goods receipt
    // Expenses......................................................... Expenses in the Invoice
    // Invoice Price Variance........ Difference of cost and expenses

    boolean changeSign = false;
    FieldProvider[] data = getObjectFieldProvider();
    BigDecimal bdCost = new BigDecimal(DocMatchInvData.selectProductAverageCost(conn,
        data[0].getField("M_Product_Id"), data[0].getField("orderAcctDate")));
    String strScale = DocMatchInvData.selectClientCurrencyPrecission(conn, vars.getClient());
    BigDecimal bdQty = new BigDecimal(data[0].getField("Qty"));
    bdCost = bdCost.multiply(bdQty).setScale(new Integer(strScale), RoundingMode.HALF_UP);

    DocMatchInvData[] invoiceData = DocMatchInvData.selectInvoiceData(conn, vars.getClient(),
        data[0].getField("C_InvoiceLine_Id"));
    String costCurrencyId = as.getC_Currency_ID();
    OBContext.setAdminMode(false);
    try {
      costCurrencyId = OBDal.getInstance().get(Client.class, AD_Client_ID).getCurrency().getId();
    } finally {
      OBContext.restorePreviousMode();
    }

    String strExpenses = invoiceData[0].linenetamt;
    String strInvoiceCurrency = invoiceData[0].cCurrencyId;
    String strDate = invoiceData[0].dateacct;
    strExpenses = getConvertedAmt(strExpenses, strInvoiceCurrency, costCurrencyId, strDate, "",
        vars.getClient(), vars.getOrg(), conn);
    BigDecimal bdExpenses = new BigDecimal(strExpenses);
    if ((new BigDecimal(data[0].getField("QTYINVOICED")).signum() != (new BigDecimal(
        data[0].getField("MOVEMENTQTY"))).signum())
        && data[0].getField("InOutStatus").equals("VO")) {
      changeSign = true;
      bdExpenses = bdExpenses.multiply(new BigDecimal(-1));
    }

    BigDecimal bdDifference = bdExpenses.subtract(bdCost);

    DocLine docLine = new DocLine(DocumentType, Record_ID, "");
    docLine.m_C_Project_ID = data[0].getField("INOUTPROJECT");

    dr = fact
        .createLine(docLine, getAccount(AcctServer.ACCTTYPE_NotInvoicedReceipts, as, conn),
            costCurrencyId, bdCost.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
            conn);

    if (dr == null) {
      log4j.warn("createFact - unable to calculate line with "
          + " cost of the product to not invoiced receipt account.");
      return null;
    }
    ProductInfo p = new ProductInfo(data[0].getField("M_Product_Id"), conn);
    for (DocLine docLineInvoice : p_lines) {
      String strAmount = "";
      if (strInvoiceCurrency != costCurrencyId) {
        strAmount = getConvertedAmt((changeSign) ? new BigDecimal(docLineInvoice.getAmount())
            .multiply(new BigDecimal(-1)).toString() : docLineInvoice.getAmount(),
            strInvoiceCurrency, costCurrencyId, strDate, "", vars.getClient(), vars.getOrg(), conn);
      } else {
        strAmount = (changeSign) ? new BigDecimal(docLineInvoice.getAmount()).multiply(
            new BigDecimal(-1)).toString() : docLineInvoice.getAmount();
      }

      cr = fact.createLine(docLineInvoice, p.getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn),
          costCurrencyId, "0", strAmount, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      if (cr == null && ZERO.compareTo(new BigDecimal(strAmount)) != 0) {
        log4j.warn("createFact - unable to calculate line with "
            + " expenses to product expenses account.");
        return null;
      }
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

    if (!bdCost.equals(bdExpenses)) {
      diff = fact.createLine(docLine, p.getAccount(ProductInfo.ACCTTYPE_P_IPV, as, conn),
          costCurrencyId, (bdDifference.compareTo(BigDecimal.ZERO) == 1) ? bdDifference.abs()
              .toString() : "0", (bdDifference.compareTo(BigDecimal.ZERO) < 1) ? bdDifference.abs()
              .toString() : "0", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      if (diff == null) {
        log4j.warn("createFact - unable to calculate line with "
            + " difference to InvoicePriceVariant account.");
        return null;
      }
    }
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * @return the log4jDocMatchInv
   */
  public static Logger getLog4jDocMatchInv() {
    return log4jDocMatchInv;
  }

  /**
   * @param log4jDocMatchInv
   *          the log4jDocMatchInv to set
   */
  public static void setLog4jDocMatchInv(Logger log4jDocMatchInv) {
    DocMatchInv.log4jDocMatchInv = log4jDocMatchInv;
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
    log4jDocMatchInv.debug("DocMatchInv - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocMatchInv.debug("DocMatchInv - nextSeqNo = " + SeqNo);
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

  /**
   * Update Product Info. - Costing (PriceLastInv) - PO (PriceLastInv)
   * 
   * @param C_AcctSchema_ID
   *          accounting schema
   */
  public void updateProductInfo(String C_AcctSchema_ID, ConnectionProvider conn, Connection con) {
    log4jDocMatchInv.debug("updateProductInfo - C_Invoice_ID=" + this.Record_ID);

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
      log4jDocMatchInv.debug("M_Product_PO - Updated=" + no);

    } catch (ServletException e) {
      log4jDocMatchInv.warn(e);
    }
  } // updateProductInfo

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
