/*
 ************************************************************************************
 * Copyright (C) 2001-2011 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.authentication;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.authentication.basic.DefaultAuthenticationManager;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.base.VariablesBase;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.security.SessionLogin;
import org.openbravo.model.ad.access.Session;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.web.BaseWebServiceServlet;

/**
 * 
 * @author adrianromero
 * @author iperdomo
 */
public abstract class AuthenticationManager {

  private static final Logger log4j = Logger.getLogger(AuthenticationManager.class);
  private static final String DEFAULT_AUTH_CLASS = "org.openbravo.authentication.basic.DefaultAuthenticationManager";

  private static final String SUCCESS_SESSION_STANDARD = "S";
  private static final String SUCCESS_SESSION_WEB_SERVICE = "WS";
  private static final String REJECTED_SESSION_WEB_SERVICE = "WSR";
  private static final String SUCCESS_SESSION_CONNECTOR = "WSC";
  private static final String FAILED_SESSION = "F";

  protected ConnectionProvider conn = null;
  protected String defaultServletUrl = null;
  protected String localAdress = null;
  protected String username = "";

  /**
   * Returns an instance of AuthenticationManager subclass, based on the authentication.class
   * property in Openbravo.properties
   */
  public final static AuthenticationManager getAuthenticationManager(HttpServlet s) {
    AuthenticationManager authManager;
    String authClass = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("authentication.class", DEFAULT_AUTH_CLASS);
    if (authClass == null || authClass.equals("")) {
      // If not defined, load default
      authClass = "org.openbravo.authentication.basic.DefaultAuthenticationManager";
    }
    try {
      authManager = (AuthenticationManager) OBClassLoader.getInstance().loadClass(authClass)
          .newInstance();
      authManager.init(s);
    } catch (Exception e) {
      log4j
          .error("Defined authentication manager cannot be loaded. Verify the 'authentication.class' entry in Openbravo.properties");
      authManager = new DefaultAuthenticationManager(s);
    }
    return authManager;
  }

  public AuthenticationManager() {
  }

  public AuthenticationManager(HttpServlet s) throws AuthenticationException {
    init(s);
  }

