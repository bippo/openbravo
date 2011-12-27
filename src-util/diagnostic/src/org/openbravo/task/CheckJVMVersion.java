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

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openbravo.utils.PropertiesManager;
import org.openbravo.utils.ServerConnection;
import org.openbravo.utils.Version;

public class CheckJVMVersion extends Task {
  static Logger log4j = Logger.getLogger(CheckJVMVersion.class);

  @Override
  public void execute() throws BuildException {
    log4j.info("Checking Tomcat's JVM version...");

    final String jvmVersion = new ServerConnection().getCheck("jvm-version");
    final String minJvmVersion = new PropertiesManager().getProperty("jvm-version");

    final String msg = "Current Tomcat's JVM version: " + jvmVersion
        + " minimum required version: " + minJvmVersion;

    if (Version.compareVersion(jvmVersion, minJvmVersion) < 0)
      throw new BuildException(msg
          + "\nTip: check http://wiki.openbravo.com/wiki/Development_Stack_Setup#Apache_Tomcat");
    else {
      log4j.info(msg);
      log4j.info("Tomcat's JVM version OK");
    }
  }

  public static void main(String[] args) {
    final CheckJVMVersion c = new CheckJVMVersion();
    c.execute();
  }

}
