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
import org.openbravo.erpCommon.utility.DateTimeData;

public class RptC_Invoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcInvoiceId = vars.getSessionValue("RptC_Invoice.inpcInvoiceId_R");
      if (strcInvoiceId.equals(""))
        strcInvoiceId = vars.getSessionValue("RptC_Invoice.inpcInvoiceId");
      printPagePDF(response, vars, strcInvoiceId);
    } else if (vars.commandIn("FIND")) {
      String strbPartnerId = vars.getStringParameter("inpcBpartnerId");
      String strDateTo = vars.getStringParameter("inpDateInvoiceFrom");
      String strDateFrom = vars.getStringParameter("inpDateInvoiceTo");
      String strDocNoFrom = vars.getStringParameter("inpInvoicedocumentnoFrom");
      String strDocNoTo = vars.getStringParameter("inpInvoicedocumentnoTo");
      String strcInvoiceId = "";
      RptCInvoiceData[] data2 = RptCInvoiceData.select(this, strDocNoFrom, strDocNoTo,
          strbPartnerId, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"));
      int j;
      for (j = 0; j < data2.length; j++) {
        if (j != 0)
          strcInvoiceId += ",";
        strcInvoiceId += data2[j].cInvoiceId;
      }
      strcInvoiceId = "(" + strcInvoiceId + ")";
      printPagePDF(response, vars, strcInvoiceId);
    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strcInvoiceId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");

    if (log4j.isDebugEnabled())
      log4j.debug("printPagePDF strInvoiceId = " + strcInvoiceId);

    RptCInvoiceHeaderData[] pdfInvoicesData = RptCInvoiceHeaderData.select(this, strcInvoiceId);

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("C_INVOICE_ID", strcInvoiceId);

    String currencyCode = pdfInvoicesData[0].currencyCode;
    // String currencySymbol=pdfInvoicesData[0].symbol;

    parameters.put("CURRENCYSYMBOL", currencyCode);

    JasperReport jasperReportLines;
    try {
      JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
          + "/org/openbravo/erpReports/RptC_Invoice_Lines.jrxml");
      jasperReportLines = JasperCompileManager.compileReport(jasperDesignLines);
    } catch (JRException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }
    parameters.put("SR_LINES_1", jasperReportLines);

    try {
      JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
          + "/org/openbravo/erpReports/RptC_Invoice_TaxLines.jrxml");
      jasperReportLines = JasperCompileManager.compileReport(jasperDesignLines);
    } catch (JRException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }
    parameters.put("SR_LINES_2", jasperReportLines);

    String strReportName = "@basedesign@/org/openbravo/erpReports/RptC_Invoice.jrxml";
    response.setHeader("Content-disposition", "inline; filename=RptC_Invoice.pdf");
    renderJR(vars, response, strReportName, "pdf", parameters, pdfInvoicesData, null);
  }

  public String getServletInfo() {
    return "Servlet that presents the RptCOrders seeker";
  } // End of getServletInfo() method
}
