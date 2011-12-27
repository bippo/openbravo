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
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.AccountTree;
import org.openbravo.erpCommon.businessUtility.AccountTreeData;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.WindowTreeData;
import org.openbravo.xmlEngine.XmlDocument;

public class GeneralAccountingReports extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "GeneralAccountingReports|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "GeneralAccountingReports|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef",
          "GeneralAccountingReports|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef",
          "GeneralAccountingReports|dateToRef", "");
      String strAsDateTo = vars.getGlobalVariable("inpAsDateTo",
          "GeneralAccountingReports|asDateTo", "");
      String strAsDateToRef = vars.getGlobalVariable("inpAsDateToRef",
          "GeneralAccountingReports|asDateToRef", "");
      String strPageNo = vars
          .getGlobalVariable("inpPageNo", "GeneralAccountingReports|PageNo", "1");
      String strElementValue = vars.getGlobalVariable("inpcElementvalueId",
          "GeneralAccountingReports|C_ElementValue_ID", "");
      String strConImporte = vars.getGlobalVariable("inpConImporte",
          "GeneralAccountingReports|conImporte", "N");
      String strConCodigo = vars.getGlobalVariable("inpConCodigo",
          "GeneralAccountingReports|conCodigo", "N");
      String strLevel = vars.getGlobalVariable("inpLevel", "GeneralAccountingReports|level", "");
      printPageDataSheet(response, vars, "", "", strDateFrom, strDateTo, strPageNo, strDateFromRef,
          strDateToRef, strAsDateTo, strAsDateToRef, strElementValue, strConImporte, "", strLevel,
          strConCodigo, "");
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getStringParameter("inpcAcctSchemaId", "");
      String strAgno = vars.getRequiredGlobalVariable("inpAgno", "GeneralAccountingReports|agno");
      String strAgnoRef = vars.getRequiredGlobalVariable("inpAgnoRef",
          "GeneralAccountingReports|agnoRef");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "GeneralAccountingReports|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "GeneralAccountingReports|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "GeneralAccountingReports|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "GeneralAccountingReports|dateToRef");
      String strPageNo = vars.getRequestGlobalVariable("inpPageNo",
          "GeneralAccountingReports|PageNo");
      String strAsDateTo = vars.getRequestGlobalVariable("inpAsDateTo",
          "GeneralAccountingReports|asDateTo");
      String strAsDateToRef = vars.getRequestGlobalVariable("inpAsDateToRef",
          "GeneralAccountingReports|asDateToRef");
      String strElementValue = vars.getRequiredGlobalVariable("inpcElementvalueId",
          "GeneralAccountingReports|C_ElementValue_ID");
      String strConImporte = vars.getRequestGlobalVariable("inpConImporte",
          "GeneralAccountingReports|conImporte");
      String strConCodigo = vars.getRequestGlobalVariable("inpConCodigo",
          "GeneralAccountingReports|conCodigo");
      String strOrg = vars.getRequestGlobalVariable("inpOrganizacion",
          "GeneralAccountingReports|organizacion");
      String strLevel = vars.getRequestGlobalVariable("inpLevel", "GeneralAccountingReports|level");
      printPagePDF(request, response, vars, strAgno, strAgnoRef, strDateFrom, strDateTo,
          strDateFromRef, strDateToRef, strAsDateTo, strAsDateToRef, strElementValue,
          strConImporte, strOrg, strLevel, strConCodigo, strcAcctSchemaId, strPageNo);
    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAgno, String strAgnoRef, String strDateFrom,
      String strDateTo, String strDateFromRef, String strDateToRef, String strAsDateTo,
      String strAsDateToRef, String strElementValue, String strConImporte, String strOrg,
      String strLevel, String strConCodigo, String strcAcctSchemaId, String strPageNo)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/GeneralAccountingReportsPDF").createXmlDocument();

    String strCalculateOpening = strElementValue.substring(0, 1);
    strElementValue = strElementValue.substring(1, strElementValue.length());
    GeneralAccountingReportsData[] strGroups = GeneralAccountingReportsData.selectGroups(this,
        strElementValue);

    try {
      strGroups[strGroups.length - 1].pagebreak = "";

      String[][] strElementValueDes = new String[strGroups.length][];
      if (log4j.isDebugEnabled())
        log4j.debug("strElementValue:" + strElementValue + " - strGroups.length:"
            + strGroups.length);
      for (int i = 0; i < strGroups.length; i++) {
        GeneralAccountingReportsData[] strElements = GeneralAccountingReportsData.selectElements(
            this, strGroups[i].id);
        strElementValueDes[i] = new String[strElements.length];
        if (log4j.isDebugEnabled())
          log4j.debug("strElements.length:" + strElements.length);
        for (int j = 0; j < strElements.length; j++) {
          strElementValueDes[i][j] = strElements[j].id;
        }
      }

      String strTreeOrg = GeneralAccountingReportsData.treeOrg(this, vars.getClient());
      AccountTree[] acct = new AccountTree[strGroups.length];

      AccountTreeData[][] elements = new AccountTreeData[strGroups.length][];

      WindowTreeData[] dataTree = WindowTreeData.selectTreeID(this,
          Utility.stringList(vars.getClient()), "EV");
      String TreeID = "";
      if (dataTree != null && dataTree.length != 0)
        TreeID = dataTree[0].id;

      // For each year, the initial and closing date is obtained
      GeneralAccountingReportsData[] startEndYear = GeneralAccountingReportsData.startEndYear(this,
          vars.getClient(), "'" + strAgno + "'");
      GeneralAccountingReportsData[] startEndYearRef = GeneralAccountingReportsData.startEndYear(
          this, vars.getClient(), "'" + strAgnoRef + "'");
      String strYear = "'" + startEndYear[0].name + "'";
      String strYearRef = "'" + startEndYearRef[0].name + "'";
      String strYearsToClose = "";
      String strYearsToCloseRef = "";

      Vector<Object> vec = new Vector<Object>();
      // Relation of open and closed years is obtained
      GeneralAccountingReportsData[] closedYears = GeneralAccountingReportsData.checkFiscalYears(
          this, vars.getClient());
      GeneralAccountingReportsData[] previousYears = GeneralAccountingReportsData.previousYear(
          this, vars.getClient());
      if (strCalculateOpening.equals("Y")) {
        strCalculateOpening = "N";
        strDateTo = strAsDateTo;
        strDateToRef = strAsDateToRef;
        strDateFrom = "";
        strDateFromRef = "";
        fulfillYearsToClose(vec, startEndYear[0].name, closedYears, previousYears);
        // If there is some year to close, will be appended to the year to show in report when
        // calculating amounts, through strYearsToClose and strYearsToCloseRef variables
        if (vec.size() > 0) {
          for (int i = 0; i < vec.size(); i++) {
            strCalculateOpening = "Y"; // If we finally calculate the opening variable takes "Y"
            strYearsToClose = strYearsToClose + ",'" + vec.elementAt(i) + "'";
          }
        }
        vec = new Vector<Object>();
        fulfillYearsToClose(vec, startEndYearRef[0].name, closedYears, previousYears);
        // If there is some year to close
        if (vec.size() > 0) {
          for (int i = 0; i < vec.size(); i++) {
            strCalculateOpening = "Y"; // If we finally calculate the opening variable takes "Y"
            strYearsToCloseRef = strYearsToCloseRef + ",'" + vec.elementAt(i) + "'";
          }
        }
      }

      for (int i = 0; i < strGroups.length; i++) {
        // All account tree is obtained
        if (vars.getLanguage().equals("en_US")) {
          elements[i] = AccountTreeData.select(this, strConCodigo, TreeID);
        } else {
          elements[i] = AccountTreeData.selectTrl(this, strConCodigo, vars.getLanguage(), TreeID);
        }
        // For each account with movements in the year, debit and credit total amounts are
        // calculated according to fact_acct movements.
        AccountTreeData[] accounts = AccountTreeData.selectAcct(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralAccountingReports"),
            Utility.getContext(this, vars, "#User_Client", "GeneralAccountingReports"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strcAcctSchemaId,
            Tree.getMembers(this, strTreeOrg, strOrg), strYear + strYearsToClose, strDateFromRef,
            DateTimeData.nDaysAfter(this, strDateToRef, "1"), strYearRef + strYearsToCloseRef);
        {
          // Income summary amount is calculated and included in the balance sheet data
          String strIncomeSummary = GeneralAccountingReportsData.incomesummary(this,
              strcAcctSchemaId);
          if (log4j.isDebugEnabled())
            log4j.debug("*********** strIncomeSummary: " + strIncomeSummary);
          String strISyear = processIncomeSummary(strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strYear + strYearsToClose, strTreeOrg,
              strOrg, strcAcctSchemaId);
          if (log4j.isDebugEnabled())
            log4j.debug("*********** strISyear: " + strISyear);
          String strISyearRef = processIncomeSummary(strDateFromRef,
              DateTimeData.nDaysAfter(this, strDateToRef, "1"), strYearRef + strYearsToCloseRef,
              strTreeOrg, strOrg, strcAcctSchemaId);
          if (log4j.isDebugEnabled())
            log4j.debug("*********** strISyearRef: " + strISyearRef);
          accounts = appendRecords(accounts, strIncomeSummary, strISyear, strISyearRef);

        }
        // Report tree is built with given the account tree, and the amounts obtained from fact_acct
        acct[i] = new AccountTree(vars, this, elements[i], accounts, strElementValueDes[i]);
        if (acct[i] != null) {
          acct[i].filterSVC();
          acct[i].filter(strConImporte.equals("Y"), strLevel, false);
        } else if (log4j.isDebugEnabled())
          log4j.debug("acct null!!!");
      }

      xmlDocument.setData("group", strGroups);

      xmlDocument.setParameter("agno", startEndYear[0].name);
      xmlDocument.setParameter("agno2", startEndYearRef[0].name);
      xmlDocument.setParameter("column", startEndYear[0].name);
      xmlDocument.setParameter("columnRef", startEndYearRef[0].name);
      xmlDocument.setParameter("org", OrganizationData.selectOrgName(this, strOrg));
      xmlDocument.setParameter("column1", startEndYear[0].name);
      xmlDocument.setParameter("columnRef1", startEndYearRef[0].name);
      xmlDocument.setParameter("companyName",
          GeneralAccountingReportsData.companyName(this, vars.getClient()));
      xmlDocument.setParameter("date", DateTimeData.today(this));
      if (strDateFrom.equals(""))
        strDateFrom = startEndYear[0].begining;
      if (strDateTo.equals(""))
        strDateTo = startEndYear[0].end;
      if (strDateFromRef.equals(""))
        strDateFromRef = startEndYearRef[0].begining;
      if (strDateToRef.equals(""))
        strDateToRef = startEndYearRef[0].end;
      xmlDocument.setParameter("period", strDateFrom + " - " + strDateTo);
      xmlDocument.setParameter("periodRef", strDateFromRef + " - " + strDateToRef);
      xmlDocument.setParameter("agnoInitial", startEndYear[0].name);
      xmlDocument.setParameter("agnoRef", startEndYearRef[0].name);

      xmlDocument.setParameter(
          "principalTitle",
          strCalculateOpening.equals("Y") ? GeneralAccountingReportsData.rptTitle(this,
              strElementValue) + " (Provisional)" : GeneralAccountingReportsData.rptTitle(this,
              strElementValue));

      xmlDocument.setParameter("pageNo", strPageNo);

      AccountTreeData[][] trees = new AccountTreeData[strGroups.length][];

      for (int i = 0; i < strGroups.length; i++)
        trees[i] = acct[i].getAccounts();

      xmlDocument.setDataArray("reportDetail", "structure1", trees);

      String strResult = xmlDocument.print();
      renderFO(strResult, response);

    } catch (ArrayIndexOutOfBoundsException e) {
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "ReportWithoutNodes", vars.getLanguage()));

    }
  }

  private Vector<Object> fulfillYearsToClose(Vector<Object> vec, String yearID,
      GeneralAccountingReportsData[] closedYears, GeneralAccountingReportsData[] previousYears) {
    Vector<Object> vecAux = vec;
    String previous = "";
    // Let's see if this year has a previous one
    for (int i = 0; i < previousYears.length; i++) {
      if (previousYears[i].name.equals(yearID))
        previous = previousYears[i].previousYear;
    }
    // If not, return with what we have got until now
    if ("".equals(previous))
      return vecAux;

    // Let's see if the previous year was closed or not
    for (int i = 0; i < closedYears.length; i++) {
      if (closedYears[i].name.substring(0, 4).equals(yearID))
        if ("N".equals(closedYears[i].id.substring(0, 1))) {
          if (!vecAux.contains(previous))
            vecAux.add(previous);
          // If not, let's check if previous of the previous was closed
          vecAux = fulfillYearsToClose(vecAux, previous, closedYears, previousYears);
        }
    }
    return vecAux;
  }

  private AccountTreeData[] appendRecords(AccountTreeData[] data, String strIncomeSummary,
      String strISyear, String strISyearRef) throws ServletException {
    if (data == null || strIncomeSummary == null || strIncomeSummary.equals("")
        || strISyear == null || strISyear.equals("") || strISyearRef == null
        || strISyearRef.equals(""))
      return data;
    AccountTreeData[] data2 = new AccountTreeData[data.length + 1];
    boolean found = false;
    for (int i = 0; i < data.length; i++) {
      if (data[i].id.equals(strIncomeSummary)) {
        found = true;
        BigDecimal isYear = new BigDecimal(strISyear);
        BigDecimal isYearRef = new BigDecimal(strISyearRef);
        data[i].qty = (new BigDecimal(data[i].qty).add(isYear)).toPlainString();
        data[i].qtycredit = (new BigDecimal(data[i].qtycredit).add(isYear)).toPlainString();
        data[i].qtyRef = (new BigDecimal(data[i].qtyRef).add(isYearRef)).toPlainString();
        data[i].qtycreditRef = (new BigDecimal(data[i].qtycreditRef).add(isYearRef))
            .toPlainString();
      }
      data2[i] = data[i];
    }
    if (!found) {
      data2[data2.length - 1] = new AccountTreeData();
      data2[data2.length - 1].id = strIncomeSummary;
      data2[data2.length - 1].qty = strISyear;
      data2[data2.length - 1].qtycredit = strISyear;
      data2[data2.length - 1].qtyRef = strISyearRef;
      data2[data2.length - 1].qtycreditRef = strISyearRef;
    } else
      return data;
    return data2;
  }

  private String processIncomeSummary(String strDateFrom, String strDateTo, String strAgno,
      String strTreeOrg, String strOrg, String strcAcctSchemaId) throws ServletException,
      IOException {
    String strISRevenue = GeneralAccountingReportsData.selectPyG(this, "R", strDateFrom, strDateTo,
        strcAcctSchemaId, strAgno, Tree.getMembers(this, strTreeOrg, strOrg));
    String strISExpense = GeneralAccountingReportsData.selectPyG(this, "E", strDateFrom, strDateTo,
        strcAcctSchemaId, strAgno, Tree.getMembers(this, strTreeOrg, strOrg));
    BigDecimal totalRevenue = new BigDecimal(strISRevenue);
    BigDecimal totalExpense = new BigDecimal(strISExpense);
    BigDecimal total = totalRevenue.add(totalExpense);
    if (log4j.isDebugEnabled())
      log4j.debug(total.toString());
    return total.toString();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strAgno, String strAgnoRef, String strDateFrom, String strDateTo, String strPageNo,
      String strDateFromRef, String strDateToRef, String strAsDateTo, String strAsDateToRef,
      String strElementValue, String strConImporte, String strOrg, String strLevel,
      String strConCodigo, String strcAcctSchemaId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/GeneralAccountingReports").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GeneralAccountingReports", false, "",
        "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.GeneralAccountingReports");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "GeneralAccountingReports.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GeneralAccountingReports.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GeneralAccountingReports");
      vars.removeMessage("GeneralAccountingReports");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("agno", strAgno);
    xmlDocument.setParameter("agnoRef", strAgnoRef);
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
    xmlDocument.setParameter("PageNo", strPageNo);
    xmlDocument.setParameter("dateToRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("asDateTo", strAsDateTo);
    xmlDocument.setParameter("asDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("asDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("asDateToRef", strAsDateToRef);
    xmlDocument.setParameter("asDateToRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("asDateToRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("conImporte", strConImporte);
    xmlDocument.setParameter("conCodigo", strConCodigo);
    xmlDocument.setParameter("C_Org_ID", strOrg);
    xmlDocument.setParameter("C_ElementValue_ID", strElementValue);
    xmlDocument.setParameter("level", strLevel);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure", AccountingSchemaMiscData
        .selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralAccountingReports"),
            Utility.getContext(this, vars, "#User_Client", "GeneralAccountingReports"),
            strcAcctSchemaId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_ElementValue level", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "GeneralAccountingReports"), Utility.getContext(this, vars, "#User_Client",
              "GeneralAccountingReports"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "GeneralAccountingReports", "");
      xmlDocument.setData("reportLevel", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter(
        "orgs",
        Utility.arrayDobleEntrada("arrOrgs",
            GeneralAccountingReportsData.selectOrgsDouble(this, vars.getClient())));
    xmlDocument.setParameter(
        "accountingReports",
        Utility.arrayDobleEntrada("arrAccountingReports",
            GeneralAccountingReportsData.selectRptDouble(this)));
    xmlDocument.setParameter(
        "years",
        Utility.arrayDobleEntrada("arrYears",
            GeneralAccountingReportsData.selectYearsDouble(this, vars.getUserClient())));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet GeneralAccountingReportsData";
  } // end of getServletInfo() method
}
