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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
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

public class SL_Conversion_Rate extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = new BigDecimal(0.0);
  private static final BigDecimal ONE = new BigDecimal(1.0);

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
      String strMultiplyRate = vars.getNumericParameter("inpmultiplyrate");
      String strDivideRate = vars.getNumericParameter("inpdividerate");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strChanged, strMultiplyRate, strDivideRate, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strMultiplyRate, String strDivideRate, String strTabId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    BigDecimal divideRate, multiplyRate, rate, one;

    multiplyRate = new BigDecimal(strMultiplyRate);
    divideRate = new BigDecimal(strDivideRate);
    rate = ZERO;
    one = ONE;

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Conversion_Rate';\n\n");
    resultado.append("var respuesta = new Array(");

    if (strChanged.equals("inpmultiplyrate")) {
      if (multiplyRate.compareTo(BigDecimal.ZERO) != 0)
        rate = one.divide(multiplyRate, 12, 4);
      resultado.append("new Array(\"inpdividerate\", " + rate.toString() + ")");

    } else {
      if (divideRate.compareTo(BigDecimal.ZERO) != 0)
        rate = one.divide(divideRate, 12, 4);
      resultado.append("new Array(\"inpmultiplyrate\", " + rate.toString() + ")");
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
