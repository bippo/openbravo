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
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportSalesOrderProvidedJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strdateFrom = vars.getStringParameter("inpDateFrom", "");
      String strdateTo = vars.getStringParameter("inpDateTo", "");
      String strcBpartnerId = vars.getStringParameter("inpcBPartnerId", "");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId", "");
      String strcProjectId = vars.getStringParameter("inpcProjectId", "");
      String strmCategoryId = vars.getStringParameter("inpProductCategory", "");
      String strProjectkind = vars.getStringParameter("inpProjectkind", "");
      String strcRegionId = vars.getStringParameter("inpcRegionId", "");
      String strProjectpublic = vars.getStringParameter("inpProjectpublic", "");
      String strProduct = vars.getStringParameter("inpProductId", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportSalesOrderProvidedJR|currency", strUserCurrencyId);
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strcBpartnerId, strmWarehouseId,
          strcProjectId, strmCategoryId, strProjectkind, strcRegionId, strProjectpublic,
          strProduct, strCurrencyId);
    } else if (vars.commandIn("FIND")) {
      String strdateFrom = vars.getStringParameter("inpDateFrom");
      String strdateTo = vars.getStringParameter("inpDateTo");
      String strcBpartnerId = vars.getStringParameter("inpcBPartnerId");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId");
      String strcProjectId = vars.getStringParameter("inpcProjectId");
      String strmCategoryId = vars.getStringParameter("inpProductCategory");
      String strProjectkind = vars.getStringParameter("inpProjectkind");
      String strcRegionId = vars.getStringParameter("inpcRegionId");
      String strProjectpublic = vars.getStringParameter("inpProjectpublic");
      String strProduct = vars.getStringParameter("inpmProductId");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportSalesOrderProvidedJR|currency", strUserCurrencyId);
      printPageDataSheetJasper(request, response, vars, strdateFrom, strdateTo, strcBpartnerId,
          strmWarehouseId, strcProjectId, strmCategoryId, strProjectkind, strcRegionId,
          strProjectpublic, strProduct, strCurrencyId);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strdateFrom, String strdateTo, String strcBpartnerId, String strmWarehouseId,
      String strcProjectId, String strmCategoryId, String strProjectkind, String strcRegionId,
      String strProjectpublic, String strProduct, String strCurrencyId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    String discard[] = { "sectionPartner" };
    String strTitle = "";
    XmlDocument xmlDocument = null;
    if (vars.commandIn("DEFAULT")) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportSalesOrderProvidedJR").createXmlDocument();

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesOrderProvidedJR", false,
          "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_reports.ReportSalesOrderProvidedJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportSalesOrderProvidedJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportSalesOrderProvidedJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportSalesOrderProvidedJR");
        vars.removeMessage("ReportSalesOrderProvidedJR");
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
      xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
      xmlDocument.setParameter("cProjectId", strcProjectId);
      xmlDocument.setParameter("mProductCategoryId", strmCategoryId);
      xmlDocument.setParameter("cProjectKind", strProjectkind);
      xmlDocument.setParameter("cRegionId", strcRegionId);
      xmlDocument.setParameter("cProjectPublic", strProjectpublic);
      xmlDocument.setParameter("projectName",
          ReportSalesOrderProvidedData.selectProject(this, strcProjectId));
      xmlDocument.setParameter("paramBPartnerDescription",
          ReportSalesOrderProvidedData.bPartnerDescription(this, strcBpartnerId));
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportSalesOrderProvidedJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportSalesOrderProvidedJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvidedJR",
            strmWarehouseId);
        xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportSalesOrderProvidedJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportSalesOrderProvidedJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvidedJR",
            strmCategoryId);
        xmlDocument.setData("reportM_PRODUCT_CATEGORYID", "liststructure",
            comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Projectkind_ID",
            "Projectkind", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportSalesOrderProvidedJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportSalesOrderProvidedJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvidedJR",
            strProjectkind);
        xmlDocument.setData("reportC_PROJECTKIND", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Region_ID",
            "", "C_Region of Country", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportSalesOrderProvidedJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportSalesOrderProvidedJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvidedJR",
            strcRegionId);
        xmlDocument.setData("reportC_REGIONID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST",
            "C_Project_Public_ID", "PublicPrivate", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReportSalesOrderProvidedJR"), Utility.getContext(this, vars,
                "#User_Client", "ReportSalesOrderProvidedJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvidedJR",
            strProjectpublic);
        xmlDocument.setData("reportC_PROJECTPUBLIC", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("ccurrencyid", strCurrencyId);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportSalesOrderProvidedJR"), Utility.getContext(this, vars, "#User_Client",
                "ReportSalesOrderProvidedJR"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvidedJR",
            strCurrencyId);
        xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

    } else { // command != DEFAULT
      ReportSalesOrderProvidedData[] data = ReportSalesOrderProvidedData.select(this,
          strCurrencyId,
          Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderProvided"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesOrderProvided"),
          strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"), strcBpartnerId,
          strmWarehouseId, strcProjectId, strmCategoryId, strProjectkind, strcRegionId,
          strProjectpublic, strProduct);
      if (data == null || data.length == 0) {
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportSalesOrderProvidedPop", discard)
            .createXmlDocument();
        xmlDocument.setData("structure1", ReportSalesOrderProvidedData.set());
      } else {
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportSalesOrderProvidedPop").createXmlDocument();
        xmlDocument.setData("structure1", data);
      }
      if (!strmWarehouseId.equals(""))
        strTitle += " " + Utility.messageBD(this, "ForWarehouse", vars.getLanguage()) + " "
            + ReportSalesOrderProvidedData.selectWarehouse(this, strmWarehouseId);
      if (!strcRegionId.equals(""))
        strTitle += ", " + Utility.messageBD(this, "InRegion", vars.getLanguage()) + " "
            + ReportSalesOrderProvidedData.selectRegionId(this, strcRegionId);
      if (!strmCategoryId.equals(""))
        strTitle += ", " + Utility.messageBD(this, "ForProductCategory", vars.getLanguage()) + " "
            + ReportSalesOrderProvidedData.selectCategoryId(this, strmCategoryId);
      if (!strProjectkind.equals(""))
        strTitle += ", "
            + Utility.messageBD(this, "ProjectType", vars.getLanguage())
            + " "
            + ReportSalesOrderProvidedData.selectProjectkind(this, vars.getLanguage(),
                strProjectkind);
      if (!strProjectpublic.equals(""))
        strTitle += ", "
            + Utility.messageBD(this, "WithInitiativeType", vars.getLanguage())
            + " "
            + ReportSalesOrderProvidedData.selectProjectpublic(this, vars.getLanguage(),
                strProjectpublic);
      if (!strdateFrom.equals(""))
        strTitle += ", " + Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom;
      if (!strdateTo.equals(""))
        strTitle += " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;
      if (!strProduct.equals(""))
        strTitle += ", " + Utility.messageBD(this, "ForProduct", vars.getLanguage()) + " "
            + ReportSalesOrderProvidedData.selectProduct(this, strProduct);
      xmlDocument.setParameter("title", strTitle);
    }
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  // Jasper calling starts here
  private void printPageDataSheetJasper(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcBpartnerId,
      String strmWarehouseId, String strcProjectId, String strmCategoryId, String strProjectkind,
      String strcRegionId, String strProjectpublic, String strProduct, String strCurrencyId)
      throws IOException, ServletException {

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    ReportSalesOrderProvidedData[] data = null;
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    try {
      data = ReportSalesOrderProvidedData.select(this, strCurrencyId,
          Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderProvided"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesOrderProvided"),
          strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"), strcBpartnerId,
          strmWarehouseId, strcProjectId, strmCategoryId, strProjectkind, strcRegionId,
          strProjectpublic, strProduct);
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
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportSalesOrderProvidedJR.jrxml";
      String strOutput = "html";
      if (strOutput.equals("pdf"))
        response.setHeader("Content-disposition", "inline; filename=ReportSalesOrderProvided.pdf");

      String strSubTitle = "";
      strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom + " "
          + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("REPORT_SUBTITLE", strSubTitle);
      renderJR(vars, response, strReportName, strOutput, parameters, data, null);
    }
  }

  public String getServletInfo() {
    return "Servlet ReportSalesOrderProvidedJR. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
