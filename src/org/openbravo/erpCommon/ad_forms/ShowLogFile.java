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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class ShowLogFile extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    final VariablesSecureApp vars = new VariablesSecureApp(request);
    String filePath = vars.getStringParameter("filePath");

    try {
      Long.parseLong(filePath);
    } catch (NumberFormatException e) {
      throw new ServletException("Invalid file path");
    }
    File file = new File(vars.getSessionValue("#sourcePath") + "/log/" + filePath + "-apply.log");

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ShowLogFile").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("fileName", "Viewing file: " + file.getName());
    if (file.exists()) {
      StringBuilder contents = new StringBuilder();
      try {
        BufferedReader input = new BufferedReader(new FileReader(file));
        try {
          String line = null;
          while ((line = input.readLine()) != null) {
            contents.append(line);
            contents.append(System.getProperty("line.separator"));
          }
        } finally {
          input.close();
        }
      } catch (IOException ex) {
        log4j.error("Error while reading file", ex);
      }
      xmlDocument.setParameter("textContent", contents.toString());
    }
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    out.println(xmlDocument.print());

    out.close();
  }
}
