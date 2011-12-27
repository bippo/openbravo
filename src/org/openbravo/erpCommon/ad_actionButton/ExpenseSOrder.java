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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.ActionButtonData;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.xmlEngine.XmlDocument;

public class ExpenseSOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final BigDecimal ZERO = new BigDecimal(0.0);

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strCompleteAuto = vars.getStringParameter("inpShowNullComplete", "Y");
      String strOrganization = vars.getGlobalVariable("inpadOrgId", "ExpenseSOrder.adOrgId", "");
      printPage(response, vars, "", "", "", "", strOrganization, strCompleteAuto);
    } else if (vars.commandIn("SAVE")) {
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strDatefrom = vars.getStringParameter("inpDateFrom");
      String strDateto = vars.getStringParameter("inpDateTo");
      String strDateOrdered = vars.getStringParameter("inpDateordered");
      String strOrganization = vars.getStringParameter("organization");
      String strCompleteAuto = vars.getStringParameter("inpShowNullComplete");

      OBError myMessage;
      myMessage = new OBError();
      myMessage = processButton(vars, strBPartner, strDatefrom, strDateto, strDateOrdered,
          strOrganization, strCompleteAuto);
      vars.setMessage("ExpenseSOrder", myMessage);

      printPage(response, vars, strDatefrom, strDateto, strDateOrdered, strBPartner,
          strOrganization, strCompleteAuto);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strBPartner, String strDatefrom,
      String strDateto, String strDateOrdered, String strOrganization, String strCompleteAuto) {
    StringBuffer textoMensaje = new StringBuffer();
    StringBuffer txtOrder = new StringBuffer();
    Connection conn = null;

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    int total = 0;
    try {
      conn = getTransactionConnection();
      ExpenseSOrderData[] data = ExpenseSOrderData.select(this, strBPartner, strDatefrom,
          DateTimeData.nDaysAfter(this, strDateto, "1"), Utility.getContext(this, vars,
              "#User_Client", "ExpenseSOrder"),
          strOrganization.equals("") ? Utility.getContext(this, vars, "#User_Org", "ExpenseSOrder")
              : Utility.stringList(strOrganization));
      String strOldOrganization = "-1";
      String strOldBPartner = "-1";
      String strOldProject = "-1";
      String strCOrderId = "";
      int line = 0;
      // ArrayList order = new ArrayList();
      for (int i = 0; data != null && i < data.length; i++) {
        // If the sales order header information (business partner, project and organization) is not
        // the same as the previous data line, complete the previous sales order, create a new sales
        // order header and insert the first line
        if (!data[i].cBpartnerId.equals(strOldBPartner)
            || !data[i].cProjectId.equals(strOldProject)
            || !data[i].adOrgId.equals(strOldOrganization)) {
          // Complete previous order
          if (!strCOrderId.equals("") && strCOrderId != null) {
            // Automatically processes Sales Order
            releaseCommitConnection(conn);
            if (strCompleteAuto.equals("Y")) {
              try {
                myMessage = processOrder(vars, strCOrderId);
                if (myMessage != null)
                  txtOrder.append(" -> ").append(myMessage.getMessage());
              } catch (ServletException ex) {
                myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
                myMessage.setMessage(Utility.messageBD(this, "Created", vars.getLanguage()) + ": "
                    + Integer.toString(total) + "<br/>"
                    + textoMensaje.append(myMessage.getMessage()).toString());
                return myMessage;
              }
            }
            textoMensaje.append(txtOrder).append("<br/>");
            txtOrder = new StringBuffer();
            total++;
            conn = getTransactionConnection();
          }
          // Create a new sales order header
          strOldBPartner = data[i].cBpartnerId;
          strOldProject = data[i].cProjectId;
          strOldOrganization = data[i].adOrgId;
          strCOrderId = SequenceIdData.getUUID();

          myMessage = insertOrderHeader(conn, vars, strCOrderId, strDateOrdered, data[i]);
          if (myMessage != null) {
            if (myMessage.getType() != "Error") {
              txtOrder.append(myMessage.getMessage());
            } else {
              myMessage.setMessage(Utility.messageBD(this, "Created", vars.getLanguage()) + ": "
                  + Integer.toString(total) + "<br/>"
                  + textoMensaje.append(myMessage.getMessage()).toString());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }

          // Insert first order line
          line = 10;
          myMessage = insertOrderLine(conn, vars, strOrganization, strCOrderId, line,
              strDateOrdered, data[i]);
          if (myMessage != null) {
            if (myMessage.getType() != "Error") {
              txtOrder.append(myMessage.getMessage());
            } else {
              myMessage.setMessage(Utility.messageBD(this, "Created", vars.getLanguage()) + ": "
                  + Integer.toString(total) + "<br/>"
                  + textoMensaje.append(myMessage.getMessage()).toString());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }
        } else { // Keep adding lines to the same sales order header
          line = line + 10;
          myMessage = insertOrderLine(conn, vars, strOrganization, strCOrderId, line,
              strDateOrdered, data[i]);
          if (myMessage != null) {
            if (myMessage.getType() != "Error") {
              txtOrder.append(myMessage.getMessage());
            } else {
              myMessage.setMessage(textoMensaje.append(
                  Utility.messageBD(this, "Created", vars.getLanguage()) + ": "
                      + Integer.toString(total) + "<br/>" + myMessage.getMessage()).toString());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }
        }
        // If we are in the last data line, complete the order
        if (i + 1 == data.length) {
          // Automatically processes Sales Order
          releaseCommitConnection(conn);
          if (strCompleteAuto.equals("Y")) {
            try {
              myMessage = processOrder(vars, strCOrderId);
              if (myMessage != null)
                txtOrder.append(" -> ").append(myMessage.getMessage());
            } catch (ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              myMessage.setMessage(Utility.messageBD(this, "Created", vars.getLanguage()) + ": "
                  + Integer.toString(total) + "<br/>"
                  + textoMensaje.append(myMessage.getMessage()).toString());
              return myMessage;
            }
          }
          textoMensaje.append(txtOrder).append("<br/>");
          txtOrder = new StringBuffer();
          total++;
          conn = getTransactionConnection();
        }
      }
      releaseCommitConnection(conn);
      myMessage.setType("Success");
      myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myMessage.setMessage(Utility.messageBD(this, "Created", vars.getLanguage()) + ": "
          + Integer.toString(total) + "<br/>" + textoMensaje.toString());
      return myMessage;
    } catch (Exception ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      try {
        if (conn != null)
          releaseRollbackConnection(conn);
        return myMessage;
      } catch (Exception ignored) {
      }
    }
    return myMessage;
  }

  // Creates a sales order header.
  private OBError insertOrderHeader(Connection conn, VariablesSecureApp vars, String strCOrderId,
      String strDateOrdered, ExpenseSOrderData data) throws SQLException {
    OBError myMessage = null;
    myMessage = new OBError();

    String strDocStatus = "DR";
    String strDocAction = "CO";
    String strProcessing = "N";
    String docType = "0";
    String strCust = data.bpname;

    if (data.mPricelistId.equals("")) {
      myMessage.setType("Error");
      myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      myMessage.setMessage(Utility.messageBD(this, "TheCustomer", vars.getLanguage()) + ' '
          + strCust + ' ' + Utility.messageBD(this, "PricelistNotdefined", vars.getLanguage())
          + ".");
      return myMessage;
    } else {
      try {
        // Selects the currency of the business partner price
        // list, that can be different from the currency of the
        // expense
        String strBPCCurrencyId = ExpenseSOrderData.selectCurrency(this, data.mPricelistId);

        BpartnerMiscData[] data1 = null;
        data1 = BpartnerMiscData.select(this, data.cBpartnerId);
        if (data1[0].finPaymentmethodId.equals("") || data1[0].finPaymentmethodId == null) {
          myMessage.setType("Error");
          myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          myMessage.setMessage(Utility.messageBD(this, "TheCustomer", vars.getLanguage()) + ' '
              + strCust + ' '
              + Utility.messageBD(this, "PayementMethodNotdefined", vars.getLanguage()) + ".");
          return myMessage;
        } else {
          if (data1[0].paymentrule.equals("") || data1[0].paymentrule == null) {
            data1[0].paymentrule = "P";
          }
          String docTargetType = ExpenseSOrderData.cDoctypeTarget(conn, this, data.adClientId,
              data.adOrgId);
          String strDocumentNo = Utility.getDocumentNo(this, vars, "", "C_Order", docTargetType,
              docTargetType, false, true);
          ExpenseSOrderData.insertCOrder(
              conn,
              this,
              strCOrderId,
              data.adClientId,
              data.adOrgId,
              vars.getUser(),
              strDocumentNo,
              strDocStatus,
              strDocAction,
              strProcessing,
              docType,
              docTargetType,
              strDateOrdered,
              strDateOrdered,
              strDateOrdered,
              data.cBpartnerId,
              ExpenseSOrderData.cBPartnerLocationId(this, data.cBpartnerId),
              ExpenseSOrderData.billto(this, data.cBpartnerId).equals("") ? ExpenseSOrderData
                  .cBPartnerLocationId(this, data.cBpartnerId) : ExpenseSOrderData.billto(this,
                  data.cBpartnerId),
              strBPCCurrencyId,
              data1[0].paymentrule,
              data1[0].finPaymentmethodId,
              data1[0].cPaymenttermId.equals("") ? ExpenseSOrderData.selectPaymentTerm(this,
                  data.adClientId) : data1[0].cPaymenttermId, data1[0].invoicerule.equals("") ? "I"
                  : data1[0].invoicerule, data1[0].deliveryrule.equals("") ? "A"
                  : data1[0].deliveryrule, "I", data1[0].deliveryviarule.equals("") ? "D"
                  : data1[0].deliveryviarule, data.mWarehouseId.equals("") ? vars.getWarehouse()
                  : data.mWarehouseId, data.mPricelistId, data.cProjectId, data.cActivityId,
              data.cCampaignId);
          myMessage.setType("Success");
          myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
          myMessage.setMessage(Utility.messageBD(this, "SalesOrderDocumentno", vars.getLanguage())
              + " " + strDocumentNo + " "
              + Utility.messageBD(this, "beenCreated", vars.getLanguage()));
        }
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        return myMessage;
      }
    }
    return myMessage;
  }

  // Creates an order line.
  private OBError insertOrderLine(Connection conn, VariablesSecureApp vars, String strOrganization,
      String strCOrderId, Integer line, String strDateOrdered, ExpenseSOrderData data)
      throws IOException, SQLException {
    OBError myMessage = null;
    myMessage = new OBError();

    String strExpenseSheetDocno = data.documentno;
    String strExpenseSheetLineno = data.line;

    String priceactual = "";
    String pricelist = "";
    String pricelimit = "";

    // Catch database error message
    try {
      // Determines the date of the expense (strDateExpense)
      String strDateexpense = data.dateexpense.equals("") ? data.datereport : data.dateexpense;
      strDateexpense = strDateexpense.equals("") ? DateTimeData.today(this) : strDateexpense;

      // Gets the tax for the sales order line
      String strCTaxID = Tax.get(this, data.mProductId, strDateOrdered, data.adOrgId,
          data.mWarehouseId.equals("") ? vars.getWarehouse() : data.mWarehouseId,
          ExpenseSOrderData.cBPartnerLocationId(this, data.cBpartnerId),
          ExpenseSOrderData.cBPartnerLocationId(this, data.cBpartnerId), data.cProjectId, true);
      if (strCTaxID.equals("") || strCTaxID == null) {
        myMessage.setType("Error");
        myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        myMessage.setMessage(Utility.messageBD(this, "ExpenseSheetNo", vars.getLanguage()) + " "
            + strExpenseSheetDocno + ", " + Utility.messageBD(this, "line", vars.getLanguage())
            + " " + strExpenseSheetLineno + ": "
            + Utility.messageBD(this, "TaxNotFound", vars.getLanguage()) + ".");
        return myMessage;
      } else {
        // Currency of the expense line
        String strCCurrencyId = data.cCurrencyId;

        // Currency of the business partner price list
        String strBPCCurrencyId = ExpenseSOrderData.selectCurrency(this, data.mPricelistId);

        String strPrecision = "0";
        String strPricePrecision = "0";
        String strDiscount = "";

        BigDecimal priceActual, priceList, priceLimit, discount;

        // If there is no invoice price, looks for the prices in the
        // pricelist of the business partner
        if (data.invoiceprice == null || data.invoiceprice.equals("")) {
          ExpenseSOrderData[] data3 = ExpenseSOrderData.selectPrice(this, data.mProductId,
              data.mPricelistId, strBPCCurrencyId);
          for (int j = 0; data3 != null && j < data3.length; j++) {
            if (data3[j].validfrom == null || data3[j].validfrom.equals("")
                || !DateTimeData.compare(this, strDateexpense, data3[j].validfrom).equals("-1")) {
              priceactual = data3[j].pricestd;
              pricelist = data3[j].pricelist;
              pricelimit = data3[j].pricelimit;
            }
          }
        } else {
          // If there is an invoice price, it takes it and makes
          // currency conversions, if necessary
          priceactual = data.invoiceprice;
          pricelist = "0";
          pricelimit = "0";

          // If the currency of the expense line is not the same as
          // the currency of the business partner pricelist, make the
          // corresponding conversions
          if (!strCCurrencyId.equals(strBPCCurrencyId)) {
            // Converts priceactual, pricelist and pricelimit to the
            // currency of the business partner pricelist
            priceactual = ExpenseSOrderData.selectConvertedAmt(this, priceactual, strCCurrencyId,
                strBPCCurrencyId, strDateexpense, vars.getClient(), vars.getOrg());
            pricelist = ExpenseSOrderData.selectConvertedAmt(this, pricelist, strCCurrencyId,
                strBPCCurrencyId, strDateexpense, vars.getClient(), vars.getOrg());
            pricelimit = ExpenseSOrderData.selectConvertedAmt(this, pricelimit, strCCurrencyId,
                strBPCCurrencyId, strDateexpense, vars.getClient(), vars.getOrg());
          }
        }

        // If priceactual is null
        if (priceactual.equals("") || priceactual == null) {
          myMessage.setType("Error");
          myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          myMessage.setMessage(Utility.messageBD(this, "ExpenseSheetNo", vars.getLanguage()) + " "
              + strExpenseSheetDocno + ", " + Utility.messageBD(this, "line", vars.getLanguage())
              + " " + strExpenseSheetLineno + ": "
              + Utility.messageBD(this, "PriceNotFound", vars.getLanguage()) + ".");
          return myMessage;
        } else {
          // Recalculates precisions for the business partner pricelist
          // currency
          ExpenseSOrderData[] data4 = ExpenseSOrderData.selectPrecisions(this, strBPCCurrencyId);
          if (data4 != null && data4.length > 0) {
            strPrecision = data4[0].stdprecision.equals("") ? "0" : data4[0].stdprecision;
            strPricePrecision = data4[0].priceprecision.equals("") ? "0" : data4[0].priceprecision;
          }
          int StdPrecision = Integer.valueOf(strPrecision).intValue();
          int PricePrecision = Integer.valueOf(strPricePrecision).intValue();
          priceActual = (priceactual.equals("") ? ZERO : (new BigDecimal(priceactual)));
          priceActual = priceActual.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
          priceList = (pricelist.equals("") ? ZERO : (new BigDecimal(pricelist)));
          priceList = priceList.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
          priceLimit = (pricelimit.equals("") ? ZERO : (new BigDecimal(pricelimit)));
          priceLimit = priceLimit.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);

          // Calculating discount
          if (priceList.compareTo(BigDecimal.ZERO) == 0)
            discount = ZERO;
          else {
            if (log4j.isDebugEnabled())
              log4j.debug("pricelist:" + Double.toString(priceList.doubleValue()));
            if (log4j.isDebugEnabled())
              log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));
            discount = ((priceList.subtract(priceActual)).divide(priceList, 12,
                BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100"));
          }
          if (log4j.isDebugEnabled())
            log4j.debug("Discount: " + discount.toString());
          if (discount.scale() > StdPrecision) {
            discount = discount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
          }
          if (log4j.isDebugEnabled())
            log4j.debug("Discount rounded: " + discount.toString());

          priceactual = priceActual.toString();
          pricelist = priceList.toString();
          pricelimit = priceLimit.toString();
          strDiscount = discount.toString();

          String strCOrderlineID = SequenceIdData.getUUID();

          ExpenseSOrderData.insertCOrderline(conn, this, strCOrderlineID, data.adClientId,
              strOrganization.equals("") ? data.adOrgId : strOrganization, vars.getUser(),
              strCOrderId, Integer.toString(line), data.cBpartnerId, ExpenseSOrderData
                  .cBPartnerLocationId(this, data.cBpartnerId), strDateOrdered, strDateOrdered,
              data.description, data.mProductId, data.mWarehouseId.equals("") ? vars.getWarehouse()
                  : data.mWarehouseId,
              data.cUomId.equals("") ? Utility.getContext(this, vars, "#C_UOM_ID", "ExpenseSOrder")
                  : data.cUomId, data.qty, strBPCCurrencyId, pricelist, priceactual,
              data.mPricelistId, pricelimit, strCTaxID, data.sResourceassignmentId, strDiscount);

          // Updates expense line with the sales order line ID
          ExpenseSOrderData.updateTimeExpenseLine(conn, this, strCOrderlineID,
              data.sTimeexpenselineId);
        }
      }
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      return myMessage;
    }
    return myMessage;
  }

  private OBError processOrder(VariablesSecureApp vars, String strCOrderId)
      throws ServletException, NoConnectionAvailableException, SQLException {
    Connection conn = null;
    conn = getTransactionConnection();

    OBError myMessage = null;

    String pinstance = SequenceIdData.getUUID();
    try {
      PInstanceProcessData.insertPInstance(this, pinstance, "104", strCOrderId, "N",
          vars.getUser(), vars.getClient(), vars.getOrg());
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      releaseRollbackConnection(conn);
      return myMessage;
    }

    ActionButtonData.process104(this, pinstance);
    PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
    myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);

    releaseCommitConnection(conn);

    return myMessage;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDatefrom,
      String strDateto, String strDateOrdered, String strBPartner, String strOrganization,
      String strCompleteAuto) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process ExpenseSOrder");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "", strProcessId = "186";
    String[] discard = { "" };
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/ExpenseSOrder").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ExpenseSOrder", false, "", "", "",
        false, "ad_actionButton", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("Bpartnerdescription",
        ExpenseSOrderData.selectBpartner(this, strBPartner));
    xmlDocument.setParameter("BpartnerId", strBPartner);
    xmlDocument.setParameter("adOrgId", strOrganization);
    xmlDocument.setParameter("dateFrom", strDatefrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateto);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateOrdered", strDateOrdered);
    xmlDocument.setParameter("dateOrddisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateOrdsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramShowNullComplete", strCompleteAuto);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ExpenseSOrder"),
          Utility.getContext(this, vars, "#User_Client", "ExpenseSOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ExpenseSOrder", strOrganization);
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
      comboTableData = null;

    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    // New interface parameters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_actionButton.ExpenseSOrder");

      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ExpenseSOrder.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ExpenseSOrder.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ExpenseSOrder");
      vars.removeMessage("ExpenseSOrder");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    // //----

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Create Sales Orders from Expenses";
  } // end of getServletInfo() method
}
