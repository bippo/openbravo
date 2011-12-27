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
package org.openbravo.advpaymentmngt.process;

import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.scheduling.ProcessBundle;

public class FIN_ReconciliationProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", bundle.getContext()
        .getLanguage()));

    OBContext.setAdminMode();
    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      String recordID = (String) bundle.getParams().get("FIN_Reconciliation_ID");
      // This code is kept to maintain compatibility with previous tab which was built
      // on to of a view
      if (recordID == null || "".equals(recordID)) {
        recordID = (String) bundle.getParams().get("Aprm_Reconciliation_V_ID");
      }
      final FIN_Reconciliation reconciliation = dao.getObject(FIN_Reconciliation.class, recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();

      reconciliation.setProcessNow(true);
      OBDal.getInstance().save(reconciliation);
      OBDal.getInstance().flush();
      if (strAction.equals("P")) {
        // Check lines exist
        if (reconciliation.getFINReconciliationLineVList().size() == 0) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_ReconciliationNoLines@" + ": " + reconciliation.getDocumentNo()));
          bundle.setResult(msg);
          return;
        }
        reconciliation.setProcessed(true);
        reconciliation.setAPRMProcessReconciliation("R");
        reconciliation.setAprmProcessRec("R");
        reconciliation.setDocumentStatus("CO");
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();

        // ***********************
        // Reactivate Reconciliation
        // ***********************
      } else if (strAction.equals("R")) {
        // Already Posted Document
        if ("Y".equals(reconciliation.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@PostedDocument@"
              + ": " + reconciliation.getDocumentNo()));
          bundle.setResult(msg);
          return;
        }
        // Transaction exists
        if (!isLastReconciliation(reconciliation)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_FutureReconciliationExists@"));
          bundle.setResult(msg);
          return;
        }
        reconciliation.setProcessed(false);
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();
        reconciliation.setDocumentStatus("DR");
        reconciliation.setAPRMProcessReconciliation("P");
        reconciliation.setAprmProcessRec("P");
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();
      }
      reconciliation.setProcessNow(false);
      OBDal.getInstance().save(reconciliation);
      OBDal.getInstance().flush();
      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private boolean isLastReconciliation(FIN_Reconciliation reconciliation) {
    final OBCriteria<FIN_Reconciliation> obc = OBDal.getInstance().createCriteria(
        FIN_Reconciliation.class);
    obc.add(Restrictions.ge(FIN_Reconciliation.PROPERTY_ENDINGDATE, reconciliation.getEndingDate()));
    obc.add(Restrictions.gt(FIN_Reconciliation.PROPERTY_CREATIONDATE,
        reconciliation.getCreationDate()));
    obc.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT, reconciliation.getAccount()));
    obc.addOrder(Order.asc(FIN_Reconciliation.PROPERTY_ENDINGDATE));
    obc.addOrder(Order.asc(FIN_Reconciliation.PROPERTY_CREATIONDATE));
    final List<FIN_Reconciliation> reconciliations = obc.list();
    if (reconciliations.size() == 0) {
      return true;
    } else if (reconciliations.size() == 1) {
      if (reconciliations.get(0).isProcessed()) {
        return false;
      } else if (reconciliations.get(0).getFINReconciliationLineVList().size() == 0) {
        FIN_Reconciliation reconciliationToDelete = OBDal.getInstance().get(
            FIN_Reconciliation.class, reconciliations.get(0).getId());
        for (FIN_BankStatement bst : reconciliationToDelete.getFINBankStatementList()) {
          FIN_BankStatement bankstatement = OBDal.getInstance().get(FIN_BankStatement.class,
              bst.getId());
          bankstatement.setFINReconciliation(null);
          OBDal.getInstance().save(bankstatement);
          OBDal.getInstance().flush();
        }
        OBDal.getInstance().remove(reconciliationToDelete);
        OBDal.getInstance().flush();
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
}
