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
package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
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

public class PriceListCreateAll extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("SAVE")) {
      String strWindow = "PriceListCreateAll";
      String strKey = vars.getRequiredStringParameter("inpmPricelistVersionId");
      OBError myMessage = processButton(vars, strKey, strWindow);
      // vars.setSessionValue("PriceListCreateAll.message",
      // messageResult);
      vars.setMessage("PriceListCreateAll", myMessage);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strPriceListVersion, String windowId)
      throws ServletException {
    Connection conn = null;
    String strMessage = "";
    OBError myMessage = null;

    myMessage = new OBError();
    myMessage.setTitle("");
    try {
      conn = getTransactionConnection();
      PriceListCreateAllData[] data = PriceListCreateAllData.selectParent(conn, this,
          strPriceListVersion, Utility.getContext(this, vars, "#AccessibleOrgTree", windowId),
          Utility.getContext(this, vars, "#User_Client", windowId));
      if (data == null || data.length == 0) {
        releaseRollbackConnection(conn);

        myMessage.setType("Error");
        myMessage.setMessage(Utility.messageBD(this, "SearchNothing", vars.getLanguage()));
        return myMessage;
        // return Utility.messageBD(this, "SearchNothing",
        // vars.getLanguage());
      }
      strMessage = processPL(conn, vars, data[0]);
      if (!strMessage.equals("")) {
        releaseRollbackConnection(conn);
        myMessage.setType("Error");
        myMessage.setMessage(strMessage);
        return myMessage;
        // return strMessage;
      }
      strMessage = processHijos(conn, vars, windowId, data[0].mPricelistVersionId);
      if (!strMessage.equals("")) {
        releaseRollbackConnection(conn);
        myMessage.setType("Error");
        myMessage.setMessage(strMessage);
        return myMessage;
        // return strMessage;
      } else
        strMessage = Utility.messageBD(this, "ProcessOK", vars.getLanguage());

      releaseCommitConnection(conn);
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      log4j.warn(e);
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myMessage;
      // return Utility.messageBD(this, "ProcessRunError",
      // vars.getLanguage());
    }
    myMessage.setType("Success");
    myMessage.setMessage(strMessage);
    return myMessage;
    // return strMessage;
  }

  private String processHijos(Connection conn, VariablesSecureApp vars, String windowId,
      String strPriceListVersion) throws ServletException {
    String strMessage = "";
    PriceListCreateAllData[] data = PriceListCreateAllData.select(conn, this, strPriceListVersion,
        Utility.getContext(this, vars, "#User_Org", windowId),
        Utility.getContext(this, vars, "#User_Client", windowId));
    if (data == null || data.length == 0)
      return "";
    for (int i = 0; i < data.length; i++) {
      strMessage = processPL(conn, vars, data[i]);
      if (!strMessage.equals(""))
        break;
      strMessage = processHijos(conn, vars, windowId, data[i].mPricelistVersionId);
      if (!strMessage.equals(""))
        break;
    }
    return strMessage;
  }

  private String processPL(Connection conn, VariablesSecureApp vars, PriceListCreateAllData data)
      throws ServletException {
    if (data.mPricelistVersionBaseId.equals(""))
      return "";
    String pinstance = SequenceIdData.getUUID();
    PInstanceProcessData.insertPInstance(this, pinstance, "800040", data.mPricelistVersionId, "N",
        vars.getUser(), vars.getClient(), vars.getOrg());

    PriceListCreateAllData.process(conn, this, pinstance);

    PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
    String messageResult = "";
    if (pinstanceData != null && pinstanceData.length > 0) {
      if (!pinstanceData[0].result.equals("1")) {
        if (!pinstanceData[0].errormsg.equals("")) {
          String message = pinstanceData[0].errormsg;
          if (message.startsWith("@") && message.endsWith("@")) {
            message = message.substring(1, message.length() - 1);
            if (message.indexOf("@") == -1)
              messageResult = Utility.messageBD(this, message, vars.getLanguage());
            else
              messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), "@"
                  + message + "@");
          } else {
            messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), message);
          }
        } else if (!pinstanceData[0].pMsg.equals("")) {
          String message = pinstanceData[0].pMsg;
          messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), message);
        } else {
          messageResult = Utility.messageBD(this, "Error", vars.getLanguage());
        }
      }
    }
    if (!messageResult.equals(""))
      if (log4j.isDebugEnabled())
        log4j.debug(messageResult);
    return messageResult;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Create Pricelist");

    ActionButtonDefaultData[] data = null;
    // String strMessage =
    // vars.getSessionValue("PriceListCreateAll.message");
    // vars.removeSessionValue("PriceListCreateAll.message");
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, classInfo.id);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), classInfo.id);

    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/PriceListCreateAll", discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "PriceListCreateAll", false, "", "",
        "", false, "ad_process", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("question",
        Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    // New interface paramenters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_process.PriceListCreateAll");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "PriceListCreateAll.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "PriceListCreateAll.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("PriceListCreateAll");
      vars.removeMessage("PriceListCreateAll");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    // //----

    // if (!strMessage.equals("")) strMessage = "alert('" + strMessage +
    // "');";
    // xmlDocument.setParameter("message", strMessage);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_PriceList_Version_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "PriceListCreateAll"), Utility.getContext(this, vars, "#User_Client",
              "PriceListCreateAll"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "PriceListCreateAll", "");
      xmlDocument.setData("reportM_PriceList_Version_ID", "liststructure",
          comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Drop reg fact acct";
  } // end of getServletInfo() method
}
