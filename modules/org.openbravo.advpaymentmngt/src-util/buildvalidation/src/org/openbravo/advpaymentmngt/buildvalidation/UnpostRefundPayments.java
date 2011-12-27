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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.database.ConnectionProvider;

public class UnpostRefundPayments extends BuildValidation {

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      // Prevent error when upgrading from a pure 2.50
      if (UnpostRefundPaymentsData.existAPRMbasetables(cp)) {

        if (UnpostRefundPaymentsData.existsPostedRefundPayments(cp)) {
          String errorClients = "";
          UnpostRefundPaymentsData[] clients = UnpostRefundPaymentsData.clientsWithPayments(cp);
          for (int i = 0; i < clients.length; i++)
            errorClients += clients[i].name + ",";
          errorClients = errorClients.substring(0, errorClients.length() - 1);

          errors
              .add("You can not apply this Advanced Payables and Receivables Management module version because your instance fails in a pre-validation: since APRM 1.0.5 version the accounting of refund payments has changed. So it is not allowed to upgrade to the latest version having payments accounted with the old rules. To fix this problem in your instance, you can know the duplicated entries by reviewing Alerts in your system (Alert Rule: Posted Refund Payments). Find the Posted Payments to unpost them, you should as well disable the accounting background process. Once it is fixed you should be able to apply this APRM module version. Clients with payments: "
                  + errorClients);
          String alertRuleId = UnpostRefundPaymentsData.getUUID(cp);
          if (!UnpostRefundPaymentsData.existsAlertRule(cp)) {
            UnpostRefundPaymentsData.insertAlertRule(cp, alertRuleId);
          }
          alertRuleId = UnpostRefundPaymentsData.getAlertRuleId(cp);
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
    UnpostRefundPaymentsData[] alertRule = UnpostRefundPaymentsData.select(cp, adAlertruleId);
    UnpostRefundPaymentsData[] alert = null;
    if (!alertRule[0].sql.equals("")) {
      try {
        alert = UnpostRefundPaymentsData.selectAlert(cp, alertRule[0].sql);
      } catch (Exception ex) {
        return;
      }
    }
    // Insert
    if (alert != null && alert.length != 0) {
      StringBuilder msg = new StringBuilder();

      for (int i = 0; i < alert.length; i++) {
        if (!UnpostRefundPaymentsData.existsReference(cp, adAlertruleId, alert[i].referencekeyId)) {
          UnpostRefundPaymentsData.insertAlert(cp, alert[i].adClientId, alert[i].description,
              alertRule[0].adAlertruleId, alert[i].recordId, alert[i].referencekeyId);
        }
      }

    }
  }

}
