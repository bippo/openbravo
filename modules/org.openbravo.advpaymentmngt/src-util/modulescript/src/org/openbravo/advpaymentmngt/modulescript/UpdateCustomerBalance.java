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

public class UpdateCustomerBalance extends ModuleScript {

  /**
   * This modulescript regenerates the customer balance for all the business partners. It takes in
   * account the outstanding sales/purchase invoices and the credit generated in payment in/out.
   */
  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      // Check if the modulescript has been executed before.
      // If the preference does not exist in the database yet the modulescript must be executed.
      boolean isCustomerBalanceFixed = UpdateCustomerBalanceData.isCustomerBalanceFixed(cp);
      if (!isCustomerBalanceFixed) {
        // Reset the customer balance to 0
        UpdateCustomerBalanceData.resetCustomerCredit(cp);
        // Obtain the correct customer balance
        UpdateCustomerBalanceData[] data = UpdateCustomerBalanceData.calculateCustomerCredit(cp);
        for (UpdateCustomerBalanceData ucb : data) {
          UpdateCustomerBalanceData.updateCustomerCredit(cp, ucb.customercredit, ucb.cBpartnerId);
        }
      }
    } catch (Exception e) {
      handleError(e);
    }
  }
}
