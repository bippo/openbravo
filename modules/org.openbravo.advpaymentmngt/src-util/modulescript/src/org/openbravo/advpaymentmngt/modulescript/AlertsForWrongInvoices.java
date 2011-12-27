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
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.modulescript;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class AlertsForWrongInvoices extends ModuleScript {
  
  final static private String ALERT_RULE_SQL = "select distinct ad_column_identifier('c_invoice', fin_payment_schedule.c_invoice_id, 'en_US') as record_id, fin_payment_schedule.c_invoice_id  as referencekey_id, 0 as ad_role_id, null as ad_user_id, 'This invoice needs to be reactivated and processed again due to wrong payment info.' as description, 'Y' as isActive, fin_payment_schedule.ad_org_id, fin_payment_schedule.ad_client_id, now() as created, 0 as createdBy, now() as updated, 0 as updatedBy from fin_payment_schedule, fin_payment_scheduledetail where fin_payment_schedule.fin_payment_schedule_id = fin_payment_scheduledetail.fin_payment_schedule_invoice and fin_payment_scheduledetail.iscanceled = 'N' group by fin_payment_schedule.ad_org_id, fin_payment_schedule.ad_client_id, fin_payment_schedule.fin_payment_schedule_id, fin_payment_schedule.c_invoice_id, fin_payment_schedule.amount having fin_payment_schedule.amount <> sum(fin_payment_scheduledetail.amount + coalesce(fin_payment_scheduledetail.writeoffamt,0)) order by 1";

  @Override
  // Inserting Alerts for invoices which needs to be recalculated/processed
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      AlertsForWrongInvoicesData[] data = AlertsForWrongInvoicesData.select(cp);
      for (AlertsForWrongInvoicesData wrongInvoice : data) {
        createAlert(cp, wrongInvoice);
      }
      
      // Fix sql for all the alert rules created not taking in account voided payments
      AlertsForWrongInvoicesData[] oldAlertRules = AlertsForWrongInvoicesData.selectOldAlertRules(cp);
      for (AlertsForWrongInvoicesData alertRule : oldAlertRules) {
        // Check if exist any alert created using the wrong sql query. In that case fix the alert
        AlertsForWrongInvoicesData[] oldAlerts = AlertsForWrongInvoicesData.selectVoidAlerts(cp, alertRule.adAlertruleId);
        for (AlertsForWrongInvoicesData alert : oldAlerts) {
          AlertsForWrongInvoicesData.updateAlertStatus(cp, "SOLVED", "Y", alert.adAlertId);
        }
        AlertsForWrongInvoicesData.updateAlertRuleSql(cp, ALERT_RULE_SQL, alertRule.adAlertruleId);
      }
      
    } catch (Exception e) {
      handleError(e);
    }
  }
  
  private void createAlert(ConnectionProvider cp, AlertsForWrongInvoicesData wrongInvoice)
      throws ServletException {
    final String SALES_INVOICE_WINDOW = "167";
    final String PURCHASE_INVOICE_WINDOW = "183";
    final String SALES_INVOICE_TAB = "263";
    final String PURCHASE_INVOICE_TAB = "290";
    String ALERT_RULE = "Wrong purchase invoice. Wrong amount in payment plan detail";
    String WindowInvoiceId = PURCHASE_INVOICE_TAB;
    String strTabId = PURCHASE_INVOICE_TAB;
    if ("Y".equals(wrongInvoice.issotrx)) {
      strTabId = SALES_INVOICE_TAB;
      WindowInvoiceId = SALES_INVOICE_WINDOW;
      ALERT_RULE = "Wrong sales invoice. Wrong amount in payment plan detail";
    }
    String strName = "Invoice: '" + wrongInvoice.invoice
        + "' needs to be reactivated and processed again due to wrong payment info.";
    String oldAlertRuleId = AlertsForWrongInvoicesData.getAlertRuleId(cp, ALERT_RULE,
        wrongInvoice.adClientId);
    if (!AlertsForWrongInvoicesData.existsAlert(cp, oldAlertRuleId, wrongInvoice.cInvoiceId)) {
      if (!AlertsForWrongInvoicesData.existsAlertRule(cp, ALERT_RULE, wrongInvoice.adClientId)) {
        AlertsForWrongInvoicesData.insertAlertRule(cp, wrongInvoice.adClientId, ALERT_RULE,
            strTabId, ALERT_RULE_SQL);
        AlertsForWrongInvoicesData[] roles = AlertsForWrongInvoicesData.getRoleId(cp,
            WindowInvoiceId, wrongInvoice.adClientId);
        for (AlertsForWrongInvoicesData role : roles) {
          AlertsForWrongInvoicesData.insertAlertRecipient(cp, wrongInvoice.adClientId,
              wrongInvoice.adOrgId,
              AlertsForWrongInvoicesData.getAlertRuleId(cp, ALERT_RULE, wrongInvoice.adClientId),
              role.adRoleId);
        }
      }
      String alertRuleId = AlertsForWrongInvoicesData.getAlertRuleId(cp, ALERT_RULE,
          wrongInvoice.adClientId);
      AlertsForWrongInvoicesData.insertAlert(cp, wrongInvoice.adClientId, wrongInvoice.adOrgId,
          strName, alertRuleId, wrongInvoice.invoice, wrongInvoice.cInvoiceId);
    }
  }
}
