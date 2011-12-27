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

package org.openbravo.erpCommon.businessUtility;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.PropertyConflictException;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Handles preferences, resolving priorities in case there are values for a same property at
 * different visibility levels
 * 
 */
public class Preferences {
  private static final Logger log4j = Logger.getLogger(Preferences.class);

  /**
   * Obtains a list of all preferences that are applicable at the given visibility level (client,
   * org, user, role).
   * <p>
   * In case of different values for a single property at a same visibility level, one of them is
   * taken.
   */
  public static List<Preference> getAllPreferences(String client, String org, String user,
      String role) {
    try {
      OBContext.setAdminMode();
      List<String> parentTree = OBContext.getOBContext().getOrganizationStructureProvider()
          .getParentList(org, true);

      ArrayList<Preference> preferences = new ArrayList<Preference>();
      for (Preference pref : getPreferences(null, false, client, org, user, role, null, false,
          false)) {
        Preference existentPreference = getPreferenceFromList(pref, preferences);
        if (existentPreference == null) {
          // There is not a preference for the current property, add it to the list
          preferences.add(pref);
        } else {
          // There is a preference for the current property, check whether it is higher priority and
          // if so replace it. In case of conflict leave current preference.
          if (isHigherPriority(pref, existentPreference, parentTree) == 1) {
            preferences.remove(existentPreference);
            preferences.add(pref);
          }
        }
      }
      return preferences;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Saves the property/value as a preference. If a preference with exactly the same visualization
   * priority already exists, it is overwritten; if not, a new one is created.
   * <p>
   * It also saves the new preference in session, in case the vars parameter is not null. If it is
   * null, the preference is not stored in session.
   * 
   * @param property
   *          Name of the property or attribute for the preference.
   * @param value
   *          New value to set.
   * @param isListProperty
   *          Determines whether list of properties or attribute should be used.
   * @param client
   *          Client visibility.
   * @param org
   *          Organization visibility.
   * @param user
   *          User visibility.
   * @param role
   *          Role visibility.
   * @param window
   *          Window visibility.
   * @param vars
   *          VariablesSecureApp to store new property value.
   * @return The preference that has been created or modified
   */
  public static Preference setPreferenceValue(String property, String value,
      boolean isListProperty, Client client, Organization org, User user, Role role, Window window,
      VariablesSecureApp vars) {
    try {
      OBContext.setAdminMode();
      Preference preference;
      String clientId = client == null ? null : client.getId();
      String orgId = org == null ? null : org.getId();
      String userId = user == null ? null : user.getId();
      String roleId = role == null ? null : role.getId();
      String windowId = window == null ? null : window.getId();

      List<Preference> prefs = getPreferences(property, isListProperty, clientId, orgId, userId,
          roleId, windowId, true, true);
      if (prefs.size() == 0) {
        // New preference
        preference = OBProvider.getInstance().get(Preference.class);
        preference.setClient(OBDal.getInstance().get(Client.class, "0"));
        preference.setOrganization(OBDal.getInstance().get(Organization.class, "0"));

        preference.setPropertyList(isListProperty);
        if (isListProperty) {
          preference.setProperty(property);
        } else {
          preference.setAttribute(property);
        }
        preference.setVisibleAtClient(client);
        preference.setVisibleAtOrganization(org);
        preference.setVisibleAtRole(role);
        preference.setUserContact(user);
        preference.setWindow(window);
      } else {
        // Rewrite value (assume there's no conflicting properties
        preference = prefs.get(0);
      }
      preference.setSearchKey(value);
      OBDal.getInstance().save(preference);

      if (vars != null) {
        savePreferenceInSession(vars, preference);
      }
      return preference;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Obtains the value for a given property with the visibility defined by the parameters. In case
   * of conflict or the property is not defined an exception is thrown.
   * <p>
   * This method is used to query in database for the property value, note that when properties are
   * set, they are also saved as session values and it is possible to obtain them using
   * {@link Utility#getPreference(VariablesSecureApp, String, String) Utility.getPreference}.
   * 
   * @throws PropertyException
   *           if the property cannot be resolved in a single value:
   *           <ul>
   *           <li> {@link PropertyNotFoundException} if the property is not defined. <li>
   *           {@link PropertyConflictException} in case of conflict
   *           </ul>
   */
  public static String getPreferenceValue(String property, boolean isListProperty, Client client,
      Organization org, User user, Role role, Window window) throws PropertyException {
    try {
      OBContext.setAdminMode();
      String clientId = client == null ? null : client.getId();
      String orgId = org == null ? null : org.getId();
      String userId = user == null ? null : user.getId();
      String roleId = role == null ? null : role.getId();
      String windowId = window == null ? null : window.getId();

      List<Preference> prefs = getPreferences(property, isListProperty, clientId, orgId, userId,
          roleId, windowId, false, true);

      Preference selectedPreference = null;
      List<String> parentTree = OBContext.getOBContext().getOrganizationStructureProvider(clientId)
          .getParentList(orgId, true);
      boolean conflict = false;
      for (Preference preference : prefs) {
        // select the highest priority or raise exception in case of conflict
        if (selectedPreference == null) {
          selectedPreference = preference;
          continue;
        }
        int higherPriority = isHigherPriority(selectedPreference, preference, parentTree);
        switch (higherPriority) {
        case 1:
          // do nothing, selected one has higher priority
          break;
        case 2:
          selectedPreference = preference;
          conflict = false;
          break;
        default:
          conflict = true;
          break;
        }
      }

      if (conflict) {
        throw new PropertyConflictException();
      }
      if (selectedPreference == null) {
        throw new PropertyNotFoundException();
      }
      return selectedPreference.getSearchKey();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * @see Preferences#getPreferenceValue(String, boolean, Client, Organization, User, Role, Window)
   */
  public static String getPreferenceValue(String property, boolean isListProperty,
      String strClient, String strOrg, String strUser, String strRole, String strWindow)
      throws PropertyException {
    try {
      OBContext.setAdminMode();
      Client client = OBDal.getInstance().get(Client.class, strClient == null ? "" : strClient);
      Organization org = OBDal.getInstance().get(Organization.class, strOrg == null ? "" : strOrg);
      User user = OBDal.getInstance().get(User.class, strUser == null ? "" : strUser);
      Role role = OBDal.getInstance().get(Role.class, strRole == null ? "" : strRole);
      Window window = OBDal.getInstance().get(Window.class, strWindow == null ? "" : strWindow);
      return getPreferenceValue(property, isListProperty, client, org, user, role, window);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Stores the preference as a session value
   * 
   * @param vars
   *          VariablesSecureApp of the current session to store preference in
   * @param preference
   *          Preference to save in session
   */
  public static void savePreferenceInSession(VariablesSecureApp vars, Preference preference) {
    String prefName = "P|"
        + (preference.getWindow() == null ? "" : (preference.getWindow().getId() + "|"))
        + (preference.isPropertyList() ? preference.getProperty() : preference.getAttribute());
    vars.setSessionValue(prefName, preference.getSearchKey());
    log4j.debug("Set preference " + prefName + " - " + preference.getSearchKey());
  }

  /**
   * Obtains a list of preferences. All the parameters can be null; when a parameter is null, it
   * will not be used in the filtering for the preference.
   * <p>
   * exactMatch parameter determines whether the returned list of properties matches exactly the
   * visibility defined by the parameters, or if it is obtained any preference that is applicable to
   * the given visibility. For no exact match, visibility prioritization and conflicts are not
   * resolved in this method.
   * 
   */
  private static List<Preference> getPreferences(String property, boolean isListProperty,
      String client, String org, String user, String role, String window, boolean exactMatch,
      boolean checkWindow) {

    List<Object> parameters = new ArrayList<Object>();
    StringBuilder hql = new StringBuilder();
    hql.append(" as p ");
    hql.append(" where ");
    if (exactMatch) {
      if (client != null) {
        hql.append(" p.visibleAtClient.id = ? ");
        parameters.add(client);
      } else {
        hql.append(" p.visibleAtClient is null");
      }
      if (org != null) {
        hql.append(" and p.visibleAtOrganization.id = ? ");
        parameters.add(org);
      } else {
        hql.append(" and p.visibleAtOrganization is null ");
      }

      if (user != null) {
        hql.append(" and p.userContact.id = ? ");
        parameters.add(user);
      } else {
        hql.append(" and p.userContact is null ");
      }

      if (role != null) {
        hql.append(" and p.visibleAtRole.id = ? ");
        parameters.add(role);
      } else {
        hql.append(" and p.visibleAtRole is null");
      }

      if (window != null) {
        hql.append(" and p.window.id = ? ");
        parameters.add(window);
      } else {
        hql.append(" and p.window is null");
      }
    } else {
      if (client != null) {
        hql.append(" (p.visibleAtClient.id = ? or ");
        parameters.add(client);
      } else {
        hql.append(" (");
      }
      hql.append(" coalesce(p.visibleAtClient, '0')='0') ");

      if (role != null) {
        hql.append(" and   (p.visibleAtRole.id = ? or ");
        parameters.add(role);
      } else {
        hql.append(" and (");
      }
      hql.append("        p.visibleAtRole is null) ");

      if (org == null) {
        hql.append("     and (coalesce(p.visibleAtOrganization, '0')='0'))");
      }

      if (user != null) {
        hql.append("  and (p.userContact.id = ? or ");
        parameters.add(user);
      } else {
        hql.append(" and (");
      }
      hql.append("         p.userContact is null) ");
      if (checkWindow) {
        if (window != null) {
          hql.append(" and  (p.window.id = ? or ");
          parameters.add(window);
        } else {
          hql.append(" and (");
        }
        hql.append("        p.window is null) ");
      }
    }

    if (property != null) {
      hql.append(" and p.propertyList = '" + (isListProperty ? "Y" : "N") + "'");
      if (isListProperty) {
        hql.append(" and p.property = ? ");
      } else {
        hql.append(" and p.attribute = ? ");
      }
      parameters.add(property);
    }

    OBQuery<Preference> qPref = OBDal.getInstance().createQuery(Preference.class, hql.toString());
    qPref.setParameters(parameters);
    List<Preference> preferences = qPref.list();

    if (org != null) {
      // Remove from list organization that are not visible
      List<String> parentTree = OBContext.getOBContext().getOrganizationStructureProvider(client)
          .getParentList(org, true);
      List<Preference> auxPreferences = new ArrayList<Preference>();
      for (Preference pref : preferences) {
        if (pref.getVisibleAtOrganization() == null
            || parentTree.contains(pref.getVisibleAtOrganization().getId())) {
          auxPreferences.add(pref);
        }
      }
      return auxPreferences;
    } else {
      return preferences;
    }
  }

  /**
   * Determines which of the 2 preferences has higher visibility priority.
   * 
   * @param pref1
   *          First preference to compare
   * @param pref2
   *          Second preference to compare
   * @param parentTree
   *          Parent tree of organizations including the current one, used to assign more priority
   *          to organizations nearer in the tree.
   * @return <ul>
   *         <li>1 in case pref1 is more visible than pref2
   *         <li>2 in case pref2 is more visible than pref1
   *         <li>0 in case of conflict (both have identical visibility and value)
   *         </ul>
   */
  private static int isHigherPriority(Preference pref1, Preference pref2, List<String> parentTree) {
    // Check priority by client
    if ((pref2.getVisibleAtClient() == null || pref2.getVisibleAtClient().getId().equals("0"))
        && pref1.getVisibleAtClient() != null && !pref1.getVisibleAtClient().getId().equals("0")) {
      return 1;
    }

    // Check priority by organization
    Organization org1 = pref1.getVisibleAtOrganization();
    Organization org2 = pref2.getVisibleAtOrganization();
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
    if (pref1.getUserContact() != null && pref2.getUserContact() == null) {
      return 1;
    }

    if (pref1.getUserContact() == null && pref2.getUserContact() != null) {
      return 2;
    }

    // Check priority by role
    if (pref1.getVisibleAtRole() != null && pref2.getVisibleAtRole() == null) {
      return 1;
    }

    if (pref1.getVisibleAtRole() == null && pref2.getVisibleAtRole() != null) {
      return 2;
    }

    // Check window
    if (pref1.getWindow() != null && pref2.getWindow() == null) {
      return 1;
    }

    if (pref1.getWindow() == null && pref2.getWindow() != null) {
      return 2;
    }

    // Same priority, check selected
    if (pref1.isSelected() && !pref2.isSelected()) {
      return 1;
    }

    if (!pref1.isSelected() && pref2.isSelected()) {
      return 2;
    }

    if ((pref1.getSearchKey() == null && pref2.getSearchKey() == null)
        || (pref1.getSearchKey() != null && pref2.getSearchKey() != null && pref1.getSearchKey()
            .equals(pref2.getSearchKey()))) {
      // Conflict with same value, it does not matter priority
      return 2;
    }

    // Actual conflict
    return 0;
  }

  /**
   * Checks whether there is a preference for the same property in a List. If so, it is returned,
   * other case null is returned.
   * 
   * @param pref
   *          Preference to look for.
   * @param preferences
   *          List of preferences to look in.
   * @return The preference if it exists in the list, null if not.
   */
  private static Preference getPreferenceFromList(Preference pref, List<Preference> preferences) {
    for (Preference listPref : preferences) {
      boolean isCurrentPreference = pref.isPropertyList() == listPref.isPropertyList()
          && ((pref.isPropertyList() && pref.getProperty().equals(listPref.getProperty())) || (!pref
              .isPropertyList() && pref.getAttribute().equals(listPref.getAttribute())));
      boolean winVisbilityNotDefined = (pref.getWindow() == null && listPref.getWindow() == null);
      boolean sameWinVisibility = (pref.getWindow() != null && pref.getWindow().equals(
          listPref.getWindow()))
          || (listPref.getWindow() != null && listPref.getWindow().equals(pref.getWindow()));

      if (isCurrentPreference && (winVisbilityNotDefined || sameWinVisibility)) {
        return listPref;
      }
    }
    return null;
  }
}
