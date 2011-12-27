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
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportCashFlow extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  public static String strTreeOrg = "";

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String process = ReportCashFlowData.processId(this, "CashFlowStatement");
    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReportCashFlow|cAcctSchemaId", "");
      String strAccountingReportId = vars.getGlobalVariable("inpAccountingReportId",
          "ReportCashFlow|accountingReport", "");
      String strOrg = vars.getGlobalVariable("inpadOrgId", "ReportCashFlow|orgId", "0");
      String strPeriod = vars.getGlobalVariable("inpPeriodId", "ReportCashFlow|period", "");
      printPage(response, vars, strcAcctSchemaId, strAccountingReportId, strOrg, strPeriod, process);
    } else if (vars.commandIn("DEPURAR")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportCashFlow|cAcctSchemaId");
      String strAccountingReportId = vars.getRequestGlobalVariable("inpAccountingReportId",
          "ReportCashFlow|accountingReport");
      String strOrg = vars.getGlobalVariable("inpadOrgId", "ReportCashFlow|orgId", "0");
      String strPeriod = vars.getRequestGlobalVariable("inpPeriodId", "ReportCashFlow|period");
      printPageDepurar(response, vars, strcAcctSchemaId, strAccountingReportId, strOrg, strPeriod,
          process);
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportCashFlow|cAcctSchemaId");
      String strAccountingReportId = vars.getRequestGlobalVariable("inpAccountingReportId",
          "ReportCashFlow|accountingReport");
      String strOrg = vars.getGlobalVariable("inpadOrgId", "ReportCashFlow|orgId", "0");
      String strPeriod = vars.getRequestGlobalVariable("inpPeriodId", "ReportCashFlow|period");
      printPagePopUp(response, vars, strcAcctSchemaId, strAccountingReportId, strOrg, strPeriod,
          process);
    } else
      pageErrorPopUp(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strcAcctSchemaId, String strAccountingReportId, String strOrg, String strPeriod,
      String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: printPage ReportCashFlow");

    ActionButtonDefaultData[] data = null;
    String strHelp = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strHelp = data[0].help;
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportCashFlow").createXmlDocument();

    String strArray = arrayEntry(vars, strcAcctSchemaId);

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setParameter("accounting", strAccountingReportId);
    xmlDocument.setParameter("org", strOrg);
    xmlDocument.setParameter("period", strPeriod);
    xmlDocument.setParameter("array", strArray);

    xmlDocument.setParameter(
        "accounArray",
        Utility.arrayDobleEntrada(
            "arrAccount",
            ReportCashFlowData.selectAD_Accountingrpt_Element_ID_Double(this,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"),
                Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"), "")));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "AD_OrgType_BU_LE",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"),
          Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportCashFlow", "");
      xmlDocument.setData("reportAD_ORG", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument
        .setData("reportAD_ACCOUNTINGRPT_ELEMENT", "liststructure", ReportCashFlowData
            .selectAD_Accountingrpt_Element_ID(this,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"),
                Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"), "",
                strcAcctSchemaId));

    xmlDocument.setData(
        "reportPeriod",
        "liststructure",
        ReportCashFlowData.selectCombo(this, vars.getLanguage(),
            Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow")));

    xmlDocument.setData(
        "reportC_ACCTSCHEMA_ID",
        "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"),
            Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"), strcAcctSchemaId));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportCashFlow", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    // New interface paramenters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportCashFlow");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportCashFlow.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportCashFlow.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportCashFlow");
      vars.removeMessage("ReportCashFlow");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePopUp(HttpServletResponse response, VariablesSecureApp vars,
      String strcAcctSchemaId, String strAccountingReportId, String strOrg, String strPeriod,
      String process) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pop up ReportCashFlow");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportCashFlowPopUp").createXmlDocument();
    String strPeriodFrom = "";
    int level = 0;
    String strPeriodTo = "";
    String strAccountingType = ReportCashFlowData.selectType(this, strAccountingReportId);
    if (strAccountingType.equals("A")) {
      ReportCashFlowData[] data = ReportCashFlowData.startEndYear(this, strPeriod);
      strPeriodFrom = data[0].startdate;
      strPeriodTo = data[0].enddate;
    } else if (strAccountingType.equals("M")) {
      ReportCashFlowData[] data = ReportCashFlowData.startEndMonth(this, strPeriod);
      strPeriodFrom = data[0].startdate;
      strPeriodTo = data[0].enddate;
    } else {
    }
    strPeriodFrom = ReportCashFlowData.selectFormat(this, strPeriodFrom, vars.getSqlDateFormat());
    strPeriodTo = ReportCashFlowData.selectFormat(this, strPeriodTo, vars.getSqlDateFormat());
    strTreeOrg = "'" + strOrg + "'";
    treeOrg(vars, strOrg);

    Vector<Object> vectorArray = new Vector<Object>();

    childData(vars, vectorArray, strcAcctSchemaId, strAccountingReportId, strPeriodFrom,
        strPeriodTo, strTreeOrg, level, "0");

    ReportCashFlowData[] dataTree = convertVector(vectorArray);
    dataTree = filterData(dataTree);
    strTreeOrg = "";
    if (dataTree == null || dataTree.length == 0)
      dataTree = ReportCashFlowData.set();
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

  private void printPageDepurar(HttpServletResponse response, VariablesSecureApp vars,
      String strcAcctSchemaId, String strAccountingReportId, String strOrg, String strPeriod,
      String process) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ReportCashFlowReload");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportCashFlowReload").createXmlDocument();
    String strPeriodFrom = "";
    String strPeriodTo = "";
    String strAccountingType = ReportCashFlowData.selectType(this, strAccountingReportId);
    if (strAccountingType.equals("A")) {
      ReportCashFlowData[] data = ReportCashFlowData.startEndYear(this, strPeriod);
      strPeriodFrom = data[0].startdate;
      strPeriodTo = data[0].enddate;
    } else if (strAccountingType.equals("M")) {
      ReportCashFlowData[] data = ReportCashFlowData.startEndMonth(this, strPeriod);
      strPeriodFrom = data[0].startdate;
      strPeriodTo = data[0].enddate;
    } else {
    }
    strPeriodFrom = ReportCashFlowData.selectFormat(this, strPeriodFrom, vars.getSqlDateFormat());
    strPeriodTo = ReportCashFlowData.selectFormat(this, strPeriodTo, vars.getSqlDateFormat());
    strTreeOrg = strOrg;
    treeOrg(vars, strOrg);

    ReportCashFlowData[] data = ReportCashFlowData.selectMissingEntries(this,
        Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"), strcAcctSchemaId,
        strPeriodFrom, strPeriodTo, vars.getClient());
    if (log4j.isDebugEnabled())
      log4j.debug("printPageDepurar - data.length: " + data.length);
    if (log4j.isDebugEnabled())
      log4j.debug("printPageDepurar - #User_Client: "
          + Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"));
    if (log4j.isDebugEnabled())
      log4j.debug("printPageDepurar - #AccessibleOrgTree: "
          + Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"));
    if (log4j.isDebugEnabled())
      log4j.debug("printPageDepurar - strPeriodFrom: " + strPeriodFrom);
    if (log4j.isDebugEnabled())
      log4j.debug("printPageDepurar - strPeriodTo: " + strPeriodTo);
    if (log4j.isDebugEnabled())
      log4j.debug("printPageDepurar - vars.getClient(): " + vars.getClient());
    if (log4j.isDebugEnabled())
      log4j.debug("printPageDepurar - bol: "
          + ((data != null && data.length > 0) ? "true" : "false"));
    if (data != null && data.length > 0) {
      OBError myError = new OBError();
      myError.setTitle("");
      myError.setType("Error");
      myError.setMessage(Utility.messageBD(this, "MissingCashFlowStatements", vars.getLanguage()));
      vars.setMessage("ReportCashFlow", myError);
    }
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    if (log4j.isDebugEnabled())
      log4j.debug("language");
    xmlDocument.setParameter("org", strOrg);
    if (log4j.isDebugEnabled())
      log4j.debug("org");
    xmlDocument.setParameter("bol", (data != null && data.length > 0) ? "true" : "false");
    if (log4j.isDebugEnabled())
      log4j.debug("bol");
    xmlDocument.setParameter("period", strPeriod);
    if (log4j.isDebugEnabled())
      log4j.debug("period");
    xmlDocument.setParameter("report", strAccountingReportId);
    if (log4j.isDebugEnabled())
      log4j.debug("report");
    xmlDocument.setParameter("acctschema", strcAcctSchemaId);
    if (log4j.isDebugEnabled())
      log4j.debug("acctschema");
    response.setContentType("text/html; charset=UTF-8");
    if (log4j.isDebugEnabled())
      log4j.debug("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug("PrintWriter");
    out.println(xmlDocument.print());
    if (log4j.isDebugEnabled())
      log4j.debug("print");
    out.close();
  }

  private String arrayEntry(VariablesSecureApp vars, String strcAcctSchemaId)
      throws ServletException {
    String result = "";
    ReportCashFlowData[] data = ReportCashFlowData.selectAD_Accountingrpt_Element_ID(this,
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"),
        Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"), "", strcAcctSchemaId);
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
      ReportCashFlowData[] dataPeriod = ReportCashFlowData.selectCombo(this, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"));
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

  private void treeOrg(VariablesSecureApp vars, String strOrg) throws ServletException {
    ReportCashFlowData[] dataOrg = ReportCashFlowData.selectOrg(this, strOrg, vars.getClient());
    for (int i = 0; i < dataOrg.length; i++) {
      strTreeOrg += ",'" + dataOrg[i].id + "'";
      if (dataOrg[i].issummary.equals("Y"))
        treeOrg(vars, dataOrg[i].id);
    }
    return;
  }

  private void childData(VariablesSecureApp vars, Vector<Object> vectorArray,
      String strcAcctSchemaId, String strAccountingReportId, String strPeriodFrom,
      String strPeriodTo, String strOrg, int level, String strParent) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Ouput: child tree data");
    String strAccountId = ReportCashFlowData.selectAccounting(this, strAccountingReportId);
    ReportCashFlowData[] data = ReportCashFlowData.select(this, strParent, String.valueOf(level),
        Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"),
        Utility.stringList(strOrg), strPeriodFrom, strPeriodTo, strAccountId,
        strAccountingReportId, strcAcctSchemaId);
    if (data == null || data.length == 0)
      return;
    vectorArray.addElement(data[0]);
    ReportCashFlowData[] dataAux = ReportCashFlowData.selectChild(this,
        Utility.getContext(this, vars, "#User_Client", "ReportCashFlow"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportCashFlow"), data[0].id,
        ReportCashFlowData.selectTree(this, vars.getClient()));
    for (int i = 0; i < dataAux.length; i++) {
      childData(vars, vectorArray, strcAcctSchemaId, dataAux[i].id, strPeriodFrom, strPeriodTo,
          strOrg, level + 1, data[0].id);
    }
  }

  private ReportCashFlowData[] convertVector(Vector<Object> vectorArray) throws ServletException {
    ReportCashFlowData[] data = new ReportCashFlowData[vectorArray.size()];
    BigDecimal count = BigDecimal.ZERO;
    for (int i = 0; i < vectorArray.size(); i++) {
      data[i] = (ReportCashFlowData) vectorArray.elementAt(i);
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

  private ReportCashFlowData[] filterData(ReportCashFlowData[] data) throws ServletException {
    ArrayList<Object> new_a = new ArrayList<Object>();
    for (int i = 0; i < data.length; i++) {
      if (data[i].isshown.equals("Y"))
        new_a.add(data[i]);
    }
    ReportCashFlowData[] newData = new ReportCashFlowData[new_a.size()];
    new_a.toArray(newData);
    return newData;
  }

  public String getServletInfo() {
    return "Servlet ReportCashFlow";
  } // end of getServletInfo() method
}
