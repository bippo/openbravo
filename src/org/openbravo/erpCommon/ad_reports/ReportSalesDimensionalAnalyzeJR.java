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
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.IsPositiveIntFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportSalesDimensionalAnalyzeJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT", "DEFAULT_COMPARATIVE")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportSalesDimensionalAnalyzeJR|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportSalesDimensionalAnalyzeJR|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef",
          "ReportSalesDimensionalAnalyzeJR|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef",
          "ReportSalesDimensionalAnalyzeJR|dateToRef", "");
      String strPartnerGroup = vars.getGlobalVariable("inpPartnerGroup",
          "ReportSalesDimensionalAnalyzeJR|partnerGroup", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportSalesDimensionalAnalyzeJR|partner", "", IsIDFilter.instance);
      String strProductCategory = vars.getGlobalVariable("inpProductCategory",
          "ReportSalesDimensionalAnalyzeJR|productCategory", "");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportSalesDimensionalAnalyzeJR|product", "", IsIDFilter.instance);
      String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId",
          "ReportSalesDimensionalAnalyzeJR|warehouse", "");
      // ad_ref_list.value for reference_id = 800087
      String strNotShown = vars.getInGlobalVariable("inpNotShown",
          "ReportSalesDimensionalAnalyzeJR|notShown", "", IsPositiveIntFilter.instance);
      String strShown = vars.getInGlobalVariable("inpShown",
          "ReportSalesDimensionalAnalyzeJR|shown", "", IsPositiveIntFilter.instance);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportSalesDimensionalAnalyzeJR|org", "");
      String strsalesrepId = vars.getGlobalVariable("inpSalesrepId",
          "ReportSalesDimensionalAnalyzeJR|salesrep", "");
      String strOrder = vars.getGlobalVariable("inpOrder", "ReportSalesDimensionalAnalyzeJR|order",
          "Normal");
      String strMayor = vars.getNumericGlobalVariable("inpMayor",
          "ReportSalesDimensionalAnalyzeJR|mayor", "");
      String strMenor = vars.getNumericGlobalVariable("inpMenor",
          "ReportSalesDimensionalAnalyzeJR|menor", "");
      String strPartnerSalesRepId = vars.getGlobalVariable("inpPartnerSalesrepId",
          "ReportSalesDimensionalAnalyzeJR|partnersalesrep", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportSalesDimensionalAnalyzeJR|currency", strUserCurrencyId);
      String strComparative = "";
      if (vars.commandIn("DEFAULT_COMPARATIVE"))
        strComparative = vars.getRequestGlobalVariable("inpComparative",
            "ReportSalesDimensionalAnalyzeJR|comparative");
      else
        strComparative = vars.getGlobalVariable("inpComparative",
            "ReportSalesDimensionalAnalyzeJR|comparative", "N");
      printPageDataSheet(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup,
          strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId, strNotShown,
          strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strOrder, strMayor,
          strMenor, strPartnerSalesRepId, strCurrencyId);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_HTML_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportSalesDimensionalAnalyzeJR|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportSalesDimensionalAnalyzeJR|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportSalesDimensionalAnalyzeJR|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportSalesDimensionalAnalyzeJR|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup",
          "ReportSalesDimensionalAnalyzeJR|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportSalesDimensionalAnalyzeJR|partner", IsIDFilter.instance);
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportSalesDimensionalAnalyzeJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportSalesDimensionalAnalyzeJR|product", IsIDFilter.instance);
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportSalesDimensionalAnalyzeJR|warehouse");
      // ad_ref_list.value for reference_id = 800087
      String strNotShown = vars.getInStringParameter("inpNotShown", IsPositiveIntFilter.instance);
      String strShown = vars.getInStringParameter("inpShown", IsPositiveIntFilter.instance);
      String strOrg = vars
          .getRequestGlobalVariable("inpOrg", "ReportSalesDimensionalAnalyzeJR|org");
      String strsalesrepId = vars.getRequestGlobalVariable("inpSalesrepId",
          "ReportSalesDimensionalAnalyzeJR|salesrep");
      String strOrder = vars.getRequestGlobalVariable("inpOrder",
          "ReportSalesDimensionalAnalyzeJR|order");
      String strMayor = vars.getNumericParameter("inpMayor", "");
      String strMenor = vars.getNumericParameter("inpMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      String strPartnerSalesrepId = vars.getRequestGlobalVariable("inpPartnerSalesrepId",
          "ReportSalesDimensionalAnalyzeJR|partnersalesrep");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportSalesDimensionalAnalyzeJR|currency", strUserCurrencyId);
      printPageHtml(request, response, vars, strComparative, strDateFrom, strDateTo,
          strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId,
          strNotShown, strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strOrder,
          strMayor, strMenor, strPartnerSalesrepId, strCurrencyId, "html");
    } else if (vars.commandIn("EDIT_PDF", "EDIT_PDF_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportSalesDimensionalAnalyzeJR|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportSalesDimensionalAnalyzeJR|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportSalesDimensionalAnalyzeJR|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportSalesDimensionalAnalyzeJR|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup",
          "ReportSalesDimensionalAnalyzeJR|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportSalesDimensionalAnalyzeJR|partner", IsIDFilter.instance);
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportSalesDimensionalAnalyzeJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportSalesDimensionalAnalyzeJR|product", IsIDFilter.instance);
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportSalesDimensionalAnalyzeJR|warehouse");
      // ad_ref_list.value for reference_id = 800087
      String strNotShown = vars.getInStringParameter("inpNotShown", IsPositiveIntFilter.instance);
      String strShown = vars.getInStringParameter("inpShown", IsPositiveIntFilter.instance);
      String strOrg = vars
          .getRequestGlobalVariable("inpOrg", "ReportSalesDimensionalAnalyzeJR|org");
      String strsalesrepId = vars.getRequestGlobalVariable("inpSalesrepId",
          "ReportSalesDimensionalAnalyzeJR|salesrep");
      String strOrder = vars.getRequestGlobalVariable("inpOrder",
          "ReportSalesDimensionalAnalyzeJR|order");
      String strMayor = vars.getNumericParameter("inpMayor", "");
      String strMenor = vars.getNumericParameter("inpMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      String strPartnerSalesrepId = vars.getRequestGlobalVariable("inpPartnerSalesrepId",
          "ReportSalesDimensionalAnalyzeJR|partnersalesrep");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportSalesDimensionalAnalyzeJR|currency", strUserCurrencyId);
      printPageHtml(request, response, vars, strComparative, strDateFrom, strDateTo,
          strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId,
          strNotShown, strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strOrder,
          strMayor, strMenor, strPartnerSalesrepId, strCurrencyId, "pdf");
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup,
      String strcBpartnerId, String strProductCategory, String strmProductId,
      String strmWarehouseId, String strNotShown, String strShown, String strDateFromRef,
      String strDateToRef, String strOrg, String strsalesrepId, String strOrder, String strMayor,
      String strMenor, String strPartnerSalesrepId, String strCurrencyId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    String discard[] = { "selEliminarHeader1" };
    if (strComparative.equals("Y")) {
      discard[0] = "selEliminarHeader2";
    }
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportSalesDimensionalAnalyzeJRFilter", discard)
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
        "ReportSalesDimensionalAnalyzeJRFilter", false, "", "", "", false, "ad_reports",
        strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportSalesDimensionalAnalyzeJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportSalesDimensionalAnalyzeJRFilter.html", classInfo.id, classInfo.type,
          strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportSalesDimensionalAnalyzeJRFilter.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportSalesDimensionalAnalyzeJR");
      vars.removeMessage("ReportSalesDimensionalAnalyzeJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromRef", strDateFromRef);
    xmlDocument.setParameter("dateFromRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRef", strDateToRef);
    xmlDocument.setParameter("dateToRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("salesRepId", strsalesrepId);
    xmlDocument.setParameter("cBpGroupId", strPartnerGroup);
    xmlDocument.setParameter("mProductCategoryId", strProductCategory);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("normal", strOrder);
    xmlDocument.setParameter("amountasc", strOrder);
    xmlDocument.setParameter("amountdesc", strOrder);
    xmlDocument.setParameter("mayor", strMayor);
    xmlDocument.setParameter("menor", strMenor);
    xmlDocument.setParameter("comparative", strComparative);
    xmlDocument.setParameter("partnerSalesRepId", strPartnerSalesrepId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportSalesDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportSalesDimensionalAnalyzeJR", strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportSalesDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportSalesDimensionalAnalyzeJR", strPartnerGroup);
      xmlDocument.setData("reportC_BP_GROUPID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportSalesDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportSalesDimensionalAnalyzeJR", strProductCategory);
      xmlDocument.setData("reportM_PRODUCT_CATEGORYID", "liststructure",
          comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "SalesRep_ID",
          "AD_User SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportSalesDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportSalesDimensionalAnalyzeJR", strsalesrepId);
      xmlDocument.setData("reportSalesRep_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportSalesDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportSalesDimensionalAnalyzeJR", strOrg);
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
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
            Utility.getContext(this, vars, "#User_Client", ""), strmProductId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
          "C_BPartner SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportSalesDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportSalesDimensionalAnalyzeJR", strPartnerSalesrepId);
      xmlDocument
          .setData("reportPartnerSalesRep_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
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

    if (vars.getLanguage().equals("en_US")) {
      xmlDocument.setData("structure1",
          ReportSalesDimensionalAnalyzeJRData.selectNotShown(this, strShown));
      xmlDocument.setData("structure2",
          strShown.equals("") ? new ReportSalesDimensionalAnalyzeJRData[0]
              : ReportSalesDimensionalAnalyzeJRData.selectShown(this, strShown));
    } else {
      xmlDocument
          .setData("structure1", ReportSalesDimensionalAnalyzeJRData.selectNotShownTrl(this,
              vars.getLanguage(), strShown));
      xmlDocument.setData(
          "structure2",
          strShown.equals("") ? new ReportSalesDimensionalAnalyzeJRData[0]
              : ReportSalesDimensionalAnalyzeJRData.selectShownTrl(this, vars.getLanguage(),
                  strShown));
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageHtml(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strComparative, String strDateFrom, String strDateTo,
      String strPartnerGroup, String strcBpartnerId, String strProductCategory,
      String strmProductId, String strmWarehouseId, String strNotShown, String strShown,
      String strDateFromRef, String strDateToRef, String strOrg, String strsalesrepId,
      String strOrder, String strMayor, String strMenor, String strPartnerSalesrepId,
      String strCurrencyId, String strOutput) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");
    String strOrderby = "";
    String[] discard = { "", "", "", "", "", "", "", "", "" };
    String[] discard1 = { "selEliminarBody1", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard" };
    if (strOrg.equals(""))
      strOrg = vars.getOrg();
    if (strComparative.equals("Y"))
      discard1[0] = "selEliminarBody2";
    String strTitle = "";
    strTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom + " "
        + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;
    if (!strPartnerGroup.equals(""))
      strTitle = strTitle + ", " + Utility.messageBD(this, "ForBPartnerGroup", vars.getLanguage())
          + " " + ReportSalesDimensionalAnalyzeJRData.selectBpgroup(this, strPartnerGroup);
    if (!strProductCategory.equals(""))
      strTitle = strTitle + ", " + Utility.messageBD(this, "ProductCategory", vars.getLanguage())
          + " "
          + ReportSalesDimensionalAnalyzeJRData.selectProductCategory(this, strProductCategory);
    if (!strsalesrepId.equals(""))
      strTitle = strTitle + ", " + Utility.messageBD(this, "TheSalesRep", vars.getLanguage()) + " "
          + ReportSalesDimensionalAnalyzeJRData.selectSalesrep(this, strsalesrepId);
    if (!strPartnerSalesrepId.equals(""))
      strTitle = strTitle + ", " + Utility.messageBD(this, "TheClientSalesRep", vars.getLanguage())
          + " " + ReportSalesDimensionalAnalyzeJRData.selectSalesrep(this, strPartnerSalesrepId);
    if (!strmWarehouseId.equals(""))
      strTitle = strTitle + " " + Utility.messageBD(this, "And", vars.getLanguage()) + " "
          + Utility.messageBD(this, "TheWarehouse", vars.getLanguage()) + " "
          + ReportSalesDimensionalAnalyzeJRData.selectMwarehouse(this, strmWarehouseId);

    ReportSalesDimensionalAnalyzeJRData[] data = null;
    String[] strShownArray = { "", "", "", "", "", "", "", "", "" };
    if (strShown.startsWith("("))
      strShown = strShown.substring(1, strShown.length() - 1);
    if (!strShown.equals("")) {
      strShown = Replace.replace(strShown, "'", "");
      strShown = Replace.replace(strShown, " ", "");
      StringTokenizer st = new StringTokenizer(strShown, ",", false);
      int intContador = 0;
      while (st.hasMoreTokens()) {
        strShownArray[intContador] = st.nextToken();
        intContador++;
      }

    }
    ReportSalesDimensionalAnalyzeJRData[] dimensionLabel = null;
    if (vars.getLanguage().equals("en_US")) {
      dimensionLabel = ReportSalesDimensionalAnalyzeJRData.selectNotShown(this, "");
    } else {
      dimensionLabel = ReportSalesDimensionalAnalyzeJRData.selectNotShownTrl(this,
          vars.getLanguage(), "");
    }

    // Checking report limit first
    StringBuffer levelsconcat = new StringBuffer();
    levelsconcat.append("''");
    String[] strLevelLabel = { "", "", "", "", "", "", "", "", "" };
    String[] strTextShow = { "", "", "", "", "", "", "", "", "" };
    int intDiscard = 0;
    int intProductLevel = 10;
    int intAuxDiscard = -1;
    for (int i = 0; i < 9; i++) {
      if (strShownArray[i].equals("1")) {
        strTextShow[i] = "C_BP_GROUP.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[0].name;
        levelsconcat.append(" || ");
        levelsconcat.append("C_BP_GROUP.C_BP_GROUP_ID");
      } else if (strShownArray[i].equals("2")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('C_Bpartner'), to_char( C_BPARTNER.C_BPARTNER_ID), to_char('"
            + vars.getLanguage() + "'))";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[1].name;
        levelsconcat.append(" || ");
        levelsconcat.append("C_BPARTNER.C_BPARTNER_ID");
      } else if (strShownArray[i].equals("3")) {
        strTextShow[i] = "M_PRODUCT_CATEGORY.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[2].name;
        levelsconcat.append(" || ");
        levelsconcat.append("M_PRODUCT_CATEGORY.M_PRODUCT_CATEGORY_ID");
      } else if (strShownArray[i].equals("4")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('M_Product'), to_char( M_PRODUCT.M_PRODUCT_ID), to_char('"
            + vars.getLanguage() + "'))||' ('||UOMSYMBOL||')'";
        intAuxDiscard = i;
        intDiscard++;
        intProductLevel = i + 1;
        strLevelLabel[i] = dimensionLabel[3].name;
        levelsconcat.append(" || ");
        levelsconcat.append("M_PRODUCT.M_PRODUCT_ID");
      } else if (strShownArray[i].equals("5")) {
        strTextShow[i] = "C_ORDER.DOCUMENTNO";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[4].name;
        levelsconcat.append(" || ");
        levelsconcat.append("C_ORDER.C_ORDER_ID");
      } else if (strShownArray[i].equals("6")) {
        strTextShow[i] = "AD_USER.FIRSTNAME||' '||' '||AD_USER.LASTNAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[5].name;
        levelsconcat.append(" || ");
        levelsconcat.append("AD_USER.AD_USER_ID");
      } else if (strShownArray[i].equals("7")) {
        strTextShow[i] = "M_WAREHOUSE.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[6].name;
        levelsconcat.append(" || ");
        levelsconcat.append("M_WAREHOUSE.M_WAREHOUSE_ID");
      } else if (strShownArray[i].equals("8")) {
        strTextShow[i] = "AD_ORG.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[7].name;
        levelsconcat.append(" || ");
        levelsconcat.append("AD_ORG.AD_ORG_ID");
      } else if (strShownArray[i].equals("9")) {
        strTextShow[i] = "AD_USER.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[8].name;
        levelsconcat.append(" || ");
        levelsconcat.append("AD_USER.AD_USER_ID");
      } else {
        strTextShow[i] = "''";
        discard[i] = "display:none;";
      }
    }
    if (intDiscard != 0 || intAuxDiscard != -1) {
      int k = 1;
      if (intDiscard == 1) {
        strOrderby = " ORDER BY NIVEL" + k + ",";
      } else {
        strOrderby = " ORDER BY ";
      }
      while (k < intDiscard) {
        strOrderby = strOrderby + "NIVEL" + k + ",";
        k++;
      }
      if (k == 1) {
        if (strOrder.equals("Normal")) {
          strOrderby = " ORDER BY NIVEL" + k;
        } else if (strOrder.equals("Amountasc")) {
          strOrderby = " ORDER BY CONVAMOUNT ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby = " ORDER BY CONVAMOUNT DESC";
        } else {
          strOrderby = "1";
        }
      } else {
        if (strOrder.equals("Normal")) {
          strOrderby += "NIVEL" + k;
        } else if (strOrder.equals("Amountasc")) {
          strOrderby += "CONVAMOUNT ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby += "CONVAMOUNT DESC";
        } else {
          strOrderby = "1";
        }
      }

    } else {
      strOrderby = " ORDER BY 1";
    }
    String strHaving = "";
    if (!strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING (SUM(CONVAMOUNT) > " + strMayor + " AND SUM(CONVAMOUNT) < " + strMenor
          + ")";
    } else if (!strMayor.equals("") && strMenor.equals("")) {
      strHaving = " HAVING (SUM(CONVAMOUNT) > " + strMayor + ")";
    } else if (strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING (SUM(CONVAMOUNT) < " + strMenor + ")";
    } else {
      strHaving = " HAVING (SUM(CONVAMOUNT) <> 0 OR SUM(CONVAMOUNTREF) <> 0)";
    }
    strOrderby = strHaving + strOrderby;

    int limit = 0;
    int mycount = 0;
    try {
      limit = Integer.parseInt(Utility.getPreference(vars, "ReportsLimit", ""));
      if (limit > 0) {
        mycount = Integer
            .parseInt((strComparative.equals("Y")) ? ReportSalesDimensionalAnalyzeJRData
                .selectCount(this, levelsconcat.toString(), Tree.getMembers(this,
                    TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this,
                    vars, "#User_Client", "ReportSalesDimensionalAnalyzeJR"), strPartnerGroup,
                    strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId,
                    strsalesrepId, strPartnerSalesrepId, strDateFrom, DateTimeData.nDaysAfter(this,
                        strDateTo, "1"), strDateFromRef, DateTimeData.nDaysAfter(this,
                        strDateToRef, "1")) : ReportSalesDimensionalAnalyzeJRData
                .selectNoComparativeCount(this, levelsconcat.toString(), Tree.getMembers(this,
                    TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this,
                    vars, "#User_Client", "ReportSalesDimensionalAnalyzeJR"), strPartnerGroup,
                    strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId,
                    strsalesrepId, strPartnerSalesrepId, strDateFrom, DateTimeData.nDaysAfter(this,
                        strDateTo, "1")));
      }
    } catch (NumberFormatException e) {
    }

    if (limit > 0 && mycount > limit) {
      String msgbody = Utility.messageBD(this, "ReportsLimitBody", vars.getLanguage());
      msgbody = msgbody.replace("@rows@", Integer.toString(mycount));
      msgbody = msgbody.replace("@limit@", Integer.toString(limit));
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "ReportsLimitHeader", vars.getLanguage()), msgbody);
    } else {
      // Checks if there is a conversion rate for each of the transactions
      // of
      // the report
      String strConvRateErrorMsg = "";
      OBError myMessage = null;
      myMessage = new OBError();
      if (strComparative.equals("Y")) {
        try {
          data = ReportSalesDimensionalAnalyzeJRData.select(this, strCurrencyId, strTextShow[0],
              strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], strTextShow[5],
              strTextShow[6], strTextShow[7], strTextShow[8],
              Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
              Utility.getContext(this, vars, "#User_Client", "ReportSalesDimensionalAnalyzeJR"),
              strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strPartnerGroup,
              strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId, strsalesrepId,
              strPartnerSalesrepId, strDateFromRef,
              DateTimeData.nDaysAfter(this, strDateToRef, "1"), strOrderby);
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        }
      } else {
        try {
          data = ReportSalesDimensionalAnalyzeJRData.selectNoComparative(this, strCurrencyId,
              strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4],
              strTextShow[5], strTextShow[6], strTextShow[7], strTextShow[8],
              Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
              Utility.getContext(this, vars, "#User_Client", "ReportSalesDimensionalAnalyzeJR"),
              strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strPartnerGroup,
              strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId, strsalesrepId,
              strPartnerSalesrepId, strOrderby);
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        }
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an error
      // message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advisePopUp(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        String strReportPath;
        if (strComparative.equals("Y")) {
          strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/WeightDimensionalComparative.jrxml";
        } else {
          strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/WeightDimensionalNoComparative.jrxml";
        }

        if (data.length == 0 || data == null) {
          data = ReportSalesDimensionalAnalyzeJRData.set();
        }
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("LEVEL1_LABEL", strLevelLabel[0]);
        parameters.put("LEVEL2_LABEL", strLevelLabel[1]);
        parameters.put("LEVEL3_LABEL", strLevelLabel[2]);
        parameters.put("LEVEL4_LABEL", strLevelLabel[3]);
        parameters.put("LEVEL5_LABEL", strLevelLabel[4]);
        parameters.put("LEVEL6_LABEL", strLevelLabel[5]);
        parameters.put("LEVEL7_LABEL", strLevelLabel[6]);
        parameters.put("LEVEL8_LABEL", strLevelLabel[7]);
        parameters.put("LEVEL9_LABEL", strLevelLabel[8]);
        parameters.put("DIMENSIONS", new Integer(intDiscard));
        parameters.put("REPORT_SUBTITLE", strTitle);
        parameters.put("PRODUCT_LEVEL", new Integer(intProductLevel));
        renderJR(vars, response, strReportPath, strOutput, parameters, data, null);
      }
    }
  }

  public String getServletInfo() {
    return "Servlet ReportSalesDimensionalAnalyzeJR.";
  } // end of getServletInfo() method
}
