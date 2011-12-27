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
package org.openbravo.client.application.test;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.application.window.StandardWindowComponent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;

/**
 * Tests generation of the javascript for standard windows
 * 
 * @author iperdomo
 */
public class StandardWindowTest extends WeldBaseTest {

  /**
   * Tests generating the javascript for all windows, printing one of them.
   */
  @Test
  public void testStandardViewGeneration() throws Exception {
    setSystemAdministratorContext();

    for (Window window : OBDal.getInstance().createQuery(Window.class, "").list()) {
      if (hasAtLeastOneActiveTab(window)) {
        System.err.println(window.getName());
        try {
          generateForWindow(window);
        } catch (Throwable t) {
          System.err.println("ERROR for window " + window.getName() + " " + window.getId());
          throw new Error(t);
        }
      }
    }
  }

  /**
   * Tests generating the javascript for one window to analyze problems.
   */
  public void _testOneStandardViewGeneration() throws Exception {
    setSystemAdministratorContext();
    generateForWindow(OBDal.getInstance().get(Window.class, "1005400002"));
  }

  private void generateForWindow(Window window) {
    final StandardWindowComponent component = super.getWeldComponent(StandardWindowComponent.class);
    component.setWindow(window);
    final String jsCode = component.generate();
    if (window.getId().equals("102")) {
      System.err.println(jsCode);
    }
  }

  private boolean hasAtLeastOneActiveTab(Window window) {
    for (Tab tab : window.getADTabList()) {
      if (tab.isActive()) {
        return true;
      }
    }
    return false;
  }
}
