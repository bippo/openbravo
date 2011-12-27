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

package org.openbravo.base.model.domaintype;

import java.lang.reflect.Constructor;

import org.openbravo.base.model.Property;
import org.openbravo.base.validation.ValidationException;

/**
 * The base class for primitive property types. Subclasses only need to implement
 * {@link PrimitiveDomainType#getPrimitiveType()}.
 * 
 * @author mtaal
 */
public abstract class BasePrimitiveDomainType extends BaseDomainType implements PrimitiveDomainType {

  private Constructor<Object> constructor;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.PrimitiveDomainType#getHibernateType()
   */
  public Class<?> getHibernateType() {
    return getPrimitiveType();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.base.model.domaintype.DomainType#checkIsValidValue(org.openbravo.base.model.Property
   * , java.lang.Object)
   */
  public void checkIsValidValue(Property property, Object value) throws ValidationException {
    if (value == null) {
      return;
    }
    if (!getPrimitiveType().isInstance(value)) {
      final ValidationException ve = new ValidationException();
      ve.addMessage(property, "Property " + property + " only allows instances of "
          + getPrimitiveType().getName() + " but the value is an instanceof "
          + value.getClass().getName());
      throw ve;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.PrimitiveDomainType#getFormatId()
   */
  public String getFormatId() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.PrimitiveDomainType#convertToString(java.lang.Object)
   */
  @Override
  public String convertToString(Object value) {
    if (value == null) {
      return EMPTY_STRING;
    }
    return value.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.PrimitiveDomainType#createFromString(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object createFromString(String strValue) {
    if (strValue == null || strValue.length() == 0) {
      return null;
    }

    try {
      if (constructor == null) {
        final Class<Object> clz = (Class<Object>) getPrimitiveType();
        constructor = clz.getConstructor(String.class);
      }
      return constructor.newInstance(strValue);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
