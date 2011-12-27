/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009-2011 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.financial.paymentreport.erpCommon.ad_reports;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.List;
import org.openbravo.model.ad.domain.ListTrl;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.project.Project;
import org.openbravo.utils.Replace;

public class PaymentReportDao {

  private static final long milisecDayConv = (1000 * 60 * 60 * 24);

  public PaymentReportDao() {
  }

  public <T extends BaseOBObject> T getObject(Class<T> t, String strId) {
    return OBDal.getInstance().get(t, strId);
  }

  public FieldProvider[] getPaymentReport(VariablesSecureApp vars, String strOrg,
      String strInclSubOrg, String strDueDateFrom, String strDueDateTo, String strAmountFrom,
      String strAmountTo, String strDocumentDateFrom, String strDocumentDateTo,
      String strcBPartnerIdIN, String strcBPGroupIdIN, String strcProjectIdIN, String strfinPaymSt,
      String strPaymentMethodId, String strFinancialAccountId, String strcCurrency,
      String strConvertCurrency, String strConversionDate, String strPaymType, String strOverdue,
      String strGroupCrit, String strOrdCrit) {
    return getPaymentReport(vars, strOrg, strInclSubOrg, strDueDateFrom, strDueDateTo,
        strAmountFrom, strAmountTo, strDocumentDateFrom, strDocumentDateTo, strcBPartnerIdIN,
        strcBPGroupIdIN, strcProjectIdIN, strfinPaymSt, strPaymentMethodId, strFinancialAccountId,
        strcCurrency, strConvertCurrency, strConversionDate, strPaymType, strOverdue, strGroupCrit,
        strOrdCrit, "Y");
  }

  public FieldProvider[] getPaymentReport(VariablesSecureApp vars, String strOrg,
      String strInclSubOrg, String strDueDateFrom, String strDueDateTo, String strAmountFrom,
      String strAmountTo, String strDocumentDateFrom, String strDocumentDateTo,
      String strcBPartnerIdIN, String strcBPGroupIdIN, String strcProjectIdIN, String strfinPaymSt,
      String strPaymentMethodId, String strFinancialAccountId, String strcCurrency,
      String strConvertCurrency, String strConversionDate, String strPaymType, String strOverdue,
      String strGroupCrit, String strOrdCrit, String strInclPaymentUsingCredit) {

    final StringBuilder hsqlScript = new StringBuilder();
    final java.util.List<Object> parameters = new ArrayList<Object>();

    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    FieldProvider[] data;
    Currency transCurrency;
    BigDecimal transAmount = null;
    ConversionRate convRate = null;
    ArrayList<FieldProvider> groupedData = new ArrayList<FieldProvider>();

    OBContext.setAdminMode();
    try {

      hsqlScript.append(" as fpsd ");
      hsqlScript.append(" left outer join fpsd.paymentDetails.finPayment pay");
      hsqlScript.append(" left outer join pay.businessPartner paybp");
      hsqlScript.append(" left outer join paybp.businessPartnerCategory paybpc");
      hsqlScript.append(" left outer join fpsd.invoicePaymentSchedule invps");
      hsqlScript.append(" left outer join invps.invoice inv");
      hsqlScript.append(" left outer join inv.businessPartner invbp");
      hsqlScript.append(" left outer join invbp.businessPartnerCategory invbpc");
      hsqlScript.append(" left outer join fpsd.paymentDetails.finPayment.currency paycur");
      hsqlScript.append(" left outer join fpsd.invoicePaymentSchedule.invoice.currency invcur");
      hsqlScript.append(" left outer join pay.project paypro");
      hsqlScript.append(" left outer join inv.project invpro");
      hsqlScript.append(" where (fpsd.");
      hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      hsqlScript.append(" is not null or invps is not null ");
      hsqlScript.append(") ");

      // organization + include sub-organization
      if (!strOrg.isEmpty()) {
        if (!strInclSubOrg.equalsIgnoreCase("include")) {
          hsqlScript.append(" and fpsd.");
          hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_ORGANIZATION);
          hsqlScript.append(".id = '");
          hsqlScript.append(strOrg);
          hsqlScript.append("'");
        } else {
          hsqlScript.append(" and fpsd.");
          hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_ORGANIZATION);
          hsqlScript.append(".id in ('");
          Set<String> orgChildTree = OBContext.getOBContext().getOrganizationStructureProvider()
              .getChildTree(strOrg, true);
          Iterator<String> orgChildTreeIter = orgChildTree.iterator();
          while (orgChildTreeIter.hasNext()) {
            hsqlScript.append(orgChildTreeIter.next());
            orgChildTreeIter.remove();
            hsqlScript.append("'");
            if (orgChildTreeIter.hasNext())
              hsqlScript.append(", '");
          }
          hsqlScript.append(")");
        }
      }

      // Exclude payments that use credit payment
      if (!strInclPaymentUsingCredit.equalsIgnoreCase("Y")) {
        hsqlScript.append(" and (not (pay.amount = 0 ");
        hsqlScript.append(" and pay.usedCredit > pay.generatedCredit) or pay is null)");
      }

      // due date from - due date to
      if (!strDueDateFrom.isEmpty()) {
        hsqlScript.append(" and invps.");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        hsqlScript.append(" >= ?");
        parameters.add(FIN_Utility.getDate(strDueDateFrom));
      }
      if (!strDueDateTo.isEmpty()) {
        hsqlScript.append(" and invps.");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        hsqlScript.append(" <= ?");
        parameters.add(FIN_Utility.getDate(strDueDateTo));
      }

