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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the enum ui definition.
 * 
 * @author mtaal
 */
public class EnumUIDefinition extends UIDefinition {

  @Override
  public String getParentType() {
    return "enum";
  }

  @Override
  public String getFormEditorType() {
    return "OBListItem";
  }

  @Override
  public String getFilterEditorType() {
    return "OBListFilterItem";
  }

  @Override
  public String getGridFieldProperties(Field field) {
    Long length = field.getColumn().getLength();

    Long displaylength = field.getDisplayedLength();
    if (displaylength == null || displaylength == 0) {
      displaylength = length;
    }

    // custom override
    if (field.getColumn().getDBColumnName().compareToIgnoreCase("documentno") == 0) {
      length = new Long(20);
    }
    return getShowHoverGridFieldSettings(field) + ", length:" + length + ", displaylength:"
        + displaylength + super.getGridFieldProperties(field);
  }

  @Override
  public String getFieldProperties(Field field, boolean getValueFromSession) {
    JSONObject value;
    try {
      value = new JSONObject(super.getFieldProperties(field, getValueFromSession));
      if (!getValueFromSession
          && ((String) DalUtil.getId(field.getColumn().getReference())).equals("28")
          && !value.has("value")) {
        // When reference is button, set 'N' as default if there is default
        value.put("value", "N");
        value.put("classicValue", "N");
      }
      return getValueInComboReference(field, getValueFromSession,
          value.has("classicValue") ? value.getString("classicValue") : "");
    } catch (JSONException e) {
      throw new OBException("Error while computing combo data", e);
    }
  }

  @Override
  public String getFieldPropertiesWithoutCombo(Field field, boolean getValueFromSession) {
    try {
      JSONObject value = new JSONObject(super.getFieldProperties(field, getValueFromSession));
      if (!getValueFromSession
          && ((String) DalUtil.getId(field.getColumn().getReference())).equals("28")
          && !value.has("value")) {
        // When reference is button, set 'N' as default if there is default
        value.put("value", "N");
        value.put("classicValue", "N");
      }
      return value.toString();
    } catch (JSONException ex) {
      throw new OBException("Error while computing combo data", ex);
    }
  }

  @Override
  public String getFilterEditorProperties(Field field) {
    return ", filterOnKeypress: false" + super.getFilterEditorProperties(field);
  }

}
