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
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Project_Planned extends HttpSecureAppServlet {
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
      String strcProjectLineId = vars.getStringParameter("inpcProjectlineId");
      String strPlannedQty = vars.getNumericParameter("inpplannedqty", "0");
      String strPlannedPrice = vars.getNumericParameter("inpplannedprice", "0");
      String strPlannedPurchasePrice = vars.getNumericParameter("inpplannedpoprice", "0");
      String strPlannedMargin = vars.getNumericParameter("inpplannedmarginamt", "0");
      String strTabId = vars.getStringParameter("inpTabId");
      try {
        printPage(response, vars, strPlannedQty, strPlannedPrice, strPlannedPurchasePrice,
            strPlannedMargin, strcProjectLineId, strTabId, strChanged);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strPlannedQty, String strPlannedPrice, String strPlannedPurchasePrice,
      String strPlannedMargin, String strcProjectLineId, String strTabId, String strChanged)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLProjectPlannedAmtData[] data = SLProjectPlannedAmtData.select(this, strcProjectLineId);
    String strPrecision = "0";
    if (data != null && data.length > 0) {
      strPrecision = data[0].stdprecision;
    } else {
      String strcCurrencyId = Utility.getContext(this, vars, "$C_Currency_ID", "");
      strPrecision = SLProjectPlannedAmtData.selectPrecision(this, strcCurrencyId);
    }
    int StdPrecision = Integer.valueOf(strPrecision).intValue();

    BigDecimal plannedAmt, plannedQty, plannedPrice, plannedPurchasePrice, plannedMargin;
    plannedQty = new BigDecimal(strPlannedQty); // PQ
    plannedPrice = (new BigDecimal(strPlannedPrice)); // PP
    plannedPurchasePrice = new BigDecimal(strPlannedPurchasePrice); // PPP
    plannedMargin = (new BigDecimal(strPlannedMargin)); // PM
    plannedAmt = BigDecimal.ZERO; // PA

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Project_Planned';\n\n");
    resultado.append("var respuesta = new Array(");

    if (strChanged.equals("inpplannedqty") || strChanged.equals("inpplannedprice")) {
      // PA = PQ*PP
      plannedAmt = plannedQty.multiply(plannedPrice);
      if (plannedAmt.scale() > StdPrecision)
        plannedAmt = plannedAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
      resultado.append("\n new Array(\"inpplannedamt\", " + plannedAmt.toString() + ")");
    }

    if (strChanged.equals("inpplannedprice") || strChanged.equals("inpplannedpoprice")) {
      // PM = (PP - PPP)*100/PP
      if (plannedPrice.compareTo(BigDecimal.ZERO) != 0) {
        plannedMargin = (((plannedPrice.subtract(plannedPurchasePrice)).multiply(new BigDecimal(
            "100"))).divide(plannedPrice, 12, BigDecimal.ROUND_HALF_EVEN)).setScale(2,
            BigDecimal.ROUND_HALF_UP);
      } else {
        plannedMargin = BigDecimal.ZERO;
      }
      if (strChanged.equals("inpplannedprice")) {
        resultado.append(",");
      }
      resultado.append("\n new Array(\"inpplannedmarginamt\", " + plannedMargin.toString() + ")");
    }

    if (strChanged.equals("inpplannedmarginamt")) {
      // PPP = PP*(1-PM/100)
      plannedPurchasePrice = plannedPrice.multiply((BigDecimal.ONE).subtract(plannedMargin.divide(
          new BigDecimal("100"), 12, BigDecimal.ROUND_HALF_EVEN)));
      if (plannedPurchasePrice.scale() > StdPrecision)
        plannedPurchasePrice = plannedPurchasePrice
            .setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
      resultado.append("\n new Array(\"inpplannedpoprice\", " + plannedPurchasePrice.toString()
          + ")\n");
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
