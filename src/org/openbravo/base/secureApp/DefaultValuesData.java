/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.base.secureApp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.exception.PoolNotFoundException;

/**
 * Clase SqlStandardData
 */
class DefaultValuesData implements FieldProvider {
  public String columnname;
  static Logger log4j = Logger.getLogger(DefaultValuesData.class);

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("columnname"))
      return columnname;
    else {
      return null;
    }
  }

  /**
   * Select for relation
   */
  public static String select(ConnectionProvider connectionProvider, String param1, String param2,
      String param3, String param4) throws ServletException {
    String strSql = "SELECT " + param1 + " AS COLUMNNAME";
    strSql = strSql + " FROM " + param2 + " ";
    strSql = strSql + " WHERE isActive = 'Y' ";
    strSql = strSql + " AND isDefault = 'Y' ";
    strSql = strSql + " AND AD_Client_ID IN (" + param3 + ") ";
    strSql = strSql + " AND AD_Org_ID IN (" + param4 + ") ";
    strSql = strSql + " ORDER BY AD_Client_ID";

    Statement st = null;
    ResultSet result;
    String resultado = "";

    try {
      st = connectionProvider.getStatement();
      result = st.executeQuery(strSql);

      if (result.next()) {
        resultado = UtilSql.getValue(result, "COLUMNNAME");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (NoConnectionAvailableException ec) {
      log4j.error("Connection error in query: " + strSql + "Exception:" + ec);
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (PoolNotFoundException ep) {
      log4j.error("Pool error in query: " + strSql + "Exception:" + ep);
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (resultado);
  }
}