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

package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

public class ChangeAudit extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    // do not save navigation history
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);
    String strWindowId = vars.getStringParameter("inpwindowId");

    String strCurrentValueAudit = vars.getSessionValue("P|" + strWindowId + "|ShowAudit");
    if (strCurrentValueAudit.equals(""))
      strCurrentValueAudit = vars.getSessionValue("#ShowAudit");
    String newValue;
    if (strCurrentValueAudit.equals("Y"))
      newValue = "N";
    else
      newValue = "Y";
    vars.setSessionValue("P|" + strWindowId + "|ShowAudit", newValue);
    PrintWriter out = response.getWriter();
    out.println("Audit value:" + newValue + " - window:" + strWindowId);
    out.close();
  }

}
