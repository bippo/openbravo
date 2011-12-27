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
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.json.DefaultJsonDataService;
import org.openbravo.service.json.JsonConstants;

/**
 * Computes how many children there in the child tabs when a record gets selected.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class ChildTabRecordCounterActionHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {

    try {
      OBContext.setAdminMode();
      final JSONObject dataObject = new JSONObject(data);
      final String parentId = dataObject.getString("parentId");
      final Map<String, String> qryParameters = new HashMap<String, String>();
      for (String key : parameters.keySet()) {
        final Object value = parameters.get(key);
        if (value instanceof String) {
          qryParameters.put(key, (String) value);
        }
      }
      qryParameters.put(JsonConstants.USE_ALIAS, "true");
      qryParameters.put(JsonConstants.ONLYCOUNT_PARAMETER, "true");
      qryParameters.put(JsonConstants.NOCOUNT_PARAMETER, "false");
      qryParameters.put(JsonConstants.STARTROW_PARAMETER, "0");
      qryParameters.put(JsonConstants.ENDROW_PARAMETER, "" + Integer.MAX_VALUE);
      qryParameters.put(JsonConstants.NO_ACTIVE_FILTER, "true");

      final JSONArray childTabList = dataObject.getJSONArray("tabs");
      final List<JSONObject> result = new ArrayList<JSONObject>();
      for (int i = 0; i < childTabList.length(); i++) {
        final JSONObject childTab = childTabList.getJSONObject(i);
        final String entityName = childTab.getString("entity");
        qryParameters.put(JsonConstants.ENTITYNAME, entityName);

        final String parentProperty = childTab.getString("parentProperty");
        qryParameters.put(parentProperty, parentId);
        if (childTab.has("whereClause")) {
          qryParameters.put(JsonConstants.WHERE_PARAMETER, childTab.getString("whereClause"));
        } else {
          qryParameters.remove(JsonConstants.WHERE_PARAMETER);
        }
        final String jsonResultStr = DefaultJsonDataService.getInstance().fetch(qryParameters);
        final JSONObject jsonResult = new JSONObject(jsonResultStr);

        final JSONObject tabResult = new JSONObject();
        tabResult.put("tabId", childTab.getString("tabId"));
        tabResult.put("count", jsonResult.getInt(JsonConstants.RESPONSE_TOTALROWS));
        result.add(tabResult);
      }
      final JSONObject resultObject = new JSONObject();
      resultObject.put("result", new JSONArray(result));
      return resultObject;
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
