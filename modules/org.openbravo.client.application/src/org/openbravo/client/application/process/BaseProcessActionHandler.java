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
package org.openbravo.client.application.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.util.Check;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.Process;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

/**
 * 
 * @author iperdomo
 */
public abstract class BaseProcessActionHandler extends BaseActionHandler {

  private static final Logger log = Logger.getLogger(BaseProcessActionHandler.class);

  @Override
  protected final JSONObject execute(Map<String, Object> parameters, String content) {

    try {
      OBContext.setAdminMode();

      final String processId = (String) parameters.get("processId");
      Check.isNotNull(processId, "Process ID missing in request");

      final Process processDefinition = OBDal.getInstance().get(Process.class, processId);
      Check.isNotNull(processDefinition, "Not valid process id");

      for (Parameter param : processDefinition.getOBUIAPPParameterList()) {
        if (param.isFixed() && param.isEvaluateFixedValue()) {
          parameters.put(param.getDBColumnName(),
              ParameterUtils.getParameterFixedValue(fixRequestMap(parameters), param));
        }
      }

      return doExecute(parameters, content);

    } catch (Exception e) {
      log.error("Error trying to execute process request: " + e.getMessage(), e);
      return new JSONObject();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /*
   * The request map is <String, Object> because includes the HTTP request and HTTP session, is not
   * required to handle process parameters
   */
  private Map<String, String> fixRequestMap(Map<String, Object> parameters) {
    final Map<String, String> retval = new HashMap<String, String>();
    for (Entry<String, Object> entries : parameters.entrySet()) {
      if (entries.getKey().equals(KernelConstants.HTTP_REQUEST)
          || entries.getKey().equals(KernelConstants.HTTP_SESSION)) {
        continue;
      }
      retval.put(entries.getKey(), entries.getValue().toString());
    }
    return new HashMap<String, String>();
  }

  protected abstract JSONObject doExecute(Map<String, Object> parameters, String content);
}
