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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
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

public class SL_Project_Margin extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strTabId = vars.getStringParameter("inpTabId");
      String strcProjectId = vars.getStringParameter("inpcProjectId");
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      // Services
      String strServiceRevenue = vars.getNumericParameter("inpservrevenue", "0");
      String strServiceCost = vars.getNumericParameter("inpservcost", "0");
      String strServiceMargin = vars.getNumericParameter("inpservmargin", "0");
      // Expenses
      String strPlannedExpenses = vars.getNumericParameter("inpexpexpenses", "0");
      String strReinvoicedExpenses = vars.getNumericParameter("inpexpreinvoicing", "0");
      String strPlannedMargin = vars.getNumericParameter("inpexpmargin", "0");
      try {
        printPage(response, vars, strTabId, strcProjectId, strChanged, strServiceRevenue,
            strServiceCost, strServiceMargin, strPlannedExpenses, strReinvoicedExpenses,
            strPlannedMargin);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTabId,
      String strcProjectId, String strChanged, String strServiceRevenue, String strServiceCost,
      String strServiceMargin, String strPlannedExpenses, String strReinvoicedExpenses,
      String strPlannedMargin) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLProjectMarginData[] data = SLProjectMarginData.select(this, strcProjectId);
    String strPrecision = "0";
    if (data != null && data.length > 0) {
      strPrecision = data[0].stdprecision;
    }
    int StdPrecision = Integer.valueOf(strPrecision).intValue();

    BigDecimal serviceRevenue, plannedExpenses, serviceCost, reinvoicedExpenses, serviceMargin, expensesMargin;
    // Services
    serviceRevenue = new BigDecimal(strServiceRevenue); // SR
    serviceCost = new BigDecimal(strServiceCost); // SC
    serviceMargin = new BigDecimal(strServiceMargin); // SM
    // Expenses
    plannedExpenses = new BigDecimal(strPlannedExpenses); // PE
    reinvoicedExpenses = new BigDecimal(strReinvoicedExpenses); // RE
    expensesMargin = new BigDecimal(strPlannedMargin); // EM

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Project_Margin';\n\n");
    resultado.append("var respuesta = new Array(");

    // Services
    if (strChanged.equals("inpservrevenue") || strChanged.equals("inpservcost")) {
      // SM = (SR-SC)*100/SR
      if (serviceRevenue.compareTo(BigDecimal.ZERO) != 0) {
        serviceMargin = (((serviceRevenue.subtract(serviceCost)).divide(serviceRevenue, 12,
            BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100"))).setScale(2,
            BigDecimal.ROUND_HALF_UP);
      } else {
        serviceMargin = BigDecimal.ZERO;
      }
      resultado.append("\n new Array(\"inpservmargin\", " + serviceMargin.toString() + ")");
    }

    if (strChanged.equals("inpservmargin")) {
      // SC = SR*(1-SM/100)
      serviceCost = serviceRevenue.multiply((BigDecimal.ONE).subtract(serviceMargin
          .divide(new BigDecimal("100"))));
      if (serviceCost.scale() > StdPrecision)
        serviceCost = serviceCost.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
      resultado.append("\n new Array(\"inpservcost\", " + serviceCost.toString() + ")\n");
    }

    // Expenses
    if (strChanged.equals("inpexpexpenses") || strChanged.equals("inpexpreinvoicing")) {
      // EM = (RE-PE)*100/RE
      if (reinvoicedExpenses.compareTo(BigDecimal.ZERO) != 0) {
        expensesMargin = (((reinvoicedExpenses.subtract(plannedExpenses)).multiply(new BigDecimal(
            "100"))).divide(reinvoicedExpenses, 12, BigDecimal.ROUND_HALF_EVEN)).setScale(2,
            BigDecimal.ROUND_HALF_UP);
      } else {
        expensesMargin = BigDecimal.ZERO;
      }
      resultado.append("\n new Array(\"inpexpmargin\", " + expensesMargin.toString() + ")");
    }

    if (strChanged.equals("inpexpmargin")) {
      if (expensesMargin.compareTo(new BigDecimal("100")) == 0) {
        // PE = 0 (because EM = 100 %)
        plannedExpenses = BigDecimal.ZERO;
        if (plannedExpenses.scale() > StdPrecision)
          plannedExpenses = plannedExpenses.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
        resultado.append("\n new Array(\"inpexpexpenses\", " + plannedExpenses.toString() + ")\n");
      } else {
        // RE = PE/(1-EM/100)
        reinvoicedExpenses = plannedExpenses.divide((BigDecimal.ONE).subtract(expensesMargin
            .divide(new BigDecimal("100"), 12, BigDecimal.ROUND_HALF_EVEN)));
        if (reinvoicedExpenses.scale() > StdPrecision)
          reinvoicedExpenses = reinvoicedExpenses.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
        resultado.append("\n new Array(\"inpexpreinvoicing\", " + reinvoicedExpenses.toString()
            + ")\n");
      }
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
