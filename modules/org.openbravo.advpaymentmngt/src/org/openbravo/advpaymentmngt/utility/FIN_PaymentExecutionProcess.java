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
package org.openbravo.advpaymentmngt.utility;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.financialmgmt.payment.PaymentRun;

public interface FIN_PaymentExecutionProcess {
  /*
   * All executed payments must update the related PaymentRunPayment record status to "S" when the
   * payment has been executed successfully. When the status is set to "S" the status of the payment
   * will automatically be set to "Payment Received" or "Payment Made".
   */
  public OBError execute(PaymentRun paymentRun) throws ServletException;
}
