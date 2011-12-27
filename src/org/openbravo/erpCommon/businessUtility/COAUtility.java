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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.base.VariablesBase;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationAcctSchema;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaDefault;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaElement;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaGL;
import org.openbravo.model.financialmgmt.accounting.coa.Element;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValue;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValueOperand;
import org.openbravo.model.financialmgmt.calendar.Calendar;
import org.openbravo.model.financialmgmt.calendar.Year;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * @author David Alsasua
 * 
 *         Chart Of Accounts (COA) Utility class
 */
public class COAUtility {
  private static final String strMessageOk = "Success";
  private static final String NEW_LINE = "<br>\n";
  private static final String strMessageError = "Error";
  private static final String ACCT_SCHEMA_ELEMENT_ORGANIZATION = "OO"; // OO AcctSchemaElement
  private static final String ACCT_SCHEMA_ELEMENT_ACCOUNT = "AC"; // AC AcctSchemaElement
  private static final String ACCT_SCHEMA_ELEMENT_PRODUCT = "PR"; // PR AcctSchemaElement
  private static final String ACCT_SCHEMA_ELEMENT_BP = "BP"; // BP AcctSchemaElement
  private static final String ACCT_SCHEMA_ELEMENT_PROJECT = "PJ"; // PJ AcctSchemaElement
  private static final String ACCT_SCHEMA_ELEMENT_CAMPAIGN = "MC"; // MC AcctSchemaElement
  private static final String ACCT_SCHEMA_ELEMENT_SALESREGION = "SR"; // SR AcctSchemaElement
  private HashMap<String, ElementValue> defaultElementValues = new HashMap<String, ElementValue>();
  private Client client;
  private Organization organization;
  private Tree treeAccount;
  private StringBuffer strLog = new StringBuffer();
  private Calendar calendar;
  private Element element;
  private static final Logger log4j = Logger.getLogger(COAUtility.class);

  public COAUtility(Client clientProvided, Tree treeAccountProvided) {
    client = clientProvided;
    organization = null;
    treeAccount = treeAccountProvided;
  }

  public COAUtility(Client clientProvided, Organization orgProvided, Tree treeAccountProvided) {
    client = clientProvided;
    organization = orgProvided;
    treeAccount = treeAccountProvided;
  }

  /**
   * 
   * @param vars
   * @param fileCoA
   * @param hasBPartner
   * @param hasProduct
   * @param hasProject
   * @param hasMCampaign
   * @param hasSRegion
   * @param strAccountString
   *          Translated name of the column Account_ID
   * @param currency
   * @param strCalendarColumnName
   */
  public OBError createAccounting(VariablesSecureApp vars, InputStream fileCoA,
      boolean hasBPartner, boolean hasProduct, boolean hasProject, boolean hasMCampaign,
      boolean hasSRegion, String strAccountString, String strGAAPProvided,
      String strCostingMethodProvided, String strCalendarColumnName, Currency currency) {

    if (client == null || treeAccount == null)
      return logError(
          "@CreateAccountingFailed@",
          "createAccounting() - ERROR - No client or account tree in class attributes! Cannot create accounting.");
    if (fileCoA == null)
      return logError("@CreateAccountingFailed@",
          "createAccounting() - ERROR - A file for the Chart of Accounts must be provided. fileCoA is null!");
    log4j.debug("createAccounting() - Starting the creation of accounting for client "
        + client.getName() + ", with account tree " + treeAccount.getName() + "("
        + treeAccount.getDescription() + ")");

    OBError obeResult = new OBError();
    obeResult.setType(strMessageOk);
    strLog.delete(0, strLog.length());

    String strElementName = client.getName();

    if (organization != null) {
      strElementName = organization.getName() + " " + strAccountString;
    }

    // Initial Organization Setup do NOT create calendar
    if (organization == null) {
      String strCalendarName = strElementName + " " + strCalendarColumnName;
      log4j.debug("createAccounting() - Creating calendar named " + strCalendarName);
      obeResult = insertCalendar(strCalendarName);
      if (!obeResult.getType().equals(strMessageOk))
        return obeResult;
      log4j.debug("createAccounting() - Calendar inserted correctly.");

      log4j.debug("createAccounting() - Inserting year");
      obeResult = insertYear();
      if (!obeResult.getType().equals(strMessageOk))
        return obeResult;
      log4j.debug("createAccounting() - Year correctly inserted");
    }
    log4j.debug("createAccounting() - Inserting element");
    obeResult = insertElement(strElementName, strAccountString);
    if (!obeResult.getType().equals(strMessageOk))
      return obeResult;
    log4j.debug("createAccounting() - Element correctly inserted");

    log4j.debug("createAccounting() - Inserting accounts from file");
    obeResult = insertElementValues(vars, fileCoA);
    if (!obeResult.getType().equals(strMessageOk))
      return obeResult;
    log4j.debug("createAccounting() - Accounts correctly added to database.");
    // Not meaningful message, but set to keep backwards compatibility
    logEvent("@C_ElementValue_ID@ #");

    log4j.debug("createAccounting() - Inserting accounting schema");
    obeResult = insertAccountingSchema(strGAAPProvided, strCostingMethodProvided, currency,
        hasBPartner, hasProduct, hasProject, hasMCampaign, hasSRegion);
    if (!obeResult.getType().equals(strMessageOk))
      return obeResult;
    log4j.debug("createAccounting() - Accounting schema correctly inserted");

    return obeResult;
  }

