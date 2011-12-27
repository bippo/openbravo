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

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class LinkCreditPayments extends ModuleScript {
  private static final Logger log4j = Logger.getLogger(LinkCreditPayments.class);

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      final LinkCreditPaymentsData[] bPartners = LinkCreditPaymentsData.selectBusinessPartners(cp);
      for (int i = 0; i < bPartners.length; i++) {
        linkCreditPayments(cp, bPartners[i].cBpartnerId, bPartners[i].name, "Y");
        linkCreditPayments(cp, bPartners[i].cBpartnerId, bPartners[i].name, "N");
      }
    } catch (Exception e) {
      handleError(e);
    }

  }

  private void linkCreditPayments(ConnectionProvider cp, String cbPartnerId, String cbPartnerName,
      String isReceipt) throws ServletException {
    LinkCreditPaymentsData[] paymentsUsingCredit = LinkCreditPaymentsData
        .selectPaymentsUsingCredit(cp, isReceipt, cbPartnerId);
    LinkCreditPaymentsData[] creditPayments = LinkCreditPaymentsData
        .selectPaymentsGeneratingCredit(cp, isReceipt, cbPartnerId);
    if (paymentsUsingCredit.length > 0 && creditPayments.length > 0) {
      log4j.info("Linking Credit Payments for business Partner: " + cbPartnerName);
      linkCreditPayment(cp, paymentsUsingCredit, creditPayments);
    }
  }

  private void linkCreditPayment(ConnectionProvider cp,
      LinkCreditPaymentsData[] paymentsUsingCredit, LinkCreditPaymentsData[] creditPayments)
      throws ServletException {
    int j = 0;
    BigDecimal availableCreditPayment = new BigDecimal(creditPayments[j].usedCredit);
    for (int i = 0; i < paymentsUsingCredit.length; i++) {
      BigDecimal consumedCredit = new BigDecimal(paymentsUsingCredit[i].usedCredit);
      while (j < creditPayments.length && consumedCredit.compareTo(BigDecimal.ZERO) > 0) {
        log4j.info("Payment Using Credit: " + paymentsUsingCredit[i].documentno
            + ". Pending amount to distribute: " + consumedCredit);
        if (availableCreditPayment.compareTo(BigDecimal.ZERO) == 0) {
          availableCreditPayment = new BigDecimal(creditPayments[j].usedCredit);
        }
        if (availableCreditPayment.compareTo(consumedCredit) >= 0) {
          log4j.info("linking with credit payment " + creditPayments[j].documentno + ", amount: "
              + consumedCredit);
          LinkCreditPaymentsData.insertUsedCreditSource(cp, paymentsUsingCredit[i].adClientId,
              paymentsUsingCredit[i].adOrgId, paymentsUsingCredit[i].finPaymentId,
              creditPayments[j].finPaymentId, consumedCredit.toString(),
              creditPayments[j].cCurrencyId);
          availableCreditPayment = availableCreditPayment.subtract(consumedCredit);
          consumedCredit = BigDecimal.ZERO;
          log4j.info("(available credit in the payment: " + availableCreditPayment + ") ");
        } else {
          log4j.info("linking with credit payment " + creditPayments[j].documentno + ", amount: "
              + availableCreditPayment);
          LinkCreditPaymentsData.insertUsedCreditSource(cp, paymentsUsingCredit[i].adClientId,
              paymentsUsingCredit[i].adOrgId, paymentsUsingCredit[i].finPaymentId,
              creditPayments[j].finPaymentId, availableCreditPayment.toString(),
              creditPayments[j].cCurrencyId);
          consumedCredit = consumedCredit.subtract(availableCreditPayment);
          availableCreditPayment = BigDecimal.ZERO;
          log4j.info("(credit payment has been fully used)");
        }
        if (availableCreditPayment.compareTo(BigDecimal.ZERO) == 0) {
          j++;
        }
      }
    }
  }
}
