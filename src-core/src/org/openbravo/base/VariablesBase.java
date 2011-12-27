/*
 ************************************************************************************
 * Copyright (C) 2001-2011 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.log4j.Logger;
import org.openbravo.base.filter.NumberFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.utils.FormatUtilities;

/**
 * This class is used to manage parameters passed to the servlets either using the URL with HTTP GET
 * method or through the HTTP POST method.
 * 
 * @author Openbravo
 * 
 */
public class VariablesBase {
  HttpSession session;
  HttpServletRequest httpRequest;
  private String postDataHash = null;
  private List<String> sortedParameters = null;
  public boolean isMultipart = false;
  List<FileItem> items;
  private final String DEFAULT_FORMAT_NAME = "qtyEdition";

  static Logger log4j = Logger.getLogger(VariablesBase.class);

  /**
   * Default empty constructor
   */
  public VariablesBase() {
  }

  /**
   * Basic constructor that takes the request object and saves it to be used by the subsequent
   * methods
   * 
   * @param request
   *          HttpServletRequest object originating from the user request.
   */
  @SuppressWarnings("unchecked")
  public VariablesBase(HttpServletRequest request) {
    this.session = request.getSession(true);
    this.httpRequest = request;
    this.isMultipart = ServletFileUpload.isMultipartContent(new ServletRequestContext(request));
    if (isMultipart) {
      DiskFileItemFactory factory = new DiskFileItemFactory();
      // factory.setSizeThreshold(yourMaxMemorySize);
      // factory.setRepositoryPath(yourTempDirectory);
      ServletFileUpload upload = new ServletFileUpload(factory);
      // upload.setSizeMax(yourMaxRequestSize);
      try {
        items = upload.parseRequest(request);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Overloaded constructor, used to prevent session removal in case of multipart requests.
   * 
   * @param request
   *          HttpServletRequest object originating from the user request.
   * @param f
   *          Dummy boolean only used for overloading the constructor.
   */
  public VariablesBase(HttpServletRequest request, boolean f) {
    this.session = request.getSession(true);
    this.httpRequest = request;
  }

  /**
   * Utility function which checks a list of input values against the provided request filter. It
   * does return normally when all input are accepted, otherwise it throws an exception. If the
   * requestFilter parameter is null, then no validation is performed.
   * 
   * @param requestFilter
   *          filter used to validate the input against
   * @param inputs
   *          list of inputs to validate
   * @throws ServletException
   *           thrown is at least one input value was not accepted by the filter
   */
  private static void filterRequest(RequestFilter requestFilter, String... inputs)
      throws ServletException {
    if (requestFilter == null) {
      return;
    }
    for (String input : inputs) {
      if (!requestFilter.accept(input)) {
        log4j.error("Input: " + input + " not accepted by filter: " + requestFilter,
            new Throwable());
        throw new ServletException("Input: " + input + " is not an accepted input");
      }
    }
  }

  /**
   * Returns the MD5 string hash based on the post data
   * 
   * @return
   */
  private String computeHash() {

    long t = 0;
    if (log4j.isDebugEnabled())
      t = System.currentTimeMillis();
    StringBuffer postString = new StringBuffer();

    for (String parameter : sortedParameters()) {
      String value = httpRequest.getParameter(parameter);
      postString.append(parameter + "=" + value + ",");
    }

    String s = DigestUtils.md5Hex(postString.toString());

    if (log4j.isDebugEnabled()) {
      log4j.debug("calculated hash: " + s);
      log4j.debug("post data hash computation took: "
          + String.valueOf(System.currentTimeMillis() - t) + " ms");
    }

    return s;
  }

  /**
   * Getter for the postDataHas member
   * 
   * @return The MD5 hash of the post data
   */
  public String getPostDataHash() {
    if (postDataHash == null) {
      postDataHash = computeHash();
    }
    return postDataHash;
  }

  /**
   * Sorts the list of parameters in the request
   * 
   * @return A sorted list of parameters
   */
  private List<String> sortedParameters() {
    if (sortedParameters == null) {
      sortedParameters = new ArrayList<String>();
      for (@SuppressWarnings("rawtypes")
      Enumeration e = httpRequest.getParameterNames(); e.hasMoreElements();) {
        String parameter = (String) e.nextElement();
        if (!parameter.equalsIgnoreCase("Command") && !parameter.contains("ProcessId")) {
          sortedParameters.add(parameter);
        }
      }
      Collections.sort(sortedParameters);
    }
    return sortedParameters;
  }

  /**
   * @see #getGlobalVariable(String,String,boolean,boolean,boolean,String,RequestFilter)
   */
  public String getGlobalVariable(String requestParameter, String sessionAttribute,
      boolean clearSession, boolean requestRequired, boolean sessionRequired, String defaultValue)
      throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, clearSession, requestRequired,
        sessionRequired, defaultValue, null);
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, it then tries to return
   * the session value specified by the sessionAttribute parameter. Before returning the final value
   * it also saves it to the session variable with the name specified by sessionAtribute parameter.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method
   * @param sessionAttribute
   *          the name of the parameter to be retrieved from the session variable in case the
   *          requestParameter is not found
   * @param clearSession
   *          If true, then the value found within the session variable specified by the
   *          sessionAttribute parameter is cleared and reset to the value specified by the
   *          defaultValue parameter. This will consequently also be the value returned then.
   * @param requestRequired
   *          If true, method throws an exception in case the parameter is not found among the ones
   *          passed to the servlet by the HTTP GET/POST method.
   * @param sessionRequired
   *          If true, method throws an exception in case the parameter is not found among the ones
   *          passed to the servlet by the HTTP GET/POST method or the ones stored in the session.
   * @param defaultValue
   *          If requestRequired or sessionRequired are false then the value returned by the method
   *          will take this value in case the parameter value is not found within the sought
   *          locations.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the value of the parameter.
   * @throws ServletException
   */
  public String getGlobalVariable(String requestParameter, String sessionAttribute,
      boolean clearSession, boolean requestRequired, boolean sessionRequired, String defaultValue,
      RequestFilter requestFilter) throws ServletException {
    String auxStr = getStringParameter(requestParameter, requestFilter);

    if (log4j.isDebugEnabled())
      log4j.debug("Request parameter: " + requestParameter + ":..." + auxStr);
    if (!(auxStr.equals(""))) {
      setSessionValue(sessionAttribute, auxStr);
    } else {
      if (requestRequired) {
        throw new ServletException("Request parameter required: " + requestParameter);
      } else {
        auxStr = getSessionValue(sessionAttribute);
        if (!sessionAttribute.equalsIgnoreCase("menuVertical") && log4j.isDebugEnabled())
          log4j.debug("Session attribute: " + sessionAttribute + ":..." + auxStr);
        if (auxStr.equals("")) {
          if (sessionRequired) {
            throw new ServletException("Session attribute required: " + sessionAttribute);
          } else {
            auxStr = defaultValue;
            if (log4j.isDebugEnabled())
              log4j.debug("Default value:..." + auxStr);
            setSessionValue(sessionAttribute, auxStr);
          }
        } else {
          if (clearSession) {
            auxStr = defaultValue;
            if (auxStr.equals(""))
              removeSessionValue(sessionAttribute);
            else
              setSessionValue(sessionAttribute, auxStr);
          }
        }
      }
    }
    return auxStr;
  }

  /**
   * @see #getInGlobalVariable(String,String,boolean,boolean,boolean,String,RequestFilter)
   */
  @Deprecated
  public String getInGlobalVariable(String requestParameter, String sessionAttribute,
      boolean clearSession, boolean requestRequired, boolean sessionRequired, String defaultValue)
      throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, clearSession, requestRequired,
        sessionRequired, defaultValue, null);
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, it then tries to return
   * the session value specified by the sessionAttribute parameter. Before returning the final value
   * it also saves it to the session variable with the name specified by sessionAtribute parameter.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the parameter to be retrieved from the session variable in case the
   *          requestParameter is not found.
   * @param clearSession
   *          If true, then the value of the session variable specified by the sessionAttribute
   *          parameter is cleared. Consequently, an empty string is returned in case
   *          requestParameter was not found previously.
   * @param requestRequired
   *          If true, method throws an exception in case the parameter is not found among the ones
   *          passed to the servlet by the HTTP GET/POST method.
   * @param sessionRequired
   *          If true, method throws an exception in case the parameter is not found among the ones
   *          passed to the servlet by the HTTP GET/POST method or the ones stored in the session.
   * @param defaultValue
   *          If requestRequired or sessionRequired are false then the value returned by the method
   *          will take this value in case the parameter value is not found within the sought
   *          locations.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of comma separated values within parentheses. For example
   *         ('value1', 'value2').
   * @throws ServletException
   */
  public String getInGlobalVariable(String requestParameter, String sessionAttribute,
      boolean clearSession, boolean requestRequired, boolean sessionRequired, String defaultValue,
      RequestFilter requestFilter) throws ServletException {
    String auxStr = getInStringParameter(requestParameter, requestFilter);
    if (log4j.isDebugEnabled())
      log4j.debug("Request IN parameter: " + requestParameter + ":..." + auxStr);
    if (!(auxStr.equals(""))) {
      setSessionValue(sessionAttribute, auxStr);
    } else {
      if (requestRequired) {
        throw new ServletException("Request IN parameter required: " + requestParameter);
      } else {
        auxStr = getSessionValue(sessionAttribute);
        if (log4j.isDebugEnabled())
          log4j.debug("Session IN attribute: " + sessionAttribute + ":..." + auxStr);
        if (auxStr.equals("")) {
          if (sessionRequired) {
            throw new ServletException("Session IN attribute required: " + sessionAttribute);
          } else {
            auxStr = defaultValue;
            if (log4j.isDebugEnabled())
              log4j.debug("Default value:..." + auxStr);
            setSessionValue(sessionAttribute, auxStr);
          }
        } else {
          if (clearSession) {
            auxStr = "";
            removeSessionValue(sessionAttribute);
          }
        }
      }
    }
    return auxStr;
  }

  /**
   * @see #getGlobalVariable(String,String,String,RequestFilter)
   */
  public String getGlobalVariable(String requestParameter, String sessionAttribute,
      String defaultValue) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, defaultValue, null);
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, it then tries to return
   * the session value specified by the sessionAttribute parameter. If none are found the
   * defaultValue is returned. Before returning the final value it also saves it to the session
   * variable with the name specified by sessionAtribute parameter.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the parameter to be retrieved from the session variable in case the
   *          requestParameter is not found.
   * @param defaultValue
   *          The value returned by the method will take this value in case the parameter value is
   *          not found within the sought locations.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the value of the parameter.
   * @throws ServletException
   */
  public String getGlobalVariable(String requestParameter, String sessionAttribute,
      String defaultValue, RequestFilter requestFilter) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false, false, false, defaultValue,
        requestFilter);
  }

  /**
   * @see #getGlobalVariable(String,String,RequestFilter)
   */
  public String getGlobalVariable(String requestParameter, String sessionAttribute)
      throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false, false, true, "", null);
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, it then tries to return
   * the session value specified by the sessionAttribute parameter. If none are found, an Exception
   * is thrown. If one of the values is found, it is also stored into the session variable with the
   * name specified by sessionAtribute parameter.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the parameter to be retrieved from the session variable in case the
   *          requestParameter is not found.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the value of the parameter.
   * @throws ServletException
   */
  public String getGlobalVariable(String requestParameter, String sessionAttribute,
      RequestFilter requestFilter) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false, false, true, "",
        requestFilter);
  }

  /**
   * @see #getRequiredGlobalVariable(String,String,RequestFilter)
   */
  public String getRequiredGlobalVariable(String requestParameter, String sessionAttribute)
      throws ServletException {
    return getRequiredGlobalVariable(requestParameter, sessionAttribute, null);
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, an exception is thrown.
   * Otherwise the value is also stored into the session variable with the name specified by
   * sessionAtribute parameter before being returned.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method
   * @param sessionAttribute
   *          The name of the session variable where the value of the parameter specified by the
   *          requestParameter should be stored.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the value of the parameter.
   * @throws ServletException
   */
  public String getRequiredGlobalVariable(String requestParameter, String sessionAttribute,
      RequestFilter requestFilter) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false, true, true, "",
        requestFilter);
  }

  /**
   * @see #getRequestGlobalVariable(String,String,RequestFilter)
   */
  public String getRequestGlobalVariable(String requestParameter, String sessionAttribute)
      throws ServletException {
    return getRequestGlobalVariable(requestParameter, sessionAttribute, null);
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, it then tries to return
   * the session value specified by the sessionAttribute parameter. If none are found an empty
   * string is returned. Before returning the final value it also clears the session variable with
   * the name specified by sessionAtribute parameter and sets its value to whatever value is being
   * returned.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the parameter to be retrieved from the session variable in case the
   *          requestParameter is not found.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the value of the parameter.
   * @throws ServletException
   */
  public String getRequestGlobalVariable(String requestParameter, String sessionAttribute,
      RequestFilter requestFilter) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, true, false, false, "",
        requestFilter);
  }

  /**
   * @see #getRequiredInputGlobalVariable(String,String,String,RequestFilter)
   */
  public String getRequiredInputGlobalVariable(String requestParameter, String sessionAttribute,
      String defaultStr) throws ServletException {
    return getRequiredInputGlobalVariable(requestParameter, sessionAttribute, defaultStr, null);
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, it then tries to return
   * the session value specified by the sessionAttribute parameter. If none are found the value of
   * defaultStr parameter is returned. Before returning the final value it also clears the session
   * variable with the name specified by sessionAtribute parameter and sets its value to whatever
   * value is being returned.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the parameter to be retrieved from the session variable in case the
   *          requestParameter is not found.
   * @param defaultStr
   *          The value returned by the method will take this value in case the parameter value is
   *          not found within the sought locations.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the value of the parameter.
   * @throws ServletException
   */
  public String getRequiredInputGlobalVariable(String requestParameter, String sessionAttribute,
      String defaultStr, RequestFilter requestFilter) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, true, false, false, defaultStr,
        requestFilter);
  }

  /**
   * @see #getInGlobalVariable(String,String,String,RequestFilter)
   */
  @Deprecated
  public String getInGlobalVariable(String requestParameter, String sessionAttribute,
      String defaultValue) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, defaultValue, null);
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, it then tries to return
   * the session value specified by the sessionAttribute parameter. Before returning the final value
   * it also saves it to the session variable with the name specified by sessionAtribute parameter.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the parameter to be retrieved from the session variable in case the
   *          requestParameter is not found.
   * @param defaultValue
   *          The value returned by the method will take this value in case the parameter value is
   *          not found within the sought locations.
   * @return String containing the set of comma separated values within parentheses. For example
   *         ('value1', 'value2').
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @throws ServletException
   */
  public String getInGlobalVariable(String requestParameter, String sessionAttribute,
      String defaultValue, RequestFilter requestFilter) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false, false, false,
        defaultValue, requestFilter);
  }

  /**
   * @see #getInGlobalVariable(String,String,RequestFilter)
   */
  @Deprecated
  public String getInGlobalVariable(String requestParameter, String sessionAttribute)
      throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false, false, true, "", null);
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the HTTP GET/POST method. If
   * this parameter is not found among the ones submitted to the servlet, it then tries to return
   * the session value specified by the sessionAttribute parameter. If not found here, it throws an
   * exception. If successful, it also saves the return to the session variable with the name
   * specified by sessionAtribute parameter.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the parameter to be retrieved from the session variable in case the
   *          requestParameter is not found.
   * @return String containing the set of comma separated values within parentheses. For example
   *         ('value1', 'value2').
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @throws ServletException
   */
  public String getInGlobalVariable(String requestParameter, String sessionAttribute,
      RequestFilter requestFilter) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false, false, true, "",
        requestFilter);
  }

  /**
   * @see #getRequiredInGlobalVariable(String,String,RequestFilter)
   */
  @Deprecated
  public String getRequiredInGlobalVariable(String requestParameter, String sessionAttribute)
      throws ServletException {
    return getRequiredInGlobalVariable(requestParameter, sessionAttribute, null);
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the HTTP GET/POST method. If
   * not found, it throws an exception. If successful, it also saves the return to the session
   * variable with the name specified by sessionAtribute parameter.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the session variable to set the value to in case the parameter value is
   *          successfully found.
   * @return String containing the set of comma separated values within parentheses. For example
   *         ('value1', 'value2').
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @throws ServletException
   */
  public String getRequiredInGlobalVariable(String requestParameter, String sessionAttribute,
      RequestFilter requestFilter) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false, true, true, "",
        requestFilter);
  }

  /**
   * @see #getRequestInGlobalVariable(String,String,RequestFilter)
   */
  @Deprecated
  public String getRequestInGlobalVariable(String requestParameter, String sessionAttribute)
      throws ServletException {
    return getRequestInGlobalVariable(requestParameter, sessionAttribute, null);
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the HTTP GET/POST method. If
   * found, it saves the value into a session variable specified by the sessionAttribute parameter
   * adn returns it. If not found, it clears the session variable specified by the sessionAttribute
   * parameter and returns an empty string.
   * 
   * @param requestParameter
   *          The name of the parameter to be retrieved from the parameters passed to the servlet by
   *          the HTTP GET/POST method.
   * @param sessionAttribute
   *          The name of the session variable to clear in case the above parameter does not exist.
   * @return String containing the set of comma separated values within parentheses. For example
   *         ('value1', 'value2').
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @throws ServletException
   */
  public String getRequestInGlobalVariable(String requestParameter, String sessionAttribute,
      RequestFilter requestFilter) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, true, false, false, "",
        requestFilter);
  }

  /**
   * @see #getStringParameter(String,String,RequestFilter)
   */
  public String getStringParameter(String parameter, String defaultValue) {
    return getStringParameter(parameter, defaultValue, null);
  }

  /**
   * Retrieves a single parameter value passed to the servlet by the HTTP GET/POST method. If value
   * is not found among submitted data, the defaultValue will be returned.
   * 
   * @param parameter
   *          The name of the parameter that needs to be retrieved.
   * @param defaultValue
   *          Value to be returned in case the parameter was not passed to the servlet.
   * @return String containing the value of the parameter passed to the servlet by the HTTP GET/POST
   *         method.
   */
  public String getStringParameter(String parameter, String defaultValue,
      RequestFilter requestFilter) {
    try {
      return getStringParameter(parameter, false, defaultValue, requestFilter);
    } catch (Exception e) {
      return null;
    }
  }

  public String getNumericParameter(String parameter) throws ServletException {
    String value = getStringParameter(parameter);

    if (value.equals("")) {
      return value;
    }

    String newValue = transformNumber(value);

    if (!NumberFilter.instance.accept(newValue)) {
      log4j.error("Input: " + parameter + " not accepted by filter: " + NumberFilter.instance);
      throw new ServletException("Input: " + parameter + " with value " + newValue
          + " is not an accepted input");
    }

    return newValue;
  }

  public String getNumericParameter(String parameter, String defaultValue) throws ServletException {
    String value = getStringParameter(parameter, false, defaultValue);

    if (value.equals("")) {
      return value;
    }

    String newValue = transformNumber(value);

    if (!NumberFilter.instance.accept(newValue)) {
      log4j.error("Input: " + parameter + " not accepted by filter: " + NumberFilter.instance);
      throw new ServletException("Input: " + parameter + " is not an accepted input");
    }

    return newValue;
  }

  public String getRequiredNumericParameter(String parameter) throws ServletException {
    String value = getRequiredStringParameter(parameter);

    if (value.equals("")) {
      return value;
    }

    String newValue = transformNumber(value);

    if (!NumberFilter.instance.accept(newValue)) {
      log4j.error("Input: " + parameter + " not accepted by filter: " + NumberFilter.instance);
      throw new ServletException("Input: " + parameter + " is not an accepted input");
    }

    return newValue;
  }

  public String getRequiredNumericParameter(String parameter, String defaultValue)
      throws ServletException {
    String value = getStringParameter(parameter, true, defaultValue);

    if (value.equals("")) {
      return value;
    }

    String newValue = transformNumber(value);

    if (!NumberFilter.instance.accept(newValue)) {
      log4j.error("Input: " + parameter + " not accepted by filter: " + NumberFilter.instance);
      throw new ServletException("Input: " + parameter + " is not an accepted input");
    }

    return newValue;
  }

  /**
   * @see #getGlobalVariable(String,String,String)
   */
  public String getNumericGlobalVariable(String requestParameter, String sessionAttribute,
      String defaultValue) throws ServletException {
    String res = getNumericParameter(requestParameter);
    if (!res.equals("")) {
      setSessionValue(sessionAttribute, res);
    } else {
      res = getSessionValue(sessionAttribute);
      if (res.equals("")) {
        res = defaultValue;
        setSessionValue(sessionAttribute, res);
      }
    }
    return res;
  }

  /**
   * @see #getRequestGlobalVariable(String,String)
   */
  public String getNumericRequestGlobalVariable(String requestParameter, String sessionAttribute)
      throws ServletException {
    String res = getNumericParameter(requestParameter);
    if (!res.equals("")) {
      setSessionValue(sessionAttribute, res);
    } else {
      res = getSessionValue(sessionAttribute);
      if (!res.equals("")) {
        removeSessionValue(sessionAttribute);
      }
    }
    return res;
  }

  private String transformNumber(String number) throws ServletException {
    String value = number;
    String groupSeparator = getSessionValue("#GROUPSEPARATOR|" + DEFAULT_FORMAT_NAME);
    String decimalSeparator = getSessionValue("#DECIMALSEPARATOR|" + DEFAULT_FORMAT_NAME);

    if (groupSeparator.equals("") || decimalSeparator.equals("")) {
      log4j.error("Error while trying to transform number: groupSeparator = " + groupSeparator
          + " ,decimalSeparator = " + decimalSeparator);
      throw new ServletException("Error while trying to transform numeric input");
    }

    if (groupSeparator.equals(".")) {
      groupSeparator = "\\.";
    }

    value = value.replaceAll(groupSeparator, "");

    if (!decimalSeparator.equals(".")) {
      value = value.replaceAll(decimalSeparator, ".");
    }

    return value;
  }

  /**
   * @see #getStringParameter(String,RequestFilter)
   */
  public String getStringParameter(String parameter) {
    return getStringParameter(parameter, "", null);
  }

  /**
   * Retrieves a single parameter value passed to the servlet by the HTTP GET/POST method. If value
   * is not found among submitted data, an empty string is returned.
   * 
   * @param parameter
   *          The name of the parameter to be retrieved.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String with the value of the parameter. If value is not found among submitted data, an
   *         empty string is returned.
   */
  public String getStringParameter(String parameter, RequestFilter requestFilter) {
    return getStringParameter(parameter, "", requestFilter);
  }

  /**
   * @see #getRequiredStringParameter(String,RequestFilter)
   */
  public String getRequiredStringParameter(String parameter) throws ServletException {
    return getRequiredStringParameter(parameter, null);
  }

  /**
   * Retrieves the single parameter value passed to the servlet by the HTTP GET/POST method. If
   * value is not found among submitted data, a ServletException is thrown.
   * 
   * @param parameter
   *          The name of the parameter to be retrieved.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String with the value of the parameter.
   * @throws ServletException
   */
  public String getRequiredStringParameter(String parameter, RequestFilter requestFilter)
      throws ServletException {
    return getStringParameter(parameter, true, "", requestFilter);
  }

  /**
   * @see #getStringParameter(String,boolean,String,RequestFilter)
   */
  public String getStringParameter(String parameter, boolean required, String defaultValue)
      throws ServletException {
    return getStringParameter(parameter, required, defaultValue, null);
  }

  /**
   * Retrieves the single parameter value passed to the servlet by the HTTP GET/POST method.
   * 
   * @param parameter
   *          The name of the parameter to be retrieved.
   * @param required
   *          If true, method throws an exception if the value of the parameter is not found in the
   *          submitted data.
   * @param defaultValue
   *          If parameter is not required, this will be the default value the parameter will take
   *          if not found in the submitted data.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String with the value of the parameter.
   * @throws ServletException
   */
  public String getStringParameter(String parameter, boolean required, String defaultValue,
      RequestFilter requestFilter) throws ServletException {
    String auxStr = null;
    try {
      if (isMultipart)
        auxStr = getMultiParameter(parameter);
      else
        auxStr = httpRequest.getParameter(parameter);
    } catch (Exception e) {
      if (!(required)) {
        auxStr = defaultValue;
      }
    }
    if (auxStr == null || auxStr.trim().equals("")) {
      if (required) {
        throw new ServletException("Request parameter required: " + parameter);
      } else {
        auxStr = defaultValue;
      }
    }

    auxStr = FormatUtilities.sanitizeInput(auxStr);
    filterRequest(requestFilter, auxStr);

    if (log4j.isDebugEnabled())
      log4j.debug("Request parameter: " + parameter + ":..." + auxStr);
    return auxStr;
  }

  /**
   * @see #getInParameter(String,String,RequestFilter)
   */
  @Deprecated
  public String getInParameter(String parameter, String defaultValue) throws ServletException {
    return getInParameter(parameter, defaultValue, null);
  }

  /**
   * Retrieves the set of values for the parameter with the specified name as passed to the servlet
   * by the HTTP POST method. String returned is in the form (value1,value2,...) and can be used
   * within SQL statements as part of the 'WHERE columnName IN' filter which is the main purpose of
   * this method. Note that the values are specified as they are, no quotation marks. If there is no
   * such parameter for this entry, the defaultValue is returned.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved.
   * @param defaultValue
   *          The value that will be returned in case the parameter is not found among the data
   *          submitted to the servlet.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of values in the form (value1,value2,...).
   * @throws ServletException
   */
  public String getInParameter(String parameter, String defaultValue, RequestFilter requestFilter)
      throws ServletException {
    return getInParameter(parameter, false, defaultValue, requestFilter);
  }

  /**
   * @see #getInParameter(String,RequestFilter)
   */
  @Deprecated
  public String getInParameter(String parameter) throws ServletException {
    return getInParameter(parameter, false, "", null);
  }

  /**
   * Retrieves the set of values for the parameter with the specified name as passed to the servlet
   * by the HTTP POST method. String returned is in the form (value1,value2,...) and can be used
   * within SQL statements as part of the 'WHERE columnName IN' filter which is the main purpose of
   * this method. Note that the values are specified as they are, no quotation marks. If there is no
   * such parameter for this entry, an empty string is returned.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of values in the form (value1,value2,...).
   * @throws ServletException
   */
  public String getInParameter(String parameter, RequestFilter requestFilter)
      throws ServletException {
    return getInParameter(parameter, false, "", requestFilter);
  }

  /**
   * @see #getRequiredInParameter(String,RequestFilter)
   */
  @Deprecated
  public String getRequiredInParameter(String parameter) throws ServletException {
    return getRequiredInParameter(parameter, null);
  }

  /**
   * Retrieves the set of values for the parameter with the specified name as passed to the servlet
   * by the HTTP POST method. String returned is in the form (value1,value2,...) and can be used
   * within SQL statements as part of the 'WHERE columnName IN' filter which is the main purpose of
   * this method. Note that the values are specified as they are, no quotation marks. If there is no
   * such parameter for this entry an exception is thrown.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of values in the form (value1,value2,...).
   * @throws ServletException
   */
  public String getRequiredInParameter(String parameter, RequestFilter requestFilter)
      throws ServletException {
    return getInParameter(parameter, true, "", requestFilter);
  }

  /**
   * @see #getInParameter(String,boolean,String,RequestFilter)
   */
  @Deprecated
  public String getInParameter(String parameter, boolean required, String defaultValue)
      throws ServletException {
    return getInParameter(parameter, required, defaultValue, null);
  }

  /**
   * Retrieves the set of values for the parameter with the specified name as passed to the servlet
   * by the HTTP POST method. String returned is in the form (value1,value2,...) and can be used
   * within SQL statements as part of the 'WHERE columnName IN' filter which is the main purpose of
   * this method. Note that the values are specified as they are, no quotation marks. If there is no
   * such parameter for this entry, then if it is required, an exception is thrown, or if not the
   * defaultValue is returned.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved.
   * @param required
   *          If true, an exception is thrown if the parameter is not among the data submitted to
   *          the servlet.
   * @param defaultValue
   *          If not required, this is the value that will be returned in case the parameter is not
   *          found among the data submitted to the servlet.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of values in the form (value1,value2,...).
   * @throws ServletException
   */
  public String getInParameter(String parameter, boolean required, String defaultValue,
      RequestFilter requestFilter) throws ServletException {
    String[] auxStr = null;
    StringBuffer strResultado = new StringBuffer();
    try {
      if (isMultipart)
        auxStr = getMultiParameters(parameter);
      else
        auxStr = httpRequest.getParameterValues(parameter);
    } catch (Exception e) {
      if (!(required)) {
        strResultado.append(defaultValue);
      }
    }

    if (auxStr == null || auxStr.length == 0 || auxStr.equals("")) {
      if (required) {
        throw new ServletException("Request IN parameter required: " + parameter);
      } else {
        strResultado.append(defaultValue);
      }
      return strResultado.toString();
    }

    auxStr = FormatUtilities.sanitizeInput(auxStr);
    filterRequest(requestFilter, auxStr);

    if (auxStr != null && auxStr.length > 0) {
      for (int i = 0; i < auxStr.length; i++) {
        if (auxStr[i].length() > 0) {
          if (strResultado.length() > 0)
            strResultado.append(",");
          strResultado.append(auxStr[i]);
        }
      }
    }

    if (strResultado.toString().equals("")) {
      if (required) {
        throw new ServletException("Request IN parameter required: " + parameter);
      } else {
        strResultado.append(defaultValue);
      }
      return strResultado.toString();
    }

    if (log4j.isDebugEnabled())
      log4j.debug("Request IN parameter: " + parameter + ":...(" + strResultado.toString() + ")");

    return "(" + strResultado.toString() + ")";
  }

  /**
   * @see #getInStringParameter(String,String,RequestFilter)
   */
  @Deprecated
  public String getInStringParameter(String parameter, String defaultValue) throws ServletException {
    return getInStringParameter(parameter, defaultValue, null);
  }

  /**
   * Retrieves the set of string values for the parameter with the specified name as passed to the
   * servlet by the HTTP POST method. String returned is in the form ('value1', 'value2',...) and
   * can be used within SQL statements as part of the 'WHERE columnName IN' filter which is the main
   * purpose of this method. Note that the values are specified within single quotation marks. If
   * there is no such parameter for this entry, the defaultValue is returned.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved.
   * @param defaultValue
   *          In case the parameter is not found among the data submitted to the servlet, this value
   *          is returned
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of values in the form ('value1', 'value2',...).
   * @throws ServletException
   */
  public String getInStringParameter(String parameter, String defaultValue,
      RequestFilter requestFilter) throws ServletException {
    return getInStringParameter(parameter, false, defaultValue, requestFilter);
  }

  /**
   * @see #getInStringParameter(String,RequestFilter)
   */
  @Deprecated
  public String getInStringParameter(String parameter) throws ServletException {
    return getInStringParameter(parameter, false, "", null);
  }

  /**
   * Retrieves the set of string values for the parameter with the specified name as passed to the
   * servlet by the HTTP POST method. String returned is in the form ('value1', 'value2',...) and
   * can be used within SQL statements as part of the 'WHERE columnName IN' filter which is the main
   * purpose of this method. Note that the values are specified within single quotation marks. If
   * there is no such parameter for this entry, an empty string is returned.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of values in the form ('value1', 'value2',...).
   * @throws ServletException
   */
  public String getInStringParameter(String parameter, RequestFilter requestFilter)
      throws ServletException {
    return getInStringParameter(parameter, false, "", requestFilter);
  }

  /**
   * @see #getRequiredInStringParameter(String,RequestFilter)
   */
  @Deprecated
  public String getRequiredInStringParameter(String parameter) throws ServletException {
    return getRequiredInStringParameter(parameter, null);
  }

  /**
   * Checks if a certain parameter is defined in the variables base.
   * 
   * @param parameter
   *          the parameter to check
   * @return true if a value is present, false otherwise
   */
  public boolean hasParameter(String parameter) {
    Object value;
    try {
      if (isMultipart) {
        value = getMultiParameters(parameter);
      } else {
        value = httpRequest.getParameterValues(parameter);
      }
    } catch (Exception e) {
      return false;
    }
    return value != null;
  }

  /**
   * @return the parameter names of the request object
   */
  @SuppressWarnings("unchecked")
  public Enumeration<String> getParameterNames() {
    return httpRequest.getParameterNames();
  }

  /**
   * Retrieves the set of string values for the parameter with the specified name as passed to the
   * servlet by the HTTP POST method. String returned is in the form ('value1', 'value2',...) and
   * can be used within SQL statements as part of the 'WHERE columnName IN' filter which is the main
   * purpose of this method. Note that the values are specified within single quotation marks. If
   * there is no such parameter for this entry, an exception is thrown.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of values in the form ('value1', 'value2',...).
   * @throws ServletException
   */
  public String getRequiredInStringParameter(String parameter, RequestFilter requestFilter)
      throws ServletException {
    return getInStringParameter(parameter, true, "", requestFilter);
  }

  /**
   * @see #getInStringParameter(String,boolean,String,RequestFilter)
   */
  @Deprecated
  public String getInStringParameter(String parameter, boolean required, String defaultValue)
      throws ServletException {
    return getInStringParameter(parameter, required, defaultValue, null);
  }

  /**
   * Retrieves the set of string values for the parameter with the specified name as passed to the
   * servlet by the HTTP POST method. String returned is in the form ('value1', 'value2',...) and
   * can be used within SQL statements as part of the 'WHERE columnName IN' filter which is the main
   * purpose of this method. Note that the values are specified within single quotation marks. If
   * there is no such parameter for this entry, then if it is required, an exception is thrown, or
   * if not the defaultValue is returned.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved
   * @param required
   *          If true, an exception is thrown if the parameter is not among the data submitted to
   *          the servlet.
   * @param defaultValue
   *          If not required, this is the value that will be returned in case the parameter is not
   *          found among the data submitted to the servlet.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the set of values in the form ('value1', 'value2',...)
   * @throws ServletException
   */
  public String getInStringParameter(String parameter, boolean required, String defaultValue,
      RequestFilter requestFilter) throws ServletException {
    String[] auxStr = null;
    StringBuffer strResult = new StringBuffer();
    try {
      if (isMultipart)
        auxStr = getMultiParameters(parameter);
      else
        auxStr = httpRequest.getParameterValues(parameter);
    } catch (Exception e) {
      if (!(required)) {
        strResult.append(defaultValue);
      }
    }

    if (auxStr == null || auxStr.length == 0) {
      if (required) {
        throw new ServletException("Request IN parameter required: " + parameter);
      } else {
        strResult.append(defaultValue);
      }
      return strResult.toString();
    }

    auxStr = FormatUtilities.sanitizeInput(auxStr);
    filterRequest(requestFilter, auxStr);

    strResult.append("('");
    for (int i = 0; i < auxStr.length; i++) {
      if (i > 0) {
        strResult.append("', '");
      }
      strResult.append(auxStr[i]);
    }
    strResult.append("')");

    if (log4j.isDebugEnabled())
      log4j.debug("Request IN parameter: " + parameter + ":..." + strResult.toString());

    return strResult.toString();
  }

  /**
   * Retrieves the set of string values for the parameter with the specified name as passed to the
   * servlet by the HTTP POST/GET method. The parameter must be a multi-valued parameter. If the
   * parameter is not set then a String[0] is returned.
   * 
   * @param parameter
   *          Name of the parameter to be retrieved
   * @param required
   *          If true, an exception is thrown if the parameter is not among the data submitted to
   *          the servlet.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return returns a String array with the values present in the request, if the parameter has no
   *         value then String[0] is returned.
   * @throws ServletException
   */
  public String[] getMultiValueStringParameter(String parameter, boolean required,
      RequestFilter requestFilter) throws ServletException {
    String[] auxStr = null;
    try {
      if (isMultipart)
        auxStr = getMultiParameters(parameter);
      else
        auxStr = httpRequest.getParameterValues(parameter);
    } catch (Exception e) {
      if (!(required)) {
        return new String[0];
      }
    }

    if (auxStr == null || auxStr.length == 0) {
      if (required) {
        throw new ServletException("Request IN parameter required: " + parameter);
      } else {
        return new String[0];
      }
    }

    auxStr = FormatUtilities.sanitizeInput(auxStr);
    filterRequest(requestFilter, auxStr);

    if (log4j.isDebugEnabled())
      log4j.debug("Request IN parameter: " + parameter + ":..." + auxStr.toString());

    return auxStr;
  }

  /**
   * Retrieve a value specified by the sessionAttribute parameter from the session variables. If not
   * found, an empty string is returned.
   * 
   * @param sessionAttribute
   *          The name of the session variable to be retrieved.
   * @return String with the value of the session variable.
   */
  public String getSessionValue(String sessionAttribute) {
    return getSessionValue(sessionAttribute, "");
  }

  /**
   * Retrieve a value specified by the sessionAttribute parameter from the session variables. If not
   * found, the defaultValue is returned.
   * 
   * @param sessionAttribute
   *          The name of the session variable to be retrieved.
   * @param defaultValue
   *          The value to be returned in case the session variable does not exist.
   * @return String with the value of the session variable.
   */
  public String getSessionValue(String sessionAttribute, String defaultValue) {
    String auxStr = null;
    try {
      auxStr = (String) session.getAttribute(sessionAttribute.toUpperCase());
      if (auxStr == null || auxStr.trim().equals(""))
        auxStr = defaultValue;
    } catch (Exception e) {
      auxStr = defaultValue;
    }
    if (!sessionAttribute.equalsIgnoreCase("menuVertical"))
      if (log4j.isDebugEnabled())
        log4j.debug("Get session attribute: " + sessionAttribute + ":..." + auxStr);
    return auxStr;
  }

  /**
   * Store a variable and its value into the session as specified by the parameters.
   * 
   * @param attribute
   *          The name of the session variable to be set.
   * @param value
   *          The value of the session variable to set.
   */
  public void setSessionValue(String attribute, String value) {
    try {
      session.setAttribute(attribute.toUpperCase(), value);
      if (!attribute.equalsIgnoreCase("menuVertical"))
        if (log4j.isDebugEnabled())
          log4j.debug("Set session attribute: " + attribute + ":..." + value.toString());
    } catch (Exception e) {
      log4j.error("setSessionValue error: " + attribute + ":..." + value);
    }
  }

  /**
   * Remove a variable and its value from the session.
   * 
   * @param attribute
   *          The name of the session variable to remove
   */
  public void removeSessionValue(String attribute) {
    try {
      if (log4j.isDebugEnabled())
        log4j.debug("Remove session attribute: " + attribute + ":..." + getSessionValue(attribute));
      session.removeAttribute(attribute.toUpperCase());

    } catch (Exception e) {
      log4j.error("removeSessionValue error: " + attribute);
    }
  }

  /**
   * Retrieve an object from the session.
   * 
   * @param sessionAttribute
   *          The name of the object to retrieve.
   * @return Object containing the requested object.
   */
  public Object getSessionObject(String sessionAttribute) {
    Object auxStr = null;
    try {
      auxStr = (Object) session.getAttribute(sessionAttribute.toUpperCase());
    } catch (Exception e) {
      auxStr = null;
    }
    return auxStr;
  }

  /**
   * Save an object and its value to the session.
   * 
   * @param attribute
   *          The name of the object to be stored.
   * @param value
   *          The value of the object to store.
   */
  public void setSessionObject(String attribute, Object value) {
    try {
      session.setAttribute(attribute.toUpperCase(), value);
    } catch (Exception e) {
      log4j.error("setSessionObject error: " + attribute + ":..." + e);
    }
  }

  /**
   * Clear session variables.
   * 
   * @param all
   *          If false, one session variable named TARGET is kept otherwise all session variables
   *          are erased.
   */
  public void clearSession(boolean all) {
    if (log4j.isDebugEnabled())
      log4j.debug("...: removing session");
    String target = "";
    String targetQueryString = null;
    try {
      String sessionName;
      Enumeration<?> e = session.getAttributeNames();
      while (e.hasMoreElements()) {
        sessionName = (String) e.nextElement();
        if (log4j.isDebugEnabled())
          log4j.debug("  session name: " + sessionName);
        if (!all && sessionName.equalsIgnoreCase("target")) {
          target = (String) session.getAttribute(sessionName);
        }
        if (!all && sessionName.equalsIgnoreCase("targetQueryString")) {
          targetQueryString = (String) session.getAttribute(sessionName);
        }
        session.removeAttribute(sessionName);
        e = session.getAttributeNames();
      }
    } catch (Exception e) {
      log4j.error("clearSession error " + e);
    }
    if (!target.equals("")) {
      session.setAttribute("TARGET", target);
    }
    if (targetQueryString != null) {
      session.setAttribute("TARGETQUERYSTRING", targetQueryString);
    }
  }

  /**
   * Convert a list specified as a comma separated list within parentheses into a Vector of
   * individual values. Strips parentheses and single quotes. For example, a string like ('1000022',
   * '1000344') becomes a Vector with two values: 1000022 and 1000344.
   * 
   * @param strList
   *          String representing a comma separated list of values.
   * @return A Vector of individual values.
   */
  public Vector<String> getListFromInString(String strList) {
    Vector<String> fields = new Vector<String>();
    if (strList == null || strList.length() == 0)
      return fields;
    strList = strList.trim();
    if (strList.equals(""))
      return fields;
    if (strList.startsWith("("))
      strList = strList.substring(1, strList.length() - 1);
    strList = strList.trim();
    if (strList.equals(""))
      return fields;
    StringTokenizer datos = new StringTokenizer(strList, ",", false);
    while (datos.hasMoreTokens()) {
      String token = datos.nextToken();
      if (token.startsWith("'"))
        token = token.substring(1, token.length() - 1);
      token = token.trim();
      if (!token.equals(""))
        fields.addElement(token);
    }
    return fields;
  }

  /**
   * @see #getMultiParameter(String,RequestFilter)
   */
  public String getMultiParameter(String parameter) {
    return getMultiParameter(parameter, null);
  }

  /**
   * Retrieve a parameter passed to the servlet as part of a multi part content.
   * 
   * @param parameter
   *          the name of the parameter to be retrieved
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String containing the value of the parameter. Empty string if the content is not
   *         multipart or the parameter is not found.
   */
  public String getMultiParameter(String parameter, RequestFilter requestFilter) {
    if (!isMultipart || items == null)
      return "";
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && item.getFieldName().equals(parameter)) {
        try {
          String value = item.getString("UTF-8");
          filterRequest(requestFilter, value);
          return value;
        } catch (Exception ex) {
          ex.printStackTrace();
          return "";
        }
      }
    }
    return "";
  }

  /**
   * @see #getMultiParameters(String,RequestFilter)
   */
  public String[] getMultiParameters(String parameter) {
    return getMultiParameters(parameter, null);
  }

  /**
   * Retrieve a set of values belonging to a parameter passed to the servlet as part of a multi part
   * content.
   * 
   * @param parameter
   *          The name of the parameter to be retrieved.
   * @param requestFilter
   *          filter used to validate the input against list of allowed inputs
   * @return String array containing the values of the parameter. Empty string if the content is not
   *         multipart.
   */
  public String[] getMultiParameters(String parameter, RequestFilter requestFilter) {
    if (!isMultipart || items == null)
      return null;
    Iterator<FileItem> iter = items.iterator();
    Vector<String> result = new Vector<String>();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && item.getFieldName().equals(parameter)) {
        try {
          String value = item.getString("UTF-8");
          filterRequest(requestFilter, value);
          result.addElement(value);
        } catch (Exception ex) {
        }
      }
    }
    String[] strResult = new String[result.size()];
    result.copyInto(strResult);
    return strResult;
  }

  /**
   * Retrieve a deserialized file passed to the servlet as a parameter and part of a multi part
   * content.
   * 
   * @param parameter
   *          The name of the parameter that contains the file
   * @return FileItem object containing the file content
   */
  public FileItem getMultiFile(String parameter) {
    if (!isMultipart || items == null)
      return null;
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (!item.isFormField() && item.getFieldName().equals(parameter))
        return item;
    }
    return null;
  }
}
