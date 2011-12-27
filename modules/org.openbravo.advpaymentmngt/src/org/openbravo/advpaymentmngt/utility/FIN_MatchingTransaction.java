/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.utility;

import java.util.List;

import javax.servlet.ServletException;

import org.openbravo.advpaymentmngt.exception.NoAlgorithmFoundException;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public class FIN_MatchingTransaction {
  protected FIN_MatchingAlgorithm algorithm = null;

  public FIN_MatchingTransaction(String _algorithm) throws NoAlgorithmFoundException {
    try {
      if (_algorithm != null && !_algorithm.equals(""))
        this.algorithm = (FIN_MatchingAlgorithm) Class.forName(_algorithm).newInstance();
      else
        throw new NoAlgorithmFoundException(
            "No algorithm has been defined to match bank statement lines");
    } catch (InstantiationException e) {
      throw new NoAlgorithmFoundException(e);
    } catch (IllegalAccessException e) {
      throw new NoAlgorithmFoundException(e);
    } catch (ClassNotFoundException e) {
      throw new NoAlgorithmFoundException(e);
    }
  }

  public FIN_MatchedTransaction match(FIN_BankStatementLine _bankstatementLine,
      List<FIN_FinaccTransaction> excluded) throws ServletException, NoAlgorithmFoundException {
    if (algorithm != null)
      return algorithm.match(_bankstatementLine, excluded);
    else
      throw new NoAlgorithmFoundException(
          "No algorithm has been defined to match bank statement lines");
  }

  public void unmatch(FIN_FinaccTransaction _transaction) throws ServletException,
      NoAlgorithmFoundException {
    if (algorithm != null)
      algorithm.unmatch(_transaction);
    else
      throw new NoAlgorithmFoundException("No algorithm has been defined to unmatch");
    return;
  }
}