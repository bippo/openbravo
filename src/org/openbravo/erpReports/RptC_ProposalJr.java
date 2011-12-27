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
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;

public class RptC_ProposalJr extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strClave = vars.getSessionValue("RptC_ProposalJr.inpcProjectproposalId_R");
      if (strClave.equals(""))
        strClave = vars.getSessionValue("RptC_ProposalJr.inpcProjectproposalId");
      printPagePartePDF(response, vars, strClave);
    } else
      pageError(response);
  }

  private void printPagePartePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strClave) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf - ID:" + strClave);
    RptCProposalJrData[] data = RptCProposalJrData.select(this, strClave,
        Utility.getContext(this, vars, "#User_Client", "RptC_ProposalJr"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "RptC_ProposalJr"));
    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

    JasperReport jasperReportLines;
    try {
      JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
          + "/org/openbravo/erpReports/SubreportLines.jrxml");
      jasperReportLines = JasperCompileManager.compileReport(jasperDesignLines);
    } catch (JRException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("SR_LINES", jasperReportLines);

    String strOutput = "pdf";
    String strReportName = "@basedesign@/org/openbravo/erpReports/RptC_ProposalJr.jrxml";
    renderJR(vars, response, strReportName, strOutput, parameters, data, null);
  }

  public String getServletInfo() {
    return "Servlet that presents the RptCOrders seeker";
  } // End of getServletInfo() method
}
