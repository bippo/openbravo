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
import org.openbravo.utils.Version;

public class CompareVersion extends Task {
  private String v1;
  private String v2;
  static Logger log4j = Logger.getLogger(CompareVersion.class);

  @Override
  public void execute() throws BuildException {
    final String msg = "Minimum version " + v2 + " current version " + v1;
    if (Version.compareVersion(v1, v2) < 0)
      throw new BuildException(msg);
    log4j.info(msg);
  }

  public void setV1(String v1) {
    this.v1 = v1;
  }

  public void setV2(String v2) {
    this.v2 = v2;
  }

}
