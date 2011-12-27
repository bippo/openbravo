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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.domaintype.BigDecimalDomainType;
import org.openbravo.base.model.domaintype.BooleanDomainType;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.LongDomainType;
import org.openbravo.base.model.domaintype.StringDomainType;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;

/**
 * Utility class for Parameters handling
 * 
 * @author iperdomo
 */
public class ParameterUtils {
  private static Logger log = Logger.getLogger(ParameterUtils.class);

  public static void setParameterValue(ParameterValue parameterValue, JSONObject requestValue) {
    try {
      setValue(parameterValue, requestValue.getString("value"));
    } catch (Exception e) {
      log.error("Error trying to set value for paramter: "
          + parameterValue.getParameter().getName(), e);
    }
  }

  public static void setDefaultParameterValue(ParameterValue value) {
    Check.isNotNull(value, "Default value is based on Parameter defintion");
    setValue(value, value.getParameter().getDefaultValue());
  }

  private static void setValue(ParameterValue parameterValue, String stringValue) {
    DomainType domainType = getParameterDomainType(parameterValue.getParameter());
    final SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    try {
      if (domainType.getClass().equals(StringDomainType.class)) {
        parameterValue.setValueString(stringValue);
      } else if (domainType.getClass().equals(DateDomainType.class)) {
        Date date = xmlDateFormat.parse(stringValue);
        parameterValue.setValueDate(date);
      } else if (domainType.getClass().getSuperclass().equals(BigDecimalDomainType.class)
          || domainType.getClass().equals(LongDomainType.class)) {
        parameterValue.setValueNumber(new BigDecimal(stringValue));
      } else { // default
        parameterValue.setValueString(stringValue);
      }
    } catch (Exception e) {
      log.error("Error trying to set value for paramter: "
          + parameterValue.getParameter().getName(), e);
    }
  }

  private static DomainType getParameterDomainType(Parameter parameter) {
    return ModelProvider.getInstance().getReference(parameter.getReference().getId())
        .getDomainType();
  }

  /**
   * Returns an Object with the Value of the Parameter Value. This object can be a String, a
   * java.util.Data or a BigDecimal.
   * 
   * @param parameterValue
   *          the Parameter Value we want to get the Value from.
   * @return the Value of the Parameter Value.
   */
  public static Object getParameterValue(ParameterValue parameterValue) {
    DomainType domainType = getParameterDomainType(parameterValue.getParameter());
    if (domainType.getClass().equals(StringDomainType.class)) {
      return parameterValue.getValueString();
    } else if (domainType.getClass().equals(DateDomainType.class)) {
      return parameterValue.getValueDate();
    } else if (domainType.getClass().getSuperclass().equals(BigDecimalDomainType.class)
        || domainType.getClass().equals(LongDomainType.class)) {
      return parameterValue.getValueNumber();
    } else if (domainType.getClass().equals(BooleanDomainType.class)) {
      return "true".equals(parameterValue.getValueString());
    } else { // default
      return parameterValue.getValueString();
    }
  }

  /**
   * Returns the Fixed value of the given parameter. If the value is a JS expression it returns the
   * result of the expression based on the parameters passed in from the request.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @param parameter
   *          the parameter we want to get the Fixed Value from
   * @return the Fixed Value of the parameter
   */
  public static Object getParameterFixedValue(Map<String, String> parameters, Parameter parameter) {
    if (parameter.isEvaluateFixedValue()) {
      try {
        return getJSExpressionResult(parameters, null, parameter.getFixedValue());
      } catch (Exception e) {
        // log.error(e.getMessage(), e);
        return null;
      }
    } else {
      return parameter.getFixedValue();
    }
  }

  /**
   * Returns the result of evaluating the given JavaScript expression.
   * 
   * @param parameters
   *          Map of Strings with the request map parameters.
   * @param session
   *          optional HttpSession object.
   * @param expression
   *          String with the JavaScript expression to be evaluated.
   * @return an Object with the result of the expression evaluation.
   * @throws ScriptException
   */
  public static Object getJSExpressionResult(Map<String, String> parameters, HttpSession session,
      String expression) throws ScriptException {
    final ScriptEngineManager manager = new ScriptEngineManager();
    final ScriptEngine engine = manager.getEngineByName("js");

    if (session != null) {
      engine.put("OB", new OBBindings(OBContext.getOBContext(), parameters, session));
    } else {
      engine.put("OB", new OBBindings(OBContext.getOBContext(), parameters));
    }

    return engine.eval(expression);
  }
}
