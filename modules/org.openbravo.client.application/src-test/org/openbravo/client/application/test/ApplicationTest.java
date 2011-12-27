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

import java.util.HashMap;

import javax.inject.Inject;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentGenerator;
import org.openbravo.client.kernel.ComponentProvider;

/**
 * 
 * @author iperdomo
 */
public class ApplicationTest extends WeldBaseTest {

  @Inject
  @ComponentProvider.Qualifier(ApplicationConstants.COMPONENT_TYPE)
  private ComponentProvider componentProvider;

  /**
   * Tests retrieving and generating the application JS.
   */
  @Test
  public void doTestMainLayoutComponentGeneration() throws Exception {
    setSystemAdministratorContext();

    final Component component = componentProvider.getComponent(ApplicationConstants.MAIN_LAYOUT_ID,
        new HashMap<String, Object>());

    final String output = ComponentGenerator.getInstance().generate(component);
    System.out.println(output);

  }
}
