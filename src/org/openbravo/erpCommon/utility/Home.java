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
package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.xmlEngine.XmlDocument;

public class Home extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    log4j.debug("Output: dataSheet");

    String[] discard = { "communityBranding" };
    OBContext.setAdminMode();
    try {
      if (OBDal.getInstance().get(org.openbravo.model.ad.system.SystemInformation.class, "0")
          .isShowCommunityBranding()
          || vars.getRole().equals("0")) {
        discard[0] = "";
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/Home",
        discard).createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "Home.html", strReplaceWith);
    xmlDocument.setParameter("leftTabs", lBar.manualTemplate());

    xmlDocument.setParameter("urls", getUrls());
    String strPurpose = getPurpose();
    if (strPurpose == null) {
      strPurpose = "unknown";
      xmlDocument.setParameter("cbPurposeTooltip",
          Utility.messageBD(myPool, "Home_Purpose_Tooltip", vars.getLanguage()));
    }
    xmlDocument.setParameter("cbPurpose", strPurpose.toLowerCase());
    xmlDocument.setParameter("cbVersion", getVersion());

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private static String getPurpose() {
    OBContext.setAdminMode();
    try {
      String strPurposeCode = OBDal.getInstance()
          .get(org.openbravo.model.ad.system.SystemInformation.class, "0").getInstancePurpose();
      if (strPurposeCode == null || "".equals(strPurposeCode)) {
        if (ActivationKey.isActiveInstance()) {
          // use value from license if possible
          strPurposeCode = ActivationKey.getInstance().getProperty("purpose");
        } else {
          // community instance without purpose configured
          return null;
        }
      }
      return Utility.getListValueName("InstancePurpose", strPurposeCode, OBContext.getOBContext()
          .getLanguage().getLanguage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static String getVersion() {
    String strVersion = "";
    OBContext.setAdminMode();
    try {
      ActivationKey ak = ActivationKey.getInstance();
      strVersion = OBVersion.getInstance().getMajorVersion();
      strVersion += " - ";
      strVersion += Utility.getListValueName("OBPSLicenseEdition", ak.getLicenseClass().getCode(),
          "en_US");
      strVersion += " - ";
      strVersion += OBVersion.getInstance().getMP();
    } finally {
      OBContext.restorePreviousMode();
    }
    return strVersion;
  }

  private static String getUrls() throws ServletException {
    String url = "\nvar communityBrandingUrl = '" + Utility.getCommunityBrandingUrl("2.50")
        + "';\n";
    url += "var staticUrl = '" + Utility.STATIC_COMMUNITY_BRANDING_URL + "?uimode=2.50';\n";
    url += "var butlerUtilsUrl = '" + Utility.BUTLER_UTILS_URL + "'";
    return url;
  }
}
