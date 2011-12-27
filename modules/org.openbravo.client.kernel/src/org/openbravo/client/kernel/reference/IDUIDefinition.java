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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.service.json.JsonConstants;

/**
 * Used to visualize ID fields. See the child tabs in the Business Partner Info field.
 * 
 * @author mtaal
 */
public class IDUIDefinition extends ForeignKeyUIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBTextItem";
  }

  @Override
  public String getFilterEditorType() {
    return "OBTextItem";
  }

  protected String getDisplayFieldName(Field field, Property prop) {
    return JsonConstants.IDENTIFIER;
  }

  @Override
  public String getFieldProperties(Field field) {
    final String superJsonStr = super.getFieldProperties(field);
    if (field == null) {
      return superJsonStr;
    }
    try {
      final JSONObject json = new JSONObject(
          superJsonStr != null && superJsonStr.startsWith("{") ? superJsonStr : "{}");
      json.put("displayField", JsonConstants.IDENTIFIER);
      return json.toString();
    } catch (JSONException e) {
      throw new OBException("Exception when generating field properties for " + field, e);
    }
  }

}