      // document date from - document date to
      if (!strDocumentDateFrom.isEmpty()) {
        hsqlScript.append(" and coalesce(inv.");
        hsqlScript.append(Invoice.PROPERTY_INVOICEDATE);
        hsqlScript.append(", pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        hsqlScript.append(") >= ?");
        parameters.add(FIN_Utility.getDate(strDocumentDateFrom));
      }
      if (!strDocumentDateTo.isEmpty()) {
        hsqlScript.append(" and coalesce(inv.");
        hsqlScript.append(Invoice.PROPERTY_INVOICEDATE);
        hsqlScript.append(", pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        hsqlScript.append(") <= ?");
        parameters.add(FIN_Utility.getDate(strDocumentDateTo));
      }

      // business partner
      if (!strcBPartnerIdIN.isEmpty()) {
        hsqlScript.append(" and coalesce(pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(", inv.");
        hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(") in ");
        hsqlScript.append(strcBPartnerIdIN);
      }

      // business partner category
      if (!strcBPGroupIdIN.isEmpty()) {
        hsqlScript.append(" and coalesce(paybpc, invbpc) = '");
        hsqlScript.append(strcBPGroupIdIN);
        hsqlScript.append("'");
      }

      // project
      if (!strcProjectIdIN.isEmpty()) {
        hsqlScript.append(" and coalesce(pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_PROJECT);
        hsqlScript.append(", inv.");
        hsqlScript.append(Invoice.PROPERTY_PROJECT);
        hsqlScript.append(") in ");
        hsqlScript.append(strcProjectIdIN);
      }

      // status
      if (!strfinPaymSt.isEmpty() && !strfinPaymSt.equalsIgnoreCase("('')")) {
        hsqlScript.append(" and (pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_STATUS);
        hsqlScript.append(" in ");
        hsqlScript.append(strfinPaymSt);
        if (strfinPaymSt.contains("RPAP")) {
          hsqlScript.append(" or fpsd.");
          hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
          hsqlScript.append(" is null)");
        } else {
          hsqlScript.append(" )");
        }
      }

      // payment method
      if (!strPaymentMethodId.isEmpty()) {
        hsqlScript.append(" and coalesce(pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTMETHOD);
        hsqlScript.append(", inv.");
        hsqlScript.append(Invoice.PROPERTY_PAYMENTMETHOD);
        hsqlScript.append(") = '");
        hsqlScript.append(strPaymentMethodId);
        hsqlScript.append("'");
      }

      // financial account
      if (!strFinancialAccountId.isEmpty()) {
        hsqlScript.append(" and (pay is not null and pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_ACCOUNT);
        hsqlScript.append(".id = '");
        hsqlScript.append(strFinancialAccountId);
        hsqlScript.append("' or ((inv.");
        hsqlScript.append(Invoice.PROPERTY_SALESTRANSACTION);
        hsqlScript.append(" = 'Y'");
        hsqlScript.append(" and invbp.");
        hsqlScript.append(BusinessPartner.PROPERTY_ACCOUNT);
        hsqlScript.append(".id = '");
        hsqlScript.append(strFinancialAccountId);
        hsqlScript.append("')");
        hsqlScript.append(" or (inv.");
        hsqlScript.append(Invoice.PROPERTY_SALESTRANSACTION);
        hsqlScript.append(" = 'N'");
        hsqlScript.append(" and invbp.");
        hsqlScript.append(BusinessPartner.PROPERTY_POFINANCIALACCOUNT);
        hsqlScript.append(".id = '");
        hsqlScript.append(strFinancialAccountId);
        hsqlScript.append("')))");
      }

      // currency
      if (!strcCurrency.isEmpty()) {
        hsqlScript.append(" and coalesce(pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_CURRENCY);
        hsqlScript.append(", inv.");
        hsqlScript.append(Invoice.PROPERTY_CURRENCY);
        hsqlScript.append(") = '");
        hsqlScript.append(strcCurrency);
        hsqlScript.append("'");
      }

      // payment type
      if (strPaymType.equalsIgnoreCase("FINPR_Receivables")) {
        hsqlScript.append(" and (pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_RECEIPT);
        hsqlScript.append(" = 'Y'");
        hsqlScript.append(" or inv.");
        hsqlScript.append(Invoice.PROPERTY_SALESTRANSACTION);
        hsqlScript.append(" = 'Y')");
      } else if (strPaymType.equalsIgnoreCase("FINPR_Payables")) {
        hsqlScript.append(" and (pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_RECEIPT);
        hsqlScript.append(" = 'N'");
        hsqlScript.append(" or inv.");
        hsqlScript.append(Invoice.PROPERTY_SALESTRANSACTION);
        hsqlScript.append(" = 'N')");
      }

      // overdue
      if (!strOverdue.isEmpty()) {
        hsqlScript.append(" and invps.");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT);
        hsqlScript.append(" != '0'");
        hsqlScript.append(" and invps.");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        hsqlScript.append(" <  ?");
        parameters.add(FIN_Utility.getDate(dateFormat.format(new Date())));
      }

      hsqlScript.append(" order by ");

      if (strGroupCrit.equalsIgnoreCase("APRM_FATS_BPARTNER")) {
        hsqlScript.append(" coalesce(paybp.");
        hsqlScript.append(BusinessPartner.PROPERTY_NAME);
        hsqlScript.append(", invbp.");
        hsqlScript.append(BusinessPartner.PROPERTY_NAME);
        hsqlScript.append("), ");
      } else if (strGroupCrit.equalsIgnoreCase("Project")) {
        hsqlScript.append("  coalesce(paypro.");
        hsqlScript.append(Project.PROPERTY_NAME);
        hsqlScript.append(", invpro.");
        hsqlScript.append(Project.PROPERTY_NAME);
        hsqlScript.append("), ");
      } else if (strGroupCrit.equalsIgnoreCase("FINPR_BPartner_Category")) {
        hsqlScript.append("  coalesce(paybpc.");
        hsqlScript.append(Category.PROPERTY_NAME);
        hsqlScript.append(", invbpc.");
        hsqlScript.append(Category.PROPERTY_NAME);
        hsqlScript.append("), ");
      } else if (strGroupCrit.equalsIgnoreCase("INS_CURRENCY")) {
        hsqlScript.append("  coalesce(paycur.");
        hsqlScript.append(Currency.PROPERTY_ISOCODE);
        hsqlScript.append(", invcur.");
        hsqlScript.append(Currency.PROPERTY_ISOCODE);
        hsqlScript.append("), ");
      }

      hsqlScript.append(" coalesce(pay.");
      hsqlScript.append(FIN_Payment.PROPERTY_STATUS);
      hsqlScript.append(", 'RPAP')");

      if (!strOrdCrit.isEmpty()) {
        String[] strOrdCritList = strOrdCrit.substring(2, strOrdCrit.length() - 2).split("', '");

        for (int i = 0; i < strOrdCritList.length; i++) {
          if (strOrdCritList[i].contains("Date")) {
            hsqlScript.append(", inv.");
            hsqlScript.append(Invoice.PROPERTY_INVOICEDATE);
          }
          if (strOrdCritList[i].contains("Project")) {
            hsqlScript.append(",  coalesce(paypro.");
            hsqlScript.append(Project.PROPERTY_NAME);
            hsqlScript.append(", invpro.");
            hsqlScript.append(Project.PROPERTY_NAME);
            hsqlScript.append(")");
          }
          if (strOrdCritList[i].contains("FINPR_BPartner_Category")) {
            hsqlScript.append(",  coalesce(paybpc.");
            hsqlScript.append(Category.PROPERTY_NAME);
            hsqlScript.append(", invbpc.");
            hsqlScript.append(Category.PROPERTY_NAME);
            hsqlScript.append(")");
          }
          if (strOrdCritList[i].contains("APRM_FATS_BPARTNER")) {
            hsqlScript.append(",  coalesce(paybp.");
            hsqlScript.append(BusinessPartner.PROPERTY_NAME);
            hsqlScript.append(", invbp.");
            hsqlScript.append(BusinessPartner.PROPERTY_NAME);
            hsqlScript.append(")");
          }
          if (strOrdCritList[i].contains("INS_CURRENCY")) {
            hsqlScript.append(",  coalesce(paycur.");
            hsqlScript.append(Currency.PROPERTY_ISOCODE);
            hsqlScript.append(", invcur.");
            hsqlScript.append(Currency.PROPERTY_ISOCODE);
            hsqlScript.append(")");
          }
        }
      }

      hsqlScript.append(", fpsd.");
      hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      hsqlScript.append(".");
      hsqlScript.append(FIN_PaymentSchedule.PROPERTY_ID);

      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, hsqlScript.toString(), parameters);
      obqPSD.setFilterOnReadableOrganization(false);
      java.util.List<FIN_PaymentScheduleDetail> obqPSDList = obqPSD.list();
      data = FieldProviderFactory.getFieldProviderArray(obqPSDList);

      FIN_PaymentScheduleDetail[] FIN_PaymentScheduleDetail = new FIN_PaymentScheduleDetail[0];
      FIN_PaymentScheduleDetail = obqPSDList.toArray(FIN_PaymentScheduleDetail);

      FIN_PaymentDetail finPaymDetail;
      Boolean mustGroup;
      String previousFPSDInvoiceId = null;
      String previousPaymentId = null;
      BigDecimal amountSum = BigDecimal.ZERO;
      BigDecimal balanceSum = BigDecimal.ZERO;
      FieldProvider previousRow = null;
      ConversionRate previousConvRate = null;
      boolean isReceipt = false;
      boolean isAmtInLimit = false;

      for (int i = 0; i < data.length; i++) {
        if (FIN_PaymentScheduleDetail[i].getPaymentDetails() != null) {
          BusinessPartner bp = getDocumentBusinessPartner(FIN_PaymentScheduleDetail[i]);
          if (bp == null) {
            FieldProviderFactory.setField(data[i], "BP_GROUP", "");
            FieldProviderFactory.setField(data[i], "BPARTNER", "");
          } else {
            // bp_group -- bp_category
            FieldProviderFactory.setField(data[i], "BP_GROUP", bp.getBusinessPartnerCategory()
                .getName());
            // bpartner
            FieldProviderFactory.setField(data[i], "BPARTNER", bp.getName());
          }

          // transCurrency
          transCurrency = FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment()
              .getCurrency();
          FieldProviderFactory.setField(data[i], "TRANS_CURRENCY", transCurrency.getISOCode());
          // paymentMethod
          FieldProviderFactory.setField(data[i], "PAYMENT_METHOD", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getPaymentMethod().getIdentifier());

          // payment
          FieldProviderFactory.setField(
              data[i],
              "PAYMENT",
              dateFormat.format(FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment()
                  .getPaymentDate())
                  + " - "
                  + FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment()
                      .getDocumentNo());
          // payment description
          FieldProviderFactory.setField(data[i], "PAYMENT_DESC", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getDescription());
          // payment_id
          FieldProviderFactory.setField(data[i], "PAYMENT_ID", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getId().toString());
          // payment_date
          FieldProviderFactory.setField(
              data[i],
              "PAYMENT_DATE",
              dateFormat.format(FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment()
                  .getPaymentDate()));
          // payment_docNo
          FieldProviderFactory.setField(data[i], "PAYMENT_DOCNO", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getDocumentNo());
          // payment yes / no
          FieldProviderFactory.setField(data[i], "PAYMENT_Y_N", "");
          // financialAccount
          FieldProviderFactory.setField(data[i], "FINANCIAL_ACCOUNT", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getAccount().getIdentifier());
          // status
          FieldProviderFactory.setField(data[i], "STATUS",
              translateRefList(FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment()
                  .getStatus()));
          // is receipt
          if (FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment().isReceipt()) {
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "Y");
            isReceipt = true;
          } else {
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "N");
            isReceipt = false;
          }
        } else {

          // bp_group -- bp_category
          FieldProviderFactory.setField(data[i], "BP_GROUP", FIN_PaymentScheduleDetail[i]
              .getInvoicePaymentSchedule().getInvoice().getBusinessPartner()
              .getBusinessPartnerCategory().getName());
          // bpartner
          FieldProviderFactory.setField(data[i], "BPARTNER", FIN_PaymentScheduleDetail[i]
              .getInvoicePaymentSchedule().getInvoice().getBusinessPartner().getName());
          // transCurrency
          transCurrency = FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
              .getCurrency();
          FieldProviderFactory.setField(data[i], "TRANS_CURRENCY", transCurrency.getISOCode());
          // paymentMethod
          FieldProviderFactory.setField(data[i], "PAYMENT_METHOD", "");
          // payment
          FieldProviderFactory.setField(data[i], "PAYMENT", "");
          // payment_id
          FieldProviderFactory.setField(data[i], "PAYMENT_ID", "");
          // payment_date
          FieldProviderFactory.setField(data[i], "PAYMENT_DATE", "");
          // payment_docNo
          FieldProviderFactory.setField(data[i], "PAYMENT_DOCNO", "");
          // payment yes / no
          FieldProviderFactory.setField(data[i], "PAYMENT_Y_N", "Display:None");
          // financialAccount
          FieldProviderFactory.setField(data[i], "FINANCIAL_ACCOUNT", "");
          // status
          FieldProviderFactory.setField(data[i], "STATUS", translateRefList("RPAP"));
          // is receipt
          if (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
              .isSalesTransaction()) {
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "Y");
            isReceipt = true;
          }

          else {
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "N");
            isReceipt = false;
          }
        }

        if (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule() != null) {
          fillLine(dateFormat, data[i], FIN_PaymentScheduleDetail[i],
              FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule(), false);
        } else if (FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment() != null) {
          java.util.List<Invoice> invoices = getInvoicesUsingCredit(FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment());
          if (invoices.size() == 1) {
            FIN_PaymentSchedule ps = getInvoicePaymentSchedule(FIN_PaymentScheduleDetail[i]
                .getPaymentDetails().getFinPayment());
            fillLine(dateFormat, data[i], FIN_PaymentScheduleDetail[i], ps, true);
          } else {
            // project
            FieldProviderFactory.setField(data[i], "PROJECT", "");
            // salesPerson
            FieldProviderFactory.setField(data[i], "SALES_PERSON", "");
            // invoiceNumber.
            FieldProviderFactory.setField(data[i], "INVOICE_NUMBER", invoices.size() > 1 ? "**"
                + getInvoicesDocNos(invoices) : "");
            // payment plan id
            FieldProviderFactory.setField(data[i], "PAYMENT_PLAN_ID", "");
            // payment plan yes / no
            FieldProviderFactory.setField(data[i], "PAYMENT_PLAN_Y_N",
                invoices.size() != 1 ? "Display:none" : "");
            // payment plan yes / no
            FieldProviderFactory.setField(data[i], "NOT_PAYMENT_PLAN_Y_N", invoices.size() > 1 ? ""
                : "Display:none");
            // invoiceDate
            FieldProviderFactory.setField(data[i], "INVOICE_DATE", "");
            // dueDate.
            FieldProviderFactory.setField(data[i], "DUE_DATE", "");
            // plannedDSO
            FieldProviderFactory.setField(data[i], "PLANNED_DSO", "0");
            // currentDSO
            FieldProviderFactory.setField(data[i], "CURRENT_DSO", "0");
            // daysOverdue
            FieldProviderFactory.setField(data[i], "OVERDUE", "0");
          }
        } else {
          // project
          FieldProviderFactory.setField(data[i], "PROJECT", "");
          // salesPerson
          FieldProviderFactory.setField(data[i], "SALES_PERSON", "");
          // invoiceNumber.
          FieldProviderFactory.setField(data[i], "INVOICE_NUMBER", "");
          // payment plan id
          FieldProviderFactory.setField(data[i], "PAYMENT_PLAN_ID", "");
          // payment plan yes / no
          FieldProviderFactory.setField(data[i], "PAYMENT_PLAN_Y_N", "Display:none");
          // payment plan yes / no
          FieldProviderFactory.setField(data[i], "NOT_PAYMENT_PLAN_Y_N", "Display:none");
          // invoiceDate
          FieldProviderFactory.setField(data[i], "INVOICE_DATE", "");
          // dueDate.
          FieldProviderFactory.setField(data[i], "DUE_DATE", "");
          // plannedDSO
          FieldProviderFactory.setField(data[i], "PLANNED_DSO", "0");
          // currentDSO
          FieldProviderFactory.setField(data[i], "CURRENT_DSO", "0");
          // daysOverdue
          FieldProviderFactory.setField(data[i], "OVERDUE", "0");

        }

        // transactional and base amounts
        transAmount = FIN_PaymentScheduleDetail[i].getAmount();

        Currency baseCurrency = OBDal.getInstance().get(Currency.class, strConvertCurrency);

        boolean sameCurrency = baseCurrency.getISOCode().equalsIgnoreCase(
            transCurrency.getISOCode());

        if (!sameCurrency) {
          convRate = this.getConversionRate(transCurrency, baseCurrency, strConversionDate);

          if (convRate != null) {
            final int stdPrecission = convRate.getToCurrency().getStandardPrecision().intValue();
            if (isReceipt) {
              FieldProviderFactory.setField(data[i], "TRANS_AMOUNT", transAmount.toString());
              FieldProviderFactory.setField(
                  data[i],
                  "BASE_AMOUNT",
                  transAmount.multiply(convRate.getMultipleRateBy())
                      .setScale(stdPrecission, BigDecimal.ROUND_HALF_UP).toString());
            } else {
              FieldProviderFactory.setField(data[i], "TRANS_AMOUNT", transAmount.negate()
                  .toString());
              FieldProviderFactory.setField(
                  data[i],
                  "BASE_AMOUNT",
                  transAmount.multiply(convRate.getMultipleRateBy())
                      .setScale(stdPrecission, BigDecimal.ROUND_HALF_UP).negate().toString());
            }
          } else {
            FieldProvider[] fp = new FieldProvider[1];
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("transCurrency", transCurrency.getId());
            hm.put("baseCurrency", strConvertCurrency);
            hm.put("conversionDate", strConversionDate);

            fp[0] = new FieldProviderFactory(hm);
            data = fp;

            OBContext.restorePreviousMode();
            return data;
          }
        } else {
          convRate = null;
          if (isReceipt) {
            FieldProviderFactory.setField(data[i], "TRANS_AMOUNT", transAmount.toString());
            FieldProviderFactory.setField(data[i], "BASE_AMOUNT", transAmount.toString());
          } else {
            FieldProviderFactory.setField(data[i], "TRANS_AMOUNT", transAmount.negate().toString());
            FieldProviderFactory.setField(data[i], "BASE_AMOUNT", transAmount.negate().toString());
          }
        }

        // currency
        FieldProviderFactory.setField(data[i], "BASE_CURRENCY", baseCurrency.getISOCode());
        // baseCurrency
        FieldProviderFactory.setField(data[i], "TRANS_CURRENCY", transCurrency.getISOCode());

        // Balance
        String status = "RPAE";
        try {
          status = FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment().getStatus();
        } catch (NullPointerException e) {
        }
        final boolean isCreditPayment = FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule() == null
            && FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment() != null;

        BigDecimal balance = BigDecimal.ZERO;
        if (isCreditPayment && status != null && "PWNC RPR RPPC PPM RDNC".indexOf(status) >= 0) {
          balance = FIN_PaymentScheduleDetail[i]
              .getPaymentDetails()
              .getFinPayment()
              .getGeneratedCredit()
              .subtract(
                  FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment().getUsedCredit());
          if (isReceipt) {
            balance = balance.negate();
          }
        } else if (!isCreditPayment && status != null
            && "PWNC RPR RPPC PPM RDNC RPVOID".indexOf(status) == -1) {
          balance = isReceipt ? transAmount : transAmount.negate();
        }
        if (convRate != null) {
          final int stdPrecission = convRate.getToCurrency().getStandardPrecision().intValue();
          balance = balance.multiply(convRate.getMultipleRateBy()).setScale(stdPrecission,
              BigDecimal.ROUND_HALF_UP);
        }
        FieldProviderFactory.setField(data[i], "BALANCE", balance.toString());

        finPaymDetail = FIN_PaymentScheduleDetail[i].getPaymentDetails();

        // Payment Schedule Detail grouping criteria
        if (finPaymDetail != null
            && FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule() != null) {
          mustGroup = finPaymDetail.getFinPayment().getId().equalsIgnoreCase(previousPaymentId)
              && FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getId()
                  .equalsIgnoreCase(previousFPSDInvoiceId);
          previousFPSDInvoiceId = FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getId();
          previousPaymentId = finPaymDetail.getFinPayment().getId();
        } else if (finPaymDetail != null
            && FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule() == null) {
          mustGroup = finPaymDetail.getFinPayment().getId().equalsIgnoreCase(previousPaymentId)
              && previousFPSDInvoiceId == null;
          previousPaymentId = finPaymDetail.getFinPayment().getId();
          previousFPSDInvoiceId = null;
        } else if (finPaymDetail == null
            && FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule() != null) {
          mustGroup = previousPaymentId == null
              && FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getId()
                  .equalsIgnoreCase(previousFPSDInvoiceId);
          previousPaymentId = null;
          previousFPSDInvoiceId = FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getId();
        } else {
          mustGroup = false;
        }

        if (mustGroup) {
          amountSum = amountSum.add(transAmount);
          balanceSum = balanceSum.add(balance);
        } else {
          if (previousRow != null) {
            // The current row has nothing to do with the previous one. Because of that, the
            // previous row has to be added to grouped data.
            if (previousRow.getField("ISRECEIPT").equalsIgnoreCase("Y"))
              FieldProviderFactory.setField(previousRow, "TRANS_AMOUNT", amountSum.toString());
            else
              FieldProviderFactory.setField(previousRow, "TRANS_AMOUNT", amountSum.negate()
                  .toString());
            FieldProviderFactory.setField(previousRow, "BALANCE", balanceSum.toString());
            if (previousConvRate == null) {
              if (previousRow.getField("ISRECEIPT").equalsIgnoreCase("Y"))
                FieldProviderFactory.setField(previousRow, "BASE_AMOUNT", amountSum.toString());
              else
                FieldProviderFactory.setField(previousRow, "BASE_AMOUNT", amountSum.negate()
                    .toString());
            } else {
              final int stdPrecission = previousConvRate.getToCurrency().getStandardPrecision()
                  .intValue();
              if (previousRow.getField("ISRECEIPT").equalsIgnoreCase("Y"))
                FieldProviderFactory.setField(
                    previousRow,
                    "BASE_AMOUNT",
                    amountSum.multiply(previousConvRate.getMultipleRateBy())
                        .setScale(stdPrecission, BigDecimal.ROUND_HALF_UP).toString());
              else
                FieldProviderFactory.setField(
                    previousRow,
                    "BASE_AMOUNT",
                    amountSum.multiply(previousConvRate.getMultipleRateBy())
                        .setScale(stdPrecission, BigDecimal.ROUND_HALF_UP).negate().toString());
            }

            if (strAmountFrom.isEmpty() && strAmountTo.isEmpty()) {
              isAmtInLimit = true;
            } else if (!strAmountFrom.isEmpty() && strAmountTo.isEmpty()) {
              isAmtInLimit = Double.parseDouble(previousRow.getField("TRANS_AMOUNT")) >= Double
                  .parseDouble(strAmountFrom);
            } else if (strAmountFrom.isEmpty() && !strAmountTo.isEmpty()) {
              isAmtInLimit = Double.parseDouble(previousRow.getField("TRANS_AMOUNT")) <= Double
                  .parseDouble(strAmountTo);
            } else {
              isAmtInLimit = Double.parseDouble(previousRow.getField("TRANS_AMOUNT")) >= Double
                  .parseDouble(strAmountFrom)
                  && Double.parseDouble(previousRow.getField("TRANS_AMOUNT")) <= Double
                      .parseDouble(strAmountTo);
            }
            if (isAmtInLimit) {
              groupedData.add(previousRow);
              isAmtInLimit = false;
            }
          }
          previousRow = data[i];
          previousConvRate = convRate;
          amountSum = transAmount;
          balanceSum = balance;
        }

        // group_crit_id this is the column that has the ids of the grouping criteria selected
        if (strGroupCrit.equalsIgnoreCase("APRM_FATS_BPARTNER")) {
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT_ID",
              previousRow.getField("BPARTNER"));
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT", "Business Partner");
        } else if (strGroupCrit.equalsIgnoreCase("Project")) {
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT_ID",
              previousRow.getField("PROJECT"));
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT", "Project");
        } else if (strGroupCrit.equalsIgnoreCase("FINPR_BPartner_Category")) {
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT_ID",
              previousRow.getField("BP_GROUP"));
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT", "Business Partner Category");
        } else if (strGroupCrit.equalsIgnoreCase("INS_CURRENCY")) {
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT_ID",
              previousRow.getField("TRANS_CURRENCY"));
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT", "Currency");
        } else {
          FieldProviderFactory.setField(previousRow, "GROUP_CRIT_ID", "");
        }

      }
      if (previousRow != null) {
        // The current row has nothing to do with the previous one. Because of that, the
        // previous row has to be added to grouped data.
        if (previousRow.getField("ISRECEIPT").equalsIgnoreCase("Y"))
          FieldProviderFactory.setField(previousRow, "TRANS_AMOUNT", amountSum.toString());
        else
          FieldProviderFactory.setField(previousRow, "TRANS_AMOUNT", amountSum.negate().toString());
        FieldProviderFactory.setField(previousRow, "BALANCE", balanceSum.toString());
        if (previousConvRate == null) {
          if (previousRow.getField("ISRECEIPT").equalsIgnoreCase("Y"))
            FieldProviderFactory.setField(previousRow, "BASE_AMOUNT", amountSum.toString());
          else
            FieldProviderFactory
                .setField(previousRow, "BASE_AMOUNT", amountSum.negate().toString());
        } else {
          final int stdPrecission = previousConvRate.getToCurrency().getStandardPrecision()
              .intValue();
          if (previousRow.getField("ISRECEIPT").equalsIgnoreCase("Y"))
            FieldProviderFactory.setField(
                previousRow,
                "BASE_AMOUNT",
                amountSum.multiply(previousConvRate.getMultipleRateBy())
                    .setScale(stdPrecission, BigDecimal.ROUND_HALF_UP).toString());
          else
            FieldProviderFactory.setField(
                previousRow,
                "BASE_AMOUNT",
                amountSum.multiply(previousConvRate.getMultipleRateBy())
                    .setScale(stdPrecission, BigDecimal.ROUND_HALF_UP).negate().toString());
        }

        if (strAmountFrom.isEmpty() && strAmountTo.isEmpty()) {
          isAmtInLimit = true;
        } else if (!strAmountFrom.isEmpty() && strAmountTo.isEmpty()) {
          isAmtInLimit = Double.parseDouble(previousRow.getField("TRANS_AMOUNT")) >= Double
              .parseDouble(strAmountFrom);
        } else if (strAmountFrom.isEmpty() && !strAmountTo.isEmpty()) {
          isAmtInLimit = Double.parseDouble(previousRow.getField("TRANS_AMOUNT")) <= Double
              .parseDouble(strAmountTo);
        } else {
          isAmtInLimit = Double.parseDouble(previousRow.getField("TRANS_AMOUNT")) >= Double
              .parseDouble(strAmountFrom)
              && Double.parseDouble(previousRow.getField("TRANS_AMOUNT")) <= Double
                  .parseDouble(strAmountTo);
        }
        if (isAmtInLimit) {
          groupedData.add(previousRow);
          isAmtInLimit = false;
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return (FieldProvider[]) groupedData.toArray(new FieldProvider[groupedData.size()]);
  }

  private void fillLine(SimpleDateFormat dateFormat, FieldProvider data,
      FIN_PaymentScheduleDetail fIN_PaymentScheduleDetail, FIN_PaymentSchedule paymentSchedule,
      boolean creditPaysInvoice) {
    Date invoicedDate;
    long plannedDSO = 0;
    long currentDSO = 0;
    long currentTime = 0;
    // project
    if (paymentSchedule.getInvoice().getProject() != null)
      FieldProviderFactory.setField(data, "PROJECT", paymentSchedule.getInvoice().getProject()
          .getIdentifier());
    else
      FieldProviderFactory.setField(data, "PROJECT", "");
    // salesPerson
    if (paymentSchedule.getInvoice().getSalesRepresentative() != null) {
      FieldProviderFactory.setField(data, "SALES_PERSON", paymentSchedule.getInvoice()
          .getSalesRepresentative().getIdentifier());
    } else {
      FieldProviderFactory.setField(data, "SALES_PERSON", "");
    }
    // invoiceNumber
    FieldProviderFactory.setField(data, "INVOICE_NUMBER", (creditPaysInvoice ? "*" : "")
        + paymentSchedule.getInvoice().getDocumentNo());
    // payment plan id
    FieldProviderFactory.setField(data, "PAYMENT_PLAN_ID", paymentSchedule.getId());
    // payment plan yes / no
    FieldProviderFactory.setField(data, "PAYMENT_PLAN_Y_N", "");
    // payment plan yes / no
    FieldProviderFactory.setField(data, "NOT_PAYMENT_PLAN_Y_N", "Display:none");
    // invoiceDate
    invoicedDate = paymentSchedule.getInvoice().getInvoiceDate();
    FieldProviderFactory.setField(data, "INVOICE_DATE", dateFormat.format(invoicedDate).toString());
    // dueDate
    FieldProviderFactory.setField(data, "DUE_DATE", dateFormat.format(paymentSchedule.getDueDate())
        .toString());
    // plannedDSO
    plannedDSO = (paymentSchedule.getDueDate().getTime() - invoicedDate.getTime()) / milisecDayConv;
    FieldProviderFactory.setField(data, "PLANNED_DSO", String.valueOf(plannedDSO));
    // currentDSO
    if (fIN_PaymentScheduleDetail.getPaymentDetails() != null) {
      currentDSO = (fIN_PaymentScheduleDetail.getPaymentDetails().getFinPayment().getPaymentDate()
          .getTime() - invoicedDate.getTime())
          / milisecDayConv;
    } else {
      currentTime = System.currentTimeMillis();
      currentDSO = (currentTime - invoicedDate.getTime()) / milisecDayConv;
    }
    FieldProviderFactory.setField(data, "CURRENT_DSO", String.valueOf((currentDSO)));
    // daysOverdue
    FieldProviderFactory.setField(data, "OVERDUE", String.valueOf((currentDSO - plannedDSO)));
  }

  public ConversionRate getConversionRate(Currency transCurrency, Currency baseCurrency,
      String conversionDate) {

    java.util.List<ConversionRate> convRateList;
    ConversionRate convRate;
    Date conversionDateObj = FIN_Utility.getDate(conversionDate);

    OBContext.setAdminMode();
    try {

      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance().createCriteria(
          ConversionRate.class);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, transCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, baseCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDateObj));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDateObj));

      convRateList = obcConvRate.list();

      if ((convRateList != null) && (convRateList.size() != 0))
        convRate = convRateList.get(0);
      else
        convRate = null;

    } finally {
      OBContext.restorePreviousMode();
    }

    return convRate;
  }

  public String[] getReferenceListValues(String refName, boolean inclEmtyValue) {
    OBContext.setAdminMode();
    String values[];
    try {
      final OBCriteria<Reference> obc = OBDal.getInstance().createCriteria(Reference.class);
      obc.add(Restrictions.eq(Reference.PROPERTY_NAME, refName));
      final OBCriteria<List> obcValue = OBDal.getInstance().createCriteria(List.class);
      obcValue.add(Restrictions.eq(List.PROPERTY_REFERENCE, obc.list().get(0)));
      java.util.List<List> v = obcValue.list();
      int n = v.size();

      if (inclEmtyValue)
        values = new String[n + 1];
      else
        values = new String[n];

      for (int i = 0; i < n; i++)
        values[i] = v.get(i).getSearchKey();

      if (inclEmtyValue)
        values[values.length - 1] = new String("");

    } finally {
      OBContext.restorePreviousMode();
    }

    return values;
  }

  public static String translateRefList(String strCode) {
    String strMessage = "";
    OBContext.setAdminMode();
    try {
      Language language = OBContext.getOBContext().getLanguage();

      if (!"en_US".equals(language.getLanguage())) {
        OBCriteria<ListTrl> obcTrl = OBDal.getInstance().createCriteria(ListTrl.class);
        obcTrl.add(Restrictions.eq(ListTrl.PROPERTY_LANGUAGE, language));
        obcTrl.createAlias(ListTrl.PROPERTY_LISTREFERENCE, "lr");
        obcTrl.add(Restrictions.eq("lr." + List.PROPERTY_SEARCHKEY, strCode));
        obcTrl.setFilterOnReadableClients(false);
        obcTrl.setFilterOnReadableOrganization(false);
        strMessage = (obcTrl.list() != null && obcTrl.list().size() > 0) ? obcTrl.list().get(0)
            .getName() : null;
      }
      if ("en_US".equals(language.getLanguage()) || strMessage == null) {
        OBCriteria<List> obc = OBDal.getInstance().createCriteria(List.class);
        obc.setFilterOnReadableClients(false);
        obc.setFilterOnReadableOrganization(false);
        obc.add(Restrictions.eq(List.PROPERTY_SEARCHKEY, strCode));
        strMessage = (obc.list() != null && obc.list().size() > 0) ? obc.list().get(0).getName()
            : null;
      }

      if (strMessage == null || strMessage.equals(""))
        strMessage = strCode;
    } finally {
      OBContext.restorePreviousMode();
    }
    return Replace.replace(Replace.replace(strMessage, "\n", "\\n"), "\"", "&quot;");
  }

  public static HashMap<String, String> getLinkParameters(String adTableId, String isReceipt) {
    HashMap<String, String> hmValues = new HashMap<String, String>();

    OBContext.setAdminMode();
    try {
      Table adTable = OBDal.getInstance().get(Table.class, adTableId);

      Window adWindow = null;
      if (isReceipt.equalsIgnoreCase("Y")) {
        adWindow = adTable.getWindow();
      } else {
        adWindow = adTable.getPOWindow();
      }
      hmValues.put("adWindowName", adWindow.getName());

      java.util.List<Tab> adTabList = adWindow.getADTabList();
      for (int i = 0; i < adTabList.size(); i++) {
        if (adTabList.get(i).getTable().getId().equalsIgnoreCase(adTableId)) {
          hmValues.put("adTabName", adTabList.get(i).getName());
          hmValues.put("adTabId", adTabList.get(i).getId());
        }
      }

      java.util.List<Column> adColumnList = adTable.getADColumnList();
      for (int i = 0; i < adColumnList.size(); i++) {
        if (adColumnList.get(i).isKeyColumn()) {
          hmValues.put("adColumnName", adColumnList.get(i).getDBColumnName());
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return hmValues;
  }

  public static FieldProvider[] getObjectList(String objectNames) {
    Vector<Object> vector = new Vector<Object>(0);
    if (objectNames.equalsIgnoreCase("")) {
      FieldProvider objectListData[] = new FieldProvider[vector.size()];
      vector.copyInto(objectListData);

      return objectListData;
    } else {
      String[] names = objectNames.substring(1, objectNames.length() - 1).split(", ");
      SQLReturnObject sqlRO;
      String name = null;

      for (int i = 0; i < names.length; i++) {
        sqlRO = new SQLReturnObject();
        names[i] = names[i];
        name = names[i].substring(1, names[i].length() - 1);
        sqlRO.setData("ID", name);
        sqlRO.setData("NAME", FIN_Utility.messageBD(name));
        sqlRO.setData("DESCRIPTION", "");
        vector.addElement(sqlRO);
      }

      FieldProvider objectListData[] = new FieldProvider[vector.size()];
      vector.copyInto(objectListData);

      return objectListData;
    }
  }

  public java.util.List<Invoice> getInvoicesUsingCredit(final FIN_Payment payment) {
    final StringBuilder sql = new StringBuilder();
    final java.util.List<Invoice> result = new ArrayList<Invoice>();

    sql.append(" select distinct(pdv.invoicePaymentPlan.invoice.id) ");
    sql.append(" from FIN_Payment_Credit pc, FIN_Payment p0, ");
    sql.append("      FIN_Payment p1, FIN_Payment_Detail_V pdv  ");
    sql.append(" where p0.id=pc.creditPaymentUsed ");
    sql.append(" and pc.payment=p1.id ");
    sql.append(" and pdv.payment=p1.id ");
    sql.append(" and p0.id = '" + payment.getId() + "' ");

    try {
      OBContext.setAdminMode(true);
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(sql.toString());
      for (final Object o : query.list()) {
        result.add(OBDal.getInstance().get(Invoice.class, (String) o));
      }

      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public FIN_PaymentSchedule getInvoicePaymentSchedule(FIN_Payment credit_payment) {
    final StringBuilder sql = new StringBuilder();
    sql.append(" select ps ");
    sql.append(" from FIN_Payment_Credit pc, FIN_Payment_Detail_V pdv, ");
    sql.append(" FIN_Payment_Schedule ps ");
    sql.append(" where pc.payment = pdv.payment ");
    sql.append(" and ps.id = pdv.invoicePaymentPlan ");
    sql.append(" and pc.creditPaymentUsed.id = '" + credit_payment.getId() + "' ");

    try {
      OBContext.setAdminMode(true);
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(sql.toString());
      return (FIN_PaymentSchedule) query.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getInvoicesDocNos(Collection<Invoice> invoices) {
    final StringBuilder sb = new StringBuilder();
    for (Invoice i : invoices) {
      sb.append(i.getDocumentNo());
      sb.append(", ");
    }
    return sb.delete(sb.length() - 2, sb.length()).toString();
  }

  private BusinessPartner getDocumentBusinessPartner(FIN_PaymentScheduleDetail psd) {
    BusinessPartner bp = null;
    if (psd.getInvoicePaymentSchedule() != null) { // Invoice
      bp = psd.getInvoicePaymentSchedule().getInvoice().getBusinessPartner();
    }
    if (psd.getOrderPaymentSchedule() != null) { // Order
      bp = psd.getOrderPaymentSchedule().getOrder().getBusinessPartner();
    }
    if (bp == null) {
      bp = psd.getPaymentDetails().getFinPayment().getBusinessPartner();
    }
    return bp;
  }
}
