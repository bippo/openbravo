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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class InsertAcces extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      final String strProcessId = vars.getStringParameter("inpProcessId");
      final String strWindow = vars.getStringParameter("inpwindowId");
      final String strTab = vars.getStringParameter("inpTabId");
      final String strKey = vars.getGlobalVariable("inpadRoleId", strWindow + "|AD_Role_ID");
      final String strMessage = "";
      printPage(response, vars, strKey, strWindow, strProcessId, strMessage, strTab);
    } else if (vars.commandIn("GENERATE")) {
      final String strKey = vars.getStringParameter("inpadRoleId");
      final String strWindow = vars.getStringParameter("inpwindowId");
      vars.setSessionValue(strWindow + "|AD_Role_ID", strKey);
      final String strTab = vars.getStringParameter("inpTabId");
      final String strModule = vars.getStringParameter("inpModules");
      final String strType = vars.getStringParameter("inpType");

      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      final OBError myMessage = getPrintPage(response, vars, strKey, strModule, strType);
      vars.setMessage(strTab, myMessage);
      // vars.setSessionValue(strWindow + "|" + strTabName + ".message",
      // messageResult);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String strProcessId, String strMessage, String strTab) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button insert acces");
    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    final String lang = vars.getLanguage();
    if (lang.equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    final String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/InsertAcces", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    if (lang.equals("en_US"))
      xmlDocument.setData("reportModules_S", "liststructure", ModuleComboData.select(this));
    else
      xmlDocument
          .setData("reportModules_S", "liststructure", ModuleComboData.selectTrl(this, lang));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    // xmlDocument.setParameter("message",strMessage.equals("")?"":"alert('"
    // + strMessage + "');");

    {
      final OBError myMessage = vars.getMessage("InsertAcces");
      vars.removeMessage("InsertAcces");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private OBError getPrintPage(HttpServletResponse response, VariablesSecureApp vars,
      String strKey, String strModule, String strType) throws IOException, ServletException {
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    try {
      final InsertAccesData[] accesData = InsertAccesData.select(this);
      generateAcces(vars, accesData, strKey, strModule, strType);
      myMessage.setType("Success");
      myMessage.setMessage(Utility.messageBD(this, "ProcessOK", vars.getLanguage()));
      return myMessage;
      // return Utility.messageBD(this, "ProcessOK", vars.getLanguage());
    } catch (final Exception e) {
      log4j.warn(e);
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myMessage;
      // return Utility.messageBD(this, "ProcessRunError",
      // vars.getLanguage());
    }
  }

  private void generateAcces(VariablesSecureApp vars, InsertAccesData[] accesData, String roleid,
      String indice, String strType) throws ServletException {
    log4j.error("longitud accesdata: " + accesData.length + " indice: " + indice + " roleid: "
        + roleid);
    for (int i = 0; i < accesData.length; i++) {
      if (accesData[i].parentId.equals(indice)) {
        if (accesData[i].issummary.equals("Y"))
          generateAcces(vars, accesData, roleid, accesData[i].nodeId, strType);
        else {
          if (accesData[i].action.equals("W")
              && (InsertAccesData.selectWindow(this, roleid, accesData[i].adwindowid) == null || InsertAccesData
                  .selectWindow(this, roleid, accesData[i].adwindowid).equals(""))
              && (strType.equals("W") || strType.equals(""))) {
            log4j.error("Action: " + accesData[i].action + " window: " + accesData[i].adwindowid);
            InsertAccesData.insertWindow(this, accesData[i].adwindowid, roleid, vars.getClient(),
                "0", vars.getUser());
            if (!accesData[i].printreport.equals("")
                && (InsertAccesData.selectProcess(this, roleid, accesData[i].printreport) == null || InsertAccesData
                    .selectProcess(this, roleid, accesData[i].printreport).equals(""))) {
              log4j.error("Action window print report: " + accesData[i].printreport);
              InsertAccesData.insertProcess(this, accesData[i].printreport, roleid,
                  vars.getClient(), "0", vars.getUser());
            }
            /*
             * if (!accesData[i].editreference.equals("") && (InsertAccesData.selectForm(this,
             * roleid, accesData[i].editreference) == null || InsertAccesData .selectForm(this,
             * roleid, accesData[i].editreference).equals(""))) {
             * log4j.error("Action window fomr tab: " + accesData[i].editreference);
             * InsertAccesData.insertForm(this, accesData[i].editreference, roleid, vars
             * .getClient(), "0", vars.getUser()); }
             */
            InsertAccesData[] buttons = InsertAccesData.selectWindowButtons(this,
                accesData[i].adwindowid, roleid);
            if (buttons != null && buttons.length > 0) {
              for (int j = 0; j < buttons.length; j++) {
                if (InsertAccesData.selectProcess(this, roleid, buttons[j].adprocessid) == null
                    || InsertAccesData.selectProcess(this, roleid, buttons[j].adprocessid).equals(
                        "")) {
                  log4j.error("Action window button: " + buttons[j].adprocessid);
                  InsertAccesData.insertProcess(this, buttons[j].adprocessid, roleid,
                      vars.getClient(), "0", vars.getUser());
                }
              }
            }
          } else if (accesData[i].action.equals("P")
              && (InsertAccesData.selectProcess(this, roleid, accesData[i].adprocessid) == null || InsertAccesData
                  .selectProcess(this, roleid, accesData[i].adprocessid).equals(""))
              && (strType.equals("P") || strType.equals(""))) {
            log4j.error("Action: " + accesData[i].action + " process: " + accesData[i].adprocessid);
            InsertAccesData.insertProcess(this, accesData[i].adprocessid, roleid, vars.getClient(),
                "0", vars.getUser());
          } else if (accesData[i].action.equals("R")
              && (InsertAccesData.selectProcess(this, roleid, accesData[i].adprocessid) == null || InsertAccesData
                  .selectProcess(this, roleid, accesData[i].adprocessid).equals(""))
              && (strType.equals("R") || strType.equals(""))) {
            log4j.error("Action: " + accesData[i].action + " report: " + accesData[i].adprocessid);
            InsertAccesData.insertProcess(this, accesData[i].adprocessid, roleid, vars.getClient(),
                "0", vars.getUser());
          } else if (accesData[i].action.equals("X")
              && (InsertAccesData.selectForm(this, roleid, accesData[i].adformid) == null || InsertAccesData
                  .selectForm(this, roleid, accesData[i].adformid).equals(""))
              && (strType.equals("X") || strType.equals(""))) {
            log4j.error("Action: " + accesData[i].action + " form: " + accesData[i].adformid);
            InsertAccesData.insertForm(this, accesData[i].adformid, roleid, vars.getClient(), "0",
                vars.getUser());
          }
        }
      }
    }
  }

  @Override
  public String getServletInfo() {
    return "Servlet for the application's roles and permissions generation.";
    // Servlet created by Galder
  } // end of getServletInfo() method
}
