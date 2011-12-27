/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.database.ConnectionProvider;

public class WrongPaymentScheduleDetailsCheck extends BuildValidation {

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      // Prevent error when upgrading from a pure 2.50 (MP0)
      if (WrongPaymentScheduleDetailsCheckData.existAPRMbasetables(cp)) {
        if (WrongPaymentScheduleDetailsCheckData.existWrongPaymentSchedules(cp)) {
          String alertRuleId = UnpostRefundPaymentsData.getUUID(cp);
          if (!WrongPaymentScheduleDetailsCheckData.existsAlertRule(cp)) {
            WrongPaymentScheduleDetailsCheckData.insertAlertRule(cp, alertRuleId);
          }
          alertRuleId = WrongPaymentScheduleDetailsCheckData.getAlertRuleId(cp);
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
    WrongPaymentScheduleDetailsCheckData[] alertRule = WrongPaymentScheduleDetailsCheckData
        .selectAlertRule(cp, adAlertruleId);
    WrongPaymentScheduleDetailsCheckData[] alert = null;
    if (!alertRule[0].sql.equals("")) {
      try {
        alert = WrongPaymentScheduleDetailsCheckData.selectAlert(cp, alertRule[0].sql);
      } catch (Exception ex) {
        return;
      }
    }
    // Insert
    if (alert != null && alert.length != 0) {
      StringBuilder msg = new StringBuilder();

      for (int i = 0; i < alert.length; i++) {
        boolean existsReference = false;
        if (WrongPaymentScheduleDetailsCheckData.existsStatusColumn(cp)) {
          existsReference = WrongPaymentScheduleDetailsCheckData.existsReference(cp, adAlertruleId,
              alert[i].referencekeyId);
        } else {
          existsReference = WrongPaymentScheduleDetailsCheckData.existsReferenceOld(cp,
              adAlertruleId, alert[i].referencekeyId);
        }
        if (!existsReference) {
          WrongPaymentScheduleDetailsCheckData.insertAlert(cp, alert[i].adClientId,
              alert[i].description, alertRule[0].adAlertruleId, alert[i].recordId,
              alert[i].referencekeyId);
        }
      }

    }
  }
}
