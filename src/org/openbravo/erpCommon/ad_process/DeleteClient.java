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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.service.system.SystemService;
import org.openbravo.xmlEngine.XmlDocument;

public class DeleteClient extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strClient = vars.getRequestGlobalVariable("inpClientId", "DeleteClient|inpClientId");
      // String strClient = vars.getStringParameter("inpClientId", "");
      printPage(response, vars, strClient);
    } else if (vars.commandIn("SAVE")) {
      String strClient = vars.getRequestGlobalVariable("inpClientId", "DeleteClient|inpClientId");
      processButton(vars, strClient);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else
      pageErrorPopUp(response);
  }

  private void processButton(VariablesSecureApp vars, String strClient) throws ServletException {
    String pinstance = SequenceIdData.getUUID();
    OBError myMessage = null;
    try {
      if (strClient.equals("")) {
        myMessage = new OBError();
        myMessage.setType("Error");
        myMessage.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(),
            "@DeleteClient_NoClientSelected@"));
        myMessage.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
            "@DeleteClient_SelectClient@"));
      } else {
        Client client = OBDal.getInstance().get(Client.class, strClient);
        SystemService.getInstance().deleteClient(client);
      }
    } catch (Exception e) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      e.printStackTrace();
      log4j.warn("Error");
    }
    if (myMessage == null) {
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    }

    vars.setMessage("DeleteClient", myMessage);

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strClient)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Delete Client");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "", strProcessId = "800147";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);

    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/DeleteClient", discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "DeleteClient", false, "", "", "",
        false, "ad_process", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument
        .setParameter("alertMsg",
            "ALERT_MSG=\"" + Utility.messageBD(this, "GoingToDeleteClient", vars.getLanguage())
                + "\";");
    xmlDocument.setParameter("question",
        Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("Client", strClient);
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_process.DeleteClient");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "DeleteClient.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "DeleteClient.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("DeleteClient");
      vars.removeMessage("DeleteClient");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    xmlDocument.setData("reportClientId", "liststructure",
        ClientComboData.selectAllClientsNoSystem1(this));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}
