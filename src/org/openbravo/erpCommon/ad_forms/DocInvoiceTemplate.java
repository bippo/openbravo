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

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;

public abstract class DocInvoiceTemplate {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   */
  public DocInvoiceTemplate() {
  }

  /**
   * Create Facts (the accounting logic) for ARI, ARC, ARF, API, APC.
   * 
   * <pre>
   *  ARI, ARF
   *      Receivables     DR
   *      Charge                  CR
   *      TaxDue                  CR
   *      Revenue                 CR
   *  ARC
   *      Receivables             CR
   *      Charge          DR
   *      TaxDue          DR
   *      Revenue         RR
   *  API
   *      Payables                CR
   *      Charge          DR
   *      TaxCredit       DR
   *      Expense         DR
   *  APC
   *      Payables        DR
   *      Charge                  CR
   *      TaxCredit               CR
   *      Expense                 CR
   * </pre>
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public abstract Fact createFact(DocInvoice docInvoice, AcctSchema as, ConnectionProvider conn,
      Connection con, VariablesSecureApp vars) throws ServletException;

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
