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
 * Contributor(s): Enterprise Intelligence Systems (http://www.eintel.com.au).
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.xmlEngine.XmlDocument;

public class AddPaymentFromTransaction extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int conversionRatePrecision = FIN_Utility.getConversionRatePrecision(vars);

    if (vars.commandIn("DEFAULT")) {
      final RequestFilter docTypeFilter = new ValueListFilter("RCIN", "PDOUT");
      final boolean isReceipt = vars.getRequiredStringParameter("inpDocumentType", docTypeFilter)
          .equals("RCIN");
      final String strFinancialAccountId = vars.getRequiredStringParameter(
          "inpFinFinancialAccountId", IsIDFilter.instance);
      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);
      String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId", "");
      String strTransactionDate = vars.getStringParameter("inpMainDate", "");
      String strWindowId = vars.getSessionValue("AddTransaction|windowId");

      printPage(response, vars, strFinancialAccountId, isReceipt, strFinBankStatementLineId,
          strTransactionDate, strCurrencyId, conversionRatePrecision, strWindowId);

    } else if (vars.commandIn("GRIDLIST")) {
      final String strBusinessPartnerId = vars.getRequestGlobalVariable("inpcBpartnerId", "");
      final String strFinancialAccountId = vars.getRequiredStringParameter("inpFinancialAccountId",
          IsIDFilter.instance);
      final String strDueDateFrom = vars.getStringParameter("inpDueDateFrom", "");
      final String strDueDateTo = vars.getStringParameter("inpDueDateTo", "");
      final String strTransDateFrom = vars.getStringParameter("inpTransDateFrom", "");
      final String strTransDateTo = vars.getStringParameter("inpTransDateTo", "");
      final String strDocumentType = vars.getStringParameter("inpDocumentType", "");
      final String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId", "");
      final String strDocumentNo = vars.getStringParameter("inpDocumentNo", "");
      final String strSelectedPaymentDetails = vars.getInStringParameter(
          "inpScheduledPaymentDetailId", IsIDFilter.instance);
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");

      printGrid(response, vars, strFinancialAccountId, strBusinessPartnerId, strDueDateFrom,
          strDueDateTo, strTransDateFrom, strTransDateTo, strDocumentType, strDocumentNo,
          strSelectedPaymentDetails, isReceipt, strCurrencyId);

    } else if (vars.commandIn("PAYMENTMETHODCOMBO")) {
      final String strBusinessPartnerId = vars.getRequestGlobalVariable("inpcBpartnerId", "");
      final String strFinancialAccountId = vars.getRequiredStringParameter("inpFinancialAccountId",
          IsIDFilter.instance);
      boolean isReceipt = "Y".equals(vars.getRequiredStringParameter("isReceipt"));
      refreshPaymentMethod(response, strBusinessPartnerId, strFinancialAccountId, isReceipt);

    } else if (vars.commandIn("LOADCREDIT")) {
      final String strBusinessPartnerId = vars.getRequiredStringParameter("inpcBpartnerId");
      final boolean isReceipt = "Y".equals(vars.getRequiredStringParameter("isReceipt"));
      final String strOrgId = vars.getRequiredStringParameter("inpadOrgId");
      BigDecimal customerCredit;
      try {
        OBContext.setAdminMode(true);
        customerCredit = dao.getCustomerCredit(
            OBDal.getInstance().get(BusinessPartner.class, strBusinessPartnerId), isReceipt, OBDal
                .getInstance().get(Organization.class, strOrgId));
      } finally {
        OBContext.restorePreviousMode();
      }
      response.setContentType("text/html; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      JSONObject json = new JSONObject();
      try {
        json.put("credit", customerCredit);
      } catch (JSONException e) {
        log4j.error("Error parsing load credit JSON: " + customerCredit, e);
      }
      out.println("data = " + json.toString());
      out.close();

    } else if (vars.commandIn("EXCHANGERATE")) {
      final String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId", "");
      final String strFinancialAccountCurrencyId = vars.getRequestGlobalVariable(
          "inpFinancialAccountCurrencyId", "");
      final String strPaymentDate = vars.getRequestGlobalVariable("inpPaymentDate", "");
      final String strFinancialAccountId = vars.getRequiredStringParameter("inpFinancialAccountId");
      FIN_FinancialAccount fa = OBDal.getInstance().get(FIN_FinancialAccount.class,
          strFinancialAccountId);
      refreshExchangeRate(response, strCurrencyId, strFinancialAccountCurrencyId, strPaymentDate,
          fa.getOrganization(), conversionRatePrecision);
    } else if (vars.commandIn("SAVE") || vars.commandIn("SAVEANDPROCESS")) {
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      String strAction = null;
      if (vars.commandIn("SAVEANDPROCESS")) {
        // The default option is process
        strAction = (isReceipt ? "PRP" : "PPP");
      } else {
        strAction = vars.getRequiredStringParameter("inpActionDocument");
      }
      String strPaymentDocumentNo = vars.getRequiredStringParameter("inpDocNumber");
      String strReceivedFromId = vars.getStringParameter("inpcBpartnerId");
      String strPaymentMethodId = vars.getRequiredStringParameter("inpPaymentMethod");
      String strFinancialAccountId = vars.getRequiredStringParameter("inpFinancialAccountId");
      String strPaymentAmount = vars.getRequiredNumericParameter("inpActualPayment");
      String strPaymentDate = vars.getRequiredStringParameter("inpPaymentDate");
      String strSelectedScheduledPaymentDetailIds = vars.getInParameter(
          "inpScheduledPaymentDetailId", IsIDFilter.instance);
      String strAddedGLItems = vars.getStringParameter("inpGLItems");
      JSONArray addedGLITemsArray = null;
      try {
        addedGLITemsArray = new JSONArray(strAddedGLItems);
      } catch (JSONException e) {
        log4j.error("Error parsing received GLItems JSON Array: " + strAddedGLItems, e);
        bdErrorGeneralPopUp(request, response, "Error",
            "Error parsing received GLItems JSON Array: " + strAddedGLItems);
        return;
      }
      String strDifferenceAction = vars.getStringParameter("inpDifferenceAction", "");
      BigDecimal refundAmount = BigDecimal.ZERO;
      if (strDifferenceAction.equals("refund"))
        refundAmount = new BigDecimal(vars.getRequiredNumericParameter("inpDifference"));
      String strReferenceNo = vars.getStringParameter("inpReferenceNo", "");
      String paymentCurrencyId = vars.getRequiredStringParameter("inpCurrencyId");
      BigDecimal exchangeRate = new BigDecimal(vars.getRequiredNumericParameter("inpExchangeRate",
          "1"));
      BigDecimal convertedAmount = new BigDecimal(vars.getRequiredNumericParameter(
          "inpActualConverted", strPaymentAmount));
      OBError message = null;
      // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
      // removed when new security implementation is done
      OBContext.setAdminMode();
      try {

        List<FIN_PaymentScheduleDetail> selectedPaymentDetails = FIN_Utility.getOBObjectList(
            FIN_PaymentScheduleDetail.class, strSelectedScheduledPaymentDetailIds);
        HashMap<String, BigDecimal> selectedPaymentDetailAmounts = FIN_AddPayment
            .getSelectedPaymentDetailsAndAmount(vars, selectedPaymentDetails);

        // When creating a payment for orders/invoices of different business partners (factoring)
        // the business partner must be empty
        BusinessPartner paymentBusinessPartner = null;
        if (selectedPaymentDetails.size() == 0 && !"".equals(strReceivedFromId)) {
          paymentBusinessPartner = OBDal.getInstance()
              .get(BusinessPartner.class, strReceivedFromId);
        } else {
          paymentBusinessPartner = getMultiBPartner(selectedPaymentDetails);
        }

        final List<Object> parameters = new ArrayList<Object>();
        parameters.add(vars.getClient());
        parameters.add(dao.getObject(FIN_FinancialAccount.class, strFinancialAccountId)
            .getOrganization().getId());
        parameters.add((isReceipt ? "ARR" : "APP"));
        // parameters.add(null);
        String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
            parameters, null);

        if (strPaymentDocumentNo.startsWith("<")) {
          // get DocumentNo
          strPaymentDocumentNo = Utility.getDocumentNo(this, vars, "AddPaymentFromTransaction",
              "FIN_Payment", strDocTypeId, strDocTypeId, false, true);
        }
        final FIN_FinancialAccount finAcc = dao.getObject(FIN_FinancialAccount.class,
            strFinancialAccountId);
        FIN_Payment payment = dao.getNewPayment(isReceipt, finAcc.getOrganization(),
            dao.getObject(DocumentType.class, strDocTypeId), strPaymentDocumentNo,
            paymentBusinessPartner, dao.getObject(FIN_PaymentMethod.class, strPaymentMethodId),
            finAcc, strPaymentAmount, FIN_Utility.getDate(strPaymentDate), strReferenceNo,
            dao.getObject(Currency.class, paymentCurrencyId), exchangeRate, convertedAmount);

        if (addedGLITemsArray != null) {
          for (int i = 0; i < addedGLITemsArray.length(); i++) {
            JSONObject glItem = addedGLITemsArray.getJSONObject(i);
            BigDecimal glItemOutAmt = new BigDecimal(glItem.getString("glitemPaidOutAmt"));
            BigDecimal glItemInAmt = new BigDecimal(glItem.getString("glitemReceivedInAmt"));
            BigDecimal glItemAmt = BigDecimal.ZERO;
            if (isReceipt) {
              glItemAmt = glItemInAmt.subtract(glItemOutAmt);
            } else {
              glItemAmt = glItemOutAmt.subtract(glItemInAmt);
            }
            final String strGLItemId = glItem.getString("glitemId");
            checkID(strGLItemId);

            // Accounting Dimensions
            final String strElement_BP = glItem.getString("cBpartnerDim");
            checkID(strElement_BP);
            final BusinessPartner businessPartner = dao.getObject(BusinessPartner.class,
                strElement_BP);

            final String strElement_PR = glItem.getString("mProductDim");
            checkID(strElement_PR);
            final Product product = dao.getObject(Product.class, strElement_PR);

            final String strElement_PJ = glItem.getString("cProjectDim");
            checkID(strElement_PJ);
            final Project project = dao.getObject(Project.class, strElement_PJ);

            final String strElement_AY = glItem.getString("cActivityDim");
            checkID(strElement_AY);
            final ABCActivity activity = dao.getObject(ABCActivity.class, strElement_AY);

            final String strElement_SR = glItem.getString("cSalesregionDim");
            checkID(strElement_SR);
            final SalesRegion salesRegion = dao.getObject(SalesRegion.class, strElement_SR);

            final String strElement_MC = glItem.getString("cCampaignDim");
            checkID(strElement_MC);
            final Campaign campaign = dao.getObject(Campaign.class, strElement_MC);

            FIN_AddPayment.saveGLItem(payment, glItemAmt, dao.getObject(GLItem.class, strGLItemId),
                businessPartner, product, project, campaign, activity, salesRegion);
          }
        }
        payment = FIN_AddPayment.savePayment(payment, isReceipt, null, null, null, null, null,
            null, null, null, null, selectedPaymentDetails, selectedPaymentDetailAmounts,
            strDifferenceAction.equals("writeoff"), strDifferenceAction.equals("refund"),
            dao.getObject(Currency.class, paymentCurrencyId), exchangeRate, convertedAmount);

        if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
            || strAction.equals("PPW")) {
          message = FIN_AddPayment.processPayment(vars, this,
              (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", payment);
          if (message != null && "Error".equals(message.getType())) {
            throw new OBException();
          }
          // PPW: process made payment and withdrawal
          // PRD: process made payment and deposit
          if ((strAction.equals("PRD") || strAction.equals("PPW"))
              && !"Error".equals(message.getType())) {
            vars.setSessionValue("AddPaymentFromTransaction|closeAutomatically", "Y");
            vars.setSessionValue("AddPaymentFromTransaction|PaymentId", payment.getId());
          }
          if (strDifferenceAction.equals("refund")) {
            Boolean newPayment = !payment.getFINPaymentDetailList().isEmpty();
            FIN_Payment refundPayment = FIN_AddPayment.createRefundPayment(this, vars, payment,
                refundAmount.negate(), exchangeRate);
            OBError auxMessage = FIN_AddPayment.processPayment(vars, this,
                (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", refundPayment);
            if (newPayment) {
              final String strNewRefundPaymentMessage = Utility
                  .parseTranslation(this, vars, vars.getLanguage(), "@APRM_RefundPayment@" + ": "
                      + refundPayment.getDocumentNo())
                  + ".";
              message.setMessage(strNewRefundPaymentMessage + " " + message.getMessage());
              if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) != 0) {
                payment
                    .setDescription(payment.getDescription() + strNewRefundPaymentMessage + "\n");
                OBDal.getInstance().save(payment);
                OBDal.getInstance().flush();
              }
            } else {
              message = auxMessage;
            }
          }
        }

      } catch (Exception ex) {
        String strMessage = FIN_Utility.getExceptionMessage(ex);
        if (message != null && "Error".equals(message.getType())) {
          strMessage = message.getMessage();
        }
        bdErrorGeneralPopUp(request, response, "Error", strMessage);
        OBDal.getInstance().rollbackAndClose();
        return;
      } finally {
        OBContext.restorePreviousMode();
      }

      log4j.debug("Output: PopUp Response");
      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/base/secureApp/PopUp_Close_Refresh").createXmlDocument();
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();

      // "../org.openbravo.advpaymentmngt.ad_actionbutton/AddTransaction.html");
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, boolean isReceipt, String strFinBankStatementLineId,
      String strTransactionDate, String strCurrencyId, int conversionRatePrecision,
      String strWindowId) throws IOException, ServletException {
    log4j.debug("Output: Add Payment button pressed on Add Transaction popup.");
    dao = new AdvPaymentMngtDao();
    String defaultPaymentMethod = "";

    final FIN_FinancialAccount financialAccount = dao.getObject(FIN_FinancialAccount.class,
        strFinancialAccountId);

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddPaymentFromTransaction")
        .createXmlDocument();

    if (!strFinBankStatementLineId.isEmpty()) {
      FIN_BankStatementLine bsline = dao.getObject(FIN_BankStatementLine.class,
          strFinBankStatementLineId);
      xmlDocument.setParameter("actualPayment",
          (isReceipt) ? bsline.getCramount().subtract(bsline.getDramount()).toString() : bsline
              .getDramount().subtract(bsline.getCramount()).toString());
      if (bsline.getBusinessPartner() == null) {
        OBCriteria<BusinessPartner> obcBP = OBDal.getInstance().createCriteria(
            BusinessPartner.class);
        obcBP.add(Restrictions.eq(BusinessPartner.PROPERTY_NAME, bsline.getBpartnername()));
        if (obcBP.list() != null && obcBP.list().size() > 0) {
          xmlDocument.setParameter("businessPartner", obcBP.list().get(0).getId());
          defaultPaymentMethod = (obcBP.list().get(0).getPaymentMethod() != null) ? obcBP.list()
              .get(0).getPaymentMethod().getId() : "";
        }
      } else {
        xmlDocument.setParameter("businessPartner", bsline.getBusinessPartner().getId());
      }
    }
    // Take payment date from the add transaction popup
    xmlDocument.setParameter("paymentDate", strTransactionDate.isEmpty() ? DateTimeData.today(this)
        : strTransactionDate);

    if (isReceipt)
      xmlDocument.setParameter("title",
          Utility.messageBD(this, "APRM_AddPaymentIn", vars.getLanguage()));
    else
      xmlDocument.setParameter("title",
          Utility.messageBD(this, "APRM_AddPaymentOut", vars.getLanguage()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("isReceipt", (isReceipt) ? "Y" : "N");
    xmlDocument.setParameter("isSoTrx", (isReceipt) ? "Y" : "N");
    xmlDocument.setParameter("finBankStatementLineId", strFinBankStatementLineId);
    xmlDocument.setParameter("orgId", financialAccount.getOrganization().getId());
    xmlDocument.setParameter("inheritedActualPayment", strFinBankStatementLineId.isEmpty() ? "N"
        : "Y");

    // get DocumentNo
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(vars.getClient());
    parameters.add(financialAccount.getOrganization().getId());
    parameters.add((isReceipt ? "ARR" : "APP"));
    // parameters.add(null);
    String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
        parameters, null);
    String strDocNo = Utility.getDocumentNo(this, vars, "AddPaymentFromTransaction", "FIN_Payment",
        strDocTypeId, strDocTypeId, false, false);
    xmlDocument.setParameter("documentNumber", "<" + strDocNo + ">");
    xmlDocument.setParameter("documentType", dao.getObject(DocumentType.class, strDocTypeId)
        .getName());

    Currency paymentCurrency;
    if (strCurrencyId == null || strCurrencyId.isEmpty()) {
      paymentCurrency = financialAccount.getCurrency();
    } else {
      paymentCurrency = dao.getObject(Currency.class, strCurrencyId);
    }

    xmlDocument.setParameter("currencyId", paymentCurrency.getId());
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
          "", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromTransaction"),
          Utility.getContext(this, vars, "#User_Client", "AddPaymentFromTransaction"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddPaymentFromTransaction",
          strCurrencyId);
      xmlDocument.setData("reportCurrencyId", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("financialAccountId", strFinancialAccountId);
    xmlDocument.setParameter("financialAccount", financialAccount.getIdentifier());

    final Currency financialAccountCurrency = dao
        .getFinancialAccountCurrency(strFinancialAccountId);
    if (financialAccountCurrency != null) {
      xmlDocument.setParameter("financialAccountCurrencyId", financialAccountCurrency.getId());
      try {
        OBContext.setAdminMode(true);
        xmlDocument.setParameter("financialAccountCurrencyPrecision", financialAccountCurrency
            .getStandardPrecision().toString());
      } finally {
        OBContext.restorePreviousMode();
      }
    }

    String exchangeRate = "1";
    if (financialAccountCurrency != null && !financialAccountCurrency.equals(paymentCurrency)) {
      exchangeRate = findExchangeRate(paymentCurrency, financialAccountCurrency, new Date(),
          financialAccount.getOrganization(), conversionRatePrecision);
    }

    xmlDocument.setParameter("exchangeRate", exchangeRate);

    // Payment Method combobox
    String paymentMethodComboHtml = FIN_Utility.getPaymentMethodList(defaultPaymentMethod,
        strFinancialAccountId, financialAccount.getOrganization().getId(), true, true, isReceipt);
    xmlDocument.setParameter("sectionDetailPaymentMethod", paymentMethodComboHtml);

    final List<FinAccPaymentMethod> paymentMethods = financialAccount
        .getFinancialMgmtFinAccPaymentMethodList();
    JSONObject json = new JSONObject();
    try {
      for (FinAccPaymentMethod method : paymentMethods) {
        if (isReceipt) {
          json.put(method.getPaymentMethod().getId(), method.isPayinIsMulticurrency());
        } else {
          json.put(method.getPaymentMethod().getId(), method.isPayoutIsMulticurrency());
        }
      }
    } catch (JSONException e) {
      log4j.error("JSON object error" + json.toString());
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<script language='JavaScript' type='text/javascript'>");
    sb.append("var paymentMethodMulticurrency = ");
    sb.append(json.toString());
    sb.append(";");
    sb.append("</script>");
    xmlDocument.setParameter("sectionDetailPaymentMethodMulticurrency", sb.toString());

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    // Action Regarding Document
    xmlDocument.setParameter("ActionDocument", (isReceipt ? "PRD" : "PPW"));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          (isReceipt ? "F903F726B41A49D3860243101CEEBA25" : "F15C13A199A748F1B0B00E985A64C036"),
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromTransaction"),
          Utility.getContext(this, vars, "#User_Client", "AddPaymentFromTransaction"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddPaymentFromTransaction", "");

      xmlDocument.setData("reportActionDocument", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
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
      String strFinancialAccountId, String strBusinessPartnerId, String strDueDateFrom,
      String strDueDateTo, String strTransDateFrom, String strTransDateTo, String strDocumentType,
      String strDocumentNo, String strSelectedPaymentDetails, boolean isReceipt,
      String strCurrencyId) throws IOException, ServletException {

    log4j.debug("Output: Grid with pending payments");

    dao = new AdvPaymentMngtDao();
    FIN_FinancialAccount financialAccount = dao.getObject(FIN_FinancialAccount.class,
        strFinancialAccountId);

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddPaymentFromTransactionGrid")
        .createXmlDocument();

    // Pending Payments from invoice
    final List<FIN_PaymentScheduleDetail> invoiceScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    // selected scheduled payments list
    final List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails = FIN_AddPayment
        .getSelectedPaymentDetails(invoiceScheduledPaymentDetails, strSelectedPaymentDetails);

    List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();

    // If business partner and document number are empty search for all filtered scheduled payments
    // list
    if (!"".equals(strBusinessPartnerId) || !"".equals(strDocumentNo)
        || isValidJSDate(strDueDateFrom) || isValidJSDate(strDueDateTo)
        || isValidJSDate(strTransDateFrom) || isValidJSDate(strTransDateTo)) {
      Currency paymentCurrency;
      if (strCurrencyId == null || strCurrencyId.isEmpty()) {
        paymentCurrency = financialAccount.getCurrency();
      } else {
        paymentCurrency = dao.getObject(Currency.class, strCurrencyId);
      }

      filteredScheduledPaymentDetails = dao.getFilteredScheduledPaymentDetails(
          financialAccount.getOrganization(),
          dao.getObject(BusinessPartner.class, strBusinessPartnerId), paymentCurrency,
          FIN_Utility.getDate(strDueDateFrom),
          FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strDueDateTo, "1")),
          FIN_Utility.getDate(strTransDateFrom),
          FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strTransDateTo, "1")), strDocumentType,
          strDocumentNo, null, selectedScheduledPaymentDetails, isReceipt);
    }
    final FieldProvider[] data = FIN_AddPayment.getShownScheduledPaymentDetails(vars,
        selectedScheduledPaymentDetails, filteredScheduledPaymentDetails, false, null);
    xmlDocument.setData("structure", (data == null) ? set() : data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Returns true in case the provided string is well formed JS-formated date
   */
  private boolean isValidJSDate(String strDate) {
    if ("".equals(strDate)) {
      return false;
    }
    try {
      OBContext.setAdminMode(true);
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");

      Date date = new SimpleDateFormat(dateFormat).parse(strDate);
      Date year1000 = new SimpleDateFormat("yyyy-MM-dd").parse("999-12-31");
      return date.after(year1000);
    } catch (Exception e) {
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void refreshPaymentMethod(HttpServletResponse response, String strBusinessPartnerId,
      String strFinancialAccountId, boolean isReceipt) throws IOException, ServletException {
    log4j.debug("Callout: Business Partner has changed to" + strBusinessPartnerId);

    String paymentMethodComboHtml = "";
    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);
    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBusinessPartnerId);
    String paymentMethodId = null;
    if (bp != null) {
      if (isReceipt && bp.getPaymentMethod() != null) {
        paymentMethodId = bp.getPaymentMethod().getId();
      } else if (!isReceipt && bp.getPOPaymentMethod() != null) {
        paymentMethodId = bp.getPOPaymentMethod().getId();
      }
    }
    paymentMethodComboHtml = FIN_Utility.getPaymentMethodList(paymentMethodId,
        strFinancialAccountId, account.getOrganization().getId(), true, true, isReceipt);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(paymentMethodComboHtml.replaceAll("\"", "\\'"));
    out.close();
  }

  /**
   * Returns the business partner (if is the same for all the elements in the list) or null in other
   * case.
   * 
   * @param paymentScheduleDetailList
   *          List of payment schedule details.
   * @return Business Partner if the payment schedule details belong to the same business partner.
   *         Null if the list of payment schedule details are associated to more than one business
   *         partner.
   * 
   */
  private BusinessPartner getMultiBPartner(List<FIN_PaymentScheduleDetail> paymentScheduleDetailList) {
    String previousBPId = null;
    String currentBPId = null;
    for (FIN_PaymentScheduleDetail psd : paymentScheduleDetailList) {
      if (psd.getInvoicePaymentSchedule() != null) { // Invoice
        currentBPId = psd.getInvoicePaymentSchedule().getInvoice().getBusinessPartner().getId();
        if (!currentBPId.equals(previousBPId) && previousBPId != null) {
          return null;
        } else {
          previousBPId = currentBPId;
        }
      }
      if (psd.getOrderPaymentSchedule() != null) { // Order
        currentBPId = psd.getOrderPaymentSchedule().getOrder().getBusinessPartner().getId();
        if (!currentBPId.equals(previousBPId) && previousBPId != null) {
          return null;
        } else {
          previousBPId = currentBPId;
        }
      }
    }
    return currentBPId != null ? OBDal.getInstance().get(BusinessPartner.class, currentBPId) : null;
  }

  private void refreshExchangeRate(HttpServletResponse response, String strCurrencyId,
      String strFinancialAccountCurrencyId, String strPaymentDate, Organization organization,
      int conversionRatePrecision) throws IOException, ServletException {

    dao = new AdvPaymentMngtDao();

    final Currency financialAccountCurrency = dao.getObject(Currency.class,
        strFinancialAccountCurrencyId);
    final Currency paymentCurrency = dao.getObject(Currency.class, strCurrencyId);

    String exchangeRate = findExchangeRate(paymentCurrency, financialAccountCurrency,
        FIN_Utility.getDate(strPaymentDate), organization, conversionRatePrecision);

    JSONObject msg = new JSONObject();
    try {
      msg.put("exchangeRate", exchangeRate);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private String findExchangeRate(Currency paymentCurrency, Currency financialAccountCurrency,
      Date paymentDate, Organization organization, int conversionRatePrecision) {
    String exchangeRate = "1";
    if (financialAccountCurrency != null && !financialAccountCurrency.equals(paymentCurrency)) {
      final ConversionRate conversionRate = FIN_Utility.getConversionRate(paymentCurrency,
          financialAccountCurrency, paymentDate, organization);
      if (conversionRate == null) {
        exchangeRate = "";
      } else {
        exchangeRate = conversionRate.getMultipleRateBy()
            .setScale(conversionRatePrecision, RoundingMode.HALF_UP).toPlainString();
      }
    }
    return exchangeRate;
  }

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();
    empty.put("finScheduledPaymentId", "");
    empty.put("salesOrderNr", "");
    empty.put("salesInvoiceNr", "");
    empty.put("dueDate", "");
    empty.put("invoicedAmount", "");
    empty.put("expectedAmount", "");
    empty.put("paymentAmount", "");
    ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
    result.add(empty);
    return FieldProviderFactory.getFieldProviderArray(result);
  }

  private void checkID(final String id) throws ServletException {
    if (!IsIDFilter.instance.accept(id)) {
      log4j.error("Input: " + id + " not accepted by filter: IsIDFilter");
      throw new ServletException("Input: " + id + " is not an accepted input");
    }
  }

  public String getServletInfo() {
    return "Servlet that presents the payment proposal";
    // end of getServletInfo() method
  }

}
