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
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportGuaranteeDateJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDate = vars.getGlobalVariable("inpDate", "ReportGuaranteeDateJR|date", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportGuaranteeDateJR|cBpartnerId", "");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId", "");
      printPageDataSheet(response, vars, strDate, strcBpartnerId, strmWarehouseId);
    } else if (vars.commandIn("PRINT_HTML")) {
      String strDate = vars.getRequestGlobalVariable("inpDate", "ReportGuaranteeDateJR|date");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpcBPartnerId",
          "ReportGuaranteeDateJR|cBpartnerId");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId");
      setHistoryCommand(request, "FIND");
      printPageDataHtml(response, vars, strDate, strcBpartnerId, strmWarehouseId, "html");
    } else if (vars.commandIn("PRINT_PDF")) {
      String strDate = vars.getRequestGlobalVariable("inpDate", "ReportGuaranteeDateJR|date");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpcBPartnerId",
          "ReportGuaranteeDateJR|cBpartnerId");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId");
      setHistoryCommand(request, "FIND");
      printPageDataHtml(response, vars, strDate, strcBpartnerId, strmWarehouseId, "pdf");
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strDate, String strcBpartnerId, String strmWarehouseId, String strOutput)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");

    // XmlDocument xmlDocument=null;
    ReportGuaranteeDateData[] data = null;
    String discard[] = { "discard" };
    data = ReportGuaranteeDateData.select(this,
        Utility.getContext(this, vars, "#User_Client", "ReportGuaranteeDateJR"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGuaranteeDateJR"),
        DateTimeData.nDaysAfter(this, strDate, "1"), strcBpartnerId, strmWarehouseId);

    if (data == null || data.length == 0) {
      discard[0] = "selEliminar";
      data = ReportGuaranteeDateData.set();
    }

    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportGuaranteeDateJR.jrxml";
    if (strOutput.equals("pdf"))
      response.setHeader("Content-disposition", "inline; filename=ReportGuaranteeDateJR.pdf");

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ReportData", strDate);

    renderJR(vars, response, strReportName, strOutput, parameters, data, null);

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDate, String strcBpartnerId, String strmWarehouseId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportGuaranteeDateJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportGuaranteeDateJR", false, "", "",
        "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportGuaranteeDateJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportGuaranteeDateJR.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportGuaranteeDateJR.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportGuaranteeDateJR");
      vars.removeMessage("ReportGuaranteeDateJR");
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
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("bPartnerDescription",
        ReportGuaranteeDateData.selectBpartner(this, strcBpartnerId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGuaranteeDateJR"),
          Utility.getContext(this, vars, "#User_Client", "ReportGuaranteeDateJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportGuaranteeDateJR",
          strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportGuaranteeDateJR. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
