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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.system;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.NamingUtil;
import org.openbravo.base.model.Property;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.modules.VersionUtility;
import org.openbravo.erpCommon.modules.VersionUtility.VersionComparator;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.model.ad.ui.Element;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Form;
import org.openbravo.model.ad.ui.Menu;
import org.openbravo.model.ad.ui.Message;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.TextInterface;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.service.system.SystemValidationResult.SystemValidationType;

/**
 * Validates modules, their dependencies and licenses
 * 
 * @author mtaal
 */
public class ModuleValidator implements SystemValidator {

  private Module validateModule;

  public String getCategory() {
    return "Module";
  }

  /**
   * Validates all modules and returns the {@link SystemValidationResult}.
   */
  public SystemValidationResult validate() {
    final SystemValidationResult result = new SystemValidationResult();
    if (getValidateModule() != null) {
      validate(getValidateModule(), result);
    } else {
      final OBCriteria<Module> modules = OBDal.getInstance().createCriteria(Module.class);
      for (Module module : modules.list()) {
        if (module.isInDevelopment()) {
          validate(module, result);
        }
      }
    }
    return result;
  }

  public void validate(Module module, SystemValidationResult result) {
    if (module.getId().equals("0")) {
      return;
    }
    final String sourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path");
    final File modulesDir = new File(sourcePath, "modules");

    final File moduleDir = new File(modulesDir, module.getJavaPackage());
    if (!moduleDir.exists()) {
      return;
    }

    // check dependency on core
    checkDepencyOnCore(module, result);

    checkDuplicatedInclusions(module, result, new ArrayList<String>());

    checkDependencyVersion(module, result);

    checkJavaPath(module, moduleDir, module.getJavaPackage(), result);

    checkJavaPackages(module, result);

    checkHasUIArtifact(module, result);

    checkTableName(module, result);

    checkHasReferenceData(module, result);

    // disable this check until this issue has been commented:
    // https://issues.openbravo.com/view.php?id=7905
    // checkHasIllegalId(module, result);

    if (module.getLicenseText() == null || module.getLicenseType() == null) {
      result.addError(SystemValidationType.MODULE_ERROR,
          "The license and/or the licenseType of the Module " + module.getName()
              + " are not set, before exporting these " + "fields should be set");
    }

  }

  private void checkJavaPath(Module module, File moduleDir, String javaPackage,
      SystemValidationResult result) {

    File curDir = new File(moduleDir, "src");
    if (!curDir.exists()) {
      return;
    }
    final String[] paths = javaPackage.split("\\.");
    boolean rootSrcDir = true;
    for (String part : paths) {
      final File partDir = new File(curDir, part);
      if (curDir.listFiles() == null || !partDir.exists()) {
        result.addError(SystemValidationType.MODULE_ERROR, "The source directory of the Module "
            + module.getName()
            + " is invalid, the source directory structure does not correspond to the "
            + "javaPackage of the module (" + javaPackage + ").");
        return;
      }
      // check all children
      for (File file : curDir.listFiles()) {
        // ignore hidden files
        if (file.isHidden()) {
          continue;
        }
        // allow properties files in the root
        if (!file.isDirectory() && rootSrcDir) {
          continue;
        }
        if (!file.getName().equals(part)) {
          result.addError(SystemValidationType.MODULE_ERROR, "The source directory of the Module "
              + module.getName() + " is invalid, it contains directories/files (" + file.getName()
              + ") which are not part of " + "the javaPackage of the module: " + javaPackage);
        }
      }
      curDir = partDir;
    }
  }

  private void checkHasUIArtifact(Module module, SystemValidationResult result) {
    if (module.isTranslationRequired()) {
      return;
    }
    final boolean reportError = hasArtifact(Window.class, module) || hasArtifact(Tab.class, module)
        || hasArtifact(Field.class, module) || hasArtifact(Element.class, module)
        || hasArtifact(TextInterface.class, module) || hasArtifact(Message.class, module)
        || hasArtifact(Form.class, module) || hasArtifact(Menu.class, module);
    if (reportError) {
      result.addError(SystemValidationType.MODULE_ERROR, "Module " + module.getName()
          + " has UI Artifacts, " + "translation required should be set to 'Y', it is now 'N'.");
    }
  }

  // checks https://issues.openbravo.com/view.php?id=7905
  // Add to validation: module artifacts shouldn't use id's in the range of 100.000-799.999 as these
  // are used for old customizations
  private void checkHasIllegalId(Module module, SystemValidationResult result) {
    for (Entity entity : ModelProvider.getInstance().getModel()) {
      Property moduleProperty = null;
      for (Property property : entity.getProperties()) {
        if (property.getTargetEntity() != null && property.getTargetEntity() == module.getEntity()) {
          moduleProperty = property;
          break;
        }
      }
      // check the artifacts pointing to a module
      if (moduleProperty != null) {
        final OBCriteria<BaseOBObject> obCriteria = OBDal.getInstance().createCriteria(
            entity.getName());
        obCriteria.setFilterOnActive(false);
        obCriteria.setFilterOnReadableClients(false);
        obCriteria.setFilterOnReadableOrganization(false);
        obCriteria.add(Restrictions.eq(moduleProperty.getName(), module));
        for (BaseOBObject baseOBObject : obCriteria.list()) {
          checkIllegalId(baseOBObject, result, module);
        }
      }
    }
  }

