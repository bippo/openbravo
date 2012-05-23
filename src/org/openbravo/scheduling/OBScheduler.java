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
 * All portions are Copyright (C) 2008-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.scheduling;

import static org.openbravo.scheduling.Process.SCHEDULED;
import static org.openbravo.scheduling.Process.UNSCHEDULED;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.ConnectionProviderContextListener;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

/**
 * @author awolski
 * 
 */
public class OBScheduler {

  private static final OBScheduler INSTANCE = new OBScheduler();

  static Logger log = Logger.getLogger(OBScheduler.class);

  private static final String OB_GROUP = "OB_QUARTZ_GROUP";

  public static final String KEY = "org.openbravo.scheduling.OBSchedulingContext.KEY";

  private Scheduler sched;

  private SchedulerContext ctx;

  public static String dateTimeFormat;

  public static String sqlDateTimeFormat;

  private OBScheduler() {
  }

  /**
   * @return the singleton instance of this class
   */
  public static final OBScheduler getInstance() {
    return INSTANCE;
  }

  /**
   * @return The Quartz Scheduler instance used by OBScheduler.
   */
  public Scheduler getScheduler() {
    return sched;
  }

  /**
   * Retrieves the Openbravo ConnectionProvider from the Scheduler Context.
   * 
   * @return A ConnectionProvider
   */
  public ConnectionProvider getConnection() {
    return (ConnectionProvider) ctx.get(ConnectionProviderContextListener.POOL_ATTRIBUTE);
  }

  /**
   * Retrieves the Openbravo ConfigParameters from the Scheduler context.
   * 
   * @return Openbravo ConfigParameters
   */
  public ConfigParameters getConfigParameters() {
    return (ConfigParameters) ctx.get(ConfigParameters.CONFIG_ATTRIBUTE);
  }

  /**
   * Schedule a new process (bundle) to run immediately in the background with the default Openbravo
   * Job implementation.
   * 
   * This will create a new record in AD_PROCESS_REQUEST.
   * 
   * @param bundle
   * @throws SchedulerException
   */
  public void schedule(ProcessBundle bundle) throws SchedulerException, ServletException {
    schedule(bundle, DefaultJob.class.asSubclass(Job.class));
  }

  /**
   * Schedule a new process (bundle) to run immediately in the background with the specified Job
   * implementation.
   * 
   * This will create a new record in AD_PROCESS_REQUEST.
   * 
   * @param bundle
   *          The bundle with all of the process' details
   * @param jobClass
   *          The Quartz Job implementation that will execute when
   * 
   * @throws SchedulerException
   *           If something goes wrong.
   */
  public void schedule(ProcessBundle bundle, Class<? extends Job> jobClass)
      throws SchedulerException, ServletException {
    if (bundle == null) {
      throw new SchedulerException("Process bundle cannot be null.");
    }
    final String requestId = SequenceIdData.getUUID();

    final String processId = bundle.getProcessId();
    final String channel = bundle.getChannel().toString();
    final ProcessContext context = bundle.getContext();

    ProcessRequestData.insert(getConnection(), context.getOrganization(), context.getClient(),
        context.getUser(), context.getUser(), requestId, processId, context.getUser(), SCHEDULED,
        channel.toString(), context.toString(), bundle.getParamsDeflated(), null, null, null, null);

    schedule(requestId, bundle, jobClass);
  }

  /**
   * Schedule a process (bundle) with the specified requestId and using the default Openbravo Job
   * implementation. The requestId is used in Quartz as the JobDetail's name. The details must be
   * saved to AD_PROCESS_REQUEST before reaching this method.
   * 
   * @param requestId
   *          the id of the request, the Quartz jobDetail name
   * @param bundle
   *          the context bundle
   * @throws SchedulerException
   */
  public void schedule(String requestId, ProcessBundle bundle) throws SchedulerException,
      ServletException {
    schedule(requestId, bundle, DefaultJob.class.asSubclass(Job.class));
  }

  /**
   * Schedule a process (bundle) with the specified requestId and the specified Job implementation.
   * The requestId is used in Quartz as the JobDetail's name. The details must be saved to
   * AD_PROCESS_REQUEST before reaching this method.
   * 
   * @param requestId
   * @param bundle
   * @param jobClass
   * @throws SchedulerException
   */
  public void schedule(String requestId, ProcessBundle bundle, Class<? extends Job> jobClass)
      throws SchedulerException, ServletException {
    if (requestId == null) {
      throw new SchedulerException("Request Id cannot be null.");
    }
    if (bundle == null) {
      throw new SchedulerException("Process bundle cannot be null.");
    }
    if (jobClass == null) {
      throw new SchedulerException("Job class cannot be null.");
    }
    final JobDetail jobDetail = JobDetailProvider.newInstance(requestId, bundle, jobClass);
    final Trigger trigger = TriggerProvider.newInstance(requestId, bundle, getConnection());

    sched.scheduleJob(jobDetail, trigger);
  }

