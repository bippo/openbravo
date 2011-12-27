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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.session;

import java.io.Serializable;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.StringType;
import org.hibernate.type.descriptor.java.BooleanTypeDescriptor;
import org.hibernate.type.descriptor.sql.CharTypeDescriptor;

/**
 * Implements the same logic as the hibernate yesno type, handles null values as false. As certain
 * methods can not be extended the solution is to catch the isDirty check by reimplementing the
 * areEqual method.
 * 
 * @author mtaal
 */
public class OBYesNoType extends AbstractSingleColumnStandardBasicType<Boolean> implements
    PrimitiveType<Boolean>, DiscriminatorType<Boolean> {
  private static final long serialVersionUID = 1L;

  public OBYesNoType() {
    super(CharTypeDescriptor.INSTANCE, new LocalBooleanTypeDescriptor());
  }

  public String getName() {
    return "yes_no";
  }

  @SuppressWarnings("rawtypes")
  public Class getPrimitiveClass() {
    return boolean.class;
  }

  public Boolean stringToObject(String xml) throws Exception {
    return fromString(xml);
  }

  public Serializable getDefaultValue() {
    return Boolean.FALSE;
  }

  public String objectToSQLString(Boolean value, Dialect dialect) throws Exception {
    return StringType.INSTANCE.objectToSQLString(value.booleanValue() ? "Y" : "N", dialect);
  }

  private static class LocalBooleanTypeDescriptor extends BooleanTypeDescriptor {

    private static final long serialVersionUID = 1L;

    public boolean areEqual(Boolean x, Boolean y) {
      if (x == y) {
        return true;
      }
      if (x == null && y != null && y instanceof Boolean) {
        return ((Boolean) y).booleanValue() == false;
      } else if (y == null && x != null && x instanceof Boolean) {
        return ((Boolean) x).booleanValue() == false;
      }

      return super.areEqual(x, y);
    }
  }
}
