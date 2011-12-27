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

import org.apache.log4j.Logger;
import org.openbravo.base.MultipartRequest;
import org.openbravo.base.VariablesBase;
import org.openbravo.data.FieldProvider;

/**
 * @author David Alsasua
 * 
 *         Chart Of Accounts (COA) Data class
 */
public class COAData extends MultipartRequest implements FieldProvider {
  private static final Logger log4j = Logger.getLogger(COAData.class);
  String accountValue = "";
  String accountName = "";
  String accountDescription = "";
  String accountType = "";
  String accountSign = "";
  String accountDocument = "";
  String accountSummary = "";
  String defaultAccount = "";
  String accountParent = "";
  String elementLevel = "";
  String operands = "";
  String balanceSheet = "";
  String balanceSheetName = "";
  String uS1120BalanceSheet = "";
  String uS1120BalanceSheetName = "";
  String showValueCond = "";
  String titleNode = "";

  public void setAccountValue(String accountValue) {
    this.accountValue = accountValue;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public void setAccountDescription(String accountDescription) {
    this.accountDescription = accountDescription;
  }

  public void setAccountType(String accountType) {
    this.accountType = accountType;
  }

  public void setAccountSign(String accountSign) {
    this.accountSign = accountSign;
  }

  public void setAccountDocument(String accountDocument) {
    this.accountDocument = accountDocument;
  }

  public void setAccountSummary(String accountSummary) {
    this.accountSummary = accountSummary;
  }

  public void setDefaultAccount(String defaultAccount) {
    this.defaultAccount = defaultAccount;
  }

  public void setAccountParent(String accountParent) {
    this.accountParent = accountParent;
  }

  public void setElementLevel(String elementLevel) {
    this.elementLevel = elementLevel;
  }

  public void setOperands(String operands) {
    this.operands = operands;
  }

  public void setBalanceSheet(String balanceSheet) {
    this.balanceSheet = balanceSheet;
  }

  public void setBalanceSheetName(String balanceSheetName) {
    this.balanceSheetName = balanceSheetName;
  }

  public void setUS1120BalanceSheet(String balanceSheet) {
    uS1120BalanceSheet = balanceSheet;
  }

  public void setUS1120BalanceSheetName(String balanceSheetName) {
    uS1120BalanceSheetName = balanceSheetName;
  }

  public void setProfitAndLoss(String profitAndLoss) {
    this.profitAndLoss = profitAndLoss;
  }

  public void setProfitAndLossName(String profitAndLossName) {
    this.profitAndLossName = profitAndLossName;
  }

  public void setUS1120IncomeStatement(String incomeStatement) {
    uS1120IncomeStatement = incomeStatement;
  }

  public void setUS1120IncomeStatementName(String incomeStatementName) {
    uS1120IncomeStatementName = incomeStatementName;
  }

  public void setCashFlow(String cashFlow) {
    this.cashFlow = cashFlow;
  }

  public void setCashFlowName(String cashFlowName) {
    this.cashFlowName = cashFlowName;
  }

  public void setShowValueCond(String showValueCond) {
    this.showValueCond = showValueCond;
  }

  public void setTitleNode(String titleNode) {
    this.titleNode = titleNode;
  }

  String profitAndLoss = "";
  String profitAndLossName = "";
  String uS1120IncomeStatement = "";
  String uS1120IncomeStatementName = "";
  String cashFlow = "";
  String cashFlowName = "";
  String cElementValueId = "";

  public COAData() {
  }

  public COAData(VariablesBase _vars, String _filename, boolean _firstLineHeads, String _format)
      throws IOException {
    super(_vars, _filename, _firstLineHeads, _format, null);
  }

  public COAData(VariablesBase _vars, InputStream _in, boolean _firstLineHeads, String _format)
      throws IOException {
    super(_vars, _in, _firstLineHeads, _format, null);
  }

  public COAData(InputStream _in, boolean _firstLineHeads, String _format) throws IOException {
    super(_in, _firstLineHeads, _format, null);
  }

  public String getAccountValue() {
    return accountValue;
  }

  public String getAccountName() {
    return accountName;
  }

  public String getAccountDescription() {
    return accountDescription;
  }

  public String getAccountType() {
    return accountType;
  }

  public String getAccountSign() {
    return accountSign;
  }

  public String getAccountDocument() {
    return accountDocument;
  }

  public String getAccountSummary() {
    return accountSummary;
  }

  public String getDefaultAccount() {
    return defaultAccount;
  }

  public String getAccountParent() {
    return accountParent;
  }

  public String getElementLevel() {
    return elementLevel;
  }

  public String getOperands() {
    return operands;
  }

  public String getBalanceSheet() {
    return balanceSheet;
  }

  public String getBalanceSheetName() {
    return balanceSheetName;
  }

  public String getUS1120BalanceSheet() {
    return uS1120BalanceSheet;
  }

  public String getUS1120BalanceSheetName() {
    return uS1120BalanceSheetName;
  }

  public String getProfitAndLoss() {
    return profitAndLoss;
  }

  public String getProfitAndLossName() {
    return profitAndLossName;
  }

  public String getUS1120IncomeStatement() {
    return uS1120IncomeStatement;
  }

  public String getUS1120IncomeStatementName() {
    return uS1120IncomeStatementName;
  }

  public String getCashFlow() {
    return cashFlow;
  }

  public String getCashFlowName() {
    return cashFlowName;
  }

  public String getCElementValueId() {
    return cElementValueId;
  }

  public String getShowValueCond() {
    return showValueCond;
  }

  public String getTitleNode() {
    return titleNode;
  }

  public void setCElementValueId(String elementValueId) {
    cElementValueId = elementValueId;
  }

  public String getField(String fieldName) {
    if (fieldName == null) {
      log4j.debug("COAData - getField - Field is null");
      return null;
    }
    if (fieldName.equalsIgnoreCase("ACCOUNT_VALUE") || fieldName.equals("accountValue"))
      return accountValue;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_NAME") || fieldName.equals("accountName"))
      return accountName;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_DESCRIPTION")
        || fieldName.equals("accountDescription"))
      return accountDescription;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_TYPE") || fieldName.equals("accountType"))
      return accountType;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_SIGN") || fieldName.equals("accountSign"))
      return accountSign;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_DOCUMENT") || fieldName.equals("accountDocument"))
      return accountDocument;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_SUMMARY") || fieldName.equals("accountSummary"))
      return accountSummary;
    else if (fieldName.equalsIgnoreCase("DEFAULT_ACCOUNT") || fieldName.equals("defaultAccount"))
      return defaultAccount;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_PARENT") || fieldName.equals("accountParent"))
      return accountParent;
    else if (fieldName.equalsIgnoreCase("ELEMENT_LEVEL") || fieldName.equals("elementLevel"))
      return elementLevel;
    else if (fieldName.equalsIgnoreCase("OPERANDS") || fieldName.equals("operands"))
      return operands.trim();
    else if (fieldName.equalsIgnoreCase("BALANCE_SHEET") || fieldName.equals("balanceSheet"))
      return balanceSheet;
    else if (fieldName.equalsIgnoreCase("BALANCE_SHEET_NAME")
        || fieldName.equals("balanceSheetName"))
      return balanceSheetName;
    else if (fieldName.equalsIgnoreCase("US_1120_BALANCE_SHEET")
        || fieldName.equals("uS1120BalanceSheet"))
      return uS1120BalanceSheet;
    else if (fieldName.equalsIgnoreCase("US_1120_BALANCE_SHEET_NAME")
        || fieldName.equals("uS1120BalanceSheetName"))
      return uS1120BalanceSheetName;
    else if (fieldName.equalsIgnoreCase("PROFIT_AND_LOSS") || fieldName.equals("profitAndLoss"))
      return profitAndLoss;
    else if (fieldName.equalsIgnoreCase("PROFIT_AND_LOSS_NAME")
        || fieldName.equals("profitAndLossName"))
      return profitAndLossName;
    else if (fieldName.equalsIgnoreCase("US_1120_INCOME_STATEMENT")
        || fieldName.equals("uS1120IncomeStatement"))
      return uS1120IncomeStatement;
    else if (fieldName.equalsIgnoreCase("US_1120_INCOME_STATEMENT_NAME")
        || fieldName.equals("uS1120IncomeStatementName"))
      return uS1120IncomeStatementName;
    else if (fieldName.equalsIgnoreCase("CASH_FLOW") || fieldName.equals("cashFlow"))
      return cashFlow;
    else if (fieldName.equalsIgnoreCase("CASH_FLOW_NAME") || fieldName.equals("cashFlowName"))
      return cashFlowName;
    else if (fieldName.equalsIgnoreCase("C_ELEMENT_VALUE_ID")
        || fieldName.equalsIgnoreCase("CELEMENTVALUEID"))
      return cElementValueId;
    else if (fieldName.equalsIgnoreCase("SHOW_VALUE_COND")
        || fieldName.equalsIgnoreCase("showValueCond"))
      return showValueCond;
    else if (fieldName.equalsIgnoreCase("TITLE_NODE") || fieldName.equalsIgnoreCase("titleNode"))
      return titleNode;
    else {
      log4j.debug("COAData - getField - Field does not exist: " + fieldName);
      return null;
    }
  }

