/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.base.secureApp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class VariablesHistory {
  private String currentHistoryIndex;
  private static final int reqHistoryLength = 10;
  private HttpSession session;
  private String role;
  private String language;
  private String dbSessionID;

  private static Logger log4j = Logger.getLogger(VariablesHistory.class);

  public VariablesHistory(HttpServletRequest request) {
    this.session = request.getSession(false);
    this.currentHistoryIndex = getSessionValue("reqHistory.current", "0");
    this.role = getSessionValue("#AD_Role_ID");
    this.language = getSessionValue("#AD_Language");
    this.dbSessionID = getSessionValue("#AD_Session_ID");
  }

  public String getCurrentHistoryIndex() {
    return Integer
        .toString(Integer.valueOf(this.currentHistoryIndex).intValue() % reqHistoryLength);
  }

  void upCurrentHistoryIndex() {
    this.currentHistoryIndex = Integer.toString(Integer.valueOf(this.currentHistoryIndex)
        .intValue() + 1);
    setSessionValue("reqHistory.current", this.currentHistoryIndex);
  }

  public void downCurrentHistoryIndex() {
    this.currentHistoryIndex = Integer.toString(Integer.valueOf(this.currentHistoryIndex)
        .intValue() - 1);
    setSessionValue("reqHistory.current", this.currentHistoryIndex);
  }

  String getCurrentServletPath(String defaultServletPath) {
    return getSessionValue("reqHistory.path" + getCurrentHistoryIndex(), defaultServletPath);
  }

  String getCurrentServletCommand() {
    return getSessionValue("reqHistory.command" + getCurrentHistoryIndex(), "DEFAULT");
  }

  String getSessionValue(String sessionAttribute) {
    return getSessionValue(sessionAttribute, "");
  }

  String getSessionValue(String sessionAttribute, String defaultValue) {
    String auxStr = null;
    try {
      auxStr = (String) session.getAttribute(sessionAttribute.toUpperCase());
      if (auxStr == null || auxStr.trim().equals(""))
        auxStr = defaultValue;
    } catch (Exception e) {
      auxStr = defaultValue;
    }
    if (!sessionAttribute.equalsIgnoreCase("menuVertical"))
      if (log4j.isDebugEnabled())
        log4j.debug("Session attribute: " + sessionAttribute + ":..." + auxStr);
    return auxStr;
  }

  public void setSessionValue(String attribute, String value) {
    try {
      session.setAttribute(attribute.toUpperCase(), value);
      if (!attribute.equalsIgnoreCase("menuVertical"))
        if (log4j.isDebugEnabled())
          log4j.debug("session value: " + attribute + ":..." + value.toString());
    } catch (Exception e) {
      log4j.error("setSessionValue error: " + attribute + ":..." + value);
    }
  }

  public void removeSessionValue(String attribute) {
    try {
      session.removeAttribute(attribute.toUpperCase());
    } catch (Exception e) {
      log4j.error("removeSessionValue error: " + attribute);
    }
  }

  String getRole() {
    return role;
  }

  public String getLanguage() {
    return language;
  }

  String getDBSession() {
    return dbSessionID;
  }

}