  private String setAccountType(COAData data) {
    if (data == null)
      return null;
    String accountType = "";
    if (!data.accountType.equals("")) {
      String s = data.accountType.toUpperCase().substring(0, 1);
      if (s.equals("A") || s.equals("L") || s.equals("O") || s.equals("E") || s.equals("R")
          || s.equals("M"))
        accountType = s;
      else
        accountType = "E";
    } else {
      accountType = "E";
    }
    log4j.debug("Account Type: " + accountType);
    return accountType;
  }

  public StringBuffer getLog() {
    return strLog;
  }

  private OBError insertElementValues(VariablesBase vars, InputStream fileCoA) {
    OBError obeResult = new OBError();
    obeResult.setType(strMessageOk);

    COAData[] coa;
    try {
      log4j.debug("insertElementValues() - Reading and parsing provided Chart of Accounts.");
      coa = parseCOA(vars, fileCoA);
    } catch (Exception e) {
      return logError("@CreateAccountingFailed@",
          "insertElementValues() - ERROR - Exception reading file", e);
    }
    log4j.debug("insertElementValues() - Provided Chart of Accounts read and parsed correctly."
        + " Inserting elements read from file to database.");
    if (coa != null && coa.length != 0) {
      try {
        obeResult = insertElementValuesInDB(coa);
        if (!obeResult.getType().equals(strMessageOk))
          return obeResult;
      } catch (Exception e) {
        return logError(
            "@CreateAccountingFailed@",
            "insertElementValues() - Exception when saving element values readen from file into database",
            e);
      }
    } else {
      return logError("@CreateAccountingFailed@", "insertElementValues() - ERROR - File is empty!");
    }
    return obeResult;
  }

