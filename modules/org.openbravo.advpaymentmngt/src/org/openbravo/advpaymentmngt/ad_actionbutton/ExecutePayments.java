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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.exception.NoExecutionProcessFoundException;
import org.openbravo.advpaymentmngt.process.FIN_ExecutePayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcessParameter;
import org.openbravo.xmlEngine.XmlDocument;

public class ExecutePayments extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final String PaymentInWindow = "E547CE89D4C04429B6340FFA44E70716";
  private static final String PaymentOutWindow = "6F8F913FA60F4CBD93DC1D3AA696E76E";
  private static final String PaymentProposalOutWindow = "1B7B3BB7FEAF41ED8D9727AB98779D3C";
  private static final String BatchPaymentExecutionForm = "FE9623C32FE749DD803ED7C64CCD7405";
  private static final String PurchaseInvoiceWindow = "183";
  private static final String SalesInvoiceWindow = "167";
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    dao = new AdvPaymentMngtDao();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ExecutePayments|Window_ID",
          IsIDFilter.instance);
      String strTabId = vars.getGlobalVariable("inpTabId", "ExecutePayments|Tab_ID", "",
          IsIDFilter.instance);

      PaymentExecutionProcess executionProcess = null;
      try {
        OBContext.setAdminMode(true);
        executionProcess = getExecutionProcess(vars, strWindowId);
      } finally {
        OBContext.restorePreviousMode();
      }

      if (executionProcess == null) {
        OBError message = Utility
            .translateError(this, vars, vars.getLanguage(), Utility.parseTranslation(this, vars,
                vars.getLanguage(), "@APRM_No_ExecutionProcess_Defined@"));
        vars.setMessage(strTabId, message);
        printPageClosePopUpAndRefreshParent(response, vars);
      }
      setPaymentsInSession(vars, strWindowId);

      printPage(response, vars, executionProcess, getOrganization(vars, strWindowId));
    } else if (vars.commandIn("PROCESS")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ExecutePayments|Window_ID",
          IsIDFilter.instance);
      final String payments = vars.getSessionValue("ExecutePayments|payments");
      final String strOrganizationId = vars.getRequiredStringParameter("inpOrganization",
          IsIDFilter.instance);
      final String executionProcess = vars.getRequiredStringParameter("inpExecutionProcess",
          IsIDFilter.instance);
      processAndClose(response, vars, strWindowId, executionProcess, payments,
          dao.getObject(Organization.class, strOrganizationId));
    } else if (vars.commandIn("CLOSE")) {
      printPageClosePopUp(response, vars);
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      PaymentExecutionProcess executionProcess, String strOrganizationId) throws IOException,
      ServletException {
    log4j.debug("Output: Execute Payments get parameters");
    dao = new AdvPaymentMngtDao();

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/ExecutePayments").createXmlDocument();

    FieldProvider[] data = getParameterList(executionProcess);
    if (data == null || data.length == 0)
      data = set();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    OBContext.setAdminMode();
    try {
      xmlDocument.setParameter("title", dao.getObject(Process.class, classInfo.id).getIdentifier());
      if (dao.getObject(Process.class, classInfo.id).getHelpComment() != null) {
        xmlDocument.setParameter("help", dao.getObject(Process.class, classInfo.id)
            .getHelpComment());
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    OBError myMessage = vars.getMessage("ExecutePayments|message");
    vars.removeMessage("ExecutePayments|message");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    xmlDocument.setParameter("executionProcess", executionProcess.getId());
    xmlDocument.setParameter("organization", strOrganizationId);
    xmlDocument.setData("structure", data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void processAndClose(HttpServletResponse response, VariablesSecureApp vars,
      String strWindowId, String execProcess, String strPayments, Organization organization)
      throws ServletException, IOException {
    log4j.debug("Output: Execute Payments process and close");
    dao = new AdvPaymentMngtDao();
    OBError result = new OBError();
    OBContext.setAdminMode();
    try {
      PaymentExecutionProcess executionProcess = dao.getObject(PaymentExecutionProcess.class,
          execProcess);
      List<PaymentExecutionProcessParameter> executionProcessInParameters = dao
          .getInPaymentExecutionParameters(executionProcess);
      HashMap<String, String> parameters = null;
      if (executionProcessInParameters != null && executionProcessInParameters.size() > 0) {
        parameters = new HashMap<String, String>();
        for (PaymentExecutionProcessParameter parameter : executionProcessInParameters) {
          String strValue = vars
              .getStringParameter(("TEXT".equals(parameter.getInputType()) ? "text" : "check")
                  + parameter.getId());
          parameters.put(parameter.getSearchKey(), strValue);
        }
      }

      List<FIN_Payment> payments = FIN_Utility.getOBObjectList(FIN_Payment.class, strPayments);
      FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
      executePayment.init(getSource(strWindowId), executionProcess, payments, parameters,
          organization);
      result = executePayment.execute();
      String paymentsDocNo = "";
      if ("Success".equals(result.getType())) {
        int i = 0;
        Iterator<FIN_Payment> iterator = payments.iterator();
        while (iterator.hasNext()) {
          FIN_Payment payment = iterator.next();
          if (i == 0) {
            paymentsDocNo += payment.getDocumentNo();
            i++;
          } else {
            paymentsDocNo += ", " + payment.getDocumentNo();
          }
        }
        result.setMessage(String.format(
            Utility.messageBD(this, "APRM_Payments_Created", vars.getLanguage()), paymentsDocNo));
      }
    } catch (NoExecutionProcessFoundException e) {
      result.setType("Error");
      result.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
          "@NoExecutionProcessFound@"));
    } finally {
      OBContext.restorePreviousMode();
    }
    if (PaymentInWindow.equals(strWindowId) || PaymentOutWindow.equals(strWindowId)
        || PaymentProposalOutWindow.equals(strWindowId)
        || PurchaseInvoiceWindow.equals(strWindowId) || SalesInvoiceWindow.equals(strWindowId)) {
      final String strTabId = vars.getGlobalVariable("inpTabId", "ExecutePayments|Tab_ID");
      vars.setMessage(strTabId, result);
      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      printPageClosePopUp(response, vars, strWindowPath);
    } else if (BatchPaymentExecutionForm.equals(strWindowId)) {
      final String strTabId = vars.getGlobalVariable("inpTabId", "ExecutePayments|Tab_ID");
      vars.setMessage(strTabId, result);
      printPageClosePopUpAndRefreshParent(response, vars);
    }
    vars.removeSessionValue("ExecutePayments|Window_ID");
    vars.removeSessionValue("ExecutePayments|Tab_ID");
    vars.removeSessionValue("ExecutePayments|payments");
    if (PurchaseInvoiceWindow.equals(strWindowId) || SalesInvoiceWindow.equals(strWindowId))
      vars.removeSessionValue("ExecutePayments|Org_ID");

  }

  private PaymentExecutionProcess getExecutionProcess(VariablesSecureApp vars, String strWindowId)
      throws ServletException {
    dao = new AdvPaymentMngtDao();
    if (PaymentInWindow.equals(strWindowId) || PaymentOutWindow.equals(strWindowId)) {
      final String strPaymentId = vars.getRequiredStringParameter("inpfinPaymentId",
          IsIDFilter.instance);
      final FIN_Payment payment = dao.getObject(FIN_Payment.class, strPaymentId);
      return dao.getExecutionProcess(payment);
    } else if (PaymentProposalOutWindow.equals(strWindowId)) {
      final String strPaymentProposalId = vars.getRequiredStringParameter(
          "inpfinPaymentProposalId", IsIDFilter.instance);
      final FIN_PaymentProposal paymentProposal = dao.getObject(FIN_PaymentProposal.class,
          strPaymentProposalId);
      return dao.getExecutionProcess(paymentProposal.getAccount(),
          paymentProposal.getPaymentMethod(), paymentProposal.isReceipt());
    } else if (BatchPaymentExecutionForm.equals(strWindowId)) {
      String strSelectedPaymentsIds = vars.getRequestGlobalVariable("inpSelectedRowList", "");
      String[] paymentList = strSelectedPaymentsIds.split(",");
      final FIN_Payment payment = dao.getObject(FIN_Payment.class, paymentList[0].trim());
      return dao.getExecutionProcess(payment);
    } else if (PurchaseInvoiceWindow.equals(strWindowId) || SalesInvoiceWindow.equals(strWindowId)) {
      String strSelectedPaymentsIds = vars.getSessionValue("ExecutePayments|payments");
      strSelectedPaymentsIds = strSelectedPaymentsIds.replace("'", "");
      String[] paymentList = strSelectedPaymentsIds.split(",");
      final FIN_Payment payment = dao.getObject(FIN_Payment.class, paymentList[0].trim());
      return dao.getExecutionProcess(payment);
    }
    return null;
  }

  private String getOrganization(VariablesSecureApp vars, String strWindowId)
      throws ServletException {
    if (PaymentInWindow.equals(strWindowId) || PaymentOutWindow.equals(strWindowId)) {
      final String strPaymentId = vars.getRequiredStringParameter("inpfinPaymentId",
          IsIDFilter.instance);
      final FIN_Payment payment = dao.getObject(FIN_Payment.class, strPaymentId);
      return payment.getOrganization().getId();
    } else if (PaymentProposalOutWindow.equals(strWindowId)) {
      final String strPaymentProposalId = vars.getRequiredStringParameter(
          "inpfinPaymentProposalId", IsIDFilter.instance);
      final FIN_PaymentProposal paymentProposal = dao.getObject(FIN_PaymentProposal.class,
          strPaymentProposalId);
      return paymentProposal.getOrganization().getId();
    } else if (BatchPaymentExecutionForm.equals(strWindowId)) {
      return vars.getRequiredStringParameter("inpOrgId", IsIDFilter.instance);
    } else if (PurchaseInvoiceWindow.equals(strWindowId) || SalesInvoiceWindow.equals(strWindowId)) {
      return vars.getSessionValue("ExecutePayments|Org_ID");
    }
    return null;
  }

  private FieldProvider[] getParameterList(PaymentExecutionProcess executionProcess) {
    dao = new AdvPaymentMngtDao();
    OBContext.setAdminMode();
    FieldProvider[] data = null;
    try {
      List<PaymentExecutionProcessParameter> parameters = dao
          .getInPaymentExecutionParameters(executionProcess);
      if (parameters != null && parameters.size() > 0) {
        PaymentExecutionProcessParameter[] parametersArray = new PaymentExecutionProcessParameter[0];
        parametersArray = parameters.toArray(parametersArray);
        data = FieldProviderFactory.getFieldProviderArray(parameters);
        for (int i = 0; i < data.length; i++) {
          FieldProviderFactory.setField(data[i], "parameterid", parametersArray[i].getId());
          FieldProviderFactory.setField(data[i], "name", parametersArray[i].getName());
          FieldProviderFactory.setField(data[i], "showtext",
              "TEXT".equals(parametersArray[i].getInputType()) ? "block" : "none");
          FieldProviderFactory.setField(data[i], "defaulttext",
              parametersArray[i].getDefaultTextValue());
          FieldProviderFactory.setField(data[i], "showcheck",
              "CHECK".equals(parametersArray[i].getInputType()) ? "block" : "none");
          FieldProviderFactory.setField(data[i], "defaultcheck",
              parametersArray[i].getDefaultValueForFlag());
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  private String getSource(String strWindowId) {
    if (PaymentInWindow.equals(strWindowId) || PaymentOutWindow.equals(strWindowId))
      return "PW"; // Payments Window
    else if (PaymentProposalOutWindow.equals(strWindowId))
      return "PPW"; // Payment Proposal Window
    else if (BatchPaymentExecutionForm.equals(strWindowId))
      return "MF"; // Execute Payments Manual Form
    else if (PurchaseInvoiceWindow.equals(strWindowId) || SalesInvoiceWindow.equals(strWindowId))
      return "AIP"; // Invoice Process

    return "OTHER";
  }

  private void setPaymentsInSession(VariablesSecureApp vars, String strWindowId)
      throws ServletException {
    dao = new AdvPaymentMngtDao();
    String payments = "";
    if (PaymentInWindow.equals(strWindowId) || PaymentOutWindow.equals(strWindowId))
      payments = vars.getRequiredStringParameter("inpfinPaymentId", IsIDFilter.instance);
    else if (PaymentProposalOutWindow.equals(strWindowId)) {
      final String strPaymentProposalId = vars.getRequiredStringParameter(
          "inpfinPaymentProposalId", IsIDFilter.instance);
      final FIN_PaymentProposal paymentProposal = dao.getObject(FIN_PaymentProposal.class,
          strPaymentProposalId);
      payments = FIN_Utility.getInStrList(dao.getPaymentProposalPayments(paymentProposal));

    } else if (BatchPaymentExecutionForm.equals(strWindowId)) {
      payments = vars.getRequestGlobalVariable("inpSelectedRowList", "");
    }

    if (!PurchaseInvoiceWindow.equals(strWindowId) && !SalesInvoiceWindow.equals(strWindowId))
      vars.setSessionValue("ExecutePayments|payments", payments);
  }

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();
    empty.put("name", "");
    empty.put("showtext", "none");
    empty.put("parameterid", "");
    empty.put("defaulttext", "");
    empty.put("showcheck", "none");
    empty.put("parameterid", "");
    empty.put("defaultcheck", "");
    ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
    result.add(empty);
    return FieldProviderFactory.getFieldProviderArray(result);
  }

  public String getServletInfo() {
    return "Servlet to Execute Payments";
  }
}