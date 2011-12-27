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

public class ReportInvoiceVendorJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getStringParameter("inpDateFrom", "");
      String strDateTo = vars.getStringParameter("inpDateTo", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportInvoiceVendorJR|currency", strUserCurrencyId);
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strCurrencyId);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strcProjectId = vars.getStringParameter("inpcProjectId");
      String strissotrx = "N";
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportInvoiceVendorJR|currency", strUserCurrencyId);
      printPageHtml(request, response, vars, strDateFrom, strDateTo, strcBpartnetId, strcProjectId,
          strissotrx, strCurrencyId);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strCurrencyId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportInvoiceVendorFilterJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportInvoiceVendorFilter", false, "",
        "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportInvoiceVendorJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportInvoiceVendorFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportInvoiceVendorFilterJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInvoiceVendorJR");
      vars.removeMessage("ReportInvoiceVendorJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerId", "");
    xmlDocument.setParameter("mWarehouseId", "");
    xmlDocument.setParameter("cProjectId", "");
    xmlDocument.setParameter("projectName", "");
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "InvoiceVendorJR"),
          Utility.getContext(this, vars, "#User_Client", "InvoiceVendorJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "InvoiceVendorFilter", "");
      xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoiceVendorJR"),
          Utility.getContext(this, vars, "#User_Client", "ReportInvoiceVendorJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoiceVendorJR",
          strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageHtml(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnetId,
      String strcProjectId, String strissotrx, String strCurrencyId) throws IOException,
      ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");

    InvoiceEditionData[] data = null;

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    try {
      if (strcProjectId != null && !strcProjectId.equals(""))
        data = InvoiceEditionData.selectProject(this, strCurrencyId,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "InvoiceVendorJR"),
            Utility.getContext(this, vars, "#User_Client", "InvoiceVendorJR"), strDateFrom,
            DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnetId, strcProjectId,
            strissotrx);
      else
        data = InvoiceEditionData.select(this, strCurrencyId,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "InvoiceVendorJR"),
            Utility.getContext(this, vars, "#User_Client", "InvoiceVendorJR"), strDateFrom,
            DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnetId, strcProjectId,
            strissotrx);
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
      String strOutput = vars.commandIn("EDIT_HTML") ? "html" : "pdf";
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportInvoiceVendorJR.jrxml";

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      // parameters.put("Subtitle",strSubtitle);
      renderJR(vars, response, strReportName, strOutput, parameters, data, null);
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
