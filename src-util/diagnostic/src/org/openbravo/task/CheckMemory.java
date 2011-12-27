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

public class CheckMemory extends Task {
  static Logger log4j = Logger.getLogger(CheckMemory.class);

  @Override
  public void execute() throws BuildException {
    log4j.info("Checking ant's memory...");
    final Runtime runtime = Runtime.getRuntime();
    final long maxCurrentMemory = runtime.maxMemory() / (1024 * 1024); // Memory
    // in
    // MB

    final long maxMemory = new Long(new PropertiesManager().getProperty("max.memory"));
    final String msg = "Current max ant's memory:" + maxCurrentMemory + "M, minimum required:"
        + maxMemory + "M";
    // check max memory +- 5%, because it is not accurate
    if (maxMemory > maxCurrentMemory * 1.05)
      throw new BuildException(msg
          + "\nTip: check http://wiki.openbravo.com/wiki/Development_Stack_Setup#Apache_Ant");
    else {
      log4j.info(msg);
      log4j.info("Ant's memory OK ");
    }
  }

}
