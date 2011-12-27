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
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class CashBankOperations extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("SAVE")) {
      String strOrgTrx = vars.getStringParameter("inpadOrgId");
      String strCashFrom = vars.getStringParameter("inpCCashFromID");
      String strCashTo = vars.getStringParameter("inpCCashToID");
      String strBankFrom = vars.getStringParameter("inpCBankAccountFromID");
      String strBankTo = vars.getStringParameter("inpCBankAccountToID");
      String strPaymentRuleFrom = vars.getStringParameter("inppaymentruleFrom");
      String strPaymentRuleTo = vars.getStringParameter("inppaymentruleTo");
      String strAmount = vars.getStringParameter("inpAmount");
      String strMovementDate = vars.getStringParameter("inpmovementdate");
      String strDescription = vars.getStringParameter("inpdescription");
      OBError myMessage = process(vars, strCashFrom, strCashTo, strBankFrom, strBankTo,
          strPaymentRuleFrom, strPaymentRuleTo, strAmount, strMovementDate, strDescription,
          strOrgTrx);
      vars.setMessage("CashBankOperations", myMessage);
      printPage(response, vars);
    } else
      pageErrorPopUp(response);
  }

  private OBError process(VariablesSecureApp vars, String strCashFrom, String strCashTo,
      String strBankFrom, String strBankTo, String strPaymentRuleFrom, String strPaymentRuleTo,
      String strAmount, String strMovementDate, String strDescription, String strOrgTrx) {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: CashBankOperations");
    Connection con = null;
    OBError myMessage = null;
    String strSettlementDocumentNo = "";
    try {
      con = getTransactionConnection();
      String strBPartner = CashBankOperationsData.select(this, strOrgTrx);
      String strCashCurrency = CashBankOperationsData.selectCashCurrency(this,
          strCashFrom.equals("") ? strCashTo : strCashFrom);
      String strBankCurrency = CashBankOperationsData.selectBankCurrency(this,
          strBankFrom.equals("") ? strBankTo : strBankFrom);
      String strSettlement = SequenceIdData.getUUID();
      String strDoctypeId = CashBankOperationsData.selectSettlementDoctypeId(this);
      strSettlementDocumentNo = Utility.getDocumentNo(this, vars, "CashBankOperations",
          "C_Settlement", "", strDoctypeId, false, true);
      if (strCashFrom.equals("") && strBankTo.equals("")) { // bank ->
        // cash
        CashBankOperationsData.insertSettlement(con, this, strSettlement, vars.getClient(),
            strOrgTrx, vars.getUser(), strSettlementDocumentNo, strMovementDate, strDoctypeId,
            strCashCurrency);
        String strDebtPaymentId = SequenceIdData.getUUID();

        CashBankOperationsData.insertDebtpayment(
            con,
            this,
            strDebtPaymentId,
            vars.getClient(),
            strOrgTrx,
            vars.getUser(),
            "Y",
            strSettlement,
            strDescription + " - " + Utility.messageBD(this, "DebtPaymentFor", vars.getLanguage())
                + Utility.messageBD(this, "Cash", vars.getLanguage())
                + CashBankOperationsData.selectCashBook(this, strCashTo), strBPartner,
            strCashCurrency, "", "", strCashTo, strPaymentRuleTo, strAmount, strMovementDate, "");
        insertCash(vars, strCashTo, strAmount, strMovementDate, strCashCurrency, strDescription,
            strDebtPaymentId, strOrgTrx, con);

        strDebtPaymentId = SequenceIdData.getUUID();
        CashBankOperationsData.insertDebtpayment(con, this, strDebtPaymentId, vars.getClient(),
            strOrgTrx, vars.getUser(), "N", strSettlement,
            strDescription + " - " + Utility.messageBD(this, "DebtPaymentFor", vars.getLanguage())
                + CashBankOperationsData.selectBankAccount(this, strBankFrom, vars.getLanguage()),
            strBPartner, strBankCurrency, "", strBankFrom, "", strPaymentRuleTo, strAmount,
            strMovementDate, "");
        CashBankOperationsData.updateSettlement(con, this, strSettlement);
      } else if (strCashTo.equals("") && strBankFrom.equals("")) { // cash
        // ->
        // bank
        CashBankOperationsData.insertSettlement(con, this, strSettlement, vars.getClient(),
            strOrgTrx, vars.getUser(), strSettlementDocumentNo, strMovementDate, strDoctypeId,
            strBankCurrency);
        String strDebtPaymentId = SequenceIdData.getUUID();

        CashBankOperationsData.insertDebtpayment(
            con,
            this,
            strDebtPaymentId,
            vars.getClient(),
            strOrgTrx,
            vars.getUser(),
            "N",
            strSettlement,
            strDescription + " - " + Utility.messageBD(this, "DebtPaymentFor", vars.getLanguage())
                + Utility.messageBD(this, "Cash", vars.getLanguage())
                + CashBankOperationsData.selectCashBook(this, strCashFrom), strBPartner,
            strCashCurrency, "", "", strCashFrom, strPaymentRuleFrom, strAmount, strMovementDate,
            "");

        insertCash(vars, strCashFrom, negate(strAmount), strMovementDate, strCashCurrency,
            strDescription, strDebtPaymentId, strOrgTrx, con);
        strDebtPaymentId = SequenceIdData.getUUID();
        CashBankOperationsData.insertDebtpayment(con, this, strDebtPaymentId, vars.getClient(),
            strOrgTrx, vars.getUser(), "Y", strSettlement,
            strDescription + " - " + Utility.messageBD(this, "DebtPaymentFor", vars.getLanguage())
                + CashBankOperationsData.selectBankAccount(this, strBankTo, vars.getLanguage()),
            strBPartner, strBankCurrency, "", strBankTo, "", strPaymentRuleTo, strAmount,
            strMovementDate, "");
        CashBankOperationsData.updateSettlement(con, this, strSettlement);
      } else if (strBankTo.equals("") && strBankFrom.equals("")) { // cash
        // ->
        // cash
        CashBankOperationsData.insertSettlement(con, this, strSettlement, vars.getClient(),
            strOrgTrx, vars.getUser(), strSettlementDocumentNo, strMovementDate, strDoctypeId,
            strCashCurrency);
        String strDebtPaymentId = SequenceIdData.getUUID();

        CashBankOperationsData.insertDebtpayment(
            con,
            this,
            strDebtPaymentId,
            vars.getClient(),
            strOrgTrx,
            vars.getUser(),
            "N",
            strSettlement,
            strDescription + " - " + Utility.messageBD(this, "DebtPaymentFor", vars.getLanguage())
                + Utility.messageBD(this, "Cash", vars.getLanguage())
                + CashBankOperationsData.selectCashBook(this, strCashFrom), strBPartner,
            strCashCurrency, "", "", strCashFrom, strPaymentRuleFrom, strAmount, strMovementDate,
            "");
        insertCash(vars, strCashFrom, negate(strAmount), strMovementDate, strCashCurrency,
            strDescription, strDebtPaymentId, strOrgTrx, con);

        strDebtPaymentId = SequenceIdData.getUUID();

        CashBankOperationsData.insertDebtpayment(
            con,
            this,
            strDebtPaymentId,
            vars.getClient(),
            strOrgTrx,
            vars.getUser(),
            "Y",
            strSettlement,
            strDescription + " - " + Utility.messageBD(this, "DebtPaymentFor", vars.getLanguage())
                + Utility.messageBD(this, "Cash", vars.getLanguage())
                + CashBankOperationsData.selectCashBook(this, strCashTo), strBPartner,
            strCashCurrency, "", "", strCashTo, strPaymentRuleTo, strAmount, strMovementDate, "");
        insertCash(vars, strCashTo, strAmount, strMovementDate, strCashCurrency, strDescription,
            strDebtPaymentId, strOrgTrx, con);
        CashBankOperationsData.updateSettlement(con, this, strSettlement);
      } else if (strCashTo.equals("") && strCashFrom.equals("")) { // bank
        // ->
        // bank
        CashBankOperationsData.insertSettlement(con, this, strSettlement, vars.getClient(),
            strOrgTrx, vars.getUser(), strSettlementDocumentNo, strMovementDate, strDoctypeId,
            strBankCurrency);
        String strDebtPaymentId = SequenceIdData.getUUID();
        CashBankOperationsData.insertDebtpayment(con, this, strDebtPaymentId, vars.getClient(),
            strOrgTrx, vars.getUser(), "N", strSettlement,
            strDescription + " - " + Utility.messageBD(this, "DebtPaymentFor", vars.getLanguage())
                + CashBankOperationsData.selectBankAccount(this, strBankFrom, vars.getLanguage()),
            strBPartner, strBankCurrency, "", strBankFrom, "", strPaymentRuleFrom, strAmount,
            strMovementDate, "");
        strDebtPaymentId = SequenceIdData.getUUID();
        CashBankOperationsData.insertDebtpayment(con, this, strDebtPaymentId, vars.getClient(),
            strOrgTrx, vars.getUser(), "Y", strSettlement,
            strDescription + " - " + Utility.messageBD(this, "DebtPaymentFor", vars.getLanguage())
                + CashBankOperationsData.selectBankAccount(this, strBankTo, vars.getLanguage()),
            strBPartner, strBankCurrency, "", strBankTo, "", strPaymentRuleTo, strAmount,
            strMovementDate, "");
        CashBankOperationsData.updateSettlement(con, this, strSettlement);
      }
      releaseCommitConnection(con);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "PaymentsSettlementDocNo", vars.getLanguage())
          + "*FT*" + strSettlementDocumentNo);
    } catch (Exception e) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      try {
        releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
      log4j.warn(e);
    }
    return myMessage;
  }

  private String negate(String amount) {
    BigDecimal amt = new BigDecimal(amount);
    amt = amt.multiply(new BigDecimal("-1.0"));
    return amt.toString();
  }

  private String insertCash(VariablesSecureApp vars, String strCashBook, String strAmount,
      String strDate, String strCurrency, String strDescription, String strDPId, String strOrgTrx,
      Connection con) throws ServletException {
    String strCash = CashBankOperationsData.selectOpenCash(this, strCashBook, strDate);
    if (strCash.equals("")) {
      strCash = SequenceIdData.getUUID();
      CashBankOperationsData.insertCash(con, this, strCash, vars.getClient(), strOrgTrx,
          vars.getUser(), strCashBook,
          strDate + " - " + CashBankOperationsData.selectCurrency(this, strCurrency), strDate);
    }
    String strCashLine = SequenceIdData.getUUID();
    CashBankOperationsData.insertCashLine(con, this, strCashLine, vars.getClient(), strOrgTrx,
        vars.getUser(), strCash, strDPId, CashBankOperationsData.selectNextCashLine(this, strCash),
        strDescription, strAmount, strCurrency);
    return strCashLine;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process CashBankOperations");
    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "", strProcessId = "800082";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/CashBankOperations").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CashBankOperations", false, "", "",
        "", false, "ad_process", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("question",
        Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("datedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("datesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter(
        "arrayBank",
        Utility.arrayDobleEntrada(
            "arrBank",
            CashBankOperationsData.selectBankDouble(this,
                Utility.getContext(this, vars, "#User_Org", "CashBankOperations"),
                Utility.getContext(this, vars, "#User_Client", "CashBankOperations"))));
    xmlDocument.setParameter(
        "arrayCash",
        Utility.arrayDobleEntrada(
            "arrCash",
            CashBankOperationsData.selectCashDouble(this, vars.getLanguage(),
                Utility.getContext(this, vars, "#User_Org", "CashBankOperations"),
                Utility.getContext(this, vars, "#User_Client", "CashBankOperations"))));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "AD_Org is transactions allowed", Utility.getContext(this, vars, "#User_Org",
              "CashBankOperations"), Utility.getContext(this, vars, "#User_Client",
              "CashBankOperations"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateAccountingReport", "");
      xmlDocument.setData("reportAD_ORG", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    /*
     * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
     * "C_BankAccount_ID", "", "", Utility.getContext(this, vars, "#User_Org",
     * "CashBankOperations"), Utility.getContext(this, vars, "#User_Client", "CashBankOperations"),
     * 0); Utility.fillSQLParameters(this, vars, null, comboTableData, "CashBankOperations", "");
     * xmlDocument.setData("reportC_BankAccountFrom_ID","liststructure",
     * comboTableData.select(false)); comboTableData = null; } catch (Exception ex) { throw new
     * ServletException(ex); }
     * 
     * 
     * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
     * "C_BankAccount_ID", "", "", Utility.getContext(this, vars, "#User_Org",
     * "CashBankOperations"), Utility.getContext(this, vars, "#User_Client", "CashBankOperations"),
     * 0); Utility.fillSQLParameters(this, vars, null, comboTableData, "CashBankOperations", "");
     * xmlDocument.setData("reportC_BankAccountTo_ID","liststructure",
     * comboTableData.select(false)); comboTableData = null; } catch (Exception ex) { throw new
     * ServletException(ex); }
     * 
     * 
     * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
     * "C_CashBook_ID", "", "", Utility.getContext(this, vars, "#User_Org", "CashBankOperations"),
     * Utility.getContext(this, vars, "#User_Client", "CashBankOperations"), 0);
     * Utility.fillSQLParameters(this, vars, null, comboTableData, "CashBankOperations", "");
     * xmlDocument.setData("reportC_CashFrom_ID","liststructure", comboTableData.select(false));
     * comboTableData = null; } catch (Exception ex) { throw new ServletException(ex); }
     * 
     * 
     * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
     * "C_CashBook_ID", "", "", Utility.getContext(this, vars, "#User_Org", "CashBankOperations"),
     * Utility.getContext(this, vars, "#User_Client", "CashBankOperations"), 0);
     * Utility.fillSQLParameters(this, vars, null, comboTableData, "CashBankOperations", "");
     * xmlDocument.setData("reportC_CashTo_ID","liststructure", comboTableData.select(false));
     * comboTableData = null; } catch (Exception ex) { throw new ServletException(ex); }
     */
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "All_Payment Rule", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "CashBankOperations"), Utility.getContext(this, vars, "#User_Client",
              "CashBankOperations"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CashBankOperations", "");
      xmlDocument.setData("reportPaymentRuleFrom", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "All_Payment Rule", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "CashBankOperations"), Utility.getContext(this, vars, "#User_Client",
              "CashBankOperations"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CashBankOperations", "");
      xmlDocument.setData("reportPaymentRuleTo", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_process.CashBankOperations");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CashBankOperations.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CashBankOperations.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CashBankOperations");
      vars.removeMessage("CashBankOperations");
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

  public String getServletInfo() {
    return "Servlet CashBankOperations";
  } // end of getServletInfo() method
}
