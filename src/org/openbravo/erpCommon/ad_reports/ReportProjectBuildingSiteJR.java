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
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportProjectBuildingSiteJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strdateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportProjectBuildingSiteJR|DateFrom", "");
      String strdateTo = vars.getGlobalVariable("inpDateTo", "ReportProjectBuildingSiteJR|DateTo",
          "");
      String strcProjectId = vars.getGlobalVariable("inpcProjectId",
          "ReportProjectBuildingSiteJR|cProjectId", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportProjectBuildingSiteJR|cBPartnerId_IN", "", IsIDFilter.instance);
      String strmCategoryId = vars.getInGlobalVariable("inpmProductCategoryId",
          "ReportProjectBuildingSiteJR|mCategoryId", "", IsIDFilter.instance);
      String strProjectkind = vars.getInGlobalVariable("inpProjectkind",
          "ReportProjectBuildingSiteJR|Projectkind", "", IsIDFilter.instance);
      String strProjectstatus = vars.getInGlobalVariable("inpProjectstatus",
          "ReportProjectBuildingSiteJR|Projectstatus", "", IsIDFilter.instance);
      String strProjectphase = vars.getInGlobalVariable("inpProjectphase",
          "ReportProjectBuildingSiteJR|Projectphase", "", IsIDFilter.instance);
      String strProduct = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportProjectBuildingSiteJR|mProductId_IN", "", IsIDFilter.instance);
      String strProjectpublic = vars.getGlobalVariable("inpProjectpublic",
          "ReportProjectBuildingSiteJR|Projectpublic", "");
      String strSalesRep = vars.getGlobalVariable("inpSalesRepId",
          "ReportProjectBuildingSiteJR|SalesRepId", "");
      String strcRegionId = vars.getInGlobalVariable("inpcRegionId",
          "ReportProjectBuildingSiteJR|cRegionId", "", IsIDFilter.instance);
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportProjectBuildingSiteJR|currency", strUserCurrencyId);
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strcBpartnerId, strcProjectId,
          strmCategoryId, strProjectkind, strProjectphase, strProjectstatus, strProjectpublic,
          strcRegionId, strSalesRep, strProduct, strCurrencyId);
    } else if (vars.commandIn("FIND") || vars.commandIn("PDF")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportProjectBuildingSiteJR|DateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportProjectBuildingSiteJR|DateTo");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId",
          "ReportProjectBuildingSiteJR|cProjectId");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportProjectBuildingSiteJR|cBPartnerId_IN", IsIDFilter.instance);
      String strmCategoryId = vars.getRequestInGlobalVariable("inpmProductCategoryId",
          "ReportProjectBuildingSiteJR|mCategoryId", IsIDFilter.instance);
      String strProjectkind = vars.getRequestInGlobalVariable("inpProjectkind",
          "ReportProjectBuildingSiteJR|Projectkind", null);
      String strProjectstatus = vars.getRequestInGlobalVariable("inpProjectstatus",
          "ReportProjectBuildingSiteJR|Projectstatus", null);
      String strProjectphase = vars.getRequestInGlobalVariable("inpProjectphase",
          "ReportProjectBuildingSiteJR|Projectphase", null);
      String strProduct = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportProjectBuildingSiteJR|mProductId_IN", IsIDFilter.instance);
      String strProjectpublic = vars.getRequestGlobalVariable("inpProjectpublic",
          "ReportProjectBuildingSiteJR|Projectpublic");
      String strSalesRep = vars.getRequestGlobalVariable("inpSalesRepId",
          "ReportProjectBuildingSiteJR|SalesRepId");
      String strcRegionId = vars.getRequestInGlobalVariable("inpcRegionId",
          "ReportProjectBuildingSiteJR|cRegionId", IsIDFilter.instance);
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportProjectBuildingSiteJR|currency", strUserCurrencyId);
      String strOutput = "html";
      if (vars.commandIn("PDF"))
        strOutput = "pdf";
      printPageDataPDF(request, response, vars, strdateFrom, strdateTo, strcBpartnerId,
          strcProjectId, strmCategoryId, strProjectkind, strProjectphase, strProjectstatus,
          strProjectpublic, strcRegionId, strSalesRep, strProduct, strCurrencyId, strOutput);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strdateFrom, String strdateTo, String strcBpartnerId, String strcProjectId,
      String strmCategoryId, String strProjectkind, String strProjectphase,
      String strProjectstatus, String strProjectpublic, String strcRegionId, String strSalesRep,
      String strProduct, String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    String discard[] = { "sectionPartner" };
    String strTitle = "";
    XmlDocument xmlDocument = null;
    if (vars.commandIn("DEFAULT")) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSiteJR").createXmlDocument();

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProjectBuildingSiteJR", false,
          "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_reports.ReportProjectBuildingSiteJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportProjectBuildingSiteJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportProjectBuildingSiteJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportProjectBuildingSiteJR");
        vars.removeMessage("ReportProjectBuildingSiteJR");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("dateFrom", strdateFrom);
      xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTo", strdateTo);
      xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
      xmlDocument.setParameter("cProjectId", strcProjectId);
      xmlDocument.setParameter("projectName",
          ReportProjectBuildingSiteData.selectProject(this, strcProjectId));
      xmlDocument.setParameter("mProductCatId", strmCategoryId);
      xmlDocument.setParameter("cProjectKind", strProjectkind);
      xmlDocument.setParameter("cRegionId", strcRegionId);
      xmlDocument.setParameter("cProjectPhase", strProjectphase);
      xmlDocument.setParameter("cProjectStatus", strProjectstatus);
      xmlDocument.setParameter("cProjectPublic", strProjectpublic);
      xmlDocument.setParameter("salesRep", strSalesRep);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Project_kind",
            "Projectkind", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSiteJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSiteJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSiteJR",
            strProjectkind);
        xmlDocument.setData("reportC_PROJECTKIND", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Project_phase",
            "Projectphase", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSiteJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSiteJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSiteJR",
            strProjectphase);
        xmlDocument.setData("reportC_PROJECTPHASE", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Project_status",
            "ProjectStatus", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSiteJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSiteJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSiteJR",
            strProjectstatus);
        xmlDocument.setData("reportC_PROJECTSTATUS", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST",
            "C_Project_publicprivate", "PublicPrivate", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReportProjectBuildingSiteJR"), Utility.getContext(this,
                vars, "#User_Client", "ReportProjectBuildingSiteJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSiteJR",
            strProjectpublic);
        xmlDocument.setData("reportC_PROJECTPUBLIC", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_PRODUCT_CATEGORY_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSiteJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSiteJR"), 0);
        comboTableData.fillParameters(null, "ReportProjectBuildingSiteJR", "");
        xmlDocument.setData("reportC_PRODUCTCATREGORY", "liststructure",
            comboTableData.select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Region_ID",
            "", "C_Region of Country", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "Account"), Utility.getContext(this, vars, "#User_Client",
                "ReportSalesOrderProvided"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvided",
            strcRegionId);
        xmlDocument.setData("reportC_REGIONID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "AD_User SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSiteJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSiteJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvided",
            strSalesRep);
        xmlDocument.setData("reportSALESREP", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("ccurrencyid", strCurrencyId);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSiteJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSiteJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSiteJR",
            strCurrencyId);
        xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setData(
          "reportCBPartnerId_IN",
          "liststructure",
          SelectorUtilityData.selectBpartner(this,
              Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
              Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerId));
      xmlDocument.setData(
          "reportMProductId_IN",
          "liststructure",
          SelectorUtilityData.selectMproduct(this,
              Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
              Utility.getContext(this, vars, "#User_Client", ""), strProduct));
    } else {
      ReportProjectBuildingSiteData[] data = ReportProjectBuildingSiteData.select(this,
          strCurrencyId,
          Utility.getContext(this, vars, "#User_Client", "ReportProjectBuildingSiteJR"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProjectBuildingSiteJR"),
          strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"), strcBpartnerId,
          strcProjectId, strmCategoryId, strProjectkind, strProjectphase, strProjectstatus,
          strProjectpublic, strcRegionId, strSalesRep, strProduct);

      if (data == null || data.length == 0) {
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSiteJR", discard)
            .createXmlDocument();
        xmlDocument.setData("structure1", ReportProjectBuildingSiteData.set());
      } else {
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSiteJR").createXmlDocument();
        xmlDocument.setData("structure1", data);
      }
      if (!strProjectpublic.equals(""))
        strTitle += ", "
            + Utility.messageBD(this, "WithInitiativeType", vars.getLanguage())
            + " "
            + ReportProjectBuildingSiteData.selectProjectpublic(this, vars.getLanguage(),
                strProjectpublic);
      if (!strdateFrom.equals(""))
        strTitle += ", " + Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom;
      if (!strdateTo.equals(""))
        strTitle += " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;
      if (!strSalesRep.equals(""))
        strTitle += ", "
            + Utility.messageBD(this, "ForTheSalesRep", vars.getLanguage())
            + " "
            + ReportProjectBuildingSiteData
                .selectSalesRep(this, Utility.getContext(this, vars, "#AccessibleOrgTree",
                    "ReportProjectBuildingSiteJR"), Utility.getContext(this, vars, "#User_Client",
                    "ReportProjectBuildingSiteJR"), strSalesRep);
      xmlDocument.setParameter("title", strTitle);
    }
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcBpartnerId,
      String strcProjectId, String strmCategoryId, String strProjectkind, String strProjectphase,
      String strProjectstatus, String strProjectpublic, String strcRegionId, String strSalesRep,
      String strProduct, String strCurrencyId, String strOutput) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");
    String discard[] = { "sectionPartner" };

    ReportProjectBuildingSiteData[] data = null;

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    try {
      data = ReportProjectBuildingSiteData.select(this, strCurrencyId,
          Utility.getContext(this, vars, "#User_Client", "ReportProjectBuildingSiteJR"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProjectBuildingSiteJR"),
          strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"), strcBpartnerId,
          strcProjectId, strmCategoryId, strProjectkind, strProjectphase, strProjectstatus,
          strProjectpublic, strcRegionId, strSalesRep, strProduct);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    strConvRateErrorMsg = myMessage.getMessage();
    // If a conversion rate is missing for a certain transaction, an error
    // message window pops-up.
    if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
          strConvRateErrorMsg);
    } else { // Launch the report as usual, calling the JRXML file
      if (data == null || data.length == 0) {
        discard[0] = "selEliminar";
        data = ReportProjectBuildingSiteData.set();
      }
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSiteJR.jrxml";

      if (strOutput.equals("pdf"))
        response.setHeader("Content-disposition",
            "inline; filename=ReportProjectBuildingSiteJR.pdf");

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      String strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom
          + " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;
      parameters.put("REPORT_SUBTITLE", strSubTitle);

      renderJR(vars, response, strReportName, strOutput, parameters, data, null);
    }
  }

  public String getServletInfo() {
    return "Servlet ReportProjectBuildingSite. This Servlet was made by Eduardo Argal";
  } // end of getServletInfo() method
}
