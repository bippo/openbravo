//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class CreateDocumentNoSequenceData implements FieldProvider {
static Logger log4j = Logger.getLogger(CreateDocumentNoSequenceData.class);
  private String InitRecordNumber="0";
  public String tablename;
  public String client;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("TABLENAME"))
      return tablename;
    else if (fieldName.equalsIgnoreCase("CLIENT"))
      return client;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static CreateDocumentNoSequenceData[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static CreateDocumentNoSequenceData[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT tablename, ad_client.ad_client_id as client" +
      "      FROM ad_table, ad_client" +
      "      WHERE EXISTS (SELECT 1 FROM ad_column" +
      "                    WHERE ad_column.ad_table_id = ad_table.ad_table_id" +
      "                      AND columnname IN ('DocumentNo', 'Value'))" +
      "      AND NOT EXISTS (SELECT 1 FROM ad_sequence" +
      "                      WHERE name LIKE 'DocumentNo_' || ad_table.tablename" +
      "                        AND ad_sequence.ad_client_id = ad_client.ad_client_id)" +
      "      AND ad_client.ad_client_id  <> '0'";

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
        CreateDocumentNoSequenceData objectCreateDocumentNoSequenceData = new CreateDocumentNoSequenceData();
        objectCreateDocumentNoSequenceData.tablename = UtilSql.getValue(result, "TABLENAME");
        objectCreateDocumentNoSequenceData.client = UtilSql.getValue(result, "CLIENT");
        objectCreateDocumentNoSequenceData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectCreateDocumentNoSequenceData);
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
    CreateDocumentNoSequenceData objectCreateDocumentNoSequenceData[] = new CreateDocumentNoSequenceData[vector.size()];
    vector.copyInto(objectCreateDocumentNoSequenceData);
    return(objectCreateDocumentNoSequenceData);
  }

  public static int insertDocumentNoSequence(Connection conn, ConnectionProvider connectionProvider, String clientId, String tablename)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO AD_Sequence (" +
      "        AD_Sequence_ID, AD_Client_ID, AD_Org_ID, IsActive, " +
      "        Created, CreatedBy, Updated, UpdatedBy," +
      "        Name, Description, " +
      "        VFormat, IsAutoSequence, IncrementNo, " +
      "        StartNo, CurrentNext, CurrentNextSys, " +
      "        IsTableID, Prefix, Suffix, StartNewYear" +
      "      ) VALUES (" +
      "        get_uuid(), ?, '0', 'Y'," +
      "        now(), '0', now(), '0'," +
      "        'DocumentNo_' || ?,  'DocumentNo/Value for Table ' || ?," +
      "        NULL,  'Y', 1," +
      "        10000000, 10000000, 10000000," +
      "        'N', NULL, NULL, 'N'" +
      "      )";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, clientId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, tablename);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, tablename);

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
