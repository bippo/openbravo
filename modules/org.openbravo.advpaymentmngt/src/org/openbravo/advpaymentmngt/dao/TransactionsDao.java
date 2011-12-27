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

package org.openbravo.advpaymentmngt.dao;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBObjectFieldProvider;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;

public class TransactionsDao {

  public static List<Tab> getWindowData(String className) {

    final List<Object> parameters = new ArrayList<Object>();
    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as td");
    whereClause.append(" left outer join td.window as win");
    whereClause.append(" left outer join td.masterDetailForm as mdf");
    whereClause.append(" where UPPER(mdf.javaClassName) = UPPER(?)");
    parameters.add(className);

    final OBQuery<Tab> obQuery = OBDal.getInstance().createQuery(Tab.class, whereClause.toString());
    obQuery.setParameters(parameters);
    return obQuery.list();
  }

  public static OBObjectFieldProvider[] getAccTrxData(String finFinancialAccountId) {
    final List<Object> parameters = new ArrayList<Object>();
    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as ft");
    whereClause.append(" left outer join ft.account as acc");
    whereClause.append(" left outer join ft.reconciliation as rec");
    whereClause.append(" where acc.id = rec.account.id");
    whereClause.append(" and acc.id = ?");
    parameters.add(finFinancialAccountId);
    OBContext.setAdminMode();
    try {
      final OBQuery<FIN_FinaccTransaction> obQuery = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString());
      obQuery.setParameters(parameters);
      OBObjectFieldProvider[] objectFieldProvider = null;
      if (obQuery != null && obQuery.list().size() > 0) {
        objectFieldProvider = OBObjectFieldProvider.createOBObjectFieldProvider(obQuery.list());
      }
      return objectFieldProvider;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static FIN_FinaccTransaction createFinAccTransaction(FIN_Payment payment) {
    final FIN_FinaccTransaction newTransaction = OBProvider.getInstance().get(
        FIN_FinaccTransaction.class);
    OBContext.setAdminMode();
    try {
      newTransaction.setFinPayment(payment);
      newTransaction.setOrganization(payment.getOrganization());
      newTransaction.setAccount(payment.getAccount());
      newTransaction.setDateAcct(payment.getPaymentDate());
      newTransaction.setTransactionDate(payment.getPaymentDate());
      newTransaction.setActivity(payment.getActivity());
      newTransaction.setProject(payment.getProject());
      newTransaction.setCurrency(payment.getAccount().getCurrency());
      newTransaction.setDescription(payment
          .getDescription()
          .replace("\n", ". ")
          .substring(0,
              payment.getDescription().length() > 254 ? 254 : payment.getDescription().length()));
      newTransaction.setClient(payment.getClient());
      newTransaction.setLineNo(getTransactionMaxLineNo(payment.getAccount()) + 10);

      BigDecimal depositAmt = FIN_Utility.getDepositAmount(payment.getDocumentType()
          .getDocumentCategory().equals("ARR"), payment.getFinancialTransactionAmount());
      BigDecimal paymentAmt = FIN_Utility.getPaymentAmount(payment.getDocumentType()
          .getDocumentCategory().equals("ARR"), payment.getFinancialTransactionAmount());

      newTransaction.setDepositAmount(depositAmt);
      newTransaction.setPaymentAmount(paymentAmt);
      newTransaction.setStatus(newTransaction.getDepositAmount().compareTo(
          newTransaction.getPaymentAmount()) > 0 ? "RPR" : "PPM");
      if (!newTransaction.getCurrency().equals(payment.getCurrency())) {
        newTransaction.setForeignCurrency(payment.getCurrency());
        newTransaction.setForeignConversionRate(payment.getFinancialTransactionConvertRate());
        newTransaction.setForeignAmount(payment.getAmount());
      }
      OBDal.getInstance().save(newTransaction);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
    return newTransaction;
  }

  public static Long getTransactionMaxLineNo(FIN_FinancialAccount financialAccount) {
    OBContext.setAdminMode();
    Long maxLine = 0l;
    try {
      final OBCriteria<FIN_FinaccTransaction> obc = OBDal.getInstance().createCriteria(
          FIN_FinaccTransaction.class);
      obc.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_ACCOUNT, financialAccount));
      obc.addOrderBy(FIN_FinaccTransaction.PROPERTY_LINENO, false);
      obc.setMaxResults(1);
      final List<FIN_FinaccTransaction> fat = obc.list();
      if (fat.size() == 0)
        return 0l;
      maxLine = fat.get(0).getLineNo();
    } finally {
      OBContext.restorePreviousMode();
    }
    return maxLine;
  }

