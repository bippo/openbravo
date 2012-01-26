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

import java.sql.BatchUpdateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Contains utility methods used in this module.
 * 
 * @author mtaal
 */
public class JsonUtils {
  private static final Logger log = Logger.getLogger(JsonUtils.class);

  /**
   * @return a new instance of the {@link SimpleDateFormat} using a format of yyyy-MM-dd (xml schema
   *         date format). The date format has lenient set to true.
   */
  public static SimpleDateFormat createDateFormat() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setLenient(true);
    return dateFormat;
  }

  /**
   * @return a new instance of the {@link SimpleDateFormat} using a format of HH:MM:SS+0000. The
   *         date format has lenient set to true.
   */
  public static SimpleDateFormat createTimeFormat() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ssZZZZZ");
    dateFormat.setLenient(true);
    return dateFormat;
  }

  /**
   * @return a new instance of the {@link SimpleDateFormat} using a format of yyyy-MM-dd'T'HH:mm:ss
   *         (see http://www.w3.org/TR/xmlschema-2/#dateTime). The date format has lenient set to
   *         true.
   */
  public static SimpleDateFormat createJSTimeFormat() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setLenient(true);
    return dateFormat;
  }

  /**
   * Note the formatted date string must be repaired in the timezone to follow the XSD format, see:
   * {@link #convertToCorrectXSDFormat(String)}.
   * 
   * @return a new instance of the {@link SimpleDateFormat} using a format of yyyy-MM-dd'T'HH:mm:ss
   *         (xml schema date time format). The date format has lenient set to true.
   */
  public static SimpleDateFormat createDateTimeFormat() {
    // Note users of this method will also use the convertToCorrectXSDFormat
    // method
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    dateFormat.setLenient(true);
    return dateFormat;
  }

  /**
   * Adds a colon in the timezone part of the formatted date, see here:
   * http://weblogs.java.net/blog/felipegaucho/archive/2009/12/06/jaxb-customization-xsddatetime
   * 
   * @param dateValue
   *          the date value without a :, for example 2009-12-06T15:59:34+0100
   * @return a colon added in the timezone part: 2009-12-06T15:59:34+01:00
   */
  public static String convertToCorrectXSDFormat(String dateValue) {
    if (dateValue == null || dateValue.length() < 3) {
      return dateValue;
    }
    final int length = dateValue.length();
    return dateValue.substring(0, length - 2) + ":" + dateValue.substring(length - 2);
  }

  /**
   * Removes the colon in the timezone definition, see {@link #convertToCorrectXSDFormat(String)}.
   */
  public static String convertFromXSDToJavaFormat(String dateValue) {
    if (dateValue == null || dateValue.length() < 3) {
      return dateValue;
    }
    final int length = dateValue.length();
    // must end with +??:?? or -??:??
    if (dateValue.charAt(length - 3) == ':'
        && (dateValue.charAt(length - 6) == '-' || dateValue.charAt(length - 6) == '+')) {
      final String result = dateValue.substring(0, length - 3) + dateValue.substring(length - 2);
      return result;
    }
    // make them utc, the timezone must be there
    return dateValue + "+0000";
  }

  /**
   * Gets the value of the {@link JsonConstants#ADDITIONAL_PROPERTIES_PARAMETER} in the parameters
   * map and returns it as a list of String, if no parameter is set an empty list is returned.
   * 
   * @param parameters
   *          the parameter map to search for the
   *          {@link JsonConstants#ADDITIONAL_PROPERTIES_PARAMETER} parameter
   * @return the values in the {@link JsonConstants#ADDITIONAL_PROPERTIES_PARAMETER} parameter
   */
  public static List<String> getAdditionalProperties(Map<String, String> parameters) {
    final String additionalPropertiesString = parameters
        .get(JsonConstants.ADDITIONAL_PROPERTIES_PARAMETER);
    if (additionalPropertiesString == null) {
      return Collections.emptyList();
    }
    final List<String> result = new ArrayList<String>();
    for (String additionalProperty : additionalPropertiesString.split(",")) {
      result.add(additionalProperty.trim());
    }
    return result;
  }

  /**
   * Converts an exception to its json represention. Uses the Smartclient format for the json
   * string, see here: <a
   * href="http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#class..RestDataSource">
   * RestDataSource</a>
   * 
   * @param throwable
   *          the exception to convert to json
   * @return the resulting json string
   */
  public static String convertExceptionToJson(Throwable throwable) {
    Throwable localThrowable = throwable;
    if (throwable.getCause() instanceof BatchUpdateException) {
      final BatchUpdateException batchException = (BatchUpdateException) throwable.getCause();
      localThrowable = batchException.getNextException();
    }

    try {
      final JSONObject jsonResult = new JSONObject();
      final JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);

      try {
        // get rid of the current transaction
        OBDal.getInstance().rollbackAndClose();
      } catch (Exception e) {
        // ignored on purpose
        log.error(e.getMessage(), e);
      }

      final VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      final OBError obError = Utility.translateError(new DalConnectionProvider(), vars, OBContext
          .getOBContext().getLanguage().getLanguage(), localThrowable.getMessage());

      if (localThrowable instanceof OBSecurityException) {
        final JSONObject error = new JSONObject();
        error.put("message", "OBUIAPP_ActionNotAllowed");
        error.put("type", "user");
        jsonResponse.put(JsonConstants.RESPONSE_ERROR, error);
      } else if (obError != null) {
        final JSONObject error = new JSONObject();
        error.put("message", obError.getMessage());
        error.put("messageType", obError.getType());
        error.put("title", obError.getTitle());
        jsonResponse.put(JsonConstants.RESPONSE_ERROR, error);
      } else {
        jsonResponse.put(JsonConstants.RESPONSE_DATA, localThrowable.getMessage());
      }

      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
      return jsonResult.toString();

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns an empty result for a fetch call
   * 
   * @return the JSON representation of an empty result
   */
  public static String getEmptyResult() {
    final JSONObject jsonResult = new JSONObject();
    final JSONObject jsonResponse = new JSONObject();

    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, "0");
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, "0");
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, "0");
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray());
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return jsonResult.toString();
  }

  /**
   * Determines the list of properties based on the property path (for example
   * bankAccount.bank.name).
   * 
   * @param entity
   *          the entity to start from, the first property in the property path is a property of
   *          this entity
   * @param propertyPath
   *          the property path, i.e. property names separated by dots
   * @return the list of properties determined from the property path
   */
  public static List<Property> getPropertiesOnPath(Entity entity, String propertyPath) {
    final String[] parts = propertyPath.split("\\.");
    Entity currentEntity = entity;
    Property result = null;
    final List<Property> properties = new ArrayList<Property>();

    for (String part : parts) {
      // only consider it as an identifier if it is called an identifier and
      // the entity does not accidentally have an identifier property
      // && !currentEntity.hasProperty(part)
      // NOTE disabled for now, there is one special case: AD_Column.IDENTIFIER
      // which is NOT HANDLED
      if (part.equals(JsonConstants.IDENTIFIER)) {
        // pick the first identifier property
        if (currentEntity.getIdentifierProperties().isEmpty()) {
          properties.add(currentEntity.getIdProperties().get(0));
        } else {
          properties.add(currentEntity.getIdentifierProperties().get(0));
        }
        return properties;
      }
      if (!currentEntity.hasProperty(part)) {
        // explicitly not logging as the new ui will also post and send other
        // properties
        // Log.warn("Property " + part + " not found in entity " + currentEntity);
        return properties;
      }
      result = currentEntity.getProperty(part);
      properties.add(result);
      if (result.getTargetEntity() != null) {
        currentEntity = result.getTargetEntity();
      }
    }
    return properties;
  }

  public static JSONObject buildCriteria(Map<String, String> parameters) {
    try {
      final JSONObject criteria = new JSONObject();

      if (parameters.get(JsonConstants.OR_EXPRESSION_PARAMETER) != null) {
        criteria.put("operator", "or");
      } else {
        criteria.put("operator", "and");
      }
      criteria.put("_constructor", "AdvancedCriteria");

      final List<JSONObject> criteriaObjects = new ArrayList<JSONObject>();
      if (parameters.containsKey("criteria") && !parameters.get("criteria").equals("")) {
        String fullCriteriaStr = parameters.get("criteria");
        if (fullCriteriaStr.startsWith("[")) {
          JSONArray criteriaArray = new JSONArray(fullCriteriaStr);
          fullCriteriaStr = "";
          for (int i = 0; i < criteriaArray.length(); i++) {
            if (i > 0) {
              fullCriteriaStr += JsonConstants.IN_PARAMETER_SEPARATOR;
            }
            fullCriteriaStr += criteriaArray.getJSONObject(i).toString();
          }
        }
        final String[] criteriaStrs = fullCriteriaStr.split(JsonConstants.IN_PARAMETER_SEPARATOR);
        if (!fullCriteriaStr.equals("")) {
          for (String criteriaStr : criteriaStrs) {
            final JSONObject criteriaJSONObject = new JSONObject(criteriaStr);
            if (criteriaJSONObject.has("fieldName")) {
              final String fieldName = criteriaJSONObject.getString("fieldName");
              if (!fieldName.startsWith("_")) {
                criteriaObjects.add(criteriaJSONObject);
              }
            } else {
              criteriaObjects.add(criteriaJSONObject);
            }
          }
        }
      }
      criteria.put("criteria", new JSONArray(criteriaObjects));
      return criteria;
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

}