  public FieldProvider lineFixedSize(String linea) {
    return null;
  }

  public FieldProvider lineSeparatorFormated(String line) {
    if (line == null)
      return null;
    if (line.length() < 1)
      return null;
    COAData coaData = new COAData();
    int next = 0;
    int previous = 0;
    String text = "";
    for (int i = 0; i < 21; i++) {
      if (next >= line.length())
        break;
      if ((previous + 1) < line.length() && line.substring(previous, previous + 1).equals("\"")) {
        int aux = line.indexOf("\"", previous + 1);
        if (aux != -1)
          next = aux;
      }
      next = line.indexOf(",", next + 1);
      if (next == -1)
        next = line.length();
      text = line.substring(previous, next);
      if (text.length() > 0) {
        if (text.charAt(0) == '"')
          text = text.substring(1);
        if (text.charAt(text.length() - 1) == '"')
          text = text.substring(0, text.length() - 1);
      }
      log4j.debug("COAData - lineSeparatorFormated - i: " + i);
      log4j.debug("COAData - lineSeparatorFormated - text: " + text);
      switch (i) {
      case 0:
        coaData.setAccountValue(text);
        break;
      case 1:
        coaData.setAccountName(text);
        break;
      case 2:
        coaData.setAccountDescription(text);
        break;
      case 3:
        coaData.setAccountType(text);
        break;
      case 4:
        coaData.setAccountSign(text);
        break;
      case 5:
        coaData.setAccountDocument(text);
        break;
      case 6:
        coaData.setAccountSummary(text);
        break;
      case 7:
        coaData.setDefaultAccount(text);
        break;
      case 8:
        coaData.setAccountParent(text);
        break;
      case 9:
        coaData.setElementLevel(text);
        break;
      case 10:
        coaData.setOperands(text);
        break;
      case 11:
        coaData.setShowValueCond(text);
        break;
      case 12:
        coaData.setTitleNode(text);
        break;
      }
      previous = next + 1;
    }
    return coaData;
  }

}
