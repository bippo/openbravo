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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.KeyMap;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportVatRegisterJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strTaxRegId = vars.getSessionValue("JR_ReportVATRegister.inpcTaxregisterId");
      String strTaxRegId_Clear = strTaxRegId.replace("(", "").replace(")", "").replace("'", "");
      TaxRegisterData[] taxreg = TaxRegisterData.select(this, strTaxRegId_Clear);
      String strTaxPayId = taxreg[0].cTaxpaymentId;

      TaxPaymentData[] taxpaym = TaxPaymentData.select(this, strTaxPayId);
      String strDateFrom = taxpaym[0].datefrom;
      String strDateTo = taxpaym[0].dateto;
      printPageDataSheet(response, vars, strDateFrom, strDateTo);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strTaxRegId = vars.getSessionValue("JR_ReportVATRegister.inpcTaxregisterId");
      String strTaxRegId_Clear = strTaxRegId.replace("(", "").replace(")", "").replace("'", "");
      TaxRegisterData[] taxreg = TaxRegisterData.select(this, strTaxRegId_Clear);
      String strTaxPayId = taxreg[0].cTaxpaymentId;

      TaxPaymentData[] taxpaym = TaxPaymentData.select(this, strTaxPayId);
      String strDateFrom = taxpaym[0].datefrom;
      String strDateTo = taxpaym[0].dateto;

      String strcTypeVatReport = vars.getRequestGlobalVariable("inpTypeVatReport",
          "ReportVatRegisterJR|TypeVatReport");

      if (strcTypeVatReport.equals("01")) {
        // ReportTransactions
        printReportJRRegisterLine(response, vars, strDateFrom, strDateTo, strTaxPayId,
            strTaxRegId_Clear, strcTypeVatReport);
      } else {
        // Standard Report
        bdError(request, response, "TypeReportCantBeNull", vars.getLanguage());
        return;
      }

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    // String strMessage = "";

    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/ReportRegisterFilter")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportRegisterFilter", false, "", "",
        "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, "ReportRegisterFilter.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpReports.TaxPayment");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportRegisterFilter.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportRegisterFilter.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportRegisterFilter");
      vars.removeMessage("ReportRegisterFilter");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");

    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat",
        vars.getSessionValue("#AD_JavaDateTimeFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_JavaDateTimeFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_JavaDateTimeFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_JavaDateTimeFormat"));

    TypeReportRegister[] aTypeVatReport = TypeReportRegister.set();
    xmlDocument.setData("reportTypeVatReport", "liststructure", aTypeVatReport);
    xmlDocument.setParameter("TypeVatReport", "01");
    out.println(xmlDocument.print());
    out.close();
  }

  private void printReportJRRegisterLine(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String TaxPayId, String TaxRegId, String strTypeReport)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");

    ReportRegisterLineJRData[] data = null;
    data = ReportRegisterLineJRData.select(this, "", "", TaxPayId, TaxRegId);

    String strOutput = vars.commandIn("EDIT_HTML") ? "html" : "pdf";
    String strReportName = "@basedesign@/org/openbravo/erpReports/ReportRegisterLineJR.jrxml";
    String StartPageNo = TaxRegisterData.selectPageNoPrior(this, TaxRegId);
    Integer IntStartPageNo = new Integer(StartPageNo);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_SUBTITLE", "From " + strDateFrom + " to " + strDateTo);
    parameters.put("invoicedateDA", strDateFrom);
    parameters.put("invoicedateA", strDateTo);
    parameters.put("StartPageNo", IntStartPageNo.intValue());

    HashMap<Object, Object> outparameters = new HashMap<Object, Object>();
    renderJR(vars, response, strReportName, strOutput, parameters, data, outparameters);
    // Read pageno
    Collection<Object> coll = outparameters.values();
    Object object;
    JasperPrint jr1 = new JasperPrint();
    String scl = "";
    for (Iterator<Object> iterator = coll.iterator(); iterator.hasNext();) {
      object = (Object) iterator.next();
      scl = object.getClass().toString();
      if (scl.contains("JasperPrint")) {
        jr1 = (JasperPrint) object;
      }
    }
    ;

    Integer pag1 = new Integer(jr1.getPages().size() + IntStartPageNo.intValue());
    TaxRegisterData[] taxregister = TaxRegisterData.select(this, TaxRegId);
    if ((taxregister[0].pageno.equals(new String("0"))) || (taxregister[0].pageno == null)) {
      TaxRegisterData.updatePageNo(this, pag1.toString(), TaxRegId);
    }
    // JasperPrint object1 = new JasperPrint();
    // JasperPrint x = (JasperPrint) outparameters.get(object1);
    // int pag = x.getPages().size();
  }

  public String getServletInfo() {
    return "Servlet ReportVatRegisterJR.";
  } // end of getServletInfo() method

}

class TypeReportRegister implements FieldProvider {
  static Logger log4j = Logger.getLogger(TypeReportRegister.class);
  // private String InitRecordNumber = "0";
  public String id;
  public String name;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("ID"))
      return id;
    else if (fieldName.equalsIgnoreCase("NAME"))
      return name;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  static TypeReportRegister[] set() throws ServletException {
    TypeReportRegister objectTypeReport[] = new TypeReportRegister[1];
    // first
    objectTypeReport[0] = new TypeReportRegister();
    objectTypeReport[0].id = "01";
    objectTypeReport[0].name = "Report Tax Register";

    return objectTypeReport;
  }

}