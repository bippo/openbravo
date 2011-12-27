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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.exception.NoConnectionAvailableException;

class SQLExecutor_Query implements FieldProvider {
  static Logger log4j = Logger.getLogger(SQLExecutor_Query.class);
  Vector<String> data = new Vector<String>();
  Vector<String> type = new Vector<String>();
  Vector<String> name = new Vector<String>();

  public String getField(String fieldIndex) {
    int field = Integer.parseInt(fieldIndex);
    if (data != null && data.size() > field)
      return data.elementAt(field);
    else {
      log4j.warn("The field does not exist: " + field);
      return null;
    }
  }

  public static SQLExecutor_Query[] select(ConnectionProvider connectionProvider, String strSQL)
      throws ServletException {
    return select(connectionProvider, strSQL, 0, 0);
  }

  public static SQLExecutor_Query[] select(ConnectionProvider connectionProvider, String strSQL,
      int firstRegister, int numberRegisters) throws ServletException {

    PreparedStatement st = null;
    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);

    try {
      if (log4j.isDebugEnabled())
        log4j.debug("select - Preparing Statement\n");
      st = connectionProvider.getPreparedStatement(strSQL);
      if (log4j.isDebugEnabled())
        log4j.debug("select - Statement Prepared\n");
      if (log4j.isDebugEnabled())
        log4j.debug("select - Executing Statement\n");
      result = st.executeQuery();
      if (log4j.isDebugEnabled())
        log4j.debug("select - Statement Executed\n");
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      ResultSetMetaData rmeta = result.getMetaData();
      int numColumns = rmeta.getColumnCount();
      Vector<String> types = new Vector<String>(0);
      Vector<String> names = new Vector<String>(0);
      if (log4j.isDebugEnabled())
        log4j.debug("select - Making data\n");
      while (continueResult && result.next()) {
        countRecord++;
        SQLExecutor_Query objectSQLExecutor_Query = new SQLExecutor_Query();
        for (int i = 1; i <= numColumns; i++) {
          String aux = "";
          try {
            aux = result.getString(i);
          } catch (Exception ignored) {
          }
          if (aux == null)
            aux = "";
          objectSQLExecutor_Query.data.addElement(aux);
          if (countRecord > 1) {
            objectSQLExecutor_Query.type = types;
            objectSQLExecutor_Query.name = names;
          } else {
            String auxType = transformSQLType(rmeta.getColumnType(i));
            String auxName = rmeta.getColumnName(i);
            if (auxType.equals("NUMBER") && auxName.toUpperCase().endsWith("_ID"))
              auxType = "ID";
            objectSQLExecutor_Query.type.addElement(auxType);
            objectSQLExecutor_Query.name.addElement(auxName);
          }
        }
        types = objectSQLExecutor_Query.type;
        names = objectSQLExecutor_Query.name;

        vector.addElement(objectSQLExecutor_Query);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      if (log4j.isDebugEnabled())
        log4j.debug("select - Closing resultset\n");
      result.close();
    } catch (NoConnectionAvailableException ex) {
      log4j.error("No connection available error in query: " + strSQL + "Exception:" + ex);
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      log4j.error("SQL error in query: " + strSQL + "Exception:" + ex2);
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@"
          + ex2.getMessage());
    } catch (Exception ex3) {
      log4j.error("Error in query: " + strSQL + "Exception:" + ex3);
      throw new ServletException("@CODE=@" + ex3.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignored) {
      }
    }
    SQLExecutor_Query objectSQLExecutor_Query[] = new SQLExecutor_Query[vector.size()];
    vector.copyInto(objectSQLExecutor_Query);
    if (log4j.isDebugEnabled())
      log4j.debug("select - returning data\n");
    return (objectSQLExecutor_Query);
  }

  public static String transformSQLType(int sql_type) {
    String strType = "";
    switch (sql_type) {
    case Types.INTEGER:
    case Types.SMALLINT:
    case Types.TINYINT:
    case Types.BIGINT:
      strType = "INTEGER";
      break;
    case Types.CLOB:
    case Types.BLOB:
    case Types.BINARY:
      strType = "FILE";
      break;
    case Types.DECIMAL:
    case Types.DOUBLE:
    case Types.FLOAT:
    case Types.LONGVARBINARY:
    case Types.LONGVARCHAR:
    case Types.NUMERIC:
    case Types.REAL:
    case Types.BIT:
      strType = "NUMBER";
      break;
    case Types.BOOLEAN:
      strType = "BOOLEAN";
      break;
    case Types.DATE:
      strType = "DATE";
      break;
    case Types.TIME:
      strType = "TIME";
      break;
    case Types.TIMESTAMP:
      strType = "DATETIME";
      break;
    default:
      strType = "STRING";
    }
    return strType;
  }
}
