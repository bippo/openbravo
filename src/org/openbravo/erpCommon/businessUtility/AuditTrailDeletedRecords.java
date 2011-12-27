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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.ExecuteQuery;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.ui.Tab;

class AuditTrailDeletedRecords {
  private static final Logger log4j = Logger.getLogger(AuditTrailDeletedRecords.class);

  /**
   * Same as
   * {@link AuditTrailDeletedRecords#getDeletedRecords(ConnectionProvider, VariablesSecureApp, String, String, String, int, int)}
   * without parent filtering.
   * 
   */
  static FieldProvider[] getDeletedRecords(ConnectionProvider conn, VariablesSecureApp vars,
      String tabId, int startPosition, int rangeLength, boolean onlyCount, String dateFrom,
      String dateTo, String user) {
    return getDeletedRecords(conn, vars, tabId, null, null, startPosition, rangeLength, onlyCount,
        dateFrom, dateTo, user);
  }

  /**
   * Obtains the deleted records for a given tab. It applies the tab's where clause and security
   * filters (client and organization).<br/>
   * In case the fkColumnName and fkId parameters are not null nor empty, it queries for the records
   * having this foreign key.
   * 
   * @param conn
   *          {@link ConnectionProvider} with connection to DB
   * @param vars
   *          {@link VariablesSecureApp} used to obtain security info (role)
   * @param tabId
   *          Id for the tab to obtain the deleted records for
   * @param fkColumnName
   *          Column name in the tab's table that is a foreign key to obtain the deleted records
   *          that are child of the given fkId. If this value is null or empty no filter for parents
   *          will be applied.
   * @param fkId
   *          Id of the fkColumnName to filter by. This value only make sense in case fkColumnName
   *          has value.
   * @param startPosition
   *          Offset for the returned result set.
   * @param rangeLength
   *          Maximum number of records returned.
   * @param dateFrom
   *          optional parameter to filter by time (format: #AD_SqlDateTimeFormat as defined in
   *          Openbravo.properties)
   * @param dateTo
   *          optional parameter to filter by time (format: #AD_SqlDateTimeFormat as defined in
   *          Openbravo.properties)
   * @param user
   *          optional parameter to filter by user
   * @param onlyCount
   *          if true, only count rows in resultset, if false return all rows
   * 
   * @return {@link FieldProvider}[] With the records matching the filters. The structure of the
   *         FieldProvider is given by the current tab's table structure in Application Dictionary.
   */
  static FieldProvider[] getDeletedRecords(ConnectionProvider conn, VariablesSecureApp vars,
      String tabId, String fkColumnName, String fkId, int startPosition, int rangeLength,
      boolean onlyCount, String dateFrom, String dateTo, String user) {
    StringBuffer sql = new StringBuffer();

    try {
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      log4j.debug("Deleted records for tab:" + tab);
      String tableName = tab.getTable().getDBTableName();
      boolean hasRange = !(startPosition == 0 && rangeLength == 0);
      boolean hasRangeLimit = !(rangeLength == 0);

      if (onlyCount) {
        sql.append("SELECT count(*) AS counter FROM (");
      }

      sql.append("SELECT * FROM (\n");
      if (hasRange && conn.getRDBMS().equalsIgnoreCase("ORACLE")) {
        sql.append("SELECT ROWNUM AS RN1, ").append(tableName).append(".* FROM(\n");
      }
      sql.append("SELECT record_id as rowkey, event_time as audittrailtime, ad_user_id as audittrailuser, processType as audittrailprocesstype, process_id as audittrailprocessid\n");
      for (Column col : tab.getTable().getADColumnList()) {
        // obtain information for all columns
        sql.append(", ");
        sql.append("(SELECT COALESCE(OLD_CHAR, TO_CHAR(OLD_NCHAR), TO_CHAR(OLD_NUMBER), TO_CHAR(OLD_DATE))\n");
        sql.append("  FROM AD_AUDIT_TRAIL\n");
        sql.append(" WHERE AD_TABLE_ID='").append(tab.getTable().getId()).append("'\n");
        sql.append("   AND AD_COLUMN_ID='").append(col.getId()).append("'\n");
        sql.append("   AND ACTION='D'\n");
        sql.append("   AND RECORD_ID = T.RECORD_ID\n");
        sql.append(" ) as ").append(col.getDBColumnName()).append("\n");
      }

      sql.append(" FROM AD_AUDIT_TRAIL T\n");
      sql.append("WHERE ACTION='D'\n");
      sql.append("  AND AD_TABLE_ID = '").append(tab.getTable().getId()).append("'\n");
      sql.append("  AND AD_COLUMN_ID = '")
          .append(
              ModelProvider.getInstance().getEntityByTableName(tableName).getIdProperties().get(0)
                  .getColumnId()).append("'\n");

      // Add security filter
      sql.append(" AND AD_CLIENT_ID IN (")
          .append(Utility.getContext(conn, vars, "#User_Client", tab.getWindow().getId()))
          .append(")\n");
      sql.append(" AND AD_ORG_ID IN (")
          .append(
              Utility.getContext(conn, vars, "#AccessibleOrgTree", tab.getWindow().getId(),
                  Integer.parseInt(tab.getTable().getDataAccessLevel()))).append(")\n");
      // optional filters from UI - pass via params as they come in unfiltered
      Vector<String> params = new Vector<String>();
      if (dateFrom != null && !dateFrom.isEmpty()) {
        sql.append(" AND event_time >= TO_DATE(?, '"
            + vars.getSessionValue("#AD_SqlDateTimeFormat") + "')");
        params.add(dateFrom);
      }
      if (dateTo != null && !dateTo.isEmpty()) {
        sql.append(" AND event_time <= TO_DATE(?, '"
            + vars.getSessionValue("#AD_SqlDateTimeFormat") + "')");
        params.add(dateTo);

      }
      if (user != null && !user.isEmpty()) {
        sql.append(" AND ad_user_id = '" + user + "'");
      }

      sql.append("  ORDER BY event_TIME DESC").append(") ").append(tableName);

      // apply where clause if exists
      String whereClause = tab.getSQLWhereClause();
      if (whereClause != null) {
        if (whereClause.indexOf("@") != -1) {
          whereClause = Utility.parseContext(conn, vars, whereClause, tab.getWindow().getId(),
              params);
        }
        sql.append(" WHERE ").append(whereClause).append("\n");
      }

      // Records for a given parent
      if (fkColumnName != null && !fkColumnName.equals("") && fkId != null && !fkId.equals("")) {
        if (tab.getSQLWhereClause() != null) {
          sql.append(" AND ");
        } else {
          sql.append(" WHERE ");
        }
        sql.append(fkColumnName).append(" = '").append(fkId).append("'\n");
      }

      if (hasRange) {
        // wrap end SQL
        // calc positions
        String rangeStart = Integer.toString(startPosition + 1);
        String rangeEnd = Integer.toString(startPosition + rangeLength);
        if (conn.getRDBMS().equalsIgnoreCase("ORACLE")) {
          sql.append(") WHERE RN1 ");
          if (hasRangeLimit)
            sql.append("BETWEEN " + rangeStart + " AND " + rangeEnd);
          else
            sql.append(">= ").append(rangeStart);
        } else {
          if (hasRangeLimit)
            sql.append(" LIMIT " + Integer.toString(rangeLength));
          sql.append(" OFFSET " + Integer.toString(startPosition));
        }
      }

      if (onlyCount) {
        sql.append(") countable");
      }

      if (log4j.isDebugEnabled()) {
        log4j.debug("SQL for deleted records:\n" + sql);
      }

      ExecuteQuery q = new ExecuteQuery(conn, sql.toString(), params);
      return q.select();
    } catch (Exception e) {
      log4j.error("Error in AuditTrailDeletedRecords", e);
    }
    return null;
  }
}
