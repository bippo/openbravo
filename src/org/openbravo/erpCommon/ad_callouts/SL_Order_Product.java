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
import org.openbravo.erpCommon.businessUtility.PAttributeSet;
import org.openbravo.erpCommon.businessUtility.PAttributeSetData;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Order_Product extends HttpSecureAppServlet {
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
      String strUOM = vars.getStringParameter("inpmProductId_UOM");
      String strPriceList = vars.getNumericParameter("inpmProductId_PLIST");
      String strPriceStd = vars.getNumericParameter("inpmProductId_PSTD");
      String strPriceLimit = vars.getNumericParameter("inpmProductId_PLIM");
      String strCurrency = vars.getStringParameter("inpmProductId_CURR");
      String strQty = vars.getNumericParameter("inpqtyordered");

      String strCBpartnerID = vars.getStringParameter("inpcBpartnerId");
      String strMProductID = vars.getStringParameter("inpmProductId");
      String strCBPartnerLocationID = vars.getStringParameter("inpcBpartnerLocationId");
      String strDateOrdered = vars.getStringParameter("inpdateordered");
      String strADOrgID = vars.getStringParameter("inpadOrgId");
      String strMWarehouseID = vars.getStringParameter("inpmWarehouseId");
      String strCOrderId = vars.getStringParameter("inpcOrderId");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");
      String cancelPriceAd = vars.getStringParameter("inpcancelpricead");

      try {
        printPage(response, vars, strUOM, strPriceList, strPriceStd, strPriceLimit, strCurrency,
            strMProductID, strCBPartnerLocationID, strDateOrdered, strADOrgID, strMWarehouseID,
            strCOrderId, strWindowId, strIsSOTrx, strCBpartnerID, strTabId, strQty, cancelPriceAd);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strUOM,
      String strPriceList, String strPriceStd, String strPriceLimit, String strCurrency,
      String strMProductID, String strCBPartnerLocationID, String strDateOrdered,
      String strADOrgID, String strMWarehouseID, String strCOrderId, String strWindowId,
      String strIsSOTrx, String strCBpartnerID, String strTabId, String strQty, String cancelPriceAd)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    String strPriceActual = "";
    String strHasSecondaryUOM = "";

    if (!strMProductID.equals("")) {
      SLOrderProductData[] dataOrder = SLOrderProductData.select(this, strCOrderId);

      if (log4j.isDebugEnabled())
        log4j.debug("get Offers date: " + dataOrder[0].dateordered + " partner:"
            + dataOrder[0].cBpartnerId + " prod:" + strMProductID + " std:"
            + strPriceStd.replace("\"", ""));
      strPriceActual = SLOrderProductData.getOffersPrice(this, dataOrder[0].dateordered,
          dataOrder[0].cBpartnerId, strMProductID, (strPriceStd.equals("undefined") ? "0"
              : strPriceStd.replace("\"", "")), strQty, dataOrder[0].mPricelistId, dataOrder[0].id);
      if (log4j.isDebugEnabled())
        log4j.debug("get Offers price:" + strPriceActual);

      dataOrder = null;
    } else {
      strUOM = strPriceList = strPriceLimit = strPriceStd = "";
    }
    StringBuffer resultado = new StringBuffer();

    if (strPriceActual.equals("") || "Y".equals(cancelPriceAd))
      strPriceActual = strPriceStd;

    // Discount...
    if (strPriceList.startsWith("\""))
      strPriceList = strPriceList.substring(1, strPriceList.length() - 1);
    if (strPriceStd.startsWith("\""))
      strPriceStd = strPriceStd.substring(1, strPriceStd.length() - 1);
    BigDecimal priceList = (strPriceList.equals("") ? new BigDecimal(0.0) : new BigDecimal(
        strPriceList));
    BigDecimal priceStd = (strPriceStd.equals("") ? new BigDecimal(0.0) : new BigDecimal(
        strPriceStd));
    BigDecimal discount = new BigDecimal(0.0);
    if (priceList.compareTo(discount) != 0) {
      discount = (((priceList.subtract(priceStd)).divide(priceList, 12, BigDecimal.ROUND_HALF_EVEN))
          .multiply(new BigDecimal("100"))).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    resultado.append("var calloutName='SL_Order_Product';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpcUomId\", \"" + strUOM + "\"),");
    resultado.append("new Array(\"inppricelist\", "
        + (strPriceList.equals("") ? "0" : strPriceList) + "),");
    resultado.append("new Array(\"inppricelimit\", "
        + (strPriceLimit.equals("") ? "0" : strPriceLimit) + "),");
    resultado.append("new Array(\"inppricestd\", " + (strPriceStd.equals("") ? "0" : strPriceStd)
        + "),");
    resultado.append("new Array(\"inppriceactual\", "
        + (strPriceActual.equals("") ? "0" : strPriceActual) + "),");
    resultado.append("new Array(\"inpcCurrencyId\", "
        + (strCurrency.equals("") ? "\"\"" : strCurrency) + "),");
    resultado.append("new Array(\"inpdiscount\", " + discount.toString() + "),");
    if (!strMProductID.equals("")) {
      PAttributeSetData[] dataPAttr = PAttributeSetData.selectProductAttr(this, strMProductID);
      if (dataPAttr != null && dataPAttr.length > 0 && dataPAttr[0].attrsetvaluetype.equals("D")) {
        PAttributeSetData[] data2 = PAttributeSetData.select(this, dataPAttr[0].mAttributesetId);
        if (PAttributeSet.isInstanceAttributeSet(data2)) {
          resultado.append("new Array(\"inpmAttributesetinstanceId\", \"\"),");
          resultado.append("new Array(\"inpmAttributesetinstanceId_R\", \"\"),");
        } else {
          resultado.append("new Array(\"inpmAttributesetinstanceId\", \""
              + dataPAttr[0].mAttributesetinstanceId + "\"),");
          resultado.append("new Array(\"inpmAttributesetinstanceId_R\", \""
              + FormatUtilities.replaceJS(dataPAttr[0].description) + "\"),");
        }
      } else {
        resultado.append("new Array(\"inpmAttributesetinstanceId\", \"\"),");
        resultado.append("new Array(\"inpmAttributesetinstanceId_R\", \"\"),");
      }
      resultado.append("new Array(\"inpattributeset\", \""
          + FormatUtilities.replaceJS(dataPAttr[0].mAttributesetId) + "\"),\n");
      resultado.append("new Array(\"inpattrsetvaluetype\", \""
          + FormatUtilities.replaceJS(dataPAttr[0].attrsetvaluetype) + "\"),\n");
      strHasSecondaryUOM = SLOrderProductData.hasSecondaryUOM(this, strMProductID);
      resultado.append("new Array(\"inphasseconduom\", " + strHasSecondaryUOM + "),\n");
    }

    String strCTaxID = "";
    String orgLocationID = SLOrderProductData.getOrgLocationId(this,
        Utility.getContext(this, vars, "#User_Client", "SLOrderProduct"), "'" + strADOrgID + "'");
    if (orgLocationID.equals("")) {
      resultado.append("new Array('MESSAGE', \""
          + FormatUtilities.replaceJS(Utility.messageBD(this, "NoLocationNoTaxCalculated",
              vars.getLanguage())) + "\"),\n");
    } else {
      SLOrderTaxData[] data = SLOrderTaxData.select(this, strCOrderId);
      strCTaxID = Tax.get(this, strMProductID, data[0].dateordered, strADOrgID, strMWarehouseID,
          (data[0].billtoId.equals("") ? strCBPartnerLocationID : data[0].billtoId),
          strCBPartnerLocationID, data[0].cProjectId, strIsSOTrx.equals("Y"));
    }
    if (!strCTaxID.equals(""))
      resultado.append("new Array(\"inpcTaxId\", \"" + strCTaxID + "\"),\n");

    resultado.append("new Array(\"inpmProductUomId\", ");
    // if (strUOM.startsWith("\""))
    // strUOM=strUOM.substring(1,strUOM.length()-1);
    // String strmProductUOMId =
    // SLOrderProductData.strMProductUOMID(this,strMProductID,strUOM);
    if (vars.getLanguage().equals("en_US")) {
      FieldProvider[] tld = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "M_Product_UOM", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "SLOrderProduct"),
            Utility.getContext(this, vars, "#User_Client", "SLOrderProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "SLOrderProduct", "");
        tld = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (tld != null && tld.length > 0) {
        resultado.append("new Array(");
        for (int i = 0; i < tld.length; i++) {
          resultado.append("new Array(\"" + tld[i].getField("id") + "\", \""
              + FormatUtilities.replaceJS(tld[i].getField("name")) + "\", \"" + ("false") + "\")");
          if (i < tld.length - 1)
            resultado.append(",\n");
        }
        resultado.append("\n)");
      } else
        resultado.append("null");
      resultado.append("\n),");
    } else {
      FieldProvider[] tld = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "M_Product_UOM", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "SLOrderProduct"),
            Utility.getContext(this, vars, "#User_Client", "SLOrderProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "SLOrderProduct", "");
        tld = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (tld != null && tld.length > 0) {
        resultado.append("new Array(");
        for (int i = 0; i < tld.length; i++) {
          resultado.append("new Array(\"" + tld[i].getField("id") + "\", \""
              + FormatUtilities.replaceJS(tld[i].getField("name")) + "\", \""
              + (i == 0 ? "true" : "false") + "\")");
          if (i < tld.length - 1)
            resultado.append(",\n");
        }
        resultado.append("\n)");
      } else
        resultado.append("null");
      resultado.append("\n),");
    }
    resultado.append("new Array(\"EXECUTE\", \"displayLogic();\"),\n");
    // Para posicionar el cursor en el campo de cantidad
    resultado.append("new Array(\"CURSOR_FIELD\", \"inpqtyordered\")\n");
    if (!strHasSecondaryUOM.equals("0"))
      resultado.append(", new Array(\"CURSOR_FIELD\", \"inpquantityorder\")\n");

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
