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
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportProductionJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportProductionJR|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportProductionJR|DateTo", "");
      String strRawMaterial = vars.getGlobalVariable("inpRawMaterial",
          "ReportProductionJR|RawMaterial", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strRawMaterial);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportProductionJR|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportProductionJR|DateTo");
      String strRawMaterial = vars.getRequestGlobalVariable("inpRawMaterial",
          "ReportProductionJR|RawMaterial");
      printPagePDF(response, vars, strDateFrom, strDateTo, strRawMaterial);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strRawMaterial) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportProductionJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProduction", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportProductionJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportProductionJR.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportProductionJR.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportProductionJR");
      vars.removeMessage("ReportProductionJR");
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
    xmlDocument.setParameter("rawMaterial", strRawMaterial);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strRawMaterial) throws IOException,
      ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: Jasper Report : Production Report");

    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/productionReport.jrxml";
    response.setHeader("Content-disposition", "inline; filename=ProductionReportJR.pdf");

    String strTitle = "Production Report";
    String strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom
        + " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;

    // String strSubTitle = (!strDateFrom.equals("")?"From "+strDateFrom:"")
    // + (!strDateTo.equals("")?" to "+strDateTo:"");

    if (!strRawMaterial.equals("Y"))
      strRawMaterial = "N";

    ReportProductionData[] data = ReportProductionData.select(this, strRawMaterial,
        Utility.getContext(this, vars, "#User_Client", "ReportProductionJR"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductionJR"), strDateFrom,
        DateTimeData.nDaysAfter(this, strDateTo, "1"));

    if (data == null || data.length == 0) {
      data = ReportProductionData.set();
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    if (log4j.isDebugEnabled())
      log4j.debug("inpDateFrom:"
          + vars.getRequestGlobalVariable("inpDateFrom", "ReportProductionJR|DateFrom"));
    if (log4j.isDebugEnabled())
      log4j.debug("inpDateTo:"
          + vars.getRequestGlobalVariable("inpDateTo", "ReportProductionJR|DateFrom"));

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

    JasperReport jasperReportLines;
    try {
      JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
          + "/org/openbravo/erpCommon/ad_reports/productionSubReport.jrxml");
      jasperReportLines = JasperCompileManager.compileReport(jasperDesignLines);
    } catch (JRException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }
    parameters.put("SR_LINES", jasperReportLines);

    parameters.put("REPORT_TITLE", strTitle);
    parameters.put("REPORT_SUBTITLE", strSubTitle);

    try {
      if (!strDateFrom.equals(""))
        parameters.put("DATE_FROM", new SimpleDateFormat("dd-MM-yyyy").parse(strDateFrom));
      if (!strDateTo.equals(""))
        parameters.put("DATE_TO", new SimpleDateFormat("dd-MM-yyyy").parse(strDateTo));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (log4j.isDebugEnabled())
      log4j.debug("parameters: " + parameters.toString());
    if (log4j.isDebugEnabled())
      log4j.debug("data: " + data);

    renderJR(vars, response, strReportName, "pdf", parameters, data, null);

  }

  public String getServletInfo() {
    return "Servlet ReportProduction. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
