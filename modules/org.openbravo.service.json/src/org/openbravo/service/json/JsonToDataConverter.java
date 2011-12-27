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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.BigDecimalDomainType;
import org.openbravo.base.model.domaintype.BinaryDomainType;
import org.openbravo.base.model.domaintype.EncryptedStringDomainType;
import org.openbravo.base.model.domaintype.HashedStringDomainType;
import org.openbravo.base.model.domaintype.TimestampDomainType;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.Traceable;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.utils.CryptoUtility;
import org.openbravo.utils.FormatUtilities;

/**
 * Converts json data to Openbravo business object(s).
 * 
 * This class can translate single a {@link BaseOBObject} as well as lists of {@link BaseOBObject}
 * instances.
 * 
 * A single JSONObject is assumed to represent one {@link BaseOBObject}. The conversion process goes
 * through the following steps for each JSONObject.
 * 
 * First it is checked if the JSONObject has an id and entityName value. If so the logic tries to
 * find the object in the internal key-to-object map (maintained during the conversion). If not
 * found then the database is search. if not found there then a new BaseOBObject is created.If the
 * object has an id then it is put in the key-to-object map so that it can be found if later objects
 * refer to it.
 * <p/>
 * Then the next step is to convert/set the values. the logic walks through the properties of the
 * object (the properties are defined by the entity of the object (see
 * {@link BaseOBObject#getEntity()} and {@link Entity#getProperties()}. For primitive values, the
 * property name ({@link Property#getName()}) is used to search a value in the JSONObject. The value
 * is retrieved converted and then set in the business object.
 * <p/>
 * Reference/foreign key properties are handled differently. In this case the property name is
 * appended with the ".id" postfix and this name is used to search for a value in the JSONObject. If
 * there is no such value present then the property name is used. The id value is used to search in
 * the key-to-object map and in the database (in this order). If not found then a new object is
 * created as it may be present later in the overall json string. The system later tracks that there
 * are no such maverick objects present (call {@link #getErrors()} to check this).
 * <p/>
 * This class also handles a special type of property/keys. It can handle setting values in related
 * objects also. For example say that the json object represents a sales order and has these keys
 * (and their values):
 * <ul>
 * <li>businessPartner.paymentTerm.name</li>
 * <li>businessPartner.paymentTerm.id</li>
 * <li>businessPartner.id</li>
 * </ul>
 * 
 * Then these keys are used to:
 * <ul>
 * <li>set the businesspartner object in the sales order using the businesspartner.id</li>
 * <li>set the paymentTerm in the business partner using the businessPartner.paymentTerm.id</li>
 * <li>set the name of the paymentTerm using the businessPartner.paymentTerm.name</li>
 * </ul>
 * 
 * So this class can handle flat JSON structures which represent a hierarchy/path in their name.
 * 
 * NOTE:
 * <ul>
 * <li>This class holds state during the conversion process. It can not be shared by multiple
 * threads.</li>
 * <li>After calling one of the toBaseOBObject* methods the conversion errors can be retrieved using
 * the {@link #getErrors()} method.</li>
 * <li>When calling toBaseOBObject* multiple times you can clear the internal state by calling
 * {@link #clearState()}.
 * </ul>
 * 
 * @author mtaal
 */
public class JsonToDataConverter {
  private static final Logger log = Logger.getLogger(JsonToDataConverter.class);

  private static final String DOT = ".";

  private final Map<String, BaseOBObject> keyToObject = new HashMap<String, BaseOBObject>();
  // these can be created as references but need to be also really imported.
  private final Map<String, BaseOBObject> newObjects = new HashMap<String, BaseOBObject>();

  private final static SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
  private final static SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();
  private final static SimpleDateFormat xmlTimeFormat = JsonUtils.createTimeFormat();

  private final List<JsonConversionError> errors = new ArrayList<JsonConversionError>();

