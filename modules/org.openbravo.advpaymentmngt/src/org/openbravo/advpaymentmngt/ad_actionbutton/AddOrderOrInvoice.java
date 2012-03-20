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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
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
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;
import org.openbravo.xmlEngine.XmlDocument;

public class AddOrderOrInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N", "");

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    dao = new AdvPaymentMngtDao();

    if (vars.commandIn("DEFAULT")) {
      String strWindowId = vars.getGlobalVariable("inpwindowId", "AddOrderOrInvoice|Window_ID");
      String strTabId = vars.getGlobalVariable("inpTabId", "AddOrderOrInvoice|Tab_ID");
      String strPaymentId = vars.getGlobalVariable("inpfinPaymentId", strWindowId + "|"
          + "FIN_Payment_ID");

      printPage(response, vars, strPaymentId, strWindowId, strTabId);

    } else if (vars.commandIn("GRIDLIST")) {
      String strBusinessPartnerId = vars.getRequestGlobalVariable("inpBusinessPartnerId", "");
      if ("".equals(strBusinessPartnerId)) {
        strBusinessPartnerId = vars.getRequestGlobalVariable("inpcBPartnerId", "");
      }
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strPaymentId = vars.getRequestGlobalVariable("inpfinPaymentId", "");
      String strDueDateFrom = vars.getStringParameter("inpDueDateFrom", "");
      String strDueDateTo = vars.getStringParameter("inpDueDateTo", "");
      String strDocumentType = vars.getStringParameter("inpDocumentType", "");
      String strSelectedPaymentDetails = vars.getInStringParameter("inpScheduledPaymentDetailId",
          "", null);
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      Boolean showAlternativePM = "Y".equals(vars.getStringParameter("inpAlternativePaymentMethod",
          filterYesNo));

      printGrid(response, vars, strBusinessPartnerId, strPaymentId, strOrgId, strDueDateFrom,
          strDueDateTo, strDocumentType, strSelectedPaymentDetails, isReceipt, showAlternativePM);
    } else if (vars.commandIn("SAVE") || vars.commandIn("SAVEANDPROCESS")) {
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      String strAction = null;
      if (vars.commandIn("SAVEANDPROCESS")) {
        // The default option is process
        strAction = (isReceipt ? "PRP" : "PPP");
      } else {
        strAction = vars.getRequiredStringParameter("inpActionDocument");
      }
      String strPaymentId = vars.getRequiredStringParameter("inpfinPaymentId");
      String strSelectedScheduledPaymentDetailIds = vars.getInStringParameter(
          "inpScheduledPaymentDetailId", "", null);
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
      String strDifferenceAction = "";
      BigDecimal refundAmount = BigDecimal.ZERO;
      String strDifference = vars.getNumericParameter("inpDifference", "0");
      if (!"0".equals(strDifference)) {
        refundAmount = new BigDecimal(vars.getRequiredNumericParameter("inpDifference"));
        strDifferenceAction = vars.getStringParameter("inpDifferenceAction", "");
      }
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strPaymentAmount = vars.getRequiredNumericParameter("inpActualPayment");
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
        HashMap<String, BigDecimal> selectedPaymentDetailAmounts = getSelectedPaymentDetailsAndAmount(
            vars, strSelectedScheduledPaymentDetailIds);

        FIN_Payment payment = dao.getObject(FIN_Payment.class, strPaymentId);
        BigDecimal newPaymentAmount = new BigDecimal(strPaymentAmount);
        if (newPaymentAmount.compareTo(payment.getAmount()) != 0) {
          payment.setAmount(newPaymentAmount);
        }

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
        FIN_AddPayment.setFinancialTransactionAmountAndRate(payment, exchangeRate, convertedAmount);
        payment = FIN_AddPayment.savePayment(payment, isReceipt, null, null, null, null, null,
            null, null, null, null, selectedPaymentDetails, selectedPaymentDetailAmounts,
            strDifferenceAction.equals("writeoff"), strDifferenceAction.equals("refund"),
            dao.getObject(Currency.class, paymentCurrencyId), exchangeRate, convertedAmount);

        if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
            || strAction.equals("PPW")) {
          try {
            // If Action PRP o PPW, Process payment but as well create a financial transaction
            message = FIN_AddPayment.processPayment(vars, this,
                (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", payment);
            if (strDifferenceAction.equals("refund")) {
              Boolean newPayment = !payment.getFINPaymentDetailList().isEmpty();
              FIN_Payment refundPayment = FIN_AddPayment.createRefundPayment(this, vars, payment,
                  refundAmount.negate(), exchangeRate);
              OBError auxMessage = FIN_AddPayment.processPayment(vars, this,
                  (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", refundPayment);
              if (newPayment) {
                final String strNewRefundPaymentMessage = Utility.parseTranslation(this, vars,
                    vars.getLanguage(),
                    "@APRM_RefundPayment@" + ": " + refundPayment.getDocumentNo())
                    + ".";
                message.setMessage(strNewRefundPaymentMessage + " " + message.getMessage());
                if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) != 0) {
                  payment.setDescription(payment.getDescription() + strNewRefundPaymentMessage
                      + "\n");
                  OBDal.getInstance().save(payment);
                  OBDal.getInstance().flush();
                }
              } else {
                message = auxMessage;
              }
            }
          } catch (Exception ex) {
            message = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            log4j.error(ex);
            if (!message.isConnectionAvailable()) {
              bdErrorConnection(response);
              return;
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

      vars.setMessage(strTabId, message);
      printPageClosePopUpAndRefreshParent(response, vars);
    }

  }

  private void checkID(final String id) throws ServletException {
    if (!IsIDFilter.instance.accept(id)) {
      log4j.error("Input: " + id + " not accepted by filter: IsIDFilter");
      throw new ServletException("Input: " + id + " is not an accepted input");
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strPaymentId, String strWindowId, String strTabId) throws IOException,
      ServletException {
    log4j.debug("Output: Add Payment button pressed on Make / Receipt Payment windows");

    FIN_Payment payment = new AdvPaymentMngtDao().getObject(FIN_Payment.class, strPaymentId);
    String[] discard = { "discard" };
    if (payment.getBusinessPartner() != null) {
      discard[0] = "bpGridColumn";
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddOrderOrInvoice", discard)
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    if (payment.isReceipt())
      xmlDocument.setParameter("title",
          Utility.messageBD(this, "APRM_AddPaymentIn", vars.getLanguage()));
    else
      xmlDocument.setParameter("title",
          Utility.messageBD(this, "APRM_AddPaymentOut", vars.getLanguage()));
    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    if (payment.getBusinessPartner() != null) {
      xmlDocument.setParameter("businessPartner", payment.getBusinessPartner().getIdentifier());
      xmlDocument.setParameter("businessPartnerId", payment.getBusinessPartner().getId());
      xmlDocument.setParameter(
          "credit",
          dao.getCustomerCredit(payment.getBusinessPartner(), payment.isReceipt(),
              payment.getOrganization()).toString());
      xmlDocument.setParameter("customerBalance",
          payment.getBusinessPartner().getCreditUsed() != null ? payment.getBusinessPartner()
              .getCreditUsed().toString() : BigDecimal.ZERO.toString());
    } else {
      xmlDocument.setParameter("businessPartner", "");
      xmlDocument.setParameter("businessPartnerId", "");
      xmlDocument.setParameter("credit", "");
      xmlDocument.setParameter("customerBalance", "");

    }
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", payment.getOrganization().getId());
    xmlDocument.setParameter("paymentId", strPaymentId);
    xmlDocument.setParameter("actualPayment", payment.getAmount().toString());
    xmlDocument.setParameter("headerAmount", payment.getAmount().toString());
    xmlDocument.setParameter("isReceipt", (payment.isReceipt() ? "Y" : "N"));
    xmlDocument.setParameter("isSoTrx", (payment.isReceipt()) ? "Y" : "N");
    if (payment.getBusinessPartner() == null
        && (payment.getGeneratedCredit() == null || BigDecimal.ZERO.compareTo(payment
            .getGeneratedCredit()) != 0)) {
      payment.setGeneratedCredit(BigDecimal.ZERO);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().flush();
    }
    xmlDocument.setParameter("generatedCredit", payment.getGeneratedCredit() != null ? payment
        .getGeneratedCredit().toString() : BigDecimal.ZERO.toString());

    final Currency financialAccountCurrency = payment.getAccount().getCurrency();
    if (financialAccountCurrency != null) {
      xmlDocument.setParameter("financialAccountCurrencyId", financialAccountCurrency.getId());
      xmlDocument.setParameter("financialAccountCurrencyName",
          financialAccountCurrency.getISOCode());
      xmlDocument.setParameter("financialAccountCurrencyPrecision", financialAccountCurrency
          .getStandardPrecision().toString());
    }
    xmlDocument.setParameter("exchangeRate",
        payment.getFinancialTransactionConvertRate() == null ? "" : payment
            .getFinancialTransactionConvertRate().toPlainString());
    xmlDocument.setParameter("actualConverted",
        payment.getFinancialTransactionAmount() == null ? "" : payment
            .getFinancialTransactionAmount().toString());
    xmlDocument.setParameter("expectedConverted",
        payment.getFinancialTransactionAmount() == null ? "" : payment
            .getFinancialTransactionAmount().toPlainString());
    xmlDocument.setParameter("currencyId", payment.getCurrency().getId());
    xmlDocument.setParameter("currencyName", payment.getCurrency().getISOCode());

    boolean forcedFinancialAccountTransaction = false;
    forcedFinancialAccountTransaction = FIN_AddPayment.isForcedFinancialAccountTransaction(payment);
    // Action Regarding Document
    xmlDocument.setParameter("ActionDocument", (payment.isReceipt() ? "PRP" : "PPP"));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          (payment.isReceipt() ? "F903F726B41A49D3860243101CEEBA25"
              : "F15C13A199A748F1B0B00E985A64C036"),
          forcedFinancialAccountTransaction ? "29010995FD39439D97A5C0CE8CE27D70" : "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromInvoice"),
          Utility.getContext(this, vars, "#User_Client", "AddPaymentFromInvoice"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddOrderOrInvoice", "");
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
      String strBusinessPartnerId, String strPaymentId, String strOrgId, String strDueDateFrom,
      String strDueDateTo, String strDocumentType, String strSelectedPaymentDetails,
      boolean isReceipt, boolean showAlternativePM) throws IOException, ServletException {

    log4j.debug("Output: Grid with pending payments");
    dao = new AdvPaymentMngtDao();
    String[] discard = { "" };
    if (!"".equals(vars.getRequestGlobalVariable("inpBusinessPartnerId", ""))) {
      discard[0] = "businessPartnerName";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddPaymentGrid", discard).createXmlDocument();

    // Pending Payments from invoice
    final List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails = FIN_AddPayment
        .getSelectedPaymentDetails(null, strSelectedPaymentDetails);

    FIN_Payment payment = dao.getObject(FIN_Payment.class, strPaymentId);

    // filtered scheduled payments list
    final List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails = dao
        .getFilteredScheduledPaymentDetails(dao.getObject(Organization.class, strOrgId),
            dao.getObject(BusinessPartner.class, strBusinessPartnerId), payment.getCurrency(),
            FIN_Utility.getDate(strDueDateFrom),
            FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strDueDateTo, "1")), strDocumentType,
            showAlternativePM ? null : payment.getPaymentMethod(), selectedScheduledPaymentDetails,
            isReceipt);

    FieldProvider[] data = FIN_AddPayment.getShownScheduledPaymentDetails(vars,
        selectedScheduledPaymentDetails, filteredScheduledPaymentDetails, false, null);
    data = groupPerDocumentType(data, strDocumentType);
    xmlDocument.setData("structure", (data == null) ? set() : data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
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

  private FieldProvider[] groupPerDocumentType(FieldProvider[] data, String strDocumenType) {
    ArrayList<FieldProvider> gridLines = new ArrayList<FieldProvider>();
    HashMap<String, Integer> amountsPerGroupingField = new HashMap<String, Integer>();
    String groupingField = "finScheduledPaymentDetailId";
    if ("I".equals(strDocumenType)) {
      groupingField = "invoicePaymentScheduleId";
    } else if ("O".equals(strDocumenType)) {
      groupingField = "orderPaymentScheduleId";
    }
    for (int i = 0; i < data.length; i++) {
      if (!amountsPerGroupingField.containsKey(data[i].getField(groupingField))
          || "".equals(data[i].getField(groupingField))) {
        amountsPerGroupingField.put(data[i].getField(groupingField), gridLines.size());
        FieldProviderFactory.setField(data[i], "rownum", String.valueOf(gridLines.size()));
        gridLines.add(data[i]);
      } else {
        Integer listIndex = amountsPerGroupingField.get(data[i].getField(groupingField));
        FieldProvider row = gridLines.get(listIndex);
        FieldProviderFactory.setField(
            row,
            "finScheduledPaymentDetailId",
            row.getField("finScheduledPaymentDetailId") + ","
                + data[i].getField("finScheduledPaymentDetailId"));
        FieldProviderFactory.setField(
            row,
            "finSelectedPaymentDetailId",
            row.getField("finSelectedPaymentDetailId") + ","
                + data[i].getField("finScheduledPaymentDetailId"));
        FieldProviderFactory.setField(
            row,
            "outstandingAmount",
            new BigDecimal(row.getField("outstandingAmount")).add(
                new BigDecimal(data[i].getField("outstandingAmount"))).toString());
        BigDecimal payAmount = BigDecimal.ZERO;
        if (!"".equals(row.getField("paymentAmount"))) {
          payAmount = new BigDecimal(row.getField("paymentAmount"));
        }
        FieldProviderFactory.setField(
            row,
            "paymentAmount",
            !"".equals(data[i].getField("paymentAmount")) ? payAmount.add(
                new BigDecimal(data[i].getField("paymentAmount"))).toString() : (payAmount
                .compareTo(BigDecimal.ZERO) == 0 ? "" : payAmount.toString()));
        if ("O".equals(strDocumenType)) {
          String strGroupedInvoicesNr = row.getField("invoiceNr");
          FieldProviderFactory.setField(row, "invoiceNr", (strGroupedInvoicesNr.isEmpty() ? ""
              : strGroupedInvoicesNr + ", ") + data[i].getField("invoiceNr"));
          String invoiceNumber = row.getField("invoiceNr");
          String invoiceNumberTrunc = (invoiceNumber.length() > 17) ? invoiceNumber
              .substring(0, 14).concat("...").toString() : invoiceNumber;
          FieldProviderFactory.setField(row, "invoiceNrTrunc", invoiceNumberTrunc);
        } else if ("I".equals(strDocumenType)) {
          String strGroupedOrdersNr = row.getField("orderNr");
          FieldProviderFactory.setField(row, "orderNr", (strGroupedOrdersNr.isEmpty() ? ""
              : strGroupedOrdersNr + ", ") + data[i].getField("orderNr"));
          String orderNumber = row.getField("orderNr");
          String orderNumberTrunc = (orderNumber.length() > 17) ? orderNumber.substring(0, 14)
              .concat("...").toString() : orderNumber;
          FieldProviderFactory.setField(row, "orderNrTrunc", orderNumberTrunc);
        }
      }
    }
    FieldProvider[] result = new FieldProvider[gridLines.size()];
    gridLines.toArray(result);
    return result;
  }

  /**
   * Creates a HashMap with the FIN_PaymentScheduleDetail id's and the amount gotten from the
   * Session.
   * 
   * The amounts are stored in Session like "inpPaymentAmount"+paymentScheduleDetail.Id
   * 
   * @param vars
   *          VariablseSecureApp with the session data.
   * @param selectedPaymentScheduleDetails
   *          List of FIN_PaymentScheduleDetails that need to be included in the HashMap.
   * @return A HashMap mapping the FIN_PaymentScheduleDetail's Id with the corresponding amount.
   */
  private HashMap<String, BigDecimal> getSelectedPaymentDetailsAndAmount(VariablesSecureApp vars,
      String _strSelectedScheduledPaymentDetailIds) throws ServletException {
    String strSelectedScheduledPaymentDetailIds = _strSelectedScheduledPaymentDetailIds;
    // Remove "(" ")"
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace("(", "");
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace(")", "");
    HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts = new HashMap<String, BigDecimal>();
    // As selected items may contain records with multiple IDs we as well need the records list as
    // amounts are related to records
    StringTokenizer records = new StringTokenizer(strSelectedScheduledPaymentDetailIds, "'");
    Set<String> recordSet = new LinkedHashSet<String>();
    while (records.hasMoreTokens()) {
      recordSet.add(records.nextToken());
    }
    for (String record : recordSet) {
      if (", ".equals(record)) {
        continue;
      }
      Set<String> psdSet = new LinkedHashSet<String>();
      StringTokenizer psds = new StringTokenizer(record, ",");
      while (psds.hasMoreTokens()) {
        psdSet.add(psds.nextToken());
      }
      BigDecimal recordAmount = new BigDecimal(vars.getNumericParameter(
          "inpPaymentAmount" + record, ""));
      HashMap<String, BigDecimal> recordsAmounts = calculateAmounts(recordAmount, psdSet);
      selectedPaymentScheduleDetailsAmounts.putAll(recordsAmounts);
    }
    return selectedPaymentScheduleDetailsAmounts;
  }

  /**
   * This method returns a HashMap with pairs of UUID of payment schedule details and amounts
   * related to those ones.
   * 
   * @param recordAmount
   *          : amount to split among the set
   * @param psdSet
   *          : set of payment schedule details where to allocate the amount
   * @return
   */
  private HashMap<String, BigDecimal> calculateAmounts(BigDecimal recordAmount, Set<String> psdSet) {
    BigDecimal remainingAmount = recordAmount;
    HashMap<String, BigDecimal> recordsAmounts = new HashMap<String, BigDecimal>();
    // PSD needs to be properly ordered to ensure negative amounts are processed first
    List<FIN_PaymentScheduleDetail> psds = getOrderedPaymentScheduleDetails(psdSet);
    for (FIN_PaymentScheduleDetail paymentScheduleDetail : psds) {
      BigDecimal outstandingAmount = paymentScheduleDetail.getAmount();
      // Manage negative amounts
      if ((remainingAmount.compareTo(BigDecimal.ZERO) > 0 && remainingAmount
          .compareTo(outstandingAmount) >= 0)
          || ((remainingAmount.compareTo(BigDecimal.ZERO) == -1 && outstandingAmount
              .compareTo(BigDecimal.ZERO) == -1) && (remainingAmount.compareTo(outstandingAmount) <= 0))) {
        recordsAmounts.put(paymentScheduleDetail.getId(), outstandingAmount);
        remainingAmount = remainingAmount.subtract(outstandingAmount);
      } else {
        recordsAmounts.put(paymentScheduleDetail.getId(), remainingAmount);
        remainingAmount = BigDecimal.ZERO;
      }

    }
    return recordsAmounts;
  }

  private List<FIN_PaymentScheduleDetail> getOrderedPaymentScheduleDetails(Set<String> psdSet) {
    OBCriteria<FIN_PaymentScheduleDetail> orderedPSDs = OBDal.getInstance().createCriteria(
        FIN_PaymentScheduleDetail.class);
    orderedPSDs.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_ID, psdSet));
    orderedPSDs.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);
    return orderedPSDs.list();
  }

  public String getServletInfo() {
    return "Servlet that presents the payment proposal";
    // end of getServletInfo() method
  }

}
