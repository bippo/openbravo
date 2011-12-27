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

import java.util.HashMap;
import java.util.Map;

import org.openbravo.data.FieldProvider;

/**
 * A simple {@link FieldProvider} which has a map as backing store.
 * 
 * @author mtaal
 */
public class SimpleFieldProvider implements FieldProvider {

  private final Map<String, String> properties = new HashMap<String, String>();

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.data.FieldProvider#getField(java.lang.String)
   */
  public String getField(String fieldName) {
    return properties.get(fieldName);
  }

  /**
   * Set the value of the field.
   * 
   * @param name
   *          the name of the field
   * @param value
   *          the value
   */
  public void setField(String name, String value) {
    properties.put(name, value);
  }

  /**
   * Set all the field values of the passed map, current values are overwritten if they have the
   * same name.
   * 
   * @param fieldValues
   *          the map with name-value pairs to set in this FieldProvider.
   */
  public void setAll(Map<String, String> fieldValues) {
    properties.putAll(fieldValues);
  }

}
