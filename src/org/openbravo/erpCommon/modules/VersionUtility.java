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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.openbravo.base.ConnectionProviderContextListener;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.module.ModuleMerge;
import org.openbravo.services.webservice.Module;
import org.openbravo.services.webservice.ModuleDependency;
import org.openbravo.services.webservice.ModuleInstallDetail;
import org.openbravo.services.webservice.WebService3Impl;
import org.openbravo.services.webservice.WebService3ImplServiceLocator;

public class VersionUtility {
  protected static ConnectionProvider pool;
  static Logger log4j = Logger.getLogger(VersionUtility.class);

  private static class Mod {
    String modId;
    String name;
    String availableMinVer;
    String availableMaxVer;
    String type;
    HashMap<String, Ver> versions;
  }

  private static class Ver {
    String verId;
    String version;
    HashMap<String, Dep> dependencies;
    HashMap<String, Dep> includes;
  }

  private static class Dep {
    String depId;
    String version;
    String minVer;
    String maxVer;
    String modId;
    String modName;
    String enforcement;
  }

  public VersionUtility() {
    initPool();
  }

  static private void initPool() {
    if (log4j.isDebugEnabled())
      log4j.debug("init");
    try {
      HttpServlet srv = (HttpServlet) MessageContext.getCurrentContext().getProperty(
          HTTPConstants.MC_HTTP_SERVLET);
      ServletContext context = srv.getServletContext();
      pool = ConnectionProviderContextListener.getPool(context);
    } catch (Exception e) {
      log4j.error("Error : initPool");
      log4j.error(e.getStackTrace());
    }
  }

  static private boolean checkVersion(String depParentMod, Dep dep, Mod mod, Vector<String> errors) {
    String strModName = dep.modName != null ? dep.modName : "< @CR_NameNotAvailable@ >";
    if ("MINOR".equals(dep.enforcement)) {
      if (dep.maxVer == null || dep.maxVer.isEmpty()) {
        if (versionCompare(dep.minVer, mod.availableMaxVer, false) != 0) {
          errors.add(depParentMod + " @CR_DependensOnModule@ \"" + strModName
              + "\" @CR_InVersion@ \"" + dep.minVer + "\", @CR_MaxAvailableVersion@ \""
              + mod.availableMaxVer + "\". ");
          return false;
        }
      } else {
        if (versionCompare(dep.minVer, mod.availableMaxVer, false) == 1) {
          errors.add(depParentMod + " @CR_DependensOnModule@ \"" + strModName
              + "\" @CR_InVersion@ \"" + dep.minVer + "\", @CR_MaxAvailableVersion@ \""
              + mod.availableMaxVer + "\". ");
          return false;
        }
        if (versionCompare(mod.availableMinVer, dep.maxVer, false) == 1) {
          errors.add(depParentMod + " @CR_DependensOnModule@ \"" + strModName
              + "\" @CR_InVersion@ \"" + dep.maxVer + "\", @CR_MaxAvailableVersion@ \""
              + mod.availableMinVer + "\". ");
          return false;
        }
      }
    } else if ("NONE".equals(dep.enforcement)) {
      if (versionCompare(dep.minVer, mod.availableMaxVer, false) == 1) {
        errors.add(depParentMod + " @CR_DependensOnModule@ \"" + strModName
            + "\" @CR_InVersion@ \"" + dep.minVer + "\", @CR_MaxAvailableVersion@ \""
            + mod.availableMaxVer + "\". ");
        return false;
      }
    } else if ("MAJOR".equals(dep.enforcement)) {
      // installedVersion <= dep.fromVersion
      if (versionCompare(dep.minVer, mod.availableMaxVer, true) == 1) {
        errors.add(depParentMod + " @CR_DependensOnModule@ \"" + strModName
            + "\" @CR_InVersion@ \"" + dep.minVer + "\", @CR_MaxAvailableVersion@ \""
            + mod.availableMaxVer + "\". ");
        return false;
      }
      if (dep.maxVer != null && !dep.maxVer.isEmpty()) {
        // if lastVersion!=null, firstVersion >= installedVersion >= lastVersion
        if (versionCompare(dep.minVer, dep.maxVer, false) == 1
            && versionCompare(mod.availableMinVer, dep.maxVer, true) == 1) {
          errors.add(depParentMod + " @CR_DependensOnModule@ \"" + strModName
              + "\" @CR_InVersion@ \"" + dep.maxVer + "\", @CR_MaxAvailableVersion@ \""
              + mod.availableMinVer + "\". ");
          return false;
        }
      } else {
        // if lastVerrsion==null, firtVersion same major version than installedVersion
        if (versionCompare(dep.minVer, mod.availableMaxVer, true) != 0) {
          errors.add(depParentMod + " @CR_DependensOnModule@ \"" + strModName
              + "\" @CR_InVersion@ \"" + dep.minVer + "\", @CR_ButOnlyVersion@ \""
              + mod.availableMaxVer + "\" @CR_IsAvailable@. ");
          return false;
        }
      }
    }
    return true;
  }

