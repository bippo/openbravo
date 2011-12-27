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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.openbravo.exception.NoConnectionAvailableException;

public interface ConnectionProvider {
  public Connection getConnection() throws NoConnectionAvailableException;

  public String getRDBMS();

  public Connection getTransactionConnection() throws NoConnectionAvailableException, SQLException;

  public void releaseCommitConnection(Connection conn) throws SQLException;

  public void releaseRollbackConnection(Connection conn) throws SQLException;

  public PreparedStatement getPreparedStatement(String poolName, String strSql) throws Exception;

  public PreparedStatement getPreparedStatement(String strSql) throws Exception;

  public PreparedStatement getPreparedStatement(Connection conn, String strSql) throws SQLException;

  public void releasePreparedStatement(PreparedStatement preparedStatement) throws SQLException;

  public Statement getStatement(String poolName) throws Exception;

  public Statement getStatement() throws Exception;

  public Statement getStatement(Connection conn) throws SQLException;

  public void releaseStatement(Statement statement) throws SQLException;

  public void releaseTransactionalStatement(Statement statement) throws SQLException;

  public void releaseTransactionalPreparedStatement(PreparedStatement preparedStatement)
      throws SQLException;

  public CallableStatement getCallableStatement(String poolName, String strSql) throws Exception;

  public CallableStatement getCallableStatement(String strSql) throws Exception;

  public CallableStatement getCallableStatement(Connection conn, String strSql) throws SQLException;

  public void releaseCallableStatement(CallableStatement callableStatement) throws SQLException;

  public void destroy() throws Exception;

  public String getStatus();
}
