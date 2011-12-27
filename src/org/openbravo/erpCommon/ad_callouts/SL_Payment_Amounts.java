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
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Payment_Amounts extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final BigDecimal ZERO = new BigDecimal(0.0);

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
      String strIsOverUnderPayment = vars.getStringParameter("inpisoverunderpayment");
      String strPayamt = vars.getNumericParameter("inppayamt");
      String strDiscountamt = vars.getNumericParameter("inpdiscountamt");
      String strWriteoffamt = vars.getNumericParameter("inpwriteoffamt");
      String strOverunderamt = vars.getNumericParameter("inpoverunderamt");
      String strcCurrencyId = vars.getStringParameter("inpcCurrencyId");
      String strcPaymentId = vars.getStringParameter("inpcPaymentId");
      String strcInvoiceId = vars.getStringParameter("inpcInvoiceId");
      String strTabId = vars.getStringParameter("inpTabId");
      try {
        printPage(response, vars, strChanged, strIsOverUnderPayment, strPayamt, strDiscountamt,
            strWriteoffamt, strOverunderamt, strcCurrencyId, strcPaymentId, strcInvoiceId, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }

    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strIsOverUnderPayment, String strPayamt, String strDiscountamt, String strWriteoffamt,
      String strOverunderamt, String strcCurrencyId, String strcPaymentId, String strcInvoiceId,
      String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    BigDecimal payamt, discountamt, writeoffamt, overunderamt, convert, invtotamt;

    payamt = new BigDecimal(strPayamt);
    if (payamt == null)
      payamt = ZERO;
    discountamt = new BigDecimal(strDiscountamt);
    if (discountamt == null)
      discountamt = ZERO;
    writeoffamt = new BigDecimal(strWriteoffamt);
    if (writeoffamt == null)
      writeoffamt = ZERO;
    overunderamt = new BigDecimal(strOverunderamt);
    if (overunderamt == null)
      overunderamt = ZERO;

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Payment_Amounts';\n\n");
    resultado.append("var respuesta = new Array(");
    if (strChanged.equals("inpisoverunderpayment")) {
      overunderamt = new BigDecimal(strOverunderamt);
      if (overunderamt == null)
        overunderamt = ZERO;
      resultado.append("new Array(\"inpoverunderamt\", " + overunderamt.toString() + ")");
    }

    else if (strChanged.equals("inpcCurrencyId")) {

      String strCurrency = vars.getSessionValue("Last.Currency");
      if (log4j.isDebugEnabled())
        log4j.debug("LAST CURRENCY: " + strCurrency);
      if (strCurrency.equals("")) {
        vars.setSessionValue("Last.Currency", strcCurrencyId);
      } else {

        if (strCurrency.equals(strcCurrencyId)) {
        } else {
          String strconvert = SLPaymentAmountsData.selectConversion(this, strCurrency,
              strcCurrencyId, DateTimeData.today(this), null, vars.getClient(), vars.getOrg());
          if (strconvert.equals("")) {
            resultado.append("new Array('MESSAGE', \""
                + FormatUtilities.replaceJS(Utility.messageBD(this, "NoCurrencyConversion",
                    vars.getLanguage())) + "\"),");
            FieldProvider[] tdd = null;
            try {
              ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
                  "C_Currency", "ID", Utility.getContext(this, vars, "#AccessibleOrgTree",
                      "SLPaymentAmounts"), Utility.getContext(this, vars, "#User_Client",
                      "SLPaymentAmounts"), 0);
              Utility.fillSQLParameters(this, vars, null, comboTableData, "SLPaymentAmounts", "");
              tdd = comboTableData.select(false);
              comboTableData = null;
            } catch (Exception ex) {
              throw new ServletException(ex);
            }

            resultado.append("new Array(\"inpcCurrencyId\", ");
            if (tdd != null && tdd.length > 0) {
              resultado.append("new Array(");
              for (int i = 0; i < tdd.length; i++) {
                resultado.append("new Array(\"" + tdd[i].getField("id") + "\", \""
                    + FormatUtilities.replaceJS(tdd[i].getField("name")) + "\", \""
                    + (tdd[i].getField("id").equalsIgnoreCase(strCurrency) ? "true" : "false")
                    + "\")");
                if (i < tdd.length - 1)
                  resultado.append(",\n");
              }
              resultado.append("\n)");
            } else
              resultado.append("null");
            resultado.append("\n)");
          } else {
            convert = new BigDecimal(strconvert);
            payamt = payamt.multiply(convert).setScale(2, 4);
            resultado.append("new Array(\"inppayamt\", " + payamt.toString() + "),");
            discountamt = discountamt.multiply(convert).setScale(2, 4);
            resultado.append("new Array(\"inpdiscountamt\", " + discountamt.toString() + "),");
            writeoffamt = writeoffamt.multiply(convert).setScale(2, 4);
            resultado.append("new Array(\"inpwriteoffamt\", " + writeoffamt.toString() + "),");
            overunderamt = overunderamt.multiply(convert).setScale(2, 4);
            resultado.append("new Array(\"inpoverunderamt\", " + overunderamt.toString() + ")");
            vars.setSessionValue("Last.Currency", strcCurrencyId);
          }

        }

      }

    } else if (strChanged.equals("inppayamt")) {
      SLPaymentAmountsData[] data = SLPaymentAmountsData.select(this, strcInvoiceId);
      if (log4j.isDebugEnabled())
        log4j.debug("PAYMENT: " + data[0].grand + "CURRENCYID: " + data[0].currencyid);
      invtotamt = new BigDecimal(data[0].grand);
      if (invtotamt == null)
        invtotamt = ZERO;
      if (strcCurrencyId.equals(data[0].currencyid)) {
      } else {
        String strconvert = SLPaymentAmountsData.selectConversion(this, data[0].currencyid,
            strcCurrencyId, DateTimeData.today(this), null, vars.getClient(), vars.getOrg());
        convert = new BigDecimal(strconvert);
        invtotamt = invtotamt.multiply(convert).setScale(2, 4);
      }
      writeoffamt = (invtotamt).subtract(payamt).subtract(discountamt).subtract(overunderamt);
      resultado.append("new Array(\"inpwriteoffamt\", " + writeoffamt.toString() + ")");
    } else if (strChanged.equals("inpdiscountamt")) {
      SLPaymentAmountsData[] data = SLPaymentAmountsData.select(this, strcInvoiceId);
      invtotamt = new BigDecimal(data[0].grand);
      if (invtotamt == null)
        invtotamt = ZERO;
      if (strcCurrencyId.equals(data[0].currencyid)) {
      } else {
        String strconvert = SLPaymentAmountsData.selectConversion(this, data[0].currencyid,
            strcCurrencyId, DateTimeData.today(this), null, vars.getClient(), vars.getOrg());
        convert = new BigDecimal(strconvert);
        invtotamt = invtotamt.multiply(convert).setScale(2, 4);
      }
      invtotamt = invtotamt.subtract(discountamt).subtract(writeoffamt).subtract(overunderamt);
      resultado.append("new Array(\"inppayamt\", " + invtotamt.toString() + ")");
    } else if (strChanged.equals("inpwriteoffamt")) {
      SLPaymentAmountsData[] data = SLPaymentAmountsData.select(this, strcInvoiceId);
      invtotamt = new BigDecimal(data[0].grand);
      if (invtotamt == null)
        invtotamt = ZERO;
      if (strcCurrencyId.equals(data[0].currencyid)) {
      } else {
        String strconvert = SLPaymentAmountsData.selectConversion(this, data[0].currencyid,
            strcCurrencyId, DateTimeData.today(this), null, vars.getClient(), vars.getOrg());
        convert = new BigDecimal(strconvert);
        invtotamt = invtotamt.multiply(convert).setScale(2, 4);
      }
      invtotamt = invtotamt.subtract(writeoffamt).subtract(discountamt).subtract(overunderamt);
      resultado.append("new Array(\"inppayamt\", " + invtotamt.toString() + ")");
    } else if (strChanged.equals("inpoverunderamt")) {
      SLPaymentAmountsData[] data = SLPaymentAmountsData.select(this, strcInvoiceId);
      invtotamt = new BigDecimal(data[0].grand);
      if (invtotamt == null)
        invtotamt = ZERO;
      invtotamt = invtotamt.subtract(overunderamt).subtract(writeoffamt).subtract(discountamt);
      resultado.append("new Array(\"inppayamt\", " + invtotamt.toString() + ")");
    }
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }
}