  static private boolean checkDependency(String strModVersion, HashMap<String, Mod> modulesMap,
      HashMap<String, Mod> modsToInstall, HashMap<String, Mod> modsToUpdate,
      Module[] modulesToMerge, Dep dependency, Vector<String> errors) throws Exception {
    boolean foundModule = false;

    Mod mod = null;
    foundModule = true;
    if (modulesMap != null && modulesMap.containsKey(dependency.modId)) {
      mod = modulesMap.get(dependency.modId);
    } else if (modsToInstall != null && modsToInstall.containsKey(dependency.modId)) {
      mod = modsToInstall.get(dependency.modId);
    } else if (modsToUpdate != null && modsToUpdate.containsKey(dependency.modId)) {
      mod = modsToUpdate.get(dependency.modId);
    } else {
      foundModule = false;
    }

    if (foundModule && checkVersion(strModVersion, dependency, mod, errors)) {
      return true;
    }
    if (!foundModule) {
      // Module is not found, check whether it is merged
      boolean merged = false;
      if (modulesToMerge != null) {
        for (Module merge : modulesToMerge) {
          if (dependency.modId.equals(merge.getModuleID())) {
            merged = true;
            errors.add(merge.getName() + " @CR_IsMergedBy@ "
                + merge.getAdditionalInfo().get("mergedWith") + " @CR_MergeCannotUninstall@ "
                + strModVersion);
          }
        }
      }
      if (!merged && modulesMap.get(dependency.modId) == null) {
        // Appending not installed error message when module is not present
        errors.add(strModVersion + " @CR_DependensOnModule@ \"" + dependency.modName
            + "\", @CR_ModuleNotInstalled@");
      }
    }

    return false;
  }

  static private boolean checkVersionDependency(String strModVersion,
      HashMap<String, Mod> modulesMap, HashMap<String, Mod> modsToInstall,
      HashMap<String, Mod> modsToUpdate, Module[] modulesToMerge, Ver version, Vector<String> errors)
      throws Exception {
    boolean checked = true;
    HashMap<String, Dep> depMap = version.dependencies;
    depMap.putAll(version.includes);

    /** Navigate through dependencies and includes of the module version */
    for (String depKey : depMap.keySet()) {
      /**
       * Check if in the whole modules versions map there is the module version that satisfies the
       * dependency and includes If no available configuration is found an exception is throw. If
       * new modules or new updates are needed, they are added to they correspondent list and added
       * to errors vector. and the checked is run again with the new configuration
       */
      if (!checkDependency(strModVersion, modulesMap, modsToInstall, modsToUpdate, modulesToMerge,
          depMap.get(depKey), errors)) {
        // If any dependency or include need a new module, it is added
        // to modsToInstall or if needed an update is added to
        // modsToUpdate
        checked = false;
      }
    }
    return checked;
  }

