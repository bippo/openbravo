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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.exception.NoExecutionProcessFoundException;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Payment_Credit;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;

public class FIN_PaymentProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    final String language = bundle.getContext().getLanguage();

    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", language));

    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("Fin_Payment_ID");
      final FIN_Payment payment = dao.getObject(FIN_Payment.class, recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();

      final ConnectionProvider conProvider = bundle.getConnection();
      final boolean isReceipt = payment.isReceipt();

      payment.setProcessNow(true);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().flush();
      if (strAction.equals("P") || strAction.equals("D")) {
        // Set APRM_Ready preference
        if (!dao.existsAPRMReadyPreference()
            && vars.getSessionValue("APRMT_MigrationToolRunning", "N").equals("Y")) {
          dao.createAPRMReadyPreference();
        }

        Set<String> documentOrganizations = OBContext.getOBContext()
            .getOrganizationStructureProvider(payment.getClient().getId())
            .getNaturalTree(payment.getOrganization().getId());
        if (!documentOrganizations.contains(payment.getAccount().getOrganization().getId())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_FinancialAccountNotInNaturalTree@"));
          bundle.setResult(msg);
          return;
        }
        Set<String> invoiceDocNos = new TreeSet<String>();
        Set<String> orderDocNos = new TreeSet<String>();
        Set<String> glitems = new TreeSet<String>();
        BigDecimal paymentAmount = BigDecimal.ZERO;
        BigDecimal paymentWriteOfAmount = BigDecimal.ZERO;

        // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
        // removed when new security implementation is done
        OBContext.setAdminMode();
        try {
          String strRefundCredit = "";
          // update payment schedule amount
          List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();

          // Show error message when payment has no lines
          if (paymentDetails.size() == 0) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@APRM_PaymentNoLines@"));
            bundle.setResult(msg);
            return;
          }
          for (FIN_PaymentDetail paymentDetail : paymentDetails) {
            for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                .getFINPaymentScheduleDetailList()) {
              paymentAmount = paymentAmount.add(paymentScheduleDetail.getAmount());
              BigDecimal writeoff = paymentScheduleDetail.getWriteoffAmount();
              if (writeoff == null)
                writeoff = BigDecimal.ZERO;
              paymentWriteOfAmount = paymentWriteOfAmount.add(writeoff);
              if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                final Invoice invoice = paymentScheduleDetail.getInvoicePaymentSchedule()
                    .getInvoice();
                invoiceDocNos.add(FIN_Utility.getDesiredDocumentNo(payment.getOrganization(),
                    invoice));
              }
              if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                orderDocNos.add(paymentScheduleDetail.getOrderPaymentSchedule().getOrder()
                    .getDocumentNo());
              }
              if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                  && paymentScheduleDetail.getOrderPaymentSchedule() == null
                  && paymentScheduleDetail.getPaymentDetails().getGLItem() == null) {
                if (paymentDetail.isRefund())
                  strRefundCredit = Utility.messageBD(conProvider, "APRM_RefundAmount", language);
                else {
                  strRefundCredit = Utility.messageBD(conProvider, "APRM_CreditAmount", language);
                  payment.setGeneratedCredit(paymentDetail.getAmount());
                }
                strRefundCredit += ": " + paymentDetail.getAmount().toString();
              }
            }
            if (paymentDetail.getGLItem() != null)
              glitems.add(paymentDetail.getGLItem().getName());
          }
          // Set description
          StringBuffer description = new StringBuffer();
          if (payment.getDescription() != null && !payment.getDescription().equals(""))
            description.append(payment.getDescription()).append("\n");
          if (!invoiceDocNos.isEmpty()) {
            description.append(Utility.messageBD(conProvider, "InvoiceDocumentno", language));
            description.append(": ").append(
                invoiceDocNos.toString().substring(1, invoiceDocNos.toString().length() - 1));
            description.append("\n");
          }
          if (!orderDocNos.isEmpty()) {
            description.append(Utility.messageBD(conProvider, "OrderDocumentno", language));
            description.append(": ").append(
                orderDocNos.toString().substring(1, orderDocNos.toString().length() - 1));
            description.append("\n");
          }
          if (!glitems.isEmpty()) {
            description.append(Utility.messageBD(conProvider, "APRM_GLItem", language));
            description.append(": ").append(
                glitems.toString().substring(1, glitems.toString().length() - 1));
            description.append("\n");
          }
          if (!"".equals(strRefundCredit))
            description.append(strRefundCredit).append("\n");

          String truncateDescription = (description.length() > 255) ? description.substring(0, 252)
              .concat("...").toString() : description.toString();
          payment.setDescription(truncateDescription);

          if (paymentAmount.compareTo(payment.getAmount()) != 0)
            payment.setUsedCredit(paymentAmount.subtract(payment.getAmount()));
          if (payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0)
            updateUsedCredit(payment);

          payment.setWriteoffAmount(paymentWriteOfAmount);
          payment.setProcessed(true);
          payment.setAPRMProcessPayment("R");
          if (BigDecimal.ZERO.compareTo(payment.getUsedCredit()) != 0
              || BigDecimal.ZERO.compareTo(payment.getGeneratedCredit()) != 0) {
            BusinessPartner businessPartner = payment.getBusinessPartner();
            if (businessPartner == null) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_CreditWithoutBPartner@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
            PriceList priceList = payment.isReceipt() ? businessPartner.getPriceList()
                : businessPartner.getPurchasePricelist();
            if (!payment.getCurrency().getId()
                .equals(priceList != null ? priceList.getCurrency().getId() : "")) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(String.format(
                  Utility.parseTranslation(conProvider, vars, language, "@APRM_CreditCurrency@"),
                  priceList != null ? priceList.getCurrency().getISOCode() : Utility
                      .parseTranslation(conProvider, vars, language,
                          "@APRM_CreditNoPricelistCurrency@")));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
          }
          // Execution Process
          if (dao.isAutomatedExecutionPayment(payment.getAccount(), payment.getPaymentMethod(),
              payment.isReceipt())) {
            try {
              payment.setStatus("RPAE");
              payment.setProcessNow(false);
              OBDal.getInstance().save(payment);
              OBDal.getInstance().flush();

              if (dao.hasNotDeferredExecutionProcess(payment.getAccount(),
                  payment.getPaymentMethod(), payment.isReceipt())) {
                PaymentExecutionProcess executionProcess = dao.getExecutionProcess(payment);
                if (dao.isAutomaticExecutionProcess(executionProcess)) {
                  final List<FIN_Payment> payments = new ArrayList<FIN_Payment>(1);
                  payments.add(payment);
                  FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
                  executePayment.init("APP", executionProcess, payments, null,
                      payment.getOrganization());
                  OBError result = executePayment.execute();
                  if ("Error".equals(result.getType())) {
                    msg.setType("Warning");
                    msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                        result.getMessage()));
                  } else if (!"".equals(result.getMessage())) {
                    String execProcessMsg = Utility.parseTranslation(conProvider, vars, language,
                        result.getMessage());
                    if (!"".equals(msg.getMessage()))
                      msg.setMessage(msg.getMessage() + "<br>");
                    msg.setMessage(msg.getMessage() + execProcessMsg);
                  }
                }
              }
            } catch (final NoExecutionProcessFoundException e) {
              e.printStackTrace(System.err);
              msg.setType("Warning");
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@NoExecutionProcessFound@"));
              bundle.setResult(msg);
              return;
            } catch (final Exception e) {
              e.printStackTrace(System.err);
              msg.setType("Warning");
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@IssueOnExecutionProcess@"));
              bundle.setResult(msg);
              return;
            }
          } else {
            BusinessPartner businessPartner = payment.getBusinessPartner();
            // When credit is used (consumed) we compensate so_creditused as this amount is already
            // included in the payment details. Credit consumed should not affect to so_creditused
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
              if (isReceipt) {
                increaseCustomerCredit(businessPartner, payment.getUsedCredit());
              } else {
                decreaseCustomerCredit(businessPartner, payment.getUsedCredit());
              }
            }
            for (FIN_PaymentDetail paymentDetail : payment.getFINPaymentDetailList()) {
              // Get payment schedule detail list ordered by amount asc.
              // First negative if they exist and then positives
              OBCriteria<FIN_PaymentScheduleDetail> obcPSD = OBDal.getInstance().createCriteria(
                  FIN_PaymentScheduleDetail.class);
              obcPSD.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS,
                  paymentDetail));
              obcPSD.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);

              for (FIN_PaymentScheduleDetail paymentScheduleDetail : obcPSD.list()) {
                BigDecimal amount = paymentScheduleDetail.getAmount().add(
                    paymentScheduleDetail.getWriteoffAmount());
                if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                  // BP SO_CreditUsed
                  businessPartner = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                      .getBusinessPartner();
                  // Payments update credit opposite to invoices
                  if (isReceipt) {
                    decreaseCustomerCredit(businessPartner, amount);
                  } else {
                    increaseCustomerCredit(businessPartner, amount);
                  }
                  FIN_AddPayment.updatePaymentScheduleAmounts(
                      paymentScheduleDetail.getInvoicePaymentSchedule(),
                      paymentScheduleDetail.getAmount(), paymentScheduleDetail.getWriteoffAmount());
                }
                if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                  FIN_AddPayment.updatePaymentScheduleAmounts(
                      paymentScheduleDetail.getOrderPaymentSchedule(),
                      paymentScheduleDetail.getAmount(), paymentScheduleDetail.getWriteoffAmount());
                }
                // when generating credit for a BP SO_CreditUsed is also updated
                if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                    && paymentScheduleDetail.getOrderPaymentSchedule() == null
                    && paymentScheduleDetail.getPaymentDetails().getGLItem() == null) {
                  // BP SO_CreditUsed
                  if (isReceipt) {
                    decreaseCustomerCredit(businessPartner, amount);
                  } else {
                    increaseCustomerCredit(businessPartner, amount);
                  }
                }
              }
            }
            payment.setStatus(isReceipt ? "RPR" : "PPM");
            if ((FIN_Utility.isAutomaticDepositWithdrawn(payment) || strAction.equals("D"))
                && payment.getAmount().compareTo(BigDecimal.ZERO) != 0)
              triggerAutomaticFinancialAccountTransaction(vars, conProvider, payment);
          }
          if (!payment.getAccount().getCurrency().equals(payment.getCurrency())
              && getConversionRateDocument(payment).size() == 0) {
            insertConversionRateDocument(payment);
          }
        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }

        // ***********************
        // Reactivate Payment
        // ***********************
      } else if (strAction.equals("R")) {
        // Already Posted Document
        if ("Y".equals(payment.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@PostedDocument@"
              + ": " + payment.getDocumentNo()));
          bundle.setResult(msg);
          return;
        }
        // Transaction exists
        if (hasTransaction(payment)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_TransactionExists@"));
          bundle.setResult(msg);
          return;
        }
        // Payment with generated credit already used on other payments.
        if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 1
            && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_PaymentGeneratedCreditIsUsed@"));
          bundle.setResult(msg);
          return;
        }

        // Do not restore paid amounts if the payment is awaiting execution.
        boolean restorePaidAmounts = !"RPAE".equals(payment.getStatus());
        // Initialize amounts
        payment.setProcessed(false);
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();
        payment.setWriteoffAmount(BigDecimal.ZERO);
        payment.setAmount(BigDecimal.ZERO);
        payment.setFinancialTransactionAmount(BigDecimal.ZERO);

        payment.setStatus("RPAP");
        payment.setDescription("");
        payment.setAPRMProcessPayment("P");
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();

        final List<FIN_PaymentDetail> removedPD = new ArrayList<FIN_PaymentDetail>();
        List<FIN_PaymentScheduleDetail> removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
        final List<String> removedPDIds = new ArrayList<String>();
        // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
        // removed when new security implementation is done
        OBContext.setAdminMode();
        try {
          BusinessPartner businessPartner = payment.getBusinessPartner();
          // When credit is used (consumed) we compensate so_creditused as this amount is already
          // included in the payment details. Credit consumed should not affect to so_creditused
          if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
              && payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
            if (isReceipt) {
              decreaseCustomerCredit(businessPartner, payment.getUsedCredit());
            } else {
              increaseCustomerCredit(businessPartner, payment.getUsedCredit());
            }
          }
          List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
          List<ConversionRateDoc> conversionRates = payment.getCurrencyConversionRateDocList();
          Set<String> invoiceDocNos = new HashSet<String>();
          for (FIN_PaymentDetail paymentDetail : paymentDetails) {
            removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
            for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                .getFINPaymentScheduleDetailList()) {
              BigDecimal amount = paymentScheduleDetail.getAmount().add(
                  paymentScheduleDetail.getWriteoffAmount());
              if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                // Remove invoice description related to the credit payments
                final Invoice invoice = paymentScheduleDetail.getInvoicePaymentSchedule()
                    .getInvoice();
                invoiceDocNos.add(invoice.getDocumentNo());
                final String invDesc = invoice.getDescription();
                if (invDesc != null) {
                  final String creditMsg = Utility.messageBD(new DalConnectionProvider(),
                      "APRM_InvoiceDescUsedCredit", vars.getLanguage());
                  if (creditMsg != null) {
                    final StringBuffer newDesc = new StringBuffer();
                    for (final String line : invDesc.split("\n")) {
                      if (!line.startsWith(creditMsg.substring(0, creditMsg.lastIndexOf("%s")))) {
                        newDesc.append(line);
                        if (!"".equals(line))
                          newDesc.append("\n");
                      }
                    }
                    invoice.setDescription(newDesc.toString());
                  }
                }
                if (restorePaidAmounts) {
                  FIN_AddPayment.updatePaymentScheduleAmounts(paymentScheduleDetail
                      .getInvoicePaymentSchedule(), paymentScheduleDetail.getAmount().negate(),
                      paymentScheduleDetail.getWriteoffAmount().negate());
                  // BP SO_CreditUsed
                  businessPartner = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                      .getBusinessPartner();
                  if (isReceipt) {
                    increaseCustomerCredit(businessPartner, amount);
                  } else {
                    decreaseCustomerCredit(businessPartner, amount);
                  }
                }
              }
              if (paymentScheduleDetail.getOrderPaymentSchedule() != null && restorePaidAmounts) {
                FIN_AddPayment.updatePaymentScheduleAmounts(paymentScheduleDetail
                    .getOrderPaymentSchedule(), paymentScheduleDetail.getAmount().negate(),
                    paymentScheduleDetail.getWriteoffAmount().negate());
              }
              // when generating credit for a BP SO_CreditUsed is also updated
              if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                  && paymentScheduleDetail.getOrderPaymentSchedule() == null
                  && paymentScheduleDetail.getPaymentDetails().getGLItem() == null
                  && restorePaidAmounts) {
                // BP SO_CreditUsed
                if (isReceipt) {
                  increaseCustomerCredit(businessPartner, amount);
                } else {
                  decreaseCustomerCredit(businessPartner, amount);
                }
              }
              FIN_AddPayment.mergePaymentScheduleDetails(paymentScheduleDetail);
              removedPDS.add(paymentScheduleDetail);

            }
            paymentDetail.getFINPaymentScheduleDetailList().removeAll(removedPDS);
            OBDal.getInstance().getSession().refresh(paymentDetail);
            removedPD.add(paymentDetail);
            removedPDIds.add(paymentDetail.getId());
            OBDal.getInstance().save(paymentDetail);
          }
          for (String pdToRm : removedPDIds) {
            OBDal.getInstance().remove(OBDal.getInstance().get(FIN_PaymentDetail.class, pdToRm));
          }
          payment.getFINPaymentDetailList().removeAll(removedPD);
          payment.getCurrencyConversionRateDocList().removeAll(conversionRates);
          payment.setFinancialTransactionConvertRate(BigDecimal.ZERO);
          OBDal.getInstance().save(payment);

          if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
              && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
            undoUsedCredit(payment, vars, invoiceDocNos);
          }

          List<FIN_Payment> creditPayments = new ArrayList<FIN_Payment>();
          for (final FIN_Payment_Credit pc : payment.getFINPaymentCreditList()) {
            creditPayments.add(pc.getCreditPaymentUsed());
          }
          for (final FIN_Payment creditPayment : creditPayments) {
            // Update Description
            final String payDesc = creditPayment.getDescription();
            if (payDesc != null) {
              final String invoiceDocNoMsg = Utility.messageBD(new DalConnectionProvider(),
                  "APRM_CreditUsedinInvoice", vars.getLanguage());
              if (invoiceDocNoMsg != null) {
                final StringBuffer newDesc = new StringBuffer();
                for (final String line : payDesc.split("\n")) {
                  boolean include = true;
                  if (line.startsWith(invoiceDocNoMsg.substring(0,
                      invoiceDocNoMsg.lastIndexOf("%s")))) {
                    for (final String docNo : invoiceDocNos) {
                      if (line.indexOf(docNo) > 0) {
                        include = false;
                        break;
                      }
                    }
                  }
                  if (include) {
                    newDesc.append(line);
                    if (!"".equals(line))
                      newDesc.append("\n");
                  }
                }
                creditPayment.setDescription(newDesc.toString());
              }
            }
          }
          payment.getFINPaymentCreditList().clear();
          payment.setGeneratedCredit(BigDecimal.ZERO);
          payment.setUsedCredit(BigDecimal.ZERO);

        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }

      } else if (strAction.equals("V")) {
        // Void
        OBContext.setAdminMode();
        try {
          if (payment.isProcessed()) {
            // Already Posted Document
            if ("Y".equals(payment.getPosted())) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@PostedDocument@" + ": " + payment.getDocumentNo()));
              bundle.setResult(msg);
              return;
            }
            // Transaction exists
            if (hasTransaction(payment)) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_TransactionExists@"));
              bundle.setResult(msg);
              return;
            }
            // Payment with generated credit already used on other payments.
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 1
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_PaymentGeneratedCreditIsUsed@"));
              bundle.setResult(msg);
              return;
            }
            // Payment not in Awaiting Execution
            if (!"RPAE".equals(payment.getStatus())) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_PaymentNotRPAE_NotVoid@"));
              bundle.setResult(msg);
              return;
            }

            /*
             * Void the payment
             */
            payment.setStatus("RPVOID");

            /*
             * Cancel all payment schedule details related to the payment
             */
            final List<FIN_PaymentScheduleDetail> removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
            Set<String> invoiceDocNos = new HashSet<String>();
            for (final FIN_PaymentDetail paymentDetail : payment.getFINPaymentDetailList()) {
              for (final FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                  .getFINPaymentScheduleDetailList()) {
                BigDecimal outStandingAmt = BigDecimal.ZERO;

                if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                  // Related to invoices
                  for (final FIN_PaymentScheduleDetail invScheDetail : paymentScheduleDetail
                      .getInvoicePaymentSchedule()
                      .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
                    if (invScheDetail.getPaymentDetails() == null) {
                      outStandingAmt = outStandingAmt.add(invScheDetail.getAmount());
                      removedPDS.add(invScheDetail);
                    } else if (invScheDetail.equals(paymentScheduleDetail)) {
                      outStandingAmt = outStandingAmt.add(invScheDetail.getAmount());
                      paymentScheduleDetail.setCanceled(true);
                    }
                    invoiceDocNos.add(paymentScheduleDetail.getInvoicePaymentSchedule()
                        .getInvoice().getDocumentNo());
                  }
                  // Create merged Payment Schedule Detail with the pending to be paid amount
                  if (outStandingAmt.compareTo(BigDecimal.ZERO) != 0) {
                    final FIN_PaymentScheduleDetail mergedScheduleDetail = dao
                        .getNewPaymentScheduleDetail(payment.getOrganization(), outStandingAmt);
                    mergedScheduleDetail.setInvoicePaymentSchedule(paymentScheduleDetail
                        .getInvoicePaymentSchedule());
                    OBDal.getInstance().save(mergedScheduleDetail);
                  }
                } else if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                  // Related to orders
                  for (final FIN_PaymentScheduleDetail ordScheDetail : paymentScheduleDetail
                      .getOrderPaymentSchedule()
                      .getFINPaymentScheduleDetailOrderPaymentScheduleList()) {
                    if (ordScheDetail.getPaymentDetails() == null) {
                      outStandingAmt = outStandingAmt.add(ordScheDetail.getAmount());
                      removedPDS.add(ordScheDetail);
                    } else if (ordScheDetail.equals(paymentScheduleDetail)) {
                      outStandingAmt = outStandingAmt.add(ordScheDetail.getAmount());
                      paymentScheduleDetail.setCanceled(true);
                    }
                  }
                  // Create merged Payment Schedule Detail with the pending to be paid amount
                  if (outStandingAmt.compareTo(BigDecimal.ZERO) != 0) {
                    final FIN_PaymentScheduleDetail mergedScheduleDetail = dao
                        .getNewPaymentScheduleDetail(payment.getOrganization(), outStandingAmt);
                    mergedScheduleDetail.setOrderPaymentSchedule(paymentScheduleDetail
                        .getOrderPaymentSchedule());
                    OBDal.getInstance().save(mergedScheduleDetail);
                  }
                } else if (paymentDetail.getGLItem() != null) {
                  paymentScheduleDetail.setCanceled(true);
                } else if (paymentScheduleDetail.getOrderPaymentSchedule() == null
                    && paymentScheduleDetail.getInvoicePaymentSchedule() == null) {
                  // Credit payment
                  payment.setGeneratedCredit(payment.getGeneratedCredit().subtract(
                      paymentScheduleDetail.getAmount()));
                  removedPDS.add(paymentScheduleDetail);
                }

                OBDal.getInstance().save(payment);
                OBDal.getInstance().flush();
              }
              paymentDetail.getFINPaymentScheduleDetailList().removeAll(removedPDS);
              for (FIN_PaymentScheduleDetail removedPD : removedPDS)
                OBDal.getInstance().remove(removedPD);
            }
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
              undoUsedCredit(payment, vars, invoiceDocNos);
            }
            payment.getFINPaymentCreditList().clear();
            payment.setUsedCredit(BigDecimal.ZERO);
          }
        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }
      }

      payment.setProcessNow(false);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().flush();

      bundle.setResult(msg);

    } catch (final Exception e) {
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }
  }

  /**
   * Method used to update the credit used when the user doing invoice processing or payment
   * processing
   * 
   * @param businessPartner
   * @param amount
   *          Payment amount
   * @param add
   */
  private void updateCustomerCredit(BusinessPartner businessPartner, BigDecimal amount, boolean add) {
    BigDecimal creditUsed = businessPartner.getCreditUsed();
    if (add) {
      creditUsed = creditUsed.add(amount);
    } else {
      creditUsed = creditUsed.subtract(amount);
    }
    businessPartner.setCreditUsed(creditUsed);
    OBDal.getInstance().save(businessPartner);
    // OBDal.getInstance().flush();
  }

  private void increaseCustomerCredit(BusinessPartner businessPartner, BigDecimal amount) {
    updateCustomerCredit(businessPartner, amount, true);
  }

  private void decreaseCustomerCredit(BusinessPartner businessPartner, BigDecimal amount) {
    updateCustomerCredit(businessPartner, amount, false);
  }

  private void triggerAutomaticFinancialAccountTransaction(VariablesSecureApp vars,
      ConnectionProvider connectionProvider, FIN_Payment payment) {
    FIN_FinaccTransaction transaction = TransactionsDao.createFinAccTransaction(payment);
    try {
      processTransaction(vars, connectionProvider, "P", transaction);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace(System.err);
    }
    return;
  }

  private static boolean hasTransaction(FIN_Payment payment) {
    OBCriteria<FIN_FinaccTransaction> transaction = OBDal.getInstance().createCriteria(
        FIN_FinaccTransaction.class);
    transaction.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_FINPAYMENT, payment));
    List<FIN_FinaccTransaction> list = transaction.list();
    if (list == null || list.size() == 0)
      return false;
    return true;
  }

  private void updateUsedCredit(FIN_Payment newPayment) {
    if (newPayment.getFINPaymentCreditList().isEmpty()) {
      // We process the payment from the Payment In/Out window (not from the Process Invoice flow)
      final BigDecimal usedAmount = newPayment.getUsedCredit();
      final BusinessPartner bp = newPayment.getBusinessPartner();
      final boolean isReceipt = newPayment.isReceipt();
      final Organization Org = newPayment.getOrganization();

      List<FIN_Payment> creditPayments = dao.getCustomerPaymentsWithCredit(Org, bp, isReceipt);
      BigDecimal pendingToAllocateAmount = usedAmount;
      for (FIN_Payment creditPayment : creditPayments) {
        BigDecimal availableAmount = creditPayment.getGeneratedCredit().subtract(
            creditPayment.getUsedCredit());
        if (pendingToAllocateAmount.compareTo(availableAmount) == 1) {
          creditPayment.setUsedCredit(creditPayment.getUsedCredit().add(availableAmount));
          pendingToAllocateAmount = pendingToAllocateAmount.subtract(availableAmount);
          linkCreditPayment(newPayment, availableAmount, creditPayment);
          OBDal.getInstance().save(creditPayment);
        } else {
          creditPayment.setUsedCredit(creditPayment.getUsedCredit().add(pendingToAllocateAmount));
          linkCreditPayment(newPayment, pendingToAllocateAmount, creditPayment);
          OBDal.getInstance().save(creditPayment);
          break;
        }
      }
    }
  }

  public static void linkCreditPayment(FIN_Payment newPayment, BigDecimal usedAmount,
      FIN_Payment creditPayment) {
    final FIN_Payment_Credit creditInfo = OBProvider.getInstance().get(FIN_Payment_Credit.class);
    creditInfo.setPayment(newPayment);
    creditInfo.setAmount(usedAmount);
    creditInfo.setCurrency(newPayment.getCurrency());
    creditInfo.setCreditPaymentUsed(creditPayment);
    creditInfo.setOrganization(newPayment.getOrganization());
    creditInfo.setClient(newPayment.getClient());
    newPayment.getFINPaymentCreditList().add(creditInfo);
  }

  private void undoUsedCredit(FIN_Payment myPayment, VariablesSecureApp vars,
      Set<String> invoiceDocNos) {
    final List<FIN_Payment> payments = new ArrayList<FIN_Payment>();
    for (final FIN_Payment_Credit pc : myPayment.getFINPaymentCreditList()) {
      final FIN_Payment creditPaymentUsed = pc.getCreditPaymentUsed();
      creditPaymentUsed.setUsedCredit(creditPaymentUsed.getUsedCredit().subtract(pc.getAmount()));
      payments.add(creditPaymentUsed);
    }

    for (final FIN_Payment payment : payments) {
      // Update Description
      final String payDesc = payment.getDescription();
      if (payDesc != null) {
        final String invoiceDocNoMsg = Utility.messageBD(new DalConnectionProvider(),
            "APRM_CreditUsedinInvoice", vars.getLanguage());
        if (invoiceDocNoMsg != null) {
          final StringBuffer newDesc = new StringBuffer();
          for (final String line : payDesc.split("\n")) {
            boolean include = true;
            if (line.startsWith(invoiceDocNoMsg.substring(0, invoiceDocNoMsg.lastIndexOf("%s")))) {
              for (final String docNo : invoiceDocNos) {
                if (line.indexOf(docNo) > 0) {
                  include = false;
                  break;
                }
              }
            }
            if (include) {
              newDesc.append(line);
              if (!"".equals(line))
                newDesc.append("\n");
            }
          }
          payment.setDescription(newDesc.toString());
        }
      }
    }
  }

  private List<ConversionRateDoc> getConversionRateDocument(FIN_Payment payment) {
    OBContext.setAdminMode();
    try {
      OBCriteria<ConversionRateDoc> obc = OBDal.getInstance().createCriteria(
          ConversionRateDoc.class);
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, payment.getCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, payment.getAccount()
          .getCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_PAYMENT, payment));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ConversionRateDoc insertConversionRateDocument(FIN_Payment payment) {
    OBContext.setAdminMode();
    try {
      ConversionRateDoc newConversionRateDoc = OBProvider.getInstance()
          .get(ConversionRateDoc.class);
      newConversionRateDoc.setOrganization(payment.getOrganization());
      newConversionRateDoc.setCurrency(payment.getCurrency());
      newConversionRateDoc.setToCurrency(payment.getAccount().getCurrency());
      newConversionRateDoc.setRate(payment.getFinancialTransactionConvertRate());
      newConversionRateDoc.setForeignAmount(payment.getFinancialTransactionAmount());
      newConversionRateDoc.setPayment(payment);
      OBDal.getInstance().save(newConversionRateDoc);
      OBDal.getInstance().flush();
      return newConversionRateDoc;
    } finally {
      OBContext.restorePreviousMode();
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
