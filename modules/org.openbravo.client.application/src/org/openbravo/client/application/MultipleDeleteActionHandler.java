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
package org.openbravo.client.application;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonUtils;

/**
 * Action handler which can delete multiple records in one transaction.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class MultipleDeleteActionHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {

    try {
      final JSONObject dataObject = new JSONObject(data);
      final String entityName = dataObject.getString("entity");
      final JSONArray ids = dataObject.getJSONArray("ids");
      for (int i = 0; i < ids.length(); i++) {
        final BaseOBObject object = OBDal.getInstance().get(entityName, (String) ids.get(i));
        if (object != null) {
          OBDal.getInstance().remove(object);
        }
      }
      OBDal.getInstance().commitAndClose();

      // just return an empty message, as the system knows how many have been deleted.
      return new JSONObject();
    } catch (Exception e) {
      try {
        return new JSONObject(JsonUtils.convertExceptionToJson(e));
      } catch (JSONException t) {
        throw new OBException(t);
      }
    }
  }
}
