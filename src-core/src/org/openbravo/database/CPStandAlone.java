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

import org.apache.commons.pool.ObjectPool;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.exception.PoolNotFoundException;

public class CPStandAlone implements ConnectionProvider {
  protected ConnectionProviderImpl myPool;

  public CPStandAlone(String xmlPoolFile) {
    if (myPool == null) {
      try {
        myPool = new ConnectionProviderImpl(xmlPoolFile);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /*
   * Database access utilities
   */
  @SuppressWarnings("unused")
  // to be used in pool status service
  private ObjectPool getPool(String poolName) throws PoolNotFoundException {
    if (myPool == null)
      throw new PoolNotFoundException(poolName + " not found");
    else
      return myPool.getPool(poolName);
  }

  @SuppressWarnings("unused")
  // to be used in pool status service
  private ObjectPool getPool() throws PoolNotFoundException {
    if (myPool == null)
      throw new PoolNotFoundException("Default pool not found");
    else
      return myPool.getPool();
  }

  public Connection getConnection() throws NoConnectionAvailableException {
    return (myPool.getConnection());
  }

  public String getRDBMS() {
    return (myPool.getRDBMS());
  }

  public Connection getTransactionConnection() throws NoConnectionAvailableException, SQLException {
    return myPool.getTransactionConnection();
  }

  public void releaseCommitConnection(Connection conn) throws SQLException {
    myPool.releaseCommitConnection(conn);
  }

  public void releaseRollbackConnection(Connection conn) throws SQLException {
    myPool.releaseRollbackConnection(conn);
  }

  public PreparedStatement getPreparedStatement(String poolName, String strSql) throws Exception {
    return myPool.getPreparedStatement(poolName, strSql);
  }

  public PreparedStatement getPreparedStatement(String strSql) throws Exception {
    return myPool.getPreparedStatement(strSql);
  }

  public PreparedStatement getPreparedStatement(Connection conn, String strSql) throws SQLException {
    return myPool.getPreparedStatement(conn, strSql);
  }

  public void releasePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    myPool.releasePreparedStatement(preparedStatement);
  }

  public Statement getStatement(String poolName) throws Exception {
    return myPool.getStatement(poolName);
  }

  public Statement getStatement() throws Exception {
    return myPool.getStatement();
  }

  public Statement getStatement(Connection conn) throws SQLException {
    return myPool.getStatement(conn);
  }

  public void releaseStatement(Statement statement) throws SQLException {
    myPool.releaseStatement(statement);
  }

  public void releaseTransactionalStatement(Statement statement) throws SQLException {
    myPool.releaseTransactionalStatement(statement);
  }

  public void releaseTransactionalPreparedStatement(PreparedStatement preparedStatement)
      throws SQLException {
    myPool.releaseTransactionalPreparedStatement(preparedStatement);
  }

  public CallableStatement getCallableStatement(String poolName, String strSql) throws Exception {
    return myPool.getCallableStatement(poolName, strSql);
  }

  public CallableStatement getCallableStatement(String strSql) throws Exception {
    return myPool.getCallableStatement(strSql);
  }

  public CallableStatement getCallableStatement(Connection conn, String strSql) throws SQLException {
    return myPool.getCallableStatement(conn, strSql);
  }

  public void releaseCallableStatement(CallableStatement callableStatement) throws SQLException {
    myPool.releaseCallableStatement(callableStatement);
  }

  public void destroy() {
    try {
      myPool.destroy();
      myPool = null;
    } catch (Exception ex) {
    }
  }

  public String getStatus() {
    // TODO Auto-generated method stub
    return null;
  }
}
