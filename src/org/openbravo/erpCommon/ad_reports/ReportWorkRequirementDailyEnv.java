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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class ReportWorkRequirementDailyEnv extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strStartDateFrom = vars.getGlobalVariable("inpStartDateFrom",
          "ReportWorkRequirementDailyEnv|StartDateFrom", "");
      String strStartDateTo = vars.getGlobalVariable("inpStartDateTo",
          "ReportWorkRequirementDailyEnv|StartDateTo", "");
      String strmaProcessPlan = vars.getGlobalVariable("inpmaProcessPlanId",
          "ReportWorkRequirementDailyEnv|MA_ProcessPlan_ID", "");
      strStartDateTo = DateTimeData.today(this);
      strStartDateFrom = DateTimeData.today(this);
      printPageDataSheet(response, vars, strStartDateFrom, strStartDateTo, strmaProcessPlan);
    } else if (vars.commandIn("FIND")) {
      String strStartDateFrom = vars.getRequestGlobalVariable("inpStartDateFrom",
          "ReportWorkRequirementDailyEnv|StartDateFrom");
      String strStartDateTo = vars.getRequestGlobalVariable("inpStartDateTo",
          "ReportWorkRequirementDailyEnv|StartDateTo");
      String strmaProcessPlan = vars.getRequestGlobalVariable("inpmaProcessPlanId",
          "ReportWorkRequirementDailyEnv|MA_ProcessPlan_ID");
      printPageDataHtml(response, vars, strStartDateFrom, strStartDateTo, strmaProcessPlan);
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strStartDateFrom, String strStartDateTo, String strmaProcessPlan) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportWorkRequirementDailyEnvData[] data = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDailyEnvEdit").createXmlDocument();
    data = ReportWorkRequirementDailyEnvData.select(this, vars.getLanguage(),
        Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDailyEnv"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportWorkRequirementDailyEnv"),
        strStartDateFrom, strStartDateTo, strmaProcessPlan);

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
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
        "org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDailyEnv").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWorkRequirementDailyEnv", false,
        "", "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportWorkRequirementDailyEnv");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportWorkRequirementDailyEnv.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportWorkRequirementDailyEnv.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportWorkRequirementDailyEnv");
      vars.removeMessage("ReportWorkRequirementDailyEnv");
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
    xmlDocument.setData("reportMA_PROCESSPLAN", "liststructure", ProcessPlanComboData.select(this,
        Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDailyEnv"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportWorkRequirementDailyEnv")));

    out.println(xmlDocument.print());
    out.close();
  }

  /*
   * void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String
   * strStartDateFrom, String strStartDateTo, String strmaProcessPlan) throws IOException,
   * ServletException { if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
   * response.setContentType("text/html; charset=UTF-8"); PrintWriter out = response.getWriter();
   * XmlDocument xmlDocument=null; ReportWorkRequirementDailyEnvData[] data=null; xmlDocument =
   * xmlEngine.readXmlTemplate ("org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDailyEnv"
   * ).createXmlDocument(); data = ReportWorkRequirementDailyEnvData.select(this,
   * vars.getLanguage(), Utility.getContext(this, vars, "#User_Client",
   * "ReportWorkRequirementDailyEnv"), Utility.getContext(this, vars, "#AccessibleOrgTree",
   * "ReportWorkRequirementDailyEnv"), strStartDateFrom, strStartDateTo, strmaProcessPlan);
   * 
   * ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWorkRequirementDailyEnv", false,
   * "", "", "",false, "ad_reports", strReplaceWith, false, true);
   * toolbar.prepareSimpleToolBarTemplate(); xmlDocument.setParameter("toolbar",
   * toolbar.toString());
   * 
   * try { WindowTabs tabs = new WindowTabs(this, vars,
   * "org.openbravo.erpCommon.ad_reports.ReportWorkRequirementDailyEnv");
   * xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
   * xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
   * xmlDocument.setParameter("childTabContainer", tabs.childTabs());
   * xmlDocument.setParameter("theme", vars.getTheme()); NavigationBar nav = new NavigationBar(this,
   * vars.getLanguage(), "ReportWorkRequirementDailyEnv.html", classInfo.id, classInfo.type,
   * strReplaceWith, tabs.breadcrumb()); xmlDocument.setParameter("navigationBar", nav.toString());
   * LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
   * "ReportWorkRequirementDailyEnv.html", strReplaceWith); xmlDocument.setParameter("leftTabs",
   * lBar.manualTemplate()); } catch (Exception ex) { throw new ServletException(ex); } { OBError
   * myMessage = vars.getMessage("ReportWorkRequirementDailyEnv");
   * vars.removeMessage("ReportWorkRequirementDailyEnv"); if (myMessage!=null) {
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
   * Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDailyEnv"),
   * Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportWorkRequirementDailyEnv")));
   * xmlDocument.setData("structure1", data); out.println(xmlDocument.print()); out.close(); }
   */

  public String getServletInfo() {
    return "Servlet ReportWorkRequirementDailyEnv.";
  } // end of getServletInfo() method
}
