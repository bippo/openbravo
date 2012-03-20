/*
 ************************************************************************************
 * Copyright (C) 2001-2012 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.authentication.basic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.base.secureApp.VariablesHistory;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;

/**
 * 
 * @author adrianromero
 * @author iperdomo
 */
public class DefaultAuthenticationManager extends AuthenticationManager {

  private static final Logger log4j = Logger.getLogger(DefaultAuthenticationManager.class);

  public DefaultAuthenticationManager() {
  }

  public DefaultAuthenticationManager(HttpServlet s) throws AuthenticationException {
    super(s);
  }

  @Override
  protected String doAuthenticate(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, ServletException, IOException {

    final VariablesSecureApp vars = new VariablesSecureApp(request, false);
    final String sUserId = (String) request.getSession().getAttribute("#Authenticated_user");
    final String strAjax = vars.getStringParameter("IsAjaxCall");

    if (!StringUtils.isEmpty(sUserId)) {
      return sUserId;
    }

    VariablesHistory variables = new VariablesHistory(request);

    // Begins code related to login process

    final String strUser = vars.getStringParameter("user");
    final String strPass = vars.getStringParameter("password");
    username = strUser;

    if (StringUtils.isEmpty(strUser)) {
      // redirects to the menu or the menu with the target
      setTargetInfoInVariables(request, variables);
      return null; // just give up, return null
    }

    final String userId = LoginUtils.getValidUserId(conn, strUser, strPass);
    final String sessionId = createDBSession(request, strUser, userId);

    if (userId == null) {

      OBError errorMsg = new OBError();
      errorMsg.setType("Error");

      if (LoginUtils.checkUserPassword(conn, strUser, strPass) == null) {
        log4j.debug("Failed user/password. Username: " + strUser + " - Session ID:" + sessionId);
        errorMsg.setTitle("IDENTIFICATION_FAILURE_TITLE");
        errorMsg.setMessage("IDENTIFICATION_FAILURE_MSG");
      } else {
        log4j.debug(strUser + " is locked cannot activate session ID " + sessionId);
        errorMsg.setTitle("LOCKED_USER_TITLE");
        errorMsg.setMessage("LOCKED_USER_MSG");
        updateDBSession(sessionId, false, "LU");
      }

      // throw error message will be caught by LoginHandler
      throw new AuthenticationException("IDENTIFICATION_FAILURE_TITLE", errorMsg);
    }

    // Using the Servlet API instead of vars.setSessionValue to avoid breaking code
    // vars.setSessionValue always transform the key to upper-case
    request.getSession(true).setAttribute("#Authenticated_user", userId);

    vars.setSessionValue("#AD_SESSION_ID", sessionId);
    vars.setSessionValue("#LogginIn", "Y");

    if (!StringUtils.isEmpty(strAjax) && StringUtils.isEmpty(userId)) {
      bdErrorAjax(response, "Error", "",
          Utility.messageBD(this.conn, "NotLogged", variables.getLanguage()));
      return null;
    } else {
      // redirects to the menu or the menu with the target
      setTargetInfoInVariables(request, variables);
    }

    return userId;
  }

  private void setTargetInfoInVariables(HttpServletRequest request, VariablesHistory variables) {
    // redirects to the menu or the menu with the target
    String strTarget = request.getRequestURL().toString();
    String qString = request.getQueryString();
    String strDireccionLocal = HttpBaseUtils.getLocalAddress(request);

    if (!strTarget.endsWith("/security/Menu.html")) {
      variables.setSessionValue("targetmenu", strTarget);
    }

    // Storing target string to redirect after a successful login
    variables.setSessionValue("target", strDireccionLocal + "/security/Menu.html"
        + (qString != null && !qString.equals("") ? "?" + qString : ""));
    if (qString != null && !qString.equals("")) {
      variables.setSessionValue("targetQueryString", qString);
    }
  }

  @Override
  protected void doLogout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    if (!response.isCommitted()) {
      response.sendRedirect(HttpBaseUtils.getLocalAddress(request));
    }
  }
}
