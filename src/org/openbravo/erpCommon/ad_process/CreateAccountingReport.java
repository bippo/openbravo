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
package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class CreateAccountingReport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String process = CreateAccountingReportData.processId(this, "UserDefinedAccountingReport");
    // TODO: Remove it if we are not using it
    /*
     * String strTabId = vars.getGlobalVariable("inpTabId", "CreateAccountingReport|tabId"); String
     * strWindowId = vars.getGlobalVariable("inpwindowId", "CreateAccountingReport|windowId");
     */
    // String strDeleteOld = vars.getStringParameter("inpDeleteOld", "Y");
    // String strCElementId = vars.getStringParameter("inpElementId", "");
    // String strUpdateDefault = vars.getStringParameter("inpUpdateDefault",
    // "Y");
    // String strCreateNewCombination =
    // vars.getStringParameter("inpCreateNewCombination", "Y");
    if (vars.commandIn("DEFAULT")) {
      // printPage(response, vars, process, strWindowId, strTabId,
      // strDeleteOld, strCElementId, strUpdateDefault,
      // strCreateNewCombination);
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "CreateAccountingReport|cAcctSchemaId", "");
      String strAccountingReportId = vars.getGlobalVariable("inpAccountingReportId",
          "CreateAccountingReport|accountingReport", "");
      String strOrg = vars.getGlobalVariable("inpadOrgId", "CreateAccountingReport|orgId", "0");
      String strPeriod = vars.getGlobalVariable("inpPeriodId", "CreateAccountingReport|period", "");
      String strYear = vars.getGlobalVariable("inpYearId", "CreateAccountingReport|year", "");
      printPage(response, vars, strcAcctSchemaId, strAccountingReportId, strOrg, strPeriod,
          strYear, process);
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "CreateAccountingReport|cAcctSchemaId");
      String strAccountingReportId = vars.getRequestGlobalVariable("inpAccountingReportId",
          "CreateAccountingReport|accountingReport");
      String strOrg = vars.getGlobalVariable("inpadOrgId", "CreateAccountingReport|orgId", "0");
      String strPeriod = vars.getRequestGlobalVariable("inpPeriodId",
          "CreateAccountingReport|period");
      String strYear = vars.getRequestGlobalVariable("inpYearId", "CreateAccountingReport|year");
      printPagePopUp(response, vars, strcAcctSchemaId, strAccountingReportId, strOrg, strPeriod,
          strYear);
      // printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strcAcctSchemaId, String strAccountingReportId, String strOrg, String strPeriod,
      String strYear, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process CreateAccountingReport");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/CreateAccountingReport").createXmlDocument();

    String strArray = arrayEntry(vars, strcAcctSchemaId);

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("accounting", strAccountingReportId);
    xmlDocument.setParameter("org", strOrg);
    xmlDocument.setParameter("period", strPeriod);
    xmlDocument.setParameter("year", strYear);
    xmlDocument.setParameter("array", strArray);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"),
          Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateAccountingReport", "");
      xmlDocument.setData("reportAD_ORG", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportAD_ACCOUNTINGRPT_ELEMENT", "liststructure",
        CreateAccountingReportData.selectAD_Accountingrpt_Element_ID(this,
            Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"),
            Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"),
            strcAcctSchemaId, ""));
    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure", AccountingSchemaMiscData
        .selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "CreateAccountingReport"),
            Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"),
            strcAcctSchemaId));

    xmlDocument.setParameter(
        "accountArray",
        Utility.arrayDobleEntrada(
            "arrAccount",
            CreateAccountingReportData.selectAD_Accountingrpt_Element_Double_ID(this,
                Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"),
                Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"), "")));

    // xmlDocument.setData("reportPeriod", "liststructure",
    // CreateAccountingReportData.selectPeriod(this, vars.getLanguage(),
    // Utility.getContext(this, vars, "#User_Org",
    // "CreateAccountingReport"), Utility.getContext(this, vars,
    // "#User_Client", "CreateAccountingReport"), "800074"));
    xmlDocument.setData(
        "reportPeriod",
        "liststructure",
        CreateAccountingReportData.selectCombo(this,
            Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"),
            Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"),
            vars.getLanguage()));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CreateAccountingReport", false, "",
        "", "", false, "ad_process", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    // New interface paramenters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_process.CreateAccountingReport");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "CreateAccountingReport.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CreateAccountingReport.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CreateAccountingReport");
      vars.removeMessage("CreateAccountingReport");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    // //----

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePopUp(HttpServletResponse response, VariablesSecureApp vars,
      String strcAcctSchemaId, String strAccountingReportId, String strOrg, String strPeriod,
      String strYear) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pop up CreateAccountingReport");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/CreateAccountingReportPopUp").createXmlDocument();
    String strPeriodFrom = "";
    int level = 0;
    String strPeriodTo = "";
    // String strYear = DateTimeData.sysdateYear(this);
    // log4j.debug("****************************strAccountingReportId: "+strAccountingReportId);
    String strAccountingType = CreateAccountingReportData.selectType(this, strAccountingReportId);
    // log4j.debug("****************************strAccountingType: "+strAccountingType);
    if (strAccountingType.equals("Q")) {
      String strAux = CreateAccountingReportData.selectMax(this, strPeriod);
      // log4j.debug("*************************strAux: "+strAux);
      strPeriodFrom = "01/" + CreateAccountingReportData.selectMin(this, strPeriod) + "/" + strYear;
      // log4j.debug("*************************strPeriodFrom: "+strPeriodFrom);
      strPeriodTo = CreateAccountingReportData.lastDay(this, "01/" + strAux + "/" + strYear,
          vars.getSqlDateFormat());
      strPeriodTo = DateTimeData.nDaysAfter(this, strPeriodTo, "1");
      // log4j.debug("*************************strPeriodTo: "+strPeriodTo);
    } else if (strAccountingType.equals("M")) {
      strPeriodFrom = "01/" + strPeriod + "/" + strYear;
      // log4j.debug("*************************strPeriodFrom1: "+strPeriodFrom);
      strPeriodTo = CreateAccountingReportData
          .lastDay(this, strPeriodFrom, vars.getSqlDateFormat());
      strPeriodTo = DateTimeData.nDaysAfter(this, strPeriodTo, "1");
      // log4j.debug("*************************strPeriodTo1: "+strPeriodTo);
    } else {
      strPeriodFrom = "01/01/" + strPeriod;
      // log4j.debug("*************************strPeriodFrom2: "+strPeriodFrom);
      // TODO: Does this work with MM/DD/YY o YY/MM/DD locales?
      strPeriodTo = DateTimeData.nDaysAfter(this, "31/12/" + strPeriod, "1");
    }
    strPeriodFrom = CreateAccountingReportData.selectFormat(this, strPeriodFrom,
        vars.getSqlDateFormat());
    strPeriodTo = CreateAccountingReportData.selectFormat(this, strPeriodTo,
        vars.getSqlDateFormat());
    StringBuilder strTreeOrg = new StringBuilder(strOrg);
    treeOrg(vars, strOrg, strTreeOrg);

    Vector<Object> vectorArray = new Vector<Object>();

    childData(vars, vectorArray, strcAcctSchemaId, strAccountingReportId, strPeriodFrom,
        strPeriodTo, strTreeOrg.toString(), level, "0", strPeriod);

    CreateAccountingReportData[] dataTree = convertVector(vectorArray);
    dataTree = filterData(dataTree);

    xmlDocument.setParameter("title", dataTree[0].name);
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setData("structure", dataTree);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String arrayEntry(VariablesSecureApp vars, String strcAcctSchemaId)
      throws ServletException {
    String result = "";
    CreateAccountingReportData[] data = CreateAccountingReportData
        .selectAD_Accountingrpt_Element_ID(this,
            Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"),
            Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"),
            strcAcctSchemaId, "");
    if (data == null || data.length == 0) {
      result = "var array = null;";
    } else {
      result = "var array = new Array(\n";
      for (int i = 0; i < data.length; i++) {
        result += "new Array(\"" + data[i].id + "\",\"" + data[i].filteredbyorganization + "\",\""
            + data[i].temporaryfiltertype + "\")";
        if (i < data.length - 1)
          result += ",\n";
      }
      result += ");";
      CreateAccountingReportData[] dataPeriod = CreateAccountingReportData.selectCombo(this,
          Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"),
          Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"),
          vars.getLanguage());
      if (dataPeriod == null || dataPeriod.length == 0) {
        result += "\nvar combo = null;";
      } else {
        result += "\nvar combo = new Array(\n";
        for (int j = 0; j < dataPeriod.length; j++) {
          result += "new Array(\"" + dataPeriod[j].value + "\", \"" + dataPeriod[j].id + "\", \""
              + dataPeriod[j].name + "\")";
          if (j < dataPeriod.length - 1)
            result += ",\n";
        }
        result += ");";
      }

    }
    return result;
  }

  private void treeOrg(VariablesSecureApp vars, String strOrg, StringBuilder treeOrg)
      throws ServletException {
    CreateAccountingReportData[] dataOrg = CreateAccountingReportData.selectOrg(this, strOrg,
        vars.getClient());
    for (int i = 0; i < dataOrg.length; i++) {
      treeOrg.append(",");
      treeOrg.append(dataOrg[i].id);
      if (dataOrg[i].issummary.equals("Y"))
        treeOrg(vars, dataOrg[i].id, treeOrg);
    }
    return;
  }

  private void childData(VariablesSecureApp vars, Vector<Object> vectorArray,
      String strcAcctSchemaId, String strAccountingReportId, String strPeriodFrom,
      String strPeriodTo, String strOrg, int level, String strParent, String strPeriod)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("**********************strAccountingReportId: " + strAccountingReportId);
    if (log4j.isDebugEnabled())
      log4j.debug("**********************strPeriodFrom: " + strPeriodFrom);
    if (log4j.isDebugEnabled())
      log4j.debug("**********************strPeriodTo: " + strPeriodTo);
    if (log4j.isDebugEnabled())
      log4j.debug("**********************strOrg: " + strOrg);
    if (log4j.isDebugEnabled())
      log4j.debug("**********************level: " + String.valueOf(level));
    if (log4j.isDebugEnabled())
      log4j.debug("**********************User_Client: "
          + Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"));
    if (log4j.isDebugEnabled())
      log4j.debug("**********************#User_Org: "
          + Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"));
    if (log4j.isDebugEnabled())
      log4j.debug("Ouput: child tree data");
    String strAccountId = CreateAccountingReportData.selectAccounting(this, strAccountingReportId);
    if (log4j.isDebugEnabled())
      log4j.debug("**********************strAccountId: " + strAccountId);

    String initialBalance = CreateAccountingReportData
        .isInitialBalance(this, strAccountingReportId);
    if (initialBalance.equals(""))
      initialBalance = "N";
    String dateInitialYear = CreateAccountingReportData.dateInitialYear(this,
        Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"),
        Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"), strPeriod);
    dateInitialYear = CreateAccountingReportData.selectFormat(this, dateInitialYear,
        vars.getSqlDateFormat());
    CreateAccountingReportData[] data;
    if (initialBalance.equals("Y")) {
      data = CreateAccountingReportData.selectInitial(this, strParent, String.valueOf(level),
          Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"),
          Utility.stringList(strOrg), dateInitialYear,
          DateTimeData.nDaysAfter(this, dateInitialYear, "1"), strAccountId, strcAcctSchemaId,
          strAccountingReportId);
    } else {
      data = CreateAccountingReportData.select(this, strParent, String.valueOf(level),
          Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"),
          Utility.stringList(strOrg), strPeriodFrom, strPeriodTo, strAccountId, strcAcctSchemaId,
          strAccountingReportId);
    }
    if (data == null || data.length == 0)
      data = CreateAccountingReportData.set();
    vectorArray.addElement(data[0]);
    if (log4j.isDebugEnabled())
      log4j.debug("**********************data[0]*********************: " + data[0].name + "  "
          + data[0].total);
    CreateAccountingReportData[] dataAux = CreateAccountingReportData.selectChild(this,
        Utility.getContext(this, vars, "#User_Client", "CreateAccountingReport"),
        Utility.getContext(this, vars, "#User_Org", "CreateAccountingReport"), data[0].id,
        strcAcctSchemaId);
    for (int i = 0; i < dataAux.length; i++) {
      childData(vars, vectorArray, strcAcctSchemaId, dataAux[i].id, strPeriodFrom, strPeriodTo,
          strOrg, level + 1, data[0].id, strPeriod);
    }
  }

  private CreateAccountingReportData[] convertVector(Vector<Object> vectorArray)
      throws ServletException {
    CreateAccountingReportData[] data = new CreateAccountingReportData[vectorArray.size()];
    BigDecimal count = BigDecimal.ZERO;
    for (int i = 0; i < vectorArray.size(); i++) {
      data[i] = (CreateAccountingReportData) vectorArray.elementAt(i);
    }
    for (int i = data.length - 1; i >= 0; i--) {
      if (data[i].issummary.equals("Y")) {
        for (int j = i + 1; j < data.length; j++) {
          if (Integer.valueOf(data[j].levelAccount).intValue() > Integer.valueOf(
              data[i].levelAccount).intValue()
              && data[j].parent.equals(data[i].id)) {
            String total = data[j].total;
            count = count.add(new BigDecimal(total));
          }
        }
        data[i].total = String.valueOf(count);
        count = BigDecimal.ZERO;
      }
    }
    return data;
  }

  private CreateAccountingReportData[] filterData(CreateAccountingReportData[] data)
      throws ServletException {
    ArrayList<Object> new_a = new ArrayList<Object>();
    for (int i = 0; i < data.length; i++) {
      if (data[i].isshown.equals("Y"))
        new_a.add(data[i]);
    }
    CreateAccountingReportData[] newData = new CreateAccountingReportData[new_a.size()];
    new_a.toArray(newData);
    return newData;
  }

  public String getServletInfo() {
    return "Servlet CreateAccountingReport";
  } // end of getServletInfo() method
}
