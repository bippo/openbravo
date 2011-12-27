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
package org.openbravo.userinterface.selector;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.client.application.OBBindings;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * 
 * @author iperdomo
 */
@ApplicationScoped
public class SelectorDefaultFilterActionHandler extends BaseActionHandler {
  private Logger log = Logger.getLogger(SelectorDefaultFilterActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject result = new JSONObject();

    Map<String, String> params = getParameterMap(parameters);

    OBContext.setAdminMode();

    try {
      if (!params.containsKey("_selectorDefinitionId")) {
        return result;
      }

      String selectorId = params.get("_selectorDefinitionId");

      Selector sel = OBDal.getInstance().get(Selector.class, selectorId);
      OBCriteria<SelectorField> obc = OBDal.getInstance().createCriteria(SelectorField.class);
      obc.add(Restrictions.eq(SelectorField.PROPERTY_OBUISELSELECTOR, sel));
      obc.add(Restrictions.isNotNull(SelectorField.PROPERTY_DEFAULTEXPRESSION));

      if (obc.count() == 0) {
        return result;
      }

      final ScriptEngineManager manager = new ScriptEngineManager();
      final ScriptEngine engine = manager.getEngineByName("js");
      engine.put(
          "OB",
          new OBBindings(OBContext.getOBContext(), params, (HttpSession) parameters
              .get(KernelConstants.HTTP_SESSION)));

      Object exprResult = null;
      for (SelectorField f : obc.list()) {
        try {
          exprResult = engine.eval(f.getDefaultExpression());

          if (sel.isCustomQuery()) {
            result.put(f.getDisplayColumnAlias(), exprResult);
          } else {
            String fieldName = f.getProperty();
            result.put(fieldName, exprResult);
          }
        } catch (Exception e) {
          log.error(
              "Error evaluating expression for property " + f.getProperty()
                  + f.getDisplayColumnAlias() + ": " + e.getMessage(), e);
        }
      }
    } catch (Exception e) {
      log.error("Error generating Default Filter action result: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return result;
  }

  private Map<String, String> getParameterMap(Map<String, Object> parameters) {
    Map<String, String> params = new HashMap<String, String>();
    for (String key : parameters.keySet()) {
      if (key.equals(KernelConstants.HTTP_SESSION) || key.equals(KernelConstants.HTTP_REQUEST)) {
        continue;
      }
      params.put(key, (String) parameters.get(key));
    }
    return params;
  }
}
