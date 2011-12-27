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
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_MovementAll_Locator extends HttpSecureAppServlet {
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
      String strLocator = vars.getStringParameter(strChanged);
      // String strLocator = vars.getStringParameter("inpmLocatorId");
      String strmInoutId = vars.getStringParameter("inpmInoutId");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strLocator, "N", strTabId, strmInoutId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strLocator,
      String strIsSOTrx, String strTabId, String strmInoutId) throws IOException, ServletException {
    log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    String locator = "0";
    String FilledLocator = "0";

    locator = SLInOutLineLocatorData.locator(this, strLocator);
    FilledLocator = SLInOutLineLocatorData.filledLocator(this, strmInoutId, strLocator);

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_MovementAll_Locator';\n\n");
    log4j.debug("IsSOTrx: " + strIsSOTrx + " - locator: " + locator + " - FilledLocator: "
        + FilledLocator);
    if (strIsSOTrx.equals("N") && !locator.equals("0")) {
      resultado.append("var respuesta = new Array(");
      resultado.append("new Array(\"MESSAGE\", \""
          + Utility.messageBD(this, "FilledWarehouseLocator", vars.getLanguage()) + "\")");
      resultado.append(");");
    } else if (strIsSOTrx.equals("N") && !FilledLocator.equals("0")) {
      resultado.append("var respuesta = new Array(");
      resultado.append("new Array(\"MESSAGE\", \""
          + Utility.messageBD(this, "FilledLocatorInout", vars.getLanguage()) + "\")");
      resultado.append(");");
    } else
      resultado.append("var respuesta = null;\n");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "frameButton");
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
