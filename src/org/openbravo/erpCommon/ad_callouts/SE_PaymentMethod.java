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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;

public class SE_PaymentMethod extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strfinPaymentmethodId = vars.getStringParameter("inpfinPaymentmethodId");
    FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        strfinPaymentmethodId);
    try {
      // IN
      info.addResult("inppayinAllow", paymentMethod.isPayinAllow() ? "Y" : "N");
      info.addResult("inpautomaticReceipt", paymentMethod.isAutomaticReceipt() ? "Y" : "N");
      info.addResult("inpautomaticDeposit", paymentMethod.isAutomaticDeposit() ? "Y" : "N");
      info.addResult("inppayinExecutionType", paymentMethod.getPayinExecutionType());
      info.addResult("inppayinExecutionProcessId",
          paymentMethod.getPayinExecutionProcess() == null ? "" : paymentMethod
              .getPayinExecutionProcess().getId());
      info.addResult("inppayinDeferred", paymentMethod.isPayinDeferred() ? "Y" : "N");
      info.addResult("inpuponreceiptuse", paymentMethod.getUponReceiptUse() == null ? ""
          : paymentMethod.getUponReceiptUse());
      info.addResult("inpupondeposituse", paymentMethod.getUponDepositUse() == null ? ""
          : paymentMethod.getUponDepositUse());
      info.addResult("inpinuponclearinguse", paymentMethod.getINUponClearingUse() == null ? ""
          : paymentMethod.getINUponClearingUse());
      info.addResult("inppayinIsmulticurrency", paymentMethod.isPayinIsMulticurrency() ? "Y" : "N");
      // OUT
      info.addResult("inppayoutAllow", paymentMethod.isPayoutAllow() ? "Y" : "N");
      info.addResult("inpautomaticPayment", paymentMethod.isAutomaticPayment() ? "Y" : "N");
      info.addResult("inpautomaticWithdrawn", paymentMethod.isAutomaticWithdrawn() ? "Y" : "N");
      info.addResult("inppayoutExecutionType", paymentMethod.getPayoutExecutionType());
      info.addResult("inppayoutExecutionProcessId",
          paymentMethod.getPayoutExecutionProcess() == null ? "" : paymentMethod
              .getPayoutExecutionProcess().getId());
      info.addResult("inppayoutDeferred", paymentMethod.isPayoutDeferred() ? "Y" : "N");
      info.addResult("inpuponpaymentuse", paymentMethod.getUponPaymentUse() == null ? ""
          : paymentMethod.getUponPaymentUse());
      info.addResult("inpuponwithdrawaluse", paymentMethod.getUponWithdrawalUse() == null ? ""
          : paymentMethod.getUponWithdrawalUse());
      info.addResult("inpoutuponclearinguse", paymentMethod.getOUTUponClearingUse() == null ? ""
          : paymentMethod.getOUTUponClearingUse());
      info.addResult("inppayoutIsmulticurrency", paymentMethod.isPayoutIsMulticurrency() ? "Y"
          : "N");
    } catch (Exception e) {
      log4j.info("No default info for the selected payment method");
    }
  }
}
