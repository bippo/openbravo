/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

package org.openbravo.erpCommon.security;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.model.ad.access.SessionUsageAudit;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Inserts usage auditory in database.
 * 
 * @author alostale
 * 
 */
public class UsageAudit {
  private static final Logger log4j = Logger.getLogger(UsageAudit.class);

  private static final String SESSION_ID_ATTR = "#AD_SESSION_ID";
  // OPERATION_TYPE_PARAM is defined in DataSourceConstants, copied here not to create a
  // core->org.openbravo.service.datasource dependency by now
  private static final String OPERATION_TYPE_PARAM = "_operationType";

  /**
   * Inserts a new record in usage audit in case auditory is enabled. Information is obtained from
   * vars parameter and SessionInfo.
   * 
   */
  public static void auditAction(VariablesSecureApp vars, String javaClassName) {
    auditAction(vars.getSessionValue(SESSION_ID_ATTR), vars.getCommand(),
        SessionInfo.getProcessType(), SessionInfo.getModuleId(), SessionInfo.getProcessId(),
        javaClassName);
  }

  /**
   * Inserts a new record in usage audit in case auditory is enabled. Information is obtained from
   * request and parameters.
   * 
   */
  public static void auditAction(HttpServletRequest request, Map<String, String> parameters) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return;
    }
    auditAction((String) session.getAttribute(SESSION_ID_ATTR),
        parameters.get(OPERATION_TYPE_PARAM), "W", parameters.get("moduleId"),
        parameters.get("tabId"), null);
  }

  private static void auditAction(String sessionId, String action, String objectType,
      String moduleId, String objectId, String javaClassName) {
    try {
      OBContext.setAdminMode();

      final boolean usageAuditEnabled = OBDal.getInstance().get(SystemInformation.class, "0")
          .isUsageauditenabled();
      final boolean auditAction = usageAuditEnabled && sessionId != null && !sessionId.isEmpty()
          && objectType != null && !objectType.isEmpty() && moduleId != null && !moduleId.isEmpty();

      if (!auditAction) {
        return;
      }
      log4j.debug("Auditing sessionId: " + sessionId + " -  action:" + action + " - objectType:"
          + objectType + " - moduleId:" + moduleId + " - objectId:" + objectId
          + " - javaClassName:" + javaClassName);
      SessionUsageAudit usageAudit = OBProvider.getInstance().get(SessionUsageAudit.class);
      usageAudit.setClient(OBDal.getInstance().get(Client.class, "0"));
      usageAudit.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
      usageAudit.setJavaClassName(javaClassName);
      usageAudit.setModule(OBDal.getInstance().get(org.openbravo.model.ad.module.Module.class,
          moduleId));
      usageAudit.setSession(OBDal.getInstance().get(org.openbravo.model.ad.access.Session.class,
          sessionId));
      usageAudit.setCommand(action);
      usageAudit.setObjectType(objectType);
      usageAudit.setObject(objectId);
      OBDal.getInstance().save(usageAudit);
    } finally {
      OBContext.restorePreviousMode();
      OBDal.getInstance().flush();
      try {
        OBDal.getInstance().getConnection().commit();
      } catch (SQLException e) {
        log4j.error("Error commiting usage audit", e);
      }
    }
  }

  /**
   * A version of the auditAction not using dal and running in a seperate connection/transaction and
   * which does not commit the currently active dal transaction.
   */
  public static void auditActionNoDal(ConnectionProvider conn, VariablesSecureApp vars,
      String javaClassName) {
    auditActionNoDal(conn, vars.getSessionValue(SESSION_ID_ATTR), SessionInfo.getCommand(),
        SessionInfo.getProcessType(), SessionInfo.getModuleId(), SessionInfo.getProcessId(),
        javaClassName, null);
  }

  public static void auditActionNoDal(ConnectionProvider conn, VariablesSecureApp vars,
      String javaClassName, long time) {
    auditActionNoDal(conn, vars.getSessionValue(SESSION_ID_ATTR), SessionInfo.getCommand(),
        SessionInfo.getProcessType(), SessionInfo.getModuleId(), SessionInfo.getProcessId(),
        javaClassName, Long.toString(time));
  }

  private static void auditActionNoDal(ConnectionProvider conn, String sessionId, String action,
      String objectType, String moduleId, String objectId, String javaClassName, String time) {
    final boolean auditAction = SessionInfo.isUsageAuditActive() && sessionId != null
        && !sessionId.isEmpty() && objectType != null && !objectType.isEmpty() && moduleId != null
        && !moduleId.isEmpty() && action != null && !action.isEmpty();
    if (!auditAction) {
      return;
    }

    try {
      log4j.debug("Auditing sessionId: " + sessionId + " -  action:" + action + " - objectType:"
          + objectType + " - moduleId:" + moduleId + " - objectId:" + objectId
          + " - javaClassName:" + javaClassName);
      Connection con = conn.getTransactionConnection();
      SessionLoginData.insertUsageAudit(con, conn, SessionInfo.getUserId(), sessionId, objectId,
          moduleId, action, javaClassName, objectType, time);
      conn.releaseCommitConnection(con);
    } catch (ServletException se) {
      log4j.error("Error inserting usage audit", se);
    } catch (NoConnectionAvailableException e) {
      log4j.error("Error getting connection to insert usage audit", e);
    } catch (SQLException e) {
      log4j.error("Error inserting usage audit", e);
    }
  }

}
