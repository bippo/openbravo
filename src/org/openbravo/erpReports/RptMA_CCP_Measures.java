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

public class RptMA_CCP_Measures extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strmaMeasureShift = vars.getSessionValue("RptMA_CCP_Measures.inpmaMeasureShift_R");
      if (strmaMeasureShift.equals(""))
        strmaMeasureShift = vars.getSessionValue("RptMA_CCP_Measures.inpmaMeasureShiftId");
      printPagePartePDF(request, response, vars, strmaMeasureShift);
    } else
      pageError(response);
  }

  private void printPagePartePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strmaMeasureShift) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpReports/RptMA_CCP_Measures").createXmlDocument();
    // here we pass the familiy-ID with report.setData
    RptMACCPMeasuresData[] data = RptMACCPMeasuresData.select(this, strmaMeasureShift);
    if (data == null || data.length == 0)
      data = RptMACCPMeasuresData.set();

    RptMACCPMeasuresHoursData[][] dataHours = new RptMACCPMeasuresHoursData[data.length][10];
    RptMACCPMeasuresValuesData[][] dataValues = new RptMACCPMeasuresValuesData[data.length][];

    for (int i = 0; i < data.length; i++) {
      dataHours[i] = RptMACCPMeasuresHoursData.select(this, data[i].groupid);
      if (dataHours[i] == null || dataHours[i].length == 0)
        dataHours[i] = new RptMACCPMeasuresHoursData[0];

      dataValues[i] = RptMACCPMeasuresValuesData.select(this, data[i].groupid);
      if (dataValues[i] == null || dataValues[i].length == 0)
        dataValues[i] = new RptMACCPMeasuresValuesData[0];
    }
    xmlDocument.setData("structure1", data);
    xmlDocument.setDataArray("reportHours", "structureHours", dataHours);
    xmlDocument.setDataArray("reportValues", "structureValues", dataValues);
    String strResult = xmlDocument.print();
    if (log4j.isDebugEnabled())
      log4j.debug(strResult);
    renderFO(strResult, request, response);
  }

  public String getServletInfo() {
    return "Servlet that presents the RptMACcp seeker";
  } // End of getServletInfo() method
}
