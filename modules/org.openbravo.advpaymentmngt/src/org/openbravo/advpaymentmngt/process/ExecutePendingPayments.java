package org.openbravo.advpaymentmngt.process;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.advpaymentmngt.APRMPendingPaymentFromInvoice;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.quartz.JobExecutionException;

public class ExecutePendingPayments extends DalBaseProcess {

  private ProcessLogger logger;
  private AdvPaymentMngtDao dao;

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    dao = new AdvPaymentMngtDao();
    PaymentExecutionProcess executionProcess = null;
    Organization organization = null;
    VariablesSecureApp vars = bundle.getContext().toVars();
    final String language = bundle.getContext().getLanguage();

    OBContext.setAdminMode();
    try {
      List<APRMPendingPaymentFromInvoice> pendingPayments = dao.getPendingPayments();
      List<FIN_Payment> payments = new ArrayList<FIN_Payment>();
      try {
        for (APRMPendingPaymentFromInvoice pendingPayment : pendingPayments) {
          if (executionProcess != null
              && organization != null
              && (executionProcess != pendingPayment.getPaymentExecutionProcess() || organization != pendingPayment
                  .getOrganization())) {
            logger.logln(executionProcess.getIdentifier());
            if (dao.isAutomaticExecutionProcess(executionProcess)) {
              FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
              executePayment.init("OTHER", executionProcess, payments, null,
                  pendingPayment.getOrganization());
              OBError result = executePayment.execute();
              logger.logln(Utility.parseTranslation(bundle.getConnection(), vars, language,
                  result.getMessage()));
            }
            payments.clear();
          }
          executionProcess = pendingPayment.getPaymentExecutionProcess();
          organization = pendingPayment.getOrganization();
          payments.add(pendingPayment.getPayment());

        }
        logger.logln(executionProcess.getIdentifier());
        if (dao.isAutomaticExecutionProcess(executionProcess)) {
          FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
          executePayment.init("APP", executionProcess, payments, null, organization);
          OBError result = executePayment.execute();
          logger.logln(Utility.parseTranslation(bundle.getConnection(), vars, language,
              result.getMessage()));
        }

      } catch (Exception e) {
        throw new JobExecutionException(e.getMessage(), e);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
