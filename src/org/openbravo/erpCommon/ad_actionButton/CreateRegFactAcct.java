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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.xmlEngine.XmlDocument;

public class CreateRegFactAcct extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private BigDecimal ExpenseAmtDr = new BigDecimal("0");
  private BigDecimal ExpenseAmtCr = new BigDecimal("0");
  private BigDecimal RevenueAmtDr = new BigDecimal("0");
  private BigDecimal RevenueAmtCr = new BigDecimal("0");

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strKey = vars.getRequiredGlobalVariable("inpcYearId", strWindow + "|C_Year_ID");
      printPage(response, vars, strKey, "", strWindow, strTab, strProcessId);
    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strOrgId = vars.getStringParameter("inpadOrgId", "");
      String strKey = vars.getRequiredGlobalVariable("inpcYearId", strWindow + "|C_Year_ID");
      String strTab = vars.getStringParameter("inpTabId");

      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myError = processButton(vars, strKey, strOrgId, strWindow);
      vars.setMessage(strTab, myError);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private synchronized OBError processButton(VariablesSecureApp vars, String strKey,
      String strOrgId, String windowId) {

    Connection conn = null;
    OBError myError = new OBError();
    try {
      conn = this.getTransactionConnection();
      CreateRegFactAcctData[] dataOrgs = CreateRegFactAcctData.treeOrg(this, vars.getClient(),
          strOrgId);
      CreateRegFactAcctData[] dataOrgAcctSchemas = CreateRegFactAcctData.treeOrgAcctSchemas(this,
          vars.getClient(), strOrgId);
      CreateRegFactAcctData[] acctSchema = CreateRegFactAcctData.treeAcctSchema(this,
          vars.getClient(), strOrgId);
      String strPediodId = CreateRegFactAcctData.getLastPeriod(this, strKey);
      for (int j = 0; j < acctSchema.length; j++) {
        String balanceAmount = CreateRegFactAcctData.balanceAmount(this, strKey, acctSchema[j].id,
            Utility.getInStrSet(new OrganizationStructureProvider().getChildTree(strOrgId, true)));
        if (BigDecimal.ZERO.compareTo(new BigDecimal(balanceAmount)) != 0) {
          releaseRollbackConnection(conn);
          myError.setType("Error");
          myError.setTitle("");
          Map<String, String> parameters = new HashMap<String, String>();
          try {
            OBContext.setAdminMode();
            AcctSchema schema = OBDal.getInstance().get(AcctSchema.class, acctSchema[j].id);
            parameters.put("AcctSchema", schema.getName());
          } finally {
            OBContext.restorePreviousMode();
          }

          myError.setMessage(Utility.parseTranslation(this, vars, parameters, vars.getLanguage(),
              Utility.messageBD(this, "BalanceIsNotBalanced", vars.getLanguage())));
          return myError;
        }
        String strRegId = SequenceIdData.getUUID();
        String strCloseId = SequenceIdData.getUUID();
        String strOpenId = SequenceIdData.getUUID();
        String strDivideUpId = SequenceIdData.getUUID();
        boolean createClosing = OBDal.getInstance().get(AcctSchema.class, acctSchema[j].id)
            .getFinancialMgmtAcctSchemaGLList().size() > 0 ? OBDal.getInstance()
            .get(AcctSchema.class, acctSchema[j].id).getFinancialMgmtAcctSchemaGLList().get(0)
            .isCreateClosing() : true;
        CreateRegFactAcctData[] retainedEarningAccount = CreateRegFactAcctData.retainedearning(
            this, acctSchema[j].id);
        if (retainedEarningAccount == null || retainedEarningAccount.length == 0) {
          strDivideUpId = "";
        }
        for (int i = 0; i < dataOrgs.length; i++) {
          if (log4j.isDebugEnabled())
            log4j.debug("Output: Before buttonReg");
          String regCount = CreateRegFactAcctData.getRegCount(this, vars.getClient(),
              dataOrgs[i].org, acctSchema[j].id, strPediodId);
          if (new Integer(regCount).intValue() > 0) {
            releaseRollbackConnection(conn);
            myError.setType("Error");
            myError.setTitle("");
            myError.setMessage(Utility.messageBD(this, "RegularizationDoneAlready",
                vars.getLanguage()));
            return myError;
          }
          String strRegOut = processButtonReg(conn, vars, strKey, windowId, dataOrgs[i].org,
              strRegId, acctSchema[j].id, strDivideUpId, retainedEarningAccount);
          String strCloseOut = createClosing ? processButtonClose(conn, vars, strKey, windowId,
              dataOrgs[i].org, strCloseId, strOpenId, acctSchema[j].id) : "Success";
          if (!createClosing) {
            strCloseId = "";
            strOpenId = "";
          }
          if (!strRegOut.equals("Success")) {
            releaseRollbackConnection(conn);
            return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
          } else if (!strCloseOut.equals("Success")) {
            return Utility.translateError(this, vars, vars.getLanguage(),
                Utility.messageBD(this, "ProcessRunError_CreateNextPeriod", vars.getLanguage()));
          }
          ExpenseAmtDr = BigDecimal.ZERO;
          ExpenseAmtCr = BigDecimal.ZERO;
          RevenueAmtDr = BigDecimal.ZERO;
          RevenueAmtCr = BigDecimal.ZERO;
        }
        for (int i = 0; i < dataOrgAcctSchemas.length; i++) {
          String strOrgSchemaId = CreateRegFactAcctData.orgAcctschema(this,
              dataOrgAcctSchemas[i].org, acctSchema[j].id);
          if (strOrgSchemaId != null && !strOrgSchemaId.equals("")) {
            if (CreateRegFactAcctData.insertOrgClosing(conn, this, vars.getClient(), strOrgId,
                vars.getUser(), strKey, strOrgSchemaId, strRegId, strCloseId, strDivideUpId,
                strOpenId) == 0)
              return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
          }
        }
        if (CreateRegFactAcctData.updateClose(conn, this, vars.getUser(), strKey, strOrgId) == 0)
          return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
      }

      releaseCommitConnection(conn);
      myError.setType("Success");
      myError.setTitle("");
      myError.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (Exception e) {
      log4j.warn(e);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
    }
    return myError;
  }

  private synchronized String processButtonReg(Connection conn, VariablesSecureApp vars,
      String strKey, String windowId, String stradOrgId, String strID, String strAcctSchema,
      String strDivideUpId, CreateRegFactAcctData[] account2) throws ServletException {
    String Fact_Acct_ID = "";
    String Fact_Acct_Group_ID = strID;
    String strPediodId = CreateRegFactAcctData.getLastPeriod(this, strKey);
    String strRegEntry = Utility.messageBD(this, "RegularizationEntry", vars.getLanguage());
    String currency = CreateRegFactAcctData.cCurrencyId(this, strAcctSchema);
    CreateRegFactAcctData[] totalAmountsExpense = CreateRegFactAcctData.getTotalAmounts(conn, this,
        strKey, "E", stradOrgId, strAcctSchema);
    ExpenseAmtDr = ExpenseAmtDr.add(new BigDecimal(totalAmountsExpense[0].totalamtdr));
    ExpenseAmtCr = ExpenseAmtCr.add(new BigDecimal(totalAmountsExpense[0].totalamtcr));
    CreateRegFactAcctData[] totalAmountsRevenue = CreateRegFactAcctData.getTotalAmounts(conn, this,
        strKey, "R", stradOrgId, strAcctSchema);
    RevenueAmtDr = RevenueAmtDr.add(new BigDecimal(totalAmountsRevenue[0].totalamtdr));
    RevenueAmtCr = RevenueAmtCr.add(new BigDecimal(totalAmountsRevenue[0].totalamtcr));
    // Inserts income summary statement
    CreateRegFactAcctData.insertSelect(conn, this, vars.getClient(), stradOrgId, vars.getUser(),
        CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId, currency,
        Fact_Acct_Group_ID, "10", "R", strRegEntry, strKey, "'E'", strAcctSchema);
    CreateRegFactAcctData.insertSelect(conn, this, vars.getClient(), stradOrgId, vars.getUser(),
        CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId, currency,
        Fact_Acct_Group_ID, "20", "R", strRegEntry, strKey, "'R'", strAcctSchema);
    CreateRegFactAcctData[] account = CreateRegFactAcctData.incomesummary(this, strAcctSchema);
    if (ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr).signum() > 0) {
      Fact_Acct_ID = SequenceIdData.getUUID();
      CreateRegFactAcctData
          .insert(conn, this, Fact_Acct_ID, vars.getClient(), stradOrgId, vars.getUser(),
              strAcctSchema, account[0].accountId,
              CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId,
              CreateRegFactAcctData.adTableId(this), "A",
              CreateRegFactAcctData.cCurrencyId(this, strAcctSchema), "0",
              ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
                  .toString(), "0",
              ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
                  .toString(), Fact_Acct_Group_ID, "10", account[0].name, account[0].value,
              account[0].cBpartnerId, account[0].recordId2, account[0].mProductId,
              account[0].aAssetId, strRegEntry, account[0].cTaxId, account[0].cProjectId,
              account[0].cActivityId, account[0].user1Id, account[0].user2Id,
              account[0].cCampaignId, account[0].cSalesregionId);
    } else if (ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
        .signum() < 0) {
      Fact_Acct_ID = SequenceIdData.getUUID();

      CreateRegFactAcctData.insert(conn, this, Fact_Acct_ID, vars.getClient(), stradOrgId,
          vars.getUser(), strAcctSchema, account[0].accountId,
          CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId,
          CreateRegFactAcctData.adTableId(this), "A",
          CreateRegFactAcctData.cCurrencyId(this, strAcctSchema), ExpenseAmtCr.add(RevenueAmtCr)
              .subtract(RevenueAmtDr).subtract(ExpenseAmtDr).toString(), "0",
          ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr).toString(),
          "0", Fact_Acct_Group_ID, "10", account[0].name, account[0].value, account[0].cBpartnerId,
          account[0].recordId2, account[0].mProductId, account[0].aAssetId, strRegEntry,
          account[0].cTaxId, account[0].cProjectId, account[0].cActivityId, account[0].user1Id,
          account[0].user2Id, account[0].cCampaignId, account[0].cSalesregionId);
    }
    // Inserts retained earning statement
    String strClosingEntry = Utility.messageBD(this, "ClosingEntry", vars.getLanguage());
    if (account2 != null && account2.length > 0) {
      if (ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr).signum() > 0) {
        Fact_Acct_ID = SequenceIdData.getUUID();
        CreateRegFactAcctData
            .insertClose(conn, this, Fact_Acct_ID, vars.getClient(), stradOrgId, vars.getUser(),
                strAcctSchema, account[0].accountId,
                CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId,
                CreateRegFactAcctData.adTableId(this), "A",
                CreateRegFactAcctData.cCurrencyId(this, strAcctSchema),
                ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
                    .toString(), "0", ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr)
                    .subtract(ExpenseAmtCr).toString(), "0", strDivideUpId, "10", "C",
                account[0].name, account[0].value, account[0].cBpartnerId, account[0].recordId2,
                account[0].mProductId, account[0].aAssetId, strClosingEntry, account[0].cTaxId,
                account[0].cProjectId, account[0].cActivityId, account[0].user1Id,
                account[0].user2Id, account[0].cCampaignId, account[0].cSalesregionId);
        Fact_Acct_ID = SequenceIdData.getUUID();
        CreateRegFactAcctData
            .insertClose(conn, this, Fact_Acct_ID, vars.getClient(), stradOrgId, vars.getUser(),
                strAcctSchema, account2[0].accountId,
                CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId,
                CreateRegFactAcctData.adTableId(this), "A",
                CreateRegFactAcctData.cCurrencyId(this, strAcctSchema), "0",
                ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
                    .toString(), "0", ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr)
                    .subtract(ExpenseAmtCr).toString(), strDivideUpId, "10", "C", account2[0].name,
                account2[0].value, account2[0].cBpartnerId, account2[0].recordId2,
                account2[0].mProductId, account2[0].aAssetId, strClosingEntry, account2[0].cTaxId,
                account2[0].cProjectId, account2[0].cActivityId, account2[0].user1Id,
                account2[0].user2Id, account2[0].cCampaignId, account2[0].cSalesregionId);
      } else if (ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
          .signum() < 0) {
        Fact_Acct_ID = SequenceIdData.getUUID();
        CreateRegFactAcctData
            .insertClose(conn, this, Fact_Acct_ID, vars.getClient(), stradOrgId, vars.getUser(),
                strAcctSchema, account[0].accountId,
                CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId,
                CreateRegFactAcctData.adTableId(this), "A",
                CreateRegFactAcctData.cCurrencyId(this, strAcctSchema), "0",
                ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr)
                    .toString(), "0", ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr)
                    .subtract(ExpenseAmtDr).toString(), strDivideUpId, "10", "C", account[0].name,
                account[0].value, account[0].cBpartnerId, account[0].recordId2,
                account[0].mProductId, account[0].aAssetId, strClosingEntry, account[0].cTaxId,
                account[0].cProjectId, account[0].cActivityId, account[0].user1Id,
                account[0].user2Id, account[0].cCampaignId, account[0].cSalesregionId);
        Fact_Acct_ID = SequenceIdData.getUUID();
        CreateRegFactAcctData
            .insertClose(conn, this, Fact_Acct_ID, vars.getClient(), stradOrgId, vars.getUser(),
                strAcctSchema, account2[0].accountId,
                CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId,
                CreateRegFactAcctData.adTableId(this), "A",
                CreateRegFactAcctData.cCurrencyId(this, strAcctSchema),
                ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr)
                    .toString(), "0", ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr)
                    .subtract(ExpenseAmtDr).toString(), "0", strDivideUpId, "10", "C",
                account2[0].name, account2[0].value, account2[0].cBpartnerId,
                account2[0].recordId2, account2[0].mProductId, account2[0].aAssetId,
                strClosingEntry, account2[0].cTaxId, account2[0].cProjectId,
                account2[0].cActivityId, account2[0].user1Id, account2[0].user2Id,
                account2[0].cCampaignId, account2[0].cSalesregionId);
      }
    }
    return "Success";
  }

  private synchronized String processButtonClose(Connection conn, VariablesSecureApp vars,
      String strKey, String windowId, String stradOrgId, String strCloseID, String strOpenID,
      String strAcctSchema) throws ServletException {
    String Fact_Acct_Group_ID = strCloseID;
    String strPediodId = CreateRegFactAcctData.getLastPeriod(this, strKey);
    String newPeriod = CreateRegFactAcctData.getNextPeriod(this, strPediodId);
    String strOpeningEntry = Utility.messageBD(this, "OpeningEntry", vars.getLanguage());
    String strClosingEntry = Utility.messageBD(this, "ClosingEntry", vars.getLanguage());
    if (newPeriod.equals("")) {
      try {
        releaseRollbackConnection(conn);
      } catch (SQLException e) {
        log4j.error("Next Period does not exist", e);
      }
      return "ProcessRunError";
    }

    String currency = CreateRegFactAcctData.cCurrencyId(this, strAcctSchema);

    CreateRegFactAcctData.insertSelect(conn, this, vars.getClient(), stradOrgId, vars.getUser(),
        CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId, currency,
        Fact_Acct_Group_ID, "20", "C", strClosingEntry, strKey, "'A'", strAcctSchema);

    CreateRegFactAcctData.insertSelect(conn, this, vars.getClient(), stradOrgId, vars.getUser(),
        CreateRegFactAcctData.getEndDate(this, strPediodId), strPediodId, currency,
        Fact_Acct_Group_ID, "10", "C", strClosingEntry, strKey, "'L','O'", strAcctSchema);

    String Fact_Acct_Group_ID2 = strOpenID;
    CreateRegFactAcctData.insertSelectOpening(conn, this, vars.getClient(), stradOrgId,
        vars.getUser(), CreateRegFactAcctData.getStartDate(this, newPeriod), newPeriod, currency,
        Fact_Acct_Group_ID2, "20", "O", strOpeningEntry, strKey, "'A','L','O'", strAcctSchema);

    return "Success";
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String strOrgId, String windowId, String strTab, String strProcessId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Create Close Fact Acct");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("es_ES"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);

    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "", "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");

    String strError = "";
    String numSchemasFor0Org = CreateRegFactAcctData.numSchemasFor0Org(this, vars.getClient());
    if (new Integer(numSchemasFor0Org).intValue() > 0) {
      strError = Utility.messageBD(this, "0OrgShouldNotHaveAcctSchema", vars.getLanguage());
    }
    if ("".equals(strError))
      discard[1] = new String("messageBoxID");
    else
      discard[1] = new String("client");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateRegFactAcct", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("question",
        Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    if (!"".equals(strError)) {
      xmlDocument.setParameter("messageType", "ERROR");
      xmlDocument.setParameter("messageTitle", "Error");
      xmlDocument.setParameter("messageMessage", strError);
    }

    xmlDocument.setData(
        "reportadOrgId",
        "liststructure",
        CreateRegFactAcctData.select(this, vars.getClient(),
            Utility.getContext(this, vars, "#User_Org", "CreateRegFactAcct"), strKey));

    xmlDocument.setParameter("adOrgId", strOrgId);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Project close fact acct";
  } // end of getServletInfo() method
}