  private OBError insertAccountingSchema(String strGAAPProvided, String strCostingMethodProvided,
      Currency currency, boolean hasBPartner, boolean hasProduct, boolean hasProject,
      boolean hasMCampaign, boolean hasSRegion) {
    OBError obeResult = new OBError();
    obeResult.setType(strMessageOk);

    String strGAAP = strGAAPProvided;
    if (strGAAP == null || strGAAP.equals(""))
      strGAAP = "US"; // AD_Reference_ID=123
    String strCostingMethod = strCostingMethodProvided;
    if (strCostingMethod == null || strCostingMethod.equals(""))
      strCostingMethod = "A"; // AD_Reference_ID=122
    String strAcctSchemaName = (organization == null) ? client.getName() : organization.getName()
        + " " + strGAAP + "/" + strCostingMethod + "/" + currency.getDescription();
    log4j.debug("insertAccountingSchema() - Creating accounting schema " + strAcctSchemaName);
    AcctSchema acctSchema = null;
    try {
      acctSchema = InitialSetupUtility.insertAcctSchema(client, organization, currency,
          strAcctSchemaName, strGAAP, strCostingMethod, true);
      if (acctSchema == null)
        return logError("@CreateAccountingFailed@",
            "insertAccountingSchema() - Could not create accounting schema " + strAcctSchemaName);
    } catch (Exception e) {
      return logError("@CreateAccountingFailed@",
          "insertAccountingSchema() - Exception occured while creating accounting schema "
              + strAcctSchemaName, e);
    }
    log4j.debug("insertAccountingSchema() - Retrieving Reference List elements belonging to"
        + " reference list (id=181, name='C_AcctSchema ElementType'");
    List<org.openbravo.model.ad.domain.List> acctSchemaElements;
    try {
      acctSchemaElements = InitialSetupUtility.getAcctSchemaElements();
      if (acctSchemaElements == null || acctSchemaElements.size() == 0)
        return logError("@CreateAccountingFailed@", "insertAccountingSchema() - "
            + "ERROR retrieving the acct.schema elements from reference list with id 181");
    } catch (Exception e) {
      return logError("@CreateAccountingFailed@", "insertAccountingSchema() - "
          + "ERROR retrieving the acct.schema elements from reference list with id 181", e);
    }
    log4j.debug("insertAccountingSchema() - Retrieved " + acctSchemaElements.size()
        + " acct. schema elements");
    AcctSchemaElement organizationAcctSchemaElement = null;
    AcctSchemaElement accountAcctSchemaElement = null;
    AcctSchemaElement productAcctSchemaElement = null;
    AcctSchemaElement bpartnerAcctSchemaElement = null;
    AcctSchemaElement projectAcctSchemaElement = null;
    AcctSchemaElement campaignAcctSchemaElement = null;
    AcctSchemaElement salesregionAcctSchemaElement = null;
    for (Iterator<org.openbravo.model.ad.domain.List> listElements = acctSchemaElements.iterator(); listElements
        .hasNext();) {
      org.openbravo.model.ad.domain.List listElement = listElements.next();

      String strElement = listElement.getSearchKey();

      log4j.debug("insertAccountingSchema() - Creating acct.schema element "
          + listElement.getName() + "(" + listElement.getDescription() + ")");
      if (strElement.equals(ACCT_SCHEMA_ELEMENT_ORGANIZATION)) {
        try {
          organizationAcctSchemaElement = InitialSetupUtility.insertAcctSchemaElement(acctSchema,
              organization, listElement, 10L, true, true, null, element);
        } catch (Exception e) {
          return logError(
              "@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for organization",
              e);
        }
        if (organizationAcctSchemaElement == null)
          return logError("@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for organization");
        logEvent("@C_AcctSchema_Element_ID@=" + listElement.getName());
      }
      if (strElement.equals(ACCT_SCHEMA_ELEMENT_ACCOUNT)) {
        try {
          accountAcctSchemaElement = InitialSetupUtility.insertAcctSchemaElement(acctSchema,
              organization, listElement, 20L, true, false,
              defaultElementValues.get("DEFAULT_ACCT"), element);
        } catch (Exception e) {
          return logError(
              "@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for account",
              e);
        }
        if (accountAcctSchemaElement == null)
          return logError("@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for account");
        logEvent("@C_AcctSchema_Element_ID@=" + listElement.getName());
      }
      if (strElement.equals(ACCT_SCHEMA_ELEMENT_PRODUCT) && hasProduct) {
        try {
          productAcctSchemaElement = InitialSetupUtility.insertAcctSchemaElement(acctSchema,
              organization, listElement, 30L, false, false, null, element);
        } catch (Exception e) {
          return logError(
              "@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for product",
              e);
        }
        if (productAcctSchemaElement == null)
          return logError("@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for product");
        logEvent("@C_AcctSchema_Element_ID@=" + listElement.getName());
      }
      if (strElement.equals(ACCT_SCHEMA_ELEMENT_BP) && hasBPartner) {
        try {
          bpartnerAcctSchemaElement = InitialSetupUtility.insertAcctSchemaElement(acctSchema,
              organization, listElement, 40L, false, false, null, element);
        } catch (Exception e) {
          return logError(
              "@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for business partner",
              e);
        }
        if (bpartnerAcctSchemaElement == null)
          return logError("@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for business partner");
        logEvent("@C_AcctSchema_Element_ID@=" + listElement.getName());
      }
      if (strElement.equals(ACCT_SCHEMA_ELEMENT_PROJECT) && hasProject) {
        try {
          projectAcctSchemaElement = InitialSetupUtility.insertAcctSchemaElement(acctSchema,
              organization, listElement, 50L, false, false, null, element);
        } catch (Exception e) {
          return logError("@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for project");
        }
        if (projectAcctSchemaElement == null)
          return logError("@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for project");
        logEvent("@C_AcctSchema_Element_ID@=" + listElement.getName());
      }
      if (strElement.equals(ACCT_SCHEMA_ELEMENT_CAMPAIGN) && hasMCampaign) {
        try {
          campaignAcctSchemaElement = InitialSetupUtility.insertAcctSchemaElement(acctSchema,
              organization, listElement, 60L, false, false, null, element);
        } catch (Exception e) {
          return logError(
              "@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for campaign",
              e);
        }
        if (campaignAcctSchemaElement == null)
          return logError("@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for campaign");
        logEvent("@C_AcctSchema_Element_ID@=" + listElement.getName());
      }
      if (strElement.equals(ACCT_SCHEMA_ELEMENT_SALESREGION) && hasSRegion) {
        try {
          salesregionAcctSchemaElement = InitialSetupUtility.insertAcctSchemaElement(acctSchema,
              organization, listElement, 70L, false, false, null, element);
        } catch (Exception e) {
          return logError(
              "@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for sales region",
              e);
        }
        if (salesregionAcctSchemaElement == null)
          return logError("@CreateAccountingFailed@",
              "insertAccountingSchema() - Error while creating accounting schema element for sales region");
        logEvent("@C_AcctSchema_Element_ID@=" + listElement.getName());
      }
    }
    log4j.debug("insertAccountingSchema() - All acct.schema elements correctly inserted."
        + " Inserting acct.schema gl account combinations");
    AcctSchemaGL acctSchGL;
    try {
      acctSchGL = InitialSetupUtility.insertAcctSchemaGL(defaultElementValues, acctSchema);
    } catch (Exception e) {
      return logError("@CreateAccountingFailed@",
          "insertAccountingSchema() - Exception while creating acct. schema G/L entry in database",
          e);
    }
    if (acctSchGL == null)
      return logError("@CreateAccountingFailed@",
          "insertAccountingSchema() - ERROR while creating acct. schema G/L entry in database");

    log4j.debug("insertAccountingSchema() - Acct.schema gl account combinations correctly "
        + "inserted. Inserting acct.schema defaults");
    AcctSchemaDefault acctSchDef;
    try {
      acctSchDef = InitialSetupUtility.insertAcctSchemaDefault(defaultElementValues, acctSchema);
    } catch (Exception e) {
      return logError(
          "@CreateAccountingFailed@",
          "insertAccountingSchema() - Exception while creating acct. schema defaults entry in database",
          e);
    }
    if (acctSchDef == null)
      return logError("@CreateAccountingFailed@",
          "insertAccountingSchema() - Exception while creating acct. schema defaults entry in database");
    log4j.debug("insertAccountingSchema() - Acct.schema defaults correctly iserted.");

    log4j.debug("insertAccountingSchema() - Inserting Organization Accounting Schema record.");
    OrganizationAcctSchema orgAcctSchema = null;
    if (organization != null && organization.getId() != "0") {
      try {
        orgAcctSchema = InitialSetupUtility.insertOrgAcctSchema(client, acctSchema, organization);
      } catch (Exception e) {
        return logError(
            "@CreateAccountingFailed@",
            "insertAccountingSchema() - Exception while creating organization - acct. schema entry",
            e);
      }

      if (orgAcctSchema == null)
        return logError("@CreateAccountingFailed@",
            "insertAccountingSchema() - Exception while creating organization - acct. schema entry");
    }
    return obeResult;
  }