  static private boolean checkAllDependencies(HashMap<String, Mod> modulesMap,
      HashMap<String, Mod> modsToInstall, HashMap<String, Mod> modsToUpdate,
      Module[] modulesToMerge, Vector<String> errors) throws Exception {
    // New hashmap of installed modules less the modules that will be
    // updated
    HashMap<String, Mod> modulesInstalledLessToUpdate = new HashMap<String, Mod>();
    HashMap<String, Mod> modsForCheckDependencies = new HashMap<String, Mod>();
    modulesInstalledLessToUpdate.putAll(modulesMap);
    for (String modUpKey : modsToUpdate.keySet())
      modulesInstalledLessToUpdate.remove(modUpKey);

    boolean checked = true;

    modsForCheckDependencies.putAll(modulesInstalledLessToUpdate);
    modsForCheckDependencies.putAll(modsToInstall);
    modsForCheckDependencies.putAll(modsToUpdate);
    Vector<String> modKey = new Vector<String>(modsForCheckDependencies.keySet());
    /** Navigate through modules */
    for (int i = 0; i < modKey.size(); i++) {
      Mod mod = modsForCheckDependencies.get(modKey.elementAt(i));
      HashMap<String, Ver> verMap = mod.versions;
      /** Navigate through modules versions */
      for (String verKey : verMap.keySet()) {
        Ver ver = verMap.get(verKey);
        String strModVersion = mod.name + "-" + ver.version;
        if (!checkVersionDependency(strModVersion, modulesInstalledLessToUpdate, modsToInstall,
            modsToUpdate, modulesToMerge, ver, errors)) {
          /**
           * When any dependency fails, the process continue to found all dependency errors, but the
           * configuration is marked as no valid
           */
          checked = false;
        }
      }
    }

    // Check modules to install are not merged
    if (modsToInstall != null) {
      List<String> modsIdMerged = new ArrayList<String>();
      if (modulesToMerge != null) {
        for (Module merge : modulesToMerge) {
          modsIdMerged.add(merge.getModuleID());
        }
      }
      for (ModuleMerge merge : OBDal.getInstance().createCriteria(ModuleMerge.class).list()) {
        modsIdMerged.add(merge.getMergedModuleUUID());
      }

      for (Mod module : modsToInstall.values()) {
        if (modsIdMerged.contains(module.modId)) {
          errors.add(module.name + " @CannotInstallMerged@");
          checked = false;
        }
      }

    }

    return checked;
  }

  /**
   * Transform a String array into a comma separated String
   * 
   * Example ["aaa", "bbb", "ccc"] -> "aaa, bbb, cccc"
   * 
   * @param arr
   *          A String Array
   * @return The comma separated String
   */
  static public String toCommaString(String[] arr) {
    return toCommaString(arr, false);
  }

  static public String toCommaString(String[] arr, boolean parenthesys) {
    if (arr == null || arr.length == 0)
      return null;
    StringBuffer modNames = new StringBuffer(" ");
    if (parenthesys)
      modNames.append("( ");
    for (int i = 0; i < arr.length; i++) {
      if (i != 0)
        modNames.append(", ");
      modNames.append("'").append(arr[i]).append("'");
    }
    if (parenthesys)
      modNames.append(" ) ");
    return modNames.toString();
  }

  /**
   * fill Mod objects (include its versions and dependencies from versions) from database for all
   * Modules IDs excluding the ones to be merged
   */
  static private HashMap<String, Mod> fillModules(Module[] modulesToMerge) throws ServletException {
    VersionUtilityData[] data = VersionUtilityData.readModules(pool);
    HashMap<String, Mod> modules = new HashMap<String, Mod>();
    for (int i = 0; i < data.length; i++) {
      if (!isInList(data[i].adModuleId, modulesToMerge) && !modules.containsKey(data[i].adModuleId)) {
        Mod mod = new Mod();
        mod.modId = data[i].adModuleId;
        mod.name = data[i].name;
        mod.type = data[i].type;
        mod.versions = fillVersions(data[i], mod, "Y".equals(data[i].installed));
        modules.put(data[i].adModuleId, mod);
      }
    }
    return modules;
  }

  /**
   * Checks whether a module id is in a list of modules
   */
  private static boolean isInList(String moduleId, Module[] modules) {
    if (modules == null) {
      return false;
    }
    for (Module mod : modules) {
      if (moduleId.equals(mod.getModuleID())) {
        return true;
      }
    }
    return false;
  }

  static private HashMap<String, Ver> fillVersions(VersionUtilityData data, Mod mod,
      boolean installed) throws ServletException {
    /** fill Ver objects from database */
    /**
     * all information needed for modules are stored in ad_module, ad_module_dependency
     */

    HashMap<String, Ver> hashVer = new HashMap<String, Ver>();
    if (data != null) {
      Ver ver = new Ver();
      ver.version = data.version;
      ver.dependencies = fillDependencies(mod.modId, false, installed);
      ver.includes = fillDependencies(mod.modId, true, installed);
      hashVer.put(ver.version, ver);

      /** in the local database there is only one version by module */
      mod.availableMinVer = ver.version;
      mod.availableMaxVer = ver.version;
    }

    return hashVer;
  }

