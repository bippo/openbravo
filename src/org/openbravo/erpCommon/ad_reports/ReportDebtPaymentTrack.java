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

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportDebtPaymentTrack extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportDebtPaymentTrack|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportDebtPaymentTrack|DateTo", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportDebtPaymentTrack|cBpartnerId", "", IsIDFilter.instance);
      String strAmtFrom = vars.getNumericGlobalVariable("inpAmtFrom",
          "ReportDebtPaymentTrack|AmtFrom", "");
      String strAmtTo = vars.getNumericGlobalVariable("inpAmtTo", "ReportDebtPaymentTrack|AmtTo",
          "");
      String strInvoice = vars.getGlobalVariable("inpInvoice", "ReportDebtPaymentTrack|Invoice",
          "I");
      String strDPCNA = vars.getGlobalVariable("inpDPCNA", "ReportDebtPaymentTrack|DPCNA", "C");
      String strDPCA = vars.getGlobalVariable("inpDPCA", "ReportDebtPaymentTrack|DPCA", "A");
      String strDPGNA = vars.getGlobalVariable("inpDPGNA", "ReportDebtPaymentTrack|DPGNA", "G");
      String strDPGA = vars.getGlobalVariable("inpDPGA", "ReportDebtPaymentTrack|DPGA", "J");
      String strDPM = vars.getGlobalVariable("inpDPM", "ReportDebtPaymentTrack|DPM", "M");
      String strDPC = vars.getGlobalVariable("inpDPC", "ReportDebtPaymentTrack|DPC", "K");
      String strDPB = vars.getGlobalVariable("inpDPB", "ReportDebtPaymentTrack|DPB", "B");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strAmtFrom,
          strAmtTo, strInvoice, strDPCNA, strDPCA, strDPGNA, strDPGA, strDPM, strDPC, strDPB);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportDebtPaymentTrack|DateFrom");
      String strDateTo = vars
          .getRequestGlobalVariable("inpDateTo", "ReportDebtPaymentTrack|DateTo");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportDebtPaymentTrack|cBpartnerId", IsIDFilter.instance);
      String strAmtFrom = vars.getNumericRequestGlobalVariable("inpAmtFrom",
          "ReportDebtPaymentTrack|AmtFrom");
      String strAmtTo = vars.getNumericRequestGlobalVariable("inpAmtTo",
          "ReportDebtPaymentTrack|AmtTo");
      String strInvoice = vars.getRequestGlobalVariable("inpInvoice",
          "ReportDebtPaymentTrack|Invoice");
      String strDPCNA = vars.getRequestGlobalVariable("inpDPCNA", "ReportDebtPaymentTrack|DPCNA");
      String strDPCA = vars.getRequestGlobalVariable("inpDPCA", "ReportDebtPaymentTrack|DPCA");
      String strDPGNA = vars.getRequestGlobalVariable("inpDPGNA", "ReportDebtPaymentTrack|DPGNA");
      String strDPGA = vars.getRequestGlobalVariable("inpDPGA", "ReportDebtPaymentTrack|DPGA");
      String strDPM = vars.getRequestGlobalVariable("inpDPM", "ReportDebtPaymentTrack|DPM");
      String strDPC = vars.getRequestGlobalVariable("inpDPC", "ReportDebtPaymentTrack|DPC");
      String strDPB = vars.getRequestGlobalVariable("inpDPB", "ReportDebtPaymentTrack|DPB");
      vars.setSessionValue("ReportDebtPaymentTrack.initRecordNumber", "0");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strAmtFrom,
          strAmtTo, strInvoice, strDPCNA, strDPCA, strDPGNA, strDPGA, strDPM, strDPC, strDPB);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportDebtPaymentTrack.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
          "ReportDebtPaymentTrack");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReportDebtPaymentTrack.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReportDebtPaymentTrack.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportDebtPaymentTrack.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
          "ReportDebtPaymentTrack");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReportDebtPaymentTrack.initRecordNumber", strInitRecord);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("PRINT_PDF")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportDebtPaymentTrack|DateFrom");
      String strDateTo = vars
          .getRequestGlobalVariable("inpDateTo", "ReportDebtPaymentTrack|DateTo");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportDebtPaymentTrack|cBpartnerId", IsIDFilter.instance);
      String strAmtFrom = vars.getNumericRequestGlobalVariable("inpAmtFrom",
          "ReportDebtPaymentTrack|AmtFrom");
      String strAmtTo = vars.getNumericRequestGlobalVariable("inpAmtTo",
          "ReportDebtPaymentTrack|AmtTo");
      String strInvoice = vars.getRequestGlobalVariable("inpInvoice",
          "ReportDebtPaymentTrack|Invoice");
      String strDPCNA = vars.getRequestGlobalVariable("inpDPCNA", "ReportDebtPaymentTrack|DPCNA");
      String strDPCA = vars.getRequestGlobalVariable("inpDPCA", "ReportDebtPaymentTrack|DPCA");
      String strDPGNA = vars.getRequestGlobalVariable("inpDPGNA", "ReportDebtPaymentTrack|DPGNA");
      String strDPGA = vars.getRequestGlobalVariable("inpDPGA", "ReportDebtPaymentTrack|DPGA");
      String strDPM = vars.getRequestGlobalVariable("inpDPM", "ReportDebtPaymentTrack|DPM");
      String strDPC = vars.getRequestGlobalVariable("inpDPC", "ReportDebtPaymentTrack|DPC");
      String strDPB = vars.getRequestGlobalVariable("inpDPB", "ReportDebtPaymentTrack|DPB");
      printPageDataPdf(request, response, vars, strDateFrom, strDateTo, strcBpartnerId, strAmtFrom,
          strAmtTo, strInvoice, strDPCNA, strDPCA, strDPGNA, strDPGA, strDPM, strDPC, strDPB);
      // setHistoryCommand(request, "FIND");
    } else
      pageError(response);
  }

  private void printPageDataPdf(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId,
      String strAmtFrom, String strAmtTo, String strInvoice, String strDPCNA, String strDPCA,
      String strDPGNA, String strDPGA, String strDPM, String strDPC, String strDPB)
      throws IOException, ServletException {
    int limit = 0;
    int mycount = 0;
    try {
      limit = Integer.parseInt(Utility.getPreference(vars, "ReportsLimit", ""));
      if (limit > 0) {
        String strDocTypes = "'" + strInvoice + "','" + strDPCNA + "','" + strDPCA + "','"
            + strDPGNA + "','" + strDPGA + "','" + strDPM + "','" + strDPC + "','" + strDPB + "'";
        mycount = Integer.parseInt(ReportDebtPaymentTrackData.selectCount(this, vars.getLanguage(),
            Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment"),
            strcBpartnerId, strDateFrom, strDateTo, strAmtFrom, strAmtTo, strDocTypes));
      }
    } catch (NumberFormatException e) {
    }

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    ReportDebtPaymentTrackData[] data = null;
    if (limit > 0 && mycount > limit) {
      String msgbody = Utility.messageBD(this, "ReportsLimitBody", vars.getLanguage());
      msgbody = msgbody.replace("@rows@", Integer.toString(mycount));
      msgbody = msgbody.replace("@limit@", Integer.toString(limit));
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "ReportsLimitHeader", vars.getLanguage()), msgbody);
    } else {
      String strDocTypes = "'" + strInvoice + "','" + strDPCNA + "','" + strDPCA + "','" + strDPGNA
          + "','" + strDPGA + "','" + strDPM + "','" + strDPC + "','" + strDPB + "'";
      data = ReportDebtPaymentTrackData.select(this, "0", vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment"),
          strcBpartnerId, strDateFrom, strDateTo, strAmtFrom, strAmtTo, strDocTypes, null, null,
          null);
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportDebtPaymentTracker.jrxml";
      renderJR(vars, response, strReportName, "pdf", null, data, null);
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strcBpartnerId, String strAmtFrom,
      String strAmtTo, String strInvoice, String strDPCNA, String strDPCA, String strDPGNA,
      String strDPGA, String strDPM, String strDPC, String strDPB) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    String strRecordRange = Utility
        .getContext(this, vars, "#RecordRange", "ReportDebtPaymentTrack");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReportDebtPaymentTrack.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
    String rowNum = "0";
    String oraLimit1 = null;
    String oraLimit2 = null;
    String pgLimit = null;
    if (intRecordRange != 0) {
      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        rowNum = "ROWNUM";
        oraLimit1 = String.valueOf(initRecordNumber + intRecordRange);
        oraLimit2 = (initRecordNumber + 1) + " AND " + oraLimit1;
      } else {
        rowNum = "0";
        pgLimit = intRecordRange + " OFFSET " + initRecordNumber;
      }
    }

    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportDebtPaymentTrackData[] data = null;
    String discard[] = { "" };
    if ((strDateFrom.equals("") && strDateTo.equals("") && strcBpartnerId.equals("")
        && strAmtFrom.equals("") && strAmtTo.equals("") && strInvoice.equals("")
        && strDPCNA.equals("") && strDPCA.equals("") && strDPGNA.equals("") && strDPGA.equals("")
        && strDPM.equals("") && strDPC.equals("") && strDPB.equals(""))) {
      data = ReportDebtPaymentTrackData.set();
      discard[0] = "sectionPartner";
    } else {
      String strDocTypes = "'" + strInvoice + "','" + strDPCNA + "','" + strDPCA + "','" + strDPGNA
          + "','" + strDPGA + "','" + strDPM + "','" + strDPC + "','" + strDPB + "'";
      data = ReportDebtPaymentTrackData.select(this, rowNum, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment"),
          strcBpartnerId, strDateFrom, strDateTo, strAmtFrom, strAmtTo, strDocTypes, oraLimit1,
          oraLimit2, pgLimit);
    }
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportDebtPaymentTrack", discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportDebtPaymentTrack", false, "",
        "", "", false, "ad_reports", strReplaceWith, false, true);
    boolean hasPrevious = !(data == null || data.length == 0 || initRecordNumber <= 1);
    boolean hasNext = !(data == null || data.length == 0 || data.length < intRecordRange);
    toolbar.prepareRelationBarTemplate(hasPrevious, hasNext);
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportDebtPaymentTrack");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportDebtPaymentTrack.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportDebtPaymentTrack.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportDebtPaymentTrack");
      vars.removeMessage("ReportDebtPaymentTrack");
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
    xmlDocument.setParameter("AmtFrom", strAmtFrom);
    xmlDocument.setParameter("AmtTo", strAmtTo);
    xmlDocument.setParameter("DPCNA", strDPCNA);
    xmlDocument.setParameter("DPCA", strDPCA);
    xmlDocument.setParameter("DPGNA", strDPGNA);
    xmlDocument.setParameter("DPGA", strDPGA);
    xmlDocument.setParameter("DPM", strDPM);
    xmlDocument.setParameter("DPC", strDPC);
    xmlDocument.setParameter("DPB", strDPB);
    xmlDocument.setParameter("Invoice", strInvoice);
    xmlDocument.setData(
        "reportCBPartnerId_IN",
        "liststructure",
        SelectorUtilityData.selectBpartner(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerId));
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportDebtPaymentTrack. This Servlet was made by Eduardo Argal";
  } // end of getServletInfo() method
}
