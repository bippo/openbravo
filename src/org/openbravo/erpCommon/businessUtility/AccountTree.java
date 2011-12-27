/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2011 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.math.BigDecimal;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;

/**
 * @author Fernando Iriazabal
 * 
 *         This one is the class in charge of the report of accounting
 */
public class AccountTree {
  private static Logger log4j = Logger.getLogger(AccountTree.class);
  private VariablesSecureApp vars;
  private ConnectionProvider conn;
  private AccountTreeData[] accounts;
  private AccountTreeData[] elements;
  private AccountTreeData[] resultantAccounts;
  private String[] elementValueParent;
  // Used to inform if the applySign function has reset to zero the qty values
  // or not
  private boolean resetFlag;
  // True when formsCalculate() calls calculateTree(), and the calculateTree()
  // calls again formsCalculte()
  private boolean recursiveForms = false;

  /**
   * Constructor
   * 
   * @param _vars
   *          VariablesSecureApp object with the session methods.
   * @param _conn
   *          ConnectionProvider object with the connection methods.
   * @param _elements
   *          Array of account's elements.
   * @param _accounts
   *          Array of accounts.
   * @param _elementValueParent
   *          String with the value of the parent element to evaluate.
   * @throws ServletException
   */
  public AccountTree(VariablesSecureApp _vars, ConnectionProvider _conn,
      AccountTreeData[] _elements, AccountTreeData[] _accounts, String _elementValueParent)
      throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("AccountTree []");
    vars = _vars;
    conn = _conn;
    elements = _elements;
    accounts = _accounts;
    elementValueParent = new String[1];
    elementValueParent[0] = _elementValueParent;
    resultantAccounts = updateTreeQuantitiesSign(null, 0, "D");
    // Calculating forms for every elements
    if (resultantAccounts != null && resultantAccounts.length > 0) {
      // forms: Array of accounts with its operands.
      AccountTreeData[] forms = AccountTreeData.selectForms(conn,
          Utility.getContext(conn, vars, "#User_Org", "AccountTree"),
          Utility.getContext(conn, vars, "#User_Client", "AccountTree"));
      resultantAccounts = calculateTree(forms, elementValueParent, new Vector<Object>());
    }
  }

  /**
   * Constructor
   * 
   * @param _vars
   *          VariablesSecureApp object with the session methods.
   * @param _conn
   *          ConnectionProvider object with the connection methods.
   * @param _elements
   *          Array of account's elements (elementValues).
   * @param _accounts
   *          Array of all the fact accts.
   * @param _elementValueParent
   *          Array with the value of the parent elements to evaluate (For example, first expenses
   *          then revenues) Objective tree.
   * @throws ServletException
   */
  public AccountTree(VariablesSecureApp _vars, ConnectionProvider _conn,
      AccountTreeData[] _elements, AccountTreeData[] _accounts, String[] _elementValueParent)
      throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("AccountTree []");
    vars = _vars;
    conn = _conn;
    elements = _elements;
    accounts = _accounts;
    elementValueParent = _elementValueParent;
    // Loading tree with new amounts, applying signs (Debit or Credit) and
    // setting the account level (1, 2, 3,...)
    resultantAccounts = updateTreeQuantitiesSign(null, 0, "D");

    if (resultantAccounts != null && resultantAccounts.length > 0) {
      // Array of accounts with its operands.
      // Calculating forms for every elements
      AccountTreeData[] forms = AccountTreeData.selectForms(conn,
          Utility.getContext(conn, vars, "#User_Org", "AccountTree"),
          Utility.getContext(conn, vars, "#User_Client", "AccountTree"));

      Vector<Object> vec = new Vector<Object>();
      AccountTreeData[] r;

      for (int i = 0; i < elementValueParent.length; i++) {
        r = calculateTree(forms, elementValueParent[i], new Vector<Object>());
        for (int j = 0; j < r.length; j++)
          vec.addElement(r[j]);
      }

      resultantAccounts = new AccountTreeData[vec.size()];
      vec.copyInto(resultantAccounts);
    }
  }

  /**
   * Method to get the processed accounts.
   * 
   * @return Array with the resultant accounts.
   */
  public AccountTreeData[] getAccounts() {
    return resultantAccounts;
  }

  /**
   * Applies the sign to the quantity, according to the showValueCond field
   * 
   * @param qty
   *          BigDecimal value with the quantity to evaluate.
   * @param sign
   *          String with the showValueCond field value.
   * @param isSummary
   *          Boolean that indicates if this is a summary record.
   * @return BigDecimal with the correct sign applied.
   */
  private BigDecimal applySign(BigDecimal qty, String sign, boolean isSummary) {
    // resetFlag will store whether the value has been truncated because of
    // showvaluecond or not
    resetFlag = false;
    BigDecimal total = BigDecimal.ZERO;
    if (isSummary && !sign.equalsIgnoreCase("A")) {
      if (sign.equalsIgnoreCase("P")) {
        if (qty.compareTo(total) > 0) {
          total = qty;
        } else {
          total = BigDecimal.ZERO;
          resetFlag = true;
        }
      } else if (sign.equalsIgnoreCase("N")) {
        if (qty.compareTo(total) < 0) {
          total = qty;
        } else {
          total = BigDecimal.ZERO;
          resetFlag = true;
        }
      } else
        total = qty;
    } else
      total = qty;
    return total;
  }

  /**
   * Update the quantity and the operation quantity fields of the element, depending on the
   * isDebitCredit field.
   * 
   * @param element
   *          AccoutnTreeData object with the element information.
   * @param isDebitCredit
   *          String with the parameter to evaluate if is a Debit or Credit element.
   * @return AccountTreeData object with the new element's information.
   */
  private AccountTreeData setDataQty(AccountTreeData element, String isDebitCredit) {
    if (element == null || accounts == null || accounts.length == 0)
      return element;
    for (int i = 0; i < accounts.length; i++) {
      if (accounts[i].id.equals(element.id)) {
        if (isDebitCredit.equals("C")) {
          accounts[i].qty = accounts[i].qtycredit;
          accounts[i].qtyRef = accounts[i].qtycreditRef;
        }
        element.qtyOperation = accounts[i].qty;
        element.qtyOperationRef = accounts[i].qtyRef;
        BigDecimal bdQty = new BigDecimal(element.qtyOperation);
        BigDecimal bdQtyRef = new BigDecimal(element.qtyOperationRef);
        element.qty = (applySign(bdQty, element.showvaluecond, element.issummary.equals("Y")))
            .toPlainString();
        element.qtyRef = (applySign(bdQtyRef, element.showvaluecond, element.issummary.equals("Y")))
            .toPlainString();
        break;
      }
    }
    return element;
  }

  /**
   * This method updates al the Quantitie's signs of the tree. Is used by the constructor to
   * initializa the element's quantities. Also initializes the level of each account
   * 
   * @param indice
   *          String with the index from which to start updating.
   * @param level
   *          Integer with the level of the elements.
   * @param isDebitCredit
   *          String with the is debit or credit value of the trunk.
   * @return Array of AccountTreeData with the updated tree.
   */
  private AccountTreeData[] updateTreeQuantitiesSign(String indice, int level, String isDebitCredit) {
    if (elements == null || elements.length == 0)
      return elements;
    AccountTreeData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    // if (log4j.isDebugEnabled())
    // log4j.debug("AccountTree.updateTreeQuantitiesSign() - elements: " +
    // elements.length);
    if (indice == null)
      indice = "0";
    for (int i = 0; i < elements.length; i++) {
      if (elements[i].parentId.equals(indice)) {
        isDebitCredit = elements[i].accountsign;
        AccountTreeData[] dataChilds = updateTreeQuantitiesSign(elements[i].nodeId, (level + 1),
            isDebitCredit);
        elements[i].elementLevel = Integer.toString(level);
        elements[i] = setDataQty(elements[i], isDebitCredit);
        vec.addElement(elements[i]);
        if (dataChilds != null && dataChilds.length > 0) {
          for (int j = 0; j < dataChilds.length; j++)
            vec.addElement(dataChilds[j]);
        }
      }
    }
    result = new AccountTreeData[vec.size()];
    vec.copyInto(result);
    return result;
  }

  /**
   * Method to know if an element has form or not.
   * 
   * @param indice
   *          String with the index of the element.
   * @param forms
   *          Array with the existing forms.
   * @return Boolean indicating if has or not form.
   */
  private boolean hasForm(String indice, AccountTreeData[] forms) {
    if (indice == null) {
      log4j.error("AccountTree.hasForm - Missing index");
      return false;
    }
    for (int i = 0; i < forms.length; i++) {
      if (forms[i].id.equals(indice))
        return true;
    }
    return false;
  }

  /**
   * Method to calculate the values with the form's conditions.
   * 
   * @param vecAll
   *          Vector with the evaluated tree.
   * @param forms
   *          Array with the forms.
   * @param indice
   *          String with the index of the element to evaluate.
   * @param vecTotal
   *          Vector with the totals of the operation.
   */
  private void formsCalculate(Vector<Object> vecAll, AccountTreeData[] forms, String indice,
      Vector<Object> vecTotal, boolean isExactValue) {
    if (isExactValue) {
      recursiveForms = true;
    } else {
      recursiveForms = false;
    }
    if (log4j.isDebugEnabled())
      log4j.debug("AccountTree.formsCalculate");
    if (resultantAccounts == null || resultantAccounts.length == 0)
      return;
    if (indice == null) {
      log4j.error("AccountTree.formsCalculate - Missing index");
      return;
    }
    if (vecTotal == null)
      vecTotal = new Vector<Object>();
    if (vecTotal.size() == 0) {
      vecTotal.addElement("0");
      vecTotal.addElement("0");
    }
    BigDecimal total = new BigDecimal((String) vecTotal.elementAt(0));
    BigDecimal totalRef = new BigDecimal((String) vecTotal.elementAt(1));
    boolean encontrado = false;
    for (int i = 0; i < forms.length; i++) {
      if (forms[i].id.equals(indice)) {
        encontrado = false;
        // There exists two options to calculate operands: run through
        // the already processed elements of the report (a) or call
        // calculateTree to obtain amount (b)
        /* (a) */for (int j = 0; j < vecAll.size(); j++) {
          AccountTreeData actual = (AccountTreeData) vecAll.elementAt(j);
          log4j.debug("AccountTree.formsCalculate - actual.nodeId: " + actual.nodeId
              + " - forms[i].nodeId: " + forms[i].nodeId);
          if (actual.nodeId.equals(forms[i].nodeId)) {
            encontrado = true;
            /*
             * 
             * 
             */
            actual.qty = (applySign(new BigDecimal(actual.qtyOperation), actual.showvaluecond,
                actual.issummary.equals("Y"))).toPlainString();
            actual.qtyRef = (applySign(new BigDecimal(actual.qtyOperationRef),
                actual.showvaluecond, actual.issummary.equals("Y"))).toPlainString();
            total = total.add(new BigDecimal(actual.qty).multiply(new BigDecimal(
                forms[i].accountsign)));

            totalRef = totalRef.add(new BigDecimal(actual.qtyRef).multiply(new BigDecimal(
                forms[i].accountsign)));
            /*
             * total += (Double.valueOf(actual.qtyOperation).doubleValue()
             * Double.valueOf(forms[i].accountsign).doubleValue()); totalRef +=
             * (Double.valueOf(actual.qtyOperationRef).doubleValue()
             * Double.valueOf(forms[i].accountsign).doubleValue());
             */
            /*
             * 
             * 
             */
            if (log4j.isDebugEnabled())
              log4j.debug("AccountTree.formsCalculate - C_ElementValue_ID: " + actual.nodeId
                  + " - total: " + total + " - actual.qtyOperation: " + actual.qtyOperation
                  + " - forms[i].accountsign: " + forms[i].accountsign + " - forms.length:"
                  + forms.length);
            break;
          }
        }
        /* (b) */if (!encontrado) {
          if (log4j.isDebugEnabled())
            log4j.debug("AccountTree.formsCalculate - C_ElementValue_ID: " + forms[i].nodeId
                + " not found");
          Vector<Object> vecParcial = new Vector<Object>();
          vecParcial.addElement("0");
          vecParcial.addElement("0");
          calculateTree(forms, forms[i].nodeId, vecParcial, true, true);
          BigDecimal parcial = new BigDecimal((String) vecParcial.elementAt(0));
          BigDecimal parcialRef = new BigDecimal((String) vecParcial.elementAt(1));
          if (log4j.isDebugEnabled())
            log4j.debug("AccountTree.formsCalculate - parcial: " + parcial.toPlainString());
          parcial = parcial.multiply(new BigDecimal(forms[i].accountsign));
          parcialRef = parcialRef.multiply(new BigDecimal(forms[i].accountsign));
          if (log4j.isDebugEnabled())
            log4j.debug("AccountTree.formsCalculate - C_ElementValue_ID: " + forms[i].nodeId
                + " found with value: " + parcial + " account sign: " + forms[i].accountsign);
          total = total.add(parcial);
          totalRef = totalRef.add(parcialRef);
        }
      }
    }
    vecTotal.set(0, total.toPlainString());
    vecTotal.set(1, totalRef.toPlainString());
  }

  /**
   * Main method, which is called by the constructor to evaluate the tree.
   * 
   * @param forms
   *          Array with the forms.
   * @param indice
   *          Array with the start indexes.
   * @param vecTotal
   *          Vector with the accumulated totals.
   * @return Array with the new calculated tree.
   */
  private AccountTreeData[] calculateTree(AccountTreeData[] forms, String[] indice,
      Vector<Object> vecTotal) {
    return calculateTree(forms, indice, vecTotal, true, false);
  }

  /**
   * Main method, which is called by the constructor to evaluate the tree.
   * 
   * @param forms
   *          Array with the forms.
   * @param indice
   *          String with the index of the start element.
   * @param vecTotal
   *          Vector with the accumulated totals.
   * @return Array with the new calculated tree.
   */
  private AccountTreeData[] calculateTree(AccountTreeData[] forms, String indice,
      Vector<Object> vecTotal) {
    String[] i = new String[1];
    i[0] = indice;
    return calculateTree(forms, indice, vecTotal, true, false);
  }

  private boolean nodeIn(String node, String[] listOfNodes) {
    for (int i = 0; i < listOfNodes.length; i++)
      if (node.equals(listOfNodes[i]))
        return true;
    return false;
  }

  /**
   * Main method, which is called by the constructor to evaluate the tree.
   * 
   * @param forms
   *          Array with the forms.
   * @param indice
   *          String with the index of the start element.
   * @param vecTotal
   *          Vector with the accumulated totals.
   * @param applysign
   *          Boolean to know if the sign must be applied or not.
   * @param isExactValue
   *          Boolean auxiliar to use only for the calls from the forms calculating.
   * @return Array with the new calculated tree.
   */
  private AccountTreeData[] calculateTree(AccountTreeData[] forms, String indice,
      Vector<Object> vecTotal, boolean applysign, boolean isExactValue) {
    String[] i = new String[1];
    i[0] = indice;

    return calculateTree(forms, i, vecTotal, applysign, isExactValue);
  }

  /**
   * Main method, which is called by the constructor to evaluate the tree.
   * 
   * @param forms
   *          Array with the forms.
   * @param indice
   *          Array with the start indexes.
   * @param vecTotal
   *          Vector with the accumulated totals.
   * @param applysign
   *          Boolean to know if the sign must be applied or not.
   * @param isExactValue
   *          Boolean auxiliar to use only for the calls from the forms calculating.
   * @return Array with the new calculated tree.
   */
  private AccountTreeData[] calculateTree(AccountTreeData[] forms, String[] indice,
      Vector<Object> vecTotal, boolean applysign, boolean isExactValue) {
    if (resultantAccounts == null || resultantAccounts.length == 0)
      return resultantAccounts;
    if (indice == null) {
      indice = new String[1];
      indice[0] = "0";
    }
    AccountTreeData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    if (log4j.isDebugEnabled())
      log4j.debug("AccountTree.calculateTree() - accounts: " + resultantAccounts.length);
    if (vecTotal == null)
      vecTotal = new Vector<Object>();
    if (vecTotal.size() == 0) {
      vecTotal.addElement("0");
      vecTotal.addElement("0");
    }
    BigDecimal total = new BigDecimal((String) vecTotal.elementAt(0));
    BigDecimal totalRef = new BigDecimal((String) vecTotal.elementAt(1));

    for (int i = 0; i < resultantAccounts.length; i++) {
      if ((isExactValue && nodeIn(resultantAccounts[i].nodeId, indice))
          || (!isExactValue && nodeIn(resultantAccounts[i].parentId, indice))) { // modified by
        // Eduardo Argal.
        // For
        // operands calculation
        AccountTreeData[] dataChilds = null;
        if (resultantAccounts[i].calculated.equals("N")) // this would
        // work if it
        // only passed
        // here once,
        // but it's
        // passing more
        // times...
        // why????
        {
          Vector<Object> vecParcial = new Vector<Object>();
          vecParcial.addElement("0");
          vecParcial.addElement("0");
          @SuppressWarnings("unchecked")
          Vector<Object> vecAux = (Vector<Object>) vec.clone();
          dataChilds = calculateTree(forms, resultantAccounts[i].nodeId, vecParcial);
          if (dataChilds != null && dataChilds.length > 0)
            for (int h = 0; h < dataChilds.length; h++)
              vecAux.addElement(dataChilds[h]);
          if (!hasForm(resultantAccounts[i].nodeId, forms)) {
            BigDecimal parcial = new BigDecimal((String) vecParcial.elementAt(0));
            BigDecimal parcialRef = new BigDecimal((String) vecParcial.elementAt(1));
            resultantAccounts[i].qtyOperation = (new BigDecimal(resultantAccounts[i].qtyOperation)
                .add(parcial)).toPlainString();
            resultantAccounts[i].qtyOperationRef = (new BigDecimal(
                resultantAccounts[i].qtyOperationRef).add(parcialRef)).toPlainString();
            log4j.debug("calculateTree - NothasForm - parcial:" + parcial
                + " - resultantAccounts[i].qtyOperation:" + resultantAccounts[i].qtyOperation
                + " - resultantAccounts[i].nodeId:" + resultantAccounts[i].nodeId);
          } else {
            vecParcial.set(0, "0");
            vecParcial.set(1, "0");
            formsCalculate(vecAux, forms, resultantAccounts[i].nodeId, vecParcial, isExactValue);
            BigDecimal parcial = new BigDecimal((String) vecParcial.elementAt(0));
            BigDecimal parcialRef = new BigDecimal((String) vecParcial.elementAt(1));
            resultantAccounts[i].qtyOperation = (new BigDecimal(resultantAccounts[i].qtyOperation)
                .add(parcial)).toPlainString();
            resultantAccounts[i].qtyOperationRef = (new BigDecimal(
                resultantAccounts[i].qtyOperationRef).add(parcialRef)).toPlainString();
            log4j.debug("calculateTree - HasForm - parcial:" + parcial
                + " - resultantAccounts[i].qtyOperation:" + resultantAccounts[i].qtyOperation
                + " - resultantAccounts[i].nodeId:" + resultantAccounts[i].nodeId);
          }
          String SVC = "";
          if (isExactValue && !recursiveForms) {
            SVC = "A";
          } else {
            SVC = resultantAccounts[i].showvaluecond;
          }
          resultantAccounts[i].qty = (applySign(new BigDecimal(resultantAccounts[i].qtyOperation),
              SVC, resultantAccounts[i].issummary.equals("Y"))).toPlainString();
          if (resetFlag)
            resultantAccounts[i].svcreset = "Y";
          resultantAccounts[i].qtyRef = (applySign(new BigDecimal(
              resultantAccounts[i].qtyOperationRef), SVC,
              resultantAccounts[i].issummary.equals("Y"))).toPlainString();
          if (resetFlag)
            resultantAccounts[i].svcresetref = "Y";
          resultantAccounts[i].calculated = "Y";
        }
        vec.addElement(resultantAccounts[i]);
        if (dataChilds != null && dataChilds.length > 0) {
          for (int j = 0; j < dataChilds.length; j++)
            vec.addElement(dataChilds[j]);
        }
        if (applysign) {
          total = total.add(new BigDecimal(resultantAccounts[i].qty));
          totalRef = totalRef.add(new BigDecimal(resultantAccounts[i].qtyRef));
        } else {
          total = total.add(new BigDecimal(resultantAccounts[i].qtyOperation));
          totalRef = totalRef.add(new BigDecimal(resultantAccounts[i].qtyOperationRef));
        }
      }
    }
    vecTotal.set(0, total.toPlainString());
    vecTotal.set(1, totalRef.toPlainString());
    result = new AccountTreeData[vec.size()];
    vec.copyInto(result);
    return result;
  }

  /**
   * Method to make the level filter of the tree, to eliminate the levels that shouldn't be shown in
   * the report.
   * 
   * @param indice
   *          Array of indexes to evaluate.
   * @param found
   *          Boolean to know if the index has been found
   * @param strLevel
   *          String with the level.
   * @return New Array with the filter applied.
   */
  private AccountTreeData[] levelFilter(String[] indice, boolean found, String strLevel) {
    if (resultantAccounts == null || resultantAccounts.length == 0 || strLevel == null
        || strLevel.equals(""))
      return resultantAccounts;
    AccountTreeData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    if (log4j.isDebugEnabled())
      log4j.debug("AccountTree.levelFilter() - accounts: " + resultantAccounts.length);

    // if (indice == null) indice="0";
    if (indice == null) {
      indice = new String[1];
      indice[0] = "0";
    }
    for (int i = 0; i < resultantAccounts.length; i++) {
      // if (resultantAccounts[i].parentId.equals(indice) && (!found ||
      // resultantAccounts[i].elementlevel.equalsIgnoreCase(strLevel))) {
      if (nodeIn(resultantAccounts[i].parentId, indice)
          && (!found || resultantAccounts[i].elementlevel.equalsIgnoreCase(strLevel))) {
        AccountTreeData[] dataChilds = levelFilter(resultantAccounts[i].nodeId,
            (found || resultantAccounts[i].elementlevel.equals(strLevel)), strLevel);
        if (isAccountLevelLower(strLevel, resultantAccounts[i]))
          vec.addElement(resultantAccounts[i]);
        if (dataChilds != null && dataChilds.length > 0)
          for (int j = 0; j < dataChilds.length; j++)
            if (isAccountLevelLower(strLevel, dataChilds[j]))
              vec.addElement(dataChilds[j]);
      }
    }
    result = new AccountTreeData[vec.size()];
    vec.copyInto(result);
    vec.clear();
    return result;
  }

  /**
   * Method to make the level filter of the tree, to eliminate the levels that shouldn't be shown in
   * the report.
   * 
   * @param indice
   *          String with the index to evaluate.
   * @param found
   *          Boolean to know if the index has been found
   * @param strLevel
   *          String with the level.
   * @return New Array with the filter applied.
   */
  private AccountTreeData[] levelFilter(String indice, boolean found, String strLevel) {
    String[] i = new String[1];
    i[0] = indice;
    if (log4j.isDebugEnabled())
      log4j.debug("AccountTree.levelFilter1");
    return levelFilter(i, found, strLevel);
  }

  /**
   * Method to filter the complete tree to show only the desired levels.
   * 
   * @param indice
   *          Array of start indexes.
   * @param notEmptyLines
   *          Boolean to indicate if the empty lines must been removed.
   * @param strLevel
   *          String with the level.
   * @param isLevel
   *          Boolean not used.
   * @return New Array with the filtered tree.
   */
  public AccountTreeData[] filterStructure(String[] indice, boolean notEmptyLines, String strLevel,
      boolean isLevel) {
    if (log4j.isDebugEnabled())
      log4j.debug("AccountTree.filterStructure() - accounts: " + resultantAccounts.length);
    if (resultantAccounts == null || resultantAccounts.length == 0)
      return resultantAccounts;
    AccountTreeData[] result = null;
    Vector<Object> vec = new Vector<Object>();

    AccountTreeData[] r = levelFilter(indice, false, strLevel);

    for (int i = 0; i < r.length; i++) {
      if (r[i].showelement.equals("Y")) {
        r[i].qty = (applySign(new BigDecimal(r[i].qty), r[i].showvaluecond, true)).toPlainString();
        r[i].qtyRef = (applySign(new BigDecimal(r[i].qtyRef), r[i].showvaluecond, true))
            .toPlainString();
        if ((!notEmptyLines || (new BigDecimal(r[i].qty).compareTo(BigDecimal.ZERO) != 0 || new BigDecimal(
            r[i].qtyRef).compareTo(BigDecimal.ZERO) != 0)) || "Y".equals(r[i].isalwaysshown)) {
          if ("Y".equals(r[i].isalwaysshown)) {
            r[i].qty = null;
            r[i].qtyRef = null;
          }
          vec.addElement(r[i]);
        }
      }
    }
    result = new AccountTreeData[vec.size()];
    vec.copyInto(result);
    return result;
  }

  /**
   * Not used
   * 
   * @param notEmptyLines
   * @param strLevel
   * @param isLevel
   */
  public void filter(boolean notEmptyLines, String strLevel, boolean isLevel) {
    if (log4j.isDebugEnabled())
      log4j.debug("filter");
    if (resultantAccounts == null)
      log4j.warn("No resultant Acct");
    resultantAccounts = filterStructure(elementValueParent, notEmptyLines, strLevel, isLevel);
  }

  /**
   * Resets amounts of subaccounts which parents have been reset because of show value condition
   */
  public void filterSVC() {
    if (log4j.isDebugEnabled())
      log4j.debug("AccountTree.filterShowValueCond() - accounts: " + resultantAccounts.length);
    if (resultantAccounts == null || resultantAccounts.length == 0)
      return;

    int[] levels = new int[2];
    levels[0] = Integer.MAX_VALUE; // Value of the min level flaged as
    // SVCReset
    levels[1] = Integer.MAX_VALUE; // Value of the min level flaged as
    // SVCResetRef

    for (int i = 0; i < resultantAccounts.length; i++) {
      int level = Integer.parseInt(resultantAccounts[i].elementLevel);
      if (resultantAccounts[i].svcreset.equals("Y")) {
        levels[0] = Math.min(level, levels[0]);
      }
      if (resultantAccounts[i].svcresetref.equals("Y")) {
        levels[1] = Math.min(level, levels[1]);
      }
      if (level > levels[0]) {
        resultantAccounts[i].qty = "0.0";
      }
      if (level == levels[0] && resultantAccounts[i].svcreset.equals("N")) {
        levels[0] = Integer.MAX_VALUE;
      }
      if (level > levels[1]) {
        resultantAccounts[i].qtyRef = "0.0";
      }
      if (level == levels[1] && resultantAccounts[i].svcresetref.equals("N")) {
        levels[1] = Integer.MAX_VALUE;
      }
    }
  }

  private boolean isAccountLevelLower(String reportAccountLevel, AccountTreeData accountToBeAdded) {
    if (reportAccountLevel.equalsIgnoreCase("D"))
      if (accountToBeAdded.elementlevel.equalsIgnoreCase("S"))
        return false;

    return true;
  }
}