  @Deprecated
  public static void process(FIN_FinaccTransaction finFinancialAccountTransaction) {
    final FIN_FinancialAccount financialAccount = OBDal.getInstance().get(
        FIN_FinancialAccount.class, finFinancialAccountTransaction.getAccount().getId());
    financialAccount.setCurrentBalance(financialAccount.getCurrentBalance().add(
        finFinancialAccountTransaction.getDepositAmount().subtract(
            finFinancialAccountTransaction.getPaymentAmount())));
    finFinancialAccountTransaction.setProcessed(true);
    FIN_Payment payment = finFinancialAccountTransaction.getFinPayment();
    if (payment != null) {
      payment.setStatus(payment.isReceipt() ? "RDNC" : "PWNC");
      finFinancialAccountTransaction.setStatus(payment.isReceipt() ? "RDNC" : "PWNC");
      OBDal.getInstance().save(payment);
    } else {
      finFinancialAccountTransaction.setStatus(finFinancialAccountTransaction.getDepositAmount()
          .compareTo(finFinancialAccountTransaction.getPaymentAmount()) > 0 ? "RDNC" : "PWNC");
    }
    OBDal.getInstance().save(financialAccount);
    OBDal.getInstance().save(finFinancialAccountTransaction);
    OBDal.getInstance().flush();
    return;
  }

  public static void post(VariablesSecureApp vars, ConnectionProvider connectionProvider,
      FIN_FinaccTransaction finFinancialAccountTransaction) {
    final String AD_TABLE_ID = "4D8C3B3C31D1410DA046140C9F024D17";
    try {
      AcctServer acct = AcctServer.get(AD_TABLE_ID, vars.getClient(),
          finFinancialAccountTransaction.getOrganization().getId(), connectionProvider);
      if (acct == null) {
        throw new OBException("Accounting process failed for the financial account transaction");
      } else if (!acct.post(finFinancialAccountTransaction.getId(), false, vars,
          connectionProvider, connectionProvider.getConnection()) || acct.errors != 0) {
        connectionProvider.releaseRollbackConnection(connectionProvider.getConnection());
        throw new OBException(acct.getMessageResult().getMessage());
      }
    } catch (Exception e) {
      throw new OBException("Accounting process failed for the financial account transaction", e);
    }
  }

