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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.Product;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_GlobalUse_Product extends HttpSecureAppServlet {
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
      String strTabId = vars.getStringParameter("inpTabId");

      String strMProductID = vars.getStringParameter("inpmProductId");
      String strPAttr = vars.getStringParameter("inpmProductId_ATR");

      try {
        printPage(response, vars, strTabId, strMProductID, strPAttr);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTabId,
      String strMProductID, String strPAttr) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    String strProductUom = SLWRPhaseProductProductData.selectProductUom(this, strMProductID);
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_GlobalUse_Product';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado
        .append("new Array(\"inpcUomId\", \""
            + FormatUtilities.replaceJS((strProductUom.equals("") ? "\"\"" : strProductUom))
            + "\"),\n");
    String strHasSecondaryUOM = SLGlobalUseProductData.hasSecondaryUOM(this, strMProductID);
    resultado.append("new Array(\"inphasseconduom\", \"" + strHasSecondaryUOM + "\"),\n");
    if (strPAttr.startsWith("\""))
      strPAttr = strPAttr.substring(1, strPAttr.length() - 1);
    resultado.append("new Array(\"inpmAttributesetinstanceId\", \"" + strPAttr + "\"),\n");
    resultado.append("new Array(\"inpmAttributesetinstanceId_R\", \""
        + FormatUtilities.replaceJS(SLInOutLineProductData.attribute(this, strPAttr)) + "\"),\n");
    String strAttrSet, strAttrSetValueType;
    strAttrSet = strAttrSetValueType = "";
    OBContext.setAdminMode();
    try {
      final Product product = OBDal.getInstance().get(Product.class, strMProductID);
      if (product != null) {
        AttributeSet attributeset = product.getAttributeSet();
        if (attributeset != null)
          strAttrSet = product.getAttributeSet().toString();
        strAttrSetValueType = product.getUseAttributeSetValueAs();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    resultado.append("new Array(\"inpattributeset\", \"" + FormatUtilities.replaceJS(strAttrSet)
        + "\"),\n");
    resultado.append("new Array(\"inpattrsetvaluetype\", \""
        + FormatUtilities.replaceJS(strAttrSetValueType) + "\"),\n");
    // String strProductUomId = SLGlobalUseProduct.selectProductUomId(this,
    // strMProductID);

    resultado.append("new Array(\"inpmProductUomId\", ");
    String strmProductUOMId = SLGlobalUseProductData.strMProductUOMID(this, strMProductID,
        strProductUom);
    if (vars.getLanguage().equals("en_US")) {
      FieldProvider[] tld = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "M_Product_UOM", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "SLGlobalUseProduct"), Utility.getContext(this, vars, "#User_Client",
                "SLGlobalUseProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "SLGlobalUseProduct", "");
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
              + (tld[i].getField("id").equalsIgnoreCase(strmProductUOMId) ? "true" : "false")
              + "\")");
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
                "SLGlobalUseProduct"), Utility.getContext(this, vars, "#User_Client",
                "SLGlobalUseProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "SLGlobalUseProduct", "");
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
              + (tld[i].getField("id").equalsIgnoreCase(strmProductUOMId) ? "true" : "false")
              + "\")");
          if (i < tld.length - 1)
            resultado.append(",\n");
        }
        resultado.append("\n)");
      } else
        resultado.append("null");
      resultado.append("\n),");
    }
    resultado.append("new Array(\"EXECUTE\", \"displayLogic();\")\n");
    // To place the cursor in the quantity field
    // resultado.append("new Array(\"CURSOR_FIELD\", \"inpqtyordered\")\n");
    // if (!strHasSecondaryUOM.equals("0"))
    // resultado.append(", new Array(\"CURSOR_FIELD\", \"inpquantityorder\")\n");
    resultado.append(");\n");

    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
