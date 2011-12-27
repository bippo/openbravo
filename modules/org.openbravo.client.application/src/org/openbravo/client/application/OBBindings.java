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
package org.openbravo.client.application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.dal.core.OBContext;

/**
 * JS - Java binding to use in JavaScript expressions.
 * 
 * @author iperdomo
 */
public class OBBindings {

  private Logger log = Logger.getLogger(OBBindings.class);

  private OBContext context;
  private Map<String, String> requestMap;
  private HttpSession httpSession;
  private SimpleDateFormat dateFormat = null;
  private SimpleDateFormat dateTimeFormat = null;

  public OBBindings(OBContext obContext) {
    Check.isNotNull(obContext, "The OBContext parameter cannot be null");
    context = obContext;
  }

  public OBBindings(OBContext obContext, Map<String, String> parameters) {
    Check.isNotNull(obContext, "The OBContext parameter cannot be null");
    context = obContext;
    requestMap = parameters;
  }

  public OBBindings(OBContext obContext, Map<String, String> parameters, HttpSession session) {
    Check.isNotNull(obContext, "The OBContext parameter cannot be null");
    context = obContext;

    Check.isNotNull(session, "The HttpSession parameter cannot be null");
    httpSession = session;

    requestMap = parameters;

    dateFormat = new SimpleDateFormat((String) httpSession.getAttribute("#AD_JAVADATEFORMAT"));

    dateTimeFormat = new SimpleDateFormat(
        (String) httpSession.getAttribute("#AD_JAVADATETIMEFORMAT"));
  }

  public OBContext getContext() {
    return context;
  }

  public Map<String, String> getParameters() {
    return requestMap;
  }

  public HttpSession getSession() {
    return httpSession;
  }

  private boolean checkRequestMap() {
    if (requestMap == null) {
      log.warn("Accessing request parameters map without initializing it");
      return false;
    }
    return true;
  }

  /**
   * Checks if is a Sales Order transaction, based on the parameters of the HTTP request
   * 
   * @return null if there is no request parameters, or the inpissotrx request parameter is not
   *         available
   */
  public Boolean isSalesTransaction() {
    if (requestMap == null) {
      log.warn("Requesting isSOTrx check without request parameters");
      return null;
    }

    String value = requestMap.get(OBBindingsConstants.SO_TRX_PARAM);
    if (value != null) {
      return "Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }
    if (httpSession == null) {
      log.warn("Requesting isSOTrx check without request parameters and session");
      return null;
    }

    value = (String) httpSession.getAttribute("inpisSOTrxTab");
    if (value != null) {
      return "Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    value = (String) httpSession.getAttribute(getWindowId() + "|ISSOTRX");
    if (value != null) {
      return "Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }
    return null;
  }

  public String getWindowId() {
    if (!checkRequestMap()) {
      return null;
    }
    return (String) requestMap.get(OBBindingsConstants.WINDOW_ID_PARAM);
  }

  public String getTabId() {
    if (!checkRequestMap()) {
      return null;
    }
    return (String) requestMap.get(OBBindingsConstants.TAB_ID_PARAM);
  }

  public String getCommandType() {
    if (!checkRequestMap()) {
      return null;
    }
    return (String) requestMap.get(OBBindingsConstants.COMMAND_TYPE_PARAM);
  }

  public Boolean isPosted() {
    if (!checkRequestMap()) {
      return null;
    }
    return "Y".equalsIgnoreCase(requestMap.get(OBBindingsConstants.POSTED_PARAM));
  }

  public Boolean isProcessed() {
    if (!checkRequestMap()) {
      return null;
    }
    return "Y".equalsIgnoreCase(requestMap.get(OBBindingsConstants.PROCESSED_PARAM));
  }

  public String formatDate(Date d) {
    return dateFormat.format(d);
  }

  public String formatDateTime(Date d) {
    return dateTimeFormat.format(d);
  }

  public Date parseDate(String date) {
    try {
      Date result = dateFormat.parse(date);
      return result;
    } catch (Exception e) {
      log.error("Error parsing string date " + date + " with format: " + dateFormat, e);
    }
    return null;
  }

  public Date parseDateTime(String dateTime) {
    try {
      Date result = dateTimeFormat.parse(dateTime);
      return result;
    } catch (Exception e) {
      log.error("Error parsing string date " + dateTime + " with format: " + dateTimeFormat, e);
    }
    return null;
  }

  public String formatDate(Date d, String format) {
    Check.isNotNull(format, "Format is a required parameter");
    SimpleDateFormat df = new SimpleDateFormat(format);
    return df.format(d);
  }

  public Date parseDate(String date, String format) {
    Check.isNotNull(format, "Format is a required parameter");
    try {
      SimpleDateFormat df = new SimpleDateFormat(format);
      return df.parse(date);
    } catch (Exception e) {
      log.error("Error parsing string " + date + " with format: " + format, e);
    }
    return null;
  }

  public String getFilterExpression(String className) {
    Check.isNotNull(className, "The class name must not be null");
    FilterExpression expr;
    try {
      expr = (FilterExpression) OBClassLoader.getInstance().loadClass(className).newInstance();
      return expr.getExpression(requestMap);
    } catch (Exception e) {
      log.error("Error trying to get filter expression from class: " + className, e);
    }
    return "";
  }

}
