/*
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2008-2010 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 */

package org.openbravo.base.expression;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.BaseOBObjectDef;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.util.Check;

/**
 * Evaluates expressions in the context of a business object, the expression language supported by
 * this class is javascript rhino.
 * 
 * @author mtaal
 */

public class Evaluator implements OBSingleton {
  private static final Logger log = Logger.getLogger(Evaluator.class);

  private static Evaluator instance = new Evaluator();

  public static synchronized Evaluator getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(Evaluator.class);
    }
    return instance;
  }

  public static synchronized void setInstance(Evaluator instance) {
    Evaluator.instance = instance;
  }

  /**
   * Evaluates the passed script in the context of the passed business object. This means that
   * properties of the business object may be used directly in the script. The result should always
   * be a boolean.
   * 
   * @param contextBob
   *          the script is executed in the context of this business object
   * @param script
   *          the javascript which much evaluate to a boolean
   * @return the result of the script evaluation
   */
  public Boolean evaluateBoolean(BaseOBObjectDef contextBob, String script) {
    // TODO: check if the compiled javascript can be cached

    log.debug("Evaluating script for " + contextBob + " script: " + script);

    try {
      // note that the name of a engine can differ: Mozilla Rhino
      // but js seems to work fine
      final ScriptEngineManager manager = new ScriptEngineManager();
      final ScriptEngine engine = manager.getEngineByName("js");
      Check
          .isNotNull(
              engine,
              "Scripting engine not found using name js, check for other scripting language names such as Mozilla Rhino");

      final Entity e = contextBob.getEntity();
      for (final Property p : e.getProperties()) {
        engine.put(p.getName(), contextBob.get(p.getName()));
      }

      final Object result = engine.eval(script);
      Check.isInstanceOf(result, Boolean.class);
      return (Boolean) result;
    } catch (final ScriptException e) {
      throw new OBException("Exception while executing " + script + " for business object "
          + contextBob, e);
    }
  }
}
