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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.Identifiable;
import org.openbravo.data.FieldProvider;

/**
 * An implementation of the {@link FieldProviderFactory} which can handle Openbravo business
 * objects.
 */
public class OBObjectFieldProvider implements FieldProvider, OBNotSingleton {

  private BaseOBObject obObject;
  private Map<String, Property> fieldNameToProperty = new HashMap<String, Property>();
  private Map<String, String> valueCache = new HashMap<String, String>();

  private boolean returnObjectValueAsId = false;

  private DateFormat dateFormatter;
  private DateFormat dateTimeFormatter;
  private NumberFormat decimalFormatter = DecimalFormat.getNumberInstance();
  private NumberFormat integerFormatter = NumberFormat.getIntegerInstance();

  /**
   * Creates a new instance of a field provider and sets the internal obObject member.
   * 
   * @param bob
   *          the business object to create the field provider for
   * @return a new FieldProvider
   */
  public static OBObjectFieldProvider createOBObjectFieldProvider(BaseOBObject bob) {
    final OBObjectFieldProvider fieldProvider = OBProvider.getInstance().get(
        OBObjectFieldProvider.class);
    fieldProvider.setObObject(bob);
    return fieldProvider;
  }

  /**
   * Returns an array of field providers.
   * 
   * @param bobs
   *          the list of {@link BaseOBObject} instances for which to create the
   *          {@link FieldProvider} instances
   * @return an array of field providers, one for each passed BaseOBObject
   */
  public static <T extends BaseOBObject> OBObjectFieldProvider[] createOBObjectFieldProvider(
      List<T> bobs) {
    final OBObjectFieldProvider[] result = new OBObjectFieldProvider[bobs.size()];
    int index = 0;
    for (BaseOBObject bob : bobs) {
      result[index++] = createOBObjectFieldProvider(bob);
    }
    return result;
  }

  /**
   * This is the implementation for the FieldProvider.getField(String s) method which will be
   * invoked in the object.
   * <p>
   * It finds the property of the BaseOBObject using the passed field name parameter. The fieldName
   * is encoded in a special way: all underscores have been removed from the column name and all the
   * characters after the underscores have been uppercased.
   * 
   */
  public String getField(String fieldName) {
    // is converted to lower case for simplicity
    final String lowerFieldName = fieldName.toLowerCase();
    final String result;
    if ((result = valueCache.get(lowerFieldName)) != null) {
      return result;
    }
    final Property property = fieldNameToProperty.get(lowerFieldName);
    if (property == null) {
      throw new OBException("The fieldName " + fieldName
          + " can not be mapped to a property of the entity " + getObObject().getEntityName());
    }
    final Object objValue = getObObject().get(property.getName());
    final String strValue = convert(property, objValue);
    valueCache.put(lowerFieldName, strValue);
    return strValue;
  }

  protected String convert(Property property, Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof String) {
      return (String) value;
    }
    if (value instanceof BaseOBObject) {
      if (isReturnObjectValueAsId()) {
        return (String) ((BaseOBObject) value).getId();
      } else {
        return ((Identifiable) value).getIdentifier();
      }
    }
    if (value instanceof Date && property.isDatetime()) {
      return dateTimeFormatter.format((Date) value);
    } else if (value instanceof Date) {
      return dateFormatter.format((Date) value);
    }

    if (value instanceof BigDecimal) {
      return decimalFormatter.format(value);
    }

    if (value instanceof Long) {
      return integerFormatter.format(value);
    }
    // TODO do some smart things with conversion!
    // to handle date/datetime/Numeric values
    return value.toString();
  }

  public BaseOBObject getObObject() {
    return obObject;
  }

  public void setObObject(BaseOBObject obObject) {
    this.obObject = obObject;
    // fill the field name to property thing
    for (Property property : obObject.getEntity().getProperties()) {
      if (property.getColumnName() == null) {
        // ignore these....
        continue;
      }

      // convert the columnname!
      // note removing all _ and to lower case
      final String convertedColName = property.getColumnName().replaceAll("_", "").toLowerCase();
      fieldNameToProperty.put(convertedColName, property);
    }

    // also set the date format
    final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    dateFormatter = new SimpleDateFormat(props.getProperty("dateFormat.java"));
    dateTimeFormatter = new SimpleDateFormat(props.getProperty("dateTimeFormat.java"));
  }

  public boolean isReturnObjectValueAsId() {
    return returnObjectValueAsId;
  }

  public void setReturnObjectValueAsId(boolean returnObjectValueAsId) {
    this.returnObjectValueAsId = returnObjectValueAsId;
  }

  /**
   * Sets the pattern by which decimal numbers are formatted.
   * 
   * @param pattern
   *          a java pattern
   * @see DecimalFormat
   */
  public void setDecimalFormatPattern(String pattern) {
    decimalFormatter = new DecimalFormat(pattern);
  }

  /**
   * Sets the pattern by which integer/long numbers are formatted.
   * 
   * @param pattern
   *          a java pattern
   * @see DecimalFormat
   */
  public void setIntegerFormatPattern(String pattern) {
    integerFormatter = new DecimalFormat(pattern);
  }
}
