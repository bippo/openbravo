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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  Enterprise Intelligence Systems (http://www.eintel.com.au).
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

/**
 * Update currency, exchange rate and financial txn amount
 * 
 * @author eintelau (ben.sommerville@eintel.com.au)
 */
public class SE_Payment_FinAccount extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String financialAccountId = vars.getStringParameter("inpfinFinancialAccountId");
    FIN_FinancialAccount financialAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
        financialAccountId);
    if (financialAccount != null) {
      info.addResult("inpcCurrencyId", DalUtil.getId(financialAccount.getCurrency()).toString());
      info.addResult("inpfinancialaccountcurrencyid", DalUtil.getId(financialAccount.getCurrency())
          .toString());

      info.addResult("inpfinaccTxnConvertRate", BigDecimal.ONE);

      String strAmount = vars.getNumericParameter("inpamount");
      if (!strAmount.isEmpty()) {
        info.addResult("inpfinaccTxnAmount", new BigDecimal(strAmount));
      }
    } else {
      info.addResult("inpfinaccTxnConvertRate", "");
      info.addResult("inpfinaccTxnAmount", "");
    }

    String finIsReceipt = info.getStringParameter("inpisreceipt", null);
    boolean isPaymentOut = "N".equals(finIsReceipt);

    String srtPaymentMethodId = info.getStringParameter("inpfinPaymentmethodId",
        IsIDFilter.instance);
    FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        srtPaymentMethodId);

    boolean isMultiCurrencyEnabled = false;
    if (paymentMethod != null && financialAccount != null) {
      for (FinAccPaymentMethod accPm : financialAccount.getFinancialMgmtFinAccPaymentMethodList()) {
        if (paymentMethod.getId().equals(accPm.getPaymentMethod().getId())) {
          if (isPaymentOut) {
            isMultiCurrencyEnabled = accPm.isPayoutAllow() && accPm.isPayoutIsMulticurrency();
          } else {
            isMultiCurrencyEnabled = accPm.isPayinAllow() && accPm.isPayinIsMulticurrency();
          }
          break;
        }
      }
    }
    info.addResult("inpismulticurrencyenabled", isMultiCurrencyEnabled ? "Y" : "N");
  }
}
