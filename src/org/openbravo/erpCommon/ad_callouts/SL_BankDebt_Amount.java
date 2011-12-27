/*ALO
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
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_BankDebt_Amount extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strDebtPayment = vars.getStringParameter("inpcDebtPaymentId");
      String strBankStatement = vars.getStringParameter("inpcBankstatementId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strCurrency = vars.getStringParameter("inpcCurrencyId");
      String strDescription = vars.getStringParameter("inpdescription");

      try {
        printPage(response, vars, strChanged, strDebtPayment, strTabId, strBankStatement,
            strCurrency, strDescription);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strDebtPayment, String strTabId, String strBankStatement, String strCurrency,
      String strDescription) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    String Amount = null;
    String ConvChargeAmt = "0";
    String conv = null;

    if (!strDebtPayment.equals("")) {
      Amount = SLCashJournalAmountsData.amountDebtPaymentBank(this, strBankStatement,
          strDebtPayment);
      if (!strDescription.equals("")) {
        strDescription = strDescription + " - ";
      }
      strDescription = strDescription
          + SLCashJournalAmountsData.debtPaymentDescription(this, strDebtPayment);
      conv = SLBankStmtAmountData.isConversion(this, strCurrency, strDebtPayment);
    } else {
      Amount = "0";
      // strDescription="";
      conv = "N";
    }

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_BankDebt_Amount';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpdescription\", \"" + FormatUtilities.replaceJS(strDescription)
        + "\"),");
    resultado.append("new Array(\"inptrxamt\", " + Amount + "),");
    resultado.append("new Array(\"inpcurrconv\", \"" + conv + "\"),");
    resultado.append("new Array(\"inpconvertchargeamt\", " + ConvChargeAmt + "),");
    resultado.append("new Array(\"inpstmtamt\", " + Amount + ")");

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
