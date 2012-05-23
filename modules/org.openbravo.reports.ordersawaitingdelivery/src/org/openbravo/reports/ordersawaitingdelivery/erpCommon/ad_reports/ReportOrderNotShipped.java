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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.reports.ordersawaitingdelivery.erpCommon.ad_reports;

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

public class ReportOrderNotShipped extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strdateFrom = vars.getGlobalVariable("inpDateFrom", "ReportOrderNotShipped|dateFrom",
          "");
      String strdateTo = vars.getGlobalVariable("inpDateTo", "ReportOrderNotShipped|dateTo", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportOrderNotShipped|bpartner", "");
      String strDeliveryTerms = vars.getGlobalVariable("inpDeliveryTerms",
          "ReportOrderNotShipped|deliveryTerms", "");
      String strOrderDocNo = vars.getGlobalVariable("inpOrderDocNo",
          "ReportOrderNotShipped|orderDocNo", "");
      String strOrderRef = vars.getGlobalVariable("inpOrderRef", "ReportOrderNotShipped|orderRef",
          "");
      String strCOrgId = vars.getGlobalVariable("inpOrg", "ReportOrderNotShipped|Org", "");
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strcBpartnerId, strDeliveryTerms,
          strOrderDocNo, strOrderRef, strCOrgId);
    } else if (vars.commandIn("FIND_HTML", "FIND_PDF")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportOrderNotShipped|dateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportOrderNotShipped|dateTo");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpcBPartnerId",
          "ReportOrderNotShipped|bpartner");
      String strDeliveryTerms = vars.getRequestGlobalVariable("inpDeliveryTerms",
          "ReportOrderNotShipped|deliveryTerms");
      String strOrderDocNo = vars.getRequestGlobalVariable("inpOrderDocNo",
          "ReportOrderNotShipped|orderDocNo");
      String strOrderRef = vars.getRequestGlobalVariable("inpOrderRef",
          "ReportOrderNotShipped|orderRef");
      String strCOrgId = vars.getRequestGlobalVariable("inpOrg", "ReportOrderNotShipped|Org");
      String strOutput = "html";
      if (vars.commandIn("FIND_PDF"))
        strOutput = "pdf";
      printPage(request, response, vars, strdateFrom, strdateTo, strcBpartnerId, strDeliveryTerms,
          strOrderDocNo, strOrderRef, strCOrgId, strOutput);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strdateFrom, String strdateTo, String strcBpartnerId, String strDeliveryTerms,
      String strOrderDocNo, String strOrderRef, String strCOrgId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/reports/ordersawaitingdelivery/erpCommon/ad_reports/ReportOrderNotShipped")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportOrderNotShipped", false, "", "",
        "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.reports.ordersawaitingdelivery.erpCommon.ad_reports.ReportOrderNotShipped");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportOrderNotShipped.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportOrderNotShipped.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportOrderNotShipped");
      vars.removeMessage("ReportOrderNotShipped");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strdateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strdateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("paramBPartnerDescription",
        ReportOrderNotShippedData.bPartnerDescription(this, strcBpartnerId));
    xmlDocument.setParameter("deliveryTerms", strDeliveryTerms);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_Order DeliveryRule", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportOrderNotShipped"), Utility.getContext(this, vars, "#User_Client",
              "ReportOrderNotShipped"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportOrderNotShipped",
          strDeliveryTerms);
      xmlDocument.setData("reportDeliveryTerms", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("orderDocNo", strOrderDocNo);
    xmlDocument.setParameter("orderRef", strOrderRef);
    xmlDocument.setParameter("adOrgId", strCOrgId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "49DC1D6F086945AB82F84C66F5F13F16", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportOrderNotShipped"), Utility.getContext(this, vars, "#User_Client",
              "ReportOrderNotShipped"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportOrderNotShipped", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
      comboTableData = null;

    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcBpartnerId,
      String strDeliveryTerms, String strOrderDocNo, String strOrderRef, String strCOrgId,
      String strOutput) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");

    ReportOrderNotShippedData[] data = null;

    data = ReportOrderNotShippedData.select(this, vars.getLanguage(),
        Utility.getContext(this, vars, "#User_Client", "ReportOrderNotShipped"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportOrderNotShipped"), strdateFrom,
        DateTimeData.nDaysAfter(this, strdateTo, "1"), strcBpartnerId, strDeliveryTerms,
        strOrderDocNo, strOrderRef, strCOrgId);

    // Launch the report as usual, calling the JRXML file
    String strReportName = "@basedesign@/org/openbravo/reports/ordersawaitingdelivery/erpCommon/ad_reports/ReportOrderNotShipped.jrxml";

    if (strOutput.equals("pdf"))
      response.setHeader("Content-disposition", "inline; filename=OrdersAwaitingDelivery.pdf");

    String strSubTitle = "";
    strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom + " "
        + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_SUBTITLE", strSubTitle);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null);
  }

  public String getServletInfo() {
    return "Servlet ReportOrderNotShipped.";
  } // end of getServletInfo() method
}
