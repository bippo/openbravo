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
package org.openbravo.erpCommon.ad_process;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.scheduling.ProcessBundle;

/**
 * @author awolski
 * 
 */
public class ScheduleProcess extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  private static final String PROCESS_REQUEST_ID = "AD_Process_Request_ID";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    final String windowId = vars.getStringParameter("inpwindowId");
    final String requestId = vars.getSessionValue(windowId + "|" + PROCESS_REQUEST_ID);

    String message = null;
    try {
      final ProcessBundle bundle = ProcessBundle.request(requestId, vars, this);
      OBScheduler.getInstance().schedule(requestId, bundle);

    } catch (final Exception e) {
      message = Utility.messageBD(this, "SCHED_ERROR", vars.getLanguage());
      String processErrorTit = Utility.messageBD(this, "Error", vars.getLanguage());
      advisePopUp(request, response, "ERROR", processErrorTit, message + " " + e.getMessage());
    }
    message = Utility.messageBD(this, "SCHED_SUCCESS", vars.getLanguage());
    String processTitle = Utility.messageBD(this, "Success", vars.getLanguage());
    advisePopUpRefresh(request, response, "SUCCESS", processTitle, message);
  }
}
