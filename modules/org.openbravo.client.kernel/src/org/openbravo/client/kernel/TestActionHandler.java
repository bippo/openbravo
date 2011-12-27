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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

/**
 * A test implementation for the action handler.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class TestActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(TestActionHandler.class);

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    log.debug(">>>>>>>>>>>>>>> Received ActionHandler request ");
    log.debug("Parameters: ");
    for (String parameter : parameters.keySet()) {
      log.debug(parameter + ": " + parameters.get(parameter));
    }
    log.debug("Content: ");
    log.debug(data.toString());

    try {
      final JSONObject result = new JSONObject();
      result.put("test", "result");
      return result;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
