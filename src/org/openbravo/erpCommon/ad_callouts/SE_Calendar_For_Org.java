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

public class SE_Calendar_For_Org extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getStringParameter("inpadOrgId");
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      String strWindowId = vars.getStringParameter("inpwindowId");
      try {
        printPage(response, vars, strWindowId, strOrgId, strChanged);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strWindowId,
      String strOrgId, String strChanged) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuilder result = new StringBuilder();
    result.append("var calloutName='SE_Calendar_For_Org';\n\n");
    result.append("var respuesta = new Array(");
    SEPeriodNoData[] tdv = null;
    if (strChanged.equals("inpadOrgId") && !strOrgId.equals("")) {
      // Update the Calendar
      try {
        tdv = SEPeriodNoData.getCalendar(this, strOrgId);
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
    }
    result.append("new Array(\"inpcCalendarId\", ");
    if (tdv != null && tdv.length > 0) {
      result.append("new Array(");
      for (int i = 0; i < tdv.length; i++) {
        result.append("new Array(\"" + tdv[i].getField("id") + "\", \"" + tdv[i].getField("Name")
            + "\")");
        if (i < tdv.length - 1)
          result.append(",\n");
      }
      result.append("\n)");
    } else
      result.append("null");
    result.append("\n)");

    result.append(");");
    xmlDocument.setParameter("array", result.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }
}
