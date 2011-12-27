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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
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

public class ReportMaterialDimensionalAnalysesJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT", "DEFAULT_COMPARATIVE")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportMaterialDimensionalAnalysesJR|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportMaterialDimensionalAnalysesJR|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef",
          "ReportMaterialDimensionalAnalysesJR|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef",
          "ReportMaterialDimensionalAnalysesJR|dateToRef", "");
      String strPartnerGroup = vars.getGlobalVariable("inpPartnerGroup",
          "ReportMaterialDimensionalAnalysesJR|partnerGroup", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportMaterialDimensionalAnalysesJR|partner", "", IsIDFilter.instance);
      String strProductCategory = vars.getGlobalVariable("inpProductCategory",
          "ReportMaterialDimensionalAnalysesJR|productCategory", "");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportMaterialDimensionalAnalysesJR|product", "", IsIDFilter.instance);
      // ad_ref_list.value for reference_id 800086
      String strNotShown = vars.getInGlobalVariable("inpNotShown",
          "ReportMaterialDimensionalAnalysesJR|notShown", "", IsPositiveIntFilter.instance);
      String strShown = vars.getInGlobalVariable("inpShown",
          "ReportMaterialDimensionalAnalysesJR|shown", "", IsPositiveIntFilter.instance);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportMaterialDimensionalAnalysesJR|org",
          "");
      String strOrder = vars.getGlobalVariable("inpOrder",
          "ReportMaterialDimensionalAnalysesJR|order", "Normal");
      String strMayor = vars.getNumericGlobalVariable("inpMayor",
          "ReportMaterialDimensionalAnalysesJR|mayor", "");
      String strMenor = vars.getNumericGlobalVariable("inpMenor",
          "ReportMaterialDimensionalAnalysesJR|menor", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportMaterialDimensionalAnalysesJR|currency", strUserCurrencyId);
      String strComparative = "";
      if (vars.commandIn("DEFAULT_COMPARATIVE"))
        strComparative = vars.getRequestGlobalVariable("inpComparative",
            "ReportMaterialDimensionalAnalysesJR|comparative");
      else
        strComparative = vars.getGlobalVariable("inpComparative",
            "ReportMaterialDimensionalAnalysesJR|comparative", "N");
      printPageDataSheet(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup,
          strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef,
          strDateToRef, strOrg, strOrder, strMayor, strMenor, strCurrencyId);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_HTML_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportMaterialDimensionalAnalysesJR|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportMaterialDimensionalAnalysesJR|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportMaterialDimensionalAnalysesJR|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportMaterialDimensionalAnalysesJR|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup",
          "ReportMaterialDimensionalAnalysesJR|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportMaterialDimensionalAnalysesJR|partner", IsIDFilter.instance);
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportMaterialDimensionalAnalysesJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportMaterialDimensionalAnalysesJR|product", IsIDFilter.instance);
      // ad_ref_list.value for reference_id 800086
      String strNotShown = vars.getInStringParameter("inpNotShown", IsPositiveIntFilter.instance);
      String strShown = vars.getInStringParameter("inpShown", IsPositiveIntFilter.instance);
      String strOrg = vars.getRequestGlobalVariable("inpOrg",
          "ReportMaterialDimensionalAnalysesJR|org");
      String strOrder = vars.getRequestGlobalVariable("inpOrder",
          "ReportMaterialDimensionalAnalysesJR|order");
      String strMayor = vars.getNumericParameter("inpMayor", "");
      String strMenor = vars.getNumericParameter("inpMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportMaterialDimensionalAnalysesJR|currency", strUserCurrencyId);
      printPageHtml(request, response, vars, strComparative, strDateFrom, strDateTo,
          strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown,
          strShown, strDateFromRef, strDateToRef, strOrg, strOrder, strMayor, strMenor,
          strCurrencyId, "html");
    } else if (vars.commandIn("EDIT_PDF", "EDIT_PDF_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportMaterialDimensionalAnalysesJR|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportMaterialDimensionalAnalysesJR|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportMaterialDimensionalAnalysesJR|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportMaterialDimensionalAnalysesJR|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup",
          "ReportMaterialDimensionalAnalysesJR|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportMaterialDimensionalAnalysesJR|partner", IsIDFilter.instance);
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportMaterialDimensionalAnalysesJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportMaterialDimensionalAnalysesJR|product", IsIDFilter.instance);
      // ad_ref_list.value for reference_id 800086
      String strNotShown = vars.getInStringParameter("inpNotShown", IsPositiveIntFilter.instance);
      String strShown = vars.getInStringParameter("inpShown", IsPositiveIntFilter.instance);
      String strOrg = vars.getRequestGlobalVariable("inpOrg",
          "ReportMaterialDimensionalAnalysesJR|org");
      String strOrder = vars.getRequestGlobalVariable("inpOrder",
          "ReportMaterialDimensionalAnalysesJR|order");
      String strMayor = vars.getNumericParameter("inpMayor", "");
      String strMenor = vars.getNumericParameter("inpMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportMaterialDimensionalAnalysesJR|currency", strUserCurrencyId);
      printPageHtml(request, response, vars, strComparative, strDateFrom, strDateTo,
          strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown,
          strShown, strDateFromRef, strDateToRef, strOrg, strOrder, strMayor, strMenor,
          strCurrencyId, "pdf");
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup,
      String strcBpartnerId, String strProductCategory, String strmProductId, String strNotShown,
      String strShown, String strDateFromRef, String strDateToRef, String strOrg, String strOrder,
      String strMayor, String strMenor, String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    String discard[] = { "selEliminarHeader1" };
    if (strComparative.equals("Y")) {
      discard[0] = "selEliminarHeader2";
    }
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportMaterialDimensionalAnalysesFilterJR", discard)
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
        "ReportMaterialDimensionalAnalysesFilterJR", false, "", "", "", false, "ad_reports",
        strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportMaterialDimensionalAnalysesJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportMaterialDimensionalAnalysesFilterJR.html", classInfo.id, classInfo.type,
          strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportMaterialDimensionalAnalysesFilterJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportMaterialDimensionalAnalysesJR");
      vars.removeMessage("ReportMaterialDimensionalAnalysesJR");
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
    xmlDocument.setParameter("normal", strOrder);
    xmlDocument.setParameter("amountasc", strOrder);
    xmlDocument.setParameter("amountdesc", strOrder);
    xmlDocument.setParameter("mayor", strMayor);
    xmlDocument.setParameter("menor", strMenor);
    xmlDocument.setParameter("comparative", strComparative);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportMaterialDimensionalAnalysesJR"), Utility.getContext(this, vars,
              "#User_Client", "ReportMaterialDimensionalAnalysesJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportMaterialDimensionalAnalysesJR", strPartnerGroup);
      xmlDocument.setData("reportC_BP_GROUPID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportMaterialDimensionalAnalysesJR"), Utility.getContext(this, vars,
              "#User_Client", "ReportMaterialDimensionalAnalysesJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportMaterialDimensionalAnalysesJR", strProductCategory);
      xmlDocument.setData("reportM_PRODUCT_CATEGORYID", "liststructure",
          comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", strCurrencyId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportMaterialDimensionalAnalysesJR"), Utility.getContext(this, vars,
              "#User_Client", "ReportMaterialDimensionalAnalysesJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportMaterialDimensionalAnalysesJR", strCurrencyId);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportMaterialDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client",
              "ReportMaterialDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,
          "ReportMaterialDimensionalAnalyzeJR", strOrg);
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

    if (vars.getLanguage().equals("en_US")) {
      xmlDocument.setData("structure1",
          ReportMaterialDimensionalAnalysesJRData.selectNotShown(this, strShown));
      xmlDocument.setData("structure2",
          strShown.equals("") ? new ReportMaterialDimensionalAnalysesJRData[0]
              : ReportMaterialDimensionalAnalysesJRData.selectShown(this, strShown));
    } else {
      xmlDocument.setData("structure1", ReportMaterialDimensionalAnalysesJRData.selectNotShownTrl(
          this, vars.getLanguage(), strShown));
      xmlDocument.setData(
          "structure2",
          strShown.equals("") ? new ReportMaterialDimensionalAnalysesJRData[0]
              : ReportMaterialDimensionalAnalysesJRData.selectShownTrl(this, vars.getLanguage(),
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
      String strDateToRef, String strOrg, String strOrder, String strMayor, String strMenor,
      String strCurrencyId, String strOutput) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");
    String strOrderby = "";
    String[] discard = { "", "", "", "", "" };
    String[] discard1 = { "selEliminarBody1", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard" };
    if (strOrg.equals(""))
      strOrg = vars.getOrg();
    if (strComparative.equals("Y"))
      discard1[0] = "selEliminarBody2";
    String strTitle = "";
    strTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom + " "
        + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;
    if (!strPartnerGroup.equals(""))
      strTitle = strTitle + ", " + Utility.messageBD(this, "ForBPartnerGroup", vars.getLanguage())
          + " " + ReportMaterialDimensionalAnalysesJRData.selectBpgroup(this, strPartnerGroup);

    if (!strProductCategory.equals(""))
      strTitle = strTitle + " " + Utility.messageBD(this, "And", vars.getLanguage()) + " "
          + Utility.messageBD(this, "ProductCategory", vars.getLanguage()) + " "
          + ReportMaterialDimensionalAnalysesJRData.selectProductCategory(this, strProductCategory);

    ReportMaterialDimensionalAnalysesJRData[] data = null;
    String[] strShownArray = { "", "", "", "", "" };
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
    ReportMaterialDimensionalAnalysesJRData[] dimensionLabel = null;
    if (vars.getLanguage().equals("en_US")) {
      dimensionLabel = ReportMaterialDimensionalAnalysesJRData.selectNotShown(this, "");
    } else {
      dimensionLabel = ReportMaterialDimensionalAnalysesJRData.selectNotShownTrl(this,
          vars.getLanguage(), "");
    }
    String[] strTextShow = { "", "", "", "", "" };
    String[] strLevelLabel = { "", "", "", "", "" };
    int intDiscard = 0;
    int intProductLevel = 6;
    int intAuxDiscard = -1;
    for (int i = 0; i < 5; i++) {
      if (strShownArray[i].equals("1")) {
        strTextShow[i] = "C_BP_GROUP.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[0].name;
      } else if (strShownArray[i].equals("2")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('C_Bpartner'), to_char( C_BPARTNER.C_BPARTNER_ID), to_char('"
            + vars.getLanguage() + "'))";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[1].name;
      } else if (strShownArray[i].equals("3")) {
        strTextShow[i] = "M_PRODUCT_CATEGORY.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[2].name;
      } else if (strShownArray[i].equals("4")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('M_Product'), to_char( M_PRODUCT.M_PRODUCT_ID), to_char('"
            + vars.getLanguage() + "'))||' ('||UOMSYMBOL||')'";
        intAuxDiscard = i;
        intDiscard++;
        intProductLevel = i + 1;
        strLevelLabel[i] = dimensionLabel[3].name;
      } else if (strShownArray[i].equals("5")) {
        strTextShow[i] = "M_INOUT.DOCUMENTNO";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[4].name;
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
          strOrderby = " ORDER BY QTY ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby = " ORDER BY QTY DESC";
        } else {
          strOrderby = "1";
        }
      } else {
        if (strOrder.equals("Normal")) {
          strOrderby += "NIVEL" + k;
        } else if (strOrder.equals("Amountasc")) {
          strOrderby += "QTY ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby += "QTY DESC";
        } else {
          strOrderby = "1";
        }
      }

    } else {
      strOrderby = " ORDER BY 1";
    }
    String strHaving = "";
    if (!strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING (SUM(QTY) > " + strMayor + " AND SUM(QTY) < " + strMenor + ")";
    } else if (!strMayor.equals("") && strMenor.equals("")) {
      strHaving = " HAVING (SUM(QTY) > " + strMayor + ")";
    } else if (strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING (SUM(QTY) < " + strMenor + ")";
    } else {
      strHaving = " HAVING SUM(QTY) <> 0 OR SUM(QTYREF) <> 0";
    }
    strOrderby = strHaving + strOrderby;

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    if (strComparative.equals("Y")) {
      try {
        data = ReportMaterialDimensionalAnalysesJRData.select(this, strCurrencyId, strTextShow[0],
            strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4],
            Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
            Utility.getContext(this, vars, "#User_Client", "ReportMaterialDimensionalAnalysesJR"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strPartnerGroup,
            strcBpartnerId, strProductCategory, strmProductId, strDateFromRef,
            DateTimeData.nDaysAfter(this, strDateToRef, "1"), strOrderby);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    } else {
      try {
        data = ReportMaterialDimensionalAnalysesJRData.selectNoComparative(this, strCurrencyId,
            strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4],
            Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
            Utility.getContext(this, vars, "#User_Client", "ReportMaterialDimensionalAnalysesJR"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strPartnerGroup,
            strcBpartnerId, strProductCategory, strmProductId, strOrderby);
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
      String strReportPath = "";
      if (strComparative.equals("Y")) {
        strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/SimpleDimensionalComparative.jrxml";
      } else {
        strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/SimpleDimensionalNoComparative.jrxml";
      }

      if (data.length == 0 || data == null) {
        data = ReportMaterialDimensionalAnalysesJRData.set();
      }
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("LEVEL1_LABEL", strLevelLabel[0]);
      parameters.put("LEVEL2_LABEL", strLevelLabel[1]);
      parameters.put("LEVEL3_LABEL", strLevelLabel[2]);
      parameters.put("LEVEL4_LABEL", strLevelLabel[3]);
      parameters.put("LEVEL5_LABEL", strLevelLabel[4]);
      parameters.put("DIMENSIONS", new Integer(intDiscard));
      parameters.put("REPORT_SUBTITLE", strTitle);
      parameters.put("PRODUCT_LEVEL", new Integer(intProductLevel));
      renderJR(vars, response, strReportPath, strOutput, parameters, data, null);
    }
  }

  public String getServletInfo() {
    return "Servlet ReportPurchaseDimensionalAnalysesJR. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
