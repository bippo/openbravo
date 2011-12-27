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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Checks whether the required environment variables are set.
 * 
 * @author awolski
 * 
 */
public class CheckEnvironmentVariables extends Task {

  static Logger log4j = Logger.getLogger(CheckEnvironmentVariables.class);

  /**
   * A mapping of the required variables and the message to display if not found.
   */
  static Map<String, String> varsToCheck;

  static {
    varsToCheck = new HashMap<String, String>();
    varsToCheck.put("CATALINA_HOME", "CATALINA_HOME environment variable is required. "
        + "Tip: Set a CATALINA_HOME environment variable to the"
        + " directory where Tomcat is installed.");
  }

  /**
   * Verifies that all of the required environment variables are set.
   */
  @Override
  public void execute() throws BuildException {
    log4j.info("Checking for required environment variables...");
    String msg = "";

    final Set<Entry<String, String>> vars = varsToCheck.entrySet();

    for (final Entry<String, String> var : vars) {
      final String name = var.getKey();
      final String tip = var.getValue();

      final String value = System.getenv(name);
      if (value == null) {
        msg += tip + "\n";
      } else {
        log4j.info(name + " found: " + value);
      }
    }
    if (!"".equals(msg)) {
      throw new BuildException(msg);
    } else {
      log4j.info("Environment variables OK");
    }
  }
}
