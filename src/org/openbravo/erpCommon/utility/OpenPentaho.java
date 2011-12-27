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

public class OpenPentaho extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String adProcessId = vars.getStringParameter("inpadProcessId");
    String pentahoServer = vars.getSessionValue("#pentahoServer");
    String userRole = vars.getSessionValue("#AD_ROLE_ID");
    if (!hasGeneralAccess(vars, "P", adProcessId))
      bdError(request, response, "AccessTableNoView", vars.getLanguage());
    else if (pentahoServer.equals(""))
      bdError(request, response, "NoPentahoServerDefined", vars.getLanguage());
    else {
      String source = OpenPentahoData.selectSource(this, adProcessId);
      if (source.equals(""))
        bdError(request, response, "NoSourceDefined", vars.getLanguage());
      else
        printPageDataSheet(response, vars, pentahoServer, source, adProcessId, userRole);
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String pentahoServer, String source, String adProcessId, String userRole) throws IOException,
      ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/OpenPentaho").createXmlDocument();
    if (!source.startsWith("/"))
      source = "/" + source;
    source = source + ((source.indexOf("?") != -1) ? "&" : "?") + "ob_role='" + userRole + "'";
    xmlDocument.setParameter("paramURL", pentahoServer + source);

    try {
      WindowTabs tabs = new WindowTabs(this, vars, new Integer(adProcessId).intValue());
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "OpenPentaho.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb(), true);
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "OpenPentaho.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}
