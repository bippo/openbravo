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

import org.openbravo.base.filter.IsIDFilter;
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

public class ReportAnnualCertification extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReportAnnualCertification|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportAnnualCertification|DateFrom", "");
      String strDateTo = vars
          .getGlobalVariable("inpDateTo", "ReportAnnualCertification|DateTo", "");
      String strAmtFrom = vars.getGlobalVariable("inpAmtFrom", "ReportAnnualCertification|AmtFrom",
          "");
      String strAmtTo = vars.getGlobalVariable("inpAmtTo", "ReportAnnualCertification|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportAnnualCertification|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportAnnualCertification|C_ElementValue_IDTO", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportAnnualCertification|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportAnnualCertification|cBpartnerId", "", IsIDFilter.instance);
      String strAll = vars.getGlobalVariable("inpAll", "ReportAnnualCertification|All", "");
      String strReportType = vars.getRequestGlobalVariable("inpcReportType",
          "ReportAnnualCertification|ReportType");
      String strHide = vars.getGlobalVariable("inpHideMatched",
          "ReportAnnualCertification|HideMatched", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strAll, strReportType,
          strHide, strcAcctSchemaId);
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportAnnualCertification|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportAnnualCertification|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportAnnualCertification|DateTo");
      String strAmtFrom = vars.getRequestGlobalVariable("inpAmtFrom",
          "ReportAnnualCertification|AmtFrom");
      String strAmtTo = vars
          .getRequestGlobalVariable("inpAmtTo", "ReportAnnualCertification|AmtTo");
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportAnnualCertification|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportAnnualCertification|C_ElementValue_IDTO");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportAnnualCertification|Org");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportAnnualCertification|cBpartnerId", IsIDFilter.instance);
      String strAll = vars.getStringParameter("inpAll");
      String strReportType = vars.getRequestGlobalVariable("inpcReportType",
          "ReportAnnualCertification|ReportType");
      String strHide = vars.getStringParameter("inpHideMatched");
      if (log4j.isDebugEnabled())
        log4j.debug("inpAll: " + strAll);
      if (strAll.equals(""))
        vars.removeSessionValue("ReportAnnualCertification|All");
      else
        strAll = vars.getGlobalVariable("inpAll", "ReportAnnualCertification|All");
      if (strHide.equals(""))
        vars.removeSessionValue("ReportAnnualCertification|HideMatched");
      else
        strHide = vars.getGlobalVariable("inpHideMatched", "ReportAnnualCertification|HideMatched");
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - Find - strcBpartnerId= " + strcBpartnerId);
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - XLS - strcelementvaluefrom= "
            + strcelementvaluefrom);
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - XLS - strcelementvalueto= "
            + strcelementvalueto);
      vars.setSessionValue("ReportAnnualCertification.initRecordNumber", "0");

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strAll, strReportType,
          strHide, strcAcctSchemaId);

    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportAnnualCertification.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
          "ReportAnnualCertification");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReportAnnualCertification.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReportAnnualCertification.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportAnnualCertification.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange",
          "ReportAnnualCertification");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      if (initRecord == 0)
        initRecord = 1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReportAnnualCertification.initRecordNumber", strInitRecord);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportAnnualCertification|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportAnnualCertification|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportAnnualCertification|DateTo");
      String strAmtFrom = vars.getRequestGlobalVariable("inpAmtFrom",
          "ReportAnnualCertification|AmteFrom");
      String strAmtTo = vars
          .getRequestGlobalVariable("inpAmtTo", "ReportAnnualCertification|AmtTo");
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportAnnualCertification|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportAnnualCertification|C_ElementValue_IDTO");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportAnnualCertification|Org");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportAnnualCertification|cBpartnerId", IsIDFilter.instance);
      String strAll = vars.getStringParameter("inpAll");
      String strReportType = vars.getRequestGlobalVariable("inpcReportType",
          "ReportAnnualCertification|ReportType");
      String strHide = vars.getStringParameter("inpHideMatched");
      printPageDataPDF(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strAll, strReportType,
          strHide, strcAcctSchemaId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strAll, String strReportType, String strHide, String strcAcctSchemaId)
      throws IOException, ServletException {
    String strRecordRange = "500";
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReportAnnualCertification.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    if (log4j.isDebugEnabled())
      log4j.debug("Date From:" + strDateFrom + "- To:" + strDateTo + " - Schema:"
          + strcAcctSchemaId);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportAnnualCertificationData[] data = null;

    String[] discard = { "discard" };
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportAnnualCertification", discard)
        .createXmlDocument();
    // Setting Key Mappings ( key shortcut)
    xmlDocument.setParameter("theme", vars.getTheme());

    // Toolbar
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportAnnualCertification", false, "",
        "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    // toolbar.prepareRelationBarTemplate(false,
    // false,
    // "submitCommandForm('XLS', false, frmMain, 'ReportAnnualCertification.xls', 'EXCEL');return false;"
    // );
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      // GESTIONE TABS
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportAnnualCertification");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      // Setting for Skin (CSS)
      xmlDocument.setParameter("theme", vars.getTheme());
      // NavigationBar
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportAnnualCertification.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      // Left Bar
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportAnnualCertification.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    // Section for Manage the Messages
    {
      OBError myMessage = vars.getMessage("ReportAnnualCertification");
      vars.removeMessage("ReportAnnualCertification");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    // PARAMETRI UTENTE
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    try {
      // AD_OrgType_BU_LE
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "AD_OrgType_BU_LE", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportAnnualCertification"), Utility.getContext(this, vars, "#User_Client",
              "ReportAnnualCertification"), '*');
      comboTableData.fillParameters(null, "ReportAnnualCertification", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    // SEZIONE RISULTATI
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      data = ReportAnnualCertificationData.set();
    } else {
      data = ReportAnnualCertificationData.select(this,
          Utility.getContext(this, vars, "#User_Client", "ReportAnnualCertification"), strOrg,
          strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId,
          initRecordNumber, intRecordRange);
    }
    xmlDocument.setData("structure1", data);

    /*
     * xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
     * //xmlDocument.setData("reportAD_ORGID", "liststructure",
     * OrganizationComboData.selectCombo(this, vars.getRole()));
     * xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
     * xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
     * xmlDocument.setData("reportCBPartnerId_IN", "liststructure",
     * ReportRefundInvoiceCustomerDimensionalAnalysesData .selectBpartner(this,
     * Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility.getContext(this, vars,
     * "#User_Client", ""), strcBpartnerIdAux));
     */
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strAll, String strReportType, String strHide,
      String strcAcctSchemaId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");
    ReportAnnualCertificationData[] data = null;

    if (!strDateFrom.equals("") && !strDateTo.equals("")) {
      data = ReportAnnualCertificationData.select(this,
          Utility.getContext(this, vars, "#User_Client", "ReportAnnualCertification"), strOrg,
          strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId);
    } else {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    }
    String strOutput = vars.commandIn("PDF") ? "pdf" : "xls";
    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportAnnualCertification.jrxml";
    OrganizationData[] dataOrganization = null;
    // populate organization data if report data is available.
    if (data != null && data.length > 0) {
      String sClientID = vars.getUserClient();
      String sOrganID = "";
      if (strOrg.equals("")) {
        for (int i = 0; i < data.length; i++) {
          sOrganID = sOrganID + "'" + data[i].orgid;
          if (!(i == data.length - 1)) {
            sOrganID = sOrganID + "',";
          } else {
            sOrganID = sOrganID + "'";
          }
        }

      } else {
        sOrganID = "'" + strOrg + "'";
      }
      dataOrganization = OrganizationData.select(this, vars.getLanguage(), sClientID, sOrganID);
      // put address of organization and employer into same data
      for (int i = 0; i < data.length; i++) {
        if (dataOrganization != null && dataOrganization.length > 0) {
          for (int j = 0; j < dataOrganization.length; j++) {
            if (data[i].orgid.equals(dataOrganization[j].adOrgId)) {
              data[i].mittente = dataOrganization[j].adClientIdr;
              data[i].erogante = dataOrganization[j].adClientIdr;
              data[i].addressorganization = dataOrganization[j].cLocationIdr;
              break;
            }
          }
        }
      }
    }
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("DateFrom", strDateFrom);
    parameters.put("DateTo", strDateTo);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null);
  }

  public String getServletInfo() {
    return "Servlet ReportAnnualCertification. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
