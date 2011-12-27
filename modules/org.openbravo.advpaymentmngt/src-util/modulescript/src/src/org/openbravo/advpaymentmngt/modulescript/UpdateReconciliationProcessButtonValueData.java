//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class UpdateReconciliationProcessButtonValueData implements FieldProvider {
static Logger log4j = Logger.getLogger(UpdateReconciliationProcessButtonValueData.class);
  private String InitRecordNumber="0";
  public String name;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("name"))
      return name;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static UpdateReconciliationProcessButtonValueData[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static UpdateReconciliationProcessButtonValueData[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        select 1 as name from dual";

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
        UpdateReconciliationProcessButtonValueData objectUpdateReconciliationProcessButtonValueData = new UpdateReconciliationProcessButtonValueData();
        objectUpdateReconciliationProcessButtonValueData.name = UtilSql.getValue(result, "name");
        objectUpdateReconciliationProcessButtonValueData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectUpdateReconciliationProcessButtonValueData);
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
    UpdateReconciliationProcessButtonValueData objectUpdateReconciliationProcessButtonValueData[] = new UpdateReconciliationProcessButtonValueData[vector.size()];
    vector.copyInto(objectUpdateReconciliationProcessButtonValueData);
    return(objectUpdateReconciliationProcessButtonValueData);
  }

  public static int update(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        UPDATE FIN_RECONCILIATION SET EM_APRM_PROCESS_REC = EM_APRM_PROCESS_RECONCILIATION" +
      "        WHERE EM_APRM_PROCESS_REC IS NULL" +
      "        AND EM_APRM_PROCESS_RECONCILIATION IS NOT NULL";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

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
