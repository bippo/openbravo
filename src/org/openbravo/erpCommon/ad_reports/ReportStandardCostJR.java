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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

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

public class ReportStandardCostJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strdate = vars.getGlobalVariable("inpDateFrom", "ReportStandardCostJR|date", "");
      String strProcessPlan = vars.getGlobalVariable("inpmaProcessPlanId",
          "ReportStandardCostJR|ProcessPlanID", "");
      String strVersion = vars.getGlobalVariable("inpmaProcessPlanVersionId",
          "ReportStandardCostJR|versionID", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportStandardCostJR|currency", Utility.stringBaseCurrencyId(this, vars.getClient()));
      printPageDataSheet(response, vars, strdate, strProcessPlan, strVersion, strCurrencyId);
    } else if (vars.commandIn("PRINT_HTML")) {
      String strdate = vars.getRequestGlobalVariable("inpDateFrom", "ReportStandardCostJR|date");
      String strProcessPlan = vars.getRequestGlobalVariable("inpmaProcessPlanId",
          "ReportStandardCostJR|ProcessPlanID");
      String strVersion = vars.getRequestGlobalVariable("inpmaProcessPlanVersionId",
          "ReportStandardCostJR|versionID");
      String strCurrencyId = vars.getRequiredGlobalVariable("inpCurrencyId",
          "ReportStandardCostJR|currency");
      printPageHtml(response, vars, strdate, strProcessPlan, strVersion, strCurrencyId, "html");
    } else if (vars.commandIn("PRINT_PDF")) {
      String strdate = vars.getRequestGlobalVariable("inpDateFrom", "ReportStandardCostJR|date");
      String strProcessPlan = vars.getRequestGlobalVariable("inpmaProcessPlanId",
          "ReportStandardCostJR|ProcessPlanID");
      String strVersion = vars.getRequestGlobalVariable("inpmaProcessPlanVersionId",
          "ReportStandardCostJR|versionID");
      String strCurrencyId = vars.getRequiredGlobalVariable("inpCurrencyId",
          "ReportStandardCostJR|currency");
      printPageHtml(response, vars, strdate, strProcessPlan, strVersion, strCurrencyId, "pdf");
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strdate, String strProcessPlan, String strVersion, String strCurrencyId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportStandardCostJRFilter").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportStandardCostJRFilter", false,
        "", "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportStandardCostJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportStandardCostJR.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportStandardCostJR.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportSalesDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportSalesDimensionalAnalyzeJR", strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    {
      OBError myMessage = vars.getMessage("ReportStandardCostJR");
      vars.removeMessage("ReportStandardCostJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("date", strdate);
    xmlDocument.setParameter("datedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("datesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setData(
        "reportMA_PROCESSPLAN",
        "liststructure",
        ProcessPlanComboData.select(this,
            Utility.getContext(this, vars, "#User_Client", "ReportStandardCostJR"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportStandardCostJR")));
    xmlDocument.setParameter(
        "standardCostReports",
        Utility.arrayDobleEntrada(
            "arrStandardCostReports",
            ProcessPlanVersionComboData.select(this,
                Utility.getContext(this, vars, "#User_Client", "ReportStandardCostJR"),
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportStandardCostJR"))));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strdate,
      String strProcessPlan, String strVersion, String strCurrencyId, String strOutput)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");
    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("MA_PROCESSPLAN_ID", strProcessPlan);
    parameters.put("MA_PROCESSPLAN_VERSION_ID", strVersion);
    parameters.put("CURRENCY_ID", strCurrencyId);
    parameters.put("BASE_CURRENCY_ID", Utility.stringBaseCurrencyId(this, vars.getClient()));
    JasperReport jasperReportCost;
    JasperReport jasperReportProduced;
    try {
      JasperDesign jasperDesignCost = JRXmlLoader.load(strBaseDesign
          + "/org/openbravo/erpCommon/ad_reports/ReportStandardCostsJR_srptcosts.jrxml");
      JasperDesign jasperDesignProduced = JRXmlLoader.load(strBaseDesign
          + "/org/openbravo/erpCommon/ad_reports/ReportStandardCostsJR_subreport0.jrxml");
      jasperReportCost = JasperCompileManager.compileReport(jasperDesignCost);
      jasperReportProduced = JasperCompileManager.compileReport(jasperDesignProduced);
    } catch (JRException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }
    parameters.put("SR_COST", jasperReportCost);
    parameters.put("SR_PRODUCED", jasperReportProduced);

    if (strdate != null && !strdate.equals("")) {
      String strDateFormat;
      strDateFormat = vars.getJavaDateFormat();
      SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
      Date date = null;
      try {
        date = dateFormat.parse(strdate);
      } catch (Exception e) {
        throw new ServletException(e.getMessage());
      }
      parameters.put("DATEFROM", date);
      parameters.put("DATETO", date);
    }
    String strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportStandardCostsJR.jrxml";
    renderJR(vars, response, strReportPath, strOutput, parameters, null, null);
  }

  public String getServletInfo() {
    return "Servlet ReportStandardCostJRFilter.";
  } // end of getServletInfo() method
}
