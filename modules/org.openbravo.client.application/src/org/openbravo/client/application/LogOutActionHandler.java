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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.client.kernel.BaseKernelServlet.KernelHttpServletResponse;

/**
 * Is responsible for logging out from the application.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class LogOutActionHandler extends BaseActionHandler {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.client.kernel.BaseActionHandler#execute(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void execute() {
    final HttpServletResponse response = RequestContext.get().getResponse();
    if (response instanceof KernelHttpServletResponse) {
      final KernelHttpServletResponse kernelResponse = (KernelHttpServletResponse) response;
      kernelResponse.setDoLogout(true);
    }
  }

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    throw new UnsupportedOperationException();
  }
}
