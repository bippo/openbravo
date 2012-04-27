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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class CreateFrom extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      final String strKey = vars.getGlobalVariable("inpKey", "CreateFrom|key");
      final String strTableId = vars.getGlobalVariable("inpTableId", "CreateFrom|tableId");
      final String strProcessId = vars
          .getGlobalVariable("inpProcessId", "CreateFrom|processId", "");
      final String strPath = vars.getGlobalVariable("inpPath", "CreateFrom|path", strDireccion
          + request.getServletPath());
      final String strWindowId = vars.getGlobalVariable("inpWindowId", "CreateFrom|windowId", "");
      final String strTabName = vars.getGlobalVariable("inpTabName", "CreateFrom|tabName", "");
      final String strDateInvoiced = vars.getGlobalVariable("inpDateInvoiced",
          "CreateFrom|dateInvoiced", "");
      final String strBPartnerLocation = vars.getGlobalVariable("inpcBpartnerLocationId",
          "CreateFrom|bpartnerLocation", "");
      final String strMPriceList = vars.getGlobalVariable("inpMPricelist", "CreateFrom|pricelist",
          "");
      final String strBPartner = vars
          .getGlobalVariable("inpcBpartnerId", "CreateFrom|bpartner", "");
      final String strStatementDate = vars.getGlobalVariable("inpstatementdate",
          "CreateFrom|statementDate", "");
      final String strBankAccount = vars.getGlobalVariable("inpcBankaccountId",
          "CreateFrom|bankAccount", "");
      final String strOrg = vars.getGlobalVariable("inpadOrgId", "CreateFrom|adOrgId", "");
      final String strIsreceipt = vars
          .getGlobalVariable("inpisreceipt", "CreateFrom|isreceipt", "");

      if (log4j.isDebugEnabled())
        log4j.debug("doPost - inpadOrgId = " + strOrg);
      if (log4j.isDebugEnabled())
        log4j.debug("doPost - inpisreceipt = " + strIsreceipt);

      // 26-06-07
      vars.setSessionValue("CreateFrom|default", "1");

      printPage_FS(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strDateInvoiced, strBPartnerLocation, strMPriceList, strBPartner,
          strStatementDate, strBankAccount, strOrg, strIsreceipt);
    } else if (vars.commandIn("FRAME1")) {
      final String strTableId = vars.getGlobalVariable("inpTableId", "CreateFrom|tableId");
      final String strType = pageType(strTableId);
      final String strKey = vars.getGlobalVariable("inpKey", "CreateFrom" + strType + "|key");
      final String strProcessId = vars.getGlobalVariable("inpProcessId", "CreateFrom" + strType
          + "|processId", "");
      final String strPath = vars.getGlobalVariable("inpPath", "CreateFrom" + strType + "|path",
          strDireccion + request.getServletPath());
      final String strWindowId = vars.getGlobalVariable("inpWindowId", "CreateFrom" + strType
          + "|windowId");
      final String strTabName = vars.getGlobalVariable("inpTabName", "CreateFrom" + strType
          + "|tabName");
      final String strDateInvoiced = vars.getGlobalVariable("inpDateInvoiced", "CreateFrom"
          + strType + "|dateInvoiced", "");
      final String strBPartnerLocation = vars.getGlobalVariable("inpcBpartnerLocationId",
          "CreateFrom" + strType + "|bpartnerLocation", "");
      final String strPriceList = vars.getGlobalVariable("inpMPricelist", "CreateFrom" + strType
          + "|pricelist", "");
      final String strBPartner = vars.getGlobalVariable("inpcBpartnerId", "CreateFrom" + strType
          + "|bpartner", "");
      final String strStatementDate = vars.getGlobalVariable("inpstatementdate", "CreateFrom"
          + strType + "|statementDate", "");
      final String strBankAccount = vars.getGlobalVariable("inpcBankaccountId", "CreateFrom"
          + strType + "|bankAccount", "");
      final String strOrg = vars.getGlobalVariable("inpadOrgId", "CreateFrom" + strType
          + "|adOrgId", "");
      final String strIsreceipt = vars.getGlobalVariable("inpisreceipt", "CreateFrom" + strType
          + "|isreceipt", "");

      if (log4j.isDebugEnabled())
        log4j.debug("doPost - inpadOrgId = " + strOrg);
      if (log4j.isDebugEnabled())
        log4j.debug("doPost - inpisreceipt = " + strIsreceipt);

      vars.removeSessionValue("CreateFrom" + strType + "|key");
      vars.removeSessionValue("CreateFrom" + strType + "|processId");
      vars.removeSessionValue("CreateFrom" + strType + "|path");
      vars.removeSessionValue("CreateFrom" + strType + "|windowId");
      vars.removeSessionValue("CreateFrom" + strType + "|tabName");
      vars.removeSessionValue("CreateFrom" + strType + "|dateInvoiced");
      vars.removeSessionValue("CreateFrom" + strType + "|bpartnerLocation");
      vars.removeSessionValue("CreateFrom" + strType + "|pricelist");
      vars.removeSessionValue("CreateFrom" + strType + "|bpartner");
      vars.removeSessionValue("CreateFrom" + strType + "|statementDate");
      vars.removeSessionValue("CreateFrom" + strType + "|bankAccount");
      vars.removeSessionValue("CreateFrom" + strType + "|adOrgId");
      vars.removeSessionValue("CreateFrom" + strType + "|isreceipt");

      callPrintPage(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strDateInvoiced, strBPartnerLocation, strPriceList, strBPartner,
          strStatementDate, strBankAccount, strOrg, strIsreceipt);
    } else if (vars.commandIn("FIND_PO", "FIND_INVOICE", "FIND_SHIPMENT", "FIND_BANK",
        "FIND_SETTLEMENT")) {
      final String strKey = vars.getRequiredStringParameter("inpKey");
      final String strTableId = vars.getStringParameter("inpTableId");
      final String strProcessId = vars.getStringParameter("inpProcessId");
      final String strPath = vars.getStringParameter("inpPath",
          strDireccion + request.getServletPath());
      final String strWindowId = vars.getStringParameter("inpWindowId");
      final String strTabName = vars.getStringParameter("inpTabName");
      final String strDateInvoiced = vars.getStringParameter("inpDateInvoiced");
      final String strBPartnerLocation = vars.getStringParameter("inpcBpartnerLocationId");
      final String strPriceList = vars.getStringParameter("inpMPricelist");
      final String strBPartner = vars.getStringParameter("inpcBpartnerId");
      final String strStatementDate = vars.getStringParameter("inpstatementdate");
      final String strBankAccount = vars.getStringParameter("inpcBankaccountId");
      final String strOrg = vars.getStringParameter("inpadOrgId");
      final String strIsreceipt = vars.getStringParameter("inpisreceipt");
      if (log4j.isDebugEnabled())
        log4j.debug("doPost - inpadOrgId = " + strOrg);
      if (log4j.isDebugEnabled())
        log4j.debug("doPost - inpisreceipt = " + strIsreceipt);

      callPrintPage(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strDateInvoiced, strBPartnerLocation, strPriceList, strBPartner,
          strStatementDate, strBankAccount, strOrg, strIsreceipt);
    } else if (vars.commandIn("REFRESH_INVOICES", "REFRESH_SHIPMENTS")) {
      final String strBPartner = vars.getStringParameter("inpcBpartnerId");
      final String strWindowId = vars.getStringParameter("inpWindowId");
      printPageInvoiceCombo(response, vars, strBPartner, strWindowId);
    } else if (vars.commandIn("SAVE")) {
      final String strProcessId = vars.getStringParameter("inpProcessId");
      final String strKey = vars.getRequiredStringParameter("inpKey");
      final String strTableId = vars.getStringParameter("inpTableId");
      final String strWindowId = vars.getStringParameter("inpWindowId");

      // Set this special case for auditing
      SessionInfo.setProcessType("CF");
      SessionInfo.setProcessId(strTableId);

      final OBError myMessage = saveMethod(vars, strKey, strTableId, strProcessId, strWindowId);
      final String strTabId = vars.getGlobalVariable("inpTabId", "CreateFrom|tabId");
      vars.setMessage(strTabId, myMessage);
      printPageClosePopUp(response, vars);
      vars.removeSessionValue("CreateFrom|key");
      vars.removeSessionValue("CreateFrom|processId");
      vars.removeSessionValue("CreateFrom|path");
      vars.removeSessionValue("CreateFrom|windowId");
      vars.removeSessionValue("CreateFrom|tabName");
      vars.removeSessionValue("CreateFrom|dateInvoiced");
      vars.removeSessionValue("CreateFrom|bpartnerLocation");
      vars.removeSessionValue("CreateFrom|pricelist");
      vars.removeSessionValue("CreateFrom|bpartner");
      vars.removeSessionValue("CreateFrom|statementDate");
      vars.removeSessionValue("CreateFrom|bankAccount");
      vars.removeSessionValue("CreateFrom|adOrgId");
      vars.removeSessionValue("CreateFrom|isreceipt");
      // response.sendRedirect(strPath);
    } else
      pageErrorPopUp(response);
  }

  private void printPage_FS(HttpServletResponse response, VariablesSecureApp vars, String strPath,
      String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName,
      String strDateInvoiced, String strBPartnerLocation, String strPriceList, String strBPartner,
      String strStatementDate, String strBankAccount, String strOrg, String strIsreceipt)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: FrameSet");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFrom_FS").createXmlDocument();
    final String strType = pageType(strTableId);
    vars.setSessionValue("CreateFrom" + strType + "|path", strPath);
    vars.setSessionValue("CreateFrom" + strType + "|key", strKey);
    vars.setSessionValue("CreateFrom" + strType + "|processId", strProcessId);
    vars.setSessionValue("CreateFrom" + strType + "|windowId", strWindowId);
    vars.setSessionValue("CreateFrom" + strType + "|tabName", strTabName);
    vars.setSessionValue("CreateFrom" + strType + "|dateInvoiced", strDateInvoiced);
    vars.setSessionValue("CreateFrom" + strType + "|bpartnerLocation", strBPartnerLocation);
    vars.setSessionValue("CreateFrom" + strType + "|pricelist", strPriceList);
    vars.setSessionValue("CreateFrom" + strType + "|bpartner", strBPartner);
    vars.setSessionValue("CreateFrom" + strType + "|statementDate", strStatementDate);
    vars.setSessionValue("CreateFrom" + strType + "|bankAccount", strBankAccount);
    vars.setSessionValue("CreateFrom" + strType + "|adOrgId", strOrg);
    vars.setSessionValue("CreateFrom" + strType + "|isreceipt", strIsreceipt);
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String pageType(String strTableId) {
    if (strTableId.equals("392"))
      return "Bank";
    else if (strTableId.equals("318"))
      return "Invoice";
    else if (strTableId.equals("319"))
      return "Shipment";
    else if (strTableId.equals("426"))
      return "Pay";
    else if (strTableId.equals("800019"))
      return "Settlement";
    else
      return "";
  }

  void callPrintPage(HttpServletResponse response, VariablesSecureApp vars, String strPath,
      String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName,
      String strDateInvoiced, String strBPartnerLocation, String strPriceList, String strBPartner,
      String strStatementDate, String strBankAccount, String strOrg, String strIsreceipt)
      throws IOException, ServletException {
    if (strTableId.equals("392")) { // C_BankStatement
      printPageBank(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strStatementDate, strBankAccount);
    } else if (strTableId.equals("318")) { // C_Invoice
      printPageInvoice(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strDateInvoiced, strBPartnerLocation, strPriceList, strBPartner);
    } else if (strTableId.equals("319")) { // M_InOut
      printPageShipment(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strBPartner);
    } else if (strTableId.equals("426")) { // C_PaySelection
      printPagePay(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strBPartner);
    } else if (strTableId.equals("800019")) { // C_Settlement
      printPageSettlement(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strBPartner);
    } else if (strTableId.equals("800176")) { // C_DP_Management
      printPageDPManagement(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strBPartner);
    } else if (strTableId.equals("800179")) { // C_Remittance
      printPageCRemittance(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId,
          strTabName, strBPartner, strOrg, strIsreceipt);
    } else {
      pageError(response);
    }
  }

  protected void printPageBank(HttpServletResponse response, VariablesSecureApp vars,
      String strPath, String strKey, String strTableId, String strProcessId, String strWindowId,
      String strTabName, String strStatementDate, String strBank) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Bank");
    final String strcBPartner = vars.getStringParameter("inpcBpartnerId");
    final String strPaymentRule = vars.getStringParameter("inppaymentrule");
    final String strPlannedDateFrom = vars.getStringParameter("inpplanneddateFrom");
    final String strPlannedDateTo = vars.getStringParameter("inpplanneddateTo");
    final String strAmountFrom = vars.getNumericParameter("inpamountFrom");
    final String strAmountTo = vars.getNumericParameter("inpamountTo");
    String strIsReceipt = vars.getStringParameter("inpisreceipt");
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    final String strBankAccount = vars.getStringParameter("inpcBankaccountId");
    final String strOrg = vars.getStringParameter("inpadOrgId");
    final String strCharge = vars.getStringParameter("inpCharge");
    final String strPlannedDate = vars.getStringParameter("inpplanneddate", strStatementDate);
    final String strCost = vars.getNumericParameter("inpcost", "0.00");
    final String strProposed = vars.getNumericParameter("inpproposed", "0.00");
    final String strDocumentNo = vars.getStringParameter("inpDocumentNo");
    final String strStatus = vars.getStringParameter("inpStatusPayment");
    CreateFromBankData[] data = null;
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFrom_Bank").createXmlDocument();

    final int numRows = Integer.valueOf(CreateFromBankData.countRows(this,
        Utility.getContext(this, vars, "#User_Client", strWindowId),
        Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
        strPlannedDateFrom, strPlannedDateTo, strAmountFrom, strAmountTo, strIsReceipt, strBank,
        strOrg, strCharge, strDocumentNo, strStatus));
    final int maxRows = Integer.valueOf(vars.getSessionValue("#RECORDRANGEINFO"));

    if (numRows > maxRows) {
      final OBError obError = new OBError();
      String strMsg = Utility.messageBD(this, "MAX_RECORDS_REACHED", vars.getLanguage());
      strMsg = strMsg.replaceAll("%returned%", String.valueOf(numRows));
      strMsg = strMsg.replaceAll("%shown%", String.valueOf(maxRows));
      obError.setMessage(strMsg);
      obError.setTitle("");
      obError.setType("WARNING");
      vars.setMessage("CreateFrom", obError);
    }

    // different limit/offset syntax in oracle and postgresql
    if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
      data = CreateFromBankData.select(this, vars.getLanguage(), strStatementDate, "ROWNUM",
          Utility.getContext(this, vars, "#User_Client", strWindowId),
          Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
          strPlannedDateFrom, strPlannedDateTo, strAmountFrom, strAmountTo, strIsReceipt, strBank,
          strOrg, strCharge, strDocumentNo, strStatus, String.valueOf(maxRows), null);
    } else {
      data = CreateFromBankData.select(this, vars.getLanguage(), strStatementDate, "1",
          Utility.getContext(this, vars, "#User_Client", strWindowId),
          Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
          strPlannedDateFrom, strPlannedDateTo, strAmountFrom, strAmountTo, strIsReceipt, strBank,
          strOrg, strCharge, strDocumentNo, strStatus, null, String.valueOf(maxRows));
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);
    xmlDocument.setParameter("statementDate", strStatementDate);
    xmlDocument.setParameter("paramCBankaccountID", strBank);
    xmlDocument.setParameter("paramPlannedDate", strPlannedDate);
    xmlDocument.setParameter("paramplanneddate", strPlannedDate);
    xmlDocument.setParameter("paramcost", strCost);
    xmlDocument.setParameter("paramproposed", strProposed);
    xmlDocument.setParameter("documentNo", strDocumentNo);
    xmlDocument.setParameter("StatusPayment", strStatus);

    xmlDocument.setParameter("paymentRule", strPaymentRule);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "All_Payment Rule", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "CreateFrom"), Utility.getContext(
              this, vars, "#User_Client", "CreateFrom"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateFrom", strPaymentRule);
      xmlDocument.setData("reportPaymentRule", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("cbpartnerId", strcBPartner);

    xmlDocument.setParameter("cbpartnerId_DES", CreateFromBankData.bpartner(this, strcBPartner));
    xmlDocument.setParameter("plannedDateFrom", strPlannedDateFrom);
    xmlDocument.setParameter("plannedDateFromdisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument
        .setParameter("plannedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTo", strPlannedDateTo);
    xmlDocument.setParameter("plannedDateTodisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("amountFrom", strAmountFrom);
    {
      final OBError myMessage = vars.getMessage("CreateFrom");
      vars.removeMessage("CreateFrom");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    xmlDocument.setParameter("amountTo", strAmountTo);
    xmlDocument.setParameter("isreceiptPago", strIsReceipt);
    xmlDocument.setParameter("isreceiptCobro", strIsReceipt);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("charge", strCharge);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "C_BankAccount_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strBank);
      xmlDocument.setData("reportC_BankAccount_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strOrg);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_DP_Management_Status", "Status - Exclude In Remittance", Utility.getContext(this,
              vars, "#AccessibleOrgTree", strWindowId), Utility.getContext(this, vars,
              "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strStatus);
      xmlDocument.setData("reportStatus_S", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  protected void printPageInvoice(HttpServletResponse response, VariablesSecureApp vars,
      String strPath, String strKey, String strTableId, String strProcessId, String strWindowId,
      String strTabName, String strDateInvoiced, String strBPartnerLocation, String strPriceList,
      String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Invoice");
    CreateFromInvoiceData[] data = null;
    XmlDocument xmlDocument;
    String strPO = vars.getStringParameter("inpPurchaseOrder");
    String strShipment = vars.getStringParameter("inpShipmentReciept");
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    if (vars.commandIn("FIND_PO"))
      strShipment = "";
    else if (vars.commandIn("FIND_SHIPMENT"))
      strPO = "";
    if (strPO.equals("") && strShipment.equals("")) {
      final String[] discard = { "sectionDetail" };
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_actionButton/CreateFrom_Invoice", discard)
          .createXmlDocument();
      data = CreateFromInvoiceData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_actionButton/CreateFrom_Invoice").createXmlDocument();
      if (strShipment.equals("")) {
        if (vars.getLanguage().equals("en_US")) {
          if (isSOTrx.equals("Y"))
            data = CreateFromInvoiceData.selectFromPOSOTrx(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          else
            data = CreateFromInvoiceData.selectFromPO(this, vars.getLanguage(), strKey,
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
        } else {
          if (isSOTrx.equals("Y"))
            data = CreateFromInvoiceData.selectFromPOTrlSOTrx(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          else
            data = CreateFromInvoiceData.selectFromPOTrl(this, vars.getLanguage(), strKey,
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
        }
      } else {
        if (vars.getLanguage().equals("en_US")) {
          if (isSOTrx.equals("Y"))
            data = CreateFromInvoiceData.selectFromShipmentSOTrx(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strShipment);
          else
            data = CreateFromInvoiceData.selectFromShipment(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strShipment);
        } else {
          if (isSOTrx.equals("Y"))
            data = CreateFromInvoiceData.selectFromShipmentTrlSOTrx(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strShipment);
          else
            data = CreateFromInvoiceData.selectFromShipmentTrl(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strShipment);
        }
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("dateInvoiced", strDateInvoiced);
    xmlDocument.setParameter("bpartnerLocation", strBPartnerLocation);
    xmlDocument.setParameter("pricelist", strPriceList);
    xmlDocument.setParameter("cBpartnerId", strBPartner);
    xmlDocument.setParameter("BPartnerDescription",
        CreateFromShipmentData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("PurchaseOrder", strPO);
    xmlDocument.setParameter("Shipment", strShipment);
    xmlDocument.setParameter("pType", (!strShipment.equals("") ? "SHIPMENT"
        : (!strPO.equals("")) ? "PO" : ""));
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);

    if (strBPartner.equals("")) {
      xmlDocument.setData("reportShipmentReciept", "liststructure", new CreateFromInvoiceData[0]);
      xmlDocument.setData("reportPurchaseOrder", "liststructure", new CreateFromInvoiceData[0]);
    } else {
      if (isSOTrx.equals("Y")) {
        xmlDocument.setData(
            "reportShipmentReciept",
            "liststructure",
            CreateFromInvoiceData.selectFromShipmentSOTrxCombo(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        xmlDocument.setData(
            "reportPurchaseOrder",
            "liststructure",
            CreateFromInvoiceData.selectFromPOSOTrxCombo(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
      } else {
        xmlDocument.setData(
            "reportShipmentReciept",
            "liststructure",
            CreateFromInvoiceData.selectFromShipmentCombo(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        xmlDocument.setData(
            "reportPurchaseOrder",
            "liststructure",
            CreateFromInvoiceData.selectFromPOCombo(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
      }
    }
    {
      final OBError myMessage = vars.getMessage("CreateFrom");
      vars.removeMessage("CreateFrom");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  protected void printPageShipment(HttpServletResponse response, VariablesSecureApp vars,
      String strPath, String strKey, String strTableId, String strProcessId, String strWindowId,
      String strTabName, String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Shipment");
    CreateFromShipmentData[] data = null;
    XmlDocument xmlDocument;
    String strPO = vars.getStringParameter("inpPurchaseOrder");
    String strInvoice = vars.getStringParameter("inpInvoice");
    final String strLocator = vars.getStringParameter("inpmLocatorId");
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    if (vars.commandIn("FIND_PO"))
      strInvoice = "";
    else if (vars.commandIn("FIND_INVOICE"))
      strPO = "";
    if (strPO.equals("") && strInvoice.equals("")) {
      final String[] discard = { "sectionDetail" };
      if (isSOTrx.equals("Y"))
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_actionButton/CreateFrom_Shipment", discard)
            .createXmlDocument();
      else
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_actionButton/CreateFrom_ShipmentPO", discard)
            .createXmlDocument();
      data = CreateFromShipmentData.set();
    } else {
      if (isSOTrx.equals("Y"))
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_actionButton/CreateFrom_Shipment").createXmlDocument();
      else
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_actionButton/CreateFrom_ShipmentPO").createXmlDocument();
      if (strInvoice.equals("")) {
        if (vars.getLanguage().equals("en_US")) {
          if (isSOTrx.equals("Y")) {
            data = CreateFromShipmentData.selectFromPOSOTrx(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          } else {
            data = CreateFromShipmentData.selectFromPO(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          }
        } else {
          if (isSOTrx.equals("Y"))
            data = CreateFromShipmentData.selectFromPOTrlSOTrx(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          else
            data = CreateFromShipmentData.selectFromPOTrl(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
        }
      } else {
        if (vars.getLanguage().equals("en_US")) {
          if (isSOTrx.equals("Y"))
            data = CreateFromShipmentData.selectFromInvoiceTrx(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strInvoice);
          else
            data = CreateFromShipmentData.selectFromInvoice(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strInvoice);
        } else {
          if (isSOTrx.equals("Y"))
            data = CreateFromShipmentData.selectFromInvoiceTrx(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strInvoice);
          else
            data = CreateFromShipmentData.selectFromInvoiceTrl(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strInvoice);
        }
      }
    }

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("cBpartnerId", strBPartner);
    xmlDocument.setParameter("BPartnerDescription",
        CreateFromShipmentData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("PurchaseOrder", strPO);
    xmlDocument.setParameter("M_Locator_ID", strLocator);
    xmlDocument.setParameter("M_Locator_ID_DES",
        CreateFromShipmentData.selectLocator(this, strLocator));
    xmlDocument.setParameter("Invoice", strInvoice);
    xmlDocument.setParameter("pType", (!strInvoice.equals("") ? "INVOICE"
        : (!strPO.equals("")) ? "PO" : ""));
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);

    if (strBPartner.equals("")) {
      xmlDocument.setData("reportInvoice", "liststructure", new CreateFromShipmentData[0]);
      xmlDocument.setData("reportPurchaseOrder", "liststructure", new CreateFromShipmentData[0]);
    } else {
      if (isSOTrx.equals("Y")) {
        xmlDocument.setData(
            "reportInvoice",
            "liststructure",
            CreateFromShipmentData.selectFromInvoiceTrxCombo(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        xmlDocument.setData(
            "reportPurchaseOrder",
            "liststructure",
            CreateFromShipmentData.selectFromPOSOTrxCombo(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
      } else {
        xmlDocument.setData(
            "reportInvoice",
            "liststructure",
            CreateFromShipmentData.selectFromInvoiceCombo(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        xmlDocument.setData(
            "reportPurchaseOrder",
            "liststructure",
            CreateFromShipmentData.selectFromPOCombo(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Client", strWindowId),
                Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
      }
    }

    {
      final OBError myMessage = vars.getMessage("CreateFrom");
      vars.removeMessage("CreateFrom");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    if (isSOTrx.equals("N")) {
      final CreateFromShipmentData[][] dataUOM = new CreateFromShipmentData[data.length][];

      for (int i = 0; i < data.length; i++) {
        // Obtain the specific units for each product

        dataUOM[i] = CreateFromShipmentData.selectUOM(this, data[i].mProductId);

        // Check the hidden fields

        final String strhavesec = data[i].havesec;

        if ("0".equals(strhavesec)) {
          data[i].havesec = "hidden";
          data[i].havesecuom = "none";
        } else {
          data[i].havesec = "text";
          data[i].havesecuom = "block";
        }
      }
      xmlDocument.setDataArray("reportM_Product_Uom_To_ID", "liststructure", dataUOM);
    }
    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageInvoiceCombo(HttpServletResponse response, VariablesSecureApp vars,
      String strBPartner, String strWindowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Refresh Invoices");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFrom_F0").createXmlDocument();
    String strArray = "";
    String strArray2 = "";
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);

    if (strBPartner.equals("")) {
      strArray = Utility.arrayEntradaSimple("arrDatos", new CreateFromShipmentData[0]);
      strArray2 = Utility.arrayEntradaSimple("arrDatos2", new CreateFromShipmentData[0]);
    } else {
      if (vars.commandIn("REFRESH_INVOICES")) { // Loading the combos in
        // the delivery note's
        // CreateFrom
        if (isSOTrx.equals("Y")) {
          strArray = Utility.arrayEntradaSimple("arrDatos", new CreateFromShipmentData[0]);
          strArray2 = Utility.arrayEntradaSimple(
              "arrDatos2",
              CreateFromShipmentData.selectFromPOSOTrxCombo(this, vars.getLanguage(),
                  Utility.getContext(this, vars, "#User_Client", strWindowId),
                  Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), strBPartner));
        } else {
          strArray = Utility.arrayEntradaSimple(
              "arrDatos",
              CreateFromShipmentData.selectFromInvoiceCombo(this, vars.getLanguage(),
                  Utility.getContext(this, vars, "#User_Client", strWindowId),
                  Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), strBPartner));
          strArray2 = Utility.arrayEntradaSimple(
              "arrDatos2",
              CreateFromShipmentData.selectFromPOCombo(this, vars.getLanguage(),
                  Utility.getContext(this, vars, "#User_Client", strWindowId),
                  Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), strBPartner));
        }
      } else { // Loading the Combos in the Invoice's CreateFrom
        if (isSOTrx.equals("Y")) {
          strArray = Utility.arrayEntradaSimple(
              "arrDatos",
              CreateFromInvoiceData.selectFromShipmentSOTrxCombo(this, vars.getLanguage(),
                  Utility.getContext(this, vars, "#User_Client", strWindowId),
                  Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), strBPartner));
          strArray2 = Utility.arrayEntradaSimple(
              "arrDatos2",
              CreateFromInvoiceData.selectFromPOSOTrxCombo(this, vars.getLanguage(),
                  Utility.getContext(this, vars, "#User_Client", strWindowId),
                  Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), strBPartner));
        } else {
          strArray = Utility.arrayEntradaSimple(
              "arrDatos",
              CreateFromInvoiceData.selectFromShipmentCombo(this, vars.getLanguage(),
                  Utility.getContext(this, vars, "#User_Client", strWindowId),
                  Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), strBPartner));
          strArray2 = Utility.arrayEntradaSimple(
              "arrDatos2",
              CreateFromInvoiceData.selectFromPOCombo(this, vars.getLanguage(),
                  Utility.getContext(this, vars, "#User_Client", strWindowId),
                  Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), strBPartner));
        }
      }
    }

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("array", strArray + "\n" + strArray2);
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  protected void printPagePay(HttpServletResponse response, VariablesSecureApp vars,
      String strPath, String strKey, String strTableId, String strProcessId, String strWindowId,
      String strTabName, String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Pay");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFrom_Pay").createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  protected void printPageSettlement(HttpServletResponse response, VariablesSecureApp vars,
      String strPath, String strKey, String strTableId, String strProcessId, String strWindowId,
      String strTabName, String strBPartner) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: Settlement");
    if (log4j.isDebugEnabled())
      log4j.debug(vars.commandIn("DEFAULT"));

    String strcBPartner = vars.getStringParameter("inpcBpartnerId");
    final String strPaymentRule = vars.getStringParameter("inppaymentrule");
    final String strPlannedDateFrom = vars.getStringParameter("inpplanneddateFrom");
    final String strPlannedDateTo = vars.getStringParameter("inpplanneddateTo");
    final String strAmountFrom = vars.getNumericParameter("inpamountFrom");
    final String strAmountTo = vars.getNumericParameter("inpamountTo");
    final String strTotalAmount = vars.getNumericParameter("inpamount");
    String strIsReceipt = vars.getStringParameter("inpisreceipt");
    if (log4j.isDebugEnabled())
      log4j.debug("IsReceipt: " + strIsReceipt);
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    final String strAutoCalc = vars.getStringParameter("inpAutoClaculated");
    String strAutoCalcSelect = "AMOUNT";

    if (strAutoCalc.equals(""))
      strAutoCalcSelect = "WRITEOFFAMT";

    final String strOrg = vars.getStringParameter("inpadOrgId");
    final String strMarcarTodos = vars.getStringParameter("inpTodos", "N");

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFrom_Settlement").createXmlDocument();
    CreateFromSettlementData[] data = null;

    if (vars.getSessionValue("CreateFrom|default").equals("1")) {

      vars.removeSessionValue("CreateFrom|default");

      /*
       * if (strcBPartner.equals("") && strPaymentRule.equals("") && strPlannedDateFrom.equals("")
       * && strPlannedDateTo.equals("") && strIsReceipt.equals("") && strTotalAmount.equals("") &&
       * strOrg.equals("")) {
       */

      // Modified 26-06-07
      if (log4j.isDebugEnabled())
        log4j.debug("strIsReceipt: \"\"");

      data = new CreateFromSettlementData[0];

      if (vars.commandIn("FRAME1")) {
        strcBPartner = strBPartner;
        strIsReceipt = isSOTrx;
      }
    } else {

      // Modified 26-06-07
      if (log4j.isDebugEnabled())
        log4j.debug("strIsReceipt: " + strIsReceipt);

      final int numRows = Integer.valueOf(CreateFromSettlementData.countRows(this,
          Utility.getContext(this, vars, "#User_Client", strWindowId),
          Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
          strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo,
          strTotalAmount, strOrg));
      final int maxRows = Integer.valueOf(vars.getSessionValue("#RECORDRANGEINFO"));

      if (numRows > maxRows) {
        final OBError obError = new OBError();
        String strMsg = Utility.messageBD(this, "MAX_RECORDS_REACHED", vars.getLanguage());
        strMsg = strMsg.replaceAll("%returned%", String.valueOf(numRows));
        strMsg = strMsg.replaceAll("%shown%", String.valueOf(maxRows));
        obError.setMessage(strMsg);
        obError.setTitle("");
        obError.setType("WARNING");
        vars.setMessage("CreateFrom", obError);
      }

      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        data = CreateFromSettlementData.select(this, vars.getLanguage(), strMarcarTodos, "ROWNUM",
            strAutoCalcSelect, Utility.getContext(this, vars, "#User_Client", strWindowId),
            Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
            strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo,
            strTotalAmount, strOrg, String.valueOf(maxRows), null);
      } else {
        data = CreateFromSettlementData.select(this, vars.getLanguage(), strMarcarTodos, "1",
            strAutoCalcSelect, Utility.getContext(this, vars, "#User_Client", strWindowId),
            Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
            strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo,
            strTotalAmount, strOrg, null, String.valueOf(maxRows));
      }

    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);
    xmlDocument.setParameter("autoCalculated", strAutoCalc);
    xmlDocument.setParameter("amountFrom", strAmountFrom);
    xmlDocument.setParameter("amountTo", strAmountTo);
    xmlDocument.setParameter("totalAmount", strTotalAmount);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("marcarTodos", strMarcarTodos);

    xmlDocument.setParameter("paymentRule", strPaymentRule);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "All_Payment Rule", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), Utility.getContext(
              this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strPaymentRule);
      xmlDocument.setData("reportPaymentRule", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strOrg);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    if (log4j.isDebugEnabled())
      log4j.debug("strcBPartner: " + strcBPartner);
    if (log4j.isDebugEnabled())
      log4j.debug("strPlannedDateFrom: " + strPlannedDateFrom);
    if (log4j.isDebugEnabled())
      log4j.debug("strPlannedDateTo: " + strPlannedDateTo);

    xmlDocument.setParameter("inpcBpartnerId", strcBPartner);
    xmlDocument.setParameter("inpBpartnerId_DES",
        CreateFromSettlementData.bpartner(this, strcBPartner));

    xmlDocument.setParameter("plannedDateFromdisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument
        .setParameter("plannedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateFromValue", strPlannedDateFrom);

    xmlDocument.setParameter("plannedDateTodisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateToValue", strPlannedDateTo);

    xmlDocument.setParameter("isreceiptPago", strIsReceipt);
    xmlDocument.setParameter("isreceiptCobro", strIsReceipt);

    {
      final OBError myMessage = vars.getMessage("CreateFrom");
      vars.removeMessage("CreateFrom");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setData("structure1", data);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  protected void printPageDPManagement(HttpServletResponse response, VariablesSecureApp vars,
      String strPath, String strKey, String strTableId, String strProcessId, String strWindowId,
      String strTabName, String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: DPManagement");
    String strcBPartner = vars.getStringParameter("inpcBpartnerId");
    final String strPaymentRule = vars.getStringParameter("inppaymentrule");
    final String strPlannedDateFrom = vars.getStringParameter("inpplanneddateFrom");
    final String strPlannedDateTo = vars.getStringParameter("inpplanneddateTo");
    final String strAmountFrom = vars.getNumericParameter("inpamountFrom");
    final String strAmountTo = vars.getNumericParameter("inpamountTo");
    final String strTotalAmount = vars.getNumericParameter("inpamount");
    String strIsReceipt = vars.getStringParameter("inpisreceipt");
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    // String strAutoCalc = vars.getStringParameter("inpAutoClaculated");
    // String strAutoCalcSelect = "AMOUNT";
    // if (strAutoCalc.equals("")) strAutoCalcSelect = "WRITEOFFAMT";

    final String strOrg = vars.getStringParameter("inpadOrgId");
    final String strStatusFrom = vars.getStringParameter("inpStatusFrom");
    final String strStatusTo = vars.getStringParameter("inpStatusTo");

    final String strMarcarTodos = vars.getStringParameter("inpTodos", "N");

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFrom_DPManagement").createXmlDocument();
    CreateFromDPManagementData[] data = null;
    if (strcBPartner.equals("") && strPaymentRule.equals("") && strPlannedDateFrom.equals("")
        && strPlannedDateTo.equals("") && strIsReceipt.equals("") && strTotalAmount.equals("")
        && strOrg.equals("")) {
      data = new CreateFromDPManagementData[0];
      if (vars.commandIn("FRAME1")) {
        strcBPartner = strBPartner;
        strIsReceipt = isSOTrx;
      }
    } else {
      final int numRows = Integer.valueOf(CreateFromDPManagementData.countRows(this,
          Utility.getContext(this, vars, "#User_Client", strWindowId),
          Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
          strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo,
          strTotalAmount, strStatusFrom, strOrg));
      final int maxRows = Integer.valueOf(vars.getSessionValue("#RECORDRANGEINFO"));

      if (numRows > maxRows) {
        String strMsg = Utility.messageBD(this, "MAX_RECORDS_REACHED", vars.getLanguage());
        strMsg = strMsg.replaceAll("%returned%", String.valueOf(numRows));
        strMsg = strMsg.replaceAll("%shown%", String.valueOf(maxRows));
        xmlDocument.setParameter("messageType", "WARNING");
        xmlDocument.setParameter("messageTitle", "");
        xmlDocument.setParameter("messageMessage", strMsg);
      }

      // different limit/offset syntax in oracle and postgresql
      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        data = CreateFromDPManagementData.select(this, vars.getLanguage(), "ROWNUM",
            strMarcarTodos, Utility.getContext(this, vars, "#User_Client", strWindowId),
            Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
            strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo,
            strTotalAmount, strStatusFrom, strOrg, String.valueOf(maxRows), null);
      } else {
        data = CreateFromDPManagementData.select(this, vars.getLanguage(), "1", strMarcarTodos,
            Utility.getContext(this, vars, "#User_Client", strWindowId),
            Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule,
            strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo,
            strTotalAmount, strStatusFrom, strOrg, null, String.valueOf(maxRows));
      }

      if (log4j.isDebugEnabled())
        log4j.debug("DPSelect: lineas" + data.length + "client "
            + Utility.getContext(this, vars, "#User_Client", strWindowId) + "userOrg "
            + Utility.getContext(this, vars, "#User_Org", strWindowId) + " partner:" + strcBPartner
            + " rule:" + strPaymentRule + "df" + strPlannedDateFrom + " dt:" + strPlannedDateTo
            + " rec:" + strIsReceipt + "amtF:" + strAmountFrom + "amt T:" + strAmountTo + "ttlamt:"
            + strTotalAmount + "org " + strOrg);
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);
    // xmlDocument.setParameter("autoCalculated", strAutoCalc);
    xmlDocument.setParameter("amountFrom", strAmountFrom);
    xmlDocument.setParameter("amountTo", strAmountTo);
    xmlDocument.setParameter("totalAmount", strTotalAmount);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("statusTo", strStatusTo);
    xmlDocument.setParameter("statusFrom", strStatusFrom);

    xmlDocument.setParameter("marcarTodos", strMarcarTodos);

    xmlDocument.setParameter("paymentRule", strPaymentRule);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "All_Payment Rule", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), Utility.getContext(
              this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strPaymentRule);
      xmlDocument.setData("reportPaymentRule", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strOrg);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_DP_Management_Status", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strStatusFrom);
      xmlDocument.setData("reportStatusFrom", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_DP_Management_Status", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strStatusFrom);
      xmlDocument.setData("reportStatusTo", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("cbpartnerId", strcBPartner);
    xmlDocument.setParameter("cbpartnerId_DES",
        CreateFromDPManagementData.bpartner(this, strcBPartner));
    xmlDocument.setParameter("plannedDateFrom", strPlannedDateFrom);
    xmlDocument.setParameter("plannedDateTo", strPlannedDateTo);

    xmlDocument.setParameter("plannedDateFromdisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument
        .setParameter("plannedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("plannedDateTodisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("isreceiptPago", strIsReceipt);
    xmlDocument.setParameter("isreceiptCobro", strIsReceipt);

    xmlDocument.setData("structure1", data);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  protected void printPageCRemittance(HttpServletResponse response, VariablesSecureApp vars,
      String strPath, String strKey, String strTableId, String strProcessId, String strWindowId,
      String strTabName, String strBPartner, String stradOrgId, String isReceipt)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: CRemittance");
    String strcBPartner = vars.getStringParameter("inpcBpartnerId");
    final String strPaymentRule = vars.getStringParameter("inppaymentrule");
    final String strPlannedDateFrom = vars.getStringParameter("inpplanneddateFrom");
    final String strPlannedDateTo = vars.getStringParameter("inpplanneddateTo");
    final String strAmountFrom = vars.getNumericParameter("inpamountFrom");
    final String strAmountTo = vars.getNumericParameter("inpamountTo");
    final String strTotalAmount = vars.getNumericParameter("inpamount");
    String strIsReceipt = vars.getStringParameter("inpisreceipt", isReceipt);
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    // String strAutoCalc = vars.getStringParameter("inpAutoCalculated");
    // String strAutoCalcSelect = "AMOUNT";
    // if (strAutoCalc.equals("")) strAutoCalcSelect = "WRITEOFFAMT";

    final String strOrg = vars.getStringParameter("inpadOrgId", stradOrgId);
    final String strStatusFrom = vars.getStringParameter("inpStatusFrom");
    // String strStatusTo = vars.getStringParameter("inpStatusTo");

    final String strMarcarTodos = vars.getStringParameter("inpTodos", "N");
    final String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    final String strOrgFamily = Tree.getMembers(this, strTreeOrg, strOrg);

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFrom_CRemittance").createXmlDocument();
    // XmlDocument xmlDocument = null;
    CreateFromCRemittanceData[] data = null;
    if (vars.commandIn("FRAME1")) {
      data = new CreateFromCRemittanceData[0];
      strcBPartner = strBPartner;
      strIsReceipt = isSOTrx;
    } else {
      final int numRows = Integer.valueOf(CreateFromCRemittanceData.countRows(this,
          Utility.getContext(this, vars, "#User_Client", strWindowId),
          Utility.getContext(this, vars, "#User_Org", strWindowId), strOrgFamily, strcBPartner,
          strPaymentRule, strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom,
          strAmountTo, strTotalAmount, strStatusFrom));
      final int maxRows = Integer.valueOf(vars.getSessionValue("#RECORDRANGEINFO"));

      if (numRows > maxRows) {
        final OBError obError = new OBError();
        String strMsg = Utility.messageBD(this, "MAX_RECORDS_REACHED", vars.getLanguage());
        strMsg = strMsg.replaceAll("%returned%", String.valueOf(numRows));
        strMsg = strMsg.replaceAll("%shown%", String.valueOf(maxRows));
        obError.setMessage(strMsg);
        obError.setTitle("");
        obError.setType("WARNING");
        vars.setMessage("CreateFrom", obError);
      }

      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        data = CreateFromCRemittanceData.select(this, vars.getLanguage(), strMarcarTodos, "ROWNUM",
            Utility.getContext(this, vars, "#User_Client", strWindowId),
            Utility.getContext(this, vars, "#User_Org", strWindowId), strOrgFamily, strcBPartner,
            strPaymentRule, strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom,
            strAmountTo, strTotalAmount, strStatusFrom, String.valueOf(maxRows), null);
      } else {
        data = CreateFromCRemittanceData.select(this, vars.getLanguage(), strMarcarTodos, "1",
            Utility.getContext(this, vars, "#User_Client", strWindowId),
            Utility.getContext(this, vars, "#User_Org", strWindowId), strOrgFamily, strcBPartner,
            strPaymentRule, strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom,
            strAmountTo, strTotalAmount, strStatusFrom, null, String.valueOf(maxRows));
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);
    // xmlDocument.setParameter("autoCalculated", strAutoCalc);
    xmlDocument.setParameter("amountFrom", strAmountFrom);
    xmlDocument.setParameter("amountTo", strAmountTo);
    xmlDocument.setParameter("totalAmount", strTotalAmount);
    // xmlDocument.setParameter("statusTo", strStatusTo);
    xmlDocument.setParameter("statusFrom", strStatusFrom);

    xmlDocument.setParameter("marcarTodos", strMarcarTodos);

    xmlDocument.setParameter("paymentRule", strPaymentRule);

    xmlDocument.setParameter("plannedDateFromdisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument
        .setParameter("plannedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("plannedDateTodisplayFormat",
        vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "All_Payment Rule", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), Utility.getContext(
              this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strPaymentRule);
      xmlDocument.setData("reportPaymentRule", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strOrg);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_DP_Management_Status", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strStatusFrom);
      xmlDocument.setData("reportStatusFrom", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("cbpartnerId", strcBPartner);
    xmlDocument.setParameter("cbpartnerId_DES",
        CreateFromDPManagementData.bpartner(this, strcBPartner));
    xmlDocument.setParameter("plannedDateFrom", strPlannedDateFrom);
    xmlDocument.setParameter("plannedDateTo", strPlannedDateTo);
    xmlDocument.setParameter("isreceiptPago", strIsReceipt);
    xmlDocument.setParameter("isreceiptCobro", strIsReceipt);
    xmlDocument.setParameter("adOrgId", strOrg);

    xmlDocument.setData("structure1", data);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  OBError saveMethod(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId, String strWindowId) throws IOException, ServletException {
    if (strTableId.equals("392"))
      return saveBank(vars, strKey, strTableId, strProcessId);
    else if (strTableId.equals("318"))
      return saveInvoice(vars, strKey, strTableId, strProcessId, strWindowId);
    else if (strTableId.equals("319"))
      return saveShipment(vars, strKey, strTableId, strProcessId, strWindowId);
    else if (strTableId.equals("426"))
      return savePay(vars, strKey, strTableId, strProcessId);
    else if (strTableId.equals("800019"))
      return saveSettlement(vars, strKey, strTableId, strProcessId);
    else if (strTableId.equals("800176"))
      return saveDPManagement(vars, strKey, strTableId, strProcessId);
    else if (strTableId.equals("800179"))
      return saveCRemittance(vars, strKey, strTableId, strProcessId);
    else
      return null;
  }

  protected OBError saveBank(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Bank");
    String strPayment = vars.getInStringParameter("inpcPaymentId", IsIDFilter.instance);
    final String strStatementDate = vars.getStringParameter("inpstatementdate");
    String strDateplanned = "";
    String strChargeamt = "";
    String strProposedAmt = "";
    if (strPayment.equals(""))
      return null;
    OBError myMessage = null;
    Connection conn = null;
    if (strPayment.equals(""))
      return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    String[] ids = restrictParameter(strPayment);

    try {
      conn = this.getTransactionConnection();
      for (int k = 0; k < ids.length; k++) {
        if (ids[k].startsWith("("))
          ids[k] = ids[k].substring(1, ids[k].length() - 1);
        if (!ids[k].equals("")) {
          ids[k] = Replace.replace(ids[k], "'", "");
          final StringTokenizer st = new StringTokenizer(ids[k], ",", false);
          while (st.hasMoreTokens()) {
            String strDebtPaymentId = st.nextToken().trim();
            if (!CreateFromBankData.NotIsReconcilied(conn, this, strDebtPaymentId)) {
              releaseRollbackConnection(conn);
              log4j.warn("CreateFrom.saveBank - debt_payment " + strDebtPaymentId
                  + " is reconcilied");
              myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                  "DebtPaymentReconcilied");
              return myMessage;
            }
            strDateplanned = vars.getStringParameter("inpplanneddate" + strDebtPaymentId.trim());
            strChargeamt = vars.getNumericParameter("inpcost" + strDebtPaymentId.trim());
            strProposedAmt = vars.getNumericParameter("inpproposed" + strDebtPaymentId.trim());
            // Amount + writeoff amount = FinalAmount to be paid/collected
            String strFinalAmount = CreateFromBankData.selectPaymentFinalAmount(this,
                strDebtPaymentId.trim());
            BigDecimal finalAmount = new BigDecimal(strFinalAmount);
            if (strProposedAmt != null && !strProposedAmt.equals("")
                && new BigDecimal(strProposedAmt).signum() != 0
                && new BigDecimal(strProposedAmt).compareTo(finalAmount) != 0) {
              final String strSettlement = SequenceIdData.getUUID();
              final String strDocNo = Utility.getDocumentNoConnection(conn, this, vars.getClient(),
                  "C_Settlement", true);
              CreateFromBankData.insertSettlement(conn, this, strSettlement, vars.getUser(),
                  strDocNo, strStatementDate, strDebtPaymentId);
              final String strNewPayment = SequenceIdData.getUUID();
              CreateFromBankData.insertPayment(conn, this, strNewPayment, vars.getUser(),
                  strSettlement, strProposedAmt, strDebtPaymentId);
              CreateFromBankData.cancelOriginalPayment(conn, this, strSettlement, strDebtPaymentId);
              CreateFromBankData.insertSecondPayment(conn, this, vars.getUser(), strSettlement,
                  strProposedAmt, strDebtPaymentId);
              strDebtPaymentId = strNewPayment;
              CreateFromBankData.processSettlement(conn, this, strSettlement);
            }
            final String strSequence = SequenceIdData.getUUID();
            try {
              CreateFromBankData.insert(conn, this, strSequence, vars.getClient(), vars.getUser(),
                  strKey, strDateplanned.equals("") ? strStatementDate : strDateplanned,
                  strChargeamt, strDebtPaymentId);
            } catch (final ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }
        }
      }
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (final Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  protected OBError saveInvoice(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId, String strWindowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Invoice");
    final String strDateInvoiced = vars.getRequiredStringParameter("inpDateInvoiced");
    final String strBPartnerLocation = vars.getRequiredStringParameter("inpcBpartnerLocationId");
    final String strBPartner = vars.getRequiredStringParameter("inpcBpartnerId");
    final String strPriceList = vars.getRequiredStringParameter("inpMPricelist");
    final String strType = vars.getRequiredStringParameter("inpType");
    final String strClaves = Utility.stringList(vars.getRequiredInParameter("inpcOrderId",
        IsIDFilter.instance));
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    String strPO = "", priceActual = "0", priceLimit = "0", priceList = "0", strPriceListVersion = "", priceStd = "0";
    CreateFromInvoiceData[] data = null;
    CreateFromInvoiceData[] dataAux = null;
    OBError myMessage = null;
    Connection conn = null;
    String[] ids = restrictParameter(strClaves);
    try {
      conn = this.getTransactionConnection();
      for (int k = 0; k < ids.length; k++) {
        if (strType.equals("SHIPMENT")) {
          if (isSOTrx.equals("Y"))
            data = CreateFromInvoiceData.selectFromShipmentUpdateSOTrx(conn, this, ids[k]);
          else
            data = CreateFromInvoiceData.selectFromShipmentUpdate(conn, this, ids[k]);
          dataAux = CreateFromInvoiceData
              .selectPriceList(conn, this, strDateInvoiced, strPriceList);
          if (dataAux == null || dataAux.length == 0) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                "PriceListVersionNotFound");
            releaseRollbackConnection(conn);
            return myMessage;
          }
          strPriceListVersion = dataAux[0].id;
        } else {
          strPO = vars.getStringParameter("inpPurchaseOrder");
          if (isSOTrx.equals("Y"))
            data = CreateFromInvoiceData.selectFromPOUpdateSOTrx(conn, this, ids[k]);
          else
            data = CreateFromInvoiceData.selectFromPOUpdate(conn, this, strKey, ids[k]);
        }
        if (data != null) {
          for (int i = 0; i < data.length; i++) {
            final String strSequence = SequenceIdData.getUUID();
            CreateFromInvoiceData[] price = null;
            String C_Tax_ID = "";
            if (data[i].cOrderlineId.equals(""))
              C_Tax_ID = Tax.get(this, data[i].mProductId, strDateInvoiced, data[i].adOrgId, vars
                  .getWarehouse(), strBPartnerLocation, strBPartnerLocation, CreateFromInvoiceData
                  .selectProject(this, strKey), isSOTrx.equals("Y") ? true : false);
            else
              C_Tax_ID = CreateFromInvoiceData.getTax(this, data[i].cOrderlineId);

            final int stdPrecision;
            final int curPrecision;
            stdPrecision = Integer.valueOf(data[i].stdprecision).intValue();
            if (strType.equals("SHIPMENT")) {
              curPrecision = Integer.valueOf(dataAux[0].priceprecision).intValue();
            } else {
              curPrecision = Integer.valueOf(data[i].priceprecision).intValue();
            }
            if (!data[i].cOrderlineId.equals("")) {
              price = CreateFromInvoiceData.selectPrices(conn, this, data[i].cOrderlineId);
              if (price != null && price.length > 0) {
                priceList = price[0].pricelist;
                priceLimit = price[0].pricelimit;
                priceStd = price[0].pricestd;
                priceActual = price[0].priceactual;
              }
              if (isSOTrx.equals("Y") && price[0].cancelpricead.equals("Y")) {
                priceActual = priceStd;
              }
              price = null;
            } else {
              price = CreateFromInvoiceData.selectBOM(conn, this, strDateInvoiced, strBPartner,
                  data[i].mProductId, strPriceListVersion);
              if (price != null && price.length > 0) {
                priceList = price[0].pricelist;
                priceLimit = price[0].pricelimit;
                priceStd = price[0].pricestd;
                priceActual = CreateFromInvoiceData.getOffersPriceInvoice(this, strDateInvoiced,
                    strBPartner, data[i].mProductId, priceStd, data[i].quantityorder, strPriceList,
                    strKey);
              }
              price = null;
            }
            BigDecimal LineNetAmt = (new BigDecimal(priceActual)).multiply(new BigDecimal(
                data[i].id));
            LineNetAmt = LineNetAmt.setScale(curPrecision, BigDecimal.ROUND_HALF_UP);
            try {
              final String strOrg2 = vars.getGlobalVariable("inpadOrgId", "CreateFrom|adOrgId", "");
              CreateFromInvoiceData.insert(conn, this, strSequence, strKey, vars.getClient(),
                  strOrg2, vars.getUser(), data[i].cOrderlineId, data[i].mInoutlineId,
                  data[i].description, data[i].mProductId, data[i].cUomId, data[i].id, priceList,
                  priceActual, priceLimit, LineNetAmt.toString(), C_Tax_ID, data[i].quantityorder,
                  data[i].mProductUomId, data[i].mAttributesetinstanceId, priceStd,
                  data[i].taxbaseamt);
            } catch (final ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }
        }

        if (!strPO.equals("")) {
          try {
            final int total = CreateFromInvoiceData.deleteC_Order_ID(conn, this, strKey, strPO);
            if (total == 0)
              CreateFromInvoiceData.updateC_Order_ID(conn, this, strPO, strKey);
          } catch (final ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }

        }
      }
      releaseCommitConnection(conn);
      if (log4j.isDebugEnabled())
        log4j.debug("Save commit");
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (final Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  protected OBError saveShipment(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId, String strWindowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Shipment");
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    if (isSOTrx.equals("Y"))
      return saveShipmentSO(vars, strKey, strTableId, strProcessId, strWindowId);
    else
      return saveShipmentPO(vars, strKey, strTableId, strProcessId, strWindowId);
  }

  protected OBError saveShipmentPO(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId, String strWindowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Shipment");
    final String strLocatorCommon = vars.getStringParameter("inpmLocatorId");
    final String strType = vars.getRequiredStringParameter("inpType");
    final String strClaves = Utility.stringList(vars.getRequiredInParameter("inpId",
        IsIDFilter.instance));
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    String strInvoice = "", strPO = "";
    CreateFromShipmentData[] data = null;
    OBError myMessage = null;
    Connection conn = null;
    String[] ids = restrictParameter(strClaves);
    try {
      conn = this.getTransactionConnection();
      for (int k = 0; k < ids.length; k++) {
        if (strType.equals("INVOICE")) {
          strInvoice = vars.getStringParameter("inpInvoice");
          if (!isSOTrx.equals("Y"))
            data = CreateFromShipmentData.selectFromInvoiceUpdate(conn, this, ids[k]);
        } else {
          strPO = vars.getStringParameter("inpPurchaseOrder");
          if (isSOTrx.equals("Y"))
            data = CreateFromShipmentData.selectFromPOUpdateSOTrx(conn, this, ids[k]);
          else
            data = CreateFromShipmentData.selectFromPOUpdate(conn, this, ids[k]);
        }
        if (data != null) {
          for (int i = 0; i < data.length; i++) {

            // Obtain the values from the window

            String strLineId = "";

            if (strType.equals("INVOICE")) {
              strLineId = data[i].cInvoicelineId;
            } else {
              strLineId = data[i].cOrderlineId;
            }

            final String strMovementqty = vars.getRequiredStringParameter("inpmovementqty"
                + strLineId);
            String strQuantityorder = "";
            String strProductUomId = "";
            String strLocator = vars.getStringParameter("inpmLocatorId" + strLineId);
            final String strmAttributesetinstanceId = vars
                .getStringParameter("inpmAttributesetinstanceId" + strLineId);
            final String strcUomIdConversion = "";
            String strbreakdown = "";
            CreateFromShipmentData[] dataUomIdConversion = null;

            if ("".equals(strLocator)) {
              strLocator = strLocatorCommon;
            }

            if ("".equals(data[i].mProductUomId)) {
              strQuantityorder = "";
              strProductUomId = "";
            } else {
              strQuantityorder = vars.getRequiredStringParameter("inpquantityorder" + strLineId);
              strProductUomId = vars.getRequiredStringParameter("inpmProductUomId" + strLineId);
              dataUomIdConversion = CreateFromShipmentData.selectcUomIdConversion(this,
                  strProductUomId);

              if (dataUomIdConversion == null || dataUomIdConversion.length == 0) {
                dataUomIdConversion = CreateFromShipmentData.set();
                strbreakdown = "N";
              } else {
                strbreakdown = dataUomIdConversion[0].breakdown;
              }
            }

            //

            String strMultiplyRate = "";
            int stdPrecision = 0;
            if ("Y".equals(strbreakdown)) {
              if (dataUomIdConversion[0].cUomIdConversion.equals("")) {
                releaseRollbackConnection(conn);
                myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                    "ProcessRunError");
                return myMessage;
              }
              final String strInitUOM = dataUomIdConversion[0].cUomIdConversion;
              final String strUOM = data[i].cUomId;
              if (strInitUOM.equals(strUOM))
                strMultiplyRate = "1";
              else
                strMultiplyRate = CreateFromShipmentData.multiplyRate(this, strInitUOM, strUOM);
              if (strMultiplyRate.equals(""))
                strMultiplyRate = CreateFromShipmentData.divideRate(this, strUOM, strInitUOM);
              if (strMultiplyRate.equals("")) {
                strMultiplyRate = "1";
                releaseRollbackConnection(conn);
                myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                    "ProcessRunError");
                return myMessage;
              }
              stdPrecision = Integer.valueOf(dataUomIdConversion[0].stdprecision).intValue();
              BigDecimal quantity, qty, multiplyRate;

              multiplyRate = new BigDecimal(strMultiplyRate);
              qty = new BigDecimal(strMovementqty);
              boolean qtyIsNegative = false;
              if (qty.compareTo(ZERO) < 0) {
                qtyIsNegative = true;
                qty = qty.negate();
              }
              quantity = qty.multiply(multiplyRate);
              if (quantity.scale() > stdPrecision)
                quantity = quantity.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
              while (qty.compareTo(ZERO) > 0) {
                String total = "1";
                BigDecimal conversion;
                if (quantity.compareTo(BigDecimal.ONE) < 0) {
                  total = quantity.toString();
                  conversion = qty;
                  quantity = ZERO;
                  qty = ZERO;
                } else {
                  conversion = multiplyRate;
                  if (conversion.compareTo(qty) > 0) {
                    conversion = qty;
                    qty = ZERO;
                  } else
                    qty = qty.subtract(conversion);
                  quantity = quantity.subtract(BigDecimal.ONE);
                }
                final String strConversion = conversion.toString();
                final String strSequence = SequenceIdData.getUUID();
                try {
                  CreateFromShipmentData.insert(conn, this, strSequence, strKey, vars.getClient(),
                      data[i].adOrgId, vars.getUser(), data[i].description, data[i].mProductId,
                      data[i].cUomId, (qtyIsNegative ? "-" + strConversion : strConversion),
                      data[i].cOrderlineId, strLocator,
                      CreateFromShipmentData.isInvoiced(conn, this, data[i].cInvoicelineId),
                      (qtyIsNegative ? "-" + total : total), data[i].mProductUomId,
                      strmAttributesetinstanceId);
                  if (!strInvoice.equals(""))
                    CreateFromShipmentData.updateInvoice(conn, this, strSequence,
                        data[i].cInvoicelineId);
                  else
                    CreateFromShipmentData.updateInvoiceOrder(conn, this, strSequence,
                        data[i].cOrderlineId);
                } catch (final ServletException ex) {
                  myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                      ex.getMessage());
                  releaseRollbackConnection(conn);
                  return myMessage;
                }
              }
            } else {
              final String strSequence = SequenceIdData.getUUID();
              try {
                CreateFromShipmentData.insert(conn, this, strSequence, strKey, vars.getClient(),
                    data[i].adOrgId, vars.getUser(), data[i].description, data[i].mProductId,
                    data[i].cUomId, strMovementqty, data[i].cOrderlineId, strLocator,
                    CreateFromShipmentData.isInvoiced(conn, this, data[i].cInvoicelineId),
                    strQuantityorder, strProductUomId, strmAttributesetinstanceId);
                if (!strInvoice.equals("")) {
                  String strInOutLineId = CreateFromShipmentData.selectInvoiceInOut(conn, this,
                      data[i].cInvoicelineId);
                  if (strInOutLineId.isEmpty())
                    CreateFromShipmentData.updateInvoice(conn, this, strSequence,
                        data[i].cInvoicelineId);
                  else {
                    CreateFromShipmentData.insertMatchInv(conn, this, vars.getUser(),
                        data[i].cInvoicelineId, strSequence, data[i].cInvoiceId);
                  }
                } else
                  CreateFromShipmentData.updateInvoiceOrder(conn, this, strSequence,
                      data[i].cOrderlineId);
              } catch (final ServletException ex) {
                myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
                releaseRollbackConnection(conn);
                return myMessage;
              }
            }
          }
        }

        if (!strPO.equals("")) {
          try {
            final int total = CreateFromShipmentData.deleteC_Order_ID(conn, this, strKey, strPO);
            if (total == 0)
              CreateFromShipmentData.updateC_Order_ID(conn, this, strPO, strKey);
          } catch (final ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
        if (!strInvoice.equals("")) {
          try {
            final int total = CreateFromShipmentData.deleteC_Invoice_ID(conn, this, strKey,
                strInvoice);
            if (total == 0)
              CreateFromShipmentData.updateC_Invoice_ID(conn, this, strInvoice, strKey);
          } catch (final ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }
      releaseCommitConnection(conn);
      if (log4j.isDebugEnabled())
        log4j.debug("Save commit");
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (final Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  protected OBError saveShipmentSO(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId, String strWindowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Shipment");
    final String strLocator = vars.getRequiredStringParameter("inpmLocatorId");
    final String strType = vars.getRequiredStringParameter("inpType");
    final String strClaves = Utility.stringList(vars.getRequiredInParameter("inpId",
        IsIDFilter.instance));
    final String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    String strInvoice = "", strPO = "";
    CreateFromShipmentData[] data = null;
    OBError myMessage = null;
    Connection conn = null;
    String[] ids = restrictParameter(strClaves);
    try {
      conn = this.getTransactionConnection();
      for (int k = 0; k < ids.length; k++) {
        if (strType.equals("INVOICE")) {
          strInvoice = vars.getStringParameter("inpInvoice");
          if (isSOTrx.equals("Y"))
            data = CreateFromShipmentData.selectFromInvoiceTrxUpdate(conn, this, ids[k]);
          else
            data = CreateFromShipmentData.selectFromInvoiceUpdate(conn, this, ids[k]);
        } else {
          strPO = vars.getStringParameter("inpPurchaseOrder");
          if (isSOTrx.equals("Y"))
            data = CreateFromShipmentData.selectFromPOUpdateSOTrx(conn, this, ids[k]);
          else
            data = CreateFromShipmentData.selectFromPOUpdate(conn, this, ids[k]);
        }
        if (data != null) {
          for (int i = 0; i < data.length; i++) {
            String strMultiplyRate = "";
            int stdPrecision = 0;
            if (data[i].breakdown.equals("Y")) {
              if (data[i].cUomIdConversion.equals("")) {
                releaseRollbackConnection(conn);
                myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                    "ProcessRunError");
                return myMessage;
              }
              final String strInitUOM = data[i].cUomIdConversion;
              final String strUOM = data[i].cUomId;
              if (strInitUOM.equals(strUOM))
                strMultiplyRate = "1";
              else
                strMultiplyRate = CreateFromShipmentData.multiplyRate(this, strInitUOM, strUOM);
              if (strMultiplyRate.equals(""))
                strMultiplyRate = CreateFromShipmentData.divideRate(this, strUOM, strInitUOM);
              if (strMultiplyRate.equals("")) {
                strMultiplyRate = "1";
                releaseRollbackConnection(conn);
                myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                    "ProcessRunError");
                return myMessage;
              }
              stdPrecision = Integer.valueOf(data[i].stdprecision).intValue();
              BigDecimal quantity, qty, multiplyRate;

              multiplyRate = new BigDecimal(strMultiplyRate);
              qty = new BigDecimal(data[i].id);
              boolean qtyIsNegative = false;
              if (qty.compareTo(ZERO) < 0) {
                qtyIsNegative = true;
                qty = qty.negate();
              }
              quantity = qty.multiply(multiplyRate);
              if (quantity.scale() > stdPrecision)
                quantity = quantity.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
              while (qty.compareTo(ZERO) > 0) {
                String total = "1";
                BigDecimal conversion;
                if (quantity.compareTo(BigDecimal.ONE) < 0) {
                  total = quantity.toString();
                  conversion = qty;
                  quantity = ZERO;
                  qty = ZERO;
                } else {
                  conversion = multiplyRate;
                  if (conversion.compareTo(qty) > 0) {
                    conversion = qty;
                    qty = ZERO;
                  } else
                    qty = qty.subtract(conversion);
                  quantity = quantity.subtract(BigDecimal.ONE);
                }
                final String strConversion = conversion.toString();
                final String strSequence = SequenceIdData.getUUID();
                try {
                  CreateFromShipmentData.insert(conn, this, strSequence, strKey, vars.getClient(),
                      data[i].adOrgId, vars.getUser(), data[i].description, data[i].mProductId,
                      data[i].cUomId, (qtyIsNegative ? "-" + strConversion : strConversion),
                      data[i].cOrderlineId, strLocator,
                      CreateFromShipmentData.isInvoiced(conn, this, data[i].cInvoicelineId),
                      (qtyIsNegative ? "-" + total : total), data[i].mProductUomId,
                      data[i].mAttributesetinstanceId);
                  if (!strInvoice.equals(""))
                    CreateFromShipmentData.updateInvoice(conn, this, strSequence,
                        data[i].cInvoicelineId);
                  else
                    CreateFromShipmentData.updateInvoiceOrder(conn, this, strSequence,
                        data[i].cOrderlineId);
                } catch (final ServletException ex) {
                  myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                      ex.getMessage());
                  releaseRollbackConnection(conn);
                  return myMessage;
                }
              }
            } else {
              final String strSequence = SequenceIdData.getUUID();
              try {
                CreateFromShipmentData.insert(conn, this, strSequence, strKey, vars.getClient(),
                    data[i].adOrgId, vars.getUser(), data[i].description, data[i].mProductId,
                    data[i].cUomId, data[i].id, data[i].cOrderlineId, strLocator,
                    CreateFromShipmentData.isInvoiced(conn, this, data[i].cInvoicelineId),
                    data[i].quantityorder, data[i].mProductUomId, data[i].mAttributesetinstanceId);
                if (!strInvoice.equals(""))
                  CreateFromShipmentData.updateInvoice(conn, this, strSequence,
                      data[i].cInvoicelineId);
                else
                  CreateFromShipmentData.updateInvoiceOrder(conn, this, strSequence,
                      data[i].cOrderlineId);
              } catch (final ServletException ex) {
                myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
                releaseRollbackConnection(conn);
                return myMessage;
              }
            }
          }
        }

        if (!strPO.equals("")) {
          try {
            final int total = CreateFromShipmentData.deleteC_Order_ID(conn, this, strKey, strPO);
            if (total == 0)
              CreateFromShipmentData.updateC_Order_ID(conn, this, strPO, strKey);
          } catch (final ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
        if (!strInvoice.equals("")) {
          try {
            final int total = CreateFromShipmentData.deleteC_Invoice_ID(conn, this, strKey,
                strInvoice);
            if (total == 0)
              CreateFromShipmentData.updateC_Invoice_ID(conn, this, strInvoice, strKey);
          } catch (final ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }
      releaseCommitConnection(conn);
      if (log4j.isDebugEnabled())
        log4j.debug("Save commit");
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (final Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  protected OBError savePay(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Pay");
    return null;
  }

  protected OBError saveSettlement(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Settlement");
    String strDebtPayment = vars.getInStringParameter("inpcDebtPaymentId", IsIDFilter.instance);
    if (strDebtPayment.equals(""))
      return null;
    OBError myMessage = null;
    Connection conn = null;
    String[] ids = restrictParameter(strDebtPayment);
    try {
      conn = this.getTransactionConnection();
      for (int k = 0; k < ids.length; k++) {
        if (ids[k].startsWith("("))
          ids[k] = ids[k].substring(1, ids[k].length() - 1);
        if (!ids[k].equals("")) {
          ids[k] = Replace.replace(ids[k], "'", "");
          final StringTokenizer st = new StringTokenizer(ids[k], ",", false);
          while (st.hasMoreTokens()) {
            final String strDebtPaymentId = st.nextToken().trim();
            final String strWriteOff = vars.getNumericParameter("inpwriteoff" + strDebtPaymentId);
            final String strIsPaid = vars.getStringParameter("inpispaid" + strDebtPaymentId, "N");
            if (!CreateFromSettlementData.NotIsCancelled(conn, this, strDebtPaymentId)) {
              releaseRollbackConnection(conn);
              log4j.warn("CreateFrom.saveSettlement - debt_payment " + strDebtPaymentId
                  + " is cancelled");
              myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                  "DebtPaymentCancelled");
              return myMessage;
            }
            try {
              CreateFromSettlementData.update(conn, this, vars.getUser(), strKey, strWriteOff,
                  strIsPaid, strDebtPaymentId);
            } catch (final ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }
        }
      }
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (final Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  protected OBError saveDPManagement(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: DPManagement");
    String strDebtPayment = vars.getInStringParameter("inpcDebtPaymentId", IsIDFilter.instance);
    if (strDebtPayment.equals(""))
      return null;
    OBError myMessage = null;
    Connection conn = null;
    String[] ids = restrictParameter(strDebtPayment);
    try {
      conn = this.getTransactionConnection();
      final String strStatusTo = vars.getStringParameter("inpStatusTo");
      for (int k = 0; k < ids.length; k++) {
        if (ids[k].startsWith("("))
          ids[k] = ids[k].substring(1, ids[k].length() - 1);
        if (!ids[k].equals("")) {
          ids[k] = Replace.replace(ids[k], "'", "");
          Integer line = new Integer(CreateFromDPManagementData.getLine(this, strKey));
          final StringTokenizer st = new StringTokenizer(ids[k], ",", false);
          while (st.hasMoreTokens()) {
            final String strDebtPaymentId = st.nextToken().trim();
            if (!CreateFromDPManagementData.NotIsCancelled(conn, this, strDebtPaymentId)) {
              releaseRollbackConnection(conn);
              log4j.warn("CreateFrom.saveSettlement - debt_payment " + strDebtPaymentId
                  + " is cancelled");
              myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                  "DebtPaymentCancelled");
              return myMessage;
            }
            final String strDPManagementLineID = SequenceIdData.getUUID();

            line += 10;
            try {
              CreateFromDPManagementData.insert(conn, this, strDPManagementLineID,
                  vars.getClient(), vars.getUser(), strKey, strStatusTo, line.toString(),
                  strDebtPaymentId);
            } catch (final ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }
        }
      }
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (final Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  protected OBError saveCRemittance(VariablesSecureApp vars, String strKey, String strTableId,
      String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Cremittance");
    String strDebtPayment = vars.getInStringParameter("inpcDebtPaymentId", IsIDFilter.instance);
    if (strDebtPayment.equals(""))
      return null;
    OBError myMessage = null;
    Connection conn = null;
    String[] ids = restrictParameter(strDebtPayment);
    try {
      conn = this.getTransactionConnection();
      Integer lineNo = Integer.valueOf(CreateFromCRemittanceData.selectLineNo(this, strKey))
          .intValue();
      for (int k = 0; k < ids.length; k++) {
        // String strStatusTo = vars.getStringParameter("inpStatusTo");
        if (ids[k].startsWith("("))
          ids[k] = ids[k].substring(1, ids[k].length() - 1);
        if (!ids[k].equals("")) {
          ids[k] = Replace.replace(ids[k], "'", "");
          final StringTokenizer st = new StringTokenizer(ids[k], ",", false);
          while (st.hasMoreTokens()) {
            final String strDebtPaymentId = st.nextToken().trim();

            if (!CreateFromDPManagementData.NotIsCancelled(conn, this, strDebtPaymentId)) {
              releaseRollbackConnection(conn);
              log4j.warn("CreateFrom.saveSettlement - debt_payment " + strDebtPaymentId
                  + " is cancelled");
              myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                  "DebtPaymentCancelled");
              return myMessage;
            }
            final String strCRemittanceLineID = SequenceIdData.getUUID();
            lineNo += 10;
            try {
              CreateFromCRemittanceData.insert(conn, this, strCRemittanceLineID, vars.getClient(),
                  vars.getUser(), strKey, lineNo.toString(), strDebtPaymentId);
            } catch (final ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }
        }
      }
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (final Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  private String[] restrictParameter(String strIds) {
    String[] ids = null;
    if (strIds == null || ("").equals(strIds)) {
      return new String[0];
    }
    strIds = strIds.substring(1, strIds.length() - 1);
    StringTokenizer st = new StringTokenizer(strIds, ",");
    int noOfRecords = 1;
    int tokenCount = st.countTokens();
    final double totalRecords = 900.0;
    int strArrayCount = tokenCount <= totalRecords ? 0 : (int) Math.ceil(tokenCount / totalRecords);
    if (strArrayCount != 0) {
      ids = new String[strArrayCount];
    } else {
      ids = new String[1];
      ids[0] = "(" + strIds + ")";
    }

    int count = 1;
    String tempIds = "";
    if (strArrayCount != 0) {
      while (st.hasMoreTokens()) {
        tempIds = tempIds + st.nextToken();
        if ((noOfRecords % totalRecords) != 0 && st.hasMoreTokens()) {
          tempIds = tempIds + ",";
        }
        if ((noOfRecords % totalRecords) == 0 || (strArrayCount == count && !st.hasMoreTokens())) {
          ids[count - 1] = "(" + tempIds + ")";
          tempIds = "";
          count++;
        }
        noOfRecords++;
      }

    }
    return ids;

  }

  @Override
  public String getServletInfo() {
    return "Servlet that presents the button of CreateFrom";
  } // end of getServletInfo() method
}
