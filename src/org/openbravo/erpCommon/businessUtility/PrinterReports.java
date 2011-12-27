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
package org.openbravo.erpCommon.businessUtility;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class PrinterReports extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strDirectPrint = vars.getStringParameter("inpdirectprint", "N");
      String strPDFPath = vars.getStringParameter("inppdfpath");
      String strHiddenKey = vars.getStringParameter("inphiddenkey");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strKeyColumnId = vars.getStringParameter("inpkeyColumnId");
      String inptabId = vars.getStringParameter("inpTabId");
      String strHiddenValue = vars.getGlobalVariable("inphiddenvalue", strWindowId + "|"
          + strKeyColumnId);
      printPage(response, vars, strDirectPrint, strPDFPath, strHiddenKey, strHiddenValue, inptabId);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strDirectPrint, String strPDFPath, String strHiddenKey, String strHiddenValue,
      String inptabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    String[] discard = { "isPrintPreview" };
    if (strDirectPrint.equals("N"))
      discard[0] = new String("isDirectPrint");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/PrinterReports", discard).createXmlDocument();
    String mapping = "";
    if (strPDFPath.startsWith("..")) {
      strPDFPath = strPDFPath.substring(2);
      mapping = strPDFPath;
      strPDFPath = FormatUtilities.replace(PrinterReportsData.select(this, strPDFPath));
    } else
      mapping = PrinterReportsData.selectMapping(this, strPDFPath);

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("pdfPath", mapping);
    xmlDocument.setParameter("directPrint", strDirectPrint);
    // if (strPDFPath.startsWith("..")) strPDFPath =
    // strPDFPath.substring(2);

    // String mapping =
    // FormatUtilities.replace(PrinterReportsData.select(this, strPDFPath));
    strPDFPath = FormatUtilities.replace(strPDFPath);
    vars.setSessionValue("inpTabID", inptabId);
    final String hiddenValue = quouteIds(strHiddenValue);
    vars.setSessionValue(strPDFPath + "." + strHiddenKey, "(" + hiddenValue + ")");
    if (!strHiddenValue.equals(""))
      vars.setSessionValue(strPDFPath + "." + strHiddenKey, "(" + hiddenValue + ")");
    else
      vars.getRequestInGlobalVariable(strHiddenKey, strPDFPath + "." + strHiddenKey,
          IsIDFilter.instance);

    // vars.getRequestInGlobalVariable(strHiddenKey + "_R", mapping + "." +
    // strHiddenKey + "_R");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String quouteIds(String idList) throws ServletException {
    final String[] ids = idList.split(",");
    final StringBuilder quoted = new StringBuilder();
    for (int i = 0; i < ids.length; i++) {
      if (!IsIDFilter.instance.accept(ids[i])) {
        log4j.error("Input: " + idList + " not accepted by filter: IsIDFilter");
        throw new ServletException("Input: " + idList + " is not an accepted input");
      }
      if (i > 0) {
        quoted.append(",");
      }
      quoted.append("'").append(ids[i]).append("'");
    }
    return quoted.toString();
  }
}
