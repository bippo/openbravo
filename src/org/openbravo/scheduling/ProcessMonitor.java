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
package org.openbravo.scheduling;

import static org.openbravo.scheduling.Process.COMPLETE;
import static org.openbravo.scheduling.Process.ERROR;
import static org.openbravo.scheduling.Process.EXECUTION_ID;
import static org.openbravo.scheduling.Process.PROCESSING;
import static org.openbravo.scheduling.Process.SCHEDULED;
import static org.openbravo.scheduling.Process.SUCCESS;
import static org.openbravo.scheduling.Process.UNSCHEDULED;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.ConnectionProviderContextListener;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/**
 * @author awolski
 * 
 */
class ProcessMonitor implements SchedulerListener, JobListener, TriggerListener {

  static final Logger log = Logger.getLogger(ProcessMonitor.class);

  public static final String KEY = "org.openbravo.scheduling.ProcessMonitor.KEY";

  private String name;

  private SchedulerContext context;

  public ProcessMonitor(String name, SchedulerContext context) {
    this.name = name;
    this.context = context;
  }

  public void jobScheduled(Trigger trigger) {
    final ProcessBundle bundle = (ProcessBundle) trigger.getJobDataMap().get(ProcessBundle.KEY);
    final ProcessContext ctx = bundle.getContext();
    try {
      ProcessRequestData.update(getConnection(), ctx.getUser(), ctx.getUser(), SCHEDULED, bundle
          .getChannel().toString(), null, null, null, null, ctx.toString(), trigger.getName());

    } catch (final ServletException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void triggerFired(Trigger trigger, JobExecutionContext jec) {
    final ProcessBundle bundle = (ProcessBundle) jec.getMergedJobDataMap().get(ProcessBundle.KEY);
    final ProcessContext ctx = bundle.getContext();
    try {
      ProcessRequestData.update(getConnection(), ctx.getUser(), ctx.getUser(), SCHEDULED, bundle
          .getChannel().toString(), format(trigger.getPreviousFireTime()),
          OBScheduler.sqlDateTimeFormat, format(trigger.getNextFireTime()), format(trigger
              .getFinalFireTime()), ctx.toString(), trigger.getName());

    } catch (final ServletException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void jobToBeExecuted(JobExecutionContext jec) {
    final ProcessBundle bundle = (ProcessBundle) jec.getMergedJobDataMap().get(ProcessBundle.KEY);
    if (bundle == null) {
      return;
    }
    final ProcessContext ctx = bundle.getContext();
    final String executionId = SequenceIdData.getUUID();
    try {
      ProcessRunData.insert(getConnection(), ctx.getOrganization(), ctx.getClient(), ctx.getUser(),
          ctx.getUser(), executionId, PROCESSING, null, null, jec.getJobDetail().getName());

      jec.put(EXECUTION_ID, executionId);
      jec.put(ProcessBundle.CONNECTION, getConnection());
      jec.put(ProcessBundle.CONFIG_PARAMS, getConfigParameters());

    } catch (final ServletException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void jobWasExecuted(JobExecutionContext jec, JobExecutionException jee) {
    final ProcessBundle bundle = (ProcessBundle) jec.getMergedJobDataMap().get(ProcessBundle.KEY);
    if (bundle == null) {
      return;
    }
    final ProcessContext ctx = bundle.getContext();
    try {
      final String executionId = (String) jec.get(EXECUTION_ID);
      final String executionLog = bundle.getLog().length() >= 4000 ? bundle.getLog().substring(0,
          3999) : bundle.getLog();
      if (jee == null) {
        ProcessRunData.update(getConnection(), ctx.getUser(), SUCCESS,
            getDuration(jec.getJobRunTime()), executionLog, executionId);
      } else {
        ProcessRunData.update(getConnection(), ctx.getUser(), ERROR,
            getDuration(jec.getJobRunTime()), executionLog, executionId);
      }

    } catch (final ServletException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void triggerFinalized(Trigger trigger) {
    try {
      ProcessRequestData.update(getConnection(), COMPLETE, trigger.getName());
    } catch (final ServletException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void jobUnscheduled(String triggerName, String triggerGroup) {
    try {
      ProcessRequestData.update(getConnection(), UNSCHEDULED, null, null, null, triggerName);

    } catch (final ServletException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void triggerMisfired(Trigger trigger) {
    // Not implemented
  }

  public void jobsPaused(String jobName, String jobGroup) {
    // Not implemented
  }

  public void jobsResumed(String jobName, String jobGroup) {
    // Not implemented
  }

  public void schedulerError(String msg, SchedulerException e) {
    // Not implemented
  }

  public void schedulerShutdown() {
    // Not implemented
  }

  public void triggersPaused(String triggerName, String triggerGroup) {
    // Not implemented
  }

  public void triggersResumed(String triggerName, String triggerGroup) {
    // Not implemented
  }

  public void jobExecutionVetoed(JobExecutionContext jec) {
    // Not implemented
  }

  public void triggerComplete(Trigger trigger, JobExecutionContext jec, int triggerInstructionCode) {
    // Not implemented
  }

  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jec) {
    // Not implemented
    return false;
  }

  /**
   * @return the database Connection Provider
   */
  public ConnectionProvider getConnection() {
    return (ConnectionProvider) context.get(ConnectionProviderContextListener.POOL_ATTRIBUTE);
  }

  /**
   * @return the configuration parameters.
   */
  public ConfigParameters getConfigParameters() {
    return (ConfigParameters) context.get(ConfigParameters.CONFIG_ATTRIBUTE);
  }

  /**
   * Formats a date according to the data time format.
   * 
   * @param date
   * @return a formatted date
   */
  public final String format(Date date) {
    final String dateTimeFormat = getConfigParameters().getJavaDateTimeFormat();
    return date == null ? null : new SimpleDateFormat(dateTimeFormat).format(date);
  }

  /**
   * Converts a duration in millis to a String
   * 
   * @param duration
   *          the duration in millis
   * @return a String representation of the duration
   */
  public static String getDuration(long duration) {

    final int milliseconds = (int) (duration % 1000);
    final int seconds = (int) ((duration / 1000) % 60);
    final int minutes = (int) ((duration / 60000) % 60);
    final int hours = (int) ((duration / 3600000) % 24);

    final String m = (milliseconds < 10 ? "00" : (milliseconds < 100 ? "0" : "")) + milliseconds;
    final String sec = (seconds < 10 ? "0" : "") + seconds;
    final String min = (minutes < 10 ? "0" : "") + minutes;
    final String hr = (hours < 10 ? "0" : "") + hours;

    return hr + ":" + min + ":" + sec + "." + m;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.quartz.JobListener#getName()
   */
  public String getName() {
    return name;
  }
}
