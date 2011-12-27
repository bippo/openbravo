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
package org.openbravo.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.openbravo.database.ConnectionProviderImpl;

/* This class inserts the rebuild log into a table in the database.
 * This information is used in the rebuild window in Openbravo
 */
public class OBRebuildAppender extends AppenderSkeleton {

  private ConnectionProviderImpl cp;
  private Connection connection;
  private static final Logger log4j = Logger.getLogger(OBRebuildAppender.class);
  private static File properties = null;
  private static String Basedir;

  @Override
  protected void append(LoggingEvent arg0) {
    if (arg0.getLevel().isGreaterOrEqual(Level.INFO))
      try {
        if (cp == null) {
          File f = new File("");
          f = new File(f.getAbsolutePath());
          File fProp = null;
          if (Basedir != null)
            fProp = new File(Basedir, "config/Openbravo.properties");
          else {
            if (new File("../../config/Openbravo.properties").exists())
              fProp = new File("../../config/Openbravo.properties");
            else if (new File("../config/Openbravo.properties").exists())
              fProp = new File("../config/Openbravo.properties");
            else if (new File("config/Openbravo.properties").exists())
              fProp = new File("config/Openbravo.properties");
          }
          if (fProp != null)
            properties = fProp;
          cp = new ConnectionProviderImpl(properties.getAbsolutePath());
        }
        if (cp == null) {
          log4j
              .error("Error while initializing connection pool" + (new File("").getAbsolutePath()));
          return;
        }
        if (connection == null || connection.isClosed()) {
          connection = cp.getConnection();
        }
        PreparedStatement ln = connection
            .prepareStatement("SELECT coalesce(max(line_number)+1,1) FROM AD_ERROR_LOG");
        ResultSet rs = ln.executeQuery();
        String line_number;
        if (rs.next()) {
          line_number = rs.getString(1);
        } else {
          line_number = "1";
        }

        String message = arg0.getMessage().toString();
        if (message.length() > 3000)
          message = message.substring(0, 2997) + "...";
        PreparedStatement ps = connection
            .prepareStatement("INSERT INTO ad_error_log (ad_error_log_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, system_status, error_level, message, line_number) SELECT get_uuid(), '0', '0', 'Y', now(), '0', now(), '0', system_status, ?,?, to_number(?) FROM ad_system_info");
        String level = arg0.getLevel().toString();

        ps.setString(1, level);
        ps.setString(2, message);
        ps.setString(3, line_number);
        ps.executeUpdate();
      } catch (Exception e) {
        // We will not log an error if the insertion in the log table
        // goes wrong for two different reasons:
        // - First, it could cause problems if the message itself is redirected to the log again
        // - Second, if the instance which is being rebuild doesn't yet have the log table, or the
        // table is being recreated, the insertion will fail, and this is ok.
        // We don't need to have log lines in the database in that case
      }

  }

  @Override
  public void close() {
    try {
      if (connection != null)
        connection.close();
    } catch (Exception e) {
    }

  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  /*
   * Sets the basedir (directory of the Openbravo sources, which needs to contain a
   * config/Openbravo.properties folder
   */
  public void setBasedir(String basedir) {
    Basedir = basedir;
  }

  /*
   * returns the basedir (directory of the Openbravo sources, which needs to contain a
   * config/Openbravo.properties folder
   */
  public String getBasedir() {
    return Basedir;
  }

}
