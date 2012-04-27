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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.navigationbarcomponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.KernelServlet;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.LicenseRestriction;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.utils.FormatUtilities;

/**
 * Action handler checks if there are alerts and if so returns these as a json object.
 * 
 * Action handler also updates the session ping value in the database.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class UserInfoWidgetActionHandler extends BaseActionHandler {

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseActionHandler#execute(Map, String)
   */
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    final String command = (String) parameters.get(ApplicationConstants.COMMAND);
    OBContext.setAdminMode();
    try {
      if (command == null) {
        throw new IllegalArgumentException("command parameter not specified");
      }
      if (command.equals(ApplicationConstants.SAVE_COMMAND)) {
        return executeSaveCommand(parameters, content);
      } else if (command.equals(ApplicationConstants.DATA_COMMAND)) {
        return executeDataCommand(parameters, content);
      } else if (command.equals(ApplicationConstants.CHANGE_PWD_COMMAND)) {
        return executeChangePasswordCommand(parameters, content);
      }
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
    throw new IllegalArgumentException("Illegal command value: " + command);
  }

  protected JSONObject executeChangePasswordCommand(Map<String, Object> parameters, String content)
      throws Exception {
    // do some checking
    final User user = OBDal.getInstance().get(User.class,
        OBContext.getOBContext().getUser().getId());
    final JSONObject json = new JSONObject(content);
    final String currentPwd = json.getString("currentPwd");
    final String newPwd = json.getString("newPwd");
    final String confirmPwd = json.getString("confirmPwd");

    if (!user.getPassword().equals(FormatUtilities.sha1Base64(currentPwd))) {
      final JSONObject result = new JSONObject();
      result.put("result", "error");
      final JSONArray fields = new JSONArray();
      final JSONObject field = new JSONObject();
      field.put("field", "currentPwd");
      field.put("messageCode", "UINAVBA_CurrentPwdIncorrect");
      fields.put(field);
      result.put("fields", fields);
      return result;
    }
    if (newPwd == null || newPwd.trim().length() == 0) {
      final JSONObject result = new JSONObject();
      result.put("result", "error");
      final JSONArray fields = new JSONArray();
      final JSONObject field = new JSONObject();
      field.put("field", "currentPwd");
      field.put("messageCode", "UINAVBA_IncorrectPwd");
      fields.put(field);
      result.put("fields", fields);
      return result;
    }
    if (!newPwd.equals(confirmPwd)) {
      final JSONObject result = new JSONObject();
      final JSONArray fields = new JSONArray();
      final JSONObject field = new JSONObject();
      field.put("field", "currentPwd");
      field.put("messageCode", "UINAVBA_UnequalPwd");
      fields.put(field);
      result.put("fields", fields);
      return result;
    }
    user.setPassword(FormatUtilities.sha1Base64(newPwd));
    OBDal.getInstance().flush();
    return ApplicationConstants.ACTION_RESULT_SUCCESS;
  }

  protected JSONObject executeDataCommand(Map<String, Object> parameters, String content)
      throws Exception {
    final JSONObject result = new JSONObject();
    result.put("language", createLanguageFormItemInfo());
    result.put("initialValues", createInitialValues());
    result.put("role", createRoleInfo(parameters));
    return result;
  }

  private JSONObject createRoleInfo(Map<String, Object> parameters) throws JSONException {
    final JSONObject formItemInfo = new JSONObject();
    formItemInfo.put("value", OBContext.getOBContext().getRole().getId());
    final List<Role> roles = getRoles(parameters);
    final List<Role> sortedRoles = new ArrayList<Role>(roles);
    DalUtil.sortByIdentifier(sortedRoles);
    final JSONArray valueMap = new JSONArray();
    for (Role role : sortedRoles) {
      final JSONObject valueMapItem = new JSONObject();
      valueMapItem.put(JsonConstants.ID, role.getId());
      valueMapItem.put(JsonConstants.IDENTIFIER, role.getIdentifier() + " - "
          + role.getClient().getIdentifier());
      valueMap.put(valueMapItem);
    }
    formItemInfo.put("valueMap", valueMap);

    // now for each role store the information
    final JSONArray jsonRoles = new JSONArray();

    for (Role role : roles) {
      final JSONObject jsonRole = new JSONObject();
      jsonRole.put("id", role.getId());

      jsonRole.put("client", role.getClient().getIdentifier());

      // now set the organizations
      final List<Organization> orgs = getOrganizations(role.getId());
      final JSONArray orgValueMap = new JSONArray();
      for (Organization org : orgs) {
        final JSONObject orgValueMapItem = new JSONObject();
        orgValueMapItem.put(JsonConstants.ID, org.getId());
        orgValueMapItem.put(JsonConstants.IDENTIFIER, org.getIdentifier());
        orgValueMap.put(orgValueMapItem);
      }
      jsonRole.put("organizationValueMap", orgValueMap);
      jsonRole.put("warehouseOrgMap", getWarehouses(role.getClient().getId()));
      jsonRoles.put(jsonRole);
    }
    formItemInfo.put("roles", jsonRoles);
    return formItemInfo;
  }

  private JSONArray getWarehouses(String clientId) throws JSONException {
    List<JSONObject> orgWarehouseArray = new ArrayList<JSONObject>();
    final OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(clientId);
    OBCriteria<Organization> orgs = OBDal.getInstance().createCriteria(Organization.class);
    for (Organization org : orgs.list()) {
      JSONObject orgWarehouse = new JSONObject();
      orgWarehouse.put("orgId", org.getId());
      final OBQuery<Warehouse> warehouses = OBDal
          .getInstance()
          .createQuery(Warehouse.class,
              "organization.id in (:orgList) and client.id=:clientId and organization.active=true order by name");
      warehouses.setNamedParameter("orgList", osp.getNaturalTree(org.getId()));
      warehouses.setNamedParameter("clientId", clientId);
      warehouses.setFilterOnReadableClients(false);
      warehouses.setFilterOnReadableOrganization(false);
      orgWarehouse.put("warehouseMap", createValueMapObject(warehouses.list()));
      orgWarehouseArray.add(orgWarehouse);
    }
    return new JSONArray(orgWarehouseArray);
  }

  private List<Organization> getOrganizations(String roleId) throws JSONException {
    final OBQuery<RoleOrganization> roleOrgs = OBDal.getInstance().createQuery(
        RoleOrganization.class, "role.id=:roleId and organization.active=true");
    roleOrgs.setFilterOnReadableClients(false);
    roleOrgs.setFilterOnReadableOrganization(false);
    roleOrgs.setNamedParameter("roleId", roleId);
    final List<Organization> orgs = new ArrayList<Organization>();
    for (RoleOrganization roleOrg : roleOrgs.list()) {
      if (!orgs.contains(roleOrg.getOrganization())) {
        orgs.add(roleOrg.getOrganization());
      }
    }
    DalUtil.sortByIdentifier(orgs);
    return orgs;
  }

  private JSONObject createLanguageFormItemInfo() throws JSONException {
    final JSONObject formItemInfo = new JSONObject();
    formItemInfo.put("value", OBContext.getOBContext().getLanguage().getId());

    final OBQuery<Language> languages = OBDal.getInstance().createQuery(
        Language.class,
        "(" + Language.PROPERTY_SYSTEMLANGUAGE + "=true or " + Language.PROPERTY_BASELANGUAGE
            + "=true)");
    languages.setFilterOnReadableClients(false);
    languages.setFilterOnReadableOrganization(false);
    formItemInfo.put("valueMap", createValueMapObject(languages.list()));
    return formItemInfo;
  }

  private JSONObject createInitialValues() throws JSONException {
    final JSONObject initialValues = new JSONObject();
    // TODO: externalize these strings
    initialValues.put("language", OBContext.getOBContext().getLanguage().getId());
    initialValues.put("role", OBContext.getOBContext().getRole().getId());
    initialValues.put("client", OBContext.getOBContext().getRole().getClient().getIdentifier());
    initialValues.put("organization", OBContext.getOBContext().getCurrentOrganization().getId());
    if (OBContext.getOBContext().getWarehouse() != null) {
      initialValues.put("warehouse", OBContext.getOBContext().getWarehouse().getId());
    }
    return initialValues;
  }

  private JSONArray createValueMapObject(List<? extends BaseOBObject> objects) throws JSONException {
    // sort the list by their identifier
    DalUtil.sortByIdentifier(objects);
    final JSONArray jsonArray = new JSONArray();
    for (BaseOBObject bob : objects) {
      final JSONObject jsonArrayItem = new JSONObject();
      jsonArrayItem.put(JsonConstants.ID, (String) bob.getId());
      jsonArrayItem.put(JsonConstants.IDENTIFIER, (String) bob.getIdentifier());
      jsonArray.put(jsonArrayItem);
    }
    return jsonArray;
  }

  protected List<Role> getRoles(Map<String, Object> parameters) {
    ActivationKey ak = ActivationKey.getInstance();
    SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
    boolean correctSystemStatus = sysInfo.getSystemStatus() == null
        || KernelServlet.getGlobalParameters().getOBProperty("safe.mode", "false")
            .equalsIgnoreCase("false") || sysInfo.getSystemStatus().equals("RB70");
    if (!correctSystemStatus) {
      return Collections.singletonList(OBDal.getInstance().get(Role.class, "0"));
    }

    if (parameters.get(KernelConstants.HTTP_SESSION) != null) {
      final HttpSession session = (HttpSession) parameters.get(KernelConstants.HTTP_SESSION);
      final String dbSessionId = (String) session.getAttribute("#AD_Session_ID".toUpperCase());
      LicenseRestriction limitation = ak.checkOPSLimitations(dbSessionId);
      if (limitation == LicenseRestriction.OPS_INSTANCE_NOT_ACTIVE
          || limitation == LicenseRestriction.NUMBER_OF_CONCURRENT_USERS_REACHED
          || limitation == LicenseRestriction.MODULE_EXPIRED
          || limitation == LicenseRestriction.NOT_MATCHED_INSTANCE
          || limitation == LicenseRestriction.HB_NOT_ACTIVE) {
        return Collections.singletonList(OBDal.getInstance().get(Role.class, "0"));
      }
    }

    // return the complete role list

    // "        SELECT A_R.AD_ROLE_ID, A_R.NAME, A_R.AD_CLIENT_ID, A_R.CLIENTLIST " +
    // "        FROM AD_ROLE A_R, AD_USER_ROLES A_U_R " +
    // "        WHERE A_R.AD_ROLE_ID = A_U_R.AD_ROLE_ID " +
    // "        AND A_U_R.ISACTIVE = 'Y' " +
    // "        AND A_R.ISACTIVE = 'Y' " +
    // "        AND A_U_R.AD_USER_ID = ?" +
    // "        ORDER BY A_R.NAME";
    final OBQuery<UserRoles> rolesQuery = OBDal.getInstance().createQuery(UserRoles.class,
        " userContact.id=? and role.active=true");
    rolesQuery.setFilterOnReadableClients(false);
    rolesQuery.setFilterOnReadableOrganization(false);
    rolesQuery.setParameters(Collections.singletonList((Object) OBContext.getOBContext().getUser()
        .getId()));
    final List<Role> result = new ArrayList<Role>();
    for (UserRoles userRole : rolesQuery.list()) {
      if (!result.contains(userRole.getRole())) {
        result.add(userRole.getRole());
      }
    }
    return result;
  }

  protected JSONObject executeSaveCommand(Map<String, Object> parameters, String content)
      throws Exception {
    final HttpServletRequest request = (HttpServletRequest) parameters
        .get(KernelConstants.HTTP_REQUEST);
    final JSONObject json = new JSONObject(content);
    final String orgId = getStringValue(json, "organization");
    final String roleId = getStringValue(json, "role");
    final Role role = OBDal.getInstance().get(Role.class, roleId);
    final String clientId = role.getClient().getId();
    final String warehouseId = getStringValue(json, "warehouse");
    String languageId = getStringValue(json, "language");
    if (languageId == null) {
      // If the default language the user has is not a system language, then another language will
      // be automatically selected
      languageId = pickLanguage();
    }
    final boolean isDefault;
    if (json.has("default")) {
      isDefault = json.getBoolean("default");
    } else {
      isDefault = false;
    }

    new UserSessionSetter().resetSession(request, isDefault, OBContext.getOBContext().getUser()
        .getId(), roleId, clientId, orgId, languageId, warehouseId);

    return ApplicationConstants.ACTION_RESULT_SUCCESS;
  }

  private String pickLanguage() {
    final OBQuery<Language> languages = OBDal.getInstance().createQuery(
        Language.class,
        "(" + Language.PROPERTY_SYSTEMLANGUAGE + "=true or " + Language.PROPERTY_BASELANGUAGE
            + "=true)");
    languages.setFilterOnReadableClients(false);
    languages.setFilterOnReadableOrganization(false);
    List<Language> languagesList = languages.list();

    Client client = OBContext.getOBContext().getCurrentClient();
    Language clientLanguage = client.getLanguage();
    if (clientLanguage != null && languagesList.contains(clientLanguage)) {
      return clientLanguage.getId();
    } else {
      return languagesList.get(0).getId();
    }
  }

  private String getStringValue(JSONObject json, String name) throws JSONException {
    if (json.isNull(name)) {
      return null;
    }
    if (!json.has(name)) {
      return null;
    }
    return json.getString(name);
  }

  // ugly inheriting from HttpSecureAppServlet because it provides a number of methods...
  private static class UserSessionSetter extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    private void resetSession(HttpServletRequest request, boolean isDefault, String userId,
        String roleId, String clientId, String organizationId, String languageId, String warehouseId)
        throws Exception {
      final VariablesSecureApp vars = new VariablesSecureApp(request); // refresh
      final Language language = OBDal.getInstance().get(Language.class, languageId);
      if (language.isRTLLanguage()) {
        vars.setSessionValue("#TextDirection", "RTL");
      } else {
        vars.setSessionValue("#TextDirection", "LTR");
      }

      if (isDefault) {
        final User user = OBDal.getInstance().get(User.class, userId);
        user.setDefaultClient(OBDal.getInstance().get(Client.class, clientId));
        user.setDefaultOrganization(OBDal.getInstance().get(Organization.class, organizationId));
        user.setDefaultRole(OBDal.getInstance().get(Role.class, roleId));
        user.setDefaultLanguage(OBDal.getInstance().get(Language.class, languageId));
        if (warehouseId != null) {
          user.setDefaultWarehouse(OBDal.getInstance().get(Warehouse.class, warehouseId));
        }
        OBDal.getInstance().save(user);
        OBDal.getInstance().flush();
      }

      if (clientId == null || organizationId == null || roleId == null) {
        throw new IllegalArgumentException("Illegal values for client/org or role " + clientId
            + "/" + organizationId + "/" + roleId);
      }

      // Clear session variables maintaining session and user
      String sessionID = vars.getSessionValue("#AD_Session_ID");
      String sessionUser = (String) request.getSession(true).getAttribute("#Authenticated_user");
      vars.clearSession(false);
      vars.setSessionValue("#AD_Session_ID", sessionID);
      request.getSession(true).setAttribute("#Authenticated_user", sessionUser);

      boolean result = LoginUtils
          .fillSessionArguments(new DalConnectionProvider(), vars, userId,
              toSaveStr(language.getLanguage()), (language.isRTLLanguage() ? "Y" : "N"),
              toSaveStr(roleId), toSaveStr(clientId), toSaveStr(organizationId),
              toSaveStr(warehouseId));
      if (!result) {
        throw new IllegalArgumentException("Error when saving default values");
      }
      readProperties(vars, KernelServlet.getGlobalParameters().getOpenbravoPropertiesPath());
      readNumberFormat(vars, KernelServlet.getGlobalParameters().getFormatPath());
    }

    private String toSaveStr(String value) {
      if (value == null) {
        return "";
      }
      return value;
    }
  }

}