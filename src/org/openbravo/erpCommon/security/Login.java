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
package org.openbravo.erpCommon.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.openbravo.base.HttpBaseServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBVersion;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.xmlEngine.XmlDocument;

public class Login extends HttpBaseServlet {
  private static final long serialVersionUID = 1L;

  private static final String GOOGLE_INTEGRATION_MODULE_ID = "FF8080813129ADA401312CA1222A0005";
  private static final String GOOGLE_PREFERENCE_PROPERTY = "OBSEIG_ShowGIcon";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("LOGIN")) {
      log4j.debug("Command: Login");
      String strTheme = vars.getTheme();

      OBContext.setAdminMode();
      try {
        Client systemClient = OBDal.getInstance().get(Client.class, "0");
        String cacheMsg = Utility.messageBD(this, "OUTDATED_FILES_CACHED", systemClient
            .getLanguage().getLanguage());
        String validBrowserMsg = Utility.messageBD(this, "BROWSER_NOT_SUPPORTED", systemClient
            .getLanguage().getLanguage());
        String orHigherMsg = Utility.messageBD(this, "OR_HIGHER_TEXT", systemClient.getLanguage()
            .getLanguage());
        String recBrowserMsgTitle = Utility.messageBD(this, "RECOMMENDED_BROWSER_TITLE",
            systemClient.getLanguage().getLanguage());
        String recBrowserMsgText = Utility.messageBD(this, "RECOMMENDED_BROWSER_TEXT", systemClient
            .getLanguage().getLanguage());
        String identificationFailureTitle = Utility.messageBD(this, "IDENTIFICATION_FAILURE_TITLE",
            systemClient.getLanguage().getLanguage());
        String emptyUsernameOrPasswordText = Utility.messageBD(this,
            "EMPTY_USERNAME_OR_PASSWORD_TEXT", systemClient.getLanguage().getLanguage());

        if (OBVersion.getInstance().is30()) {
          printPageLogin30(vars, response, strTheme, cacheMsg, validBrowserMsg, orHigherMsg,
              recBrowserMsgTitle, recBrowserMsgText, identificationFailureTitle,
              emptyUsernameOrPasswordText);
        } else {
          printPageLogin250(response, strTheme, cacheMsg, validBrowserMsg, orHigherMsg);
        }
      } finally {
        vars.clearSession(false);
        OBContext.restorePreviousMode();
      }

    } else if (vars.commandIn("BLANK")) {
      printPageBlank(response, vars);
    } else if (vars.commandIn("CHECK")) {
      String checkString = "success";
      response.setContentType("text/plain; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      out.print(checkString);
      out.close();
    } else if (vars.commandIn("WELCOME")) {
      log4j.debug("Command: Welcome");
      if (OBVersion.getInstance().is30()) {
        printPageBlank(response, vars);
      } else {
        String strTheme = vars.getTheme();
        printPageWelcome(response, strTheme);
      }
    } else if (vars.commandIn("LOGO")) {
      printPageLogo(response, vars);
    } else {
      // Look for forced login URL property and redirect in case it is set and the login is accessed
      // through a different URL
      try {
        String forcedLoginUrl = Preferences.getPreferenceValue("ForcedLoginURL", true,
            (Client) null, null, null, null, null);
        log4j.debug("Forced URL: " + forcedLoginUrl);
        if (forcedLoginUrl != null && !forcedLoginUrl.isEmpty()
            && !request.getRequestURL().toString().startsWith(forcedLoginUrl)) {
          log4j.info("Redireting login from " + request.getRequestURL().toString()
              + " to forced login URL " + forcedLoginUrl);
          response.sendRedirect(forcedLoginUrl);
          return;
        }
      } catch (PropertyException e) {
        // Ignore and continue with the standard login. PropertyException is raised in case property
        // is not defined (standard case) or in case of conflict.
        log4j.debug("Exception getting ForcedLoginURL", e);
      }

      // Standard login
      String textDirection = vars.getSessionValue("#TextDirection", "LTR");
      printPageFrameIdentificacion(response, "Login_Welcome.html?Command=WELCOME",
          "Login_F1.html?Command=LOGIN", textDirection);
    }
  }

  private void printPageFrameIdentificacion(HttpServletResponse response, String strMenu,
      String strDetalle, String textDirection) throws IOException, ServletException {

    XmlDocument xmlDocument;
    if (textDirection.equals("RTL")) {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/security/Login_FS_RTL")
          .createXmlDocument();
      xmlDocument.setParameter("frameMenu", strMenu);
      xmlDocument.setParameter("frameMenuLoading", strDetalle);
      xmlDocument.setParameter("frame1", strMenu);
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/security/Login_FS")
          .createXmlDocument();
      xmlDocument.setParameter("frameMenu", strMenu);
      xmlDocument.setParameter("frameMenuLoading", strMenu);
      xmlDocument.setParameter("frame1", strDetalle);
    }

    String jsConstants = "\nvar isMenuHide = false; \n var isRTL = " + "RTL".equals(textDirection)
        + "; \n var menuWidth = '25%';\n var isMenuBlock = false;\n";

    xmlDocument.setParameter("jsConstants", jsConstants);
    xmlDocument.setParameter("framesetMenu", "25");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageBlank(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/security/Login_F0").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageWelcome(HttpServletResponse response, String strTheme) throws IOException,
      ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/security/Login_Welcome").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", strTheme);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageLogo(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/security/Login_Logo").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Shows 2.50 login page
   */
  private void printPageLogin250(HttpServletResponse response, String strTheme, String cacheMsg,
      String validBrowserMsg, String orHigherMsg) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/security/Login_F1").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", strTheme);
    xmlDocument.setParameter("itService", SessionLoginData.selectSupportContact(this));

    String cacheMsgFinal = (cacheMsg != null && !cacheMsg.equals("")) ? cacheMsg
        : "Your browser's cache has outdated files. Please clean it and reload the page.";
    cacheMsgFinal = "var cacheMsg = \"" + cacheMsgFinal + "\"";
    xmlDocument.setParameter("cacheMsg", cacheMsgFinal.replaceAll("\\n", "\n"));

    String orHigherMsgFinal = (orHigherMsg != null && !orHigherMsg.equals("")) ? orHigherMsg
        : "or higher";

    String validBrowserMsgFinal = (validBrowserMsg != null && !validBrowserMsg.equals("")) ? validBrowserMsg
        : "Your browser is not officially supported.\n\nYou can continue at your own risk or access the application with one of the supported browsers:";

    validBrowserMsgFinal = validBrowserMsgFinal + "\\n * Mozilla Firefox 3.0 " + orHigherMsgFinal
        + "\\n * Microsoft Internet Explorer 7.0 " + orHigherMsgFinal;
    validBrowserMsgFinal = "var validBrowserMsg = \"" + validBrowserMsgFinal + "\"";
    xmlDocument.setParameter("validBrowserMsg", validBrowserMsgFinal.replaceAll("\\n", "\n"));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Shows 3.0 login page
   */
  private void printPageLogin30(VariablesSecureApp vars, HttpServletResponse response,
      String strTheme, String cacheMsg, String validBrowserMsg, String orHigherMsg,
      String recBrowserMsgTitle, String recBrowserMsgText, String identificationFailureTitle,
      String emptyUsernameOrPasswordText) throws IOException, ServletException {

    boolean showForgeLogo = true;
    boolean showITLogo = false;
    boolean showCompanyLogo = false;
    boolean showGoogleIcon = false;

    String itLink = "";
    String companyLink = "";
    SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
    Module module = OBDal.getInstance().get(Module.class, GOOGLE_INTEGRATION_MODULE_ID);

    ActivationKey ak = ActivationKey.getInstance();
    if (ak.isActive()) {
      String hql = "from ADPreference pref where searchKey like :value and property = :prop and (visibleAtClient is null or visibleAtClient.id = '0')";
      Query q = OBDal.getInstance().getSession().createQuery(hql);
      q.setParameter("value", "N");
      q.setParameter("prop", GOOGLE_PREFERENCE_PROPERTY);

      // show by default - not show when there is a preference to disable it
      showGoogleIcon = q.list().size() == 0;
    } else {
      showGoogleIcon = (module != null && module.isEnabled());
    }

    if (sysInfo == null) {
      log4j.error("System information not found");
    } else {
      showITLogo = sysInfo.getYourItServiceLoginImage() != null;
      showCompanyLogo = sysInfo.getYourCompanyLoginImage() != null;
      showForgeLogo = !ActivationKey.getInstance().isActive()
          || (ActivationKey.getInstance().isActive() && sysInfo.isShowForgeLogoInLogin());
      itLink = sysInfo.getSupportContact() == null ? "" : sysInfo.getSupportContact();
      companyLink = sysInfo.getYourCompanyURL() == null ? "" : sysInfo.getYourCompanyURL();
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/security/Login")
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", strTheme);

    String visualPrefs = "var showCompanyLogo = " + showCompanyLogo + ", showSupportLogo = "
        + showITLogo + ", showForgeLogo = " + showForgeLogo + ", urlCompany = '" + companyLink
        + "', urlSupport = '" + itLink + "', urlOBForge = 'http://forge.openbravo.com/';";
    xmlDocument.setParameter("visualPrefs", visualPrefs);

    String expirationMessage = "var expirationMessage="
        + ak.getExpirationMessage(vars.getLanguage()).toString() + ";";
    xmlDocument.setParameter("expirationMessage", expirationMessage);
    xmlDocument.setParameter("itServiceUrl",
        "var itServiceUrl = '" + SessionLoginData.selectSupportContact(this) + "'");

    String cacheMsgFinal = (cacheMsg != null && !cacheMsg.equals("")) ? cacheMsg
        : "Your browser's cache has outdated files. Please clean it and reload the page.";
    cacheMsgFinal = "var cacheMsg = \"" + cacheMsgFinal + "\"";
    xmlDocument.setParameter("cacheMsg", cacheMsgFinal.replaceAll("\\n", "\n"));

    String identificationFailureFinal = (identificationFailureTitle != null && !identificationFailureTitle
        .equals("")) ? identificationFailureTitle : "Invalid user name or password.";
    identificationFailureFinal = "var identificationFailureTitle = \"" + identificationFailureFinal
        + "\"";
    xmlDocument.setParameter("identificationFailureTitle",
        identificationFailureFinal.replaceAll("\\n", "\n"));

    String emptyUserNameOrPasswordFinal = (emptyUsernameOrPasswordText != null && !emptyUsernameOrPasswordText
        .equals("")) ? emptyUsernameOrPasswordText : "Enter your username and password.";
    emptyUserNameOrPasswordFinal = "var errorEmptyContent = \"" + emptyUserNameOrPasswordFinal
        + "\"";
    xmlDocument.setParameter("errorEmptyContent",
        emptyUserNameOrPasswordFinal.replaceAll("\\n", "\n"));

    String orHigherMsgFinal = (orHigherMsg != null && !orHigherMsg.equals("")) ? orHigherMsg
        : "or higher";

    String validBrowserMsgFinal = (validBrowserMsg != null && !validBrowserMsg.equals("")) ? validBrowserMsg
        : "Your browser is not officially supported.\n\nYou can continue at your own risk or access the application with one of the supported browsers:";

    validBrowserMsgFinal = "var validBrowserMsg = \"" + validBrowserMsgFinal + "\"";
    orHigherMsgFinal = "var validBrowserMsgOrHigher = \"" + orHigherMsgFinal + "\"";
    xmlDocument.setParameter("validBrowserMsg", validBrowserMsgFinal.replaceAll("\\n", "\n"));
    xmlDocument.setParameter("validBrowserMsgOrHigher", orHigherMsgFinal.replaceAll("\\n", "\n"));

    String recBrowserMsgTitleFinal = (recBrowserMsgTitle != null && !recBrowserMsgTitle.equals("")) ? recBrowserMsgTitle
        : "NOTE";

    String recBrowserMsgTextFinal = (recBrowserMsgText != null && !recBrowserMsgText.equals("")) ? recBrowserMsgText
        : "For a better experience, it is recommended to use XX or YY";

    recBrowserMsgTitleFinal = "var recBrowserMsgTitle = \"" + recBrowserMsgTitleFinal + "\"";
    recBrowserMsgTextFinal = "var recBrowserMsgText = \"" + recBrowserMsgTextFinal + "\"";
    xmlDocument.setParameter("recBrowserMsgTitle", recBrowserMsgTitleFinal.replaceAll("\\n", "\n"));
    xmlDocument.setParameter("recBrowserMsgText", recBrowserMsgTextFinal.replaceAll("\\n", "\n"));

    if (showGoogleIcon) {

      String authServlet = "../org.openbravo.service.integration.google/auth.html";
      String link = "<p class=\"LabelText Login_LabelText\" style=\"line-height:24px;\">"
          + Utility.messageBD(this, "OBSEIG_SignIn", vars.getLanguage()) + "&nbsp; <a href=\""
          + authServlet + "\" title=\""
          + Utility.messageBD(this, "OBSEIG_SignInAltMsg", vars.getLanguage())
          + "\" target=\"_top\" "
          + "><img style=\"vertical-align:middle;\" src=\"../web/images/google.png\" alt=\""
          + Utility.messageBD(this, "OBSEIG_SignInAltMsg", vars.getLanguage())
          + "\" width=\"24\" height=\"24\" border=\"0\"/></a></p>";

      xmlDocument.setParameter("sign-in", link);
    }

    OBError error = (OBError) vars.getSessionObject("LoginErrorMsg");
    if (error != null) {
      vars.removeSessionValue("LoginErrorMsg");
      xmlDocument.setParameter("errorMsgStyle", ""); // clear style
      xmlDocument.setParameter("errorMsgTitle", error.getTitle());
      xmlDocument.setParameter("errorMsgContent", error.getMessage());
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
