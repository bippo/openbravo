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

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportOffer extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportOffer|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportOffer|dateTo", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN", "ReportOffer|partner",
          "", IsIDFilter.instance);
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportOffer|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportOffer|dateTo", "");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportOffer|partner", IsIDFilter.instance);
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId);
    } else if (vars.commandIn("OPENAJAX")) {
      String strOfferId = vars.getRequiredStringParameter("inpOfferAjax");
      printPageAjaxDocumentResponse(response, vars, strOfferId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strcBpartnerId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = null;

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportOffer")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportOffer", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();

    ReportOfferData[] data = ReportOfferData.select(this,
        Utility.getContext(this, vars, "#User_Client", "ReportOffer"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportOffer"), strDateFrom,
        strDateTo, strcBpartnerId);
    /*
     * SubreportOfferProductData[][] dataProduct = new SubreportOfferProductData[data.length][]; for
     * (int i=0; i<data.length; i++) { dataProduct[i] = SubreportOfferProductData.select(this,
     * Utility.getContext(this, vars, "#User_Client", "ReportOffer"), Utility.getContext(this, vars,
     * "#AccessibleOrgTree", "ReportOffer"), data[i].mOfferId); }
     */
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportOffer");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportOffer.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportOffer.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportOffer");
      vars.removeMessage("ReportOffer");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("today", DateTimeData.today(this));

    xmlDocument.setData(
        "reportCBPartnerId_IN",
        "liststructure",
        SelectorUtilityData.selectBpartner(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportOffer"),
            Utility.getContext(this, vars, "#User_Client", "ReportOffer"), strcBpartnerId));
    xmlDocument.setData("structure1", data);
    // xmlDocument.setDataArray("reportProduct", "structure1" ,
    // dataProduct);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageAjaxDocumentResponse(HttpServletResponse response, VariablesSecureApp vars,
      String strOfferId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajaxreponse");
    XmlDocument xmlDocument = null;

    SubreportOfferProductData[] data = SubreportOfferProductData.select(this,
        Utility.getContext(this, vars, "#User_Client", "ReportOffer"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportOffer"), strOfferId);

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/SubreportOfferProduct").createXmlDocument();

    response.setContentType("text/plain; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportOffer. This Servlet was made by Pablo Sarobe";
  } // end of the getServletInfo() method
}