  /**
   * Gets a value from json and converts it to a valid value for the DAL.
   */
  public static synchronized Object convertJsonToPropertyValue(Property property, Object value) {
    try {
      if (isEmptyOrNull(value)) {
        return null;
      }
      if (!property.isPrimitive()) {
        return value;
      }
      // do some common conversions
      final Class<?> clz = property.getPrimitiveObjectType();
      if (clz != null && Date.class.isAssignableFrom(clz)) {
        try {
          if (property.getDomainType() instanceof TimestampDomainType) {
            String strValue = (String) value;
            if (strValue.equals("null")) {
              return null;
            }
            // there are cases that also the date part is sent in, get rid of it
            if (strValue.indexOf("T") != -1) {
              final int index = strValue.indexOf("T");
              strValue = strValue.substring(index + 1);
            }

            if (strValue.indexOf("+") == -1 && strValue.indexOf("-") == -1) {
              strValue = strValue + "+0000";
            } else {
              strValue = JsonUtils.convertFromXSDToJavaFormat(strValue);
            }

            return new Timestamp(xmlTimeFormat.parse(strValue).getTime());
          } else if (property.isDatetime() || Timestamp.class.isAssignableFrom(clz)) {
            final String repairedString = JsonUtils.convertFromXSDToJavaFormat((String) value);
            return new Timestamp(xmlDateTimeFormat.parse(repairedString).getTime());
          } else {
            return xmlDateFormat.parse((String) value);
          }
        } catch (ParseException e) {
          throw new Error(e);
        }
      } else if (property.isBoolean() && value instanceof String) {
        if (value.equals("Y")) {
          return true;
        }
        return Boolean.parseBoolean((String) value);
      } else if (value instanceof Double) {
        return new BigDecimal((Double) value);
      } else if (value instanceof Integer && property.getPrimitiveObjectType() == Long.class) {
        return new Long((Integer) value);
      } else if (value instanceof Integer && property.getPrimitiveObjectType() == Float.class) {
        return new Float((Integer) value);
      } else if (value instanceof Long && property.getPrimitiveObjectType() == Float.class) {
        return new Float((Long) value);
      } else if (value instanceof BigDecimal
          && property.getPrimitiveObjectType() == BigDecimal.class) {
        return value;
      } else if (value instanceof Number && property.getPrimitiveObjectType() == BigDecimal.class) {
        return new BigDecimal(((Number) value).doubleValue());
      } else if (value instanceof String
          && property.getDomainType() instanceof HashedStringDomainType) {
        String str = (String) value;
        try {
          return FormatUtilities.sha1Base64(str);
        } catch (ServletException e) {
          log.error("Error hashing password", e);
          // TODO: translate error message
          throw new Error("Could not encrypt password", e);
        }
      } else if (value instanceof String
          && property.getDomainType() instanceof EncryptedStringDomainType) {
        String str = (String) value;
        try {
          return CryptoUtility.encrypt(str);
        } catch (ServletException e) {
          log.error("Error encrypting password", e);
          // TODO: translate error message
          throw new Error("Could not encrypt password", e);
        }
      } else if (value instanceof String && property.getDomainType() instanceof BinaryDomainType) {
        return Base64.decodeBase64((String) value);
      }
      return value;
    } catch (Exception e) {
      throw new OBException("Error when converting value " + value + " for prop " + property, e);
    }
  }

  private static boolean isEmptyOrNull(Object value) {
    if (JSONObject.NULL.equals(value)) { // note JSONObject.NULL.equals(null) == true
      return true;
    }
    if (value == null) {
      return true;
    }
    if (value instanceof String && ((String) value).trim().length() == 0) {
      return true;
    }
    return false;
  }

  public void clearState() {
    errors.clear();
    keyToObject.clear();
  }

