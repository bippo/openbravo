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
package org.openbravo.client.kernel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.web.WebServiceUtil;

/**
 * The main servlet responsible for handling all the requests for components from the system.
 * 
 * @author mtaal
 */
public class KernelServlet extends BaseKernelServlet {
  // private static final Logger log = Logger.getLogger(DataSourceServlet.class);
  private static final Logger log4j = Logger.getLogger(KernelServlet.class);

  // this is needed to support logout deep in the code...
  // TODO: make it easier to get to the authentication manager from
  // the
  public static final String KERNEL_SERVLET = "kernelServletInstance";

  private static final String REQUEST_HEADER_IFMODIFIEDSINCE = "If-Modified-Since";
  private static final String REQUEST_HEADER_IFNONEMATCH = "If-None-Match";

  private static final long serialVersionUID = 1L;

  private static String servletPathPart = "org.openbravo.client.kernel";

  // are used to compute the relative path
  private static ConfigParameters globalParameters;

  private static ServletContext servletContext;

  /**
   * @return the parameters as they are defined in the servlet context.
   */
  public static ConfigParameters getGlobalParameters() {
    return globalParameters;
  }

  public static String getServletPathPart() {
    return servletPathPart;
  }

  @Inject
  @Any
  private Instance<ComponentProvider> componentProviders;

  @Inject
  private WeldUtils weldUtils;

  public void init(ServletConfig config) {
    super.init(config);
    globalParameters = ConfigParameters.retrieveFrom(config.getServletContext());
    servletContext = config.getServletContext();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    if (!request.getRequestURI().contains("/" + servletPathPart)) {
      throw new UnsupportedOperationException("Invalid url " + request.getRequestURI());
    }

    final String action = request.getParameter(KernelConstants.ACTION_PARAMETER);
    if (action != null) {
      processActionRequest(request, response);
    } else {
      processComponentRequest(request, response);
    }
  }

  protected void processComponentRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    final int nameIndex = request.getRequestURI().indexOf(servletPathPart);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);
    if (pathParts.length < 2) {
      throw new UnsupportedOperationException("No service name present in url "
          + request.getRequestURI());
    }

    final String componentProviderName = pathParts[1];

    final ComponentProvider componentProvider = componentProviders.select(
        new ComponentProvider.Selector(componentProviderName)).get();

    final String componentId;
    if (pathParts.length > 2) {
      componentId = pathParts[2];
    } else {
      componentId = null;
    }

    final Map<String, Object> parameters = getParameterMap(request);
    final Component component = componentProvider.getComponent(componentId, parameters);
    OBContext.setAdminMode();
    String eTag;
    try {
      eTag = component.getETag();
    } finally {
      OBContext.restorePreviousMode();
    }
    final String requestETag = request.getHeader(REQUEST_HEADER_IFNONEMATCH);

    if (requestETag != null && eTag.equals(requestETag)) {
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      response.setDateHeader(RESPONSE_HEADER_LASTMODIFIED,
          request.getDateHeader(REQUEST_HEADER_IFMODIFIEDSINCE));
      return;
    }

    try {
      final String result = ComponentGenerator.getInstance().generate(component);

      response.setHeader(RESPONSE_HEADER_ETAG, eTag);
      response.setDateHeader(RESPONSE_HEADER_LASTMODIFIED, component.getLastModified().getTime());
      response.setContentType(component.getContentType());
      response.setHeader(RESPONSE_HEADER_CONTENTTYPE, component.getContentType());
      response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);

      final PrintWriter pw = response.getWriter();
      pw.write(result);
      pw.close();
    } catch (Exception e) {
      log4j.error(e.getMessage(), e);
      if (!response.isCommitted()) {
        response.setContentType(KernelConstants.JAVASCRIPT_CONTENTTYPE);
        response.setHeader(RESPONSE_HEADER_CONTENTTYPE, KernelConstants.JAVASCRIPT_CONTENTTYPE);
        response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);
        response.getWriter().write(KernelUtils.getInstance().createErrorJavaScript(e));
      }
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doGet(request, response);
  }

  protected void processActionRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    final String action = request.getParameter(KernelConstants.ACTION_PARAMETER);

    response.setContentType(KernelConstants.JAVASCRIPT_CONTENTTYPE);
    response.setHeader(RESPONSE_HEADER_CONTENTTYPE, KernelConstants.JAVASCRIPT_CONTENTTYPE);
    response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);

    try {
      @SuppressWarnings("unchecked")
      final Class<ActionHandler> actionHandlerClass = (Class<ActionHandler>) OBClassLoader
          .getInstance().loadClass(action);
      final ActionHandler actionHandler = weldUtils.getInstance(actionHandlerClass);
      actionHandler.execute();
    } catch (Exception e) {
      log4j.error("Error executing action " + action + " error: " + e.getMessage(), e);
      if (!response.isCommitted()) {
        response.setContentType(KernelConstants.JAVASCRIPT_CONTENTTYPE);
        response.setHeader(RESPONSE_HEADER_CONTENTTYPE, KernelConstants.JAVASCRIPT_CONTENTTYPE);
        response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);
        response.getWriter().write(KernelUtils.getInstance().createErrorJavaScript(e));
      }
    }

  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    throw new UnsupportedOperationException("Only GET/POST is supported");
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    throw new UnsupportedOperationException("Only GET/POST is supported");
  }

  @SuppressWarnings("rawtypes")
  private Map<String, Object> getParameterMap(HttpServletRequest request) {
    final Map<String, Object> parameterMap = new HashMap<String, Object>();
    for (Enumeration keys = request.getParameterNames(); keys.hasMoreElements();) {
      final String key = (String) keys.nextElement();
      parameterMap.put(key, request.getParameter(key));
    }

    if (!parameterMap.containsKey(KernelConstants.HTTP_SESSION)) {
      parameterMap.put(KernelConstants.HTTP_SESSION, request.getSession());
    }

    if (!parameterMap.containsKey(KernelConstants.CONTEXT_URL)) {
      parameterMap.put(KernelConstants.CONTEXT_URL, computeContextURL(request));
    }

    if (!parameterMap.containsKey(KernelConstants.SERVLET_CONTEXT)) {
      parameterMap.put(KernelConstants.SERVLET_CONTEXT, servletContext);
    }

    if (!parameterMap.containsKey(KernelConstants.SKIN_PARAMETER)) {
      if (OBContext.getOBContext().isNewUI()) { // FIXME: isNewUI true the first load?
        parameterMap.put(KernelConstants.SKIN_PARAMETER, KernelConstants.SKIN_DEFAULT);
      } else {
        parameterMap.put(KernelConstants.SKIN_PARAMETER, KernelConstants.SKIN_CLASSIC);
      }
    }

    return parameterMap;
  }

  private String computeContextURL(HttpServletRequest request) {
    return HttpBaseUtils.getLocalAddress(request);
  }
}
