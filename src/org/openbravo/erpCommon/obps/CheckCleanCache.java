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

package org.openbravo.erpCommon.obps;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

/**
 * Servlet to check if the browser cache needs to be cleaned once after an instance activation or
 * deactivation.
 * 
 * It returns a small javascript part with the instance activation status. Depending on a request
 * parameter the response will be marked as cached (similar to images) or not-cached. By comparing
 * the cached and non-cached response it is possible to see it the browser cache contains data from
 * before the instance activation/deactivation.
 * 
 * @author huehner
 * 
 */
public class CheckCleanCache extends HttpBaseServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String cache = vars.getRequiredStringParameter("cache");
    boolean cached = new Boolean(cache);

    // either set explicit cache forever, or don't cache behavior
    if (cached) {
      response.addHeader("Expires", "Sun, 17 Jan 2038 19:14:07 GMT");
      response.addHeader("Cache-Control", "public");
    } else {
      response.addHeader("Expires", "-1");
      response.addHeader("Cache-Control", "no-cache");
    }

    // get instance active status from db
    boolean active = false;
    OBContext.setAdminMode();
    try {
      active = ActivationKey.getInstance().isActive();
      log4j.debug("Instance activate: " + active);
    } finally {
      OBContext.restorePreviousMode();
    }

    PrintWriter pw = response.getWriter();
    StringBuilder res = new StringBuilder();
    res.append("function isOpsInstance");
    if (cached) {
      res.append("Cached");
    }
    res.append("() { return ");
    res.append(String.valueOf(active));
    res.append(";};");
    pw.println(res);
  }

}
