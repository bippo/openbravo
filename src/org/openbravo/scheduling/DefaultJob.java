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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.scheduling;

import org.apache.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Openbravo implementation of the Quartz Job interface to enable Openbravo processes to be
 * scheduled through the Quartz Scheduler. DefaultJob simply removes the {@link Process} and
 * {@link ProcessBundle} objects from the JobExecutionContext and executes them.
 * 
 * @author awolski
 * 
 */
public class DefaultJob implements Job {

  static Logger log = Logger.getLogger(DefaultJob.class);

  /**
   * See the execute method of the Quartz Job class.
   */
  public void execute(JobExecutionContext jec) throws JobExecutionException {
    final ProcessBundle bundle = (ProcessBundle) jec.getMergedJobDataMap().get(ProcessBundle.KEY);
    try {
      final Process process = bundle.getProcessClass().newInstance();
      bundle.setConnection((ConnectionProvider) jec.get(ProcessBundle.CONNECTION));
      bundle.setConfig((ConfigParameters) jec.get(ProcessBundle.CONFIG_PARAMS));
      bundle.setLog(new ProcessLogger(bundle.getConnection()));

      // Set audit info
      SessionInfo.setUserId(bundle.getContext().getUser());
      SessionInfo.setProcessType("P");
      SessionInfo.setProcessId(bundle.getProcessId());

      process.execute(bundle);

    } catch (final Exception e) {
      log.error("Error executing process " + bundle.toString(), e);
      throw new JobExecutionException(e);
    }
  }
}
