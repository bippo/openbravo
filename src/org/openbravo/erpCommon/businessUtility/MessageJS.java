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

package org.openbravo.erpCommon.businessUtility;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;

public class MessageJS extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    final String strValue = vars.getRequiredStringParameter("inpvalue");
    printPage(response, vars, strValue);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strValue)
      throws IOException, ServletException {

    String type = "Hidden";
    String title = "";
    String description = "";
    String strLanguage = vars.getStringParameter("inplanguage");
    if (strLanguage == null || strLanguage.equals(""))
      strLanguage = vars.getLanguage();
    MessageJSData[] data = null;
    try {
      data = MessageJSData.getMessage(this, strLanguage, strValue);
    } catch (final Exception ex) {
      type = "Error";
      title = "Error";
      description = ex.toString();
    }
    if (data != null && data.length > 0) {
      type = (data[0].msgtype.equals("E") ? "Error" : (data[0].msgtype.equals("I") ? "Info"
          : (data[0].msgtype.equals("S") ? "Success" : "Warning")));
      title = Utility.messageBD(this, type, strLanguage);
      description = "<![CDATA[" + data[0].msgtext + "]]>";
    }

    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");

    // Fixing issue #17486
    // Don't use xmlEngine to prevent translation of message codes
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version='1.0' encoding='UTF-8' ?>\n");
    xml.append("<xml-structure>\n");
    xml.append("  <status>\n");
    xml.append("    <type>").append(type).append("</type>\n");
    xml.append("    <title>").append(title).append("</title>\n");
    xml.append("    <description>").append(description).append("</description>\n");
    xml.append("  </status>\n");
    xml.append("</xml-structure>\n");

    log4j.debug(xml.toString());

    final PrintWriter out = response.getWriter();
    out.println(xml.toString());
    out.close();
  }
}
