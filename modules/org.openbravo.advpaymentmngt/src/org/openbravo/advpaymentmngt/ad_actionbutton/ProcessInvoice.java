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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_PaymentProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonUtility;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetailV;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.service.db.CallProcess;
import org.openbravo.xmlEngine.XmlDocument;

public class ProcessInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private List<FIN_Payment> creditPayments = new ArrayList<FIN_Payment>();
  private final AdvPaymentMngtDao dao = new AdvPaymentMngtDao();

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessInvoice|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessInvoice|Tab_ID",
          IsIDFilter.instance);

      final String strC_Invoice_ID = vars.getGlobalVariable("inpcInvoiceId", strWindowId
          + "|C_Invoice_ID", "", IsIDFilter.instance);

      final String strdocaction = vars.getStringParameter("inpdocaction");
      final String strProcessing = vars.getStringParameter("inpprocessing", "Y");
      final String strOrg = vars.getRequestGlobalVariable("inpadOrgId", "ProcessInvoice|Org_ID",
          IsIDFilter.instance);
      final String strClient = vars.getStringParameter("inpadClientId", IsIDFilter.instance);

      final String strdocstatus = vars.getRequiredStringParameter("inpdocstatus");
      final String stradTableId = "318";
      final int accesslevel = 1;

      if ((org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(),
          strTabId))
          || !(Utility.isElementInList(
              Utility.getContext(this, vars, "#User_Client", strWindowId, accesslevel), strClient) && Utility
              .isElementInList(
                  Utility.getContext(this, vars, "#User_Org", strWindowId, accesslevel), strOrg))) {
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(),
            Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars);
      } else {
        printPageDocAction(response, vars, strC_Invoice_ID, strdocaction, strProcessing,
            strdocstatus, stradTableId, strWindowId);
      }
    } else if (vars.commandIn("SAVE_BUTTONDocAction111")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessInvoice|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessInvoice|Tab_ID",
          IsIDFilter.instance);
      final String strC_Invoice_ID = vars.getGlobalVariable("inpKey",
          strWindowId + "|C_Invoice_ID", "");
      final String strdocaction = vars.getStringParameter("inpdocaction");
      final String strOrg = vars.getGlobalVariable("inpadOrgId", "ProcessInvoice|Org_ID",
          IsIDFilter.instance);

      OBError myMessage = null;
      try {

        Invoice invoice = dao.getObject(Invoice.class, strC_Invoice_ID);
        invoice.setDocumentAction(strdocaction);
        OBDal.getInstance().save(invoice);
        OBDal.getInstance().flush();

        OBContext.setAdminMode(true);
        Process process = null;
        try {
          process = dao.getObject(Process.class, "111");
        } finally {
          OBContext.restorePreviousMode();
        }

        final ProcessInstance pinstance = CallProcess.getInstance().call(process, strC_Invoice_ID,
            null);

        OBDal.getInstance().getSession().refresh(invoice);
        invoice.setAPRMProcessinvoice(invoice.getDocumentAction());
        // Remove invoice's used credit description
        if ("RE".equals(strdocaction) && pinstance.getResult() != 0L) {
          final String invDesc = invoice.getDescription();
          if (invDesc != null) {
            final String creditMsg = Utility.messageBD(this, "APRM_InvoiceDescUsedCredit",
                vars.getLanguage());
            if (creditMsg != null) {
              final StringBuffer newDesc = new StringBuffer();
              for (final String line : invDesc.split("\n")) {
                if (!line.startsWith(creditMsg.substring(0, creditMsg.lastIndexOf("%s")))) {
                  newDesc.append(line);
                  if (!"".equals(line))
                    newDesc.append("\n");
                }
              }
              invoice.setDescription(newDesc.toString());
            }
          }
        }
        OBDal.getInstance().save(invoice);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
        final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this,
            pinstance.getId());
        myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
        log4j.debug(myMessage.getMessage());
        vars.setMessage(strTabId, myMessage);
        OBContext.setAdminMode();
        try {
          // on error close popup
          if (pinstance.getResult() == 0L || !"CO".equals(strdocaction)) {
            String strWindowPath = Utility.getTabURL(strTabId, "R", true);
            if (strWindowPath.equals(""))
              strWindowPath = strDefaultServlet;
            printPageClosePopUp(response, vars, strWindowPath);
          }
        } finally {
          OBContext.restorePreviousMode();
        }

        if ("CO".equals(strdocaction)) {
          // Need to refresh the invoice again from the db
          invoice = dao.getObject(Invoice.class, strC_Invoice_ID);
          OBContext.setAdminMode(false);
          String invoiceDocCategory = "";
          try {
            invoiceDocCategory = invoice.getDocumentType().getDocumentCategory();
          } finally {
            OBContext.restorePreviousMode();
          }
          /*
           * Print a grid popup in case of credit payment
           */
          // If the invoice grand total is ZERO or already has payments (due to
          // payment method automation) or the business partner does not have a default financial
          // account defined or invoice's payment method is not inside BP's financial
          // account do not cancel credit
          if (BigDecimal.ZERO.compareTo(invoice.getGrandTotalAmount()) != 0
              && isPaymentMethodConfigured(invoice) && !isInvoiceWithPayments(invoice)
              && ("API".equals(invoiceDocCategory) || "ARI".equals(invoiceDocCategory))) {
            creditPayments = dao.getCustomerPaymentsWithCredit(invoice.getOrganization(),
                invoice.getBusinessPartner(), invoice.isSalesTransaction());
            if (creditPayments != null && !creditPayments.isEmpty()) {
              printPageCreditPaymentGrid(response, vars, strC_Invoice_ID, strdocaction, strTabId,
                  strC_Invoice_ID, strdocaction, strWindowId, strTabId, invoice.getInvoiceDate(),
                  strOrg);
            }
          }

          executePayments(response, vars, strWindowId, strTabId, strC_Invoice_ID, strOrg);
        }

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myMessage.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else
          vars.setMessage(strTabId, myMessage);
      }

    } else if (vars.commandIn("GRIDLIST")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessInvoice|Window_ID",
          IsIDFilter.instance);
      final String strC_Invoice_ID = vars.getGlobalVariable("inpKey",
          strWindowId + "|C_Invoice_ID", "", IsIDFilter.instance);

      printGrid(response, vars, strC_Invoice_ID);
    } else if (vars.commandIn("USECREDITPAYMENTS") || vars.commandIn("CANCEL_USECREDITPAYMENTS")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessInvoice|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessInvoice|Tab_ID",
          IsIDFilter.instance);
      final String strC_Invoice_ID = vars.getGlobalVariable("inpKey",
          strWindowId + "|C_Invoice_ID", "");
      final String strPaymentDate = vars.getRequiredStringParameter("inpPaymentDate");
      final String strOrg = vars.getGlobalVariable("inpadOrgId", "ProcessInvoice|Org_ID",
          IsIDFilter.instance);

      final String strCreditPaymentIds;
      if (vars.commandIn("CANCEL_USECREDITPAYMENTS")) {
        strCreditPaymentIds = null;
      } else {
        strCreditPaymentIds = vars.getInParameter("inpCreditPaymentId", IsIDFilter.instance);
      }

      /*
       * Use credit logic
       */
      if (strCreditPaymentIds != null && !strCreditPaymentIds.isEmpty()) {
        List<FIN_Payment> selectedCreditPayment = FIN_Utility.getOBObjectList(FIN_Payment.class,
            strCreditPaymentIds);
        HashMap<String, BigDecimal> selectedCreditPaymentAmounts = FIN_AddPayment
            .getSelectedBaseOBObjectAmount(vars, selectedCreditPayment, "inpPaymentAmount");
        try {
          OBContext.setAdminMode(true);
          final Invoice invoice = OBDal.getInstance().get(Invoice.class, strC_Invoice_ID);

          final StringBuffer creditPaymentsIdentifiers = new StringBuffer();
          BigDecimal totalUsedCreditAmt = BigDecimal.ZERO;
          for (final FIN_Payment creditPayment : selectedCreditPayment) {
            final BigDecimal usedCreditAmt = selectedCreditPaymentAmounts
                .get(creditPayment.getId());
            // Set Used Credit = Amount + Previous used credit introduced by the user
            creditPayment.setUsedCredit(usedCreditAmt.add(creditPayment.getUsedCredit()));
            final StringBuffer description = new StringBuffer();
            if (creditPayment.getDescription() != null
                && !creditPayment.getDescription().equals(""))
              description.append(creditPayment.getDescription()).append("\n");
            description.append(String.format(
                Utility.messageBD(this, "APRM_CreditUsedinInvoice", vars.getLanguage()),
                invoice.getDocumentNo()));
            creditPayment.setDescription(description.toString());
            totalUsedCreditAmt = totalUsedCreditAmt.add(usedCreditAmt);
            creditPaymentsIdentifiers.append(creditPayment.getDocumentNo());
            creditPaymentsIdentifiers.append(", ");
          }
          creditPaymentsIdentifiers.delete(creditPaymentsIdentifiers.length() - 2,
              creditPaymentsIdentifiers.length());
          creditPaymentsIdentifiers.append("\n");

          final List<FIN_PaymentScheduleDetail> paymentScheduleDetails = new ArrayList<FIN_PaymentScheduleDetail>();
          final HashMap<String, BigDecimal> paymentScheduleDetailsAmounts = new HashMap<String, BigDecimal>();
          BigDecimal allocatedAmt = BigDecimal.ZERO;
          for (final FIN_PaymentScheduleDetail paymentScheduleDetail : dao
              .getInvoicePendingScheduledPaymentDetails(invoice)) {
            if (totalUsedCreditAmt.compareTo(allocatedAmt) > 0) {
              final BigDecimal pendingToAllocate = totalUsedCreditAmt.subtract(allocatedAmt);
              paymentScheduleDetails.add(paymentScheduleDetail);

              final BigDecimal psdAmt = paymentScheduleDetail.getAmount();
              if (psdAmt.compareTo(pendingToAllocate) <= 0) {
                paymentScheduleDetailsAmounts.put(paymentScheduleDetail.getId(), psdAmt);
                allocatedAmt = allocatedAmt.add(psdAmt);
              } else {
                paymentScheduleDetailsAmounts.put(paymentScheduleDetail.getId(), pendingToAllocate);
                allocatedAmt = allocatedAmt.add(pendingToAllocate);
              }
            }
          }

          // Create new Payment
          final boolean isSalesTransaction = invoice.isSalesTransaction();
          final DocumentType docType = FIN_Utility.getDocumentType(invoice.getOrganization(),
              isSalesTransaction ? "ARR" : "APP");
          final String strPaymentDocumentNo = FIN_Utility.getDocumentNo(docType,
              docType.getTable() != null ? docType.getTable().getDBTableName() : "");
          final FIN_FinancialAccount bpFinAccount = isSalesTransaction ? invoice
              .getBusinessPartner().getAccount() : invoice.getBusinessPartner()
              .getPOFinancialAccount();
          final FIN_Payment newPayment = FIN_AddPayment.savePayment(null, isSalesTransaction,
              docType, strPaymentDocumentNo, invoice.getBusinessPartner(),
              invoice.getPaymentMethod(), bpFinAccount, "0", FIN_Utility.getDate(strPaymentDate),
              invoice.getOrganization(), invoice.getDocumentNo(), paymentScheduleDetails,
              paymentScheduleDetailsAmounts, false, false);
          newPayment.setAmount(BigDecimal.ZERO);
          newPayment.setGeneratedCredit(BigDecimal.ZERO);
          newPayment.setUsedCredit(totalUsedCreditAmt);

          // Link new Payment with the credit payments used
          for (final FIN_Payment creditPayment : selectedCreditPayment) {
            final BigDecimal usedCreditAmt = selectedCreditPaymentAmounts
                .get(creditPayment.getId());
            FIN_PaymentProcess.linkCreditPayment(newPayment, usedCreditAmt, creditPayment);
          }

          // Process the new payment
          FIN_AddPayment.processPayment(vars, this, "P", newPayment);

          // Update Invoice's description
          final StringBuffer invDesc = new StringBuffer();
          if (invoice.getDescription() != null) {
            invDesc.append(invoice.getDescription());
            invDesc.append("\n");
          }
          invDesc.append(String.format(
              Utility.messageBD(this, "APRM_InvoiceDescUsedCredit", vars.getLanguage()),
              creditPaymentsIdentifiers.toString()));
          invoice.setDescription(invDesc.toString());

        } catch (final Exception e) {
          log4j.error("Exception while canceling the credit in the invoice: " + strC_Invoice_ID);
          e.printStackTrace();
        } finally {
          OBContext.restorePreviousMode();
        }
      }
      executePayments(response, vars, strWindowId, strTabId, strC_Invoice_ID, strOrg);
    }
  }

  private void executePayments(HttpServletResponse response, VariablesSecureApp vars,
      final String strWindowId, final String strTabId, final String strC_Invoice_ID,
      final String strOrg) throws IOException, ServletException {
    OBError myMessage = new OBError();

    List<FIN_Payment> payments = null;
    try {
      OBContext.setAdminMode(true);
      payments = dao.getPendingExecutionPayments(strC_Invoice_ID);
    } finally {
      OBContext.restorePreviousMode();
    }
    if (payments != null && payments.size() > 0) {
      vars.setSessionValue("ExecutePayments|Window_ID", strWindowId);
      vars.setSessionValue("ExecutePayments|Tab_ID", strTabId);
      vars.setSessionValue("ExecutePayments|Org_ID", strOrg);
      vars.setSessionValue("ExecutePayments|payments", FIN_Utility.getInStrList(payments));
      if (myMessage != null)
        vars.setMessage("ExecutePayments|message", myMessage);
      response.sendRedirect(strDireccion
          + "/org.openbravo.advpaymentmngt.ad_actionbutton/ExecutePayments.html");
    } else {
      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;
      printPageClosePopUp(response, vars, strWindowPath);
    }

    vars.removeSessionValue("ProcessInvoice|Window_ID");
    vars.removeSessionValue("ProcessInvoice|Tab_ID");
    vars.removeSessionValue("ProcessInvoice|Org_ID");
  }

  void printPageDocAction(HttpServletResponse response, VariablesSecureApp vars,
      String strC_Invoice_ID, String strdocaction, String strProcessing, String strdocstatus,
      String stradTableId, String strWindowId) throws IOException, ServletException {
    log4j.debug("Output: Button process 111");
    String[] discard = { "newDiscard" };
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/DocAction", discard).createXmlDocument();
    xmlDocument.setParameter("key", strC_Invoice_ID);
    xmlDocument.setParameter("processing", strProcessing);
    xmlDocument.setParameter("form", "ProcessInvoice.html");
    xmlDocument.setParameter("window", strWindowId);
    xmlDocument.setParameter("css", vars.getTheme());
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("processId", "111");
    xmlDocument.setParameter("cancel", Utility.messageBD(this, "Cancel", vars.getLanguage()));
    xmlDocument.setParameter("ok", Utility.messageBD(this, "OK", vars.getLanguage()));

    OBError myMessage = vars.getMessage("111");
    vars.removeMessage("111");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    xmlDocument.setParameter("docstatus", strdocstatus);
    xmlDocument.setParameter("adTableId", stradTableId);
    xmlDocument.setParameter("processId", "111");
    xmlDocument.setParameter("processDescription", "Process Invoice");
    xmlDocument.setParameter("docaction", (strdocaction.equals("--") ? "CL" : strdocaction));
    FieldProvider[] dataDocAction = ActionButtonUtility.docAction(this, vars, strdocaction, "135",
        strdocstatus, strProcessing, stradTableId);
    xmlDocument.setData("reportdocaction", "liststructure", dataDocAction);
    StringBuffer dact = new StringBuffer();
    if (dataDocAction != null) {
      dact.append("var arrDocAction = new Array(\n");
      for (int i = 0; i < dataDocAction.length; i++) {
        dact.append("new Array(\"" + dataDocAction[i].getField("id") + "\", \""
            + dataDocAction[i].getField("name") + "\", \""
            + dataDocAction[i].getField("description") + "\")\n");
        if (i < dataDocAction.length - 1)
          dact.append(",\n");
      }
      dact.append(");");
    } else
      dact.append("var arrDocAction = null");
    xmlDocument.setParameter("array", dact.toString());

    out.println(xmlDocument.print());
    out.close();

  }

  void printPageCreditPaymentGrid(HttpServletResponse response, VariablesSecureApp vars,
      String strC_Invoice_ID, String strdocaction, String strProcessing, String strdocstatus,
      String stradTableId, String strWindowId, String strTabId, Date invoiceDate, String strOrg)
      throws IOException, ServletException {
    log4j.debug("Output: Credit Payment Grid popup");
    String[] discard = { "" };
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreditPaymentGrid", discard).createXmlDocument();
    xmlDocument.setParameter("css", vars.getTheme());
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("window", strWindowId);
    xmlDocument.setParameter("tab", strTabId);
    xmlDocument.setParameter("adOrgId", strOrg);

    xmlDocument.setParameter("messageType", "SUCCESS");
    xmlDocument.setParameter("messageTitle",
        Utility.messageBD(this, "InvoiceComplete", vars.getLanguage()));

    xmlDocument.setParameter("invoiceGrossAmt", dao.getObject(Invoice.class, strC_Invoice_ID)
        .getGrandTotalAmount().toString());

    OBError myMessage = vars.getMessage("ProcessInvoice|CreditPaymentGrid");
    vars.removeMessage("ProcessInvoice|CreditPaymentGrid");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paymentDate",
        Utility.formatDate(invoiceDate, vars.getJavaDateFormat()));

    out.println(xmlDocument.print());
    out.close();

  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars, String invoiceId)
      throws IOException, ServletException {
    log4j.debug("Output: Grid with credit payments");

    final Invoice invoice = dao.getObject(Invoice.class, invoiceId);

    String[] discard = {};
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/AddCreditPaymentGrid", discard)
        .createXmlDocument();

    xmlDocument.setData("structure", getCreditPayments(invoice));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private FieldProvider[] getCreditPayments(Invoice invoice) {
    FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(creditPayments);
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);

    BigDecimal pendingToPay = invoice.getGrandTotalAmount();
    for (int i = 0; i < data.length; i++) {
      FieldProviderFactory.setField(data[i], "finCreditPaymentId", creditPayments.get(i).getId());
      FieldProviderFactory.setField(data[i], "documentNo", creditPayments.get(i).getDocumentNo());
      FieldProviderFactory.setField(data[i], "paymentDescription", creditPayments.get(i)
          .getDescription());
      if (creditPayments.get(i).getPaymentDate() != null) {
        FieldProviderFactory.setField(data[i], "documentDate",
            dateFormater.format(creditPayments.get(i).getPaymentDate()).toString());
      }

      final BigDecimal outStandingAmt = creditPayments.get(i).getGeneratedCredit()
          .subtract(creditPayments.get(i).getUsedCredit());
      FieldProviderFactory.setField(data[i], "outstandingAmount", outStandingAmt.toString());

      FieldProviderFactory.setField(
          data[i],
          "paymentAmount",
          pendingToPay.compareTo(outStandingAmt) > 0 ? outStandingAmt.toString() : (pendingToPay
              .compareTo(BigDecimal.ZERO) > 0 ? pendingToPay.toString() : ""));
      pendingToPay = pendingToPay.subtract(outStandingAmt);

      FieldProviderFactory.setField(data[i], "finSelectedCreditPaymentId",
          "".equals(data[i].getField("paymentAmount")) ? "" : creditPayments.get(i).getId());
      FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));
    }

    return data;
  }

  private boolean isInvoiceWithPayments(Invoice invoice) {
    for (FIN_PaymentSchedule ps : OBDao.getFilteredCriteria(FIN_PaymentSchedule.class,
        Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, invoice)).list()) {
      for (FIN_PaymentDetailV pdv : OBDao.getFilteredCriteria(FIN_PaymentDetailV.class,
          Restrictions.eq(FIN_PaymentDetailV.PROPERTY_INVOICEPAYMENTPLAN, ps)).list()) {
        if (pdv.getPayment() != null && !"RPVOID".equals(pdv.getPayment().getStatus())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if the invoice business partner has defined a default financial account and if the
   * payment method selected in the invoice belongs to the default financial account.
   * 
   * @param invoice
   *          Invoice.
   * @return True if the invoice business partner has defined a default financial account and the
   *         payment method selected in the invoice belongs to the default financial account. False
   *         in other cases.
   */
  private boolean isPaymentMethodConfigured(Invoice invoice) {
    final FIN_FinancialAccount bpFinAccount = invoice.isSalesTransaction() ? invoice
        .getBusinessPartner().getAccount() : invoice.getBusinessPartner().getPOFinancialAccount();
    if (bpFinAccount != null) {
      for (final FinAccPaymentMethod bpFinAccPaymentMethod : bpFinAccount
          .getFinancialMgmtFinAccPaymentMethodList()) {
        if (bpFinAccPaymentMethod.getPaymentMethod().equals(invoice.getPaymentMethod())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getServletInfo() {
    return "Servlet to Process Invoice";
  }
}
