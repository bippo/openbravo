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
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class RptC_Settlement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcSettlementId = vars.getSessionValue("RptC_Settlement.inpcSettlementId_R");
      if (strcSettlementId.equals(""))
        strcSettlementId = vars.getSessionValue("RptC_Settlement.inpcSettlementId");
      printPagePDF(response, vars, strcSettlementId);
    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strcSettlementId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_Settlement")
        .createXmlDocument();

    RptCSettlementData[] pdfSettlementData = RptCSettlementData.select(this,
        Utility.getContext(this, vars, "#User_Client", "RptC_Settlement"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "RptC_Settlement"), strcSettlementId);

    if (pdfSettlementData == null || pdfSettlementData.length == 0)
      pdfSettlementData = RptCSettlementData.set();

    RptCSettlementHeaderData[][] pdfSettlementHeaderData = new RptCSettlementHeaderData[pdfSettlementData.length][];

    RptCSettlementLinesData[][] pdfSettlementLinesData = new RptCSettlementLinesData[pdfSettlementData.length][];

    for (int i = 0; i < pdfSettlementData.length; i++) {
      pdfSettlementHeaderData[i] = RptCSettlementHeaderData.select(this,
          pdfSettlementData[i].cSettlementId);
      if (pdfSettlementHeaderData[i] == null || pdfSettlementHeaderData[i].length == 0)
        RptCSettlementHeaderData.set();
      pdfSettlementLinesData[i] = RptCSettlementLinesData.select(this,
          pdfSettlementData[i].cSettlementId);
      if (pdfSettlementLinesData[i] == null || pdfSettlementLinesData[i].length == 0)
        pdfSettlementLinesData[i] = RptCSettlementLinesData.set();
    }

    xmlDocument.setData("structure1", pdfSettlementData);
    xmlDocument.setDataArray("reportSettlementHeader", "structureSettlementHeader",
        pdfSettlementHeaderData);
    xmlDocument.setDataArray("reportSettlementLines", "structureSettlementLines",
        pdfSettlementLinesData);

    String strResult = xmlDocument.print();
    renderFO(strResult, response);
  }

  public String getServletInfo() {
    return "Servlet that presents the RptCOrders seeker";
  } // End of getServletInfo() method
}
