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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.test.draft;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.advpaymentmngt.process.FIN_ExecutePayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceDiscount;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.discount.Discount;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.test.base.BaseTest;

public class PaymentTest_10 extends BaseTest {

  // 1) Create an order without discount
  // 2) Add a partial payment but do not execute it
  // 3) Invoice the complete order (with discount) using an automatic receipt payment method
  // 4) Review payment info
  // 5) Execute payment
  // 6) Review payment info

  private static final Logger log = Logger.getLogger(PaymentTest_10.class);

  private static final String MANUAL_EXECUTION = "M";
  private static final String AUTOMATIC_EXECUTION = "A";
  private static final String CLEARED_ACCOUNT = "CLE";
  private static final String IN_TRANSIT_ACCOUNT = "INT";
  private static final String WITHDRAWN_ACCOUNT = "WIT";
  private static final String DEPOSIT_ACCOUNT = "DEP";
  private static final String CASH = "C";
  private static final String STANDARD_DESCRIPTION = "JUnit Test Payment_10";
  private static final String SIMPLE_EXECUTION_PROCESS = "717D521D5F454FAD9199B831001BB4E0";

  private String financialAccountId;

  /**
   * Initial Set up.
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TestUtility.setTestContextSpain();
  }

  public void testRunPayment_10() {
    boolean exception = false;
    try {
      OBContext.setAdminMode();
      try {
        // DATA SETUP
        Invoice invoice = dataSetup();
        FIN_Payment payment = null;
        for (FIN_PaymentScheduleDetail psd : invoice.getFINPaymentScheduleList().get(0)
            .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
          if ("RPAE".equals(psd.getPaymentDetails().getFinPayment().getStatus())) {
            payment = psd.getPaymentDetails().getFinPayment();
          }
        }

        FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
        List<FIN_Payment> payments = new ArrayList<FIN_Payment>();
        payments.add(payment);
        executePayment.init("OTHER",
            OBDal.getInstance().get(PaymentExecutionProcess.class, SIMPLE_EXECUTION_PROCESS),
            payments, null, payment.getOrganization());
        OBError result = executePayment.execute();
        // CHECK PAYMENT EXECUTION
        assertTrue("Payment error while executing", !"Error".equals(result.getType()));

        Order order = invoice.getSalesOrder();
        // CHECK OUTPUT DATA ORDER
        assertTrue(
            "Order Payment Schedule Outstanding Amount != 0",
            BigDecimal.ZERO.compareTo(order.getFINPaymentScheduleList().get(0)
                .getOutstandingAmount()) == 0);
        assertTrue(
            "Invoice: " + invoice.getDocumentNo() + " Order Payment Schedule Received Amount ("
                + order.getGrandTotalAmount().toPlainString() + ") != Total Amount ("
                + getPaidAmount(order.getFINPaymentScheduleList()).toPlainString() + ")",
            order.getGrandTotalAmount().compareTo(getPaidAmount(order.getFINPaymentScheduleList())) == 0);
        assertTrue("Status != Payment Received", "RPR".equals(payment.getStatus()));
        // CHECK OUTPUT DATA INVOICE
        assertTrue(
            "Invoice Payment Schedule Outstanding Amount != 0",
            BigDecimal.ZERO.compareTo(invoice.getFINPaymentScheduleList().get(0)
                .getOutstandingAmount()) == 0);
        assertTrue("Invoice remains not paid", invoice.isPaymentComplete());
        assertTrue(
            "Invoice Payment Schedule Received Amount != Total Amount",
            invoice.getGrandTotalAmount().compareTo(
                getPaidAmount(invoice.getFINPaymentScheduleList())) == 0);
        // CHECK OUTPUT DATA PAYMENT
        assertTrue("Status != Payment Received", "RPR".equals(payment.getStatus()));

      } finally {
        OBContext.restorePreviousMode();
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error(FIN_Utility.getExceptionMessage(e));
      exception = true;
    }

    assertFalse(exception);

  }

  private Invoice dataSetup() throws Exception {

    // DATA SETUP
    String bpartnerId = "8A64B71A2B0B2946012B0FE1E86301FA"; // Hoteles Buenas Noches, S.A.
    String priceListId = "8A64B71A2B0B2946012B0BD97324018A"; // Precios de Venta
    String paymentTermId = "3F22D83730EE4FD5AE42542A2839DAC4"; // 30 days
    String currencyId = "102"; // EUR
    String productId = "8A64B71A2B0B2946012B0BC438410118"; // Vino Tinto 0,75L
    String taxId = "F27A37C66749487A876F4DA0EF7467E3"; // Ventas Exentas
    String docTypeId = "40EE9B1CD3B345FABEFDA62B407B407F"; // AR Invoice
    String orderDocTypeId = "CB6EEA256BBC41109911215C5A14D39B"; // Standard Order
    String warehouseId = "71B43F2AAE3641CA849B131960BCEFF4";
    String discountId = "831A7639FBE348B1A06EA3C0B0616B75"; // Descuento 10%

    // Process
    BigDecimal quantity = new BigDecimal("10");
    BigDecimal invoicedQuantity = new BigDecimal("10");
    BigDecimal netUnitPrice = new BigDecimal("10");
    BigDecimal netListPrice = new BigDecimal("10");
    BigDecimal lineNetAmount = new BigDecimal("100");
    BigDecimal paymentAmount = new BigDecimal("50");
    BigDecimal invoiceLineNetAmount = new BigDecimal("100");
    BigDecimal priceLimit = new BigDecimal("10");

    PriceList testPriceList = OBDal.getInstance().get(PriceList.class, priceListId);
    BusinessPartner testBusinessPartner = OBDal.getInstance()
        .get(BusinessPartner.class, bpartnerId);
    Location location = TestUtility.getOneInstance(Location.class, new Value(
        Location.PROPERTY_BUSINESSPARTNER, testBusinessPartner));
    PaymentTerm testPaymentTerm = OBDal.getInstance().get(PaymentTerm.class, paymentTermId);
    Currency testCurrency = OBDal.getInstance().get(Currency.class, currencyId);
    Product testProduct = OBDal.getInstance().get(Product.class, productId);
    UOM uom = TestUtility.getOneInstance(UOM.class, new Value(UOM.PROPERTY_NAME, testProduct
        .getUOM().getName()));
    TaxRate testTaxRate = OBDal.getInstance().get(TaxRate.class, taxId);
    DocumentType testDocumentType = OBDal.getInstance().get(DocumentType.class, docTypeId);
    DocumentType testOrderDocumentType = OBDal.getInstance()
        .get(DocumentType.class, orderDocTypeId);
    FIN_FinancialAccount testAccount = TestUtility.insertFinancialAccount("APRM_FINACC_PAYMENT_10",
        STANDARD_DESCRIPTION, testCurrency, CASH, false,
        getOneInstance(org.openbravo.model.common.geography.Location.class), null, null, null,
        null, null, null, null, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, null, true,
        true);

    FIN_PaymentMethod testPaymentMethod = TestUtility.insertPaymentMethod("APRM_PM_PAYMENT_10",
        STANDARD_DESCRIPTION, true, true, false, MANUAL_EXECUTION, null, false, IN_TRANSIT_ACCOUNT,
        DEPOSIT_ACCOUNT, CLEARED_ACCOUNT, true, false, false, MANUAL_EXECUTION, null, false,
        IN_TRANSIT_ACCOUNT, WITHDRAWN_ACCOUNT, CLEARED_ACCOUNT, true, true);

    FinAccPaymentMethod existAssociation = TestUtility.getOneInstance(FinAccPaymentMethod.class,
        new Value(FinAccPaymentMethod.PROPERTY_ACCOUNT, testAccount), new Value(
            FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, testPaymentMethod));

    if (existAssociation == null)
      TestUtility.associatePaymentMethod(testAccount, testPaymentMethod);
    this.financialAccountId = testAccount.getId();

    // UPDATE BPARTNER AND ASSOCIATED NEW FINANCIAL ACCOUNT
    testBusinessPartner.setAccount(testAccount);
    OBDal.getInstance().save(testBusinessPartner);

    Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, warehouseId);
    Order order = TestUtility.createNewOrder(OBContext.getOBContext().getCurrentClient(), OBContext
        .getOBContext().getCurrentOrganization(), new Date(), new Date(), new Date(),
        testOrderDocumentType, testBusinessPartner, location, warehouse, "I", testPriceList,
        testCurrency, testPaymentMethod, testPaymentTerm, testProduct, uom, quantity, netUnitPrice,
        netListPrice, priceLimit, testTaxRate, lineNetAmount, true);

    TestUtility.processOrder(order);
    order = OBDal.getInstance().get(Order.class, order.getId());

    // CHECK OUTPUT DATA FOR ORDER
    OBContext.setAdminMode();
    try {
      FIN_PaymentScheduleDetail psd = TestUtility.getOneInstance(FIN_PaymentScheduleDetail.class,
          new Value(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, order
              .getFINPaymentScheduleList().get(0)));

      assertTrue("Payment Schedule Outstanding Amount == Order amount", order.getGrandTotalAmount()
          .compareTo(order.getFINPaymentScheduleList().get(0).getOutstandingAmount()) == 0);
      assertTrue("Payment Schedule Received Amount == 0",
          BigDecimal.ZERO.compareTo(order.getFINPaymentScheduleList().get(0).getPaidAmount()) == 0);

      assertTrue("Payment Schedule Deatail Amount == Total Amount", order.getGrandTotalAmount()
          .compareTo(psd.getAmount()) == 0);
      assertTrue("Payment Schedule Detail Write-off Amount == 0",
          BigDecimal.ZERO.compareTo(psd.getWriteoffAmount()) == 0);
    } finally {
      OBContext.restorePreviousMode();
    }
    FIN_Payment payment = TestUtility.addPaymentFromOrder(order,
        OBDal.getInstance().get(FIN_FinancialAccount.class, financialAccountId), paymentAmount,
        false);

    FIN_PaymentMethod awaitingExecutionPM = TestUtility.insertPaymentMethod(
        "APRM_PM_PAYMENT_10_AWE", STANDARD_DESCRIPTION, true, false, false, AUTOMATIC_EXECUTION,
        OBDal.getInstance().get(PaymentExecutionProcess.class, SIMPLE_EXECUTION_PROCESS), true,
        IN_TRANSIT_ACCOUNT, DEPOSIT_ACCOUNT, CLEARED_ACCOUNT, true, false, false, MANUAL_EXECUTION,
        null, false, IN_TRANSIT_ACCOUNT, WITHDRAWN_ACCOUNT, CLEARED_ACCOUNT, true, true);

    existAssociation = TestUtility.getOneInstance(FinAccPaymentMethod.class, new Value(
        FinAccPaymentMethod.PROPERTY_ACCOUNT, testAccount), new Value(
        FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, awaitingExecutionPM));

    if (existAssociation == null)
      TestUtility.associatePaymentMethod(testAccount, awaitingExecutionPM);

    // SET NEW PAYMENT METHOD FOR CREATED PAYMENT: AWAITING EXECUTION
    // NO PAYMENT INFO SHOULD BE UPDATED AS PAYMENT HAS NOT BEEN EXECUTED YET
    payment.setPaymentMethod(awaitingExecutionPM);
    OBDal.getInstance().save(payment);
    // PROCESS THE PAYMENT
    TestUtility.processPayment(payment, "P");
    payment = OBDal.getInstance().get(FIN_Payment.class, payment.getId());
    order = OBDal.getInstance().get(Order.class, order.getId());

    // CHECK OUTPUT DATA FOR ORDER
    OBContext.setAdminMode();
    try {
      // CHECK OUTPUT DATA FOR ORDER AGAIN NO CHANGE SHOULD HAPPEN AS PAYMENT REMAINS AWE
      FIN_PaymentScheduleDetail psd = TestUtility.getOneInstance(FIN_PaymentScheduleDetail.class,
          new Value(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS, payment
              .getFINPaymentDetailList().get(0)));

      assertTrue("Payment Schedule Outstanding Amount != Order amount", order.getGrandTotalAmount()
          .compareTo(order.getFINPaymentScheduleList().get(0).getOutstandingAmount()) == 0);
      assertTrue("Payment Schedule Received Amount != 0",
          BigDecimal.ZERO.compareTo(order.getFINPaymentScheduleList().get(0).getPaidAmount()) == 0);
      assertTrue("Payment Schedule Detail Write-off Amount != 0",
          BigDecimal.ZERO.compareTo(psd.getWriteoffAmount()) == 0);

      assertTrue("Status != Awaiting Execution", "RPAE".equals(payment.getStatus()));
      assertTrue("Payment Line Write-off Amount != 0",
          BigDecimal.ZERO.compareTo(psd.getPaymentDetails().getWriteoffAmount()) == 0);
    } finally {
      OBContext.restorePreviousMode();
    }
    // This created order is invoiced

    Invoice invoice = TestUtility.createNewInvoice(OBContext.getOBContext().getCurrentClient(),
        OBContext.getOBContext().getCurrentOrganization(), new Date(), new Date(), new Date(),
        testDocumentType, testBusinessPartner, location, testPriceList, testCurrency,
        testPaymentMethod, testPaymentTerm, testProduct, uom, invoicedQuantity, netUnitPrice,
        netListPrice, priceLimit, testTaxRate, invoiceLineNetAmount, true);
    OBDal.getInstance().refresh(invoice);
    // Order is associated both to header and lines
    InvoiceLine invoiceLine = invoice.getInvoiceLineList().get(0);
    invoiceLine.setSalesOrderLine(order.getOrderLineList().get(0));

    OBDal.getInstance().save(invoiceLine);

    invoice.setSalesOrder(order);

    OBDal.getInstance().save(invoice);

    // Add invoice Discount
    Discount discount = OBDal.getInstance().get(Discount.class, discountId);
    InvoiceDiscount invoiceDiscount = OBProvider.getInstance().get(InvoiceDiscount.class);
    invoiceDiscount.setInvoice(invoice);
    invoiceDiscount.setLineNo(10l);
    invoiceDiscount.setDiscount(discount);
    OBDal.getInstance().save(invoiceDiscount);

    TestUtility.processInvoice(invoice);
    invoice = OBDal.getInstance().get(Invoice.class, invoice.getId());

    return invoice;
  }

  BigDecimal getPaidAmount(List<FIN_PaymentSchedule> finPaymentScheduleList) {
    BigDecimal amount = BigDecimal.ZERO;
    for (FIN_PaymentSchedule ps : finPaymentScheduleList) {
      boolean isInvoice = ps.getInvoice() != null;
      for (FIN_PaymentScheduleDetail psd : isInvoice ? ps
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList() : ps
          .getFINPaymentScheduleDetailOrderPaymentScheduleList()) {
        if (psd.getPaymentDetails() != null
            && psd.getPaymentDetails().getFinPayment().isProcessed()
            && !"RPAE".equals(psd.getPaymentDetails().getFinPayment().getStatus())) {
          amount = amount.add(psd.getAmount());
        }
      }
    }
    return amount;
  }
}