  private COAData[] parseCOA(VariablesBase vars, InputStream instFile) throws IOException {
    log4j.debug("parseCOA() - Parsing chart of acconts file provided."
        + " A COAData object is created.");
    COAData coa;
    if (vars == null)
      coa = new COAData(instFile, true, "C");
    else
      coa = new COAData(vars, instFile, true, "C");
    log4j.debug("parseCOA() - A COAData object correctly created."
        + " Parsing the data readen from the file.");
    return parseData(coa.getFieldProvider());
  }

  private COAData[] parseData(FieldProvider[] data) {
    if (data == null)
      return null;
    log4j.debug("parseData() - Parsing " + data.length + " elements read from file.");
    COAData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    for (int i = 0; i < data.length; i++) {
      log4j.debug("parseData() - Processing element " + data[i].getField("accountValue"));
      COAData dataAux = new COAData();
      dataAux.setAccountValue(data[i].getField("accountValue"));
      log4j.debug("parseData() - dataAux.accountValue: " + dataAux.getAccountValue());
      dataAux.setAccountName(data[i].getField("accountName"));
      log4j.debug("parseData() - dataAux.accountName: " + dataAux.getAccountName());
      dataAux.setAccountDescription(data[i].getField("accountDescription"));
      log4j.debug("parseData() - dataAux.accountDescription: " + dataAux.getAccountDescription());
      dataAux.setAccountType(data[i].getField("accountType"));
      log4j.debug("parseData() - dataAux.accountType: " + dataAux.getAccountType());
      dataAux.setAccountSign(data[i].getField("accountSign"));
      log4j.debug("parseData() - dataAux.accountSign: " + dataAux.getAccountSign());
      dataAux.setAccountDocument(data[i].getField("accountDocument"));
      log4j.debug("parseData() - dataAux.accountDocument: " + dataAux.getAccountDocument());
      dataAux.setAccountSummary(data[i].getField("accountSummary"));
      log4j.debug("parseData() - dataAux.accountSummary: " + dataAux.getAccountSummary());
      dataAux.setDefaultAccount(data[i].getField("defaultAccount"));
      log4j.debug("parseData() - dataAux.defaultAccount: " + dataAux.getDefaultAccount());
      dataAux.setAccountParent(data[i].getField("accountParent"));
      log4j.debug("parseData() - dataAux.accountParent: " + dataAux.getAccountParent());
      dataAux.setElementLevel(data[i].getField("elementLevel"));
      log4j.debug("parseData() - dataAux.elementLevel: " + dataAux.getElementLevel());
      dataAux.setBalanceSheet(data[i].getField("balanceSheet"));
      log4j.debug("parseData() - dataAux.operands: " + dataAux.getOperands());
      dataAux.setOperands(data[i].getField("operands"));
      log4j.debug("parseData() - dataAux.balanceSheet: " + dataAux.getBalanceSheet());
      dataAux.setBalanceSheetName(data[i].getField("balanceSheetName"));
      log4j.debug("parseData() - dataAux.balanceSheetName: " + dataAux.getBalanceSheetName());
      dataAux.setUS1120BalanceSheet(data[i].getField("uS1120BalanceSheet"));
      log4j.debug("parseData() - dataAux.uS1120BalanceSheet: " + dataAux.getUS1120BalanceSheet());
      dataAux.setUS1120BalanceSheetName(data[i].getField("uS1120BalanceSheetName"));
      log4j.debug("parseData() - dataAux.uS1120BalanceSheetName: "
          + dataAux.getUS1120BalanceSheetName());
      dataAux.setProfitAndLoss(data[i].getField("profitAndLoss"));
      log4j.debug("parseData() - dataAux.profitAndLoss: " + dataAux.getProfitAndLoss());
      dataAux.setProfitAndLossName(data[i].getField("profitAndLossName"));
      log4j.debug("parseData() - dataAux.profitAndLossName: " + dataAux.getProfitAndLossName());
      dataAux.setUS1120IncomeStatement(data[i].getField("uS1120IncomeStatement"));
      log4j.debug("parseData() - dataAux.uS1120IncomeStatement: "
          + dataAux.getUS1120IncomeStatement());
      dataAux.setUS1120IncomeStatementName(data[i].getField("uS1120IncomeStatementName"));
      log4j.debug("parseData() - dataAux.uS1120IncomeStatementName: "
          + dataAux.getUS1120IncomeStatementName());
      dataAux.setCashFlow(data[i].getField("cashFlow"));
      log4j.debug("parseData() - dataAux.cashFlow: " + dataAux.getCashFlow());
      dataAux.setCashFlowName(data[i].getField("cashFlowName"));
      log4j.debug("parseData() - dataAux.cashFlowName: " + dataAux.getCashFlowName());
      dataAux.setCElementValueId(data[i].getField("cElementValueId"));
      log4j.debug("parseData() - dataAux.cElementValueId: " + dataAux.getCElementValueId());
      vec.addElement(dataAux);

      dataAux.setShowValueCond(data[i].getField("showValueCond"));
      log4j.debug("parseData() - showValueCond: " + dataAux.getShowValueCond());
      dataAux.setTitleNode(data[i].getField("titleNode"));
      log4j.debug("parseData() - dataAux.accountValue: " + dataAux.getTitleNode());

    }
    log4j.debug("parseData() - All elements processed correctly.");
    result = new COAData[vec.size()];
    vec.copyInto(result);
    return result;
  }

