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

import java.math.BigDecimal;

import org.openbravo.base.model.Property;
import org.openbravo.base.validation.ValidationException;

/**
 * The type for an integer/long column.
 * 
 * @author mtaal
 */

public class LongDomainType extends BasePrimitiveDomainType {

  /**
   * @return class of the {@link Long}
   */
  public Class<?> getPrimitiveType() {
    return Long.class;
  }

  public void checkIsValidValue(Property property, Object value) throws ValidationException {
    if (value == null) {
      return;
    }
    if (Integer.class.isInstance(value)) {
      // is allowed
      return;
    }
    super.checkIsValidValue(property, value);
  }

  public String getFormatId() {
    return "integer";
  }

  @Override
  public Object createFromString(String strValue) {
    if (strValue == null || strValue.trim().length() == 0) {
      return null;
    }
    return new Long(new BigDecimal(strValue).longValueExact());
  }

  @Override
  public String getXMLSchemaType() {
    return "ob:long";
  }

}
