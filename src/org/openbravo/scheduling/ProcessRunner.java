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
package org.openbravo.scheduling;

import static org.openbravo.scheduling.Process.COMPLETE;
import static org.openbravo.scheduling.Process.ERROR;
import static org.openbravo.scheduling.Process.PROCESSING;
import static org.openbravo.scheduling.Process.SCHEDULED;
import static org.openbravo.scheduling.Process.SUCCESS;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;

/**
 * @author awolski
 * 
 */
public class ProcessRunner {

  static Logger log = Logger.getLogger(ProcessRunner.class);

  private ProcessBundle bundle;

  public ProcessRunner(ProcessBundle bundle) {
    this.bundle = bundle;
  }

  /**
   * Execute this process.
   * 
   * @param conn
   *          the database connection
   * @throws ServletException
   */
  public String execute(ConnectionProvider conn) throws ServletException {

    Process process = null;
    try {
      process = bundle.getProcessClass().newInstance();

    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      throw new ServletException(e.getMessage(), e);
    }
    final String requestId = SequenceIdData.getUUID();
    String status = SCHEDULED;

    final ProcessContext ctx = bundle.getContext();
    ProcessRequestData.insert(conn, ctx.getOrganization(), ctx.getClient(), ctx.getUser(),
        ctx.getUser(), requestId, bundle.getProcessId(), ctx.getUser(), status, "Direct",
        ctx.toString(), "", null, null, null, null);

    final String executionId = SequenceIdData.getUUID();
    final long startTime = System.currentTimeMillis();
    long endTime = startTime;

    status = PROCESSING;
    ProcessRunData.insert(conn, ctx.getOrganization(), ctx.getClient(), ctx.getUser(),
        ctx.getUser(), executionId, status, null, bundle.getLog(), requestId);

    try {
      log.debug("Calling execute on process " + requestId);
      process.execute(bundle);
      endTime = System.currentTimeMillis();
      status = SUCCESS;

    } catch (final Exception e) {
      endTime = System.currentTimeMillis();
      status = ERROR;
      log.error("Process " + requestId + " threw an Exception: " + e.getMessage(), e);
      throw new ServletException(e);
    } finally {
      final String duration = ProcessMonitor.getDuration(endTime - startTime);
      ProcessRequestData.update(conn, COMPLETE, requestId);
      ProcessRunData.update(conn, ctx.getUser(), status, duration, bundle.getLog(), executionId);
    }

    return executionId;
  }
}
