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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
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
import org.openbravo.xmlEngine.XmlDocument;

public class ReportTrialBalance extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReportTrialBalance|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportTrialBalance|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportTrialBalance|DateTo", "");
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReportTrialBalance|PageNo", "1");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportTrialBalance|Org", "");
      String strLevel = vars.getGlobalVariable("inpLevel", "ReportTrialBalance|Level", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportTrialBalance|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportTrialBalance|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReportTrialBalance|cProjectId", "", IsIDFilter.instance);
      String strGroupBy = vars.getGlobalVariable("inpGroupBy", "ReportTrialBalance|GroupBy", "");
      String strcElementValueFrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportTrialBalance|C_ElementValue_IDFROM", "");
      String strcElementValueTo = vars.getGlobalVariable(
          "inpcElementValueIdTo",
          "ReportTrialBalance|C_ElementValue_IDTO",
          ReportTrialBalanceData.selectLastAccount(this,
              Utility.getContext(this, vars, "#AccessibleOrgTree", "Account"),
              Utility.getContext(this, vars, "#User_Client", "Account")));
      String strNotInitialBalance = vars.getGlobalVariable("inpNotInitialBalance",
          "ReportTrialBalance|notInitialBalance", "N");
      String strcElementValueFromDes = "", strcElementValueToDes = "";
      if (!strcElementValueFrom.equals(""))
        strcElementValueFromDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueFrom);
      if (!strcElementValueTo.equals(""))
        strcElementValueToDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueTo);
      strcElementValueFromDes = (strcElementValueFromDes == null) ? "" : strcElementValueFromDes;
      strcElementValueToDes = (strcElementValueToDes == null) ? "" : strcElementValueToDes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcElementValueFromDes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcElementValueToDes);

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strOrg, strLevel,
          strcElementValueFrom, strcElementValueTo, strcElementValueFromDes, strcElementValueToDes,
          strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId, strNotInitialBalance,
          strGroupBy);

    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportTrialBalance|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportTrialBalance|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportTrialBalance|DateTo");
      String strPageNo = vars.getRequestGlobalVariable("inpPageNo", "ReportTrialBalance|PageNo");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportTrialBalance|Org");
      String strLevel = vars.getRequestGlobalVariable("inpLevel", "ReportTrialBalance|Level");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportTrialBalance|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportTrialBalance|mProductId", IsIDFilter.instance);
      String strcProjectId = vars.getRequestInGlobalVariable("inpcProjectId_IN",
          "ReportTrialBalance|cProjectId", IsIDFilter.instance);
      String strGroupBy = vars.getRequestGlobalVariable("inpGroupBy", "ReportTrialBalance|GroupBy");
      String strcElementValueFrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportTrialBalance|C_ElementValue_IDFROM");
      String strcElementValueTo = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportTrialBalance|C_ElementValue_IDTO");
      String strNotInitialBalance = vars.getStringParameter("inpNotInitialBalance", "N");
      vars.setSessionValue("ReportTrialBalance|notInitialBalance", strNotInitialBalance);
      String strcElementValueFromDes = "", strcElementValueToDes = "";
      if (!strcElementValueFrom.equals(""))
        strcElementValueFromDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueFrom);
      if (!strcElementValueTo.equals(""))
        strcElementValueToDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueTo);
      vars.setSessionValue("inpElementValueIdFrom_DES", strcElementValueFromDes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcElementValueToDes);

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strOrg, strLevel,
          strcElementValueFrom, strcElementValueTo, strcElementValueFromDes, strcElementValueToDes,
          strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId, strNotInitialBalance,
          strGroupBy);

    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportTrialBalance|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportTrialBalance|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportTrialBalance|DateTo");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportTrialBalance|Org");
      String strLevel = vars.getRequestGlobalVariable("inpLevel", "ReportTrialBalance|Level");
      String strcElementValueFrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportTrialBalance|C_ElementValue_IDFROM", "");
      String strcElementValueTo = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportTrialBalance|C_ElementValue_IDTO", "");
      String strcElementValueFromDes = "", strcElementValueToDes = "";
      if (!strcElementValueFrom.equals(""))
        strcElementValueFromDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueFrom);
      if (!strcElementValueTo.equals(""))
        strcElementValueToDes = ReportTrialBalanceData.selectSubaccountDescription(this,
            strcElementValueTo);
      strcElementValueFromDes = (strcElementValueFromDes == null) ? "" : strcElementValueFromDes;
      strcElementValueToDes = (strcElementValueToDes == null) ? "" : strcElementValueToDes;
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportTrialBalance|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportTrialBalance|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReportTrialBalance|cProjectId", "", IsIDFilter.instance);
      String strGroupBy = vars.getRequestGlobalVariable("inpGroupBy", "ReportTrialBalance|GroupBy");
      String strPageNo = vars.getRequestGlobalVariable("inpPageNo", "ReportTrialBalance|PageNo");
      String strNotInitialBalance = vars.getStringParameter("inpNotInitialBalance", "N");
      vars.setSessionValue("ReportTrialBalance|notInitialBalance", strNotInitialBalance);
      if (vars.commandIn("PDF"))
        printPageDataPDF(request, response, vars, strDateFrom, strDateTo, strOrg, strLevel,
            strcElementValueFrom, strcElementValueFromDes, strcElementValueTo,
            strcElementValueToDes, strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId,
            strNotInitialBalance, strGroupBy, strPageNo);
      else
        printPageDataXLS(request, response, vars, strDateFrom, strDateTo, strOrg, strLevel,
            strcElementValueFrom, strcElementValueTo, strcBpartnerId, strmProductId, strcProjectId,
            strcAcctSchemaId, strNotInitialBalance, strGroupBy);

    } else if (vars.commandIn("OPEN")) {
      String strAccountId = vars.getRequiredStringParameter("inpcAccountId");
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportTrialBalance|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportTrialBalance|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportTrialBalance|DateTo");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportTrialBalance|Org");
      String strLevel = vars.getRequestGlobalVariable("inpLevel", "ReportTrialBalance|Level");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportTrialBalance|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportTrialBalance|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReportTrialBalance|cProjectId", "", IsIDFilter.instance);
      String strGroupBy = vars.getRequestGlobalVariable("inpGroupBy", "ReportTrialBalance|GroupBy");
      String strNotInitialBalance = vars.getStringParameter("inpNotInitialBalance", "N");
      vars.setSessionValue("ReportTrialBalance|notInitialBalance", strNotInitialBalance);

      printPageOpen(response, vars, strDateFrom, strDateTo, strOrg, strLevel, strcBpartnerId,
          strmProductId, strcProjectId, strcAcctSchemaId, strGroupBy, strAccountId,
          strNotInitialBalance);

    } else {
      pageError(response);
    }
  }

  private void printPageOpen(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strLevel, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strcAcctSchemaId, String strGroupBy,
      String strAccountId, String strNotInitialBalance) throws IOException, ServletException {

    ReportTrialBalanceData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    log4j.debug("Output: Expand subaccount details " + strAccountId);

    data = ReportTrialBalanceData.selectAccountLines(this, strGroupBy, vars.getLanguage(),
        strLevel, strOrgFamily,
        Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"), null, null,
        strDateFrom, strAccountId, strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId,
        (strNotInitialBalance.equals("Y") ? "O" : "P"),
        DateTimeData.nDaysAfter(this, strDateTo, "1"));

    if (data == null) {
      data = ReportTrialBalanceData.set();
    }

    // response.setContentType("text/plain");
    response.setContentType("text/html; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();

    // Create JSON object
    // { "rows" : [ {"td1":"Bellen Ent.","td3":"0,00","td2":"0,00","td5":"-48,59","td4":"48,59"},
    // {"td1":"Mafalda Corporation","td3":"34,56","td2":"0,00","td5":"-334,79","td4":"369,35"}],
    // "config" : {"classDefault":"DataGrid_Body_Cell","classAmount":"DataGrid_Body_Cell_Amount"}
    // }
    DecimalFormat df = Utility.getFormat(vars, "euroInform");
    JSONObject table = new JSONObject();
    JSONArray tr = new JSONArray();
    Map<String, String> tds = null;
    try {

      for (int i = 0; i < data.length; i++) {
        tds = new HashMap<String, String>();
        tds.put("td1", data[i].groupbyname);
        tds.put("td2", df.format(new BigDecimal(data[i].saldoInicial)));
        tds.put("td3", df.format(new BigDecimal(data[i].amtacctdr)));
        tds.put("td4", df.format(new BigDecimal(data[i].amtacctcr)));
        tds.put("td5", df.format(new BigDecimal(data[i].saldoFinal)));
        tr.put(data.length - (i + 1), tds);
        table.put("rows", tr);
      }
      Map<String, String> props = new HashMap<String, String>();
      props.put("classAmount", "DataGrid_Body_Cell_Amount");
      props.put("classDefault", "DataGrid_Body_Cell");
      table.put("config", props);

    } catch (JSONException e) {
      log4j.error("Error creating JSON object for representing subaccount lines", e);
      throw new ServletException(e);
    }

    log4j.debug("JSON string: " + table.toString());

    out.println("jsonTable = " + table.toString());
    out.close();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strPageNo, String strOrg, String strLevel,
      String strcElementValueFrom, String strcElementValueTo, String strcElementValueFromDes,
      String strcElementValueToDes, String strcBpartnerId, String strmProductId,
      String strcProjectId, String strcAcctSchemaId, String strNotInitialBalance, String strGroupBy)
      throws IOException, ServletException {

    String strMessage = "";
    XmlDocument xmlDocument = null;
    ReportTrialBalanceData[] data = null;
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    String discard[] = { "sectionGridView", "discard", "discard", "discard" };

    if (strLevel.equals("C")) {
      discard[1] = "fieldId1";
    } else {
      discard[1] = "fieldDescAccount";
    }

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strTreeAccount = ReportTrialBalanceData.treeAccount(this, vars.getClient());
    // Remember values
    String strcBpartnerIdAux = strcBpartnerId;
    String strmProductIdAux = strmProductId;
    String strcProjectIdAux = strcProjectId;

    String strAccountFromValue = ReportTrialBalanceData.selectAccountValue(this,
        strcElementValueFrom);
    String strAccountToValue = ReportTrialBalanceData.selectAccountValue(this, strcElementValueTo);

    log4j.debug("Output: DataSheet");
    log4j.debug("strTreeOrg: " + strTreeOrg + "strOrgFamily: " + strOrgFamily + "strTreeAccount: "
        + strTreeAccount);
    log4j.debug("strcBpartnerId: " + strcBpartnerId + "strmProductId: " + strmProductId
        + "strcProjectId: " + strcProjectId);

    if (strDateFrom.equals("") && strDateTo.equals("")) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportTrialBalance", discard).createXmlDocument();
      data = ReportTrialBalanceData.set();
      if (vars.commandIn("FIND")) {
        strMessage = Utility.messageBD(this, "BothDatesCannotBeBlank", vars.getLanguage());
        log4j.warn("Both dates are blank");
      }
    } else {
      if (strLevel.equals("S")) { // SubAccount selected
        data = ReportTrialBalanceData.selectAccountLines(this, "", vars.getLanguage(), strLevel,
            strOrgFamily, Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"),
            strAccountFromValue, strAccountToValue, strDateFrom, null, strcBpartnerId,
            strmProductId, strcProjectId, strcAcctSchemaId, (strNotInitialBalance.equals("Y") ? "O"
                : "P"), DateTimeData.nDaysAfter(this, strDateTo, "1"));
        if (strGroupBy.equals(""))
          discard[2] = "showExpand";

      } else {
        discard[2] = "showExpand";
        data = getDataWhenNotSubAccount(vars, strDateFrom, strDateTo, strOrg, strOrgFamily,
            strcAcctSchemaId, strLevel, strTreeAccount, strNotInitialBalance);
      }

      if (data != null && data.length > 0)
        discard[0] = "discard";

    }

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportTrialBalance", discard).createXmlDocument();
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_ElementValue level", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportTrialBalance"), Utility.getContext(this, vars, "#User_Client",
              "ReportTrialBalance"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportTrialBalance", "");
      xmlDocument.setData("reportLevel", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportTrialBalance", false, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar
        .prepareRelationBarTemplate(
            false,
            false,
            "validate(); submitCommandForm('XLS', false, frmMain, 'ReportTrialBalanceExcel.xls', 'EXCEL');return false;");
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportTrialBalance");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportTrialBalance.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportTrialBalance.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    OBError myMessage = vars.getMessage("ReportTrialBalance");
    vars.removeMessage("ReportTrialBalance");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"),
          Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"), '*');
      comboTableData.fillParameters(null, "ReportTrialBalance", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument
        .setData("reportC_ACCTSCHEMA_ID", "liststructure", AccountingSchemaMiscData
            .selectC_ACCTSCHEMA_ID(this,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"),
                Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
                strcAcctSchemaId));
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("PageNo", strPageNo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("Level", strLevel);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setParameter("paramElementvalueIdFrom", strcElementValueFrom);
    xmlDocument.setParameter("paramElementvalueIdTo", strcElementValueTo);
    xmlDocument.setParameter("inpElementValueIdFrom_DES", strcElementValueFromDes);
    xmlDocument.setParameter("inpElementValueIdTo_DES", strcElementValueToDes);
    xmlDocument.setParameter("paramMessage", (strMessage.equals("") ? "" : "alert('" + strMessage
        + "');"));
    xmlDocument.setParameter("groupbyselected", strGroupBy);
    xmlDocument.setParameter("notInitialBalance", strNotInitialBalance);

    xmlDocument.setData(
        "reportCBPartnerId_IN",
        "liststructure",
        SelectorUtilityData.selectBpartner(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerIdAux));

    xmlDocument.setData(
        "reportMProductId_IN",
        "liststructure",
        SelectorUtilityData.selectMproduct(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strmProductIdAux));

    xmlDocument.setData(
        "reportCProjectId_IN",
        "liststructure",
        SelectorUtilityData.selectProject(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strcProjectIdAux));

    if (data != null && data.length > 0) {
      xmlDocument.setData("structure1", data);
    } else {
      if (vars.commandIn("FIND")) {
        // No data has been found. Show warning message.
        xmlDocument.setParameter("messageType", "WARNING");
        xmlDocument.setParameter("messageTitle",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()));
        xmlDocument.setParameter("messageMessage",
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      }
    }

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strLevel, String strcElementValueFrom, String strcElementValueTo,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strcAcctSchemaId,
      String strNotInitialBalance, String strGroupBy) throws IOException, ServletException {

    response.setContentType("text/html; charset=UTF-8");
    ReportTrialBalanceData[] data = null;
    boolean showDimensions = false;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strTreeAccount = ReportTrialBalanceData.treeAccount(this, vars.getClient());

    String strAccountFromValue = ReportTrialBalanceData.selectAccountValue(this,
        strcElementValueFrom);
    String strAccountToValue = ReportTrialBalanceData.selectAccountValue(this, strcElementValueTo);

    log4j.debug("Output: XLS report");
    log4j.debug("strTreeOrg: " + strTreeOrg + "strOrgFamily: " + strOrgFamily + "strTreeAccount: "
        + strTreeAccount);
    log4j.debug("strcBpartnerId: " + strcBpartnerId + "strmProductId: " + strmProductId
        + "strcProjectId: " + strcProjectId);

    if (!strDateFrom.equals("") && !strDateTo.equals("") && !strOrg.equals("")
        && !strcAcctSchemaId.equals("")) {

      if (strLevel.equals("S")) {
        data = ReportTrialBalanceData.selectXLS(this, vars.getLanguage(), strLevel, strOrgFamily,
            Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"),
            strAccountFromValue, strAccountToValue, strDateFrom, strcBpartnerId, strmProductId,
            strcProjectId, strcAcctSchemaId, (strNotInitialBalance.equals("Y") ? "O" : "P"),
            DateTimeData.nDaysAfter(this, strDateTo, "1"));
        showDimensions = true;
      } else {
        data = getDataWhenNotSubAccount(vars, strDateFrom, strDateTo, strOrg, strOrgFamily,
            strcAcctSchemaId, strLevel, strTreeAccount, strNotInitialBalance);
      }

      if (data == null || data.length == 0) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      } else {

        String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportTrialBalanceExcel.jrxml";

        HashMap<String, Object> parameters = new HashMap<String, Object>();

        String strLanguage = vars.getLanguage();

        StringBuilder strSubTitle = new StringBuilder();
        strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
            + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
        strSubTitle.append(ReportTrialBalanceData.selectCompany(this, vars.getClient()) + " - ");
        strSubTitle.append(ReportTrialBalanceData.selectOrgName(this, strOrg) + ")");
        parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
        parameters.put("SHOWTOTALS", false);
        parameters.put("SHOWDIMENSIONS", showDimensions);

        renderJR(vars, response, strReportName, "xls", parameters, data, null);
      }
    } else {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    }

  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strLevel, String strcElementValueFrom, String strcElementValueFromDes,
      String strcElementValueTo, String strcElementValueToDes, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strcAcctSchemaId,
      String strNotInitialBalance, String strGroupBy, String strPageNo) throws IOException,
      ServletException {

    response.setContentType("text/html; charset=UTF-8");
    ReportTrialBalanceData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strTreeAccount = ReportTrialBalanceData.treeAccount(this, vars.getClient());
    boolean strIsSubAccount = false;

    String strAccountFromValue = ReportTrialBalanceData.selectAccountValue(this,
        strcElementValueFrom);
    String strAccountToValue = ReportTrialBalanceData.selectAccountValue(this, strcElementValueTo);

    log4j.debug("Output: PDF report");
    log4j.debug("strTreeOrg: " + strTreeOrg + "strOrgFamily: " + strOrgFamily + "strTreeAccount: "
        + strTreeAccount);
    log4j.debug("strcBpartnerId: " + strcBpartnerId + "strmProductId: " + strmProductId
        + "strcProjectId: " + strcProjectId);

    if (!strDateFrom.equals("") && !strDateTo.equals("") && !strOrg.equals("")
        && !strcAcctSchemaId.equals("")) {

      if (strLevel.equals("S")) {
        data = ReportTrialBalanceData.selectAccountLines(this, strGroupBy, vars.getLanguage(),
            strLevel, strOrgFamily, Utility.getContext(this, vars, "#User_Client",
                "ReportTrialBalance"), Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportTrialBalance"), strAccountFromValue, strAccountToValue, strDateFrom, null,
            strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId, (strNotInitialBalance
                .equals("Y") ? "O" : "P"), DateTimeData.nDaysAfter(this, strDateTo, "1"));
        if (!strGroupBy.equals(""))
          strIsSubAccount = true;

      } else {
        data = getDataWhenNotSubAccount(vars, strDateFrom, strDateTo, strOrg, strOrgFamily,
            strcAcctSchemaId, strLevel, strTreeAccount, strNotInitialBalance);
      }

      if (data == null || data.length == 0) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      } else {

        String strLanguage = vars.getLanguage();
        String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportTrialBalancePDF.jrxml";
        HashMap<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("TOTAL", Utility.messageBD(this, "Total", strLanguage));
        StringBuilder strSubTitle = new StringBuilder();
        strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
            + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + "\n");
        strSubTitle.append(ReportTrialBalanceData.selectCompany(this, vars.getClient()) + " - ");
        strSubTitle.append(ReportTrialBalanceData.selectOrgName(this, strOrg));
        parameters.put("REPORT_SUBTITLE", strSubTitle.toString());

        parameters.put("DEFAULTVIEW", !strIsSubAccount);
        parameters.put("SUBACCOUNTVIEW", strIsSubAccount);
        parameters.put("DUMMY", true);
        parameters.put("PageNo", strPageNo);

        renderJR(vars, response, strReportName, "pdf", parameters, data, null);
      }

    } else {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    }

  }

  private ReportTrialBalanceData[] getDataWhenNotSubAccount(VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strOrgFamily,
      String strcAcctSchemaId, String strLevel, String strTreeAccount, String strNotInitialBalance)
      throws IOException, ServletException {
    ReportTrialBalanceData[] data = null;
    ReportTrialBalanceData[] dataAux = null;
    dataAux = ReportTrialBalanceData.select(this, strDateFrom, strDateTo, strOrg, strTreeAccount,
        strcAcctSchemaId, strNotInitialBalance.equals("Y") ? "O" : "P", strOrgFamily,
        Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"), strDateFrom,
        DateTimeData.nDaysAfter(this, strDateTo, "1"), "", "");
    ReportTrialBalanceData[] dataInitialBalance = ReportTrialBalanceData.selectInitialBalance(this,
        strDateFrom, strcAcctSchemaId, "", "", "", strOrgFamily,
        Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"), 
	strNotInitialBalance.equals("Y") ? "initial" : "notinitial", 
	strNotInitialBalance.equals("Y") ? "initial" : "notinitial");

    log4j.debug("Calculating tree...");
    dataAux = calculateTree(dataAux, null, new Vector<Object>(), dataInitialBalance,
        strNotInitialBalance);
    dataAux = levelFilter(dataAux, null, false, strLevel);
    dataAux = dataFilter(dataAux);

    log4j.debug("Tree calculated");

    if (dataAux != null && dataAux.length > 0) {
      data = filterTree(dataAux, strLevel);
      Arrays.sort(data, new ReportTrialBalanceDataComparator());
      for (int i = 0; i < data.length; i++) {
        data[i].rownum = "" + i;
      }
    } else {
      data = dataAux;
    }
    return data;

  }

  private ReportTrialBalanceData[] filterTree(ReportTrialBalanceData[] data, String strLevel) {
    ArrayList<Object> arrayList = new ArrayList<Object>();
    for (int i = 0; data != null && i < data.length; i++) {
      if (data[i].elementlevel.equals(strLevel))
        arrayList.add(data[i]);
    }
    ReportTrialBalanceData[] new_data = new ReportTrialBalanceData[arrayList.size()];
    arrayList.toArray(new_data);
    return new_data;
  }

  private ReportTrialBalanceData[] calculateTree(ReportTrialBalanceData[] data, String indice,
      Vector<Object> vecTotal, ReportTrialBalanceData[] dataIB, String strNotInitialBalance) {
    if (data == null || data.length == 0)
      return data;
    if (indice == null)
      indice = "0";
    ReportTrialBalanceData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    // if (log4j.isDebugEnabled())
    // log4j.debug("ReportTrialBalanceData.calculateTree() - data: " +
    // data.length);
    if (vecTotal == null)
      vecTotal = new Vector<Object>();
    if (vecTotal.size() == 0) {
      vecTotal.addElement("0");
      vecTotal.addElement("0");
      vecTotal.addElement("0");
      vecTotal.addElement("0");
    }
    BigDecimal totalDR = new BigDecimal((String) vecTotal.elementAt(0));
    BigDecimal totalCR = new BigDecimal((String) vecTotal.elementAt(1));
    BigDecimal totalInicial = new BigDecimal((String) vecTotal.elementAt(2));
    BigDecimal totalFinal = new BigDecimal((String) vecTotal.elementAt(3));
    boolean encontrado = false;
    for (int i = 0; i < data.length; i++) {
      if (data[i].parentId.equals(indice)) {
        encontrado = true;
        Vector<Object> vecParcial = new Vector<Object>();
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        ReportTrialBalanceData[] dataChilds = calculateTree(data, data[i].id, vecParcial, dataIB,
            strNotInitialBalance);
        BigDecimal parcialDR = new BigDecimal((String) vecParcial.elementAt(0));
        BigDecimal parcialCR = new BigDecimal((String) vecParcial.elementAt(1));
        BigDecimal parcialInicial = new BigDecimal((String) vecParcial.elementAt(2));
        BigDecimal parcialFinal = new BigDecimal((String) vecParcial.elementAt(3));
        data[i].amtacctdr = (new BigDecimal(data[i].amtacctdr).add(parcialDR)).toPlainString();
        data[i].amtacctcr = (new BigDecimal(data[i].amtacctcr).add(parcialCR)).toPlainString();
        data[i].saldoInicial = (new BigDecimal(data[i].saldoInicial).add(parcialInicial))
            .toPlainString();
        // Edit how the final balance is calculated
        data[i].saldoFinal = (new BigDecimal(data[i].saldoInicial).add(parcialDR)
            .subtract(parcialCR)).toPlainString();

        // Set calculated Initial Balances
        for (int k = 0; k < dataIB.length; k++) {
          if (dataIB[k].accountId.equals(data[i].id)) {
            if (strNotInitialBalance.equals("Y")) {
              data[i].saldoInicial = (new BigDecimal(dataIB[k].saldoInicial).add(parcialInicial))
                  .toPlainString();
            } else {
              data[i].amtacctdr = (new BigDecimal(dataIB[k].amtacctdr).add(parcialDR))
                  .toPlainString();
              data[i].amtacctcr = (new BigDecimal(dataIB[k].amtacctcr).add(parcialCR))
                  .toPlainString();
            }
            data[i].saldoFinal = (new BigDecimal(dataIB[k].saldoInicial).add(parcialDR)
                .subtract(parcialCR)).toPlainString();
          }
        }

        totalDR = totalDR.add(new BigDecimal(data[i].amtacctdr));
        totalCR = totalCR.add(new BigDecimal(data[i].amtacctcr));
        totalInicial = totalInicial.add(new BigDecimal(data[i].saldoInicial));
        totalFinal = totalFinal.add(new BigDecimal(data[i].saldoFinal));

        vec.addElement(data[i]);
        if (dataChilds != null && dataChilds.length > 0) {
          for (int j = 0; j < dataChilds.length; j++)
            vec.addElement(dataChilds[j]);
        }
      } else if (encontrado)
        break;
    }
    vecTotal.set(0, totalDR.toPlainString());
    vecTotal.set(1, totalCR.toPlainString());
    vecTotal.set(2, totalInicial.toPlainString());
    vecTotal.set(3, totalFinal.toPlainString());
    result = new ReportTrialBalanceData[vec.size()];
    vec.copyInto(result);
    return result;
  }

  /**
   * Filters positions with amount credit, amount debit, initial balance and final balance distinct
   * to zero.
   * 
   * @param data
   * @return ReportTrialBalanceData array filtered.
   */
  private ReportTrialBalanceData[] dataFilter(ReportTrialBalanceData[] data) {
    if (data == null || data.length == 0)
      return data;
    Vector<Object> dataFiltered = new Vector<Object>();
    for (int i = 0; i < data.length; i++) {
      if (new BigDecimal(data[i].amtacctdr).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].amtacctcr).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].saldoInicial).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].saldoFinal).compareTo(BigDecimal.ZERO) != 0) {
        dataFiltered.addElement(data[i]);
      }
    }
    ReportTrialBalanceData[] result = new ReportTrialBalanceData[dataFiltered.size()];
    dataFiltered.copyInto(result);
    return result;
  }

  private ReportTrialBalanceData[] levelFilter(ReportTrialBalanceData[] data, String indice,
      boolean found, String strLevel) {
    if (data == null || data.length == 0 || strLevel == null || strLevel.equals(""))
      return data;
    ReportTrialBalanceData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    // if (log4j.isDebugEnabled())
    // log4j.debug("ReportTrialBalanceData.levelFilter() - data: " +
    // data.length);

    if (indice == null)
      indice = "0";
    for (int i = 0; i < data.length; i++) {
      if (data[i].parentId.equals(indice)
          && (!found || data[i].elementlevel.equalsIgnoreCase(strLevel))) {
        ReportTrialBalanceData[] dataChilds = levelFilter(data, data[i].id,
            (found || data[i].elementlevel.equals(strLevel)), strLevel);
        vec.addElement(data[i]);
        if (dataChilds != null && dataChilds.length > 0)
          for (int j = 0; j < dataChilds.length; j++)
            vec.addElement(dataChilds[j]);
      }
    }
    result = new ReportTrialBalanceData[vec.size()];
    vec.copyInto(result);
    vec.clear();
    return result;
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
  }

  public String getServletInfo() {
    return "Servlet ReportTrialBalance. This Servlet was made by Eduardo Argal and mirurita";
  }
}
