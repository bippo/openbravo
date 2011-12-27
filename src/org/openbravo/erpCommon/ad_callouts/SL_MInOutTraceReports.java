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
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_MInOutTraceReports extends HttpSecureAppServlet {
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
      String strMProductID = vars.getStringParameter("inpmProductId");
      // String strMAttributeSetInstanceID =
      // vars.getStringParameter("inpmAttributeSetInstanceId");
      String strMAttributeSetInstanceID = vars.getRequestGlobalVariable(
          "inpmAttributeSetInstanceId", "MInOutTraceReports|M_AttributeSetInstance_Id");

      try {
        printPage(response, vars, strMProductID, strMAttributeSetInstanceID);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strMProductID, String strMAttributeSetInstanceID) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    SLMInOutTraceReportsData[] dataAttribute = SLMInOutTraceReportsData.select(this,
        vars.getLanguage(), strMProductID);
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_MInOutTraceReports';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpmAttributeSetInstanceId\", ");
    if (dataAttribute != null && dataAttribute.length > 0) {
      resultado.append("new Array(");
      for (int i = 0; i < dataAttribute.length; i++) {
        resultado.append("new Array(\"" + dataAttribute[i].id + "\", \""
            + FormatUtilities.replaceJS(dataAttribute[i].name) + "\", \""
            + (dataAttribute[i].id.equalsIgnoreCase(strMAttributeSetInstanceID) ? "true" : "false")
            + "\")");
        if (i < dataAttribute.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n)");
    resultado.append(");\n");

    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
