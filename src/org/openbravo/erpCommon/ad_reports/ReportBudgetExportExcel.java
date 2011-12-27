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

package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportBudgetExportExcel extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strKey = vars.getRequiredGlobalVariable("inpcBudgetId",
          "ReportBudgetGenerateExcel|inpcBudgetId");
      printPageDataExportExcel(response, vars, strKey);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataExportExcel(HttpServletResponse response, VariablesSecureApp vars,
      String strBudgetId) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: EXCEL");

    vars.removeSessionValue("ReportBudgetGenerateExcel|inpTabId");

    response.setContentType("application/xls; charset=UTF-8");
    PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;
    ReportBudgetGenerateExcelData[] data = null;
    data = ReportBudgetGenerateExcelData.selectLines(this, vars.getLanguage(), strBudgetId);

    if (data.length != 0 && data[0].exportactual.equals("Y")) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportBudgetGenerateExcelXLS").createXmlDocument();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportBudgetGenerateExcelExportXLS")
          .createXmlDocument();
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());

  }

  public String getServletInfo() {
    return "Servlet ReportBudgetGenerateExcel.";
  } // end of getServletInfo() method
}
