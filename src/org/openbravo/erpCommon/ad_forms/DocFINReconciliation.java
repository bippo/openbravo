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
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.AcctSchemaTableDocType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.calendar.Calendar;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.calendar.PeriodControl;
import org.openbravo.model.financialmgmt.calendar.Year;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Payment_Credit;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLine_v;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.service.db.DalConnectionProvider;

public class DocFINReconciliation extends AcctServer {
  /** Transaction type - Financial Account */
  public static final String TRXTYPE_BPDeposit = "BPD";
  public static final String TRXTYPE_BPWithdrawal = "BPW";
  public static final String TRXTYPE_BankFee = "BF";

  private static final long serialVersionUID = 1L;
  private static final Logger log4j = Logger.getLogger(DocFINReconciliation.class);

  String SeqNo = "0";

  public DocFINReconciliation() {
  }

  public DocFINReconciliation(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  @Override
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_Reconciliation;
    DateDoc = data[0].getField("statementDate");
    C_DocType_ID = data[0].getField("C_Doctype_ID");
    DocumentNo = data[0].getField("DocumentNo");
    OBContext.setAdminMode();
    try {
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class,
          Record_ID);
      Amounts[0] = reconciliation.getEndingBalance().subtract(reconciliation.getStartingbalance())
          .toString();
    } finally {
      OBContext.restorePreviousMode();
    }
    loadDocumentType();
    p_lines = loadLines();
    return true;
  }

  public FieldProviderFactory[] loadLinesFieldProvider(String Id) {
    FieldProviderFactory[] linesInfo = null;
    OBContext.setAdminMode();
    try {
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class, Id);
      List<FIN_FinaccTransaction> transactions = getTransactionList(reconciliation);
      for (FIN_FinaccTransaction transaction : transactions) {
        FIN_Payment payment = transaction.getFinPayment();
        // If payment exists the payment details are loaded, if not the GLItem info is loaded,
        // finally fee is loaded
        if (payment != null)
          linesInfo = add(linesInfo, loadLinesPaymentFieldProvider(transaction));
        else if (transaction.getGLItem() != null)
          linesInfo = add(linesInfo, loadLinesGLItemFieldProvider(transaction));
        else
          linesInfo = add(linesInfo, loadLinesFeeFieldProvider(transaction));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return linesInfo;
  }

  FieldProviderFactory[] add(FieldProviderFactory[] one, FieldProviderFactory[] two) {
    if (one == null)
      return two;
    if (two == null)
      return one;
    FieldProviderFactory[] result = new FieldProviderFactory[one.length + two.length];
    for (int i = 0; i < one.length; i++) {
      if (one[i] != null)
        result[i] = one[i];
    }
    for (int i = 0; i < two.length; i++) {
      if (two[i] != null)
        result[i + one.length] = two[i];
    }
    return result;
  }

  public List<FIN_FinaccTransaction> getTransactionList(FIN_Reconciliation reconciliation) {
    OBContext.setAdminMode();
    List<FIN_FinaccTransaction> transactions = null;
    try {
      OBCriteria<FIN_FinaccTransaction> trans = OBDal.getInstance().createCriteria(
          FIN_FinaccTransaction.class);
      trans.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_RECONCILIATION, reconciliation));
      trans.setFilterOnReadableClients(false);
      trans.setFilterOnReadableOrganization(false);
      transactions = trans.list();
    } finally {
      OBContext.restorePreviousMode();
    }
    return transactions;
  }

  public FieldProviderFactory[] loadLinesPaymentDetailsFieldProvider(
      FIN_FinaccTransaction transaction) {
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class,
        transaction.getFinPayment().getId());
    List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
    FieldProviderFactory[] data = new FieldProviderFactory[paymentDetails.size()];
    OBContext.setAdminMode();
    try {
      for (int i = 0; i < data.length; i++) {
        // Details refunded used credit are excluded as the entry will be created using the credit
        // used
        if (paymentDetails.get(i).isRefund() && paymentDetails.get(i).isPrepayment())
          continue;
        data[i] = new FieldProviderFactory(null);
        FieldProviderFactory.setField(data[i], "FIN_Reconciliation_ID", transaction
            .getReconciliation().getId());
        FieldProviderFactory.setField(data[i], "FIN_Finacc_Transaction_ID", transaction.getId());
        FieldProviderFactory.setField(data[i], "AD_Client_ID", paymentDetails.get(i).getClient()
            .getId());
        FieldProviderFactory.setField(data[i], "AD_Org_ID", paymentDetails.get(i).getOrganization()
            .getId());
        FieldProviderFactory.setField(data[i], "FIN_Payment_Detail_ID", paymentDetails.get(i)
            .getId());
        FieldProviderFactory.setField(data[i], "FIN_Payment_ID", payment.getId());
        String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
            .getProperty("dateFormat.java");
        SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
        FieldProviderFactory.setField(data[i], "dateacct",
            outputFormat.format(transaction.getDateAcct()));
        FieldProviderFactory.setField(data[i], "DepositAmount", transaction.getDepositAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "PaymentAmount", transaction.getPaymentAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "Amount", paymentDetails.get(i).getAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "isprepayment",
            paymentDetails.get(i).isPrepayment() ? "Y" : "N");
        FieldProviderFactory.setField(data[i], "WriteOffAmt", paymentDetails.get(i)
            .getWriteoffAmount().toString());
        FieldProviderFactory.setField(data[i], "cGlItemId",
            paymentDetails.get(i).getGLItem() != null ? paymentDetails.get(i).getGLItem().getId()
                : "");
        // Calculate Business Partner from payment header or from details if header is null
        BusinessPartner bPartner = payment.getBusinessPartner() != null ? payment
            .getBusinessPartner() : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
            .getInvoicePaymentSchedule() != null ? paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule().getInvoice()
            .getBusinessPartner() : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
            .getOrderPaymentSchedule() != null ? paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule().getOrder()
            .getBusinessPartner() : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
            .getBusinessPartner())));
        FieldProviderFactory.setField(data[i], "cBpartnerId", bPartner != null ? bPartner.getId()
            : "");
        FieldProviderFactory.setField(data[i], "Refund", paymentDetails.get(i).isRefund() ? "Y"
            : "N");
        FieldProviderFactory.setField(data[i], "adOrgId", transaction.getOrganization().getId());
        FieldProviderFactory.setField(
            data[i],
            "cGlItemId",
            transaction.getGLItem() != null ? transaction.getGLItem().getId() : data[i]
                .getField("cGlItemId"));
        FieldProviderFactory.setField(data[i], "description", transaction.getDescription());
        FieldProviderFactory.setField(data[i], "cCurrencyId", transaction.getCurrency().getId());
        FieldProviderFactory.setField(data[i], "cProjectId", paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
            && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                .getInvoicePaymentSchedule().getInvoice().getProject() != null ? paymentDetails
            .get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule()
            .getInvoice().getProject().getId() : (paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule() != null
            && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                .getOrderPaymentSchedule().getOrder().getProject() != null ? paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule().getOrder()
            .getProject().getId() : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
            .getProject() != null ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
            .getProject().getId() : "")));
        FieldProviderFactory
            .setField(
                data[i],
                "cCampaignId",
                paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                    .getInvoicePaymentSchedule() != null
                    && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                        .getInvoicePaymentSchedule().getInvoice().getSalesCampaign() != null ? paymentDetails
                    .get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule()
                    .getInvoice().getSalesCampaign().getId()
                    : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                        .getOrderPaymentSchedule() != null
                        && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                            .getOrderPaymentSchedule().getOrder().getSalesCampaign() != null ? paymentDetails
                        .get(i).getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule()
                        .getOrder().getSalesCampaign().getId()
                        : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                            .getSalesCampaign() != null ? paymentDetails.get(i)
                            .getFINPaymentScheduleDetailList().get(0).getSalesCampaign().getId()
                            : "")));
        FieldProviderFactory.setField(data[i], "cActivityId", paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
            && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                .getInvoicePaymentSchedule().getInvoice().getActivity() != null ? paymentDetails
            .get(i).getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule()
            .getInvoice().getActivity().getId() : (paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule() != null
            && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                .getOrderPaymentSchedule().getOrder().getActivity() != null ? paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule().getOrder()
            .getActivity().getId() : (paymentDetails.get(i).getFINPaymentScheduleDetailList()
            .get(0).getActivity() != null ? paymentDetails.get(i).getFINPaymentScheduleDetailList()
            .get(0).getActivity().getId() : "")));
        FieldProviderFactory.setField(data[i], "mProductId", paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getProduct() != null ? paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getProduct().getId() : "");
        FieldProviderFactory.setField(data[i], "cSalesregionId", paymentDetails.get(i)
            .getFINPaymentScheduleDetailList().get(0).getSalesRegion() != null ? paymentDetails
            .get(i).getFINPaymentScheduleDetailList().get(0).getSalesRegion().getId() : "");
        FieldProviderFactory.setField(data[i], "lineno", transaction.getLineNo().toString());
        try { // Get User1_ID and User2_ID using xsql
          ConnectionProvider conn = new DalConnectionProvider(false);
          DocFINPaymentData[] paymentInfo = DocFINPaymentData.select(conn, payment.getId());
          if (paymentInfo.length > 0) {
            FieldProviderFactory.setField(data[i], "user1Id", paymentInfo[0].user1Id);
            FieldProviderFactory.setField(data[i], "user2Id", paymentInfo[0].user2Id);
          }
        } catch (Exception e) {
          log4j.error("Error while retreiving user1 and user2 - ", e);
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  public FieldProviderFactory[] loadLinesPaymentFieldProvider(FIN_FinaccTransaction transaction) {
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class,
        transaction.getFinPayment().getId());
    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      for (int i = 0; i < data.length; i++) {
        data[i] = new FieldProviderFactory(null);
        FieldProviderFactory.setField(data[i], "FIN_Reconciliation_ID", transaction
            .getReconciliation().getId());
        FieldProviderFactory.setField(data[i], "FIN_Finacc_Transaction_ID", transaction.getId());
        FieldProviderFactory.setField(data[i], "AD_Client_ID", payment.getClient().getId());
        FieldProviderFactory.setField(data[i], "AD_Org_ID", payment.getOrganization().getId());
        FieldProviderFactory.setField(data[i], "FIN_Payment_ID", payment.getId());
        FieldProviderFactory.setField(data[i], "DepositAmount", transaction.getDepositAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "PaymentAmount", transaction.getPaymentAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "Amount", payment.getFinancialTransactionAmount()
            .toString());
        String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
            .getProperty("dateFormat.java");
        SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
        FieldProviderFactory.setField(data[i], "dateacct",
            outputFormat.format(transaction.getDateAcct()));
        FieldProviderFactory.setField(data[i], "WriteOffAmt", payment.getWriteoffAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "cBpartnerId",
            payment.getBusinessPartner() != null ? payment.getBusinessPartner().getId() : "");
        FieldProviderFactory.setField(data[i], "adOrgId", transaction.getOrganization().getId());
        FieldProviderFactory.setField(
            data[i],
            "cGlItemId",
            transaction.getGLItem() != null ? transaction.getGLItem().getId() : data[i]
                .getField("cGlItemId"));
        FieldProviderFactory.setField(data[i], "description", transaction.getDescription());
        FieldProviderFactory.setField(data[i], "cCurrencyId", transaction.getCurrency().getId());
        if (transaction.getActivity() != null)
          FieldProviderFactory.setField(data[i], "cActivityId", transaction.getActivity().getId());
        if (transaction.getProject() != null)
          FieldProviderFactory.setField(data[i], "cProjectId", transaction.getProject().getId());
        if (transaction.getSalesCampaign() != null)
          FieldProviderFactory.setField(data[i], "cCampaignId", transaction.getSalesCampaign()
              .getId());
        if (transaction.getProduct() != null)
          FieldProviderFactory.setField(data[0], "mProductId", transaction.getProduct().getId());
        if (transaction.getSalesRegion() != null)
          FieldProviderFactory.setField(data[0], "cSalesregionId", transaction.getSalesRegion()
              .getId());
        FieldProviderFactory.setField(data[i], "lineno", transaction.getLineNo().toString());
        try { // Get User1_ID and User2_ID using xsql
          ConnectionProvider conn = new DalConnectionProvider(false);
          DocFINFinAccTransactionData[] trxInfo = DocFINFinAccTransactionData.select(conn,
              transaction.getId());
          if (trxInfo.length > 0) {
            FieldProviderFactory.setField(data[i], "user1Id", trxInfo[0].user1Id);
            FieldProviderFactory.setField(data[i], "user2Id", trxInfo[0].user2Id);
          }
        } catch (Exception e) {
          log4j.error("Error while retreiving user1 and user2 - ", e);
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  public FieldProviderFactory[] loadLinesGLItemFieldProvider(FIN_FinaccTransaction transaction) {
    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      data[0] = new FieldProviderFactory(null);
      FieldProviderFactory.setField(data[0], "FIN_Reconciliation_ID", transaction
          .getReconciliation().getId());
      FieldProviderFactory.setField(data[0], "FIN_Finacc_Transaction_ID", transaction.getId());
      FieldProviderFactory.setField(data[0], "AD_Client_ID", transaction.getClient().getId());
      FieldProviderFactory.setField(data[0], "adOrgId", transaction.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "cGlItemId", transaction.getGLItem().getId());
      FieldProviderFactory.setField(data[0], "DepositAmount", transaction.getDepositAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "PaymentAmount", transaction.getPaymentAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "description", transaction.getDescription());
      FieldProviderFactory.setField(data[0], "cCurrencyId", transaction.getCurrency().getId());
      FieldProviderFactory.setField(data[0], "cBpartnerId",
          transaction.getBusinessPartner() != null ? transaction.getBusinessPartner().getId() : "");
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      FieldProviderFactory.setField(data[0], "dateacct",
          outputFormat.format(transaction.getDateAcct()));
      if (transaction.getActivity() != null)
        FieldProviderFactory.setField(data[0], "cActivityId", transaction.getActivity().getId());
      if (transaction.getProject() != null)
        FieldProviderFactory.setField(data[0], "cProjectId", transaction.getProject().getId());
      if (transaction.getSalesCampaign() != null)
        FieldProviderFactory.setField(data[0], "cCampaignId", transaction.getSalesCampaign()
            .getId());
      if (transaction.getProduct() != null)
        FieldProviderFactory.setField(data[0], "mProductId", transaction.getProduct().getId());
      if (transaction.getSalesRegion() != null)
        FieldProviderFactory.setField(data[0], "cSalesregionId", transaction.getSalesRegion()
            .getId());
      FieldProviderFactory.setField(data[0], "lineno", transaction.getLineNo().toString());
      try { // Get User1_ID and User2_ID using xsql
        ConnectionProvider conn = new DalConnectionProvider(false);
        DocFINFinAccTransactionData[] trxInfo = DocFINFinAccTransactionData.select(conn,
            transaction.getId());
        if (trxInfo.length > 0) {
          FieldProviderFactory.setField(data[0], "user1Id", trxInfo[0].user1Id);
          FieldProviderFactory.setField(data[0], "user2Id", trxInfo[0].user2Id);
        }
      } catch (Exception e) {
        log4j.error("Error while retreiving user1 and user2 - ", e);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  public FieldProviderFactory[] loadLinesFeeFieldProvider(FIN_FinaccTransaction transaction) {
    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      data[0] = new FieldProviderFactory(null);
      FieldProviderFactory.setField(data[0], "FIN_Reconciliation_ID", transaction
          .getReconciliation().getId());
      FieldProviderFactory.setField(data[0], "FIN_Finacc_Transaction_ID", transaction.getId());
      FieldProviderFactory.setField(data[0], "AD_Client_ID", transaction.getClient().getId());
      FieldProviderFactory.setField(data[0], "adOrgId", transaction.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "DepositAmount", transaction.getDepositAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "PaymentAmount", transaction.getPaymentAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "description", transaction.getDescription());
      FieldProviderFactory.setField(data[0], "cCurrencyId", transaction.getCurrency().getId());
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      FieldProviderFactory.setField(data[0], "dateacct",
          outputFormat.format(transaction.getDateAcct()));
      if (transaction.getActivity() != null)
        FieldProviderFactory.setField(data[0], "cActivityId", transaction.getActivity().getId());
      if (transaction.getProject() != null)
        FieldProviderFactory.setField(data[0], "cProjectId", transaction.getProject().getId());
      if (transaction.getSalesCampaign() != null)
        FieldProviderFactory.setField(data[0], "cCampaignId", transaction.getSalesCampaign()
            .getId());
      FieldProviderFactory.setField(data[0], "lineno", transaction.getLineNo().toString());
      try { // Get User1_ID and User2_ID using xsql
        ConnectionProvider conn = new DalConnectionProvider(false);
        DocFINFinAccTransactionData[] trxInfo = DocFINFinAccTransactionData.select(conn,
            transaction.getId());
        if (trxInfo.length > 0) {
          FieldProviderFactory.setField(data[0], "user1Id", trxInfo[0].user1Id);
          FieldProviderFactory.setField(data[0], "user2Id", trxInfo[0].user2Id);
        }
      } catch (Exception e) {
        log4j.error("Error while retreiving user1 and user2 - ", e);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  private DocLine[] loadLines() {
    ArrayList<Object> list = new ArrayList<Object>();
    FieldProviderFactory[] data = loadLinesFieldProvider(Record_ID);
    if (data == null || data.length == 0)
      return null;
    OBContext.setAdminMode();
    try {
      for (int i = 0; i < data.length; i++) {
        String Line_ID = data[i].getField("FIN_Finacc_Transaction_ID");
        DocLine_FINReconciliation docLine = new DocLine_FINReconciliation(DocumentType, Record_ID,
            Line_ID);
        String strPaymentId = data[i].getField("FIN_Payment_ID");
        if (strPaymentId != null && !strPaymentId.equals(""))
          docLine.setFinPaymentId(strPaymentId);
        docLine.m_Record_Id2 = strPaymentId;
        docLine.setIsPrepayment(data[i].getField("isprepayment"));
        docLine.setCGlItemId(data[i].getField("cGlItemId"));
        docLine.setPaymentAmount(data[i].getField("PaymentAmount"));
        docLine.setDepositAmount(data[i].getField("DepositAmount"));
        docLine.setWriteOffAmt(data[i].getField("WriteOffAmt"));
        docLine.setAmount(data[i].getField("Amount"));
        docLine.setFinFinAccTransactionId(data[i].getField("FIN_Finacc_Transaction_ID"));
        docLine.loadAttributes(data[i], this);
        list.add(docLine);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    // Return Array
    DocLine_FINReconciliation[] dl = new DocLine_FINReconciliation[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  @Override
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Select specific definition
    String strClassname = "";
    final StringBuilder whereClause = new StringBuilder();
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    OBContext.setAdminMode();
    try {
      whereClause.append(" as astdt ");
      whereClause.append(" where astdt.acctschemaTable.accountingSchema.id = '"
          + as.m_C_AcctSchema_ID + "'");
      whereClause.append(" and astdt.acctschemaTable.table.id = '" + AD_Table_ID + "'");
      whereClause.append(" and astdt.documentCategory = '" + DocumentType + "'");

      final OBQuery<AcctSchemaTableDocType> obqParameters = OBDal.getInstance().createQuery(
          AcctSchemaTableDocType.class, whereClause.toString());
      final List<AcctSchemaTableDocType> acctSchemaTableDocTypes = obqParameters.list();

      if (acctSchemaTableDocTypes != null && acctSchemaTableDocTypes.size() > 0)
        strClassname = acctSchemaTableDocTypes.get(0).getCreatefactTemplate().getClassname();

      if (strClassname.equals("")) {
        final StringBuilder whereClause2 = new StringBuilder();

        whereClause2.append(" as ast ");
        whereClause2.append(" where ast.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
        whereClause2.append(" and ast.table.id = '" + AD_Table_ID + "'");

        final OBQuery<AcctSchemaTable> obqParameters2 = OBDal.getInstance().createQuery(
            AcctSchemaTable.class, whereClause2.toString());
        final List<AcctSchemaTable> acctSchemaTables = obqParameters2.list();
        if (acctSchemaTables != null && acctSchemaTables.size() > 0
            && acctSchemaTables.get(0).getCreatefactTemplate() != null)
          strClassname = acctSchemaTables.get(0).getCreatefactTemplate().getClassname();
      }
      if (!strClassname.equals("")) {
        try {
          DocFINReconciliationTemplate newTemplate = (DocFINReconciliationTemplate) Class.forName(
              strClassname).newInstance();
          return newTemplate.createFact(this, as, conn, con, vars);
        } catch (Exception e) {
          log4j.error("Error while creating new instance for DocFINReconciliationTemplate - " + e);
        }
      }
      String Fact_Acct_Group_ID = SequenceIdData.getUUID();
      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        DocLine_FINReconciliation line = (DocLine_FINReconciliation) p_lines[i];
        FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
            line.getFinFinAccTransactionId());
        // 3 Scenarios: 1st Bank fee 2nd payment related transaction 3rd GL item transaction
        if (TRXTYPE_BankFee.equals(transaction.getTransactionType()))
          fact = createFactFee(line, as, conn, fact, Fact_Acct_Group_ID);
        else if (!"".equals(line.getFinPaymentId()))
          fact = createFactPayment(line, as, conn, fact, Fact_Acct_Group_ID);
        else
          fact = createFactGLItem(line, as, conn, fact, Fact_Acct_Group_ID);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return fact;
  }

  /*
   * Creates the accounting for a bank Fee
   */
  public Fact createFactFee(DocLine_FINReconciliation line, AcctSchema as, ConnectionProvider conn,
      Fact fact, String Fact_Acct_Group_ID) throws ServletException {
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        line.getFinFinAccTransactionId());
    if (getDocumentTransactionConfirmation(transaction)) {
      BigDecimal transactionPaymentAmount = new BigDecimal(line.getPaymentAmount());
      BigDecimal transactionDepositedAmount = new BigDecimal(line.getDepositAmount());
      String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
      // Should map with the amount booked in the transaction and adjust with currency gain/loss
      transactionPaymentAmount = convertAmount(transactionPaymentAmount, true,
          dateFormat.format(transaction.getDateAcct()), TABLEID_Transaction, transaction.getId(),
          C_Currency_ID, as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          conn);
      transactionDepositedAmount = convertAmount(transactionDepositedAmount, false,
          dateFormat.format(transaction.getDateAcct()), TABLEID_Transaction, transaction.getId(),
          C_Currency_ID, as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          conn);
      fact.createLine(line, getWithdrawalAccount(as, transaction.getAccount(), conn),
          C_Currency_ID, transactionPaymentAmount.toString(),
          transactionDepositedAmount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    } else {
      fact.createLine(line, getAccountFee(as, transaction.getAccount(), conn), C_Currency_ID,
          line.getPaymentAmount(), line.getDepositAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    }
    fact.createLine(line, getClearOutAccount(as, transaction.getAccount(), conn), C_Currency_ID,
        line.getDepositAmount(), line.getPaymentAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
        DocumentType, conn);
    SeqNo = "0";
    return fact;
  }

  public Fact createFactPayment(DocLine_FINReconciliation line, AcctSchema as,
      ConnectionProvider conn, Fact fact, String Fact_Acct_Group_ID) throws ServletException {
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, line.getFinPaymentId());
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        line.getFinFinAccTransactionId());
    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
    if (getDocumentTransactionConfirmation(transaction)) {
      BigDecimal transactionAmount = new BigDecimal(line.getAmount());
      // Should map with the amount booked in the transaction and adjust with currency gain/loss
      transactionAmount = convertAmount(new BigDecimal(line.getAmount()), !payment.isReceipt(),
          dateFormat.format(transaction.getDateAcct()), TABLEID_Transaction, transaction.getId(),
          C_Currency_ID, as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          conn);
      fact.createLine(line, getAccountTransactionPayment(conn, payment, as), C_Currency_ID,
          !payment.isReceipt() ? transactionAmount.toString() : "",
          payment.isReceipt() ? transactionAmount.toString() : "", Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), DocumentType, conn);
    } else if (!getDocumentPaymentConfirmation(payment)) {
      FieldProviderFactory[] data = loadLinesPaymentDetailsFieldProvider(transaction);
      for (int i = 0; i < data.length; i++) {
        if (data[i] == null)
          continue;
        DocLine_FINReconciliation detail = new DocLine_FINReconciliation(DocumentType, Record_ID,
            line.Line_ID);
        detail.setCGlItemId(data[i].getField("cGlItemId"));
        detail.m_C_BPartner_ID = data[i].getField("cBpartnerId");
        detail.m_C_Project_ID = data[i].getField("cProjectId");
        detail.m_C_Campaign_ID = data[i].getField("cCampaignId");
        detail.m_C_Activity_ID = data[i].getField("cActivityId");
        detail.m_M_Product_ID = data[i].getField("mProductId");
        detail.m_C_SalesRegion_ID = data[i].getField("cSalesregionId");
        detail.m_AD_Org_ID = line.m_AD_Org_ID;
        detail.m_description = line.m_description;
        detail.m_C_Currency_ID = line.m_C_Currency_ID;
        detail.m_DateAcct = line.m_DateAcct;
        detail.m_DateDoc = line.m_DateDoc;
        detail.finFinAccTransactionId = transaction.getId();
        detail.m_User1_ID = data[i].getField("user1Id");
        detail.m_User2_ID = data[i].getField("user2Id");
        // Cambiar line to reflect BPs
        FIN_PaymentDetail paymentDetail = OBDal.getInstance().get(FIN_PaymentDetail.class,
            data[i].getField("FIN_Payment_Detail_ID"));
        fact = createFactPaymentDetails(detail, paymentDetail, as, conn, fact, Fact_Acct_Group_ID);
      }
    } else {
      BigDecimal paymentAmount = new BigDecimal(line.getAmount());
      // Should map with the amount booked in the payment and adjust with currency gain/loss
      paymentAmount = convertAmount(new BigDecimal(line.getAmount()), !payment.isReceipt(),
          dateFormat.format(transaction.getDateAcct()), TABLEID_Payment, line.getFinPaymentId(),
          C_Currency_ID, as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          conn);
      fact.createLine(line, getAccountPayment(conn, payment, as), C_Currency_ID,
          !payment.isReceipt() ? paymentAmount.toString() : "",
          payment.isReceipt() ? paymentAmount.toString() : "", Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), DocumentType, conn);
    }
    fact.createLine(line, getAccountReconciliation(conn, payment, as), C_Currency_ID,
        payment.isReceipt() ? line.getAmount() : "", !payment.isReceipt() ? line.getAmount() : "",
        Fact_Acct_Group_ID, "999999", DocumentType, conn);
    if (!getDocumentPaymentConfirmation(payment)
        && !getDocumentTransactionConfirmation(transaction)) {
      // Pre-payment is consumed when Used Credit Amount not equals Zero. When consuming Credit no
      // credit is generated
      if (payment.getUsedCredit().compareTo(ZERO) != 0
          && payment.getGeneratedCredit().compareTo(ZERO) == 0) {
        List<FIN_Payment_Credit> creditPayments = transaction.getFinPayment()
            .getFINPaymentCreditList();
        for (FIN_Payment_Credit creditPayment : creditPayments) {
          boolean isReceiptPayment = creditPayment.getCreditPaymentUsed().isReceipt();
          String creditAmountConverted = convertAmount(creditPayment.getAmount(), isReceiptPayment,
              DateAcct, TABLEID_Payment, creditPayment.getCreditPaymentUsed().getId(),
              creditPayment.getCreditPaymentUsed().getCurrency().getId(), as.m_C_Currency_ID, line,
              as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn).toString();
          fact.createLine(
              line,
              getAccountBPartner(creditPayment.getCreditPaymentUsed().getBusinessPartner().getId(),
                  as, isReceiptPayment, true, conn), creditPayment.getCreditPaymentUsed()
                  .getCurrency().getId(), (isReceiptPayment ? creditAmountConverted : ""),
              (isReceiptPayment ? "" : creditAmountConverted), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
        }
        if (creditPayments.isEmpty()) {
          fact.createLine(
              line,
              getAccountBPartner(payment.getBusinessPartner().getId(), as, payment.isReceipt(),
                  true, conn), payment.getCurrency().getId(), (payment.isReceipt() ? payment
                  .getUsedCredit().toString() : ""), (payment.isReceipt() ? "" : payment
                  .getUsedCredit().toString()), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              conn);
        }
      }
    }

    SeqNo = "0";
    return fact;
  }

  @Deprecated
  public Fact createFactPaymentDetails(DocLine_FINReconciliation line, AcctSchema as,
      ConnectionProvider conn, Fact fact, String Fact_Acct_Group_ID) throws ServletException {
    boolean isPrepayment = "Y".equals(line.getIsPrepayment());
    BigDecimal paymentAmount = new BigDecimal(line.getPaymentAmount());
    BigDecimal depositAmount = new BigDecimal(line.getDepositAmount());
    boolean isReceipt = paymentAmount.compareTo(depositAmount) < 0;
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, line.getFinPaymentId());
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        line.getFinFinAccTransactionId());
    if (getDocumentTransactionConfirmation(transaction))
      fact.createLine(line, getAccountTransactionPayment(conn, payment, as), C_Currency_ID,
          !isReceipt ? line.getAmount() : "", isReceipt ? line.getAmount() : "",
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    else if (!getDocumentPaymentConfirmation(payment)) {
      BigDecimal bpAmount = new BigDecimal(line.getAmount());
      if (line.getWriteOffAmt() != null
          && ZERO.compareTo(new BigDecimal(line.getWriteOffAmt())) != 0) {
        Account account = isReceipt ? getAccountWriteOffBPartner(AcctServer.ACCTTYPE_WriteOff,
            line.m_C_BPartner_ID, as, conn) : getAccountWriteOffBPartner(
            AcctServer.ACCTTYPE_WriteOff_Revenue, line.m_C_BPartner_ID, as, conn);
        if (account == null) {
          account = isReceipt ? getAccount(AcctServer.ACCTTYPE_WriteOffDefault, as, conn)
              : getAccount(AcctServer.ACCTTYPE_WriteOffDefault_Revenue, as, conn);
        }
        fact.createLine(line, account, C_Currency_ID, (isReceipt ? line.getWriteOffAmt() : ""),
            (isReceipt ? "" : line.getWriteOffAmt()), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
        bpAmount = bpAmount.add(new BigDecimal(line.getWriteOffAmt()));
      }
      fact.createLine(
          line,
          getAccountBPartner(
              (line.m_C_BPartner_ID == null || line.m_C_BPartner_ID.equals("")) ? this.C_BPartner_ID
                  : line.m_C_BPartner_ID, as, isReceipt, isPrepayment, conn), C_Currency_ID,
          !isReceipt ? line.getAmount() : "", isReceipt ? line.getAmount() : "",
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    } else {
      fact.createLine(line, getAccountPayment(conn, payment, as), C_Currency_ID,
          !isReceipt ? line.getAmount() : "", isReceipt ? line.getAmount() : "",
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    }

    SeqNo = "0";
    return fact;
  }

  public Fact createFactPaymentDetails(DocLine_FINReconciliation line,
      FIN_PaymentDetail paymentDetail, AcctSchema as, ConnectionProvider conn, Fact fact,
      String Fact_Acct_Group_ID) throws ServletException {
    boolean isPrepayment = paymentDetail.isPrepayment();
    boolean isReceipt = paymentDetail.getFinPayment().isReceipt();
    BigDecimal bpAmount = paymentDetail.getAmount();
    Currency paymentCurrency = paymentDetail.getFinPayment().getCurrency();
    if (paymentDetail.getWriteoffAmount() != null
        && paymentDetail.getWriteoffAmount().compareTo(BigDecimal.ZERO) != 0) {
      Account account = isReceipt ? getAccountWriteOffBPartner(AcctServer.ACCTTYPE_WriteOff,
          line.m_C_BPartner_ID, as, conn) : getAccountWriteOffBPartner(
          AcctServer.ACCTTYPE_WriteOff_Revenue, line.m_C_BPartner_ID, as, conn);
      if (account == null) {
        account = isReceipt ? getAccount(AcctServer.ACCTTYPE_WriteOffDefault, as, conn)
            : getAccount(AcctServer.ACCTTYPE_WriteOffDefault_Revenue, as, conn);
      }
      // Write off amount is generated at payment time so conversion is calculated taking into
      // account conversion at payment date to calculate gains or losses
      BigDecimal writeOffAmt = convertAmount(paymentDetail.getWriteoffAmount(), !isReceipt,
          line.m_DateAcct, TABLEID_Payment, paymentDetail.getFinPayment().getId(),
          paymentCurrency.getId(), as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), conn);
      fact.createLine(line, account, paymentCurrency.getId(), (isReceipt ? writeOffAmt.toString()
          : ""), (isReceipt ? "" : writeOffAmt.toString()), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
      bpAmount = bpAmount.add(paymentDetail.getWriteoffAmount());
    }
    String bpartnerId = (line.m_C_BPartner_ID == null || line.m_C_BPartner_ID.equals("")) ? this.C_BPartner_ID
        : line.m_C_BPartner_ID;
    if (bpartnerId == null || bpartnerId.equals("")) {
      bpartnerId = paymentDetail.getFINPaymentScheduleDetailList().get(0)
          .getInvoicePaymentSchedule() != null ? paymentDetail.getFINPaymentScheduleDetailList()
          .get(0).getInvoicePaymentSchedule().getInvoice().getBusinessPartner().getId()
          : paymentDetail.getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule() != null ? paymentDetail
              .getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule().getOrder()
              .getBusinessPartner().getId()
              : "";
    }
    if (line.cGlItemId != null && !"".equals(line.cGlItemId)) {
      fact.createLine(
          line,
          getAccountGLItem(OBDal.getInstance().get(GLItem.class, line.cGlItemId), as, isReceipt,
              conn), paymentCurrency.getId(), (isReceipt ? "" : bpAmount.toString()),
          (isReceipt ? bpAmount.toString() : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    } else {
      BigDecimal bpAmountConverted = bpAmount;
      Invoice invoice = paymentDetail.getFINPaymentScheduleDetailList().get(0)
          .getInvoicePaymentSchedule() != null ? paymentDetail.getFINPaymentScheduleDetailList()
          .get(0).getInvoicePaymentSchedule().getInvoice() : null;
      if (!isPrepayment && invoice != null) {
        // To force opposite posting isReceipt is opposite as well. this is required when
        // looking backwards
        bpAmountConverted = convertAmount(bpAmountConverted, !isReceipt, line.m_DateAcct,
            TABLEID_Invoice, invoice.getId(), paymentCurrency.getId(), as.m_C_Currency_ID, line,
            as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn);
      }
      if (isPrepayment) {
        // To force opposite posting isReceipt is opposite as well. this is required when
        // looking backwards. When prepayments date for event is always date for PAYMENT
        bpAmountConverted = convertAmount(bpAmountConverted, !isReceipt, line.m_DateAcct,
            TABLEID_Payment, paymentDetail.getFinPayment().getId(), paymentCurrency.getId(),
            as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn);
      }
      fact.createLine(line, getAccountBPartner(bpartnerId, as, isReceipt, isPrepayment, conn),
          paymentCurrency.getId(), !isReceipt ? bpAmountConverted.toString() : "",
          isReceipt ? bpAmountConverted.toString() : "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    }

    SeqNo = "0";
    return fact;
  }

  /*
   * Creates the accounting for a transaction related directly with a GLItem
   */
  public Fact createFactGLItem(DocLine_FINReconciliation line, AcctSchema as,
      ConnectionProvider conn, Fact fact, String Fact_Acct_Group_ID) throws ServletException {
    BigDecimal paymentAmount = new BigDecimal(line.getPaymentAmount());
    BigDecimal depositAmount = new BigDecimal(line.getDepositAmount());
    boolean isReceipt = paymentAmount.compareTo(depositAmount) < 0;
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        line.getFinFinAccTransactionId());
    if (getDocumentTransactionConfirmation(transaction)) {
      BigDecimal transactionPaymentAmount = new BigDecimal(line.getPaymentAmount());
      BigDecimal transactionDepositedAmount = new BigDecimal(line.getDepositAmount());
      // Should map with the amount booked in the transaction and adjust with currency gain/loss
      transactionPaymentAmount = convertAmount(transactionPaymentAmount, true, DateAcct,
          TABLEID_Transaction, transaction.getId(), C_Currency_ID, as.m_C_Currency_ID, line, as,
          fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn);
      transactionDepositedAmount = convertAmount(transactionDepositedAmount, false, DateAcct,
          TABLEID_Transaction, transaction.getId(), C_Currency_ID, as.m_C_Currency_ID, line, as,
          fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn);
      fact.createLine(line, getAccountTransaction(conn, transaction.getAccount(), as, isReceipt),
          C_Currency_ID, transactionPaymentAmount.toString(),
          transactionDepositedAmount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
      // Why empty string is being checked??
    } else if (!"".equals(line.getCGlItemId())) {
      fact.createLine(
          line,
          getAccountGLItem(OBDal.getInstance().get(GLItem.class, line.getCGlItemId()), as,
              isReceipt, conn), C_Currency_ID, line.getPaymentAmount(), line.getDepositAmount(),
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    }
    fact.createLine(line, getAccount(conn, transaction.getAccount(), as, isReceipt), C_Currency_ID,
        line.getDepositAmount(), line.getPaymentAmount(), Fact_Acct_Group_ID, "999999",
        DocumentType, conn);
    SeqNo = "0";
    return fact;
  }

  public String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }

  /**
   * Get Source Currency Balance - subtracts line amounts from total - no rounding
   * 
   * @return positive amount, if total is bigger than lines
   */
  @Override
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    StringBuffer sb = new StringBuffer(" [");
    // Total
    retValue = retValue.add(new BigDecimal(getAmount(AcctServer.AMTTYPE_Gross)));
    sb.append(getAmount(AcctServer.AMTTYPE_Gross));
    // - Lines
    for (int i = 0; i < p_lines.length; i++) {
      BigDecimal lineBalance = new BigDecimal(
          ((DocLine_FINReconciliation) p_lines[i]).DepositAmount);
      lineBalance = lineBalance.subtract(new BigDecimal(
          ((DocLine_FINReconciliation) p_lines[i]).PaymentAmount));
      retValue = retValue.subtract(lineBalance);
      sb.append("-").append(lineBalance);
    }
    sb.append("]");
    //
    log4j.debug(" Balance=" + retValue + sb.toString());
    return retValue;
  } // getBalance

  public boolean getDocumentPaymentConfirmation(FIN_Payment payment) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
          payment.getPaymentMethod()));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      List<FIN_FinancialAccountAccounting> accounts = payment.getAccount()
          .getFINFinancialAccountAcctList();
      for (FIN_FinancialAccountAccounting account : accounts) {
        if (payment.isReceipt()) {
          if (("INT").equals(lines.get(0).getUponReceiptUse())
              && account.getInTransitPaymentAccountIN() != null)
            confirmation = true;
          else if (("DEP").equals(lines.get(0).getUponReceiptUse())
              && account.getDepositAccount() != null)
            confirmation = true;
          else if (("CLE").equals(lines.get(0).getUponReceiptUse())
              && account.getClearedPaymentAccount() != null)
            confirmation = true;
        } else {
          if (("INT").equals(lines.get(0).getUponPaymentUse())
              && account.getFINOutIntransitAcct() != null)
            confirmation = true;
          else if (("WIT").equals(lines.get(0).getUponPaymentUse())
              && account.getWithdrawalAccount() != null)
            confirmation = true;
          else if (("CLE").equals(lines.get(0).getUponPaymentUse())
              && account.getClearedPaymentAccountOUT() != null)
            confirmation = true;
        }
      }
    } catch (Exception e) {
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    return confirmation;
  }

  public boolean getDocumentTransactionConfirmation(FIN_FinaccTransaction transaction) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      List<FIN_FinancialAccountAccounting> accounts = transaction.getAccount()
          .getFINFinancialAccountAcctList();
      FIN_Payment payment = transaction.getFinPayment();
      if (payment != null) {
        OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
            FinAccPaymentMethod.class);
        obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
        obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
            payment.getPaymentMethod()));
        obCriteria.setFilterOnReadableClients(false);
        obCriteria.setFilterOnReadableOrganization(false);
        List<FinAccPaymentMethod> lines = obCriteria.list();
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if (payment.isReceipt()) {
            if (("INT").equals(lines.get(0).getUponDepositUse())
                && account.getInTransitPaymentAccountIN() != null)
              confirmation = true;
            else if (("DEP").equals(lines.get(0).getUponDepositUse())
                && account.getDepositAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponDepositUse())
                && account.getClearedPaymentAccount() != null)
              confirmation = true;
          } else {
            if (("INT").equals(lines.get(0).getUponWithdrawalUse())
                && account.getFINOutIntransitAcct() != null)
              confirmation = true;
            else if (("WIT").equals(lines.get(0).getUponWithdrawalUse())
                && account.getWithdrawalAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponWithdrawalUse())
                && account.getClearedPaymentAccountOUT() != null)
              confirmation = true;
          }
        }
      } else {
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if ((TRXTYPE_BPDeposit.equals(transaction.getTransactionType()) && account
              .getDepositAccount() != null)
              || (TRXTYPE_BPWithdrawal.equals(transaction.getTransactionType()) && account
                  .getWithdrawalAccount() != null)
              || (TRXTYPE_BankFee.equals(transaction.getTransactionType()) && account
                  .getWithdrawalAccount() != null))
            confirmation = true;
        }
      }
    } catch (Exception e) {
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    return confirmation;
  }

  /*
   * Checks if this step (Reconciliation) is configured to generate accounting for the selected
   * financial account
   */
  @Override
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class,
          strRecordId);
      List<FIN_FinaccTransaction> transactions = getTransactionList(reconciliation);
      List<FIN_FinancialAccountAccounting> accounts = reconciliation.getAccount()
          .getFINFinancialAccountAcctList();
      for (FIN_FinaccTransaction transaction : transactions) {
        if (confirmation)
          break;
        FIN_Payment payment = transaction.getFinPayment();
        // If payment exists, check Payment Method + financial Account Configuration
        if (payment != null) {
          OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
              FinAccPaymentMethod.class);
          obCriteria
              .add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
          obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
              payment.getPaymentMethod()));
          obCriteria.setFilterOnReadableClients(false);
          obCriteria.setFilterOnReadableOrganization(false);
          List<FinAccPaymentMethod> lines = obCriteria.list();
          for (FIN_FinancialAccountAccounting account : accounts) {
            if (confirmation)
              return confirmation;
            if (payment.isReceipt()) {
              if (("INT").equals(lines.get(0).getINUponClearingUse())
                  && account.getInTransitPaymentAccountIN() != null)
                confirmation = true;
              else if (("DEP").equals(lines.get(0).getINUponClearingUse())
                  && account.getDepositAccount() != null)
                confirmation = true;
              else if (("CLE").equals(lines.get(0).getINUponClearingUse())
                  && account.getClearedPaymentAccount() != null)
                confirmation = true;
            } else {
              if (("INT").equals(lines.get(0).getOUTUponClearingUse())
                  && account.getFINOutIntransitAcct() != null)
                confirmation = true;
              else if (("WIT").equals(lines.get(0).getOUTUponClearingUse())
                  && account.getWithdrawalAccount() != null)
                confirmation = true;
              else if (("CLE").equals(lines.get(0).getOUTUponClearingUse())
                  && account.getClearedPaymentAccountOUT() != null)
                confirmation = true;
            }
          }
        } else if (transaction.getGLItem() != null) {
          for (FIN_FinancialAccountAccounting account : accounts) {
            if (confirmation)
              return confirmation;
            if ("BPD".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccount() != null) {
              confirmation = true;
            } else if ("BPW".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccountOUT() != null) {
              confirmation = true;
            }
          }
        } else {
          for (FIN_FinancialAccountAccounting account : accounts) {
            if (confirmation)
              return confirmation;
            if ("BF".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccountOUT() != null) {
              confirmation = true;
            }
          }
        }
      }
      if (confirmation) {
        // Exists line in closed period
        Period period = documentGetPeriod(reconciliation.getTransactionDate());
        OBCriteria<FIN_ReconciliationLine_v> obCriteria = OBDal.getInstance().createCriteria(
            FIN_ReconciliationLine_v.class);
        obCriteria.add(Restrictions.eq(FIN_ReconciliationLine_v.PROPERTY_RECONCILIATION,
            reconciliation));
        obCriteria.add(Restrictions.or(
            Restrictions.ge(FIN_ReconciliationLine_v.PROPERTY_ACCOUNTINGDATE,
                period.getEndingDate()),
            Restrictions.le(FIN_ReconciliationLine_v.PROPERTY_ACCOUNTINGDATE,
                period.getStartingDate())));
        obCriteria.setFilterOnReadableOrganization(false);
        obCriteria.addOrder(Order.asc(FIN_ReconciliationLine_v.PROPERTY_ACCOUNTINGDATE));
        obCriteria.toString();
        List<FIN_ReconciliationLine_v> lines = obCriteria.list();
        for (FIN_ReconciliationLine_v line : lines) {
          Period linePeriod = documentGetPeriod(line.getAccountingDate());
          if (linePeriod == null) {
            confirmation = false;
            setStatus(STATUS_PeriodClosed);
            return confirmation;
          }
        }
      }
    } catch (Exception e) {
      setStatus(STATUS_DocumentDisabled);
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    if (!confirmation)
      setStatus(STATUS_DocumentDisabled);
    return confirmation;
  }

  @Override
  public void loadObjectFieldProvider(ConnectionProvider conn, String strAD_Client_ID, String Id)
      throws ServletException {
    FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class, Id);

    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      data[0] = new FieldProviderFactory(null);
      FieldProviderFactory.setField(data[0], "AD_Client_ID", reconciliation.getClient().getId());
      FieldProviderFactory.setField(data[0], "AD_Org_ID", reconciliation.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "FIN_Finacc_Transaction_ID", reconciliation.getId());
      FieldProviderFactory.setField(data[0], "C_Currency_ID", reconciliation.getAccount()
          .getCurrency().getId());
      FieldProviderFactory.setField(data[0], "C_Doctype_ID", reconciliation.getDocumentType()
          .getId());
      FieldProviderFactory.setField(data[0], "DocumentNo", reconciliation.getDocumentNo());
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      FieldProviderFactory.setField(data[0], "statementDate",
          outputFormat.format(reconciliation.getTransactionDate()));
      FieldProviderFactory.setField(data[0], "Posted", reconciliation.getPosted());
      FieldProviderFactory.setField(data[0], "Processed", reconciliation.isProcessed() ? "Y" : "N");
      FieldProviderFactory.setField(data[0], "Processing", reconciliation.isProcessNow() ? "Y"
          : "N");
    } finally {
      OBContext.restorePreviousMode();
    }
    setObjectFieldProvider(data);
  }

  public Account getWithdrawalAccount(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    return getAccountTransaction(conn, finAccount, as, false);
  }

  public Account getDepositAccount(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    return getAccountTransaction(conn, finAccount, as, true);
  }

  public Account getAccountTransaction(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as, boolean bIsReceipt) throws ServletException {
    Account account = null;
    OBContext.setAdminMode();
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Restrictions.eq(
          FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(
              org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0) {
        return account;
      }
      if (bIsReceipt) {
        account = new Account(conn, accountList.get(0).getDepositAccount().getId());
      } else {
        account = new Account(conn, accountList.get(0).getWithdrawalAccount().getId());
      }
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Account", bIsReceipt ? "@DepositAccount@" : "@WithdrawalAccount@");
        parameters.put("Entity", finAccount.getIdentifier());
        parameters.put(
            "AccountingSchema",
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  public Account getAccountTransactionPayment(ConnectionProvider conn, FIN_Payment payment,
      AcctSchema as) throws ServletException {
    Account account = null;
    OBContext.setAdminMode();
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT,
          payment.getAccount()));
      accounts.add(Restrictions.eq(
          FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(
              org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
          payment.getPaymentMethod()));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      if (accountList == null || accountList.size() == 0) {
        return account;
      }
      AccountingCombination result = null;
      if (payment.isReceipt()) {
        if (lines.get(0).getUponDepositUse().equals("INT"))
          result = accountList.get(0).getInTransitPaymentAccountIN();
        else if (lines.get(0).getUponDepositUse().equals("DEP"))
          result = accountList.get(0).getDepositAccount();
        else if (lines.get(0).getUponDepositUse().equals("CLE"))
          result = accountList.get(0).getClearedPaymentAccount();
      } else {
        if (lines.get(0).getUponWithdrawalUse().equals("INT"))
          result = accountList.get(0).getFINOutIntransitAcct();
        else if (lines.get(0).getUponWithdrawalUse().equals("WIT"))
          result = accountList.get(0).getWithdrawalAccount();
        else if (lines.get(0).getUponWithdrawalUse().equals("CLE"))
          result = accountList.get(0).getClearedPaymentAccountOUT();
      }
      if (result != null)
        account = new Account(conn, result.getId());
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Account", payment.isReceipt() ? "@UponDepositAccount@"
            : "@UponWithdrawalAccount@");
        parameters.put("Entity", payment.getAccount().getIdentifier() + " - "
            + payment.getPaymentMethod().getIdentifier());
        parameters.put(
            "AccountingSchema",
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  public Account getClearOutAccount(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    return getAccount(conn, finAccount, as, false);
  }

  public Account getClearInAccount(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    return getAccount(conn, finAccount, as, true);
  }

  public Account getAccount(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as, boolean bIsReceipt) throws ServletException {
    Account account = null;
    OBContext.setAdminMode();
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Restrictions.eq(
          FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(
              org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0)
        return null;
      if (bIsReceipt)
        account = new Account(conn, accountList.get(0).getClearedPaymentAccount().getId());
      else
        account = new Account(conn, accountList.get(0).getClearedPaymentAccountOUT().getId());
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = getInvalidAccountParameters(
            (bIsReceipt ? "@ClearedPaymentAccount@" : "@ClearedPaymentAccountOUT@"),
            finAccount.getIdentifier(),
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  @Deprecated
  public Account getAccountPayment(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as, boolean bIsReceipt) throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Restrictions.eq(
          FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(
              org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0) {
        return account;
      }
      if (bIsReceipt) {
        account = new Account(conn, accountList.get(0).getReceivePaymentAccount().getId());
      } else {
        account = new Account(conn, accountList.get(0).getMakePaymentAccount().getId());
      }
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = getInvalidAccountParameters(
            (bIsReceipt ? "ReceivePayment" : "MakePayment"),
            finAccount.getIdentifier(),
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  public Account getAccountPayment(ConnectionProvider conn, FIN_Payment payment, AcctSchema as)
      throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    AccountingCombination result = null;
    try {
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
          payment.getPaymentMethod()));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT,
          payment.getAccount()));
      accounts.add(Restrictions.eq(
          FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(
              org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (payment.isReceipt()) {
        if (("INT").equals(lines.get(0).getUponReceiptUse()))
          result = accountList.get(0).getInTransitPaymentAccountIN();
        else if (("DEP").equals(lines.get(0).getUponReceiptUse()))
          result = accountList.get(0).getDepositAccount();
        else if (("CLE").equals(lines.get(0).getUponReceiptUse()))
          result = accountList.get(0).getClearedPaymentAccount();
      } else {
        if (("INT").equals(lines.get(0).getUponPaymentUse()))
          result = accountList.get(0).getFINOutIntransitAcct();
        else if (("WIT").equals(lines.get(0).getUponPaymentUse()))
          result = accountList.get(0).getWithdrawalAccount();
        else if (("CLE").equals(lines.get(0).getUponPaymentUse()))
          result = accountList.get(0).getClearedPaymentAccountOUT();
      }
      if (result != null)
        account = new Account(conn, result.getId());
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = getInvalidAccountParameters(
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier(),
            payment.getAccount().getIdentifier() + " - "
                + payment.getPaymentMethod().getIdentifier(),
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  public Account getAccountReconciliation(ConnectionProvider conn, FIN_Payment payment,
      AcctSchema as) throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    AccountingCombination result = null;
    try {
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
          payment.getPaymentMethod()));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT,
          payment.getAccount()));
      accounts.add(Restrictions.eq(
          FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(
              org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (payment.isReceipt()) {
        if (("INT").equals(lines.get(0).getINUponClearingUse()))
          result = accountList.get(0).getInTransitPaymentAccountIN();
        else if (("DEP").equals(lines.get(0).getINUponClearingUse()))
          result = accountList.get(0).getDepositAccount();
        else if (("CLE").equals(lines.get(0).getINUponClearingUse()))
          result = accountList.get(0).getClearedPaymentAccount();
      } else {
        if (("INT").equals(lines.get(0).getOUTUponClearingUse()))
          result = accountList.get(0).getFINOutIntransitAcct();
        else if (("WIT").equals(lines.get(0).getOUTUponClearingUse()))
          result = accountList.get(0).getWithdrawalAccount();
        else if (("CLE").equals(lines.get(0).getOUTUponClearingUse()))
          result = accountList.get(0).getClearedPaymentAccountOUT();
      }
      if (result != null)
        account = new Account(conn, result.getId());
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = getInvalidAccountParameters(
            payment.isReceipt() ? "@INUponClearingUse@" : "@OUTUponClearingUse@",
            payment.getAccount().getIdentifier() + " - "
                + payment.getPaymentMethod().getIdentifier(),
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  Period documentGetPeriod(Date date) {
    OBCriteria<PeriodControl> obCriteria = OBDal.getInstance().createCriteria(PeriodControl.class);
    obCriteria.createAlias(PeriodControl.PROPERTY_PERIOD, "p");
    obCriteria.createAlias("p." + Period.PROPERTY_YEAR, "y");
    obCriteria.add(Restrictions.eq(PeriodControl.PROPERTY_PERIODSTATUS, "O"));
    obCriteria.add(Restrictions.eq(PeriodControl.PROPERTY_DOCUMENTCATEGORY,
        AcctServer.DOCTYPE_Reconciliation));
    obCriteria.add(Restrictions.eq("y." + Year.PROPERTY_CALENDAR, getCalendar(AD_Org_ID)));
    obCriteria.add(Restrictions.in(PeriodControl.PROPERTY_ORGANIZATION + "."
        + Organization.PROPERTY_ID, OBContext.getOBContext().getOrganizationStructureProvider()
        .getNaturalTree(AD_Org_ID)));
    obCriteria.add(Restrictions.le("p." + Period.PROPERTY_STARTINGDATE, date));
    obCriteria.add(Restrictions.ge("p." + Period.PROPERTY_ENDINGDATE, date));
    obCriteria.setFilterOnReadableOrganization(false);
    List<PeriodControl> lines = obCriteria.list();
    return lines.size() == 0 ? null : lines.get(0).getPeriod();
  }

  Calendar getCalendar(String organization) {
    OBCriteria<Organization> obCriteria = OBDal.getInstance().createCriteria(Organization.class);
    obCriteria.add(Restrictions.eq(Organization.PROPERTY_ID, organization));
    obCriteria.setFilterOnReadableClients(false);
    obCriteria.setFilterOnReadableOrganization(false);
    List<Organization> lines = obCriteria.list();
    Calendar calendar = lines.get(0).getCalendar();
    if (calendar != null) {
      return calendar;
    } else {
      return getCalendar(OBContext.getOBContext().getOrganizationStructureProvider()
          .getParentOrg(organization));
    }
  }

  private Account getAccountWriteOffBPartner(String AcctType, String strBPartnerId, AcctSchema as,
      ConnectionProvider conn) {
    AcctServerData[] data = null;
    Account acct = null;
    try {
      if (AcctType.equals(ACCTTYPE_WriteOff)) {
        data = AcctServerData.selectWriteOffAcct(conn, strBPartnerId, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_WriteOff_Revenue)) {
        data = AcctServerData.selectWriteOffAcctRevenue(conn, strBPartnerId,
            as.getC_AcctSchema_ID());
      }
      // Get Acct
      String Account_ID = "";
      if (data != null && data.length != 0) {
        Account_ID = data[0].accountId;
      } else
        return acct;
      // No account
      if (Account_ID.equals("")) {
        log4j.warn("AcctServer - getAccount - NO account Type=" + AcctType + ", Record="
            + Record_ID);
        return acct;
      }
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    } finally {
      if (acct == null) {
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBPartnerId);
        Map<String, String> parameters = getInvalidAccountParameters(
            AcctType.equals(ACCTTYPE_WriteOff) ? "@WriteOffAccount@" : "@WriteOff_RevenueAccount@",
            bp != null ? bp.getIdentifier() : "",
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return acct;
  }
}