  /**
   * Adds a message to the log to be returned
   * 
   * @param strMessage
   *          Message to be added to the log returned (will be translated)
   */
  private void logEvent(String strMessage) {
    strLog.append(strMessage).append(NEW_LINE);
    log4j.debug(strMessage);
  }

  /**
   * This functions registers an error occurred in any of the functions of the class
   * 
   * @param strMessage
   *          Message to be shown in the title of the returned web page (will be translated)
   * @param strLogError
   *          Message to be added to the log4j (not translated)
   * @param e
   *          Exception: optional parameter, just in case the error was caused by an exception
   *          raised
   */
  private OBError logError(String strMessage, String strLogError, Exception e) {
    OBError obeResult = new OBError();
    obeResult.setType(strMessageError);
    obeResult.setTitle(strMessage);
    strLog = new StringBuffer(strMessage);
    if (strLogError != null)
      log4j.error(strLogError);

    if (e != null) {
      log4j.error("Exception ", e);
      logEvent(e.getMessage());
    }
    try {
      OBDal.getInstance().rollbackAndClose();
    } catch (Exception ex) {
      log4j.error("Exception executing rollback ", ex);
      logEvent(ex.getMessage());
    }
    return obeResult;
  }

  private OBError logError(String strMessage, String strLogError) {
    return logError(strMessage, strLogError, null);
  }

