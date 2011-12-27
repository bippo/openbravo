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
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_JournalLineAmt extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  static final BigDecimal ZERO = new BigDecimal(0.0);

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      String strWindowId = vars.getStringParameter("inpWindowId");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strGLJournal = vars.getRequiredStringParameter("inpglJournalId");
      String strCurrencyRate = vars.getNumericParameter("inpcurrencyrate", "1");
      String strCurrency = vars.getStringParameter("inpcCurrencyId");
      String strDateAcct = vars.getStringParameter("inpdateacct", DateTimeData.today(this));
      String strCurrencyRateType = vars.getStringParameter("inpcurrencyratetype", "S");
      String strAmtSourceDr = vars.getNumericParameter("inpamtsourcedr", "0");
      String strAmtSourceCr = vars.getNumericParameter("inpamtsourcecr", "0");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strGLJournal, strCurrencyRate, strCurrency, strDateAcct,
            strCurrencyRateType, strWindowId, strAmtSourceDr, strAmtSourceCr, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strGLJournal, String strCurrencyRate, String strCurrency, String strDateAcct,
      String strCurrencyRateType, String strWindowId, String strAmtSourceDr, String strAmtSourceCr,
      String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    String strAcctSchema = Utility.getContext(this, vars, "$C_ACCTSCHEMA_ID", strWindowId);
    SLJournalLineAmtData[] data = SLJournalLineAmtData.select(this, strAcctSchema);
    String strPrecision = "2", strTargetCurrencyId = "";
    if (data != null && data.length > 0) {
      strPrecision = data[0].stdprecision.equals("") ? "2" : data[0].stdprecision;
      strTargetCurrencyId = data[0].cCurrencyId.equals("") ? "0" : data[0].cCurrencyId;
    }
    String CurrencyRate = SLJournalLineAmtData.currencyRate(this, strCurrency, strTargetCurrencyId,
        strDateAcct, strCurrencyRateType, vars.getClient(), vars.getOrg());
    if (CurrencyRate.equals(""))
      CurrencyRate = SLJournalLineAmtData.currencyRate2(this, strGLJournal, strCurrency,
          strCurrencyRateType);
    int StdPrecision = Integer.valueOf(strPrecision).intValue();

    BigDecimal AmtSourceDr = new BigDecimal(strAmtSourceDr);
    BigDecimal AmtSourceCr = new BigDecimal(strAmtSourceCr);
    BigDecimal CurrencyRateValue = new BigDecimal(strCurrencyRate);
    CurrencyRateValue = CurrencyRateValue.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
    BigDecimal AmtAcctDr, AmtAcctCr;

    AmtAcctDr = (strAmtSourceDr.equals("") ? ZERO : AmtSourceDr.multiply(new BigDecimal(
        CurrencyRate))).setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
    AmtAcctCr = (strAmtSourceCr.equals("") ? ZERO : AmtSourceCr.multiply(new BigDecimal(
        CurrencyRate))).setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_JournalLineAmt';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpamtacctdr\", " + AmtAcctDr.toString() + "),");
    resultado.append("new Array(\"inpamtacctcr\", " + AmtAcctCr.toString() + "),");
    resultado.append("new Array(\"inpcurrencyrate\", " + CurrencyRateValue.toString() + "),");
    resultado.append("new Array(\"inpcurrencyratetype\", \"" + strCurrencyRateType + "\")");

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
