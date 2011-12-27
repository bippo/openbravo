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

public class SE_Years_For_Calendar extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strCalendarId = vars.getStringParameter("inpcCalendarId");
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      String strWindowId = vars.getStringParameter("inpwindowId");
      try {
        printPage(response, vars, strWindowId, strCalendarId, strChanged);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strWindowId,
      String strCalendarId, String strChanged) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuilder result = new StringBuilder();
    result.append("var calloutName='SE_Years_For_Calendar';\n\n");
    result.append("var respuesta = new Array(");

    SEPeriodNoData[] tdv = null;
    if (strChanged.equals("inpcCalendarId") && !strCalendarId.equals("")) {
      // Update the years
      try {
        tdv = SEPeriodNoData.getYears(this, strCalendarId);
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
    }
    result.append("new Array(\"inpcYearId\", ");
    if (tdv != null && tdv.length > 0) {
      result.append("new Array(");
      for (int i = 0; i < tdv.length; i++) {
        result.append("new Array(\"" + tdv[i].getField("id") + "\", \"" + tdv[i].getField("Name")
            + "\", \"" + (i == 0 ? "true" : "false") + "\")");
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
