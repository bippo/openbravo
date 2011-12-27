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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * 
 * @author gorkaion
 */
public abstract class JSExpressionCallout extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  private static final String CLEAR_MSG_CODE = "['MESSAGE','']";
  private static final String JS_ERROR_MSG_PREFIX = "sun.org.mozilla.javascript.internal.EcmaError:";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      try {
        printPage(vars, request, response);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else {
      pageError(response);
    }
  }

  private void printPage(VariablesSecureApp vars, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    xmlDocument.setParameter("array", getResponse(vars, request, getExpression(vars)));
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  protected abstract String getExpression(VariablesSecureApp vars);

  private String getResponse(VariablesSecureApp vars, HttpServletRequest request, String expression)
      throws ServletException {

    StringBuffer sb = new StringBuffer();
    sb.append("var calloutName='").append(this.getClass().getName()).append("';\n");
    sb.append("var respuesta = [");

    if (expression.equals("")) {
      sb.append(CLEAR_MSG_CODE + "];\n");
      return sb.toString();
    }

    sb.append(CLEAR_MSG_CODE);

    try {
      Object result = ParameterUtils.getJSExpressionResult(getParameterMap(request),
          request.getSession(false), expression);

      sb.append(", ['INFO','"
          + Utility.messageBD(this, "OBUIAPP_CALLOUT_JS_EXPR_RESULT", vars.getLanguage()) + " "
          + (result) + "']");
    } catch (Exception e) {
      String errorMsg = e.getMessage().substring(JS_ERROR_MSG_PREFIX.length());
      sb.append(", ['WARNING','"
          + Utility.messageBD(this, "OBUIAPP_CALLOUT_JS_EXPR_ERROR", vars.getLanguage()) + " "
          + errorMsg + "']");
    }
    sb.append("];\n");
    return sb.toString();
  }

  private Map<String, String> getParameterMap(HttpServletRequest request) {
    final Map<String, String> parameterMap = new HashMap<String, String>();
    for (Enumeration<?> keys = request.getParameterNames(); keys.hasMoreElements();) {
      final String key = (String) keys.nextElement();
      parameterMap.put(key, request.getParameter(key));
    }
    return parameterMap;
  }
}
