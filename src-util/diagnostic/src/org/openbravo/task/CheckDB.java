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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.task;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openbravo.utils.PropertiesManager;
import org.openbravo.utils.Version;

public class CheckDB extends Task {
  static Logger log4j = Logger.getLogger(CheckDB.class);

  @Override
  public void execute() throws BuildException {

    log4j.info("Checking database...");
    final Properties properties = new Properties();
    try {
      properties.load(new FileInputStream("config/Openbravo.properties"));
    } catch (final Exception e) {
      throw new BuildException(e);
    }

    final String rdbms = properties.getProperty("bbdd.rdbms");
    final String dbDriver = properties.getProperty("bbdd.driver");
    String dbServer = properties.getProperty("bbdd.url");

    final String user = properties.getProperty("bbdd.user");
    final String userPassword = properties.getProperty("bbdd.password");

    final String systemUser = properties.getProperty("bbdd.systemUser");
    final String systemUserPassword = properties.getProperty("bbdd.systemPassword");

    if (rdbms.equalsIgnoreCase("POSTGRE"))
      dbServer += "/" + properties.getProperty("bbdd.sid");

    try {
      Class.forName(dbDriver);
    } catch (final Exception e) {
      throw new BuildException(e.getMessage());
    }

    log4j.info("Trying to connect to db server as standard user.");
    try {
      final Connection connUser = DriverManager.getConnection(dbServer, user, userPassword);
      connUser.close();
    } catch (final Exception e) {
      log4j.warn("Couldn't connect to standard user (" + user
          + ") tip, database might require to be created. Error:" + e.getMessage());
    }
    log4j.info("Connection OK");

    log4j.info("Trying to connect to db server as system user.");
    Connection connSystem = null;
    try {
      connSystem = DriverManager.getConnection(dbServer, systemUser, systemUserPassword);
    } catch (final Exception e) {
      throw new BuildException(e.getMessage());
    }
    log4j.info("Connection OK");

    PreparedStatement st;
    ResultSet result;

    if (rdbms.equalsIgnoreCase("ORACLE")) {
      // Check version
      String versionString = "";
      log4j.info("Checking Oracle version...");
      try {
        st = connSystem.prepareStatement("select * from v$version where banner like '%Oracle%'");
        result = st.executeQuery();
        while (result.next()) {
          versionString = result.getString(1);
        }
        result.close();
        st.close();
      } catch (final Exception e) {
        throw new BuildException(e.getMessage());
      }
      final String version = Version.getVersion(versionString);
      final String minVersion = new PropertiesManager().getProperty("db.ora.version");
      String msg = "Minimum required version: " + minVersion + ", current version " + version;
      if (Version.compareVersion(version, minVersion) < 0)
        throw new BuildException(msg
            + "\nTip: check http://wiki.openbravo.com/wiki/Development_Stack_Setup#Oracle");
      else {
        log4j.info(msg);
        log4j.info("Oracle version OK");
      }

      // check open_cursors
      log4j.info("Checking Oracle open cursors...");
      long openCursors = 0;
      try {
        st = connSystem
            .prepareStatement("SELECT value FROM v$parameter WHERE name ='open_cursors'");
        result = st.executeQuery();
        while (result.next()) {
          openCursors = new Long(result.getString(1));
        }
        result.close();
        st.close();
      } catch (final Exception e) {
        throw new BuildException(e.getMessage());
      }
      final long minOpenCursors = new Long(
          new PropertiesManager().getProperty("db.ora.opencursors"));
      msg = "Minimum open cursors required: " + minOpenCursors + ", current open cursors "
          + openCursors;
      if (openCursors < minOpenCursors)
        throw new BuildException(msg
            + "\nTip: check http://wiki.openbravo.com/wiki/Development_Stack_Setup#Oracle");
      else {
        log4j.info(msg);
        log4j.info("Open cursors OK");
      }

      // check processes
      log4j.info("Checking Oracle open cursors...");
      long processes = 0;
      try {
        st = connSystem.prepareStatement("SELECT value FROM v$parameter WHERE name ='processes'");
        result = st.executeQuery();
        while (result.next()) {
          processes = new Long(result.getString(1));
        }
        result.close();
        st.close();
      } catch (final Exception e) {
        throw new BuildException(e.getMessage());
      }
      final long minProcesses = new Long(new PropertiesManager().getProperty("db.ora.processes"));
      msg = "Minimum open processes required: " + minProcesses + ", current processes " + processes;
      if (processes < minProcesses)
        throw new BuildException(msg
            + "\nTip: check http://wiki.openbravo.com/wiki/Development_Stack_Setup#Oracle");
      else {
        log4j.info(msg);
        log4j.info("Open cursors OK");
      }

      try {
        st = connSystem
            .prepareStatement("select value from nls_database_parameters where parameter='NLS_NCHAR_CHARACTERSET'");
        result = st.executeQuery();
        result.next();
        String nchar_charset = result.getString(1);
        if (nchar_charset.equals("AL16UTF16"))
          log4j.info("NCHAR charset encoding OK.");
        else {
          throw new BuildException("NCHAR charset encoding incorrect. Current encoding: "
              + nchar_charset + ". Required encoding: AL16UTF16");
        }
        result.close();
        st.close();
      } catch (final Exception e) {
        throw new BuildException(e.getMessage());
      }
    } else { // PostgreSQL
      // Check version
      log4j.info("Checking PostgreSQL version...");
      String version = "";
      try {
        st = connSystem
            .prepareStatement("SELECT setting FROM pg_settings WHERE name = 'server_version'");

        result = st.executeQuery();

        while (result.next()) {
          version = result.getString(1);
        }
        result.close();
        st.close();
      } catch (final Exception e) {
        throw new BuildException(e.getMessage());
      }

      String minVersion = new PropertiesManager().getProperty("db.pg.version");
      if (System.getProperty("os.name").equalsIgnoreCase("Windows"))
        minVersion = new PropertiesManager().getProperty("db.pg.windows.version");
      else
        minVersion = new PropertiesManager().getProperty("db.pg.version");

      final String msg = "Minimum required version: " + minVersion + ", current version " + version;
      if (Version.compareVersion(version, minVersion) < 0)
        throw new BuildException(msg
            + "\nTip: check http://wiki.openbravo.com/wiki/Development_Stack_Setup#PostgreSQL");
      else {
        log4j.info(msg);
        log4j.info("PostgreSQL version OK");
      }

      // Check contrib
      log4j.info("Checking PostgreSQL contrib package installed...");
      try {
        st = connSystem.prepareStatement("SELECT uuid_generate_v4()");
        result = st.executeQuery();
        result.close();
        st.close();
      } catch (final Exception e) {
        throw new BuildException("PostgreSQL contrib package seems not to be installed"
            + "\nTip: check http://wiki.openbravo.com/wiki/Development_Stack_Setup#PostgreSQL");
      }
      log4j.info("Contrib package OK");

    }
    try {
      connSystem.close();
    } catch (final Exception e) {
      throw new BuildException(e.getMessage());
    }
  }

}
