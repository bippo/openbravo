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

package org.openbravo.base.validation;

import java.math.BigDecimal;

import org.openbravo.base.model.Property;
import org.openbravo.base.util.Check;

/**
 * Validates numeric properties (min and maxvalue).
 * 
 * @author mtaal
 */

public class NumericPropertyValidator extends BasePropertyValidator {

  static boolean isValidationRequired(Property p) {
    if (p.isPrimitive()
        && (p.getPrimitiveType() == Float.class || p.getPrimitiveType() == BigDecimal.class
            || p.getPrimitiveType() == Integer.class || p.getPrimitiveType() == Long.class)) {
      if (p.getMinValue() != null || p.getMaxValue() != null) {
        return true;
      }
    }
    return false;
  }

  private BigDecimal minValue;
  private BigDecimal maxValue;

  public void initialize() {
    Check.isTrue(getProperty().getFieldLength() > 0,
        "Fieldlength should be larger than 0 for validation");
    if (getProperty().getMinValue() != null) {
      minValue = new BigDecimal(getProperty().getMinValue());
    }
    if (getProperty().getMaxValue() != null) {
      maxValue = new BigDecimal(getProperty().getMaxValue());
    }
  }

  @Override
  public String validate(Object value) {
    if (value == null) {
      // mandatory is checked in Hibernate and in the property itself
      return null;
    }
    final BigDecimal localValue;
    if (float.class.isAssignableFrom(value.getClass())
        || Float.class.isAssignableFrom(value.getClass())) {
      localValue = new BigDecimal((Float) value);
    } else if (int.class.isAssignableFrom(value.getClass())
        || Integer.class.isAssignableFrom(value.getClass())) {
      localValue = new BigDecimal((Integer) value);
    } else if (long.class.isAssignableFrom(value.getClass())
        || Long.class.isAssignableFrom(value.getClass())) {
      localValue = new BigDecimal((Long) value);
    } else {
      Check.isInstanceOf(value, BigDecimal.class);
      localValue = (BigDecimal) value;
    }

    if (minValue != null) {
      if (minValue.compareTo(localValue) > 0) {
        return "Value (" + value + ") is smaller than the min value: " + minValue;
      }
    }
    if (maxValue != null) {
      if (maxValue.compareTo(localValue) < 0) {
        return "Value (" + localValue + ") is larger than the max value: " + maxValue;
      }
    }
    return null;
  }
}
