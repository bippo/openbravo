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
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportShipper extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strFrom = vars.getGlobalVariable("inpFrom", "ReportShipper|From", "");
      String strTo = vars.getGlobalVariable("inpTo", "ReportShipper|To", "");
      String strShipper = vars.getGlobalVariable("inpShipper", "ReportShipper|Shipper", "");
      String strShipperReport = vars.getGlobalVariable("inpShipperReport", "ReportShipper|all",
          "all");
      String strDetail = vars.getGlobalVariable("inpDetail", "ReportShipper|Detail", "N");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportShipper|currency",
          strUserCurrencyId);
      printPageDataSheet(request, response, vars, strFrom, strTo, strShipper, strShipperReport,
          strDetail, strCurrencyId);

    } else if (vars.commandIn("FIND")) {
      String strFrom = vars.getRequestGlobalVariable("inpFrom", "ReportShipper|From");
      String strTo = vars.getRequestGlobalVariable("inpTo", "ReportShipper|To");
      String strShipper = vars.getRequestGlobalVariable("inpShipper", "ReportShipper|Shipper");
      String strShipperReport = vars.getStringParameter("inpShipperReport");
      String strDetail = vars.getRequestGlobalVariable("inpDetail", "ReportShipper|Detail");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId", "ReportShipper|currency",
          strUserCurrencyId);
      printPageDataSheet(request, response, vars, strFrom, strTo, strShipper, strShipperReport,
          strDetail, strCurrencyId);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strFrom, String strTo, String strShipper,
      String strShipperReport, String strDetail, String strCurrencyId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportShipperData[] data = null;
    ReportShipperData[][] dataLine = null;

    String discard[] = { "" };
    if (!strDetail.equals("Y"))
      discard[0] = "reportLine";
    if (vars.commandIn("DEFAULT"))
      discard[0] = "selEliminar";

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportShipper",
        discard).createXmlDocument();

    String strIsSOTrx = "";
    if (strShipperReport.equalsIgnoreCase("sale"))
      strIsSOTrx = "Y";
    else if (strShipperReport.equalsIgnoreCase("purchase"))
      strIsSOTrx = "N";
    else if (strShipperReport.equalsIgnoreCase("all"))
      strIsSOTrx = "";

    if (log4j.isDebugEnabled())
      log4j.debug("****data passed from: " + strFrom + " to: " + strTo + " shiper: " + strShipper
          + " isso " + strIsSOTrx + " det " + strDetail);
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    if (vars.commandIn("FIND")) {
      // Checks if there is a conversion rate for each of the transactions of
      // the report
      String strBaseCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
      try {
        data = ReportShipperData.select(this, vars.getLanguage(), strCurrencyId, strBaseCurrencyId,
            strFrom, strTo, strShipper, strIsSOTrx);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an error
      // message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        dataLine = new ReportShipperData[0][0];
        if (data != null && data.length > 0) {
          dataLine = new ReportShipperData[data.length][];

          for (int i = 0; i < data.length; i++) {
            if (log4j.isDebugEnabled())
              log4j.debug("shipment " + data[i].shipmentid);
            dataLine[i] = ReportShipperData
                .selectLine(this, vars.getLanguage(), data[i].shipmentid);
            // if (RawMaterialData[i] == null ||
            // RawMaterialData[i].length == 0) RawMaterialData[i] =
            // ReportRawMaterialData.set();
          }
        } // else dataLine[0] = ReportShipperData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportShipper", false, "", "", "",
          false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_reports.ReportShipper");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportShipper.html",
            classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportShipper.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        myMessage = vars.getMessage("ReportShipper");
        vars.removeMessage("ReportShipper");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("paramFrom", strFrom);
      xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("paramTo", strTo);
      xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("sale", strShipperReport);
      xmlDocument.setParameter("purchase", strShipperReport);
      xmlDocument.setParameter("all", strShipperReport);
      xmlDocument.setParameter("paramDetalle", strDetail);

      xmlDocument.setParameter("paramShipper", strShipper);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Shipper_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportShipper"),
            Utility.getContext(this, vars, "#User_Client", "ReportShipper"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportShipper", strShipper);
        xmlDocument.setData("reportShipper", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("ccurrencyid", strCurrencyId);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportShipper"),
            Utility.getContext(this, vars, "#User_Client", "ReportShipper"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportShipper", strCurrencyId);
        xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setData("structure", data);
      xmlDocument.setDataArray("reportLine", "structureLine", dataLine);
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet ReportShipper.";
  } // end of getServletInfo() method
}
