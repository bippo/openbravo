/*************************************************************************
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
 ************************************************************************/
package org.openbravo.erpCommon.ad_process;

public class ApplyModulesResponse {
  private int state;
  private String statusofstate;
  private String[] warnings;
  private String[] errors;
  private String lastmessage;
  private String processFinished;

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public String getStatusofstate() {
    return statusofstate;
  }

  public void setStatusofstate(String statusofstate) {
    this.statusofstate = statusofstate;
  }

  public String[] getWarnings() {
    return warnings;
  }

  public void setWarnings(String[] warnings) {
    this.warnings = warnings;
  }

  public String[] getErrors() {
    return errors;
  }

  public void setErrors(String[] errors) {
    this.errors = errors;
  }

  public String getLastmessage() {
    return lastmessage;
  }

  public void setLastmessage(String lastmessage) {
    this.lastmessage = lastmessage;
  }

  public String getProcessFinished() {
    return processFinished;
  }

  public void setProcessFinished(String processFinished) {
    this.processFinished = processFinished;
  }
}
