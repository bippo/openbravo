//Sqlc generated V1.O00-1
package org.openbravo.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class PopulateOriginalPaymentPlanData implements FieldProvider {
static Logger log4j = Logger.getLogger(PopulateOriginalPaymentPlanData.class);
  private String InitRecordNumber="0";
  public String finPaymentScheduleId;
  public String adClientId;
  public String adOrgId;
  public String created;
  public String createdby;
  public String updated;
  public String updatedby;
  public String cInvoiceId;
  public String cOrderId;
  public String duedate;
  public String finPaymentmethodId;
  public String cCurrencyId;
  public String amount;
  public String isactive;
  public String finPaymentPriorityId;
  public String updatePaymentPlan;
  public String finOrigPaymentScheduleId;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("fin_payment_schedule_id") || fieldName.equals("finPaymentScheduleId"))
      return finPaymentScheduleId;
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
    else if (fieldName.equalsIgnoreCase("c_invoice_id") || fieldName.equals("cInvoiceId"))
      return cInvoiceId;
    else if (fieldName.equalsIgnoreCase("c_order_id") || fieldName.equals("cOrderId"))
      return cOrderId;
    else if (fieldName.equalsIgnoreCase("duedate"))
      return duedate;
    else if (fieldName.equalsIgnoreCase("fin_paymentmethod_id") || fieldName.equals("finPaymentmethodId"))
      return finPaymentmethodId;
    else if (fieldName.equalsIgnoreCase("c_currency_id") || fieldName.equals("cCurrencyId"))
      return cCurrencyId;
    else if (fieldName.equalsIgnoreCase("amount"))
      return amount;
    else if (fieldName.equalsIgnoreCase("isactive"))
      return isactive;
    else if (fieldName.equalsIgnoreCase("fin_payment_priority_id") || fieldName.equals("finPaymentPriorityId"))
      return finPaymentPriorityId;
    else if (fieldName.equalsIgnoreCase("update_payment_plan") || fieldName.equals("updatePaymentPlan"))
      return updatePaymentPlan;
    else if (fieldName.equalsIgnoreCase("fin_orig_payment_schedule_id") || fieldName.equals("finOrigPaymentScheduleId"))
      return finOrigPaymentScheduleId;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static PopulateOriginalPaymentPlanData[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static PopulateOriginalPaymentPlanData[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      select FIN_PAYMENT_SCHEDULE_ID, AD_CLIENT_ID, AD_ORG_ID, CREATED," +
      "             CREATEDBY, UPDATED, UPDATEDBY, C_INVOICE_ID, C_ORDER_ID, DUEDATE," +
      "             FIN_PAYMENTMETHOD_ID, C_CURRENCY_ID, AMOUNT," +
      "             ISACTIVE, FIN_PAYMENT_PRIORITY_ID, UPDATE_PAYMENT_PLAN," +
      "             get_uuid() AS FIN_ORIG_PAYMENT_SCHEDULE_ID" +
      "      from fin_payment_schedule PS" +
      "      where not exists (" +
      "              select 1" +
      "              from fin_orig_payment_schedule ops" +
      "              where ps.c_invoice_id = ops.c_invoice_id" +
      "              )" +
      "      AND PS.C_INVOICE_ID IS NOT NULL";

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
        PopulateOriginalPaymentPlanData objectPopulateOriginalPaymentPlanData = new PopulateOriginalPaymentPlanData();
        objectPopulateOriginalPaymentPlanData.finPaymentScheduleId = UtilSql.getValue(result, "fin_payment_schedule_id");
        objectPopulateOriginalPaymentPlanData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectPopulateOriginalPaymentPlanData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectPopulateOriginalPaymentPlanData.created = UtilSql.getDateValue(result, "created", "dd-MM-yyyy");
        objectPopulateOriginalPaymentPlanData.createdby = UtilSql.getValue(result, "createdby");
        objectPopulateOriginalPaymentPlanData.updated = UtilSql.getDateValue(result, "updated", "dd-MM-yyyy");
        objectPopulateOriginalPaymentPlanData.updatedby = UtilSql.getValue(result, "updatedby");
        objectPopulateOriginalPaymentPlanData.cInvoiceId = UtilSql.getValue(result, "c_invoice_id");
        objectPopulateOriginalPaymentPlanData.cOrderId = UtilSql.getValue(result, "c_order_id");
        objectPopulateOriginalPaymentPlanData.duedate = UtilSql.getDateValue(result, "duedate", "dd-MM-yyyy");
        objectPopulateOriginalPaymentPlanData.finPaymentmethodId = UtilSql.getValue(result, "fin_paymentmethod_id");
        objectPopulateOriginalPaymentPlanData.cCurrencyId = UtilSql.getValue(result, "c_currency_id");
        objectPopulateOriginalPaymentPlanData.amount = UtilSql.getValue(result, "amount");
        objectPopulateOriginalPaymentPlanData.isactive = UtilSql.getValue(result, "isactive");
        objectPopulateOriginalPaymentPlanData.finPaymentPriorityId = UtilSql.getValue(result, "fin_payment_priority_id");
        objectPopulateOriginalPaymentPlanData.updatePaymentPlan = UtilSql.getValue(result, "update_payment_plan");
        objectPopulateOriginalPaymentPlanData.finOrigPaymentScheduleId = UtilSql.getValue(result, "fin_orig_payment_schedule_id");
        objectPopulateOriginalPaymentPlanData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectPopulateOriginalPaymentPlanData);
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
    PopulateOriginalPaymentPlanData objectPopulateOriginalPaymentPlanData[] = new PopulateOriginalPaymentPlanData[vector.size()];
    vector.copyInto(objectPopulateOriginalPaymentPlanData);
    return(objectPopulateOriginalPaymentPlanData);
  }

  public static int populateOPS(ConnectionProvider connectionProvider, String FinOrigPaymentScheduleID, String FinPaymentScheduleID)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      insert into fin_orig_payment_schedule(FIN_ORIG_PAYMENT_SCHEDULE_ID, AD_CLIENT_ID , AD_ORG_ID, CREATED," +
      "                                            CREATEDBY, UPDATED, UPDATEDBY, C_INVOICE_ID, C_ORDER_ID, DUEDATE," +
      "                                            FIN_PAYMENTMETHOD_ID , C_CURRENCY_ID, AMOUNT," +
      "                                            ISACTIVE, FIN_PAYMENT_PRIORITY_ID," +
      "                                            UPDATE_PAYMENT_PLAN)" +
      "      select ?, AD_CLIENT_ID, AD_ORG_ID, CREATED, CREATEDBY, UPDATED, UPDATEDBY," +
      "             C_INVOICE_ID, C_ORDER_ID, DUEDATE, FIN_PAYMENTMETHOD_ID, C_CURRENCY_ID, AMOUNT," +
      "             ISACTIVE, FIN_PAYMENT_PRIORITY_ID, UPDATE_PAYMENT_PLAN" +
      "      from fin_payment_schedule ps" +
      "      where fin_payment_schedule_id = ?";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, FinOrigPaymentScheduleID);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, FinPaymentScheduleID);

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

  public static int populateOPSD(ConnectionProvider connectionProvider, String FinOrigPaymentScheduleID, String FinPaymentScheduleID)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        INSERT INTO fin_orig_paym_scheddetail(FIN_ORIG_PAYM_SCHEDDETAIL_ID," +
      "                                              AD_CLIENT_ID, AD_ORG_ID, CREATED, CREATEDBY, UPDATED, UPDATEDBY," +
      "                                              FIN_ORIG_PAYMENT_SCHEDULE_ID, FIN_PAYMENT_SCHEDULEDETAIL_ID," +
      "                                              AMOUNT, ISACTIVE, WRITEOFFAMT, ISCANCELED)" +
      "        SELECT get_uuid() as FIN_ORIG_PAYM_SCHEDDETAIL_ID," +
      "               AD_CLIENT_ID, AD_ORG_ID, CREATED, CREATEDBY, UPDATED, UPDATEDBY," +
      "               ? as FIN_ORIG_PAYMENT_SCHEDULE_ID, FIN_PAYMENT_SCHEDULEDETAIL_ID," +
      "               AMOUNT, ISACTIVE, WRITEOFFAMT, ISCANCELED" +
      "        FROM FIN_PAYMENT_SCHEDULEDETAIL PSD" +
      "        WHERE NOT EXISTS (" +
      "                      select 1" +
      "                      from fin_orig_paym_scheddetail opsd" +
      "                      WHERE OPSD.FIN_PAYMENT_SCHEDULEDETAIL_ID = PSD.FIN_PAYMENT_SCHEDULEDETAIL_ID" +
      "        )" +
      "          and psd.fin_payment_schedule_invoice=?" +
      "          and psd.fin_payment_detail_id is not null";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, FinOrigPaymentScheduleID);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, FinPaymentScheduleID);

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
