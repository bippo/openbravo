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
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class DocTax {
  static Logger log4jDocTax = Logger.getLogger(DocTax.class);

  public DocTax(String C_Tax_ID, String name, String rate, String taxBaseAmt, String amount) {
    this(C_Tax_ID, name, rate, taxBaseAmt, amount, false, false);
  } // DocTax

  public DocTax(String C_Tax_ID, String name, String rate, String taxBaseAmt, String amount,
      boolean isUndeductable, boolean isTaxDeductable) {
    m_C_Tax_ID = C_Tax_ID;
    m_name = name;
    m_rate = rate;
    m_amount = amount;
    m_isTaxUndeductable = isUndeductable;
    m_isTaxDeductable = isTaxDeductable;
  } // DocTax

  /** Tax ID */
  public String m_C_Tax_ID = "";
  /** Amount */
  public String m_amount = "";
  /** Tax Rate */
  public String m_rate = "";
  /** Name */
  public String m_name = "";

  // m_isTaxUndeductable will be used for public organization.
  public boolean m_isTaxUndeductable = false;
  // m_isTaxDeductable will be used for commercial organization in intracommunity.
  public boolean m_isTaxDeductable = false;

  /** Tax Due Acct */
  public static final int ACCTTYPE_TaxDue = 0;
  /** Tax Liability */
  public static final int ACCTTYPE_TaxLiability = 1;
  /** Tax Credit */
  public static final int ACCTTYPE_TaxCredit = 2;
  /** Tax Receivables */
  public static final int ACCTTYPE_TaxReceivables = 3;
  /** Tax Expense */
  public static final int ACCTTYPE_TaxExpense = 4;

  /**
   * Get Account
   * 
   * @param AcctType
   *          see ACCTTYPE_*
   * @param as
   *          account schema
   * @return Account
   */
  public Account getAccount(int AcctType, AcctSchema as, ConnectionProvider conn) {
    if (AcctType < 0 || AcctType > 4)
      return null;
    String validCombination_ID = "";
    DocTaxData[] data = null;
    Account acc = null;
    try {
      data = DocTaxData.select(conn, m_C_Tax_ID, as.m_C_AcctSchema_ID);
      if (data.length > 0) {
        switch (AcctType) {
        case 0:
          validCombination_ID = data[0].tDueAcct;
          break;
        case 1:
          validCombination_ID = data[0].tLiabilityAcct;
          break;
        case 2:
          validCombination_ID = data[0].tCreditAcct;
          break;
        case 3:
          validCombination_ID = data[0].tReceivablesAcct;
          break;
        case 4:
          validCombination_ID = data[0].tExpenseAcct;
          break;
        }
      }
      if (validCombination_ID.equals(""))
        return null;
      acc = Account.getAccount(conn, validCombination_ID);
    } catch (ServletException e) {
      log4jDocTax.warn(e);
    }
    return acc;
  } // getAccount

  /**
   * Get Amount
   * 
   * @return gross amount
   */
  public String getAmount() {
    return m_amount;
  }

}
