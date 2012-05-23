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
 * All portions are Copyright (C) 2001-200 Openbravo SLU 
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
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Order_Amt extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final BigDecimal ZERO = new BigDecimal(0.0);

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strQtyOrdered = vars.getNumericParameter("inpqtyordered");
      String strPriceActual = vars.getNumericParameter("inppriceactual");
      String strDiscount = vars.getNumericParameter("inpdiscount");
      String strPriceLimit = vars.getNumericParameter("inppricelimit");
      String strPriceList = vars.getNumericParameter("inppricelist");
      String strPriceStd = vars.getNumericParameter("inppricestd");
      String strCOrderId = vars.getStringParameter("inpcOrderId");
      String strProduct = vars.getStringParameter("inpmProductId");
      String strUOM = vars.getStringParameter("inpcUomId");
      String strAttribute = vars.getStringParameter("inpmAttributesetinstanceId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strQty = vars.getNumericParameter("inpqtyordered");
      String cancelPriceAd = vars.getStringParameter("inpcancelpricead");
      String strLineNetAmt = vars.getNumericParameter("inplinenetamt");

      try {
        printPage(response, vars, strChanged, strQtyOrdered, strPriceActual, strDiscount,
            strPriceLimit, strPriceList, strCOrderId, strProduct, strUOM, strAttribute, strTabId,
            strQty, strPriceStd, cancelPriceAd, strLineNetAmt);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strQtyOrdered, String strPriceActual, String strDiscount, String strPriceLimit,
      String strPriceList, String strCOrderId, String strProduct, String strUOM,
      String strAttribute, String strTabId, String strQty, String strPriceStd,
      String cancelPriceAd, String strLineNetAmt) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) {
      log4j.debug("Output: dataSheet");
      log4j.debug("CHANGED:" + strChanged);
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLOrderAmtData[] data = SLOrderAmtData.select(this, strCOrderId);
    SLOrderStockData[] data1 = SLOrderStockData.select(this, strProduct);
    String strPrecision = "0", strPricePrecision = "0";
    String strStockSecurity = "0";
    String strEnforceAttribute = "N";
    String Issotrx = SLOrderStockData.isSotrx(this, strCOrderId);
    String strStockNoAttribute;
    String strStockAttribute;
    if (data1 != null && data1.length > 0) {
      strStockSecurity = data1[0].stock;
      strEnforceAttribute = data1[0].enforceAttribute;
    }
    // boolean isUnderLimit=false;
    if (data != null && data.length > 0) {
      strPrecision = data[0].stdprecision.equals("") ? "0" : data[0].stdprecision;
      strPricePrecision = data[0].priceprecision.equals("") ? "0" : data[0].priceprecision;
    }
    int StdPrecision = Integer.valueOf(strPrecision).intValue();
    int PricePrecision = Integer.valueOf(strPricePrecision).intValue();

    BigDecimal qtyOrdered, priceActual, discount, priceLimit, priceList, stockSecurity, stockNoAttribute, stockAttribute, resultStock, priceStd, LineNetAmt;

    stockSecurity = new BigDecimal(strStockSecurity);
    qtyOrdered = (strQtyOrdered.equals("") ? ZERO : new BigDecimal(strQtyOrdered));
    priceActual = (strPriceActual.equals("") ? ZERO : (new BigDecimal(strPriceActual))).setScale(
        PricePrecision, BigDecimal.ROUND_HALF_UP);
    discount = (strDiscount.equals("") ? ZERO : new BigDecimal(strDiscount));
    priceLimit = (strPriceLimit.equals("") ? ZERO : (new BigDecimal(strPriceLimit))).setScale(
        PricePrecision, BigDecimal.ROUND_HALF_UP);
    priceList = (strPriceList.equals("") ? ZERO : (new BigDecimal(strPriceList))).setScale(
        PricePrecision, BigDecimal.ROUND_HALF_UP);
    priceStd = (strPriceStd.equals("") ? ZERO : (new BigDecimal(strPriceStd))).setScale(
        PricePrecision, BigDecimal.ROUND_HALF_UP);
    LineNetAmt = (strLineNetAmt.equals("") ? ZERO : (new BigDecimal(strLineNetAmt))).setScale(
        PricePrecision, BigDecimal.ROUND_HALF_UP);
    /*
     * if (enforcedLimit) { String strPriceVersion = ""; PriceListVersionComboData[] data1 =
     * PriceListVersionComboData.selectActual(this, data[0].mPricelistId, DateTimeData.today(this));
     * if (data1!=null && data1.length>0) strPriceVersion = data1[0].mPricelistVersionId; BigDecimal
     * lineLimit = new BigDecimal(SLOrderAmtData.selectPriceLimit(this, strPriceVersion,
     * strProduct)); if (lineLimit.floatValue() >priceActual.floatValue()) isUnderLimit=true; }
     */

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Order_Amt';\n\n");
    resultado.append("var respuesta = new Array(");

    if (strChanged.equals("inplinenetamt")) {
      priceActual = LineNetAmt.divide(qtyOrdered, PricePrecision, BigDecimal.ROUND_HALF_UP);
      if (priceActual.compareTo(BigDecimal.ZERO) == 0)
        LineNetAmt = BigDecimal.ZERO;
    }
    // Calculating prices for offers...
    SLOrderProductData[] dataOrder = SLOrderProductData.select(this, strCOrderId);
    if (strChanged.equals("inppriceactual") || strChanged.equals("inplinenetamt")) {
      if (log4j.isDebugEnabled())
        log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));
      if ("Y".equals(cancelPriceAd)) {
        priceStd = priceActual;
        resultado.append("new Array(\"inppricestd\", " + priceStd.toString() + "),");
      } else {
        // priceStd needs to be changed?
        BigDecimal expectedPriceActual = new BigDecimal(SLOrderProductData.getOffersPrice(this,
            dataOrder[0].dateordered, dataOrder[0].cBpartnerId, strProduct, priceStd.toString(),
            strQty, dataOrder[0].mPricelistId, dataOrder[0].id));
        if (expectedPriceActual.scale() > PricePrecision)
          expectedPriceActual = expectedPriceActual.setScale(PricePrecision,
              BigDecimal.ROUND_HALF_UP);

        // To avoid rounding issues if the expected priceActual is equals to the current
        // priceActual.
        // Do not do anything.
        if (!priceActual.equals(expectedPriceActual)) {
          priceStd = new BigDecimal(SLOrderProductData.getOffersStdPrice(this,
              dataOrder[0].cBpartnerId, expectedPriceActual.toString().replace("\"", ""),
              strProduct, dataOrder[0].dateordered, strQty, dataOrder[0].mPricelistId,
              dataOrder[0].id));
        }
        // priceList
        resultado.append("new Array(\"inppricestd\", " + priceStd.toString() + "),");
      }

    }

    if (strChanged.equals("inpcancelpricead")) {
      if ("Y".equals(cancelPriceAd)) {
        resultado.append("new Array(\"inppriceactual\", " + strPriceStd + "),");
      } else {
        strPriceActual = SLOrderProductData.getOffersPrice(this, dataOrder[0].dateordered,
            dataOrder[0].cBpartnerId, strProduct, (strPriceStd.equals("undefined") ? "0"
                : strPriceStd.replace("\"", "")), strQty, dataOrder[0].mPricelistId,
            dataOrder[0].id);
        priceActual = new BigDecimal(strPriceActual);
        resultado.append("new Array(\"inppriceactual\", " + strPriceActual + "),");
      }
    }

    /*
     * if (strChanged.equals("inppricelist")||strChanged.equals("inpqtyordered" )) { if
     * (log4j.isDebugEnabled()) log4j.debug("priceList:" +
     * Double.toString(priceList.doubleValue())); priceActual = new
     * BigDecimal(SLOrderProductData.getOffersPrice(this, dataOrder[0].dateordered,
     * dataOrder[0].cBpartnerId, strProduct, strPriceList.replace("\"", ""), strQty,
     * dataOrder[0].mPricelistId, dataOrder[0].id));
     * resultado.append("new Array(\"inppriceactual\", \"" + priceActual.toString() + "\"),"); }
     */

    // calculating discount
    if (strChanged.equals("inppricelist") || strChanged.equals("inppriceactual")
        || strChanged.equals("inplinenetamt")) {
      if (priceList.compareTo(BigDecimal.ZERO) == 0)
        discount = ZERO;
      else {
        if (log4j.isDebugEnabled())
          log4j.debug("pricelist:" + Double.toString(priceList.doubleValue()));
        if (log4j.isDebugEnabled())
          log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));
        discount = ((priceList.subtract(priceStd))
            .divide(priceList, 12, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100"));
      }
      if (log4j.isDebugEnabled())
        log4j.debug("Discount: " + discount.toString());
      if (discount.scale() > StdPrecision)
        discount = discount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
      if (log4j.isDebugEnabled())
        log4j.debug("Discount rounded: " + discount.toString());
      resultado.append("new Array(\"inpdiscount\", " + discount.toString() + "),");
    } else if (strChanged.equals("inpqtyordered")) { // calculate Actual
      if ("Y".equals(cancelPriceAd)) {
        priceActual = priceStd;
        resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");
      } else {
        BigDecimal previousOffer = BigDecimal.ZERO;
        if (priceActual.signum() != 0) {
          previousOffer = new BigDecimal(SLOrderProductData.getOffersPrice(this,
              dataOrder[0].dateordered, dataOrder[0].cBpartnerId, strProduct, priceStd.toString(),
              (LineNetAmt.divide(priceActual, BigDecimal.ROUND_HALF_EVEN)).toString(),
              dataOrder[0].mPricelistId, dataOrder[0].id));
        }
        final BigDecimal actualOffer = new BigDecimal(SLOrderProductData.getOffersPrice(this,
            dataOrder[0].dateordered, dataOrder[0].cBpartnerId, strProduct, priceStd.toString(),
            strQty, dataOrder[0].mPricelistId, dataOrder[0].id));

        if (!previousOffer.equals(actualOffer)) {
          priceActual = actualOffer;
        }
        if (priceActual.scale() > PricePrecision)
          priceActual = priceActual.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
        resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");
      }

    } else if (strChanged.equals("inpdiscount")) { // calculate std and actual
      BigDecimal discount1 = null;
      if (priceList.compareTo(BigDecimal.ZERO) != 0)
        discount1 = (((priceList.subtract(priceStd)).divide(priceList, 12,
            BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100"))).setScale(StdPrecision,
            BigDecimal.ROUND_HALF_UP);
      else
        discount1 = BigDecimal.ZERO;
      BigDecimal discount2 = discount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
      if (discount1.compareTo(discount2) != 0) // checks if rounded
      // discount has changed
      {
        priceStd = priceList.subtract(priceList.multiply(discount).divide(new BigDecimal("100"),
            12, BigDecimal.ROUND_HALF_EVEN));
        priceActual = new BigDecimal(SLOrderProductData.getOffersPrice(this,
            dataOrder[0].dateordered, dataOrder[0].cBpartnerId, strProduct,
            priceStd.toPlainString(), strQty, dataOrder[0].mPricelistId, dataOrder[0].id));
        if (priceStd.scale() > PricePrecision)
          priceStd = priceStd.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
        if (priceActual.scale() > PricePrecision)
          priceActual = priceActual.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
        resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");
        resultado.append("new Array(\"inppricestd\", " + priceStd.toString() + "),");
      }
    }

    if (Issotrx.equals("Y")) {
      if (!strStockSecurity.equals("0")) {
        if (qtyOrdered.compareTo(BigDecimal.ZERO) != 0) {
          if (strEnforceAttribute.equals("N")) {
            strStockNoAttribute = SLOrderStockData.totalStockNoAttribute(this, strProduct, strUOM);
            stockNoAttribute = new BigDecimal(strStockNoAttribute);
            resultStock = stockNoAttribute.subtract(qtyOrdered);
            if (stockSecurity.compareTo(resultStock) > 0) {
              resultado.append("new Array('MESSAGE', \""
                  + FormatUtilities.replaceJS(Utility.messageBD(this, "StockLimit",
                      vars.getLanguage())) + "\"),");
            }
          } else {
            if (!strAttribute.equals("") && strAttribute != null) {
              strStockAttribute = SLOrderStockData.totalStockAttribute(this, strProduct, strUOM,
                  strAttribute);
              stockAttribute = new BigDecimal(strStockAttribute);
              resultStock = stockAttribute.subtract(qtyOrdered);
              if (stockSecurity.compareTo(resultStock) > 0) {
                resultado.append("new Array('MESSAGE', \""
                    + FormatUtilities.replaceJS(Utility.messageBD(this, "StockLimit",
                        vars.getLanguage())) + "\"),");
              }
            }
          }
        }
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug(resultado.toString());
    if (!strChanged.equals("inpqtyordered") || strChanged.equals("inplinenetamt")) { // Check
      // PriceLimit
      boolean enforced = SLOrderAmtData.listPriceType(this, strPriceList);
      // Check Price Limit?
      if (enforced && priceLimit.compareTo(BigDecimal.ZERO) != 0
          && priceActual.compareTo(priceLimit) < 0)
        resultado.append("new Array('MESSAGE', \""
            + Utility.messageBD(this, "UnderLimitPrice", vars.getLanguage()) + "\")");
    }

    // Multiply
    if ("Y".equals(cancelPriceAd)) {
      LineNetAmt = qtyOrdered.multiply(priceStd);
    } else {
      if (!strChanged.equals("inplinenetamt")) {
        LineNetAmt = qtyOrdered.multiply(priceActual);
        if (LineNetAmt.scale() > StdPrecision)
          LineNetAmt = LineNetAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
      }
    }
    if (strChanged.equals("inplinenetamt"))
      resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");
    if (!strChanged.equals("inplinenetamt") || priceActual.compareTo(BigDecimal.ZERO) == 0)
      resultado.append("new Array(\"inplinenetamt\", " + LineNetAmt.toString() + "),");
    resultado.append("new Array(\"inptaxbaseamt\", " + LineNetAmt.toString() + ")");

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
