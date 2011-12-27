package org.openbravo.erpCommon.ad_actionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRMPendingPaymentFromInvoice;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_ExecutePayment;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallProcess;

public class RMCreateInvoice implements org.openbravo.scheduling.Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    final String language = bundle.getContext().getLanguage();
    final ConnectionProvider conProvider = bundle.getConnection();
    final VariablesSecureApp vars = bundle.getContext().toVars();

    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", language));

    final String strOrderId = (String) bundle.getParams().get("C_Order_ID");

    OBContext.setAdminMode(true);
    Process process = null;
    try {
      process = OBDal.getInstance().get(Process.class, "119");
    } finally {
      OBContext.restorePreviousMode();
    }
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("DateInvoiced", new Date());
    parameters.put("AD_Org_ID", null);
    parameters.put("C_Order_ID", strOrderId);
    parameters.put("C_BPartner_ID", null);
    parameters.put("InvoiceToDate", null);

    final ProcessInstance pinstance = CallProcess.getInstance().callProcess(process, strOrderId,
        parameters);
    msg.setMessage(Utility.parseTranslation(conProvider, vars, language, pinstance.getErrorMsg()));

    if (pinstance.getResult() == 0L) {
      // Error
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(conProvider, "Error", language));
      bundle.setResult(msg);
      return;
    }

    // Check if there is any payment pending execution for this invoice.
    try {
      executePayments();
    } catch (Exception e) {
      msg.setType("Warning");
      msg.setMessage(msg.getMessage() + "\n" + e.getMessage());
    }

    bundle.setResult(msg);
  }

  private void executePayments() {
    OBContext.setAdminMode();
    try {
      if (getLeaveAsCreditProcesses().isEmpty()) {
        return;
      }
      List<APRMPendingPaymentFromInvoice> pendingPayments = getPendingPayments();
      if (pendingPayments.isEmpty()) {
        return;
      }
      List<FIN_Payment> payments = new ArrayList<FIN_Payment>();
      PaymentExecutionProcess executionProcess = null;
      Organization organization = null;
      AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
      for (APRMPendingPaymentFromInvoice pendingPayment : pendingPayments) {
        if (executionProcess != null
            && organization != null
            && (executionProcess != pendingPayment.getPaymentExecutionProcess() || organization != pendingPayment
                .getOrganization())) {
          if (dao.isAutomaticExecutionProcess(executionProcess)) {
            FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
            executePayment.init("OTHER", executionProcess, payments, null,
                pendingPayment.getOrganization());
            executePayment.execute();
          }
          payments.clear();
        }
        executionProcess = pendingPayment.getPaymentExecutionProcess();
        organization = pendingPayment.getOrganization();
        payments.add(pendingPayment.getPayment());

      }
      if (dao.isAutomaticExecutionProcess(executionProcess)) {
        FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
        executePayment.init("APP", executionProcess, payments, null, organization);
        executePayment.execute();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private List<APRMPendingPaymentFromInvoice> getPendingPayments() {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Restrictions.eq(APRMPendingPaymentFromInvoice.PROPERTY_PROCESSNOW, false));
    ppfiCriteria.add(Restrictions
        .in(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENTEXECUTIONPROCESS,
            getLeaveAsCreditProcesses()));
    ppfiCriteria.addOrderBy(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENTEXECUTIONPROCESS, false);
    ppfiCriteria.addOrderBy(APRMPendingPaymentFromInvoice.PROPERTY_ORGANIZATION, false);
    return ppfiCriteria.list();
  }

  private List<PaymentExecutionProcess> getLeaveAsCreditProcesses() {
    OBCriteria<PaymentExecutionProcess> payExecProcCrit = OBDal.getInstance().createCriteria(
        PaymentExecutionProcess.class);
    payExecProcCrit.add(Restrictions.eq(PaymentExecutionProcess.PROPERTY_JAVACLASSNAME,
        "org.openbravo.advpaymentmngt.executionprocess.LeaveAsCredit"));

    return payExecProcCrit.list();
  }
}
