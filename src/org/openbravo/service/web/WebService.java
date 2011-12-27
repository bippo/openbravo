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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBModulePrefixRequired;

/**
 * Defines the standard webservice interface which needs to be implemented by all webservices.
 * 
 * @author mtaal
 */

public interface WebService extends OBModulePrefixRequired {

  public enum ChangeAction {
    CREATE, UPDATE, DELETE
  }

  /**
   * Is called for the Http GET method.
   * 
   * @param path
   *          the HttpRequest.getPathInfo(), the part of the url after the context path
   * @param request
   *          the HttpServletRequest
   * @param response
   *          the HttpServletResponse
   */
  public void doGet(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  /**
   * Is called for the Http POST method.
   * 
   * @param path
   *          the HttpRequest.getPathInfo(), the part of the url after the context path
   * @param request
   *          the HttpServletRequest
   * @param response
   *          the HttpServletResponse
   */
  public void doPost(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  /**
   * Is called for the Http DELETE method.
   * 
   * @param path
   *          the HttpRequest.getPathInfo(), the part of the url after the context path
   * @param request
   *          the HttpServletRequest
   * @param response
   *          the HttpServletResponse
   */
  public void doDelete(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  /**
   * Is called for the Http PUT method.
   * 
   * @param path
   *          the HttpRequest.getPathInfo(), the part of the url after the context path
   * @param request
   *          the HttpServletRequest
   * @param response
   *          the HttpServletResponse
   */
  public void doPut(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception;
}