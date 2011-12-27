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
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
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

public class ReportRefundSalesDimensionalAnalyses extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT", "DEFAULT_COMPARATIVE")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportRefundSalesDimensionalAnalyses|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportRefundSalesDimensionalAnalyses|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef",
          "ReportRefundSalesDimensionalAnalyses|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef",
          "ReportRefundSalesDimensionalAnalyses|dateToRef", "");
      String strPartnerGroup = vars.getGlobalVariable("inpPartnerGroup",
          "ReportRefundSalesDimensionalAnalyses|partnerGroup", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportRefundSalesDimensionalAnalyses|partner", "", IsIDFilter.instance);
      String strProductCategory = vars.getGlobalVariable("inpProductCategory",
          "ReportRefundSalesDimensionalAnalyses|productCategory", "");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportRefundSalesDimensionalAnalyses|product", "", IsIDFilter.instance);
      // ad_ref_list.value where reference_id = 800087
      String strNotShown = vars.getInGlobalVariable("inpNotShown",
          "ReportRefundSalesDimensionalAnalyses|notShown", "", IsPositiveIntFilter.instance);
      String strShown = vars.getInGlobalVariable("inpShown",
          "ReportRefundSalesDimensionalAnalyses|shown", "", IsPositiveIntFilter.instance);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportRefundSalesDimensionalAnalyses|org",
          "");
      String strsalesrepId = vars.getGlobalVariable("inpSalesrepId",
          "ReportRefundSalesDimensionalAnalyses|salesrep", "");
      String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId",
          "ReportRefundSalesDimensionalAnalyses|warehouseId", "");
      String strOrder = vars.getGlobalVariable("inpOrder",
          "ReportRefundSalesDimensionalAnalyses|order", "Normal");
      String strMayor = vars.getNumericGlobalVariable("inpMayor",
          "ReportRefundSalesDimensionalAnalyses|mayor", "");
      String strMenor = vars.getNumericGlobalVariable("inpMenor",
          "ReportRefundSalesDimensionalAnalyses|menor", "");
      String strRatioMayor = vars.getNumericGlobalVariable("inpRatioMayor",
          "ReportRefundSalesDimensionalAnalyses|ratioMayor", "");
      String strRatioMenor = vars.getNumericGlobalVariable("inpRatioMenor",
          "ReportRefundSalesDimensionalAnalyses|ratioMenor", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportRefundSalesDimensionalAnalyses|currency", strUserCurrencyId);
      String strComparative = "";
      if (vars.commandIn("DEFAULT_COMPARATIVE"))
        strComparative = vars.getRequestGlobalVariable("inpComparative",
            "ReportRefundSalesDimensionalAnalyses|comparative");
      else
        strComparative = vars.getGlobalVariable("inpComparative",
            "ReportRefundSalesDimensionalAnalyses|comparative", "N");
      printPageDataSheet(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup,
          strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef,
          strDateToRef, strOrg, strsalesrepId, strmWarehouseId, strOrder, strMayor, strMenor,
          strRatioMayor, strRatioMenor, strCurrencyId);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_HTML_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportRefundSalesDimensionalAnalyses|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportRefundSalesDimensionalAnalyses|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportRefundSalesDimensionalAnalyses|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportRefundSalesDimensionalAnalyses|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup",
          "ReportRefundSalesDimensionalAnalyses|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportRefundSalesDimensionalAnalyses|partner", IsIDFilter.instance);
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportRefundSalesDimensionalAnalyses|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportRefundSalesDimensionalAnalyses|product", IsIDFilter.instance);
      // ad_ref_list.value where reference_id = 800087
      String strNotShown = vars.getInStringParameter("inpNotShown", IsPositiveIntFilter.instance);
      String strShown = vars.getInStringParameter("inpShown", IsPositiveIntFilter.instance);
      String strOrg = vars.getRequestGlobalVariable("inpOrg",
          "ReportRefundSalesDimensionalAnalyses|org");
      String strsalesrepId = vars.getRequestGlobalVariable("inpSalesrepId",
          "ReportRefundSalesDimensionalAnalyses|salesrep");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportRefundSalesDimensionalAnalyses|warehouseId");
      String strOrder = vars.getRequestGlobalVariable("inpOrder",
          "ReportRefundSalesDimensionalAnalyses|order");
      String strMayor = vars.getNumericParameter("inpMayor", "");
      String strMenor = vars.getNumericParameter("inpMenor", "");
      String strRatioMayor = vars.getNumericParameter("inpRatioMayor", "");
      String strRatioMenor = vars.getNumericParameter("inpRatioMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportRefundSalesDimensionalAnalyses|currency", strUserCurrencyId);
      printPageHtml(request, response, vars, strComparative, strDateFrom, strDateTo,
          strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown,
          strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strmWarehouseId, strOrder,
          strMayor, strMenor, strRatioMayor, strRatioMenor, strCurrencyId);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup,
      String strcBpartnerId, String strProductCategory, String strmProductId, String strNotShown,
      String strShown, String strDateFromRef, String strDateToRef, String strOrg,
      String strsalesrepId, String strmWarehouseId, String strOrder, String strMayor,
      String strMenor, String strRatioMayor, String strRatioMenor, String strCurrencyId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    String discard[] = { "selEliminarHeader1" };
    String strCommand = "EDIT_PDF";
    if (strComparative.equals("Y")) {
      discard[0] = "selEliminarHeader2";
      strCommand = "EDIT_PDF_COMPARATIVE";
    }
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportRefundSalesDimensionalAnalysesFilter", discard)
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
        "ReportRefundSalesDimensionalAnalysesFilter", false, "", "", "", false, "ad_reports",
        strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportRefundSalesDimensionalAnalyses");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportRefundSalesDimensionalAnalyses.html", classInfo.id, classInfo.type,
          strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportRefundSalesDimensionalAnalyses.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportRefundSalesDimensionalAnalyses");
      vars.removeMessage("ReportRefundSalesDimensionalAnalyses");
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
    xmlDocument.setParameter("cBpGroupId", strPartnerGroup);
    xmlDocument.setParameter("mProductCategoryId", strProductCategory);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("salesRepId", strsalesrepId);
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("normal", strOrder);
    xmlDocument.setParameter("amountasc", strOrder);
    xmlDocument.setParameter("amountdesc", strOrder);
    xmlDocument.setParameter("ratioasc", strOrder);
    xmlDocument.setParameter("ratiodesc", strOrder);
    xmlDocument.setParameter("mayor", strMayor);
    xmlDocument.setParameter("menor", strMenor);
    xmlDocument.setParameter("ratioMayor", strRatioMayor);
    xmlDocument.setParameter("ratioMenor", strRatioMenor);
    xmlDocument.setParameter("comparative", strComparative);
    xmlDocument.setParameter("command", strCommand);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportRefundSalesDimensionalAnalyses"), Utility.getContext(this, vars,
              "#User_Client", "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportRefundSalesDimensionalAnalyses", strPartnerGroup);
      xmlDocument.setData("reportC_BP_GROUPID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportRefundSalesDimensionalAnalyses"), Utility.getContext(this, vars,
              "#User_Client", "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportRefundSalesDimensionalAnalyses", strProductCategory);
      xmlDocument.setData("reportM_PRODUCT_CATEGORYID", "liststructure",
          comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportRefundSalesDimensionalAnalyses"), Utility.getContext(this, vars,
              "#User_Client", "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportRefundSalesDimensionalAnalyses", strOrg);
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
      comboTableData = null;

    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "SalesRep_ID",
          "AD_User SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportRefundSalesDimensionalAnalyses"), Utility.getContext(this, vars,
              "#User_Client", "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportRefundSalesDimensionalAnalyses", strsalesrepId);
      xmlDocument.setData("reportSalesRep_ID", "liststructure", comboTableData.select(false));
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
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportSalesDimensionalAnalyze"), Utility.getContext(this, vars, "#User_Client",
              "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportRefundSalesDimensionalAnalyses", strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportRefundSalesDimensionalAnalyses"), Utility.getContext(this, vars,
              "#User_Client", "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportRefundSalesDimensionalAnalyses", strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (vars.getLanguage().equals("en_US")) {
      xmlDocument.setData("structure1",
          ReportRefundSalesDimensionalAnalysesData.selectNotShown(this, strShown));
      xmlDocument.setData("structure2",
          strShown.equals("") ? new ReportRefundSalesDimensionalAnalysesData[0]
              : ReportRefundSalesDimensionalAnalysesData.selectShown(this, strShown));
    } else {
      xmlDocument.setData("structure1", ReportRefundSalesDimensionalAnalysesData.selectNotShownTrl(
          this, vars.getLanguage(), strShown));
      xmlDocument.setData(
          "structure2",
          strShown.equals("") ? new ReportRefundSalesDimensionalAnalysesData[0]
              : ReportRefundSalesDimensionalAnalysesData.selectShownTrl(this, vars.getLanguage(),
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
      String strmProductId, String strNotShown, String strShown, String strDateFromRef,
      String strDateToRef, String strOrg, String strsalesrepId, String strmWarehouseId,
      String strOrder, String strMayor, String strMenor, String strRatioMayor,
      String strRatioMenor, String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");
    XmlDocument xmlDocument = null;
    String strOrderby = "";
    String[] discard = { "", "", "", "", "", "", "" };
    String[] discard1 = { "selEliminarBody1", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard" };
    if (strOrg.equals(""))
      strOrg = vars.getOrg();
    if (strComparative.equals("Y"))
      discard1[0] = "selEliminarBody2";
    String strTitle = "";
    strTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom + " "
        + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;
    if (!strPartnerGroup.equals(""))
      strTitle = strTitle + ", " + Utility.messageBD(this, "ForBPartnerGroup", vars.getLanguage())
          + " " + ReportRefundSalesDimensionalAnalysesData.selectBpgroup(this, strPartnerGroup);
    if (!strProductCategory.equals(""))
      strTitle = strTitle
          + ", "
          + Utility.messageBD(this, "ProductCategory", vars.getLanguage())
          + " "
          + ReportRefundSalesDimensionalAnalysesData
              .selectProductCategory(this, strProductCategory);
    if (!strsalesrepId.equals(""))
      strTitle = strTitle + ", " + Utility.messageBD(this, "TheSalesRep", vars.getLanguage()) + " "
          + ReportRefundSalesDimensionalAnalysesData.selectSalesrep(this, strsalesrepId);
    if (!strmWarehouseId.equals(""))
      strTitle = strTitle + " " + Utility.messageBD(this, "And", vars.getLanguage()) + " "
          + Utility.messageBD(this, "TheWarehouse", vars.getLanguage()) + " "
          + ReportRefundSalesDimensionalAnalysesData.selectMwarehouse(this, strmWarehouseId);

    ReportRefundSalesDimensionalAnalysesData[] data = null;
    String[] strShownArray = { "", "", "", "", "", "", "" };
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
    String[] strTextShow = { "", "", "", "", "", "", "" };
    int intDiscard = 0;
    int intAuxDiscard = -1;
    for (int i = 0; i < 7; i++) {
      if (strShownArray[i].equals("1")) {
        strTextShow[i] = "C_BP_GROUP.NAME";
        intDiscard++;
      } else if (strShownArray[i].equals("2")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('C_Bpartner'), to_char( C_BPARTNER.C_BPARTNER_ID), to_char('"
            + vars.getLanguage() + "'))";
        intDiscard++;
      } else if (strShownArray[i].equals("3")) {
        strTextShow[i] = "M_PRODUCT_CATEGORY.NAME";
        intDiscard++;
      } else if (strShownArray[i].equals("4")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('M_Product'), to_char( M_PRODUCT.M_PRODUCT_ID), to_char('"
            + vars.getLanguage() + "'))||' ('||UOMSYMBOL||')'";
        intAuxDiscard = i;
        intDiscard++;
      } else if (strShownArray[i].equals("5")) {
        strTextShow[i] = "C_ORDER.DOCUMENTNO";
        intDiscard++;
      } else if (strShownArray[i].equals("6")) {
        strTextShow[i] = "AD_USER.FIRSTNAME||' '||' '||AD_USER.LASTNAME";
        intDiscard++;
      } else if (strShownArray[i].equals("7")) {
        strTextShow[i] = "M_WAREHOUSE.NAME";
        intDiscard++;
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
          strOrderby = " ORDER BY LINENETAMT ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby = " ORDER BY LINENETAMT DESC";
        } else if (strOrder.equals("Ratioasc")) {
          strOrderby = " ORDER BY RATIO ASC";
        } else if (strOrder.equals("Ratiodesc")) {
          strOrderby = " ORDER BY RATIO DESC";
        } else {
          strOrderby = "1";
        }
      } else {
        if (strOrder.equals("Normal")) {
          strOrderby += "NIVEL" + k;
        } else if (strOrder.equals("Amountasc")) {
          strOrderby += "LINENETAMT ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby += "LINENETAMT DESC";
        } else if (strOrder.equals("Ratioasc")) {
          strOrderby += "RATIO ASC";
        } else if (strOrder.equals("Ratiodesc")) {
          strOrderby += "RATIO DESC";
        } else {
          strOrderby = "1";
        }
      }

    } else {
      strOrderby = " ORDER BY 1";
    }
    String strHaving = "";
    if (!strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) > " + strMayor + " AND SUM(LINENETAMT) < " + strMenor;
    } else if (!strMayor.equals("") && strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) > " + strMayor;
    } else if (strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) < " + strMenor;
    } else {
    }
    if (strHaving.equals("")) {
      if (!strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor
            + " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else if (!strRatioMayor.equals("") && strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor;
      } else if (strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else {
      }
    } else {
      if (!strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor
            + " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else if (!strRatioMayor.equals("") && strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor;
      } else if (strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else {
      }
    }
    strOrderby = strHaving + strOrderby;

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    if (strComparative.equals("Y")) {
      try {
        data = ReportRefundSalesDimensionalAnalysesData.select(this, strCurrencyId, strTextShow[0],
            strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], strTextShow[5],
            strTextShow[6], Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()),
                strOrg), Utility.getContext(this, vars, "#User_Client",
                "ReportRefundInvoiceCustomerDimensionalAnalyses"), strDateFrom, DateTimeData
                .nDaysAfter(this, strDateTo, "1"), strPartnerGroup, strcBpartnerId,
            strProductCategory, strmProductId, strsalesrepId, strmWarehouseId, strDateFromRef,
            DateTimeData.nDaysAfter(this, strDateToRef, "1"), strOrderby);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    } else {
      try {
        data = ReportRefundSalesDimensionalAnalysesData.selectNoComparative(this, strCurrencyId,
            strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4],
            strTextShow[5], strTextShow[6], Tree.getMembers(this,
                TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this,
                vars, "#User_Client", "ReportRefundInvoiceCustomerDimensionalAnalyses"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strPartnerGroup,
            strcBpartnerId, strProductCategory, strmProductId, strsalesrepId, strmWarehouseId,
            strOrderby);
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
      if (data.length == 0 || data == null) {
        data = ReportRefundSalesDimensionalAnalysesData.set();
      } else {
        int contador = intDiscard;
        if (intAuxDiscard != -1)
          contador = intAuxDiscard;
        int k = 1;
        if (strComparative.equals("Y")) {
          for (int j = contador; j > 0; j--) {
            discard1[k] = "fieldTotalQtyNivel" + String.valueOf(j);
            discard1[k + 12] = "fieldTotalRefundQtyNivel" + String.valueOf(j);
            discard1[k + 24] = "fieldUomsymbol" + String.valueOf(j);
            discard1[k + 6] = "fieldTotalRefQtyNivel" + String.valueOf(j);
            discard1[k + 18] = "fieldTotalRefRefundQtyNivel" + String.valueOf(j);
            k++;
          }
        } else {
          for (int j = contador; j > 0; j--) {
            discard1[k] = "fieldNoncomparativeTotalQtyNivel" + String.valueOf(j);
            discard1[k + 10] = "fieldNoncomparativeTotalRefundQtyNivel" + String.valueOf(j);
            discard1[k + 20] = "fieldNoncomparativeUomsymbol" + String.valueOf(j);
            k++;
          }
        }

      }
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportRefundSalesDimensionalAnalysesEdition",
          discard1).createXmlDocument();
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("eliminar2", discard[1]);
      xmlDocument.setParameter("eliminar3", discard[2]);
      xmlDocument.setParameter("eliminar4", discard[3]);
      xmlDocument.setParameter("eliminar5", discard[4]);
      xmlDocument.setParameter("eliminar6", discard[5]);
      xmlDocument.setParameter("eliminar7", discard[6]);
      xmlDocument.setParameter("title", strTitle);
      String strCurISOSym = Utility.stringISOSymbol(this, strCurrencyId);
      strCurISOSym = strCurISOSym.replace('(', ' ');
      strCurISOSym = strCurISOSym.replace(')', ' ');
      xmlDocument.setParameter("convisosym", strCurISOSym);
      xmlDocument.setParameter("constante", "100");
      if (strComparative.equals("Y")) {
        xmlDocument.setData("structure1", data);
      } else {
        xmlDocument.setData("structure2", data);
      }
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet ReportRefundSalesDimensionalAnalyses. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
