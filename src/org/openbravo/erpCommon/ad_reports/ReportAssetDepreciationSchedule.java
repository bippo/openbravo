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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
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
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportAssetDepreciationSchedule extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportAssetDepreciationSchedule|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportAssetDepreciationSchedule|DateTo", "");
      String strValue = vars.getStringParameter("inpValue", "");
      String strDescription = vars.getStringParameter("inpDescription", "");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "");
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId", "");
      String strcAssetCategoryId = vars.getRequestGlobalVariable("inpcAssetCategoryId", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strValue, strDescription,
          strcAssetCategoryId, strcAcctSchemaId, strOrg);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportAssetDepreciationSchedule|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportAssetDepreciationSchedule|DateTo");
      String strValue = vars.getStringParameter("inpValue", "");
      String strDescription = vars.getStringParameter("inpDescription", "");
      String strOrg = vars
          .getRequestGlobalVariable("inpOrg", "ReportAssetDepreciationSchedule|Org");
      String strcAssetCategoryId = vars.getRequestGlobalVariable("inpcAssetCategoryId",
          "ReportAssetDepreciationSchedule|cAssetCategoryId");
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportAssetDepreciationSchedule|cAcctSchemaId");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strValue, strDescription,
          strcAssetCategoryId, strcAcctSchemaId, strOrg);
    } else if (vars.commandIn("PRINT_PDF")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportAssetDepreciationSchedule|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportAssetDepreciationSchedule|DateTo", "");
      String strValue = vars.getStringParameter("inpValue", "");
      String strDescription = vars.getStringParameter("inpDescription", "");
      String strOrg = vars
          .getRequestGlobalVariable("inpOrg", "ReportAssetDepreciationSchedule|Org");
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportAssetDepreciationSchedule|cAcctSchemaId");
      String strcAssetCategoryId = vars.getRequestGlobalVariable("inpcAssetCategoryId",
          "ReportAssetDepreciationSchedule|cAssetCategoryId");
      printPageDataPdf(response, vars, strDateFrom, strDateTo, strValue, strDescription,
          strcAssetCategoryId, strcAcctSchemaId, strOrg);
    } else
      pageError(response);
  } // end of the doPost() method

  /*
   * printPageDataSheet method is to generate the HTML version of the Asset Report for Depreciation
   * schedule
   */

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strValue, String strDescription,
      String strcAssetCategoryId, String strcAcctSchemaId, String strOrg) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "sectionAssetReport" };
    XmlDocument xmlDocument = null;
    ReportAssetDepreciationScheduleData[] data = null;
    if (strOrg.equals("")) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportAssetDepreciationSchedule", discard)
          .createXmlDocument();
      data = ReportAssetDepreciationScheduleData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportAssetDepreciationSchedule").createXmlDocument();
      data = ReportAssetDepreciationScheduleData.select(this, vars.getClient(), strDateFrom,
          strDateTo, strValue, strDescription, strcAssetCategoryId, strcAcctSchemaId,
          Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg));
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportAssetDepreciationSchedule",
        false, "", "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportAssetDepreciationSchedule");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportAssetDepreciationSchedule.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportAssetDepreciationSchedule.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportAssetDepreciationSchedule");
      vars.removeMessage("ReportAssetDepreciationSchedule");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "A_Asset_Group_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
          Utility.getContext(this, vars, "#User_Client", ""), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "", "");
      xmlDocument.setData("reportA_ASSET_GROUP_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("value", strValue);
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("cAssetCategoryId", strcAssetCategoryId);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setData("structure1", data);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportAssetDepreciationSchedule"), Utility.getContext(this, vars, "#User_Client",
              "ReportAssetDepreciationSchedule"), '*');
      comboTableData.fillParameters(null, "ReportAssetDepreciationSchedule", "");
      xmlDocument.setData("reportAD_ORG_ID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument
        .setData("reportC_ACCTSCHEMA_ID", "liststructure", AccountingSchemaMiscData
            .selectC_ACCTSCHEMA_ID(this, Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportAssetDepreciationSchedule"), Utility.getContext(this, vars, "#User_Client",
                "ReportAssetDepreciationSchedule"), strcAcctSchemaId));

    out.println(xmlDocument.print());
    out.close();
  } // end of the printPageDataSheet() method

  /*
   * printPageDataPdf method is to generate the PDF version of the Asset Report for Depreciation
   * schedule
   */

  private void printPageDataPdf(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strValue, String strDescription,
      String strcAssetCategoryId, String strcAcctSchemaId, String strOrg) throws IOException,
      ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");

    ReportAssetDepreciationScheduleData[] pdfData = null;

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    pdfData = ReportAssetDepreciationScheduleData.select(this, vars.getClient(), strDateFrom,
        strDateTo, strValue, strDescription, strcAssetCategoryId, strcAcctSchemaId,
        Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg));
    String strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportAssetDepreciationSchedule.jrxml";
    renderJR(vars, response, strReportPath, "pdf", parameters, pdfData, null);
  } // end of the printPageDataPdf() method

}
