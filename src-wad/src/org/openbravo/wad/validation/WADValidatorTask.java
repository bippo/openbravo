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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.wad.validation;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openbravo.database.CPStandAlone;

public class WADValidatorTask extends Task {
  private String propertiesFile;
  private String modules;
  private boolean stoponerror;
  private boolean friendlyWarnings;

  @Override
  public void execute() throws BuildException {
    CPStandAlone conn = new CPStandAlone(propertiesFile);
    WADValidator val = new WADValidator(conn, modules, friendlyWarnings);
    WADValidationResult result = val.validate();
    if (!friendlyWarnings) {
      result.printLog(stoponerror);
    }
    result.printFriendlyLog();
    if (result.hasErrors() && stoponerror) {
      throw new BuildException("WAD verification has errors");
    }
  }

  public String getPropertiesFile() {
    return propertiesFile;
  }

  public void setPropertiesFile(String propertiesFile) {
    this.propertiesFile = propertiesFile;
  }

  public String getModules() {
    return modules;
  }

  public void setModules(String modules) {
    this.modules = modules;
  }

  public boolean isStoponerror() {
    return stoponerror;
  }

  public void setStoponerror(boolean failonerror) {
    this.stoponerror = failonerror;
  }

  public boolean isFriendlyWarnings() {
    return friendlyWarnings;
  }

  public void setFriendlyWarnings(boolean friendlyWarnings) {
    this.friendlyWarnings = friendlyWarnings;
  }

}