  /**
   * Converts a JSONArray to a list of BaseOBObject instances. The JSONArray is assumed to contain
   * only JSONObjects which represent each a BaseOBObject.
   * 
   * See the conversion logic description in the class header for more information.
   * 
   * @param jsonArray
   *          the array with JSONObjects
   * @return the list of BaseOBObjects.
   */
  public List<BaseOBObject> toBaseOBObjects(JSONArray jsonArray) {
    try {
      Check.isNotNull(jsonArray, "Object may not be null");
      final List<BaseOBObject> result = new ArrayList<BaseOBObject>();
      for (int i = 0; i < jsonArray.length(); i++) {
        result.add(toBaseOBObject((JSONObject) jsonArray.get(i)));
      }
      return result;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Converts a list with JSONObjects to a list of BaseOBObject instances.
   * 
   * See the conversion logic description in the class header for more information.
   * 
   * @param jsonObjects
   *          the list with JSONObjects
   * @return the list of BaseOBObjects.
   */
  public List<BaseOBObject> toBaseOBObjects(List<JSONObject> jsonObjects) {
    try {
      Check.isNotNull(jsonObjects, "Object may not be null");
      final List<BaseOBObject> result = new ArrayList<BaseOBObject>();
      for (JSONObject jsonObject : jsonObjects) {
        result.add(toBaseOBObject(jsonObject));
      }
      return result;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Converts a single JSONObject to a BaseOBObject.
   * 
   * See the conversion logic description in the class header for more information.
   * 
   * @param jsonObject
   *          the jsonObject to convert
   * @return the BaseOBObject (new or retrieved from the database)
   */
  public BaseOBObject toBaseOBObject(JSONObject jsonObject) throws Exception {
    String id = null;
    if (jsonObject.has(JsonConstants.ID)) {
      id = jsonObject.getString(JsonConstants.ID);
    }
    // if there is a new indicator then nullify the id again to treat the object has new
    final boolean isNew = jsonObject.has(JsonConstants.NEW_INDICATOR)
        && jsonObject.getBoolean(JsonConstants.NEW_INDICATOR);
    if (isNew) {
      id = null;
    }

    String entityName = null;
    if (jsonObject.has(JsonConstants.ENTITYNAME)) {
      entityName = jsonObject.getString(JsonConstants.ENTITYNAME);
    }
    if (entityName == null) {
      throw new IllegalArgumentException("Entity name not defined in jsonobject " + jsonObject);
    }

    BaseOBObject obObject;
    if (id != null) {
      final String key = getObjectKey(id, entityName);
      if (keyToObject.get(key) != null) {
        obObject = keyToObject.get(key);
      } else {
        obObject = OBDal.getInstance().get(entityName, id);
        if (obObject == null) {
          obObject = (BaseOBObject) OBProvider.getInstance().get(entityName);
        }
      }
      keyToObject.put(key, obObject);
      // remove it from the newObjects as we found it in the json set.
      newObjects.remove(key);
    } else {
      obObject = (BaseOBObject) OBProvider.getInstance().get(entityName);
    }

    if (isNew) {
      obObject.setNewOBObject(true);
    }

    setData(jsonObject, obObject);

    // see the DefaultJSonDataService for a description on specific handling of
    // new computed id's
    if (isNew) {
      // remove the id before it gets saved
      obObject.setValue(JsonConstants.ID, null);
    }

    return obObject;
  }

  /**
   * Sets the data in the BaseOBObject by converting data from the json object and finding the
   * correct properties to set.
   * 
   * @param jsonObject
   *          the source of the data
   * @param obObject
   *          the target of the data
   */
  protected void setData(JSONObject jsonObject, BaseOBObject obObject) throws JSONException {

    // just use a random entity to get the name of the updated property
    if (jsonObject.has(Organization.PROPERTY_UPDATED) && obObject instanceof Traceable) {
      final String jsonDateStr = jsonObject.getString(Organization.PROPERTY_UPDATED);
      if (jsonDateStr != null && !jsonDateStr.equals("null")) {
        try {
          final String repairedString = JsonUtils.convertFromXSDToJavaFormat(jsonDateStr);
          final Date jsonDate = new Timestamp(xmlDateTimeFormat.parse(repairedString).getTime());
          final Date objectDate = ((Traceable) obObject).getUpdated();
          if (!areDatesEqual(jsonDate, objectDate, true, false)) {
            // return this message code to let the client show a translated label
            throw new OBStaleObjectException("@OBJSON_StaleDate@");
          }
        } catch (OBStaleObjectException x) {
          throw x;
        } catch (Exception e) {
          throw new OBException("Exception when updating " + obObject, e);
        }
      }
    }

    final Entity entity = obObject.getEntity();

    // collect the keys
    final List<String> keyNames = new ArrayList<String>();
    final Iterator<?> iterator = jsonObject.keys();
    while (iterator.hasNext()) {
      keyNames.add((String) iterator.next());
    }

    for (String keyName : keyNames) {
      // a foreign key
      final boolean multiPathProperty = keyName.contains(DOT);
      if (multiPathProperty) {
        // TODO: handle passwords here also
        handleMultiPathProperty("", keyName, obObject, jsonObject, jsonObject.get(keyName));
      } else {
        if (!entity.hasProperty(keyName)) {
          continue;
        }
        final Property property = entity.getProperty(keyName);
        if (property.getDomainType() instanceof EncryptedStringDomainType
            || property.getDomainType() instanceof HashedStringDomainType) {
          if (jsonObject.has(keyName + "_cleartext")) {
            Object valCleartext = jsonObject.get(keyName + "_cleartext");
            setValue(obObject, property, valCleartext);
          } else {
            // no _cleartext value found -> skipping field
          }
        } else if (property.getDomainType() instanceof BigDecimalDomainType
            && jsonObject.has(keyName + "_textualValue")) {
          final String strValue = (String) jsonObject.get(keyName + "_textualValue");
          try {
            if (strValue == null || strValue.trim().length() == 0) {
              setValue(obObject, property, null);
            } else {
              setValue(obObject, property, new BigDecimal(strValue));
            }
          } catch (Exception e) {
            throw new OBException("Exception while trying to convert value:-->" + strValue + "<-- "
                + e.getMessage(), e);
          }
        } else {
          setValue(obObject, property, jsonObject.get(keyName));
        }
      }
    }
  }

  /**
   * Handles the complex case of keys in the JSONObject which consists of multiple steps. For
   * example businessPartner.paymentTerm.name. To handle these keys the system has to walk the path
   * and find the object at the end of the path to set the value.
   * 
   * The system should take into account that not all objects along the path already exists, for
   * example the paymentTerm in the businessPartner maybe null. In that case a new PaymentTerm needs
   * to be set in the businessPartner. The system should also check if there is an id passed in for
   * the paymentTerm: businessPartner.paymentTerm.id. If so then that one should be used to
   * create/read the PaymentTerm.
   * 
   * The previousPath and remainingPath parameters are used to keep track of the path walked. For
   * example if the system is processing the part of the paymentTerm then the previousPath will be:
   * 'businessPartner' and the remaining path will be: 'paymentTerm.name'.
   * 
   * @param previousPath
   *          see part of the path which has been 'walked', is initially empty.
   * @param remainingPath
   *          the part which needs to be handled.
   * @param obObject
   *          the current BaseOBObject to set
   * @param jsonObject
   *          the jsonObject containing the values
   * @param value
   *          the value to set when the end of the path has been reached.
   */
  protected void handleMultiPathProperty(String previousPath, String remainingPath,
      BaseOBObject obObject, JSONObject jsonObject, Object value) throws JSONException {
    final int dotIndex = remainingPath.indexOf(DOT);
    if (dotIndex == -1) {
      if (remainingPath.equals(JsonConstants.IDENTIFIER)) {
        return;
      }
      if (!obObject.getEntity().hasProperty(remainingPath)) {
        // invalid property, ignore those...
        logError(obObject, "Property not found " + remainingPath + " error when parsing path "
            + (previousPath + DOT + remainingPath));
        return;
      }
      final Property property = obObject.getEntity().getProperty(remainingPath);
      setValue(obObject, property, value);
    } else {
      final String firstPart = remainingPath.substring(0, dotIndex);
      if (!obObject.getEntity().hasProperty(firstPart)) {
        logError(obObject, "Property not found " + firstPart + " error when parsing path "
            + (previousPath + DOT + remainingPath));
        return;
      }
      final Property property = obObject.getEntity().getProperty(firstPart);
      final String secondPart = remainingPath.substring(dotIndex + 1);
      final boolean isId = secondPart.equals(JsonConstants.ID);
      if (property.isPrimitive() || property.isOneToMany()) {
        // TODO: log this error condition
        return;
      }
      if (isId) {
        // the final part of a multiPart
        setValue(obObject, property, value);
      } else {
        BaseOBObject currentOBObject = (BaseOBObject) obObject.get(property.getName());

        // really multi-part, check if there is an id property
        String id = null;
        final String idPath = previousPath + DOT + firstPart + DOT + JsonConstants.ID;
        // check if we are still looking at the correct object, if not replace it
        if (jsonObject.has(idPath) && !jsonObject.isNull(idPath)) {
          id = jsonObject.getString(idPath);
          if (currentOBObject == null || !id.equals(currentOBObject.getId())) {
            setValue(obObject, property, id);
            currentOBObject = (BaseOBObject) obObject.get(property.getName());
            Check.isNotNull(currentOBObject, "The baseObObject was not set " + obObject + " "
                + previousPath + " " + remainingPath);
          }
        } else if (currentOBObject == null) {
          currentOBObject = (BaseOBObject) OBProvider.getInstance().get(
              property.getTargetEntity().getName());
        }
        handleMultiPathProperty(previousPath + (previousPath.length() > 0 ? DOT : "") + firstPart,
            secondPart, currentOBObject, jsonObject, value);
      }
    }

  }

  /**
   * @see #getBaseOBObjectFromId(Entity, Property, String)
   */
  protected BaseOBObject getBaseOBObjectFromId(Entity entity, String referencedId) {
    BaseOBObject value = null;
    if (referencedId != null) {
      final String key = getObjectKey(referencedId, entity.getName());
      if (keyToObject.get(key) != null) {
        value = keyToObject.get(key);
      } else {
        value = OBDal.getInstance().get(entity.getName(), referencedId);
      }
      if (value == null) {
        value = (BaseOBObject) OBProvider.getInstance().get(entity.getName());
        // put it here, it must be imported later as a real object
        newObjects.put(key, (BaseOBObject) value);
      }
      keyToObject.put(key, (BaseOBObject) value);
    } else {
      value = (BaseOBObject) OBProvider.getInstance().get(entity.getName());
    }

    // note: when inheritance is supported then this Check should be changed/removed
    Check.isTrue(value.getEntity() == entity, "The object " + value
        + " has a different entity then the request entity " + entity);
    return value;
  }

  /**
   * Tries to find an object first in the internal map which is maintained to first resolve id's
   * internally within the complete json string. If not found there then the database is queried
   * using the entity and referenceId. If not found there (or the referenceId == null) then create a
   * new instance of the entity.
   * 
   * @param entity
   *          the entity to retrieve/create
   * @param property
   *          the property referencing to the entity
   * @param referencedId
   *          the id of the entity
   * @return an existing of new BaseOBObject
   */
  protected BaseOBObject getBaseOBObjectFromId(Entity entity, Property property, String referencedId) {
    BaseOBObject value = null;
    if (referencedId != null) {
      final String key = getObjectKey(referencedId, entity.getName());
      if (keyToObject.get(key) != null) {
        value = keyToObject.get(key);
      } else {
        if (property.getReferencedProperty() != null) {
          final OBQuery<BaseOBObject> qry = OBDal.getInstance().createQuery(entity.getName(),
              property.getReferencedProperty().getName() + "=:reference");
          qry.setNamedParameter("reference", referencedId);
          qry.setFilterOnActive(false);
          qry.setFilterOnReadableClients(false);
          qry.setFilterOnReadableOrganization(false);
          final List<BaseOBObject> result = qry.list();
          if (result.size() > 1) {
            log.warn("More than one result when querying " + entity + " using property "
                + property.getReferencedProperty() + " with value " + referencedId
                + ", choosing the first result");
            value = result.get(0);
          } else if (result.size() == 1) {
            value = result.get(0);
          } else {
            value = null;
          }
        } else {
          value = OBDal.getInstance().get(entity.getName(), referencedId);
        }
      }
      if (value == null) {
        value = (BaseOBObject) OBProvider.getInstance().get(entity.getName());
        // put it here, it must be imported later as a real object
        newObjects.put(key, (BaseOBObject) value);
      }
      keyToObject.put(key, (BaseOBObject) value);
    } else {
      value = (BaseOBObject) OBProvider.getInstance().get(entity.getName());
    }

    // note: when inheritance is supported then this Check should be changed/removed
    Check.isTrue(value.getEntity() == entity, "The object " + value
        + " has a different entity then the request entity " + entity);
    return value;
  }

  /**
   * Checks if the property is settable ({@link #isNotConvertable(BaseOBObject, Property)}. If so
   * the jsonValue is converted ({@link #convertPrimitive(Property, Object)}, in case of a primive),
   * or in case of a reference property the referenced object is read/created.
   * 
   * @param obObject
   *          the object to set
   * @param property
   *          the property of the object to set
   * @param jsonValue
   *          the value as it is present in the original JSONObject
   */
  protected void setValue(BaseOBObject obObject, Property property, Object jsonValue) {
    Check.isTrue(obObject.getEntity().hasProperty(property.getName()), "The object " + obObject
        + " does not have the property " + property);

    if (isNotConvertable(obObject, property)) {
      // valid case, do not log
      return;
    }

    try {
      // convert/read the value
      final Object value;
      if (isEmptyOrNull(jsonValue)) {
        value = null;
      } else if (property.isPrimitive()) {
        // convert the value
        value = convertJsonToPropertyValue(property, jsonValue);
      } else if (jsonValue instanceof String) {
        // an id
        final String referenceId = (String) jsonValue;
        if (property.getReferencedProperty() != null) {
          value = getBaseOBObjectFromId(property.getTargetEntity(), property, referenceId);
        } else {
          value = getBaseOBObjectFromId(property.getTargetEntity(), referenceId);
        }
      } else {
        // a json object
        // try another approach, maybe the value is a jsonobject itself
        final JSONObject fkValue = (JSONObject) jsonValue;
        // add the entityname to help retrieving it from the db
        if (!fkValue.has(JsonConstants.ENTITYNAME)) {
          fkValue.put(JsonConstants.ENTITYNAME, property.getTargetEntity().getName());
        }
        value = toBaseOBObject(fkValue);
      }

      // if nothing changed then don't set anything
      // this is usefull if the current and new value are the same but not 'valid' anymore
      if (!obObject.isNewOBObject() && value != null) {
        final Object currentValue = obObject.get(property.getName());
        if (property.isPrimitive() && value.equals(currentValue)) {
          return;
        } else if (value == currentValue) {
          // non-primitive, if the exact same value then don't set it
          return;
        } else if (Date.class.isAssignableFrom(value.getClass()) && currentValue != null
            && Date.class.isAssignableFrom(currentValue.getClass())) {
          // there are mismatches between json and the database in
          // precision of times/dates, these are repaired here by
          // not updating if the relevant part is the same
          if (areDatesEqual((Date) value, (Date) currentValue, property.isDatetime(),
              property.getDomainType() instanceof TimestampDomainType)) {
            return;
          }
        }
      }

      // and set the value
      obObject.set(property.getName(), value);

    } catch (Throwable t) {
      // store/log all errors
      final JsonConversionError conversionError = new JsonConversionError();
      conversionError.setBaseOBObject(obObject);
      conversionError.setProperty(property);
      conversionError.setThrowable(t);
      errors.add(conversionError);
    }
  }

  /**
   * The json conversion looses precision in milliseconds and seconds. This comparison method only
   * compares the other parts of the date object. Depending if a date or a date time is set.
   * 
   * @param d1
   *          the first date to compare
   * @param d2
   *          the second date to compare
   * @param isDatetime
   *          is it a datetime
   * @return true if d1 and d2 have equal values for year, month and day and for date time also same
   *         values for hour, minutes and seconds.
   */
  protected boolean areDatesEqual(Date d1, Date d2, boolean isDatetime, boolean isTime) {
    final Calendar c1 = Calendar.getInstance();
    c1.setTime(d1);
    final Calendar c2 = Calendar.getInstance();
    c2.setTime(d2);
    if (isTime) {
      c2.set(Calendar.MILLISECOND, 0);
      c1.set(Calendar.MILLISECOND, 0);
    } else if (isDatetime) {
      c2.set(Calendar.MILLISECOND, 0);
      c1.set(Calendar.MILLISECOND, 0);
    } else {
      c2.set(Calendar.MILLISECOND, 0);
      c1.set(Calendar.MILLISECOND, 0);
      c2.set(Calendar.SECOND, 0);
      c1.set(Calendar.SECOND, 0);
      c2.set(Calendar.MINUTE, 0);
      c1.set(Calendar.MINUTE, 0);
      c2.set(Calendar.HOUR, 0);
      c1.set(Calendar.HOUR, 0);
      c1.set(Calendar.AM_PM, Calendar.AM);
      c2.set(Calendar.AM_PM, Calendar.AM);
    }
    return c2.getTimeInMillis() == c1.getTimeInMillis();
  }

  protected boolean areDatesEqual(Date d1, Date d2, boolean isDatetime) {
    return this.areDatesEqual(d1, d2, isDatetime, false);
  }

  /**
   * Determines if a property can be converted from json to a value stored in the database.
   * Properties which are not updatable (and the object is not new) are not converted, the same for
   * properties which are expired etc.
   * 
   * @param obObject
   *          the object for which the property is set
   * @param property
   *          the property to set
   * @see Property#isInactive()
   * @see Property#isUpdatable()
   * @see Property#isAuditInfo()
   * @see Property#isClientOrOrganization()
   * @see Property#isOneToMany()
   * @return
   */
  protected boolean isNotConvertable(BaseOBObject obObject, Property property) {
    // one-to-many are never json-ized
    boolean doNotHandleThisProperty = property.isOneToMany();
    // do not change auditinfo or client for an existing object
    doNotHandleThisProperty |= !obObject.isNewOBObject()
        && (property.isAuditInfo() || property.getName().equals(Organization.PROPERTY_CLIENT));
    // do not change not changeable properties
    doNotHandleThisProperty |= property.isInactive();
    // do not change not updatable properties
    // Updatable is a UI concept
    // doNotHandleThisProperty |= !obObject.isNewOBObject() && !property.isUpdatable();
    return doNotHandleThisProperty;
  }

  private String getObjectKey(String id, String entityName) {
    return id + "_" + entityName;
  }

  public boolean hasErrors() {
    // create an exception for each new object
    if (!newObjects.isEmpty()) {
      for (String key : newObjects.keySet()) {
        final BaseOBObject bob = newObjects.get(key);
        final JsonConversionError conversionError = new JsonConversionError();
        conversionError.setBaseOBObject(bob);
        conversionError.setProperty(bob.getEntity().getIdProperties().get(0));
        conversionError.setExceptionWithMessage("New object " + bob + " (key: " + key
            + ") refered to but not present in the import set");
        errors.add(conversionError);
      }
    }
    return errors.size() > 0;
  }

  /**
   * Creates a {@link JsonConversionError} and adds it to the internal error list (
   * {@link #getErrors()}). As no property instance is passed the first id property of the obObject
   * is set as the property in the JsonConversionError (
   * {@link JsonConversionError#setProperty(Property)}).
   * 
   * @param obObject
   *          the object to which the error condition applies
   * @param msg
   *          the message to log
   */
  protected void logError(BaseOBObject obObject, String msg) {
    final JsonConversionError conversionError = new JsonConversionError();
    conversionError.setBaseOBObject(obObject);
    // just select any property
    conversionError.setProperty(obObject.getEntity().getIdProperties().get(0));
    conversionError.setExceptionWithMessage(msg);
    errors.add(conversionError);
  }

  public List<JsonConversionError> getErrors() {
    return errors;
  }

  /**
   * Is created when setting a value for a specific property fails.
   * 
   * @author mtaal
   */
  public class JsonConversionError {
    private BaseOBObject baseOBObject;
    private Throwable throwable;
    private Property property;

    public Throwable getThrowable() {
      return throwable;
    }

    public void setThrowable(Throwable throwable) {
      this.throwable = throwable;
    }

    public Property getProperty() {
      return property;
    }

    public void setProperty(Property property) {
      this.property = property;
    }

    public BaseOBObject getBaseOBObject() {
      return baseOBObject;
    }

    public void setBaseOBObject(BaseOBObject baseOBObject) {
      this.baseOBObject = baseOBObject;
    }

    public void setExceptionWithMessage(String msg) {
      throwable = new IllegalStateException(msg);
    }

    public String toString() {
      return "Error " + baseOBObject + " " + property + " " + throwable.getMessage();
    }
  }
}
