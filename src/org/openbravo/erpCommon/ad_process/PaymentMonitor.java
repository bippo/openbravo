package org.openbravo.erpCommon.ad_process;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.DebtPayment;
import org.openbravo.service.db.DalConnectionProvider;

public class PaymentMonitor {
  private static final Logger log4j = Logger.getLogger(PaymentMonitor.class);

  /**
   * Updates payment monitor information
   * 
   * Users of this method should check for existence of the PaymentMonitor property (disabling it)
   * to be able to provide the user with a relevant message.
   */
  public static void updateInvoice(Invoice invoice) {
    // Check for PaymentMonitor-disabling switch.
    try {
      try {
        Preferences.getPreferenceValue("PaymentMonitor", true, invoice.getClient(),
            invoice.getOrganization(), null, null, null);
        return;
      } catch (PropertyNotFoundException e) {
      }
    } catch (PropertyException e) {
      return;
    }
    OBContext.setAdminMode();
    try {
      List<DebtPayment> payments = invoice.getFinancialMgmtDebtPaymentList();
      BigDecimal paidAmount = BigDecimal.ZERO;
      BigDecimal overDueAmount = BigDecimal.ZERO;
      for (DebtPayment payment : payments) {
        if (payment.isPaymentComplete())
          paidAmount = paidAmount.add(getConvertedAmt(payment.getAmount(), payment.getCurrency()
              .getId(), invoice.getCurrency().getId(), invoice.getAccountingDate(), invoice
              .getClient().getId(), invoice.getOrganization().getId()));
        else {
          paidAmount = paidAmount.add(calculatePaidAmount(payment, invoice.getCurrency().getId(),
              invoice.getAccountingDate(), BigDecimal.ONE));
          overDueAmount = overDueAmount.add(calculateOverdueAmount(payment, invoice.getCurrency()
              .getId(), invoice.getAccountingDate(), BigDecimal.ONE));
        }
      }
      if (paidAmount.setScale(invoice.getCurrency().getStandardPrecision().intValue(),
          BigDecimal.ROUND_HALF_UP).compareTo(invoice.getGrandTotalAmount()) == 0) {
        invoice.setDaysTillDue(0L);
        invoice.setDueAmount(BigDecimal.ZERO);
        invoice.setPaymentComplete(true);
      } else {
        invoice.setDaysTillDue(getDaysTillDue(invoice));
        invoice.setPaymentComplete(false);
      }
      invoice.setTotalPaid(paidAmount.setScale(invoice.getCurrency().getStandardPrecision()
          .intValue(), BigDecimal.ROUND_HALF_UP));
      invoice.setDueAmount(overDueAmount.setScale(invoice.getCurrency().getStandardPrecision()
          .intValue(), BigDecimal.ROUND_HALF_UP));
      invoice.setOutstandingAmount(invoice.getGrandTotalAmount().subtract(invoice.getTotalPaid()));
      invoice.setLastCalculatedOnDate(new Date());
      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
    return;
  }

  static Long getDaysTillDue(Invoice invoice) {
    Long daysToDue = 0L;

    String whereClause = " as dp where dp.invoice.id = :invoice order by dp.dueDate";

    final OBQuery<DebtPayment> obqParameters = OBDal.getInstance().createQuery(DebtPayment.class,
        whereClause);
    obqParameters.setNamedParameter("invoice", invoice.getId());
    // For Background process execution at system level
    if (OBContext.getOBContext().isInAdministratorMode()) {
      obqParameters.setFilterOnReadableClients(false);
      obqParameters.setFilterOnReadableOrganization(false);
    }
    final List<DebtPayment> payments = obqParameters.list();
    ArrayList<Long> allDaysToDue = new ArrayList<Long>();
    for (DebtPayment payment : payments) {
      if (!payment.isPaymentComplete()) {
        Long paymentDue = getPaymentDaysToDue(payment);
        if (paymentDue != null)
          allDaysToDue.add(paymentDue);
      }
    }
    if (allDaysToDue == null || allDaysToDue.size() == 0)
      return daysToDue;
    allDaysToDue = sort(allDaysToDue);
    daysToDue = allDaysToDue.get(0);
    return daysToDue;
  }

  static Long getPaymentDaysToDue(DebtPayment payment) {
    if (payment.isPaymentComplete())
      return null;
    if (payment.getSettlementCancelled() == null)
      return getDaysToDue(payment.getDueDate());
    final OBCriteria<DebtPayment> obc = OBDal.getInstance().createCriteria(DebtPayment.class);
    obc.add(Restrictions.eq("settlementGenerate", payment.getSettlementCancelled()));
    obc.addOrderBy("dueDate", true);
    final List<DebtPayment> payments = obc.list();
    ArrayList<Long> allDaysToDue = new ArrayList<Long>();
    for (DebtPayment generatedPayment : payments) {
      Long generatedPaymentOverDue = getPaymentDaysToDue(generatedPayment);
      if (generatedPaymentOverDue != null)
        allDaysToDue.add(generatedPaymentOverDue);
    }
    if (allDaysToDue == null || allDaysToDue.size() == 0)
      return null;
    allDaysToDue = sort(allDaysToDue);
    Long daysToDue = allDaysToDue.get(0);
    return daysToDue;
  }

  static Long getDaysToDue(Date date) {
    Date now = new Date(System.currentTimeMillis());
    DateFormat df1 = DateFormat.getDateInstance(DateFormat.SHORT);
    String strToday = df1.format(now);
    Date today = null;
    try {
      today = df1.parse(strToday);
      return (date.getTime() - today.getTime()) / 86400000;
    } catch (ParseException e) {
      log4j.error("Error parsing date: ", e);
    }
    return null;
  }

  static ArrayList<Long> sort(ArrayList<Long> al) {
    Collections.sort(al);
    return al;
  }

  static BigDecimal calculatePaidAmount(DebtPayment payment, String strCurrencyTo,
      Date conversionDate, BigDecimal multiplier) {
    BigDecimal paidAmount = BigDecimal.ZERO;
    if (payment.getSettlementCancelled() == null)
      return paidAmount;
    else if (payment.getSettlementCancelled().getProcessed().equals("Y")) {
      if (payment.isPaymentComplete())
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency()
            .getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment
            .getOrganization().getId());

      BigDecimal cancelledNotPaidAmount = BigDecimal.ZERO;
      BigDecimal cancelledNotPaidWriteOffAmount = BigDecimal.ZERO;
      List<DebtPayment> cancelledPayments = payment.getSettlementCancelled()
          .getFinancialMgmtDebtPaymentSettlementCancelledList();
      ArrayList<DebtPayment> cancelledNotPaidPayments = new ArrayList<DebtPayment>();
      for (DebtPayment cancelledPayment : cancelledPayments) {
        if (!cancelledPayment.isPaymentComplete()) {
          cancelledNotPaidPayments.add(cancelledPayment);
          BigDecimal paymentCancelledAmt = getConvertedAmt(cancelledPayment.getAmount(),
              cancelledPayment.getCurrency().getId(), strCurrencyTo, cancelledPayment
                  .getSettlementCancelled().getAccountingDate(), cancelledPayment.getClient()
                  .getId(), cancelledPayment.getOrganization().getId());
          cancelledNotPaidAmount = cancelledNotPaidAmount
              .add(payment.isReceipt() == cancelledPayment.isReceipt() ? paymentCancelledAmt
                  : paymentCancelledAmt.negate());
          BigDecimal paymentCancelledWOAmt = getConvertedAmt(cancelledPayment.getWriteoffAmount(),
              cancelledPayment.getCurrency().getId(), strCurrencyTo, cancelledPayment
                  .getSettlementCancelled().getAccountingDate(), cancelledPayment.getClient()
                  .getId(), cancelledPayment.getOrganization().getId());
          cancelledNotPaidWriteOffAmount = cancelledNotPaidWriteOffAmount
              .add(payment.isReceipt() == cancelledPayment.isReceipt() ? paymentCancelledWOAmt
                  : paymentCancelledWOAmt.negate());
        }
      }

      if (cancelledNotPaidAmount.compareTo(BigDecimal.ZERO) == 0)
        // The sum of all canceled not paid payments in the settlement is zero. This means that the
        // payment has been paid completely, as it was canceled with some other pending payments
        // (for example, the ones comming from a credit memo)
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency()
            .getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment
            .getOrganization().getId());

      List<DebtPayment> generatedPayments = payment.getSettlementCancelled()
          .getFinancialMgmtDebtPaymentCSettlementGenerateIDList();
      // Add the write-off amount
      paidAmount = paidAmount.add(getConvertedAmt(payment.getWriteoffAmount(), payment
          .getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(),
          payment.getOrganization().getId()));
      if (generatedPayments == null || generatedPayments.size() == 0)
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency()
            .getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment
            .getOrganization().getId());
      for (DebtPayment generatedPayment : generatedPayments) {
        BigDecimal generatedPaymentPaidAmount = BigDecimal.ZERO;
        generatedPaymentPaidAmount = calculatePaidAmount(generatedPayment, strCurrencyTo,
            generatedPayment.getSettlementGenerate().getAccountingDate(), payment.getAmount()
                .divide(cancelledNotPaidAmount, 1000, BigDecimal.ROUND_HALF_UP));
        paidAmount = paidAmount
            .add(payment.isReceipt() == generatedPayment.isReceipt() ? generatedPaymentPaidAmount
                : generatedPaymentPaidAmount.negate());
      }
    }
    return paidAmount;
  }

  static BigDecimal calculateOverdueAmount(DebtPayment payment, String strCurrencyTo,
      Date conversionDate, BigDecimal multiplier) {
    BigDecimal overdueAmount = BigDecimal.ZERO;

    if (payment.getSettlementCancelled() == null
        && payment.getDueDate().compareTo(new Date(System.currentTimeMillis())) < 0)
      return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency()
          .getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment
          .getOrganization().getId());
    else if (payment.getSettlementCancelled() != null
        && payment.getSettlementCancelled().getProcessed().equals("Y")) {
      if (payment.isPaymentComplete())
        return BigDecimal.ZERO;
      BigDecimal cancelledNotPaidAmount = BigDecimal.ZERO;
      BigDecimal cancelledNotPaidWriteOffAmount = BigDecimal.ZERO;
      List<DebtPayment> cancelledPayments = payment.getSettlementCancelled()
          .getFinancialMgmtDebtPaymentSettlementCancelledList();
      ArrayList<DebtPayment> cancelledNotPaidPayments = new ArrayList<DebtPayment>();
      for (DebtPayment cancelledPayment : cancelledPayments) {
        if (!cancelledPayment.isPaymentComplete()) {
          cancelledNotPaidPayments.add(cancelledPayment);
          BigDecimal paymentCancelledAmt = getConvertedAmt(cancelledPayment.getAmount(),
              cancelledPayment.getCurrency().getId(), strCurrencyTo, cancelledPayment
                  .getSettlementCancelled().getAccountingDate(), cancelledPayment.getClient()
                  .getId(), cancelledPayment.getOrganization().getId());
          cancelledNotPaidAmount = cancelledNotPaidAmount
              .add(payment.isReceipt() == cancelledPayment.isReceipt() ? paymentCancelledAmt
                  : paymentCancelledAmt.negate());
          BigDecimal paymentCancelledWOAmt = getConvertedAmt(cancelledPayment.getWriteoffAmount(),
              cancelledPayment.getCurrency().getId(), strCurrencyTo, cancelledPayment
                  .getSettlementCancelled().getAccountingDate(), cancelledPayment.getClient()
                  .getId(), cancelledPayment.getOrganization().getId());
          cancelledNotPaidWriteOffAmount = cancelledNotPaidWriteOffAmount
              .add(payment.isReceipt() == cancelledPayment.isReceipt() ? paymentCancelledWOAmt
                  : paymentCancelledWOAmt.negate());

        }
      }
      if (cancelledNotPaidAmount.compareTo(BigDecimal.ZERO) == 0)
        // The sum of all canceled not paid payments in the settlement is zero. This means that the
        // payment has been paid completely, as it was canceled with some other pending payments
        // (for example, the ones comming from a credit memo)
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency()
            .getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment
            .getOrganization().getId());
      List<DebtPayment> generatedPayments = payment.getSettlementCancelled()
          .getFinancialMgmtDebtPaymentCSettlementGenerateIDList();
      if (generatedPayments == null || generatedPayments.size() == 0)
        return BigDecimal.ZERO;
      for (DebtPayment generatedPayment : generatedPayments) {
        BigDecimal generatedPaymentOverdueAmount = BigDecimal.ZERO;
        if (!generatedPayment.isPaymentComplete())
          generatedPaymentOverdueAmount = calculateOverdueAmount(generatedPayment, strCurrencyTo,
              generatedPayment.getSettlementGenerate().getAccountingDate(), payment.getAmount()
                  .divide(cancelledNotPaidAmount, 1000, BigDecimal.ROUND_HALF_UP));
        overdueAmount = overdueAmount
            .add(payment.isReceipt() == generatedPayment.isReceipt() ? generatedPaymentOverdueAmount
                : generatedPaymentOverdueAmount.negate());
      }
    }
    return overdueAmount;
  }

  static BigDecimal getConvertedAmt(BigDecimal Amt, String CurFrom_ID, String CurTo_ID,
      Date ConvDate, String client, String org) {
    if (CurFrom_ID == null || CurTo_ID == null || CurFrom_ID.equals(CurTo_ID))
      return Amt;
    DalConnectionProvider conn = new DalConnectionProvider();
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    String strConvertedAmount = AcctServer.getConvertedAmt(Amt.toString(), CurFrom_ID, CurTo_ID,
        dateFormater.format(ConvDate).toString(), "S", client, org, conn);
    return new BigDecimal(strConvertedAmount);
  }
}
