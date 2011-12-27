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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.ParameterValue;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DalConnectionProvider;

@ApplicationScoped
public class MyOpenbravoActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(MyOpenbravoActionHandler.class);
  private static final String WIDGET_MOVED = "WIDGET_MOVED";
  private static final String WIDGET_ADDED = "WIDGET_ADDED";
  private static final String WIDGET_REMOVED = "WIDGET_REMOVED";
  private static final String PUBLISH_CHANGES = "PUBLISH_CHANGES";
  private static final String RELOAD_WIDGETS = "RELOAD_WIDGETS";
  private static final String GET_COMMUNITY_BRANDING_URL = "GET_COMMUNITY_BRANDING_URL";
  private static final String GET_AVAILABLE_WIDGET_CLASSES = "GET_AVAILABLE_WIDGET_CLASSES";

  @Inject
  private MyOBUtils myOBUtils;

  @Inject
  private WeldUtils weldUtils;

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    OBContext.setAdminMode();
    try {
      OBError message = new OBError();
      message.setType("Success");
      message.setMessage("@Success@");
      // Retrieve content values
      JSONObject o = new JSONObject(content);
      final String strEventType = o.getString("eventType");
      log.debug("=== New action, eventType: " + strEventType + " ===");

      JSONObject context = o.getJSONObject("context");
      final boolean isAdminMode = context.getBoolean("adminMode");
      String availableAtLevel = "";
      String[] availableAtLevelValue = { "" };
      if (isAdminMode) {
        availableAtLevel = context.getString("availableAtLevel");
        availableAtLevelValue[0] = context.getString("availableAtLevelValue");
      }
      log.debug("context: " + context.toString());

      JSONArray widgets = o.getJSONArray("widgets");

      log.debug("retrieved widgets: " + widgets.toString());
      try {

        if (strEventType.equals(WIDGET_ADDED) || strEventType.equals(WIDGET_MOVED)
            || strEventType.equals(WIDGET_REMOVED) || strEventType.equals(PUBLISH_CHANGES)) {
          processWidgets(strEventType, isAdminMode, message, widgets, availableAtLevel,
              availableAtLevelValue);

        } else if (strEventType.equals(RELOAD_WIDGETS)) {
          // Add available classes
          String roleId = null;
          if (isAdminMode && availableAtLevel.equals("ROLE")) {
            roleId = availableAtLevelValue[0];
          }
          addAvailableWidgetClasses(o, roleId);

          // Add widget instances
          widgets = new JSONArray();
          reloadWidgets(isAdminMode, message, widgets, availableAtLevel, availableAtLevelValue);
          o.put("widgets", widgets);
        } else if (strEventType.equals(GET_AVAILABLE_WIDGET_CLASSES)) {
          String roleId = null;
          if (isAdminMode && availableAtLevel.equals("ROLE")) {
            roleId = availableAtLevelValue[0];
          }
          addAvailableWidgetClasses(o, roleId);
        } else if (strEventType.equals(GET_COMMUNITY_BRANDING_URL)) {
          o.put("url", Utility.getCommunityBrandingUrl("MyOB"));
        } else {
          message.setType("Error");
          message.setMessage("@OBKMO_UnknownEventType@");
        }
      } catch (OBException e) {
        OBDal.getInstance().rollbackAndClose();
        widgets = new JSONArray();
        reloadWidgets(isAdminMode, message, widgets, availableAtLevel, availableAtLevelValue);
        o.put("widgets", widgets);
        message.setType("Error");
        message.setMessage(e.getMessage());
      }

      // Translate message:
      VariablesSecureApp vars = new VariablesSecureApp(RequestContext.get().getRequest());
      message.setMessage(Utility.parseTranslation(new DalConnectionProvider(), vars,
          vars.getLanguage(), message.getMessage()));

      o.put("message", message.toMap());
      log.debug("returned widgets: " + widgets.toString());
      log.debug("returned message type: " + message.getType() + " title: " + message.getTitle()
          + " message: " + message.getMessage());

      return o;
    } catch (JSONException e) {
      log.error("Error executing action: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return new JSONObject();
  }

  private void reloadWidgets(boolean isAdminMode, OBError message, JSONArray widgets,
      String availableAtLevel, String[] availableAtLevelValue) throws JSONException {
    if (isAdminMode) {
      List<WidgetInstance> widgetsList = MyOBUtils.getDefaultWidgetInstances(availableAtLevel,
          availableAtLevelValue);
      for (WidgetInstance widgetInstance : widgetsList) {
        final JSONObject jsonObject = myOBUtils.getWidgetProvider(widgetInstance.getWidgetClass())
            .getWidgetInstanceDefinition(widgetInstance);
        widgets.put(jsonObject);
        log.debug(">> Added widget instance: " + jsonObject.toString());
      }
    } else {
      MyOpenbravoComponent myOBComponent = weldUtils.getInstance(MyOpenbravoComponent.class);
      List<String> widgetsList = myOBComponent.getWidgetInstanceDefinitions();
      for (String widgetInstance : widgetsList) {
        widgets.put(new JSONObject(widgetInstance));
        log.debug(">> Added widget instance: " + widgetInstance);
      }
    }
    if (widgets.length() == 0) {
      message.setType("Warning");
      message.setMessage("OBKMO_NoInstancesFound");
      log.warn(">> No instances found.");
    }

  }

  private void addAvailableWidgetClasses(JSONObject o, String roleId) {
    MyOpenbravoComponent component = weldUtils.getInstance(MyOpenbravoComponent.class);
    try {
      List<String> availableClasses = component.getAvailableWidgetClasses(roleId);
      o.put("availableWidgetClasses", availableClasses);
    } catch (Exception e) {
      log.error("Error retreiving widget classes", e);
      try {
        o.put("availableWidgetClasses", Collections.EMPTY_LIST);
      } catch (Exception ignore) {
        // Give up
      }
    }
  }

  private void processWidgets(String strEventType, boolean isAdminMode, OBError message,
      JSONArray widgets, String availableAtLevel, String[] availableAtLevelValue)
      throws JSONException, OBException {
    log.debug(">> processing widgets");
    boolean hasRemovedInstances = false, hasAddedInstances = false, hasModifiedInstances = false;
    Role role = OBDal.getInstance().get(Role.class, OBContext.getOBContext().getRole().getId());
    User user = OBDal.getInstance().get(User.class, OBContext.getOBContext().getUser().getId());

    // Currently only 2 columns are supported, increase the array length if more columns are
    // supported.
    Long[] maxOpenbravoTypeInstanceRow = { 0L, 0L };
    Long[] minNotOpenbravoTypeInstanceRow = { null, null };

    List<WidgetInstance> currentWidgetInstances = Collections.emptyList();
    if (isAdminMode) {
      currentWidgetInstances = MyOBUtils.getDefaultWidgetInstances(availableAtLevel,
          availableAtLevelValue);
    } else {
      currentWidgetInstances = MyOBUtils.getUserWidgetInstances();
    }
    for (int i = 0; i < widgets.length(); i++) {
      JSONObject widget = (JSONObject) widgets.get(i);
      final String newWidgetInstanceId = widget.getString("dbInstanceId");
      final Long newColNum = widget.getLong("colNum");
      final Long newRowNum = widget.getLong("rowNum");
      boolean isOpenbravoTypeInstance = false;
      log.debug(">> process widget id: " + newWidgetInstanceId + " colNum: " + newColNum
          + " rowNum: " + newRowNum);
      if (StringUtils.isNotEmpty(newWidgetInstanceId)) {
        WidgetInstance retrievedWidgetInstance = OBDal.getInstance().get(WidgetInstance.class,
            newWidgetInstanceId);
        log.debug(">> existing widget, colNum: " + retrievedWidgetInstance.getColumnPosition()
            + " rowNum: " + retrievedWidgetInstance.getSequenceInColumn());
        isOpenbravoTypeInstance = (retrievedWidgetInstance.getRelativePriority() != null && retrievedWidgetInstance
            .getRelativePriority().compareTo(0L) == 0);

        currentWidgetInstances.remove(retrievedWidgetInstance);
        // Widget modified, check for colNum and rowNum changes.
        if (newColNum.compareTo(retrievedWidgetInstance.getColumnPosition()) != 0) {
          retrievedWidgetInstance.setColumnPosition(widget.getLong("colNum"));
          OBDal.getInstance().save(retrievedWidgetInstance);
          hasModifiedInstances = true;
        }
        if (newRowNum.compareTo(retrievedWidgetInstance.getSequenceInColumn()) != 0) {
          retrievedWidgetInstance.setSequenceInColumn(widget.getLong("rowNum"));
          OBDal.getInstance().save(retrievedWidgetInstance);
          hasModifiedInstances = true;
        }
      } else {
        // Added widget, create a new widget
        WidgetInstance newWidgetInstance = OBProvider.getInstance().get(WidgetInstance.class);
        newWidgetInstance.setColumnPosition(newColNum);
        newWidgetInstance.setSequenceInColumn(newRowNum);
        newWidgetInstance.setWidgetClass(OBDal.getInstance().get(WidgetClass.class,
            widget.getString("widgetClassId")));
        if (!isAdminMode) {
          newWidgetInstance.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
          newWidgetInstance.setVisibleAtRole(role);
          newWidgetInstance.setVisibleAtUser(user);
          newWidgetInstance.setRelativePriority(6L);
        } else if (availableAtLevel.equals("SYSTEM")) {
          newWidgetInstance.setRelativePriority(1L);
        } else if (availableAtLevel.equals("CLIENT")) {
          newWidgetInstance.setRelativePriority(2L);
          newWidgetInstance.setClient(OBDal.getInstance().get(Client.class,
              availableAtLevelValue[0]));
          newWidgetInstance.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
        } else if (availableAtLevel.equals("ORG")) {
          newWidgetInstance.setRelativePriority(3L);
          newWidgetInstance.setOrganization(OBDal.getInstance().get(Organization.class,
              availableAtLevelValue[0]));
        } else if (availableAtLevel.equals("ROLE")) {
          newWidgetInstance.setRelativePriority(4L);
          newWidgetInstance.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
          newWidgetInstance.setVisibleAtRole(OBDal.getInstance().get(Role.class,
              availableAtLevelValue[0]));
        }
        OBDal.getInstance().save(newWidgetInstance);
        widget.put("dbInstanceId", newWidgetInstance.getId());
        log.debug(">> new widget added: " + newWidgetInstance.getId());
        hasAddedInstances = true;

        // Process parameter values
        processParameters(newWidgetInstance);
      }
      if (isOpenbravoTypeInstance && maxOpenbravoTypeInstanceRow[newColNum.intValue()] < newRowNum) {
        maxOpenbravoTypeInstanceRow[newColNum.intValue()] = newRowNum;
      } else if (!isOpenbravoTypeInstance
          && minNotOpenbravoTypeInstanceRow[newColNum.intValue()] != null
          && minNotOpenbravoTypeInstanceRow[newColNum.intValue()] > newRowNum) {
        minNotOpenbravoTypeInstanceRow[newColNum.intValue()] = newRowNum;
      } else if (!isOpenbravoTypeInstance
          && minNotOpenbravoTypeInstanceRow[newColNum.intValue()] == null) {
        minNotOpenbravoTypeInstanceRow[newColNum.intValue()] = newRowNum;
      }
    }

    // Check Openbravo type instances remain on top, only for community instances.
    if (!ActivationKey.getInstance().isOPSInstance()) {
      for (int i = 0; i < minNotOpenbravoTypeInstanceRow.length; i++) {
        if (minNotOpenbravoTypeInstanceRow[i] != null
            && minNotOpenbravoTypeInstanceRow[i] < maxOpenbravoTypeInstanceRow[i]) {
          throw new OBException("@OBKMO_OpenbravoTypeInstanceMustOnTop@");
        }
      }
    }

    if (!currentWidgetInstances.isEmpty()) {
      // Removed widget instance to delete.
      for (WidgetInstance widgetInstance : currentWidgetInstances) {
        log.debug(">> removed widget instance: " + widgetInstance.getId());
        if (widgetInstance.getCopiedFrom() != null) {
          widgetInstance.setActive(Boolean.FALSE);
          OBDal.getInstance().save(widgetInstance);
        } else {
          OBDal.getInstance().remove(widgetInstance);
        }
      }
      hasRemovedInstances = true;
    }
    if (hasRemovedInstances || hasAddedInstances || hasModifiedInstances) {
      OBDal.getInstance().flush();
    }
    if (strEventType.equals(WIDGET_ADDED) && !hasAddedInstances) {
      message.setType("Warning");
      message.setMessage("@OBKMO_NoWidgetsAdded@");
    } else if (strEventType.equals(WIDGET_REMOVED) && !hasRemovedInstances) {
      message.setType("Warning");
      message.setMessage("@OBKMO_NoWidgetsRemoved@");
    } else if (strEventType.equals(WIDGET_MOVED) && !hasModifiedInstances) {
      message.setType("Info");
      message.setMessage("@OBKMO_NoWidgetsMoved@");
    }
  }

  private void processParameters(WidgetInstance widgetInstance) throws JSONException {
    for (Parameter p : widgetInstance.getWidgetClass()
        .getOBUIAPPParameterEMObkmoWidgetClassIDList()) {

      if (p.getDefaultValue() == null) {
        continue;
      }

      final ParameterValue value = OBProvider.getInstance().get(ParameterValue.class);
      value.setParameter(p);
      value.setObkmoWidgetInstance(widgetInstance);
      ParameterUtils.setDefaultParameterValue(value);
      OBDal.getInstance().save(value);
    }
    OBDal.getInstance().flush();
  }
}
