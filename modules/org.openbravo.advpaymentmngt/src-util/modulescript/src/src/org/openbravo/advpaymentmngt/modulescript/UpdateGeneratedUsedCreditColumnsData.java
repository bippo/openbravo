//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class UpdateGeneratedUsedCreditColumnsData implements FieldProvider {
static Logger log4j = Logger.getLogger(UpdateGeneratedUsedCreditColumnsData.class);
  private String InitRecordNumber="0";
  public String rowCount;
  public String type;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("ROW_COUNT") || fieldName.equals("rowCount"))
      return rowCount;
    else if (fieldName.equalsIgnoreCase("TYPE"))
      return type;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static UpdateGeneratedUsedCreditColumnsData[] select(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    return select(conn, connectionProvider, 0, 0);
  }

  public static UpdateGeneratedUsedCreditColumnsData[] select(Connection conn, ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT 1 AS row_count, '' AS TYPE" +
      "      FROM DUAL";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

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
        UpdateGeneratedUsedCreditColumnsData objectUpdateGeneratedUsedCreditColumnsData = new UpdateGeneratedUsedCreditColumnsData();
        objectUpdateGeneratedUsedCreditColumnsData.rowCount = UtilSql.getValue(result, "ROW_COUNT");
        objectUpdateGeneratedUsedCreditColumnsData.type = UtilSql.getValue(result, "TYPE");
        objectUpdateGeneratedUsedCreditColumnsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectUpdateGeneratedUsedCreditColumnsData);
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
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    UpdateGeneratedUsedCreditColumnsData objectUpdateGeneratedUsedCreditColumnsData[] = new UpdateGeneratedUsedCreditColumnsData[vector.size()];
    vector.copyInto(objectUpdateGeneratedUsedCreditColumnsData);
    return(objectUpdateGeneratedUsedCreditColumnsData);
  }

  public static boolean hasGeneratedCreditToUpdate(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT count(*) AS TYPE" +
      "        FROM DUAL" +
      "        WHERE EXISTS (SELECT 1" +
      "                      FROM fin_payment p, fin_payment_detail pd, fin_payment_scheduledetail psd" +
      "                      WHERE p.fin_payment_id = pd.fin_payment_id" +
      "                        AND pd.fin_payment_detail_id = psd.fin_payment_detail_id" +
      "                        AND psd.fin_payment_schedule_invoice is null" +
      "                        AND psd.fin_payment_schedule_order is null" +
      "                        AND pd.c_glitem_id is null" +
      "                        AND p.generated_credit = 0" +
      "                        AND pd.refund='N')";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "TYPE").equals("0");
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
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static boolean hasRefundToUpdate(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT count(*) AS TYPE" +
      "        FROM DUAL" +
      "        WHERE EXISTS (SELECT 1" +
      "                      FROM fin_payment p, fin_payment_detail pd" +
      "                      WHERE p.fin_payment_id = pd.fin_payment_id" +
      "                        AND pd.refund = 'Y'" +
      "                        AND pd.amount > 0" +
      "                        AND p.used_credit = 0)";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "TYPE").equals("0");
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
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static int updateRefund(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE fin_payment" +
      "      SET used_credit = (SELECT ABS(SUM(pd.amount))" +
      "                    FROM fin_payment_detail pd" +
      "                    WHERE pd.fin_payment_id = fin_payment.fin_payment_id" +
      "                    AND pd.refund = 'Y')" +
      "      WHERE EXISTS (SELECT 1" +
      "                    FROM fin_payment_detail pd" +
      "                    WHERE pd.fin_payment_id = fin_payment.fin_payment_id" +
      "                      AND pd.refund = 'Y')";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int updateRefundPrepayment(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE fin_payment_detail" +
      "      SET isprepayment = 'Y'" +
      "      WHERE refund= 'Y'";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int updateRefundDetail(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE fin_payment_detail" +
      "      SET refund = 'N'" +
      "      WHERE refund= 'Y'" +
      "        AND amount > 0";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int updateGeneratedCredit(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE fin_payment" +
      "      SET generated_credit = (SELECT SUM(psd.amount)" +
      "                    FROM fin_payment_detail pd, fin_payment_scheduledetail psd" +
      "                    WHERE pd.fin_payment_id = fin_payment.fin_payment_id" +
      "                      AND pd.fin_payment_detail_id = psd.fin_payment_detail_id" +
      "                      AND psd.fin_payment_schedule_order is null" +
      "                      AND psd.fin_payment_schedule_invoice is null" +
      "                      AND pd.c_glitem_id is null" +
      "                      AND pd.refund = 'N')" +
      "      WHERE EXISTS (SELECT 1" +
      "                    FROM fin_payment_detail pd, fin_payment_scheduledetail psd" +
      "                    WHERE pd.fin_payment_id = fin_payment.fin_payment_id" +
      "                      AND pd.fin_payment_detail_id = psd.fin_payment_detail_id" +
      "                      AND psd.fin_payment_schedule_order is null" +
      "                      AND psd.fin_payment_schedule_invoice is null" +
      "                      AND pd.c_glitem_id is null" +
      "                      AND pd.refund = 'N')" +
      "        AND generated_credit = 0";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int initializeGenerated(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE fin_payment" +
      "      SET generated_credit = 0" +
      "      WHERE generated_credit IS NULL";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int initializeUsed(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE fin_payment" +
      "      SET used_credit = 0" +
      "      WHERE used_credit IS NULL";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }
}
