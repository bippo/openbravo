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

package org.openbravo.advpaymentmngt.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.service.db.CallStoredProcedure;

public abstract class FIN_BankStatementImport {
  private FIN_FinancialAccount financialAccount;
  OBError myError = null;
  String filename = "";

  /** TALONES - REINTEGROS */
  public static final String DOCUMENT_BankStatementFile = "BSF";

  public FIN_BankStatementImport(FIN_FinancialAccount _financialAccount) {
    setFinancialAccount(_financialAccount);
  }

  public FIN_BankStatementImport() {
  }

  /**
   * @return the myError
   */
  public OBError getMyError() {
    return myError;
  }

  /**
   * @param error
   *          the myError to set
   */
  public void setMyError(OBError error) {
    this.myError = error;
  }

  public void init(FIN_FinancialAccount _financialAccount) {
    setFinancialAccount(_financialAccount);
  }

  private void setFinancialAccount(FIN_FinancialAccount _financialAccount) {
    financialAccount = _financialAccount;
  }

  private InputStream getFile(VariablesSecureApp vars) throws IOException {
    FileItem fi = vars.getMultiFile("inpFile");
    if (fi == null)
      throw new IOException("Invalid filename");
    filename = fi.getName();
    InputStream in = fi.getInputStream();
    if (in == null)
      throw new IOException("Corrupted file");
    return in;
  }

  private FIN_BankStatement createFINBankStatement(ConnectionProvider conn, VariablesSecureApp vars)
      throws Exception {
    final FIN_BankStatement newBankStatement = OBProvider.getInstance()
        .get(FIN_BankStatement.class);
    newBankStatement.setAccount(financialAccount);
    DocumentType doc = null;
    try {
      doc = getDocumentType();
    } catch (Exception e) {
      throw new Exception(e);
    }
    String documentNo = getDocumentNo(conn, vars, doc);
    newBankStatement.setDocumentType(doc);
    newBankStatement.setDocumentNo(documentNo);
    newBankStatement.setOrganization(financialAccount.getOrganization());
    String name = documentNo + " - " + filename;
    if (name.length() > 60) {
      name = name.substring(0, 60);
    }
    newBankStatement.setName(name);
    newBankStatement.setImportdate(new Date());
    newBankStatement.setTransactionDate(new Date());
    newBankStatement.setFileName(filename);
    OBDal.getInstance().save(newBankStatement);
    OBDal.getInstance().flush();
    return newBankStatement;
  }

  public OBError importFile(ConnectionProvider conn, VariablesSecureApp vars) {
    InputStream file = null;
    FIN_BankStatement bankStatement;
    List<FIN_BankStatementLine> bankStatementLines = new ArrayList<FIN_BankStatementLine>();
    int numberOfLines = 0;

    try {
      file = getFile(vars);
    } catch (IOException e) {
      return getOBError(conn, vars, "@WrongFile@", "Error", "Error");
    }

    try {
      bankStatement = createFINBankStatement(conn, vars);
    } catch (Exception ex) {
      return getOBError(conn, vars, "@APRM_DocumentTypeNotFound@", "Error", "Error");
    }

    try {
      bankStatementLines = loadFile(file, bankStatement);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      return getMyError();
    }
    if (bankStatementLines == null || bankStatementLines.size() == 0) {
      OBDal.getInstance().rollbackAndClose();
      return getMyError();
    }

    try {
      numberOfLines = saveFINBankStatementLines(bankStatementLines);
      OBDal.getInstance().refresh(bankStatement);
      OBError processResult = FIN_AddPayment.processBankStatement(vars, conn, "P",
          bankStatement.getId());
      setMyError(processResult);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      return getMyError();
    }
    if (getMyError() != null && !getMyError().getType().toLowerCase().equals("success")) {
      OBDal.getInstance().rollbackAndClose();
      return getMyError();
    } else if (getMyError() != null && getMyError().getType().toLowerCase().equals("success")) {
      return getMyError();
    } else {
      return getOBError(conn, vars, "@APRM_BankStatementNo@ " + bankStatement.getDocumentNo()
          + "<br/>" + numberOfLines + " " + "@RowsInserted@", "Success", "Success");
    }
  }

