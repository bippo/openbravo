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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_help;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

public class DisplayHelp extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      if (log4j.isDebugEnabled())
        log4j.debug("BY DEFAULT\n");
      String strKeyId = "";
      String strType = vars.getRequiredStringParameter("inpwindowType");
      if (strType.equals("W"))
        strKeyId = vars.getRequiredStringParameter("inpwindowId");
      printPageDataSheet(response, vars, strKeyId);
    } else {
      pageError(response);
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWindow) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Displayer");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    // out.println(xmlDocument.print());
    out.println(HelpWindow.generateWindow(this, xmlEngine, vars, false, strWindow));
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents window's help";
  } // end of getServletInfo() method
}