  static private HashMap<String, Dep> fillDependencies(String modID, boolean include,
      boolean installed) throws ServletException {
    /** fill Dep objects from database */

    VersionUtilityData[] data;
    if (installed) {
      data = VersionUtilityData.readDependencies(pool, modID, include ? "Y" : "N");
    } else {
      data = VersionUtilityData.readDependenciesToInstall(pool, modID, include ? "Y" : "N");
    }
    HashMap<String, Dep> hashDep = new HashMap<String, Dep>();
    for (int i = 0; i < data.length; i++) {
      Dep dep = new Dep();
      dep.depId = data[i].adModuleDependencyId;
      dep.minVer = data[i].startversion;
      dep.maxVer = data[i].endversion;
      dep.modId = data[i].adDependentModuleId;
      dep.modName = data[i].dependantModuleName;

      // set enforcement
      if ("Y".equals(data[i].userEditableEnforcement) && data[i].instanceEnforcement != null
          && !data[i].instanceEnforcement.isEmpty()) {
        dep.enforcement = data[i].instanceEnforcement;
      } else {
        dep.enforcement = data[i].dependencyEnforcement;
        if (dep.enforcement == null || dep.enforcement.isEmpty()) {
          dep.enforcement = "MAJOR";
        }
      }

      hashDep.put(data[i].adModuleDependencyId, dep);
    }
    return hashDep;
  }

  public static class VersionComparator implements Comparator<Object> {

    public int compare(Object o1, Object o2) {
      if (!(o1 instanceof String) || !(o2 instanceof String)) {
        return 0;
      }
      final String s1 = (String) o1;
      final String s2 = (String) o2;
      try {
        return versionCompare(s1, s2);
      } catch (NumberFormatException n) {
        return 0;
      }
    }

    /**
     * Compare just major versions without taking into account minor part
     * 
     * @param ver1
     * @param ver2
     */
    public int compareMajorVersions(String ver1, String ver2) {
      return versionCompare(ver1, ver2, true);
    }
  }

  static private int versionCompare(String ver1, String ver2) {
    return versionCompare(ver1, ver2, false);
  }

  /**
   * Compares two versions.
   * 
   * Depending on onlyMayorVersion parameter:
   * <ul>
   * <li><b> false</b>: The comparison is done for the whole versions taking thus 2.50.2 is higher
   * than 2.50.1
   * <li><b> true</b>: The comparison is done taking into account the minor version in a different
   * way:
   * <ul>
   * <li>If major versions are the same and minor version is higher then the result is higher. Thus
   * 2.50.2 is higher than 2.50.1
   * <li>If major versions are the same and minor version is higher the result is equals. Thus
   * 2.50.1 is equals to 2.50.2 (if passed as parameters in this order).
   * <li>If major versions are different no minor version is taken into account.
   * 
   * </ul>
   * </ul>
   * 
   * @param ver1
   *          Installing version to compare (example module)
   * 
   * @param ver2
   *          Installed version to compare with (example core)
   * @param onlyMayorVersion
   * @return <ul>
   *         <li>-1 in case ver1 is lower than ver2
   *         <li>0 in case ver1 equals ver2
   *         <li>1 in case ver1 is higher than ver2
   *         </ul>
   */
  static private int versionCompare(String ver1, String ver2, boolean onlyMayorVersion) {
    if ((ver1 == null || ver1.equals("")) && (ver2 == null || ver2.equals("")))
      return 0;
    if ((ver1 == null || ver1.equals("")))
      return 1;
    if ((ver2 == null || ver2.equals("")))
      return -1;

    String s1[] = ver1.split("[.]");
    String s2[] = ver2.split("[.]");

    int n1[] = new int[3];
    int n2[] = new int[3];

    for (int i = 0; i < 3; i++) {
      if (s1.length - 1 < i || s1[i] == null || s1[i].equals(""))
        n1[i] = 0;
      else
        n1[i] = Integer.parseInt(s1[i]);

      if (s2.length - 1 < i || s2[i] == null || s2[i].equals(""))
        n2[i] = 0;
      else
        n2[i] = Integer.parseInt(s2[i]);
    }

    if (n1[0] > n2[0] || (n1[0] == n2[0] && n1[1] > n2[1])
        || (n1[0] == n2[0] && n1[1] == n2[1] && n1[2] > n2[2]))
      return 1;
    else if ((onlyMayorVersion && n1[0] == n2[0] && n1[1] == n2[1] && n1[2] <= n2[2])
        || (!onlyMayorVersion && n1[0] == n2[0] && n1[1] == n2[1] && n1[2] == n2[2]))
      return 0;
    else
      return -1;
  }

