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
package org.openbravo.base.secureApp;

import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.utils.FormatUtilities;

public class LoginUtils {

  public static Logger log4j = Logger.getLogger(LoginUtils.class);

  /** Creates a new instance of LoginUtils */
  private LoginUtils() {
  }

  /**
   * Returns a userId which matches the login and password. If no user is found then null is
   * returned. The combination of login and password is used to find the user.
   * 
   * Blocking users is taking into account
   * 
   * Note that only active users are returned.
   * 
   * @param connectionProvider
   *          , see the {@link DalConnectionProvider} for an instance of a ConnectionProvider for
   *          the DAL.
   * @param login
   *          the login
   * @param unHashedPassword
   *          the password, the unhashed password as it is entered by the user.
   * @return the user id or null if no user could be found or the user is locked.
   * @see FormatUtilities#sha1Base64(String)
   */
  public static String getValidUserId(ConnectionProvider connectionProvider, String login,
      String unHashedPassword) {
    try {
      // Deley response and check for locked user
      UserLock lockSettings = new UserLock(login);
      lockSettings.delayResponse();
      if (lockSettings.isLockedUser()) {
        return null;
      }

      final String userId = checkUserPassword(connectionProvider, login, unHashedPassword);
      if (userId == null) {
        lockSettings.addFail();
      }
      return userId;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Similar to {@link LoginUtils#getValidUserId(ConnectionProvider, String, String)} but not
   * blocking user accounts.
   * 
   */
  public static String checkUserPassword(ConnectionProvider connectionProvider, String login,
      String unHashedPassword) {
    try {
      final String hashedPassword = FormatUtilities.sha1Base64(unHashedPassword);
      final String userId = SeguridadData.valido(connectionProvider, login, hashedPassword);
      if (userId.equals("-1")) {
        return null;
      }

      return userId;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  static boolean validUserRole(ConnectionProvider conn, String strUserAuth, String strRol)
      throws ServletException {
    boolean valid = SeguridadData.isUserRole(conn, strUserAuth, strRol);
    if (!valid) {
      log4j.error("Login role is not in user roles list");
      log4j.error("User: " + strUserAuth);
      log4j.error("Role: " + strRol);
    }
    return valid;
  }

  static boolean validRoleClient(ConnectionProvider conn, String strRol, String strCliente)
      throws ServletException {
    boolean valid = SeguridadData.isRoleClient(conn, strRol, strCliente);
    if (!valid) {
      log4j.error("Login client is not in role clients list");
    }
    return valid;
  }

  static boolean validRoleOrg(ConnectionProvider conn, String strRol, String strOrg)
      throws ServletException {
    boolean valid = SeguridadData.isLoginRoleOrg(conn, strRol, strOrg);
    if (!valid) {
      log4j.error("Login organization is not in role organizations list");
    }
    return valid;
  }

  public static List<RoleOrganization> loadRoleOrganization(String strRol) {

    OBContext.setAdminMode();
    try {
      OBQuery<RoleOrganization> query = OBDal.getInstance().createQuery(RoleOrganization.class,
          "WHERE role.id = :roleId ORDER BY client.id, organization.id");
      query.setNamedParameter("roleId", strRol);
      query.setFilterOnReadableClients(false);
      query.setFilterOnReadableOrganization(false);
      return query.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static String buildClientList(List<RoleOrganization> roleorglist) {
    StringBuilder clientlist = new StringBuilder();
    String currentclient = null;
    for (RoleOrganization roleorg : roleorglist) {
      if (currentclient == null || !currentclient.equals(roleorg.getClient().getId())) {
        currentclient = roleorg.getClient().getId();
        if (clientlist.length() > 0) {
          clientlist.append(',');
        }
        clientlist.append('\'');
        clientlist.append(roleorg.getClient().getId());
        clientlist.append('\'');
      }
    }
    return clientlist.toString();
  }

  public static String buildOrgList(List<RoleOrganization> roleorglist) {
    StringBuilder orglist = new StringBuilder();
    for (RoleOrganization roleorg : roleorglist) {
      if (orglist.length() > 0) {
        orglist.append(',');
      }
      orglist.append('\'');
      orglist.append(roleorg.getOrganization().getId());
      orglist.append('\'');
    }
    return orglist.toString();
  }

  public static boolean fillSessionArguments(ConnectionProvider conn, VariablesSecureApp vars,
      String strUserAuth, String strLanguage, String strIsRTL, String strRol, String strCliente,
      String strOrg, String strAlmacen) throws ServletException {

    // variable to save organization currency
    AttributeData[] orgCurrency;

    // Check session options
    if (!validUserRole(conn, strUserAuth, strRol) || !validRoleClient(conn, strRol, strCliente)
        || !validRoleOrg(conn, strRol, strOrg)) {
      return false;
    }

    OBContext currentContext = OBContext.getOBContext();
    // set the obcontext
    try {
      OBContext.setOBContext(strUserAuth, strRol, strCliente, strOrg, strLanguage, strAlmacen);
    } catch (final OBSecurityException e) {
      log4j.error("Error trying to initialize OBContext: " + e.getMessage(), e);
      return false;
    }

    // Set session vars
    vars.setSessionValue("#AD_User_ID", strUserAuth);
    vars.setSessionValue("#SalesRep_ID", strUserAuth);
    vars.setSessionValue("#AD_Language", strLanguage);
    vars.setSessionValue("#AD_Role_ID", strRol);
    vars.setSessionValue("#AD_Client_ID", strCliente);
    vars.setSessionValue("#AD_Org_ID", strOrg);
    vars.setSessionValue("#M_Warehouse_ID", strAlmacen);

    vars.setSessionValue("#StdPrecision", "2");

    // Organizations tree
    // enable admin mode, as normal non admin-role
    // has no read-access to i.e. AD_OrgType
    OBContext.setAdminMode();
    try {

      OrgTree tree = new OrgTree(conn, strCliente);
      vars.setSessionObject("#CompleteOrgTree", tree);
      OrgTree accessibleTree = tree.getAccessibleTree(conn, strRol);
      vars.setSessionValue("#AccessibleOrgTree", accessibleTree.toString());
    } catch (Exception e) {
      log4j.warn("Error while setting Organzation tree to session " + e);
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }

    try {
      // set organization currency
      orgCurrency = AttributeData.selectOrgCurrency(conn, strOrg, strCliente);

      SeguridadData[] data = SeguridadData.select(conn, strRol, strUserAuth);
      if (data == null || data.length == 0) {
        OBContext.setOBContext(currentContext);
        return false;
      }

      List<RoleOrganization> datarolelist = loadRoleOrganization(strRol);

      vars.setSessionValue("#User_Level", data[0].userlevel);
      vars.setSessionValue("#User_Client", buildClientList(datarolelist));
      vars.setSessionValue("#User_Org", buildOrgList(datarolelist));
      vars.setSessionValue("#Approval_C_Currency_ID", data[0].cCurrencyId);
      vars.setSessionValue("#Approval_Amt", data[0].amtapproval);
      vars.setSessionValue("#Client_Value", data[0].value);
      vars.setSessionValue("#Client_SMTP", data[0].smtphost);

      data = null;
      AttributeData[] attr = AttributeData.select(conn,
          Utility.getContext(conn, vars, "#User_Client", "LoginHandler"),
          Utility.getContext(conn, vars, "#User_Org", "LoginHandler"));
      if (attr != null && attr.length > 0) {
        vars.setSessionValue("$C_AcctSchema_ID", attr[0].value);
        if (orgCurrency.length > 0) {
          vars.setSessionValue("$C_Currency_ID", orgCurrency[0].cCurrencyId);
        } else
          vars.setSessionValue("$C_Currency_ID", attr[0].attribute);
        vars.setSessionValue(
            "#StdPrecision",
            AttributeData.selectStdPrecision(conn, attr[0].attribute,
                Utility.getContext(conn, vars, "#User_Client", "LoginHandler"),
                Utility.getContext(conn, vars, "#User_Org", "LoginHandler")));
        vars.setSessionValue("$HasAlias", attr[0].hasalias);
        for (int i = 0; i < attr.length; i++)
          vars.setSessionValue("$Element_" + attr[i].elementtype, "Y");
      }
      attr = null;

      List<Preference> preferences = Preferences.getAllPreferences(strCliente, strOrg, strUserAuth,
          strRol);
      for (Preference preference : preferences) {
        Preferences.savePreferenceInSession(vars, preference);
      }

      attr = AttributeData.selectIsSOTrx(conn);
      if (attr != null && attr.length > 0) {
        for (int i = 0; i < attr.length; i++)
          vars.setSessionValue(attr[i].adWindowId + "|isSOTrx", attr[i].value);
      }
      attr = null;

      DefaultSessionValuesData[] ds = DefaultSessionValuesData.select(conn);
      if (ds != null && ds.length > 0) {
        for (int i = 0; i < ds.length; i++) {
          String value = DefaultValuesData.select(conn, ds[i].columnname, ds[i].tablename,
              Utility.getContext(conn, vars, "#User_Client", "LoginHandler"),
              Utility.getContext(conn, vars, "#User_Org", "LoginHandler"));
          if (ds[i].tablename.equals("C_DocType"))
            vars.setSessionValue("#C_DocTypeTarget_ID", value);
          vars.setSessionValue("#" + ds[i].columnname, value);
        }
      }
      vars.setSessionValue("#Date", Utility.getContext(conn, vars, "#Date", "LoginHandler"));
      vars.setSessionValue("#ShowTrl", Utility.getPreference(vars, "ShowTrl", ""));
      vars.setSessionValue("#ShowAcct", Utility.getPreference(vars, "ShowAcct", ""));
      vars.setSessionValue("#ShowAudit", Utility.getPreference(vars, "ShowAuditDefault", ""));
      vars.setSessionValue("#ShowConfirmation",
          Utility.getPreference(vars, "ShowConfirmationDefault", ""));
      vars.setSessionValue("#Autosave", Utility.getPreference(vars, "Autosave", ""));

      SystemPreferencesData[] dataSystem = SystemPreferencesData.select(conn);
      if (dataSystem != null && dataSystem.length > 0) {
        vars.setSessionValue("#RecordRange", dataSystem[0].tadRecordrange);
        vars.setSessionValue("#RecordRangeInfo", dataSystem[0].tadRecordrangeInfo);
        vars.setSessionValue("#Transactional$Range", dataSystem[0].tadTransactionalrange);
        if (strIsRTL.equals("Y")) {
          vars.setSessionValue("#Theme", "rtl/" + dataSystem[0].tadTheme);
          vars.setSessionValue("#TextDirection", "RTL");
        } else if (strIsRTL.equals("N")) {
          vars.setSessionValue("#Theme", "ltr/" + dataSystem[0].tadTheme);
          vars.setSessionValue("#TextDirection", "LTR");
        } else {
          OBContext.setOBContext(currentContext);
          log4j
              .error("Can't detect direction of language: ltr? rtl? parameter isRTL missing in call to LoginUtils.getStringParameter");
          return false;
        }
      }

    } catch (ServletException e) {
      OBContext.setOBContext(currentContext);
      log4j.warn("Error while loading session arguments: " + e);
      return false;
    }

    // Login process if finished, set the flag as not logging in
    // this flag may not be removed from the session, it must be set
    // to N to prevent re-initializing the session continuously
    // See the HttpSecureAppServlet
    vars.setSessionValue("#loggingIn", "N");
    return true;
  }

  /**
   * Obtains defaults defined for a user and throws DefaultValidationException in case they are not
   * correct.
   */
  public static RoleDefaults getLoginDefaults(String strUserAuth, String role, ConnectionProvider cp)
      throws ServletException, DefaultValidationException {
    String strRole = role;
    if (strRole.equals("")) {
      // use default role
      strRole = DefaultOptionsData.defaultRole(cp, strUserAuth);
      if (strRole == null || !LoginUtils.validUserRole(cp, strUserAuth, strRole)) {
        // if default not set or not valid take any one
        strRole = DefaultOptionsData.getDefaultRole(cp, strUserAuth);
      }
    }
    validateDefault(strRole, strUserAuth, "Role");

    String strOrg = DefaultOptionsData.defaultOrg(cp, strUserAuth);
    // use default org
    if (strOrg == null || !LoginUtils.validRoleOrg(cp, strRole, strOrg)) {
      // if default not set or not valid take any one
      strOrg = DefaultOptionsData.getDefaultOrg(cp, strRole);
    }
    validateDefault(strOrg, strRole, "Org");

    String strClient = DefaultOptionsData.defaultClient(cp, strUserAuth);
    // use default client
    if (strClient == null || !LoginUtils.validRoleClient(cp, strRole, strClient)) {
      // if default not set or not valid take any one
      strClient = DefaultOptionsData.getDefaultClient(cp, strRole);
    }
    validateDefault(strClient, strRole, "Client");

    String strWarehouse = DefaultOptionsData.defaultWarehouse(cp, strUserAuth);
    if (strWarehouse == null) {
      if (!strRole.equals("0")) {
        strWarehouse = DefaultOptionsData.getDefaultWarehouse(cp, strClient, new OrgTree(cp,
            strClient).getAccessibleTree(cp, strRole).toString());
      } else
        strWarehouse = "";
    }
    RoleDefaults defaults = new RoleDefaults();
    defaults.role = strRole;
    defaults.client = strClient;
    defaults.org = strOrg;
    defaults.warehouse = strWarehouse;
    return defaults;
  }

  /**
   * Validates if a selected default value is null or empty String
   * 
   * @param strValue
   * @param strKey
   * @param strError
   * @throws Exeption
   * */
  private static void validateDefault(String strValue, String strKey, String strError)
      throws DefaultValidationException {
    if (strValue == null || strValue.equals(""))
      throw new DefaultValidationException("Unable to read default " + strError + " for:" + strKey,
          strError);
  }

  /**
   * Utility class to store login defaults
   * 
   */
  static class RoleDefaults {
    String role;
    String client;
    String org;
    String warehouse;
  }
}
