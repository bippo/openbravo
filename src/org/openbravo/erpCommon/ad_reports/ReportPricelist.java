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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportPricelist extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProductCategory = vars.getGlobalVariable("inpProductCategory",
          "ReportPricelist|productCategory", "");
      String strPricelistversionId = vars.getGlobalVariable("inpmPricelistVersion",
          "ReportPricelist|pricelistversion", "");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportPricelist|mProductId", "", IsIDFilter.instance);
      printPageDataSheet(response, vars, strProductCategory, strPricelistversionId, strmProductId);
      /*
       * } else if (vars.commandIn("DIRECT")) { String strDateFrom =
       * vars.getGlobalVariable("inpDateFrom", "ReportPricelist|dateFrom", ""); String strDateTo =
       * vars.getGlobalVariable("inpDateTo", "ReportPricelist|dateTo", ""); String strcBpartnerId =
       * vars.getGlobalVariable("inpcBPartnerId", "ReportPricelist|cBpartnerId", ""); String
       * strPartner = vars.getGlobalVariable("inpPartner", "ReportPricelist|partner", "");
       * setHistoryCommand(request, "DIRECT"); printPageDataSheet(response, vars, strDateFrom,
       * strDateTo, strcBpartnerId, strPartner);
       */
    } else if (vars.commandIn("FIND")) {
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportPricelist|productCategory");
      String strPricelistversionId = vars.getRequestGlobalVariable("inpmPricelistVersion",
          "ReportPricelist|pricelistversion");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportPricelist|mProductId", IsIDFilter.instance);
      /* setHistoryCommand(request, "DIRECT"); */
      printPageDataSheet(response, vars, strProductCategory, strPricelistversionId, strmProductId);
    } else if (vars.commandIn("EDIT_PDF")) {
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportPricelist|productCategory");
      String strPricelistversionId = vars.getRequestGlobalVariable("inpmPricelistVersion",
          "ReportPricelist|pricelistversion");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportPricelist|mProductId", IsIDFilter.instance);
      printPagePdf(request, response, vars, strProductCategory, strPricelistversionId,
          strmProductId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strProductCategory, String strPricelistversionId, String strmProductId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportPricelistData[] data = null;
    String discard[] = { "discard" };
    if (vars.commandIn("DEFAULT") && strProductCategory.equals("")
        && strPricelistversionId.equals("") && strmProductId.equals("")) {
      discard[0] = "sectionPricelistVersion";
      data = ReportPricelistData.set();
    } else {
      data = ReportPricelistData.select(this,
          Utility.getContext(this, vars, "#User_Client", "ReportPricelist"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportPricelist"),
          strPricelistversionId, strProductCategory, strmProductId);
    }
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportPricelist",
        discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportPricelist", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportPricelist");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportPricelist.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportPricelist.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportPricelist");
      vars.removeMessage("ReportPricelist");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("mProductCategoryId", strProductCategory);
    xmlDocument.setParameter("mPricelistVersionId", strPricelistversionId);

    xmlDocument.setData(
        "reportMProductId_IN",
        "liststructure",
        SelectorUtilityData.selectMproduct(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strmProductId));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportPricelist"),
          Utility.getContext(this, vars, "#User_Client", "ReportPricelist"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportPricelist",
          strProductCategory);
      xmlDocument.setData("reportM_PRODUCT_CATEGORYID", "liststructure",
          comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_PriceList_Version_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportPricelist"),
          Utility.getContext(this, vars, "#User_Client", "ReportPricelist"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportPricelist",
          strPricelistversionId);
      xmlDocument.setData("reportM_PRICELIST_VERSIONID", "liststructure",
          comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structure1", data);

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePdf(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strProductCategory, String strPricelistversionId,
      String strmProductId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print pdf");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportPricelist_Pdf").createXmlDocument();
    xmlDocument.setData("structure1", ReportPricelistData.selectPDF(this,
        Utility.getContext(this, vars, "#User_Client", "ReportPricelist"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportPricelist"),
        strPricelistversionId, strProductCategory, strmProductId));
    String strResult = xmlDocument.print();
    if (log4j.isDebugEnabled())
      log4j.debug(strResult);
    renderFO(strResult, request, response);
  }

  public String getServletInfo() {
    return "Servlet ReportPricelist. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
