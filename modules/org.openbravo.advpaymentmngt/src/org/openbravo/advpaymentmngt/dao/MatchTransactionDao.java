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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.db.CallStoredProcedure;

public class MatchTransactionDao {

  public MatchTransactionDao() {
  }

  public static <T extends BaseOBObject> T getObject(Class<T> t, String strId) {
    return OBDal.getInstance().get(t, strId);
  }

  @Deprecated
  public static FIN_Reconciliation getReconciliationPending() {
    OBCriteria<FIN_Reconciliation> obCriteria = OBDal.getInstance().createCriteria(
        FIN_Reconciliation.class);
    obCriteria.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_PROCESSED, false));
    List<FIN_Reconciliation> lines = obCriteria.list();

    if (lines.isEmpty())
      return null;
    else
      return lines.get(0);
  }

  public static BigDecimal getClearedLinesAmount(String strReconciliationId) {
    OBCriteria<FIN_FinaccTransaction> obCriteria = OBDal.getInstance().createCriteria(
        FIN_FinaccTransaction.class);
    obCriteria.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_RECONCILIATION,
        MatchTransactionDao.getObject(FIN_Reconciliation.class, strReconciliationId)));
    obCriteria.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_STATUS, "RPPC"));
    List<FIN_FinaccTransaction> lines = obCriteria.list();

    BigDecimal total = new BigDecimal("0");
    if (lines.isEmpty())
      return total;

    for (FIN_FinaccTransaction item : lines) {
      total = total.add(item.getDepositAmount().subtract(item.getPaymentAmount()));
    }

    return total;
  }

  public static boolean checkAllLinesCleared(String strFinancialAccountId) {
    // Check if all lines has been cleared: Bank Statement Lines
    OBCriteria<FIN_BankStatementLine> obCriteria = OBDal.getInstance().createCriteria(
        FIN_BankStatementLine.class);
    FIN_FinancialAccount financialAccount = MatchTransactionDao.getObject(
        FIN_FinancialAccount.class, strFinancialAccountId);
    // FIXME : ****There should be some other filter, like the reconciliation id?
    obCriteria.add(Restrictions.in(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT,
        financialAccount.getFINBankStatementList()));
    obCriteria.add(Restrictions.isNull(FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION));
    obCriteria.setMaxResults(1);
    List<FIN_BankStatementLine> lines = obCriteria.list();

    return (lines.isEmpty());
  }

  public static FIN_Reconciliation addNewReconciliation(ConnectionProvider conProvider,
      VariablesSecureApp vars, String strFinancialAccount) throws ServletException {
    FIN_FinancialAccount financialAccount = MatchTransactionDao.getObject(
        FIN_FinancialAccount.class, strFinancialAccount);
    // FIXME: added to access table to be
    // removed when new security implementation is done
    OBContext.setAdminMode();

    final FIN_Reconciliation newData = OBProvider.getInstance().get(FIN_Reconciliation.class);
    try {
      final List<Object> parameters = new ArrayList<Object>();
      parameters.add(financialAccount.getClient().getId());
      parameters.add(financialAccount.getOrganization().getId());
      parameters.add("REC");
      String strDocType = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
          parameters, null);
      if (strDocType == null || strDocType.equals("")) {
        // FIXME : Well-formed error message
        throw new ServletException("No Document Type defined for the Reconciliation");
      }
      String strDocumentNo = Utility.getDocumentNo(conProvider, vars, "Finnancial Transaction",
          "FIN_Reconciliation", strDocType, strDocType, false, true);
      if (strDocumentNo == null || strDocumentNo.equals("")) {
        // FIXME : Well-formed error message
        throw new ServletException(
            "No Reconciliation Document Number obtained for the defined Document Type");
      }
      String strDocStatus = "DR";
      newData.setActive(true);
      newData.setOrganization(financialAccount.getOrganization());
      newData.setClient(financialAccount.getClient());
      newData.setAccount(financialAccount);
      newData.setDocumentNo(strDocumentNo);
      newData.setDocumentType(MatchTransactionDao.getObject(DocumentType.class, strDocType));
      newData.setDocumentStatus(strDocStatus);
      Date endingDate = MatchTransactionDao.getBankStatementLineMaxDate(financialAccount);
      newData.setTransactionDate(endingDate != null ? endingDate : new Date());
      newData.setEndingDate(endingDate != null ? endingDate : new Date());
      BigDecimal startingBalance = MatchTransactionDao
          .getReconciliationLastAmount(financialAccount);
      OBDal.getInstance().save(newData);
      OBDal.getInstance().flush();
      newData.setEndingBalance(startingBalance.add(getReconciliationEndingBalance(newData)));
      newData.setStartingbalance(startingBalance != null ? startingBalance : BigDecimal.ZERO);

      OBDal.getInstance().save(newData);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }

    return newData;
  }

  public static List<FIN_BankStatementLine> getMatchingBankStatementLines(
      String strFinancialAccountId, String strReconciliationId, String strPaymentTypeFilter,
      String strShowCleared) {
    final StringBuilder whereClause = new StringBuilder();

    whereClause.append(" as bsl ");
    whereClause.append(" left outer join bsl.financialAccountTransaction as fat");
    whereClause.append(" where bsl.").append(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT);
    whereClause.append(".").append(FIN_BankStatement.PROPERTY_ACCOUNT).append(".id = '");
    whereClause.append(strFinancialAccountId).append("'");
    whereClause.append(" and bsl.bankStatement.processed = 'Y'");
    if (strPaymentTypeFilter.equalsIgnoreCase("D")) {
      whereClause.append("   and (bsl.").append(FIN_BankStatementLine.PROPERTY_DRAMOUNT);
      whereClause.append(" is null ");
      whereClause.append("   or bsl.").append(FIN_BankStatementLine.PROPERTY_DRAMOUNT);
      whereClause.append(" = 0) ");
    } else if (strPaymentTypeFilter.equalsIgnoreCase("P")) {
      whereClause.append("   and (bsl.").append(FIN_BankStatementLine.PROPERTY_CRAMOUNT);
      whereClause.append(" is null ");
      whereClause.append("   or bsl.").append(FIN_BankStatementLine.PROPERTY_CRAMOUNT);
      whereClause.append(" = 0) ");
    }
    whereClause.append("   and (fat is null");
    whereClause.append("   or (fat.").append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION);
    whereClause.append(".id = '").append(strReconciliationId).append("'");
    if (!strShowCleared.equalsIgnoreCase("Y")) {
      whereClause.append("   and fat.").append(FIN_FinaccTransaction.PROPERTY_STATUS);
      whereClause.append(" <> 'RPPC' ");
    }
    whereClause.append("))");

    whereClause.append(" order by bsl.").append(FIN_BankStatementLine.PROPERTY_TRANSACTIONDATE);
    whereClause.append(", bsl.").append(FIN_BankStatementLine.PROPERTY_LINENO);
    whereClause.append(", bsl.").append(FIN_BankStatementLine.PROPERTY_BPARTNERNAME);
    final OBQuery<FIN_BankStatementLine> obData = OBDal.getInstance().createQuery(
        FIN_BankStatementLine.class, whereClause.toString());

    return obData.list();
  }

  public static List<FIN_BankStatementLine> getUnMatchedBankStatementLines(
      FIN_FinancialAccount account) {
    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as bsl ");
    whereClause.append(" where bsl.").append(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT);
    whereClause.append(".").append(FIN_BankStatement.PROPERTY_ACCOUNT).append(".id = '");
    whereClause.append(account.getId()).append("'");
    whereClause.append("   and bsl.").append(
        FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION);
    whereClause.append(" is null");
    whereClause.append(" and bsl.bankStatement.processed = 'Y'");
    final OBQuery<FIN_BankStatementLine> obData = OBDal.getInstance().createQuery(
        FIN_BankStatementLine.class, whereClause.toString());

    return obData.list();
  }

  public static List<FIN_BankStatementLine> getMatchedBankStatementLines(
      FIN_BankStatement bankStatement) {
    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as bsl ");
    whereClause.append(" where bsl.").append(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT);
    whereClause.append(".").append(".id = '");
    whereClause.append(bankStatement.getId()).append("'");
    whereClause.append("   and bsl.").append(
        FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION);
    whereClause.append(" is not null");
    whereClause.append(" and bsl.bankStatement.processed = 'Y'");
    final OBQuery<FIN_BankStatementLine> obData = OBDal.getInstance().createQuery(
        FIN_BankStatementLine.class, whereClause.toString());
    return obData.list();
  }

  public static List<FIN_FinaccTransaction> getMatchingFinancialTransaction(
      String strFinancialAccountId, Date transactionDate, String strReference, BigDecimal amount,
      String strBpartner, List<FIN_FinaccTransaction> excluded) {
    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    List<FIN_FinaccTransaction> result = null;
    OBContext.setAdminMode();
    try {
      whereClause.append(" as ft ");
      whereClause.append(" where ft.").append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
      whereClause.append(".id = '").append(strFinancialAccountId).append("'");
      whereClause.append("   and ft.").append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION);
      whereClause.append(" is null");
      whereClause.append("   and ft.").append(FIN_FinaccTransaction.PROPERTY_STATUS);
      whereClause.append(" <> 'RPPC' ");
      whereClause.append("   and (ft.").append(FIN_FinaccTransaction.PROPERTY_DEPOSITAMOUNT);
      whereClause.append(" - ").append(FIN_FinaccTransaction.PROPERTY_PAYMENTAMOUNT).append(")");
      whereClause.append(" = ?");
      parameters.add(amount);
      if (transactionDate != null) {
        whereClause.append("   and ft.").append(FIN_FinaccTransaction.PROPERTY_TRANSACTIONDATE);
        whereClause.append(" = ?");
        parameters.add(transactionDate);
      }
      whereClause.append("   and ft.").append(FIN_FinaccTransaction.PROPERTY_FINPAYMENT);
      whereClause.append(".").append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
      whereClause.append(".").append(BusinessPartner.PROPERTY_NAME);

      if (strBpartner != null) {
        whereClause.append(" = ?");
        parameters.add(strBpartner);
      } else {
        whereClause.append(" is null");
      }
      if (!"".equals(strReference) && !"**".equals(strReference)) {
        whereClause.append("   and (ft.").append(FIN_FinaccTransaction.PROPERTY_FINPAYMENT);
        whereClause.append(".").append(FIN_Payment.PROPERTY_REFERENCENO);
        whereClause.append(" = ?");
        parameters.add(strReference);
        whereClause.append("   or ft.").append(FIN_FinaccTransaction.PROPERTY_FINPAYMENT);
        whereClause.append(".").append(FIN_Payment.PROPERTY_DOCUMENTNO);
        whereClause.append(" = ?)");
        parameters.add(strReference);
      }

      final OBQuery<FIN_FinaccTransaction> obData = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString());
      obData.setParameters(parameters);

      result = obData.list();
      result.removeAll(excluded);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  public static List<FIN_FinaccTransaction> getMatchingFinancialTransaction(
      String strFinancialAccountId, String strReference, BigDecimal amount, String strBpartner,
      List<FIN_FinaccTransaction> excluded) {
    return getMatchingFinancialTransaction(strFinancialAccountId, null, strReference, amount,
        strBpartner, excluded);
  }

  public static List<FIN_FinaccTransaction> getMatchingFinancialTransaction(
      String strFinancialAccountId, Date transactionDate, BigDecimal amount,
      List<FIN_FinaccTransaction> excluded) {
    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();

    whereClause.append(" as ft ");
    whereClause.append(" where ft.").append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
    whereClause.append(".id = '").append(strFinancialAccountId).append("'");
    whereClause.append("   and ft.").append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION);
    whereClause.append(" is null");
    whereClause.append("   and ft.").append(FIN_FinaccTransaction.PROPERTY_STATUS);
    whereClause.append(" <> 'RPPC' ");
    whereClause.append("   and (ft.").append(FIN_FinaccTransaction.PROPERTY_DEPOSITAMOUNT);
    whereClause.append(" - ").append(FIN_FinaccTransaction.PROPERTY_PAYMENTAMOUNT).append(")");
    whereClause.append(" = ?");
    parameters.add(amount);
    if (transactionDate != null) {
      whereClause.append("   and ft.").append(FIN_FinaccTransaction.PROPERTY_TRANSACTIONDATE);
      whereClause.append(" = ?");
      parameters.add(transactionDate);
    }
    final OBQuery<FIN_FinaccTransaction> obData = OBDal.getInstance().createQuery(
        FIN_FinaccTransaction.class, whereClause.toString());
    obData.setParameters(parameters);
    List<FIN_FinaccTransaction> result = obData.list();
    result.removeAll(excluded);
    return result;
  }

  public static List<FIN_FinaccTransaction> getMatchingFinancialTransaction(
      String strFinancialAccountId, BigDecimal amount, List<FIN_FinaccTransaction> excluded) {
    return getMatchingFinancialTransaction(strFinancialAccountId, null, amount, excluded);
  }

  public static List<FIN_FinaccTransaction> getMatchingFinancialTransaction(
      String strFinancialAccountId, BigDecimal amount) {
    return getMatchingFinancialTransaction(strFinancialAccountId, null, amount, null);
  }

  public static Date getBankStatementLineMaxDate(FIN_FinancialAccount financialAccount) {
    OBContext.setAdminMode();
    Date maxDate = new Date();
    try {
      final OBCriteria<FIN_BankStatementLine> obc = OBDal.getInstance().createCriteria(
          FIN_BankStatementLine.class);
      obc.createAlias(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT, "bs");
      obc.add(Restrictions.eq("bs." + FIN_BankStatement.PROPERTY_ACCOUNT, financialAccount));
      obc.add(Restrictions.eq("bs." + FIN_BankStatement.PROPERTY_PROCESSED, true));
      obc.addOrderBy(FIN_BankStatementLine.PROPERTY_TRANSACTIONDATE, false);
      obc.setMaxResults(1);
      final List<FIN_BankStatementLine> bst = obc.list();
      if (bst.size() == 0)
        return maxDate;
      maxDate = bst.get(0).getTransactionDate();
    } finally {
      OBContext.restorePreviousMode();
    }
    return maxDate;
  }

  public static BigDecimal getReconciliationLastAmount(FIN_FinancialAccount financialAccount) {
    OBContext.setAdminMode();
    BigDecimal lastAmount = financialAccount.getInitialBalance();
    try {
      final OBCriteria<FIN_Reconciliation> obc = OBDal.getInstance().createCriteria(
          FIN_Reconciliation.class);
      obc.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_PROCESSED, true));
      obc.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT, financialAccount));
      obc.addOrderBy(FIN_Reconciliation.PROPERTY_ENDINGDATE, false);
      obc.setMaxResults(1);
      final List<FIN_Reconciliation> rec = obc.list();
      if (rec.size() == 0)
        return lastAmount;
      lastAmount = rec.get(0).getEndingBalance();
    } finally {
      OBContext.restorePreviousMode();
    }
    return lastAmount;
  }

  /**
   * Calculates the ending balance of automatic reconciliations. The sum of all the bank statement
   * lines of the reconciliation financial account that belong to the given reconciliation plus the
   * ones that does not have a transaction associated yet.
   * 
   * @param reconciliation
   *          Reconciliation.
   * @return Ending balance of an automatic reconciliation.
   */
  public static BigDecimal getReconciliationEndingBalance(FIN_Reconciliation reconciliation) {
    BigDecimal total = BigDecimal.ZERO;
    OBContext.setAdminMode(true);
    try {
      OBCriteria<FIN_BankStatementLine> obcBsl = OBDal.getInstance().createCriteria(
          FIN_BankStatementLine.class);
      obcBsl.createAlias(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT, "bs");
      obcBsl.createAlias(FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION, "tr",
          OBCriteria.LEFT_JOIN);
      obcBsl.add(Restrictions.or(
          Restrictions.isNull(FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION),
          Restrictions.eq("tr." + FIN_FinaccTransaction.PROPERTY_RECONCILIATION, reconciliation)));

      obcBsl.add(Restrictions.eq("bs." + FIN_BankStatement.PROPERTY_ACCOUNT,
          reconciliation.getAccount()));
      obcBsl.add(Restrictions.eq("bs." + FIN_BankStatement.PROPERTY_PROCESSED, true));
      ProjectionList projections = Projections.projectionList();
      projections.add(Projections.sum(FIN_BankStatementLine.PROPERTY_CRAMOUNT));
      projections.add(Projections.sum(FIN_BankStatementLine.PROPERTY_DRAMOUNT));
      obcBsl.setProjection(projections);

      if (obcBsl.list() != null && obcBsl.list().size() > 0) {
        @SuppressWarnings("rawtypes")
        List o = obcBsl.list();
        Object[] resultSet = (Object[]) o.get(0);
        BigDecimal credit = (resultSet[0] != null) ? (BigDecimal) resultSet[0] : BigDecimal.ZERO;
        BigDecimal debit = (resultSet[1] != null) ? (BigDecimal) resultSet[1] : BigDecimal.ZERO;
        total = credit.subtract(debit);
      }

    } finally {
      OBContext.restorePreviousMode();
    }

    return total;
  }

  /**
   * Calculates the balance of unmatched bank statements for the given reconciliation
   * 
   * @param lastReconciliation
   *          Reconciliation.
   * @return Last reconciliation UnMatched balance
   */
  public static BigDecimal getLastReconciliationUnmatchedBalance(
      FIN_Reconciliation lastReconciliation) {
    BigDecimal total = BigDecimal.ZERO;
    OBContext.setAdminMode(true);
    try {
      OBCriteria<FIN_BankStatementLine> obcBsl = OBDal.getInstance().createCriteria(
          FIN_BankStatementLine.class);
      obcBsl.createAlias(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT, "bs");
      obcBsl.createAlias(FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION, "tr",
          OBCriteria.LEFT_JOIN);

      List<FIN_Reconciliation> afterReconciliations = getReconciliationListAfterDate(lastReconciliation);
      if (afterReconciliations.size() > 0) {
        obcBsl.add(Restrictions.or(Restrictions
            .isNull(FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION), Restrictions.in(
            "tr." + FIN_FinaccTransaction.PROPERTY_RECONCILIATION, afterReconciliations)));
      } else {
        obcBsl.add(Restrictions.isNull(FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION));
      }
      obcBsl.add(Restrictions.eq("bs." + FIN_BankStatement.PROPERTY_ACCOUNT,
          lastReconciliation.getAccount()));
      obcBsl.add(Restrictions.eq("bs." + FIN_BankStatement.PROPERTY_PROCESSED, true));
      obcBsl.add(Restrictions.le(FIN_BankStatementLine.PROPERTY_TRANSACTIONDATE,
          lastReconciliation.getTransactionDate()));
      ProjectionList projections = Projections.projectionList();
      projections.add(Projections.sum(FIN_BankStatementLine.PROPERTY_CRAMOUNT));
      projections.add(Projections.sum(FIN_BankStatementLine.PROPERTY_DRAMOUNT));
      obcBsl.setProjection(projections);

      if (obcBsl.list() != null && obcBsl.list().size() > 0) {
        @SuppressWarnings("rawtypes")
        List o = obcBsl.list();
        Object[] resultSet = (Object[]) o.get(0);
        BigDecimal credit = (resultSet[0] != null) ? (BigDecimal) resultSet[0] : BigDecimal.ZERO;
        BigDecimal debit = (resultSet[1] != null) ? (BigDecimal) resultSet[1] : BigDecimal.ZERO;
        total = credit.subtract(debit);
      }

    } finally {
      OBContext.restorePreviousMode();
    }

    return total;
  }

  /**
   * 
   * @param reconciliation
   * @return List of later reconciliations that given one for the same financial account
   */
  public static List<FIN_Reconciliation> getReconciliationListAfterDate(
      FIN_Reconciliation reconciliation) {
    OBContext.setAdminMode(true);
    List<FIN_Reconciliation> reconciliations = new ArrayList<FIN_Reconciliation>();
    try {
      OBCriteria<FIN_Reconciliation> obc = OBDal.getInstance().createCriteria(
          FIN_Reconciliation.class);
      obc.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT, reconciliation.getAccount()));
      obc.add(Restrictions.gt(FIN_Reconciliation.PROPERTY_CREATIONDATE,
          reconciliation.getCreationDate()));
      reconciliations = obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
    return reconciliations;
  }
}
