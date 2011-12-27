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

package org.openbravo.service.system;

import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.ExcludeFilter;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.DalInitializingTask;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.ddlutils.util.DBSMOBUtil;
import org.openbravo.ddlutils.util.ModuleRow;
import org.openbravo.model.ad.module.Module;

/**
 * Performs different types of validations on the basis of the type parameter.
 * 
 * @author mtaal
 */
public class SystemValidationTask extends DalInitializingTask {
  private static final Logger log = Logger.getLogger("SystemValidation");

  private String type;
  private boolean failOnError = false;
  private String moduleJavaPackage;

  @Override
  protected void doExecute() {
    final Module module = getModule();

    if (getType().contains("database")) {
      final Database database = createDatabaseObject();
      log.info("Validating Database and Application Dictionary");
      final DatabaseValidator databaseValidator = new DatabaseValidator();
      databaseValidator.setValidateModule(module);
      databaseValidator.setDatabase(database);
      final SystemValidationResult result = databaseValidator.validate();
      if (result.getErrors().isEmpty() && result.getWarnings().isEmpty()) {
        log.warn("Validation successfull no warnings or errors");
      } else {
        final String errors = SystemService.getInstance().logValidationResult(log, result);
        if (failOnError) {
          throw new OBException(errors);
        }
      }
    }
    // does both module and database
    if (getType().contains("module")) {
      log.info("Validating Modules");

      // Validate module
      final SystemValidationResult result = SystemService.getInstance()
          .validateModule(module, null);

      // validate DB for module if there's dbprefixes
      if (module != null && module.getModuleDBPrefixList() != null
          && module.getModuleDBPrefixList().size() != 0) {
        log.info("Validating DB");
        Platform platform = getPlatform();
        String dbPrefix = module.getModuleDBPrefixList().get(0).getName();

        ExcludeFilter excludeFilter;
        excludeFilter = DBSMOBUtil.getInstance().getExcludeFilter(getProject().getBaseDir());
        DBSMOBUtil.getInstance().getModules(platform, excludeFilter);
        final ModuleRow row = DBSMOBUtil.getInstance().getRowFromDir(module.getJavaPackage());
        ExcludeFilter filter = row.filter;

        Database database = platform.loadModelFromDatabase(filter, dbPrefix, true, module.getId());
        final DatabaseValidator databaseValidator = new DatabaseValidator();
        databaseValidator.setValidateModule(module);
        databaseValidator.setDatabase(database);
        SystemValidationResult resultDb = databaseValidator.validate();
        result.addAll(resultDb);
      }

      if (result.getErrors().isEmpty() && result.getWarnings().isEmpty()) {
        log.warn("Validation successfull no warnings or errors");
      } else {
        final String errors = SystemService.getInstance().logValidationResult(log, result);
        if (!result.getErrors().isEmpty() && failOnError) {
          throw new OBException(errors);
        }
      }
    }
  }

  private Platform getPlatform() {
    final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();

    final BasicDataSource ds = new BasicDataSource();
    ds.setDriverClassName(props.getProperty("bbdd.driver"));
    if (props.getProperty("bbdd.rdbms").equals("POSTGRE")) {
      ds.setUrl(props.getProperty("bbdd.url") + "/" + props.getProperty("bbdd.sid"));
    } else {
      ds.setUrl(props.getProperty("bbdd.url"));
    }
    ds.setUsername(props.getProperty("bbdd.user"));
    ds.setPassword(props.getProperty("bbdd.password"));
    return PlatformFactory.createNewPlatformInstance(ds);
  }

  private Database createDatabaseObject() {
    Platform platform = getPlatform();
    platform.getModelLoader().setOnlyLoadTableColumns(true);
    return platform.loadModelFromDatabase(null);
  }

  private Module getModule() {
    if (getModuleJavaPackage() == null) {
      return null;
    }
    final OBCriteria<Module> modules = OBDal.getInstance().createCriteria(Module.class);
    modules.add(Restrictions.eq(Module.PROPERTY_JAVAPACKAGE, moduleJavaPackage));

    if (modules.list().size() == 0) {
      throw new OBException("Module with javapackage " + moduleJavaPackage + " does not exist");
    }
    return modules.list().get(0);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isFailOnError() {
    return failOnError;
  }

  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  public String getModuleJavaPackage() {
    return moduleJavaPackage;
  }

  public void setModuleJavaPackage(String moduleJavaPackage) {
    this.moduleJavaPackage = moduleJavaPackage;
  }
}
