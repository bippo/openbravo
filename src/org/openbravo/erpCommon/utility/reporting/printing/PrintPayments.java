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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.erpCommon.utility.reporting.printing;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.reporting.DocumentType;

@SuppressWarnings("serial")
public class PrintPayments extends PrintController {

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    DocumentType documentType = DocumentType.PAYMENT;
    // The prefix PRINTPAYMENTS is a fixed name based on the KEY of the
    // AD_PROCESS
    String sessionValuePrefix = "PRINTPAYMENTS";
    String strDocumentId = null;

    strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpfinPaymentId_R");
    if (strDocumentId.equals(""))
      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpfinPaymentId");

    post(request, response, vars, documentType, sessionValuePrefix, strDocumentId);
  }

  public String getServletInfo() {
    return "Servlet that processes the print action";
  } // End of getServletInfo() method
}