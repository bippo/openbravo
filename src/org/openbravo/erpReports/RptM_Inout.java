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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
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

public class RptM_Inout extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strmInoutId = vars.getSessionValue("RptM_Inout.inpmInoutId_R");
      if (strmInoutId.equals(""))
        strmInoutId = vars.getSessionValue("RptM_Inout.inpmInoutId");
      printPagePartePDF(request, response, vars, strmInoutId);
    } else
      pageError(response);
  }

  private void printPagePartePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strmInoutId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptM_Inout")
        .createXmlDocument();

    RptMInoutData[] data = RptMInoutData.select(this, strmInoutId);
    String strCopies = minimumOne(RptMInoutData.selectCopies(this, strmInoutId));
    RptMInoutData[] dataPrincipal = RptMInoutData.selectNumCopies(this,
        Integer.toString(data.length));
    RptMInoutHeaderData[][] dataHeader = new RptMInoutHeaderData[data.length][];
    RptMInoutLinesData[][] dataLines = new RptMInoutLinesData[data.length][];
    int contador = 0;
    for (int i = 0; i < data.length; i++) {
      String strDocumentCopies = minimumOne(RptMInoutData.selectDocumentcopies(this,
          data[i].mInoutId));
      for (int j = 0; j < Integer.valueOf(strDocumentCopies).intValue(); j++) {
        dataHeader[contador] = RptMInoutHeaderData.select(this, data[i].mInoutId);
        if (dataHeader[contador] == null || dataHeader[contador].length == 0)
          dataHeader[j] = new RptMInoutHeaderData[0];
        dataLines[contador] = RptMInoutLinesData.select(this, data[i].mInoutId);
        if (dataLines[contador] == null || dataLines[contador].length == 0)
          dataLines[j] = new RptMInoutLinesData[0];
        contador++;
      }
    }
    xmlDocument.setData("structure1", dataPrincipal);
    xmlDocument.setDataArray("reportInoutHeader", "structure1", dataHeader);
    xmlDocument.setDataArray("reportInoutLines", "structure2", dataLines);
    String strResult = xmlDocument.print();
    renderFO(strResult, request, response);
  }

  private String minimumOne(String strCopies) {
    if (strCopies == null || strCopies.length() == 0 || strCopies.equals("0")) {
      strCopies = "1";
    }
    return strCopies;
  }

  public String getServletInfo() {
    return "Servlet that presents the RptMInout seeker";
  } // End of getServletInfo() method
}
