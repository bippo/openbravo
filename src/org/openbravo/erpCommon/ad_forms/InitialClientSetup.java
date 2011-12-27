/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.modules.ModuleReferenceDataClientTree;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class InitialClientSetup extends HttpSecureAppServlet {

  private static final Logger log4j = Logger.getLogger(InitialClientSetup.class);
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("OK")) {
      String strModules = vars.getInStringParameter("inpNodes", IsIDFilter.instance);
      StringBuffer strLog = new StringBuffer();
      OBError obeResultado = process(request, response, vars, strModules, strLog);
      log4j.debug("InitialClientSetup - after processFile");
      printPageResult(response, vars, obeResultado, strLog);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    ModuleReferenceDataClientTree tree = new ModuleReferenceDataClientTree(this, true);
    XmlDocument xmlDocument = null;
    String[] discard = { "selEliminar" };
    if (tree.getData() == null || tree.getData().length == 0)
      xmlDocument = xmlEngine
          .readXmlTemplate("org/openbravo/erpCommon/ad_forms/InitialClientSetup")
          .createXmlDocument();
    else
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/InitialClientSetup", discard).createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialClientSetup", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InitialClientSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialClientSetup.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialClientSetup.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    vars.removeMessage("InitialClientSetup");
    OBError myMessage = vars.getMessage("InitialClientSetup");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    xmlDocument.setParameter("moduleTree", tree.toHtml());
    xmlDocument.setParameter("moduleTreeDescription", tree.descriptionToHtml());

    xmlDocument.setData("reportCurrency", "liststructure", MonedaComboData.selectISO(this));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }

  private void printPageResult(HttpServletResponse response, VariablesSecureApp vars,
      OBError obeResult, StringBuffer strLog) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/Resultado").createXmlDocument();
    String strLanguage = vars.getLanguage();

    xmlDocument.setParameter("resultado",
        Utility.parseTranslation(this, vars, strLanguage, strLog.toString()));

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialClientSetup", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InitialClientSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialClientSetup.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialClientSetup.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("messageType", obeResult.getType());
    xmlDocument.setParameter("messageTitle",
        Utility.parseTranslation(this, vars, strLanguage, obeResult.getTitle()));
    xmlDocument.setParameter("messageMessage",
        Utility.parseTranslation(this, vars, strLanguage, obeResult.getMessage()));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private OBError process(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strModules, StringBuffer strLog) throws IOException {

    log4j.debug("process() - Initial Client Setup Process Start - strModules - " + strModules);

    String strClientName = vars.getStringParameter("inpClient");
    String strClientUser = vars.getStringParameter("inpClientUser");
    String strPassword = vars.getStringParameter("inpPassword");
    String strCurrency = vars.getStringParameter("inpCurrency");
    org.apache.commons.fileupload.FileItem fileCoAFilePath = vars.getMultiFile("inpFile");
    boolean bCreateAccounting = isTrue(vars.getStringParameter("inpCreateAccounting"));
    boolean bBPartner = isTrue(vars.getStringParameter("inpBPartner"));
    boolean bProduct = isTrue(vars.getStringParameter("inpProduct"));
    boolean bProject = isTrue(vars.getStringParameter("inpProject"));
    boolean bCampaign = isTrue(vars.getStringParameter("inpCampaign"));
    boolean bSalesRegion = isTrue(vars.getStringParameter("inpSalesRegion"));
    log4j.debug("process() - Client name: " + strClientName + ". Client user name: "
        + strClientUser);

    org.openbravo.erpCommon.businessUtility.InitialClientSetup ics = new org.openbravo.erpCommon.businessUtility.InitialClientSetup();
    OBError obeResult = ics.createClient(vars, strCurrency, strClientName, strClientUser,
        strPassword, strModules, Utility.messageBD(this, "Account_ID", vars.getLanguage()),
        Utility.messageBD(this, "C_Calendar_ID", vars.getLanguage()), bCreateAccounting,
        fileCoAFilePath, bBPartner, bProduct, bProject, bCampaign, bSalesRegion);

    strLog.append(ics.getLog());

    return obeResult;
  }

  private boolean isTrue(String s) {
    if (s == null || s.equals(""))
      return false;
    else
      return true;
  }

}
