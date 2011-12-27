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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.myob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.ApplicationUtils;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Creates the properties list which is initially loaded in the client.
 * 
 * @author mtaal
 */
public class MyOpenbravoComponent extends BaseTemplateComponent {

  static final String COMPONENT_ID = "MyOpenbravo";
  private static final String TEMPLATEID = "CA8047B522B44F61831A8CAA3AE2A7CD";

  private List<WidgetInstance> widgets = null;
  private List<String> widgetClassDefinitions = null;
  private Logger log = Logger.getLogger(MyOpenbravoComponent.class);

  @Inject
  private MyOBUtils myOBUtils;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseTemplateComponent#getComponentTemplate()
   */
  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATEID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseComponent#getId()
   */
  public String getId() {
    return COMPONENT_ID;
  }

  public List<String> getAvailableWidgetClasses() throws Exception {
    return getAvailableWidgetClasses(null);
  }

  public List<String> getAvailableWidgetClasses(String roleId) throws Exception {
    OBContext.setAdminMode();
    try {
      if (widgetClassDefinitions != null) {
        return widgetClassDefinitions;
      }

      final List<JSONObject> definitions = new ArrayList<JSONObject>();
      final List<String> tmp = new ArrayList<String>();
      String classDef = "";
      final OBQuery<WidgetClass> widgetClassesQry = OBDal.getInstance().createQuery(
          WidgetClass.class, WidgetClass.PROPERTY_SUPERCLASS + " is false");
      for (WidgetClass widgetClass : widgetClassesQry.list()) {
        if (isAccessible(widgetClass, roleId)) {
          final WidgetProvider widgetProvider = myOBUtils.getWidgetProvider(widgetClass);
          if (!widgetProvider.validate()) {
            continue;
          }
          definitions.add(widgetProvider.getWidgetClassDefinition());
          try {
            classDef = widgetProvider.generate();
            classDef = classDef.substring(0, classDef.length() - 1);
            tmp.add(classDef);
          } catch (Exception e) {
            // Do nothing as the definition is already in a loaded js file
          }
        }
      }
      Collections.sort(definitions, new WidgetClassComparator());

      widgetClassDefinitions = new ArrayList<String>();
      for (JSONObject json : definitions) {
        widgetClassDefinitions.add(json.toString());
      }
      widgetClassDefinitions.addAll(tmp);
      log.debug("Available Widget Classes: " + widgetClassDefinitions.size());
      return widgetClassDefinitions;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<String> getWidgetInstanceDefinitions() {
    OBContext.setAdminMode();
    try {
      final List<String> result = new ArrayList<String>();
      for (WidgetInstance widget : getContextWidgetInstances()) {
        final JSONObject jsonObject = myOBUtils.getWidgetProvider(widget.getWidgetClass())
            .getWidgetInstanceDefinition(widget);
        result.add(jsonObject.toString());
      }
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getEnableAdminMode() {
    if (ApplicationUtils.isClientAdmin() || ApplicationUtils.isOrgAdmin()
        || ApplicationUtils.isRoleAdmin()) {
      return "true";
    }
    return "false";
  }

  // when changing code, check the ApplicationUtils.getAdminFormSettings
  // method also
  public String getAdminModeValueMap() {
    if (getEnableAdminMode().equals("false")) {
      return "{}";
    }

    try {

      final JSONObject valueMap = new JSONObject();
      final JSONObject jsonLevels = new JSONObject();

      final Role currentRole = OBDal.getInstance().get(Role.class,
          OBContext.getOBContext().getRole().getId());

      if (currentRole.getId().equals("0")) {
        Map<String, String> systemLevel = new HashMap<String, String>();
        systemLevel.put("system", "OBKMO_AdminLevelSystem");
        valueMap.put("level", systemLevel);
        valueMap.put("levelValue", JSONObject.NULL);
        return valueMap.toString();
      }

      final List<RoleOrganization> adminOrgs = ApplicationUtils.getAdminOrgs();
      final List<UserRoles> adminRoles = ApplicationUtils.getAdminRoles();

      if (ApplicationUtils.isClientAdmin()) {
        jsonLevels.put("client", "OBKMO_AdminLevelClient");
      }

      if (adminOrgs.size() > 0) {
        jsonLevels.put("org", "OBKMO_AdminLevelOrg");
      }

      if (adminRoles.size() > 0) {
        jsonLevels.put("role", "OBKMO_AdminLevelRole");
      }

      valueMap.put("level", jsonLevels);

      final Map<String, String> client = new HashMap<String, String>();
      client.put(OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
          .getCurrentClient().getName());

      final Map<String, String> org = new HashMap<String, String>();
      for (RoleOrganization currentRoleOrg : adminOrgs) {
        org.put(currentRoleOrg.getOrganization().getId(), currentRoleOrg.getOrganization()
            .getName());
      }

      final Map<String, String> role = new HashMap<String, String>();
      for (UserRoles currentUserRole : adminRoles) {
        role.put(currentUserRole.getRole().getId(), currentUserRole.getRole().getName());
      }

      final JSONObject levelValueMap = new JSONObject();
      levelValueMap.put("client", client);
      levelValueMap.put("org", org);
      levelValueMap.put("role", role);

      valueMap.put("levelValue", levelValueMap);

      return valueMap.toString();
    } catch (JSONException e) {
      log.error("Error building 'Admin Mode' value map: " + e.getMessage(), e);
    }
    return "{}";
  }

  @Override
  public String getETag() {
    return UUID.randomUUID().toString();
  }

  private List<WidgetInstance> getContextWidgetInstances() {
    if (widgets != null) {
      return widgets;
    }
    copyWidgets();

    widgets = new ArrayList<WidgetInstance>();
    final List<WidgetInstance> userWidgets = new ArrayList<WidgetInstance>(
        MyOBUtils.getUserWidgetInstances());
    log.debug("Defined User widgets:" + userWidgets.size());
    // filter on the basis of role access
    for (WidgetInstance widget : userWidgets) {
      if (isAccessible(widget.getWidgetClass(), null)) {
        widgets.add(widget);
      }
    }

    log.debug("Available User widgets:" + widgets.size());
    return widgets;
  }

  private void copyWidgets() {
    final List<WidgetInstance> userWidgets = new ArrayList<WidgetInstance>(
        MyOBUtils.getUserWidgetInstances(false));
    final User user = OBDal.getInstance().get(User.class,
        OBContext.getOBContext().getUser().getId());
    final Role role = OBDal.getInstance().get(Role.class,
        OBContext.getOBContext().getRole().getId());
    final Client client = OBDal.getInstance().get(Client.class,
        OBContext.getOBContext().getCurrentClient().getId());
    final Set<WidgetInstance> defaultWidgets = new HashSet<WidgetInstance>();
    defaultWidgets.addAll(MyOBUtils.getDefaultWidgetInstances("OB", null));
    defaultWidgets.addAll(MyOBUtils.getDefaultWidgetInstances("SYSTEM", null));
    defaultWidgets.addAll(MyOBUtils.getDefaultWidgetInstances("CLIENT",
        new String[] { client.getId() }));
    final Set<String> orgs = OBContext.getOBContext().getWritableOrganizations();
    for (String org : orgs) {
      defaultWidgets.addAll(MyOBUtils.getDefaultWidgetInstances("ORG", new String[] { org }));
    }
    defaultWidgets
        .addAll(MyOBUtils.getDefaultWidgetInstances("ROLE", new String[] { role.getId() }));
    log.debug("Copying new widget instances on user: " + user.getId() + " role: " + role.getId());

    // remove the default widgets which are already defined on the user
    for (WidgetInstance widget : userWidgets) {
      if (widget.getCopiedFrom() != null) {
        defaultWidgets.remove(widget.getCopiedFrom());
      }
    }
    // now copy all the default widgets that are not defined on the user
    final Organization orgZero = OBDal.getInstance().get(Organization.class, "0");
    boolean copyDone = false;
    for (WidgetInstance widget : defaultWidgets) {
      final WidgetInstance copy = (WidgetInstance) DalUtil.copy(widget);
      copy.setClient(client);
      copy.setOrganization(orgZero);
      copy.setVisibleAtRole(role);
      copy.setVisibleAtUser(user);
      copy.setCopiedFrom(widget);
      OBDal.getInstance().save(copy);
      log.debug("Copied widget instance: " + copy.getId() + " of Widget Class: "
          + copy.getWidgetClass().getWidgetTitle());
      copyDone = true;
    }
    if (copyDone) {
      OBDal.getInstance().flush();
    }
  }

  private boolean isAccessible(WidgetClass widgetClass, String _roleId) {
    if (widgetClass.isAllowAnonymousAccess()) {
      return true;
    }
    String roleId = _roleId;
    if (StringUtils.isEmpty(roleId)) {
      roleId = OBContext.getOBContext().getRole().getId();
    }
    for (WidgetClassAccess widgetClassAccess : widgetClass.getOBKMOWidgetClassAccessList()) {
      if (DalUtil.getId(widgetClassAccess.getRole()).equals(roleId)) {
        return true;
      }
    }
    return false;
  }

  private class WidgetClassComparator implements Comparator<JSONObject> {

    @Override
    public int compare(JSONObject arg0, JSONObject arg1) {
      try {
        final String title0 = arg0.getString(WidgetProvider.TITLE);
        final String title1 = arg1.getString(WidgetProvider.TITLE);
        return title0.compareTo(title1);
      } catch (Exception e) {
        throw new OBException(e);
      }
    }

  }
}
