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
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class SE_Expense_Amount extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strExpenseAmt = vars.getNumericParameter("inpexpenseamt");
      String strDateexpense = vars.getStringParameter("inpdateexpense");
      String strcCurrencyId = vars.getStringParameter("inpcCurrencyId");
      String strTimeExpenseId = vars.getStringParameter("inpsTimeexpenseId");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strExpenseAmt, strDateexpense, strcCurrencyId, strTimeExpenseId,
            strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strExpenseAmt, String strDateexpense, String strcCurrencyId, String strTimeExpenseId,
      String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    String c_Currency_To_ID = Utility.getContext(this, vars, "$C_Currency_ID", "");
    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();

    if (strDateexpense.equals("")) {
      strDateexpense = SEExpenseAmountData.selectReportDate(this, strTimeExpenseId).equals("") ? DateTimeData
          .today(this) : SEExpenseAmountData.selectReportDate(this, strTimeExpenseId);
    }

    BigDecimal amount = null;
    if (!strExpenseAmt.equals("")) {
      amount = new BigDecimal(strExpenseAmt);
    } else {
      amount = new BigDecimal(0.0);
    }
    String strPrecision = "0";
    if (!strcCurrencyId.equals("")) {
      strPrecision = SEExpenseAmountData.selectPrecision(this, strcCurrencyId);
    }
    int stdPrecision = Integer.valueOf(strPrecision).intValue();
    if (amount.scale() > stdPrecision)
      amount = amount.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);

    String convertedAmount = strExpenseAmt;
    BigDecimal convAmount = amount;

    if (!strcCurrencyId.equals(c_Currency_To_ID)) {
      String strPrecisionConv = "0";
      if (!c_Currency_To_ID.equals("")) {
        strPrecisionConv = SEExpenseAmountData.selectPrecision(this, c_Currency_To_ID);
      }
      int stdPrecisionConv = Integer.valueOf(strPrecisionConv).intValue();
      try {
        convertedAmount = SEExpenseAmountData.selectConvertedAmt(this, strExpenseAmt,
            strcCurrencyId, c_Currency_To_ID, strDateexpense, vars.getClient(), vars.getOrg());
      } catch (ServletException e) {
        convertedAmount = "";
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
        strConvRateErrorMsg = myMessage.getMessage();
        log4j.warn("Currency does not exist. Exception:" + e);
      }
      if (!convertedAmount.equals("")) {
        convAmount = new BigDecimal(convertedAmount);
      } else {
        convAmount = BigDecimal.ZERO;
      }
      if (convAmount.scale() > stdPrecisionConv)
        convAmount = convAmount.setScale(stdPrecisionConv, BigDecimal.ROUND_HALF_UP);
    }
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_Expense_Amount';\n\n");
    resultado.append("var respuesta = new Array(");
    if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
      resultado.append("new Array('MESSAGE', \"" + strConvRateErrorMsg + "\"), ");
    }
    resultado.append("new Array(\"inpexpenseamt\", " + amount.toPlainString() + ")");
    resultado.append(", new Array(\"inpconvertedamt\", "
        + (convAmount.compareTo(BigDecimal.ZERO) == 0 ? "\"\"" : convAmount.toPlainString()) + ")");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
