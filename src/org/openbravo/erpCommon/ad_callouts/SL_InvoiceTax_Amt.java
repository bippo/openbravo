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

public class SL_InvoiceTax_Amt extends HttpSecureAppServlet {
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
      String strTaxAmt = vars.getNumericParameter("inptaxamt");
      String strTaxBaseAmt = vars.getNumericParameter("inptaxbaseamt");
      String strTaxId = vars.getStringParameter("inpcTaxId");
      String strInvoiceId = vars.getStringParameter("inpcInvoiceId");

      try {
        printPage(response, vars, strChanged, strTaxAmt, strTaxBaseAmt, strTaxId, strInvoiceId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strTaxAmt, String strTaxBaseAmt, String strTaxId, String strInvoiceId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();

    // Discount...
    if (strTaxAmt.startsWith("\""))
      strTaxAmt = strTaxAmt.substring(1, strTaxAmt.length() - 1);
    if (strTaxBaseAmt.startsWith("\""))
      strTaxBaseAmt = strTaxBaseAmt.substring(1, strTaxBaseAmt.length() - 1);
    if (strTaxId.startsWith("\""))
      strTaxId = strTaxId.substring(1, strTaxId.length() - 1);
    if (strInvoiceId.startsWith("\""))
      strInvoiceId = strInvoiceId.substring(1, strTaxId.length() - 1);

    SLInvoiceTaxAmtData[] data = SLInvoiceTaxAmtData.select(this, strTaxId, strInvoiceId);

    BigDecimal taxAmt = (strTaxAmt.equals("") ? new BigDecimal(0.0) : new BigDecimal(strTaxAmt));
    BigDecimal taxBaseAmt = (strTaxBaseAmt.equals("") ? new BigDecimal(0.0) : new BigDecimal(
        strTaxBaseAmt));
    BigDecimal taxRate = (data[0].rate.equals("") ? new BigDecimal(1)
        : new BigDecimal(data[0].rate));
    Integer taxScale = new Integer(data[0].priceprecision);

    if (strChanged.equals("inptaxamt")) {
      if (taxRate.compareTo(BigDecimal.ZERO) != 0)
        taxBaseAmt = ((taxAmt.divide(taxRate, 12, BigDecimal.ROUND_HALF_EVEN))
            .multiply(new BigDecimal("100"))).setScale(taxScale, BigDecimal.ROUND_HALF_UP);
    } else {
      taxAmt = ((taxBaseAmt.multiply(taxRate)).divide(new BigDecimal("100"), 12,
          BigDecimal.ROUND_HALF_EVEN)).setScale(taxScale, BigDecimal.ROUND_HALF_UP);
    }

    resultado.append("var calloutName='SL_InvoiceTax_Amt';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inptaxamt\", " + taxAmt.toPlainString() + "),");
    resultado.append("new Array(\"inptaxbaseamt\", " + taxBaseAmt.toPlainString() + "));");

    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
