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
 * All portions are Copyright (C) 2007-2012 Openbravo SLU 
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

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportSalesOrderJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strdateFrom = vars.getGlobalVariable("inpDateFrom", "ReportSalesOrderJR|dateFrom", "");
      String strdateTo = vars.getGlobalVariable("inpDateTo", "ReportSalesOrderJR|dateTo", "");
      String strcProjectId = vars.getGlobalVariable("inpcProjectId",
          "ReportSalesOrderJR|projectId", "");
      String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId",
          "ReportSalesOrderJR|warehouseId", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportSalesOrderJR|currency",
          strUserCurrencyId);
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strmWarehouseId, strcProjectId,
          strCurrencyId);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportSalesOrderJR|dateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportSalesOrderJR|dateTo");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportSalesOrderJR|warehouseId");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId",
          "ReportSalesOrderJR|projectId");
      String strcRegionId = vars.getRequestInGlobalVariable("inpcRegionId",
          "ReportSalesOrderJR|regionId", IsIDFilter.instance);
      String strmProductCategoryId = vars.getRequestInGlobalVariable("inpmProductCategoryId",
          "ReportSalesOrderJR|productCategoryId", IsIDFilter.instance);
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportSalesOrderJR|bpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportSalesOrderJR|productId", IsIDFilter.instance);
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportSalesOrderJR|currency",
          strUserCurrencyId);
      printPageHtml(request, response, vars, strdateFrom, strdateTo, strmWarehouseId,
          strcProjectId, strcRegionId, strmProductCategoryId, strcBpartnerId, strmProductId,
          strCurrencyId);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strdateFrom, String strdateTo, String strmWarehouseId, String strcProjectId,
      String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportSalesOrderFilterJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(
        this,
        vars.getLanguage(),
        "ReportSalesOrderJR",
        false,
        "",
        "",
        "openServletNewWindow('EDIT_PDF', true, 'ReportSalesOrderJR.pdf', 'ReportSalesOrderFilterJR', null, false, '700', '1000', true);return false;",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportSalesOrderJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportSalesOrderFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportSalesOrderFilterJR.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportSalesOrderJR");
      vars.removeMessage("ReportSalesOrderJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strdateFrom);
    xmlDocument.setParameter("dateTo", strdateTo);
    xmlDocument.setParameter("paramBPartnerId", "");
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("cProjectId", strcProjectId);
    xmlDocument.setParameter("projectName", OrderEditionData.selectProject(this, strcProjectId));
    xmlDocument.setParameter("cRegionId", "");
    xmlDocument.setParameter("mProductCatId", "");
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "SalesOrderFilterJR"),
          Utility.getContext(this, vars, "#User_Client", "SalesOrderFilter"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SalesOrderFilterJR", "");
      xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Region_ID", "",
          "C_Region of Country", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "SalesOrderFilterJR"), Utility.getContext(this, vars, "#User_Client",
              "SalesOrderFilterJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SalesOrderFilterJR", "");
      xmlDocument.setData("reportC_REGIONID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "SalesOrderFilterJR"),
          Utility.getContext(this, vars, "#User_Client", "SalesOrderFilterJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SalesOrderFilterJR",
          strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", new FieldProvider[0]);
    xmlDocument.setData("reportMProductId_IN", "liststructure", new FieldProvider[0]);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_PRODUCT_CATEGORY_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesOrderJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportSalesOrderJR"), 0);
      comboTableData.fillParameters(null, "ReportSalesOrderJR", "");
      xmlDocument
          .setData("reportC_PRODUCTCATREGORY", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");

    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageHtml(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strdateFrom, String strdateTo, String strmWarehouseId,
      String strcProjectId, String strcRegionId, String strmProductCategoryId,
      String strcBpartnerId, String strmProductId, String strCurrencyId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");

    OrderEditionData[] data = null;

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    try {
      data = OrderEditionData.select(this, strCurrencyId,
          Utility.getContext(this, vars, "#AccessibleOrgTree", "SalesOrderFilterJR"),
          Utility.getContext(this, vars, "#User_Client", "SalesOrderFilterJR"), strdateFrom,
          strdateTo, strmWarehouseId, strcProjectId, strcRegionId, strmProductCategoryId,
          strcBpartnerId, strmProductId);
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
      if (data == null || data.length == 0)
        data = OrderEditionData.set();

      String strOutput = vars.commandIn("EDIT_HTML") ? "html" : "pdf";
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportSalesOrderJR.jrxml";

      String strSubTitle = "";
      strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom + " "
          + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("REPORT_SUBTITLE", strSubTitle);

      renderJR(vars, response, strReportName, strOutput, parameters, data, null);
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
