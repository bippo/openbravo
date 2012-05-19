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
 * All portions are Copyright (C) 2009-2012 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.financial.paymentreport.erpCommon.ad_reports;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.Utility;
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
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.project.Project;
import org.openbravo.utils.Replace;

public class PaymentReportDao {

  private static final long milisecDayConv = (1000 * 60 * 60 * 24);
  static Logger log4j = Logger.getLogger(Utility.class);
  private java.util.List<String> bpList;
  private java.util.List<String> bpCategoryList;
  private java.util.List<String> projectList;

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

    try {
      return getPaymentReport(vars, strOrg, strInclSubOrg, strDueDateFrom, strDueDateTo,
          strAmountFrom, strAmountTo, strDocumentDateFrom, strDocumentDateTo, strcBPartnerIdIN,
          strcBPGroupIdIN, "include", strcProjectIdIN, strfinPaymSt, strPaymentMethodId,
          strFinancialAccountId, strcCurrency, strConvertCurrency, strConversionDate, strPaymType,
          strOverdue, strGroupCrit, strOrdCrit, "Y", "", "");
    } catch (OBException e) {
      FieldProvider[] fp = new FieldProvider[1];
      HashMap<String, String> hm = new HashMap<String, String>();
      hm.put("transCurrency", strcCurrency);
      hm.put("baseCurrency", strConvertCurrency);
      hm.put("conversionDate", strConversionDate);

      fp[0] = new FieldProviderFactory(hm);
      FieldProvider[] data = fp;

      OBContext.restorePreviousMode();
      return data;
    }
  }

  public FieldProvider[] getPaymentReport(VariablesSecureApp vars, String strOrg,
      String strInclSubOrg, String strDueDateFrom, String strDueDateTo, String strAmountFrom,
      String strAmountTo, String strDocumentDateFrom, String strDocumentDateTo,
      String strcBPartnerIdIN, String strcBPGroupIdIN, String strcProjectIdIN, String strfinPaymSt,
      String strPaymentMethodId, String strFinancialAccountId, String strcCurrency,
      String strConvertCurrency, String strConversionDate, String strPaymType, String strOverdue,
      String strGroupCrit, String strOrdCrit, String strInclPaymentUsingCredit) {

    try {
      return getPaymentReport(vars, strOrg, strInclSubOrg, strDueDateFrom, strDueDateTo,
          strAmountFrom, strAmountTo, strDocumentDateFrom, strDocumentDateTo, strcBPartnerIdIN,
          strcBPGroupIdIN, "include", strcProjectIdIN, strfinPaymSt, strPaymentMethodId,
          strFinancialAccountId, strcCurrency, strConvertCurrency, strConversionDate, strPaymType,
          strOverdue, strGroupCrit, strOrdCrit, strInclPaymentUsingCredit, "", "");
    } catch (OBException e) {
      FieldProvider[] fp = new FieldProvider[1];
      HashMap<String, String> hm = new HashMap<String, String>();
      hm.put("transCurrency", strcCurrency);
      hm.put("baseCurrency", strConvertCurrency);
      hm.put("conversionDate", strConversionDate);

      fp[0] = new FieldProviderFactory(hm);
      FieldProvider[] data = fp;

      OBContext.restorePreviousMode();
      return data;
    }
  }

  public FieldProvider[] getPaymentReport(VariablesSecureApp vars, String strOrg,
      String strInclSubOrg, String strDueDateFrom, String strDueDateTo, String strAmountFrom,
      String strAmountTo, String strDocumentDateFrom, String strDocumentDateTo,
      String strcBPartnerIdIN, String strcBPGroupIdIN, String strcNoBusinessPartner,
      String strcProjectIdIN, String strfinPaymSt, String strPaymentMethodId,
      String strFinancialAccountId, String strcCurrency, String strConvertCurrency,
      String strConversionDate, String strPaymType, String strOverdue, String strGroupCrit,
      String strOrdCrit, String strInclPaymentUsingCredit, String strPaymentDateFrom,
      String strPaymentDateTo) throws OBException {

    final StringBuilder hsqlScript = new StringBuilder();
    final java.util.List<Object> parameters = new ArrayList<Object>();

    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    FieldProvider[] data;
    FieldProvider[] transactionData;
    Currency transCurrency;
    BigDecimal transAmount = null;
    ConversionRate convRate = null;
    ArrayList<FieldProvider> groupedData = new ArrayList<FieldProvider>();
    ArrayList<FieldProvider> totalData = new ArrayList<FieldProvider>();
    int numberOfElements = 0;
    int lastElement = 0;
    boolean existsConvRate = false;

    OBContext.setAdminMode(true);
    try {

      hsqlScript
          .append("select fpsd.id, (select a.sequenceNumber from ADList a where a.reference.id = '575BCB88A4694C27BC013DE9C73E6FE7' and a.searchKey = coalesce(pay.status, 'RPAP')) as a,");
      hsqlScript
          .append(" (select trans.id from FIN_Finacc_Transaction trans left outer join trans.finPayment payment where payment.id=pay.id) as trans ");
      hsqlScript.append(" from FIN_Payment_ScheduleDetail as fpsd ");
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

      hsqlScript.append(" and fpsd.");
      hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_ORGANIZATION);
      hsqlScript.append(".id in ");
      hsqlScript.append(concatOrganizations(OBContext.getOBContext().getReadableOrganizations()));

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

      // payment date from - payment date to
      if (!strPaymentDateFrom.isEmpty()) {
        hsqlScript.append(" and ((pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        hsqlScript.append(" > ?)  or (pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        hsqlScript.append(" is null and invps.");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        hsqlScript.append(" >= ?))");
        parameters.add(FIN_Utility.getDate(strPaymentDateFrom));
        parameters.add(FIN_Utility.getDate(strPaymentDateFrom));
      }
      if (!strPaymentDateTo.isEmpty()) {
        hsqlScript.append(" and coalesce(pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        hsqlScript.append(", invps.");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        hsqlScript.append(") <= ?");
        parameters.add(FIN_Utility.getDate(strPaymentDateTo));
      }

      // Empty Business Partner included
      if (strcNoBusinessPartner.equals("include")) {

        // business partner
        if (!strcBPartnerIdIN.isEmpty()) {
          hsqlScript.append(" and ((coalesce(pay.");
          hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
          hsqlScript.append(", inv.");
          hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
          hsqlScript.append(") in ");
          hsqlScript.append(strcBPartnerIdIN);
          hsqlScript.append(") or (pay.");
          hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
          hsqlScript.append(" is null and inv.");
          hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
          hsqlScript.append(" is null))");
        }
        // business partner category
        if (!strcBPGroupIdIN.isEmpty()) {
          hsqlScript.append(" and (coalesce(paybpc, invbpc) = '");
          hsqlScript.append(strcBPGroupIdIN);
          hsqlScript.append("' or (pay.");
          hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
          hsqlScript.append(" is null and inv.");
          hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
          hsqlScript.append(" is null))");
        }

        // Empty Businesss Partner excluded
      } else if (strcNoBusinessPartner.equals("exclude")) {

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
        // exclude empty business partner
        if (strcBPartnerIdIN.isEmpty() && strcBPGroupIdIN.isEmpty()) {
          hsqlScript.append(" and (pay.");
          hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
          hsqlScript.append(" is not null or inv.");
          hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
          hsqlScript.append(" is not null) ");
        }

        // Only Empty Business Partner
      } else {// if ((strcNoBusinessPartner.equals("only")))
        hsqlScript.append(" and pay.");
        hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(" is null and inv.");
        hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(" is null ");
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
        parameters.add(DateUtils.truncate(new Date(), Calendar.DATE));
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

      hsqlScript.append(" a, coalesce(pay.");
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

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hsqlScript.toString());
      int pos = 0;
      for (final Object param : parameters) {
        if (param instanceof BaseOBObject) {
          query.setEntity(pos++, param);
        } else {
          query.setParameter(pos++, param);
        }
      }

      HashMap<String, FIN_FinaccTransaction> hashMapTransactions = new HashMap<String, FIN_FinaccTransaction>();
      int index = 0;
      java.util.List<FIN_PaymentScheduleDetail> obqPSDList = new ArrayList<FIN_PaymentScheduleDetail>();
      for (Object resultObject : query.list()) {
        if (resultObject.getClass().isArray()) {
          final Object[] values = (Object[]) resultObject;
          String StringPSDId = "";
          for (Object value : values) {
            if (index == 0) {
              obqPSDList.add(OBDal.getInstance().get(FIN_PaymentScheduleDetail.class,
                  (String) value));
              StringPSDId = (String) value;
            } else if (index == 2) {
              if (value != null) {
                hashMapTransactions.put(StringPSDId,
                    OBDal.getInstance().get(FIN_FinaccTransaction.class, value));
              }
              index = -1;// firstMember = true;
            }
            index++;
          }
        }
      }
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

      // Before processing the data the Transactions without a Payment associated are recovered
      java.util.List<FIN_FinaccTransaction> transactionsList = getTransactionsList(strInclSubOrg,
          strOrg, strcBPartnerIdIN, strFinancialAccountId, strDocumentDateFrom, strDocumentDateTo,
          strPaymentDateFrom, strPaymentDateTo, strAmountFrom, strAmountTo, strcBPGroupIdIN,
          strcProjectIdIN, strfinPaymSt, strcCurrency, strPaymType, strGroupCrit, strOrdCrit,
          strcNoBusinessPartner);

      transactionData = FieldProviderFactory.getFieldProviderArray(transactionsList);
      int totalTransElements = transactionsList.size();

      // There are three variables involved in this loop. The first one is data, wich is the
      // the one the loop processes. Then grouped data is used to group similar data lines into
      // one. Finally total data adds the remaining information that is not in data.
      for (int i = 0; i < data.length; i++) {

        // If the payment schedule detail has a payment detail, then, the information is taken from
        // the payment. If not, the information is taken from the invoice (the else).
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
          FieldProviderFactory.setField(data[i], "STATUS_CODE", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getStatus());
          // is receipt
          if (FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment().isReceipt()) {
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "Y");
            isReceipt = true;
          } else {
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "N");
            isReceipt = false;
          }
          // deposit/withdraw date
          if (hashMapTransactions.containsKey(FIN_PaymentScheduleDetail[i].getId().toString())) {
            FieldProviderFactory.setField(data[i], "DEPOSIT_WITHDRAW_DATE", dateFormat
                .format(hashMapTransactions.get(FIN_PaymentScheduleDetail[i].getId())
                    .getTransactionDate()));
          } else {
            FieldProviderFactory.setField(data[i], "DEPOSIT_WITHDRAW_DATE", "");
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
          FieldProviderFactory.setField(data[i], "PAYMENT_METHOD", FIN_PaymentScheduleDetail[i]
              .getInvoicePaymentSchedule().getFinPaymentmethod().getIdentifier());
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
          FieldProviderFactory.setField(data[i], "STATUS_CODE", "RPAP");
          // is receipt
          if (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
              .isSalesTransaction()) {
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "Y");
            isReceipt = true;
          } else {
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "N");
            isReceipt = false;
          }
          // deposit/withdraw date
          FieldProviderFactory.setField(data[i], "DEPOSIT_WITHDRAW_DATE", "");
        }

        /*
         * - If the payment schedule detail has an invoice, the line is filled normally.
         * 
         * - If it has a payment it does not have an invoice or it should have entered the first if,
         * thus, it is a credit payment. If it is a credit payment, it is checked whether it pays
         * one or multiple invoices. If it is one, the information of that invoice is provided. If
         * not, it is filled with '**'.
         * 
         * - Otherwise, it is filled empty.
         */
        if (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule() != null) {
          fillLine(dateFormat, data[i], FIN_PaymentScheduleDetail[i],
              FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule(), false);
        } else if (FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment() != null) {
          java.util.List<Invoice> invoices = getInvoicesUsingCredit(FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment());
          if (invoices.size() == 1) {
            java.util.List<FIN_PaymentSchedule> ps = getInvoicePaymentSchedules(FIN_PaymentScheduleDetail[i]
                .getPaymentDetails().getFinPayment());
            fillLine(dateFormat, data[i], FIN_PaymentScheduleDetail[i], ps.get(0), true);
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
            String message = transCurrency.getISOCode() + " -> " + baseCurrency.getISOCode() + " "
                + strConversionDate;
            throw new OBException(message);
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
              numberOfElements++;
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

        // Insert the transactions without payment if necessary
        if (lastElement != numberOfElements) {
          if (transactionsList.size() > 0) {
            try {
              existsConvRate = insertIntoTotal(groupedData.get(lastElement), transactionsList,
                  totalData, strGroupCrit, strOrdCrit, transactionData, totalTransElements,
                  strConvertCurrency, strConversionDate);
            } catch (OBException e) {
              // If there is no conversion rate
              throw e;
            }
          }
          totalData.add(groupedData.get(lastElement));
          lastElement++;
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
          numberOfElements++;
        }
      }

      // Insert the transactions without payment if necessary
      if (lastElement != numberOfElements) {
        if (transactionsList.size() > 0) {
          try {
            existsConvRate = insertIntoTotal(groupedData.get(lastElement), transactionsList,
                totalData, strGroupCrit, strOrdCrit, transactionData, totalTransElements,
                strConvertCurrency, strConversionDate);
          } catch (OBException e) {
            // If there is no conversion rate
            throw e;
          }
        }
        totalData.add(groupedData.get(lastElement));
        lastElement++;
      }

      // Insert the remaining transactions wihtout payment if necessary
      while (transactionsList.size() > 0) {
        try {
          transactionData[totalTransElements - transactionsList.size()] = createFieldProviderForTransaction(
              transactionsList.get(0),
              transactionData[totalTransElements - transactionsList.size()], strGroupCrit,
              strConvertCurrency, strConversionDate);
        } catch (OBException e) {
          // If there is no conversion rate
          throw e;
        }
        totalData.add(transactionData[totalTransElements - transactionsList.size()]);
        transactionsList.remove(0);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return (FieldProvider[]) totalData.toArray(new FieldProvider[totalData.size()]);
  }

  /**
   * This method combines the information from the transactions list and the last element inserted
   * into grouped data into total data.
   * 
   * @param fieldProvider
   * @param transactionsList
   * @param totalData
   * @throws OBException
   */
  private boolean insertIntoTotal(FieldProvider data,
      java.util.List<FIN_FinaccTransaction> transactionsList, ArrayList<FieldProvider> totalData,
      String strGroupCrit, String strOrdCrit, FieldProvider[] transactionData,
      int totalTransElements, String strConvertCurrency, String strConversionDate)
      throws OBException {

    while (transactionsList.size() > 0
        && transactionIsBefore(transactionsList.get(0), data, strGroupCrit, strOrdCrit)) {
      try {
        transactionData[totalTransElements - transactionsList.size()] = createFieldProviderForTransaction(
            transactionsList.get(0), transactionData[totalTransElements - transactionsList.size()],
            strGroupCrit, strConvertCurrency, strConversionDate);
      } catch (OBException e) {
        // If there is no conversion rate
        throw e;
      }
      totalData.add(transactionData[totalTransElements - transactionsList.size()]);
      transactionsList.remove(0);
    }
    return true;
  }

  /**
   * This method creates a field provider with the information of the transaction
   * 
   * @param fin_FinaccTransaction
   * @return
   * @throws OBException
   */
  private FieldProvider createFieldProviderForTransaction(FIN_FinaccTransaction transaction,
      FieldProvider transactionData, String strGroupCrit, String strConvertCurrency,
      String strConversionDate) throws OBException {
    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    BigDecimal transAmount = null;
    ConversionRate convRate = null;

    // bp_group -- bp_category
    if (transaction.getBusinessPartner() != null) {
      FieldProviderFactory.setField(transactionData, "BP_GROUP", transaction.getBusinessPartner()
          .getBusinessPartnerCategory().getName());
      // bpartner
      FieldProviderFactory.setField(transactionData, "BPARTNER", transaction.getBusinessPartner()
          .getName());
    } else {
      // bp_group -- bp_category & bpartner
      FieldProviderFactory.setField(transactionData, "BP_GROUP", "");
      FieldProviderFactory.setField(transactionData, "BPARTNER", "");
    }
    // transCurrency
    FieldProviderFactory.setField(transactionData, "TRANS_CURRENCY", transaction.getCurrency()
        .getISOCode());
    // paymentMethod
    FieldProviderFactory.setField(transactionData, "PAYMENT_METHOD", "");
    // payment
    FieldProviderFactory.setField(transactionData, "PAYMENT", "");
    // description
    FieldProviderFactory.setField(transactionData, "PAYMENT_DESC", transaction.getDescription());
    // payment_id
    FieldProviderFactory.setField(transactionData, "PAYMENT_ID", "");
    // payment_date
    FieldProviderFactory.setField(transactionData, "PAYMENT_DATE",
        dateFormat.format(transaction.getDateAcct()));
    // payment_docNo
    FieldProviderFactory.setField(transactionData, "PAYMENT_DOCNO", "");
    // payment yes / no
    FieldProviderFactory.setField(transactionData, "PAYMENT_Y_N", "Display:None");
    // financialAccount
    FieldProviderFactory.setField(transactionData, "FINANCIAL_ACCOUNT", transaction.getAccount()
        .getIdentifier());
    // status
    FieldProviderFactory.setField(transactionData, "STATUS",
        translateRefList(transaction.getStatus()));
    FieldProviderFactory.setField(transactionData, "STATUS_CODE", transaction.getStatus());
    // is receipt
    if (transaction.getStatus().equals("PWNC")) {
      FieldProviderFactory.setField(transactionData, "ISRECEIPT", "Y");
      // isReceipt = true;
    } else if (transaction.getStatus().equals("RDNC")) {
      FieldProviderFactory.setField(transactionData, "ISRECEIPT", "N");
      // isReceipt = false;
    }
    // deposit/withdraw date
    FieldProviderFactory.setField(transactionData, "DEPOSIT_WITHDRAW_DATE",
        dateFormat.format(transaction.getDateAcct()));
    // project
    FieldProviderFactory.setField(transactionData, "PROJECT", "");
    // salesPerson
    FieldProviderFactory.setField(transactionData, "SALES_PERSON", "");
    // invoiceNumber.
    FieldProviderFactory.setField(transactionData, "INVOICE_NUMBER", "");
    // payment plan id
    FieldProviderFactory.setField(transactionData, "PAYMENT_PLAN_ID", "");
    // payment plan yes / no
    FieldProviderFactory.setField(transactionData, "PAYMENT_PLAN_Y_N", "Display:none");
    // payment plan yes / no
    FieldProviderFactory.setField(transactionData, "NOT_PAYMENT_PLAN_Y_N", "Display:none");
    // invoiceDate
    FieldProviderFactory.setField(transactionData, "INVOICE_DATE", "");
    // dueDate.
    FieldProviderFactory.setField(transactionData, "DUE_DATE",
        dateFormat.format(transaction.getDateAcct()));
    // plannedDSO
    FieldProviderFactory.setField(transactionData, "PLANNED_DSO", "0");
    // currentDSO
    FieldProviderFactory.setField(transactionData, "CURRENT_DSO", "0");
    // daysOverdue
    FieldProviderFactory.setField(transactionData, "OVERDUE", "0");

    // transactional and base amounts
    transAmount = transaction.getDepositAmount().subtract(transaction.getPaymentAmount());

    Currency baseCurrency = OBDal.getInstance().get(Currency.class, strConvertCurrency);

    boolean sameCurrency = baseCurrency.getISOCode().equalsIgnoreCase(
        transaction.getCurrency().getISOCode());

    if (!sameCurrency) {
      convRate = this.getConversionRate(transaction.getCurrency(), baseCurrency, strConversionDate);

      if (convRate != null) {
        final int stdPrecission = convRate.getToCurrency().getStandardPrecision().intValue();
        FieldProviderFactory.setField(transactionData, "TRANS_AMOUNT", transAmount.toString());
        FieldProviderFactory.setField(
            transactionData,
            "BASE_AMOUNT",
            transAmount.multiply(convRate.getMultipleRateBy())
                .setScale(stdPrecission, BigDecimal.ROUND_HALF_UP).toString());
      } else {
        String message = transaction.getCurrency().getISOCode() + " -> "
            + baseCurrency.getISOCode() + " " + strConversionDate;

        throw new OBException(message);
      }
    } else {
      // convRate = null;
      FieldProviderFactory.setField(transactionData, "TRANS_AMOUNT", transAmount.toString());
      FieldProviderFactory.setField(transactionData, "BASE_AMOUNT", transAmount.toString());
    }
    // currency
    FieldProviderFactory.setField(transactionData, "BASE_CURRENCY", baseCurrency.getISOCode());
    // group_crit_id this is the column that has the ids of the grouping criteria selected
    if (strGroupCrit.equalsIgnoreCase("APRM_FATS_BPARTNER")) {
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT_ID",
          transactionData.getField("BPARTNER"));
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT", "Business Partner");
    } else if (strGroupCrit.equalsIgnoreCase("Project")) {
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT_ID",
          transactionData.getField("PROJECT"));
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT", "Project");
    } else if (strGroupCrit.equalsIgnoreCase("FINPR_BPartner_Category")) {
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT_ID",
          transactionData.getField("BP_GROUP"));
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT", "Business Partner Category");
    } else if (strGroupCrit.equalsIgnoreCase("INS_CURRENCY")) {
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT_ID",
          transactionData.getField("TRANS_CURRENCY"));
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT", "Currency");
    } else {
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT_ID", "");
      FieldProviderFactory.setField(transactionData, "GROUP_CRIT", "");
    }

    return transactionData;
  }

  /**
   * This method compares the transaction element with the previous data element and returns true if
   * the transaction goes before according to the comparing parameters
   */
  private boolean transactionIsBefore(FIN_FinaccTransaction transaction, FieldProvider data,
      String strGroupCrit, String strOrdCrit) {

    boolean isBefore = false;
    String BPName = "";
    String BPCategory = "";
    String strProject = "";
    if (transaction.getBusinessPartner() != null) {
      BPName = transaction.getBusinessPartner().getName().toString();
      BPCategory = transaction.getBusinessPartner().getBusinessPartnerCategory().getName()
          .toString();
    }
    if (transaction.getProject() != null) {
      strProject = transaction.getProject().getId().toString();
    }

    if (!strGroupCrit.equals("")) {

      // General boolean rule for comparation when A!=B -->[ (A<B || B="") && A!="" ]
      if (strGroupCrit.equalsIgnoreCase("APRM_FATS_BPARTNER")) {
        if (bpList == null) {
          createBPList();
        }
        int posData = bpList.indexOf(data.getField("BPARTNER"));
        int pos = bpList.indexOf(BPName);

        if (BPName.equals(data.getField("BPARTNER"))) {
          isBefore = isBeforeStatusAndOrder(transaction, data, strOrdCrit, BPName, BPCategory,
              strProject);
        } else if ((pos < posData || data.getField("BPARTNER").equals("")) && !BPName.equals("")) {
          isBefore = true;
        }
      } else if (strGroupCrit.equalsIgnoreCase("Project")) {
        if (projectList == null) {
          createProjectList();
        }
        int posData = projectList.indexOf(data.getField("PROJECT"));
        int pos = projectList.indexOf(strProject);

        if (strProject.equals(data.getField("PROJECT"))) {
          isBefore = isBeforeStatusAndOrder(transaction, data, strOrdCrit, BPName, BPCategory,
              strProject);
        } else if ((pos < posData || data.getField("PROJECT").equals("")) && !strProject.equals("")) {
          isBefore = true;
        }
      } else if (strGroupCrit.equalsIgnoreCase("FINPR_BPartner_Category")) {
        if (bpCategoryList == null) {
          createBPCategoryList();
        }
        int posData = bpList.indexOf(data.getField("BP_GROUP"));
        int pos = bpList.indexOf(BPCategory);

        if (BPCategory.equals(data.getField("BP_GROUP"))) {
          isBefore = isBeforeStatusAndOrder(transaction, data, strOrdCrit, BPName, BPCategory,
              strProject);
        } else if ((pos < posData || data.getField("BP_GROUP").equals(""))
            && !BPCategory.equals("")) {
          isBefore = true;
        }
      } else if (strGroupCrit.equalsIgnoreCase("INS_CURRENCY")) {
        if (transaction.getCurrency().getISOCode().toString()
            .compareTo(data.getField("TRANS_CURRENCY")) == 0) {
          isBefore = isBeforeStatusAndOrder(transaction, data, strOrdCrit, BPName, BPCategory,
              strProject);
        } else if (transaction.getCurrency().getISOCode().toString()
            .compareTo(data.getField("TRANS_CURRENCY")) < 0) {
          isBefore = true;
        }
      }

    } else {
      isBefore = isBeforeStatusAndOrder(transaction, data, strOrdCrit, BPName, BPCategory,
          strProject);
    }
    return isBefore;
  }

  /**
   * This method compares the status and the order criteria of the transactions and data
   * 
   * @param transaction
   * @param data
   * @param strOrdCrit
   * @return
   */
  private boolean isBeforeStatusAndOrder(FIN_FinaccTransaction transaction, FieldProvider data,
      String strOrdCrit, String BPName, String BPCategory, String strProject) {
    boolean isBefore = false;

    if (transaction.getStatus().toString().equals(data.getField("STATUS_CODE"))) {
      if (!strOrdCrit.isEmpty()) {
        String[] strOrdCritList = strOrdCrit.substring(2, strOrdCrit.length() - 2).split("', '");
        isBefore = isBeforeOrder(transaction, data, strOrdCritList, 0, BPName, BPCategory,
            strProject);
      }
    } else if (isBeforeStatus(transaction.getStatus().toString(), data.getField("STATUS_CODE"))) {
      isBefore = true;
    }
    return isBefore;
  }

  /**
   * This method compares recursively the order criteria of the transactions and data
   * 
   * @param transaction
   * @param data
   * @param strOrdCrit
   * @return
   */
  private boolean isBeforeOrder(FIN_FinaccTransaction transaction, FieldProvider data,
      String[] strOrdCritList, int i, String BPName, String BPCategory, String strProject) {
    boolean isBefore = false;

    if (i == strOrdCritList.length - 1) {
      if (strOrdCritList[i].contains("Project")) {
        if (projectList == null) {
          createProjectList();
        }
        int posData = projectList.indexOf(data.getField("PROJECT"));
        int pos = projectList.indexOf(strProject);

        isBefore = isBefore
            || (((pos < posData) || data.getField("PROJECT").equals("")) && !strProject.equals(""));
      }
      if (strOrdCritList[i].contains("FINPR_BPartner_Category")) {
        if (bpCategoryList == null) {
          createBPCategoryList();
        }
        int posData = bpCategoryList.indexOf(data.getField("BP_GROUP"));
        int pos = bpCategoryList.indexOf(BPCategory);

        isBefore = isBefore
            || (((pos < posData) || data.getField("BP_GROUP").equals("")) && !BPCategory.equals(""));
      }
      if (strOrdCritList[i].contains("APRM_FATS_BPARTNER")) {
        if (bpList == null) {
          createBPList();
        }
        int posData = bpList.indexOf(data.getField("BPARTNER"));
        int pos = bpList.indexOf(BPName);

        isBefore = isBefore
            || (((pos < posData) || data.getField("BPARTNER").equals("")) && !BPName.equals(""));
      }
      if (strOrdCritList[i].contains("INS_CURRENCY")) {
        isBefore = isBefore
            || (transaction.getCurrency().getISOCode().toString()
                .compareTo(data.getField("TRANS_CURRENCY")) < 0);
      }
      return isBefore;
    } else {
      if (strOrdCritList[i].contains("Project")) {
        if (projectList == null) {
          createProjectList();
        }
        int posData = projectList.indexOf(data.getField("PROJECT"));
        int pos = projectList.indexOf(strProject);

        if ((pos < posData || data.getField("PROJECT").equals("")) && !strProject.equals("")) {
          isBefore = true;
        } else if (strProject.equals(data.getField("PROJECT"))) {
          isBefore = isBeforeOrder(transaction, data, strOrdCritList, i + 1, BPName, BPCategory,
              strProject);
        }
      } else if (strOrdCritList[i].contains("FINPR_BPartner_Category")) {
        if (bpCategoryList == null) {
          createBPCategoryList();
        }
        int posData = bpCategoryList.indexOf(data.getField("BP_GROUP"));
        int pos = bpCategoryList.indexOf(BPCategory);

        if ((pos < posData || data.getField("BP_GROUP").equals("")) && !BPCategory.equals("")) {
          isBefore = true;
        } else if (BPCategory.toString().equals(data.getField("BP_GROUP"))) {
          isBefore = isBeforeOrder(transaction, data, strOrdCritList, i + 1, BPName, BPCategory,
              strProject);
        }
      } else if (strOrdCritList[i].contains("APRM_FATS_BPARTNER")) {
        if (bpList == null) {
          createBPList();
        }
        int posData = bpList.indexOf(data.getField("BPARTNER"));
        int pos = bpList.indexOf(BPName);

        if ((pos < posData || data.getField("BPARTNER").equals("")) && !BPName.equals("")) {
          isBefore = true;
        } else if (BPName.equals(data.getField("BPARTNER"))) {
          isBefore = isBeforeOrder(transaction, data, strOrdCritList, i + 1, BPName, BPCategory,
              strProject);
        }
      } else if (strOrdCritList[i].contains("INS_CURRENCY")) {
        if (transaction.getCurrency().getISOCode().toString()
            .compareTo(data.getField("TRANS_CURRENCY")) < 0) {
          isBefore = true;
        } else if (transaction.getCurrency().getISOCode().toString()
            .compareTo(data.getField("TRANS_CURRENCY")) == 0) {
          isBefore = isBeforeOrder(transaction, data, strOrdCritList, i + 1, BPName, BPCategory,
              strProject);
        }
      } else if (strOrdCritList[i].contains("Date")) {
        Date dataDate = FIN_Utility.getDate(data.getField("INVOICE_DATE"));
        if (dataDate != null) {
          isBefore = false;
        } else {
          isBefore = isBeforeOrder(transaction, data, strOrdCritList, i + 1, BPName, BPCategory,
              strProject);
        }
      }
      return isBefore;
    }
  }

  /**
   * Compares two DIFFERENT payment status. If the first one goes before the second it returns true,
   * elsewise it returns false
   * 
   * @param firstValue
   * @param secondValue
   * @return
   */
  private boolean isBeforeStatus(String firstValue, String secondValue) {
    String[] strStatus = { firstValue, secondValue };
    boolean isBefore = false;

    OBContext.setAdminMode(true);
    try {
      OBCriteria<List> obCriteria = OBDal.getInstance().createCriteria(List.class);
      obCriteria.createAlias(List.PROPERTY_REFERENCE, "r", OBCriteria.LEFT_JOIN);
      obCriteria.add(Restrictions.ilike("r."
          + org.openbravo.model.ad.domain.Reference.PROPERTY_NAME, "FIN_Payment status"));
      obCriteria.add(Restrictions.in(List.PROPERTY_SEARCHKEY, strStatus));
      obCriteria.addOrderBy(List.PROPERTY_SEQUENCENUMBER, true);
      obCriteria.addOrderBy(List.PROPERTY_SEARCHKEY, true);
      final java.util.List<List> statusList = obCriteria.list();
      List status = statusList.get(0);
      if (status.getSearchKey().equals(firstValue)) {
        isBefore = true;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return isBefore;
  }

  /**
   * 
   * This method returns a list of transactions without a payment associated
   */
  private java.util.List<FIN_FinaccTransaction> getTransactionsList(String strInclSubOrg,
      String strOrg, String strcBPartnerIdIN, String strFinancialAccountId,
      String strDocumentDateFrom, String strDocumentDateTo, String strPaymentDateFrom,
      String strPaymentDateTo, String strAmountFrom, String strAmountTo, String strcBPGroupIdIN,
      String strcProjectIdIN, String strfinPaymSt, String strcCurrency, String strPaymType,
      String strGroupCrit, String strOrdCrit, String strcNoBusinessPartner) {
    Organization[] organizations;
    if (strInclSubOrg.equalsIgnoreCase("include")) {
      Set<String> orgChildTree = OBContext.getOBContext().getOrganizationStructureProvider()
          .getChildTree(strOrg, true);
      organizations = getOrganizations(orgChildTree);
    } else {
      organizations = new Organization[1];
      organizations[0] = OBDal.getInstance().get(Organization.class, strOrg);
    }
    java.util.List<BusinessPartner> bPartners = OBDao.getOBObjectListFromString(
        BusinessPartner.class, strcBPartnerIdIN);
    java.util.List<Project> projects = OBDao.getOBObjectListFromString(Project.class,
        strcProjectIdIN);
    OBContext.setAdminMode(true);
    try {
      OBCriteria<FIN_FinaccTransaction> obCriteriaTrans = OBDal.getInstance().createCriteria(
          FIN_FinaccTransaction.class);
      obCriteriaTrans.createAlias(FIN_FinaccTransaction.PROPERTY_BUSINESSPARTNER, "bp",
          OBCriteria.LEFT_JOIN);
      obCriteriaTrans.createAlias("bp." + BusinessPartner.PROPERTY_BUSINESSPARTNERCATEGORY, "bpc",
          OBCriteria.LEFT_JOIN);
      obCriteriaTrans
          .createAlias(FIN_FinaccTransaction.PROPERTY_PROJECT, "p", OBCriteria.LEFT_JOIN);
      obCriteriaTrans.createAlias(FIN_FinaccTransaction.PROPERTY_CURRENCY, "c",
          OBCriteria.LEFT_JOIN);
      obCriteriaTrans.add(Restrictions.isNull(FIN_FinaccTransaction.PROPERTY_FINPAYMENT));
      obCriteriaTrans.add(Restrictions.in(FIN_FinaccTransaction.PROPERTY_ORGANIZATION,
          organizations));

      // Empty Business Partner included
      if (strcNoBusinessPartner.equals("include")) {

        // BPartners
        if (!bPartners.isEmpty()) {
          obCriteriaTrans.add(Restrictions.or(
              Restrictions.in(FIN_FinaccTransaction.PROPERTY_BUSINESSPARTNER, bPartners),
              Restrictions.isNull(FIN_FinaccTransaction.PROPERTY_BUSINESSPARTNER)));
        }

        // BPartner Category
        if (!strcBPGroupIdIN.equals("")) {
          obCriteriaTrans.add(Restrictions.or(Restrictions.eq("bp."
              + BusinessPartner.PROPERTY_BUSINESSPARTNERCATEGORY, strcBPGroupIdIN), Restrictions
              .isNull(FIN_FinaccTransaction.PROPERTY_BUSINESSPARTNER)));
        }

        // Empty Business Partner excluded
      } else if (strcNoBusinessPartner.equals("exclude")) {

        // BPartners
        if (!bPartners.isEmpty()) {
          obCriteriaTrans.add(Restrictions.in(FIN_FinaccTransaction.PROPERTY_BUSINESSPARTNER,
              bPartners));
        }

        // BPartner Category
        if (!strcBPGroupIdIN.equals("")) {
          obCriteriaTrans.add(Restrictions.eq("bp."
              + BusinessPartner.PROPERTY_BUSINESSPARTNERCATEGORY, strcBPGroupIdIN));
        }

        if (bPartners.isEmpty() && strcBPGroupIdIN.equals("")) {
          obCriteriaTrans.add(Restrictions
              .isNotNull(FIN_FinaccTransaction.PROPERTY_BUSINESSPARTNER));
        }

        // Only empty Business Partners
      } else { // if if (strcNoBusinessPartner.equals("only"))
        obCriteriaTrans.add(Restrictions.isNull(FIN_FinaccTransaction.PROPERTY_BUSINESSPARTNER));
      }

      // Financial Account
      if (!strFinancialAccountId.equals("")) {
        obCriteriaTrans.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_ACCOUNT, OBDal
            .getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId)));
      }

      // Document Date & Payment Date
      if (!strDocumentDateFrom.equals("")) {
        obCriteriaTrans.add(Restrictions.ge(FIN_FinaccTransaction.PROPERTY_DATEACCT,
            FIN_Utility.getDate(strDocumentDateFrom)));
      }
      if (!strDocumentDateTo.equals("")) {
        obCriteriaTrans.add(Restrictions.le(FIN_FinaccTransaction.PROPERTY_DATEACCT,
            FIN_Utility.getDate(strDocumentDateTo)));
      }
      if (!strPaymentDateFrom.equals("")) {
        obCriteriaTrans.add(Restrictions.ge(FIN_FinaccTransaction.PROPERTY_DATEACCT,
            FIN_Utility.getDate(strPaymentDateFrom)));
      }
      if (!strPaymentDateTo.equals("")) {
        obCriteriaTrans.add(Restrictions.le(FIN_FinaccTransaction.PROPERTY_DATEACCT,
            FIN_Utility.getDate(strPaymentDateTo)));
      }

      // Amount
      if (!strAmountFrom.equals("")) {
        obCriteriaTrans.add(Restrictions.or(Restrictions.ge(
            FIN_FinaccTransaction.PROPERTY_DEPOSITAMOUNT, new BigDecimal(strAmountFrom)),
            Restrictions.ge(FIN_FinaccTransaction.PROPERTY_PAYMENTAMOUNT, new BigDecimal(
                strAmountFrom))));
      }
      if (!strAmountTo.equals("")) {
        obCriteriaTrans.add(Restrictions.or(Restrictions.le(
            FIN_FinaccTransaction.PROPERTY_DEPOSITAMOUNT, new BigDecimal(strAmountTo)),
            Restrictions.le(FIN_FinaccTransaction.PROPERTY_PAYMENTAMOUNT, new BigDecimal(
                strAmountTo))));
      }

      // Projects
      if (!projects.isEmpty()) {
        obCriteriaTrans.add(Restrictions.in(FIN_FinaccTransaction.PROPERTY_PROJECT, projects));
      }

      // Status
      if (!strfinPaymSt.isEmpty() && !strfinPaymSt.equalsIgnoreCase("('')")) {
        strfinPaymSt = strfinPaymSt.replace("(", "");
        strfinPaymSt = strfinPaymSt.replace(")", "");
        strfinPaymSt = strfinPaymSt.replace("'", "");
        strfinPaymSt = strfinPaymSt.replace(" ", "");
        String[] status = strfinPaymSt.split(",");
        obCriteriaTrans.add(Restrictions.in(FIN_FinaccTransaction.PROPERTY_STATUS, status));
      }

      // Currency
      if (!strcCurrency.equals("")) {
        obCriteriaTrans.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_CURRENCY, OBDal
            .getInstance().get(Currency.class, strcCurrency)));
      }

      // payment type
      if (strPaymType.equalsIgnoreCase("FINPR_Receivables")) {
        String[] status = { "PWNC", "RPPC" };
        obCriteriaTrans.add(Restrictions.in(FIN_FinaccTransaction.PROPERTY_STATUS, status));
      } else if (strPaymType.equalsIgnoreCase("FINPR_Payables")) {
        String[] status = { "RDNC", "RPPC" };
        obCriteriaTrans.add(Restrictions.in(FIN_FinaccTransaction.PROPERTY_STATUS, status));
      }

      // order

      if (strGroupCrit.equalsIgnoreCase("APRM_FATS_BPARTNER")) {
        obCriteriaTrans.addOrder(Order.asc("bp." + BusinessPartner.PROPERTY_NAME));
      } else if (strGroupCrit.equalsIgnoreCase("Project")) {
        obCriteriaTrans.addOrder(Order.asc("p." + Project.PROPERTY_NAME));
      } else if (strGroupCrit.equalsIgnoreCase("FINPR_BPartner_Category")) {
        obCriteriaTrans.addOrder(Order.asc("bpc." + Category.PROPERTY_NAME));
      } else if (strGroupCrit.equalsIgnoreCase("INS_CURRENCY")) {
        obCriteriaTrans.addOrder(Order.asc("c." + Currency.PROPERTY_ISOCODE));
      }

      obCriteriaTrans.addOrder(Order.asc(FIN_FinaccTransaction.PROPERTY_STATUS));

      if (!strOrdCrit.isEmpty()) {
        String[] strOrdCritList = strOrdCrit.substring(2, strOrdCrit.length() - 2).split("', '");
        for (int i = 0; i < strOrdCritList.length; i++) {
          if (strOrdCritList[i].contains("Date")) {
            obCriteriaTrans.addOrder(Order.asc(FIN_FinaccTransaction.PROPERTY_DATEACCT));
          }
          if (strOrdCritList[i].contains("Project")) {
            obCriteriaTrans.addOrder(Order.asc("p." + Project.PROPERTY_NAME));
          }
          if (strOrdCritList[i].contains("FINPR_BPartner_Category")) {
            obCriteriaTrans.addOrder(Order.asc("bpc." + Category.PROPERTY_NAME));
          }
          if (strOrdCritList[i].contains("APRM_FATS_BPARTNER")) {
            obCriteriaTrans.addOrder(Order.asc("bp." + BusinessPartner.PROPERTY_NAME));
          }
          if (strOrdCritList[i].contains("INS_CURRENCY")) {
            obCriteriaTrans.addOrder(Order.asc("c." + Currency.PROPERTY_ISOCODE));
          }
        }
      }
      obCriteriaTrans.addOrderBy(FIN_FinaccTransaction.PROPERTY_ID, true);

      final java.util.List<FIN_FinaccTransaction> transList = obCriteriaTrans.list();
      return transList;

    } catch (Exception e) {
      log4j.error(e.getMessage(), e);
      return new ArrayList<FIN_FinaccTransaction>();
    } finally {
      OBContext.restorePreviousMode();
    }
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

    OBContext.setAdminMode(true);
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
    OBContext.setAdminMode(true);
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
    OBContext.setAdminMode(true);
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

    OBContext.setAdminMode(true);
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

  public java.util.List<FIN_PaymentSchedule> getInvoicePaymentSchedules(FIN_Payment credit_payment) {
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
      @SuppressWarnings("unchecked")
      java.util.List<FIN_PaymentSchedule> psList = query.list();
      return psList;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Deprecated
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

  private String concatOrganizations(String[] orgs) {
    String concatOrgs = "";
    for (int i = 0; i < orgs.length; i++) {
      concatOrgs = concatOrgs.concat("', '" + orgs[i]);
    }
    if (!concatOrgs.equalsIgnoreCase("")) {
      concatOrgs = concatOrgs.substring(3);
      concatOrgs = "(" + concatOrgs + "')";
    }

    return concatOrgs;
  }

  /**
   * Given a String of organizations this method returns an array of organizations
   * 
   * @param strOrgFamily
   * @return
   */
  private Organization[] getOrganizations(Set<String> strOrgFamily) {
    Iterator<String> orgChildTreeIter = strOrgFamily.iterator();
    Organization[] organizations = new Organization[strOrgFamily.size()];
    int i = 0;
    while (orgChildTreeIter.hasNext()) {
      organizations[i] = OBDal.getInstance().get(Organization.class, orgChildTreeIter.next());
      i++;
    }
    return organizations;
  }

  private void createBPList() {
    bpList = new ArrayList<String>();
    OBCriteria<BusinessPartner> critBPartner = OBDal.getInstance().createCriteria(
        BusinessPartner.class);
    critBPartner.addOrderBy(BusinessPartner.PROPERTY_NAME, true);
    for (BusinessPartner bp : critBPartner.list()) {
      bpList.add(bp.getName());
    }
  }

  private void createBPCategoryList() {
    bpCategoryList = new ArrayList<String>();
    OBCriteria<Category> critBPCategory = OBDal.getInstance().createCriteria(Category.class);
    critBPCategory.addOrderBy(Category.PROPERTY_NAME, true);
    for (Category bpc : critBPCategory.list()) {
      bpCategoryList.add(bpc.getName());
    }
  }

  private void createProjectList() {
    projectList = new ArrayList<String>();
    OBCriteria<Project> critProject = OBDal.getInstance().createCriteria(Project.class);
    critProject.addOrderBy(Project.PROPERTY_NAME, true);
    for (Project project : critProject.list()) {
      projectList.add(project.getName());
    }
  }

}
