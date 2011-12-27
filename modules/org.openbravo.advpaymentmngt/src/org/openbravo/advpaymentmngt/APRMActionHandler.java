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
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

public class APRMActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(APRMActionHandler.class);
  private static final String BANK_TRANSITORY_CALLOUT_RESPONSE = "bankTransitoryCalloutResponse";

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject object = new JSONObject();
    JSONObject o;
    try {
      o = new JSONObject(content);
      final String strEventType = o.getString("eventType");
      if (BANK_TRANSITORY_CALLOUT_RESPONSE.equals(strEventType)) {
        final String strFinancialAccount = o.getString("financialAccountId");
        updatePaymentMethodConfiguration(strFinancialAccount);
      } else {
        log.error("Unsupported event type: " + strEventType);
      }
    } catch (JSONException e) {
      log.error("Error executing action: " + e.getMessage(), e);
    }

    return object;
  }

  void updatePaymentMethodConfiguration(String strfinFinancialAccountId) {
    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strfinFinancialAccountId);

    // Configure clearing account for all payment methods upon clearing event
    for (FinAccPaymentMethod paymentMethod : account.getFinancialMgmtFinAccPaymentMethodList()) {
      paymentMethod.setOUTUponClearingUse("CLE");
      paymentMethod.setINUponClearingUse("CLE");
      OBDal.getInstance().save(paymentMethod);
    }
    OBDal.getInstance().flush();
  }
}