  /**
   * Compares 2 versions taking into account just their major versions.
   * 
   * @return<ul> <li>-1 in case ver1 is lower than ver2 <li>0 in case ver1 equals ver2 <li>1 in case
   *             ver1 is higher than ver2 </ul>
   */
  static public int versionCompareStrictMajorVersion(String ver1, String ver2) {
    if ((ver1 == null || ver1.equals("")) && (ver2 == null || ver2.equals("")))
      return 0;
    if ((ver1 == null || ver1.equals("")))
      return 1;
    if ((ver2 == null || ver2.equals("")))
      return -1;

    String s1[] = ver1.split("[.]");
    String s2[] = ver2.split("[.]");

    int n1[] = new int[3];
    int n2[] = new int[3];

    for (int i = 0; i < 3; i++) {
      if (s1.length - 1 < i || s1[i] == null || s1[i].equals(""))
        n1[i] = 0;
      else
        n1[i] = Integer.parseInt(s1[i]);

      if (s2.length - 1 < i || s2[i] == null || s2[i].equals(""))
        n2[i] = 0;
      else
        n2[i] = Integer.parseInt(s2[i]);
    }

    if (n1[0] > n2[0] || (n1[0] == n2[0] && n1[1] > n2[1]))
      return 1;
    else if (n1[0] == n2[0] && n1[1] == n2[1])
      return 0;
    else
      return -1;
  }

  @SuppressWarnings("unchecked")
  static private HashMap<String, Mod> modules2mods(Module[] modules) {
    HashMap<String, Mod> mods = new HashMap<String, Mod>();
    if (modules == null)
      return mods;

    for (int i = 0; i < modules.length; i++) {
      if (!mods.containsKey(modules[i].getModuleID())) {
        Mod mod = new Mod();
        mod.modId = modules[i].getModuleID();
        mod.name = modules[i].getName();
        mod.versions = new HashMap<String, Ver>();
        mods.put(mod.modId, mod);
      }
    }

    for (int i = 0; i < modules.length; i++) {
      Mod mod = mods.get(modules[i].getModuleID());
      Ver ver = new Ver();
      ver.verId = modules[i].getModuleVersionID();
      ver.version = modules[i].getVersionNo();
      if (mod.availableMinVer == null || versionCompare(ver.version, mod.availableMinVer) == -1) {
        mod.availableMinVer = ver.version;
      }
      if (mod.availableMaxVer == null || versionCompare(ver.version, mod.availableMaxVer) == 1) {
        mod.availableMaxVer = ver.version;
      }

      ver.dependencies = new HashMap<String, Dep>();
      ver.includes = new HashMap<String, Dep>();

      HashMap<String, String> enforcements = (HashMap<String, String>) modules[i]
          .getAdditionalInfo().get("enforcements");

      ModuleDependency[] dependencies = modules[i].getDependencies();
      if (dependencies != null) {
        for (int j = 0; j < dependencies.length; j++) {
          Dep dep = new Dep();
          dep.modId = dependencies[j].getModuleID();
          dep.modName = dependencies[j].getModuleName();
          dep.minVer = dependencies[j].getVersionStart();
          dep.maxVer = dependencies[j].getVersionEnd();
          dep.enforcement = enforcements.get(dependencies[j].getModuleID());
          ver.dependencies.put(String.valueOf(j), dep);
        }
      }
      ModuleDependency[] includes = modules[i].getIncludes();
      if (includes != null) {
        for (int j = 0; j < includes.length; j++) {
          Dep dep = new Dep();
          dep.modId = includes[j].getModuleID();
          dep.modName = includes[j].getModuleName();
          dep.minVer = includes[j].getVersionStart();
          dep.maxVer = includes[j].getVersionEnd();
          ver.includes.put(String.valueOf(j), dep);
        }
      }
      mod.versions.put(ver.version, ver);
    }
    return mods;
  }

  static private boolean installModulesLocal(Module[] modulesToInstall, Module[] modulesToUpdate,
      Module[] modulesToMerge, Vector<String> vecErrors) throws Exception {
    boolean checked = false;
    HashMap<String, Mod> modsInstalled = fillModules(modulesToMerge);
    HashMap<String, Mod> modsToInstall = modules2mods(modulesToInstall);
    HashMap<String, Mod> modsToUpdate = modules2mods(modulesToUpdate);

    try {
      /** Check if all dependencies are satisfied with installed modules */
      checked = checkAllDependencies(modsInstalled, modsToInstall, modsToUpdate, modulesToMerge,
          vecErrors);
    } catch (Exception e) {
      throw e;
    }

    return checked;
  }