  private OBError insertCalendar(String strCalendarName) {
    if (client == null)
      return logError("@CreateClientFailed@",
          "insertTrees() - ERROR - No client in class attribute client! Cannot insert calendar.");
    OBError obeResult = new OBError();
    obeResult.setType(strMessageOk);

    log4j.debug("insertCalendar() - Creating calendar named " + strCalendarName);
    try {
      calendar = InitialSetupUtility.insertCalendar(client, organization, strCalendarName);
      if (calendar == null) {
        return logError("@CalendarNotInserted@",
            "insertCalendar() - ERROR - Calendar NOT inserted; calendar : " + strCalendarName);
      }
    } catch (Exception e) {
      return logError("@CalendarNotInserted@",
          "insertCalendar() - ERROR - Calendar NOT inserted; calendar : " + strCalendarName, e);
    }
    log4j.debug("insertCalendar() - Calendar correctly inserted.");
    logEvent("@C_Calendar_ID@=" + strCalendarName);

    return obeResult;
  }

  private OBError insertYear() {
    if (client == null || calendar == null)
      return logError("@CreateClientFailed@",
          "insertYear() - ERROR - No client or calendar in class attribute! Cannot insert year.");
    OBError obeResult = new OBError();
    obeResult.setType(strMessageOk);

    try {
      String strYearName = Utility.formatDate(new Date(), "yyyy").toString();
      log4j.debug("insertYear() - Inserting year " + strYearName + " in calendar "
          + calendar.getName());
      Year year = InitialSetupUtility.insertYear(client, organization, calendar, strYearName);
      if (year == null) {
        return logError("@YearNotInserted@", "insertYear() - ERROR - Year NOT inserted");
      }
    } catch (Exception e) {
      return logError("@YearNotInserted@", "insertYear() - ERROR - Year NOT inserted", e);
    }
    log4j.debug("insertYear() - Year correctly inserted.");

    return obeResult;
  }

  /**
   * 
   * @param strClientName
   * @param strAccountString
   *          Translated name of the column Account_ID
   * @return
   */
  private OBError insertElement(String strClientName, String strAccountString) {

    if (client == null || treeAccount == null)
      return logError("@CreateClientFailed@",
          "insertElement() - ERROR - No client or treeAccount in class attributes! Cannot create element.");

    OBError obeResult = new OBError();
    obeResult.setType(strMessageOk);

    String strElementName = strClientName + " " + strAccountString;
    log4j.debug("insertElement() - Inserting element " + strElementName);
    try {
      element = InitialSetupUtility.insertElement(client, organization, strElementName,
          treeAccount, true);
      if (element == null) {
        return logError("@ElementNotInserted@",
            "insertElement() - ERROR - Acct Element NOT inserted");
      }
    } catch (Exception e) {
      return logError("@ElementNotInserted@",
          "insertElement() - ERROR - Acct Element NOT inserted", e);
    }
    log4j.debug("insertElement() - Element inserted correctly " + element.getName());
    logEvent("@C_Element_ID@=" + strElementName);

    return obeResult;
  }