  private void checkIllegalId(BaseOBObject baseOBObject, SystemValidationResult result,
      Module module) {
    final String id = (String) baseOBObject.getId();
    if (id.compareTo("100000") > 0 && id.compareTo("799999") < 0 && id.length() == 6) {
      result.addError(SystemValidationType.CUSTOMIZATION_ID, "Module " + module.getName()
          + " has an artifact (" + baseOBObject
          + ") with an id in the customization range 100000 to 799999. This is not allowed.");
    }

    // also check the children
    for (Property property : baseOBObject.getEntity().getProperties()) {
      if (property.isOneToMany() && property.isChild()) {
        @SuppressWarnings("unchecked")
        final List<BaseOBObject> children = (List<BaseOBObject>) baseOBObject.get(property
            .getName());
        for (BaseOBObject child : children) {
          checkIllegalId(child, result, module);
        }
      }
    }
  }

  private <T extends BaseOBObject> boolean hasArtifact(Class<T> clz, Module module) {
    final OBCriteria<T> obc = OBDal.getInstance().createCriteria(clz);
    obc.add(Restrictions.eq("module", module));
    return obc.count() > 0;
  }

  /**
   * Checks the defined dependency versions are present in DB
   * 
   * @param module
   * @param result
   */
  private void checkDependencyVersion(Module module, SystemValidationResult result) {
    for (ModuleDependency dependentModule : module.getModuleDependencyList()) {
      String firstVer = dependentModule.getFirstVersion();
      String lastVer = dependentModule.getLastVersion();
      String depActualVersion = dependentModule.getDependentModule().getVersion();

      if (dependentModule.isIncluded()) {
        // for inclusions check the dependency matches exactly with the defined one
        if (!depActualVersion.equals(firstVer)) {
          result.addWarning(SystemValidationType.MODULE_ERROR, module.getName()
              + " defines inclussion of module " + dependentModule.getDependentModule().getName()
              + " in version " + firstVer + ", but actual version in DB is " + depActualVersion
              + ". They must exactly match.");
        }
        // check dependencies for included modules recursively
        checkDependencyVersion(dependentModule.getDependentModule(), result);
      } else {
        VersionUtility.VersionComparator vc = new VersionComparator();
        String enforcement = dependentModule.getDependencyEnforcement();

        if (lastVer != null && vc.compare(firstVer, lastVer) > 0) {
          result.addError(SystemValidationType.MODULE_ERROR, module.getName()
              + " defines dependency on " + dependentModule.getDependentModule().getName()
              + ". It is incorrect beacuase first version (" + firstVer
              + ") is higher than last version (" + lastVer + ")");
        }

        if ("MAJOR".equals(enforcement)) {
          if (vc.compare(firstVer, depActualVersion) > 0) {
            result.addError(SystemValidationType.MODULE_ERROR, module.getName()
                + " defines dependency on " + dependentModule.getDependentModule().getName()
                + " start version " + firstVer + ", but actual version in DB is "
                + depActualVersion + ".");
          } else if (lastVer == null && vc.compareMajorVersions(firstVer, depActualVersion) != 0) {
            result.addError(SystemValidationType.MODULE_ERROR, module.getName()
                + " defines dependency on " + dependentModule.getDependentModule().getName()
                + " start version " + firstVer + ", but actual version in DB is "
                + depActualVersion + ". Which has a different major version.");
          } else if (lastVer != null
              && VersionUtility.versionCompareStrictMajorVersion(depActualVersion, lastVer) > 0) {
            result.addError(SystemValidationType.MODULE_ERROR, module.getName()
                + " defines dependency on " + dependentModule.getDependentModule().getName()
                + " end version " + lastVer + ", but actual version in DB is " + depActualVersion);
          }
        } else if ("MINOR".equals(enforcement)) {
          if (lastVer == null && vc.compare(firstVer, depActualVersion) != 0) {
            result.addError(SystemValidationType.MODULE_ERROR, module.getName()
                + " defines a MINOR enforcement dependency on "
                + dependentModule.getDependentModule().getName() + " version " + firstVer
                + ", but actual version in DB is " + depActualVersion
                + ". Both versions must be exactly the same.");
          } else if (lastVer != null && vc.compare(depActualVersion, lastVer) > 0) {
            result.addError(SystemValidationType.MODULE_ERROR, module.getName()
                + " defines a MINOR enforcement dependency on "
                + dependentModule.getDependentModule().getName() + " end version " + lastVer
                + ", but actual version in DB is " + depActualVersion);
          }
        } else { // NONE
          if (vc.compare(firstVer, depActualVersion) > 0) {
            result.addError(SystemValidationType.MODULE_ERROR, module.getName()
                + " defines NONE enforcement dependency on "
                + dependentModule.getDependentModule().getName() + " start version " + firstVer
                + ", but actual version in DB is " + depActualVersion + ".");
          }
        }
      }
    }
  }