  static public boolean getOBError(OBError rt, ConnectionProvider conn, VariablesSecureApp vars,
      String[] errors) {
    if (errors.length != 0) {
      rt.setType("Error");
      StringBuffer strErrors = new StringBuffer();
      for (String s : errors)
        strErrors.append(s).append("\n");

      if (vars != null) {
        String lang = vars.getLanguage();
        rt.setMessage(Utility.parseTranslation(conn, vars, lang, strErrors.toString()));
        rt.setTitle(Utility.messageBD(conn, rt.getType(), lang));
      } else {
        rt.setMessage(strErrors.toString());
        rt.setTitle("Error");
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * 
   * check the dependencies to install new modules
   * 
   * @param moduleVersionId
   *          In param. An String array with the modules version ids, that we want to install.
   * @param moduleVersionToUpdateId
   *          Out param. Modules that need an update.
   * @param obErrors
   *          Out param. Errors in dependencies. Null if no errors.
   * @param maturityLevels
   * @return ModuleInstallDetail with modulesToInstall, modulesToUpdate and isValidConfiguration
   */
  static public ModuleInstallDetail checkRemote(VariablesSecureApp vars, String[] moduleVersionId,
      String[] moduleVersionToUpdateId, OBError obErrors, HashMap<String, String> maturityLevels)
      throws Exception {
    WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
    WebService3Impl ws = (WebService3Impl) loc.getWebService3();
    String[] errors = new String[0];

    ModuleInstallDetail mid = ws.checkConsistency(ImportModule.getInstalledModulesAndDeps(),
        moduleVersionId, moduleVersionToUpdateId, maturityLevels);

    errors = mid.getDependencyErrors();

    getOBError(obErrors, pool, vars, errors);
    return mid;
  }

  /**
   * @deprecated use
   *             {@link VersionUtility#checkRemote(VariablesSecureApp, String[], String[], OBError, HashMap)}
   *             instead
   */
  static public ModuleInstallDetail checkRemote(VariablesSecureApp vars, String[] moduleVersionId,
      String[] moduleVersionToUpdateId, OBError obErrors) throws Exception {
    HashMap<String, String> maturityLevels = new HashMap<String, String>();
    maturityLevels.put("update.level", "500");
    maturityLevels.put("install.level", "500");
    return checkRemote(vars, moduleVersionId, moduleVersionToUpdateId, obErrors, maturityLevels);
  }

  /**
   * 
   * check the dependencies to install new modules locally, without connecting to central repository
   * 
   * @param modulesToInstall
   *          In param. New modules to install, with its dependencies.
   * @param modulesToUpdate
   *          In param. Modules to install, with its dependencies.
   * @param modulesToMerge
   *          In param. Modules to merge.
   * @param obErrors
   *          Out param. Errors in dependencies. Null if no errors.
   * @return true if all dependencies can be resolved without need of download any package from
   *         central repository, false in other case.
   */
  static public boolean checkLocal(VariablesSecureApp vars, Module[] modulesToInstall,
      Module[] modulesToUpdate, Module[] modulesToMerge, OBError obErrors) throws Exception {
    Vector<String> vecErrors = new Vector<String>();

    boolean checked = installModulesLocal(modulesToInstall, modulesToUpdate, modulesToMerge,
        vecErrors);

    String[] errors = vecErrors.toArray(new String[0]);
    getOBError(obErrors, pool, vars, errors);
    return checked;
  }

  /**
   * @deprecated use
   *             {@link VersionUtility#checkLocal(VariablesSecureApp, Module[], Module[], Module[], OBError)}
   */
  static public boolean checkLocal(VariablesSecureApp vars, Module[] modulesToInstall,
      Module[] modulesToUpdate, OBError obErrors) throws Exception {
    return checkLocal(vars, modulesToInstall, null, null, obErrors);
  }

  /**
   * @deprecated use
   *             {@link VersionUtility#checkLocal(VariablesSecureApp, Module[], Module[], Module[], OBError)}
   */
  static public boolean checkLocal(VariablesSecureApp vars, Module[] modulesToInstall,
      OBError obErrors) throws Exception {
    return checkLocal(vars, modulesToInstall, null, null, obErrors);
  }

  static public void setPool(ConnectionProvider cp) {
    pool = cp;
  }
}
