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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Calculates the version number for core.
 * 
 * It is used when extracting obx for core
 * 
 * 
 */
@Deprecated
public class CalculateCoreRevision extends Task {
  private String revision;
  private String hgRevNo;

  @Override
  public void execute() throws BuildException {
    // remove + in case there are uncommited changes
    hgRevNo = hgRevNo.replace("+", "");

    getProject().setProperty(revision, "3.0." + hgRevNo);
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public String getHgRevNo() {
    return hgRevNo;
  }

  public void setHgRevNo(String hgRevNo) {
    this.hgRevNo = hgRevNo;
  }

}
