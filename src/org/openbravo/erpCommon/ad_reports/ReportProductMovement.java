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
package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportProductMovement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportProductMovement|dateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportProductMovement|dateTo", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportProductMovement|cBpartnerId", "");
      String strmProductId = vars.getGlobalVariable("inpmProductId",
          "ReportProductMovement|mProductId", "");
      String strmAttributesetinstanceId = vars.getGlobalVariable("inpmAttributeSetInstanceId",
          "ReportProductMovement|M_AttributeSetInstance_Id", "");
      String strInout = vars.getGlobalVariable("inpInout", "ReportProductMovement|inout", "-1");
      String strInventory = vars.getGlobalVariable("inpInventory",
          "ReportProductMovement|inventory", "-1");
      String strMovement = vars.getGlobalVariable("inpMovement", "ReportProductMovement|movement",
          "-1");
      String strProduction = vars.getGlobalVariable("inpProduction",
          "ReportProductMovement|production", "-1");
      String strInternalConsumption = vars.getGlobalVariable("inpInternalConsumption",
          "ReportProductMovement|internalConsumption", "-1");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strmProductId,
          strInout, strInventory, strMovement, strProduction, strmAttributesetinstanceId,
          strInternalConsumption);
    } else if (vars.commandIn("DIRECT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportProductMovement|dateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportProductMovement|dateTo", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportProductMovement|cBpartnerId", "");
      String strmProductId = vars.getGlobalVariable("inpmProductId",
          "ReportProductMovement|mProductId", "");
      String strmAttributesetinstanceId = vars.getGlobalVariable("inpmAttributeSetInstanceId",
          "ReportProductMovement|M_AttributeSetInstance_Id", "");
      String strInout = vars.getGlobalVariable("inpInout", "ReportProductMovement|inout", "");
      String strInventory = vars.getGlobalVariable("inpInventory",
          "ReportProductMovement|inventory", "");
      String strMovement = vars.getGlobalVariable("inpMovement", "ReportProductMovement|movement",
          "");
      String strProduction = vars.getGlobalVariable("inpProduction",
          "ReportProductMovement|production", "");
      String strInternalConsumption = vars.getGlobalVariable("inpInternalConsumption",
          "ReportProductMovement|internalConsumption", "");
      setHistoryCommand(request, "DIRECT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strmProductId,
          strInout, strInventory, strMovement, strProduction, strmAttributesetinstanceId,
          strInternalConsumption);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportProductMovement|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportProductMovement|dateTo");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpcBPartnerId",
          "ReportProductMovement|cBpartnerId");
      String strmProductId = vars.getRequestGlobalVariable("inpmProductId",
          "ReportProductMovement|mProductId");
      String strmAttributesetinstanceId = vars.getRequestGlobalVariable(
          "inpmAttributeSetInstanceId", "ReportProductMovement|M_AttributeSetInstance_Id");
      String strInout = vars.getRequestGlobalVariable("inpInout", "ReportProductMovement|inout");
      String strInventory = vars.getRequestGlobalVariable("inpInventory",
          "ReportProductMovement|inventory");
      String strMovement = vars.getRequestGlobalVariable("inpMovement",
          "ReportProductMovement|movement");
      String strProduction = vars.getRequestGlobalVariable("inpProduction",
          "ReportProductMovement|production");
      String strInternalConsumption = vars.getRequestGlobalVariable("inpInternalConsumption",
          "ReportProductMovement|internalConsumption");
      setHistoryCommand(request, "DIRECT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strmProductId,
          strInout, strInventory, strMovement, strProduction, strmAttributesetinstanceId,
          strInternalConsumption);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strcBpartnerId, String strmProductId,
      String strInout, String strInventory, String strMovement, String strProduction,
      String strmAttributesetinstanceId, String strInternalConsumption) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportProductMovementData[] data = null;
    ReportProductMovementData[] data1 = null;
    ReportProductMovementData[] data2 = null;
    ReportProductMovementData[] data3 = null;
    ReportProductMovementData[] data4 = null;
    String discard[] = { "discard", "discard", "discard", "discard", "discard" };
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      strDateTo = DateTimeData.today(this);
      strDateFrom = DateTimeData.weekBefore(this);
    }
    if (vars.commandIn("FIND", "DIRECT")) {
      if (strInout.equals("-1")) {
        data = ReportProductMovementData.select(this, vars.getLanguage(),
            Utility.getContext(this, vars, "#User_Client", "ReportProductMovement"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductMovement"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId,
            strmProductId, strmAttributesetinstanceId);
        if (data == null || data.length == 0) {
          discard[0] = "selEliminar1";
          data = ReportProductMovementData.set();
        }
      } else {
        discard[0] = "selEliminar1";
        data = ReportProductMovementData.set();
      }
      if (strInventory.equals("-1")) {
        data1 = ReportProductMovementData.selectInventory(this,
            Utility.getContext(this, vars, "#User_Client", "ReportProductMovement"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductMovement"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId,
            strmProductId);
        if (data1 == null || data1.length == 0) {
          discard[1] = "selEliminar2";
          data1 = ReportProductMovementData.set();
        }
      } else {
        discard[1] = "selEliminar2";
        data1 = ReportProductMovementData.set();
      }
      if (strMovement.equals("-1")) {
        data2 = ReportProductMovementData.selectMovement(this,
            Utility.getContext(this, vars, "#User_Client", "ReportProductMovement"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductMovement"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId,
            strmProductId);
        if (data2 == null || data2.length == 0) {
          discard[2] = "selEliminar3";
          data2 = ReportProductMovementData.set();
        }
      } else {
        discard[2] = "selEliminar3";
        data2 = ReportProductMovementData.set();
      }
      if (strProduction.equals("-1")) {
        data3 = ReportProductMovementData.selectProduction(this,
            Utility.getContext(this, vars, "#User_Client", "ReportProductMovement"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductMovement"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId,
            strmProductId);
        if (data3 == null || data3.length == 0) {
          discard[3] = "selEliminar4";
          data3 = ReportProductMovementData.set();
        }
      } else {
        discard[3] = "selEliminar4";
        data3 = ReportProductMovementData.set();
      }
      if (strInternalConsumption.equals("-1")) {
        data4 = ReportProductMovementData.selectInternalConsumption(this,
            Utility.getContext(this, vars, "#User_Client", "ReportProductMovement"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductMovement"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strcBpartnerId,
            strmProductId);
        if (data4 == null || data4.length == 0) {
          discard[4] = "selEliminar5";
          data4 = ReportProductMovementData.set();
        }
      } else {
        discard[4] = "selEliminar5";
        data4 = ReportProductMovementData.set();
      }
    } else {
      discard[0] = "selEliminar1";
      discard[1] = "selEliminar2";
      discard[2] = "selEliminar3";
      discard[3] = "selEliminar4";
      discard[4] = "selEliminar5";
      data = ReportProductMovementData.set();
      data1 = ReportProductMovementData.set();
      data2 = ReportProductMovementData.set();
      data3 = ReportProductMovementData.set();
      data4 = ReportProductMovementData.set();
    }
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportProductMovement", discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProductMovement", false, "", "",
        "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportProductMovement");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportProductMovement.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportProductMovement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportProductMovement");
      vars.removeMessage("ReportProductMovement");
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
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("mProduct", strmProductId);

    xmlDocument.setData(
        "reportM_ATTRIBUTESETINSTANCE_ID",
        "liststructure",
        AttributeSetInstanceComboData.select(this, vars.getLanguage(), strmProductId,
            Utility.getContext(this, vars, "#User_Client", "ReportProductMovement"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProductMovement")));
    xmlDocument.setParameter("parameterM_ATTRIBUTESETINSTANCE_ID", strmAttributesetinstanceId);

    xmlDocument.setParameter("bPartnerDescription",
        ReportProductMovementData.selectBpartner(this, strcBpartnerId));
    xmlDocument.setParameter("productDescription",
        ReportProductMovementData.selectMproduct(this, strmProductId));
    xmlDocument.setParameter("inout", strInout);
    xmlDocument.setParameter("inventory", strInventory);
    xmlDocument.setParameter("movement", strMovement);
    xmlDocument.setParameter("production", strProduction);
    xmlDocument.setParameter("internalConsumption", strInternalConsumption);
    xmlDocument.setData("structure1", data);
    xmlDocument.setData("structure2", data1);
    xmlDocument.setData("structure3", data2);
    xmlDocument.setData("structure4", data3);
    xmlDocument.setData("structure5", data4);

    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportProductMovement. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