  protected void bdErrorAjax(HttpServletResponse response, String strType, String strTitle,
      String strText) throws IOException {
    response.setContentType("text/xml; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    out.println("<xml-structure>\n");
    out.println("  <status>\n");
    out.println("    <type>" + strType + "</type>\n");
    out.println("    <title>" + strTitle + "</title>\n");
    out.println("    <description><![CDATA[" + strText + "]]></description>\n");
    out.println("  </status>\n");
    out.println("</xml-structure>\n");
    out.close();
  }

  public void init(HttpServlet s) throws AuthenticationException {
    if (s instanceof ConnectionProvider) {
      conn = (ConnectionProvider) s;
    } else {
      conn = new DalConnectionProvider();
    }
    defaultServletUrl = s.getServletConfig().getServletContext()
        .getInitParameter("ServletSinIdentificar");
  }

  /**
   * Used in the service method of the {@link HttpSecureAppServlet} to know if the request is
   * authenticated or not. This method calls the <b>doAuthenticate</b> that makes the actual checks
   * and could be easily extended by sub-classes. Returns the user id if the user is already logged
   * in or null if is not authenticated.
   * 
   * @param request
   *          HTTP request object to handle parameters and session attributes
   * @param response
   *          HTTP response object to handle possible redirects
   * @return the value of AD_User_ID if the user is already authenticated or <b>null</b> if not
   */
  public final String authenticate(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, ServletException, IOException {

    if (localAdress == null) {
      localAdress = HttpBaseUtils.getLocalAddress(request);
    }

    final String userId = doAuthenticate(request, response);

    final VariablesSecureApp vars = new VariablesSecureApp(request, false);
    if (StringUtils.isEmpty(vars.getSessionValue("#AD_SESSION_ID"))) {
      setDBSession(request, userId, SUCCESS_SESSION_STANDARD, true);
    }

    if (userId == null && !response.isCommitted()) {
      response.sendRedirect(localAdress + defaultServletUrl);
      return null;
    }

    return userId;
  }

  /**
   * Authentication for web services and external services. All authenticated requests not using the
   * standard UI *MUST* use this authentication or
   * {@link AuthenticationManager#webServiceAuthenticate(String, String)}.
   * 
   * @param request
   *          HTTP request object to handle parameters and session attributes
   * @return the value of AD_User_ID if the user is already authenticated or <b>null</b> if not
   * @throws AuthenticationException
   *           in case of an authentication error different than incorrect user/password (which just
   *           returns null)
   */
  public final String webServiceAuthenticate(HttpServletRequest request)
      throws AuthenticationException {
    final String userId = doWebServiceAuthenticate(request);

    final String dbSessionId = setDBSession(request, userId, SUCCESS_SESSION_WEB_SERVICE, false);

    return webServicePostAuthenticate(userId, dbSessionId);
  }

  /**
   * Authentication for web services and external services. All authenticated requests not using the
   * standard UI *MUST* use this authentication or
   * {@link AuthenticationManager#webServiceAuthenticate(HttpServletRequest)}. This one is intended
   * for authentications for non standard REST web services (such as SOAP).
   * 
   * @param user
   *          User name to authenticate
   * @param password
   *          Password to validate user
   * @return the value of AD_User_ID if the user is already authenticated or <b>null</b> if not
   * @throws AuthenticationExceptionin
   *           case of an authentication error different than incorrect user/password (which just
   *           returns null)
   */
  public final String webServiceAuthenticate(String user, String password)
      throws AuthenticationException {
    username = user;
    final String userId = doWebServiceAuthenticate(user, password);
    final String dbSessionId = setDBSession(null, userId, SUCCESS_SESSION_WEB_SERVICE, false);
    return webServicePostAuthenticate(userId, dbSessionId);
  }

  private String webServicePostAuthenticate(String userId, String dbSessionId)
      throws AuthenticationException {
    if (userId == null) {
      return null;
    }

    switch (ActivationKey.getInstance(true).checkNewWSCall(true)) {
    case NO_RESTRICTION:
      return userId;
    case EXCEEDED_WARN_WS_CALLS:
      log4j.warn("Number of webservice calls exceeded today.");
      return userId;
    case EXCEEDED_MAX_WS_CALLS:
      updateDBSession(dbSessionId, false, REJECTED_SESSION_WEB_SERVICE);
      log4j.warn("Cannot use WS, exceeded number of calls");
      throw new AuthenticationException("Exceeded maximum number of allowed calls to web services.");
    case EXPIRED:
      updateDBSession(dbSessionId, false, REJECTED_SESSION_WEB_SERVICE);
      log4j.warn("Cannot use WS, license expired");
      throw new AuthenticationException("Exceeded maximum number of allowed calls to web services.");
    }

    return null;
  }

  /**
   * Authentication for approved connectors. Only authorized connectors are allowed to use this
   * authentication.
   * 
   * @param request
   *          HTTP request object to handle parameters and session attributes
   * @return the value of AD_User_ID if the user is already authenticated or <b>null</b> if not
   * @throws AuthenticationException
   *           in case of an authentication error different than incorrect user/password (which just
   *           returns null)
   */
  public final String connectorAuthenticate(HttpServletRequest request)
      throws AuthenticationException {
    final String userId = doWebServiceAuthenticate(request);

    setDBSession(request, userId, SUCCESS_SESSION_CONNECTOR, false);

    return userId;
  }

  /**
   * Authentication for approved connectors. Only authorized connectors are allowed to use this
   * authentication.
   * 
   * @param user
   *          User name to authenticate
   * @param password
   *          Password to validate user
   * @return the value of AD_User_ID if the user is already authenticated or <b>null</b> if not
   * @throws AuthenticationExceptionin
   *           case of an authentication error different than incorrect user/password (which just
   *           returns null)
   */
  public final String connectorAuthenticate(String user, String password)
      throws AuthenticationException {
    final String userId = doWebServiceAuthenticate(user, password);

    setDBSession(null, userId, SUCCESS_SESSION_CONNECTOR, false);

    return userId;
  }

  private String setDBSession(HttpServletRequest request, String userId, String successSessionType,
      boolean setSession) {
    final VariablesSecureApp vars = new VariablesSecureApp(request, false);
    String dbSessionId = vars.getSessionValue("#AD_SESSION_ID");
    if (StringUtils.isEmpty(dbSessionId)) {
      dbSessionId = createDBSession(request, username, userId, successSessionType);
      if (setSession) {
        vars.setSessionValue("#AD_SESSION_ID", dbSessionId);
      }
    }
    return dbSessionId;
  }

  /**
   * Clears all session attributes and calls the <b>doLogout</b> method
   */
  public final void logout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    VariablesBase vars = new VariablesBase(request);
    vars.clearSession(true);

    doLogout(request, response);
  }

  /**
   * Called from the <b>authenticate</b> method makes the necessary processing to check if the
   * request is authenticated or not. The simplest way to check is if the #Authenticated_user
   * session attribute is present and return it.
   * 
   * @param request
   *          HTTP request object, used for handling parameters and session attributes
   * @param response
   * @return <ul>
   *         <li>The user id (AD_User_ID) if the request is already authenticated or the
   *         authentication process succeeded</li>
   *         <li><b>null</b> if the request is not authenticated or authentication process failed
   *         (e.g. wrong password)</li>
   *         </ul>
   * @see DefaultAuthenticationManager
   */
  protected abstract String doAuthenticate(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, ServletException, IOException;

  /**
   * Authentication used by web services and connectors. This authentication can be overriden by
   * subclasses. By default it looks for user and password parameters in the request, if they are
   * not present, Basic authentication is performed
   * 
   * @param request
   *          HTTP request object, used for handling parameters and session attributes
   * @return <ul>
   *         <li>The user id (AD_User_ID) if the request is already authenticated or the
   *         authentication process succeeded</li>
   *         <li><b>null</b> if the request is not authenticated or authentication process failed
   *         (e.g. wrong password)</li>
   *         </ul>
   */
  protected String doWebServiceAuthenticate(HttpServletRequest request) {
    final String login = request.getParameter(BaseWebServiceServlet.LOGIN_PARAM);
    final String password = request.getParameter(BaseWebServiceServlet.PASSWORD_PARAM);
    String userId = null;
    if (login != null && password != null) {
      username = login;
      userId = LoginUtils.getValidUserId(new DalConnectionProvider(false), login, password);
    } else { // use basic authentication
      userId = doBasicAuthentication(request);
    }

    return userId;
  }

  protected String doWebServiceAuthenticate(String user, String password) {
    return LoginUtils.getValidUserId(new DalConnectionProvider(false), user, password);
  }

  /**
   * Method called from the <b>logout</b> method after clearing all session attributes. The usual
   * process is to redirect the user to the login page
   * 
   * @param request
   *          HTTP request object
   * @param response
   *          HTTP response object
   */
  protected abstract void doLogout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException;

  protected final String createDBSession(HttpServletRequest req, String strUser, String strUserAuth) {
    return createDBSession(req, strUser, strUserAuth, "S");
  }

  protected final String createDBSession(HttpServletRequest req, String strUser,
      String strUserAuth, String successSessionType) {
    try {
      String usr = strUserAuth == null ? "0" : strUserAuth;

      final SessionLogin sl = new SessionLogin(req, "0", "0", usr);

      if (strUserAuth == null) {
        sl.setStatus(FAILED_SESSION);
      } else {
        sl.setStatus(successSessionType);
      }

      sl.setUserName(strUser);
      if (req != null) {
        sl.setServerUrl(HttpBaseUtils.getLocalAddress(req));
      }
      sl.save();
      return sl.getSessionID();
    } catch (Exception e) {
      log4j.error("Error creating DB session", e);
      return null;
    }
  }

  protected final void updateDBSession(String sessionId, boolean sessionActive, String status) {
    try {
      OBContext.setAdminMode();
      Session session = OBDal.getInstance().get(Session.class, sessionId);
      session.setSessionActive(sessionActive);
      session.setLoginStatus(status);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log4j.error("Error updating session in DB", e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private String doBasicAuthentication(HttpServletRequest request) {
    try {
      final String auth = request.getHeader("Authorization");
      if (auth == null) {
        return null;
      }
      if (!auth.toUpperCase().startsWith("BASIC ")) {
        return null; // only BASIC supported
      }

      // user and password come after BASIC
      final String userpassEncoded = auth.substring(6);

      // Decode it, using any base 64 decoder
      final String decodedUserPass = new String(Base64.decodeBase64(userpassEncoded.getBytes()));
      final int index = decodedUserPass.indexOf(":");
      if (index == -1) {
        return null;
      }
      final String login = decodedUserPass.substring(0, index);
      final String password = decodedUserPass.substring(index + 1);
      String userId = LoginUtils.getValidUserId(new DalConnectionProvider(), login, password);
      username = login;
      return userId;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

}
