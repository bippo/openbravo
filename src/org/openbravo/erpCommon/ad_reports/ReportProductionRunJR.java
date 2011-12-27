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
 * All portions are Copyright (C) 2007-2010 Openbravo SLU 
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
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportProductionRunJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strLaunchDateFrom = vars.getGlobalVariable("inpLaunchDateFrom",
          "ReportProductionRunJR|LaunchDateFrom", "");
      String strLaunchDateTo = vars.getGlobalVariable("inpLaunchDateTo",
          "ReportProductionRunJR|LaunchDateTo", "");
      String strStartDateFrom = vars.getGlobalVariable("inpStartDateFrom",
          "ReportProductionRunJR|StartDateFrom", "");
      String strStartDateTo = vars.getGlobalVariable("inpStartDateTo",
          "ReportProductionRunJR|StartDateTo", "");
      String strEndDateFrom = vars.getGlobalVariable("inpEndDateFrom",
          "ReportProductionRunJR|EndDateFrom", "");
      String strEndDateTo = vars.getGlobalVariable("inpEndDateTo",
          "ReportProductionRunJR|EndDateTo", "");
      String strmaWorkRequirement = vars.getGlobalVariable("inpmaWorkRequirementId",
          "ReportProductionRunJR|maWorkRequirement", "");
      printPageDataSheet(response, vars, strLaunchDateFrom, strLaunchDateTo, strStartDateFrom,
          strStartDateTo, strEndDateFrom, strEndDateTo, strmaWorkRequirement);
    } else if (vars.commandIn("PRINT_HTML")) {
      String strLaunchDateFrom = vars.getRequestGlobalVariable("inpLaunchDateFrom",
          "ReportProductionRunJR|LaunchDateFrom");
      String strLaunchDateTo = vars.getRequestGlobalVariable("inpLaunchDateTo",
          "ReportProductionRunJR|LaunchDateTo");
      String strStartDateFrom = vars.getRequestGlobalVariable("inpStartDateFrom",
          "ReportProductionRunJR|StartDateFrom");
      String strStartDateTo = vars.getRequestGlobalVariable("inpStartDateTo",
          "ReportProductionRunJR|StartDateTo");
      String strEndDateFrom = vars.getRequestGlobalVariable("inpEndDateFrom",
          "ReportProductionRunJR|EndDateFrom");
      String strEndDateTo = vars.getRequestGlobalVariable("inpEndDateTo",
          "ReportProductionRunJR|EndDateTo");
      String strmaWorkRequirement = vars.getRequestGlobalVariable("inpmaWorkRequirementId",
          "ReportProductionRunJR|maWorkRequirement");
      printPageDataHtml(response, vars, strLaunchDateFrom, strLaunchDateTo, strStartDateFrom,
          strStartDateTo, strEndDateFrom, strEndDateTo, strmaWorkRequirement, "html");
    } else if (vars.commandIn("PRINT_PDF")) {
      String strLaunchDateFrom = vars.getRequestGlobalVariable("inpLaunchDateFrom",
          "ReportProductionRunJR|LaunchDateFrom");
      String strLaunchDateTo = vars.getRequestGlobalVariable("inpLaunchDateTo",
          "ReportProductionRunJR|LaunchDateTo");
      String strStartDateFrom = vars.getRequestGlobalVariable("inpStartDateFrom",
          "ReportProductionRunJR|StartDateFrom");
      String strStartDateTo = vars.getRequestGlobalVariable("inpStartDateTo",
          "ReportProductionRunJR|StartDateTo");
      String strEndDateFrom = vars.getRequestGlobalVariable("inpEndDateFrom",
          "ReportProductionRunJR|EndDateFrom");
      String strEndDateTo = vars.getRequestGlobalVariable("inpEndDateTo",
          "ReportProductionRunJR|EndDateTo");
      String strmaWorkRequirement = vars.getRequestGlobalVariable("inpmaWorkRequirementId",
          "ReportProductionRunJR|maWorkRequirement");
      printPageDataHtml(response, vars, strLaunchDateFrom, strLaunchDateTo, strStartDateFrom,
          strStartDateTo, strEndDateFrom, strEndDateTo, strmaWorkRequirement, "pdf");
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strLaunchDateFrom, String strLaunchDateTo, String strStartDateFrom,
      String strStartDateTo, String strEndDateFrom, String strEndDateTo,
      String strmaWorkRequirement, String strOutput) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataHtmlJR");

    ReportProductionRunData[] data = null;
    data = ReportProductionRunData.select(this, vars.getLanguage(),
        Utility.getContext(this, vars, "#User_Client", "ReportProductionRunJR"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductionRunJR"),
        strLaunchDateFrom, strLaunchDateTo, strStartDateFrom, strStartDateTo, strEndDateFrom,
        strEndDateTo, strmaWorkRequirement);

    String strSubtitle = "";
    if (!strLaunchDateFrom.equals(""))
      strSubtitle = Utility.messageBD(this, "LaunchDateFrom", vars.getLanguage()) + ":"
          + strLaunchDateFrom;
    if (!strLaunchDateTo.equals(""))
      strSubtitle += strSubtitle.equals("") ? "" : " - "
          + Utility.messageBD(this, "LaunchDateTo", vars.getLanguage()) + ":" + strLaunchDateTo;
    if (!strStartDateFrom.equals(""))
      strSubtitle += strSubtitle.equals("") ? "" : " - "
          + Utility.messageBD(this, "StartDateFrom", vars.getLanguage()) + ":" + strStartDateFrom;
    if (!strStartDateTo.equals(""))
      strSubtitle += strSubtitle.equals("") ? "" : " - "
          + Utility.messageBD(this, "StartDateTo", vars.getLanguage()) + ":" + strStartDateTo;
    if (!strEndDateFrom.equals(""))
      strSubtitle += strSubtitle.equals("") ? "" : " - "
          + Utility.messageBD(this, "EndDateFrom", vars.getLanguage()) + ":" + strEndDateFrom;
    if (!strmaWorkRequirement.equals(""))
      strSubtitle += strSubtitle.equals("") ? "" : " - "
          + Utility.messageBD(this, "WorkRequirement", vars.getLanguage()) + ":"
          + strmaWorkRequirement;

    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportProductionRun.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strLaunchDateFrom, String strLaunchDateTo, String strStartDateFrom,
      String strStartDateTo, String strEndDateFrom, String strEndDateTo, String strmaWorkRequirement)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportProductionRunJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProductionRunJR", false, "", "",
        "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportProductionRunJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportProductionRunJR.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportProductionRunJR.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportProductionRunJR");
      vars.removeMessage("ReportProductionRunJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("maWorkRequirement", strmaWorkRequirement);
    xmlDocument.setParameter("launchDateFrom", strLaunchDateFrom);
    xmlDocument.setParameter("launchDateFromdisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("launchDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("launchDateTo", strLaunchDateTo);
    xmlDocument
        .setParameter("launchDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("launchDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("startDateFrom", strStartDateFrom);
    xmlDocument.setParameter("startDateFromdisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("startDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("startDateTo", strStartDateTo);
    xmlDocument.setParameter("startDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("startDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("endDateFrom", strEndDateFrom);
    xmlDocument.setParameter("endDateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("endDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("endDateTo", strEndDateTo);
    xmlDocument.setParameter("endDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("endDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "MA_Workrequirement_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportProductionRunJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportProductionRunJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProductionRunJR",
          strmaWorkRequirement);
      xmlDocument
          .setData("reportMA_WORKREQUIREMENT", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  /*
   * void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String
   * strLaunchDateFrom, String strLaunchDateTo, String strStartDateFrom, String strStartDateTo,
   * String strEndDateFrom, String strEndDateTo, String strmaWorkRequirement) throws IOException,
   * ServletException { if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
   * response.setContentType("text/html; charset=UTF-8"); PrintWriter out = response.getWriter();
   * //String discard[]={"sectionAmount"}; XmlDocument xmlDocument=null; ReportProductionRunData[]
   * data=null; xmlDocument = xmlEngine
   * .readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportProductionRun"
   * ).createXmlDocument(); data = ReportProductionRunData.select(this, Utility.getContext(this,
   * vars, "#User_Client", "ReportProductionRun"), Utility.getContext(this, vars,
   * "#AccessibleOrgTree", "ReportProductionRun"), strLaunchDateFrom, strLaunchDateTo,
   * strStartDateFrom, strStartDateTo, strEndDateFrom, strEndDateTo, strmaWorkRequirement);
   * 
   * ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProductionRun", false, "", "",
   * "",false, "ad_reports", strReplaceWith, false, true); toolbar.prepareSimpleToolBarTemplate();
   * xmlDocument.setParameter("toolbar", toolbar.toString());
   * 
   * try { WindowTabs tabs = new WindowTabs(this, vars,
   * "org.openbravo.erpCommon.ad_reports.ReportProductionRun");
   * xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
   * xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
   * xmlDocument.setParameter("childTabContainer", tabs.childTabs());
   * xmlDocument.setParameter("theme", vars.getTheme()); NavigationBar nav = new NavigationBar(this,
   * vars.getLanguage(), "ReportProductionRun.html", classInfo.id, classInfo.type, strReplaceWith,
   * tabs.breadcrumb()); xmlDocument.setParameter("navigationBar", nav.toString()); LeftTabsBar lBar
   * = new LeftTabsBar(this, vars.getLanguage(), "ReportProductionRun.html", strReplaceWith);
   * xmlDocument.setParameter("leftTabs", lBar.manualTemplate()); } catch (Exception ex) { throw new
   * ServletException(ex); } { OBError myMessage = vars.getMessage("ReportProductionRun");
   * vars.removeMessage("ReportProductionRun"); if (myMessage!=null) {
   * xmlDocument.setParameter("messageType", myMessage.getType());
   * xmlDocument.setParameter("messageTitle", myMessage.getTitle());
   * xmlDocument.setParameter("messageMessage", myMessage.getMessage()); } }
   * 
   * xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
   * xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
   * xmlDocument.setParameter("maWorkRequirement", strmaWorkRequirement);
   * xmlDocument.setParameter("launchDateFrom", strLaunchDateFrom);
   * xmlDocument.setParameter("launchDateTo", strLaunchDateTo);
   * xmlDocument.setParameter("startDateFrom", strStartDateFrom);
   * xmlDocument.setParameter("startDateTo", strStartDateTo);
   * xmlDocument.setParameter("endDateFrom", strEndDateFrom); xmlDocument.setParameter("endDateTo",
   * strEndDateTo); xmlDocument.setData("reportMA_WORKREQUIREMENT", "liststructure",
   * WorkRequirementComboData.select(this, Utility.getContext(this, vars, "#User_Client",
   * "ReportProductionRun"), Utility.getContext(this, vars, "#AccessibleOrgTree",
   * "ReportProductionRun"))); xmlDocument.setData("structure1", data);
   * out.println(xmlDocument.print()); out.close(); }
   */
  public String getServletInfo() {
    return "Servlet ReportProductionRunJR.";
  } // end of getServletInfo() method
}
