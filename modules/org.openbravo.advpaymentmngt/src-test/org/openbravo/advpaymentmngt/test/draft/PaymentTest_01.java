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

package org.openbravo.advpaymentmngt.test.draft;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.test.base.BaseTest;

public class PaymentTest_01 extends BaseTest {

  private static final Logger log = Logger.getLogger(PaymentTest_01.class);

  private static final String MANUAL_EXECUTION = "M";
  private static final String CLEARED_ACCOUNT = "CLE";
  private static final String IN_TRANSIT_ACCOUNT = "INT";
  private static final String WITHDRAWN_ACCOUNT = "WIT";
  private static final String DEPOSIT_ACCOUNT = "DEP";
  private static final String CASH = "C";
  private static final String STANDARD_DESCRIPTION = "JUnit Test Payment_01";

  private String financialAccountId;

  /**
   * Initial Set up.
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TestUtility.setTestContext();
  }

  public void testRunPayment_01() {
    boolean exception = false;
    Invoice invoice = null;
    FIN_Payment payment = null;

    try {

      // DATA SETUP
      invoice = dataSetup();

      // PAY COMPLETELY THE INVOICE
      invoice = OBDal.getInstance().get(Invoice.class, invoice.getId());
      payment = TestUtility.addPaymentFromInvoice(invoice,
          OBDal.getInstance().get(FIN_FinancialAccount.class, financialAccountId),
          invoice.getGrandTotalAmount(), false);

      // PROCESS THE PAYMENT
      TestUtility.processPayment(payment, "P");

      // CHECK OUTPUT DATA
      OBContext.setAdminMode();
      try {
        FIN_PaymentScheduleDetail psd = TestUtility.getOneInstance(FIN_PaymentScheduleDetail.class,
            new Value(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE, invoice
                .getFINPaymentScheduleList().get(0)));

        assertTrue(
            "Payment Schedule Outstanding Amount != 0",
            BigDecimal.ZERO.compareTo(invoice.getFINPaymentScheduleList().get(0)
                .getOutstandingAmount()) == 0);
        assertTrue(
            "Payment Schedule Received Amount != Total Amount",
            invoice.getGrandTotalAmount().compareTo(
                invoice.getFINPaymentScheduleList().get(0).getPaidAmount()) == 0);

        assertTrue("Payment Schedule Deatail Amount != Total Amount", invoice.getGrandTotalAmount()
            .compareTo(psd.getAmount()) == 0);
        assertTrue("Payment Schedule Detail Write-off Amount != 0",
            BigDecimal.ZERO.compareTo(psd.getWriteoffAmount()) == 0);

        assertTrue("Payment Amount != Total Amount",
            invoice.getGrandTotalAmount().compareTo(payment.getAmount()) == 0);
        assertTrue("Status != Payment Received", "RPR".equals(payment.getStatus()));
        assertTrue("Payment Line Amount != Total Amount",
            invoice.getGrandTotalAmount().compareTo(psd.getPaymentDetails().getAmount()) == 0);
        assertTrue("Payment Line Write-off Amount != 0",
            BigDecimal.ZERO.compareTo(psd.getPaymentDetails().getWriteoffAmount()) == 0);

      } finally {
        OBContext.restorePreviousMode();
      }

      // REACTIVATE
      TestUtility.processPayment(payment, "R");

      // CHECK OUTPUT DATA AFTER REACTIVATION
      OBContext.setAdminMode();
      try {
        FIN_PaymentScheduleDetail psd = TestUtility.getOneInstance(FIN_PaymentScheduleDetail.class,
            new Value(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE, invoice
                .getFINPaymentScheduleList().get(0)));

        assertTrue(
            "Expected Amount != Total Amount",
            invoice.getGrandTotalAmount().compareTo(
                invoice.getFINPaymentScheduleList().get(0).getAmount()) == 0);
        assertTrue(
            "Outstanding Amount != Total Amount",
            invoice.getGrandTotalAmount().compareTo(
                invoice.getFINPaymentScheduleList().get(0).getOutstandingAmount()) == 0);
        assertTrue(
            "Received Amount != 0",
            BigDecimal.ZERO.compareTo(invoice.getFINPaymentScheduleList().get(0).getPaidAmount()) == 0);

        assertTrue("Payment Schedule Deatail Amount != 0",
            invoice.getGrandTotalAmount().compareTo(psd.getAmount()) == 0);
        assertTrue("Payment Schedule Detail Write-off Amount != 0",
            BigDecimal.ZERO.compareTo(psd.getWriteoffAmount()) == 0);

        assertTrue("Payment Amount != 0", BigDecimal.ZERO.compareTo(payment.getAmount()) == 0);
        assertTrue("Status != Awaiting Payment", "RPAP".equals(payment.getStatus()));

        assertTrue("There are Payment Lines for this payment",
            TestUtility.getOneInstance(FIN_PaymentDetail.class, new Value(
                FIN_PaymentDetail.PROPERTY_FINPAYMENT, payment)) == null);
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
    String bpartnerId = "8A64B71A2B0B2946012B0FE1E51401C1"; // Sleep Well Hotel
    String priceListId = "8A64B71A2B0B2946012B0BD96E850150"; // General Sales
    String paymentTermId = "3F22D83730EE4FD5AE42542A2839DAC4"; // 30 days
    String currencyId = "102"; // EUR
    String productId = "8A64B71A2B0B2946012B0BC4345000FB"; // Ale Beer
    String taxId = "1FE610D3A8844F85B17CA32525C15353"; // NY Sales Tax
    String docTypeId = "C99C4AE941E1460B91BC97665BE5D141"; // AR Invoice
    BigDecimal invoicedQuantity = new BigDecimal("5");
    BigDecimal netUnitPrice = new BigDecimal("2.04");
    BigDecimal netListPrice = new BigDecimal("2.04");
    BigDecimal lineNetAmount = new BigDecimal("10.20");
    BigDecimal priceLimit = new BigDecimal("1");

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

    FIN_FinancialAccount testAccount = TestUtility.insertFinancialAccount("APRM_FINACC_PAYMENT_01",
        STANDARD_DESCRIPTION, testCurrency, CASH, false,
        getOneInstance(org.openbravo.model.common.geography.Location.class), testBusinessPartner,
        null, null, null, null, null, null, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO,
        null, true, true);

    FIN_PaymentMethod testPaymentMethod = TestUtility.insertPaymentMethod("APRM_PM_PAYMENT_01",
        STANDARD_DESCRIPTION, true, false, false, MANUAL_EXECUTION, null, false,
        IN_TRANSIT_ACCOUNT, DEPOSIT_ACCOUNT, CLEARED_ACCOUNT, true, false, false, MANUAL_EXECUTION,
        null, false, IN_TRANSIT_ACCOUNT, WITHDRAWN_ACCOUNT, CLEARED_ACCOUNT, true, true);

    FinAccPaymentMethod existAssociation = TestUtility.getOneInstance(FinAccPaymentMethod.class,
        new Value(FinAccPaymentMethod.PROPERTY_ACCOUNT, testAccount), new Value(
            FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, testPaymentMethod));

    if (existAssociation == null)
      TestUtility.associatePaymentMethod(testAccount, testPaymentMethod);
    this.financialAccountId = testAccount.getId();

    Invoice invoice = TestUtility.createNewInvoice(OBContext.getOBContext().getCurrentClient(),
        OBContext.getOBContext().getCurrentOrganization(), new Date(), new Date(), new Date(),
        testDocumentType, testBusinessPartner, location, testPriceList, testCurrency,
        testPaymentMethod, testPaymentTerm, testProduct, uom, invoicedQuantity, netUnitPrice,
        netListPrice, priceLimit, testTaxRate, lineNetAmount, true);

    TestUtility.processInvoice(invoice);

    return invoice;
  }
}
