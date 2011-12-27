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

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;

public class SE_Payment_BPartner extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strcBpartnerId = vars.getStringParameter("inpcBpartnerId");
    String strisreceipt = vars.getStringParameter("inpisreceipt");
    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strcBpartnerId);
    boolean isReceipt = "Y".equals(strisreceipt);
    try {
      info.addResult("inpfinPaymentmethodId", isReceipt ? bpartner.getPaymentMethod().getId()
          : bpartner.getPOPaymentMethod().getId());
      info.addResult("inpfinFinancialAccountId", isReceipt ? bpartner.getAccount().getId()
          : bpartner.getPOFinancialAccount().getId());
    } catch (Exception e) {
      log4j.info("No default info for the selected business partner");
    }
  }
}
