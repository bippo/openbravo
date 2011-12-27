/*
 ************************************************************************************
 * Copyright (C) 2010-2011 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.base;

import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;

/**
 * This class starts a build of Openbravo. It is designed to be called from the application itself
 * (via the Module Management Console). The kind of build which is started is decided by the status
 * of the system database.
 */
public class BuildTask {

  private static String propertiesFile;
  private static Logger log;

  /**
   * This class starts a build of Openbravo. The kind of build is decided by this class logic
   */
  public static void main(String[] args) throws Exception {

    final Properties props = new Properties();
    final String level = Level.INFO.toString();
    props.setProperty("log4j.rootCategory", level + ",A,O2");
    props.setProperty("log4j.appender.A", "org.apache.log4j.ConsoleAppender");
    props.setProperty("log4j.appender.A.layout", "org.apache.log4j.PatternLayout");
    props.setProperty("log4j.appender.A.layout.ConversionPattern", "%-4r %-5p - %m%n");
    // we don't want debug logging from Digester/Betwixt
    props.setProperty("log4j.logger.org.apache.commons", "WARN");
    props.setProperty("log4j.logger.org.hibernate", "WARN");

    // Adding properties for log of Improved Upgrade Process
    props.setProperty("log4j.appender.O2", "org.openbravo.utils.OBRebuildAppender");
    props.setProperty("log4j.appender.O2.layout", "org.apache.log4j.PatternLayout");
    props.setProperty("log4j.appender.O2.layout.ConversionPattern", "%-4r [%t] %-5p %c - %m%n");
    LogManager.resetConfiguration();
    PropertyConfigurator.configure(props);
    log = Logger.getLogger(BuildTask.class);

    propertiesFile = args[0];
    String logFileName = args[1];
    Properties properties = new Properties();
    properties.load(new FileInputStream(propertiesFile));
    AntExecutor ant = new AntExecutor(properties.getProperty("source.path"));
    ant.setLogFileAndListener(logFileName);
    // do not execute translation process (all entries should be already in the module)
    ant.setProperty("tr", "no");
    // We will show special, friendly warnings when they are available
    ant.setProperty("friendlyWarnings", "true");

    // Set a property so build tasks know they have been called via the Rebuild UI from the MMC
    ant.setProperty("runningInRebuildUI", "true");

    final Vector<String> tasks = new Vector<String>();
    final String unnappliedModules = getUnnapliedModules();
    tasks.add("update.database");
    if (isUpdatingCoreOrTemplate()) {
      tasks.add("core.lib");
      tasks.add("wad.lib");
      tasks.add("trl.lib");
      tasks.add("compile.complete.deploy");
      ant.setProperty("apply.on.create", "true");
    } else {
      if (compileCompleteNeeded()) {
        // compile complete is needed for templates because in this case it is not needed which
        // elements belong to the template and for uninistalling modules in order to remove old
        // files and references
        ant.setProperty("apply.modules.complete.compilation", "true");
      }
      tasks.add("apply.modules");
      ant.setProperty("module", unnappliedModules);
    }
    log.info("Executing tasks:");
    for (String task : tasks)
      log.info(task);
    log.info("Modules to be applied: " + unnappliedModules);
    ant.runTask(tasks);
    ant.closeLogFile();
  }

  private static String getUnnapliedModules() throws Exception {
    String strSql = "SELECT JAVAPACKAGE AS NAME FROM AD_MODULE M "
        + " WHERE ISACTIVE='Y' AND (STATUS='I' OR STATUS='U' OR STATUS='P')  "
        + "      AND NOT EXISTS (SELECT 1  FROM AD_MODULE_INSTALL WHERE AD_MODULE_ID = M.AD_MODULE_ID)"
        + " UNION SELECT JAVAPACKAGE AS NAME FROM AD_MODULE_INSTALL";
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement ps = cp.getPreparedStatement(strSql);
    ResultSet rs = ps.executeQuery();
    String rt = "";
    int i = 0;
    while (rs.next()) {
      if (i > 0)
        rt += ",";
      rt += rs.getString(1);
      i++;
    }
    return rt;
  }

  private static boolean isUpdatingCoreOrTemplate() throws Exception {
    String strSql = "SELECT count(*) as NAME FROM"
        + "               ((SELECT 1   FROM AD_MODULE  WHERE (STATUS='I' OR STATUS='P')"
        + "               AND (AD_MODULE_ID = '0' OR TYPE='T'))"
        + "                           UNION "
        + "               (SELECT 1 FROM AD_MODULE_INSTALL  WHERE (STATUS='I' OR STATUS='P') "
        + "               AND (AD_MODULE_ID = '0' OR TYPE='T'))) q";
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement ps = cp.getPreparedStatement(strSql);
    ResultSet rs = ps.executeQuery();
    rs.next();
    return rs.getInt(1) != 0;
  }

  private static boolean compileCompleteNeeded() throws Exception {
    String strSql = "SELECT count(*) as NAME FROM AD_MODULE"
        + " WHERE ((STATUS='I' OR STATUS='P')  AND TYPE = 'T') OR (STATUS='U')";
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement ps = cp.getPreparedStatement(strSql);
    ResultSet rs = ps.executeQuery();
    rs.next();
    return rs.getInt(1) != 0;
  }

  private static ConnectionProvider getConnectionProvider() {
    ConnectionProvider cp = null;
    cp = new CPStandAlone(propertiesFile);
    return cp;
  }

}
