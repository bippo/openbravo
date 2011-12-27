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
package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

public class DeleteTransaction implements Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    VariablesSecureApp vars = bundle.getContext().toVars();
    Map<String, Object> params = bundle.getParams();

    String strKey = (String) params.get("Fin_Finacc_Transaction_ID");
    if (strKey == null) {
      strKey = (String) params.get("Aprm_Finacc_Transaction_V_ID");
    }
    FIN_FinaccTransaction transaction = OBDal.getInstance()
        .get(FIN_FinaccTransaction.class, strKey);
    try {
      OBError msg = processTransaction(vars, bundle.getConnection(), "R", transaction);
      if ("Success".equals(msg.getType())) {
        OBContext.setAdminMode();
        try {
          OBDal.getInstance().remove(transaction);
          OBDal.getInstance().flush();
        } finally {
          OBContext.restorePreviousMode();
        }
      }
      bundle.setResult(msg);
    } catch (Exception e) {
      throw new OBException("Process failed deleting the financial account Transaction", e);
    }
  }

  /**
   * It calls the Transaction Process for the given transaction and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strAction
   *          String with the action of the process. {P, D, R}
   * @param transaction
   *          FIN_FinaccTransaction that needs to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  private OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      String strAction, FIN_FinaccTransaction transaction) throws Exception {
    ProcessBundle pb = new ProcessBundle("F68F2890E96D4D85A1DEF0274D105BCE", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strAction);
    parameters.put("Fin_FinAcc_Transaction_ID", transaction.getId());
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_TransactionProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

}
