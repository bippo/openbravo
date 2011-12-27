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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class UpdateGeneratedUsedCreditColumns extends ModuleScript {

  @Override
  // Initializes values of Used Credit and Generated credit columns of FIN_Payment
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      UpdateGeneratedUsedCreditColumnsData.initializeUsed(cp.getConnection(), cp);
      UpdateGeneratedUsedCreditColumnsData.initializeGenerated(cp.getConnection(), cp);
      if (UpdateGeneratedUsedCreditColumnsData.hasRefundToUpdate(cp.getConnection(), cp)) {
        UpdateGeneratedUsedCreditColumnsData.updateRefund(cp.getConnection(), cp);
        UpdateGeneratedUsedCreditColumnsData.updateRefundPrepayment(cp.getConnection(), cp);
        UpdateGeneratedUsedCreditColumnsData.updateRefundDetail(cp.getConnection(), cp);
      }
      if (UpdateGeneratedUsedCreditColumnsData.hasGeneratedCreditToUpdate(cp.getConnection(), cp))
        UpdateGeneratedUsedCreditColumnsData.updateGeneratedCredit(cp.getConnection(), cp);
    } catch (Exception e) {
      handleError(e);
    }
  }
}
