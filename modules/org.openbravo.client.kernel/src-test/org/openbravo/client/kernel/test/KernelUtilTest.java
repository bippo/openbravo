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

package org.openbravo.client.kernel.test;

import java.util.List;

import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.test.base.BaseTest;

/**
 * Test the {@link KernelUtils} class.
 * 
 * @author mtaal
 */

public class KernelUtilTest extends BaseTest {

  private static void initializeStatics() {
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // after super.setUp, must be done after initializing dal layer in super class
    initializeStatics();
  }

  /**
   * Tests {@link KernelUtils#getModulesOrderedByDependency()}.
   */
  public void testModulesOrderByDependency() throws Exception {
    setSystemAdministratorContext();
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();

    // do a quick test
    // a module may not be in the dependency list of any module later in the list
    boolean testDone = false;
    for (int i = 0; i < modules.size(); i++) {
      final Module mainModule = modules.get(i);
      for (int j = i + 1; j < modules.size(); j++) {
        final Module otherModule = modules.get(j);
        for (ModuleDependency dependency : mainModule.getModuleDependencyList()) {
          if (dependency.getDependentModule() == otherModule) {
            fail("Module " + otherModule + " in dependency list of " + mainModule);
          }
        }
      }
      testDone = true;
    }
    assertTrue(testDone);
  }

  /**
   * Test generation of the version id number {@link KernelUtils#getVersionString()}.
   * 
   * @throws Exception
   */
  public void testVersionId() throws Exception {
    setSystemAdministratorContext();
    try {
      // set at least one module in development mode
      final Module module = OBDal.getInstance().createCriteria(Module.class).list().get(0);
      module.setInDevelopment(true);
      OBDal.getInstance().flush();

      final String version1 = KernelUtils.getInstance().getVersionParameters(module);
      for (Module devModule : OBDal.getInstance().createCriteria(Module.class).list()) {
        devModule.setInDevelopment(false);
      }
      OBDal.getInstance().flush();
      final String version2 = KernelUtils.getInstance().getVersionParameters(module);
      final String version3 = KernelUtils.getInstance().getVersionParameters(module);
      assertTrue(!version1.equals(version2));
      assertTrue(version2.equals(version3));
    } finally {
      OBDal.getInstance().rollbackAndClose();
    }
  }
}