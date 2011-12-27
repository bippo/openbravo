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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.services.webservice.Module;
import org.openbravo.services.webservice.WebService3Impl;
import org.openbravo.services.webservice.WebService3ImplServiceLocator;
import org.openbravo.xmlEngine.XmlDocument;

public class RegisterModule extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars, false);
    }
    if (vars.commandIn("REGISTER")) {
      printPage(response, vars, true);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, boolean process)
      throws IOException, ServletException {

    String discard[] = { "", "" };
    String moduleId = vars.getStringParameter("inpadModuleId");
    // execute registration process
    if (process) {
      discard[0] = "discardOk";
      discard[1] = "discardParams";

      // set the module
      log4j.info("Registering module " + moduleId);
      RegisterModuleData data = RegisterModuleData.selectModule(this, moduleId);
      Module module = new Module();
      module.setModuleID(moduleId);
      module.setName(data.name);
      module.setPackageName(data.javapackage);
      module.setAuthor(data.author);
      module.setType(data.type);
      module.setHelp(data.help);
      module.setDbPrefix(data.dbPrefix);
      module.setDescription(data.description);

      WebService3Impl ws = null;
      boolean error = !HttpsUtils.isInternetAvailable();
      try {
        if (!error) {
          // retrieve the module details from the webservice
          WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
          ws = (WebService3Impl) loc.getWebService3();
        }
      } catch (Exception e) {
        error = true;
        log4j.error("Error obtaining ws to register module", e);
      }

      if (error) {
        OBError message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        message.setMessage(Utility.messageBD(this, "WSError", vars.getLanguage()));
        vars.setMessage("RegisterModule", message);
      } else {
        try {
          module = ws.moduleRegister(module, vars.getStringParameter("inpUser"),
              vars.getStringParameter("inpPassword"));
          RegisterModuleData.setRegistered(this, moduleId);
        } catch (Exception e) {
          OBError message = new OBError();
          message.setType("Error");
          message.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          message.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
              e.getMessage()));
          vars.setMessage("RegisterModule", message);
          error = true;
          e.printStackTrace();
        }
      }
      if (!error) {
        OBError message = new OBError();
        message.setType("Success");
        message.setTitle(Utility.messageBD(this, "ProcessOK", vars.getLanguage()));
        vars.setMessage("RegisterModule", message);
        error = true;
      }
    } else {
      discard[0] = "discardDefault";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RegisterModule", discard).createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("help", RegisterModuleData.getHelp(this, vars.getLanguage()));
    xmlDocument.setParameter("inpadModuleId", moduleId);

    {
      OBError myMessage = vars.getMessage("RegisterModule");
      vars.removeMessage("RegisterModule");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    PrintWriter out = response.getWriter();
    response.setContentType("text/html; charset=UTF-8");
    out.println(xmlDocument.print());
    out.close();
  }
}
