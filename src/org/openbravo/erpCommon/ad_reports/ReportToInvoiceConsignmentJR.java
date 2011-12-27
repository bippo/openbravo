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

public class ReportToInvoiceConsignmentJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportToInvoiceConsignmentJR|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportToInvoiceConsignmentJR|DateTo",
          "");
      String strWarehouse = vars.getGlobalVariable("inpmWarehouseId",
          "ReportToInvoiceConsignmentJR|Warehouse", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportToInvoiceConsignmentJR|currency", strUserCurrencyId);
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strWarehouse, strCurrencyId);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportToInvoiceConsignmentJR|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportToInvoiceConsignmentJR|DateTo");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportToInvoiceConsignmentJR|Warehouse");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportToInvoiceConsignmentJR|currency", strUserCurrencyId);
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strWarehouse, strCurrencyId);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strWarehouse, String strCurrencyId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportToInvoiceConsignmentJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportToInvoiceConsignmentJR", false,
        "", "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportToInvoiceConsignmentJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportToInvoiceConsignmentJR.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportToInvoiceConsignmentJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportToInvoiceConsignmentJR");
      vars.removeMessage("ReportToInvoiceConsignmentJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("mWarehouseId", strWarehouse);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "M_Warehouse_ID",
          "M_Warehouse of Client", "", Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
          Utility.getContext(this, vars, "#User_Client", ""), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "", "");
      xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportToInvoiceConsignmentJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportToInvoiceConsignmentJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportToInvoiceConsignmentJR",
          strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strWarehouse,
      String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");

    response.setContentType("text/html; charset=UTF-8");

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    ReportToInvoiceConsignmentData[] data = null;
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    try {
      data = ReportToInvoiceConsignmentData.select(this, strCurrencyId,
          Utility.getContext(this, vars, "#User_Client", "ReportToInvoiceConsignmentJR"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportToInvoiceConsignmentJR"),
          strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strWarehouse);
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
      if (data == null || data.length == 0) {
        data = ReportToInvoiceConsignmentData.set();
      }
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportToInvoiceConsignmentJR.jrxml";
      String strOutput = "pdf";
      if (strOutput.equals("pdf"))
        response.setHeader("Content-disposition",
            "inline; filename=ReportToInvoiceConsignmentJR.pdf");

      String strSubTitle = "";
      strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom + " "
          + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("REPORT_SUBTITLE", strSubTitle);
      renderJR(vars, response, strReportName, strOutput, parameters, data, null);
    }
  }

  public String getServletInfo() {
    return "Servlet ReportToInvoiceConsignment. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
