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
package org.openbravo.client.application;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.SQLQuery;
import org.hibernate.exception.SQLGrammarException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.UsedByLink;
import org.openbravo.model.ad.alert.Alert;
import org.openbravo.model.ad.alert.AlertRecipient;
import org.openbravo.model.ad.alert.AlertRule;

/**
 * @author gorkaion
 * 
 */
@ApplicationScoped
public class AlertManagementActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(AlertManagementActionHandler.class);
  private static final String GET_ALERT_RULES = "getAlertRules";
  private static final String MOVE_TO_STATUS = "moveToStatus";
  private static final Logger log4j = Logger.getLogger(AlertManagementActionHandler.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseActionHandler#execute(java.util.Map, java.lang.String)
   */
  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject object = new JSONObject();

    OBContext.setAdminMode();
    try {
      JSONObject o = new JSONObject(content);
      final String strEventType = o.getString("eventType");
      if (GET_ALERT_RULES.equals(strEventType)) {
        object.put("alertRules", getAlertRules());
      } else if (MOVE_TO_STATUS.equals(strEventType)) {
        final String alertIDs = o.getString("alertIDs");
        final String oldStatus = o.getString("oldStatus");
        final String newStatus = o.getString("newStatus");
        setNewStatus(alertIDs, newStatus);
        object.put("oldStatus", oldStatus);
        object.put("newStatus", newStatus);
      } else {
        log.error("Unsupported event type: " + strEventType);
      }

    } catch (JSONException e) {
      log.error("Error executing action: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return object;
  }

  private JSONArray getAlertRules() {
    // Get alert rules visible for context's the role/user.
    StringBuffer whereClause = new StringBuffer();
    whereClause.append(" as ar ");
    whereClause.append("\nwhere exists (select 1 from ar."
        + AlertRule.PROPERTY_ADALERTRECIPIENTLIST + " as arr");
    whereClause.append("\n    where arr." + AlertRecipient.PROPERTY_USERCONTACT + ".id = :user");
    whereClause.append("\n      or (");
    whereClause.append("arr." + AlertRecipient.PROPERTY_USERCONTACT + " is null");
    whereClause.append("\n          and arr." + AlertRecipient.PROPERTY_ROLE + ".id = :role))");

    OBQuery<AlertRule> alertRulesQuery = OBDal.getInstance().createQuery(AlertRule.class,
        whereClause.toString());
    alertRulesQuery.setNamedParameter("user", OBContext.getOBContext().getUser().getId());
    alertRulesQuery.setNamedParameter("role", OBContext.getOBContext().getRole().getId());

    JSONArray alertRules = new JSONArray();
    try {
      if (alertRulesQuery.count() > 0) {
        for (AlertRule alertRule : alertRulesQuery.list()) {
          JSONObject alertRuleJson = null;

          // Adding alert rule if it has not filter clause. In case it has, it will be added only in
          // case it returns data after applying the filter clause.
          if (alertRule.getFilterClause() == null) {
            alertRuleJson = new JSONObject();
            alertRuleJson.put("name", alertRule.getIdentifier());
            alertRuleJson.put("alertRuleId", alertRule.getId());
            if (alertRule.getTab() != null) {
              alertRuleJson.put("tabId", alertRule.getTab().getId());
            } else {
              alertRuleJson.put("tabId", "");
            }
          }

          String filterClause = null;
          if (alertRule.getFilterClause() != null) {
            try {
              filterClause = new UsedByLink().getWhereClause(RequestContext.get()
                  .getVariablesSecureApp(), "", alertRule.getFilterClause());
            } catch (ServletException e) {
              throw new IllegalStateException(e);
            }
            final String sql = "select * from AD_ALERT where ISACTIVE='Y'" + " AND AD_CLIENT_ID "
                + OBDal.getInstance().getReadableClientsInClause() + " AND AD_ORG_ID "
                + OBDal.getInstance().getReadableOrganizationsInClause()
                + " AND AD_ALERTRULE_ID = ? " + (filterClause == null ? "" : filterClause);
            final SQLQuery sqlQuery = OBDal.getInstance().getSession().createSQLQuery(sql)
                .addEntity(Alert.ENTITY_NAME);
            sqlQuery.setParameter(0, alertRule.getId());

            try {
              log4j.debug("Alert " + alertRule.getName() + " (" + alertRule.getId() + ") - SQL:'"
                  + sql + "' - Rows: " + sqlQuery.list().size());
              // It is not possible to add an SQL filter clause to the grid's default datasource.
              // A String with the alert_id's to filter the grid's so only alerts with access are
              // shown.
              if (sqlQuery.list().size() > 0) {
                // Alert rule returns data, adding it to list of alert rules.
                alertRuleJson = new JSONObject();
                alertRuleJson.put("name", alertRule.getIdentifier());
                alertRuleJson.put("alertRuleId", alertRule.getId());
                if (alertRule.getTab() != null) {
                  alertRuleJson.put("tabId", alertRule.getTab().getId());
                } else {
                  alertRuleJson.put("tabId", "");
                }

                String filterAlerts = "";
                @SuppressWarnings("unchecked")
                List<Alert> alerts = sqlQuery.list();
                for (Alert alert : alerts) {
                  if (!filterAlerts.isEmpty()) {
                    filterAlerts += ", ";
                  }
                  filterAlerts += "'" + alert.getId() + "'";
                }
                alertRuleJson.put("alerts", filterAlerts);

              }
            } catch (SQLGrammarException e) {
              log4j.error(
                  "An error has ocurred when trying to process the alerts: " + e.getMessage(), e);
            }
          }
          if (alertRuleJson != null) {
            alertRules.put(alertRuleJson);
          }
        }
      }
    } catch (JSONException e) {
      log.error("Error executing action: " + e.getMessage(), e);
    }

    return alertRules;
  }

  private void setNewStatus(String alertIDs, String newStatus) {
    if (StringUtils.isEmpty(alertIDs)) {
      return;
    }
    List<Alert> alerts = OBDao.getOBObjectListFromString(Alert.class, alertIDs);
    for (Alert alert : alerts) {
      alert.setAlertStatus(newStatus.toUpperCase());
      OBDal.getInstance().save(alert);
    }
    OBDal.getInstance().flush();
  }
}
