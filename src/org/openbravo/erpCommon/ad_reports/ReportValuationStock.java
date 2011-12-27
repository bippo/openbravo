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

public class ReportValuationStock extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT", "RELATION")) {
      String strDate = vars.getGlobalVariable("inpDate", "ReportValuationStock|Date",
          DateTimeData.today(this));
      String strWarehouse = vars.getGlobalVariable("inpmWarehouseId",
          "ReportValuationStock|Warehouse", "");
      String strCategoryProduct = vars.getGlobalVariable("inpCategoryProduct",
          "ReportValuationStock|CategoryProduct", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportValuationStock|currency", strUserCurrencyId);
      printPageDataSheet(request, response, vars, strDate, strWarehouse, strCategoryProduct,
          strCurrencyId);
    } else if (vars.commandIn("FIND")) {
      String strDate = vars.getGlobalVariable("inpDate", "ReportValuationStock|Date",
          DateTimeData.today(this));
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportValuationStock|Warehouse");
      String strCategoryProduct = vars.getRequestGlobalVariable("inpCategoryProduct",
          "ReportValuationStock|CategoryProduct");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportValuationStock|currency", strUserCurrencyId);
      printPageDataSheet(request, response, vars, strDate, strWarehouse, strCategoryProduct,
          strCurrencyId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDate, String strWarehouse, String strCategoryProduct,
      String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "discard" };
    XmlDocument xmlDocument;

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    ReportValuationStockData[] data = null;
    String strBaseCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    OBError myMessage = null;
    myMessage = new OBError();
    String strConvRateErrorMsg = "";
    if (vars.commandIn("FIND")) {
      try {
        data = ReportValuationStockData.select(this, vars.getLanguage(), strDate,
            strBaseCurrencyId, strCurrencyId, DateTimeData.nDaysAfter(this, strDate, "1"),
            strWarehouse, strCategoryProduct);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
    }
    // If a conversion rate is missing for a certain transaction, an error
    // message window pops-up.
    if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
      advise(request, response, "ERROR",
          Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
          strConvRateErrorMsg);
    } else { // Otherwise, the report is launched
      if (data == null || data.length == 0) {
        data = ReportValuationStockData.set();
        discard[0] = "sectionCategoryProduct";
      }
      if (vars.commandIn("DEFAULT")) {
        discard[0] = "sectionCategoryProduct";
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportValuationStock", discard).createXmlDocument();
        data = ReportValuationStockData.set();
      } else {
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportValuationStock", discard).createXmlDocument();
      }

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportValuationStock", false, "",
          "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_reports.ReportValuationStock");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportValuationStock.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportValuationStock.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        myMessage = vars.getMessage("ReportValuationStock");
        vars.removeMessage("ReportValuationStock");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("date", strDate);
      xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("mWarehouseId", strWarehouse);
      xmlDocument.setParameter("categoryProduct", strCategoryProduct);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "M_Warehouse_ID",
            "M_Warehouse of Client", "", Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "", "");
        xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                ""), Utility.getContext(this, vars, "#User_Client", ""), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "", strCategoryProduct);
        xmlDocument.setData("reportM_PRODUCT_CATEGORYID", "liststructure",
            comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("ccurrencyid", strCurrencyId);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportValuationStock"),
            Utility.getContext(this, vars, "#User_Client", "ReportValuationStock"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportValuationStock",
            strCurrencyId);
        xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setData("structure1", data);
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet ReportValuationStock. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