  private OBError insertElementValuesInDB(COAData[] data) {
    OBError obeResult = new OBError();
    obeResult.setType(strMessageOk);
    if (data == null)
      return logError("@CreateAccountingFailed@",
          "insertElementValuesInDB() - No element provided to be inserted in database");
    log4j.debug("insertElementValuesInDB() - Inserting " + data.length + " elements into database");
    // mapElementValueId while store the link c_elementvalue_id <-> value
    HashMap<String, String> mapElementValueValue = new HashMap<String, String>();
    // mapElementValueId while store the link value <-> c_elementvalue_id
    HashMap<String, String> mapElementValueId = new HashMap<String, String>();
    // mapParent while store the link value <-> value of the parent
    HashMap<String, String> mapParent = new HashMap<String, String>();
    // mapSequence will store the link value <-> sequence that must be assigned to that node
    // in the ADTreeNode. To start with, root node (value=0) has sequence 0
    HashMap<String, Long> mapSequence = new HashMap<String, Long>();
    mapSequence.put("0", 0L);
    // mapChildSequence will store the link value <-> last sequence assigned to a son-node. To start
    // with, root node (value=0) is initialized to 0
    HashMap<String, Long> mapChildSequence = new HashMap<String, Long>();
    mapChildSequence.put("0", 0L);

    for (int i = 0; i < data.length; i++) {
      log4j.debug("insertElementValuesInDB() - Procesing element in position " + i
          + " with default account " + data[i].getDefaultAccount());

      Boolean IsDocControlled = data[i].getAccountDocument().startsWith("Y");
      Boolean IsSummary = data[i].getAccountSummary().startsWith("Y");
      String C_ElementValue_ID = data[i].getCElementValueId();
      String accountType = setAccountType(data[i]);
      String showValueCond = data[i].getShowValueCond();
      String titleNode = data[i].getTitleNode();
      OBContext.setAdminMode();
      try {
        String language = OBContext.getOBContext().getLanguage().getLanguage();

        if (!"".equals(showValueCond) && !showValueCond.startsWith("P")
            && !showValueCond.startsWith("N") && !showValueCond.startsWith("A")) {
          showValueCond = null;
          logEvent(String
              .format(Utility.messageBD(new DalConnectionProvider(),
                  "ValueIgnoredImportingAccount", language), data[i].getAccountValue(),
                  "ShowValueCond"));
        }

        if (!"".equals(titleNode) && !titleNode.startsWith("Y") && !titleNode.startsWith("N")) {
          titleNode = null;
          logEvent(String.format(Utility.messageBD(new DalConnectionProvider(),
              "ValueIgnoredImportingAccount", language), data[i].getAccountValue(), "TitleNode"));
        }

      } finally {
        OBContext.restorePreviousMode();
      }

      if (accountType == null) {
        logError("@CreateAccountingFailed@",
            "insertElementValuesInDB() - Account type could not be stablished for account "
                + data[i].getAccountName());
      }
      String accountSign = setAccountSign(data[i]);

      log4j.debug("insertElementValuesInDB() - Adding Account: Value: " + data[i].getAccountValue()
          + ". Name: " + data[i].getAccountName() + ". Default: " + data[i].getDefaultAccount()
          + ". C_ElementValue_ID: " + C_ElementValue_ID + ". C_Element_ID: " + element.getId()
          + ". Value: " + data[i].getAccountValue() + ". Name: " + data[i].getAccountName()
          + ". Description: " + data[i].getAccountDescription());

      if (data[i].getAccountValue().equals("")) {
        logEvent("@AccountNotAdded@. @Name@:" + data[i].getAccountName() + ". @Description@: "
            + data[i].getAccountDescription());
        data[i].setCElementValueId("");
      } else {
        log4j.debug("insertElementValuesInDB() - Inserting element value in database");
        ElementValue elementValue = null;
        try {
          elementValue = InitialSetupUtility.insertElementValue(element, organization,
              data[i].getAccountName(), data[i].getAccountValue(), data[i].getAccountDescription(),
              accountType, accountSign, IsDocControlled, IsSummary, data[i].getElementLevel(),
              false, showValueCond, titleNode);
        } catch (Exception e) {
          return logError(
              "@CreateAccountingFailed@",
              "insertElementValuesInDB() - Not inserted account with value: "
                  + data[i].getAccountValue(), e);
        }
        if (elementValue == null) {
          return logError(
              "@CreateAccountingFailed@",
              "insertElementValuesInDB() - Not inserted account with value: "
                  + data[i].getAccountValue());
        }
        if (!data[i].getDefaultAccount().equals(""))
          defaultElementValues.put(data[i].getDefaultAccount(), elementValue);

        mapElementValueValue.put(elementValue.getId(), elementValue.getSearchKey());
        mapElementValueId.put(elementValue.getSearchKey(), elementValue.getId());

        log4j
            .debug("insertElementValuesInDB() - Element value correctly inserted. Figuring out the correct sequence number.");

        Long lSequence = 10L;
        String strParent = data[i].getAccountParent();
        if (strParent.equals(""))
          strParent = "0";
        if (mapChildSequence.containsKey(strParent)) {
          lSequence = mapChildSequence.get(strParent) + 10;
          mapChildSequence.put(strParent, lSequence);
          mapChildSequence.put(data[i].getAccountValue(), 0L);
          mapSequence.put(data[i].getAccountValue(), lSequence);
          mapParent.put(data[i].getAccountValue(), strParent);
        } else {
          logEvent("@ParentNotFound@ " + data[i].getAccountValue() + " = "
              + data[i].getAccountParent());
        }
        log4j.debug("insertElementValues() - Sequence for the element value: " + lSequence);
      }
    }
    OBDal.getInstance().flush();

    log4j
        .debug("insertElementValuesInDB() - All accounts processed correctly. Updating tree node.");
    List<TreeNode> lTreeNodes = null;
    try {
      lTreeNodes = InitialSetupUtility.getTreeNode(treeAccount, client, organization);
      if (lTreeNodes == null)
        logEvent("@AccountTreeNotSorted@");
      else {
        log4j.debug("insertElementValuesInDB() - Read from database " + lTreeNodes.size()
            + " ADTreeNode elements. Updating tree.");
        InitialSetupUtility.updateAccountTree(lTreeNodes, mapSequence, mapElementValueValue,
            mapElementValueId, mapParent, false);
        log4j.debug("insertElementValuesInDB() - Account tree updated.");
        try {
          OBContext.setAdminMode();
          OBDal.getInstance().flush();
        } finally {
          OBContext.restorePreviousMode();
        }
      }
    } catch (Exception e) {
      logEvent("@AccountTreeNotSorted@");
    }
    log4j
        .debug("insertElementValuesInDB() - All accounts inserted correctly in database. Updating operands.");
    return updateOperands(data);
  }

