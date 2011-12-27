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

package org.openbravo.client.application.personalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.ApplicationUtils;
import org.openbravo.client.application.UIPersonalization;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Handles personalization settings, stores them and retrieves them, taking into account priority
 * order.
 */
@RequestScoped
public class PersonalizationHandler {
  private static final Logger log = Logger.getLogger(PersonalizationHandler.class);

  /**
   * Returns all the personalization settings in an object keyed by tabid. The current client, org,
   * role and user are taken into account to find the correct personalization entry. If no
   * personalization record is present then null is returned for that specific tab.
   * 
   * @param window
   *          the window for which the personalization settings are to be returned.
   * @return the personalization settings in a json object for a window.
   */
  public JSONObject getPersonalizationForWindow(Window window) {
    OBContext.setAdminMode(false);
    try {
      // first get the form layouts per tab
      final JSONObject formPersonalization = new JSONObject();
      for (Tab tab : window.getADTabList()) {
        final UIPersonalization uiPersonalization = getPersonalizationForTab(tab);
        if (uiPersonalization == null || uiPersonalization.getValue() == null) {
          formPersonalization.put(tab.getId(), (Object) null);
        } else {
          try {
            final JSONObject persJSON = new JSONObject(uiPersonalization.getValue());
            // if on user level then allow delete
            if (uiPersonalization.getUser() != null) {
              persJSON.put("canDelete", true);
            }
            persJSON.put("personalizationId", uiPersonalization.getId());
            formPersonalization.put(tab.getId(), persJSON);
          } catch (Exception e) {
            // on purpose not rethrowing to be robust
            log.error("Exception when getting personalization records for window " + window, e);
          }
        }
      }

      final List<RoleOrganization> adminOrgs = ApplicationUtils.getAdminOrgs();
      final List<UserRoles> adminRoles = ApplicationUtils.getAdminRoles();

      // and get the personalization records on view level
      final List<UIPersonalization> personalizations = getPersonalizationsForWindow(window);
      final JSONArray windowPersonalization = new JSONArray();
      for (UIPersonalization uiPersonalization : personalizations) {
        try {
          final JSONObject persJSON = new JSONObject(uiPersonalization.getValue());

          if (canEdit(uiPersonalization, adminOrgs, adminRoles)) {
            persJSON.put("canEdit", true);
          }

          persJSON.put("clientId", getNullOrId(uiPersonalization.getVisibleAtClient()));
          persJSON.put("orgId", getNullOrId(uiPersonalization.getVisibleAtOrganization()));
          persJSON.put("roleId", getNullOrId(uiPersonalization.getVisibleAtRole()));
          persJSON.put("userId", getNullOrId(uiPersonalization.getUser()));
          persJSON.put("personalizationId", uiPersonalization.getId());
          windowPersonalization.put(persJSON);
        } catch (Exception e) {
          // on purpose not rethrowing to be robust
          log.error("Exception when getting personalization records for window " + window, e);
        }
      }

      final JSONObject result = new JSONObject();
      result.put("forms", formPersonalization);
      result.put("views", windowPersonalization);
      result.put("formData", getAdminFormSettings(adminOrgs, adminRoles));
      return result;
    } catch (Exception e) {
      throw new OBException("Exception when getting personalization settings for window " + window,
          e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private boolean canEdit(UIPersonalization uiPersonalization, List<RoleOrganization> adminOrgs,
      final List<UserRoles> adminRoles) {
    // if on user level then allow delete
    if (uiPersonalization.getUser() != null) {
      return true;
    }
    if (ApplicationUtils.isClientAdmin() && uiPersonalization.getVisibleAtClient() != null) {
      return true;
    }
    if (uiPersonalization.getVisibleAtOrganization() != null) {
      final String orgId = (String) DalUtil.getId(uiPersonalization.getVisibleAtOrganization());
      for (RoleOrganization roleOrg : adminOrgs) {
        if (DalUtil.getId(roleOrg).equals(orgId)) {
          return true;
        }
      }
    }
    if (uiPersonalization.getVisibleAtRole() != null) {
      final String roleId = (String) DalUtil.getId(uiPersonalization.getVisibleAtRole());
      for (UserRoles userRole : adminRoles) {
        if (DalUtil.getId(userRole.getRole()).equals(roleId)) {
          return true;
        }
      }
    }

    return false;
  }

  private Object getNullOrId(Object object) {
    if (object == null) {
      return null;
    }
    return DalUtil.getId(object);
  }

  // when changing code here, also check the
  // MyOpenbravoComponent.getAdminModeValueMap code
  private JSONObject getAdminFormSettings(List<RoleOrganization> adminOrgs,
      final List<UserRoles> adminRoles) {
    final JSONObject result = new JSONObject();
    if (!ApplicationUtils.isClientAdmin() && !ApplicationUtils.isOrgAdmin()
        && !ApplicationUtils.isRoleAdmin()) {
      return result;
    }

    try {

      final Role currentRole = OBDal.getInstance().get(Role.class,
          OBContext.getOBContext().getRole().getId());

      if (currentRole.getId().equals("0")) {
        result.put("system", true);
        return result;
      }

      if (ApplicationUtils.isClientAdmin()) {
        final JSONObject clientObject = new JSONObject();
        clientObject.put(OBContext.getOBContext().getCurrentClient().getId(), OBContext
            .getOBContext().getCurrentClient().getIdentifier());
        result.put("clients", clientObject);
      }

      final Map<String, String> orgs = new HashMap<String, String>();
      for (RoleOrganization currentRoleOrg : adminOrgs) {
        orgs.put(currentRoleOrg.getOrganization().getId(), currentRoleOrg.getOrganization()
            .getName());
      }

      final Map<String, String> roles = new HashMap<String, String>();
      for (UserRoles currentUserRole : adminRoles) {
        roles.put(currentUserRole.getRole().getId(), currentUserRole.getRole().getName());
      }

      if (orgs.size() > 0) {
        result.put("orgs", orgs);
      }
      if (roles.size() > 0) {
        result.put("roles", roles);
      }

      return result;
    } catch (JSONException e) {
      log.error("Error building 'Admin Mode' value map: " + e.getMessage(), e);
    }
    return result;
  }

  /**
   * Returns all the personalization settings for a tab. The current client, org, role and user are
   * taken into account to find the correct personalization entry. If no personalization entries are
   * present then null is returned.
   * 
   * @param tab
   *          the tab for which the personalization settings are to be returned.
   * @return the personalization settings in a json object for this tab.
   */
  public UIPersonalization getPersonalizationForTab(Tab tab) {
    OBContext.setAdminMode(false);
    try {
      return getPersonalization(OBContext.getOBContext().getCurrentClient().getId(), OBContext
          .getOBContext().getCurrentOrganization().getId(), OBContext.getOBContext().getRole()
          .getId(), OBContext.getOBContext().getUser().getId(), tab.getId(), null, false);
    } catch (Exception e) {
      throw new OBException("Exception when getting personalization settings for tab " + tab, e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  // note will return the personalizations in order of their priority
  private List<UIPersonalization> getPersonalizationsForWindow(Window window) {
    OBContext.setAdminMode(false);
    try {
      final List<UIPersonalization> personalizations = getPersonalizations(OBContext.getOBContext()
          .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
          OBContext.getOBContext().getUser().getId(), OBContext.getOBContext().getRole().getId(),
          null, window.getId(), false);
      sortPersonalizations(personalizations);
      return personalizations;
    } catch (Exception e) {
      throw new OBException("Exception when getting personalization settings for window " + window,
          e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void sortPersonalizations(List<UIPersonalization> personalizations) {
    final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();

    List<String> parentTree = null;
    if (orgId != null) {
      parentTree = OBContext.getOBContext()
          .getOrganizationStructureProvider(OBContext.getOBContext().getCurrentClient().getId())
          .getParentList(orgId, true);
    }
    Collections.sort(personalizations, new PersonalizationComparator(parentTree));
  }

  private static class PersonalizationComparator implements Comparator<UIPersonalization> {

    private List<String> parentTree;

    private PersonalizationComparator(List<String> parentTree) {
      this.parentTree = parentTree;
    }

    @Override
    public int compare(UIPersonalization o1, UIPersonalization o2) {
      final int higherPriority = isHigherPriority(o1, o2, parentTree);
      if (higherPriority == 1) {
        return 1;
      } else if (higherPriority == 2) {
        return -1;
      }
      return 0;
    }
  }

  public UIPersonalization getPersonalization(String clientId, String orgId, String roleId,
      String userId, String tabId, String windowId, boolean exactMatch) {
    OBContext.setAdminMode(false);
    try {
      final List<UIPersonalization> pers = getPersonalizations(clientId, orgId, userId, roleId,
          tabId, windowId, exactMatch);
      if (pers.isEmpty()) {
        return null;
      }
      if (exactMatch) {
        if (pers.size() > 1) {
          log.warn("There are/is more than one ui personalization record "
              + "for a certain exact match, ignoring it, just picking the first one: "
              + pers.get(0));
        }
        return pers.get(0);
      }

      // find the best match
      UIPersonalization selectedUIPersonalization = null;
      List<String> parentTree = null;
      if (orgId != null) {
        parentTree = OBContext.getOBContext().getOrganizationStructureProvider(clientId)
            .getParentList(orgId, true);
      }
      for (UIPersonalization uiPersonalization : pers) {
        // select the highest priority or raise exception in case of conflict
        if (selectedUIPersonalization == null) {
          selectedUIPersonalization = uiPersonalization;
          continue;
        }
        int higherPriority = isHigherPriority(selectedUIPersonalization, uiPersonalization,
            parentTree);
        switch (higherPriority) {
        case 1:
          // do nothing, selected one has higher priority
          break;
        case 2:
          selectedUIPersonalization = uiPersonalization;
          break;
        default:
          // conflict ignore
          break;
        }
      }
      return selectedUIPersonalization;
    } catch (Exception e) {
      // TODO: add param values to message
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Stores the personalization json object for a certain level, if there is no current record then
   * a new one is created and persisted. If the pers
   * 
   * @param persId
   *          if a specific personalization id is set then the system updates that record and
   *          ignores the other parameters.
   * @param clientId
   *          the client, maybe null
   * @param orgId
   *          the organization id, maybe null
   * @param roleId
   *          the role id, maybe null
   * @param userId
   *          the user id, maybe null
   * @param tabId
   *          the tab id, may not be null
   * @param target
   *          the personalization target, is either form or grid
   * @param value
   *          the value, a json string
   * @return the persisted record
   */
  public UIPersonalization storePersonalization(String persId, String clientId, String orgId,
      String roleId, String userId, String tabId, String windowId, String target, String value) {
    OBContext.setAdminMode(false);
    try {
      UIPersonalization uiPersonalization;
      if (persId != null) {
        uiPersonalization = OBDal.getInstance().get(UIPersonalization.class, persId);
        if (uiPersonalization == null) {
          throw new IllegalArgumentException("UI Personalization with id " + persId + " not found");
        }
      } else {
        uiPersonalization = getPersonalization(clientId, orgId, roleId, userId, tabId, windowId,
            true);
      }

      if (uiPersonalization == null) {
        uiPersonalization = OBProvider.getInstance().get(UIPersonalization.class);
        uiPersonalization.setClient(OBDal.getInstance().get(Client.class, "0"));
        uiPersonalization.setOrganization(OBDal.getInstance().get(Organization.class, "0"));

        if (tabId != null) {
          uiPersonalization.setTab(OBDal.getInstance().get(Tab.class, tabId));
          uiPersonalization.setType("Form");
        }

        if (windowId != null) {
          uiPersonalization.setWindow(OBDal.getInstance().get(Window.class, windowId));
          uiPersonalization.setType("Window");
        }
      }

      if (clientId != null) {
        uiPersonalization.setVisibleAtClient(OBDal.getInstance().get(Client.class, clientId));
        // also store it in that client
        uiPersonalization.setClient(uiPersonalization.getVisibleAtClient());
      } else {
        uiPersonalization.setVisibleAtClient(null);
      }

      if (orgId != null) {
        uiPersonalization.setVisibleAtOrganization(OBDal.getInstance().get(Organization.class,
            orgId));
      } else {
        uiPersonalization.setVisibleAtOrganization(null);
      }

      if (roleId != null) {
        uiPersonalization.setVisibleAtRole(OBDal.getInstance().get(Role.class, roleId));
      } else {
        uiPersonalization.setVisibleAtRole(null);
      }

      if (userId != null) {
        uiPersonalization.setUser(OBDal.getInstance().get(User.class, userId));
      } else {
        uiPersonalization.setUser(null);
      }

      final JSONObject jsonValue = new JSONObject(value);
      JSONObject jsonObject;
      if (uiPersonalization.getValue() != null) {
        jsonObject = new JSONObject(uiPersonalization.getValue());
      } else {
        jsonObject = new JSONObject();
      }
      jsonObject.put(target, (Object) jsonValue);
      uiPersonalization.setValue(jsonObject.toString());
      OBDal.getInstance().save(uiPersonalization);
      return uiPersonalization;
    } catch (Exception e) {
      // TODO: add param values to message
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static List<UIPersonalization> getPersonalizations(String clientId, String orgId,
      String userId, String roleId, String tabId, String windowId, boolean exactMatch) {

    List<Object> parameters = new ArrayList<Object>();
    StringBuilder hql = new StringBuilder();
    hql.append(" as p ");
    hql.append(" where ");
    if (exactMatch) {
      if (clientId != null) {
        hql.append(" p.visibleAtClient.id = ? ");
        parameters.add(clientId);
      } else {
        hql.append(" p.visibleAtClient is null");
      }

      if (orgId != null) {
        hql.append(" and p.visibleAtOrganization.id = ? ");
        parameters.add(orgId);
      } else {
        hql.append(" and p.visibleAtOrganization is null ");
      }

      if (userId != null) {
        hql.append(" and p.user.id = ? ");
        parameters.add(userId);
      } else {
        hql.append(" and p.user is null ");
      }

      if (roleId != null) {
        hql.append(" and p.visibleAtRole.id = ? ");
        parameters.add(roleId);
      } else {
        hql.append(" and p.visibleAtRole is null");
      }
    } else {
      if (clientId != null) {
        hql.append(" (p.visibleAtClient.id = ? or ");
        parameters.add(clientId);
      } else {
        hql.append(" (");
      }
      hql.append(" coalesce(p.visibleAtClient, '0')='0') ");

      if (roleId != null) {
        hql.append(" and   (p.visibleAtRole.id = ? or ");
        parameters.add(roleId);
      } else {
        hql.append(" and (");
      }
      hql.append(" p.visibleAtRole is null) ");

      // note orgId != null is handled below
      if (orgId == null) {
        hql.append(" and (coalesce(p.visibleAtOrganization, '0')='0'))");
      }

      if (userId != null) {
        hql.append("  and (p.user.id = ? or ");
        parameters.add(userId);
      } else {
        hql.append(" and (");
      }
      hql.append(" p.user is null) ");
    }

    if (tabId != null) {
      hql.append(" and  p.tab.id = ? ");
      parameters.add(tabId);
    } else {
      hql.append(" and  p.window.id = ? ");
      parameters.add(windowId);
    }

    OBQuery<UIPersonalization> qPers = OBDal.getInstance().createQuery(UIPersonalization.class,
        hql.toString());
    qPers.setParameters(parameters);
    List<UIPersonalization> personalizations = qPers.list();

    if (orgId != null && !exactMatch) {
      // Remove from list organization that are not visible
      final Organization org = OBDal.getInstance().get(Organization.class, orgId);
      List<String> parentTree = OBContext.getOBContext()
          .getOrganizationStructureProvider((String) DalUtil.getId(org.getClient()))
          .getParentList(orgId, true);
      List<UIPersonalization> auxPersonalizations = new ArrayList<UIPersonalization>();
      for (UIPersonalization pers : personalizations) {
        if (pers.getVisibleAtOrganization() == null
            || parentTree.contains(pers.getVisibleAtOrganization().getId())) {
          auxPersonalizations.add(pers);
        }
      }
      return auxPersonalizations;
    } else {
      return personalizations;
    }
  }

  /**
   * Determines which of the 2 personalizations has higher visibility priority.
   * 
   * @param pers1
   *          First personalization to compare
   * @param pers2
   *          Second personalization to compare
   * @param parentTree
   *          Parent tree of organizations including the current one, used to assign more priority
   *          to organizations nearer in the tree.
   * @return <ul>
   *         <li>1 in case pers1 is more visible than pers2
   *         <li>2 in case pers2 is more visible than pers1
   *         <li>0 in case of conflict (both have identical visibility and value)
   *         </ul>
   */
  private static int isHigherPriority(UIPersonalization pers1, UIPersonalization pers2,
      List<String> parentTree) {
    // Check priority by client
    if ((pers2.getVisibleAtClient() == null || pers2.getVisibleAtClient().getId().equals("0"))
        && pers1.getVisibleAtClient() != null && !pers1.getVisibleAtClient().getId().equals("0")) {
      return 1;
    }

    // Check priority by organization
    Organization org1 = pers1.getVisibleAtOrganization();
    Organization org2 = pers2.getVisibleAtOrganization();
    if (org1 != null && org2 == null) {
      return 1;
    }

    if ((org1 == null && org2 != null)) {
      return 2;
    }

    if (org1 != null && org2 != null) {
      int depth1 = parentTree.indexOf(org1.getId());
      int depth2 = parentTree.indexOf(org2.getId());

      if (depth1 < depth2) {
        return 1;
      } else if (depth1 > depth2) {
        return 2;
      }
    }

    // Check priority by user
    if (pers1.getUser() != null && pers2.getUser() == null) {
      return 1;
    }

    if (pers1.getUser() == null && pers2.getUser() != null) {
      return 2;
    }

    // Check priority by role
    if (pers1.getVisibleAtRole() != null && pers2.getVisibleAtRole() == null) {
      return 1;
    }

    if (pers1.getVisibleAtRole() == null && pers2.getVisibleAtRole() != null) {
      return 2;
    }

    // Actual conflict
    return 0;
  }
}
