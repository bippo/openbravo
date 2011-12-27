//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.buildvalidation;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class UnpostRefundPaymentsData implements FieldProvider {
static Logger log4j = Logger.getLogger(UnpostRefundPaymentsData.class);
  private String InitRecordNumber="0";
  public String adClientId;
  public String adOrgId;
  public String created;
  public String createdby;
  public String updated;
  public String updatedby;
  public String isactive;
  public String recordId;
  public String description;
  public String referencekeyId;
  public String adRoleId;
  public String adAlertruleId;
  public String sql;
  public String name;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("ad_client_id") || fieldName.equals("adClientId"))
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
    else if (fieldName.equalsIgnoreCase("record_id") || fieldName.equals("recordId"))
      return recordId;
    else if (fieldName.equalsIgnoreCase("description"))
      return description;
    else if (fieldName.equalsIgnoreCase("referencekey_id") || fieldName.equals("referencekeyId"))
      return referencekeyId;
    else if (fieldName.equalsIgnoreCase("ad_role_id") || fieldName.equals("adRoleId"))
      return adRoleId;
    else if (fieldName.equalsIgnoreCase("ad_alertrule_id") || fieldName.equals("adAlertruleId"))
      return adAlertruleId;
    else if (fieldName.equalsIgnoreCase("sql"))
      return sql;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static UnpostRefundPaymentsData[] select(ConnectionProvider connectionProvider, String alertRule)    throws ServletException {
    return select(connectionProvider, alertRule, 0, 0);
  }

  public static UnpostRefundPaymentsData[] select(ConnectionProvider connectionProvider, String alertRule, int firstRegister, int numberRegisters)    throws ServletException {
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
        UnpostRefundPaymentsData objectUnpostRefundPaymentsData = new UnpostRefundPaymentsData();
        objectUnpostRefundPaymentsData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectUnpostRefundPaymentsData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectUnpostRefundPaymentsData.created = UtilSql.getDateValue(result, "created", "dd-MM-yyyy");
        objectUnpostRefundPaymentsData.createdby = UtilSql.getValue(result, "createdby");
        objectUnpostRefundPaymentsData.updated = UtilSql.getDateValue(result, "updated", "dd-MM-yyyy");
        objectUnpostRefundPaymentsData.updatedby = UtilSql.getValue(result, "updatedby");
        objectUnpostRefundPaymentsData.isactive = UtilSql.getValue(result, "isactive");
        objectUnpostRefundPaymentsData.recordId = UtilSql.getValue(result, "record_id");
        objectUnpostRefundPaymentsData.description = UtilSql.getValue(result, "description");
        objectUnpostRefundPaymentsData.referencekeyId = UtilSql.getValue(result, "referencekey_id");
        objectUnpostRefundPaymentsData.adRoleId = UtilSql.getValue(result, "ad_role_id");
        objectUnpostRefundPaymentsData.adAlertruleId = UtilSql.getValue(result, "ad_alertrule_id");
        objectUnpostRefundPaymentsData.sql = UtilSql.getValue(result, "sql");
        objectUnpostRefundPaymentsData.name = UtilSql.getValue(result, "name");
        objectUnpostRefundPaymentsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectUnpostRefundPaymentsData);
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
    UnpostRefundPaymentsData objectUnpostRefundPaymentsData[] = new UnpostRefundPaymentsData[vector.size()];
    vector.copyInto(objectUnpostRefundPaymentsData);
    return(objectUnpostRefundPaymentsData);
  }

  public static boolean existsAlertRule(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM AD_ALERTRULE" +
      "       WHERE NAME LIKE 'Posted Refund Payments'" +
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
      "       WHERE NAME LIKE 'Posted Refund Payments'" +
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

  public static boolean existsReference(ConnectionProvider connectionProvider, String alertRule, String ref)    throws ServletException {
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

/**
This query counts the number of rows in c_bp_vendor_acct
 */
  public static UnpostRefundPaymentsData[] clientsWithPayments(ConnectionProvider connectionProvider)    throws ServletException {
    return clientsWithPayments(connectionProvider, 0, 0);
  }

/**
This query counts the number of rows in c_bp_vendor_acct
 */
  public static UnpostRefundPaymentsData[] clientsWithPayments(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT DISTINCT AD_COLUMN_IDENTIFIER('AD_Client', p.ad_client_id, 'en_US') AS NAME" +
      "      FROM fin_payment p, fin_payment_detail pd" +
      "      WHERE p.fin_payment_id = pd.fin_payment_id" +
      "        AND p.posted = 'Y'" +
      "        AND pd.refund = 'Y'" +
      "        AND pd.isprepayment = 'N'";

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
        UnpostRefundPaymentsData objectUnpostRefundPaymentsData = new UnpostRefundPaymentsData();
        objectUnpostRefundPaymentsData.name = UtilSql.getValue(result, "name");
        objectUnpostRefundPaymentsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectUnpostRefundPaymentsData);
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
    UnpostRefundPaymentsData objectUnpostRefundPaymentsData[] = new UnpostRefundPaymentsData[vector.size()];
    vector.copyInto(objectUnpostRefundPaymentsData);
    return(objectUnpostRefundPaymentsData);
  }

/**
This query counts the number of rows in c_bp_vendor_acct
 */
  public static boolean existsPostedRefundPayments(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT count(*) AS EXISTING" +
      "      FROM fin_payment p, fin_payment_detail pd" +
      "      WHERE p.fin_payment_id = pd.fin_payment_id" +
      "        AND p.posted = 'Y'" +
      "        AND pd.refund = 'Y'" +
      "        AND pd.isprepayment = 'N'";

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
      "        'Posted Refund Payments', 'C4B6506838E14A349D6717D6856F1B56', '', 'D'," +
      "        'select fin_payment_id as referencekey_id," +
      "           ad_column_identifier(''FIN_Payment'', fin_payment_id, ''en_US'') as record_id," +
      "           0 as ad_role_id, null as ad_user_id," +
      "           ''Posted refund payment. Please ensure that it is unposted before applying the module version upgrade.'' as description," +
      "           ''Y'' as isActive," +
      "           ad_org_id, ad_client_id," +
      "           now() as created, 0 as createdBy, now() as updated, 0 as updatedBy" +
      "           from fin_payment p" +
      "           where p.posted= ''Y''" +
      "             and exists (select 1 from fin_payment_detail pd where p.fin_payment_id = pd.fin_payment_id and pd.refund=''Y'' and pd.isprepayment=''N'')'" +
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

  public static UnpostRefundPaymentsData[] selectAlert(ConnectionProvider connectionProvider, String sql)    throws ServletException {
    return selectAlert(connectionProvider, sql, 0, 0);
  }

  public static UnpostRefundPaymentsData[] selectAlert(ConnectionProvider connectionProvider, String sql, int firstRegister, int numberRegisters)    throws ServletException {
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
        UnpostRefundPaymentsData objectUnpostRefundPaymentsData = new UnpostRefundPaymentsData();
        objectUnpostRefundPaymentsData.description = UtilSql.getValue(result, "description");
        objectUnpostRefundPaymentsData.recordId = UtilSql.getValue(result, "record_id");
        objectUnpostRefundPaymentsData.referencekeyId = UtilSql.getValue(result, "referencekey_id");
        objectUnpostRefundPaymentsData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectUnpostRefundPaymentsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectUnpostRefundPaymentsData);
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
    UnpostRefundPaymentsData objectUnpostRefundPaymentsData[] = new UnpostRefundPaymentsData[vector.size()];
    vector.copyInto(objectUnpostRefundPaymentsData);
    return(objectUnpostRefundPaymentsData);
  }

/**
Check if the FIN_Payment table exist
 */
  public static boolean existAPRMbasetables(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT count(*) AS EXISTING" +
      "       FROM ad_table" +
      "       WHERE ad_table_id = 'D1A97202E832470285C9B1EB026D54E2'";

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
