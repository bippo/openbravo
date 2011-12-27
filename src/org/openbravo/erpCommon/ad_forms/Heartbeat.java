/*
 * 
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2011 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessRunner;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class Heartbeat extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    updateServletContainer(request.getSession().getServletContext().getServerInfo());

    if (vars.commandIn("DEFAULT", "DEFAULT_MODULE", "UPDATE_MODULE", "UPGRADE_MODULE")) {
      printPageDataSheet(response, vars);
    } else if (vars.commandIn("CONFIGURE", "CONFIGURE_MODULE_UPDATE", "CONFIGURE_MODULE_INSTALL",
        "CONFIGURE_MODULE_UPGRADE")) {
      response.sendRedirect(strDireccion + "/ad_process/TestHeartbeat.html?Command="
          + vars.getCommand() + "&inpcRecordId="
          + vars.getStringParameter("inpcRecordId", IsIDFilter.instance) + "&version="
          + vars.getStringParameter("version"));
    } else if (vars.commandIn("DEFER")) {
      setPostponeDate();
      sendBeat(vars, "DEFER");
    } else if (vars.commandIn("DECLINE")) {
      sendBeat(vars, "DECLINE");
    } else {
      pageError(response);
    }
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;
    String[] discard = { "" };
    if (!vars.commandIn("DEFAULT")) {
      discard[0] = "deferButton";
    }
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/Heartbeat", discard)
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    String msgCode = vars.getCommand().equals("DEFAULT_MODULE") ? "HB_WELCOME_MODULE"
        : "HB_WELCOME";
    xmlDocument.setParameter("welcome",
        Replace.replace(Utility.messageBD(this, msgCode, vars.getLanguage()), "\\n", "<br/>"));

    xmlDocument.setParameter("recordId",
        vars.getStringParameter("inpcRecordId", IsIDFilter.instance));
    xmlDocument.setParameter("version", vars.getStringParameter("version"));

    String jsCommand = "var cmd='";
    if (vars.commandIn("DEFAULT")) {
      jsCommand += "CONFIGURE";
    } else {
      String moduleAction = vars.getCommand().equals("UPDATE_MODULE") ? "UPDATE" : vars
          .getCommand().equals("UPGRADE_MODULE") ? "UPGRADE" : "INSTALL";
      jsCommand += "CONFIGURE_MODULE_" + moduleAction;
    }
    jsCommand += "';";
    xmlDocument.setParameter("cmd", jsCommand);

    if (HeartbeatProcess.isShowRegistrationRequired(vars, myPool)
        && vars.getCommand().equals("DEFAULT")) {
      xmlDocument.setParameter("registration", "Y");
    }

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Updates the servlet container information on the System Information table.
   * 
   * @param serverInfo
   * @throws ServletException
   */
  private void updateServletContainer(String serverInfo) throws ServletException {
    final org.openbravo.erpCommon.businessUtility.HeartbeatData[] data = org.openbravo.erpCommon.businessUtility.HeartbeatData
        .selectSystemProperties(this);
    if (data.length > 0) {
      String servletContainer = data[0].servletContainer;
      String servletContainerVersion = data[0].servletContainerVersion;
      if ((servletContainer == null || servletContainer.equals(""))
          && (servletContainerVersion == null || servletContainerVersion.equals(""))) {
        if (serverInfo != null && serverInfo.contains("/")) {
          servletContainer = serverInfo.split("/")[0];
          servletContainerVersion = serverInfo.split("/")[1];

          HeartbeatData.updateServletContainer(this, servletContainer, servletContainerVersion);
        }
      }
    }
  }

  /**
   * Executes the HeartbeatProcess to send a beat with the specified action.
   * 
   * @param vars
   * @param strAction
   * @throws ServletException
   */
  private void sendBeat(VariablesSecureApp vars, String strAction) throws ServletException {
    ProcessBundle bundle = new ProcessBundle(HeartbeatProcess.HB_PROCESS_ID, vars).init(this);
    Map<String, Object> params = bundle.getParams();
    params.put("action", strAction);
    new ProcessRunner(bundle).execute(this);
  }

  /**
   * Sets the PostponeDate of the System Information table on 3 days from today.
   */
  private void setPostponeDate() {
    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 3);
    org.openbravo.model.ad.system.SystemInformation sysInfo = OBDal.getInstance().get(
        org.openbravo.model.ad.system.SystemInformation.class, "0");
    sysInfo.setPostponeDate(cal.getTime());
    OBDal.getInstance().save(sysInfo);
  }

  @Override
  public String getServletInfo() {
    return "Heartbeat pop-up form servlet.";
  } // end of getServletInfo() method
}
