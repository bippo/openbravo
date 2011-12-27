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

public class ReportExpense extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportExpense|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportExpense|dateTo", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId", "ReportExpense|cBpartnerId",
          "");
      String strPartner = vars.getGlobalVariable("inpUser", "ReportExpense|partner", "");
      String strProject = vars.getGlobalVariable("inpcProjectId", "ReportExpense|project", "");
      String strExpense = vars.getGlobalVariable("inpExpense", "ReportExpense|expense", "all");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportExpense|currency",
          strUserCurrencyId);
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strPartner,
          strProject, strExpense, strCurrencyId);
    } else if (vars.commandIn("DIRECT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportExpense|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportExpense|dateTo", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId", "ReportExpense|cBpartnerId",
          "");
      String strPartner = vars.getGlobalVariable("inpUser", "ReportExpense|partner", "");
      String strProject = vars.getGlobalVariable("inpcProjectId", "ReportExpense|project", "");
      String strExpense = vars.getGlobalVariable("inpExpense", "ReportExpense|expense", "");
      setHistoryCommand(request, "DIRECT");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportExpense|currency",
          strUserCurrencyId);
      printPageDataHtml(request, response, vars, strDateFrom, strDateTo, strcBpartnerId,
          strPartner, strProject, strExpense, strCurrencyId);
    } else if (vars.commandIn("FIND") || vars.commandIn("PDF")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportExpense|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportExpense|dateTo");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpcBPartnerId",
          "ReportExpense|cBpartnerId");
      String strPartner = vars.getRequestGlobalVariable("inpUser", "ReportExpense|partner");
      String strProject = vars.getRequestGlobalVariable("inpcProjectId", "ReportExpense|project");
      String strExpense = vars.getStringParameter("inpExpense");
      setHistoryCommand(request, "DIRECT");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportExpense|currency",
          strUserCurrencyId);
      String strOutput = "html";
      if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        printPageDataPDF(request, response, vars, strDateFrom, strDateTo, strcBpartnerId,
            strPartner, strProject, strExpense, strCurrencyId, strOutput);

      } else
        printPageDataHtml(request, response, vars, strDateFrom, strDateTo, strcBpartnerId,
            strPartner, strProject, strExpense, strCurrencyId);
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId,
      String strPartner, String strProject, String strExpense, String strCurrencyId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportExpenseData[] data1 = null;
    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();

    if (vars.commandIn("DEFAULT") && strDateFrom.equals("") && strDateTo.equals("")
        && strcBpartnerId.equals("") && strPartner.equals("")) {
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strPartner,
          strProject, strExpense, strCurrencyId);
    } else {
      String strBaseCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
      try {
        data1 = ReportExpenseData.select(this, strCurrencyId, strBaseCurrencyId,
            vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportExpense"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportExpense"), strDateFrom,
            DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId, strPartner, strProject,
            (strExpense.equals("time") ? "Y" : ""), (strExpense.equals("expense") ? "N" : ""));
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    }
    for (int i = 0; i < data1.length; i++) {
      if (data1[i].horas != null && !data1[i].horas.equals("")
          && !data1[i].horas.trim().equals("0") && data1[i].cuomid != null
          && !data1[i].cuomid.equals("")) {
        String count = ReportExpenseData.selectUOM(this, data1[i].cuomid);
        String count2 = ReportExpenseData.selectUOM2(this, data1[i].cuomid);
        if (Integer.parseInt(count) + Integer.parseInt(count2) == 0) {
          advisePopUp(request, response, "ERROR",
              Utility.messageBD(this, "Error", vars.getLanguage()),
              Utility.messageBD(this, "NoConversionDayUom", vars.getLanguage()));
        }
      }
    }
    strConvRateErrorMsg = myMessage.getMessage();
    // If a conversion rate is missing for a certain transaction, an error
    // message window pops-up.
    if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
          strConvRateErrorMsg);
    } else { // Otherwise, the report is launched
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportExpenseEdit").createXmlDocument();

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());

      xmlDocument.setData("structure1", data1);

      out.println(xmlDocument.print());
      out.close();
    }
  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId,
      String strPartner, String strProject, String strExpense, String strCurrencyId,
      String strOutput) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");
    String discard[] = { "sectionPartner" };

    ReportExpenseData[] data1 = null;

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    if (vars.commandIn("DEFAULT") && strDateFrom.equals("") && strDateTo.equals("")
        && strcBpartnerId.equals("") && strPartner.equals("")) {
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strPartner,
          strProject, strExpense, strCurrencyId);
    } else {
      String strBaseCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
      try {
        data1 = ReportExpenseData.select(this, strCurrencyId, strBaseCurrencyId,
            vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportExpense"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportExpense"), strDateFrom,
            DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId, strPartner, strProject,
            (strExpense.equals("time") ? "Y" : ""), (strExpense.equals("expense") ? "N" : ""));
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    }
    for (int i = 0; i < data1.length; i++) {
      if (data1[i].horas != null && !data1[i].horas.equals("")
          && !data1[i].horas.trim().equals("0") && data1[i].cuomid != null
          && !data1[i].cuomid.equals("")) {
        String count = ReportExpenseData.selectUOM(this, data1[i].cuomid);
        String count2 = ReportExpenseData.selectUOM2(this, data1[i].cuomid);
        if (Integer.parseInt(count) + Integer.parseInt(count2) == 0) {
          advisePopUp(request, response, "ERROR",
              Utility.messageBD(this, "Error", vars.getLanguage()),
              Utility.messageBD(this, "NoConversionDayUom", vars.getLanguage()));
        }
      }
    }
    strConvRateErrorMsg = myMessage.getMessage();
    // If a conversion rate is missing for a certain transaction, an error
    // message window pops-up.
    if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
          strConvRateErrorMsg);
    } else { // Launch the report as usual, calling the JRXML file
      if (data1 == null || data1.length == 0) {
        discard[0] = "selEliminar";
        data1 = ReportExpenseData.set();
      }
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportExpense.jrxml";

      if (strOutput.equals("pdf"))
        response.setHeader("Content-disposition",
            "inline; filename=ReportProjectBuildingSiteJR.pdf");

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      String strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom
          + " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;
      parameters.put("REPORT_SUBTITLE", strSubTitle);

      renderJR(vars, response, strReportName, strOutput, parameters, data1, null);
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strcBpartnerId, String strPartner,
      String strProject, String strExpense, String strCurrencyId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportExpense")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportExpense", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportExpense");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportExpense.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportExpense.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportExpense");
      vars.removeMessage("ReportExpense");
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
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("bPartnerDescription",
        ReportExpenseData.selectBpartner(this, strcBpartnerId));
    xmlDocument.setParameter("partner", strPartner);
    xmlDocument.setParameter("project", strProject);
    xmlDocument.setParameter("time", strExpense);
    xmlDocument.setParameter("expense", strExpense);
    xmlDocument.setParameter("all", strExpense);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_BPartner_ID",
          "C_BPartner Employee w Address", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportExpense"), Utility.getContext(this, vars, "#User_Client", "ReportExpense"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportExpense", "");

      ComboTableData comboTableDataProject = new ComboTableData(this, "TABLE", "C_Project_ID",
          "C_Project", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportExpense"),
          Utility.getContext(this, vars, "#User_Client", "ReportExpense"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableDataProject, "ReportExpense", "");

      xmlDocument.setData("reportC_BPartner_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportExpense"),
          Utility.getContext(this, vars, "#User_Client", "ReportExpense"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportExpense", strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportExpense. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
