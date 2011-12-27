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
package org.openbravo.wad;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.exception.NoConnectionAvailableException;

class WadConnection implements ConnectionProvider {
  private static final Logger log4j = Logger.getLogger(WadConnection.class);
  private Connection myPool;
  private String defaultPoolName = "";
  private String bbdd = "";
  private String rdbms = "";

  public WadConnection(String xmlPoolFile) {
    if (myPool == null) {
      try {
        connect(xmlPoolFile);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void connect(String file) throws ClassNotFoundException, SQLException {
    if (log4j.isDebugEnabled())
      log4j.debug("Creating Connection");

    String dbDriver = null;
    String dbLogin = null;
    String dbPassword = null;
    int minConns = 1;
    int maxConns = 10;
    double maxConnTime = 0.5;
    String dbSessionConfig = null;

    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(file));
      this.defaultPoolName = properties.getProperty("bbdd.poolName", "myPool");
      dbDriver = properties.getProperty("bbdd.driver");
      this.bbdd = properties.getProperty("bbdd.url");
      dbLogin = properties.getProperty("bbdd.user");
      dbPassword = properties.getProperty("bbdd.password");
      minConns = new Integer(properties.getProperty("bbdd.minConns", "1"));
      maxConns = new Integer(properties.getProperty("bbdd.maxConns", "10"));
      maxConnTime = new Double(properties.getProperty("maxConnTime", "0.5"));
      dbSessionConfig = properties.getProperty("bbdd.sessionConfig");
      this.rdbms = properties.getProperty("bbdd.rdbms");
      if (this.rdbms.equalsIgnoreCase("POSTGRE"))
        this.bbdd += '/' + properties.getProperty("bbdd.sid");
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (log4j.isDebugEnabled())
      log4j.debug("poolName: " + this.defaultPoolName);
    {
      log4j.debug("dbDriver: " + dbDriver);
      log4j.debug("dbServer: " + this.bbdd);
      log4j.debug("dbLogin: " + dbLogin);
      log4j.debug("dbPassword: " + dbPassword);
      log4j.debug("minConns: " + minConns);
      log4j.debug("maxConns: " + maxConns);
      log4j.debug("maxConnTime: " + Double.toString(maxConnTime));
      log4j.debug("dbSessionConfig: " + dbSessionConfig);
      log4j.debug("rdbms: " + this.rdbms);
    }

    try {
      log4j.info("Loading driver: " + dbDriver);
      Class.forName(dbDriver);
      log4j.info("Getting Connection: " + this.bbdd + ',' + dbLogin);
      this.myPool = DriverManager.getConnection(this.bbdd, dbLogin, dbPassword);
      this.myPool.setAutoCommit(true);
    } catch (Exception e) {
      log4j.error(e);
      throw new SQLException("Failed when creating database connections pool");
    }
  }

  public void destroy() {
    try {
      if (myPool != null)
        myPool.close();
      myPool = null;
    } catch (SQLException e) {
      log4j.error("SQL error in closeConnection: " + e);
    }
  }

  public Connection getConnection() throws NoConnectionAvailableException {
    return this.myPool;
  }

  public String getRDBMS() {
    return rdbms;
  }

  public boolean releaseConnection(Connection conn) {
    if (conn == null)
      return false;
    try {
      conn.setAutoCommit(true);
      // conn.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
    return true;
  }

  public Connection getTransactionConnection() throws NoConnectionAvailableException, SQLException {
    Connection conn = getConnection();
    if (conn == null)
      throw new NoConnectionAvailableException("Couldn´t get an available connection");
    conn.setAutoCommit(false);
    return conn;
  }

  public void releaseCommitConnection(Connection conn) throws SQLException {
    if (conn == null)
      return;
    conn.commit();
    releaseConnection(conn);
  }

  public void releaseRollbackConnection(Connection conn) throws SQLException {
    if (conn == null)
      return;
    conn.rollback();
    releaseConnection(conn);
  }

  public PreparedStatement getPreparedStatement(String poolName, String SQLPreparedStatement)
      throws Exception {
    return getPreparedStatement(SQLPreparedStatement);
  }

  public PreparedStatement getPreparedStatement(String SQLPreparedStatement) throws Exception {
    if (log4j.isDebugEnabled())
      log4j.debug("connection requested");
    Connection conn = getConnection();
    if (log4j.isDebugEnabled())
      log4j.debug("connection established");
    return getPreparedStatement(conn, SQLPreparedStatement);
  }

  public PreparedStatement getPreparedStatement(Connection conn, String SQLPreparedStatement)
      throws SQLException {
    if (conn == null || SQLPreparedStatement == null || SQLPreparedStatement.equals(""))
      return null;
    PreparedStatement ps = null;
    try {
      if (log4j.isDebugEnabled())
        log4j.debug("preparedStatement requested");
      ps = conn.prepareStatement(SQLPreparedStatement, ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      if (log4j.isDebugEnabled())
        log4j.debug("preparedStatement received");
    } catch (SQLException e) {
      log4j.error("getPreparedStatement: " + SQLPreparedStatement + "\n" + e);
      releaseConnection(conn);
      throw e;
    }
    return (ps);
  }

  public CallableStatement getCallableStatement(String poolName, String SQLCallableStatement)
      throws Exception {
    return getCallableStatement(SQLCallableStatement);
  }

  public CallableStatement getCallableStatement(String SQLCallableStatement) throws Exception {
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
      log4j.error("getCallableStatement: " + SQLCallableStatement + "\n" + e);
      releaseConnection(conn);
      throw e;
    }
    return (cs);
  }

  public Statement getStatement(String name) throws Exception {
    return getStatement();
  }

  public Statement getStatement() throws Exception {
    Connection conn = getConnection();
    return getStatement(conn);
  }

  public Statement getStatement(Connection conn) throws SQLException {
    if (conn == null)
      return null;
    try {
      return (conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY));
    } catch (SQLException e) {
      log4j.error("getStatement: " + e);
      releaseConnection(conn);
      throw e;
    }
  }

  public void releasePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    if (preparedStatement == null)
      return;
    Connection conn = null;
    try {
      conn = preparedStatement.getConnection();
      preparedStatement.close();
      releaseConnection(conn);
    } catch (SQLException e) {
      log4j.error("releasePreparedStatement: " + e);
      releaseConnection(conn);
      throw e;
    }
  }

  public void releaseCallableStatement(CallableStatement callableStatement) throws SQLException {
    if (callableStatement == null)
      return;
    Connection conn = null;
    try {
      conn = callableStatement.getConnection();
      callableStatement.close();
      releaseConnection(conn);
    } catch (SQLException e) {
      log4j.error("releaseCallableStatement: " + e);
      releaseConnection(conn);
      throw e;
    }
  }

  public void releaseStatement(Statement statement) throws SQLException {
    if (statement == null)
      return;
    Connection conn = null;
    try {
      conn = statement.getConnection();
      statement.close();
      releaseConnection(conn);
    } catch (SQLException e) {
      log4j.error("releaseStatement: " + e);
      releaseConnection(conn);
      throw e;
    }
  }

  public void releaseTransactionalStatement(Statement statement) throws SQLException {
    if (statement == null)
      return;
    statement.close();
  }

  public void releaseTransactionalPreparedStatement(PreparedStatement preparedStatement)
      throws SQLException {
    if (preparedStatement == null)
      return;
    preparedStatement.close();
  }

  /**
   * Returns the actual status of the dynamic pool.
   */
  public String getStatus() {
    StringBuffer strResultado = new StringBuffer();
    strResultado.append("Not implemented yet");
    return strResultado.toString();
  }// End getStatus()
}
