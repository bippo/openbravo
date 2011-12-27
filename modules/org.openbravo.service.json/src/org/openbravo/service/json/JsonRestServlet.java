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
package org.openbravo.service.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.CheckException;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.web.BaseWebServiceServlet;
import org.openbravo.service.web.InvalidContentException;
import org.openbravo.service.web.InvalidRequestException;
import org.openbravo.service.web.ResourceNotFoundException;
import org.openbravo.service.web.WebServiceUtil;

/**
 * A web service which provides a JSON REST service. Makes extensive use of the
 * {@link DefaultJsonDataService}.
 * 
 * @author mtaal
 */
public class JsonRestServlet extends BaseWebServiceServlet {
  private static final Logger log = Logger.getLogger(JsonRestServlet.class);

  private static final long serialVersionUID = 1L;

  private static String servletPathPart = "org.openbravo.service.json.jsonrest";

  public static String getServletPathPart() {
    return servletPathPart;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    if (config.getInitParameter(JsonConstants.JSON_REST_URL_NAME_PARAM) != null) {
      servletPathPart = config.getInitParameter(JsonConstants.JSON_REST_URL_NAME_PARAM);
    }
    super.init(config);
  }

  protected void doService(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      callServiceInSuper(request, response);
      response.setStatus(200);
    } catch (final InvalidRequestException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(400);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final InvalidContentException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(409);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final ResourceNotFoundException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(404);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final OBSecurityException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(401);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final Throwable t) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(500);
      log.error(t.getMessage(), t);
      writeResult(response, JsonUtils.convertExceptionToJson(t));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    try {
      final Map<String, String> parameters = getParameterMap(request);
      // checks and set parameters, if not valid then go away
      if (!checkSetParameters(request, response, parameters)) {
        return;
      }
      // now do the action
      String result = DefaultJsonDataService.getInstance().fetch(parameters);

      // a special case if the id is asked directly then only return the single record.
      if (parameters.containsKey(JsonConstants.ID)) {
        final JSONObject jsonObject = new JSONObject(result);
        final JSONObject responseObject = jsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE);
        if (responseObject.get(JsonConstants.DATA) instanceof JSONArray) {
          final JSONArray jsonArray = responseObject.getJSONArray(JsonConstants.DATA);
          if (jsonArray.length() == 0) {
            throw new ResourceNotFoundException("Object (" + parameters.get(JsonConstants.ID) + "/"
                + parameters.get(JsonConstants.ENTITYNAME) + ") not found");
          } else if (jsonArray.length() == 1) {
            result = jsonArray.getJSONObject(0).toString();
          }
        } else {
          final JSONObject jsonDataObject = responseObject.getJSONObject(JsonConstants.DATA);
          result = jsonDataObject.toString();
        }
      }
      writeResult(response, result);

    } catch (final JSONException e) {
      throw new InvalidContentException(e);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final Map<String, String> parameters = getParameterMap(request);
    // note if clause updates parameter map
    if (checkSetIDEntityName(request, response, parameters)) {
      final String result = DefaultJsonDataService.getInstance().add(parameters,
          getRequestContent(request));
      writeResult(response, result);
    }
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    final Map<String, String> parameters = getParameterMap(request);
    // checks and set parameters, if not valid then go away
    if (!checkSetParameters(request, response, parameters)) {
      return;
    }
    final String id = parameters.get(JsonConstants.ID);
    if (id == null) {
      throw new InvalidRequestException("No id parameter");
    }
    final String entityName = parameters.get(JsonConstants.ENTITYNAME);
    if (entityName == null) {
      throw new InvalidRequestException("No entityName parameter");
    }
    final BaseOBObject bob = OBDal.getInstance().get(entityName, id);
    if (bob == null) {
      throw new ResourceNotFoundException("Object (" + id + "/" + entityName + ") not found");
    }

    // now do the action
    final String result = DefaultJsonDataService.getInstance().remove(parameters);
    writeResult(response, result);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final Map<String, String> parameters = getParameterMap(request);
    // note if clause updates parameter map
    if (checkSetIDEntityName(request, response, parameters)) {
      final String result = DefaultJsonDataService.getInstance().update(parameters,
          getRequestContent(request));
      writeResult(response, result);
    }
  }

  private boolean checkSetParameters(HttpServletRequest request, HttpServletResponse response,
      Map<String, String> parameters) throws IOException {
    if (!request.getRequestURI().contains("/" + servletPathPart)) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, the path should contain the service name: " + servletPathPart)));
      return false;
    }
    final int nameIndex = request.getRequestURI().indexOf(servletPathPart);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);
    if (pathParts.length == 0 || !pathParts[0].equals(servletPathPart)) {
      writeResult(
          response,
          JsonUtils.convertExceptionToJson(new InvalidRequestException("Invalid url: "
              + request.getRequestURI())));
      return false;
    }
    if (pathParts.length == 1) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, no entityName: " + request.getRequestURI())));
      return false;
    }
    final String entityName = pathParts[1];
    // check it the entity
    try {
      ModelProvider.getInstance().getEntity(entityName);
    } catch (CheckException e) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, no entity found with entityName: " + request.getRequestURI())));
      return false;
    }
    parameters.put(JsonConstants.ENTITYNAME, entityName);
    if (pathParts.length > 2) {
      // search on the exact id
      parameters.put(JsonConstants.ID, pathParts[2]);
      if (!parameters.containsKey(JsonConstants.TEXTMATCH_PARAMETER)) {
        parameters.put(JsonConstants.TEXTMATCH_PARAMETER, JsonConstants.TEXTMATCH_EXACT);
        parameters.put(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE, JsonConstants.TEXTMATCH_EXACT);
      }
    }
    return true;
  }

  private Map<String, String> getParameterMap(HttpServletRequest request) {
    final Map<String, String> parameterMap = new HashMap<String, String>();
    for (@SuppressWarnings("rawtypes")
    Enumeration keys = request.getParameterNames(); keys.hasMoreElements();) {
      final String key = (String) keys.nextElement();
      parameterMap.put(key, request.getParameter(key));
    }
    return parameterMap;
  }

  // NOTE: parameters parameter is updated inside this method
  private boolean checkSetIDEntityName(HttpServletRequest request, HttpServletResponse response,
      Map<String, String> parameters) throws IOException {
    if (!request.getRequestURI().contains("/" + servletPathPart)) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, the path should contain the service name: " + servletPathPart)));
      return false;
    }
    final int nameIndex = request.getRequestURI().indexOf(servletPathPart);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);
    if (pathParts.length == 0 || !pathParts[0].equals(servletPathPart)) {
      writeResult(
          response,
          JsonUtils.convertExceptionToJson(new InvalidRequestException("Invalid url: "
              + request.getRequestURI())));
      return false;
    }
    if (pathParts.length == 1) {
      return true;
    }
    final String entityName = pathParts[1];
    // check it the entity
    try {
      ModelProvider.getInstance().getEntity(entityName);
    } catch (CheckException e) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, no entity found with entityName: " + request.getRequestURI())));
      return false;
    }
    parameters.put(JsonConstants.ENTITYNAME, entityName);
    if (pathParts.length > 2) {
      // search on the exact id
      parameters.put(JsonConstants.ID, pathParts[2]);
      if (!parameters.containsKey(JsonConstants.TEXTMATCH_PARAMETER)) {
        parameters.put(JsonConstants.TEXTMATCH_PARAMETER, JsonConstants.TEXTMATCH_EXACT);
        parameters.put(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE, JsonConstants.TEXTMATCH_EXACT);
      }
    }
    return true;
  }

  private void writeResult(HttpServletResponse response, String result) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Content-Type", "application/json;charset=UTF-8");

    final Writer w = response.getWriter();
    w.write(result);
    w.close();
  }

  private String getRequestContent(HttpServletRequest request) throws IOException {
    final BufferedReader reader = request.getReader();
    if (reader == null) {
      return "";
    }
    String line;
    final StringBuilder sb = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(line);
    }
    log.debug("REQUEST CONTENT>>>>");
    log.debug(sb.toString());
    return sb.toString();
  }

}
