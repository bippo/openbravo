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

import java.util.HashSet;
import java.util.Set;

import org.openbravo.base.model.Property;
import org.openbravo.base.validation.ValidationException;

/**
 * The type of a column which can only have a value from a pre-defined set.
 * 
 * @author mtaal
 */

public abstract class BaseEnumerateDomainType<E extends Object> extends BasePrimitiveDomainType
    implements EnumerateDomainType {

  private Set<E> enumerateValues = new HashSet<E>();

  /**
   * @return the set of enumerate values
   */
  public Set<E> getEnumerateValues() {
    return enumerateValues;
  }

  public void addEnumerateValue(E enumerateValue) {
    enumerateValues.add(enumerateValue);
  }

  /**
   * @return class of {@link Object}.
   */
  public Class<?> getPrimitiveType() {
    return Object.class;
  }

  public void checkIsValidValue(Property property, Object value) throws ValidationException {
    super.checkIsValidValue(property, value);

    if (!getEnumerateValues().contains(value)) {
      final ValidationException ve = new ValidationException();
      ve.addMessage(property, "Property " + property + ", value (" + value
          + ") is not allowed, it should be one of the following values: " + getEnumerateValues()
          + " but it is value " + value);
      throw ve;
    }
  }

}
