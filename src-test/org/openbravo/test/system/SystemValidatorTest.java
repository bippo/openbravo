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

package org.openbravo.test.system;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.ExcludeFilter;
import org.apache.log4j.Logger;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.ddlutils.util.DBSMOBUtil;
import org.openbravo.model.ad.module.Module;
import org.openbravo.service.system.DatabaseValidator;
import org.openbravo.service.system.ModuleValidator;
import org.openbravo.service.system.SystemValidationResult;
import org.openbravo.service.system.SystemValidationResult.SystemValidationType;
import org.openbravo.test.base.BaseTest;

/**
 * Tests System Validation.
 * 
 * @see DatabaseValidator
 * @see ModuleValidator
 * 
 * @author mtaal
 */

public class SystemValidatorTest extends BaseTest {

  private static final Logger log = Logger.getLogger(SystemValidatorTest.class);

  /**
   * Executes the {@link DatabaseValidator#validate()} method on the current database.
   */
  public void noTestSystemValidation() {
    // disabled as covered by dbconsistency test and quite slow
    setSystemAdministratorContext();
    final DatabaseValidator databaseValidator = new DatabaseValidator();
    databaseValidator.setDatabase(createDatabaseObject(null));
    final SystemValidationResult result = databaseValidator.validate();
    printResult(result, true);
  }

  /**
   * Executes the {@link DatabaseValidator#validate()} method on the current database for all
   * modules.
   */
  public void noTestSystemValidationModules() {
    // disabled as covered by dbconsistency test and quite slow
    setSystemAdministratorContext();
    for (Module module : OBDal.getInstance().createQuery(Module.class, "").list()) {
      final DatabaseValidator databaseValidator = new DatabaseValidator();
      if (module.getModuleDBPrefixList().isEmpty()) {
        // ignore these ones
        continue;
      }
      databaseValidator.setDatabase(createDatabaseObject(module));
      databaseValidator.setValidateModule(module);
      final SystemValidationResult result = databaseValidator.validate();
      printResult(result, false);
    }
  }

  private Database createDatabaseObject(Module module) {
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
    Platform platform = PlatformFactory.createNewPlatformInstance(ds);
    platform.getModelLoader().setOnlyLoadTableColumns(true);

    if (module != null) {
      final String dbPrefix = module.getModuleDBPrefixList().get(0).getName();
      final ExcludeFilter filter = DBSMOBUtil.getInstance().getExcludeFilter(
          new File(props.getProperty("source.path")));
      filter.addPrefix(dbPrefix);

      return platform.loadModelFromDatabase(filter, dbPrefix, true, module.getId());
    }

    return platform.loadModelFromDatabase(null);
  }

  /**
   * Performs module validation using the {@link ModuleValidator}.
   */
  public void testModulesValidation() {
    setSystemAdministratorContext();
    final ModuleValidator moduleValidator = new ModuleValidator();
    final SystemValidationResult result = moduleValidator.validate();
    printResult(result, true);
  }

  private void printResult(SystemValidationResult result, boolean allowFail) {
    for (SystemValidationType validationType : result.getWarnings().keySet()) {
      log.debug("\n+++++++++++++++++++++++++++++++++++++++++++++++++++");
      log.debug("Warnings for Validation type: " + validationType);
      log.debug("\n+++++++++++++++++++++++++++++++++++++++++++++++++++");
      final List<String> warnings = result.getWarnings().get(validationType);
      for (String warning : warnings) {
        log.debug(warning);
      }
    }

    final StringBuilder sb = new StringBuilder();
    for (SystemValidationType validationType : result.getErrors().keySet()) {
      sb.append("\n+++++++++++++++++++++++++++++++++++++++++++++++++++");
      sb.append("Errors for Validation type: " + validationType);
      sb.append("\n+++++++++++++++++++++++++++++++++++++++++++++++++++");
      final List<String> errors = result.getErrors().get(validationType);
      for (String err : errors) {
        sb.append(err);
        if (sb.length() > 0) {
          sb.append("\n");
        }
      }
      if (allowFail && errors.size() > 0) {
        fail(sb.toString());
      }
    }
    log.debug(sb.toString());
  }

}