  public static FIN_Reconciliation getLastReconciliation(FIN_FinancialAccount account,
      String isProcessed) {
    OBContext.setAdminMode();
    try {
      final OBCriteria<FIN_Reconciliation> obc = OBDal.getInstance().createCriteria(
          FIN_Reconciliation.class);
      obc.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT, account));
      if ("Y".equals(isProcessed)) {
        obc.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_PROCESSED, true));
      } else if ("N".equals(isProcessed)) {
        obc.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_PROCESSED, false));
      }
      obc.addOrderBy(FIN_Reconciliation.PROPERTY_ENDINGDATE, false);
      obc.addOrderBy(FIN_Reconciliation.PROPERTY_CREATIONDATE, false);
      obc.setMaxResults(1);
      final List<FIN_Reconciliation> rec = obc.list();
      if (rec.size() == 0)
        return null;
      return rec.get(0);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static int getPendingToMatchCount(FIN_FinancialAccount financialAccount) {
    final StringBuilder whereClause = new StringBuilder();

    OBContext.setAdminMode();
    try {
      whereClause.append(" as fatrx ");
      whereClause.append(" where fatrx.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
      whereClause.append(".id='");
      whereClause.append(financialAccount.getId());
      whereClause.append("'");
      whereClause.append(" and fatrx.").append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION)
          .append(" is null ");
      final OBQuery<FIN_FinaccTransaction> obqFATrx = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString());
      return obqFATrx.list().size();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static void updateAccountingDate(FIN_FinaccTransaction transaction) {
    final String FIN_FINACC_TRANSACTION_TABLE = "4D8C3B3C31D1410DA046140C9F024D17";
    OBCriteria<AccountingFact> obcAF = OBDal.getInstance().createCriteria(AccountingFact.class);
    obcAF.add(Restrictions.eq(AccountingFact.PROPERTY_TABLE,
        OBDal.getInstance().get(Table.class, FIN_FINACC_TRANSACTION_TABLE)));
    obcAF.add(Restrictions.eq(AccountingFact.PROPERTY_RECORDID, transaction.getId()));
    for (AccountingFact aFact : obcAF.list()) {
      aFact.setAccountingDate(transaction.getTransactionDate());
      aFact.setTransactionDate(transaction.getTransactionDate());
      aFact.setPeriod(getPeriod(transaction.getTransactionDate()));
    }
    return;
  }

  public static Period getPeriod(Date date) {
    Period period = null;
    OBCriteria<Period> obcPe = OBDal.getInstance().createCriteria(Period.class);
    obcPe.add(Restrictions.le(Period.PROPERTY_ENDINGDATE, date));
    obcPe.add(Restrictions.ge(Period.PROPERTY_STARTINGDATE, date));
    if (obcPe.list() != null && obcPe.list().size() > 0) {
      period = obcPe.list().get(0);
    }
    return period;
  }

  public static List<FIN_FinaccTransaction> getTransactionsToReconciled(
      FIN_FinancialAccount account, Date statementDate, boolean hideAfterDate) {

    OBContext.setAdminMode();
    try {

      final List<Object> parameters = new ArrayList<Object>();
      final StringBuilder whereClause = new StringBuilder();
      whereClause.append(" as ft");
      whereClause.append(" left outer join ft.reconciliation as rec");
      whereClause.append(" where ft.account.id = ?");
      whereClause.append(" and (rec is null or rec.processed = 'N')");
      whereClause.append(" and ft.processed = 'Y'");
      parameters.add(account.getId());
      if (hideAfterDate) {
        whereClause.append(" and ft.transactionDate < ?");
        parameters.add(statementDate);
      }
      whereClause.append(" order by ft.transactionDate, ft.lineNo");

      final OBQuery<FIN_FinaccTransaction> obQuery = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString(), parameters);

      return obQuery.list();

    } catch (Exception e) {
      throw new OBException(e);

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static FieldProvider[] getTransactionsFiltered(FIN_FinancialAccount account,
      Date statementDate, boolean hideAfterDate) {

    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    OBContext.setAdminMode();
    try {

      final List<Object> parameters = new ArrayList<Object>();
      final StringBuilder whereClause = new StringBuilder();
      whereClause.append(" as ft");
      whereClause.append(" left outer join ft.reconciliation as rec");
      whereClause.append(" where ft.account.id = ?");
      whereClause.append(" and (rec is null or rec.processed = 'N')");
      whereClause.append(" and ft.processed = 'Y'");
      parameters.add(account.getId());
      if (hideAfterDate) {
        whereClause.append(" and ft.transactionDate < ?");
        parameters.add(statementDate);
      }
      whereClause.append(" order by ft.transactionDate, ft.lineNo");

      final OBQuery<FIN_FinaccTransaction> obQuery = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString(), parameters);

      List<FIN_FinaccTransaction> transactionOBList = obQuery.list();

      FIN_FinaccTransaction[] FIN_Transactions = new FIN_FinaccTransaction[0];
      FIN_Transactions = transactionOBList.toArray(FIN_Transactions);
      FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(transactionOBList);

      for (int i = 0; i < data.length; i++) {
        String strPaymentDocNo = "";
        String strBusinessPartner = "";
        FieldProviderFactory.setField(data[i], "transactionId", FIN_Transactions[i].getId());
        FieldProviderFactory.setField(data[i], "transactionDate",
            dateFormater.format(FIN_Transactions[i].getTransactionDate()));
        if (FIN_Transactions[i].getFinPayment() != null) {
          if (FIN_Transactions[i].getFinPayment().getBusinessPartner() != null) {
            strBusinessPartner = FIN_Transactions[i].getFinPayment().getBusinessPartner().getName();
          }
          strPaymentDocNo = FIN_Transactions[i].getFinPayment().getDocumentNo();
        }

        // Truncate business partner name
        String truncateBPname = (strBusinessPartner.length() > 30) ? strBusinessPartner
            .substring(0, 27).concat("...").toString() : strBusinessPartner;
        FieldProviderFactory.setField(data[i], "businessPartner",
            (strBusinessPartner.length() > 30) ? strBusinessPartner : "");
        FieldProviderFactory.setField(data[i], "businessPartnerTrunc", truncateBPname);

        FieldProviderFactory.setField(data[i], "paymentDocument", strPaymentDocNo);

        // Truncate description
        String description = FIN_Transactions[i].getDescription();
        String truncateDescription = "";
        if (description != null) {
          truncateDescription = (description.length() > 42) ? description.substring(0, 39)
              .concat("...").toString() : description;
        }
        FieldProviderFactory.setField(data[i], "description",
            (description != null && description.length() > 42) ? description : "");
        FieldProviderFactory.setField(data[i], "descriptionTrunc", truncateDescription);

        FieldProviderFactory.setField(data[i], "paymentAmount", FIN_Transactions[i]
            .getPaymentAmount().toString());
        FieldProviderFactory.setField(data[i], "depositAmount", FIN_Transactions[i]
            .getDepositAmount().toString());
        FieldProviderFactory.setField(data[i], "rownum", "" + (i + 1));
        FieldProviderFactory.setField(data[i], "markSelectedId",
            (FIN_Transactions[i].getStatus().equals("RPPC")) ? FIN_Transactions[i].getId() : "");
      }

      return data;

    } catch (Exception e) {
      throw new OBException(e);

    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
