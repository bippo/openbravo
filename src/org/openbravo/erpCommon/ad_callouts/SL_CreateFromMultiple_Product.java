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
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_CreateFromMultiple_Product extends HttpSecureAppServlet {
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
      String strLocator = vars.getStringParameter("inpmProductId_LOC");
      String strQty = vars.getNumericParameter("inpmProductId_QTY");
      String strUOM = vars.getStringParameter("inpmProductId_UOM");
      String strAttribute = vars.getStringParameter("inpmProductId_ATR");
      String strQtyOrder = vars.getNumericParameter("inpmProductId_PQTY");
      String strPUOM = vars.getStringParameter("inpmProductId_PUOM");
      String strMProductID = vars.getStringParameter("inpmProductId");
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strWharehouse = Utility.getContext(this, vars, "#M_Warehouse_ID", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strLocator, strQty, strUOM, strAttribute, strQtyOrder, strPUOM,
            strMProductID, strIsSOTrx, strWharehouse, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strLocator,
      String strQty, String strUOM, String strAttribute, String strQtyOrder, String strPUOM,
      String strMProductID, String strIsSOTrx, String strWharehouse, String strTabId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    resultado.append("var frameDefault='frameButton';\n\n");
    resultado.append("var calloutName='SL_CreateFromMultiple_Product';\n\n");
    resultado.append("var respuesta = new Array(");
    /*
     * if (strIsSOTrx.equals("Y")) { if (strLocator.startsWith("\""))
     * strLocator=strLocator.substring(1,strLocator.length()-1);
     * resultado.append("new Array(\"inpmLocatorId\", \"" + strLocator + "\"),");
     * resultado.append("new Array(\"inpmLocatorId_R\", \"" + SLInOutLineProductData.locator(this,
     * strLocator, vars.getLanguage()) + "\"),"); if (strAttribute.startsWith("\""))
     * strAttribute=strAttribute.substring(1,strAttribute.length()-1);
     * resultado.append("new Array(\"inpmAttributesetinstanceId\", \"" + strAttribute + "\"),");
     * resultado.append("new Array(\"inpmAttributesetinstanceId_R\", \"" +
     * SLInOutLineProductData.attribute(this, strAttribute) + "\"),");
     * resultado.append("new Array(\"inpquantityorder\", " +
     * (strQtyOrder.equals("")?"\"\"":strQtyOrder) + "),");
     * resultado.append("new Array(\"inpmovementqty\", " + (strQty.equals("")?"\"\"":strQty) +
     * "),"); }
     */
    String strHasSecondaryUOM = SLOrderProductData.hasSecondaryUOM(this, strMProductID);
    resultado.append("new Array(\"inphasseconduom\", " + strHasSecondaryUOM + "),\n");
    resultado.append("new Array(\"inpmProductUomId\", ");
    if (strPUOM.startsWith("\""))
      strPUOM = strPUOM.substring(1, strPUOM.length() - 1);
    if (vars.getLanguage().equals("en_US")) {
      FieldProvider[] tld = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "M_Product_UOM", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "SLCreateFromMultipleProduct"), Utility.getContext(this, vars, "#User_Client",
                "SLCreateFromMultipleProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "SLCreateFromMultipleProduct",
            "");
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
              + (tld[i].getField("id").equalsIgnoreCase(strPUOM) ? "true" : "false") + "\")");
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
                "SLCreateFromMultipleProduct"), Utility.getContext(this, vars, "#User_Client",
                "SLCreateFromMultipleProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "SLCreateFromMultipleProduct",
            "");
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
              + (tld[i].getField("id").equalsIgnoreCase(strPUOM) ? "true" : "false") + "\")");
          if (i < tld.length - 1)
            resultado.append(",\n");
        }
        resultado.append("\n)");
      } else
        resultado.append("null");
      resultado.append("\n),");
    }
    resultado.append("new Array(\"inpcUomId\", \"" + strUOM + "\"),\n");
    resultado.append("new Array(\"EXECUTE\", \"displayLogic();\")\n");

    resultado.append(");");

    if (log4j.isDebugEnabled())
      log4j.debug("Array: " + resultado.toString());
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "frameButton");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
