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
import org.openbravo.xmlEngine.XmlDocument;

public class SE_InOut_DocType extends HttpSecureAppServlet {
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
      String strDocType = vars.getStringParameter("inpcDoctypeId");

      try {
        printPage(response, vars, strDocType, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDocType,
      String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    SEInOutDocTypeData[] data = SEInOutDocTypeData.select(this, strDocType);

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_InOut_DocType';\n\n");
    if (data != null && data.length > 0) {
      resultado.append("var respuesta = new Array(");
      if (data[0].docbasetype.equals("MMS"))
        resultado.append("new Array(\"inpmovementtype\", \"C-\")");
      else if (data[0].docbasetype.equals("MMR"))
        resultado.append("new Array(\"inpmovementtype\", \"V+\")");
      else
        resultado.append("new Array(\"inpmovementtype\", \"\")");
      if (data[0].isdocnocontrolled.equals("Y"))
        resultado.append(", new Array(\"inpdocumentno\", \"<" + data[0].currentnext + ">\")");
      resultado.append("\n);");
    } else {
      resultado.append("var respuesta = new Array(");
      resultado.append("new Array(\"inpmovementtype\", \"\")");
      resultado.append(");");
    }

    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
