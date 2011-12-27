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

package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.xmlEngine.XmlDocument;

public class AddTransaction extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "AddTransaction|Org");
      String strWindowId = vars.getRequestGlobalVariable("inpwindowId", "AddTransaction|windowId");
      String strTabId = vars.getRequestGlobalVariable("inpTabId", "AddTransaction|tabId");
      String strFinancialAccountId = vars.getStringParameter("inpfinFinancialAccountId");
      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);

      printPage(response, vars, strOrgId, strWindowId, strTabId, strFinancialAccountId,
          strFinBankStatementLineId);

    } else if (vars.commandIn("GRID")) {
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      boolean strIsReceipt = "RCIN".equals(vars.getStringParameter("inpDocumentType"));
      String strFromDate = vars.getStringParameter("inpDateFrom");
      String strToDate = vars.getStringParameter("inpDateTo");
      String closeAutomatically = vars
          .getSessionValue("AddPaymentFromTransaction|closeAutomatically");
      String paymentWithTransaction = vars.getSessionValue("AddPaymentFromTransaction|PaymentId");
      vars.removeSessionValue("AddPaymentFromTransaction|closeAutomatically");
      vars.removeSessionValue("AddPaymentFromTransaction|PaymentId");
      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);

      printGrid(response, vars, strFinancialAccountId, strFromDate, strToDate, strIsReceipt,
          strFinBankStatementLineId, closeAutomatically, paymentWithTransaction);

    } else if (vars.commandIn("SAVE")) {
      String strTabId = vars.getGlobalVariable("inpTabId", "AddTransaction|tabId");
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      String selectedPaymentsIds = vars.getInParameter("inpPaymentId", IsIDFilter.instance);
      String strTransactionType = vars.getStringParameter("inpTransactionType");
      String strTransactionDate = vars.getStringParameter("inpMainDate", "");

      String strGLItemId = vars.getStringParameter("inpGLItemId", "");
      String strGLItemDepositAmount = vars.getNumericParameter("inpDepositAmountGLItem", "");
      String strGLItemPaymentAmount = vars.getNumericParameter("inpPaymentAmountGLItem", "");
      String strGLItemDescription = vars.getStringParameter("inpGLItemDescription", "");

      String strFeeDepositAmount = vars.getNumericParameter("inpDepositAmount", "");
      String strFeePaymentAmount = vars.getNumericParameter("inpPaymentAmount", "");
      String strFeeDescription = vars.getStringParameter("inpFeeDescription", "");

      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);

      saveAndCloseWindow(response, vars, strTabId, strFinancialAccountId, selectedPaymentsIds,
          strTransactionType, strGLItemId, strGLItemDepositAmount, strGLItemPaymentAmount,
          strFeeDepositAmount, strFeePaymentAmount, strTransactionDate, strFinBankStatementLineId,
          strGLItemDescription, strFeeDescription);
    }

  }

  private void saveAndCloseWindow(HttpServletResponse response, VariablesSecureApp vars,
      String strTabId, String strFinancialAccountId, String selectedPaymentIds,
      String strTransactionType, String strGLItemId, String strGLItemDepositAmount,
      String strGLItemPaymentAmount, String strFeeDepositAmount, String strFeePaymentAmount,
      String strTransactionDate, String strFinBankStatementLineId, String strGLItemDescription,
      String strFeeDescription) throws IOException, ServletException {

    dao = new AdvPaymentMngtDao();
    String strMessage = "";
    OBError msg = new OBError();
    OBContext.setAdminMode();
    try {
      // SALES = DEPOSIT
      // PURCHASE = PAYMENT
      if (strTransactionType.equals("P")) { // Payment

        List<FIN_Payment> selectedPayments = FIN_Utility.getOBObjectList(FIN_Payment.class,
            selectedPaymentIds);

        for (FIN_Payment p : selectedPayments) {
          BigDecimal depositAmt = FIN_Utility.getDepositAmount(p.isReceipt(),
              p.getFinancialTransactionAmount());
          BigDecimal paymentAmt = FIN_Utility.getPaymentAmount(p.isReceipt(),
              p.getFinancialTransactionAmount());

          String description = null;
          if (p.getDescription() != null) {
            description = p.getDescription().replace("\n", ". ");
          }

          FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(p.getOrganization(), p
              .getAccount(), TransactionsDao.getTransactionMaxLineNo(p.getAccount()) + 10, p,
              description, FIN_Utility.getDate(strTransactionDate), null, p.isReceipt() ? "RDNC"
                  : "PWNC", depositAmt, paymentAmt, null, null, null,
              p.isReceipt() ? "BPD" : "BPW", FIN_Utility.getDate(strTransactionDate), p
                  .getCurrency(), p.getFinancialTransactionConvertRate(), p.getAmount());
          OBError processTransactionError = processTransaction(vars, this, "P", finTrans);
          if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
            throw new OBException(processTransactionError.getMessage());
          }
          if (!"".equals(strFinBankStatementLineId)) {
            matchBankStatementLine(vars, finTrans, strFinBankStatementLineId);
          }
        }

        if (selectedPaymentIds != null && selectedPayments.size() > 0) {
          strMessage = selectedPayments.size() + " " + "@RowsInserted@";
        }

      } else if (strTransactionType.equals("GL")) { // GL Item
        // Accounting Dimensions
        final String strElement_BP = vars.getStringParameter("inpCBPartnerId", IsIDFilter.instance);
        final BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
            strElement_BP);

        final String strElement_PR = vars.getStringParameter("inpMProductId", IsIDFilter.instance);
        final Product product = OBDal.getInstance().get(Product.class, strElement_PR);

        final String strElement_PJ = vars.getStringParameter("inpCProjectId", IsIDFilter.instance);
        final Project project = OBDal.getInstance().get(Project.class, strElement_PJ);

        final String strElement_AY = vars.getStringParameter("inpCActivityId", IsIDFilter.instance);
        final ABCActivity activity = OBDal.getInstance().get(ABCActivity.class, strElement_AY);

        final String strElement_SR = vars.getStringParameter("inpCSalesRegionId",
            IsIDFilter.instance);
        final SalesRegion salesRegion = OBDal.getInstance().get(SalesRegion.class, strElement_SR);

        final String strElement_MC = vars.getStringParameter("inpCampaignId", IsIDFilter.instance);
        final Campaign campaign = OBDal.getInstance().get(Campaign.class, strElement_MC);

        BigDecimal glItemDepositAmt = new BigDecimal(strGLItemDepositAmount);
        BigDecimal glItemPaymentAmt = new BigDecimal(strGLItemPaymentAmount);

        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        GLItem glItem = OBDal.getInstance().get(GLItem.class, strGLItemId);
        String description = strGLItemDescription.isEmpty() ? Utility.messageBD(this,
            "APRM_GLItem", vars.getLanguage()) + ": " + glItem.getName() : strGLItemDescription;
        boolean isReceipt = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);

        // Currency, Organization, paymentDate,
        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(account.getOrganization(),
            account, TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
            FIN_Utility.getDate(strTransactionDate), glItem, isReceipt ? "RDNC" : "PWNC",
            glItemDepositAmt, glItemPaymentAmt, project, campaign, activity, isReceipt ? "BPD"
                : "BPW", FIN_Utility.getDate(strTransactionDate), null, null, null,
            businessPartner, product, salesRegion);
        OBError processTransactionError = processTransaction(vars, this, "P", finTrans);
        if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
          throw new OBException(processTransactionError.getMessage());
        }
        strMessage = "1 " + "@RowsInserted@";
        if (!"".equals(strFinBankStatementLineId)) {
          matchBankStatementLine(vars, finTrans, strFinBankStatementLineId);
        }

      } else if (strTransactionType.equals("F")) { // Fee
        BigDecimal feeDepositAmt = new BigDecimal(strFeeDepositAmount);
        BigDecimal feePaymentAmt = new BigDecimal(strFeePaymentAmount);
        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        boolean isReceipt = (feeDepositAmt.compareTo(feePaymentAmt) >= 0);
        String description = strFeeDescription.isEmpty() ? Utility.messageBD(this, "APRM_BankFee",
            vars.getLanguage()) : strFeeDescription;

        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(account.getOrganization(),
            account, TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
            FIN_Utility.getDate(strTransactionDate), null, isReceipt ? "RDNC" : "PWNC",
            feeDepositAmt, feePaymentAmt, null, null, null, "BF",
            FIN_Utility.getDate(strTransactionDate), null, null, null);
        OBError processTransactionError = processTransaction(vars, this, "P", finTrans);
        if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
          throw new OBException(processTransactionError.getMessage());
        }
        strMessage = "1 " + "@RowsInserted@";
        if (!"".equals(strFinBankStatementLineId)) {
          matchBankStatementLine(vars, finTrans, strFinBankStatementLineId);
        }

      }

      // Message
      msg.setType("Success");
      msg.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
      vars.setMessage(strTabId, msg);
      msg = null;
      if ("".equals(strFinBankStatementLineId))
        printPageClosePopUpAndRefreshParent(response, vars);
      else {
        log4j.debug("Output: PopUp Response");
        final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/base/secureApp/PopUp_Close_Refresh").createXmlDocument();
        xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
        response.setContentType("text/html; charset=UTF-8");
        final PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
      }

    } catch (Exception e) {
      OBError newError = Utility.translateError(this, vars, vars.getLanguage(),
          FIN_Utility.getExceptionMessage(e));
      throw new OBException(newError.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strWindowId, String strTabId, String strFinancialAccountId,
      String strBankStatementLineId) throws IOException, ServletException {

    log4j.debug("Output: Add Transaction pressed on Financial Account || Transaction tab");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddTransaction").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("mainDate", DateTimeData.today(this));
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", strOrgId);
    xmlDocument.setParameter("finFinancialAccountId", strFinancialAccountId);
    xmlDocument.setParameter("finBankStatementLineId", strBankStatementLineId);
    if (!"".equals(strBankStatementLineId)) {
      FIN_BankStatementLine bsl = OBDal.getInstance().get(FIN_BankStatementLine.class,
          strBankStatementLineId);
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);

      xmlDocument.setParameter("depositAmount", bsl.getCramount().toString());
      xmlDocument.setParameter("paymentAmount", bsl.getDramount().toString());
      xmlDocument.setParameter("depositAmountGLItem", bsl.getCramount().toString());
      xmlDocument.setParameter("paymentAmountGLItem", bsl.getDramount().toString());
      xmlDocument.setParameter("mainDate", dateFormater.format(bsl.getTransactionDate()));

    } else {
      xmlDocument.setParameter("depositAmount", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("paymentAmount", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("depositAmountGLItem", BigDecimal.ZERO.toString());
      xmlDocument.setParameter("paymentAmountGLItem", BigDecimal.ZERO.toString());
    }

    // Accounting Dimensions
    final String strElement_BP = Utility.getContext(this, vars, "$Element_BP", strWindowId);
    final String strElement_PR = Utility.getContext(this, vars, "$Element_PR", strWindowId);
    final String strElement_PJ = Utility.getContext(this, vars, "$Element_PJ", strWindowId);
    final String strElement_AY = Utility.getContext(this, vars, "$Element_AY", strWindowId);
    final String strElement_SR = Utility.getContext(this, vars, "$Element_SR", strWindowId);
    final String strElement_MC = Utility.getContext(this, vars, "$Element_MC", strWindowId);
    xmlDocument.setParameter("strElement_BP", strElement_BP);
    xmlDocument.setParameter("strElement_PR", strElement_PR);
    xmlDocument.setParameter("strElement_PJ", strElement_PJ);
    xmlDocument.setParameter("strElement_AY", strElement_AY);
    xmlDocument.setParameter("strElement_SR", strElement_SR);
    xmlDocument.setParameter("strElement_MC", strElement_MC);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, String strFromDate, String strToDate, boolean isReceipt,
      String strFinBankStatementLineId, String closeAutomatically, String paymentWithTransaction)
      throws IOException, ServletException {
    dao = new AdvPaymentMngtDao();

    log4j.debug("Output: Grid with transactions not reconciled");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddTransactionGrid").createXmlDocument();

    OBContext.setAdminMode();
    try {
      // From AddPaymentFromTransaction the payment has been deposited and the transaction exist
      if (!"".equals(strFinBankStatementLineId) && !"".equals(paymentWithTransaction)
          && "Y".equals(closeAutomatically)) {

        FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, paymentWithTransaction);
        OBCriteria<FIN_FinaccTransaction> obcTrans = OBDal.getInstance().createCriteria(
            FIN_FinaccTransaction.class);
        obcTrans.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_FINPAYMENT, payment));
        FIN_FinaccTransaction finTrans = obcTrans.list().get(0);

        matchBankStatementLine(vars, finTrans, strFinBankStatementLineId);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);

    // Payments not deposited/withdrawal
    // Not stored in Fin_Finacc_Transaction table
    final FieldProvider[] data = dao.getPaymentsNotDeposited(account,
        FIN_Utility.getDate(strFromDate),
        FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strToDate, "1")), isReceipt);

    xmlDocument.setData("structure", (data == null) ? set() : data);
    JSONObject table = new JSONObject();
    try {
      table.put("grid", xmlDocument.print());
      table.put("closeAutomatically", closeAutomatically);
    } catch (JSONException e) {
      log4j.debug("JSON object error" + table.toString());
    }
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("data = " + table.toString());
    out.close();
  }

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();
    empty.put("paymentId", "");
    empty.put("paymentInfo", "");
    empty.put("paymentDescription", "");
    empty.put("paymentDate", "");
    empty.put("depositAmount", "");
    empty.put("paymentAmount", "");
    ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
    result.add(empty);
    return FieldProviderFactory.getFieldProviderArray(result);
  }

  private void matchBankStatementLine(VariablesSecureApp vars, FIN_FinaccTransaction finTrans,
      String strFinBankStatementLineId) {
    FIN_BankStatementLine bsline = dao.getObject(FIN_BankStatementLine.class,
        strFinBankStatementLineId);
    // The amounts must match
    if (bsline.getCramount().compareTo(finTrans.getDepositAmount()) != 0
        || bsline.getDramount().compareTo(finTrans.getPaymentAmount()) != 0) {
      vars.setSessionValue("AddTransaction|ShowJSMessage", "Y");
    } else {
      FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(
          finTrans.getAccount(), "N");
      bsline.setMatchingtype("AD");
      bsline.setFinancialAccountTransaction(finTrans);
      if (finTrans.getFinPayment() != null) {
        bsline.setBusinessPartner(finTrans.getFinPayment().getBusinessPartner());
        finTrans.getFinPayment().setStatus("RPPC");
      }
      finTrans.setReconciliation(reconciliation);
      finTrans.setStatus("RPPC");
      OBDal.getInstance().save(bsline);
      OBDal.getInstance().save(finTrans);
      OBDal.getInstance().flush();
    }
  }

  /**
   * It calls the Transaction Process for the given transaction and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strAction
   *          String with the action of the process. {P, D, R}
   * @param transaction
   *          FIN_FinaccTransaction that needs to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  private OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      String strAction, FIN_FinaccTransaction transaction) throws Exception {
    ProcessBundle pb = new ProcessBundle("F68F2890E96D4D85A1DEF0274D105BCE", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strAction);
    parameters.put("Fin_FinAcc_Transaction_ID", transaction.getId());
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_TransactionProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

  public String getServletInfo() {
    return "This servlet adds transaction for a financial account";
  }

}
