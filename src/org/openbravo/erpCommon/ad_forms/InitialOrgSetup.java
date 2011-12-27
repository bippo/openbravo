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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.OrgTree;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.modules.ModuleReferenceDataOrgTree;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class InitialOrgSetup extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("OK")) {
      final String strOrganization = vars.getStringParameter("inpOrganization");
      final String strOrgUser = vars.getStringParameter("inpOrgUser");
      final String strOrgType = vars.getStringParameter("inpOrgType");
      final String strParentOrg = vars.getStringParameter("inpParentOrg");
      final String strcLocationId = vars.getStringParameter("inpcLocationId");
      final String strPassword = vars.getStringParameter("inpPassword");
      final String strCreateAccounting = vars.getStringParameter("inpCreateAccounting");
      org.apache.commons.fileupload.FileItem fileCoAFilePath = vars.getMultiFile("inpFile");
      final String strCurrency = vars.getStringParameter("inpCurrency");
      final boolean bBPartner = isTrue(vars.getStringParameter("inpBPartner"));
      final boolean bProduct = isTrue(vars.getStringParameter("inpProduct"));
      final boolean bProject = isTrue(vars.getStringParameter("inpProject"));
      final boolean bCampaign = isTrue(vars.getStringParameter("inpCampaign"));
      final boolean bSalesRegion = isTrue(vars.getStringParameter("inpSalesRegion"));
      final String strModules = vars.getInStringParameter("inpNodes", IsIDFilter.instance);
      log4j.debug("InitialOrgSetup - Command OK");

      org.openbravo.erpCommon.businessUtility.InitialOrgSetup ios = new org.openbravo.erpCommon.businessUtility.InitialOrgSetup(
          OBContext.getOBContext().getCurrentClient());
      OBError obeResult = ios.createOrganization(strOrganization, strOrgUser, strOrgType,
          strParentOrg, strcLocationId, strPassword, strModules, isTrue(strCreateAccounting),
          fileCoAFilePath, strCurrency, bBPartner, bProduct, bProject, bCampaign, bSalesRegion,
          vars.getSessionValue("#SOURCEPATH"));
      vars.setSessionValue("#USER_ORG", vars.getSessionValue("#USER_ORG") + ", '" + ios.getOrgId()
          + "'");
      vars.setSessionValue("#ORG_CLIENT",
          vars.getSessionValue("#ORG_CLIENT") + ", '" + ios.getOrgId() + "'");
      OrgTree tree = new OrgTree(this, vars.getClient());
      vars.setSessionObject("#CompleteOrgTree", tree);
      OrgTree accessibleTree = tree.getAccessibleTree(this, vars.getRole());
      vars.setSessionValue("#AccessibleOrgTree", accessibleTree.toString());
      printPageResult(response, vars, ios.getLog(), obeResult);
    } else if (vars.commandIn("CANCEL")) {
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    final ModuleReferenceDataOrgTree tree = new ModuleReferenceDataOrgTree(this, vars.getClient(),
        false, true);
    XmlDocument xmlDocument = null;
    final String[] discard = { "selEliminar" };
    if (tree.getData() == null || tree.getData().length == 0)
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/InitialOrgSetup")
          .createXmlDocument();
    else
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/InitialOrgSetup",
          discard).createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialOrgSetup", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InitialOrgSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialOrgSetup.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialOrgSetup.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    {
      vars.removeMessage("InitialOrgSetup");
      final OBError myMessage = vars.getMessage("InitialOrgSetup");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      xmlDocument.setParameter("moduleTree", tree.toHtml());
      xmlDocument.setParameter("moduleTreeDescription", tree.descriptionToHtml());

      xmlDocument.setParameter("paramLocationId", "");
      xmlDocument.setParameter("paramLocationDescription", "");
      xmlDocument.setData("reportCurrency", "liststructure", MonedaComboData.selectISO(this));
      xmlDocument.setData("reportOrgType", "liststructure",
          InitialOrgSetupData.selectOrgType(this, vars.getLanguage(), vars.getClient()));
      xmlDocument.setData("reportParentOrg", "liststructure",
          InitialOrgSetupData.selectParentOrg(this, vars.getLanguage(), vars.getClient()));

      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private static boolean isTrue(String s) {
    if (s == null || s.equals(""))
      return false;
    else
      return true;
  }

  private void printPageResult(HttpServletResponse response, VariablesSecureApp vars,
      String strResult, OBError obeResult) throws IOException, ServletException {
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/Resultado").createXmlDocument();
    String strLanguage = vars.getLanguage();

    xmlDocument.setParameter("resultado",
        Utility.parseTranslation(this, vars, strLanguage, strResult));

    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialOrgSetup", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InitialOrgSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialOrgSetup.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialOrgSetup.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    if (obeResult != null) {
      xmlDocument.setParameter("messageType", obeResult.getType());
      xmlDocument.setParameter("messageTitle", obeResult.getTitle());
      xmlDocument.setParameter("messageMessage",
          Utility.parseTranslation(this, vars, strLanguage, obeResult.getMessage()));
    }
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
