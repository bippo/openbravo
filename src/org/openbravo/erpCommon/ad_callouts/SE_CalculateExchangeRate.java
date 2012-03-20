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
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.advpaymentmngt.APRM_FinaccTransactionV;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public class SE_CalculateExchangeRate extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final String ADWINDOW_PurchaseInvoice = "183";
  private static final String ADWINDOW_SalesInvoice = "167";
  private static final String ADWINDOW_PaymentOut = "6F8F913FA60F4CBD93DC1D3AA696E76E";
  private static final String ADWINDOW_PaymentIn = "E547CE89D4C04429B6340FFA44E70716";
  private static final String ADWINDOW_Transaction = "94EAA455D2644E04AB25D93BE5157B6D";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String strWindowId = info.getWindowId();

    VariablesSecureApp vars = info.vars;
    String strLastFieldChanged = info.getLastFieldChanged();
    BigDecimal originalAmt = BigDecimal.ZERO;
    BigDecimal rate = new BigDecimal(vars.getNumericParameter("inprate", "0"));
    BigDecimal foreignAmt = new BigDecimal(vars.getNumericParameter("inpforeignAmount", "0"));
    try {
      if (strWindowId.equals(ADWINDOW_PurchaseInvoice) || strWindowId.equals(ADWINDOW_SalesInvoice)) {
        String strcInvoiceId = info.vars.getStringParameter("inpcInvoiceId");
        Invoice invoice = OBDal.getInstance().get(Invoice.class, strcInvoiceId);
        originalAmt = invoice.getGrandTotalAmount();

      } else if (strWindowId.equals(ADWINDOW_PaymentOut) || strWindowId.equals(ADWINDOW_PaymentIn)) {
        String strfinPaymentId = info.vars.getStringParameter("inpfinPaymentId");
        FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, strfinPaymentId);
        originalAmt = payment.getAmount();
      } else if (strWindowId.equals(ADWINDOW_Transaction)) {
        String strfinTransactionId = info.vars.getStringParameter("inpaprmFinaccTransactionVId");
        APRM_FinaccTransactionV transaction = OBDal.getInstance().get(
            APRM_FinaccTransactionV.class, strfinTransactionId);
        originalAmt = transaction.getDepositAmount().subtract(transaction.getWithdrawalAmount());
      }
      if (strLastFieldChanged.equals("inprate")) {
        foreignAmt = originalAmt.multiply(rate);
        info.addResult("inpforeignAmount", foreignAmt);
      } else if (strLastFieldChanged.equals("inpforeignAmount")) {
        rate = foreignAmt.divide(originalAmt, 12, 4);
        info.addResult("inprate", rate);
      }

    } catch (Exception e) {
      log4j.info("No default info for the selected payment method");
    }

  }
}
