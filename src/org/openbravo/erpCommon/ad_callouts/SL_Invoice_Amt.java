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
import java.text.DecimalFormat;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Invoice_Amt extends HttpSecureAppServlet {
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
      String strQtyInvoice = vars.getNumericParameter("inpqtyinvoiced");
      String strPriceActual = vars.getNumericParameter("inppriceactual");
      String strPriceLimit = vars.getNumericParameter("inppricelimit");
      String strInvoiceId = vars.getStringParameter("inpcInvoiceId");
      String strProduct = vars.getStringParameter("inpmProductId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strPriceList = vars.getNumericParameter("inppricelist");
      String strPriceStd = vars.getNumericParameter("inppricestd");
      String strLineNetAmt = vars.getNumericParameter("inplinenetamt");
      String strTaxId = vars.getStringParameter("inpcTaxId");

      try {
        printPage(response, vars, strChanged, strQtyInvoice, strPriceActual, strInvoiceId,
            strProduct, strPriceLimit, strTabId, strPriceList, strPriceStd, strLineNetAmt, strTaxId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strQtyInvoice, String strPriceActual, String strInvoiceId, String strProduct,
      String strPriceLimit, String strTabId, String strPriceList, String strPriceStd,
      String strLineNetAmt, String strTaxId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLInvoiceAmtData[] data = SLInvoiceAmtData.select(this, strInvoiceId);
    String strPrecision = "0", strPricePrecision = "0";
    boolean enforcedLimit = false;
    if (data != null && data.length > 0) {
      strPrecision = data[0].stdprecision.equals("") ? "0" : data[0].stdprecision;
      strPricePrecision = data[0].priceprecision.equals("") ? "0" : data[0].priceprecision;
      enforcedLimit = (data[0].enforcepricelimit.equals("Y") ? true : false);
    }
    int StdPrecision = Integer.valueOf(strPrecision).intValue();
    int PricePrecision = Integer.valueOf(strPricePrecision).intValue();

    SLInvoiceTaxAmtData[] dataTax = SLInvoiceTaxAmtData.select(this, strTaxId, strInvoiceId);
    BigDecimal taxRate = BigDecimal.ZERO;
    Integer taxScale = new Integer(0);
    if (dataTax.length > 0) {
      taxRate = (dataTax[0].rate.equals("") ? new BigDecimal(1) : new BigDecimal(dataTax[0].rate));
      taxScale = new Integer(dataTax[0].priceprecision);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("strPriceActual: " + strPriceActual);
    if (log4j.isDebugEnabled())
      log4j.debug("strPriceLimit: " + strPriceLimit);
    if (log4j.isDebugEnabled())
      log4j.debug("strLineNetAmt: " + strLineNetAmt);
    if (log4j.isDebugEnabled())
      log4j.debug("taxRate: " + taxRate);

    BigDecimal qtyInvoice, priceActual, LineNetAmt, priceLimit, priceStd;

    qtyInvoice = (!Utility.isBigDecimal(strQtyInvoice) ? ZERO : new BigDecimal(strQtyInvoice));
    priceStd = (!Utility.isBigDecimal(strPriceStd) ? ZERO : new BigDecimal(strPriceStd));
    priceActual = (!Utility.isBigDecimal(strPriceActual) ? ZERO : (new BigDecimal(strPriceActual)))
        .setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
    priceLimit = (!Utility.isBigDecimal(strPriceLimit) ? ZERO : (new BigDecimal(strPriceLimit)))
        .setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
    LineNetAmt = (!Utility.isBigDecimal(strLineNetAmt) ? ZERO : new BigDecimal(strLineNetAmt));

    StringBuffer resultado = new StringBuffer();

    resultado.append("var calloutName='SL_Invoice_Amt';\n\n");
    resultado.append("var respuesta = new Array(");

    SLOrderProductData[] dataInvoice = SLOrderProductData.selectInvoice(this, strInvoiceId);

    if (strChanged.equals("inplinenetamt")) {
      if (qtyInvoice.compareTo(BigDecimal.ZERO) == 0) {
        priceActual = BigDecimal.ZERO;
      } else {
        priceActual = LineNetAmt.divide(qtyInvoice, PricePrecision, BigDecimal.ROUND_HALF_UP);
      }
    }
    if (priceActual.compareTo(BigDecimal.ZERO) == 0)
      LineNetAmt = BigDecimal.ZERO;
    // If unit price (actual price) changes, recalculates standard price
    // (std price) applying price adjustments (offers) if any
    if (strChanged.equals("inppriceactual") || strChanged.equals("inplinenetamt")) {
      if (log4j.isDebugEnabled())
        log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));
      priceStd = new BigDecimal(SLOrderProductData.getOffersStdPriceInvoice(this,
          dataInvoice[0].cBpartnerId, priceActual.toString(), strProduct,
          dataInvoice[0].dateinvoiced, qtyInvoice.toString(), dataInvoice[0].mPricelistId,
          dataInvoice[0].id));
      resultado.append("new Array(\"inppricestd\", " + priceStd.toString() + "),");
    }

    // If quantity changes, recalculates unit price (actual price) applying
    // price adjustments (offers) if any
    if (strChanged.equals("inpqtyinvoiced")) {
      if (log4j.isDebugEnabled())
        log4j.debug("strPriceList: " + strPriceList.replace("\"", "") + " product:" + strProduct
            + " qty:" + qtyInvoice.toString());
      priceActual = new BigDecimal(SLOrderProductData.getOffersPriceInvoice(this,
          dataInvoice[0].dateinvoiced, dataInvoice[0].cBpartnerId, strProduct, priceStd.toString(),
          qtyInvoice.toString(), dataInvoice[0].mPricelistId, dataInvoice[0].id));
      if (priceActual.scale() > PricePrecision)
        priceActual = priceActual.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
    }

    if (!strChanged.equals("inplinenetamt"))
      // Net amount of a line equals quantity x unit price (actual price)
      LineNetAmt = qtyInvoice.multiply(priceActual);

    if (strChanged.equals("inplinenetamt")) {
      DecimalFormat priceEditionFmt = Utility.getFormat(vars, "priceEdition");
      DecimalFormat euroEditionFmt = Utility.getFormat(vars, "euroEdition");
      BigDecimal CalculatedLineNetAmt = qtyInvoice
          .multiply(
              priceActual.setScale(priceEditionFmt.getMaximumFractionDigits(),
                  BigDecimal.ROUND_HALF_UP)).setScale(euroEditionFmt.getMaximumFractionDigits(),
              BigDecimal.ROUND_HALF_UP);
      if (!LineNetAmt
          .setScale(priceEditionFmt.getMaximumFractionDigits(), BigDecimal.ROUND_HALF_UP).equals(
              CalculatedLineNetAmt)) {
        StringBuffer strMessage = new StringBuffer(Utility.messageBD(this,
            "NotCorrectAmountProvided", vars.getLanguage()));
        strMessage.append(": ");
        strMessage.append((strLineNetAmt.equals("") ? BigDecimal.ZERO : new BigDecimal(
            strLineNetAmt)));
        strMessage.append(". ");
        strMessage.append(Utility.messageBD(this, "CosiderUsing", vars.getLanguage()));
        strMessage.append(" " + CalculatedLineNetAmt);
        resultado.append("new Array('MESSAGE', \"" + strMessage.toString() + "\"),");
      }
    }

    if (LineNetAmt.scale() > StdPrecision)
      LineNetAmt = LineNetAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);

    // Check price limit
    if (enforcedLimit) {
      if (priceLimit.compareTo(BigDecimal.ZERO) != 0 && priceActual.compareTo(priceLimit) < 0)
        resultado.append("new Array('MESSAGE', \""
            + FormatUtilities.replaceJS(Utility.messageBD(this, "UnderLimitPrice",
                vars.getLanguage())) + "\"), ");
    }
    BigDecimal taxAmt = ((LineNetAmt.multiply(taxRate)).divide(new BigDecimal("100"), 12,
        BigDecimal.ROUND_HALF_EVEN)).setScale(taxScale, BigDecimal.ROUND_HALF_UP);

    if (!strChanged.equals("inplinenetamt") || LineNetAmt.compareTo(BigDecimal.ZERO) == 0)
      resultado.append("new Array(\"inplinenetamt\", " + LineNetAmt.toString() + "),");
    resultado.append("new Array(\"inptaxbaseamt\", " + LineNetAmt.toString() + "),");
    resultado.append("new Array(\"inptaxamt\", " + taxAmt.toPlainString() + "),");
    resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + ")");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());

    out.close();
  }
}
