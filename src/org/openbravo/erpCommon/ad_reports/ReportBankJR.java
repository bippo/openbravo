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
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportBankJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportBankJR|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportBankJR|DateTo", "");
      String strcbankaccount = vars.getGlobalVariable("inpmProductId",
          "ReportBankJR|C_Bankaccount_ID", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcbankaccount);
    } else if (vars.commandIn("PRINT_HTML")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportBankJR|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportBankJR|DateTo");
      String strcbankaccount = vars.getRequestGlobalVariable("inpcBankAccountId",
          "ReportBankJR|C_Bankaccount_ID");
      printPageDataHtml(response, vars, strDateFrom, strDateTo, strcbankaccount, "html");
    } else if (vars.commandIn("PRINT_PDF")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportBankJR|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportBankJR|DateTo");
      String strcbankaccount = vars.getRequestGlobalVariable("inpcBankAccountId",
          "ReportBankJR|C_Bankaccount_ID");
      printPageDataHtml(response, vars, strDateFrom, strDateTo, strcbankaccount, "pdf");
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strcbankaccount) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String strMessage = "";

    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportBankJR")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportBankJR", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportBankJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportBankJR.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportBankJR.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportBankJR");
      vars.removeMessage("ReportBankJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("cBankAccount", strcbankaccount);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramMessage", (strMessage.equals("") ? "" : "alert('" + strMessage
        + "');"));
    xmlDocument.setData(
        "reportC_ACCOUNTNUMBER",
        "liststructure",
        AccountNumberComboData.select(this, vars.getLanguage(),
            Utility.getContext(this, vars, "#User_Client", "ReportBankJR"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportBankJR")));

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strcbankaccount, String strOutput)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    String strMessage = "";
    // BigDecimal initialBalance= new BigDecimal(0);
    ReportBankJRData[] data = null;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      String discard[] = { "sectionAmount" };
      XmlDocument xmlDocument = null;
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportBankJR",
          discard).createXmlDocument();
      data = ReportBankJRData.set();
      if (vars.commandIn("FIND")) {
        strMessage = Utility.messageBD(this, "BothDatesCannotBeBlank", vars.getLanguage());
        log4j.warn("Both dates are blank");
      }
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportBankJR", false, "", "", "",
          false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("cBankAccount", strcbankaccount);
      xmlDocument.setParameter("dateFrom", strDateFrom);
      xmlDocument.setParameter("dateTo", strDateTo);
      xmlDocument.setParameter("paramMessage", (strMessage.equals("") ? "" : "alert('" + strMessage
          + "');"));
      xmlDocument.setData(
          "reportC_ACCOUNTNUMBER",
          "liststructure",
          AccountNumberComboData.select(this, vars.getLanguage(),
              Utility.getContext(this, vars, "#User_Client", "ReportBankJR"),
              Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportBankJR")));
    } else {
      // initialBalance = new BigDecimal(
      // ReportBankJRData.BeginningBalance(this, Utility.getContext(this,
      // vars, "#User_Client", "ReportBankJR"), Utility.getContext(this,
      // vars, "#AccessibleOrgTree", "ReportBankJR"),strDateFrom,
      // strcbankaccount));
      data = ReportBankJRData.select(this, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "ReportBankJR"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportBankJR"), strDateFrom,
          DateTimeData.nDaysAfter(this, strDateTo, "1"), strcbankaccount);
      // xmlDocument.setParameter("sumAmount",
      // ReportBankJRData.BeginningBalance(this, Utility.getContext(this,
      // vars, "#User_Client", "ReportBankJR"), Utility.getContext(this,
      // vars, "#AccessibleOrgTree", "ReportBankJR"),strDateFrom,
      // strcbankaccount));
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("DATE_FROM", strDateFrom);
    parameters
        .put("USER_ORG", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportBankJR"));
    parameters.put("USER_CLIENT", Utility.getContext(this, vars, "#User_Client", "ReportBankJR"));
    String strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportBankJR.jrxml";
    renderJR(vars, response, strReportPath, strOutput, parameters, data, null);
  }

  public String getServletInfo() {
    return "Servlet ReportBankJR.";
  } // end of getServletInfo() method
}
