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

package org.openbravo.dal.xml;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;

/**
 * Converts primitive types to a XML representation and back.
 * 
 * NOTE: class is deprecated, use the {@link PrimitiveDomainType} interface which can be obtained
 * from the {@link Property#getDomainType()}.
 * 
 * 
 * @author mtaal
 */

public class XMLTypeConverter implements OBSingleton {

  private static XMLTypeConverter instance = new XMLTypeConverter();

  public static synchronized XMLTypeConverter getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(XMLTypeConverter.class);
    }
    return instance;
  }

  public static synchronized void setInstance(XMLTypeConverter instance) {
    XMLTypeConverter.instance = instance;
  }

  private final SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");

  /**
   * Returns the String format of a {@link Date}, the standard xml format is used:
   * yyyy-MM-dd'T'HH:mm:ss.S'Z'
   * 
   * @param dt
   *          the Date to format
   * @return the String representation of the Date in xml format
   */
  public synchronized String toXML(Date dt) {
    return xmlDateFormat.format(dt);
  }

  /**
   * @return numbers are converted using the Number.toString method
   */
  public String toXML(Number number) {
    return number.toString();
  }

  /** @return the parameter str is returned without changing it */
  public String toXML(String str) {
    return str;
  }

  /** @return the result of the b.toString() method is returned */
  public String toXML(Boolean b) {
    return b.toString();
  }

  /**
   * Returns an empty string if the object is null. In other cases the call is forwarded to one of
   * the other toXML methods in this class.
   * 
   * @param o
   *          the value to convert to a String XML representation
   * @return the String XML representation
   */
  public String toXML(Object o) {
    if (o == null) {
      return "";
    }
    if (o instanceof Number) {
      return toXML((Number) o);
    }
    if (o instanceof Date) {
      return toXML((Date) o);
    }
    if (o instanceof String) {
      return toXML((String) o);
    }
    if (o instanceof Boolean) {
      return toXML((Boolean) o);
    }
    if (o instanceof byte[]) {
      try {
        return new String(Base64.encodeBase64((byte[]) o), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return o.toString();
    // throw new OBException("Type " + o.getClass().getName() +
    // " not supported");
  }

  /**
   * Converts an xml String back to a primitive type java Object. If the xml string has lenght zero
   * then null is returned.
   * 
   * @param targetClass
   *          the class of the primitive type (e.g. String.class, Float.class)
   * @param xml
   *          the xml string to convert
   * @return the converted object
   */
  @SuppressWarnings("unchecked")
  public synchronized <T extends Object> T fromXML(Class<T> targetClass, String xml) {
    if (xml.length() == 0) {
      return null;
    }
    try {
      if (Date.class == targetClass) {
        return (T) xmlDateFormat.parse(xml);
      }
      if (Timestamp.class == targetClass) {
        final Date dt = xmlDateFormat.parse(xml);
        return (T) new Timestamp(dt.getTime());
      }
      if (String.class == targetClass) {
        return (T) xml;
      }
      if (BigDecimal.class == targetClass) {
        return (T) new BigDecimal(xml);
      }
      if (Long.class == targetClass) {
        return (T) new Long(new BigDecimal(xml).longValueExact());
      }
      if (boolean.class == targetClass) {
        return (T) new Boolean(xml);
      }
      if (Boolean.class == targetClass) {
        return (T) new Boolean(xml);
      }
      if (Float.class == targetClass) {
        return (T) new Float(xml);
      }
      if (byte[].class == targetClass) {
        return (T) Base64.decodeBase64(xml.getBytes("UTF-8"));
      }
    } catch (final Exception e) {
      throw new EntityXMLException("Value " + xml + " can not be parsed to an instance of class "
          + targetClass.getName());
    }
    throw new EntityXMLException("Unsupported target class " + targetClass.getName());
  }

  /**
   * @return the XML Schema type which matches the targetClass parameter
   */
  public String toXMLSchemaType(Class<?> targetClass) {
    if (Date.class == targetClass) {
      return "ob:dateTime";
    }
    if (Timestamp.class == targetClass) {
      return "ob:dateTime";
    }
    if (String.class == targetClass) {
      return "ob:string";
    }
    if (BigDecimal.class == targetClass) {
      return "ob:decimal";
    }
    if (Long.class == targetClass) {
      return "ob:long";
    }
    if (boolean.class == targetClass) {
      return "ob:boolean";
    }
    if (Boolean.class == targetClass) {
      return "ob:boolean";
    }
    if (Float.class == targetClass) {
      return "ob:float";
    }
    if (byte[].class == targetClass) {
      return "ob:base64Binary";
    }
    if (Object.class == targetClass) {
      // TODO catch this
      return "xs:anyType";
    }
    if (targetClass == null) {
      // TODO catch this
      return "NULL";
    }
    throw new OBException("Unsupported target class " + targetClass.getName());
  }
}