//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.buildvalidation;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class WrongPaymentScheduleDetailsCheckData implements FieldProvider {
static Logger log4j = Logger.getLogger(WrongPaymentScheduleDetailsCheckData.class);
  private String InitRecordNumber="0";
  public String client;
  public String documentno;
  public String name;
  public String adAlertruleId;
  public String sql;
  public String description;
  public String recordId;
  public String referencekeyId;
  public String adClientId;
  public String adOrgId;
  public String created;
  public String createdby;
  public String updated;
  public String updatedby;
  public String isactive;
  public String adRoleId;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("client"))
      return client;
    else if (fieldName.equalsIgnoreCase("documentno"))
      return documentno;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    else if (fieldName.equalsIgnoreCase("ad_alertrule_id") || fieldName.equals("adAlertruleId"))
      return adAlertruleId;
    else if (fieldName.equalsIgnoreCase("sql"))
      return sql;
    else if (fieldName.equalsIgnoreCase("description"))
      return description;
    else if (fieldName.equalsIgnoreCase("record_id") || fieldName.equals("recordId"))
      return recordId;
    else if (fieldName.equalsIgnoreCase("referencekey_id") || fieldName.equals("referencekeyId"))
      return referencekeyId;
    else if (fieldName.equalsIgnoreCase("ad_client_id") || fieldName.equals("adClientId"))
      return adClientId;
    else if (fieldName.equalsIgnoreCase("ad_org_id") || fieldName.equals("adOrgId"))
      return adOrgId;
    else if (fieldName.equalsIgnoreCase("created"))
      return created;
    else if (fieldName.equalsIgnoreCase("createdby"))
      return createdby;
    else if (fieldName.equalsIgnoreCase("updated"))
      return updated;
    else if (fieldName.equalsIgnoreCase("updatedby"))
      return updatedby;
    else if (fieldName.equalsIgnoreCase("isactive"))
      return isactive;
    else if (fieldName.equalsIgnoreCase("ad_role_id") || fieldName.equals("adRoleId"))
      return adRoleId;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static WrongPaymentScheduleDetailsCheckData[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static WrongPaymentScheduleDetailsCheckData[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT '' AS CLIENT, '' AS documentno, '' AS NAME, '' AS AD_ALERTRULE_ID, '' AS SQL," +
      "              '' AS DESCRIPTION, '' AS RECORD_ID, '' AS REFERENCEKEY_ID, '' AS AD_CLIENT_ID, '' AS AD_ORG_ID," +
      "              '' AS CREATED, '' AS CREATEDBY, '' AS UPDATED, '' AS UPDATEDBY, '' AS ISACTIVE," +
      "              '' AS AD_ROLE_ID" +
      "       FROM DUAL";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        WrongPaymentScheduleDetailsCheckData objectWrongPaymentScheduleDetailsCheckData = new WrongPaymentScheduleDetailsCheckData();
        objectWrongPaymentScheduleDetailsCheckData.client = UtilSql.getValue(result, "client");
        objectWrongPaymentScheduleDetailsCheckData.documentno = UtilSql.getValue(result, "documentno");
        objectWrongPaymentScheduleDetailsCheckData.name = UtilSql.getValue(result, "name");
        objectWrongPaymentScheduleDetailsCheckData.adAlertruleId = UtilSql.getValue(result, "ad_alertrule_id");
        objectWrongPaymentScheduleDetailsCheckData.sql = UtilSql.getValue(result, "sql");
        objectWrongPaymentScheduleDetailsCheckData.description = UtilSql.getValue(result, "description");
        objectWrongPaymentScheduleDetailsCheckData.recordId = UtilSql.getValue(result, "record_id");
        objectWrongPaymentScheduleDetailsCheckData.referencekeyId = UtilSql.getValue(result, "referencekey_id");
        objectWrongPaymentScheduleDetailsCheckData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectWrongPaymentScheduleDetailsCheckData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectWrongPaymentScheduleDetailsCheckData.created = UtilSql.getValue(result, "created");
        objectWrongPaymentScheduleDetailsCheckData.createdby = UtilSql.getValue(result, "createdby");
        objectWrongPaymentScheduleDetailsCheckData.updated = UtilSql.getValue(result, "updated");
        objectWrongPaymentScheduleDetailsCheckData.updatedby = UtilSql.getValue(result, "updatedby");
        objectWrongPaymentScheduleDetailsCheckData.isactive = UtilSql.getValue(result, "isactive");
        objectWrongPaymentScheduleDetailsCheckData.adRoleId = UtilSql.getValue(result, "ad_role_id");
        objectWrongPaymentScheduleDetailsCheckData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectWrongPaymentScheduleDetailsCheckData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    WrongPaymentScheduleDetailsCheckData objectWrongPaymentScheduleDetailsCheckData[] = new WrongPaymentScheduleDetailsCheckData[vector.size()];
    vector.copyInto(objectWrongPaymentScheduleDetailsCheckData);
    return(objectWrongPaymentScheduleDetailsCheckData);
  }

  public static boolean existWrongPaymentSchedules(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT count(*) AS existing" +
      "      FROM (" +
      "        SELECT o.c_order_id, o.documentno" +
      "        FROM c_order o" +
      "            JOIN fin_payment_schedule pso ON o.c_order_id = pso.c_order_id" +
      "        WHERE EXISTS (SELECT 1 " +
      "                      FROM c_invoice i" +
      "                          JOIN c_invoiceline il ON il.c_invoice_id = i.c_invoice_id" +
      "                          JOIN c_orderline ol ON il.c_orderline_id = ol.c_orderline_id" +
      "                      WHERE ol.c_order_id = o.c_order_id" +
      "                        AND i.docstatus = 'CO'" +
      "                        AND NOT EXISTS (SELECT 1 FROM fin_payment_scheduledetail psd" +
      "                                                      JOIN fin_payment_schedule psi ON psd.fin_payment_schedule_invoice = psi.fin_payment_schedule_id" +
      "                                        WHERE il.c_invoice_id = psi.c_invoice_id" +
      "                                          AND psd.fin_payment_schedule_order = pso.fin_payment_schedule_id))" +
      "        AND EXISTS (SELECT 1 FROM fin_payment_scheduledetail psdo" +
      "                    WHERE psdo.fin_payment_schedule_order = pso.fin_payment_schedule_id)) o" +
      "          JOIN c_orderline ol ON ol.c_order_id = o.c_order_id" +
      "          JOIN c_invoiceline il ON il.c_orderline_id = ol.c_orderline_id" +
      "          JOIN c_invoice i ON i.c_invoice_id = il.c_invoice_id";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static String getUUID(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT get_uuid() as name" +
      "       FROM dual";

    ResultSet result;
    String strReturn = null;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(strReturn);
  }

  public static boolean existsAlertRule(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM AD_ALERTRULE" +
      "       WHERE NAME LIKE 'Wrong payment plan on invoiced orders'" +
      "         AND ISACTIVE = 'Y'";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static String getAlertRuleId(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT MAX(ad_alertrule_id) AS name" +
      "       FROM AD_ALERTRULE" +
      "       WHERE NAME LIKE 'Wrong payment plan on invoiced orders'" +
      "         AND ISACTIVE = 'Y'";

    ResultSet result;
    String strReturn = null;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(strReturn);
  }

  public static WrongPaymentScheduleDetailsCheckData[] selectAlertRule(ConnectionProvider connectionProvider, String alertRule)    throws ServletException {
    return selectAlertRule(connectionProvider, alertRule, 0, 0);
  }

  public static WrongPaymentScheduleDetailsCheckData[] selectAlertRule(ConnectionProvider connectionProvider, String alertRule, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT  AD_CLIENT_ID, AD_ORG_ID," +
      "         CREATED, CREATEDBY, UPDATED, UPDATEDBY, ISACTIVE," +
      "         '' as RECORD_ID, '' as DESCRIPTION, '' as REFERENCEKEY_ID, '' as AD_ROLE_ID," +
      "         AD_ALERTRULE_ID, SQL, NAME" +
      "       FROM AD_ALERTRULE" +
      "       WHERE AD_ALERTRULE_ID = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRule);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        WrongPaymentScheduleDetailsCheckData objectWrongPaymentScheduleDetailsCheckData = new WrongPaymentScheduleDetailsCheckData();
        objectWrongPaymentScheduleDetailsCheckData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectWrongPaymentScheduleDetailsCheckData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectWrongPaymentScheduleDetailsCheckData.created = UtilSql.getDateValue(result, "created", "dd-MM-yyyy");
        objectWrongPaymentScheduleDetailsCheckData.createdby = UtilSql.getValue(result, "createdby");
        objectWrongPaymentScheduleDetailsCheckData.updated = UtilSql.getDateValue(result, "updated", "dd-MM-yyyy");
        objectWrongPaymentScheduleDetailsCheckData.updatedby = UtilSql.getValue(result, "updatedby");
        objectWrongPaymentScheduleDetailsCheckData.isactive = UtilSql.getValue(result, "isactive");
        objectWrongPaymentScheduleDetailsCheckData.recordId = UtilSql.getValue(result, "record_id");
        objectWrongPaymentScheduleDetailsCheckData.description = UtilSql.getValue(result, "description");
        objectWrongPaymentScheduleDetailsCheckData.referencekeyId = UtilSql.getValue(result, "referencekey_id");
        objectWrongPaymentScheduleDetailsCheckData.adRoleId = UtilSql.getValue(result, "ad_role_id");
        objectWrongPaymentScheduleDetailsCheckData.adAlertruleId = UtilSql.getValue(result, "ad_alertrule_id");
        objectWrongPaymentScheduleDetailsCheckData.sql = UtilSql.getValue(result, "sql");
        objectWrongPaymentScheduleDetailsCheckData.name = UtilSql.getValue(result, "name");
        objectWrongPaymentScheduleDetailsCheckData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectWrongPaymentScheduleDetailsCheckData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    WrongPaymentScheduleDetailsCheckData objectWrongPaymentScheduleDetailsCheckData[] = new WrongPaymentScheduleDetailsCheckData[vector.size()];
    vector.copyInto(objectWrongPaymentScheduleDetailsCheckData);
    return(objectWrongPaymentScheduleDetailsCheckData);
  }

  public static WrongPaymentScheduleDetailsCheckData[] selectAlert(ConnectionProvider connectionProvider, String sql)    throws ServletException {
    return selectAlert(connectionProvider, sql, 0, 0);
  }

  public static WrongPaymentScheduleDetailsCheckData[] selectAlert(ConnectionProvider connectionProvider, String sql, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT AAA.*" +
      "       FROM (";
    strSql = strSql + ((sql==null || sql.equals(""))?"":sql);
    strSql = strSql + 
      ") AAA ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);
      if (sql != null && !(sql.equals(""))) {
        }

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        WrongPaymentScheduleDetailsCheckData objectWrongPaymentScheduleDetailsCheckData = new WrongPaymentScheduleDetailsCheckData();
        objectWrongPaymentScheduleDetailsCheckData.description = UtilSql.getValue(result, "description");
        objectWrongPaymentScheduleDetailsCheckData.recordId = UtilSql.getValue(result, "record_id");
        objectWrongPaymentScheduleDetailsCheckData.referencekeyId = UtilSql.getValue(result, "referencekey_id");
        objectWrongPaymentScheduleDetailsCheckData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectWrongPaymentScheduleDetailsCheckData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectWrongPaymentScheduleDetailsCheckData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    WrongPaymentScheduleDetailsCheckData objectWrongPaymentScheduleDetailsCheckData[] = new WrongPaymentScheduleDetailsCheckData[vector.size()];
    vector.copyInto(objectWrongPaymentScheduleDetailsCheckData);
    return(objectWrongPaymentScheduleDetailsCheckData);
  }

  public static boolean existsStatusColumn(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM  user_tab_columns" +
      "       WHERE lower(table_name) like 'ad_alert'" +
      "         AND lower(column_name) like 'status'";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static boolean existsReference(ConnectionProvider connectionProvider, String alertRule, String ref)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM AD_ALERT" +
      "       WHERE AD_ALERTRULE_ID = ?" +
      "         AND REFERENCEKEY_ID = ?" +
      "         AND STATUS != 'SOLVED'";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRule);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ref);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static boolean existsReferenceOld(ConnectionProvider connectionProvider, String alertRule, String ref)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM AD_ALERT" +
      "       WHERE AD_ALERTRULE_ID = ?" +
      "         AND REFERENCEKEY_ID = ?" +
      "         AND ISFIXED = 'N'";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRule);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ref);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static int insertAlertRule(ConnectionProvider connectionProvider, String alertRuleId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO AD_ALERTRULE (" +
      "        AD_ALERTRULE_ID, AD_CLIENT_ID, AD_ORG_ID,ISACTIVE," +
      "        CREATED, CREATEDBY,  UPDATED, UPDATEDBY," +
      "        NAME, AD_TAB_ID, FILTERCLAUSE, TYPE," +
      "        SQL" +
      "      ) VALUES (" +
      "        ?, '0', '0', 'Y'," +
      "        now(), '100', now(), '100'," +
      "        'Wrong payment plan on invoiced orders', '263', '', 'D'," +
      "        'select distinct inv.c_invoice_id as referencekey_id," +
      "           ad_column_identifier(''C_Invoice'', inv.c_invoice_id, ''en_US'') as record_id," +
      "           0 as ad_role_id, null as ad_user_id," +
      "           ''This invoice belongs to an order that has its payment plan wrong distributed through its invoices. Please reactivate all the invoices related to the order and complete them again.'' as description," +
      "           ''Y'' as isActive," +
      "           inv.ad_org_id, inv.ad_client_id," +
      "           now() as created, 0 as createdBy, now() as updated, 0 as updatedBy" +
      "          FROM (" +
      "            SELECT o.c_order_id" +
      "            FROM c_order o" +
      "                JOIN fin_payment_schedule pso ON o.c_order_id = pso.c_order_id" +
      "            WHERE EXISTS (SELECT 1 " +
      "                          FROM c_invoice i" +
      "                              JOIN c_invoiceline il ON il.c_invoice_id = i.c_invoice_id" +
      "                              JOIN c_orderline ol ON il.c_orderline_id = ol.c_orderline_id" +
      "                          WHERE ol.c_order_id = o.c_order_id" +
      "                            AND i.docstatus = ''CO''" +
      "                            AND NOT EXISTS (SELECT 1 FROM fin_payment_scheduledetail psd" +
      "                                                          JOIN fin_payment_schedule psi ON psd.fin_payment_schedule_invoice = psi.fin_payment_schedule_id" +
      "                                            WHERE il.c_invoice_id = psi.c_invoice_id" +
      "                                              AND psd.fin_payment_schedule_order = pso.fin_payment_schedule_id))" +
      "              AND EXISTS (SELECT 1 FROM fin_payment_scheduledetail psdo" +
      "                        WHERE psdo.fin_payment_schedule_order = pso.fin_payment_schedule_id)) o" +
      "            JOIN c_orderline ol ON ol.c_order_id = o.c_order_id" +
      "            JOIN c_invoiceline il ON il.c_orderline_id = ol.c_orderline_id" +
      "            JOIN c_invoice inv ON inv.c_invoice_id = il.c_invoice_id'" +
      "      )";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRuleId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int insertAlert(ConnectionProvider connectionProvider, String client, String description, String adAlertRuleId, String recordId, String referencekey_id)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO AD_Alert (" +
      "        AD_Alert_ID, AD_Client_ID, AD_Org_ID, IsActive," +
      "        Created, CreatedBy, Updated, UpdatedBy," +
      "        Description, AD_AlertRule_ID, Record_Id, Referencekey_ID" +
      "      ) VALUES (" +
      "        get_uuid(), ?, '0', 'Y'," +
      "        NOW(), '0', NOW(), '0'," +
      "        ?, ?, ?, ?)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, description);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adAlertRuleId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, recordId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, referencekey_id);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Check if the FIN_Payment_ScheduleDetail table exist
 */
  public static boolean existAPRMbasetables(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT count(*) AS EXISTING" +
      "       FROM ad_table" +
      "       WHERE ad_table_id = 'C0233061EA504EFEAB0483E836BBAF31'";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }
}
