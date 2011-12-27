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

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportSalesOrderOpenItemJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strWarehouse = vars.getGlobalVariable("inpWarehouse",
          "ReportSalesOrderOpenItemJR|Warehouse", "");
      printPageDataSheet(response, vars, strWarehouse);
    } else if (vars.commandIn("FIND")) {
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse",
          "ReportSalesOrderOpenItemJR|Warehouse");
      printPageDataHtml(response, vars, strWarehouse);
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strWarehouse) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");

    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItemEdit.jrxml";
    String strOutput = "html";

    ReportSalesOrderOpenItemData[] data = ReportSalesOrderOpenItemData.select(this,
        Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderOpenItemJR"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesOrderOpenItemJR"),
        strWarehouse);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    renderJR(vars, response, strReportName, strOutput, parameters, data, null);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWarehouse) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    XmlDocument xmlDocument;

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItemJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesOrderOpenItemJR", false,
        "", "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportSalesOrderOpenItemJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportSalesOrderOpenItemJR.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportSalesOrderOpenItemJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportSalesOrderOpenItemJR");
      vars.removeMessage("ReportSalesOrderOpenItemJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("mWarehouseId", strWarehouse);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID",
          "", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesOrderOpenItemJR"),
          Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderOpenItemJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderOpenItemJR",
          strWarehouse);
      xmlDocument
          .setData("reportM_WAREHOUSESHIPPER", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  /*
   * void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String
   * strWarehouse) throws IOException, ServletException { if (log4j.isDebugEnabled())
   * log4j.debug("Output: dataSheet"); response.setContentType("text/html; charset=UTF-8");
   * PrintWriter out = response.getWriter(); String discard[]={"discard"}; XmlDocument xmlDocument;
   * ReportSalesOrderOpenItemData[] data=ReportSalesOrderOpenItemData.select(this,
   * Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderOpenItem"),
   * Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesOrderOpenItem"),
   * strWarehouse); if (data == null || data.length == 0){ xmlDocument = xmlEngine.readXmlTemplate(
   * "org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItem", discard).createXmlDocument();
   * data = ReportSalesOrderOpenItemData.set(); } else { xmlDocument =xmlEngine.readXmlTemplate(
   * "org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItem" ).createXmlDocument(); }
   * 
   * ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesOrderOpenItem", false, "",
   * "", "",false, "ad_reports", strReplaceWith, false, true);
   * toolbar.prepareSimpleToolBarTemplate(); xmlDocument.setParameter("toolbar",
   * toolbar.toString());
   * 
   * try { WindowTabs tabs = new WindowTabs(this, vars,
   * "org.openbravo.erpCommon.ad_reports.ReportSalesOrderOpenItem");
   * xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
   * xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
   * xmlDocument.setParameter("childTabContainer", tabs.childTabs());
   * xmlDocument.setParameter("theme", vars.getTheme()); NavigationBar nav = new NavigationBar(this,
   * vars.getLanguage(), "ReportSalesOrderOpenItem.html", classInfo.id, classInfo.type,
   * strReplaceWith, tabs.breadcrumb()); xmlDocument.setParameter("navigationBar", nav.toString());
   * LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportSalesOrderOpenItem.html",
   * strReplaceWith); xmlDocument.setParameter("leftTabs", lBar.manualTemplate()); } catch
   * (Exception ex) { throw new ServletException(ex); } { OBError myMessage =
   * vars.getMessage("ReportSalesOrderOpenItem"); vars.removeMessage("ReportSalesOrderOpenItem"); if
   * (myMessage!=null) { xmlDocument.setParameter("messageType", myMessage.getType());
   * xmlDocument.setParameter("messageTitle", myMessage.getTitle());
   * xmlDocument.setParameter("messageMessage", myMessage.getMessage()); } }
   * 
   * 
   * xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
   * xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
   * xmlDocument.setParameter("mWarehouseId", strWarehouse); try { ComboTableData comboTableData =
   * new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this,
   * vars, "#AccessibleOrgTree", "ReportSalesOrderOpenItem"), Utility.getContext(this, vars,
   * "#User_Client", "ReportSalesOrderOpenItem"), 0); Utility.fillSQLParameters(this, vars, null,
   * comboTableData, "ReportSalesOrderOpenItem", strWarehouse);
   * xmlDocument.setData("reportM_WAREHOUSESHIPPER","liststructure", comboTableData.select(false));
   * comboTableData = null; } catch (Exception ex) { throw new ServletException(ex); }
   * 
   * xmlDocument.setData("structure1", data); out.println(xmlDocument.print()); out.close();
   * 
   * }
   */
  public String getServletInfo() {
    return "Servlet ReportSalesOrderOpenItem. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