  private String setAccountSign(COAData data) {
    String accountSign = "";
    if (!data.accountSign.equals("")) {
      String s = data.accountSign.toUpperCase().substring(0, 1);
      if (s.equals("D") || s.equals("C"))
        accountSign = s;
      else
        accountSign = "N";
    } else
      accountSign = "N";
    log4j.debug("AccountSign: " + accountSign);
    return accountSign;
  }

  private OBError updateOperands(COAData[] data) {
    log4j.debug("updateOperands() - Updating operands for " + data.length
        + " element values inserted.");
    OBError obeResult = new OBError();
    obeResult.setType(strMessageOk);

    for (int i = 0; i < data.length; i++) {
      log4j.debug("updateOperands() - Procesing account in position " + i + ": "
          + data[i].accountValue);
      String[][] strOperand = operandProcess(data[i].operands);
      String strSeqNo = "10";
      try {
        ElementValue elementValue = InitialSetupUtility.getElementValue(element,
            data[i].accountValue);
        for (int j = 0; strOperand != null && j < strOperand.length; j++) {
          ElementValue operand = InitialSetupUtility.getElementValue(element, strOperand[j][0]);
          if (elementValue != null && operand != null) {
            log4j.debug("updateOperands() - Procesing operand " + strOperand[j][0]
                + ", of the account " + data[i].accountValue);
            ElementValueOperand operandElement = InitialSetupUtility.insertOperand(operand,
                elementValue, new Long((strOperand[j][1].equals("+") ? "1" : "-1")), new Long(
                    strSeqNo));
            strSeqNo = nextSeqNo(strSeqNo);
            if (operandElement == null)
              logEvent("@OperandNotInserted@. @Account_ID@ = " + data[i].accountValue
                  + " - @Account_ID@ = " + strOperand[j][0]);
          } else {
            logEvent("Operand not inserted: Account = " + data[i].accountValue + " - Operand = "
                + strOperand[j][0]);
            log4j.error("Operand not inserted - Value = " + strOperand[j][0]);
          }
        }
      } catch (Exception e) {
        return logError("@OperandNotInserted@", "updateOperands() - ERROR - ", e);
      }
    }
    return obeResult;
  }// updateOperands()

  private String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    String SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }

  private String[][] operandProcess(String strOperand) {
    if (strOperand == null || strOperand.equals(""))
      return null;
    StringTokenizer st = new StringTokenizer(strOperand, "+-", true);
    StringTokenizer stNo = new StringTokenizer(strOperand, "+-", false);
    int no = stNo.countTokens();
    String[][] strResult = new String[no][2];
    no = 0; // Token No
    int i = 0; // Array position
    strResult[0][1] = "+";
    while (st.hasMoreTokens()) {
      if (i % 2 != 1) {
        strResult[no][0] = st.nextToken().trim();
        no++;
      } else
        strResult[no][1] = st.nextToken().trim();
      i++;
    }
    // strResult = filterArray(strResult);
    return strResult;
  }
}
