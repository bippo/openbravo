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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the YesNo/Boolean ui definition.
 * 
 * @author mtaal
 */
public class YesNoUIDefinition extends UIDefinition {

  @Override
  public String getParentType() {
    return "boolean";
  }

  @Override
  public String getFilterEditorType() {
    return "OBYesNoItem";
  }

  @Override
  public String getFormEditorType() {
    return "OBCheckboxItem";
  }

  @Override
  public String getFieldProperties(Field field) {
    try {
      // prevent wide click area on a yes/no item, if the below is not done
      // then there is a large white area which is clickable and sets the yes/no
      // field
      final String superJsonStr = super.getFieldProperties(field);
      final JSONObject json = new JSONObject(
          superJsonStr != null && superJsonStr.startsWith("{") ? superJsonStr : "{}");
      json.put("width", 1);
      json.put("overflow", "visible");
      return json.toString();
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  @Override
  public String getTypeProperties() {
    return "    valueMap: [null, true, false],"
        + "shortDisplayFormatter: function(value, field, component, record) {"
        + "return OB.Utilities.getYesNoDisplayValue(value);" + "},  "
        + "createClassicString: function(value) {return OB.Utilities.getClassicValue(value);},"
        + "normalDisplayFormatter: function(value, field, component, record) {"
        + "return OB.Utilities.getYesNoDisplayValue(value);" + "},  ";
  }

  @Override
  public String getGridEditorFieldProperties(Field field) {
    final String superGridEditorProps = super.getGridEditorFieldProperties(field);
    if (superGridEditorProps.length() > 0) {
      try {
        final JSONObject json = new JSONObject(superGridEditorProps);
        json.put("showTitle", false);
        json.put("showLabel", false);
        return json.toString();
      } catch (Exception e) {
        throw new OBException(e);
      }
    } else {
      return "{showTitle: false, showLabel: false}";
    }
  }

  @Override
  public String getGridFieldProperties(Field field) {
    return super.getGridFieldProperties(field) + ", width: '*', autoFitWidth: false, yesNo: true";
  }

  @Override
  public String convertToClassicString(Object value) {
    if (value instanceof Boolean) {
      if ((Boolean) value) {
        return "Y";
      }
    }
    return "N";
  }

  @Override
  public String getFieldProperties(Field field, boolean getValueFromSession) {
    String result = super.getFieldProperties(field, getValueFromSession);
    try {
      JSONObject jsnobject = new JSONObject(result);
      if (!getValueFromSession && field.getColumn().getDefaultValue() == null
          && (!jsnobject.has("value") || jsnobject.get("value").equals(""))) {
        jsnobject.put("value", createFromClassicString("N"));
        jsnobject.put("classicValue", "N");
        return jsnobject.toString();
      }
      return result;
    } catch (JSONException e) {
      throw new OBException("Error while parsing boolean value", e);
    }
  }
}