  /**
   * @param requestId
   * @param bundle
   * @throws SchedulerException
   * @throws ServletException
   */
  public void reschedule(String requestId, ProcessBundle bundle) throws SchedulerException,
      ServletException {
    try {
      sched.unscheduleJob(requestId, OB_GROUP);
      sched.deleteJob(requestId, OB_GROUP);

    } catch (final SchedulerException e) {
      log.error("An error occurred rescheduling process " + bundle.toString(), e);
    }
    schedule(requestId, bundle);
  }

  public void unschedule(String requestId, ProcessContext context) throws SchedulerException {
    try {
      sched.unscheduleJob(requestId, OB_GROUP);
      sched.deleteJob(requestId, OB_GROUP);
      ProcessRequestData.update(getConnection(), UNSCHEDULED, null, OBScheduler.sqlDateTimeFormat,
          format(new Date()), requestId);
    } catch (final Exception e) {
      log.error("An error occurred unscheduling process " + requestId, e);
    }
  }

  /**
   * @param date
   * @return the date as a formatted string
   */
  public static final String format(Date date) {
    return date == null ? null : new SimpleDateFormat(dateTimeFormat).format(date);
  }

  /**
   * @param schdlr
   * @throws SchedulerException
   */
  public void initialize(Scheduler schdlr) throws SchedulerException {
    this.ctx = schdlr.getContext();
    this.sched = schdlr;

    final ProcessMonitor monitor = new ProcessMonitor("Monitor." + OB_GROUP, this.ctx);
    schdlr.addSchedulerListener(monitor);
    schdlr.addGlobalJobListener(monitor);
    schdlr.addGlobalTriggerListener(monitor);

    dateTimeFormat = getConfigParameters().getJavaDateTimeFormat();
    sqlDateTimeFormat = getConfigParameters().getSqlDateTimeFormat();

    ProcessRequestData[] data = null;
    try {
      data = ProcessRequestData.selectByStatus(getConnection(), SCHEDULED);

      for (final ProcessRequestData request : data) {
        final String requestId = request.id;
        final VariablesSecureApp vars = ProcessContext.newInstance(request.obContext).toVars();
        try {
          final ProcessBundle bundle = ProcessBundle.request(requestId, vars, getConnection());
          schedule(requestId, bundle);

        } catch (final ServletException e) {
          log.error("Error scheduling process: " + e.getMessage(), e);
        }
      }
    } catch (final ServletException e) {
      log.error("An error occurred retrieving scheduled process data: " + e.getMessage(), e);
    }
  }

  /**
   * @author awolski
   * 
   */
  private static class JobDetailProvider {

    /**
     * Creates a new JobDetail with the specified name and job class. Inserts the process bundle
     * into the JobDetail's jobDataMap for retrieval when the job is executed.
     * 
     * @param name
     *          The name of the JobDetail
     * @param bundle
     *          The Openbravo process bundle.
     * @param jobClass
     *          The class to be executed when Job.execute is called.
     * @return
     * @throws SchedulerException
     */
    private static JobDetail newInstance(String name, ProcessBundle bundle,
        Class<? extends Job> jobClass) throws SchedulerException {
      if (bundle == null) {
        throw new SchedulerException("Process bundle cannot be null.");
      }
      final JobDetail jobDetail = new JobDetail(name, OB_GROUP, jobClass);
      jobDetail.getJobDataMap().put(ProcessBundle.KEY, bundle);

      return jobDetail;
    }
  }

  /**
   * @author awolski
   */
  private static class TriggerProvider {

    private static final String TIMING_OPTION_IMMEDIATE = "I";

    private static final String TIMING_OPTION_LATER = "L";

    private static final String TIMING_OPTION_SCHEDULED = "S";

    private static final String FREQUENCY_SECONDLY = "1";

    private static final String FREQUENCY_MINUTELY = "2";

    private static final String FREQUENCY_HOURLY = "3";

    private static final String FREQUENCY_DAILY = "4";

    private static final String FREQUENCY_WEEKLY = "5";

    private static final String FREQUENCY_MONTHLY = "6";

    private static final String FREQUENCY_CRON = "7";

    private static final String FINISHES = "Y";

    private static final String WEEKDAYS = "W";

