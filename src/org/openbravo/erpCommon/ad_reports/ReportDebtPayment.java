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

public class ReportDebtPayment extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT", "DIRECT")) {
      String strcbankaccount = vars.getGlobalVariable("inpmProductId",
          "ReportDebtPayment|C_Bankaccount_ID", "");
      String strC_BPartner_ID;
      String strDateFrom;
      String strDateTo;
      // When the Business Partner Multiple Selector is added to this
      // report, it will continue supporting the previous BP selector
      String strcBpartnerId; // BP Multiple Selector Variable
      // If this report is reached through the AgingBalance Report, some
      // session variables are ignored, so Aging Balance data is readden
      if (vars.getStringParameter("inpFlagFromAging").equals("Y")) {
        strC_BPartner_ID = vars.getStringParameter("inpBpartnerId");
        strDateFrom = vars.getStringParameter("inpDateFrom");
        strDateTo = vars.getStringParameter("inpDateTo");
        if (strC_BPartner_ID.length() > 0) {
          strcBpartnerId = "('" + strC_BPartner_ID + "')";
        } else {
          // strcBpartnerId =
          // vars.getInStringParameter("inpcBPartnerId_IN");
          strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
              "ReportAgingBalance|cBpartnerId", "", IsIDFilter.instance);
          vars.setSessionValue("ReportDebtPayment|C_BPartner_ID", strcBpartnerId);
        }
      } else {
        strC_BPartner_ID = vars.getGlobalVariable("inpBpartnerId",
            "ReportDebtPayment|C_BPartner_ID", "");
        strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportDebtPayment|DateFrom", "");
        strDateTo = vars.getGlobalVariable("inpDateTo", "ReportDebtPayment|DateTo", "");
        strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId_IN",
            "ReportDebtPayment|inpcBPartnerId_IN", "");
      }
      String strCal1 = vars.getNumericGlobalVariable("inpCal1", "ReportDebtPayment|Cal1", "");
      String strCal2 = vars.getNumericGlobalVariable("inpCal2", "ReportDebtPayment|Cal2", "");
      String strPaymentRule = vars.getGlobalVariable("inpCPaymentRuleId",
          "ReportDebtPayment|PaymentRule", "");
      String strSettle = vars.getGlobalVariable("inpSettle", "ReportDebtPayment|Settle", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportDebtPayment|currency",
          strUserCurrencyId);
      String strConciliate = vars.getGlobalVariable("inpConciliate",
          "ReportDebtPayment|Conciliate", "");
      String strStatus = vars.getGlobalVariable("inpStatus", "ReportDebtPayment|Status", "");
      String strGroup = vars.getGlobalVariable("inpGroup", "ReportDebtPayment|Group", "isGroup");
      String strPending = "";
      String strReceipt = "";
      if (vars.commandIn("DIRECT")) {
        strReceipt = vars.getGlobalVariable("inpReceipt", "ReportDebtPayment|Receipt", "N");
        strPending = vars.getGlobalVariable("inpPending", "ReportDebtPayment|Pending", "");
      } else {
        strReceipt = vars.getGlobalVariable("inpReceipt", "ReportDebtPayment|Receipt", "Y");
        strPending = vars.getGlobalVariable("inpPending", "ReportDebtPayment|Pending", "isPending");
      }
      // String strEntry = vars.getGlobalVariable("inpEntry",
      // "ReportDebtPayment|Entry","0");
      setHistoryCommand(request, "DIRECT");
      String strGroupBA = vars.getRequestGlobalVariable("inpGroupBA", "ReportDebtPayment|Group");
      printPageDataSheet(response, vars, strcBpartnerId, strDateFrom, strDateTo, strCal1, strCal2,
          strPaymentRule, strSettle, strConciliate, strReceipt, strPending, strcbankaccount,
          strStatus, strGroup, strGroupBA, strCurrencyId);
    } else if (vars.commandIn("FIND")) {
      String strcbankaccount = vars.getRequestGlobalVariable("inpcBankAccountId",
          "ReportDebtPayment|C_Bankaccount_ID");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportDebtPayment|inpcBPartnerId_IN", IsIDFilter.instance);
      // String strC_BPartner_ID =
      // vars.getRequestGlobalVariable("inpBpartnerId",
      // "ReportDebtPayment|C_BPartner_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportDebtPayment|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportDebtPayment|DateTo");
      String strCal1 = vars.getNumericParameter("inpCal1");
      vars.setSessionValue("ReportDebtPayment|Cal1", strCal1);
      String strCal2 = vars.getNumericParameter("inpCal2");
      vars.setSessionValue("ReportDebtPayment|Cal2", strCal2);
      String strPaymentRule = vars.getRequestGlobalVariable("inpCPaymentRuleId",
          "ReportDebtPayment|PaymentRule");
      String strSettle = vars.getRequestGlobalVariable("inpSettle", "ReportDebtPayment|Settle");
      String strConciliate = vars.getRequestGlobalVariable("inpConciliate",
          "ReportDebtPayment|Conciliate");
      String strPending = vars.getRequestGlobalVariable("inpPending", "ReportDebtPayment|Pending");
      String strGroup = vars.getRequestGlobalVariable("inpGroup", "ReportDebtPayment|Group");
      String strStatus = vars.getRequestGlobalVariable("inpStatus", "ReportDebtPayment|Status");
      String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId",
          "ReportDebtPayment|currency");
      // String strReceipt = vars.getRequestGlobalVariable("inpReceipt",
      // "ReportDebtPayment|Receipt");
      String strReceipt = vars.getStringParameter("inpReceipt").equals("") ? "N" : vars
          .getStringParameter("inpReceipt");
      vars.setSessionValue("ReportDebtPayment|Receipt", strReceipt);
      // String strEntry = vars.getGlobalVariable("inpEntry",
      // "ReportDebtPayment|Entry","1");
      setHistoryCommand(request, "DIRECT");
      String strGroupBA = vars.getRequestGlobalVariable("inpGroupBA", "ReportDebtPayment|Group");
      printPageDataSheet(response, vars, strcBpartnerId, strDateFrom, strDateTo, strCal1, strCal2,
          strPaymentRule, strSettle, strConciliate, strReceipt, strPending, strcbankaccount,
          strStatus, strGroup, strGroupBA, strCurrencyId);
    } else if (vars.commandIn("PRINT_PDF")) {
      String strcbankaccount = vars.getRequestGlobalVariable("inpcBankAccountId",
          "ReportDebtPayment|C_Bankaccount_ID");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportDebtPayment|inpcBPartnerId_IN", IsIDFilter.instance);
      // String strC_BPartner_ID = vars.getRequestGlobalVariable("inpBpartnerId",
      // "ReportDebtPayment|C_BPartner_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportDebtPayment|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportDebtPayment|DateTo");
      String strCal1 = vars.getNumericParameter("inpCal1");
      vars.setSessionValue("ReportDebtPayment|Cal1", strCal1);
      String strCal2 = vars.getNumericParameter("inpCal2");
      vars.setSessionValue("ReportDebtPayment|Cal2", strCal2);
      String strPaymentRule = vars.getRequestGlobalVariable("inpCPaymentRuleId",
          "ReportDebtPayment|PaymentRule");
      String strSettle = vars.getRequestGlobalVariable("inpSettle", "ReportDebtPayment|Settle");
      String strConciliate = vars.getRequestGlobalVariable("inpConciliate",
          "ReportDebtPayment|Conciliate");
      String strPending = vars.getRequestGlobalVariable("inpPending", "ReportDebtPayment|Pending");
      String strGroup = vars.getRequestGlobalVariable("inpGroup", "ReportDebtPayment|Group");
      String strGroupBA = vars.getRequestGlobalVariable("inpGroupBA", "ReportDebtPayment|Group");
      String strStatus = vars.getRequestGlobalVariable("inpStatus", "ReportDebtPayment|Status");
      String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId",
          "ReportDebtPayment|currency");
      // String strReceipt = vars.getRequestGlobalVariable("inpReceipt",
      // "ReportDebtPayment|Receipt");
      String strReceipt = vars.getStringParameter("inpReceipt").equals("") ? "N" : vars
          .getStringParameter("inpReceipt");
      vars.setSessionValue("ReportDebtPayment|Receipt", strReceipt);
      // String strEntry = vars.getGlobalVariable("inpEntry", "ReportDebtPayment|Entry","1");
      setHistoryCommand(request, "DIRECT");
      printPageDataPdf(response, vars, strcBpartnerId, strDateFrom, strDateTo, strCal1, strCal2,
          strPaymentRule, strSettle, strConciliate, strReceipt, strPending, strcbankaccount,
          strStatus, strGroup, strGroupBA, strCurrencyId);
    } else
      pageError(response);
  }

  private void printPageDataPdf(HttpServletResponse response, VariablesSecureApp vars,
      String strC_BPartner_ID, String strDateFrom, String strDateTo, String strCal1,
      String strCalc2, String strPaymentRule, String strSettle, String strConciliate,
      String strReceipt, String strPending, String strcbankaccount, String strStatus,
      String strGroup, String strGroupBA, String cCurrencyConv) throws IOException,
      ServletException {
    String strAux = "";
    if (log4j.isDebugEnabled())
      log4j.debug("strGroup = " + strGroup);
    if (strPending.equals("") && strConciliate.equals("") && strSettle.equals("")) {
      strAux = "";
    } else {
      if (strPending.equals("isPending")) {
        strAux = "'P'";
      }
      if (strConciliate.equals("isConciliate")) {
        if (!strAux.equals("")) {
          strAux = strAux + ",";
        }
        strAux = strAux + "'C'";
      }
      if (strSettle.equals("isSettle")) {
        if (!strAux.equals("")) {
          strAux = strAux + ",";
        }
        strAux = strAux + "'A'";
      }
      strAux = "(" + strAux + ")";
    }
    ReportDebtPaymentData[] data = null;
    String strReportName = null;
    if (!strGroup.equals("")) {
      data = ReportDebtPaymentData.select(this, cCurrencyConv, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment"),
          strC_BPartner_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1,
          strCalc2, strPaymentRule, strReceipt, strStatus, strAux, strcbankaccount,
          "BPARTNER, BANKACC");
      if (!strGroupBA.equals("")) {
        strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportDebtPayment_BankAcc.jrxml";
      } else {
        strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportDebtPayment.jrxml";
      }

    } else {
      data = ReportDebtPaymentData.selectNoBpartner(this, cCurrencyConv, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment"),
          strC_BPartner_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1,
          strCalc2, strPaymentRule, strReceipt, strStatus, strAux, strcbankaccount,
          "BANKACC, BPARTNER");
      if (!strGroupBA.equals("")) {
        strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportDebtPayment_NoBP_BankAcc.jrxml";
      } else {
        strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportDebtPayment_NoBP.jrxml";
      }
    }
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("group", "no");
    renderJR(vars, response, strReportName, "pdf", parameters, data, null);

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strC_BPartner_ID, String strDateFrom, String strDateTo, String strCal1,
      String strCalc2, String strPaymentRule, String strSettle, String strConciliate,
      String strReceipt, String strPending, String strcbankaccount, String strStatus,
      String strGroup, String strGroupBA, String cCurrencyConv) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "discard", "discard2", "discard3", "discard4", "discard5", "discard6",
        "discard7", "discard8" };
    String strAux = "";
    if (log4j.isDebugEnabled())
      log4j.debug("strGroup = " + strGroup);
    if (strPending.equals("") && strConciliate.equals("") && strSettle.equals("")) {
      strAux = "";
    } else {
      if (strPending.equals("isPending")) {
        strAux = "'P'";
      }
      if (strConciliate.equals("isConciliate")) {
        if (!strAux.equals("")) {
          strAux = strAux + ",";
        }
        strAux = strAux + "'C'";
      }
      if (strSettle.equals("isSettle")) {
        if (!strAux.equals("")) {
          strAux = strAux + ",";
        }
        strAux = strAux + "'A'";
      }
      strAux = "(" + strAux + ")";
    }
    XmlDocument xmlDocument;
    ReportDebtPaymentData[] data = null;
    if (!strGroup.equals(""))
      data = ReportDebtPaymentData.select(this, cCurrencyConv, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment"),
          strC_BPartner_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1,
          strCalc2, strPaymentRule, strReceipt, strStatus, strAux, strcbankaccount,
          "BPARTNER, BANKACC");
    else
      data = ReportDebtPaymentData.selectNoBpartner(this, cCurrencyConv, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment"),
          strC_BPartner_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1,
          strCalc2, strPaymentRule, strReceipt, strStatus, strAux, strcbankaccount,
          "BANKACC, BPARTNER");
    if (data == null || data.length == 0) {
      data = ReportDebtPaymentData.set();
      discard[0] = "sectionBpartner";
      discard[1] = "sectionStatus2";
      discard[2] = "sectionTotal2";
      discard[3] = "sectionBankAcc";
      discard[4] = "sectionTotal3";
      discard[5] = "sectionTotal4";
      discard[6] = "sectionAll";
      if (!strGroup.equals("")) {
        discard[7] = "sectionDetail2";
      } else {
        discard[7] = "sectionTotal";
      }
    } else {
      if (!strGroupBA.equals("") && !strGroup.equals("")) {
        discard[0] = "sectionDetail2";
        discard[1] = "sectionStatus2";
        discard[2] = "sectionTotal2";
        discard[3] = "sectionBpartner";
        discard[4] = "sectionTotal";
        discard[5] = "sectionBankAcc";
        discard[6] = "sectionTotal3";
      } else if (!strGroupBA.equals("")) {
        discard[0] = "sectionDetail2";
        discard[1] = "sectionStatus2";
        discard[2] = "sectionTotal2";
        discard[3] = "sectionBpartner";
        discard[4] = "sectionTotal";
        discard[5] = "sectionTotal4";
      } else if (!strGroup.equals("")) {
        discard[0] = "sectionDetail2";
        discard[1] = "sectionStatus2";
        discard[2] = "sectionTotal2";
        discard[3] = "sectionBankAcc";
        discard[4] = "sectionTotal3";
        discard[5] = "sectionTotal4";

      } else {
        discard[0] = "sectionBpartner";
        discard[1] = "sectionTotal";
        discard[2] = "sectionTotal2";
        discard[3] = "sectionTotal3";
        discard[4] = "sectionTotal4";
      }
    }
    if (vars.commandIn("DEFAULT")) {
      discard[0] = "sectionBpartner";
      discard[1] = "sectionStatus2";
      discard[2] = "sectionTotal2";
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportDebtPayment", discard).createXmlDocument();
      data = ReportDebtPaymentData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportDebtPayment", discard).createXmlDocument();
    }
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportDebtPayment", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportDebtPayment");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportDebtPayment.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportDebtPayment.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportDebtPayment");
      vars.removeMessage("ReportDebtPayment");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("cBankAccount", strcbankaccount);
    xmlDocument.setData(
        "reportC_ACCOUNTNUMBER",
        "liststructure",
        AccountNumberComboData.select(this, vars.getLanguage(),
            Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment")));
    xmlDocument.setData(
        "reportCBPartnerId_IN",
        "liststructure",
        SelectorUtilityData.selectBpartner(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strC_BPartner_ID));
    // xmlDocument.setParameter("paramBPartnerId", strC_BPartner_ID);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("amountFrom", strCal1);
    xmlDocument.setParameter("amountTo", strCalc2);
    xmlDocument.setParameter("paymentRule", strPaymentRule);
    xmlDocument.setParameter("settle", strSettle);
    xmlDocument.setParameter("conciliate", strConciliate);
    xmlDocument.setParameter("pending", strPending);
    xmlDocument.setParameter("receipt", strReceipt);
    xmlDocument.setParameter("payable", strReceipt);
    xmlDocument.setParameter("status", strStatus);
    xmlDocument.setParameter("group", strGroup);
    xmlDocument.setParameter("groupBA", strGroupBA);
    if (log4j.isDebugEnabled())
      log4j.debug("diacard = " + discard[0] + " - " + discard[1] + " - " + discard[2]);
    // xmlDocument.setParameter("paramBPartnerDescription",
    // ReportDebtPaymentData.bPartnerDescription(this, strC_BPartner_ID));
    if (log4j.isDebugEnabled())
      log4j.debug("ListData.select PaymentRule:" + strPaymentRule);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "All_Payment Rule", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportDebtPayment"), Utility.getContext(this, vars, "#User_Client",
              "ReportDebtPayment"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportDebtPayment",
          strPaymentRule);
      xmlDocument.setData("reportPaymentRule", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("ListData.select Status:" + strPaymentRule);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_DP_Management_Status", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportDebtPayment"), Utility.getContext(this, vars, "#User_Client",
              "ReportDebtPayment"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportDebtPayment", strStatus);
      xmlDocument.setData("reportStatus", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("ccurrencyid", cCurrencyConv);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportDebtPayment"),
          Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportDebtPayment",
          cCurrencyConv);
      xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    if (!strGroup.equals("") && !strGroupBA.equals("")) {
      xmlDocument.setData("structure4", data);
    } else if (!strGroupBA.equals("")) {
      xmlDocument.setData("structure3", data);
    } else if (!strGroup.equals("")) {
      xmlDocument.setData("structure1", data);
    } else {
      xmlDocument.setData("structure2", data);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportDebtPayment. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
