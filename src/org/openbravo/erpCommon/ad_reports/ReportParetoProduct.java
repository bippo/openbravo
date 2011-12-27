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
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportParetoProduct extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strWarehouse = vars.getGlobalVariable("inpmWarehouseId",
          "ReportParetoProduct|M_Warehouse_ID", "");
      String strClient = vars.getClient();
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId", "ReportParetoProduct|AD_Org_ID",
          "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportParetoProduct|currency", strUserCurrencyId);
      printPageDataSheet(request, response, vars, strWarehouse, strAD_Org_ID, strClient,
          strCurrencyId);
    } else if (vars.commandIn("FIND")) {
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportParetoProduct|M_Warehouse_ID");
      String strClient = vars.getClient();
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "ReportParetoProduct|AD_Org_ID");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportParetoProduct|currency", strUserCurrencyId);
      printPageDataSheet(request, response, vars, strWarehouse, strAD_Org_ID, strClient,
          strCurrencyId);
    } else if (vars.commandIn("GENERATE")) {
      String strClient = vars.getClient();
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportParetoProduct|M_Warehouse_ID");
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "ReportParetoProduct|AD_Org_ID");
      OBError myMessage = mUpdateParetoProduct(vars, strWarehouse, strAD_Org_ID, strClient);
      myMessage.setTitle("");
      myMessage.setType("Success");
      myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      vars.setMessage("ReportParetoProduct", myMessage);
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportParetoProduct|currency", strUserCurrencyId);
      printPageDataSheet(request, response, vars, strWarehouse, strAD_Org_ID, strClient,
          strCurrencyId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strWarehouse, String strAD_Org_ID, String strClient,
      String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportParetoProductData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportParetoProduct", discard).createXmlDocument();
    if (vars.commandIn("FIND")) {
      // Checks if there is a conversion rate for each of the transactions
      // of the report
      String strBaseCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        data = ReportParetoProductData.select(this, strWarehouse, strClient, vars.getLanguage(),
            strBaseCurrencyId, strCurrencyId, strAD_Org_ID);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an
      // error message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (data == null || data.length == 0) {
          discard[0] = "selEliminar";
          data = ReportParetoProductData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReportParetoProductData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      // Load Toolbar
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportParetoProduct", false, "", "",
          "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      // Create WindowTabs
      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_reports.ReportParetoProduct");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportParetoProduct.html",
            classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportParetoProduct.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      // Load Message Area
      {
        OBError myMessage = vars.getMessage("ReportParetoProduct");
        vars.removeMessage("ReportParetoProduct");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      // Pass parameters to the window
      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");

      // Load Business Partner Group combo with data
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportParetoProduct"), Utility.getContext(this, vars, "#User_Client",
                "ReportParetoProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportParetoProduct",
            strWarehouse);
        xmlDocument.setData("reportM_Warehouse_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
            "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportParetoProduct"),
            Utility.getContext(this, vars, "#User_Client", "ReportParetoProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportParetoProduct",
            strAD_Org_ID);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("ccurrencyid", strCurrencyId);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportParetoProduct"),
            Utility.getContext(this, vars, "#User_Client", "ReportParetoProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportParetoProduct",
            strCurrencyId);
        xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter(
          "warehouseArray",
          Utility.arrayDobleEntrada(
              "arrWarehouse",
              ReportParetoProductData.selectWarehouseDouble(this,
                  Utility.getContext(this, vars, "#User_Client", "ReportParetoProduct"))));

      xmlDocument.setParameter("mWarehouseId", strWarehouse);
      xmlDocument.setParameter("adOrg", strAD_Org_ID);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private OBError mUpdateParetoProduct(VariablesSecureApp vars, String strWarehouse,
      String strAD_Org_ID, String strAD_Client_ID) throws IOException, ServletException {
    String pinstance = SequenceIdData.getUUID();

    PInstanceProcessData.insertPInstance(this, pinstance, "1000500001", "0", "N", vars.getUser(),
        vars.getClient(), vars.getOrg());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "1", "m_warehouse_id", strWarehouse,
        vars.getClient(), vars.getOrg(), vars.getUser());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "2", "ad_org_id", strAD_Org_ID,
        vars.getClient(), vars.getOrg(), vars.getUser());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "3", "ad_client_id",
        strAD_Client_ID, vars.getClient(), vars.getOrg(), vars.getUser());
    ReportParetoProductData.mUpdateParetoProduct0(this, pinstance);

    PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
    OBError myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
    return myMessage;
  }

  public String getServletInfo() {
    return "Servlet ReportParetoProduct info. Insert here any relevant information";
  } // end of getServletInfo() method
}
