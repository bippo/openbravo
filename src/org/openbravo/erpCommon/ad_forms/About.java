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

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.modules.ModuleTreeData;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBVersion;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class About extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPageDataSheet(response, vars);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    OBContext.setAdminMode();
    try {
      ActivationKey ak = ActivationKey.getInstance();
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      String discard[] = { "" };
      XmlDocument xmlDocument = null;

      String licenseInfo = "";
      if (ActivationKey.isActiveInstance()) {
        String edition = "";
        if (ak.isTrial()) {
          edition = Utility.messageBD(this, "OPSTrial", vars.getLanguage()) + " - ";
        }
        edition += Utility.getListValueName("OBPSLicenseEdition", ak.getLicenseClass().getCode(),
            vars.getLanguage());

        licenseInfo = Utility.messageBD(this, "OPSLicensedTo", vars.getLanguage()).replace(
            "@OBPSEdition@", edition)
            + " " + ak.getProperty("customer");
      } else {
        licenseInfo = Utility.messageBD(this, "OPSCommunityEdition", vars.getLanguage());
        discard[0] = "paramOPSInfo";
      }
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/About", discard)
          .createXmlDocument();

      OBVersion version = OBVersion.getInstance();

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("paramLicensedTo", licenseInfo);
      xmlDocument.setParameter("ver", version.getMajorVersion() + " " + version.getMP());
      xmlDocument.setParameter("versionId", version.getVersionId());
      xmlDocument.setParameter("versionNo", version.getVersionNumber());
      xmlDocument.setData("installedModules", getInstalledModules());

      if (ActivationKey.isActiveInstance()) {
        xmlDocument.setParameter("paraOPSPurpose", ak.getPurpose(vars.getLanguage()));
        xmlDocument.setParameter("paraOPSType", ak.getLicenseExplanation(this, vars.getLanguage()));
        xmlDocument.setParameter("paraOPSWSAccess", ak.getWSExplanation(this, vars.getLanguage()));
        xmlDocument.setParameter("paraOBPSStatus",
            ak.getSubscriptionStatus().getStatusName(vars.getLanguage()));
      }

      out.println(xmlDocument.print());
      out.close();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * Obtains a list of all modules installed in the instance.
   * 
   * @return
   */
  private FieldProvider[] getInstalledModules() {
    List<HashMap<String, String>> installedModules = new ArrayList<HashMap<String, String>>();
    try {
      ModuleTreeData[] rootNodes = ModuleTreeData.select(this, "");
      for (ModuleTreeData module : rootNodes) {
        installedModules.add(getModuleHashMap(module, 1));
        installedModules.addAll(getSubTree(module, 2));
      }
    } catch (ServletException e) {
      log4j.error("Error obtaining installed modules", e);
    }
    return FieldProviderFactory.getFieldProviderArray(installedModules);
  }

  /**
   * Obtains the list of the child modules for a parent module. This method is called from
   * getInstalledModules and it composes a tree of inclusions.
   */
  private List<HashMap<String, String>> getSubTree(ModuleTreeData parentModule, int level) {
    List<HashMap<String, String>> installedModules = new ArrayList<HashMap<String, String>>();
    try {
      ModuleTreeData[] modules = ModuleTreeData.selectSubTree(this, "", parentModule.nodeId);
      for (ModuleTreeData module : modules) {
        installedModules.add(getModuleHashMap(module, level));
        installedModules.addAll(getSubTree(module, level + 1));
      }
    } catch (ServletException e) {
      log4j.error("Error obtaining installed modules subtree module: "
          + (parentModule != null ? parentModule.nodeId : "null"), e);
    }
    return installedModules;
  }

  /**
   * Converts the module in a hashmap to be displayed as FieldProvider, it takes into account the
   * level to insert HTML blank spaces.
   */
  private HashMap<String, String> getModuleHashMap(ModuleTreeData module, int level) {
    HashMap<String, String> rt = new HashMap<String, String>();
    rt.put("moduleName", module.modulename);
    rt.put("moduleVersion",
        module.version
            + (module.versionLabel == null || module.versionLabel.isEmpty() ? "" : " - "
                + module.versionLabel));
    rt.put("moduleAuthor", module.author == null || module.author.isEmpty() ? "-" : module.author);
    String tab = "";
    for (int i = 0; i < level; i++) {
      tab += "&nbsp;&nbsp;&nbsp;";
    }
    rt.put("modTab", tab);
    return rt;
  }

  public String getServletInfo() {
    return "Servlet DebtPaymentUnapply. This Servlet was made by Eduardo Argal";
  } // end of getServletInfo() method
}
