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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.ad.ui.Field;

public class PAttributeSearchUIDefinition extends FKSearchUIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBPAttributeSearchItem";
  }

  @Override
  public String getFieldProperties(Field field, boolean getValueFromSession) {
    // TODO: This is hack to remove the default attribute set 0 in grid view, it should be removed
    // when the strategy of display logic is defined for grid view
    String fieldProperties = super.getFieldProperties(field, getValueFromSession);
    try {
      JSONObject o = new JSONObject(fieldProperties);
      if (o.get("value") != null && o.get("value").equals("0")) {
        o.put("value", "");
      }
      return o.toString();
    } catch (Exception e) {
      log.error("Error trying to modify JSON object: " + fieldProperties, e);
    }
    return fieldProperties;
  }
}
