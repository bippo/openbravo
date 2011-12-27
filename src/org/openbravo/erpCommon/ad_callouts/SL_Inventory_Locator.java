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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
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
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Inventory_Locator extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      final String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      final String strProduct = vars.getStringParameter("inpmProductId");
      final String strLocator = vars.getStringParameter("inpmLocatorId");
      final String strAttribute = vars.getStringParameter("inpmAttributesetinstanceId");
      final String strUOM = vars.getStringParameter("inpcUomId");
      final String strSecUOM = vars.getStringParameter("inpmProductUomId");
      final String strTabId = vars.getStringParameter("inpTabId");
      printPage(response, vars, strChanged, strProduct, strLocator, strAttribute, strUOM,
          strSecUOM, strTabId);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strProduct, String strLocator, String strAttribute, String strUOM, String strSecUOM,
      String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    final StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Inventory_Locator';\n\n");
    resultado.append("var respuesta = new Array(");

    if (strProduct.startsWith("\""))
      strProduct = strProduct.substring(1, strProduct.length() - 1);

    if (!strProduct.equals("")) {

      SLInventoryLocatorData[] data = SLInventoryLocatorData.select(this, strProduct, strLocator,
          strUOM, strSecUOM, ((strSecUOM == null || strSecUOM.equals("")) ? "productuom" : ""),
          ((strAttribute == null || strAttribute.equals("")) ? null : strAttribute));
      if (data == null || data.length == 0) {
        data = SLInventoryLocatorData.set();
        data[0].qty = "0";
        data[0].qtyorder = "0";
      }

      resultado.append("new Array(\"inpquantityorderbook\", "
          + ((data[0].qtyorder == null || data[0].qtyorder.equals("")) ? "\"\"" : data[0].qtyorder)
          + "), \n");
      resultado.append("new Array(\"inpqtycount\", "
          + ((data[0].qty == null || data[0].qty.equals("")) ? "\"\"" : data[0].qty) + "), \n");
      resultado.append("new Array(\"inpqtybook\", "
          + ((data[0].qty == null || data[0].qty.equals("")) ? "\"\"" : data[0].qty) + "), \n");

      resultado.append("new Array(\"EXECUTE\", \"displayLogic();\")\n");
    }
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