  OBError getOBError(ConnectionProvider conn, VariablesSecureApp vars, String strMessage,
      String strMsgType, String strTittle) {
    OBError message = new OBError();
    message.setType(strMsgType);
    message.setTitle(Utility.messageBD(conn, strTittle, vars.getLanguage()));
    message.setMessage(Utility.parseTranslation(conn, vars, vars.getLanguage(), strMessage));
    return message;
  }

  private int saveFINBankStatementLines(List<FIN_BankStatementLine> bankStatementLines) {
    int counter = 0;
    for (FIN_BankStatementLine bankStatementLine : bankStatementLines) {
      BusinessPartner businessPartner;
      try {
        businessPartner = matchBusinessPartner(bankStatementLine.getBpartnername(),
            bankStatementLine.getOrganization(), bankStatementLine.getBankStatement().getAccount());
      } catch (Exception e) {
        businessPartner = null;
      }
      bankStatementLine.setBusinessPartner(businessPartner);
      OBDal.getInstance().save(bankStatementLine);
      counter++;
    }
    OBDal.getInstance().flush();
    return counter;
  }

  private DocumentType getDocumentType() throws Exception {
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(financialAccount.getClient());
    parameters.add(financialAccount.getOrganization());
    parameters.add(DOCUMENT_BankStatementFile);
    String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
        parameters, null);
    if (strDocTypeId == null) {
      throw new Exception("The Document Type is missing for the Bank Statement");
    }
    return new AdvPaymentMngtDao().getObject(DocumentType.class, strDocTypeId);
  }

  private String getDocumentNo(ConnectionProvider conn, VariablesSecureApp vars,
      DocumentType documentType) {
    return Utility.getDocumentNo(conn, vars, "AddPaymentFromInvoice", "FIN_Payment",
        documentType.getId(), documentType.getId(), false, true);

  }

  private BusinessPartner matchBusinessPartner(String partnername, Organization organization,
      FIN_FinancialAccount account) {
    // TODO extend with other matching methods. It will make it easier to later reconcile
    BusinessPartner bp = matchBusinessPartnerByName(partnername, organization, account);
    if (bp == null) {
      bp = finBPByName(partnername, organization);
    }
    if (bp == null) {
      bp = matchBusinessPartnerByNameTokens(partnername, organization);
    }
    return bp;
  }

  private BusinessPartner matchBusinessPartnerByName(String partnername, Organization organization,
      FIN_FinancialAccount account) {
    if (partnername == null || "".equals(partnername)) {
      return null;
    }
    final StringBuilder whereClause = new StringBuilder();
    List<Object> parameters = new ArrayList<Object>();
    OBContext.setAdminMode();
    try {
      whereClause.append(" as bsl ");
      whereClause.append(" where bsl." + FIN_BankStatementLine.PROPERTY_BPARTNERNAME + " = ?");
      parameters.add(partnername);
      whereClause.append(" and bsl." + FIN_BankStatementLine.PROPERTY_BUSINESSPARTNER
          + " is not null");
      whereClause.append(" and bsl." + FIN_BankStatementLine.PROPERTY_BANKSTATEMENT + ".");
      whereClause.append(FIN_BankStatement.PROPERTY_ACCOUNT + ".id = ?");
      parameters.add(account.getId());
      whereClause.append(" and bsl." + FIN_BankStatementLine.PROPERTY_ORGANIZATION + ".id in (");
      whereClause.append(FIN_Utility.getInStrSet(new OrganizationStructureProvider()
          .getNaturalTree(organization.getId())) + ") ");
      whereClause.append(" and bsl.bankStatement.processed = 'Y'");
      whereClause.append(" order by bsl." + FIN_BankStatementLine.PROPERTY_CREATIONDATE + " desc");
      final OBQuery<FIN_BankStatementLine> bsl = OBDal.getInstance().createQuery(
          FIN_BankStatementLine.class, whereClause.toString(), parameters);
      bsl.setFilterOnReadableOrganization(false);
      List<FIN_BankStatementLine> matchedLines = bsl.list();
      if (matchedLines.size() == 0)
        return null;
      else
        return matchedLines.get(0).getBusinessPartner();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private BusinessPartner finBPByName(String partnername, Organization organization) {
    if (partnername == null || "".equals(partnername)) {
      return null;
    }
    final StringBuilder whereClause = new StringBuilder();
    List<Object> parameters = new ArrayList<Object>();

    OBContext.setAdminMode();
    try {
      whereClause.append(" as bp ");
      whereClause.append(" where bp." + BusinessPartner.PROPERTY_NAME + " = ?");
      parameters.add(partnername);
      whereClause.append(" and bp." + BusinessPartner.PROPERTY_ORGANIZATION + ".id in (");
      whereClause.append(FIN_Utility.getInStrSet(new OrganizationStructureProvider()
          .getNaturalTree(organization.getId())) + ") ");
      final OBQuery<BusinessPartner> bp = OBDal.getInstance().createQuery(BusinessPartner.class,
          whereClause.toString(), parameters);
      bp.setFilterOnReadableOrganization(false);
      List<BusinessPartner> matchedBP = bp.list();
      if (matchedBP.size() == 0)
        return null;
      else
        return matchedBP.get(0);

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public abstract List<FIN_BankStatementLine> loadFile(InputStream in,
      FIN_BankStatement targetBankStatement);

  private BusinessPartner matchBusinessPartnerByNameTokens(String partnername,
      Organization organization) {
    if (partnername == null || "".equals(partnername)) {
      return null;
    }
    StringTokenizer st = new StringTokenizer(partnername);
    List<String> list = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (token.length() > 3) {
        list.add(token);
      }
    }
    if (list.isEmpty()) {
      return null;
    }
    final StringBuilder whereClause = new StringBuilder();
    List<Object> parameters = new ArrayList<Object>();
    OBContext.setAdminMode();
    try {
      whereClause.append(" as b ");
      whereClause.append(" where (");
      for (String token : list) {
        whereClause.append(" lower(b." + BusinessPartner.PROPERTY_NAME + ") like lower(?) or ");
        parameters.add("%" + token + "%");
      }
      whereClause.delete(whereClause.length() - 3, whereClause.length()).append(")");
      whereClause.append(" and b." + BusinessPartner.PROPERTY_ORGANIZATION + ".id in (");
      whereClause.append(FIN_Utility.getInStrSet(new OrganizationStructureProvider()
          .getNaturalTree(organization.getId())) + ") ");
      final OBQuery<BusinessPartner> bl = OBDal.getInstance().createQuery(BusinessPartner.class,
          whereClause.toString(), parameters);
      bl.setFilterOnReadableOrganization(false);
      List<BusinessPartner> businessPartners = bl.list();
      if (businessPartners.size() == 0) {
        return null;
      } else if (businessPartners.size() == 1) {
        return businessPartners.get(0);
      } else {
        return closest(businessPartners, partnername);
      }

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private BusinessPartner closest(List<BusinessPartner> businessPartners, String partnername) {
    BusinessPartner targetBusinessPartner = businessPartners.get(0);
    int distance = StringUtils.getLevenshteinDistance(partnername, businessPartners.get(0)
        .getName());
    for (BusinessPartner bp : businessPartners) {
      // Calculates distance between two strings meaning number of changes required for a string to
      // convert in another string
      int bpDistance = StringUtils.getLevenshteinDistance(partnername, bp.getName());
      if (bpDistance < distance) {
        distance = bpDistance;
        targetBusinessPartner = bp;
      }
    }
    // Tolerance: discard business partners where number of changes needed to match is higher than
    // half of its length
    if (distance > (partnername.length() / 2)) {
      return null;
    } else {
      return targetBusinessPartner;
    }
  }

}
