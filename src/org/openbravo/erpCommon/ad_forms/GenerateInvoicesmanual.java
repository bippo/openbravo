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

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

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

public class GenerateInvoicesmanual extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "GenerateInvoicesmanual|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "GenerateInvoicesmanual|DateTo", "");
      String strInvDate = vars
          .getGlobalVariable("inpInvDate", "GenerateInvoicesmanual|invDate", "");
      String strC_BPartner_ID = vars.getGlobalVariable("inpcBpartnerId",
          "GenerateInvoicesmanual|C_BPartner_ID", "");
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "GenerateInvoicesmanual|Ad_Org_ID", vars.getOrg());
      String strIncludeTaxes = vars.getGlobalVariable("inpinctaxes",
          "GenerateInvoicesmanual|withtax", "");
      printPageDataSheet(response, vars, strC_BPartner_ID, strAD_Org_ID, strDateFrom, strDateTo,
          strIncludeTaxes, strInvDate);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "GenerateInvoicesmanual|DateFrom");
      String strDateTo = vars
          .getRequestGlobalVariable("inpDateTo", "GenerateInvoicesmanual|DateTo");
      String strInvDate = vars.getRequestGlobalVariable("inpInvDate",
          "GenerateInvoicesmanual|invDate");
      String strC_BPartner_ID = vars.getRequestGlobalVariable("inpcBpartnerId",
          "GenerateInvoicesmanual|C_BPartner_ID");
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "GenerateInvoicesmanual|Ad_Org_ID");
      String strIncludeTaxes = vars.getRequestGlobalVariable("inpinctaxes",
          "GenerateInvoicesmanual|withtax");
      printPageDataSheet(response, vars, strC_BPartner_ID, strAD_Org_ID, strDateFrom, strDateTo,
          strIncludeTaxes, strInvDate);
    } else if (vars.commandIn("GENERATE")) {
      String strCOrderId = vars.getInStringParameter("inpOrder", IsIDFilter.instance);
      String strInvDate = vars.getRequestGlobalVariable("inpInvDate",
          "GenerateInvoicesmanual|invDate");
      if (strCOrderId.equals(""))
        strCOrderId = "('0')";
      GenerateInvoicesmanualData.initUpdate(this);
      GenerateInvoicesmanualData.updateSelection(this, strCOrderId);
      String pinstance = SequenceIdData.getUUID();
      PInstanceProcessData.insertPInstance(this, pinstance, "134", "0", "N", vars.getUser(),
          vars.getClient(), vars.getOrg());
      PInstanceProcessData.insertPInstanceParam(this, pinstance, "1", "Selection", "Y",
          vars.getClient(), vars.getOrg(), vars.getUser());
      if (strInvDate != null && !"".equals(strInvDate)) {
        PInstanceProcessData.insertPInstanceParamDate(this, pinstance, "2", "DateInvoiced",
            strInvDate, vars.getClient(), vars.getOrg(), vars.getUser());
      }
      ActionButtonData.process134(this, pinstance);
      PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
      OBError myMessage = new OBError();
      String message = "";
      myMessage.setTitle("");
      myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
      vars.setMessage("GenerateInvoicesmanual", myMessage);
      GenerateInvoicesmanualData.resetSelection(this, strCOrderId);
      if (log4j.isDebugEnabled())
        log4j.debug(message);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strC_BPartner_ID, String strAD_Org_ID, String strDateFrom, String strDateTo,
      String strIncludeTaxes, String strInvDate) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "sectionDetail" };
    XmlDocument xmlDocument = null;
    GenerateInvoicesmanualData[] data = null;
    String strTreeOrg = GenerateShipmentsmanualData.treeOrg(this, vars.getClient());
    if (strC_BPartner_ID.equals("") && strAD_Org_ID.equals("")) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/GenerateInvoicesmanual", discard).createXmlDocument();
      data = GenerateInvoicesmanualData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/GenerateInvoicesmanual").createXmlDocument();
      // data = GenerateInvoicesmanualData.select(this,
      // Utility.getContext(this, vars, "#User_Client",
      // "GenerateInvoicesmanual"), Utility.getContext(this, vars,
      // "#User_Org", "GenerateInvoicesmanual"), strC_BPartner_ID,
      // strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"),
      // strTreeOrg, strAD_Org_ID);
      if (strIncludeTaxes.equals("Y")) {
        data = GenerateInvoicesmanualData.selectGross(this, vars.getLanguage(),
            Utility.getContext(this, vars, "#User_Client", "GenerateInvoicesmanual"),
            Utility.getContext(this, vars, "#User_Org", "GenerateInvoicesmanual"),
            strC_BPartner_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"),
            Tree.getMembers(this, strTreeOrg, strAD_Org_ID));
      } else
        data = GenerateInvoicesmanualData.select(this, vars.getLanguage(),
            Utility.getContext(this, vars, "#User_Client", "GenerateInvoicesmanual"),
            Utility.getContext(this, vars, "#User_Org", "GenerateInvoicesmanual"),
            strC_BPartner_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"),
            Tree.getMembers(this, strTreeOrg, strAD_Org_ID));
      data = calcAmountsWithInvoiceTerm(data);
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateInvoicesmanual", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.GenerateInvoicesmanual");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "GenerateInvoicesmanual.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GenerateInvoicesmanual.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GenerateInvoicesmanual");
      vars.removeMessage("GenerateInvoicesmanual");
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
    xmlDocument.setParameter("paramADOrgID", strAD_Org_ID);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("invDate", strInvDate);
    xmlDocument.setParameter("invDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("invDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerDescription",
        GenerateInvoicesmanualData.bPartnerDescription(this, strC_BPartner_ID));
    xmlDocument.setParameter("incTaxes", strIncludeTaxes);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "AD_Org Security validation", Utility.getContext(this, vars, "#User_Org",
              "GenerateInvoicesmanual"), Utility.getContext(this, vars, "#User_Client",
              "GenerateInvoicesmanual"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateInvoicesmanual",
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

  private GenerateInvoicesmanualData[] calcAmountsWithInvoiceTerm(GenerateInvoicesmanualData[] gData) {
    for (GenerateInvoicesmanualData gIData : gData) {
      String strTermValue = gIData.getField("termvalue");
      if (strTermValue.equals("N")) {
        gIData.notinvoicedlines = "0";
        gIData.pendinglines = "0";
      } else if (strTermValue.equals("I")) {
        gIData.pendinglines = gIData.notinvoicedlines;
      } else if (strTermValue.equals("O")) {
        if ((new BigDecimal(gIData.qtydelivered).compareTo(new BigDecimal(gIData.qtyordered)) < 0)) {
          gIData.notinvoicedlines = gIData.amountlines;
          gIData.pendinglines = "0";
        }
      }
    }
    return gData;
  }

  public String getServletInfo() {
    return "GenerateInvoicesmanual Servlet. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
