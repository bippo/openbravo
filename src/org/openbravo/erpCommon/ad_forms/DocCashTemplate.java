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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.sql.Connection;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;

public abstract class DocCashTemplate {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocCash = Logger.getLogger(DocInvoice.class);

  /**
   * Constructor
   */
  public DocCashTemplate() {
  }

  /**
   * Create Facts (the accounting logic) for CMC.
   * 
   * <pre>
   *  Expense
   *          CashExpense     DR
   *          CashAsset               CR
   *  Receipt
   *          CashAsset       DR
   *          CashReceipt             CR
   *  Charge
   *          Charge          DR
   *          CashAsset               CR
   * jarenor
   *  G/L Item
   *          Charge          DR
   *          CashAsset               CR
   *  Difference
   *          CashDifference  DR
   *          CashAsset               CR
   *  Invoice
   *          CashAsset       DR
   *          CashTransfer            CR
   *  Transfer
   *          BankInTransit   DR
   *          CashAsset               CR
   * </pre>
   * 
   * @param as
   *          account schema
   * @return Fact
   */
  public abstract Fact createFact(DocCash docCash, AcctSchema as, ConnectionProvider conn,
      Connection con, VariablesSecureApp vars) throws ServletException;

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
