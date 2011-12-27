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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportWorkRequirementDaily extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strStartDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportWorkRequirementDaily|dateFrom", "");
      String strStartDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportWorkRequirementDaily|dateTo", "");
      String strmaProcessPlan = vars.getGlobalVariable("inpmaProcessPlanId",
          "ReportWorkRequirementDaily|MA_ProcessPlan_ID", "");
      strStartDateTo = DateTimeData.today(this);
      strStartDateFrom = DateTimeData.today(this);
      printPageDataSheet(response, vars, strStartDateFrom, strStartDateTo, strmaProcessPlan);
    } else if (vars.commandIn("PRINT_HTML")) {
      String strStartDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportWorkRequirementDaily|dateFrom");
      String strStartDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportWorkRequirementDaily|dateTo");
      String strmaProcessPlan = vars.getRequestGlobalVariable("inpmaProcessPlanId",
          "ReportWorkRequirementDaily|MA_ProcessPlan_ID");
      printPageDataHtml(response, vars, strStartDateFrom, strStartDateTo, strmaProcessPlan, "html");
    } else if (vars.commandIn("PRINT_PDF")) {
      String strStartDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportWorkRequirementDaily|dateFrom");
      String strStartDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportWorkRequirementDaily|dateTo");
      String strmaProcessPlan = vars.getRequestGlobalVariable("inpmaProcessPlanId",
          "ReportWorkRequirementDaily|MA_ProcessPlan_ID");
      printPageDataHtml(response, vars, strStartDateFrom, strStartDateTo, strmaProcessPlan, "pdf");
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strStartDateFrom, String strStartDateTo, String strmaProcessPlan, String strOutput)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    ReportWorkRequirementDailyData[] data = null;

    data = ReportWorkRequirementDailyData.select(this, vars.getLanguage(),
        Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDaily"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportWorkRequirementDaily"),
        strStartDateFrom, strStartDateTo, strmaProcessPlan);

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);
    JasperReport jasperReportProducts;
    try {
      jasperReportProducts = Utility.getTranslatedJasperReport(this, strBaseDesign
          + "/org/openbravo/erpCommon/ad_reports/SubreportWorkRequirementDaily.jrxml",
          vars.getLanguage(), strBaseDesign);
    } catch (JRException e) {
      log4j.error("Could not load/compile jrxml-file", e);
      throw new ServletException(e);
    }

    JasperReport jasperReportProducts2;
    try {
      jasperReportProducts2 = Utility.getTranslatedJasperReport(this, strBaseDesign
          + "/org/openbravo/erpCommon/ad_reports/SubreportWorkRequirementDaily2.jrxml",
          vars.getLanguage(), strBaseDesign);
    } catch (JRException e) {
      log4j.error("Could not load/compile jrxml-file", e);
      throw new ServletException(e);
    }

    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDailyEdit.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("PRODUCTS", jasperReportProducts);
    parameters.put("PRODUCTS2", jasperReportProducts2);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null);

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strStartDateFrom, String strStartDateTo, String strmaProcessPlan) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDaily").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWorkRequirementDaily", false,
        "", "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportWorkRequirementDaily");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportWorkRequirementDaily.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportWorkRequirementDaily.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportWorkRequirementDaily");
      vars.removeMessage("ReportWorkRequirementDaily");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("maProcessPlan", strmaProcessPlan);
    xmlDocument.setParameter("dateFrom", strStartDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strStartDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setData(
        "reportMA_PROCESSPLAN",
        "liststructure",
        ProcessPlanComboData.select(this,
            Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDaily"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportWorkRequirementDaily")));

    out.println(xmlDocument.print());
    out.close();
  }

  /*
   * void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String
   * strStartDateFrom, String strStartDateTo, String strmaProcessPlan) throws IOException,
   * ServletException { if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
   * response.setContentType("text/html; charset=UTF-8"); PrintWriter out = response.getWriter();
   * XmlDocument xmlDocument=null; ReportWorkRequirementDailyData[] data=null; xmlDocument =
   * xmlEngine.readXmlTemplate ("org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDaily"
   * ).createXmlDocument(); data = ReportWorkRequirementDailyData.select(this,
   * Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDaily"),
   * Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportWorkRequirementDaily"),
   * strStartDateFrom, strStartDateTo, strmaProcessPlan); for (int i=0; i<data.length; i++) {
   * ReportWorkRequirementDailyData[] product = ReportWorkRequirementDailyData.producedproduct(this,
   * data[i].wrpid); data[i].prodproduct = product[0].name; String strqty =
   * ReportWorkRequirementDailyData.inprocess(this, data[i].wrid, data[i].productid);
   * data[i].inprocess = strqty; if (strqty != null && strqty.equals("")) { strqty = "0"; } }
   * 
   * ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWorkRequirementDaily", false,
   * "", "", "",false, "ad_reports", strReplaceWith, false, true);
   * toolbar.prepareSimpleToolBarTemplate(); xmlDocument.setParameter("toolbar",
   * toolbar.toString());
   * 
   * try { WindowTabs tabs = new WindowTabs(this, vars,
   * "org.openbravo.erpCommon.ad_reports.ReportWorkRequirementDaily");
   * xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
   * xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
   * xmlDocument.setParameter("childTabContainer", tabs.childTabs());
   * xmlDocument.setParameter("theme", vars.getTheme()); NavigationBar nav = new NavigationBar(this,
   * vars.getLanguage(), "ReportWorkRequirementDaily.html", classInfo.id, classInfo.type,
   * strReplaceWith, tabs.breadcrumb()); xmlDocument.setParameter("navigationBar", nav.toString());
   * LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportWorkRequirementDaily.html",
   * strReplaceWith); xmlDocument.setParameter("leftTabs", lBar.manualTemplate()); } catch
   * (Exception ex) { throw new ServletException(ex); } { OBError myMessage =
   * vars.getMessage("ReportWorkRequirementDaily");
   * vars.removeMessage("ReportWorkRequirementDaily"); if (myMessage!=null) {
   * xmlDocument.setParameter("messageType", myMessage.getType());
   * xmlDocument.setParameter("messageTitle", myMessage.getTitle());
   * xmlDocument.setParameter("messageMessage", myMessage.getMessage()); } }
   * 
   * xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
   * xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
   * xmlDocument.setParameter("maProcessPlan", strmaProcessPlan);
   * xmlDocument.setParameter("startDateFrom", strStartDateFrom);
   * xmlDocument.setParameter("startDateTo", strStartDateTo);
   * xmlDocument.setData("reportMA_PROCESSPLAN", "liststructure", ProcessPlanComboData.select(this,
   * Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDaily"),
   * Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportWorkRequirementDaily")));
   * xmlDocument.setData("structure1", data); out.println(xmlDocument.print()); out.close(); }
   */
  public String getServletInfo() {
    return "Servlet ReportWorkRequirementDaily.";
  } // end of getServletInfo() method
}
