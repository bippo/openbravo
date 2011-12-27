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
package org.openbravo.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.data.UtilSql;

public class RDBMSIndependent {

  public static Vector<String> getCallableResult(Connection conn,
      ConnectionProvider connectionProvider, String sql, Vector<String> parameters,
      Vector<String> types, int totalOutParameters) throws Exception {
    StringBuffer strSql = new StringBuffer();
    sql = sql.toUpperCase().replace("CALL ", "SELECT * FROM ");
    sql = sql.toUpperCase().replace("ALL TRIGGERS", "TRIGGER ALL");
    if (totalOutParameters == 1) {
      int init = sql.indexOf("(");
      if (init == -1)
        throw new ServletException("Badly formed sql: " + sql);
      strSql.append(sql.substring(0, init + 1));
      int end = sql.lastIndexOf(")");
      if (end == -1)
        throw new ServletException("Badly formed sql: " + sql);
      boolean found = false, first = true;
      int count = 0;
      StringTokenizer stoken = new StringTokenizer(sql.substring(init + 1, end), ",", false);
      while (stoken.hasMoreTokens()) {
        String token = stoken.nextToken();
        if (token.indexOf("?") != -1) {
          if (!found && types.elementAt(count).equalsIgnoreCase("out")) {
            found = true;
          } else {
            strSql.append((!first ? "," : "")).append(token);
            first = false;
          }
          count++;
        } else {
          strSql.append((!first ? "," : "")).append(token);
          first = false;
        }
      }
      strSql.append(sql.substring(end));
    } else
      strSql.append(sql);

    PreparedStatement st = null;
    if (conn == null)
      st = connectionProvider.getPreparedStatement(strSql.toString());
    else
      st = connectionProvider.getPreparedStatement(conn, strSql.toString());
    ResultSet result;
    Vector<String> total = new Vector<String>();

    int iParameter = 0;
    try {
      if (parameters != null) {
        for (int i = 0; i < parameters.size(); i++) {
          String typeAux = types.elementAt(i);
          if (!typeAux.equalsIgnoreCase("out")) {
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, "Test", parameters.elementAt(i));
          }
        }
      }
      if (!strSql.toString().toUpperCase().trim().startsWith("ALTER ")) {
        result = st.executeQuery();
        int pos = 0;
        if (result.next() && totalOutParameters > 0 && parameters != null) {
          for (int i = 0; i < parameters.size(); i++) {
            if (types.elementAt(i).equalsIgnoreCase("out")) {
              pos++;
              total.addElement(UtilSql.getValue(result, pos));
            }
          }
        }
        result.close();
      } else {
        st.executeUpdate();
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      if (conn == null)
        connectionProvider.releasePreparedStatement(st);
      else
        connectionProvider.releaseTransactionalStatement(st);
    }
    return (total);
  }
}
