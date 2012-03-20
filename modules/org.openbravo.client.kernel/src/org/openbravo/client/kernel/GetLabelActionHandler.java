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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;

/**
 * Retrieves a label from the server.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class GetLabelActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(GetLabelActionHandler.class);

  private static final String KEY_PARAMETER = "key";
  private static final String LABEL_PROPERTY = "label";

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    if (!parameters.containsKey(KEY_PARAMETER)) {
      throw new OBException("Request parameter " + KEY_PARAMETER + " is mandatory");
    }
    final String key = (String) parameters.get(KEY_PARAMETER);
    final JSONObject result = new JSONObject();
    OBContext.setAdminMode();
    try {
      final String label = KernelUtils.getInstance().getI18N(key, null);
      if (label == null) {
        // not found, will result in a strange label on the client
        return result;
      }
      result.put(LABEL_PROPERTY, label);
    } catch (Exception e) {
      throw new OBException("Exception when getting message for key: " + key, e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }
}
