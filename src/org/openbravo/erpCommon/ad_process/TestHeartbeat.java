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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.ModuleManagement;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.ad.ui.ProcessRequest;
import org.openbravo.model.ad.ui.ProcessRun;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.scheduling.ProcessRunner;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * This process activates and deactivates Heartebeat.
 * 
 */
public class TestHeartbeat extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  private static final String HB_tabId = "1005400005";
  private static final String SystemInfomation_ID = "0";
  private static final String EVERY_N_DAYS = "N";
  private static final String SCHEDULE = "S";
  private static final ValueListFilter activeFilter = new ValueListFilter("", "Y", "N");

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    ConnectionProvider connectionProvider = new DalConnectionProvider();

    VariablesSecureApp vars = new VariablesSecureApp(request);

    final Process HBProcess = OBDal.getInstance()
        .get(Process.class, HeartbeatProcess.HB_PROCESS_ID);
    final SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class,
        SystemInfomation_ID);

    final String clickedButton = vars.getStringParameter("inpLastFieldChanged");
    final String isHBActive = vars.getStringParameter("inptestproxy", activeFilter);

    if (HeartbeatProcess.isHeartbeatEnabled()
        || clickedButton.equalsIgnoreCase("inpisheartbeatactive") || "Y".equals(isHBActive)) {
      // Disable Heartbeat
      try {

        if (sysInfo.isEnableHeartbeat() != null && sysInfo.isEnableHeartbeat()) {
          // Sending deactivation beat
          ProcessBundle beat = new ProcessBundle(HeartbeatProcess.HB_PROCESS_ID, vars)
              .init(connectionProvider);
          new ProcessRunner(beat).execute(connectionProvider);
        } else {
          // Sending Declining beat
          ProcessBundle bundle = new ProcessBundle(HeartbeatProcess.HB_PROCESS_ID, vars)
              .init(connectionProvider);
          Map<String, Object> params = bundle.getParams();
          params.put("action", "DECLINE");
          final String beatExecutionId = new ProcessRunner(bundle).execute(connectionProvider);
        }

        // Deactivating the process at SystemInfo
        sysInfo.setEnableHeartbeat(false);
        sysInfo.setTestHeartbeat("N");
        OBDal.getInstance().save(sysInfo);

        // Un-scheduling the process
        final OBCriteria<ProcessRequest> prCriteria = OBDal.getInstance().createCriteria(
            ProcessRequest.class);
        prCriteria.add(Restrictions.and(
            Restrictions.eq(ProcessRequest.PROPERTY_PROCESS, HBProcess), Restrictions.or(
                Restrictions.eq(ProcessRequest.PROPERTY_STATUS,
                    org.openbravo.scheduling.Process.SCHEDULED), Restrictions.eq(
                    ProcessRequest.PROPERTY_STATUS, org.openbravo.scheduling.Process.MISFIRED))));

        final List<ProcessRequest> requestList = prCriteria.list();

        if (requestList.size() != 0) {

          final ProcessRequest pr = requestList.get(0);

          OBDal.getInstance().save(pr);

          final ProcessBundle bundle = ProcessBundle.request(pr.getId(), vars, connectionProvider);

          OBScheduler.getInstance().unschedule(pr.getId(), bundle.getContext());
        }

        String msg = Utility.messageBD(connectionProvider, "HB_SUCCESS", vars.getLanguage());
        advisePopUpRefresh(request, response, "SUCCESS", "Heartbeat Configuration", msg);

      } catch (Exception e) {
        log4j.error(e.getMessage(), e);
        advisePopUpRefresh(request, response, "ERROR", "Heartbeat Configuration", e.getMessage());
      }

    } else { // Enable Heartbeat

      try {

        HBProcess.setActive(true);
        OBDal.getInstance().save(HBProcess);

        // Sending beat
        ProcessBundle bundle = new ProcessBundle(HeartbeatProcess.HB_PROCESS_ID, vars)
            .init(connectionProvider);
        final String beatExecutionId = new ProcessRunner(bundle).execute(connectionProvider);

        // Getting beat result
        final ProcessRun result = OBDal.getInstance().get(ProcessRun.class, beatExecutionId);

        if (result.getStatus().equals("ERR")) {
          // Restoring not active state
          sysInfo.setEnableHeartbeat(false);
          sysInfo.setTestHeartbeat("N");
          OBDal.getInstance().save(sysInfo);

          String msg = Utility.messageBD(connectionProvider, "HB_INTERNAL_ERROR",
              vars.getLanguage());
          // Extracting the last line from the log
          String log = result.getLog().substring(0, result.getLog().lastIndexOf("\n"));
          log = log.substring(log.lastIndexOf("\n"));

          msg += "\n" + log;
          msg = Utility.formatMessageBDToHtml(msg);

          if (vars.commandIn("CONFIGURE", "CONFIGURE_MODULE_INSTALL", "CONFIGURE_MODULE_UPDATE",
              "CONFIGURE_MODULE_UPGRADE")) {
            OBError err = new OBError();
            err.setType("Error");
            err.setMessage(msg);
            vars.setMessage(HB_tabId, err);
            printPageRedirect(response, vars);
          } else {
            advisePopUpRefresh(request, response, "ERROR", "Heartbeat Configuration", msg);
          }
          return;
        }

        // Scheduling the process
        final OBCriteria<ProcessRequest> prCriteria = OBDal.getInstance().createCriteria(
            ProcessRequest.class);
        prCriteria.add(Restrictions.and(
            Restrictions.eq(ProcessRequest.PROPERTY_PROCESS, HBProcess), Restrictions.or(
                Restrictions.eq(ProcessRequest.PROPERTY_STATUS,
                    org.openbravo.scheduling.Process.UNSCHEDULED), Restrictions.eq(
                    ProcessRequest.PROPERTY_STATUS, org.openbravo.scheduling.Process.MISFIRED))));
        final List<ProcessRequest> requestList = prCriteria.list();

        ProcessRequest pr = null;

        if (requestList.size() == 0) { // Creating a process request
          pr = OBProvider.getInstance().get(ProcessRequest.class);
          pr.setProcess(HBProcess);
          pr.setActive(true);
          final ProcessContext context = new ProcessContext(vars);
          pr.setOpenbravoContext(context.toString());
        } else {
          pr = requestList.get(0);
        }

        // Schedule the next beat in 7 days
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DATE, 7);
        pr.setStartDate(c1.getTime());

        // At today's same time
        pr.setStartTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        pr.setSecurityBasedOnRole(true);
        pr.setDailyOption(EVERY_N_DAYS);
        pr.setDailyInterval(Long.parseLong("7"));
        pr.setTiming(SCHEDULE);

        OBDal.getInstance().save(pr);

        final ProcessBundle bundle2 = ProcessBundle.request(pr.getId(), vars, connectionProvider);

        // at this point a commit has to be done because the OBScheduler uses its
        // own connection provider and if it is not committed the OBScheduler won't
        // see the ProcessRequest if it is new.
        OBDal.getInstance().commitAndClose();
        // refresh the connection provider
        connectionProvider = new DalConnectionProvider();

        if (requestList.size() == 0) {
          OBScheduler.getInstance().schedule(pr.getId(), bundle2);
        } else {
          OBScheduler.getInstance().reschedule(pr.getId(), bundle2);
        }

        if (vars.commandIn("CONFIGURE_MODULE_INSTALL", "CONFIGURE_MODULE_UPDATE",
            "CONFIGURE_MODULE_UPGRADE")) {
          // Continue with the module install
          String recordId = vars.getStringParameter("inpcRecordId", IsIDFilter.instance);
          String command = vars.getCommand().endsWith("UPDATE") ? "UPDATE" : vars.getCommand()
              .endsWith("UPGRADE") ? "UPGRADE" : "INSTALL";

          if (command.equals("UPDATE")) {
            recordId = (recordId.equals(ModuleManagement.UPDATE_ALL_RECORD_ID) ? "&inpcUpdate=all"
                : "&inpcUpdate=" + recordId);
          } else if (command.equals("UPGRADE")) {
            recordId = "&inpcUpdate=" + recordId;
            recordId += "&upgradeVersion=" + vars.getStringParameter("version");
          } else {
            recordId = "&inpcRecordId=" + recordId;
          }

          response.sendRedirect(strDireccion + "/ad_forms/ModuleManagement.html?Command=" + command
              + recordId);
          return;
        } else {
          // Prompt HB configured
          String msg = Utility.messageBD(connectionProvider, "HB_SUCCESS", vars.getLanguage());
          advisePopUpRefresh(request, response, "SUCCESS", "Heartbeat Configuration", msg);
        }
      } catch (Exception e) {
        log4j.error(e.getMessage(), e);
        advisePopUpRefresh(request, response, "ERROR", "Heartbeat Configuration", e.getMessage());
      }
    }
  }

  private void printPageRedirect(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/HeartbeatRedirect")
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    out.println(xmlDocument.print());
    out.close();
  }
}
