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
package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportInvoicesJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strC_BPartner_ID = vars.getGlobalVariable("inpcBPartnerId",
          "ReportInvoices|C_BPartner_ID", "");
      String strM_Product_ID = vars.getGlobalVariable("inpmProductId",
          "ReportInvoices|M_Product_ID", "");
      String strDateFrom = vars.getGlobalVariable("inpDateInvoiceFrom", "ReportInvoices|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateInvoiceTo", "ReportInvoices|DateTo", "");
      String strDocumentNo = vars.getGlobalVariable("inpInvoicedocumentno",
          "ReportInvoices|DocumentNo", "");
      String strOrder = vars.getGlobalVariable("inpOrder", "ReportInvoices|Order", "SalesOrder");
      String strC_BpGroup_ID = vars.getGlobalVariable("inpcBpGroupId",
          "ReportInvoices|C_BpGroup_ID", "");
      String strM_Product_Category_ID = vars.getGlobalVariable("inpmProductCategoryId",
          "ReportInvoices|M_Product_Category_ID", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportInvoices|currency",
          strUserCurrencyId);
      printPageDataSheet(response, vars, strC_BPartner_ID, strM_Product_ID, strDateFrom, strDateTo,
          strDocumentNo, strOrder, strC_BpGroup_ID, strM_Product_Category_ID, strCurrencyId);
    } else if (vars.commandIn("FIND")) {
      String strC_BPartner_ID = vars.getRequestGlobalVariable("inpcBPartnerId",
          "ReportInvoices|C_BPartner_ID");
      String strM_Product_ID = vars.getRequestGlobalVariable("inpmProductId",
          "ReportInvoices|M_Product_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateInvoiceFrom",
          "ReportInvoices|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateInvoiceTo", "ReportInvoices|DateTo");
      String strDocumentNo = vars.getRequestGlobalVariable("inpInvoicedocumentno",
          "ReportInvoices|DocumentNo");
      String strOrder = vars.getGlobalVariable("inpOrder", "ReportInvoices|Order");
      String strC_BpGroup_ID = vars.getRequestGlobalVariable("inpcBpGroupId",
          "ReportInvoices|C_BpGroup_ID");
      String strM_Product_Category_ID = vars.getRequestGlobalVariable("inpmProductCategoryId",
          "ReportInvoices|M_Product_Category_ID");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportInvoices|currency",
          strUserCurrencyId);
      printPageDataHtml(request, response, vars, strC_BPartner_ID, strM_Product_ID, strDateFrom,
          strDateTo, strDocumentNo, strOrder, strC_BpGroup_ID, strM_Product_Category_ID,
          strCurrencyId);
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strC_BPartner_ID, String strM_Product_ID, String strDateFrom,
      String strDateTo, String strDocumentNo, String strOrder, String strC_BpGroup_ID,
      String strM_Product_Category_ID, String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");

    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportInvoicesEditJR.jrxml";
    String strOutput = "html";
    if (strOutput.equals("html"))
      response.setHeader("Content-disposition", "inline; filename=ReportInvoicesEdit.html");

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    ReportInvoicesData[] data = null;
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    try {
      data = ReportInvoicesData.select(this, strCurrencyId, Utility.getContext(this, vars,
          "#User_Client", "ReportInvoices"), Utility.getContext(this, vars, "#AccessibleOrgTree",
          "ReportInvoices"), strC_BpGroup_ID, strM_Product_Category_ID, strC_BPartner_ID,
          strM_Product_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"),
          strDocumentNo, (strOrder.equals("PurchaseOrder")) ? "" : "sales", (strOrder
              .equals("PurchaseOrder")) ? "purchase" : "");
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    strConvRateErrorMsg = myMessage.getMessage();
    // If a conversion rate is missing for a certain transaction, an error
    // message window pops-up.
    if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
          strConvRateErrorMsg);
    } else { // Launch the report as usual, calling the JRXML file
      String strSubTitle = "";
      strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom + " "
          + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("REPORT_SUBTITLE", strSubTitle);

      renderJR(vars, response, strReportName, strOutput, parameters, data, null);
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strC_BPartner_ID, String strM_Product_ID, String strDateFrom, String strDateTo,
      String strDocumentNo, String strOrder, String strC_BpGroup_ID,
      String strM_Product_Category_ID, String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportInvoicesJR")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportInvoicesJR", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportInvoicesJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportInvoicesJR.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportInvoicesJR.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInvoices");
      vars.removeMessage("ReportInvoices");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramBPartnerId", strC_BPartner_ID);
    xmlDocument.setParameter("paramMProductId", strM_Product_ID);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramDocumentNo", strDocumentNo);
    xmlDocument.setParameter("paramCBpGroupID", strC_BpGroup_ID);
    xmlDocument.setParameter("paramMProductCategoryID", strM_Product_Category_ID);
    xmlDocument.setParameter("sales", strOrder);
    xmlDocument.setParameter("purchase", strOrder);
    xmlDocument.setParameter("paramBPartnerDescription",
        ReportInvoicesData.bPartnerDescription(this, strC_BPartner_ID));
    xmlDocument.setParameter("paramMProductIDDES",
        ReportInvoicesData.mProductDescription(this, strM_Product_ID));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoices"),
          Utility.getContext(this, vars, "#User_Client", "ReportInvoices"), 0);
      Utility
          .fillSQLParameters(this, vars, null, comboTableData, "ReportInvoices", strC_BpGroup_ID);
      xmlDocument.setData("reportC_Bp_Group", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportInvoices"), Utility.getContext(this, vars, "#User_Client", "ReportInvoices"),
          0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoices",
          strM_Product_Category_ID);
      xmlDocument
          .setData("reportM_Product_Category", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoices"),
          Utility.getContext(this, vars, "#User_Client", "ReportInvoices"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoices", strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportInvoices. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
