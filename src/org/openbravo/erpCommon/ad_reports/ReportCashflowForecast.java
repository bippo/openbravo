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
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportCashflowForecast extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strBankAccount = vars.getRequestGlobalVariable("inpcBankAccountId",
          "ReportCashflowForecast|AcctNo");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportCashflowForecast|DateFrom");
      String strBreakDate = vars.getRequestGlobalVariable("inpBreakDate",
          "ReportCashflowForecast|BreakDate");

      printPageDataSheet(response, vars, strBankAccount, strDateFrom, strBreakDate, true);
    } else if (vars.commandIn("FIND")) {
      String strBankAccount = vars.getRequestGlobalVariable("inpcBankAccountId",
          "ReportCashflowForecast|AcctNo");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportCashflowForecast|DateFrom");
      String strBreakDate = vars.getRequestGlobalVariable("inpBreakDate",
          "ReportCashflowForecast|BreakDate");

      printPageDataSheet(response, vars, strBankAccount, strDateFrom, strBreakDate, false);
    } else if (vars.commandIn("PRINT_PDF")) {
      String strBankAccount = vars.getRequestGlobalVariable("inpcBankAccountId",
          "ReportCashflowForecast|AcctNo");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportCashflowForecast|DateFrom");
      String strBreakDate = vars.getRequestGlobalVariable("inpBreakDate",
          "ReportCashflowForecast|BreakDate");

      printPageDataPdf(response, vars, strBankAccount, strDateFrom, strBreakDate, false);
    } else
      pageError(response);
  }

  private void printPageDataPdf(HttpServletResponse response, VariablesSecureApp vars,
      String strBankAccount, String strDateMax, String strBreakDate, boolean showDefault)
      throws IOException, ServletException {

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportCashflowForecast", false, "",
        "", "", false, "ad_reports", strReplaceWith, false, true);

    // ReportCashflowForecastData[] dataSummary =
    // ReportCashflowForecastData.select(this,Utility.getContext(this, vars, "#User_Client",
    // "ReportBank"), Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportBank"));
    ReportCashflowForecastData[] dataSummary = ReportCashflowForecastData.select(this,
        vars.getLanguage(), strDateMax, "",
        Utility.getContext(this, vars, "#User_Client", "ReportBank"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportBank"));

    if (!showDefault) {
      ReportCashflowForecastData[] dataDetail = null;
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      String strLanguage = vars.getLanguage();
      String strBaseDesign = getBaseDesignPath(strLanguage);
      JasperReport jasperReportLines;
      try {
        JasperDesign jasperDesignLines = (JRXmlLoader.load(strBaseDesign
            + "/org/openbravo/erpCommon/ad_reports/ReportCashflowForecast_sub.jrxml"));
        jasperReportLines = JasperCompileManager.compileReport(jasperDesignLines);
      } catch (JRException e) {
        log4j.error("Error Compiling report ", e);
        throw new ServletException(e.getMessage());
      }
      parameters.put("ReportData", jasperReportLines);
      Date date = null;
      try {
        String strDateFormat;
        strDateFormat = vars.getJavaDateFormat();
        SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        date = dateFormat.parse(strDateMax);
      } catch (Exception e) {
        throw new ServletException(e.getMessage());
      }
      parameters.put("DatePlanned", date);
      parameters.put("BankAcc",
          vars.getRequestGlobalVariable("inpcBankAccountId", "ReportCashflowForecast|AcctNo"));

      try {
        if (strBreakDate.equals("")) {
          dataDetail = ReportCashflowForecastData.selectAllLines(this, vars.getSqlDateFormat(),
              vars.getLanguage(),
              vars.getRequestGlobalVariable("inpcBankAccountId", "ReportCashflowForecast|AcctNo"),
              strDateMax, "BANKACCOUNT, ISRECEIPT desc,DATEPLANNED,INVOICENO ");
        } else {
          dataDetail = ReportCashflowForecastData.selectAllLines(this, vars.getSqlDateFormat(),
              vars.getLanguage(),
              vars.getRequestGlobalVariable("inpcBankAccountId", "ReportCashflowForecast|AcctNo"),
              strDateMax, "BANKACCOUNT,DATEPLANNED,ISRECEIPT desc,INVOICENO ");
        }
        String strReportName = (("on".equals(strBreakDate)) ? "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportCashflowForecast_perDay.jrxml"
            : "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportCashflowForecast.jrxml");
        renderJR(vars, response, strReportName, "pdf", parameters, dataDetail, null);
      } catch (Exception e) {
        log4j.error("Error trying to render the PDF", e);
      }
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strBankAccount, String strDateMax, String strBreakDate, boolean showDefault)
      throws IOException, ServletException {
    String[] discard = { "", "" };
    XmlDocument xmlDocument = null;

    if (showDefault)
      discard[0] = "subrpt";
    else {
      if (strBreakDate.equals(""))
        discard[0] = "reportAccountDate";
      else
        discard[0] = "reportAccount";
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportCashflowForecast", false, "",
        "", "", false, "ad_reports", strReplaceWith, false, true);

    // ReportCashflowForecastData[] dataSummary =
    // ReportCashflowForecastData.select(this,Utility.getContext(this, vars,
    // "#User_Client", "ReportBank"), Utility.getContext(this, vars,
    // "#AccessibleOrgTree", "ReportBank"));
    ReportCashflowForecastData[] dataSummary = ReportCashflowForecastData.select(this,
        vars.getLanguage(), strDateMax, "",
        Utility.getContext(this, vars, "#User_Client", "ReportBank"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportBank"));

    if (!showDefault) {
      ReportCashflowForecastData[][] data = null;
      ReportCashflowForecastData[] dataAcct = null;
      ReportCashflowForecastData[] dataDetail = null;

      // dataAcct = ReportCashflowForecastData.select(this, strDateMax,
      // strBankAccount);
      dataAcct = ReportCashflowForecastData.select(this, vars.getLanguage(), strDateMax,
          strBankAccount, Utility.getContext(this, vars, "#User_Client", "ReportBank"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportBank"));
      data = new ReportCashflowForecastData[dataAcct.length][];

      if (log4j.isDebugEnabled())
        log4j.debug("length: " + dataAcct.length + " - bankaccount:" + strBankAccount);
      if (dataAcct.length == 0) {
        discard[0] = "reportAccountDate";
        discard[1] = "reportAccount";
      } else {
        for (int i = 0; i < dataAcct.length; i++) {
          if (strBreakDate.equals(""))
            dataDetail = ReportCashflowForecastData.selectLines(this, vars.getSqlDateFormat(),
                vars.getLanguage(), dataAcct[i].cBankaccountId, strDateMax, "2 DESC, 1");
          else
            dataDetail = ReportCashflowForecastData.selectLines(this, vars.getSqlDateFormat(),
                vars.getLanguage(), dataAcct[i].cBankaccountId, strDateMax, "1,2 DESC");
          if (log4j.isDebugEnabled())
            log4j.debug("length: " + dataAcct.length + " bankacct:" + dataAcct[i].cBankaccountId
                + " lenght:" + dataDetail.length);
          data[i] = dataDetail;
        }
      }
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportCashflowForecast", discard).createXmlDocument();

      xmlDocument.setData("structureDetail", dataAcct);
      if (strBreakDate.equals(""))
        xmlDocument.setDataArray("reportAcct", "structureAccount", data);
      else
        xmlDocument.setDataArray("reportAcctDate", "structureAccount", data);
    } else
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportCashflowForecast", discard).createXmlDocument();

    // ReportCashflowForecastData.select(this,Utility.getContext(this, vars,
    // "#User_Client", "ReportBank"), Utility.getContext(this, vars,
    // "#AccessibleOrgTree", "ReportBank"));

    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    xmlDocument.setData(
        "reportC_ACCOUNTNUMBER",
        "liststructure",
        AccountNumberComboData.select(this, vars.getLanguage(),
            Utility.getContext(this, vars, "#User_Client", "ReportCashflowForecast"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashflowForecast")));
    xmlDocument.setParameter("cBankAccount", strBankAccount);
    xmlDocument.setParameter("dateFrom", strDateMax);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("finalDate", strDateMax);
    xmlDocument.setParameter("date",
        ReportCashflowForecastData.getDate(this, vars.getSqlDateFormat()));
    xmlDocument.setParameter("date1",
        ReportCashflowForecastData.getDate(this, vars.getSqlDateFormat()));
    xmlDocument.setParameter("breakDate", strBreakDate.equals("") ? "0" : "1");
    xmlDocument.setData("structureSummary", dataSummary);

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportCashflowForecast");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportCashflowForecast.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportCashflowForecast.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportCashflowForecast");
      vars.removeMessage("ReportCashflowForecast");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportCashflowForecast";
  } // end of the getServletInfo() method
}
