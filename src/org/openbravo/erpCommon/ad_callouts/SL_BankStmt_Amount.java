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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_BankStmt_Amount extends HttpSecureAppServlet {
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
      String strStmAmount = vars.getNumericParameter("inpstmtamt");
      String strChgAmount = vars.getNumericParameter("inpchargeamt");
      String strTrxAmount = vars.getNumericParameter("inptrxamt");
      String strConvChgAmount = vars.getNumericParameter("inpconvertchargeamt");
      String strTabId = vars.getStringParameter("inpTabId");
      String strBankStmtLine = vars.getStringParameter("inpcBankstatementlineId");
      String strCurrencyId = vars.getStringParameter("inpcCurrencyId");
      String strDP = vars.getStringParameter("inpcDebtPaymentId");

      try {
        printPage(response, vars, strChanged, strStmAmount, strTrxAmount, strChgAmount, strTabId,
            strConvChgAmount, strBankStmtLine, strCurrencyId, strDP);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strStmAmount, String strTrxAmount, String strChgAmount, String strTabId,
      String strConChgAmount, String strBankStmtLine, String strCurrencyId, String strDP)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    BigDecimal StmAmount = new BigDecimal(strStmAmount.equals("") ? "0" : strStmAmount);
    BigDecimal TrxAmount = new BigDecimal(strTrxAmount.equals("") ? "0" : strTrxAmount);
    BigDecimal ChgAmount = new BigDecimal(strChgAmount.equals("") ? "0" : strChgAmount);
    BigDecimal ConvChgAmount = new BigDecimal(strConChgAmount.equals("") ? "0" : strConChgAmount);

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_BankStmt_Amount';\n\n");
    resultado.append("var respuesta = new Array(");
    boolean isConversion = false;

    if (!strDP.equals(""))
      isConversion = SLBankStmtAmountData.isConversion(this, strCurrencyId, strDP).equals("Y");

    if (strChanged.equals("inpstmtamt")) {
      if (isConversion) {
        if (log4j.isDebugEnabled())
          log4j.debug("trx: " + TrxAmount.toString() + "chg" + ChgAmount.toString());
        // ConvChgAmount =
        // StmAmount.subtract(TrxAmount).subtract(ChgAmount);
        ConvChgAmount = TrxAmount.subtract(ChgAmount).subtract(StmAmount);
        resultado.append("new Array(\"inpconvertchargeamt\", " + ConvChgAmount.toString() + ")");
        resultado.append(");");
      } else {
        TrxAmount = StmAmount.subtract(ChgAmount);
        resultado.append("new Array(\"inptrxamt\", " + TrxAmount.toString() + ")");
        resultado.append(");");
      }
    }
    if (strChanged.equals("inpchargeamt") || (strChanged.equals("inpconvertchargeamt"))) {
      // StmAmount = TrxAmount.add(ChgAmount).add(ConvChgAmount);
      StmAmount = TrxAmount.subtract(ChgAmount).subtract(ConvChgAmount);
      resultado.append("new Array(\"inpstmtamt\", " + StmAmount.toString() + ")");
      resultado.append(");");
    }

    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
