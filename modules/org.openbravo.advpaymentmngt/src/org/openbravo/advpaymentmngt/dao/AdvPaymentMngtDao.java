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
 * Contributor(s):  Enterprise Intelligence Systems (http://www.eintel.com.au).
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.dao;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.openbravo.advpaymentmngt.APRMPendingPaymentFromInvoice;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.advpaymentmngt.utility.Value;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcessParameter;
import org.openbravo.model.financialmgmt.payment.PaymentPriority;
import org.openbravo.model.financialmgmt.payment.PaymentRun;
import org.openbravo.model.financialmgmt.payment.PaymentRunParameter;
import org.openbravo.model.financialmgmt.payment.PaymentRunPayment;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;

public class AdvPaymentMngtDao {

  public enum PaymentDirection {
    IN, OUT, EITHER
  }

  public AdvPaymentMngtDao() {
  }

  public <T extends BaseOBObject> T getObject(Class<T> t, String strId) {
    return OBDal.getInstance().get(t, strId);
  }

  public List<FIN_PaymentScheduleDetail> getInvoicePendingScheduledPaymentDetails(Invoice invoice) {
    final StringBuilder whereClause = new StringBuilder();

    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as psd ");
      whereClause.append(" where psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      whereClause.append(" is null");
      whereClause.append("   and psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(".id = '");
      whereClause.append(invoice.getId());
      whereClause.append("'");
      whereClause.append(" order by psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT);
      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, whereClause.toString());

      return obqPSD.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<FIN_PaymentScheduleDetail> getOrderPendingScheduledPaymentDetails(Order order) {
    final StringBuilder whereClause = new StringBuilder();

    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as psd ");
      whereClause.append(" where psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      whereClause.append(" is null");
      whereClause.append("   and psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      whereClause.append(".id = '");
      whereClause.append(order.getId());
      whereClause.append("'");
      whereClause.append(" order by psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT);
      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, whereClause.toString());

      return obqPSD.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<FIN_PaymentScheduleDetail> getFilteredScheduledPaymentDetails(
      Organization organization, BusinessPartner businessPartner, Currency currency,
      Date dueDateFrom, Date dueDateTo, String strTransactionType, FIN_PaymentMethod paymentMethod,
      List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails, boolean isReceipt) {
    return getFilteredScheduledPaymentDetails(organization, businessPartner, currency, dueDateFrom,
        dueDateTo, null, null, strTransactionType, "", paymentMethod,
        selectedScheduledPaymentDetails, isReceipt);
  }

  public List<FIN_PaymentScheduleDetail> getFilteredScheduledPaymentDetails(
      Organization organization, BusinessPartner businessPartner, Currency currency,
      Date dueDateFrom, Date dueDateTo, Date transactionDateFrom, Date transactionDateTo,
      String strTransactionType, String strDocumentNo, FIN_PaymentMethod paymentMethod,
      List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails, boolean isReceipt) {

    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();

    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as psd "); // pending scheduled payments //
      whereClause.append(" left outer join psd.orderPaymentSchedule as ops ");
      whereClause.append(" left outer join ops.order as ord");
      whereClause.append(" left outer join ops.fINPaymentPriority as opriority");
      whereClause.append(" left outer join psd.invoicePaymentSchedule as ips ");
      whereClause.append(" left outer join ips.invoice as inv");
      whereClause.append(" left outer join ips.fINPaymentPriority as ipriority");
      whereClause.append(" where psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      whereClause.append(" is null");
      whereClause.append(" and psd.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORGANIZATION);
      whereClause.append(".id in (");
      whereClause.append(FIN_Utility.getInStrSet(OBContext.getOBContext()
          .getOrganizationStructureProvider().getNaturalTree(organization.getId())));
      whereClause.append(")");

      // remove selected payments
      if (selectedScheduledPaymentDetails != null && selectedScheduledPaymentDetails.size() > 0) {
        String strSelectedPaymentDetails = FIN_Utility
            .getInStrList(selectedScheduledPaymentDetails);
        whereClause.append(" and psd not in (");
        whereClause.append(strSelectedPaymentDetails);
        whereClause.append(")");
      }

      // block schedule payments in other payment proposal
      final OBCriteria<FIN_PaymentPropDetail> obc = OBDal.getInstance().createCriteria(
          FIN_PaymentPropDetail.class);
      obc.add(Restrictions.isNotNull(FIN_PaymentPropDetail.PROPERTY_FINPAYMENTSCHEDULEDETAIL));
      if (obc.list() != null && obc.list().size() > 0) {
        List<FIN_PaymentScheduleDetail> aux = new ArrayList<FIN_PaymentScheduleDetail>();
        for (FIN_PaymentPropDetail ppd : obc.list()) {
          aux.add(ppd.getFINPaymentScheduledetail());
        }
        whereClause.append(" and psd.id not in (" + FIN_Utility.getInStrList(aux) + ")");
      }

      // Transaction type filter
      whereClause.append(" and (");
      if (strTransactionType.equals("I") || strTransactionType.equals("B")) {
        whereClause.append(" (inv is not null");
        if (businessPartner != null) {
          whereClause.append(" and inv.");
          whereClause.append(Invoice.PROPERTY_BUSINESSPARTNER);
          whereClause.append(".id = '");
          whereClause.append(businessPartner.getId());
          whereClause.append("'");
        }
        if (paymentMethod != null) {
          whereClause.append(" and inv.");
          whereClause.append(Invoice.PROPERTY_PAYMENTMETHOD);
          whereClause.append(".id = '");
          whereClause.append(paymentMethod.getId());
          whereClause.append("'");
        }
        if (transactionDateFrom != null) {
          whereClause.append(" and inv.");
          whereClause.append(Invoice.PROPERTY_INVOICEDATE);
          whereClause.append(" >= ?");
          parameters.add(transactionDateFrom);
        }
        if (transactionDateTo != null) {
          whereClause.append(" and inv.");
          whereClause.append(Invoice.PROPERTY_INVOICEDATE);
          whereClause.append(" < ?");
          parameters.add(transactionDateTo);
        }
        whereClause.append(" and inv.");
        whereClause.append(Invoice.PROPERTY_SALESTRANSACTION);
        whereClause.append(" = ");
        whereClause.append(isReceipt);
        if (!StringUtils.isEmpty(strDocumentNo)) {
          whereClause.append(" and inv.");
          whereClause.append(Invoice.PROPERTY_DOCUMENTNO);
          whereClause.append(" like '%");
          whereClause.append(strDocumentNo);
          whereClause.append("%' ");
        }
        whereClause.append(" and inv.");
        whereClause.append(Invoice.PROPERTY_CURRENCY);
        whereClause.append(".id = '");
        whereClause.append(currency.getId());
        whereClause.append("')");

      }
      if (strTransactionType.equals("B"))
        whereClause.append(" or ");
      if (strTransactionType.equals("O") || strTransactionType.equals("B")) {
        whereClause.append(" (ord is not null");
        if (businessPartner != null) {
          whereClause.append(" and ord.");
          whereClause.append(Order.PROPERTY_BUSINESSPARTNER);
          whereClause.append(".id = '");
          whereClause.append(businessPartner.getId());
          whereClause.append("'");
        }
        if (paymentMethod != null) {
          whereClause.append(" and ord.");
          whereClause.append(Order.PROPERTY_PAYMENTMETHOD);
          whereClause.append(".id = '");
          whereClause.append(paymentMethod.getId());
          whereClause.append("'");
        }
        if (transactionDateFrom != null) {
          whereClause.append(" and ord.");
          whereClause.append(Order.PROPERTY_ORDERDATE);
          whereClause.append(" >= ?");
          parameters.add(transactionDateFrom);
        }
        if (transactionDateTo != null) {
          whereClause.append(" and ord.");
          whereClause.append(Order.PROPERTY_ORDERDATE);
          whereClause.append(" < ?");
          parameters.add(transactionDateTo);
        }
        if (!StringUtils.isEmpty(strDocumentNo)) {
          whereClause.append(" and ord.");
          whereClause.append(Order.PROPERTY_DOCUMENTNO);
          whereClause.append(" like '%");
          whereClause.append(strDocumentNo);
          whereClause.append("%' ");
        }
        whereClause.append(" and ord.");
        whereClause.append(Order.PROPERTY_SALESTRANSACTION);
        whereClause.append(" = ");
        whereClause.append(isReceipt);
        whereClause.append(" and ord.");
        whereClause.append(Order.PROPERTY_CURRENCY);
        whereClause.append(".id = '");
        whereClause.append(currency.getId());
        whereClause.append("')");
      }
      whereClause.append(")");
      // dateFrom
      if (dueDateFrom != null) {
        whereClause.append(" and COALESCE(ips.");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        whereClause.append(", ops.");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        whereClause.append(") >= ?");
        parameters.add(dueDateFrom);
      }
      // dateTo
      if (dueDateTo != null) {
        whereClause.append(" and COALESCE(ips.");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        whereClause.append(", ops.");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        whereClause.append(") < ?");
        parameters.add(dueDateTo);
      }

      // TODO: Add order to show first scheduled payments from invoices and later scheduled payments
      // from not invoiced orders.
      whereClause.append(" order by");
      whereClause.append(" COALESCE(ipriority.");
      whereClause.append(PaymentPriority.PROPERTY_PRIORITY);
      whereClause.append(", opriority.");
      whereClause.append(PaymentPriority.PROPERTY_PRIORITY);
      whereClause.append(")");
      whereClause.append(", ");
      whereClause.append(" COALESCE(ips.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(", ops.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(")");
      whereClause.append(", COALESCE(inv.");
      whereClause.append(Invoice.PROPERTY_DOCUMENTNO);
      whereClause.append(", ord.");
      whereClause.append(Order.PROPERTY_DOCUMENTNO);
      whereClause.append(")");
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT);
      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, whereClause.toString());

      obqPSD.setParameters(parameters);
      return obqPSD.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public FIN_Payment getNewPayment(boolean isReceipt, Organization organization,
      DocumentType docType, String strPaymentDocumentNo, BusinessPartner businessPartner,
      FIN_PaymentMethod paymentMethod, FIN_FinancialAccount finAccount, String strPaymentAmount,
      Date paymentDate, String referenceNo) {
    return getNewPayment(isReceipt, organization, docType, strPaymentDocumentNo, businessPartner,
        paymentMethod, finAccount, strPaymentAmount, paymentDate, referenceNo, null, null, null);
  }

  public FIN_Payment getNewPayment(boolean isReceipt, Organization organization,
      DocumentType docType, String strPaymentDocumentNo, BusinessPartner businessPartner,
      FIN_PaymentMethod paymentMethod, FIN_FinancialAccount finAccount, String strPaymentAmount,
      Date paymentDate, String referenceNo, Currency paymentCurrency, BigDecimal finTxnConvertRate,
      BigDecimal finTxnAmount) {
    final FIN_Payment newPayment = OBProvider.getInstance().get(FIN_Payment.class);
    newPayment.setReceipt(isReceipt);
    newPayment.setDocumentType(docType);
    newPayment.setDocumentNo(strPaymentDocumentNo);
    newPayment.setOrganization(organization);
    newPayment.setClient(organization.getClient());
    newPayment.setStatus("RPAP");
    newPayment.setBusinessPartner(businessPartner);
    newPayment.setPaymentMethod(paymentMethod);
    newPayment.setAccount(finAccount);
    final BigDecimal paymentAmount = new BigDecimal(strPaymentAmount);
    newPayment.setAmount(paymentAmount);
    newPayment.setPaymentDate(paymentDate);
    if (paymentCurrency != null) {
      newPayment.setCurrency(paymentCurrency);
    } else {
      newPayment.setCurrency(finAccount.getCurrency());
    }
    newPayment.setReferenceNo(referenceNo);
    if (finTxnConvertRate == null || finTxnConvertRate.compareTo(BigDecimal.ZERO) <= 0) {
      finTxnConvertRate = BigDecimal.ONE;
    }
    if (finTxnAmount == null || finTxnAmount.compareTo(BigDecimal.ZERO) == 0) {
      finTxnAmount = paymentAmount.multiply(finTxnConvertRate);
    }
    // This code commented due to fix in bug 17829
    // else if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) != 0) {
    // // Correct exchange rate for rounding that occurs in UI
    // finTxnConvertRate = finTxnAmount.divide(paymentAmount, MathContext.DECIMAL64);
    // }

    newPayment.setFinancialTransactionConvertRate(finTxnConvertRate);
    newPayment.setFinancialTransactionAmount(finTxnAmount);

    OBDal.getInstance().save(newPayment);

    return newPayment;
  }

  public FIN_PaymentDetail getNewPaymentDetail(FIN_Payment payment,
      FIN_PaymentScheduleDetail paymentScheduleDetail, BigDecimal paymentDetailAmount,
      BigDecimal writeoffAmount, boolean isRefund, GLItem glitem) {
    try {
      OBContext.setAdminMode(true);
      final FIN_PaymentDetail newPaymentDetail = OBProvider.getInstance().get(
          FIN_PaymentDetail.class);
      List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
      newPaymentDetail.setFinPayment(payment);
      newPaymentDetail.setOrganization(payment.getOrganization());
      newPaymentDetail.setClient(payment.getClient());
      newPaymentDetail.setAmount(paymentDetailAmount);
      newPaymentDetail.setWriteoffAmount(writeoffAmount);
      newPaymentDetail.setRefund(isRefund);
      newPaymentDetail.setGLItem(glitem);
      newPaymentDetail.setPrepayment(glitem == null
          && paymentScheduleDetail.getInvoicePaymentSchedule() == null);

      paymentDetails.add(newPaymentDetail);
      payment.setFINPaymentDetailList(paymentDetails);
      payment.setWriteoffAmount(payment.getWriteoffAmount().add(writeoffAmount));

      List<FIN_PaymentScheduleDetail> paymentScheduleDetails = newPaymentDetail
          .getFINPaymentScheduleDetailList();
      paymentScheduleDetail.setPaymentDetails(newPaymentDetail);
      paymentScheduleDetails.add(paymentScheduleDetail);
      newPaymentDetail.setFINPaymentScheduleDetailList(paymentScheduleDetails);

      OBDal.getInstance().save(payment);
      OBDal.getInstance().save(newPaymentDetail);
      OBDal.getInstance().save(paymentScheduleDetail);
      OBDal.getInstance().flush();

      return newPaymentDetail;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public FIN_PaymentScheduleDetail getNewPaymentScheduleDetail(Organization organization,
      BigDecimal amount) {
    final FIN_PaymentScheduleDetail newPaymentScheduleDetail = OBProvider.getInstance().get(
        FIN_PaymentScheduleDetail.class);
    newPaymentScheduleDetail.setOrganization(organization);
    // As '0' is not a valid organization for transactions we can assume that organization client is
    // transaction client
    newPaymentScheduleDetail.setClient(organization.getClient());
    newPaymentScheduleDetail.setAmount(amount);

    OBDal.getInstance().save(newPaymentScheduleDetail);
    // OBDal.getInstance().flush();

    return newPaymentScheduleDetail;
  }

  /**
   * Returns a new FIN_PaymentScheduleDetail for the given accounting dimensions
   * 
   * @param organization
   * @param amount
   * @param businessPartner
   *          accounting dimension
   * @param product
   *          accounting dimension
   * @param project
   *          accounting dimension
   * @param campaign
   *          accounting dimension
   * @param activity
   *          accounting dimension
   * @param salesRegion
   *          accounting dimension
   * @return
   */
  public FIN_PaymentScheduleDetail getNewPaymentScheduleDetail(Organization organization,
      BigDecimal amount, BusinessPartner businessPartner, Product product, Project project,
      Campaign campaign, ABCActivity activity, SalesRegion salesRegion) {
    final FIN_PaymentScheduleDetail psd = getNewPaymentScheduleDetail(organization, amount);
    psd.setBusinessPartner(businessPartner);
    psd.setProduct(product);
    psd.setProject(project);
    psd.setSalesCampaign(campaign);
    psd.setActivity(activity);
    psd.setSalesRegion(salesRegion);
    return psd;
  }

  public FIN_PaymentPropDetail getNewPaymentProposalDetail(Organization organization,
      FIN_PaymentProposal paymentProposal, FIN_PaymentScheduleDetail paymentScheduleDetail,
      BigDecimal amount, BigDecimal writeoffamount, GLItem glitem) {
    final FIN_PaymentPropDetail newPaymentProposalDetail = OBProvider.getInstance().get(
        FIN_PaymentPropDetail.class);
    newPaymentProposalDetail.setOrganization(organization);
    newPaymentProposalDetail.setAmount(amount);
    if (writeoffamount != null)
      newPaymentProposalDetail.setWriteoffAmount(writeoffamount);
    if (glitem != null)
      newPaymentProposalDetail.setGLItem(glitem);
    newPaymentProposalDetail.setFINPaymentScheduledetail(paymentScheduleDetail);

    List<FIN_PaymentPropDetail> paymentProposalDetails = paymentProposal
        .getFINPaymentPropDetailList();
    paymentProposalDetails.add(newPaymentProposalDetail);
    paymentProposal.setFINPaymentPropDetailList(paymentProposalDetails);
    newPaymentProposalDetail.setFinPaymentProposal(paymentProposal);

    OBDal.getInstance().save(newPaymentProposalDetail);
    OBDal.getInstance().save(paymentProposal);
    OBDal.getInstance().flush();

    return newPaymentProposalDetail;
  }

  public FIN_FinaccTransaction getFinancialTransaction(FIN_Payment payment) {
    FIN_FinaccTransaction transaction = FIN_Utility.getOneInstance(FIN_FinaccTransaction.class,
        new Value(FIN_FinaccTransaction.PROPERTY_FINPAYMENT, payment));
    if (transaction == null) {
      transaction = getNewFinancialTransaction(
          payment.getOrganization(),
          payment.getAccount(),
          TransactionsDao.getTransactionMaxLineNo(payment.getAccount()) + 10,
          payment,
          payment.getDescription(),
          payment.getPaymentDate(),
          null,
          "RPPC",
          FIN_Utility.getDepositAmount(payment.isReceipt(), payment.getFinancialTransactionAmount()),
          FIN_Utility.getPaymentAmount(payment.isReceipt(), payment.getFinancialTransactionAmount()),
          payment.getProject(), payment.getSalesCampaign(), payment.getActivity(), payment
              .isReceipt() ? "BPD" : "BPW", payment.getPaymentDate(), payment.getCurrency(),
          payment.getFinancialTransactionConvertRate(), payment.getAmount());
    }
    return transaction;
  }

  @Deprecated
  public FIN_FinaccTransaction getNewFinancialTransaction(Organization organization,
      Currency currency, FIN_FinancialAccount account, Long line, FIN_Payment payment,
      String description, Date accountingDate, GLItem glItem, String status,
      BigDecimal depositAmount, BigDecimal paymentAmount, Project project, Campaign campaing,
      ABCActivity activity, String transactionType, Date statementDate) {
    return getNewFinancialTransaction(organization, account, line, payment, description,
        accountingDate, glItem, status, depositAmount, paymentAmount, project, campaing, activity,
        transactionType, statementDate, null, null, null);
  }

  public FIN_FinaccTransaction getNewFinancialTransaction(Organization organization,
      FIN_FinancialAccount account, Long line, FIN_Payment payment, String description,
      Date accountingDate, GLItem glItem, String status, BigDecimal depositAmount,
      BigDecimal paymentAmount, Project project, Campaign campaing, ABCActivity activity,
      String transactionType, Date statementDate, Currency paymentCurrency, BigDecimal convertRate,
      BigDecimal sourceAmount) {
    FIN_FinaccTransaction finTrans = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    finTrans.setActive(true);
    finTrans.setOrganization(organization);
    finTrans.setCurrency(account.getCurrency());
    finTrans.setAccount(account);
    finTrans.setLineNo(line);
    finTrans.setFinPayment(payment);
    String truncateDescription = null;
    if (description != null) {
      truncateDescription = (description.length() > 255) ? description.substring(0, 252)
          .concat("...").toString() : description.toString();
    }
    finTrans.setDescription(truncateDescription);
    finTrans.setDateAcct(accountingDate);
    finTrans.setGLItem(glItem);
    finTrans.setStatus(status);
    finTrans.setDepositAmount(depositAmount);
    finTrans.setPaymentAmount(paymentAmount);
    finTrans.setProject(project);
    finTrans.setSalesCampaign(campaing);
    finTrans.setActivity(activity);
    finTrans.setTransactionType(transactionType);
    finTrans.setTransactionDate(statementDate);
    if (paymentCurrency != null && !paymentCurrency.equals(finTrans.getCurrency())) {
      finTrans.setForeignCurrency(paymentCurrency);
      finTrans.setForeignConversionRate(convertRate);
      finTrans.setForeignAmount(sourceAmount);
    }

    OBDal.getInstance().save(finTrans);
    OBDal.getInstance().flush();

    return finTrans;
  }

  public FIN_FinaccTransaction getNewFinancialTransaction(Organization organization,
      FIN_FinancialAccount account, Long line, FIN_Payment payment, String description,
      Date accountingDate, GLItem glItem, String status, BigDecimal depositAmount,
      BigDecimal paymentAmount, Project project, Campaign campaing, ABCActivity activity,
      String transactionType, Date statementDate, Currency paymentCurrency, BigDecimal convertRate,
      BigDecimal sourceAmount, BusinessPartner businessPartner, Product product,
      SalesRegion salesRegion) {
    final FIN_FinaccTransaction finTrans = getNewFinancialTransaction(organization, account, line,
        payment, description, accountingDate, glItem, status, depositAmount, paymentAmount,
        project, campaing, activity, transactionType, statementDate, paymentCurrency, convertRate,
        sourceAmount);
    finTrans.setBusinessPartner(businessPartner);
    finTrans.setProduct(product);
    finTrans.setSalesRegion(salesRegion);

    OBDal.getInstance().save(finTrans);
    OBDal.getInstance().flush();

    return finTrans;
  }

  public FIN_Reconciliation getNewReconciliation(Organization org, FIN_FinancialAccount account,
      String documentNo, DocumentType docType, Date dateTo, Date statementDate,
      BigDecimal startingBalance, BigDecimal endingBalance, String docStatus) {
    FIN_Reconciliation finRecon = OBProvider.getInstance().get(FIN_Reconciliation.class);
    finRecon.setOrganization(org);
    finRecon.setAccount(account);
    finRecon.setDocumentNo(documentNo);
    finRecon.setDocumentType(docType);
    finRecon.setEndingDate(dateTo);
    finRecon.setTransactionDate(statementDate);
    finRecon.setDocumentStatus(docStatus);
    finRecon.setStartingbalance(startingBalance);
    finRecon.setEndingBalance(endingBalance);

    OBDal.getInstance().save(finRecon);
    OBDal.getInstance().flush();

    return finRecon;

  }

  public PaymentRun getNewPaymentRun(String sourceType, PaymentExecutionProcess executionProcess,
      Organization organization) {
    PaymentRun paymentRun = OBProvider.getInstance().get(PaymentRun.class);
    paymentRun.setStatus("P");
    paymentRun.setOrganization(organization);
    paymentRun.setPaymentExecutionProcess(executionProcess);
    paymentRun.setSourceOfTheExecution(sourceType);

    OBDal.getInstance().save(paymentRun);
    OBDal.getInstance().flush();
    return paymentRun;
  }

  public PaymentRunPayment getNewPaymentRunPayment(PaymentRun paymentRun, FIN_Payment payment) {
    PaymentRunPayment paymentRunPayment = OBProvider.getInstance().get(PaymentRunPayment.class);
    paymentRunPayment.setPaymentRun(paymentRun);
    paymentRunPayment.setPayment(payment);
    paymentRunPayment.setOrganization(paymentRun.getOrganization());
    paymentRunPayment.setResult("P");

    List<PaymentRunPayment> paymentRunPayments = paymentRun.getFinancialMgmtPaymentRunPaymentList();
    paymentRunPayments.add(paymentRunPayment);
    paymentRun.setFinancialMgmtPaymentRunPaymentList(paymentRunPayments);

    OBDal.getInstance().save(paymentRunPayment);
    OBDal.getInstance().save(paymentRun);
    OBDal.getInstance().flush();
    return paymentRunPayment;
  }

  public PaymentRunParameter getNewPaymentRunParameter(PaymentRun paymentRun,
      PaymentExecutionProcessParameter parameter, String value) {
    PaymentRunParameter paymentRunParameter = OBProvider.getInstance().get(
        PaymentRunParameter.class);
    paymentRunParameter.setPaymentRun(paymentRun);
    paymentRunParameter.setOrganization(paymentRun.getOrganization());
    paymentRunParameter.setPaymentExecutionProcessParameter(parameter);
    if ("CHECK".equals(parameter.getInputType()))
      paymentRunParameter.setValueOfTheCheck("Y".equals(value));
    else if ("TEXT".equals(parameter.getInputType()))
      paymentRunParameter.setValueOfTheTextParameter(value);

    List<PaymentRunParameter> paymentRunParameters = paymentRun
        .getFinancialMgmtPaymentRunParameterList();
    paymentRunParameters.add(paymentRunParameter);
    paymentRun.setFinancialMgmtPaymentRunParameterList(paymentRunParameters);

    OBDal.getInstance().save(paymentRunParameter);
    OBDal.getInstance().save(paymentRun);
    OBDal.getInstance().flush();
    return paymentRunParameter;

  }

  public void duplicateScheduleDetail(FIN_PaymentScheduleDetail paymentScheduleDetail,
      BigDecimal writeoffAmount) {
    final FIN_PaymentScheduleDetail newPaymentScheduleDetail = (FIN_PaymentScheduleDetail) DalUtil
        .copy(paymentScheduleDetail);
    newPaymentScheduleDetail.setAmount(writeoffAmount);
    OBDal.getInstance().save(newPaymentScheduleDetail);
    OBDal.getInstance().flush();
  }

  public List<FIN_PaymentPropDetail> getOrderedPaymentProposalDetails(
      FIN_PaymentProposal paymentProposal) {

    final StringBuilder whereClause = new StringBuilder();

    OBContext.setAdminMode();
    try {

      whereClause.append(" as ppd ");
      whereClause.append(" left outer join ppd.fINPaymentScheduledetail as psd");
      whereClause.append(" left outer join psd.invoicePaymentSchedule as ips");
      whereClause.append(" left outer join ips.invoice as inv");
      whereClause.append(" left outer join psd.orderPaymentSchedule as ops");
      whereClause.append(" left outer join ops.order as ord");
      whereClause.append(" where ppd.finPaymentProposal.id='");
      whereClause.append(paymentProposal.getId());
      whereClause.append("' ");
      whereClause.append(" order by COALESCE (inv.businessPartner, ord.businessPartner)");

      final OBQuery<FIN_PaymentPropDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentPropDetail.class, whereClause.toString());

      return obqPSD.list();

    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public FieldProvider[] getReconciliationDetailReport(VariablesSecureApp vars, String strDate,
      String strReconID) {
    final StringBuilder hsqlScript = new StringBuilder();

    OBContext.setAdminMode();
    try {
      hsqlScript.append(" as recon ");
      hsqlScript.append(" where recon.id='");
      hsqlScript.append(strReconID);
      hsqlScript.append("'");
      final OBQuery<FIN_Reconciliation> obqRecon = OBDal.getInstance().createQuery(
          FIN_Reconciliation.class, hsqlScript.toString());

      List<FIN_Reconciliation> obqRecList = obqRecon.list();
      FIN_Reconciliation[] FIN_Reconcile = new FIN_Reconciliation[0];
      FIN_Reconcile = obqRecList.toArray(FIN_Reconcile);

      FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(obqRecList);
      for (int i = 0; i < data.length; i++) {
        FieldProviderFactory.setField(data[i], "FIN_RECONCILIATION_ID", FIN_Reconcile[i].getId());
        FieldProviderFactory.setField(data[i], "ENDDATE",
            Utility.formatDate(FIN_Reconcile[i].getEndingDate(), vars.getJavaDateFormat()));
        FieldProviderFactory.setField(data[i], "BPARTNER", "Account Balance in Openbravo");
        FieldProviderFactory.setField(data[i], "REFERENCE", "");
        FieldProviderFactory.setField(data[i], "STARTINGBALANCE", FIN_Reconcile[i]
            .getStartingbalance().toString());
      }
      return data;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public FieldProvider[] getReconciliationSummaryReport(VariablesSecureApp vars, String strDate,
      String strReconID) {
    final StringBuilder hsqlScript = new StringBuilder();

    OBContext.setAdminMode();
    try {
      hsqlScript.append(" as recon ");
      hsqlScript.append(" where recon.id='");
      hsqlScript.append(strReconID);
      hsqlScript.append("'");
      final OBQuery<FIN_Reconciliation> obqRecon = OBDal.getInstance().createQuery(
          FIN_Reconciliation.class, hsqlScript.toString());
      List<FIN_Reconciliation> obqRecList = obqRecon.list();
      FIN_Reconciliation[] FIN_Reconcile = new FIN_Reconciliation[0];
      FIN_Reconcile = obqRecList.toArray(FIN_Reconcile);

      FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(obqRecList);
      for (int i = 0; i < data.length; i++) {
        FieldProviderFactory.setField(data[i], "FIN_RECONCILIATION_ID", FIN_Reconcile[i].getId());
        FieldProviderFactory.setField(data[i], "ENDDATE",
            Utility.formatDate(FIN_Reconcile[i].getEndingDate(), vars.getJavaDateFormat()));
        FieldProviderFactory.setField(data[i], "ENDINGBALANCE", FIN_Reconcile[i].getEndingBalance()
            .toString());
        FieldProviderFactory.setField(data[i], "STARTINGBALANCE", FIN_Reconcile[i]
            .getStartingbalance().toString());
      }
      return data;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public int getTrxGridRowCount(FIN_FinancialAccount financialAccount, Boolean hideReconciledTrx,
      int maxrowspergridpage, int offset) {
    final StringBuilder whereClause = new StringBuilder();

    OBContext.setAdminMode();
    try {
      whereClause.append(" as fatrx ");
      whereClause.append(" left outer join fatrx.")
          .append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION).append(" as reconciliation");
      whereClause.append(" where fatrx.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
      whereClause.append(".id='");
      whereClause.append(financialAccount.getId());
      whereClause.append("'");
      if (hideReconciledTrx) {
        whereClause.append(" and (fatrx.").append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION)
            .append(" is null ");
        whereClause.append(" or reconciliation.").append(FIN_Reconciliation.PROPERTY_PROCESSED)
            .append(" = 'N') ");
      }
      final OBQuery<FIN_FinaccTransaction> obqFATrx = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString());
      obqFATrx.setFirstResult(offset);
      obqFATrx.setMaxResult(maxrowspergridpage);
      return obqFATrx.list().size();

    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public List<FIN_FinaccTransaction> getTrxGridRows(FIN_FinancialAccount financialAccount,
      Boolean hideReconciledTrx, int pageSize, int offset, String orderBy) {
    final StringBuilder whereClause = new StringBuilder();

    OBContext.setAdminMode();
    try {

      whereClause.append(" as fatrx ");
      whereClause.append(" left outer join fatrx.")
          .append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION).append(" as reconciliation");
      whereClause.append(" where fatrx.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
      whereClause.append(".id='");
      whereClause.append(financialAccount.getId());
      whereClause.append("'");
      if (hideReconciledTrx) {
        whereClause.append(" and (fatrx.").append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION)
            .append(" is null ");
        whereClause.append(" or reconciliation.").append(FIN_Reconciliation.PROPERTY_PROCESSED)
            .append(" = 'N') ");
      }
      whereClause.append(" order by ");
      whereClause.append(orderBy);
      if (!"".equals(orderBy))
        whereClause.append(", ");
      whereClause.append(" fatrx.").append(FIN_FinaccTransaction.PROPERTY_LINENO);
      final OBQuery<FIN_FinaccTransaction> obqFATrx = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString());
      obqFATrx.setFirstResult(offset);
      obqFATrx.setMaxResult(pageSize);
      return obqFATrx.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public FieldProvider[] getPaymentsNotDeposited(FIN_FinancialAccount account, Date fromDate,
      Date toDate, boolean isReceipt) {

    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    OBContext.setAdminMode();
    try {
      final StringBuilder whereClause = new StringBuilder();
      final List<Object> parameters = new ArrayList<Object>();

      whereClause.append(" as p ");
      whereClause.append(" where p.");
      whereClause.append(FIN_Payment.PROPERTY_ID);
      whereClause.append(" not in ");
      whereClause.append(" ( select coalesce(ft.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_FINPAYMENT);
      whereClause.append(".");
      whereClause.append(FIN_Payment.PROPERTY_ID);
      whereClause.append(", '-1') ");
      whereClause.append(" from ");
      whereClause.append(FIN_FinaccTransaction.TABLE_NAME);
      whereClause.append(" as ft");
      whereClause.append(" where ft.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
      whereClause.append(".id = ?) ");
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_ACCOUNT);
      whereClause.append(".id = ? ");
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_STATUS);
      whereClause.append(" IN ('RPR', 'PPM')");
      parameters.add(account.getId());
      parameters.add(account.getId());
      // IsReceipt
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_RECEIPT);
      whereClause.append(" = ");
      whereClause.append(isReceipt);

      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_AMOUNT);
      whereClause.append(" != ");
      whereClause.append(BigDecimal.ZERO);

      // From Date
      if (fromDate != null) {
        whereClause.append(" and p.");
        whereClause.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        whereClause.append(" >= ? ");
        parameters.add(fromDate);
      }
      // To Date
      if (toDate != null) {
        whereClause.append(" AND p.");
        whereClause.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        whereClause.append(" < ?");
        parameters.add(toDate);
      }
      // Order by date and payment no
      whereClause.append(" ORDER BY p.");
      whereClause.append(FIN_Payment.PROPERTY_PAYMENTDATE);
      whereClause.append(", p.");
      whereClause.append(FIN_Payment.PROPERTY_DOCUMENTNO);

      final OBQuery<FIN_Payment> obqP = OBDal.getInstance().createQuery(FIN_Payment.class,
          whereClause.toString(), parameters);

      List<FIN_Payment> paymentOBList = obqP.list();

      FIN_Payment[] FIN_Payments = new FIN_Payment[0];
      FIN_Payments = paymentOBList.toArray(FIN_Payments);
      FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(paymentOBList);

      for (int i = 0; i < data.length; i++) {
        Boolean paymentIsReceipt = FIN_Payments[i].isReceipt();

        BigDecimal depositAmt = FIN_Utility.getDepositAmount(paymentIsReceipt,
            FIN_Payments[i].getFinancialTransactionAmount());
        BigDecimal paymentAmt = FIN_Utility.getPaymentAmount(paymentIsReceipt,
            FIN_Payments[i].getFinancialTransactionAmount());
        BigDecimal foreignDepositAmt = FIN_Utility.getDepositAmount(paymentIsReceipt,
            FIN_Payments[i].getAmount());
        BigDecimal foreignPaymentAmt = FIN_Utility.getPaymentAmount(paymentIsReceipt,
            FIN_Payments[i].getAmount());

        final Currency foreignCurrency = FIN_Payments[i].getCurrency();

        FieldProviderFactory.setField(data[i], "paymentId", FIN_Payments[i].getId());
        FieldProviderFactory.setField(data[i], "paymentInfo", FIN_Payments[i].getDocumentNo()
            + " - "
            + ((FIN_Payments[i].getBusinessPartner() != null) ? FIN_Payments[i]
                .getBusinessPartner().getName() : "") + " - "
            + FIN_Payments[i].getCurrency().getISOCode());

        // Truncate description
        String description = FIN_Payments[i].getDescription();
        String truncateDescription = "";
        if (description != null) {
          truncateDescription = (description.length() > 57) ? description.substring(0, 54)
              .concat("...").toString() : description;
        }
        FieldProviderFactory.setField(data[i], "paymentDescription",
            (description != null && description.length() > 57) ? description : "");
        FieldProviderFactory.setField(data[i], "paymentDescriptionTrunc", truncateDescription);

        FieldProviderFactory.setField(data[i], "paymentDate",
            dateFormater.format(FIN_Payments[i].getPaymentDate()).toString());
        FieldProviderFactory.setField(data[i], "depositAmount", FIN_Utility
            .multiCurrencyAmountToDisplay(depositAmt, account.getCurrency(), foreignDepositAmt,
                foreignCurrency));
        FieldProviderFactory.setField(data[i], "paymentAmount", FIN_Utility
            .multiCurrencyAmountToDisplay(paymentAmt, account.getCurrency(), foreignPaymentAmt,
                foreignCurrency));

        FieldProviderFactory.setField(data[i], "rownum", "" + i);
      }

      return data;

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getPaymentProposalDetailAmount(FIN_PaymentScheduleDetail finPaymentScheduleDetail,
      FIN_PaymentProposal paymentProposal) {
    String amount = "";
    for (FIN_PaymentPropDetail propDetail : paymentProposal.getFINPaymentPropDetailList())
      if (propDetail.getFINPaymentScheduledetail() == finPaymentScheduleDetail)
        amount = propDetail.getAmount().toString();

    return amount;
  }

  @Deprecated
  public List<FIN_PaymentMethod> getFilteredPaymentMethods(String strFinancialAccountId,
      String strOrgId, boolean excludePaymentMethodWithoutAccount) {
    return getFilteredPaymentMethods(strFinancialAccountId, strOrgId,
        excludePaymentMethodWithoutAccount, PaymentDirection.EITHER);
  }

  public List<FIN_PaymentMethod> getFilteredPaymentMethods(String strFinancialAccountId,
      String strOrgId, boolean excludePaymentMethodWithoutAccount, PaymentDirection paymentDirection) {
    final OBCriteria<FIN_PaymentMethod> obc = OBDal.getInstance().createCriteria(
        FIN_PaymentMethod.class);
    obc.add(Restrictions.in("organization.id", OBContext.getOBContext()
        .getOrganizationStructureProvider().getNaturalTree(strOrgId)));
    obc.setFilterOnReadableOrganization(false);

    List<String> payMethods = new ArrayList<String>();
    if (strFinancialAccountId != null && !strFinancialAccountId.isEmpty()) {
      for (FinAccPaymentMethod finAccPayMethod : getObject(FIN_FinancialAccount.class,
          strFinancialAccountId).getFinancialMgmtFinAccPaymentMethodList()) {
        if (paymentDirection == PaymentDirection.EITHER) {
          payMethods.add(finAccPayMethod.getPaymentMethod().getId());
        } else if (paymentDirection == PaymentDirection.IN && finAccPayMethod.isPayinAllow()) {
          payMethods.add(finAccPayMethod.getPaymentMethod().getId());
        } else if (paymentDirection == PaymentDirection.OUT && finAccPayMethod.isPayoutAllow()) {
          payMethods.add(finAccPayMethod.getPaymentMethod().getId());
        }
        // else not valid for this type of payment
      }
      if (payMethods.isEmpty()) {
        return (new ArrayList<FIN_PaymentMethod>());
      }
      obc.add(Restrictions.in("id", payMethods));
    } else {
      if (excludePaymentMethodWithoutAccount) {

        final OBCriteria<FinAccPaymentMethod> obcExc = OBDal.getInstance().createCriteria(
            FinAccPaymentMethod.class);
        obcExc.createAlias(FinAccPaymentMethod.PROPERTY_ACCOUNT, "acc");
        obcExc.add(Restrictions.in("acc.organization.id", OBContext.getOBContext()
            .getOrganizationStructureProvider().getNaturalTree(strOrgId)));
        obcExc.setFilterOnReadableOrganization(false);
        for (FinAccPaymentMethod fapm : obcExc.list()) {
          payMethods.add(fapm.getPaymentMethod().getId());
        }
        if (payMethods.isEmpty()) {
          return (new ArrayList<FIN_PaymentMethod>());
        }
        obc.add(Restrictions.in("id", payMethods));
      }
      if (paymentDirection == PaymentDirection.IN) {
        obc.add(Restrictions.eq(FIN_PaymentMethod.PROPERTY_PAYINALLOW, true));
      } else if (paymentDirection == PaymentDirection.OUT) {
        obc.add(Restrictions.eq(FIN_PaymentMethod.PROPERTY_PAYOUTALLOW, true));
      }
    }

    return obc.list();
  }

  @Deprecated
  public List<FIN_FinancialAccount> getFilteredFinancialAccounts(String strPaymentMethodId,
      String strOrgId, String strCurrencyId) {
    return getFilteredFinancialAccounts(strPaymentMethodId, strOrgId, strCurrencyId,
        PaymentDirection.EITHER);
  }

  public List<FIN_FinancialAccount> getFilteredFinancialAccounts(String strPaymentMethodId,
      String strOrgId, String strCurrencyId, PaymentDirection paymentDirection) {
    final OBCriteria<FIN_FinancialAccount> obc = OBDal.getInstance().createCriteria(
        FIN_FinancialAccount.class, "acc");
    obc.add(Restrictions.in("organization.id", OBContext.getOBContext()
        .getOrganizationStructureProvider().getNaturalTree(strOrgId)));
    obc.setFilterOnReadableOrganization(false);

    Currency requiredCurrency = null;
    if (strCurrencyId != null && !strCurrencyId.isEmpty()) {
      DetachedCriteria multiCurrAllowed = DetachedCriteria.forEntityName(
          FinAccPaymentMethod.ENTITY_NAME, "fapm").add(
          Restrictions.eqProperty(FinAccPaymentMethod.PROPERTY_ACCOUNT + ".id", "acc.id"));
      if (paymentDirection == PaymentDirection.IN || paymentDirection == PaymentDirection.EITHER) {
        multiCurrAllowed.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYINISMULTICURRENCY,
            true));
      }
      if (paymentDirection == PaymentDirection.OUT || paymentDirection == PaymentDirection.EITHER) {
        multiCurrAllowed.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYOUTISMULTICURRENCY,
            true));
      }
      requiredCurrency = OBDal.getInstance().get(Currency.class, strCurrencyId);
      obc.add(Restrictions.or(
          Restrictions.eq(FIN_FinancialAccount.PROPERTY_CURRENCY, requiredCurrency),
          Subqueries.exists(multiCurrAllowed.setProjection(Projections.id()))));
    }

    if (strPaymentMethodId != null && !strPaymentMethodId.isEmpty()) {
      List<FinAccPaymentMethod> finAccsMethods = getObject(FIN_PaymentMethod.class,
          strPaymentMethodId).getFinancialMgmtFinAccPaymentMethodList();

      if (finAccsMethods.isEmpty()) {
        return (new ArrayList<FIN_FinancialAccount>());
      }
      ExpressionForFinAccPayMethod exp = new ExpressionForFinAccPayMethod();

      for (FinAccPaymentMethod finAccPayMethod : finAccsMethods) {
        boolean validPaymentDirection = true;
        if (paymentDirection == PaymentDirection.IN) {
          validPaymentDirection = finAccPayMethod.isPayinAllow();
        } else if (paymentDirection == PaymentDirection.OUT) {
          validPaymentDirection = finAccPayMethod.isPayoutAllow();
        }

        boolean validCurrency = true;
        if (requiredCurrency != null) {
          boolean multiCurrencyAllowed = false;
          if (paymentDirection == PaymentDirection.IN) {
            multiCurrencyAllowed = finAccPayMethod.isPayinIsMulticurrency();
          } else if (paymentDirection == PaymentDirection.OUT) {
            multiCurrencyAllowed = finAccPayMethod.isPayoutIsMulticurrency();
          } else if (paymentDirection == PaymentDirection.EITHER) {
            multiCurrencyAllowed = finAccPayMethod.isPayinIsMulticurrency()
                || finAccPayMethod.isPayoutIsMulticurrency();
          }

          validCurrency = multiCurrencyAllowed
              || requiredCurrency.equals(finAccPayMethod.getAccount().getCurrency());
        }

        if (validPaymentDirection && validCurrency) {
          exp.addFinAccPaymentMethod(finAccPayMethod);
        }

      }

      Criterion crit = exp.getCriterion();
      if (crit != null) {
        obc.add(crit);
      }
    }
    return obc.list();
  }

  private static class ExpressionForFinAccPayMethod {

    private int MAX = 999;

    private Criterion compoundexp = null;
    List<String> finAccs = new ArrayList<String>();

    public void addFinAccPaymentMethod(FinAccPaymentMethod finAccPayMethod) {
      finAccs.add(finAccPayMethod.getAccount().getId());
      if (finAccs.size() >= MAX) {
        refresh();
      }
    }

    public Criterion getCriterion() {
      if (finAccs.size() > 0) {
        refresh();
      }
      return compoundexp;
    }

    private void refresh() {
      // finAccs.size() must be > 0
      if (compoundexp == null) {
        compoundexp = Restrictions.in("id", finAccs);
      } else {
        compoundexp = Restrictions.or(compoundexp, Restrictions.in("id", finAccs));
      }
      finAccs = new ArrayList<String>();
    }
  }

  public FinAccPaymentMethod getFinancialAccountPaymentMethod(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod) {
    final OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance().createCriteria(
        FinAccPaymentMethod.class);
    obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, account));
    obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
    obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACTIVE, true));
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    return obc.list().get(0);
  }

  public boolean isAutomatedExecutionPayment(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod, boolean isReceipt) {
    FinAccPaymentMethod finAccPaymentMethod = getFinancialAccountPaymentMethod(account,
        paymentMethod);
    return "A".equals(isReceipt ? finAccPaymentMethod.getPayinExecutionType() : finAccPaymentMethod
        .getPayoutExecutionType());
  }

  public boolean hasNotDeferredExecutionProcess(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod, Boolean isReceipt) {
    FinAccPaymentMethod finAccPaymentMethod = getFinancialAccountPaymentMethod(account,
        paymentMethod);
    return (isReceipt ? (finAccPaymentMethod.getPayinExecutionProcess() != null && !finAccPaymentMethod
        .isPayinDeferred())
        : (finAccPaymentMethod.getPayoutExecutionProcess() != null && !finAccPaymentMethod
            .isPayoutDeferred()));
  }

  public PaymentExecutionProcess getExecutionProcess(FIN_Payment payment) {
    return getExecutionProcess(payment.getAccount(), payment.getPaymentMethod(),
        payment.isReceipt());
  }

  public PaymentExecutionProcess getExecutionProcess(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod, Boolean receipt) {
    FinAccPaymentMethod finAccPaymentMethod = getFinancialAccountPaymentMethod(account,
        paymentMethod);
    return receipt ? finAccPaymentMethod.getPayinExecutionProcess() : finAccPaymentMethod
        .getPayoutExecutionProcess();
  }

  public boolean isAutomaticExecutionProcess(PaymentExecutionProcess executionProcess) {
    List<PaymentExecutionProcessParameter> parameters = executionProcess
        .getFinancialMgmtPaymentExecutionProcessParameterList();
    for (PaymentExecutionProcessParameter parameter : parameters) {
      if ("CONSTANT".equals(parameter.getParameterType())
          && parameter.getDefaultTextValue() != null && parameter.getDefaultTextValue() != "")
        return false;
      else if ("IN".equals(parameter.getParameterType())) {
        if ("CHECK".equals(parameter.getInputType())
            && (parameter.getDefaultValueForFlag() == null || "".equals(parameter
                .getDefaultValueForFlag())))
          return false;
        else if ("TEXT".equals(parameter.getInputType())
            && (parameter.getDefaultTextValue() == null || "".equals(parameter
                .getDefaultTextValue())))
          return false;
      }
    }
    return true;
  }

  public List<PaymentExecutionProcessParameter> getInPaymentExecutionParameters(
      PaymentExecutionProcess executionProcess) {
    OBCriteria<PaymentExecutionProcessParameter> obc = OBDal.getInstance().createCriteria(
        PaymentExecutionProcessParameter.class);
    obc.add(Restrictions.eq(PaymentExecutionProcessParameter.PROPERTY_PAYMENTEXECUTIONPROCESS,
        executionProcess));
    obc.add(Restrictions.eq(PaymentExecutionProcessParameter.PROPERTY_PARAMETERTYPE, "IN"));
    return obc.list();
  }

  public List<FIN_Payment> getPaymentProposalPayments(FIN_PaymentProposal paymentProposal) {
    List<FIN_Payment> paymentsInProposal = new ArrayList<FIN_Payment>();
    for (FIN_PaymentPropDetail proposalDetail : paymentProposal.getFINPaymentPropDetailList())
      if ("RPAE".equals(proposalDetail.getFINPaymentScheduledetail().getPaymentDetails()
          .getFinPayment().getStatus()))
        paymentsInProposal.add(proposalDetail.getFINPaymentScheduledetail().getPaymentDetails()
            .getFinPayment());

    return paymentsInProposal;
  }

  /**
   * This method returns list of Payments that are in Awaiting Execution status and filtered by the
   * following parameters.
   * 
   * @param organizationId
   *          Organization
   * @param paymentMethodId
   *          Payment Method used for the payment.
   * @param financialAccountId
   *          Financial Account used for the payment.
   * @param dateFrom
   *          Optional. Filters payments made after the specified date.
   * @param dateTo
   *          Optional. Filters payments made before the specified date.
   * @param offset
   *          Starting register number.
   * @param pageSize
   *          Limited the max number of results.
   * @param strOrderByProperty
   *          Property used for ordering the results.
   * @param strAscDesc
   *          if true order by asc, if false order by desc
   * @param isReceipt
   *          if true sales, if false purchase
   * @return Filtered Payment list.
   */
  public List<FIN_Payment> getPayExecRowCount(String organizationId, String paymentMethodId,
      String financialAccountId, Date dateFrom, Date dateTo, int offset, int pageSize,
      String strOrderByProperty, String strAscDesc, boolean isReceipt) {

    List<FIN_Payment> emptyList = new ArrayList<FIN_Payment>();
    if (organizationId == null || organizationId.isEmpty()) {
      return emptyList;
    }

    OBContext.setAdminMode();
    try {

      FIN_PaymentMethod obPayMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
          paymentMethodId);
      FIN_FinancialAccount obFinAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
          financialAccountId);

      OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
      obcPayment.add(Restrictions.in("organization.id", OBContext.getOBContext()
          .getOrganizationStructureProvider().getParentTree(organizationId, true)));
      obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_STATUS, "RPAE"));
      obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_PAYMENTMETHOD, obPayMethod));
      obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_ACCOUNT, obFinAccount));
      obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
      if (dateFrom != null)
        obcPayment.add(Restrictions.ge(FIN_Payment.PROPERTY_PAYMENTDATE, dateFrom));
      if (dateTo != null)
        obcPayment.add(Restrictions.lt(FIN_Payment.PROPERTY_PAYMENTDATE, dateTo));

      boolean ascDesc = true;
      if (strAscDesc != null && !strAscDesc.isEmpty())
        ascDesc = "asc".equalsIgnoreCase(strAscDesc);
      if (strOrderByProperty != null && !strOrderByProperty.isEmpty())
        obcPayment.addOrderBy(strOrderByProperty, ascDesc);
      obcPayment.setFirstResult(offset);
      obcPayment.setMaxResults(pageSize);

      return obcPayment.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<FIN_Payment> getPendingExecutionPayments(String strInvoiceId) {
    List<FIN_Payment> payments = new ArrayList<FIN_Payment>();
    List<FIN_PaymentSchedule> paySchedList = new AdvPaymentMngtDao().getObject(Invoice.class,
        strInvoiceId).getFINPaymentScheduleList();
    OBCriteria<FIN_PaymentScheduleDetail> psdCriteria = OBDal.getInstance().createCriteria(
        FIN_PaymentScheduleDetail.class);
    if (!paySchedList.isEmpty())
      psdCriteria.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
          paySchedList));
    for (FIN_PaymentScheduleDetail psd : psdCriteria.list()) {
      if (psd.getPaymentDetails() != null) {
        FIN_Payment payment = psd.getPaymentDetails().getFinPayment();
        if ("RPAE".equals(payment.getStatus())
            && hasNotDeferredExecutionProcess(payment.getAccount(), payment.getPaymentMethod(),
                payment.isReceipt()))
          payments.add(payment);
      }
    }

    return payments;
  }

  public void setPaymentExecuting(FIN_Payment payment, boolean executing) {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Restrictions.eq(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENT, payment));
    List<APRMPendingPaymentFromInvoice> pendingPayments = ppfiCriteria.list();
    if (pendingPayments != null && pendingPayments.size() > 0) {
      APRMPendingPaymentFromInvoice pendingPayment = pendingPayments.get(0);
      pendingPayment.setProcessNow(executing);
      OBDal.getInstance().flush();
      OBDal.getInstance().save(pendingPayment);
    }
  }

  public boolean isPaymentBeingExecuted(FIN_Payment payment) {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Restrictions.eq(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENT, payment));
    List<APRMPendingPaymentFromInvoice> pendingPayments = ppfiCriteria.list();
    if (pendingPayments != null && pendingPayments.size() > 0) {
      return pendingPayments.get(0).isProcessNow();
    } else
      return false;

  }

  public void removeFromExecutionPending(FIN_Payment payment) {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Restrictions.eq(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENT, payment));
    List<APRMPendingPaymentFromInvoice> pendingPayments = ppfiCriteria.list();
    OBDal.getInstance().remove(pendingPayments.get(0));
    OBDal.getInstance().flush();
  }

  public List<APRMPendingPaymentFromInvoice> getPendingPayments() {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Restrictions.eq(APRMPendingPaymentFromInvoice.PROPERTY_PROCESSNOW, false));
    ppfiCriteria.addOrderBy(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENTEXECUTIONPROCESS, false);
    ppfiCriteria.addOrderBy(APRMPendingPaymentFromInvoice.PROPERTY_ORGANIZATION, false);
    return ppfiCriteria.list();
  }

  public BigDecimal getCustomerCredit(BusinessPartner bp, boolean isReceipt) {
    BigDecimal creditAmount = BigDecimal.ZERO;
    for (FIN_Payment payment : getCustomerPaymentsWithCredit(bp, isReceipt))
      creditAmount = creditAmount.add(payment.getGeneratedCredit()).subtract(
          payment.getUsedCredit());
    return creditAmount;
  }

  public List<FIN_Payment> getCustomerPaymentsWithCredit(BusinessPartner bp, boolean isReceipt) {
    OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
    obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_BUSINESSPARTNER, bp));
    obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
    obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_GENERATEDCREDIT, BigDecimal.ZERO));
    obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_STATUS, "RPAP"));
    obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_STATUS, "RPVOID"));
    obcPayment.add(Restrictions.neProperty(FIN_Payment.PROPERTY_GENERATEDCREDIT,
        FIN_Payment.PROPERTY_USEDCREDIT));
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, true);
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_DOCUMENTNO, true);
    return obcPayment.list();
  }

  /**
   * Returns the list of credit payments for the selected business partner that belongs to the legal
   * entity's natural tree of the given organization
   * 
   * @param org
   * @param bp
   * @param isReceipt
   * @return
   */
  public List<FIN_Payment> getCustomerPaymentsWithCredit(Organization org, BusinessPartner bp,
      boolean isReceipt) {
    try {
      OBContext.setAdminMode(true);
      OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
      obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_BUSINESSPARTNER, bp));
      obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
      obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_GENERATEDCREDIT, BigDecimal.ZERO));
      obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_STATUS, "RPAP"));
      obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_STATUS, "RPVOID"));
      obcPayment.add(Restrictions.neProperty(FIN_Payment.PROPERTY_GENERATEDCREDIT,
          FIN_Payment.PROPERTY_USEDCREDIT));
      final Organization legalEntity = FIN_Utility.getLegalEntityOrg(org);
      obcPayment.add(Restrictions.in("organization.id", OBContext.getOBContext()
          .getOrganizationStructureProvider().getNaturalTree(legalEntity.getId())));
      obcPayment.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, true);
      obcPayment.addOrderBy(FIN_Payment.PROPERTY_DOCUMENTNO, true);
      return obcPayment.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<FIN_Payment> getCustomerPaymentsWithUsedCredit(BusinessPartner bp, Boolean isReceipt) {
    OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
    obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_BUSINESSPARTNER, bp));
    obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
    obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_GENERATEDCREDIT, BigDecimal.ZERO));
    obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_USEDCREDIT, BigDecimal.ZERO));
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, false);
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_DOCUMENTNO, false);
    return obcPayment.list();
  }

  public Currency getFinancialAccountCurrency(String strFinancialAccountId) {
    if (strFinancialAccountId != null && !strFinancialAccountId.isEmpty()) {
      final FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
          strFinancialAccountId);
      return account.getCurrency();
    }
    return null;
  }

  public FIN_FinancialAccount getDefaultFinancialAccountFor(String strOrgId) {
    final OBCriteria<FIN_FinancialAccount> obc = OBDal.getInstance().createCriteria(
        FIN_FinancialAccount.class, "acc");
    obc.add(Restrictions.in("organization.id", OBContext.getOBContext()
        .getOrganizationStructureProvider().getNaturalTree(strOrgId)));
    obc.setFilterOnReadableOrganization(false);

    obc.add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_DEFAULT, true));

    final List<FIN_FinancialAccount> defaultAccounts = obc.list();
    if (defaultAccounts.size() > 0) {
      return defaultAccounts.get(0);
    } else {
      return null;
    }
  }

  public boolean existsAPRMReadyPreference() {
    OBCriteria<Preference> obcPreference = OBDal.getInstance().createCriteria(Preference.class);
    obcPreference.setFilterOnReadableClients(false);
    obcPreference.setFilterOnReadableOrganization(false);
    obcPreference.add(Restrictions.eq(Preference.PROPERTY_ATTRIBUTE, "APRM_Ready"));

    return obcPreference.count() > 0;
  }

  /**
   * Create a preference to be able to determine that the instance is ready to use APRM.
   */
  public void createAPRMReadyPreference() {
    Organization org0 = OBDal.getInstance().get(Organization.class, "0");
    Client client0 = OBDal.getInstance().get(Client.class, "0");

    Preference newPref = OBProvider.getInstance().get(Preference.class);
    newPref.setClient(client0);
    newPref.setOrganization(org0);
    newPref.setPropertyList(false);
    newPref.setAttribute("APRM_Ready");

    OBDal.getInstance().save(newPref);
  }

  /**
   * Gets the oldest credit payment for the given parameters
   * 
   * @param organization
   * @param businessPartner
   * @param amount
   * @param currency
   * @param isReceipt
   * @param toDate
   * @return if exists, returns the credit payment, else returns null
   */
  public FIN_Payment getCreditPayment(final Organization organization,
      final BusinessPartner businessPartner, final BigDecimal amount, final Currency currency,
      final boolean isReceipt, final Date toDate) {
    try {
      OBContext.setAdminMode(true);
      OBCriteria<FIN_Payment> obcFinPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
      obcFinPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_PROCESSED, true));
      obcFinPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_BUSINESSPARTNER, businessPartner));
      obcFinPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_GENERATEDCREDIT, amount));
      obcFinPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_USEDCREDIT, BigDecimal.ZERO));
      obcFinPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_CURRENCY, currency));
      obcFinPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
      obcFinPayment.add(Restrictions.le(FIN_Payment.PROPERTY_PAYMENTDATE, toDate));
      obcFinPayment.add(Restrictions.in("organization.id", OBContext.getOBContext()
          .getOrganizationStructureProvider().getNaturalTree(organization.getId())));
      obcFinPayment.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, true);
      obcFinPayment.setMaxResults(1);

      final List<FIN_Payment> finPayments = obcFinPayment.list();
      if (finPayments.size() > 0) {
        return finPayments.get(0);
      } else {
        return null;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Gets the oldest credit payment for the given invoice
   * 
   * @param invoice
   * @return if exists, returns the credit payment, else returns null
   */
  public FIN_Payment getCreditPayment(final Invoice invoice) {
    if (invoice == null) {
      return null;
    } else {
      return getCreditPayment(invoice.getOrganization(), invoice.getBusinessPartner(),
          invoice.getGrandTotalAmount(), invoice.getCurrency(), invoice.isSalesTransaction(),
          invoice.getInvoiceDate());
    }
  }
}