    private static final String WEEKENDS = "E";

    private static final String EVERY_N_DAYS = "N";

    private static final String MONTH_OPTION_FIRST = "1";

    private static final String MONTH_OPTION_SECOND = "2";

    private static final String MONTH_OPTION_THIRD = "3";

    private static final String MONTH_OPTION_FOURTH = "4";

    private static final String MONTH_OPTION_LAST = "L";

    private static final String MONTH_OPTION_SPECIFIC = "S";

    /**
     * Loads the trigger details from AD_PROCESS_REQUEST and converts them into a schedulable Quartz
     * Trigger instance.
     * 
     * @return
     */
    private static Trigger newInstance(String name, ProcessBundle bundle, ConnectionProvider conn)
        throws ServletException {

      final TriggerData data = TriggerData.select(conn, dateTimeFormat, name);

      Trigger trigger = null;

      if (data == null) {
        trigger = new SimpleTrigger(name, OB_GROUP, new Date());
        trigger.getJobDataMap().put(ProcessBundle.KEY, bundle);
        return trigger;
      }

      Calendar start = null;
      Calendar finish = null;
      try {
        final String timingOption = data.timingOption;
        if ("".equals(timingOption) || timingOption.equals(TIMING_OPTION_IMMEDIATE)) {
          trigger = new SimpleTrigger(name, OB_GROUP, new Date());

        } else if (data.timingOption.equals(TIMING_OPTION_LATER)) {
          trigger = new SimpleTrigger();
          start = timestamp(data.startDate, data.startTime, dateTimeFormat);
          trigger.setStartTime(start.getTime());

        } else if (data.timingOption.equals(TIMING_OPTION_SCHEDULED)) {
          start = timestamp(data.startDate, data.startTime, dateTimeFormat);

          final int second = start.get(Calendar.SECOND);
          final int minute = start.get(Calendar.MINUTE);
          final int hour = start.get(Calendar.HOUR_OF_DAY);

          if (data.frequency.equals(FREQUENCY_SECONDLY)) {
            trigger = makeIntervalTrigger(FREQUENCY_SECONDLY, data.secondlyInterval,
                data.secondlyRepetitions);

          } else if (data.frequency.equals(FREQUENCY_MINUTELY)) {
            trigger = makeIntervalTrigger(FREQUENCY_MINUTELY, data.minutelyInterval,
                data.minutelyRepetitions);

          } else if (data.frequency.equals(FREQUENCY_HOURLY)) {
            trigger = makeIntervalTrigger(FREQUENCY_HOURLY, data.hourlyInterval,
                data.hourlyRepetitions);

          } else if (data.frequency.equals(FREQUENCY_DAILY)) {
            if ("".equals(data.dailyOption)) {
              trigger = TriggerUtils.makeDailyTrigger(hour, minute);

            } else if (data.dailyOption.equals(EVERY_N_DAYS)) {
              try {
                final int interval = Integer.parseInt(data.dailyInterval);
                trigger = TriggerUtils.makeHourlyTrigger(interval * 24);

              } catch (final NumberFormatException e) {
                throw new ParseException("Invalid interval specified.", -1);
              }

            } else if (data.dailyOption.equals(WEEKDAYS)) {
              final String cronExpression = second + " " + minute + " " + hour + " ? * MON-FRI";
              trigger = new CronTrigger(name, OB_GROUP, cronExpression);

            } else if (data.dailyOption.equals(WEEKENDS)) {
              final String cronExpression = second + " " + minute + " " + hour + " ? * SAT,SUN";
              trigger = new CronTrigger(name, OB_GROUP, cronExpression);

            } else {
              throw new ParseException("At least one option must be selected.", -1);
            }

          } else if (data.frequency.equals(FREQUENCY_WEEKLY)) {
            final StringBuilder sb = new StringBuilder();
            if (data.daySun.equals("Y"))
              sb.append("SUN");
            if (data.dayMon.equals("Y"))
              sb.append(sb.length() == 0 ? "MON" : ",MON");
            if (data.dayTue.equals("Y"))
              sb.append(sb.length() == 0 ? "TUE" : ",TUE");
            if (data.dayWed.equals("Y"))
              sb.append(sb.length() == 0 ? "WED" : ",WED");
            if (data.dayThu.equals("Y"))
              sb.append(sb.length() == 0 ? "THU" : ",THU");
            if (data.dayFri.equals("Y"))
              sb.append(sb.length() == 0 ? "FRI" : ",FRI");
            if (data.daySat.equals("Y"))
              sb.append(sb.length() == 0 ? "SAT" : ",SAT");

            if (sb.length() != 0) {
              sb.insert(0, second + " " + minute + " " + hour + " ? * ");
              trigger = new CronTrigger(name, OB_GROUP, sb.toString());
            } else {
              throw new ParseException("At least one day must be selected.", -1);
            }

          } else if (data.frequency.equals(FREQUENCY_MONTHLY)) {
            final StringBuilder sb = new StringBuilder();
            sb.append(second + " " + minute + " " + hour + " ");

            if (data.monthlyOption.equals(MONTH_OPTION_FIRST)
                || data.monthlyOption.equals(MONTH_OPTION_SECOND)
                || data.monthlyOption.equals(MONTH_OPTION_THIRD)
                || data.monthlyOption.equals(MONTH_OPTION_FOURTH)) {
              final String num = data.monthlyOption;
              final int day = Integer.parseInt(data.monthlyDayOfWeek) + 1;
              sb.append("? * " + (day > 7 ? 1 : day) + "#" + num);

            } else if (data.monthlyOption.equals(MONTH_OPTION_LAST)) {
              sb.append("L * ?");

            } else if (data.monthlyOption.equals(MONTH_OPTION_SPECIFIC)) {
              sb.append(Integer.parseInt(data.monthlySpecificDay) + " * ?");
            } else {
              throw new ParseException("At least one month option be selected.", -1);
            }
            trigger = new CronTrigger(name, OB_GROUP, sb.toString());

          } else if (data.frequency.equals(FREQUENCY_CRON)) {
            trigger = new CronTrigger(name, OB_GROUP, data.cron);
          } else {
            throw new ServletException("Invalid option: " + data.frequency);
          }

          if (data.nextFireTime.equals("")) {
            trigger.setStartTime(start.getTime());
          } else {
            Calendar nextTriggerTime = timestamp(data.nextFireTime, data.nextFireTime,
                dateTimeFormat);
            trigger.setStartTime(nextTriggerTime.getTime());
          }

          if (data.finishes.equals(FINISHES)) {
            finish = timestamp(data.finishesDate, data.finishesTime, dateTimeFormat);
            trigger.setEndTime(finish.getTime());
          }

        }
      } catch (final ParseException e) {
        final String msg = Utility.messageBD(conn, "TRIG_INVALID_DATA", bundle.getContext()
            .getLanguage());
        throw new ServletException(msg + " " + e.getMessage());
      }

      if (trigger.getName() == null)
        trigger.setName(name);
      if (trigger.getGroup() == null)
        trigger.setGroup(OB_GROUP);

      trigger.getJobDataMap().put(ProcessBundle.KEY, bundle);

      return trigger;
    }

