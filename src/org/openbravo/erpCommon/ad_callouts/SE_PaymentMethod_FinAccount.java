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

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

public class SE_PaymentMethod_FinAccount extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    String srtPaymentMethodId = info.getStringParameter("inpfinPaymentmethodId",
        IsIDFilter.instance);
    String srtPOPaymentMethodId = info.getStringParameter("inppoPaymentmethodId",
        IsIDFilter.instance);

    String tabId = info.getTabId();
    boolean isVendorTab = "224".equals(tabId);
    String finIsReceipt = info.getStringParameter("inpisreceipt", null);
    boolean isPaymentOut = isVendorTab || "N".equals(finIsReceipt);
    String srtOrgId = info.getStringParameter("inpadOrgId", IsIDFilter.instance);

    FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        isVendorTab ? srtPOPaymentMethodId : srtPaymentMethodId);

    info.addSelect(isVendorTab ? "inppoFinancialAccountId" : "inpfinFinancialAccountId");
    String srtSelectedFinancialAccount = info.getStringParameter(
        isVendorTab ? "inppoFinancialAccountId" : "inpfinFinancialAccountId", IsIDFilter.instance);

    boolean isSelected = true;
    boolean isMultiCurrencyEnabled = false;

    // No Payment Method selected
    if (srtPaymentMethodId.isEmpty() && srtPOPaymentMethodId.isEmpty()) {
      OBCriteria<FIN_FinancialAccount> obc = OBDal.getInstance().createCriteria(
          FIN_FinancialAccount.class);
      obc.add(Restrictions.in("organization.id", OBContext.getOBContext()
          .getOrganizationStructureProvider().getNaturalTree(srtOrgId)));
      obc.setFilterOnReadableOrganization(false);
      for (FIN_FinancialAccount acc : obc.list()) {
        info.addSelectResult(acc.getId(), acc.getIdentifier());
      }

    } else {
      OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
      obc.add(Restrictions.in("organization.id", OBContext.getOBContext()
          .getOrganizationStructureProvider().getNaturalTree(srtOrgId)));
      if (isPaymentOut) {
        obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYOUTALLOW, true));
      } else {
        obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYINALLOW, true));
      }

      FinAccPaymentMethod selectedPaymentMethod = null;
      for (FinAccPaymentMethod accPm : obc.list()) {
        if (srtSelectedFinancialAccount.equals(accPm.getAccount().getId())) {
          isSelected = true;
        } else if (srtSelectedFinancialAccount.isEmpty()) {
          srtSelectedFinancialAccount = accPm.getAccount().getIdentifier();
          isSelected = true;
        }
        selectedPaymentMethod = accPm;
        info.addSelectResult(accPm.getAccount().getId(), accPm.getAccount().getIdentifier(),
            isSelected);
        isSelected = false;
      }
      if (selectedPaymentMethod != null) {
        if (isPaymentOut) {
          isMultiCurrencyEnabled = selectedPaymentMethod.isPayoutAllow()
              && selectedPaymentMethod.isPayoutIsMulticurrency();
        } else {
          isMultiCurrencyEnabled = selectedPaymentMethod.isPayinAllow()
              && selectedPaymentMethod.isPayinIsMulticurrency();
        }
      }
    }
    info.endSelect();
    info.addResult("inpismulticurrencyenabled", isMultiCurrencyEnabled ? "Y" : "N");
  }
}
