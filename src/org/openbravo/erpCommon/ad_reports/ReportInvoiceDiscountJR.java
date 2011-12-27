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

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportInvoiceDiscountJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportInvoiceDiscountJR|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportInvoiceDiscountJR|dateTo", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportInvoiceDiscountJR|partner", "", IsIDFilter.instance);

      String strDiscount = vars.getGlobalVariable("inpDiscount",
          "ReportInvoiceDiscountJR|discount", "N");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportInvoiceDiscountJR|currency", strUserCurrencyId);
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strCurrencyId,
          strDiscount);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportInvoiceDiscountJR|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportInvoiceDiscountJR|dateTo", "");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportInvoiceDiscountJR|partner", IsIDFilter.instance);
      String strDiscount = vars.getRequestGlobalVariable("inpDiscount",
          "ReportInvoiceDiscountJR|discount");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportInvoiceDiscountJR|currency", strUserCurrencyId);
      printPageDataHtml(request, response, vars, strDateFrom, strDateTo, strcBpartnerId,
          strDiscount, strCurrencyId);
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId,
      String strDiscount, String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    if (strDiscount.equals(""))
      strDiscount = "N";

    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportInvoiceDiscountJR.jrxml";
    String strOutput = "html";
    if (strOutput.equals("pdf"))
      response.setHeader("Content-disposition", "inline; filename=ReportInvoiceDiscountEdit.pdf");

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    ReportInvoiceDiscountData[] data = null;
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    try {
      data = ReportInvoiceDiscountData.select(this, strCurrencyId,
          Utility.getContext(this, vars, "#User_Client", "ReportInvoiceDiscountJR"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoiceDiscountJR"),
          strDateFrom, strDateTo, strcBpartnerId, (strDiscount.equals("N")) ? "" : "discount");
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
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      String strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom
          + " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;
      parameters.put("REPORT_SUBTITLE", strSubTitle);

      renderJR(vars, response, strReportName, strOutput, parameters, data, null);
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strcBpartnerId, String strCurrencyId,
      String strDiscount) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    XmlDocument xmlDocument = null;
    if (strDiscount.equals(""))
      strDiscount = "N";
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportInvoiceDiscountJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
        "ReportInvoiceDiscountReportInvoiceDiscountJR", false, "", "", "", false, "ad_reports",
        strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportInvoiceDiscountJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportInvoiceDiscountJR.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportInvoiceDiscountJR.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInvoiceDiscountJR");
      vars.removeMessage("ReportInvoiceDiscountJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("discount", strDiscount);
    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", SelectorUtilityData
        .selectBpartner(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoiceDiscountJR"),
            Utility.getContext(this, vars, "#User_Client", "ReportInvoiceDiscountJR"),
            strcBpartnerId));

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoiceDiscountJR"),
          Utility.getContext(this, vars, "#User_Client", "ReportInvoiceDiscountJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoiceDiscountJR",
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

  public String getServletInfo() {
    return "Servlet ReportInvoiceDiscount. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
