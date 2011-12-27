package org.openbravo.advpaymentmngt.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.scheduling.ProcessBundle;

public class FIN_BankStatementProcess implements org.openbravo.scheduling.Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", bundle.getContext()
        .getLanguage()));

    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("FIN_Bankstatement_ID");
      final FIN_BankStatement bankStatement = OBDal.getInstance().get(FIN_BankStatement.class,
          recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();

      bankStatement.setProcessNow(true);
      OBDal.getInstance().save(bankStatement);
      OBDal.getInstance().flush();

      if ("P".equals(strAction)) {
        // ***********************
        // Process Bank Statement
        // ***********************

        // Check all dates are after last reconciliation date or last transaction date of previous
        // bank statements.
        Date maxBSLDate = getMaxBSLDate(bankStatement.getAccount(), bankStatement);
        if (maxBSLDate != null) {
          for (FIN_BankStatementLine bsl : bankStatement.getFINBankStatementLineList()) {
            if (bsl.getTransactionDate().compareTo(maxBSLDate) <= 0) {
              msg.setType("Error");
              msg.setTitle(FIN_Utility.messageBD("Error"));
              String pattern = OBPropertiesProvider.getInstance().getOpenbravoProperties()
                  .getProperty("dateFormat.java");
              msg.setMessage(FIN_Utility.messageBD("APRM_BankStatementLineWrongDate")
                  + Utility.formatDate(maxBSLDate, pattern));
              bundle.setResult(msg);
              return;
            }
          }
        }

        bankStatement.setProcessed(true);
        bankStatement.setAPRMProcessBankStatement("R");
        OBDal.getInstance().save(bankStatement);
        OBDal.getInstance().flush();
      } else if (strAction.equals("R")) {
        // *************************
        // Reactivate Bank Statement
        // *************************
        // Already Posted Document
        if ("Y".equals(bankStatement.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@PostedDocument@"
              + ": " + bankStatement.getIdentifier()));
          bundle.setResult(msg);
          return;
        }
        // Already Reconciled
        for (FIN_BankStatementLine bsl : bankStatement.getFINBankStatementLineList()) {
          if (bsl.getFinancialAccountTransaction() != null) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@APRM_BSLineReconciled@" + ": " + bsl.getLineNo().toString()));
            bundle.setResult(msg);
            return;
          }
        }

        bankStatement.setProcessed(false);
        bankStatement.setAPRMProcessBankStatement("P");
        OBDal.getInstance().save(bankStatement);
        OBDal.getInstance().flush();
      }

      bankStatement.setProcessNow(false);
      OBDal.getInstance().save(bankStatement);
      OBDal.getInstance().flush();
      bundle.setResult(msg);

    } catch (Exception e) {
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }

  }

  private Date getMaxBSLDate(FIN_FinancialAccount account, FIN_BankStatement bankstatement) {
    // Get last transaction date from previous bank statements
    final StringBuilder whereClause = new StringBuilder();
    List<Object> parameters = new ArrayList<Object>();
    whereClause.append(" as bsl ");
    whereClause.append(" where bsl.");
    whereClause.append(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT);
    whereClause.append("." + FIN_BankStatement.PROPERTY_ACCOUNT + " = ?");
    parameters.add(account);
    whereClause.append(" and bsl." + FIN_BankStatementLine.PROPERTY_BANKSTATEMENT + " <> ?");
    parameters.add(bankstatement);
    whereClause.append(" and bsl.bankStatement.processed = 'Y'");
    whereClause.append(" order by bsl." + FIN_BankStatementLine.PROPERTY_TRANSACTIONDATE);
    whereClause.append(" desc");

    final OBQuery<FIN_BankStatementLine> obData = OBDal.getInstance().createQuery(
        FIN_BankStatementLine.class, whereClause.toString(), parameters);
    if (obData.count() > 0) {
      return obData.list().get(0).getTransactionDate();
    }

    // If no previous bank statement is found get the ending date of the last reconciliation
    FIN_Reconciliation rec = org.openbravo.advpaymentmngt.dao.TransactionsDao
        .getLastReconciliation(account, null);
    org.openbravo.dal.core.OBContext.setAdminMode(true);
    try {
      return (rec == null) ? null : rec.getEndingDate();
    } finally {
      org.openbravo.dal.core.OBContext.restorePreviousMode();
    }
  }
}
