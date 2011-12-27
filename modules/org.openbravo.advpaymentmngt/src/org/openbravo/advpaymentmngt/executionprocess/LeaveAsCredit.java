package org.openbravo.advpaymentmngt.executionprocess;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.advpaymentmngt.utility.FIN_PaymentExecutionProcess;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.PaymentRun;
import org.openbravo.model.financialmgmt.payment.PaymentRunPayment;

public class LeaveAsCredit implements FIN_PaymentExecutionProcess {

  @Override
  public OBError execute(PaymentRun paymentRun) throws ServletException {
    paymentRun.setStatus("PE");
    OBDal.getInstance().save(paymentRun);
    for (PaymentRunPayment paymentRunPayment : paymentRun.getFinancialMgmtPaymentRunPaymentList()) {
      FIN_Payment payment = paymentRunPayment.getPayment();
      if (payment.getAmount().compareTo(BigDecimal.ZERO) < 0) {
        payment.setGeneratedCredit(payment.getAmount().negate());
        payment.setAmount(BigDecimal.ZERO);
      }
      payment.setStatus(payment.isReceipt() ? "RPR" : "PPM");
      paymentRunPayment.setResult("S");
      OBDal.getInstance().save(payment);
      OBDal.getInstance().save(paymentRunPayment);
    }
    paymentRun.setStatus("E");
    OBDal.getInstance().save(paymentRun);
    OBDal.getInstance().flush();
    OBError result = new OBError();
    result.setType("Success");
    result.setMessage("@Success@");
    return result;
  }
}