  private void checkDepencyOnCore(Module module, SystemValidationResult result) {
    boolean coreModuleFound = false;
    for (ModuleDependency md : module.getModuleDependencyList()) {
      final Module dependentModule = findCoreModule(md.getDependentModule(), module.getId());
      if (dependentModule != null) {
        if (dependentModule.getId().equals(module.getId())) {
          result.addError(SystemValidationType.MODULE_ERROR,
              "Cycle in module dependencies with module " + module.getName());
          coreModuleFound = true; // prevents additional message
          break;
        }
        if (dependentModule.getId().equals("0")) {
          coreModuleFound = true;
          break;
        }
      }
    }
    if (!coreModuleFound) {
      result.addWarning(SystemValidationType.MODULE_ERROR, "Module " + module.getName()
          + " or any of its ancestors " + "does not depend on the Core module.");
    }
  }

  /**
   * Checks module inclusions are not defined more than once.
   */
  private void checkDuplicatedInclusions(Module module, SystemValidationResult result,
      List<String> verifiedInclusions) {
    for (ModuleDependency md : module.getModuleDependencyList()) {
      if (md.isIncluded()) {
        if (verifiedInclusions.contains(md.getDependentModule().getId())) {
          result.addError(SystemValidationType.DUPLICATED_INCLUSION, "Module "
              + md.getDependentModule().getName()
              + " is included several times in the dependency tree.");
        }
        verifiedInclusions.add(md.getDependentModule().getId());
        checkDuplicatedInclusions(md.getDependentModule(), result, verifiedInclusions);
      }
    }
  }

  private void checkTableName(Module module, SystemValidationResult result) {
    for (org.openbravo.model.ad.module.DataPackage pckg : module.getDataPackageList()) {
      OBCriteria<Table> tablesCriteria = OBDal.getInstance().createCriteria(Table.class);
      tablesCriteria.add(Restrictions.eq(Table.PROPERTY_DATAPACKAGE, pckg));
      final List<Table> tables = tablesCriteria.list();
      for (Table table : tables) {
        final String name = table.getName();
        if (NamingUtil.doesNameHaveIllegalChars(name)) {
          result.addError(SystemValidationType.WRONG_NAME, "Table " + table.getName()
              + " has a name containing illegal characters such as dot, comma, /, etc.");
        } else if (NamingUtil.doesNameContainNonNormalCharacters(name)) {
          result.addWarning(SystemValidationType.WRONG_NAME, "Table " + table.getName()
              + " has a name containing less normal characters, "
              + "it is best to only use characters from a to z, A to Z, 0 to 9 or _");
        }
      }
    }
  }

  private void checkHasReferenceData(Module module, SystemValidationResult result) {
    OBCriteria<DataSet> datasetsCriteria = OBDal.getInstance().createCriteria(DataSet.class);
    datasetsCriteria.add(Restrictions.eq(DataSet.PROPERTY_MODULE, module));
    int numDatasets = datasetsCriteria.count();

    if (module.isHasReferenceData() && numDatasets == 0) {
      result.addWarning(SystemValidationType.MODULE_ERROR, "Module " + module.getName()
          + " has been marked as 'has reference data' but it does not have any Dataset associated");
    } else if (!module.isHasReferenceData() && numDatasets > 0) {
      result
          .addWarning(
              SystemValidationType.MODULE_ERROR,
              "Module "
                  + module.getName()
                  + " has at least one Dataset associated but it has not been marked as 'has reference data'");
    }
  }

  private void checkJavaPackages(Module module, SystemValidationResult result) {
    for (org.openbravo.model.ad.module.DataPackage pckg : module.getDataPackageList()) {
      if (pckg.getJavaPackage() != null
          && !pckg.getJavaPackage().startsWith(module.getJavaPackage())) {
        result.addError(SystemValidationType.DEPENDENCY_PROBLEM,
            "Data package " + pckg.getName()
                + " has a java package which is not within the java package of its module "
                + module.getName());
      }
    }
  }

  // find the core module
  private Module findCoreModule(Module module, String originalModuleId) {
    if (module.getId().equals(originalModuleId)) {
      return module;
    }
    if (module.getId().equals("0")) {
      return module;
    }

    for (ModuleDependency md : module.getModuleDependencyList()) {
      final Module depModule = findCoreModule(md.getDependentModule(), originalModuleId);
      if (depModule != null) {
        return depModule;
      }
    }
    return null;
  }

  public Module getValidateModule() {
    return validateModule;
  }

  public void setValidateModule(Module validateModule) {
    this.validateModule = validateModule;
  }
}
