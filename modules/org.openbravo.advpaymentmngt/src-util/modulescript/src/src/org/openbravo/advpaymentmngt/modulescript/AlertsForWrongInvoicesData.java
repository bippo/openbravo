//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class AlertsForWrongInvoicesData implements FieldProvider {
static Logger log4j = Logger.getLogger(AlertsForWrongInvoicesData.class);
  private String InitRecordNumber="0";
  public String adClientId;
  public String invoice;
  public String cInvoiceId;
  public String adOrgId;
  public String issotrx;
  public String adRoleId;
  public String adAlertruleId;
  public String adAlertId;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("AD_CLIENT_ID") || fieldName.equals("adClientId"))
      return adClientId;
    else if (fieldName.equalsIgnoreCase("INVOICE"))
      return invoice;
    else if (fieldName.equalsIgnoreCase("C_INVOICE_ID") || fieldName.equals("cInvoiceId"))
      return cInvoiceId;
    else if (fieldName.equalsIgnoreCase("AD_ORG_ID") || fieldName.equals("adOrgId"))
      return adOrgId;
    else if (fieldName.equalsIgnoreCase("ISSOTRX"))
      return issotrx;
    else if (fieldName.equalsIgnoreCase("AD_ROLE_ID") || fieldName.equals("adRoleId"))
      return adRoleId;
    else if (fieldName.equalsIgnoreCase("AD_ALERTRULE_ID") || fieldName.equals("adAlertruleId"))
      return adAlertruleId;
    else if (fieldName.equalsIgnoreCase("AD_ALERT_ID") || fieldName.equals("adAlertId"))
      return adAlertId;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static AlertsForWrongInvoicesData[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static AlertsForWrongInvoicesData[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      select distinct fin_payment_schedule.ad_client_id, ad_column_identifier('c_invoice', fin_payment_schedule.c_invoice_id, 'en_US') as invoice, " +
      "      fin_payment_schedule.c_invoice_id, fin_payment_schedule.ad_org_id, c_invoice.issotrx, '' as ad_role_id, '' as ad_alertrule_id, '' as ad_alert_id" +
      "      from c_invoice, fin_payment_schedule, fin_payment_scheduledetail" +
      "      where c_invoice.c_invoice_id = fin_payment_schedule.c_invoice_id " +
      "      and fin_payment_schedule.fin_payment_schedule_id = fin_payment_scheduledetail.fin_payment_schedule_invoice" +
      "      and fin_payment_scheduledetail.iscanceled = 'N'" +
      "      group by fin_payment_schedule.ad_client_id, fin_payment_schedule.ad_org_id, c_invoice.issotrx, fin_payment_schedule.fin_payment_schedule_id, fin_payment_schedule.c_invoice_id, fin_payment_schedule.amount" +
      "      having fin_payment_schedule.amount <> sum(fin_payment_scheduledetail.amount + coalesce(fin_payment_scheduledetail.writeoffamt,0))" +
      "      order by 1, 2";

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
        AlertsForWrongInvoicesData objectAlertsForWrongInvoicesData = new AlertsForWrongInvoicesData();
        objectAlertsForWrongInvoicesData.adClientId = UtilSql.getValue(result, "AD_CLIENT_ID");
        objectAlertsForWrongInvoicesData.invoice = UtilSql.getValue(result, "INVOICE");
        objectAlertsForWrongInvoicesData.cInvoiceId = UtilSql.getValue(result, "C_INVOICE_ID");
        objectAlertsForWrongInvoicesData.adOrgId = UtilSql.getValue(result, "AD_ORG_ID");
        objectAlertsForWrongInvoicesData.issotrx = UtilSql.getValue(result, "ISSOTRX");
        objectAlertsForWrongInvoicesData.adRoleId = UtilSql.getValue(result, "AD_ROLE_ID");
        objectAlertsForWrongInvoicesData.adAlertruleId = UtilSql.getValue(result, "AD_ALERTRULE_ID");
        objectAlertsForWrongInvoicesData.adAlertId = UtilSql.getValue(result, "AD_ALERT_ID");
        objectAlertsForWrongInvoicesData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectAlertsForWrongInvoicesData);
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
    AlertsForWrongInvoicesData objectAlertsForWrongInvoicesData[] = new AlertsForWrongInvoicesData[vector.size()];
    vector.copyInto(objectAlertsForWrongInvoicesData);
    return(objectAlertsForWrongInvoicesData);
  }

