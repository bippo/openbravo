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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.exception.NoConnectionAvailableException;

/**
 * A connection provider which is created on the basis of the current connection of the DAL (see
 * {@link OBDal#getConnection()}).
 * 
 * It read the properties through the {@link OBPropertiesProvider}.
 * 
 * Note: this implementation
 * <ul>
 * <li>does not support connection pooling</li>
 * <li>does not close the connection</li>
 * <li>it flushes the hibernate session before returning a connection by default, but this can be
 * overriden by using the constructor with the flush parameter ({@link OBDal#flush()})</li>
 * </ul>
 * 
 * @author mtaal
 */
public class DalConnectionProvider implements ConnectionProvider {

  private Connection connection;
  private Properties properties;
  // This parameter can be used to define whether the OBDal needs to be flushed when the connection
  // is retrieved or not
  private boolean flush = true;

  public void destroy() throws Exception {
    // never close
  }

  public DalConnectionProvider() {

  }

  /**
   * 
   * @param flush
   *          if set to true, the getConnection method will flush the OBDal instance.
   */
  public DalConnectionProvider(boolean flush) {
    this.flush = flush;
  }

  public Connection getConnection() throws NoConnectionAvailableException {
    if (connection == null) {
      connection = OBDal.getInstance().getConnection(flush);
    }

    // always flush all remaining actions
    if (flush) {
      OBDal.getInstance().flush();
    }
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public String getRDBMS() {
    return getProperties().getProperty("bbdd.rdbms");
  }

  public Connection getTransactionConnection() throws NoConnectionAvailableException, SQLException {
    Connection conn = getConnection();
    if (conn == null) {
      throw new NoConnectionAvailableException("Couldn´t get an available connection");
    }
    conn.setAutoCommit(false);
    return conn;
  }

  public void releaseCommitConnection(Connection conn) throws SQLException {
    if (conn == null)
      return;
    conn.commit();
  }

  public void releaseRollbackConnection(Connection conn) throws SQLException {
    if (conn == null)
      return;
    conn.rollback();
  }

  public PreparedStatement getPreparedStatement(String SQLPreparedStatement) throws Exception {
    return getPreparedStatement(getConnection(), SQLPreparedStatement);
  }

  public PreparedStatement getPreparedStatement(String poolName, String SQLPreparedStatement)
      throws Exception {
    return getPreparedStatement(getConnection(), SQLPreparedStatement);
  }

  public PreparedStatement getPreparedStatement(Connection conn, String SQLPreparedStatement)
      throws SQLException {
    PreparedStatement ps = conn.prepareStatement(SQLPreparedStatement,
        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    return ps;
  }

  public CallableStatement getCallableStatement(String SQLCallableStatement) throws Exception {
    return getCallableStatement("", SQLCallableStatement);
  }

  public CallableStatement getCallableStatement(String poolName, String SQLCallableStatement)
      throws Exception {
    Connection conn = getConnection();
    return getCallableStatement(conn, SQLCallableStatement);
  }

  public CallableStatement getCallableStatement(Connection conn, String SQLCallableStatement)
      throws SQLException {
    if (conn == null || SQLCallableStatement == null || SQLCallableStatement.equals(""))
      return null;
    CallableStatement cs = null;
    try {
      cs = conn.prepareCall(SQLCallableStatement);
    } catch (SQLException e) {
      throw e;
    }
    return (cs);
  }

  public Statement getStatement() throws Exception {
    return getStatement("");
  }

  public Statement getStatement(String poolName) throws Exception {
    Connection conn = getConnection();
    return getStatement(conn);
  }

  public Statement getStatement(Connection conn) throws SQLException {
    if (conn == null)
      return null;
    try {
      return (conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY));
    } catch (SQLException e) {
      throw e;
    }
  }

  public void releasePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    if (preparedStatement == null) {
      return;
    }
    preparedStatement.close();
  }

  public void releaseCallableStatement(CallableStatement callableStatement) throws SQLException {
    if (callableStatement == null) {
      return;
    }
    callableStatement.close();
  }

  public void releaseStatement(Statement statement) throws SQLException {
    if (statement == null) {
      return;
    }
    statement.close();
  }

  public void releaseTransactionalStatement(Statement statement) throws SQLException {
    if (statement == null) {
      return;
    }
    statement.close();
  }

  public void releaseTransactionalPreparedStatement(PreparedStatement preparedStatement)
      throws SQLException {
    if (preparedStatement == null) {
      return;
    }
    preparedStatement.close();
  }

  public String getStatus() {
    return "Not implemented";
  }

  public Properties getProperties() {
    if (properties == null) {
      properties = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    }
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }
}
