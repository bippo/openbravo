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
package org.openbravo.erpReports;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.KeyMap;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportTaxPaymentJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strTaxPayId = vars.getSessionValue("JR_ReportTaxPayment.inpcTaxpaymentId");
      String strTaxPayId_Clear = strTaxPayId.replace("(", "").replace(")", "").replace("'", "");
      TaxPaymentData[] taxpaym = TaxPaymentData.select(this, strTaxPayId_Clear);
      String strDateFrom = taxpaym[0].datefrom;
      String strDateTo = taxpaym[0].dateto;
      printPageDataSheet(response, vars, strDateFrom, strDateTo);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      // String strTaxPayId =
      // vars.getRequestGlobalVariable("inpcTaxpaymentId","ReportVatRegisterJR|C_TaxpaymentId");
      String strTaxPayId = vars.getSessionValue("JR_ReportTaxPayment.inpcTaxpaymentId");
      String strTaxPayId_Clear = strTaxPayId.replace("(", "").replace(")", "").replace("'", "");
      TaxPaymentData[] taxpaym = TaxPaymentData.select(this, strTaxPayId_Clear);
      String strDateFrom = taxpaym[0].datefrom;
      String strDateTo = taxpaym[0].dateto;
      String strcTypeVatReport = vars.getRequestGlobalVariable("inpTypeVatReport",
          "JR_ReportTaxPayment|TypeVatReport");

      if (strcTypeVatReport.equals("01")) {
        // ReportTransactions
        printReportJRRegisterByVat(response, vars, strDateFrom, strDateTo, strcTypeVatReport);
      } else {
        // Standard Report
        bdError(request, response, "TypeReportCantBeNull", vars.getLanguage());
        return;
      }

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    // String strMessage = "";

    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/ReportPaymentFilter")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportRegisterFilter", false, "", "",
        "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, "ReportRegisterPayment.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpReports.TaxPayment");// ReportVatRegisterJR
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportPaymentFilter.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportPaymentFilter.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportPaymentFilter");
      vars.removeMessage("ReportPaymentFilter");
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
    xmlDocument.setParameter("dateFromdisplayFormat",
        vars.getSessionValue("#AD_JavaDateTimeFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_JavaDateTimeFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_JavaDateTimeFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_JavaDateTimeFormat"));
    // xmlDocument.setParameter("paramMessage", (strMessage.equals("") ? ""
    // : "alert('" + strMessage + "');"));

    TypeReportPayment[] aTypeVatReport = TypeReportPayment.set();
    xmlDocument.setData("reportTypeVatReport", "liststructure", aTypeVatReport);
    xmlDocument.setParameter("TypeVatReport", "01");
    out.println(xmlDocument.print());
    out.close();
  }

  private void printReportJRRegisterByVat(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strTypeReport) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");

    ReportRegisterByVatJRData[] data = null;
    data = ReportRegisterByVatJRData.select(this, strDateFrom,
        DateTimeData.nDaysAfter(this, strDateTo, "1"));

    String strOutput = vars.commandIn("EDIT_HTML") ? "html" : "pdf";
    String strReportName = "@basedesign@/org/openbravo/erpReports/ReportRegisterByVatJR.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_SUBTITLE", "From " + strDateFrom + " to " + strDateTo);
    parameters.put("invoicedateDA", strDateFrom);
    parameters.put("invoicedateA", strDateTo);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null);
  }

  public String getServletInfo() {
    return "Servlet ReportVatRegisterJR.";
  } // end of getServletInfo() method

}

class TypeReportPayment implements FieldProvider {
  static Logger log4j = Logger.getLogger(TypeReportPayment.class);
  // private String InitRecordNumber = "0";
  public String id;
  public String name;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("ID"))
      return id;
    else if (fieldName.equalsIgnoreCase("NAME"))
      return name;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  static TypeReportPayment[] set() throws ServletException {
    TypeReportPayment objectTypeReport[] = new TypeReportPayment[1];
    // first
    objectTypeReport[0] = new TypeReportPayment();
    objectTypeReport[0].id = "01";
    objectTypeReport[0].name = "Report Tax Payment By VAT";

    return objectTypeReport;
  }

}