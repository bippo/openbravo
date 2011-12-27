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

public class UniquePaymentForTransaction extends BuildValidation {

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      // Prevent error when upgrading from a pure 2.50
      if (UniquePaymentForTransactionData.existAPRMbasetables(cp)) {

        UniquePaymentForTransactionData[] listofPayments = UniquePaymentForTransactionData
            .selectDuplicatePaymentsForTransaction(cp);
        if (listofPayments != null && listofPayments.length > 0) {
          String message = "You cannot apply this Advanced Payables and Receivables Management module version because your instance fails in a pre-validation. "
              + "It is not allowed to upgrade to this version having the same payment linked to several transactions. "
              + "To fix this problem in your instance, have a look to generated alerts (Payment In/Out linked with more than one transaction) and identify the affected transactions. "
              + "If you have for example two transactions for the same payment, delete both transactions and create a new transaction associated to the payment. "
              + "Once it is fixed you should be able to apply this module version";
          errors.add(message);
        }

        for (UniquePaymentForTransactionData payment : listofPayments) {
          processAlert(cp, payment);
        }

      }
    } catch (Exception e) {
      return handleError(e);
    }
    return errors;
  }

  private void processAlert(ConnectionProvider cp, UniquePaymentForTransactionData payment)
      throws Exception {
    final String PAYMENT_IN_TAB = "C4B6506838E14A349D6717D6856F1B56";
    final String PAYMENT_OUT_TAB = "F7A52FDAAA0346EFA07D53C125B40404";
    final String PAYMENT_IN_WINDOW = "E547CE89D4C04429B6340FFA44E70716";
    final String PAYMENT_OUT_WINDOW = "6F8F913FA60F4CBD93DC1D3AA696E76E";

    String ALERT_RULE_NAME = "Payment " + ("Y".equals(payment.isreceipt) ? "In" : "Out")
        + " linked with more than one transaction";
    String alertDescription = "Payment " + ("Y".equals(payment.isreceipt) ? "In: " : "Out: ")
        + payment.documentno + " is linked with more than one transaction. "
        + "Navigate to the document and using linked items browse to linked transactions. "
        + "Then delete associated transactions and create a new transaction for the payment.";

    String strTabId = "Y".equals(payment.isreceipt) ? PAYMENT_IN_TAB : PAYMENT_OUT_TAB;
    String strWindowId = "Y".equals(payment.isreceipt) ? PAYMENT_IN_WINDOW : PAYMENT_OUT_WINDOW;
    String alertRuleId = "";

    String ALERT_RULE_SQL = "SELECT distinct t.fin_payment_id as referencekey_id, "
        + " ad_column_identifier('fin_payment', t.fin_payment_id, 'en_US') as record_id, 0 as ad_role_id, null as ad_user_id,"
        + " '"
        + alertDescription
        + "' as description,"
        + " 'Y' as isActive, p.ad_org_id, p.ad_client_id,"
        + " now() as created, 0 as createdBy, now() as updated, 0 as updatedBy"
        + " FROM fin_finacc_transaction t join fin_payment p on (t.fin_payment_id=p.fin_payment_id)"
        + " WHERE isreceipt='" + payment.isreceipt + "'"
        + " GROUP BY t.fin_payment_id, p.documentno, p.isreceipt, p.ad_client_id, p.ad_org_id"
        + " HAVING count(t.fin_finacc_transaction_id) > 1" + " ORDER BY 1";

    // Check if exists the alert rule
    if (!UniquePaymentForTransactionData.existsAlertRule(cp, ALERT_RULE_NAME, payment.adClientId)) {
      UniquePaymentForTransactionData.insertAlertRule(cp, payment.adClientId, payment.adOrgId,
          ALERT_RULE_NAME, strTabId, ALERT_RULE_SQL);

      alertRuleId = UniquePaymentForTransactionData.getAlertRuleId(cp, ALERT_RULE_NAME,
          payment.adClientId);
      UniquePaymentForTransactionData[] roles = UniquePaymentForTransactionData.getRoleId(cp,
          strWindowId, payment.adClientId);
      for (UniquePaymentForTransactionData role : roles) {
        UniquePaymentForTransactionData.insertAlertRecipient(cp, payment.adClientId,
            payment.adOrgId, alertRuleId, role.adRoleId);
      }
    } else {
      alertRuleId = UniquePaymentForTransactionData.getAlertRuleId(cp, ALERT_RULE_NAME,
          payment.adClientId);
    }

    // Check if exist the concrete alert for the payment
    if (!UniquePaymentForTransactionData.existsAlert(cp, alertRuleId, payment.finPaymentId)) {
      UniquePaymentForTransactionData.insertAlert(cp, payment.adClientId, alertDescription,
          alertRuleId, payment.documentno, payment.finPaymentId);
    }

  }

}
