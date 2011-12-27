/*
 ************************************************************************************
 * Copyright (C) 2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.modulescript;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;

/**
 * Clases extending ModuleScript can be included in Openbravo Core or a module and will be
 * automatically executed when the system is rebuilt (technically in: update.database and
 * update.database.mod)
 * 
 */
public abstract class ModuleScript {

  private static final Logger log4j = Logger.getLogger(ModuleScript.class);
  private ConnectionProvider cp = null;

  /**
   * This method must be implemented by the ModuleScripts, and is used to define the actions that
   * the script itself will take. This method will be automatically called by the
   * ModuleScriptHandler when the update.database or the update.database.mod tasks are being
   * executed
   */
  public abstract void execute();

  /**
   * This method returns a connection provider, which can be used to execute statements in the
   * database
   * 
   * @return a ConnectionProvider
   */
  protected ConnectionProvider getConnectionProvider() {
    if (cp != null) {
      return cp;
    }
    File fProp = getPropertiesFile();
    cp = new CPStandAlone(fProp.getAbsolutePath());
    return cp;
  }

  protected File getPropertiesFile() {
    File fProp = null;
    if (new File("config/Openbravo.properties").exists())
      fProp = new File("config/Openbravo.properties");
    else if (new File("../config/Openbravo.properties").exists())
      fProp = new File("../config/Openbravo.properties");
    else if (new File("../../config/Openbravo.properties").exists())
      fProp = new File("../../config/Openbravo.properties");
    return fProp;
  }

  protected void handleError(Throwable t) {
    log4j
        .error("Error executing moduleScript " + this.getClass().getName() + ": " + t.getMessage());
    throw new BuildException("Execution of moduleScript " + this.getClass().getName() + "failed.");
  }
}