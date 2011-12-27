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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallStoredProcedure;

public class FIN_PaymentProposalProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    final Logger log4j = Logger.getLogger(this.getClass());
    dao = new AdvPaymentMngtDao();
    OBError message = null;
    try {
      // retrieve params
      final String strAction = (String) bundle.getParams().get("processProposalAction");
      final String recordID = (String) bundle.getParams().get("Fin_Payment_Proposal_ID");

      final VariablesSecureApp vars = bundle.getContext().toVars();
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      StringBuilder strMessageResult = new StringBuilder();
      String strMessageType = "Success";

      final FIN_PaymentProposal paymentProposal = dao
          .getObject(FIN_PaymentProposal.class, recordID);

      if (strAction.equals("GSP")) {
        if (paymentProposal.isProcessed()) {
          OBError msg = new OBError();
          String strMessage = "@DocumentProcessed@" + paymentProposal.getDocumentNo();
          String strMsgType = "Error";
          msg.setType(strMsgType);
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language, strMessage));
          bundle.setResult(msg);
          return;
        }

        FIN_PaymentMethod paymentMethodId = paymentProposal.getPaymentMethod();
        FIN_FinancialAccount financialAccountId = paymentProposal.getAccount();
        Organization orgId = paymentProposal.getOrganization();
        Date paymentDate = paymentProposal.getPaymentDate();
        boolean isReceipt = paymentProposal.isReceipt();
        Currency paymentCurrency = paymentProposal.getCurrency();
        Currency financialAccountCurrency = paymentProposal.getAccount().getCurrency();
        BigDecimal exchangeRate = paymentProposal.getFinancialTransactionConvertRate();

        OBContext.setAdminMode();
        try {

          List<FIN_PaymentPropDetail> selectedPaymentProposalDetails = dao
              .getOrderedPaymentProposalDetails(paymentProposal);

          final List<Object> parameters = new ArrayList<Object>();
          parameters.add(vars.getClient());
          parameters.add(orgId.getId());
          parameters.add(isReceipt ? "ARR" : "APP");
          String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
              parameters, null);

          BigDecimal paymentTotal = BigDecimal.ZERO;
          String strBusinessPartner_old = "-1";
          String strBusinessPartner = "";
          List<FIN_PaymentScheduleDetail> selectedPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
          HashMap<String, BigDecimal> selectedPaymentDetailsAmounts = new HashMap<String, BigDecimal>();
          boolean isWriteOff = false;
          boolean isRefund = false;

          for (FIN_PaymentPropDetail paymentProposalDetail : selectedPaymentProposalDetails) {
            if (paymentProposalDetail.getFINPaymentScheduledetail().getInvoicePaymentSchedule() != null) {
              strBusinessPartner = paymentProposalDetail.getFINPaymentScheduledetail()
                  .getInvoicePaymentSchedule().getInvoice().getBusinessPartner().getId();
            } else {
              strBusinessPartner = paymentProposalDetail.getFINPaymentScheduledetail()
                  .getOrderPaymentSchedule().getOrder().getBusinessPartner().getId();
            }

            if (!strBusinessPartner_old.equals(strBusinessPartner)
                && !strBusinessPartner_old.equals("-1")) {

              // String strPaymentDocumentNo = Utility.getDocumentNo(conProvider, vars,
              // "Payment Proposal", "FIN_Payment", strDocTypeId, strDocTypeId, false, true);
              String strPaymentDocumentNo = FIN_Utility.getDocumentNo(orgId, (isReceipt) ? "ARR"
                  : "APP", "DocumentNo_FIN_Payment_Proposal");

              BigDecimal finAccTxnAmount = paymentTotal.multiply(exchangeRate);
              long faPrecision = financialAccountCurrency.getStandardPrecision();
              finAccTxnAmount = finAccTxnAmount.setScale((int) faPrecision, RoundingMode.HALF_UP);
              FIN_Payment payment = FIN_AddPayment.savePayment(null, isReceipt,
                  dao.getObject(DocumentType.class, strDocTypeId), strPaymentDocumentNo,
                  dao.getObject(BusinessPartner.class, strBusinessPartner_old), paymentMethodId,
                  financialAccountId, paymentTotal.toString(), paymentDate, orgId, null,
                  selectedPaymentDetails, selectedPaymentDetailsAmounts, isWriteOff, isRefund,
                  paymentCurrency, exchangeRate, finAccTxnAmount);

              // process payment
              message = FIN_AddPayment.processPayment(vars, conProvider, "P", payment);
              if (message.getType().equals("Error")) {
                String exceptionMessage = payment.getBusinessPartner().getName();
                exceptionMessage += ": " + message.getMessage();
                throw new OBException(exceptionMessage);
              } else if (message.getType().equals("Warning"))
                strMessageType = message.getType();
              strMessageResult.append("@Payment@ ").append(payment.getDocumentNo());
              strMessageResult.append(" (").append(payment.getBusinessPartner().getName())
                  .append(") ");
              if (!"".equals(message.getMessage()))
                strMessageResult.append(": ").append(message.getMessage());
              strMessageResult.append("<br>");
              selectedPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
              selectedPaymentDetailsAmounts = new HashMap<String, BigDecimal>();
              isWriteOff = false;
              paymentTotal = BigDecimal.ZERO;
            }

            paymentTotal = paymentTotal.add(paymentProposalDetail.getAmount());
            selectedPaymentDetails.add(paymentProposalDetail.getFINPaymentScheduledetail());
            selectedPaymentDetailsAmounts.put(paymentProposalDetail.getFINPaymentScheduledetail()
                .getId(), paymentProposalDetail.getAmount());
            if (BigDecimal.ZERO.compareTo(paymentProposalDetail.getWriteoffAmount()) != 0)
              isWriteOff = true;
            strBusinessPartner_old = strBusinessPartner;
          }
          // String strPaymentDocumentNo = Utility.getDocumentNo(conProvider, vars,
          // "PaymentProcessProposal", "FIN_Payment", strDocTypeId, strDocTypeId, false, true);

          String strPaymentDocumentNo = FIN_Utility.getDocumentNo(orgId, (isReceipt) ? "ARR"
              : "APP", "DocumentNo_FIN_Payment_Proposal");

          BigDecimal finAccTxnAmount = paymentTotal.multiply(exchangeRate);
          long faPrecision = financialAccountCurrency.getStandardPrecision();
          finAccTxnAmount = finAccTxnAmount.setScale((int) faPrecision, RoundingMode.HALF_UP);

          FIN_Payment payment = FIN_AddPayment.savePayment(null, isReceipt,
              dao.getObject(DocumentType.class, strDocTypeId), strPaymentDocumentNo,
              dao.getObject(BusinessPartner.class, strBusinessPartner), paymentMethodId,
              financialAccountId, paymentTotal.toString(), paymentDate, orgId, null,
              selectedPaymentDetails, selectedPaymentDetailsAmounts, isWriteOff, isRefund,
              paymentCurrency, exchangeRate, finAccTxnAmount);
          paymentProposal.setStatus(isReceipt ? "RPR" : "PPM");
          // process payment
          message = FIN_AddPayment.processPayment(vars, conProvider, "P", payment);
          if ("RPAE".equals(payment.getStatus())) {
            paymentProposal.setStatus("RPAE");
            OBDal.getInstance().save(paymentProposal);
            OBDal.getInstance().flush();
          }
          if (message.getType().equals("Error")) {
            String exceptionMessage = payment.getBusinessPartner().getName();
            exceptionMessage += ": " + message.getMessage();
            throw new OBException(exceptionMessage);
          } else if (message.getType().equals("Warning"))
            strMessageType = message.getType();
          strMessageResult.append("@Payment@ ").append(payment.getDocumentNo());
          strMessageResult.append(" (").append(payment.getBusinessPartner().getName()).append(")");
          if (!"".equals(message.getMessage()))
            strMessageResult.append(": ").append(message.getMessage());
          strMessageResult.append("<br>");

        } finally {

          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }
        // If Automatic step is enabled and not awaiting execution get proposal status
        if (!"RPAE".equals(paymentProposal.getStatus())) {
          if (FIN_Utility.isAutomaticDepositWithdrawn(paymentProposal)) {
            paymentProposal.setStatus(isReceipt ? "RDNC" : "PWNC");
          } else {
            paymentProposal.setStatus(isReceipt ? "RPR" : "PPM");
          }
        }
        paymentProposal.setProcessed(true);
        paymentProposal.setAPRMProcessProposal("RE");
        OBDal.getInstance().save(paymentProposal);
        OBDal.getInstance().flush();

      } else if (strAction.equals("RE")) { // REACTIVATE
        paymentProposal.setProcessNow(true);

        List<String> paymentIdList = FIN_AddPayment.getPaymentFromPaymentProposal(paymentProposal);
        for (String id : paymentIdList) {
          FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, id);
          message = FIN_AddPayment.processPayment(vars, conProvider, "R", payment);
          if (message.getType().equals("Error")) {
            bundle.setResult(message);
            return;
          }
        }

        // Remove Payments and Payments Details
        for (String id : paymentIdList) {
          OBDal.getInstance().remove(OBDal.getInstance().get(FIN_Payment.class, id));
        }
        paymentProposal.setStatus("RPAP");
        paymentProposal.setProcessed(false);
        paymentProposal.setAPRMProcessProposal("G");
      }

      paymentProposal.setProcessNow(false);
      OBDal.getInstance().save(paymentProposal);
      OBDal.getInstance().flush();

      final OBError msg = new OBError();
      msg.setType(strMessageType);
      msg.setTitle(Utility.messageBD(conProvider, "Success", language));
      msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
          strMessageResult.toString()));
      bundle.setResult(msg);
    } catch (OBException e) {
      log4j.error("FIN_PaymentProposalProcess: " + e.getMessage());
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(Utility.parseTranslation(bundle.getConnection(), bundle.getContext().toVars(),
          bundle.getContext().getLanguage(), FIN_Utility.getExceptionMessage(e)));
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      bundle.setResult(msg);
    } catch (final Exception e) {
      e.printStackTrace(System.err);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      msg.setTitle("Error occurred");
      bundle.setResult(msg);
    }
  }
}