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
package org.openbravo.erpCommon.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

public class SessionLogin {
  static Logger log4j = Logger.getLogger(SessionLogin.class);
  protected String sessionID;
  protected String client;
  protected String org;
  protected String isactive = "Y";
  protected String user;
  protected String websession;
  protected String remoteAddr;
  protected String remoteHost;
  protected String processed = "N";
  protected String serverUrl;
  private String username;
  private String status;

  public SessionLogin(String ad_client_id, String ad_org_id, String ad_user_id)
      throws ServletException {
    if (ad_client_id == null || ad_client_id.equals(""))
      throw new ServletException("SessionLogin load - client is null");
    else if (ad_org_id == null || ad_org_id.equals(""))
      throw new ServletException("SessionLogin load - organization is null");
    else if (ad_user_id == null || ad_user_id.equals(""))
      throw new ServletException("SessionLogin load - user is null");
    setClient(ad_client_id);
    setOrg(ad_org_id);
    setUser(ad_user_id);
    defaultParameters();
  }

  public SessionLogin(HttpServletRequest request, String ad_client_id, String ad_org_id,
      String ad_user_id) throws ServletException {
    if (ad_client_id == null || ad_client_id.equals(""))
      throw new ServletException("SessionLogin load - client is null");
    else if (ad_org_id == null || ad_org_id.equals(""))
      throw new ServletException("SessionLogin load - organization is null");
    else if (ad_user_id == null || ad_user_id.equals(""))
      throw new ServletException("SessionLogin load - user is null");
    setClient(ad_client_id);
    setOrg(ad_org_id);
    setUser(ad_user_id);
    if (request != null) {
      defaultParameters(request);
    }
  }

  public void setServerUrl(String strAddr) {
    serverUrl = strAddr;
  }

  private void defaultParameters() {
    try {
      InetAddress lh = InetAddress.getLocalHost();
      setRemoteAddr(lh.getHostAddress());
      setRemoteHost(lh.getHostName());
    } catch (UnknownHostException e) {
      log4j.error("SessionLogin.defaultParameters() - No local host. " + e);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("SessionLogin.defaultParameters() - Remote Address: " + getRemoteAddr()
          + " - Remote Host: " + getRemoteHost());
  }

  private void defaultParameters(HttpServletRequest request) {
    setRemoteAddr(request.getRemoteAddr());
    setRemoteHost(request.getRemoteHost());
    if (log4j.isDebugEnabled())
      log4j.debug("SessionLogin.defaultParameters(request) - Remote Address: " + getRemoteAddr()
          + " - Remote Host: " + getRemoteHost());
  }

  public int save() throws ServletException {
    if (getSessionID().equals("")) {
      String key = SequenceIdData.getUUID();
      SessionListener.addSession(key);
      if (key == null || key.equals(""))
        throw new ServletException("SessionLogin.save() - key creation failed");
      setSessionID(key);
    }
    try {
      OBContext.setAdminMode();
      Session session = OBProvider.getInstance().get(Session.class);

      session.setCreationDate(new Date());
      session.setUpdated(new Date());
      session.setClient(OBDal.getInstance().get(Client.class, getClient()));
      session.setOrganization(OBDal.getInstance().get(Organization.class, getOrg()));
      session.setActive(getIsActive());
      User user1 = OBDal.getInstance().get(User.class, getUser());
      session.setCreatedBy(user1);
      session.setUpdatedBy(user1);
      session.setWebSession(getWebSession());
      session.setRemoteAddress(getRemoteAddr());
      session.setRemoteHost(getRemoteHost());
      session.setProcessed(getProcessed());
      session.setServerUrl(serverUrl);
      // save inactive session for failed and webservice logins
      boolean sessionActive = !status.equals("F") && !status.equals("WS");
      session.setSessionActive(sessionActive);
      session.setLoginStatus(status);
      session.setUsername(username);
      OBDal.getInstance().save(session);
      OBDal.getInstance().commitAndClose();
      setSessionID(session.getId());
      return 1;
    } catch (Exception e) {
      log4j.error("Error saving session in DB", e);
      return 0;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * @deprecated use save() instead
   */
  public int save(ConnectionProvider conn) throws ServletException {
    return save();
  }

  public void update(ConnectionProvider conn) throws ServletException {
    try {
      OBContext.setAdminMode();
      Session session = OBDal.getInstance().get(Session.class, getSessionID());
      session.setActive(getIsActive());
      User user1 = OBDal.getInstance().get(User.class, getUser());
      session.setUpdatedBy(user1);
      session.setWebSession(getWebSession());
      session.setRemoteAddress(getRemoteAddr());
      session.setRemoteHost(getRemoteHost());
      session.setProcessed(getProcessed());
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log4j.error("Error updating session in DB", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void setSessionID(String newValue) {
    this.sessionID = (newValue == null) ? "" : newValue;
  }

  public String getSessionID() {
    return ((this.sessionID == null) ? "" : this.sessionID);
  }

  private void setClient(String newValue) {
    this.client = (newValue == null) ? "" : newValue;
  }

  private String getClient() {
    return ((this.client == null) ? "" : this.client);
  }

  private void setOrg(String newValue) {
    this.org = (newValue == null) ? "" : newValue;
  }

  private String getOrg() {
    return ((this.org == null) ? "" : this.org);
  }

  private void setIsActive(String newValue) {
    this.isactive = (newValue == null) ? "Y" : newValue;
  }

  private boolean getIsActive() {
    return (this.isactive.equals("Y"));
  }

  private void setUser(String newValue) {
    this.user = (newValue == null) ? "" : newValue;
  }

  private String getUser() {
    return ((this.user == null) ? "" : this.user);
  }

  private void setWebSession(String newValue) {
    this.websession = (newValue == null) ? "" : newValue;
  }

  private String getWebSession() {
    return ((this.websession == null) ? "" : this.websession);
  }

  private void setRemoteAddr(String newValue) {
    this.remoteAddr = (newValue == null) ? "" : newValue;
  }

  private String getRemoteAddr() {
    return ((this.remoteAddr == null) ? "" : this.remoteAddr);
  }

  private void setRemoteHost(String newValue) {
    this.remoteHost = (newValue == null) ? "" : newValue;
  }

  private String getRemoteHost() {
    return ((this.remoteHost == null) ? "" : this.remoteHost);
  }

  private void setProcessed(String newValue) {
    this.processed = (newValue == null) ? "" : newValue;
  }

  private boolean getProcessed() {
    return ((this.processed == null) ? false : this.processed.equals("Y"));
  }

  public void setUserName(String strUser) {
    username = strUser;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
