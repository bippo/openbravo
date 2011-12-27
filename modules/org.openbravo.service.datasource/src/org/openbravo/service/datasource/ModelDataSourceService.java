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
package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

/**
 * A data source which provides the data for a field which refers to properties in the data model.
 * 
 * @author mtaal
 * @author iperdomo
 */
public class ModelDataSourceService extends BaseDataSourceService {

  private static final String PROPERTY_FIELD = "inpproperty";
  private static final String DATASOURCE_FIELD = "modelProperty";
  private static final String FORM_FIELD = "inpadTableId";

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(ModelDataSourceService.class);
  private static final Property identifier = new Property();

  static {
    // Setting identifier property name
    identifier.setName(JsonConstants.IDENTIFIER);
  }

  @Override
  public String add(Map<String, String> parameters, String content) {
    throw new UnsupportedOperationException(
        "This operation is not supported by this data source implementation");
  }

  @Override
  public String fetch(Map<String, String> parameters) {

    final Entity baseEntity = getBaseEntity(parameters);
    String propertyPath = parameters.get(PROPERTY_FIELD);
    if (propertyPath == null) {
      HashMap<String, String> criteria = getCriteria(parameters);
      if (criteria != null && criteria.containsKey(DATASOURCE_FIELD)) {
        propertyPath = criteria.get(DATASOURCE_FIELD);
      } else {
        propertyPath = "";
      }
    }

    if (baseEntity == null) {
      // The first request doesn't contain the adTableId
      // that's why baseEntity is null
      final List<Property> baseEntityProperties = new ArrayList<Property>();
      if (propertyPath != null) {
        final Property savedPath = new Property();
        savedPath.setName(propertyPath);
        baseEntityProperties.add(savedPath);
      }
      try {
        return getJSONResponse(baseEntityProperties, "", 0);
      } catch (JSONException e) {
        log.error("Error building JSON response: " + e.getMessage(), e);
        return JsonUtils.getEmptyResult();
      }
    }

    Property foundProperty = null;
    int startRow = 0;

    if (propertyPath == null || propertyPath.equals("")) {
      try {
        final List<Property> baseEntityProperties = getEntityProperties(baseEntity);

        return getJSONResponse(baseEntityProperties, "", 0);

      } catch (JSONException e) {
        log.error("Error building JSON response: " + e.getMessage(), e);
        return JsonUtils.getEmptyResult();
      }
    }

    if (propertyPath.lastIndexOf("..") != -1) {
      return JsonUtils.getEmptyResult();
    }

    final boolean endsWithDot = propertyPath.endsWith(".");

    try {
      final String[] parts = propertyPath.split("\\.");
      Entity currentEntity = baseEntity;
      Property currentProperty = null;
      List<Property> props = new ArrayList<Property>();
      int currentDepth = 0;
      int pathDepth = parts.length;

      boolean getAllProperties = (propertyPath.lastIndexOf('.') == propertyPath.length() - 1);
      int index = 0;
      for (String part : parts) {

        final boolean lastPart = index == (parts.length - 1);
        currentDepth++;

        boolean propNotFound = true;

        final List<Property> currentEntityProperties = getEntityProperties(currentEntity);

        for (Property prop : currentEntityProperties) {
          boolean tryProperty = false;
          if (lastPart && endsWithDot) {
            tryProperty = prop.getName().equalsIgnoreCase(part.toLowerCase());
          } else {
            tryProperty = prop.getName().toLowerCase().startsWith(part.toLowerCase());
          }
          if (tryProperty) {
            if (prop.getName().equals(JsonConstants.IDENTIFIER)) {
              currentProperty = identifier;
            } else {
              currentProperty = currentEntity.getProperty(prop.getName());
            }
            foundProperty = prop;
            propNotFound = false;
            if (currentDepth != pathDepth) {
              // Breaking loop to continue with next property in the path
              break;
            }
            props.add(prop);
          }
        }

        if (propNotFound) {
          return JsonUtils.getEmptyResult();
        }

        foundProperty = currentProperty;
        currentEntity = foundProperty.getTargetEntity();

        if (currentDepth == pathDepth && getAllProperties && currentEntity != null) {
          // User just pressed a final dot (.) key - getting all properties
          // of current Entity
          return getJSONResponse(getEntityProperties(currentEntity), propertyPath, 0);
        }
        index++;
      }

      if (getAllProperties && props.size() > 0) {
        if (props.get(0).getTargetEntity() == null) {
          return JsonUtils.getEmptyResult();
        }
      }

      return getJSONResponse(props, propertyPath, startRow);

    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String remove(Map<String, String> parameters) {
    throw new UnsupportedOperationException(
        "This operation is not supported by this data source implementation");
  }

  @Override
  public String update(Map<String, String> parameters, String content) {
    throw new UnsupportedOperationException(
        "This operation is not supported by this data source implementation");
  }

  /**
   * Returns an entity based on the table record id. Returns null if the ipadTableId input is not
   * present or no Entity is found for a given table id.
   * 
   * @param parameters
   *          Map of the parameters from the request
   * @return the Entity or null if not found
   */
  protected Entity getBaseEntity(Map<String, String> parameters) {
    final String tableId = parameters.get(FORM_FIELD);
    if (tableId == null) {
      return null;
    }
    final Entity entity = ModelProvider.getInstance().getEntityByTableId(tableId);
    return entity;
  }

  /**
   * Returns the list of properties sorted alphabetically and with a extra _identifier property
   * 
   * @param entity
   *          the parent Entity from which the Property will be extracted
   * @return a list of properties plus an extra _identifier property
   */
  protected List<Property> getEntityProperties(Entity entity) {

    final List<Property> entityProperties = new ArrayList<Property>();
    // Appending identifier property
    entityProperties.add(identifier);
    entityProperties.addAll(entity.getProperties());

    Collections.sort(entityProperties, new PropertyNameComparator());

    return entityProperties;
  }

  /**
   * Returns a JSON string representation of the properties matched based n user input
   * 
   * @param props
   *          the list of properties to be transformed into JSON representation
   * @param propertyPath
   *          the user's request input string
   * @param startRow
   *          the start index. Used for paging
   * @return a JSON string response for the client
   * @throws JSONException
   */
  private String getJSONResponse(List<Property> props, String propertyPath, int startRow)
      throws JSONException {
    final JSONObject jsonResult = new JSONObject();
    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
    jsonResponse.put(JsonConstants.RESPONSE_ENDROW, props.size() + startRow - 1);
    jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, props.size());
    jsonResponse.put(JsonConstants.RESPONSE_DATA,
        new JSONArray(convertToJSONObjects(props, propertyPath)));
    jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

    return jsonResult.toString();
  }

  /**
   * Converts a List of {@link org.openbravo.base.model.Property properties} into JSON objects
   * 
   * @param properties
   *          the list of properties
   * @param propertyPath
   *          the user request input
   * @return a list of JSONObjects
   */
  private List<JSONObject> convertToJSONObjects(List<Property> properties, String propertyPath) {
    final List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
    final int pos = propertyPath.lastIndexOf('.');
    String propertyPrefix = "";

    if (pos != -1) {
      propertyPrefix = propertyPath.substring(0, pos + 1);
    }

    for (Property prop : properties) {
      jsonObjects.add(convertToJSONObject(prop, propertyPrefix));
    }

    return jsonObjects;
  }

  /**
   * Converts a Property into its JSONObject representation.<br>
   * Note: The JSONObject representation takes only the property name
   * 
   * @param property
   *          the {@link org.openbravo.base.model.Property Property} to convert
   * @param propertyPrefix
   *          the prefix that will be appended to the property name
   * @return a JSONObject representation of the Property
   */
  private JSONObject convertToJSONObject(Property property, String propertyPrefix) {
    final JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put(DATASOURCE_FIELD, propertyPrefix + property.getName());
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
    return jsonObject;
  }

  private HashMap<String, String> getCriteria(Map<String, String> parameters) {
    if (!"AdvancedCriteria".equals(parameters.get("_constructor"))) {
      return null;
    }
    HashMap<String, String> criteriaValues = new HashMap<String, String>();
    try {
      JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
      for (int i = 0; i < criterias.length(); i++) {
        final JSONObject criteria = criterias.getJSONObject(i);
        criteriaValues.put(criteria.getString("fieldName"), criteria.getString("value"));
      }
    } catch (JSONException e) {
      // Ignore exception.
    }
    if (criteriaValues.isEmpty()) {
      return null;
    }
    return criteriaValues;
  }

  /**
   * Comparator implementation based on Property names
   * 
   * @author iperdomo
   */
  private static class PropertyNameComparator implements Comparator<Property> {

    /**
     * Compares 2 {@link org.openbravo.base.model.Property properties} based on the name
     */
    @Override
    public int compare(Property o1, Property o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}
