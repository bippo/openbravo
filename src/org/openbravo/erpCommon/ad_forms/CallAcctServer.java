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
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.scheduling.ProcessRunner;
import org.openbravo.xmlEngine.XmlDocument;
import org.quartz.SchedulerException;

public class CallAcctServer extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String adProcessId = CallAcctServerData.processID(this);
    CallAcctServerData[] data = CallAcctServerData.selectByProcessId(this, adProcessId,
        vars.getClient());

    // PeriodicBackground acctServer = getBackgroundProcess(adProcessId);
    if (vars.commandIn("DEFAULT")) {
      String strTableId = vars.getStringParameter("inpadTableId", "");
      String strAdOrgId = vars.getStringParameter("inpadOrgId");
      printPage(response, vars, strTableId, strAdOrgId, "");
    } else if (vars.commandIn("CANCELAR")) {
      String strTableId = vars.getStringParameter("inpadTableId", "");
      String strAdOrgId = vars.getStringParameter("inpadOrgId");
      if (data.length > 0 && data[0].status.equals(org.openbravo.scheduling.Process.SCHEDULED)) {
        try {
          OBScheduler.getInstance().unschedule(data[0].id, new ProcessContext(vars));
        } catch (SchedulerException e) {
          throw new ServletException(e.getMessage(), e);
        }
      }
      // acctServer.cancelDirectProcess();
      printPage(response, vars, strTableId, strAdOrgId, "");
    } else if (vars.commandIn("REFRESH_INFO")) {
    } else if (vars.commandIn("RUN")) {
      String strTableId = vars.getStringParameter("inpadTableId");
      String strAdOrgId = vars.getStringParameter("inpadOrgId");
      if (strAdOrgId == null || strAdOrgId.equals(""))
        strAdOrgId = "0";
      if (log4j.isDebugEnabled())
        log4j.debug(strTableId);
      runProcess(response, vars, strTableId, strAdOrgId, adProcessId);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTableId,
      String strOrgId, String strMessage) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/CallAcctServer").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CallAcctServer", false, "", "", "",
        false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.CallAcctServer");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CallAcctServer.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CallAcctServer.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CallAcctServer");
      vars.removeMessage("CallAcctServer");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("body",
          (strMessage.equals("") ? "" : "alert('" + strMessage + "');"));

      AcctServerData[] data = AcctServerData.selectTables(this, vars.getLanguage(),
          Utility.getContext(this, vars, "#User_Client", ""));
      if (log4j.isDebugEnabled())
        log4j.debug("select tables org:" + Utility.getContext(this, vars, "#User_Org", "")
            + ", Client:" + Utility.getContext(this, vars, "#User_Client", "") + ",lang:"
            + vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("lenght:" + data.length);
      xmlDocument.setData("reportadTableId", "liststructure", data);

      xmlDocument.setParameter("adTableId", strTableId);

      data = AcctServerData.selectOrganizations(this,
          Utility.getContext(this, vars, "#User_Client", ""));
      if (log4j.isDebugEnabled())
        log4j.debug("select organizations:" + Utility.getContext(this, vars, "#User_Org", "")
            + ", Client:" + Utility.getContext(this, vars, "#User_Client", ""));
      if (log4j.isDebugEnabled())
        log4j.debug("lenght:" + data.length);
      xmlDocument.setData("reportadOrgId", "liststructure", data);

      xmlDocument.setParameter("adOrgId", strOrgId);

      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private void runProcess(HttpServletResponse response, VariablesSecureApp vars, String strTableId,
      String strOrgId, String adProcessId) throws IOException, ServletException {
    OBError myMessage = new OBError();
    myMessage.setTitle("");
    boolean scheduled = false;
    try {
      CallAcctServerData[] data = CallAcctServerData.selectByProcessId(this, adProcessId,
          vars.getClient());
      if (data.length > 0) {
        for (int i = 0; i < data.length; i++) {
          if (data[i].status.equals(Process.SCHEDULED)) {
            myMessage.setMessage(Utility.messageBD(this, "BP_RUNNING", vars.getLanguage()));
            myMessage.setType("Error");
            scheduled = true;
            break;
          }
        }
      }
      if (!scheduled) {
        String adPinstanceId = SequenceIdData.getUUID();
        PInstanceProcessData.insertPInstance(this, adPinstanceId, adProcessId, strOrgId, "N",
            vars.getUser(), vars.getClient(), vars.getOrg());
        PInstanceProcessData.insertPInstanceParam(this, adPinstanceId, "10", "AD_Table_ID",
            strTableId, vars.getClient(), vars.getOrg(), vars.getUser());
        PInstanceProcessData.insertPInstanceParam(this, adPinstanceId, "20", "AD_Org_ID", strOrgId,
            vars.getClient(), vars.getOrg(), vars.getUser());

        ProcessBundle bundle = new ProcessBundle(adProcessId, vars).init(this);
        bundle.getParams().put(ProcessBundle.PINSTANCE, adPinstanceId);
        String executionId = new ProcessRunner(bundle).execute(this);

        String log = CallAcctServerData.selectLog(this, executionId);
        myMessage.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), log));
        myMessage.setType("Info");
      } else {
        myMessage.setMessage(Utility.messageBD(this, "BP_RUNNING", vars.getLanguage()));
        myMessage.setType("Error");
      }
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      vars.setMessage("CallAcctServer", myMessage);
      printPage(response, vars, strTableId, strOrgId, "");
    }
  }

  public String getServletInfo() {
    return "Servlet that calls the contabilization process";
  } // end of getServletInfo() method
}
