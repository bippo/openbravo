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

package org.openbravo.service.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.service.OBServiceException;

/**
 * The default servlet which catches all requests for a webservice. This servlet finds the
 * WebService instance implementing the requested service by calling the {@link OBProvider} with the
 * top segment in the path. When the WebService implementation is found the request is forwarded to
 * that service.
 * 
 * @author mtaal
 */

public class WebServiceServlet extends BaseWebServiceServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    try {
      final String segment = WebServiceUtil.getInstance().getFirstSegment(request.getPathInfo());
      final WebService ws = getWebService(segment);
      if (ws != null) {
        ws.doGet(getRemainingPath(request.getPathInfo(), segment), request, response);
      }
    } catch (final OBException e) {
      throw e;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    try {
      final String segment = WebServiceUtil.getInstance().getFirstSegment(request.getPathInfo());
      final WebService ws = getWebService(segment);
      if (ws != null) {
        ws.doPost(getRemainingPath(request.getPathInfo(), segment), request, response);
      }
    } catch (final OBException e) {
      throw e;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    try {
      final String segment = WebServiceUtil.getInstance().getFirstSegment(request.getPathInfo());
      final WebService ws = getWebService(segment);
      if (ws != null) {
        ws.doDelete(getRemainingPath(request.getPathInfo(), segment), request, response);
      }
    } catch (final OBException e) {
      throw e;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    try {
      final String segment = WebServiceUtil.getInstance().getFirstSegment(request.getPathInfo());
      final WebService ws = getWebService(segment);
      if (ws != null) {
        ws.doPut(getRemainingPath(request.getPathInfo(), segment), request, response);
      }
    } catch (final OBException e) {
      throw e;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  private WebService getWebService(String segment) {
    final Object o = OBProvider.getInstance().get(segment);
    if (o instanceof WebService) {
      return (WebService) o;
    }
    throw new OBServiceException("No WebService found using the name " + segment);
  }

  private String getRemainingPath(String pathInfo, String segment) {
    String localPathInfo = pathInfo;
    if (pathInfo.startsWith("/")) {
      localPathInfo = pathInfo.substring(1);
    }
    if (localPathInfo.length() == segment.length()) {
      return "";
    }
    return localPathInfo.substring(segment.length());
  }
}