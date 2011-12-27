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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.json.JsonConstants;

/**
 * Implements the ActionHandler and provides utility methods to sub classes.
 * 
 * @author mtaal
 */
public abstract class BaseActionHandler implements ActionHandler {

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ActionHandler#execute(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  public void execute() {
    final StringBuilder sb = new StringBuilder();
    String line;
    try {
      final HttpServletRequest request = RequestContext.get().getRequest();
      final BufferedReader reader = new BufferedReader(new InputStreamReader(
          request.getInputStream(), "UTF-8"));
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      final String content = (sb.length() > 0 ? sb.toString() : null);

      final Map<String, Object> parameterMap = new HashMap<String, Object>();
      for (Enumeration<?> keys = request.getParameterNames(); keys.hasMoreElements();) {
        final String key = (String) keys.nextElement();
        if (request.getParameterValues(key) != null && request.getParameterValues(key).length > 1) {
          parameterMap.put(key, request.getParameterValues(key));
        } else {
          parameterMap.put(key, request.getParameter(key));
        }
      }
      // also add the Http Stuff
      parameterMap.put(KernelConstants.HTTP_SESSION, request.getSession(false));
      parameterMap.put(KernelConstants.HTTP_REQUEST, request);

      final JSONObject result = execute(parameterMap, content);
      final HttpServletResponse response = RequestContext.get().getResponse();
      response.setContentType(JsonConstants.JSON_CONTENT_TYPE);
      response.setHeader("Content-Type", JsonConstants.JSON_CONTENT_TYPE);
      response.getWriter().write(result.toString());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Needs to be implemented by a subclass.
   * 
   * @param parameters
   *          the parameters obtained from the request. Note that the request object and the session
   *          object are also present in this map, resp. as the constants
   *          {@link KernelConstants#HTTP_REQUEST} and {@link KernelConstants#HTTP_SESSION}.
   * @param content
   *          the request content (if any)
   * @return the return should be a JSONObject, this is passed back to the caller on the client.
   */
  protected abstract JSONObject execute(Map<String, Object> parameters, String content);
}
