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

/**
 * All Openbravo Processes that require scheduling through the {@link OBScheduler} or monitoring
 * through the {@link ProcessMonitor} are required to implement this interface.
 * 
 * @author awolski
 * 
 */
public interface Process {

  public static final String EXECUTION_ID = "param.execution.id";

  public static final String SCHEDULED = "SCH";

  public static final String UNSCHEDULED = "UNS";

  public static final String MISFIRED = "MIS";

  public static final String PROCESSING = "PRC";

  public static final String SUCCESS = "SUC";

  public static final String ERROR = "ERR";

  public static final String COMPLETE = "COM";

  /**
   * @param bundle
   *          the process' parameters, security and contextual information
   * @throws Exception
   *           if an error occurs executing the process
   */
  public void execute(ProcessBundle bundle) throws Exception;

}
