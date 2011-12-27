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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ObjectNotFoundException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.BinaryDomainType;
import org.openbravo.base.model.domaintype.EncryptedStringDomainType;
import org.openbravo.base.model.domaintype.HashedStringDomainType;
import org.openbravo.base.model.domaintype.TimestampDomainType;
import org.openbravo.base.structure.ActiveEnabled;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;

/**
 * Is responsible for converting Openbravo business objects ({@link BaseOBObject} to a json
 * representation. This converter supports both converting single BaseOBObject instances and a
 * collection of business objects.
 * 
 * Values are converted as follows:
 * <ul>
 * <li>Reference values are converted as a JSONObject with only the id and identifier set.</li>
 * <li>Primitive date values are converted to a representation following the xml formatting.</li>
 * <li>Other primitive values are converted by the JSONObject itself.</li>
 * </ul>
 * 
 * @author mtaal
 */
public class DataToJsonConverter {

  public static final String REF_SEPARATOR = "/";

  // TODO: need to be revisited when client side data formatting is solved
  private final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
  private final SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();
  private final static SimpleDateFormat xmlTimeFormat = JsonUtils.createTimeFormat();

  // additional properties to return as a flat list
  private List<String> additionalProperties = new ArrayList<String>();

  /**
   * Convert a list of Maps with key value pairs to a list of {@link JSONObject}.
   * 
   * @param data
   *          the list of Maps
   * @return the corresponding list of JSONObjects
   */
  public List<JSONObject> convertToJsonObjects(List<Map<String, Object>> data) {
    try {
      final List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
      for (Map<String, Object> dataInstance : data) {
        final JSONObject jsonObject = new JSONObject();
        for (String key : dataInstance.keySet()) {
          final Object value = dataInstance.get(key);
          if (value instanceof BaseOBObject) {
            addBaseOBObject(jsonObject, null, key, null, (BaseOBObject) value);
          } else {
            // TODO: format!
            jsonObject.put(key, convertPrimitiveValue(value));
          }
        }
        jsonObjects.add(jsonObject);
      }
      return jsonObjects;
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Convert a list of {@link BaseOBObject} to a list of {@link JSONObject}.
   * 
   * @param bobs
   *          the list of BaseOBObjects to convert
   * @return the corresponding list of JSONObjects
   */
  public List<JSONObject> toJsonObjects(List<BaseOBObject> bobs) {
    final List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
    for (BaseOBObject bob : bobs) {
      jsonObjects.add(toJsonObject((BaseOBObject) bob, DataResolvingMode.FULL));
    }
    return jsonObjects;
  }

  /**
   * Convert a single {@link BaseOBObject} into a {@link JSONObject}.
   * 
   * @param bob
   *          the BaseOBObject to convert
   * @param dataResolvingMode
   *          the data resolving mode determines how much information is converted (only the
   *          identifying info or everything).
   * @return the converted object
   */
  public JSONObject toJsonObject(BaseOBObject bob, DataResolvingMode dataResolvingMode) {
    try {
      final JSONObject jsonObject = new JSONObject();
      jsonObject.put(JsonConstants.IDENTIFIER, bob.getIdentifier());
      jsonObject.put(JsonConstants.ENTITYNAME, bob.getEntityName());
      jsonObject.put(JsonConstants.REF, encodeReference(bob));
      if (dataResolvingMode == DataResolvingMode.SHORT) {
        jsonObject.put(JsonConstants.ID, bob.getId());
        if (bob instanceof ActiveEnabled) {
          jsonObject.put(JsonConstants.ACTIVE, ((ActiveEnabled) bob).isActive());
        }
        return jsonObject;
      }
      final boolean isDerivedReadable = OBContext.getOBContext().getEntityAccessChecker()
          .isDerivedReadable(bob.getEntity());

      for (Property property : bob.getEntity().getProperties()) {
        if (property.isOneToMany()) {
          // ignore these for now....
          continue;
        }
        // do not convert if the object is derived readable and the property is not
        if (isDerivedReadable && !property.allowDerivedRead()) {
          continue;
        }
        final Object value = bob.get(property.getName());
        if (value != null) {
          if (property.isPrimitive()) {
            // TODO: format!
            jsonObject.put(property.getName(), convertPrimitiveValue(property, value));
          } else {
            addBaseOBObject(jsonObject, property, property.getName(),
                property.getReferencedProperty(), (BaseOBObject) value);
          }
        } else {
          jsonObject.put(property.getName(), JSONObject.NULL);
        }
      }
      for (String additionalProperty : additionalProperties) {
        // sometimes empty strings are passed in
        if (additionalProperty.length() == 0) {
          continue;
        }
        final Object value = DalUtil.getValueFromPath(bob, additionalProperty);
        if (value instanceof BaseOBObject) {
          final Property additonalPropertyObject = getPropertyFromPath(bob, additionalProperty);
          addBaseOBObject(jsonObject, additonalPropertyObject, additionalProperty,
              additonalPropertyObject.getReferencedProperty(), (BaseOBObject) value);
        } else {
          final Property property = DalUtil
              .getPropertyFromPath(bob.getEntity(), additionalProperty);
          // identifier
          if (additionalProperty.endsWith(JsonConstants.IDENTIFIER)) {
            jsonObject.put(additionalProperty, value);
          } else {
            jsonObject.put(additionalProperty, convertPrimitiveValue(property, value));
          }
        }
      }

      // The recordTime is also added. This is the time (in milliseconds) at which the record was
      // generated. This time can be used in the client side to compute the record "age", for
      // example, or how much time has passed since the record was updated
      jsonObject.put("recordTime", new Date().getTime());
      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
  }

  private Property getPropertyFromPath(BaseOBObject bob, String propertyPath) {
    final String[] parts = propertyPath.split("\\.");
    BaseOBObject currentBob = bob;
    Property result = null;
    Object value = null;
    for (String part : parts) {
      // only consider it as an identifier if it is called an identifier and
      // the entity does not accidentally have an identifier property
      // && !currentEntity.hasProperty(part)
      // NOTE disabled for now, there is one special case: AD_Column.IDENTIFIER
      // which is NOT HANDLED
      final Entity currentEntity = currentBob.getEntity();
      if (!currentEntity.hasProperty(part)) {
        return null;
      }
      value = currentBob.get(part);
      // if there is a next step, just make it
      // if it is last then we stop anyway
      if (value instanceof BaseOBObject) {
        currentBob = (BaseOBObject) value;
      } else {
        return currentEntity.getProperty(part);
      }
    }
    return result;
  }

  private void addBaseOBObject(JSONObject jsonObject, Property referencingProperty,
      String propertyName, Property referencedProperty, BaseOBObject obObject) throws JSONException {
    // jsonObject.put(propertyName, toJsonObject(obObject, DataResolvingMode.SHORT));
    if (referencedProperty != null) {
      try {
        jsonObject.put(propertyName, obObject.get(referencedProperty.getName()));
      } catch (ObjectNotFoundException e) {
        // Referenced object does not exist, set UUID
        jsonObject.put(propertyName, e.getIdentifier());
        jsonObject.put(propertyName + "." + JsonConstants.IDENTIFIER, e.getIdentifier());
        return;
      }
    } else {
      jsonObject.put(propertyName, obObject.getId());
    }
    // jsonObject.put(propertyName + "." + JsonConstants.ID, obObject.getId());

    if (referencingProperty != null && referencingProperty.hasDisplayColumn()) {

      Property displayColumnProperty = DalUtil.getPropertyFromPath(referencedProperty.getEntity(),
          referencingProperty.getDisplayPropertyName());
      if (displayColumnProperty.hasDisplayColumn()) {
        // Allowing one level deep of displayed column pointing to references with display column
        jsonObject.put(propertyName + "." + JsonConstants.IDENTIFIER, ((BaseOBObject) obObject
            .get(referencingProperty.getDisplayPropertyName())).get(displayColumnProperty
            .getDisplayPropertyName()));
      } else if (!displayColumnProperty.isPrimitive()) {
        // Displaying identifier for non primitive properties
        jsonObject.put(propertyName + "." + JsonConstants.IDENTIFIER, ((BaseOBObject) obObject
            .get(referencingProperty.getDisplayPropertyName())).getIdentifier());
      } else {
        jsonObject.put(propertyName + "." + JsonConstants.IDENTIFIER,
            obObject.get(referencingProperty.getDisplayPropertyName()));
      }
    } else {
      jsonObject.put(propertyName + "." + JsonConstants.IDENTIFIER, obObject.getIdentifier());
    }
  }

  // TODO: do some form of formatting here?
  protected Object convertPrimitiveValue(Property property, Object value) {
    final Class<?> clz = property.getPrimitiveObjectType();
    if (Date.class.isAssignableFrom(clz)) {
      if (property.getDomainType() instanceof TimestampDomainType) {
        final String formattedValue = xmlTimeFormat.format(value);
        return JsonUtils.convertToCorrectXSDFormat(formattedValue);
      } else if (property.isDatetime() || Timestamp.class.isAssignableFrom(clz)) {
        final String formattedValue = xmlDateTimeFormat.format(value);
        return JsonUtils.convertToCorrectXSDFormat(formattedValue);
      } else {
        return xmlDateFormat.format(value);
      }
      // for the properties of type password -> do not return raw-value at all
    } else if (property.getDomainType() instanceof HashedStringDomainType
        || property.getDomainType() instanceof EncryptedStringDomainType) {
      return "***";
    } else if (property.getDomainType() instanceof BinaryDomainType && value instanceof byte[]) {
      return Base64.encodeBase64String((byte[]) value);
    }
    return value;
  }

  protected Object convertPrimitiveValue(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Date) {
      return xmlDateFormat.format(value);
    }
    if (value instanceof Timestamp) {
      return xmlDateTimeFormat.format(value);
    }
    return value;
  }

  protected String encodeReference(BaseOBObject bob) {
    return bob.getEntityName() + REF_SEPARATOR + bob.getId();
  }

  public List<String> getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(List<String> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }
}
