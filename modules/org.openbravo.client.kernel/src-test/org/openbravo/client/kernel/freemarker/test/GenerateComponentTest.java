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

package org.openbravo.client.kernel.freemarker.test;

import java.util.HashMap;

import javax.inject.Inject;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentGenerator;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelComponentProvider;
import org.openbravo.client.kernel.KernelConstants;

/**
 * Test the generation of several kernel components.
 * 
 * @author mtaal
 */

public class GenerateComponentTest extends WeldBaseTest {

  @Inject
  @ComponentProvider.Qualifier(KernelComponentProvider.QUALIFIER)
  private ComponentProvider kernelComponentProvider;

  @Test
  public void testApplication() throws Exception {
    generateComponent(KernelConstants.APPLICATION_COMPONENT_ID);
  }

  @Test
  public void testStaticResources() throws Exception {
    generateComponent(KernelConstants.RESOURCE_COMPONENT_ID);
  }

  @Test
  public void testLabels() throws Exception {
    generateComponent(KernelConstants.LABELS_COMPONENT_ID);
  }

  protected void generateComponent(String componentID) {
    final Component component = kernelComponentProvider.getComponent(componentID,
        new HashMap<String, Object>());

    final String output = ComponentGenerator.getInstance().generate(component);
    System.err.println(output);
  }

}