/**
Identify alerts created not taking in account the void payment functionality.
 */
  public static AlertsForWrongInvoicesData[] selectVoidAlerts(ConnectionProvider connectionProvider, String alertRuleId)    throws ServletException {
    return selectVoidAlerts(connectionProvider, alertRuleId, 0, 0);
  }

/**
Identify alerts created not taking in account the void payment functionality.
 */
  public static AlertsForWrongInvoicesData[] selectVoidAlerts(ConnectionProvider connectionProvider, String alertRuleId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      select distinct fin_payment_schedule.c_invoice_id, ad_alert.ad_alert_id" +
      "      from fin_payment_schedule, fin_payment_scheduledetail, ad_alert" +
      "      where fin_payment_schedule.fin_payment_schedule_id = fin_payment_scheduledetail.fin_payment_schedule_invoice" +
      "            and ad_alert.referencekey_id = fin_payment_schedule.c_invoice_id" +
      "      and exists (select 1" +
      "                  from fin_payment_scheduledetail psd" +
      "                  where psd.fin_payment_scheduledetail_id = fin_payment_scheduledetail.fin_payment_scheduledetail_id" +
      "                        and psd.iscanceled = 'Y')" +
      "      and ad_alert.ad_alertrule_id = ?" +
      "      group by fin_payment_schedule.fin_payment_schedule_id, fin_payment_schedule.c_invoice_id, fin_payment_schedule.amount, ad_alert.ad_alert_id" +
      "      having fin_payment_schedule.amount <> sum(fin_payment_scheduledetail.amount + coalesce(fin_payment_scheduledetail.writeoffamt,0))";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRuleId);

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
        AlertsForWrongInvoicesData objectAlertsForWrongInvoicesData = new AlertsForWrongInvoicesData();
        objectAlertsForWrongInvoicesData.cInvoiceId = UtilSql.getValue(result, "C_INVOICE_ID");
        objectAlertsForWrongInvoicesData.adAlertId = UtilSql.getValue(result, "AD_ALERT_ID");
        objectAlertsForWrongInvoicesData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectAlertsForWrongInvoicesData);
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
    AlertsForWrongInvoicesData objectAlertsForWrongInvoicesData[] = new AlertsForWrongInvoicesData[vector.size()];
    vector.copyInto(objectAlertsForWrongInvoicesData);
    return(objectAlertsForWrongInvoicesData);
  }

  public static boolean existsAlertRule(ConnectionProvider connectionProvider, String alertRule, String client)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM AD_ALERTRULE" +
      "       WHERE NAME = ?" +
      "         AND ISACTIVE = 'Y'" +
      "         AND AD_CLIENT_ID = ?";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRule);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "EXISTING").equals("0");
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

  public static boolean existsAlert(ConnectionProvider connectionProvider, String alertRule, String invoice)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM AD_ALERT" +
      "       WHERE AD_ALERTRULE_ID = ?" +
      "       AND REFERENCEKEY_ID = ?" +
      "       AND ISFIXED = 'N'";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRule);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, invoice);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "EXISTING").equals("0");
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

  public static String getAlertRuleId(ConnectionProvider connectionProvider, String name, String client)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT MAX(ad_alertrule_id) AS name" +
      "       FROM AD_ALERTRULE" +
      "       WHERE NAME LIKE ?" +
      "         AND ISACTIVE = 'Y'" +
      "         AND AD_CLIENT_ID = ?";

    ResultSet result;
    String strReturn = null;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, name);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);

      result = st.executeQuery();
      if(result.next()) {
        strReturn = UtilSql.getValue(result, "NAME");
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

  public static AlertsForWrongInvoicesData[] selectOldAlertRules(ConnectionProvider connectionProvider)    throws ServletException {
    return selectOldAlertRules(connectionProvider, 0, 0);
  }

  public static AlertsForWrongInvoicesData[] selectOldAlertRules(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT ad_alertrule_id" +
      "       FROM AD_ALERTRULE" +
      "       WHERE (name = 'Wrong purchase invoice. Wrong amount in payment plan detail' OR" +
      "              name = 'Wrong sales invoice. Wrong amount in payment plan detail')" +
      "         AND ISACTIVE = 'Y'" +
      "         AND lower(SQL) not like '%iscanceled%'";

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
        AlertsForWrongInvoicesData objectAlertsForWrongInvoicesData = new AlertsForWrongInvoicesData();
        objectAlertsForWrongInvoicesData.adAlertruleId = UtilSql.getValue(result, "AD_ALERTRULE_ID");
        objectAlertsForWrongInvoicesData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectAlertsForWrongInvoicesData);
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
    AlertsForWrongInvoicesData objectAlertsForWrongInvoicesData[] = new AlertsForWrongInvoicesData[vector.size()];
    vector.copyInto(objectAlertsForWrongInvoicesData);
    return(objectAlertsForWrongInvoicesData);
  }

  public static AlertsForWrongInvoicesData[] getRoleId(ConnectionProvider connectionProvider, String window, String clientId)    throws ServletException {
    return getRoleId(connectionProvider, window, clientId, 0, 0);
  }

  public static AlertsForWrongInvoicesData[] getRoleId(ConnectionProvider connectionProvider, String window, String clientId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT distinct ad_role_id" +
      "       FROM ad_window_access" +
      "       WHERE ad_window_id = ?" +
      "       AND AD_CLIENT_ID = ?" +
      "         AND ISACTIVE = 'Y'";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, window);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, clientId);

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
        AlertsForWrongInvoicesData objectAlertsForWrongInvoicesData = new AlertsForWrongInvoicesData();
        objectAlertsForWrongInvoicesData.adRoleId = UtilSql.getValue(result, "AD_ROLE_ID");
        objectAlertsForWrongInvoicesData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectAlertsForWrongInvoicesData);
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
    AlertsForWrongInvoicesData objectAlertsForWrongInvoicesData[] = new AlertsForWrongInvoicesData[vector.size()];
    vector.copyInto(objectAlertsForWrongInvoicesData);
    return(objectAlertsForWrongInvoicesData);
  }

  public static int insertAlertRule(ConnectionProvider connectionProvider, String clientId, String name, String tabId, String sql)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO AD_ALERTRULE (" +
      "        AD_ALERTRULE_ID, AD_CLIENT_ID, AD_ORG_ID,ISACTIVE," +
      "        CREATED, CREATEDBY,  UPDATED, UPDATEDBY," +
      "        NAME, AD_TAB_ID, FILTERCLAUSE, TYPE," +
      "        SQL" +
      "      ) VALUES (" +
      "        get_uuid(), ?, '0', 'Y'," +
      "        now(), '100', now(), '100'," +
      "        ?, ?, '', 'D'," +
      "        ?" +
      "      )";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, clientId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, name);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, tabId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, sql);

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

  public static int insertAlert(ConnectionProvider connectionProvider, String client, String org, String description, String adAlertRuleId, String recordId, String referencekey_id)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO AD_Alert (" +
      "        AD_Alert_ID, AD_Client_ID, AD_Org_ID, IsActive," +
      "        Created, CreatedBy, Updated, UpdatedBy," +
      "        Description, AD_AlertRule_ID, Record_Id, Referencekey_ID" +
      "      ) VALUES (" +
      "        get_uuid(), ?, ?, 'Y'," +
      "        NOW(), '0', NOW(), '0'," +
      "        ?, ?, ?, ?)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, org);
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

  public static int insertAlertRecipient(ConnectionProvider connectionProvider, String client, String org, String adAlertRuleId, String role)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "    INSERT INTO ad_alertrecipient(" +
      "            ad_user_id, ad_client_id, ad_org_id, isactive, created, createdby, " +
      "            updated, updatedby, ad_alertrecipient_id, ad_alertrule_id, ad_role_id, " +
      "            sendemail)" +
      "    VALUES (null, ?, ?, 'Y', now(), '100', " +
      "            now(), '100', get_uuid(), ?, ?, " +
      "            'N')";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, org);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adAlertRuleId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, role);

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

  public static int updateAlertRuleSql(ConnectionProvider connectionProvider, String sql, String adAlertRuleId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE ad_alertrule SET SQL = ? WHERE ad_alertrule_id = ?";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, sql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adAlertRuleId);

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

  public static int updateAlertStatus(ConnectionProvider connectionProvider, String status, String isfixed, String alertId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE ad_alert SET status = ?, isfixed = ? WHERE ad_alert_id = ?";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, status);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, isfixed);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertId);

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
}
