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
package org.openbravo.client.myob.widgetinform;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.myob.WidgetClass;
import org.openbravo.client.myob.WidgetReference;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the Widget in Form UIdefinition
 * 
 * @author huehner
 * 
 */
public class WidgetInFormUIDefinition extends UIDefinition {

  private static Logger widgetLog = Logger.getLogger(WidgetInFormUIDefinition.class);

  @Override
  public String getFormEditorType() {
    return "OBWidgetInFormItem";
  }

  @Override
  public String getFieldProperties(Field field) {
    String fieldProperties = super.getFieldProperties(field);
    if (field == null) {
      return fieldProperties;
    }

    // get widgetClass from reference
    WidgetReference wr = OBDal.getInstance().get(WidgetReference.class, getReference().getId());
    WidgetClass widgetClass = wr.getWidgetClass();

    try {
      JSONObject o = (fieldProperties == null || fieldProperties.trim().length() == 0) ? new JSONObject()
          : new JSONObject(fieldProperties);
      o.put("widgetClassId", widgetClass.getId());
      o.put("showTitle", wr.isShowFieldTitle());
      return o.toString();
    } catch (JSONException e) {
      // be robust
      widgetLog.error(e.getMessage(), e);
      return fieldProperties;
    }
  }

}
