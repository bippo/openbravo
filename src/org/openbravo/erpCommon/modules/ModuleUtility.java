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
 * All portions are Copyright (C) 2008-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;

/**
 * Module related utilities needed by the 'ApplyModule' module class.
 * 
 * Before the 'ant apply.module' target is run a mini compilation is done to compile all classes
 * needed by that code (and not more). This ModuleUtility class was split out of the main 'ModuleUtiltiy'
 * class to not pull in the big chain of transitive compile-time dependencies into ApplyModule.
 * 
 * Nothing should be added here without reviewing the list of files compiled by the 'ant
 * compile.apply.module' target to check that it did not grow.
 * 
 */

/**
 * This class implements different utilities related to modules
 * 
 * 
 */
class ModuleUtility {
  protected static Logger log4j = Logger.getLogger(ModuleUtility.class);

  /**
   * It receives an ArrayList<String> with modules IDs and returns the same list ordered taking into
   * account the module dependency tree.
   * <p/>
   * Note that the module list must be a complete list of modules, no dependencies will be checked
   * for more than one level of deep, this means that passing an incomplete list might not be
   * ordered correctly.
   * 
   * @param modules
   *          List of module to order
   * @return modules list ordered
   * @throws Exception
   */
  public static List<String> orderByDependency(List<String> modules) throws Exception {

    Map<String, List<String>> modsWithDeps = getModsDeps(modules);
    List<String> rt = orderDependencies(modsWithDeps);
    return rt;
  }

  /**
   * Modifies the passed modules {@link FieldProvider} parameter ordering it taking into account
   * dependencies.
   * <p/>
   * 
   * @param modules
   *          {@link FieldProvider} that will be sorted. It must contain at least a field named
   *          <i>adModuleId</i>
   * @throws Exception
   */
  public static void orderModuleByDependency(FieldProvider[] modules) throws Exception {
    OBContext.setAdminMode();
    try {
      List<Module> allModules = OBDal.getInstance().createCriteria(Module.class).list();
      ArrayList<String> allMdoulesId = new ArrayList<String>();
      for (Module mod : allModules) {
        allMdoulesId.add(mod.getId());
      }
      List<String> modulesOrder = orderByDependency(allMdoulesId);

      FieldProvider[] fpModulesOrder = new FieldProvider[modules.length];
      int i = 0;
      for (String modId : modulesOrder) {
        for (int j = 0; j < modules.length; j++) {
          if (modules[j].getField("adModuleId").equals(modId)) {
            fpModulesOrder[i] = modules[j];
            i++;
          }
        }
      }

      for (int j = 0; j < modules.length; j++) {
        modules[j] = fpModulesOrder[j];
      }
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * Orders modules by dependencies. It adds to a List the modules that have not dependencies to the
   * ones in the list and calls itself recursively
   */
  private static List<String> orderDependencies(Map<String, List<String>> modsWithDeps)
      throws Exception {
    ArrayList<String> rt = new ArrayList<String>();

    for (String moduleId : modsWithDeps.keySet()) {
      if (noDependenciesFromModule(moduleId, modsWithDeps)) {
        rt.add(moduleId);
      }
    }

    for (String modId : rt) {
      modsWithDeps.remove(modId);
    }

    if (rt.size() == 0) {
      throw new Exception("Recursive module dependencies found!" + modsWithDeps.size());
    }

    if (modsWithDeps.size() != 0) {
      rt.addAll(orderDependencies(modsWithDeps));
    }
    return rt;
  }

  /**
   * Checks the module has not dependencies to other modules in the list
   * 
   */
  private static boolean noDependenciesFromModule(String checkModule,
      Map<String, List<String>> modsWithDeps) {

    List<String> moduleDependencies = modsWithDeps.get(checkModule);

    for (String module : modsWithDeps.keySet()) {
      if (moduleDependencies.contains(module)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns a Map with all the modules and their dependencies
   */
  private static Map<String, List<String>> getModsDeps(List<String> modules) {
    Map<String, List<String>> rt = new HashMap<String, List<String>>();
    for (String moduleId : modules) {
      Module module = OBDal.getInstance().get(Module.class, moduleId);
      ArrayList<String> deps = new ArrayList<String>();
      for (ModuleDependency dep : module.getModuleDependencyList()) {
        deps.add(dep.getDependentModule().getId());
      }
      rt.put(moduleId, deps);
    }
    return rt;
  }

}
