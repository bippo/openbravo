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
package org.openbravo.erpReports;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class RptM_Movement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strmMovementId = vars.getSessionValue("RptM_Movement.inpmMovementId_R");
      if (strmMovementId.equals(""))
        strmMovementId = vars.getSessionValue("RptM_Movement.inpmMovementId");
      printPagePartePDF(response, vars, strmMovementId);
    } else
      pageError(response);
  }

  private void printPagePartePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strmMovementId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptM_Movement")
        .createXmlDocument();
    // here we pass the familiy-ID with report.setData
    RptMMovementData[] data = RptMMovementData.select(this, strmMovementId);
    if (data == null || data.length == 0)
      data = RptMMovementData.set();
    RptMMovementData[][] dataLines = new RptMMovementData[data.length][];

    for (int i = 0; i < data.length; i++) {
      dataLines[i] = RptMMovementData.selectMovement(this, data[i].mMovementId);
      if (dataLines[i] == null || dataLines[i].length == 0)
        dataLines[i] = RptMMovementData.set();
    }
    xmlDocument.setData("structure", data);
    xmlDocument.setDataArray("reportMovementLines", "structure1", dataLines);
    String strResult = xmlDocument.print();
    if (log4j.isDebugEnabled())
      log4j.debug(strResult);
    renderFO(strResult, response);
  }

  public String getServletInfo() {
    return "Servlet that presents the RptMMovement document";
  } // End of getServletInfo() method
}
