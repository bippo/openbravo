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

import org.openbravo.database.ConnectionProvider;
import org.openbravo.wad.validation.WADValidationResult.WADValidationType;

/**
 * Performs a series of validations for WAD tabs. It does not use DAL but sqlc not to have to init
 * DAL for each compilation.
 * 
 */
class WADValidator {
  private String modules;
  private ConnectionProvider conn;
  private String checkAll;
  private boolean friendlyWarnings;

  /**
   * Constructor
   * 
   * @param conn
   *          Database ConnectionProvider
   * @param modules
   *          Module to check
   */
  public WADValidator(ConnectionProvider conn, String modules, boolean friendlyWarnings) {
    checkAll = (modules == null || modules.equals("%") || modules.equals("")) ? "Y" : "N";
    this.modules = "'"
        + (checkAll.equals("Y") ? "%" : modules.replace(", ", ",").replace(",", "', '")) + "'";
    this.conn = conn;
    this.friendlyWarnings = friendlyWarnings;
  }

  /**
   * Performs the validations on the assigned tabs
   * 
   * @return the result of the validations
   */
  public WADValidationResult validate() {
    WADValidationResult result = new WADValidationResult();
    validateIdentifier(result);
    validateKey(result);
    validateModelObject(result);
    validateModelObjectMapping(result);
    validateColumnNaming(result);
    validateAuxiliarInput(result);
    validateReferences(result);
    validateProcessWithoutClass(result);
    validateTabsWithMultipleFieldsForSameColumn(result);
    return result;
  }

  /**
   * Validates tables have at least one column set as identifier
   */
  private void validateIdentifier(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkIdentifier(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename, WADValidationType.MISSING_IDENTIFIER,
            "Table " + issue.objectname + " has not identifier.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating identifiers: " + e.getMessage());
    }
  }

  /**
   * Validates tables have one and only one primary key column
   */
  private void validateKey(WADValidationResult result) {
    try {
      // Check tables without key
      WADValidatorData data[] = WADValidatorData.checkKey(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename, WADValidationType.MISSING_KEY, "Table "
            + issue.objectname + " has not primary key.");
      }

      data = WADValidatorData.checkMultipleKey(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename, WADValidationType.MULTIPLE_KEYS, "Table "
            + issue.objectname + " has more than one key column.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating primary keys: " + e.getMessage());
    }
  }

  /**
   * Validates all classes defined in model object are inside the module package
   */
  private void validateModelObject(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkModelObject(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename, WADValidationType.MODEL_OBJECT,
            issue.objecttype + " " + issue.objectname + " has classname: " + issue.currentvalue
                + ". But it should be in " + issue.expectedvalue + " package.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating model object: " + e.getMessage());
    }
  }

  /**
   * Validates all mappings for modules start by the java package
   */
  private void validateModelObjectMapping(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkModelObjectMapping(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename, WADValidationType.MODEL_OBJECT_MAPPING,
            issue.objecttype + " " + issue.objectname + " has mapping: " + issue.currentvalue
                + ". But it should start with /" + issue.expectedvalue + ".");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating model object: " + e.getMessage());
    }
  }

  /**
   * Validates names and columnnames in columns
   */
  private void validateColumnNaming(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkColumnName(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename, WADValidationType.COLUMN_NAME,
            issue.objecttype + " " + issue.objectname + " has value: " + issue.currentvalue
                + ". But it should start with EM_" + issue.expectedvalue);
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating model object: " + e.getMessage());
    }
  }

  /**
   * Validates names of auxiliar inputs columns
   */
  private void validateAuxiliarInput(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkAuxiliarInput(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename, WADValidationType.AUXILIARINPUT,
            issue.objectname + " does not start by its module's DBPrefix: " + issue.expectedvalue);
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating model object: " + e.getMessage());
    }
  }

  /**
   * Validates base references don't have parent reference
   */
  private void validateReferences(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkBaseReferenceWithParent(conn, modules,
          checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename,
            WADValidationType.BASEREFERENCE_WITH_PARENT, issue.objectname
                + " base reference has parent reference " + issue.currentvalue
                + ". Base references should not have parent reference.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating references: " + e.getMessage());
    }

  }

  /**
   * Validates all UI Standard processes define a process class to execute
   * 
   * @param result
   */
  private void validateProcessWithoutClass(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkProcessClasses(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename, WADValidationType.PROCESS_WITHOUT_CLASS,
            issue.objectname + " process does not define a Java class to implement it.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating references: " + e.getMessage());
    }
  }

  /**
   * The validation fails when there are fields referencing to a column that is already referenced
   * by another field of the tab
   * 
   * @param result
   */
  private void validateTabsWithMultipleFieldsForSameColumn(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkTabsWithMultipleFieldsForSameColumn(conn,
          modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(issue.moduleid, issue.modulename,
            WADValidationType.TABS_WITH_MULTIPLE_FIELDS_FOR_SAME_COLUMN, "Error in field "
                + issue.fieldname + ". There are more than one fields pointing to the column "
                + issue.columnname + " in the tab " + issue.tabname + " of the " + issue.windowname
                + " window.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating references: " + e.getMessage());
    }
  }
}
