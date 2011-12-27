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
package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.xmlEngine.XmlDocument;

public class PoolStatus extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT", "REFRESH")) {
      printPageMenuPoolStatus(response, vars);
    }
  }

  private void printPageMenuPoolStatus(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/PoolStatus").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");

    xmlDocument.setParameter("status", formatearTextoJavascript(getPoolStatus()));
    xmlDocument.setParameter("body", "");
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "SetPriority", false, "", "", "",
        false, "utility", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.ShowSession");
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "PoolStatus.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "PoolStatus.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    {
      OBError myMessage = vars.getMessage("PoolStatus");
      vars.removeMessage("PoolStatus");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  // replaces the linebreak character and the enter carriage with \n and \r,
  // in order to make it identify just the in the second reading
  private String formatearTextoJavascript(String strTexto) {
    int pos;
    while (strTexto.indexOf('\r') != -1) {
      pos = strTexto.indexOf('\r');
      strTexto = strTexto.substring(0, pos) + "<br>"
          + strTexto.substring(pos + 1, strTexto.length());
    }

    while (strTexto.indexOf('\n') != -1) {
      pos = strTexto.indexOf('\n');
      strTexto = strTexto.substring(0, pos) + "<br>"
          + strTexto.substring(pos + 1, strTexto.length());
    }
    return strTexto;

  }

  public String getServletInfo() {
    return "Protected resources Servlet";
  } // end of getServletInfo() method
}
