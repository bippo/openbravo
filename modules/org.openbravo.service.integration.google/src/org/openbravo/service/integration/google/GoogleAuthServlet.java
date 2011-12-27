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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.integration.google;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.openbravo.base.HttpBaseServlet;
import org.openbravo.base.VariablesBase;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.security.SessionLogin;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.service.integration.openid.OBSOIDUserIdentifier;
import org.openbravo.service.integration.openid.OpenIDManager;
import org.openid4java.discovery.Identifier;

/**
 * @author iperdomo
 */
public class GoogleAuthServlet extends HttpBaseServlet {

  private static final long serialVersionUID = 1L;

  private static final Logger log = Logger.getLogger(GoogleAuthServlet.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    VariablesBase vars = new VariablesBase(req);

    OBContext.setAdminMode(false);

    try {

      String lang = OBDal.getInstance().get(Client.class, "0").getLanguage().getLanguage();

      if (!ActivationKey.getInstance().isActive()) {
        OBError error = new OBError();

        String message = Utility.messageBD(this, "OBUIAPP_ActivateMessage", lang);
        message = message.replace("%0", Utility.messageBD(this, "OBSEIG_Activate", lang));
        message = message.replaceAll("&quot;", "\"");

        error.setTitle("");
        error.setMessage(message);
        error.setType("Error");
        vars.setSessionObject("LoginErrorMsg", error);
        resp.sendRedirect(strDireccion);
        return;
      }

      if ("true".equals(vars.getStringParameter("is_return"))) {
        processReturn(req, resp);
      } else {
        if ("true".equals(vars.getStringParameter("is_association"))) {
          vars.setSessionValue("is_association", "true");
        }

        OpenIDManager.getInstance()
            .authRequest(OpenIDManager.GOOGLE_OPENID_DISCOVER_URL, req, resp);
      }

    } catch (Exception e) {
      log4j.error("Error trying to authenticate using Google Auth service: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private void processReturn(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    final String loginPageURL = getServletConfig().getServletContext().getInitParameter(
        "ServletSinIdentificar");

    final VariablesSecureApp vars = new VariablesSecureApp(req);

    try {
      OBContext.setAdminMode(false);

      String lang = OBDal.getInstance().get(Client.class, "0").getLanguage().getLanguage();

      Identifier oid = OpenIDManager.getInstance().getIdentifier(req);

      if ("true".equals(vars.getSessionValue("is_association"))) {
        vars.removeSessionValue("is_association");
        try {
          OpenIDManager.getInstance().associateAccount(oid, req, resp);
          vars.setSessionValue("startup-message", Utility.messageBD(this, "OBSEIG_LinkedOK", lang)
              .replaceAll("&quot;", "\""));
          vars.setSessionValue("startup-message-title", Utility.messageBD(this, "ProcessOK", lang)
              .replaceAll("&quot;", "\""));

        } catch (ConstraintViolationException e) {
          log.error("Error trying to associate account with OpenID identifier: " + oid.toString(),
              e);
          // User notification
          vars.setSessionValue(
              "startup-message",
              Utility.messageBD(this, "OBSEIG_DuplicatedIdentifier", lang).replaceAll("&quot;",
                  "\""));
          vars.setSessionValue("startup-message-title",
              Utility.messageBD(this, "ProcessFailed", lang).replaceAll("&quot;", "\""));

        }
        resp.sendRedirect(strDireccion);
        return;
      }

      User user = OpenIDManager.getInstance().getUser(oid);

      if (user == null) {
        user = createUser(oid, req, resp);
        if (user == null) {
          return;
        }
      }

      req.getSession(true).removeAttribute("#Authenticated_user");

      String sessionId = createDBSession(req, user.getUsername(), user.getId());
      req.getSession(true).setAttribute("#Authenticated_user", user.getId());
      vars.setSessionValue("#AD_SESSION_ID", sessionId);
      vars.setSessionValue("#LOGGINGIN", "Y");

      resp.sendRedirect(strDireccion + "/security/Menu.html");

    } catch (Exception e) {
      log.error("Error processing return of Google Auth Service:" + e.getMessage(), e);
      this.getServletContext().getRequestDispatcher(loginPageURL).forward(req, resp);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unchecked")
  private User createUser(Identifier oid, HttpServletRequest req, HttpServletResponse resp)
      throws OBException, IOException {
    Map<String, String> attributes = (Map<String, String>) req.getAttribute("attributes");

    OBSEIGDefaults newUserDefaults;
    VariablesBase vars = new VariablesBase(req);

    String lang = OBDal.getInstance().get(Client.class, "0").getLanguage().getLanguage();

    if (attributes.get(OpenIDManager.ATTRIBUTE_FIRSTNAME) == null
        || attributes.get(OpenIDManager.ATTRIBUTE_LASTNAME) == null
        || attributes.get(OpenIDManager.ATTRIBUTE_EMAIL) == null) {
      throw new OBException("Google Integration: OpenID identifier attributes missing");
    }

    OBCriteria<OBSEIGDefaults> defaults = OBDal.getInstance().createCriteria(OBSEIGDefaults.class);
    defaults.setFilterOnReadableClients(false);
    defaults.setFilterOnReadableOrganization(false);

    if (defaults.count() == 0) {
      OBError error = new OBError();
      error.setMessage(Utility.messageBD(this, "OBSEIG_NoDefaultConf", lang)
          .replaceAll("@@email@@", attributes.get(OpenIDManager.ATTRIBUTE_EMAIL))
          .replaceAll("&quot;", "\""));
      error.setTitle("");
      error.setType("Error");
      vars.setSessionObject("LoginErrorMsg", error);
      resp.sendRedirect(strDireccion);
      return null;
    }

    if (defaults.count() > 1) {
      OBError error = new OBError();
      error.setMessage(Utility.messageBD(this, "OBSEIG_TooMuchConf", lang)
          .replaceAll("@@email@@", attributes.get(OpenIDManager.ATTRIBUTE_EMAIL))
          .replaceAll("&quot;", "\""));
      error.setTitle("");
      error.setType("Error");
      vars.setSessionObject("LoginErrorMsg", error);
      resp.sendRedirect(strDireccion);
      return null;
    }

    newUserDefaults = defaults.list().get(0);

    User newUser = OBProvider.getInstance().get(User.class);
    final String name = attributes.get(OpenIDManager.ATTRIBUTE_FIRSTNAME) + " "
        + attributes.get(OpenIDManager.ATTRIBUTE_LASTNAME);

    newUser.setName(name);
    newUser.setEmail(attributes.get(OpenIDManager.ATTRIBUTE_EMAIL));
    newUser.setOrganization(newUserDefaults.getRole().getOrganization());
    newUser.setClient(newUserDefaults.getRole().getClient());
    newUser.setActive(newUserDefaults.isNewuseractive());
    newUser.setUsername(name);

    OBDal.getInstance().save(newUser);

    OBSOIDUserIdentifier userIdentifier = OBProvider.getInstance().get(OBSOIDUserIdentifier.class);
    userIdentifier.setUserContact(newUser);
    userIdentifier.setClient(newUserDefaults.getRole().getClient());
    userIdentifier.setOrganization(newUserDefaults.getRole().getOrganization());
    userIdentifier.setOpenIDIdentifier(oid.toString());
    userIdentifier.setActive(true);

    OBDal.getInstance().save(userIdentifier);

    UserRoles uRoles = OBProvider.getInstance().get(UserRoles.class);
    uRoles.setClient(newUserDefaults.getRole().getClient());
    uRoles.setOrganization(newUserDefaults.getRole().getOrganization());
    uRoles.setUserContact(newUser);
    uRoles.setRole(newUserDefaults.getRole());

    OBDal.getInstance().save(uRoles);

    newUser.setDefaultRole(newUserDefaults.getRole());

    OBDal.getInstance().flush();

    return newUser;
  }

  // All the methods below are a copy and modified from LoginHandler.java

  private String createDBSession(HttpServletRequest req, String strUser, String strUserAuth) {
    try {
      String usr = strUserAuth == null ? "0" : strUserAuth;

      final SessionLogin sl = new SessionLogin(req, "0", "0", usr);

      if (strUserAuth == null) {
        sl.setStatus("F");
      } else {
        sl.setStatus("S");
      }

      sl.setUserName(strUser);
      sl.setServerUrl(strDireccion);
      sl.save();
      return sl.getSessionID();
    } catch (Exception e) {
      log4j.error("Error creating DB session", e);
      return null;
    }
  }
}
