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

package org.openbravo.erpCommon.utility;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FileUtility;

public class DownloadReport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String report = vars.getStringParameter("report");
    downloadReport(vars, response, report);
  }

  private void downloadReport(VariablesSecureApp vars, HttpServletResponse response, String report)
      throws IOException, ServletException {

    if (report.contains("..") || report.contains(File.separator))
      throw new ServletException("Invalid report name");

    FileUtility f = new FileUtility(globalParameters.strFTPDirectory, report, false, true);
    if (!f.exists())
      return;
    int pos = report.indexOf("-");
    String filename = report.substring(0, pos);
    pos = report.lastIndexOf(".");
    String extension = report.substring(pos);
    if (extension.equalsIgnoreCase(".pdf")) {
      response.setContentType("application/pdf");
    } else if (extension.equalsIgnoreCase(".csv")) {
      response.setContentType("text/csv");
    } else if (extension.equalsIgnoreCase(".xls")) {
      response.setContentType("application/vnd.ms-excel");
    } else {
      response.setContentType("application/x-download");
    }
    response.setHeader("Content-Disposition", "attachment; filename=" + filename + extension);
    f.dumpFile(response.getOutputStream());
    response.getOutputStream().flush();
    response.getOutputStream().close();
    if (!f.deleteFile())
      log4j.error("Download report could not delete the file :" + report);
  }
}
