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

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.SystemInfo;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class Registration extends HttpSecureAppServlet {
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

    // removeFromPageHistory(request);
    String checkString = "";
    if (vars.commandIn("DEFAULT")) {
      printPageDataSheet(response, vars);
    } else if (vars.commandIn("REGISTER")) {
      checkString = "REGISTER";
      RegisterData.updateIsRegistrationActive(myPool, "Y");
    } else if (vars.commandIn("DISABLE")) {
      checkString = "DISABLE";
      RegisterData.updateIsRegistrationActive(myPool, "N");
    } else if (vars.commandIn("POSTPONE")) {
      checkString = "POSTPONE";
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, 3);
      final String date = new SimpleDateFormat(vars.getJavaDateFormat()).format(cal.getTime());
      RegisterData.postpone(myPool, date);
    } else {
      pageError(response);
    }

    if (vars.commandIn("REGISTER") || vars.commandIn("DISABLE") || vars.commandIn("POSTPONE")) {
      response.setContentType("text/plain; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      out.print(checkString);
      out.close();
    }
  }

  /**
   * Removes the Registration pop-up from the page history so when Openbravo back arrow is pressed,
   * Registration window has no chance of being shown.
   * 
   * @param request
   *          the HttpServletRequest object
   * 
   *          public void removeFromPageHistory(HttpServletRequest request) { final Variables
   *          variables = new Variables(request); final String sufix =
   *          variables.getCurrentHistoryIndex(); variables.removeSessionValue("reqHistory.servlet"
   *          + sufix); variables.removeSessionValue("reqHistory.path" + sufix);
   *          variables.removeSessionValue("reqHistory.command" + sufix);
   *          variables.downCurrentHistoryIndex(); }
   */

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/Registration")
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("welcome",
        Utility.formatMessageBDToHtml(Utility.messageBD(this, "REG_WELCOME", vars.getLanguage())));

    // Building registration URL js variable
    String url = "var url = 'http://www.openbravo.com/embedreg/form";
    url += "?system_id=" + SystemInfo.getSystemIdentifier();
    url += "&database_id=" + SystemInfo.getDBIdentifier();
    url += "&mac_id=" + SystemInfo.getMacAddress();
    url += "';";

    xmlDocument.setParameter("url", url);

    out.println(xmlDocument.print());
    out.close();
  }

  @Override
  public String getServletInfo() {
    return "Registration form servlet.";
  } // end of getServletInfo() method
}
