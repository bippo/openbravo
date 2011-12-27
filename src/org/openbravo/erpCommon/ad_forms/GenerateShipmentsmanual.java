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

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.ActionButtonData;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class GenerateShipmentsmanual extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "GenerateShipmentsmanual|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "GenerateShipmentsmanual|DateTo", "");
      String strC_BPartner_ID = vars.getGlobalVariable("inpcBpartnerId",
          "GenerateShipmentsmanual|C_BPartner_ID", "");
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "GenerateShipmentsmanual|AD_Org_ID", vars.getOrg());
      vars.setSessionValue("GenerateShipmentsmanual|isSOTrx", "Y");
      printPageDataSheet(response, vars, strC_BPartner_ID, strAD_Org_ID, strDateFrom, strDateTo);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "GenerateShipmentsmanual|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "GenerateShipmentsmanual|DateTo");
      String strC_BPartner_ID = vars.getRequestGlobalVariable("inpcBpartnerId",
          "GenerateShipmentsmanual|C_BPartner_ID");
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "GenerateShipmentsmanual|AD_Org_ID");
      printPageDataSheet(response, vars, strC_BPartner_ID, strAD_Org_ID, strDateFrom, strDateTo);
    } else if (vars.commandIn("GENERATE")) {
      myMessage = new OBError();
      myMessage.setTitle("");
      String strSalesOrder = vars.getRequiredInStringParameter("inpOrder", IsIDFilter.instance);

      GenerateShipmentsmanualData.update(this);
      GenerateShipmentsmanualData.updateSelection(this, strSalesOrder);

      String pinstance = SequenceIdData.getUUID();

      PInstanceProcessData.insertPInstance(this, pinstance, "199", "0", "N", vars.getUser(),
          vars.getClient(), vars.getOrg());
      PInstanceProcessData.insertPInstanceParam(this, pinstance, "1", "Selection", "Y",
          vars.getClient(), vars.getOrg(), vars.getUser());
      ActionButtonData.process199(this, pinstance);

      try {
        PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
        myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
      } catch (Exception e) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
        e.printStackTrace();
        log4j.warn("Error");
      }
      GenerateShipmentsmanualData.updateReset(this, strSalesOrder);

      if (log4j.isDebugEnabled())
        log4j.debug(myMessage.getMessage());
      // new message system
      vars.setMessage("GenerateShipmentsmanual", myMessage);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strC_BPartner_ID, String strAD_Org_ID, String strDateFrom, String strDateTo)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "sectionDetail" };
    XmlDocument xmlDocument = null;

    GenerateShipmentsmanualData[] data = null;
    String strTreeOrg = GenerateShipmentsmanualData.treeOrg(this, vars.getClient());
    if (strC_BPartner_ID.equals("") && strAD_Org_ID.equals("")) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/GenerateShipmentsmanual", discard).createXmlDocument();
      data = GenerateShipmentsmanualData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/GenerateShipmentsmanual").createXmlDocument();
      data = GenerateShipmentsmanualData.select(this, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", "GenerateShipmentsmanual"),
          Utility.getContext(this, vars, "#User_Org", "GenerateShipmentsmanual"), strC_BPartner_ID,
          strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"),
          Tree.getMembers(this, strTreeOrg, strAD_Org_ID));
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateShipmentsmanual", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.GenerateShipmentsmanual");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "GenerateShipmentsmanual.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GenerateShipmentsmanual.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GenerateShipmentsmanual");
      vars.removeMessage("GenerateShipmentsmanual");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramBPartnerId", strC_BPartner_ID);
    xmlDocument.setParameter("paramAdOrgId", strAD_Org_ID);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerDescription",
        GenerateInvoicesmanualData.bPartnerDescription(this, strC_BPartner_ID));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "AD_Org Security validation", Utility.getContext(this, vars, "#User_Org",
              "GenerateShipmentsmanual"), Utility.getContext(this, vars, "#User_Client",
              "GenerateShipmentsmanual"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateShipmentsmanual",
          strAD_Org_ID);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "GenerateShipmentsmanual Servlet. This Servlet was made by Wad constructor";
  } // end of getServletInfo() method
}
