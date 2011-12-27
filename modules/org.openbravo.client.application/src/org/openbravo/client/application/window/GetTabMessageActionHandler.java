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
package org.openbravo.client.application.window;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.erpCommon.utility.OBError;

/**
 * Is used to get the message set in session for a tab
 * 
 */
@ApplicationScoped
public class GetTabMessageActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(GetTabMessageActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject result = new JSONObject();

    String tabId = "";
    RequestContext rc = RequestContext.get();
    try {
      tabId = new JSONObject(content).getString("tabId");
      final String attr = tabId + "|MESSAGE";
      final OBError msg = (OBError) rc.getSessionAttribute(attr);
      if (msg != null) {
        result.put("type", "TYPE_" + msg.getType().toUpperCase());
        result.put("title", msg.getTitle());
        result.put("text", msg.getMessage());
        rc.removeSessionAttribute(attr);
      }
    } catch (Exception e) {
      log.error("Error getting message for tab " + tabId, e);
    }
    return result;
  }
}
