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

package org.openbravo.wad.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Contains information of all the warnings and errors detected during the WAD validation
 * 
 */
class WADValidationResult {
  private static final Logger log = Logger.getLogger(WADValidationResult.class);

  /*
   * Whitelist of modules for which all validation problems will be ignored and not reported. So far
   * only the special modules "org.openbravo.deprecated.cleanupv3", "org.openbravo.importdata" are
   * ignored.
   */
  private static final String[] moduleWhitelist = { "677192D0C60F411384832241227360E3",
      "FF8081812FBF677A012FBF6A0D3E0003" };

  /**
   * Types of possible WAD validations, they have an identifier and a description
   * 
   */
  public enum WADValidationType {
    SQL("SQL"), MISSING_IDENTIFIER("Missing Identifier"), MISSING_KEY("Missing Key Column"), MODEL_OBJECT(
        "Model Object"), MODEL_OBJECT_MAPPING("HTML Mapping"), COLUMN_NAME("Column Naming"), AUXILIARINPUT(
        "Auxiliary Input Name"), MULTIPLE_KEYS("Multiple Key Columns in Table"), BASEREFERENCE_WITH_PARENT(
        "Base Reference with Parent Reference"), PROCESS_WITHOUT_CLASS(
        "Standard UI Processes without class"), TABS_WITH_MULTIPLE_FIELDS_FOR_SAME_COLUMN(
        "Tabs with multiple fields for same column");

    private String description;

    WADValidationType(String description) {
      this.description = description;
    }

    /**
     * Returns the description for the validation type
     * 
     * @return description
     */
    public String getDescription() {
      return description;
    }
  }

  private HashMap<WADValidationType, List<String>> errors = new HashMap<WADValidationType, List<String>>();
  private HashMap<WADValidationType, List<String>> warnings = new HashMap<WADValidationType, List<String>>();
  private ArrayList<String> modules = new ArrayList<String>();

  /**
   * Adds a warning message to a validation type
   * 
   * @param validationType
   *          validation type to add the warning to
   * @param warning
   *          warning message
   */
  public void addWarning(String moduleId, String moduleName, WADValidationType validationType,
      String warning) {
    addToResult(moduleId, moduleName, warnings, validationType, warning);
  }

  public void addWarning(WADValidationType validationType, String warning) {
    addWarning(null, null, validationType, warning);
  }

  /**
   * Adds an error message to a validation type
   * 
   * @param validationType
   *          validation type to add the error to
   * @param warning
   *          error message
   */
  public void addError(String moduleId, String moduleName, WADValidationType validationType,
      String warning) {
    addToResult(moduleId, moduleName, errors, validationType, warning);
  }

  private void addToResult(String moduleId, String moduleName,
      Map<WADValidationType, List<String>> result, WADValidationType validationType, String msg) {

    // if not called for general error
    if (moduleId != null && moduleName != null) {
      // skip modules in whilelist completely
      for (String s : moduleWhitelist) {
        if (s.equals(moduleId)) {
          return;
        }
      }

      if (!modules.contains(moduleName)) {
        modules.add(moduleName);
      }
    }

    List<String> msgList = result.get(validationType);
    if (msgList == null) {
      msgList = new ArrayList<String>();
      result.put(validationType, msgList);
    }
    msgList.add(msg);
  }

  /**
   * Returns true in case the validation contain errors
   * 
   */
  public boolean hasErrors() {
    return errors.size() > 0;
  }

  /**
   * Prints the result in the log
   */
  public void printLog(boolean stopOnError) {
    for (WADValidationType type : warnings.keySet()) {
      log.warn("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      log.warn("Warnings for Validation type: " + type.getDescription());
      log.warn("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      for (String warn : warnings.get(type)) {
        log.warn(warn);
      }
    }

    if (!stopOnError && errors.size() > 0) {
      log.error("The following errors are violations to the Openbravo naming rules.");
      log.error("They do not stop the build process but they should be fixed ");
      log.error("as soon as possible.");
    }

    for (WADValidationType type : errors.keySet()) {
      log.error("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      log.error("Errors for Validation type: " + type.getDescription());
      log.error("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      for (String error : errors.get(type)) {
        log.error(error);
      }
    }

  }

  public void printFriendlyLog() {
    String message = "";
    String message2 = "";
    if (modules.size() == 0) {
      return;
    }
    if (modules.size() == 1) {
      message = "Module ";
      message2 = " does not comply with ";
    } else {
      message = "Modules ";
      message2 = " do not comply with ";
    }
    for (int i = 0; i < modules.size(); i++) {
      if (i < modules.size() - 1) {
        message += ",";
      }
      message += modules.get(i);
    }
    message2 += "Openbravo naming rules.";
    log.info(message + message2);
    log.info("The rebuild process has completed successfully but this module");
    log.info("might cause conflicts with other modules in the future.");
    log.info("Please request the author of this module to produce a new version");
    log.info("that addresses these violations.");
  }
}
