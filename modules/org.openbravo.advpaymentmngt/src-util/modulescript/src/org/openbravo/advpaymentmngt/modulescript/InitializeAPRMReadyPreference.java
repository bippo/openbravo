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

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class InitializeAPRMReadyPreference extends ModuleScript {

  /**
   * This modulescript regenerates the customer balance for all the business partners. It takes in
   * account the outstanding sales/purchase invoices and the credit generated in payment in/out.
   */
  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      // If the preference does not exist in the database yet the modulescript must be executed.
      boolean isAPRMReady = InitializeAPRMReadyPreferenceData.isAPRMReady(cp);
      if (!isAPRMReady) {
        // Check if APRM is already in use based on the existence of records in FIN_Payment_Schedule
        // table
        isAPRMReady = InitializeAPRMReadyPreferenceData.isAPRMInUse(cp);
        if (isAPRMReady) {
          InitializeAPRMReadyPreferenceData.createPreference(cp);
          return;
        }
        // Check if old flow is not used based on the existence of records in C_DebtPayment table
        isAPRMReady = InitializeAPRMReadyPreferenceData.isOldFlowNotUsed(cp);
        if (isAPRMReady) {
          InitializeAPRMReadyPreferenceData.createPreference(cp);
          return;
        }
      }
    } catch (Exception e) {
      handleError(e);
    }
  }
}
