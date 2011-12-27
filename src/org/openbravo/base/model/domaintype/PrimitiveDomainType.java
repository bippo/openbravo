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

package org.openbravo.base.model.domaintype;

/**
 * The ModelReference implements the reference extensions used for the Data Access Layer. See <a
 * href
 * ="http://wiki.openbravo.com/wiki/Projects/Reference_Extension/Technical_Documentation#DAL">here
 * </a> for more information.
 * 
 * @author mtaal
 */

public interface PrimitiveDomainType extends DomainType {

  public static final String EMPTY_STRING = "";

  /**
   * Converts an object value of this type to a locale/system neutral String. Is used in XML
   * conversion. The opposite of this method is the {@link #createFromString(String)} method.
   * 
   * @param value
   *          the value to convert to a string.
   * @return the String representation, if the value is null then an empty String is returned
   * @throws IllegalArgumentException
   *           if the conversion is not possible
   */
  String convertToString(Object value);

  /**
   * Converts a string back to an object value of the primitive type ({link #getPrimitiveType()})
   * represented by this DomainType. This method is the opposite of the
   * {@link #convertToString(Object)} method.
   * 
   * @param strValue
   *          the string value to convert
   * @return the object value, null is returned if an empty string or null are passed as parameter.
   * @throws IllegalArgumentException
   *           if the conversion is not possible
   */
  Object createFromString(String strValue);

  /**
   * @return the xml schema type which matches the primitive type, is used to create the XML Schema
   *         for the REST web services.
   */
  String getXMLSchemaType();

  /**
   * The type used in the hibernate mapping. Most of the time is the same as the
   * {@link #getPrimitiveType()}. Can be used to set a hibnernate user type class. See the hibernate
   * documentation for more information on this.
   * 
   * @return the class representing the hibernate type
   */
  Class<?> getHibernateType();

  /**
   * The primitive type class (for example java.lang.Long) if this is a primitive type.
   * 
   * @return the class representing the primitive type
   */
  Class<?> getPrimitiveType();

  /**
   * Returns the id of the format definition to use for this domain type. Is normally only relevant
   * for numeric domain types. The id is the prefix part of the name in the Format.xml file. So for
   * example the id 'integer' maps to all the Format.xml entries with integer as a prefix.
   * 
   * @return the name of the format definition in the format.xml, if not relevant then null is
   *         returned.
   */
  String getFormatId();
}
