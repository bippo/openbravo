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

public class SL_RequisitionLine_Amt extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final BigDecimal ZERO = new BigDecimal("0");

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
      String strQty = vars.getNumericParameter("inpqty");
      String strPriceActual = vars.getNumericParameter("inppriceactual");
      String strPriceList = vars.getNumericParameter("inppricelist");
      String strDiscount = vars.getNumericParameter("inpdiscount");
      try {
        printPage(response, vars, strQty, strPriceActual, strDiscount, strPriceList, strChanged);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else {
      pageError(response);
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strQty,
      String strPriceActual, String strDiscount, String strPriceList, String strChanged)
      throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    String strLineNetAmt = "";
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_RequisitionLine_Amt';\n\n");
    resultado.append("var respuesta = new Array(");

    if (!strPriceActual.equals("")) {

      BigDecimal qty, LineNetAmt, priceActual, discount, priceList;

      String strRequisition = vars.getStringParameter("inpmRequisitionId");
      SLRequisitionLineAmtData[] data = SLRequisitionLineAmtData.select(this, strRequisition);

      Integer stdPrecision = null;
      Integer pricePrecision = null;

      if (data != null && data.length > 0) {
        stdPrecision = Integer.valueOf(data[0].stdprecision);
        pricePrecision = Integer.valueOf(data[0].priceprecision);
      }
      priceActual = strPriceActual.equals("") ? ZERO : internalRound(
          new BigDecimal(strPriceActual), pricePrecision);
      discount = (strDiscount.equals("") ? ZERO : new BigDecimal(strDiscount));
      priceList = (strPriceList.equals("") ? ZERO : new BigDecimal(strPriceList));
      qty = (strQty.equals("") ? ZERO : new BigDecimal(strQty));

      // calculating discount
      if (strChanged.equals("inppricelist") || strChanged.equals("inppriceactual")) {
        if (priceList.compareTo(ZERO) == 0) {
          discount = ZERO;
        } else {
          if (log4j.isDebugEnabled())
            log4j.debug("pricelist:" + Double.toString(priceList.doubleValue()));
          if (log4j.isDebugEnabled())
            log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));
          discount = ((priceList.subtract(priceActual)).divide(priceList, 12,
              BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100"));
        }

        if (log4j.isDebugEnabled())
          log4j.debug("Discount: " + discount.toString());
        discount = internalRound(discount, stdPrecision);
        if (log4j.isDebugEnabled())
          log4j.debug("Discount rounded: " + discount.toString());

        if (!strDiscount.equals(discount.toString())) {
          resultado.append("new Array(\"inpdiscount\", " + discount.toString() + "),");
        }
      } else if (strChanged.equals("inpdiscount")) { // calculate std and
        // actual
        BigDecimal discount1 = null;
        if (priceList.compareTo(ZERO) != 0) {
          discount1 = internalRound(((priceList.subtract(priceActual)).divide(priceList, 12,
              BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100")), stdPrecision);
        } else {
          discount1 = new BigDecimal(0);
        }
        BigDecimal discount2 = internalRound(discount, stdPrecision);
        if (discount1.compareTo(discount2) != 0) { // checks if rounded
          // discount has changed
          priceActual = priceList.subtract(priceList.multiply(discount).divide(
              new BigDecimal("100"), 12, BigDecimal.ROUND_HALF_EVEN));
          priceActual = internalRound(priceActual, pricePrecision);
          resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");
        }
      }
      LineNetAmt = qty.multiply(priceActual);

      LineNetAmt = internalRound(LineNetAmt, stdPrecision);
      strLineNetAmt = LineNetAmt.toString();
    }

    resultado.append("new Array(\"inplinenetamt\", " + strLineNetAmt + ")");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());

    out.close();
  }

  private BigDecimal internalRound(BigDecimal value, Integer precision) {
    return (precision == null || value.scale() <= precision) ? value : value.setScale(precision,
        BigDecimal.ROUND_HALF_UP);
  }
}
