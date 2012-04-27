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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class ReportGeneralLedger extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId", "");
      String strDateFrom = vars
          .getGlobalVariable("inpDateFrom", "ReportGeneralLedger|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo", "");
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReportGeneralLedger|PageNo", "1");
      String strAmtFrom = vars.getNumericGlobalVariable("inpAmtFrom",
          "ReportGeneralLedger|AmtFrom", "");
      String strAmtTo = vars.getNumericGlobalVariable("inpAmtTo", "ReportGeneralLedger|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO", "");
      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvalueto);
      strcelementvaluefromdes = (strcelementvaluefromdes.equals("null")) ? ""
          : strcelementvaluefromdes;
      strcelementvaluetodes = (strcelementvaluetodes.equals("null")) ? "" : strcelementvaluetodes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportGeneralLedger|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReportGeneralLedger|cProjectId", "", IsIDFilter.instance);
      String strGroupBy = vars.getGlobalVariable("inpGroupBy", "ReportGeneralLedger|GroupBy", "");
      String strHide = vars.getGlobalVariable("inpHideMatched", "ReportGeneralLedger|HideMatched",
          "");

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strHide, strcAcctSchemaId, strcelementvaluefromdes,
          strcelementvaluetodes);
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportGeneralLedger|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo");
      String strPageNo = vars.getRequestGlobalVariable("inpPageNo", "ReportGeneralLedger|PageNo");
      String strAmtFrom = vars.getNumericParameter("inpAmtFrom");
      vars.setSessionValue("ReportGeneralLedger|AmtFrom", strAmtFrom);
      String strAmtTo = vars.getNumericParameter("inpAmtTo");
      vars.setSessionValue("ReportGeneralLedger|AmtTo", strAmtTo);
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO");
      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvalueto);
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportGeneralLedger|mProductId", IsIDFilter.instance);
      String strcProjectId = vars.getRequestInGlobalVariable("inpcProjectId_IN",
          "ReportGeneralLedger|cProjectId", IsIDFilter.instance);
      String strGroupBy = vars
          .getRequestGlobalVariable("inpGroupBy", "ReportGeneralLedger|GroupBy");
      String strHide = vars.getStringParameter("inpHideMatched");
      if (strHide.equals(""))
        vars.removeSessionValue("ReportGeneralLedger|HideMatched");
      else
        strHide = vars.getGlobalVariable("inpHideMatched", "ReportGeneralLedger|HideMatched");
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - Find - strcBpartnerId= " + strcBpartnerId);
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - XLS - strcelementvaluefrom= "
            + strcelementvaluefrom);
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - XLS - strcelementvalueto= "
            + strcelementvalueto);
      vars.setSessionValue("ReportGeneralLedger.initRecordNumber", "0");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strPageNo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
          strcProjectId, strGroupBy, strHide, strcAcctSchemaId, strcelementvaluefromdes,
          strcelementvaluetodes);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReportGeneralLedger.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReportGeneralLedger.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      // if (initRecord == 0)
      // initRecord = 1; Removed by DAL 30/4/09
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReportGeneralLedger.initRecordNumber", strInitRecord);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportGeneralLedger|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo");
      String strAmtFrom = vars.getNumericParameter("inpAmtFrom");
      vars.setSessionValue("ReportGeneralLedger|AmtFrom", strAmtFrom);
      String strAmtTo = vars.getNumericParameter("inpAmtTo");
      vars.setSessionValue("ReportGeneralLedger|AmtTo", strAmtTo);
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportGeneralLedger|mProductId", "", IsIDFilter.instance);
      String strcProjectId = vars.getInGlobalVariable("inpcProjectId_IN",
          "ReportGeneralLedger|cProjectId", "", IsIDFilter.instance);
      String strGroupBy = vars
          .getRequestGlobalVariable("inpGroupBy", "ReportGeneralLedger|GroupBy");
      String strHide = vars.getStringParameter("inpHideMatched");
      String strPageNo = vars.getGlobalVariable("inpPageNo", "ReportGeneralLedger|PageNo", "1");
      if (vars.commandIn("PDF"))
        printPageDataPDF(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
            strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
            strcProjectId, strGroupBy, strHide, strcAcctSchemaId, strPageNo);
      else
        printPageDataXLS(request, response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
            strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strmProductId,
            strcProjectId, strGroupBy, strHide, strcAcctSchemaId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strPageNo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strGroupBy, String strHide,
      String strcAcctSchemaId, String strcelementvaluefromdes, String strcelementvaluetodes)
      throws IOException, ServletException {
    String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
    // built limit/offset parameters for oracle/postgres
    String rowNum = "0";
    String oraLimit1 = null;
    String oraLimit2 = null;
    String pgLimit = null;
    if (intRecordRange != 0) {
      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        rowNum = "ROWNUM";
        oraLimit1 = String.valueOf(initRecordNumber + intRecordRange);
        oraLimit2 = (initRecordNumber + 1) + " AND " + oraLimit1;
      } else {
        rowNum = "0";
        pgLimit = intRecordRange + " OFFSET " + initRecordNumber;
      }
    }
    log4j.debug("offset= " + initRecordNumber + " pageSize= " + intRecordRange);
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    if (log4j.isDebugEnabled())
      log4j.debug("Date From:" + strDateFrom + "- To:" + strDateTo + " - Schema:"
          + strcAcctSchemaId);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportGeneralLedgerData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    // String strTreeAccount = ReportTrialBalanceData.treeAccount(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strFinancialOrgFamily = getFinancialFamily(strTreeOrg, strOrg, vars.getClient());
    String strExistsInitialDate = ReportGeneralLedgerData.yearInitialDate(this,
        vars.getSessionValue("#AD_SqlDateFormat"), strDateFrom,
        Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
        strFinancialOrgFamily);
    String strYearInitialDate = strDateFrom;
    if (strExistsInitialDate.equals(""))
      strYearInitialDate = strExistsInitialDate;
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strGroupByText = (strGroupBy.equals("BPartner") ? Utility.messageBD(this, "BusPartner",
        vars.getLanguage()) : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product",
        vars.getLanguage()) : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project",
        vars.getLanguage()) : "")));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportGeneralLedger", true, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    String strcBpartnerIdAux = strcBpartnerId;
    String strmProductIdAux = strmProductId;
    String strcProjectIdAux = strcProjectId;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      String discard[] = { "sectionAmount", "sectionPartner" };
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportGeneralLedger", discard).createXmlDocument();
      toolbar
          .prepareRelationBarTemplate(false, false,
              "submitCommandForm('XLS', false, frmMain, 'ReportGeneralLedgerExcel.xls', 'EXCEL');return false;");
      data = ReportGeneralLedgerData.set();
    } else {
      String[] discard = { "discard" };
      if (strGroupBy.equals(""))
        discard[0] = "sectionPartner";
      else
        discard[0] = "sectionAmount";
      BigDecimal previousDebit = BigDecimal.ZERO;
      BigDecimal previousCredit = BigDecimal.ZERO;
      if (strHide.equals(""))
        strHide = "N";
      String strAllaccounts = "Y";
      if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
        if (strcelementvalueto.equals("")) {
          strcelementvalueto = strcelementvaluefrom;
          strcelementvaluetodes = ReportGeneralLedgerData.selectSubaccountDescription(this,
              strcelementvalueto);
          vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);

        }
        strAllaccounts = "N";
        if (log4j.isDebugEnabled())
          log4j.debug("##################### strcelementvaluefrom= " + strcelementvaluefrom);
        if (log4j.isDebugEnabled())
          log4j.debug("##################### strcelementvalueto= " + strcelementvalueto);
      } else {
        strcelementvalueto = "";
        strcelementvaluetodes = "";
        vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      }
      data = ReportGeneralLedgerData.select(this, rowNum, strGroupByText, strGroupBy,
          vars.getLanguage(), strDateFrom, toDatePlusOne, strAllaccounts, strcelementvaluefrom,
          strcelementvalueto,
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
          Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strHide,
          strcAcctSchemaId, strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId,
          strmProductId, strcProjectId, strAmtFrom, strAmtTo, null, null, null, pgLimit, oraLimit1,
          oraLimit2);
      if (log4j.isDebugEnabled())
        log4j.debug("RecordNo: " + initRecordNumber);
      // In case this is not the first screen to show, initial balance may need to include amounts
      // of previous screen, so same sql -but from the beginning of the fiscal year- is executed

      ReportGeneralLedgerData[] dataTotal = null;
      if (data != null && data.length >= 1) {
        dataTotal = ReportGeneralLedgerData.select(this, rowNum, strGroupByText, strGroupBy,
            vars.getLanguage(), strDateFrom, toDatePlusOne, strAllaccounts, strcelementvaluefrom,
            strcelementvalueto,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
            Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strHide,
            strcAcctSchemaId, strYearInitialDate,
            DateTimeData.nDaysAfter(this, data[0].dateacct, "1"), strOrgFamily, strcBpartnerId,
            strmProductId, strcProjectId, strAmtFrom, strAmtTo, data[0].id, data[0].dateacctnumber
                + data[0].factAcctGroupId + data[0].description + data[0].isdebit,
            data[0].groupbyid, null, null, null);
      }
      // Now dataTotal is covered adding debit and credit amounts
      for (int i = 0; dataTotal != null && i < dataTotal.length; i++) {
        previousDebit = previousDebit.add(new BigDecimal(dataTotal[i].amtacctdr));
        previousCredit = previousCredit.add(new BigDecimal(dataTotal[i].amtacctcr));
      }
      String strOld = "";
      ReportGeneralLedgerData[] subreportElement = new ReportGeneralLedgerData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strOld.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReportGeneralLedgerData[1];
          if (i == 0 && initRecordNumber > 0) {
            subreportElement = new ReportGeneralLedgerData[1];
            subreportElement[0] = new ReportGeneralLedgerData();
            subreportElement[0].totalacctdr = previousDebit.toPlainString();
            subreportElement[0].totalacctcr = previousCredit.toPlainString();
            subreportElement[0].total = previousDebit.subtract(previousCredit).toPlainString();
          } else {
            if ("".equals(data[i].groupbyid)) {
              // The argument " " is used to simulate one value and put the optional parameter-->
              // AND FACT_ACCT.C_PROJECT_ID IS NULL for example
              subreportElement = ReportGeneralLedgerData.selectTotal(this, strDateFrom,
                  toDatePlusOne, null, (strGroupBy.equals("BPartner") ? " " : null), null,
                  (strGroupBy.equals("Product") ? " " : null), null,
                  (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id,
                  strYearInitialDate, strDateFrom, strOrgFamily, strHide);
            } else {
              subreportElement = ReportGeneralLedgerData.selectTotal(this, strDateFrom,
                  toDatePlusOne, (strGroupBy.equals("BPartner") ? "('" + data[i].groupbyid + "')"
                      : strcBpartnerId), null, (strGroupBy.equals("Product") ? "('"
                      + data[i].groupbyid + "')" : strmProductId), null, (strGroupBy
                      .equals("Project") ? "('" + data[i].groupbyid + "')" : strcProjectId), null,
                  strcAcctSchemaId, data[i].id, strYearInitialDate, strDateFrom, strOrgFamily,
                  strHide);
            }
          }
          data[i].totalacctdr = subreportElement[0].totalacctdr;
          data[i].totalacctcr = subreportElement[0].totalacctcr;
        }
        data[i].totalacctsub = subreportElement[0].total;

        data[i].previousdebit = subreportElement[0].totalacctdr;
        data[i].previouscredit = subreportElement[0].totalacctcr;
        data[i].previoustotal = subreportElement[0].total;
        strOld = data[i].groupbyid + data[i].id;
      }
      String strTotal = "";
      int g = 0;
      subreportElement = new ReportGeneralLedgerData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strTotal.equals(data[i].groupbyid + data[i].id)) {
          subreportElement = new ReportGeneralLedgerData[1];
          if ("".equals(data[i].groupbyid)) {
            // The argument " " is used to simulate one value and put the optional parameter--> AND
            // FACT_ACCT.C_PROJECT_ID IS NULL for example
            subreportElement = ReportGeneralLedgerData.selectTotal(this, strDateFrom,
                toDatePlusOne, null, (strGroupBy.equals("BPartner") ? " " : null), null,
                (strGroupBy.equals("Product") ? " " : null), null,
                (strGroupBy.equals("Project") ? " " : null), strcAcctSchemaId, data[i].id,
                strYearInitialDate, toDatePlusOne, strOrgFamily, strHide);
          } else {
            subreportElement = ReportGeneralLedgerData.selectTotal(this, strDateFrom,
                toDatePlusOne, (strGroupBy.equals("BPartner") ? "('" + data[i].groupbyid + "')"
                    : strcBpartnerId), null, (strGroupBy.equals("Product") ? "('"
                    + data[i].groupbyid + "')" : strmProductId), null, (strGroupBy
                    .equals("Project") ? "('" + data[i].groupbyid + "')" : strcProjectId), null,
                strcAcctSchemaId, data[i].id, strYearInitialDate, toDatePlusOne, strOrgFamily,
                strHide);
          }
          g++;
        }
        data[i].finaldebit = subreportElement[0].totalacctdr;
        data[i].finalcredit = subreportElement[0].totalacctcr;
        data[i].finaltotal = subreportElement[0].total;
        strTotal = data[i].groupbyid + data[i].id;
      }

      boolean hasPrevious = !(data == null || data.length == 0 || initRecordNumber <= 1);
      boolean hasNext = !(data == null || data.length == 0 || data.length < intRecordRange);
      toolbar
          .prepareRelationBarTemplate(hasPrevious, hasNext,
              "submitCommandForm('XLS', true, frmMain, 'ReportGeneralLedgerExcel.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportGeneralLedger", discard).createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportGeneralLedger");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportGeneralLedger.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportGeneralLedger.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportGeneralLedger");
      vars.removeMessage("ReportGeneralLedger");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
          Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), '*');
      comboTableData.fillParameters(null, "ReportGeneralLedger", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("PageNo", strPageNo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("amtFrom", strAmtFrom);
    xmlDocument.setParameter("amtTo", strAmtTo);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setParameter("paramElementvalueIdTo", strcelementvalueto);
    xmlDocument.setParameter("paramElementvalueIdFrom", strcelementvaluefrom);
    xmlDocument.setParameter("inpElementValueIdTo_DES", strcelementvaluetodes);
    xmlDocument.setParameter("inpElementValueIdFrom_DES", strcelementvaluefromdes);
    xmlDocument.setParameter("paramHide0", !strHide.equals("Y") ? "0" : "1");
    xmlDocument.setParameter("groupbyselected", strGroupBy);
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
    xmlDocument
        .setData("reportC_ACCTSCHEMA_ID", "liststructure", AccountingSchemaMiscData
            .selectC_ACCTSCHEMA_ID(this,
                Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
                Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
                strcAcctSchemaId));

    if (log4j.isDebugEnabled())
      log4j.debug("data.length: " + data.length);

    if (data != null && data.length > 0) {
      if (strExistsInitialDate.equals("") && vars.commandIn("FIND")) {
        xmlDocument.setParameter("messageType", "WARNING");
        xmlDocument.setParameter("messageTitle",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()));
        xmlDocument.setParameter("messageMessage",
            Utility.messageBD(this, "InitialDateNotFoundCalendar", vars.getLanguage()));
      }
      if (strGroupBy.equals(""))
        xmlDocument.setData("structure1", data);
      else
        xmlDocument.setData("structure2", data);
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

    /*
     * if (strcBpartnerId.equals("") && strAll.equals("")) xmlDocument.setDataArray("reportTotals",
     * "structure", subreport); else xmlDocument.setDataArray("reportTotals2", "structure",
     * subreport); if (strcBpartnerId.equals("") && strAll.equals(""))
     * xmlDocument.setDataArray("reportAll", "structure", subreport2); else
     * xmlDocument.setDataArray("reportAll2", "structure", subreport2);
     */

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strHide, String strcAcctSchemaId, String strPageNo) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");
    response.setContentType("text/html; charset=UTF-8");
    ReportGeneralLedgerData[] data = null;
    ReportGeneralLedgerData[] subreport = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = "";
    strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strFinancialOrgFamily = getFinancialFamily(strTreeOrg, strOrg, vars.getClient());
    String strYearInitialDate = ReportGeneralLedgerData.yearInitialDate(this,
        vars.getSessionValue("#AD_SqlDateFormat"), strDateFrom,
        Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
        strFinancialOrgFamily);
    if (strYearInitialDate.equals(""))
      strYearInitialDate = strDateFrom;
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strGroupByText = (strGroupBy.equals("BPartner") ? Utility.messageBD(this, "BusPartner",
        vars.getLanguage()) : (strGroupBy.equals("Product") ? Utility.messageBD(this, "Product",
        vars.getLanguage()) : (strGroupBy.equals("Project") ? Utility.messageBD(this, "Project",
        vars.getLanguage()) : "")));
    String strAllaccounts = "Y";

    if (!strDateFrom.equals("") && !strDateTo.equals("")) {
      strOrgFamily = getFamily(strTreeOrg, strOrg);
      if (!strHide.equals("Y"))
        strHide = "N";
      if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
        if (strcelementvalueto.equals(""))
          strcelementvalueto = strcelementvaluefrom;
        strAllaccounts = "N";
      }
      data = ReportGeneralLedgerData.select(this, "0", strGroupByText, strGroupBy,
          vars.getLanguage(), strDateFrom, toDatePlusOne, strAllaccounts, strcelementvaluefrom,
          strcelementvalueto,
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
          Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strHide,
          strcAcctSchemaId, strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId,
          strmProductId, strcProjectId, strAmtFrom, strAmtTo, null, null, null, null, null, null);
    }
    if (data == null || data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    } else {
      String strOld = "";
      BigDecimal totalDebit = BigDecimal.ZERO;
      BigDecimal totalCredit = BigDecimal.ZERO;
      BigDecimal subTotal = BigDecimal.ZERO;

      subreport = new ReportGeneralLedgerData[data.length];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strOld.equals(data[i].groupbyid + data[i].id)) {
          if ("".equals(data[i].groupbyid)) {
            // The argument " " is used to simulate one value and put the optional parameter--> AND
            // FACT_ACCT.C_PROJECT_ID IS NULL for example
            subreport = ReportGeneralLedgerData.selectTotal(this, strDateFrom, DateTimeData
                .nDaysAfter(this, strDateTo, "1"), null, (strGroupBy.equals("BPartner") ? " "
                : null), null, (strGroupBy.equals("Product") ? " " : null), null, (strGroupBy
                .equals("Project") ? " " : null), strcAcctSchemaId, data[i].id, strYearInitialDate,
                strDateFrom, strOrgFamily, strHide);
          } else {
            subreport = ReportGeneralLedgerData.selectTotal(this, strDateFrom, DateTimeData
                .nDaysAfter(this, strDateTo, "1"), (strGroupBy.equals("BPartner") ? "('"
                + data[i].groupbyid + "')" : strcBpartnerId), null,
                (strGroupBy.equals("Product") ? "('" + data[i].groupbyid + "')" : strmProductId),
                null, (strGroupBy.equals("Project") ? "('" + data[i].groupbyid + "')"
                    : strcProjectId), null, strcAcctSchemaId, data[i].id, strYearInitialDate,
                strDateFrom, strOrgFamily, strHide);
          }
          totalDebit = BigDecimal.ZERO;
          totalCredit = BigDecimal.ZERO;
          subTotal = BigDecimal.ZERO;
        }
        totalDebit = totalDebit.add(new BigDecimal(data[i].amtacctdr));
        data[i].totalacctdr = new BigDecimal(subreport[0].totalacctdr).add(totalDebit).toString();
        totalCredit = totalCredit.add(new BigDecimal(data[i].amtacctcr));
        data[i].totalacctcr = new BigDecimal(subreport[0].totalacctcr).add(totalCredit).toString();
        subTotal = subTotal.add(new BigDecimal(data[i].total));
        data[i].totalacctsub = new BigDecimal(subreport[0].total).add(subTotal).toString();
        data[i].previousdebit = subreport[0].totalacctdr;
        data[i].previouscredit = subreport[0].totalacctcr;
        data[i].previoustotal = subreport[0].total;
        strOld = data[i].groupbyid + data[i].id;
      }

      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportGeneralLedger.jrxml";
      response.setHeader("Content-disposition", "inline; filename=ReportGeneralLedgerPDF.pdf");

      HashMap<String, Object> parameters = new HashMap<String, Object>();

      String strLanguage = vars.getLanguage();

      parameters.put("ShowGrouping", new Boolean(!strGroupBy.equals("")));
      StringBuilder strSubTitle = new StringBuilder();
      strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
          + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
      strSubTitle.append(ReportGeneralLedgerData.selectCompany(this, vars.getClient()) + " - ");
      strSubTitle.append(ReportGeneralLedgerData.selectOrganization(this, strOrg) + ")");
      parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
      parameters.put("Previous", Utility.messageBD(this, "Initial Balance", strLanguage));
      parameters.put("Total", Utility.messageBD(this, "Total", strLanguage));
      parameters.put("PageNo", strPageNo);
      String strDateFormat;
      strDateFormat = vars.getJavaDateFormat();
      parameters.put("strDateFormat", strDateFormat);

      renderJR(vars, response, strReportName, "pdf", parameters, data, null);
    }
  }

  private void printPageDataXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAmtFrom,
      String strAmtTo, String strcelementvaluefrom, String strcelementvalueto, String strOrg,
      String strcBpartnerId, String strmProductId, String strcProjectId, String strGroupBy,
      String strHide, String strcAcctSchemaId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: XLS");
    response.setContentType("text/html; charset=UTF-8");
    ReportGeneralLedgerData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = "";
    strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strFinancialOrgFamily = getFinancialFamily(strTreeOrg, strOrg, vars.getClient());
    String strYearInitialDate = ReportGeneralLedgerData.yearInitialDate(this,
        vars.getSessionValue("#AD_SqlDateFormat"), strDateFrom,
        Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
        strFinancialOrgFamily);
    if (strYearInitialDate.equals(""))
      strYearInitialDate = strDateFrom;
    String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");

    String strAllaccounts = "Y";

    if (!strDateFrom.equals("") && !strDateTo.equals("")) {
      if (!strHide.equals("Y"))
        strHide = "N";
      if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
        if (strcelementvalueto.equals(""))
          strcelementvalueto = strcelementvaluefrom;
        strAllaccounts = "N";
      }
      data = ReportGeneralLedgerData.selectXLS(this, vars.getLanguage(), strDateFrom,
          toDatePlusOne, strAllaccounts, strcelementvaluefrom, strcelementvalueto,
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
          Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strHide,
          strcAcctSchemaId, strDateFrom, toDatePlusOne, strOrgFamily, strcBpartnerId,
          strmProductId, strcProjectId, strAmtFrom, strAmtTo);
    }
    if (data == null || data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    } else {

      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportGeneralLedgerExcel.jrxml";

      HashMap<String, Object> parameters = new HashMap<String, Object>();

      String strLanguage = vars.getLanguage();

      StringBuilder strSubTitle = new StringBuilder();
      strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
          + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
      strSubTitle.append(ReportGeneralLedgerData.selectCompany(this, vars.getClient()) + " - ");
      strSubTitle.append(ReportGeneralLedgerData.selectOrganization(this, strOrg) + ")");
      parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
      String strDateFormat;
      strDateFormat = vars.getJavaDateFormat();
      parameters.put("strDateFormat", strDateFormat);

      renderJR(vars, response, strReportName, "xls", parameters, data, null);
    }
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
  }

  private String getFinancialFamily(String strTree, String strChild, String strClientId)
      throws IOException, ServletException {
    log4j.debug("Tree.getFinancialMembers");
    ReportGeneralLedgerData[] data = ReportGeneralLedgerData.getFinancialOrgs(this, strTree,
        strChild, strClientId);

    boolean bolFirstLine = true;
    String strText = "";
    for (int i = 0; i < data.length; i++) {
      data[i].id = "'" + data[i].id + "'";
      if (bolFirstLine) {
        bolFirstLine = false;
        strText = data[i].id;
      } else {
        strText = data[i].id + "," + strText;
      }
    }
    return strText;
  }

  @Override
  public String getServletInfo() {
    return "Servlet ReportGeneralLedger. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
