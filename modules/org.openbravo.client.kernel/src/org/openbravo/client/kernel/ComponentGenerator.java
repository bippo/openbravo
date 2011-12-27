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
package org.openbravo.client.kernel;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;

/**
 * Uses a component to generate the component output and then postprocesses this output.
 * 
 * @see Component#generate()
 * @see JSLintChecker
 * @see JSCompressor
 * @author mtaal
 */
public class ComponentGenerator {
  private static final Logger log = Logger.getLogger(ComponentGenerator.class);

  private static ComponentGenerator instance = new ComponentGenerator();

  public static synchronized ComponentGenerator getInstance() {
    if (instance == null) {
      return new ComponentGenerator();
    }
    return instance;
  }

  public static synchronized void setInstance(ComponentGenerator instance) {
    ComponentGenerator.instance = instance;
  }

  /**
   * Calls {@link Component#generate()} and optionally compresses the output and optionally checks
   * the syntax.
   * 
   * Compressing is done if the module of the component ({@link Component#getModule()}) is not in
   * development.
   * 
   * Syntax checking is done if the module of the component ({@link Component#getModule()}) is in
   * development.
   * 
   * @param component
   *          the component to generate javascript for
   * @return generated and compressed javascript.
   */
  public String generate(Component component) {
    OBContext.setAdminMode();
    try {
      final String originalResult = component.generate();
      if (component.isJavaScriptComponent() && component.isInDevelopment()) {
        if (originalResult.contains(KernelConstants.JSLINT_DIRECTIVE)) {
          final String errors = JSLintChecker.getInstance().check(
              component.getModule().getName() + "." + component.getId(), originalResult);
          if (errors != null) {
            log.error("Error parsing component "
                + (component.getId() != null ? component.getId() : "") + "\n" + errors);
          }
        }
      }
      final String compressedResult;
      if (component.isJavaScriptComponent() && !component.isInDevelopment()) {
        compressedResult = JSCompressor.getInstance().compress(originalResult);
      } else {
        compressedResult = originalResult;
      }
      return compressedResult;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
