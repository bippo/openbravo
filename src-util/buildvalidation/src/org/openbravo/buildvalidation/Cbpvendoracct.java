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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.database.ConnectionProvider;

/**
 * This validation is related to this issue: https://issues.openbravo.com/view.php?id=12824 The
 * unique constraint C_BP_VENDOR_ACCT_ACCTSCHEMA_UN was made more restrictive, and it will fail if
 * the data is not correct and is not fixed before updating.
 */
public class Cbpvendoracct extends BuildValidation {

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      int a = Integer.parseInt(CbpvendoracctData.countNoDistinct(cp));
      int b = Integer.parseInt(CbpvendoracctData.countDistinct(cp));
      if (a != b) {
        errors
            .add("You can not apply this MP because your instance fails in a pre-validation: from Openbravo 2.50 MP18 it is not allowed to have more than one entry in business partner ->  vendor ->  accounting tab with the same accounting schema and status. Untill MP18 it was allowed although it was wrong since the behaviour was unpredictable: any of the duplicated accounts could be used for the accounting of that business partner. To fix this problem in your instance, you can know the duplicated entries by reviewing Alerts in your system (Alert Rule: Vendor Duplicate Accounts). Once you find the duplicated entries you should remove the wrong ones. Once it is fixed you should be able to apply this MP.");
        String alertRuleId = CbpvendoracctData.getUUID(cp);
        if (CbpvendoracctData.existsAlertRule(cp).equals("0")) {
          CbpvendoracctData.insertAlertRule(cp, alertRuleId);
          processAlert(alertRuleId, cp);
        }
      }
    } catch (Exception e) {
      return handleError(e);
    }
    return errors;
  }

  /**
   * @param alertRule
   * @param conn
   * @throws Exception
   */
  private void processAlert(String adAlertruleId, ConnectionProvider cp) throws Exception {
    CbpvendoracctData[] alertRule = CbpvendoracctData.select(cp, adAlertruleId);
    CbpvendoracctData[] alert = null;
    if (!alertRule[0].sql.equals("")) {
      try {
        alert = CbpvendoracctData.selectAlert(cp, alertRule[0].sql);
      } catch (Exception ex) {
        return;
      }
    }
    // Insert
    if (alert != null && alert.length != 0) {
      StringBuilder msg = new StringBuilder();
      ;

      for (int i = 0; i < alert.length; i++) {
        if (CbpvendoracctData.existsReference(cp, adAlertruleId, alert[i].referencekeyId).equals(
            "0")) {
          CbpvendoracctData.insertAlert(cp, alert[i].description, alertRule[0].adAlertruleId,
              alert[i].recordId, alert[i].referencekeyId);
        }
      }
    }
  }

}
