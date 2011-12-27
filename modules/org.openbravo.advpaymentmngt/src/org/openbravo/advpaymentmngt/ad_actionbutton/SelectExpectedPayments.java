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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.xmlEngine.XmlDocument;

public class SelectExpectedPayments extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N", "");

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strWindowId = vars
          .getGlobalVariable("inpwindowId", "SelectExpectedPayments|Window_ID");
      String strTabId = vars.getGlobalVariable("inpTabId", "SelectExpectedPayments|Tab_ID");
      String strPaymentProposalId = vars.getGlobalVariable("inpfinPaymentProposalId", strWindowId
          + "|" + "FIN_Payment_Proposal_ID");

      printPage(response, vars, strPaymentProposalId, strWindowId, strTabId);

    } else if (vars.commandIn("GRIDLIST")) {
      String strPaymentProposalId = vars.getRequiredStringParameter("inpfinPaymentProposalId");
      String strSelectedPaymentDetails = vars.getInStringParameter("inpScheduledPaymentDetailId",
          IsIDFilter.instance);
      Boolean showAlternativePM = "Y".equals(vars.getStringParameter("inpAlternativePaymentMethod",
          filterYesNo));

      printGrid(response, vars, strPaymentProposalId, strSelectedPaymentDetails, showAlternativePM);

    } else if (vars.commandIn("SAVE")) {
      String strPaymentProposalId = vars.getRequiredStringParameter("inpfinPaymentProposalId");
      String strSelectedScheduledPaymentDetailIds = vars.getInParameter(
          "inpScheduledPaymentDetailId", IsIDFilter.instance);
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strPaymentAmount = vars.getRequiredNumericParameter("inpActualPayment");
      String strDifferenceAction = vars.getStringParameter("inpDifferenceAction", "");
      String strDifferenceAmount = vars.getRequiredNumericParameter("inpDifference");

      saveAndClose(response, vars, strPaymentProposalId, strSelectedScheduledPaymentDetailIds,
          strTabId, strPaymentAmount, strDifferenceAction, strDifferenceAmount);

    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strPaymentProposalId, String strWindowId, String strTabId) throws IOException,
      ServletException {
    log4j.debug("Output: Add Payment button pressed on Make Proposal window");

    dao = new AdvPaymentMngtDao();
    String[] discard = { "discard" };
    FIN_PaymentProposal paymentProposal = dao.getObject(FIN_PaymentProposal.class,
        strPaymentProposalId);
    if (paymentProposal.getDuedate() == null) {
      discard[0] = "dueDateInfoRow";
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/SelectExpectedPayments", discard)
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    // classInfo.name not loaded for Processes
    // xmlDocument.setParameter("title", classInfo.name);
    OBContext.setAdminMode();
    try {
      xmlDocument.setParameter("title", dao.getObject(Process.class, classInfo.id).getIdentifier());
      xmlDocument.setParameter("precision", paymentProposal.getCurrency().getStandardPrecision()
          .toString());
      xmlDocument.setParameter("currency", paymentProposal.getCurrency().getId());
    } finally {
      OBContext.restorePreviousMode();
    }

    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", paymentProposal.getOrganization().getId());
    xmlDocument.setParameter("paymentProposalId", strPaymentProposalId);
    xmlDocument.setParameter("isReceipt", (paymentProposal.isReceipt() ? "Y" : "N"));

    xmlDocument.setParameter("proposalNo", paymentProposal.getDocumentNo());
    xmlDocument.setParameter("paymentMethod", paymentProposal.getPaymentMethod().getName());
    xmlDocument.setParameter(
        "businessPartner",
        paymentProposal.getBusinessPartner() == null ? Utility.messageBD(this, "All",
            vars.getLanguage()) : paymentProposal.getBusinessPartner().getName());
    if (paymentProposal.getDuedate() != null) {
      xmlDocument.setParameter("dueDate",
          Utility.formatDate(paymentProposal.getDuedate(), vars.getJavaDateFormat()));
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars,
      String strPaymentProposalId, String strSelectedPaymentDetails, boolean showAlternativePM)
      throws IOException, ServletException {
    log4j.debug("Output: Grid with pending payments");

    dao = new AdvPaymentMngtDao();

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddPaymentProposalGrid").createXmlDocument();

    FIN_PaymentProposal paymentProposal = dao.getObject(FIN_PaymentProposal.class,
        strPaymentProposalId);
    List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    boolean firstLoad = false;
    if (strSelectedPaymentDetails == null || "".equals(strSelectedPaymentDetails))
      firstLoad = true;
    if (firstLoad)
      selectedScheduledPaymentDetails = FIN_AddPayment
          .getSelectedPendingPaymentsFromProposal(paymentProposal);
    else
      selectedScheduledPaymentDetails = FIN_AddPayment.getSelectedPaymentDetails(null,
          strSelectedPaymentDetails);

    Date dueDate = paymentProposal.getDuedate();
    if (dueDate != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(DateUtils.truncate(paymentProposal.getDuedate(), Calendar.DATE));
      cal.add(Calendar.DATE, 1);
      dueDate = cal.getTime();
    }
    // filtered scheduled payments list
    final List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails = dao
        .getFilteredScheduledPaymentDetails(paymentProposal.getOrganization(),
            paymentProposal.getBusinessPartner(), paymentProposal.getCurrency(), null, dueDate,
            "B", showAlternativePM ? null : paymentProposal.getPaymentMethod(),
            selectedScheduledPaymentDetails, paymentProposal.isReceipt());

    final FieldProvider[] data = FIN_AddPayment.getShownScheduledPaymentDetails(vars,
        selectedScheduledPaymentDetails, filteredScheduledPaymentDetails, firstLoad,
        paymentProposal);
    xmlDocument.setData("structure", (data == null) ? set() : data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void saveAndClose(HttpServletResponse response, VariablesSecureApp vars,
      String strPaymentProposalId, String strSelectedScheduledPaymentDetailIds, String strTabId,
      String strPaymentAmount, String strDifferenceAction, String strDifferenceAmount)
      throws IOException, ServletException {
    OBError message = new OBError();
    String strMessageType = "Success";
    String strMessage = "";
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {
      FIN_PaymentProposal paymentProposal = new AdvPaymentMngtDao().getObject(
          FIN_PaymentProposal.class, strPaymentProposalId);
      // Initialize payment proposal deleting existing ones.
      List<String> propDetailsToDelete = new ArrayList<String>();
      List<FIN_PaymentPropDetail> proposalDetails = paymentProposal.getFINPaymentPropDetailList();
      for (FIN_PaymentPropDetail propDetail : proposalDetails)
        propDetailsToDelete.add(propDetail.getId());
      for (String strPropDetailId : propDetailsToDelete) {
        proposalDetails.remove(dao.getObject(FIN_PaymentPropDetail.class, strPropDetailId));
        OBDal.getInstance().remove(dao.getObject(FIN_PaymentPropDetail.class, strPropDetailId));
      }
      paymentProposal.setFINPaymentPropDetailList(proposalDetails);
      paymentProposal.setAmount(BigDecimal.ZERO);
      paymentProposal.setWriteoffAmount(BigDecimal.ZERO);
      OBDal.getInstance().save(paymentProposal);

      if (!"".equals(strSelectedScheduledPaymentDetailIds)) {
        List<FIN_PaymentScheduleDetail> selectedPaymentScheduleDetails = FIN_Utility
            .getOBObjectList(FIN_PaymentScheduleDetail.class, strSelectedScheduledPaymentDetailIds);
        HashMap<String, BigDecimal> selectedPaymentScheduleDetailAmounts = FIN_AddPayment
            .getSelectedPaymentDetailsAndAmount(vars, selectedPaymentScheduleDetails);

        BigDecimal newPaymentAmount = new BigDecimal(strPaymentAmount);
        FIN_AddPayment.savePaymentProposal(paymentProposal, newPaymentAmount,
            selectedPaymentScheduleDetails, selectedPaymentScheduleDetailAmounts,
            strDifferenceAction.equals("writeoff") ? new BigDecimal(strDifferenceAmount) : null);

        boolean warningDifferentPaymentMethod = false;
        for (FIN_PaymentScheduleDetail payScheDe : selectedPaymentScheduleDetails) {
          if ((payScheDe.getInvoicePaymentSchedule() != null)
              && !paymentProposal.getPaymentMethod().getId()
                  .equals(payScheDe.getInvoicePaymentSchedule().getFinPaymentmethod().getId())) {
            warningDifferentPaymentMethod = true;
            break;
          } else if ((payScheDe.getOrderPaymentSchedule() != null)
              && !paymentProposal.getPaymentMethod().getId()
                  .equals(payScheDe.getOrderPaymentSchedule().getFinPaymentmethod().getId())) {
            warningDifferentPaymentMethod = true;
            break;
          }
        }

        strMessage = selectedPaymentScheduleDetails.size() + " " + "@RowsInserted@";
        if (warningDifferentPaymentMethod) {
          strMessage += ". @APRM_Different_PaymentMethod_Selected@";
          strMessageType = "Warning";
        }
      }

    } finally {
      OBContext.restorePreviousMode();
    }

    message.setType(strMessageType);
    message.setTitle(Utility.messageBD(this, strMessageType, vars.getLanguage()));
    message.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
    vars.setMessage(strTabId, message);
    message = null;

    printPageClosePopUpAndRefreshParent(response, vars);
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

  public String getServletInfo() {
    return "Servlet that presents the payment proposal";
    // end of getServletInfo() method
  }

}
