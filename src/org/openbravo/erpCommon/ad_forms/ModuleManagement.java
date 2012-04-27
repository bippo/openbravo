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
 * All portions are Copyright (C) 2008-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.OBInterceptor;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.modules.ImportModule;
import org.openbravo.erpCommon.modules.ModuleTree;
import org.openbravo.erpCommon.modules.ModuleUtiltiy;
import org.openbravo.erpCommon.modules.UninstallModule;
import org.openbravo.erpCommon.modules.VersionUtility;
import org.openbravo.erpCommon.modules.VersionUtility.VersionComparator;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.CommercialModuleStatus;
import org.openbravo.erpCommon.obps.ActivationKey.LicenseClass;
import org.openbravo.erpCommon.obps.ActivationKey.SubscriptionStatus;
import org.openbravo.erpCommon.obps.DisabledModules;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.services.webservice.Module;
import org.openbravo.services.webservice.ModuleDependency;
import org.openbravo.services.webservice.SimpleModule;
import org.openbravo.services.webservice.WebService3Impl;
import org.openbravo.services.webservice.WebService3ImplServiceLocator;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * This servlet is in charge of showing the Module Manager Console which have three tabs: *Installed
 * modules *Add Modules *Installation history
 * 
 * 
 */
public class ModuleManagement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  public static final String UPDATE_ALL_RECORD_ID = "FFF";
  private static final String UPGRADE_INFO_URL = "https://butler.openbravo.com/heartbeat-server/org.openbravo.utility.centralrepository/UpgradeInfo";

  /**
   * Main method that controls the sent command
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPageInstalled(response, vars);
    } else if (vars.commandIn("APPLY")) {
      printPageApply(response, vars);
    } else if (vars.commandIn("ADD")) {
      final String searchText = vars.getGlobalVariable("inpSearchText", "ModuleManagemetAdd|text",
          "");
      printPageAdd(request, response, vars, searchText, true);
    } else if (vars.commandIn("ADD_NOSEARCH")) {
      final String searchText = vars.getGlobalVariable("inpSearchText", "ModuleManagemetAdd|text",
          "");
      printPageAdd(request, response, vars, searchText, false);
    } else if (vars.commandIn("ADD_SEARCH")) {
      final String searchText = vars.getRequestGlobalVariable("inpSearchText",
          "ModuleManagemetAdd|text");
      printPageAdd(request, response, vars, searchText, true);
    } else if (vars.commandIn("HISTORY")) {
      final String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ModuleManagement|DateFrom",
          "");
      final String strDateTo = vars.getGlobalVariable("inpDateTo", "ModuleManagement|DateTo", "");
      final String strUser = vars.getGlobalVariable("inpUser", "ModuleManagement|inpUser", "");
      printPageHistory(response, vars, strDateFrom, strDateTo, strUser);
    } else if (vars.commandIn("HISTORY_SEARCH")) {
      final String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ModuleManagement|DateFrom");
      final String strDateTo = vars
          .getRequestGlobalVariable("inpDateTo", "ModuleManagement|DateTo");
      final String strUser = vars.getRequestGlobalVariable("inpUser", "ModuleManagement|inpUser");
      printPageHistory(response, vars, strDateFrom, strDateTo, strUser);
    } else if (vars.commandIn("DETAIL")) {
      final String record = vars.getStringParameter("inpcRecordId");
      final boolean local = vars.getStringParameter("inpLocalInstall").equals("Y");
      printPageDetail(response, vars, record, local);
    } else if (vars.commandIn("INSTALL")) {
      final String record = vars.getStringParameter("inpcRecordId");

      printPageInstall1(response, request, vars, record, false, null, new String[0],
          ModuleUtiltiy.getSystemMaturityLevels(true), null);
    } else if (vars.commandIn("INSTALL2")) {
      printPageInstall2(response, vars);
    } else if (vars.commandIn("INSTALL3")) {
      printPageInstall3(response, vars);
    } else if (vars.commandIn("LICENSE")) {
      final String record = vars.getStringParameter("inpcRecordId");
      printLicenseAgreement(response, vars, record);
    } else if (vars.commandIn("LOCAL")) {
      printSearchFile(response, vars, null);
    } else if (vars.commandIn("INSTALLFILE")) {
      printPageInstallFile(response, request, vars);

    } else if (vars.commandIn("UNINSTALL")) {
      final String modules = vars.getInStringParameter("inpNodes", IsIDFilter.instance);
      final UninstallModule um = new UninstallModule(this, vars.getSessionValue("#sourcePath"),
          vars);
      um.execute(modules);
      OBError msg = um.getOBError();
      vars.setMessage("ModuleManagement|message", msg);
      // clean module updates if there are any
      boolean isCleaned = cleanModulesUpdates();
      if (isCleaned) {
        msg = OBErrorBuilder.buildMessage(msg, "Info",
            Utility.messageBD(this, "ModuleUpdatesRemoved", vars.getLanguage()));
      }
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
      log4j.info(modules);
    } else if (vars.commandIn("DISABLE")) {
      disable(vars);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
    } else if (vars.commandIn("ENABLE")) {
      enable(vars);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
    } else if (vars.commandIn("SCAN")) {
      printScan(response, vars);
    } else if (vars.commandIn("UPDATE")) {
      final String updateModule = vars.getStringParameter("inpcUpdate");
      String[] modulesToUpdate;
      if (updateModule.equals("all")) {
        modulesToUpdate = getUpdateableModules();
      } else {
        modulesToUpdate = new String[1];
        modulesToUpdate[0] = updateModule;
      }

      // For update obtain just update maturity level
      printPageInstall1(response, request, vars, null, false, null, modulesToUpdate,
          ModuleUtiltiy.getSystemMaturityLevels(false), null);
    } else if (vars.commandIn("UPGRADE", "UPGRADE1")) {
      OBContext.setAdminMode();
      try {
        printPageUpgrade(response, request);
      } finally {
        OBContext.restorePreviousMode();
      }
    } else if (vars.commandIn("SETTINGS", "SETTINGS_ADD", "SETTINGS_REMOVE", "SETTINGS_SAVE")) {
      printPageSettings(response, request);
    } else {
      pageError(response);
    }
  }

  /**
   * Show the tab for installed modules, where it is possible to look for updates, uninstall and
   * apply changes-
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPageInstalled(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Installed");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagementInstalled").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");

    // Interface parameters
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.ModuleManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ModuleManagement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    {
      final OBError myMessage = vars.getMessage("ModuleManagement|message");
      vars.removeMessage("ModuleManagement|message");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    // //----
    final ModuleTree tree = new ModuleTree(this);
    tree.setLanguage(vars.getLanguage());
    tree.showNotifications(true);

    String notificationsHTML = "";
    try {
      JSONObject updatesUpgrades = getNotificationsJSON(vars.getLanguage());
      if (updatesUpgrades.has("updatesRebuildHTML")) {
        notificationsHTML = updatesUpgrades.getString("updatesRebuildHTML");
      }
      List<Map<String, String>> upgs = new ArrayList<Map<String, String>>();
      if (updatesUpgrades.has("upgrades")) {
        JSONArray jsonUpgrades = updatesUpgrades.getJSONArray("upgrades");
        for (int i = 0; i < jsonUpgrades.length(); i++) {
          JSONArray versions = jsonUpgrades.getJSONObject(i).getJSONArray("version");
          for (int v = 0; v < versions.length(); v++) {
            Map<String, String> upg = new HashMap<String, String>();
            upg.put("id", ((JSONObject) jsonUpgrades.get(i)).getString("moduleId"));
            upg.put("name", ((JSONObject) jsonUpgrades.get(i)).getString("moduleName") + " "
                + versions.get(v));
            upg.put("version", versions.getString(v));
            upgs.add(upg);
          }
        }
        xmlDocument.setParameter("showUpgrades", "");
      }
      xmlDocument.setData("upgrades", FieldProviderFactory.getFieldProviderArray(upgs));

    } catch (JSONException e) {
      log4j.error("Error getting notifications", e);
    }
    tree.setNotifications(notificationsHTML);

    // Obtains a tree for the installed modules
    xmlDocument.setParameter("moduleTree", tree.toHtml());

    // Obtains a box for display the modules descriptions
    xmlDocument.setParameter("moduleTreeDescription", tree.descriptionToHtml());

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Displays the pop-up to execute an ant task
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPageApply(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    try {
      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/ApplyModule").createXmlDocument();
      final PrintWriter out = response.getWriter();
      response.setContentType("text/html; charset=UTF-8");
      out.println(xmlDocument.print());
      out.close();
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
    }
  }

  /**
   * Returns an HTML with the available notifications which can be: *Unapplied changes: rebuild
   * system *Available updates: install them
   * 
   * @param lang
   * @return
   */
  private JSONObject getNotificationsJSON(String lang) {
    String updatesRebuildHTML = "";

    JSONArray upgrades = new JSONArray();
    try {
      String restartTomcat = ModuleManagementData.selectRestartTomcat(this);
      // Check if last build was done but Tomcat wasn't restarted
      if (!restartTomcat.equals("0")) {
        updatesRebuildHTML = "<a class=\"LabelLink_noicon\" href=\"#\" onclick=\"openServletNewWindow('TOMCAT', false, '../ad_process/ApplyModules.html', 'BUTTON', null, true, 650, 900, null, null, null, null, true);return false;\">"
            + Utility.messageBD(this, "Restart_Tomcat", lang) + "</a>";
      } else {
        // Check for rebuild system
        String total = ModuleManagementData.selectRebuild(this);
        if (!total.equals("0")) {
          updatesRebuildHTML = total
              + "&nbsp;"
              + Utility.messageBD(this, "ApplyModules", lang)
              + ", <a id=\"rebuildNow\" class=\"LabelLink_noicon\" href=\"#\" onclick=\"openServletNewWindow('DEFAULT', false, '../ad_process/ApplyModules.html', 'BUTTON', null, true, 700, 900, null, null, null, null, true);return false;\">"
              + Utility.messageBD(this, "RebuildNow", lang) + "</a>";
        }

        // Check for updates
        String message = "";
        total = ModuleManagementData.selectUpdate(this);
        if (!total.equals("0")) {
          if (!updatesRebuildHTML.isEmpty()) {
            updatesRebuildHTML += "&nbsp;/&nbsp;";
          }
          if (total.equals("1")) {
            message = Utility.messageBD(this, "UpdateAvailable", lang).toLowerCase();
          } else {
            message = Utility.messageBD(this, "UpdatesAvailable", lang);
          }
          updatesRebuildHTML += total
              + "&nbsp;"
              + message
              + "&nbsp;"
              + "<a class=\"LabelLink_noicon\" href=\"#\" onclick=\"installUpdate('all'); return false;\">"
              + Utility.messageBD(this, "InstallUpdatesNow", lang) + "</a>";

        }
      }

      OBCriteria<org.openbravo.model.ad.module.Module> qUpgr = OBDal.getInstance().createCriteria(
          org.openbravo.model.ad.module.Module.class);
      qUpgr.add(Restrictions
          .isNotNull(org.openbravo.model.ad.module.Module.PROPERTY_UPGRADEAVAILABLE));

      for (org.openbravo.model.ad.module.Module upgr : qUpgr.list()) {
        JSONObject upgrade = new JSONObject();
        upgrade.put("moduleId", upgr.getId());
        upgrade.put("version", new JSONArray(upgr.getUpgradeAvailable()));
        upgrade.put("moduleName", upgr.getName());
        upgrades.put(upgrade);
      }

    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
    }

    JSONObject rt = new JSONObject();
    try {
      if (!updatesRebuildHTML.isEmpty()) {
        rt.put("updatesRebuildHTML", updatesRebuildHTML);
      }

      if (upgrades.length() != 0) {
        rt.put("upgrades", upgrades);
      }
    } catch (JSONException e) {
      log4j.error("Error genrating updates notifications", e);
    }
    return rt;
  }

  /**
   * Displays the second tab: Add modules where it is possible to search and install modules
   * remotely or locally
   * 
   * @param request
   * @param response
   * @param vars
   * @param searchText
   * @param displaySearch
   * @throws IOException
   * @throws ServletException
   */
  private void printPageAdd(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String searchText, boolean displaySearch) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Installed");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagementAdd").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    // Interface parameters
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.ModuleManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ModuleManagement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    {
      final OBError myMessage = vars.getMessage("ModuleManagement");
      vars.removeMessage("ModuleManagement");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    // //----

    xmlDocument.setParameter("inpSearchText", searchText);

    // In case the search results must be shown request and display them
    if (displaySearch)
      xmlDocument.setParameter("searchResults",
          getSearchResults(request, response, vars, searchText));

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Displays the third tab "Installation History" with a log of all installation actions
   * 
   * @param response
   * @param vars
   * @param strDateFrom
   * @param strDateTo
   * @param strUser
   * @throws IOException
   * @throws ServletException
   */
  private void printPageHistory(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strUser) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Installed");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagementHistory").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    // Interface parameters
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.ModuleManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ModuleManagement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("inpUser", strUser);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "18", "AD_User_ID", "110", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ModuleManagement"),
          Utility.getContext(this, vars, "#User_Client", "ModuleManagement"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ModuleManagement", strUser);
      xmlDocument.setData("reportUser", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    final ModuleManagementData data[] = ModuleManagementData.selectLog(this, vars.getLanguage(),
        strUser, strDateFrom, strDateTo);
    xmlDocument.setData("detail", data);

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Shows the detail pop-up for a module
   * 
   * @param response
   * @param vars
   * @param recordId
   * @throws IOException
   * @throws ServletException
   */
  private void printPageDetail(HttpServletResponse response, VariablesSecureApp vars,
      String recordId, boolean local) throws IOException, ServletException {
    Module module = null;
    if (!local) {
      try {
        // retrieve the module details from the webservice
        final WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
        final WebService3Impl ws = loc.getWebService3();
        module = ws.moduleDetail(recordId);
      } catch (final Exception e) {
        log4j.error(e.getMessage(), e);
        throw new ServletException(e);
      }
    } else {
      final ImportModule im = (ImportModule) vars.getSessionObject("InstallModule|ImportModule");
      module = im.getModule(recordId);
    }

    final ModuleDependency[] dependencies = module.getDependencies();
    final ModuleDependency[] includes = module.getIncludes();

    final String discard[] = { "", "" };
    if (includes == null || includes.length == 0)
      discard[0] = "includeDiscard";
    if (dependencies == null || dependencies.length == 0)
      discard[1] = "dependDiscard";

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagementDetails", discard).createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("key", recordId);
    xmlDocument.setParameter("type", (module.getType() == null ? "M" : module.getType())
        .equals("M") ? "Module" : module.getType().equals("T") ? "Template" : "Pack");
    xmlDocument.setParameter("moduleName", module.getName());
    xmlDocument.setParameter("moduleVersion", module.getVersionNo());
    xmlDocument.setParameter("description", module.getDescription());
    xmlDocument.setParameter("help", module.getHelp());
    xmlDocument.setParameter("author", module.getAuthor());
    String url = module.getUrl();
    if (url == null || url.equals("")) {
      xmlDocument.setParameter("urlDisplay", "none");
    } else {
      xmlDocument.setParameter("urlLink", getLink(url));
      xmlDocument.setParameter("url", url);
    }
    xmlDocument.setParameter("license",
        Utility.getListValueName("License Type", module.getLicenseType(), vars.getLanguage()));

    if (dependencies != null && dependencies.length > 0) {
      xmlDocument.setData("dependencies", formatDeps4Display(dependencies, vars, this));
    }

    if (includes != null && includes.length > 0) {
      xmlDocument.setData("includes", formatDeps4Display(includes, vars, this));
    }

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String getLink(String url) {
    if (url == null || url.isEmpty()) {
      return "";
    }
    String link = url;
    if (!url.matches("^[a-z]+://.+")) {
      // url without protocol: infer http
      link = "http://" + url;
    }
    return link;
  }

  private static FieldProvider[] formatDeps4Display(ModuleDependency[] deps,
      VariablesSecureApp vars, ConnectionProvider conn) {
    @SuppressWarnings("unchecked")
    HashMap<String, String>[] res = new HashMap[deps.length];

    for (int i = 0; i < deps.length; i++) {
      res[i] = new HashMap<String, String>();
      res[i].put("moduleName", getDisplayString(deps[i], vars, conn));
    }
    return FieldProviderFactory.getFieldProviderArray(res);
  }

  private static String getDisplayString(ModuleDependency dep, VariablesSecureApp vars,
      ConnectionProvider conn) {

    final String DETAIL_MSG_DETAIL_BETWEEN = Utility.messageBD(conn, "MODULE_VERSION_BETWEEN",
        vars.getLanguage());

    final String DETAIL_MSG_OR_LATER = Utility.messageBD(conn, "MODULE_VERSION_OR_LATER",
        vars.getLanguage());

    final String VERSION = Utility.messageBD(conn, "VERSION", vars.getLanguage());

    String displayString = dep.getModuleName() + " " + VERSION + " ";

    if (dep.getVersionEnd() != null && dep.getVersionEnd().equals(dep.getVersionStart())) {
      displayString += dep.getVersionStart();
    } else if (dep.getVersionEnd() == null || dep.getVersionEnd().contains(".999999")) {
      // NOTE: dep.getVersionEnd() is .999999 from CR but null when installing from .obx
      displayString += DETAIL_MSG_OR_LATER.replace("@MODULE_VERSION@", dep.getVersionStart());
    } else {
      String tmp = DETAIL_MSG_DETAIL_BETWEEN.replace("@MIN_VERSION@", dep.getVersionStart());
      tmp = tmp.replace("@MAX_VERSION@", dep.getVersionEnd());
      displayString += tmp;
    }

    return displayString;
  }

  /**
   * A decision needs to be made when executing this method/pop-up:
   * 
   * a. The file is not a .obx file -> Display the search file pop-up again, with an error
   * indicating the file must be a .obx file.
   * 
   * b. The file is an .obx file but no need to update -> Display the same pop-up again with a
   * warning indicating the module is already the most recent version.
   * 
   * b. The .obx file is okay -> redirect to the moduleInstall1 pop-up.
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  private void printPageInstallFile(HttpServletResponse response, HttpServletRequest request,
      VariablesSecureApp vars) throws ServletException, IOException {
    final FileItem fi = vars.getMultiFile("inpFile");

    if (!fi.getName().toUpperCase().endsWith(".OBX")) {
      // We don't have a .obx file
      OBError message = new OBError();
      message.setType("Error");
      message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
      message.setMessage(Utility.messageBD(this, "MOD_OBX", vars.getLanguage()));

      printSearchFile(response, vars, message);

    } else {
      ImportModule im = new ImportModule(this, vars.getSessionValue("#sourcePath"), vars);
      try {
        if (im.isModuleUpdate(fi.getInputStream())) {
          vars.setSessionObject("ModuleManagementInstall|File", vars.getMultiFile("inpFile"));
          printPageInstall1(response, request, vars, null, true, fi.getInputStream(),
              new String[0], null, null);
        } else {
          OBError message = im.getOBError(this);
          printSearchFile(response, vars, message);
        }
      } catch (Exception e) {
        log4j.error(e.getMessage(), e);
        throw new ServletException(e);
      }
    }
  }

  private void printPageUpgrade(HttpServletResponse response, HttpServletRequest request)
      throws IOException, ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    final String moduleId = vars.getStringParameter("inpcUpdate");
    final String version = vars.getStringParameter("upgradeVersion");

    String usingAprm = "Y";

    if (vars.commandIn("UPGRADE")) {

      // Remote upgrade is only allowed for heartbeat enabled instances
      if (!HeartbeatProcess.isHeartbeatEnabled()) {
        String command = "UPGRADE";
        response.sendRedirect(strDireccion + "/ad_forms/Heartbeat.html?Command=" + command
            + "_MODULE&inpcRecordId=" + moduleId + "&version=" + version);
        return;
      }

      // Show information about upgrade
      org.openbravo.model.ad.module.Module mod = OBDal.getInstance().get(
          org.openbravo.model.ad.module.Module.class, moduleId);
      String upgradeName = mod.getName() + " - " + version;

      String upgradeInfo = "";
      try {
        String infoRequest = "Command=info";
        infoRequest += "&modId=" + moduleId;
        infoRequest += "&version=" + version;
        infoRequest += "&lang=" + vars.getLanguage();
        infoRequest += "&aprm=" + usingAprm;
        infoRequest += "&professional=" + (ActivationKey.getInstance().isActive() ? "Y" : "N");

        upgradeInfo = new JSONObject(HttpsUtils.sendSecure(new URL(UPGRADE_INFO_URL), infoRequest))
            .getString("description");
      } catch (Exception e1) {
        log4j.error("Error getting upgrade info", e1);
      }

      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/ModuleManagement_UpgradeInfo",
          new String[] { "updateNeeded" }).createXmlDocument();
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("upgradeName", upgradeName);
      xmlDocument.setParameter("upgradeInfo", upgradeInfo);
      xmlDocument.setParameter("moduleID", moduleId);
      xmlDocument.setParameter("upgradeVersion", version);

      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
      return;
    }

    if (vars.commandIn("UPGRADE1")) {
      // Check the pre-requisites are fulfilled
      String infoRequest = "Command=requirements";
      infoRequest += "&modId=" + moduleId;
      infoRequest += "&version=" + version;
      infoRequest += "&lang=" + vars.getLanguage();
      infoRequest += "&aprm=" + usingAprm;
      infoRequest += "&professional=" + (ActivationKey.getInstance().isActive() ? "Y" : "N");

      try {
        JSONArray requirements = new JSONObject(HttpsUtils.sendSecure(new URL(UPGRADE_INFO_URL),
            infoRequest)).getJSONArray("requirements");

        List<Map<String, String>> requiredUpdates = new ArrayList<Map<String, String>>();

        SystemInformation sys = OBDal.getInstance().get(SystemInformation.class, "0");
        Integer maturityUpdate = Integer.parseInt(sys.getMaturityUpdate());

        for (int i = 0; i < requirements.length(); i++) {
          JSONObject requisit = requirements.getJSONObject(i);
          log4j.debug("Checking upgrade prerequisit " + requisit.toString());

          String modId = requisit.getString("moduleId");
          org.openbravo.model.ad.module.Module mod = OBDal.getInstance().get(
              org.openbravo.model.ad.module.Module.class, modId);
          if (mod == null) {
            log4j.debug("Module is not installed, skipt it");
            continue;
          }

          String requiredVersion = requisit.getString("version");
          String installedVersion = mod.getVersion();
          if (new VersionUtility.VersionComparator().compare(installedVersion, requiredVersion) >= 0) {
            log4j.debug("ok " + installedVersion + ">=" + requiredVersion);
            continue;
          }

          Map<String, String> requiredUpdate = new HashMap<String, String>();
          requiredUpdate.put("id", modId);
          requiredUpdate.put("name", mod.getName());
          requiredUpdate.put("installed", installedVersion);
          requiredUpdate.put("required", requisit.getString("versionName"));

          Integer maximumMaturity = Integer.parseInt(requisit.getString("maturity"));
          Integer acceptedMaturity;
          if (mod.getMaturityUpdate() != null) {
            acceptedMaturity = Integer.parseInt(mod.getMaturityUpdate());
          } else {
            acceptedMaturity = maturityUpdate;
          }

          if (maximumMaturity < acceptedMaturity) {
            requiredUpdate.put("displayMaturity", "block");
            requiredUpdate.put("mat", requisit.getString("maturityName"));
          } else {
            requiredUpdate.put("displayMaturity", "none");
          }
          requiredUpdates.add(requiredUpdate);
        }

        if (!requiredUpdates.isEmpty()) {
          String discard[] = { "buttonContinue", "info" };
          OBError msg = new OBError();
          msg.setType("Warning");
          msg.setTitle(Utility.messageBD(this, "UpgradeRequiresUpdates", vars.getLanguage()));
          msg.setMessage(Utility.messageBD(this, "UpgradeRequiresUpdatesMsg", vars.getLanguage()));

          final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
              "org/openbravo/erpCommon/ad_forms/ModuleManagement_UpgradeInfo", discard)
              .createXmlDocument();

          xmlDocument.setParameter("messageType", msg.getType());
          xmlDocument.setParameter("messageTitle", msg.getTitle());
          xmlDocument.setParameter("messageMessage", msg.getMessage());

          xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith
              + "/\";\n");
          xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
          xmlDocument.setParameter("theme", vars.getTheme());
          xmlDocument.setParameter("moduleID", moduleId);
          xmlDocument.setParameter("upgradeVersion", version);

          xmlDocument.setData("updateNeeded",
              FieldProviderFactory.getFieldProviderArray(requiredUpdates));

          response.setContentType("text/html; charset=UTF-8");
          final PrintWriter out = response.getWriter();
          out.println(xmlDocument.print());
          out.close();
          return;
        }
      } catch (Exception e) {
        log4j.error("Error getting upgrade pre requisites", e);
      }

      // All pre-checks were successful, now start with the upgrade
      try {
        HashMap<String, String> additionalInfo = ModuleUtiltiy.getSystemMaturityLevels(false);
        additionalInfo.put("upgrade.module", moduleId);
        additionalInfo.put("upgrade.version", version);
        additionalInfo.put("upgrade.aprm", usingAprm);
        final ImportModule im = new ImportModule(this, vars.getSessionValue("#sourcePath"), vars);
        im.setInstallLocal(false);
        im.checkDependenciesId(new String[0], new String[0], additionalInfo);
        if (im.isUpgradePrecheckFail()) {
          printPageUpgradeError(response, request, new JSONArray(im.getDependencyErrors()[1]));
        } else {
          // Continue with standard update
          printPageInstall1(response, request, vars, "0", false, null, new String[] { "0" },
              ModuleUtiltiy.getSystemMaturityLevels(false), im);
        }
      } catch (Exception e) {
        log4j.error("Error on upgrade", e);
      }
    }

  }

  private void printPageUpgradeError(HttpServletResponse response, HttpServletRequest request,
      JSONArray upgradeErrors) throws IOException, JSONException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    List<Map<String, String>> deps = new ArrayList<Map<String, String>>();
    List<Map<String, String>> mats = new ArrayList<Map<String, String>>();

    String msgDepGeneric = Utility.messageBD(this, "Dependency", vars.getLanguage());
    String accepetedMaturity;
    HashMap<String, String> maturities = ModuleUtiltiy.getSystemMaturityLevels(true);
    if ("true".equals(maturities.get("isProfessional"))
        && Integer.toString(MaturityLevel.CS_MATURITY).equals(maturities.get("update.level"))) {
      // Community instances use CR as maximum restriction for maturity
      accepetedMaturity = "200";
    } else {
      accepetedMaturity = maturities.get("update.level");
    }

    for (int i = 0; i < upgradeErrors.length(); i++) {
      JSONObject mod = upgradeErrors.getJSONObject(i);

      Map<String, String> error = new HashMap<String, String>();
      String modId = mod.getString("module");
      String name;
      if (!mod.getString("name").isEmpty()) {
        name = mod.getString("name");
      } else {
        org.openbravo.model.ad.module.Module m = OBDal.getInstance().get(
            org.openbravo.model.ad.module.Module.class, modId);
        if (m != null) {
          name = m.getName();
        } else {
          name = modId;
        }
      }

      JSONArray errors = mod.getJSONArray("errors");
      String msg = "";

      boolean isDepError = mod.has("type") && "deps".equals(mod.getString("type"));

      if (isDepError) {
        // dependency error
        String latestVersion = "";
        if (mod.has("latestVersion")) {
          latestVersion = Utility.messageBD(this, "latestVerDep", vars.getLanguage()).replace("%0",
              mod.getString("latestVersion"));
        }
        msg += latestVersion + " ";

        String coreDep = "";
        String otherDeps = "";
        String unknownDeps = "";
        for (int e = 0; e < errors.length(); e++) {
          JSONObject err = errors.getJSONObject(e);
          String code = err.has("errorCode") ? err.getString("errorCode") : "";
          if (code.isEmpty()) {
            log4j.warn("Unknown upgrade error:" + err.toString());
          }

          String dep = "";
          if (err.has("dependency")) {
            JSONObject dependency = err.getJSONObject("dependency");
            String depModName;
            if (dependency.has("moduleName")) {
              depModName = dependency.getString("moduleName");
            } else {
              String depModID = dependency.getString("moduleId");
              org.openbravo.model.ad.module.Module module = OBDal.getInstance().get(
                  org.openbravo.model.ad.module.Module.class, depModID);

              if (module == null) {
                depModName = depModID;
              } else {
                depModName = module.getName();
              }
            }

            String depVersion = dependency.getString("firstVersion")
                + (dependency.has("lastVersion") ? " - " + dependency.getString("lastVersion") : "");
            dep = msgDepGeneric.replace("%0", depModName).replace("%1", depVersion);
            if (!"MAJOR".equals(dependency.getString("enforcement"))) {
              dep += " "
                  + Utility.messageBD(this, err.getString("errorCode"), vars.getLanguage())
                      .replace("%0", dependency.getString("enforcement"));
            }

            if ("No3.0CoreDep".equals(code)) {
              coreDep += dep;
            } else if ("No3.0Dependency".equals(code)) {
              if (!otherDeps.isEmpty()) {
                otherDeps += ", ";
              }
              otherDeps += dep;
            } else {
              String unknownMsg = Utility.messageBD(this, err.getString("errorCode"),
                  vars.getLanguage());
              if (unknownMsg.equals(err.getString("errorCode"))) {
                unknownMsg = err.getString("message");
              } else if (err.has("isMaturity") && err.getBoolean("isMaturity")) {
                unknownMsg = unknownMsg.replace("%0", err.getString("minMaturityRequired"))
                    .replace("%1", err.getString("maxMaturityAvailable"));
              }
              unknownDeps += " " + unknownMsg;
            }
          }
        }
        msg += coreDep;
        if (!otherDeps.isEmpty()) {
          otherDeps = Utility.messageBD(this, "otherDeps", vars.getLanguage()).replace("%0",
              otherDeps);
          if (!coreDep.isEmpty()) {
            msg += " " + Utility.messageBD(this, "And", vars.getLanguage());
          }
          msg += " " + otherDeps;
        }
        if (!unknownDeps.isEmpty()) {
          msg += " " + unknownDeps;
        }
      } else {
        // maturity error
        for (int e = 0; e < errors.length(); e++) {
          JSONObject err = errors.getJSONObject(e);
          String code = err.has("errorCode") ? err.getString("errorCode") : "";
          if (code.isEmpty()) {
            log4j.warn("Unknown upgrade error:" + err.toString());
          }
          String m;
          if ("No3.0DependencyWithMaturity".equals(code)
              && accepetedMaturity.equals(err.getString("minMaturityRequiredCode"))) {
            m = Utility.messageBD(this, "No3.0DependencyWithMaturitySameLevel", vars.getLanguage())
                .replace("%0", err.getString("maxMaturityAvailable"));
          } else {
            m = Utility.messageBD(this, err.getString("errorCode"), vars.getLanguage());
            if (m.equals(err.getString("errorCode"))) {
              m = err.getString("message");
            } else if (err.has("isMaturity") && err.getBoolean("isMaturity")) {
              m = m.replace("%0", err.getString("minMaturityRequired")).replace("%1",
                  err.getString("maxMaturityAvailable"));
            }
          }
          msg += " " + m;
        }
      }

      msg += ".";
      error.put("msg", msg);
      error.put("name", name);

      if (isDepError) {
        deps.add(error);
      } else {
        mats.add(error);
      }

    }

    String discard[] = { deps.isEmpty() ? "deps" : "", mats.isEmpty() ? "maturity" : "" };

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_ErrorUpgrade", discard)
        .createXmlDocument();

    if (!mats.isEmpty()) {
      String level = new MaturityLevel().getLevelName(accepetedMaturity);
      xmlDocument.setParameter("instanceMat", level);
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setData("deps", FieldProviderFactory.getFieldProviderArray(deps));
    xmlDocument.setData("maturity", FieldProviderFactory.getFieldProviderArray(mats));
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }

  /**
   * Shows the first pop-up for the installation process, where it is displayed the modules to
   * install/update and an error message in case it is not possible to install the selected one or a
   * warning message in case the selected version is not installable but it is possible to install
   * another one.
   * 
   */
  private void printPageInstall1(HttpServletResponse response, HttpServletRequest request,
      VariablesSecureApp vars, String recordId, boolean islocal, InputStream obx,
      String[] updateModules, HashMap<String, String> maturityLevels, ImportModule upgradeIM)
      throws IOException, ServletException {
    final String discard[] = { "", "", "", "", "", "", "warnMaturity", "", "missingDeps" };
    Module module = null;

    // Remote installation is only allowed for heartbeat enabled instances
    if (!islocal && !HeartbeatProcess.isHeartbeatEnabled()) {
      String inpcRecordId = recordId;
      String command = "DEFAULT";

      if (updateModules != null && updateModules.length > 0 && !updateModules[0].equals("")) {
        if (updateModules.length == 1) {
          // User clicked "Install Now" from the module description
          inpcRecordId = updateModules[0];
        } else {
          inpcRecordId = UPDATE_ALL_RECORD_ID;
        }
        command = "UPDATE";
      }

      response.sendRedirect(strDireccion + "/ad_forms/Heartbeat.html?Command=" + command
          + "_MODULE&inpcRecordId=" + inpcRecordId);
      return;
    }

    if (upgradeIM != null) {
      for (Module mod : upgradeIM.getModulesToUpdate()) {
        if (mod.getModuleID().equals(ModuleUtiltiy.TEMPLATE_30)) {
          module = mod;
        }
      }
    } else if (!islocal && (updateModules == null || updateModules.length == 0)) {
      // if it is a remote installation get the module from webservice,
      // other case the obx file is passed as an InputStream
      try {
        if (HttpsUtils.isInternetAvailable()) {
          final WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
          final WebService3Impl ws = loc.getWebService3();
          module = ws.moduleDetail(recordId);
        }
      } catch (final Exception e) {
        log4j.error("Error obtaining module info", e);
      }
    } else {
      discard[4] = "core";
    }

    Module[] inst = null;
    Module[] upd = null;
    Module[] merges = null;
    OBError message = null;
    boolean found = false;
    boolean check = false;
    // to hold (key,value) = (moduleId, minVersion)
    Map<String, String> minVersions = new HashMap<String, String>();

    VersionUtility.setPool(this);

    // Create a new ImportModule instance which will be used to check
    // dependencies and to process the installation
    ImportModule im;
    if (upgradeIM == null) {
      im = new ImportModule(this, vars.getSessionValue("#sourcePath"), vars);
    } else {
      im = upgradeIM;
    }
    im.setInstallLocal(islocal);
    try {
      // check the dependencies and obtain the modules to install/update
      if (upgradeIM != null) {
        check = im.isChecked();
      }
      if (!islocal) {
        final String[] installableModules = { module != null ? module.getModuleVersionID() : "" };
        if (upgradeIM == null) {
          check = im.checkDependenciesId(installableModules, updateModules, maturityLevels);
        }
      } else {
        check = im.checkDependenciesFile(obx);
      }

      if (islocal || check) {
        // dependencies are satisfied or local installation, show modules to install. If local
        // installation and dependencies are not ok show warning.

        // installOrig includes also the module to install
        final Module[] installOrig = im.getModulesToInstall();

        if (installOrig == null || installOrig.length == 0)
          discard[0] = "modulesToinstall";
        else {
          if (!islocal && module != null) {
            inst = new Module[installOrig.length - 1]; // to remove
            // the module
            // itself
            // check if the version for the selected module is the
            // selected one
            int j = 0;
            for (int i = 0; i < installOrig.length; i++) {
              found = installOrig[i].getModuleID().equals(module.getModuleID());
              if (found && !module.getModuleVersionID().equals(installOrig[i].getModuleVersionID())) {

                message = new OBError();
                message.setType("Warning");
                message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
                message.setMessage(module.getName() + " " + module.getVersionNo() + " "
                    + Utility.messageBD(this, "OtherModuleVersionToinstall", vars.getLanguage())
                    + " " + installOrig[i].getVersionNo());
              }
              if (found) {
                module = installOrig[i];
              } else {
                inst[j] = installOrig[i];
                j++;
              }

            }
          } else {
            inst = installOrig;
          }
        }
        upd = im.getModulesToUpdate();
        // after all the checks, save the ImportModule object in session
        // to take it in next steps
        vars.setSessionObject("InstallModule|ImportModule", im);

        // calculate minimum required version of each extra module (installs & updates)
        minVersions = calcMinVersions(im);

        if (module == null) {
          // set the selected module for obx installation
          if (installOrig != null && installOrig.length > 0) {
            module = installOrig[0];
          } else {
            Module[] modsToUpdate = im.getModulesToUpdate();
            if (modsToUpdate != null && modsToUpdate.length > 0) {
              module = modsToUpdate[0];
            }
          }
        }
        // check commercial modules and show error page if not allowed to install
        if (!checkCommercialModules(im, minVersions, response, vars, module)) {
          return;
        }

        // Show warning message when installing/updating modules not in General availability level
        if (!islocal) {
          if (module != null
              && !Integer.toString(MaturityLevel.CS_MATURITY).equals(
                  (String) module.getAdditionalInfo().get("maturity.level"))) {
            discard[6] = "";
          } else {
            if (inst != null) {
              for (Module m : inst) {
                if (!Integer.toString(MaturityLevel.CS_MATURITY).equals(
                    (String) m.getAdditionalInfo().get("maturity.level"))) {
                  discard[6] = "";
                }
              }
            }
            if (upd != null) {
              for (Module m : upd) {
                if (!Integer.toString(MaturityLevel.CS_MATURITY).equals(
                    (String) m.getAdditionalInfo().get("maturity.level"))) {
                  discard[6] = "";
                }
              }
            }
          }
        }
        // Add additional messages that can come from CR
        OBError additionalMsg = im.getCheckError();
        if (!additionalMsg.getMessage().isEmpty()) {
          if (message == null) {
            message = new OBError();
            message.setType("Info");
            message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
          } else {
            message.setMessage(message.getMessage() + "\n");
          }

          message.setMessage(message.getMessage() + additionalMsg.getMessage());
          if (!check) {
            discard[8] = ""; // show missing dependencies message
            message.setType("Warning");
            message
                .setTitle(Utility.messageBD(this, "DependenciesNotSatisfied", vars.getLanguage()));
          }
        }

      } else { // Dependencies not satisfied, do not show continue button
        message = im.getCheckError();
        discard[5] = "discardContinue";

        if (message == null || message.getMessage() == null || message.getMessage().isEmpty()) {
          // No message: set generic one
          message = new OBError();
          message.setType("Error");
          message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
          message.setMessage(Utility.messageBD(this, "ModulesNotInstallable", vars.getLanguage()));
        }
      }
      if (upd == null || upd.length == 0)
        discard[1] = "updateModules";
      if (inst == null || inst.length == 0)
        discard[2] = "installModules";
      if ((upd == null || upd.length == 0) && (inst == null || inst.length == 0)
          && (module == null)) {
        discard[3] = "discardAdditional";
        discard[5] = "discardContinue";
      }

      merges = im.getModulesToMerge();
      if (merges == null || merges.length == 0) {
        discard[7] = "mergeModules";
      }
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
      message = new OBError();
      message.setType("Error");
      message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
      message.setMessage(e.toString());
    }

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_InstallP1", discard).createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    if (inst != null && inst.length > 0) {
      xmlDocument.setData("installs",
          getModuleFieldProvider(inst, minVersions, false, vars.getLanguage(), islocal));
    }

    if (upd != null && upd.length > 0) {
      xmlDocument.setData("updates",
          getModuleFieldProvider(upd, minVersions, false, vars.getLanguage(), islocal));
    }

    if (merges != null && merges.length > 0) {
      xmlDocument.setData("merges", getMergesFieldProvider(merges));
    }

    xmlDocument.setParameter("inpLocalInstall", islocal ? "Y" : "N");

    if (!islocal && module != null) {
      xmlDocument.setParameter("key", recordId);
      xmlDocument.setParameter("moduleID", module.getModuleID());
      xmlDocument.setParameter("moduleName", module.getName());
      xmlDocument.setParameter("moduleVersion", module.getVersionNo());
      xmlDocument.setParameter("linkCore", module.getModuleVersionID());

      if (!check
          || Integer.toString(MaturityLevel.CS_MATURITY).equals(
              (String) module.getAdditionalInfo().get("maturity.level"))) {
        xmlDocument.setParameter("maturityStyle", "none");
      } else {
        xmlDocument.setParameter("maturityStyle", "yes");
        xmlDocument.setParameter("maturityLevel",
            (String) module.getAdditionalInfo().get("maturity.name"));
      }
    }
    {
      if (message != null) {
        xmlDocument.setParameter("messageType", message.getType());
        xmlDocument.setParameter("messageTitle", message.getTitle());
        xmlDocument.setParameter("messageMessage", message.getMessage());
      }
    }
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Obtains a FieldProvider to display the merged modules.
   */
  private FieldProvider[] getMergesFieldProvider(Module[] merges) {
    List<Map<String, String>> rt = new ArrayList<Map<String, String>>();

    for (Module merge : merges) {
      Map<String, String> mod = new HashMap<String, String>();
      mod.put("mergedModule", merge.getName());
      mod.put("mergedWith", (String) merge.getAdditionalInfo().get("mergedWith"));
      rt.add(mod);
    }

    return FieldProviderFactory.getFieldProviderArray(rt);
  }

  private FieldProvider[] getModuleFieldProvider(Module[] inst, Map<String, String> minVersions,
      boolean installed, String lang, boolean islocal) {
    ArrayList<HashMap<String, String>> rt = new ArrayList<HashMap<String, String>>();

    for (Module module : inst) {
      HashMap<String, String> mod = new HashMap<String, String>();
      mod.put("name", module.getName());
      mod.put("versionNo", module.getVersionNo());
      mod.put("moduleVersionID", module.getModuleVersionID());

      if (installed) {
        if (minVersions != null && minVersions.get(module.getModuleID()) != null
            && !minVersions.get(module.getModuleID()).equals("")) {
          mod.put("versionNoMin", Utility.messageBD(this, "UpdateModuleNeed", lang) + " "
              + minVersions.get(module.getModuleID()));
        }
        mod.put("versionNoCurr", currentInstalledVersion(module.getModuleID()));
      } else {
        mod.put(
            "versionNoMin",
            (minVersions.get(module.getModuleID()) == null ? module.getVersionNo() : minVersions
                .get(module.getModuleID())));
      }

      if (!islocal) {
        if (Integer.toString(MaturityLevel.CS_MATURITY).equals(
            (String) module.getAdditionalInfo().get("maturity.level"))) {
          mod.put("maturityStyle", "none");
        } else {
          mod.put("maturityStyle", "yes");
          mod.put("maturityLevel", (String) module.getAdditionalInfo().get("maturity.name"));
        }
      }
      rt.add(mod);
    }
    return FieldProviderFactory.getFieldProviderArray(rt);
  }

  private String currentInstalledVersion(String moduleId) {
    String currentVersion = "";
    org.openbravo.model.ad.module.Module mod = OBDal.getInstance().get(
        org.openbravo.model.ad.module.Module.class, moduleId);
    if (mod != null) {
      currentVersion = mod.getVersion();
    }
    return currentVersion;
  }

  /**
   * calculate minimum required version for each module in consistent set of (installs, updates)
   * returned by a checkConsistency call
   * 
   * @param im
   */
  private Map<String, String> calcMinVersions(ImportModule im) {
    // (key,value) = (moduleId, minRequiredVersion)
    Map<String, String> minVersions = new HashMap<String, String>();
    for (Module m : im.getModulesToInstall()) {
      calcMinVersionFromDeps(minVersions, m.getDependencies());
      if (m.getIncludes() != null) {
        calcMinVersionFromDeps(minVersions, m.getIncludes());
      }
    }
    for (Module m : im.getModulesToUpdate()) {
      calcMinVersionFromDeps(minVersions, m.getDependencies());
      if (m.getIncludes() != null) {
        calcMinVersionFromDeps(minVersions, m.getIncludes());
      }
    }

    // check and show:
    for (Module m : im.getModulesToInstall()) {
      log4j.debug("Install module " + m.getName() + " in version " + m.getVersionNo()
          + ", required is version >=" + minVersions.get(m.getModuleID()));
    }
    for (Module m : im.getModulesToUpdate()) {
      log4j.debug("Updating module " + m.getName() + " in version " + m.getVersionNo()
          + ", required is version >=" + minVersions.get(m.getModuleID()));
    }
    return minVersions;
  }

  /**
   * Utility method which processes a list of dependencies and fills a Map of (moduleID, minVersion)
   * 
   * @param minVersions
   *          in/out-parameter with map of (moduleID, minVersion)
   * @param deps
   *          array of dependency entries
   */
  private static void calcMinVersionFromDeps(Map<String, String> minVersions,
      ModuleDependency[] deps) {
    for (ModuleDependency md : deps) {
      String oldMinVersion = minVersions.get(md.getModuleID());
      VersionComparator vc = new VersionComparator();
      if (oldMinVersion == null || vc.compare(oldMinVersion, md.getVersionStart()) < 0) {
        minVersions.put(md.getModuleID(), md.getVersionStart());
      }
    }
  }

  /**
   * Verifies the commercial modules to be installed/updated and shows an error message with the
   * actions to be taken in case some of them cannot be installed due to license restrictions.
   */
  private boolean checkCommercialModules(ImportModule im, Map<String, String> minVersions,
      HttpServletResponse response, VariablesSecureApp vars, Module selectedModule)
      throws IOException {
    // get the list of all commercial modules that are not allowed in this instance
    List<Module> notAllowedModules = getNotAllowedModules(im.getModulesToInstall());
    notAllowedModules.addAll(getNotAllowedModules(im.getModulesToUpdate()));
    if (notAllowedModules.isEmpty()) {
      // all modules are allowed, continue the installation without error
      return true;
    }
    String discard[] = { "", "", "", "", "", "" };
    ActivationKey ak = ActivationKey.getInstance();

    // check if the only module is core as a dependency of another one
    String minCoreVersion = "";
    List<Module> modulesToAcquire = getModulesToAcquire((ArrayList<Module>) notAllowedModules);

    if (notAllowedModules.size() == 1 && !"0".equals(selectedModule.getModuleID())
        && "0".equals(notAllowedModules.get(0).getModuleID())) {
      discard[0] = "OBPSInstance-Canceled";
      discard[1] = "actionList";
      minCoreVersion = minVersions.get("0");
    } else {
      discard[0] = "installCore";
      // check canceled instance
      if (ak.getSubscriptionStatus() == SubscriptionStatus.CANCEL) {
        discard[1] = "actionList";

        // show all commercial modules, not only the ones that should be paid
        modulesToAcquire = notAllowedModules;
      } else {
        discard[1] = "OBPSInstance-Canceled";
      }

      // Decide license type, if any
      int maxTier = getMaxTier(notAllowedModules);
      LicenseClass licenseEdition = ak.getLicenseClass();

      if (!ak.isActive()) {
        if (maxTier == 1) {
          // show subscribe to Basic
          discard[2] = "subscribeSTD";
          discard[3] = "upgradeSTD";
        } else {
          // show subscribe to Standard
          discard[2] = "subscribeBAS";
          discard[3] = "upgradeSTD";
        }
      } else if (licenseEdition == LicenseClass.BASIC) {
        discard[2] = "subscribeBAS";
        discard[3] = "subscribeSTD";
        if (maxTier == 1) {
          // do not show license action
          discard[4] = "upgradeSTD";
        } else {
          // show upgrade to Standard
        }
      } else if (licenseEdition == LicenseClass.STD) {
        // do not show license action
        discard[2] = "subscribeBAS";
        discard[3] = "subscribeSTD";
        discard[4] = "upgradeSTD";
      }
    }

    if (modulesToAcquire.isEmpty()) {
      discard[5] = "acquireModules";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_ErrorCommercial", discard)
        .createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("minCoreVersion", minCoreVersion);
    xmlDocument.setData("notAllowedModules",
        FieldProviderFactory.getFieldProviderArray(modulesToAcquire));
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

    return false;
  }

  /**
   * Returns the list of modules which license must be acquired to be installed in the instance.
   * Currently it only removes core from the list of not allowed modules because it is not needed to
   * acquire a license for the module, just the Professional Subscription. It could be implemented
   * to remove also all the free commercial modules, in case this info came from CR.
   * 
   * @param notAllowedModules
   *          List with all the not allowed commercial modules
   * @return List with all the commercial modules that need a license to be acquired
   */
  @SuppressWarnings("unchecked")
  private List<Module> getModulesToAcquire(ArrayList<Module> notAllowedModules) {
    List<Module> rt = (List<Module>) notAllowedModules.clone();
    for (Module mod : rt) {
      if ("0".equals(mod.getModuleID())) {
        rt.remove(mod);
        break;
      }
    }
    return rt;
  }

  /**
   * Returns the maximum tier of the commercial modules passed as parameter.
   * 
   * @param modulesToCheck
   * @return The maximum tier of the modules (1 or 2)
   */
  private int getMaxTier(List<Module> modulesToCheck) {
    for (Module mod : modulesToCheck) {
      String modTier = (String) mod.getAdditionalInfo().get("tier");
      if ("2".equals(modTier)) {
        return 2;
      }
    }
    return 1;
  }

  /**
   * Checks from the array of commercial modules passed as parameter, which ones are not allowed to
   * be installed in the instance and returns this list.
   * 
   * @param modulesToCheck
   * @return List of modules that cannot be installed in the instance
   */
  private List<Module> getNotAllowedModules(Module[] modulesToCheck) {
    ArrayList<Module> notAllowedModules = new ArrayList<Module>();
    ActivationKey ak = ActivationKey.getInstance();
    for (Module mod : modulesToCheck) {
      if (mod.isIsCommercial()
          && (!ak.isActive() || ak.isModuleSubscribed(mod.getModuleID()) != CommercialModuleStatus.ACTIVE)) {
        notAllowedModules.add(mod);
      }
    }
    return notAllowedModules;
  }

  /**
   * Shows the second installation pup-up with all the license agreements for the modules to
   * install/update
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPageInstall2(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    Module[] inst = null;
    Module[] selected;

    // Obtain the session object with the modules to install/update
    final ImportModule im = (ImportModule) vars.getSessionObject("InstallModule|ImportModule");
    final Module[] installOrig = im.getModulesToInstall();
    final Module[] upd = im.getModulesToUpdate();

    final String adModuleId = vars.getStringParameter("inpModuleID"); // selected
    // module
    // to
    // install
    final boolean islocal = im.getIsLocal();

    if (!islocal) {
      selected = new Module[1];
      inst = new Module[installOrig.length == 0 ? 0 : adModuleId.equals("") ? installOrig.length
          : installOrig.length - 1]; // to
      // remove
      // the
      // module
      // itself
      // check if the version for the selected module is the selected one
      int j = 0;
      for (int i = 0; i < installOrig.length; i++) {
        final boolean found = installOrig[i].getModuleID().equals(adModuleId);
        if (found) {
          selected[0] = installOrig[i];
        } else {
          inst[j] = installOrig[i];
          j++;
        }

      }
    } else {
      if (!im.isChecked()) {
        im.setForce(true);
      }
      selected = installOrig;
    }

    final String discard[] = { "", "", "" };

    if (inst == null || inst.length == 0)
      discard[0] = "moduleIntallation";

    if (upd == null || upd.length == 0)
      discard[1] = "moduleUpdate";

    if (selected == null || selected.length == 0)
      discard[2] = "moduleSelected";

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_InstallP2", discard).createXmlDocument();

    // Set positions to names in order to be able to use keyboard for
    // navigation in the box
    int position = 1;
    if (selected != null && selected.length > 0) {
      final FieldProvider[] fp = FieldProviderFactory.getFieldProviderArray(selected);
      for (int i = 0; i < fp.length; i++)
        FieldProviderFactory.setField(fp[i], "position", new Integer(position++).toString());
      xmlDocument.setData("selected", fp);
    }

    if (inst != null && inst.length > 0) {
      final FieldProvider[] fp = FieldProviderFactory.getFieldProviderArray(inst);
      for (int i = 0; i < fp.length; i++)
        FieldProviderFactory.setField(fp[i], "position", new Integer(position++).toString());
      xmlDocument.setData("installs", fp);
    }

    if (upd != null && upd.length > 0) {
      final FieldProvider[] fp = FieldProviderFactory.getFieldProviderArray(upd);
      for (int i = 0; i < fp.length; i++)
        FieldProviderFactory.setField(fp[i], "position", new Integer(position++).toString());
      xmlDocument.setData("updates", fp);
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Shows the third pup-up for the installation process, in this popup the installation is executed
   * and afterwards a message is displayed with the success or fail information.
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPageInstall3(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    final ImportModule im = (ImportModule) vars.getSessionObject("InstallModule|ImportModule");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_InstallP4").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    OBError message;
    if (im.getIsLocal())
      im.execute(((FileItem) vars.getSessionObject("ModuleManagementInstall|File"))
          .getInputStream());
    else
      im.execute();
    message = im.getOBError(this);

    {
      if (message != null) {
        xmlDocument.setParameter("messageType", message.getType());
        xmlDocument.setParameter("messageTitle", message.getTitle());
        if (message.getType().equalsIgnoreCase("error")) {
          message.setMessage(message.getMessage() + "<br/>"
              + Utility.messageBD(this, "CheckUpdateTips", vars.getLanguage(), false));
        }
        xmlDocument.setParameter("messageMessage", message.getMessage());
      }
    }

    vars.removeSessionValue("ModuleManagementInstall|File");
    vars.removeSessionValue("InstallModule|ImportModule");

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Executes a search query in the web service and returns a HTML with the list of modules
   * retrieved from the query. This list is HTML with styles.
   * 
   * @param request
   * @param response
   * @param vars
   * @param text
   * @return
   */
  private String getSearchResults(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String text) {
    SimpleModule[] modules = null;
    try {
      if (!HttpsUtils.isInternetAvailable()) {
        final OBError message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        message.setMessage(Utility.messageBD(this, "WSError", vars.getLanguage()));
        vars.setMessage("ModuleManagement", message);
        try {
          response.sendRedirect(strDireccion + request.getServletPath() + "?Command=ADD_NOSEARCH");
        } catch (final Exception ex) {
          log4j.error(ex.getMessage(), ex);
        }
      }
      final WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
      final WebService3Impl ws = loc.getWebService3();

      // Stub stub = (javax.xml.rpc.Stub) ws;
      // stub._setProperty(Stub.USERNAME_PROPERTY, "test");
      // stub._setProperty(Stub.PASSWORD_PROPERTY, "1");

      HashMap<String, String> maturitySearch = new HashMap<String, String>();
      maturitySearch.put("search.level", getSystemMaturity(false));
      modules = ws.moduleSearch(text, getInstalledModules(), maturitySearch);

    } catch (final Exception e) {
      final OBError message = new OBError();
      message.setType("Error");
      message.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      message.setMessage(Utility.messageBD(this, "WSError", vars.getLanguage()));
      vars.setMessage("ModuleManagement", message);
      log4j.error("Error searching modules", e);
      try {
        response.sendRedirect(strDireccion + request.getServletPath() + "?Command=ADD_NOSEARCH");
      } catch (final Exception ex) {
        log4j.error("error searching modules", ex);
      }
    }

    FieldProvider[] modulesBox = new FieldProvider[0];
    if (modules != null && modules.length > 0) {
      modulesBox = new FieldProvider[modules.length];
      int i = 0;
      for (SimpleModule mod : modules) {
        HashMap<String, String> moduleBox = new HashMap<String, String>();

        // set different icon depending on module type
        String icon = mod.getType();
        icon = (icon == null ? "M" : icon).equals("M") ? "Module" : icon.equals("T") ? "Template"
            : "Pack";

        moduleBox.put("name", mod.getName());
        moduleBox.put("description", mod.getDescription());
        moduleBox.put("type", icon);
        moduleBox.put("help", mod.getHelp());
        // If there is no url, we need to hide the 'Visit Site' link and separator.
        if (mod.getUrl() == null || mod.getUrl().equals("")) {
          moduleBox.put("urlStyle", "none");
        } else {
          moduleBox.put("url", getLink(mod.getUrl()));
          moduleBox.put("urlStyle", "true");
        }
        moduleBox.put("moduleVersionID", mod.getModuleVersionID());
        moduleBox.put("commercialStyle", (mod.isIsCommercial() ? "true" : "none"));

        @SuppressWarnings("unchecked")
        HashMap<String, String> additioanlInfo = mod.getAdditionalInfo();
        if (additioanlInfo != null
            && !Integer.toString(MaturityLevel.CS_MATURITY).equals(
                additioanlInfo.get("maturity.level"))) {
          // Display module's maturity in case it is not General availability (500)
          moduleBox.put("maturityStyle", "true");
          moduleBox.put("maturityLevel", additioanlInfo.get("maturity.name"));
        } else {
          moduleBox.put("maturityStyle", "none");
        }

        modulesBox[i] = FieldProviderFactory.getFieldProvider(moduleBox);
        i++;
      }
    }
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/modules/ModuleBox").createXmlDocument();

    xmlDocument.setData("structureBox", modulesBox);
    return xmlDocument.print();
  }

  /**
   * Returns String[] with the installed modules, this is used for perform the search in the
   * webservice and not to obtain in the list the already installed ones.
   * 
   * @return
   */
  private String[] getInstalledModules() {
    try {
      final ModuleManagementData data[] = ModuleManagementData.selectInstalled(this);
      if (data != null && data.length != 0) {
        final String[] rt = new String[data.length];
        for (int i = 0; i < data.length; i++)
          rt[i] = data[i].adModuleId;
        return rt;
      } else
        return new String[0];
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
      return (new String[0]);
    }
  }

  private String[] getUpdateableModules() {
    try {
      final ModuleManagementData data[] = ModuleManagementData.selectUpdateable(this);
      if (data != null && data.length != 0) {
        final String[] rt = new String[data.length];
        for (int i = 0; i < data.length; i++)
          rt[i] = data[i].adModuleVersionId;
        return rt;
      } else
        return new String[0];
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
      return (new String[0]);
    }
  }

  /**
   * This ajax call displays the license agreement for a module.
   * 
   * @param response
   * @param vars
   * @param record
   * @throws IOException
   * @throws ServletException
   */
  private void printLicenseAgreement(HttpServletResponse response, VariablesSecureApp vars,
      String record) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajaxreponse");

    response.setContentType("text/plain; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    final PrintWriter out = response.getWriter();
    final ImportModule im = (ImportModule) vars.getSessionObject("InstallModule|ImportModule");
    final Module[] inst = im.getModulesToInstall();
    final Module[] upd = im.getModulesToUpdate();

    int i = 0;
    boolean found = false;
    String agreement = "";
    while (!found && inst != null && i < inst.length) {
      if (found = inst[i].getModuleID().equals(record))
        agreement = inst[i].getLicenseAgreement();
      i++;
    }
    i = 0;
    while (!found && upd != null && i < upd.length) {
      if (found = upd[i].getModuleID().equals(record))
        agreement = upd[i].getLicenseAgreement();
      i++;
    }

    out.println(agreement);
    out.close();

  }

  /**
   * Displays the pop-up for the search locally file in order to look for an obx file and to install
   * it locally.
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printSearchFile(HttpServletResponse response, VariablesSecureApp vars,
      OBError message) throws IOException, ServletException {
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_InstallLocal").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    if (message != null) {
      xmlDocument.setParameter("messageType", message.getType());
      xmlDocument.setParameter("messageTitle", message.getTitle());
      xmlDocument.setParameter("messageMessage", message.getMessage());
    }

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printScan(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajax response ");

    // clean module updates if there are any
    cleanModulesUpdates();

    final HashMap<String, String> updates = ImportModule.scanForUpdates(this, vars);

    JSONObject notifications = new JSONObject();
    try {
      notifications = getNotificationsJSON(vars.getLanguage());
      if (!notifications.has("updatesRebuildHTML")) {
        if (!"".equals(ImportModule.getScanError().toString())) {
          notifications.put("updatesRebuildHTML",
              Utility.messageBD(this, ImportModule.getScanError().toString(), vars.getLanguage()));
        } else {
          notifications = notifications.put("updatesRebuildHTML",
              Utility.messageBD(this, "NoUpdatesAvailable", vars.getLanguage()));
        }
      }

      JSONObject jsonUpdates = new JSONObject(updates);
      notifications.put("updates", jsonUpdates);
    } catch (Exception e) {
      log4j.error("Error reading notifications", e);
    }
    response.setContentType("Content-type: application/json; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    final PrintWriter out = response.getWriter();
    out.println(notifications.toString());
    out.close();
  }

  @SuppressWarnings("unchecked")
  private void printPageSettings(HttpServletResponse response, HttpServletRequest request)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    boolean activeInstance = ActivationKey.getInstance().isActive();

    // Possible maturity levels are obtained from CR, obtain them once per session and store
    MaturityLevel levels = (MaturityLevel) vars.getSessionObject("SettingsModule|MaturityLevels");
    if (levels == null) {
      levels = new MaturityLevel();
      if (!levels.hasInternetError()) {
        vars.setSessionObject("SettingsModule|MaturityLevels", levels);
      }
    }
    String discard[] = { "", "" };
    OBError myMessage = null;

    try {
      OBContext.setAdminMode();
      SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");

      if (vars.commandIn("SETTINGS_ADD", "SETTINGS_REMOVE")) {
        String moduleId;

        if (vars.commandIn("SETTINGS_ADD")) {
          moduleId = vars.getStringParameter("inpModule", IsIDFilter.instance);
        } else {
          moduleId = vars.getStringParameter("inpModuleId", IsIDFilter.instance);
        }

        org.openbravo.model.ad.module.Module mod = OBDal.getInstance().get(
            org.openbravo.model.ad.module.Module.class, moduleId);
        if (mod != null) {
          // do not update the audit info here, as its a local config change, which should not be
          // treated as 'local changes' by i.e. update.database
          try {
            boolean warn = false;
            OBInterceptor.setPreventUpdateInfoChange(true);
            if (vars.commandIn("SETTINGS_ADD")) {
              // GA is not allowed for community instances
              int level = Integer.parseInt(vars.getNumericParameter("inpModuleLevel"));
              if (!activeInstance && level >= MaturityLevel.CS_MATURITY) {
                myMessage = OBErrorBuilder.buildMessage(
                    myMessage,
                    "Warning",
                    Utility.messageBD(this, "OBUIAPP_GAinCommunity", vars.getLanguage()).replace(
                        "%0", levels.getLevelName(Integer.toString(level))));

                warn = true;
              } else {
                mod.setMaturityUpdate(Integer.toString(level));
              }

            } else {
              mod.setMaturityUpdate(null);
            }
            OBDal.getInstance().flush();
            OBDal.getInstance().commitAndClose();

            // clean module updates if there are any
            if (!warn) {
              boolean isCleaned = cleanModulesUpdates();
              if (isCleaned) {
                myMessage = OBErrorBuilder.buildMessage(myMessage, "Info",
                    Utility.messageBD(this, "ModuleUpdatesRemoved", vars.getLanguage()));
              }
              myMessage = OBErrorBuilder.buildMessage(myMessage, "Success",
                  Utility.messageBD(this, "ModuleManagementSettingSaved", vars.getLanguage()));
            }
          } finally {
            OBInterceptor.setPreventUpdateInfoChange(false);
          }
        } else {
          log4j.error("Module does not exists ID:" + moduleId);
        }
      } else if (vars.commandIn("SETTINGS_SAVE")) {

        boolean warn = false;

        // Save global maturity levels. GA is not allowed for community instances
        String maturityWarnMsg = "";
        try {
          int maturitySearch = Integer.parseInt(vars.getNumericParameter("inpSearchLevel"));
          if (!activeInstance && maturitySearch >= MaturityLevel.CS_MATURITY) {
            maturityWarnMsg = Utility.messageBD(this, "OBUIAPP_GAinCommunity", vars.getLanguage())
                .replace("%0", levels.getLevelName(Integer.toString(maturitySearch)));
            warn = true;
          } else {
            sysInfo.setMaturitySearch(Integer.toString(maturitySearch));
          }

          int maturityScan = Integer.parseInt(vars.getNumericParameter("inpScanLevel"));
          if (!activeInstance && maturityScan >= MaturityLevel.CS_MATURITY) {
            if (maturityWarnMsg.isEmpty()) {
              maturityWarnMsg += Utility.messageBD(this, "OBUIAPP_GAinCommunity",
                  vars.getLanguage()).replace("%0",
                  levels.getLevelName(Integer.toString(maturityScan)));
            }
            warn = true;
          } else {
            sysInfo.setMaturityUpdate(Integer.toString(maturityScan));
          }
        } catch (Exception e) {
          log4j.error("Error reading maturity search", e);
        }

        // Save enforcement
        String warnMsg = "";
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
          String parameter = (String) e.nextElement();
          if (parameter.startsWith("inpEnforcement")) {
            String depId = parameter.replace("inpEnforcement", "");
            String value = vars.getStringParameter(parameter);
            org.openbravo.model.ad.module.ModuleDependency dep = OBDal.getInstance().get(
                org.openbravo.model.ad.module.ModuleDependency.class, depId);
            if (dep != null) {
              boolean save = true;
              if ("MINOR".equals(value)) {
                // Setting Minor version enforcement, check the configuration is still valid
                VersionComparator vc = new VersionComparator();
                if (dep.getLastVersion() == null
                    && vc.compare(dep.getFirstVersion(), dep.getDependentModule().getVersion()) != 0) {
                  save = false;
                  warn = true;
                  warnMsg += "<br/>"
                      + Utility.messageBD(this, "ModuleDependsButInstalled", vars.getLanguage())
                          .replace("@module@", dep.getDependentModule().getName())
                          .replace("@version@", dep.getFirstVersion())
                          .replace("@installed@", dep.getDependentModule().getVersion());
                } else if (dep.getLastVersion() != null
                    && !(vc.compare(dep.getFirstVersion(), dep.getDependentModule().getVersion()) <= 0 && vc
                        .compare(dep.getLastVersion(), dep.getDependentModule().getVersion()) >= 0)) {
                  save = false;
                  warn = true;
                  warnMsg += "<br/>"
                      + Utility
                          .messageBD(this, "ModuleDependsButInstalled", vars.getLanguage())
                          .replace("@module@", dep.getDependentModule().getName())
                          .replace("@version@",
                              dep.getFirstVersion() + " - " + dep.getLastVersion())
                          .replace("@installed@", dep.getDependentModule().getVersion());
                }
              }
              if (save) {
                if (value.equals(dep.getDependencyEnforcement())) {
                  // setting no instance enforcement in case the selected value is the default
                  dep.setInstanceEnforcement(null);
                } else {
                  dep.setInstanceEnforcement(value);
                }
              }
            }
          }
        }

        // clean module updates if there are any
        final boolean isCleaned = cleanModulesUpdates();
        if (isCleaned) {
          myMessage = OBErrorBuilder.buildMessage(myMessage, "Info",
              Utility.messageBD(this, "ModuleUpdatesRemoved", vars.getLanguage()));
        }

        if (warn) {
          String msgBody = "";
          if (!maturityWarnMsg.isEmpty()) {
            msgBody += maturityWarnMsg;
          }

          if (!warnMsg.isEmpty()) {
            msgBody += "<br/>"
                + Utility.messageBD(this, "CannotSetMinorEnforcements", vars.getLanguage())
                + warnMsg;
          }

          myMessage = OBErrorBuilder.buildMessage(myMessage, "Warning", msgBody);
        } else {
          myMessage = OBErrorBuilder.buildMessage(myMessage, "Success",
              Utility.messageBD(this, "ModuleManagementSettingSaved", vars.getLanguage()));
        }
      }

      // Populate module specific grid
      OBCriteria<org.openbravo.model.ad.module.Module> qModuleSpecific = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.module.Module.class);
      qModuleSpecific.add(Restrictions
          .isNotNull(org.openbravo.model.ad.module.Module.PROPERTY_MATURITYUPDATE));
      qModuleSpecific.addOrder(Order.asc(org.openbravo.model.ad.module.Module.PROPERTY_NAME));
      ArrayList<HashMap<String, String>> moduleSpecifics = new ArrayList<HashMap<String, String>>();
      List<org.openbravo.model.ad.module.Module> moduleSpecificList = qModuleSpecific.list();
      if (moduleSpecificList.isEmpty()) {
        discard[0] = "moduleTable";
      }
      for (org.openbravo.model.ad.module.Module module : moduleSpecificList) {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("id", module.getId());
        m.put("name", module.getName());

        if (!activeInstance
            && Integer.parseInt(module.getMaturityUpdate()) >= MaturityLevel.CS_MATURITY) {
          m.put("level", levels.getLevelName(Integer.toString(MaturityLevel.QA_APPR_MATURITY)));
        } else {
          m.put("level", levels.getLevelName(module.getMaturityUpdate()));
        }
        moduleSpecifics.add(m);
      }

      // Populate combo of modules without specific setting
      OBCriteria<org.openbravo.model.ad.module.Module> qModule = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.module.Module.class);
      qModule
          .add(Restrictions.isNull(org.openbravo.model.ad.module.Module.PROPERTY_MATURITYUPDATE));
      qModule.addOrder(Order.asc(org.openbravo.model.ad.module.Module.PROPERTY_NAME));

      ArrayList<HashMap<String, String>> modules = new ArrayList<HashMap<String, String>>();
      List<org.openbravo.model.ad.module.Module> moduleList = qModule.list();
      if (moduleList.isEmpty()) {
        discard[0] = "assignModule";
      }
      for (org.openbravo.model.ad.module.Module module : moduleList) {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("id", module.getId());
        m.put("name", module.getName());
        modules.add(m);
      }

      // Dependencies table
      OBCriteria<org.openbravo.model.ad.module.ModuleDependency> qDeps = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.module.ModuleDependency.class);
      qDeps.add(Restrictions.eq(
          org.openbravo.model.ad.module.ModuleDependency.PROPERTY_USEREDITABLEENFORCEMENT, true));
      qDeps.addOrder(Order.asc(org.openbravo.model.ad.module.ModuleDependency.PROPERTY_MODULE));
      qDeps.addOrder(Order.asc(org.openbravo.model.ad.module.ModuleDependency.PROPERTY_ISINCLUDED));
      qDeps.addOrder(Order
          .asc(org.openbravo.model.ad.module.ModuleDependency.PROPERTY_DEPENDANTMODULENAME));
      List<org.openbravo.model.ad.module.ModuleDependency> deps = qDeps.list();

      if (deps.isEmpty()) {
        discard[1] = "enforcementTable";
      } else {
        discard[1] = "noEditableEnforcement";
      }

      FieldProvider fpDeps[] = new FieldProvider[deps.size()];
      FieldProvider fpEnforcements[][] = new FieldProvider[deps.size()][];
      int i = 0;
      String lastName = "";
      Boolean lastType = null;

      // Get the static text values once, not to query db each time for them
      OBCriteria<org.openbravo.model.ad.domain.List> qList = OBDal.getInstance().createCriteria(
          org.openbravo.model.ad.domain.List.class);
      qList.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE + ".id",
          "8BA0A3775CE14CE69989B6C09982FB2E"));
      qList.addOrder(Order.asc(org.openbravo.model.ad.domain.List.PROPERTY_SEQUENCENUMBER));
      SQLReturnObject[] fpEnforcementCombo = new SQLReturnObject[qList.list().size()];
      for (org.openbravo.model.ad.domain.List value : qList.list()) {
        SQLReturnObject val = new SQLReturnObject();
        val.setData("ID", value.getSearchKey());
        val.setData(
            "NAME",
            Utility.getListValueName("Dependency Enforcement", value.getSearchKey(),
                vars.getLanguage()));
        fpEnforcementCombo[i] = val;
        i++;
      }
      String inclusionType = Utility.messageBD(this, "InclusionType", vars.getLanguage());
      String dependencyType = Utility.messageBD(this, "DependencyType", vars.getLanguage());
      String defaultStr = Utility.messageBD(this, "Default", vars.getLanguage());

      i = 0;
      for (org.openbravo.model.ad.module.ModuleDependency dep : deps) {
        HashMap<String, String> d = new HashMap<String, String>();

        d.put("baseModule", dep.getDependentModule().getName());
        d.put("currentVersion", dep.getDependentModule().getVersion());
        d.put("firstVersion", dep.getFirstVersion());
        d.put("lastVersion", dep.getLastVersion());
        d.put("depId", dep.getId());

        // Grouping by module and dependency
        String currentName = dep.getModule().getName();
        Boolean currentType = dep.isIncluded();
        if (lastName.equals(currentName)) {
          d.put("modName", "");
          if (!currentType.equals(lastType)) {
            d.put("depType", dep.isIncluded() ? inclusionType : dependencyType);
          } else {
            d.put("depType", "");
          }
        } else {
          d.put("modName", currentName);
          d.put("depType", dep.isIncluded() ? inclusionType : dependencyType);
          lastName = currentName;
          lastType = currentType;
        }

        d.put(
            "selectedEnforcement",
            dep.getInstanceEnforcement() == null ? dep.getDependencyEnforcement() : dep
                .getInstanceEnforcement());
        fpDeps[i] = FieldProviderFactory.getFieldProvider(d);
        fpEnforcements[i] = getEnforcementCombo(dep, fpEnforcementCombo, defaultStr);
        i++;
      }

      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/ModuleManagementSettings", discard).createXmlDocument();

      xmlDocument.setData("moduleDetail",
          FieldProviderFactory.getFieldProviderArray(moduleSpecifics));
      xmlDocument.setData("moduleCombo", FieldProviderFactory.getFieldProviderArray(modules));

      // Populate maturity levels combos
      String selectedScanLevel;
      String selectedSearchLevel;

      if (activeInstance) {
        selectedScanLevel = sysInfo.getMaturityUpdate() == null ? Integer
            .toString(MaturityLevel.CS_MATURITY) : sysInfo.getMaturityUpdate();
        selectedSearchLevel = sysInfo.getMaturitySearch() == null ? Integer
            .toString(MaturityLevel.CS_MATURITY) : sysInfo.getMaturitySearch();
      } else {
        // Community instances cannot use GA, setting CR if it is used
        int actualScanLevel = sysInfo.getMaturityUpdate() == null ? MaturityLevel.QA_APPR_MATURITY
            : Integer.parseInt(sysInfo.getMaturityUpdate());
        int actualSearchLevel = sysInfo.getMaturitySearch() == null ? MaturityLevel.QA_APPR_MATURITY
            : Integer.parseInt(sysInfo.getMaturitySearch());

        if (actualScanLevel >= MaturityLevel.CS_MATURITY) {
          actualScanLevel = MaturityLevel.QA_APPR_MATURITY;
        }
        if (actualSearchLevel >= MaturityLevel.CS_MATURITY) {
          actualSearchLevel = MaturityLevel.QA_APPR_MATURITY;
        }
        selectedScanLevel = Integer.toString(actualScanLevel);
        selectedSearchLevel = Integer.toString(actualSearchLevel);
      }

      xmlDocument.setParameter("selectedScanLevel", selectedScanLevel);
      xmlDocument.setData("reportScanLevel", "liststructure", levels.getCombo());
      xmlDocument.setParameter("selectedSearchLevel", selectedSearchLevel);
      xmlDocument.setData("reportSearchLevel", "liststructure", levels.getCombo());
      xmlDocument.setData("reportModuleLevel", "liststructure", levels.getCombo());

      // less and most mature values
      xmlDocument.setParameter("lessMature", levels.getLessMature());
      xmlDocument.setParameter("mostMature", levels.getMostMature());

      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");

      xmlDocument.setData("dependencyDetail", fpDeps);
      xmlDocument.setDataArray("reportEnforcementType", "liststructure", fpEnforcements);

      // Interface parameters
      final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
          "", "", false, "ad_forms", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        final WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_forms.ModuleManagement");
        xmlDocument.setParameter("theme", vars.getTheme());
        final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ModuleManagement.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (final Exception ex) {
        throw new ServletException(ex);
      }

      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      out.println(xmlDocument.print());
      out.close();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Checks whether there are any updates available for the installed modules and cleans the update
   * info if so.
   */
  private boolean cleanModulesUpdates() throws ServletException {
    boolean hasChanged = false;

    if (ModuleManagementData.selectUpdateable(this).length > 0) {
      // cleaning modules updates
      ModuleManagementData.cleanModulesUpdates(this);
      hasChanged = true;
    }

    // cleaning module upgrades
    OBCriteria<org.openbravo.model.ad.module.Module> qUpgr = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.module.Module.class);
    qUpgr.add(Restrictions
        .isNotNull(org.openbravo.model.ad.module.Module.PROPERTY_UPGRADEAVAILABLE));
    try {
      OBInterceptor.setPreventUpdateInfoChange(true);
      for (org.openbravo.model.ad.module.Module mod : qUpgr.list()) {
        mod.setUpgradeAvailable(null);
        hasChanged = true;
      }
      OBDal.getInstance().flush();
      try {
        // A commit is necessary to avoid a lock which can happen because xsql statements are being
        // executed (but not committed yet) in parallel with DAL statements over the same tables
        // (see issue 18697)
        OBDal.getInstance().getConnection().commit();
      } catch (SQLException e) {
        // Do nothing, this will not happen
      }
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
    return hasChanged;
  }

  /**
   * Obtains the combo used for enforcement, showing which is the default setting.
   */
  private FieldProvider[] getEnforcementCombo(org.openbravo.model.ad.module.ModuleDependency dep,
      SQLReturnObject[] fpEnforcementCombo, String defaultStr) {
    SQLReturnObject[] rt = new SQLReturnObject[fpEnforcementCombo.length];

    int i = 0;
    for (SQLReturnObject val : fpEnforcementCombo) {
      rt[i] = new SQLReturnObject();
      rt[i].setData("ID", val.getData("ID"));
      if (val.getData("ID").equals(dep.getDependencyEnforcement())) {
        rt[i].setData("NAME", val.getData("NAME") + " " + defaultStr);
      } else {
        rt[i].setData("NAME", val.getData("NAME"));
      }
      i++;
    }
    return rt;
  }

  private String getSystemMaturity(boolean updateLevel) {
    try {
      OBContext.setAdminMode();
      SystemInformation sys = OBDal.getInstance().get(SystemInformation.class, "0");
      String maturity;

      boolean activeInstance = ActivationKey.getInstance().isActive();
      if (updateLevel) {
        maturity = sys.getMaturityUpdate();
      } else {
        maturity = sys.getMaturitySearch();
      }

      if (!activeInstance && maturity != null
          && Integer.parseInt(maturity) >= MaturityLevel.CS_MATURITY) {
        maturity = Integer.toString(MaturityLevel.QA_APPR_MATURITY);
      }
      return maturity;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Enable the passed in module
   */
  private void enable(VariablesSecureApp vars) throws ServletException {
    try {
      OBInterceptor.setPreventUpdateInfoChange(true);
      ArrayList<String> notEnabledModules = new ArrayList<String>();
      enableDisableModule(
          OBDal.getInstance().get(org.openbravo.model.ad.module.Module.class,
              vars.getStringParameter("inpcRecordId")), true, notEnabledModules);
      finishEnabling(notEnabledModules, vars);
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
  }

  /**
   * Disables all the selected modules
   */
  private void disable(VariablesSecureApp vars) throws ServletException {
    String modules = vars.getInStringParameter("inpNodes", IsIDFilter.instance);

    // check if disabling core
    if (modules.contains("'0'")) {
      OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage("Cannot disable core");
      vars.setMessage("ModuleManagement|message", msg);
      return;
    }
    String[] moduleIds = modules.replace("(", "").replace(")", "").replace(" ", "")
        .replace("'", "").split(",");
    ArrayList<String> notEnabledModules = new ArrayList<String>();
    try {
      OBInterceptor.setPreventUpdateInfoChange(true);

      for (String moduleId : moduleIds) {
        org.openbravo.model.ad.module.Module module = OBDal.getInstance().get(
            org.openbravo.model.ad.module.Module.class, moduleId);
        enableDisableModule(module, false, notEnabledModules);
      }

      finishEnabling(notEnabledModules, vars);
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
  }

  /**
   * Enables or disables the module passed as parameter. In case it has other modules has inclusions
   * they are disabled/enabled recursively.
   * 
   * @param module
   *          Module to enable or disable
   * @param enable
   *          If true, the module will be enabled, if false, it will be disabled
   * @param notEnabledModules
   *          List of modules that couldn't be enabled because they are commercial and are not part
   *          of the instance license's subscribed modules
   */
  private void enableDisableModule(org.openbravo.model.ad.module.Module module, boolean enable,
      List<String> notEnabledModules) {
    if (module == null) {
      return;
    }

    if (enable && !ActivationKey.getInstance().isModuleEnableable(module)) {
      log4j.warn("Cannot enable not subscribed commercial module " + module);
      notEnabledModules.add(module.getName());
    } else {
      log4j.info((enable ? "Enabling " : "Disabling ") + module.getName());
      module.setEnabled(enable);
    }
    if ("M".equals(module.getType())) {
      // Standard modules do not have inclusions
      return;
    }

    // For packs and templates enable/disable recursively all inclusions
    for (org.openbravo.model.ad.module.ModuleDependency dependency : module
        .getModuleDependencyList()) {
      if (dependency.isIncluded()) {
        enableDisableModule(dependency.getDependentModule(), enable, notEnabledModules);
      }
    }
  }

  /**
   * Finishes the enabling/disabling process. The actions it performs are:
   * <ul>
   * <li>In case the are no modules that couldn't be enabled, it reloads the disabled modules in
   * memory.
   * <li>If any module coudn't no be enabled, shows an error message and rolls back the transaction
   * </ul>
   */
  private void finishEnabling(List<String> notEnabledModules, VariablesSecureApp vars) {
    if (notEnabledModules == null || notEnabledModules.isEmpty()) {
      OBDal.getInstance().flush();
      DisabledModules.reload();
      return;
    }
    String msg = Utility.messageBD(this, "CannotEnableNonSubscribedModules", vars.getLanguage());
    for (String module : notEnabledModules) {
      msg += "<br/>" + module;
    }
    OBError err = new OBError();
    err.setType("Error");
    err.setMessage(msg);
    vars.setMessage("ModuleManagement|message", err);
    OBDal.getInstance().rollbackAndClose();
  }
}
