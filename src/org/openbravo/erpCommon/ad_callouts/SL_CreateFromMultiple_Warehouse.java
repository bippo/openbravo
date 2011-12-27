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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_CreateFromMultiple_Warehouse extends HttpSecureAppServlet {
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
      log4j.debug("CHANGED: " + strChanged);
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strWarehouse = vars.getStringParameter("inpmWarehouseId");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strWarehouse, strIsSOTrx, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strWarehouse, String strIsSOTrx, String strTabId) throws IOException, ServletException {
    log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    resultado.append("var frameDefault='frameButton';\n\n");
    resultado.append("var calloutName='SL_CreateFromMultiple_Warehouse';\n\n");
    resultado.append("var respuesta = new Array(");

    LocatorComboData[] data = LocatorComboData.select(this, vars.getLanguage(), strWarehouse,
        vars.getClient());
    resultado.append("new Array(\"inpmLocatorX\", ");
    if (data != null && data.length > 0) {
      resultado.append("new Array(");
      for (int i = 0; i < data.length; i++) {
        resultado.append("new Array(\"" + data[i].id + "\", \"" + data[i].name + "\", \"false\")");
        if (i < data.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n)");
    resultado.append(");");

    log4j.debug("Array: " + resultado.toString());
    xmlDocument.setParameter("array", resultado.toString());
    // xmlDocument.setParameter("frameName", (Utility.isTreeTab(this,
    // strTabId)?"appFrame.frameWindowTreeTab":"appFrame"));
    xmlDocument.setParameter("frameName", "frameButton");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
