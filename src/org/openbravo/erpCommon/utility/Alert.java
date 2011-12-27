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
package org.openbravo.erpCommon.utility;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class Alert {

  private int alertRuleId;
  private String recordId;
  private String description;
  private String referencekeyId = "0";

  private static Logger log4j = Logger.getLogger(Alert.class);
  private static final char DATA_DRIVEN = 'D';
  private static final char EXTERNAL = 'E';

  public Alert() {
    this(0);
  }

  public Alert(int ruleId) {
    this(ruleId, null);
  }

  public Alert(int ruleId, String recordId) {
    this.alertRuleId = ruleId;
    this.recordId = recordId;
  }

  public int getAlertRuleId() {
    return alertRuleId;
  }

  public void setAlertRuleId(int value) {
    alertRuleId = value;
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public boolean save(ConnectionProvider conn) {
    if (alertRuleId == 0 || description == null || description.equals(""))
      return false;

    try {
      AlertData[] data = null;
      if (recordId != null) {
        data = AlertData.select(conn, String.valueOf(alertRuleId), recordId);
      } else {
        data = AlertData.selectByDescription(conn, String.valueOf(alertRuleId), description);
      }
      if (data.length <= 0) {
        AlertData.insert(conn, description, String.valueOf(alertRuleId), recordId, referencekeyId);
      }
    } catch (Exception e) {
      log4j.error("Error saving an alert instance: " + e.getMessage());
      return false;
    }
    return true;
  }

}