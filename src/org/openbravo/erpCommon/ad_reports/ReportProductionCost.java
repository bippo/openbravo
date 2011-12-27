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

public class ReportProductionCost extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strdateFrom = vars.getGlobalVariable("inpDateFrom", "ReportProductionCost|dateFrom",
          "");
      String strdateTo = vars.getGlobalVariable("inpDateTo", "ReportProductionCost|dateTo", "");
      String strmProductId = vars.getGlobalVariable("inpmProductId",
          "ReportProductionCost|mProductId", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportProductionCost|currency", strUserCurrencyId);
      printPageDataSheet(request, response, vars, strdateFrom, strdateTo, strmProductId,
          strCurrencyId);
    } else if (vars.commandIn("FIND")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportProductionCost|dateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportProductionCost|dateTo");
      String strmProductId = vars.getRequestGlobalVariable("inpmProductId",
          "ReportProductionCost|mProductId");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportProductionCost|currency", strUserCurrencyId);
      printPageDataSheet(request, response, vars, strdateFrom, strdateTo, strmProductId,
          strCurrencyId);
    } else if (vars.commandIn("OPEN")) {
      String strdateFrom = vars.getRequiredStringParameter("inpDateFrom");
      String strdateTo = vars.getRequiredStringParameter("inpDateTo");
      String strmProductId = vars.getRequiredStringParameter("inpProduct");
      String strId = vars.getRequiredStringParameter("inpId");
      String strLevel = vars.getRequiredStringParameter("inpLevel");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportProductionCost|currency", strUserCurrencyId);
      if (log4j.isDebugEnabled())
        log4j.debug("***************************+: " + strdateFrom);
      if (log4j.isDebugEnabled())
        log4j.debug("***************************+: " + strdateTo);
      if (log4j.isDebugEnabled())
        log4j.debug("***************************+: " + strmProductId);
      if (log4j.isDebugEnabled())
        log4j.debug("***************************+: " + strId);
      if (log4j.isDebugEnabled())
        log4j.debug("***************************+: " + strLevel);

      printPageOpen(request, response, vars, strdateFrom, strdateTo, strmProductId, strId,
          strLevel, strCurrencyId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strdateFrom, String strdateTo, String strmProductId,
      String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    String discard[] = { "discard" };
    XmlDocument xmlDocument = null;
    String strLevel = "0";
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportProductionCost").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProductionCost", false, "", "",
        "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    ReportProductionCostData[] data = null;

    String strConvRateErrorMsg = "";
    if (strdateFrom.equals("") && strdateTo.equals("")) {
      data = ReportProductionCostData.set();
      discard[0] = "sectionDetail";
    } else {
      if (vars.commandIn("FIND")) {
        // Checks if there is a conversion rate for each of the transactions
        // of the report
        String strBaseCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
        OBError myMessage = null;
        myMessage = new OBError();
        try {
          data = ReportProductionCostData.select(this, strBaseCurrencyId, strCurrencyId, strLevel,
              strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"), strmProductId);
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        }
        strConvRateErrorMsg = myMessage.getMessage();
        // If a conversion rate is missing for a certain transaction, an
        // error message window pops-up.
        if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
          advise(request, response, "ERROR",
              Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
              strConvRateErrorMsg);
        } else { // Otherwise, the report is launched
          if (data == null || data.length == 0) {
            data = ReportProductionCostData.set();
            discard[0] = "sectionDetail";
          }
        }
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_reports.ReportProductionCost");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportProductionCost.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportProductionCost.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("ccurrencyid", strCurrencyId);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductionCost"),
            Utility.getContext(this, vars, "#User_Client", "ReportProductionCost"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProductionCost",
            strCurrencyId);
        xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      {
        OBError myMessage = vars.getMessage("ReportProductionCost");
        vars.removeMessage("ReportProductionCost");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("dateFrom", strdateFrom);
      xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTo", strdateTo);
      xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      // xmlDocument.setParameter("adOrgId", strCOrgId);
      // xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
      // xmlDocument.setParameter("paramBPartnerDescription",
      // ReportProductionCostData.bPartnerDescription(this,
      // strcBpartnerId));
      xmlDocument.setParameter("parammProductId", strmProductId);
      xmlDocument.setParameter("paramProductDescription",
          ReportProductionCostData.mProductDescription(this, strmProductId));

      // xmlDocument.setData("structureOrganizacion",
      // OrganizationComboData.select(this, vars.getRole()));
      xmlDocument.setData("structure", data);
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private void printPageOpen(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strdateFrom, String strdateTo, String strmProductId,
      String strId, String strLevel, String strCurrencyId) throws IOException, ServletException {
    // Ajax response
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajax");
    XmlDocument xmlDocument = null;
    String[] discard = { "discard", "discard", "discard", "discard", "discard" };
    ReportProductionCostData[] dataMaterial = null;
    ReportProductionCostData[] dataMachine = null;
    ReportProductionCostData[] dataIndirect = null;
    ReportProductionCostData[] dataEmployee = null;
    ReportProductionCostData[] dataCostCenter = null;
    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    String strBaseCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    OBError myMessage = null;
    myMessage = new OBError();

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      try {
        dataMaterial = ReportProductionCostData.selectMaterial(this, strBaseCurrencyId,
            strCurrencyId, strId, strLevel, strdateFrom,
            DateTimeData.nDaysAfter(this, strdateTo, "1"), strmProductId);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an
      // error message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advisePopUp(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (dataMaterial == null || dataMaterial.length == 0) {
          dataMaterial = ReportProductionCostData.set();
          discard[0] = "sectionMaterial";
        }
      }
    }
    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      try {
        dataMachine = ReportProductionCostData.selectMachine(this, strBaseCurrencyId,
            strCurrencyId, strLevel, strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"),
            strmProductId);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an
      // error message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advisePopUp(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (dataMachine == null || dataMachine.length == 0) {
          dataMachine = ReportProductionCostData.set();
          discard[1] = "sectionMachine";
        }
      }
    }
    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      try {
        dataIndirect = ReportProductionCostData.selectIndirect(this, strBaseCurrencyId,
            strCurrencyId, strLevel, strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"),
            strmProductId);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an
      // error message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advisePopUp(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (dataIndirect == null || dataIndirect.length == 0) {
          dataIndirect = ReportProductionCostData.set();
          discard[2] = "sectionIndirect";
        }
      }
    }
    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      try {
        dataEmployee = ReportProductionCostData.selectEmployee(this, strBaseCurrencyId,
            strCurrencyId, strLevel, strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"),
            strmProductId);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an
      // error message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advisePopUp(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (dataEmployee == null || dataEmployee.length == 0) {
          dataEmployee = ReportProductionCostData.set();
          discard[3] = "sectionEmployee";
        }
      }
    }
    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      try {
        dataCostCenter = ReportProductionCostData.selectCostCenter(this, strBaseCurrencyId,
            strCurrencyId, strLevel, strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"),
            strmProductId);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an
      // error message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advisePopUp(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (dataCostCenter == null || dataCostCenter.length == 0) {
          dataCostCenter = ReportProductionCostData.set();
          discard[4] = "sectionCostCenter";
        }
      }
    }

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportProductionCostSubreport", discard)
        .createXmlDocument();
    // xmlDocument.setData("structure1", data);
    xmlDocument.setData("structureMaterial", dataMaterial);
    xmlDocument.setData("structureMachine", dataMachine);
    xmlDocument.setData("structureIndirect", dataIndirect);
    xmlDocument.setData("structureEmployee", dataEmployee);
    xmlDocument.setData("structureCostCenter", dataCostCenter);

    response.setContentType("text/plain");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    // xmlDocument.setData("structure", data);
    out.println(xmlDocument.print());
    out.close();
  }

  /*
   * void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom,
   * String strdateTo, String strcBpartnetId, String strCOrgId, String strInvoiceRule, String
   * strDetail) throws IOException, ServletException{ if (log4j.isDebugEnabled())
   * log4j.debug("Output: print html"); XmlDocument xmlDocument=null; ReportOrderNotInvoiceData[]
   * data = null; if (!strDetail.equals("-1")) { String[] discard = {"selEliminar"}; xmlDocument =
   * xmlEngine.readXmlTemplate ("org/openbravo/erpCommon/ad_reports/ReportOrderNotInvoice",
   * discard).createXmlDocument(); } else { xmlDocument = xmlEngine.readXmlTemplate
   * ("org/openbravo/erpCommon/ad_reports/ReportOrderNotInvoice" ).createXmlDocument(); } data =
   * ReportOrderNotInvoiceData.select(this, vars.getLanguage(), Utility.getContext(this, vars,
   * "#User_Client", "ReportOrderNotInvoice"), Utility.getContext(this, vars, "#AccessibleOrgTree",
   * "ReportOrderNotInvoice"), strcBpartnetId, strCOrgId, strInvoiceRule, strdateFrom,
   * DateTimeData.nDaysAfter(this, strdateTo,"1")); xmlDocument.setData("structure1", data);
   * response.setContentType("text/html; charset=UTF-8"); PrintWriter out = response.getWriter();
   * out.println(xmlDocument.print()); out.close(); }
   */

  public String getServletInfo() {
    return "Servlet ReportProductionCost.";
  } // end of getServletInfo() method
}
