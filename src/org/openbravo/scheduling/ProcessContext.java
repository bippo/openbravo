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
package org.openbravo.scheduling;

import org.openbravo.base.secureApp.VariablesSecureApp;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * @author awolski
 * 
 */
public class ProcessContext {

  public static final String KEY = "org.openbravo.base.secureApp.ObContext";

  private String user;
  private String role;
  private String language;
  private String theme;
  private String client;
  private String organization;
  private String warehouse;
  private String command;
  private String userClient;
  private String userOrganization;
  private String dbSessionID;
  private String javaDateFormat;
  private String javaDateTimeFormat;
  private String jsDateFormat;
  private String sqlDateFormat;
  private String accessLevel;

  private boolean roleSecurity;

  /**
   * 
   */
  public ProcessContext() {

  }

  /**
   * @param vars
   */
  public ProcessContext(VariablesSecureApp vars) {
    this.user = vars.getUser();
    this.role = vars.getRole();
    this.language = vars.getLanguage();
    this.theme = vars.getTheme();
    this.client = vars.getClient();
    this.organization = vars.getOrg();
    this.warehouse = vars.getWarehouse();
    this.command = vars.getCommand();
    this.userClient = vars.getUserClient();
    this.userOrganization = vars.getUserOrg();
    this.dbSessionID = vars.getDBSession();
    this.javaDateFormat = vars.getJavaDateFormat();
    this.javaDateTimeFormat = vars.getJavaDataTimeFormat();
    this.jsDateFormat = vars.getJsDateFormat();
    this.sqlDateFormat = vars.getSqlDateFormat();
    this.accessLevel = vars.getAccessLevel();

    this.roleSecurity = true;

  }

  /**
   * Create a request with the selected client and organization.
   * 
   * @param vars
   * @param client
   * @param org
   */
  public ProcessContext(VariablesSecureApp vars, String client, String org, boolean roleSecurity) {
    this(vars);
    this.client = client;
    this.organization = org;
    this.roleSecurity = roleSecurity;
  }

  /**
   * @return a new instance of {@link VariablesSecureApp} created from the client, organization and
   *         user.
   * @see #getClient()
   * @see #getOrganization()
   * @see #getUser()
   */
  public VariablesSecureApp toVars() {
    return new VariablesSecureApp(user, client, organization, role, language);
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @return the role
   */
  public String getRole() {
    return role;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @return the theme
   */
  public String getTheme() {
    return theme;
  }

  /**
   * @return the client
   */
  public String getClient() {
    return client;
  }

  /**
   * @return the organization
   */
  public String getOrganization() {
    return organization;
  }

  /**
   * @return the warehouse
   */
  public String getWarehouse() {
    return warehouse;
  }

  /**
   * @return the command
   */
  public String getCommand() {
    return command;
  }

  /**
   * @return the userClient
   */
  public String getUserClient() {
    return userClient;
  }

  /**
   * @return the userOrganization
   */
  public String getUserOrganization() {
    return userOrganization;
  }

  /**
   * @return the dbSessionID
   */
  public String getDbSessionID() {
    return dbSessionID;
  }

  /**
   * @return the javaDateFormat
   */
  public String getJavaDateFormat() {
    return javaDateFormat;
  }

  /**
   * @return the javaDateTimeFormat
   */
  public String getJavaDateTimeFormat() {
    return javaDateTimeFormat;
  }

  /**
   * @return the jsDateFormat
   */
  public String getJsDateFormat() {
    return jsDateFormat;
  }

  /**
   * @return the sqlDateFormat
   */
  public String getSqlDateFormat() {
    return sqlDateFormat;
  }

  /**
   * @return the accessLevel
   */
  public String getAccessLevel() {
    return accessLevel;
  }

  /**
   * @return whether the security for the process is based on role.
   */
  public boolean isRoleSecurity() {
    return roleSecurity;
  }

  /**
   * @return a JSON string representation of this obContext
   */
  public String toString() {
    XStream xstream = new XStream(new JettisonMappedXmlDriver());
    return xstream.toXML(this);
  }

  /**
   * @param arg0
   *          the xml to create the process context from
   * @return a new instance created from the string argument (an xml definition)
   */
  public synchronized static ProcessContext newInstance(String arg0) {
    if (arg0 == null || arg0.trim().equals("")) {
      return null;
    }
    XStream xstream = new XStream(new JettisonMappedXmlDriver());
    return (ProcessContext) xstream.fromXML(arg0);
  }

}
