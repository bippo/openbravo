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
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

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
import org.openbravo.model.common.enterprise.AcctSchemaTableDocType;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;

public class DocFINBankStatement extends AcctServer {

  private static final long serialVersionUID = 1L;

  String SeqNo = "0";
  BigDecimal totalAmount = BigDecimal.ZERO;

  public DocFINBankStatement() {
  }

  public DocFINBankStatement(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  @Override
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_FinBankStatement;
    DateDoc = data[0].getField("statementDate");
    C_DocType_ID = data[0].getField("C_Doctype_ID");
    DocumentNo = data[0].getField("DocumentNo");
    OBContext.setAdminMode();
    try {
      FIN_BankStatement bankStatement = OBDal.getInstance().get(FIN_BankStatement.class, Record_ID);
      totalAmount = getTotalAmount(bankStatement);
      Amounts[0] = totalAmount.toString();
    } finally {
      OBContext.restorePreviousMode();
    }
    loadDocumentType();
    return true;
  }

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
          DocFINBankStatementTemplate newTemplate = (DocFINBankStatementTemplate) Class.forName(
              strClassname).newInstance();
          return newTemplate.createFact(this, as, conn, con, vars);
        } catch (Exception e) {
          log4j.error("Error while creating new instance for DocFINBankStatementTemplate - " + e);
        }
      }
      String Fact_Acct_Group_ID = SequenceIdData.getUUID();
      FIN_BankStatement bst = OBDal.getInstance().get(FIN_BankStatement.class, Record_ID);
      fact.createLine(null, getAccount(conn, bst.getAccount(), as, totalAmount.signum() < 0),
          C_Currency_ID,
          (totalAmount.signum() > 0 ? totalAmount.abs().toString() : ZERO.toString()),
          (totalAmount.signum() < 0 ? totalAmount.abs().toString() : ZERO.toString()),
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      fact.createLine(null, getAccount(conn, bst.getAccount(), as, totalAmount.signum() > 0),
          C_Currency_ID,
          (totalAmount.signum() < 0 ? totalAmount.abs().toString() : ZERO.toString()),
          (totalAmount.signum() > 0 ? totalAmount.abs().toString() : ZERO.toString()),
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    } finally {
      OBContext.restorePreviousMode();
    }
    return fact;
  }

  /**
   * Get Source Currency Balance - subtracts line amounts from total - no rounding
   * 
   * @return positive amount, if total is bigger than lines
   */
  @Override
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    return retValue;
  } // getBalance

  public boolean getDocumentConfirmation(ConnectionProvider conn, String Id) {
    FIN_BankStatement bankStatement = OBDal.getInstance().get(FIN_BankStatement.class, Id);
    for (FIN_FinancialAccountAccounting faa : bankStatement.getAccount()
        .getFINFinancialAccountAcctList()) {
      if (faa.isEnablebankstatement() && faa.getFINAssetAcct() != null
          && faa.getFINTransitoryAcct() != null) {
        return true;
      }
    }
    setStatus(STATUS_DocumentDisabled);
    return false;
  }

  @Override
  public void loadObjectFieldProvider(ConnectionProvider conn, String strAD_Client_ID, String Id)
      throws ServletException {
    FIN_BankStatement bankStatement = OBDal.getInstance().get(FIN_BankStatement.class, Id);

    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      data[0] = new FieldProviderFactory(null);
      FieldProviderFactory.setField(data[0], "AD_Client_ID", bankStatement.getClient().getId());
      FieldProviderFactory.setField(data[0], "AD_Org_ID", bankStatement.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "FIN_BankStatement_ID", bankStatement.getId());
      FieldProviderFactory.setField(data[0], "C_Currency_ID", bankStatement.getAccount()
          .getCurrency().getId());
      FieldProviderFactory.setField(data[0], "C_Doctype_ID", bankStatement.getDocumentType()
          .getId());
      FieldProviderFactory.setField(data[0], "DocumentNo", bankStatement.getDocumentNo());
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      FieldProviderFactory.setField(data[0], "statementDate",
          outputFormat.format(bankStatement.getTransactionDate()));
      FieldProviderFactory.setField(data[0], "Posted", bankStatement.getPosted());
      FieldProviderFactory.setField(data[0], "Processed", bankStatement.isProcessed() ? "Y" : "N");
      FieldProviderFactory
          .setField(data[0], "Processing", bankStatement.isProcessNow() ? "Y" : "N");
    } finally {
      OBContext.restorePreviousMode();

    }
    setObjectFieldProvider(data);
  }

  public Account getAccount(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as, boolean isTransitAccount) throws ServletException {
    String strValidCombinationId = "";
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
      if (isTransitAccount)
        strValidCombinationId = accountList.get(0).getFINTransitoryAcct() == null ? ""
            : accountList.get(0).getFINTransitoryAcct().getId();
      else
        strValidCombinationId = accountList.get(0).getFINAssetAcct() == null ? "" : accountList
            .get(0).getFINAssetAcct().getId();
      if (strValidCombinationId.equals(""))
        return null;
    } finally {
      OBContext.restorePreviousMode();
    }
    return new Account(conn, strValidCombinationId);
  }

  BigDecimal getTotalAmount(FIN_BankStatement bankStatement) {
    BigDecimal amount = BigDecimal.ZERO;
    Iterator<FIN_BankStatementLine> lines = bankStatement.getFINBankStatementLineList().iterator();
    while (lines.hasNext()) {
      FIN_BankStatementLine line = lines.next();
      amount = amount.add(line.getDramount()).subtract(line.getCramount());
    }
    return amount;
  }

  public String nextSeqNo(String oldSeqNo) {
    log4j.debug("DocFINBankStatement - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4j.debug("DocFINBankStatement - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

}