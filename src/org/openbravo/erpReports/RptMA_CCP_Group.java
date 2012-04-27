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

public class RptMA_CCP_Group extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strmaCcpGroup = vars.getSessionValue("RptMA_CCP_Group.inpmaCcpGroup_R");
      if (strmaCcpGroup.equals(""))
        strmaCcpGroup = vars.getSessionValue("RptMA_CCP_Group.inpmaCcpGroupId");
      printPagePartePDF(request, response, vars, strmaCcpGroup);
    } else
      pageError(response);
  }

  private void printPagePartePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strmaCcpGroup) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptMA_CCP_Group")
        .createXmlDocument();
    // here we pass the familiy-ID with report.setData
    RptMACCPGroupData[] data1 = RptMACCPGroupData.select(this, strmaCcpGroup);
    if (data1 == null || data1.length == 0)
      data1 = RptMACCPGroupData.set();

    int length = 0;
    for (int i = 0; i < data1.length; i++) {
      length += Integer.valueOf(data1[i].valuenumber).intValue();
    }

    RptMACCPGroupData[] data2 = new RptMACCPGroupData[length];
    int k = 0;
    for (int i = 0; i < data1.length; i++) {
      for (int j = 0; j < Integer.valueOf(data1[i].valuenumber).intValue(); j++) {
        data2[k] = data1[i];
        k++;
      }
    }

    xmlDocument.setData("structure1", data2);
    String strResult = xmlDocument.print();
    if (log4j.isDebugEnabled())
      log4j.debug(strResult);
    renderFO(strResult, request, response);
  }

  public String getServletInfo() {
    return "Servlet that presents the RptMACcp seeker";
  } // End of getServletInfo() method
}