    private static final Trigger makeIntervalTrigger(String type, String interval,
        String repititions) throws ParseException {
      try {
        final int i = Integer.parseInt(interval);
        int r = SimpleTrigger.REPEAT_INDEFINITELY;
        if (!repititions.trim().equals("")) {
          r = Integer.parseInt(repititions);
        }
        if (type.equals(FREQUENCY_SECONDLY)) {
          return TriggerUtils.makeSecondlyTrigger(i, r);

        } else if (type.equals(FREQUENCY_MINUTELY)) {
          return TriggerUtils.makeMinutelyTrigger(i, r);

        } else if (type.equals(FREQUENCY_HOURLY)) {
          return TriggerUtils.makeHourlyTrigger(i, r);
        }
        return null;

      } catch (final NumberFormatException e) {
        throw new ParseException("Invalid interval or repitition value.", -1);
      }
    }

    /**
     * Utility method to parse a start date string and a start time string into a date.
     * 
     * @param date
     * @param time
     * @param dtFormat
     * @return
     * @throws ParseException
     */
    private static Calendar timestamp(String date, String time, String dtFormat)
        throws ParseException {

      if (dtFormat == null || dtFormat.trim().equals("")) {
        throw new ParseException("dateTimeFormat cannot be null.", -1);
      }

      Calendar cal = null;
      final String dateFormat = dtFormat.substring(0, dtFormat.indexOf(' '));

      if (date == null || date.equals("")) {
        cal = Calendar.getInstance();
      } else {
        cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat(dateFormat).parse(date));
      }

      if (time != null && !time.equals("")) {
        final int hour = Integer.parseInt(time.substring(time.indexOf(" ") + 1, time.indexOf(':')));
        final int minute = Integer.parseInt(time.substring(time.indexOf(':') + 1,
            time.lastIndexOf(':')));
        final int second = Integer
            .parseInt(time.substring(time.lastIndexOf(':') + 1, time.length()));

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
      }

      return cal;
    }
  }
}
