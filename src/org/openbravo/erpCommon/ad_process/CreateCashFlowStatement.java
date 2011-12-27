/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2001-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class CreateCashFlowStatement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String process = CreateCashFlowStatementData.processId(this, "GenerateCashFlowStatement");
    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars, process);
    } else if (vars.commandIn("FIND")) {
      OBError myError = process(response, vars);
      vars.setMessage("CreateCashFlowStatement", myError);
      printPage(response, vars, process);
    } else
      pageErrorPopUp(response);
  }

  private OBError process(HttpServletResponse response, VariablesSecureApp vars) {

    Connection conn = null;
    OBError myError = null;
    int i = 0;

    try {
      String strClient = vars.getClient();
      CreateCashFlowStatementData[] data = CreateCashFlowStatementData.select(this, strClient);
      conn = getTransactionConnection();
      for (i = 0; i < data.length; i++) {
        insertCFS(conn, response, vars, data[i].recordId2, data[i].factAcctId, data[i].amount,
            data[i].accountId, 0);
      }

      releaseCommitConnection(conn);
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myError
          .setMessage(Utility.messageBD(this, "RecordsProcessed", vars.getLanguage()) + ": " + i);
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
    }
    return myError;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strProcessId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process CreateCashFlowStatement");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/CreateCashFlowStatement").createXmlDocument();

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CreateCashFlowStatement", false, "",
        "", "", false, "ad_process", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    // New interface paramenters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_process.CreateCashFlowStatement");

      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "CreateCashFlowStatement.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CreateCashFlowStatement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CreateCashFlowStatement");
      vars.removeMessage("CreateCashFlowStatement");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void insertCFS(Connection conn, HttpServletResponse response, VariablesSecureApp vars,
      String strPaymentId, String strFactAcctId, String strAmount, String strAccount, int level)
      throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: CreateCashFlowStatement");
    if (log4j.isDebugEnabled())
      log4j.debug("strPaymentId - " + strPaymentId + " - strFactAcctId - " + strFactAcctId
          + " - strAmount - " + strAmount);
    CreateCashFlowStatementData[] data = CreateCashFlowStatementData.selectPaymentInfo(this,
        strPaymentId);

    if (log4j.isDebugEnabled() && ((data != null && data.length != 0)))
      log4j.debug("cInvoiceId - " + data[0].cInvoiceId + " - cOrderId - " + data[0].cOrderId
          + " - cSettlementGenerateId - " + data[0].cSettlementGenerateId + " - ismanual - "
          + data[0].ismanual);
    if (data == null || data.length == 0) {
      if (log4j.isDebugEnabled())
        log4j.debug("CreateCashFlowStatement - NO PAYMENT");
      String strFactAcctCFS = SequenceIdData.getUUID();
      if (log4j.isDebugEnabled())
        log4j.debug("CreateCashFlowStatement - " + " - strNewAmount - " + strAmount);
      CreateCashFlowStatementData.insertStatements(conn, this, strFactAcctCFS, strFactAcctId,
          vars.getClient(), vars.getOrg(), vars.getUser(), strAccount, strAmount, null);
    } else {
      CreateCashFlowStatementData[] writeOff = CreateCashFlowStatementData.selectPaymentWriteOff(
          this, strPaymentId, data[0].cSettlementCancelId);
      if (writeOff != null && writeOff.length > 0) {
        String strFactAcctCFS = SequenceIdData.getUUID();
        CreateCashFlowStatementData.insertStatements(conn, this, strFactAcctCFS, strFactAcctId,
            vars.getClient(), vars.getOrg(), vars.getUser(), writeOff[0].accountId,
            writeOff[0].amount, writeOff[0].id);
      }
      if (!(data[0].cInvoiceId).equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("CreateCashFlowStatement - INVOICE - " + data[0].cInvoiceId);
        CreateCashFlowStatementData[] statements = CreateCashFlowStatementData.selectStatements(
            this, "318", data[0].cInvoiceId);
        String strTotal = CreateCashFlowStatementData.selectSumStatements(this, "318",
            data[0].cInvoiceId);
        if (log4j.isDebugEnabled())
          log4j.debug("strTotal - " + strTotal + " - strAmount - " + strAmount);
        if (strTotal == null || strTotal.equals("")) {
          if (log4j.isDebugEnabled())
            log4j.debug("CreateCashFlowStatement - NOT POSTED INVOICE");
          String strOrderAccount = CreateCashFlowStatementData.selectOrderAccount(this);
          String strFactAcctCFS = SequenceIdData.getUUID();
          if (log4j.isDebugEnabled())
            log4j.debug("CreateCashFlowStatement - " + " - strNewAmount - " + strAmount);
          CreateCashFlowStatementData.insertStatements(conn, this, strFactAcctCFS, strFactAcctId,
              vars.getClient(), vars.getOrg(), vars.getUser(), strOrderAccount, strAmount, null);
        } else {
          if (log4j.isDebugEnabled())
            log4j.debug("CreateCashFlowStatement - POSTED INVOICE");
          String strRatio = calculateRatio(strTotal, strAmount);
          if (log4j.isDebugEnabled())
            log4j.debug("strRatio - " + strRatio);
          for (int i = 0; i < statements.length; i++) {
            String strFactAcctCFS = SequenceIdData.getUUID();
            if (log4j.isDebugEnabled())
              log4j.debug("CreateCashFlowStatement - " + " - strNewAmount - "
                  + multiply(statements[i].amount, strRatio));
            CreateCashFlowStatementData.insertStatements(conn, this, strFactAcctCFS, strFactAcctId,
                vars.getClient(), vars.getOrg(), vars.getUser(), statements[i].accountId,
                multiply(statements[i].amount, strRatio), statements[i].id);
          }
        }
      } else if (!(data[0].cOrderId).equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("CreateCashFlowStatement - ORDER");
        String strOrderAccount = CreateCashFlowStatementData.selectOrderAccount(this);
        String strFactAcctCFS = SequenceIdData.getUUID();
        if (log4j.isDebugEnabled())
          log4j.debug("CreateCashFlowStatement - " + " - strNewAmount - " + strAmount);
        CreateCashFlowStatementData.insertStatements(conn, this, strFactAcctCFS, strFactAcctId,
            vars.getClient(), vars.getOrg(), vars.getUser(), strOrderAccount, strAmount, null);
      } else if (!(data[0].cSettlementGenerateId).equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("CreateCashFlowStatement - SETTLEMENT_GENERATE");
        if ((data[0].ismanual).equals("Y")) {
          if (log4j.isDebugEnabled())
            log4j.debug("CreateCashFlowStatement - SETTLEMENT_GENERATE MANUAL");
          if (log4j.isDebugEnabled())
            log4j.debug("CreateCashFlowStatement - SETTLEMENT_GENERATE - "
                + data[0].cSettlementGenerateId + " - SETTLEMENT_CANCEL - "
                + data[0].cSettlementCancelId);
          CreateCashFlowStatementData[] glItems = CreateCashFlowStatementData
              .selectGLItemsSettlementGenerate(this, data[0].cSettlementGenerateId, strPaymentId);
          String strTotal = CreateCashFlowStatementData.selectSumGLItemsGenerate(this,
              data[0].cSettlementGenerateId, strPaymentId);
          if (glItems == null || glItems.length == 0) {
            glItems = CreateCashFlowStatementData.selectGLItemsSettlementCancel(this,
                data[0].cSettlementCancelId, strPaymentId);
            strTotal = CreateCashFlowStatementData.selectSumGLItemsCancel(this,
                data[0].cSettlementCancelId, strPaymentId);
          }
          for (int j = 0; j < glItems.length; j++) {
            String strNewAmount = multiply(calculateRatio(strTotal, strAmount), glItems[j].amount);
            if (log4j.isDebugEnabled())
              log4j.debug("CreateCashFlowStatement - Inserting glItem - " + (j + 1)
                  + " - strNewAmount - " + strNewAmount);
            String strFactAcctCFS = SequenceIdData.getUUID();
            CreateCashFlowStatementData.insertStatements(conn, this, strFactAcctCFS, strFactAcctId,
                vars.getClient(), vars.getOrg(), vars.getUser(), glItems[j].account, strNewAmount,
                glItems[j].id);
          }
        } else {
          if (log4j.isDebugEnabled())
            log4j.debug("CreateCashFlowStatement - SETTLEMENT_GENERATE NOT MANUAL");
          CreateCashFlowStatementData[] canceledPayments = CreateCashFlowStatementData
              .selectCancelledPayments(this, data[0].cSettlementGenerateId);
          if (canceledPayments == null || canceledPayments.length == 0) {
            String strPaymentAccount = CreateCashFlowStatementData.selectPaymentAccount(this);
            String strFactAcctCFS = SequenceIdData.getUUID();
            if (log4j.isDebugEnabled())
              log4j.debug("CreateCashFlowStatement - " + " - strNewAmount - " + strAmount);
            CreateCashFlowStatementData
                .insertStatements(conn, this, strFactAcctCFS, strFactAcctId, vars.getClient(),
                    vars.getOrg(), vars.getUser(), strPaymentAccount, strAmount, null);
          } else {
            String strTotal = CreateCashFlowStatementData.selectSumGeneratedPayments(this,
                data[0].cSettlementGenerateId);
            for (int j = 0; j < canceledPayments.length; j++) {
              String strNewAmount = multiply(calculateRatio(strTotal, strAmount),
                  canceledPayments[j].amount);
              if (log4j.isDebugEnabled())
                log4j.debug("CreateCashFlowStatement - Rellamada - strNewAmount - " + strNewAmount
                    + " - Payment - " + canceledPayments[j].id + " - strFactAcctId - "
                    + strFactAcctId);
              insertCFS(conn, response, vars, canceledPayments[j].id, strFactAcctId, strNewAmount,
                  strAccount, (level + 1));
            }
          }
        }
      }
    }
    if (level == 0) { // We make up for the difference with the higher value
      // in absolute terms
      if (log4j.isDebugEnabled())
        log4j
            .debug("CreateCashFlowStatement - Compensamos la diferencia con el valor mas alto en terminos absolutos");
      String strDifference = CreateCashFlowStatementData.selectCheckDifference(conn, this,
          strFactAcctId);
      CreateCashFlowStatementData[] records = CreateCashFlowStatementData.selectGetMaxId(conn,
          this, strFactAcctId);
      if (records != null && records.length > 0) {
        if (log4j.isDebugEnabled())
          log4j.debug("CreateCashFlowStatement - updateDifference - strDifference - "
              + strDifference + " - records[0].id - " + records[0].id);
        CreateCashFlowStatementData.updateDifference(conn, this, strDifference, records[0].id);
      }
    }
  }

  private String calculateRatio(String strTotal, String strAmt) {
    if (log4j.isDebugEnabled())
      log4j.debug("CreateCashFlowStatement - calculateRatio - strTotal - " + strTotal
          + " - strAmt - " + strAmt);
    if (strTotal == null || strAmt == null || strTotal.equals("0") || strAmt.equals("0")
        || strTotal.equals("") || strAmt.equals(""))
      return "0";
    BigDecimal total = new BigDecimal(strTotal);
    BigDecimal amt = new BigDecimal(strAmt);
    if (log4j.isDebugEnabled())
      log4j.debug("CreateCashFlowStatement - calculateRatio - strTotal - " + strTotal
          + " - strAmt - " + strAmt);
    if (log4j.isDebugEnabled())
      log4j.debug("CreateCashFlowStatement - calculateRatio - total - " + total.doubleValue()
          + " - amt - " + amt.doubleValue());
    String strRatio = "";
    try {
      amt = amt.divide(total, 200, BigDecimal.ROUND_HALF_UP);
      strRatio = amt.toString();
    } catch (Exception e) {
      e.printStackTrace();
      log4j.warn("Servlet CreateCashFlowStatement - calculateRatio - Exception");
    }
    if (log4j.isDebugEnabled())
      log4j.debug("CreateCashFlowStatement - calculateRatio - strRatio - " + strRatio);
    return strRatio;
  } // end of getServletInfo() method

  private String multiply(String strOP1, String strOP2) {
    if (log4j.isDebugEnabled())
      log4j.debug("CreateCashFlowStatement - multiply - strOP1 - " + strOP1 + " - strOP2 - "
          + strOP2);
    BigDecimal op1 = new BigDecimal(strOP1);
    BigDecimal op2 = new BigDecimal(strOP2);
    op1 = op1.setScale(200);
    op2 = op2.setScale(200);
    String strResult = "";
    try {
      strResult = op1.multiply(op2).toString();
    } catch (Exception e) {
      e.printStackTrace();
      log4j.warn("Servlet CreateCashFlowStatement - multiply - Exception");
    }
    return strResult;
  } // end of getServletInfo() method

  public String getServletInfo() {
    return "Servlet CreateCashFlowStatement";
  } // end of getServletInfo() method
}
