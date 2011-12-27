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

package org.openbravo.utils;

import org.apache.tools.ant.Task;

public class CheckSumCondition extends Task {

  protected String obDir;
  protected String type;
  protected String property;

  public void execute() {
    CheckSum cs = new CheckSum(obDir);
    // project.setProperty(property,
    // !cs.getCheckSum(type).equals(cs.calculateCheckSum(type).toString());//

    getProject().setProperty(property,
        (!cs.getCheckSum(type).equals(cs.calculateCheckSum(type))) ? "true" : "false");
    // return !cs.getCheckSum(type).equals(cs.calculateCheckSum(type));
  }

  public String getObDir() {
    return obDir;
  }

  public void setObDir(String obDir) {
    this.obDir = obDir;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

}
