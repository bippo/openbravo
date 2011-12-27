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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of the errors/warnings for a certain type of validation.
 * 
 * @author mtaal
 */
public class SystemValidationResult {

  public enum SystemValidationType {
    NAME_TOO_LONG, MODULE_ERROR, CUSTOMIZATION_ID, INCORRECT_DEFAULT_VALUE, WRONG_NAME, WRONG_LENGTH, NO_PRIMARY_KEY_COLUMNS, NOT_NULL_IN_DB_NOT_MANDATORY_IN_AD, MANDATORY_IN_AD_NULLABLE_IN_DB, NOT_EXIST_IN_AD, NOT_EXIST_IN_DB, NOT_PART_OF_FOREIGN_KEY, WRONG_TYPE, INCORRECT_CLIENT_ORG_PROPERTY_NAME, UNEQUAL_DEFAULTVALUE, INCORRECT_PK_NAME, INCORRECT_FK_NAME, INCORRECT_CHECK_NAME, INCORRECT_UNIQUE_NAME, INCORRECT_INDEX_NAME, INCORRECT_NAME_LENGTH, INCORRECT_DATASET_NAME, DEPENDENCY_PROBLEM, HAS_PROPERTY_CONFIGURATION, OLDSTYLE_PASSWORD_COLUMNS, DUPLICATED_INCLUSION;

    public String getName() {
      return this.getClass().getSimpleName();
    }
  }

  private Map<SystemValidationType, List<String>> warnings = new HashMap<SystemValidationType, List<String>>();
  private Map<SystemValidationType, List<String>> errors = new HashMap<SystemValidationType, List<String>>();

  private String category;

  /**
   * Add the warnings and errors of one validationResult to this one.
   * 
   * @param validationResult
   *          the warnings/errors of the validationResult are added to this one
   */
  public void addAll(SystemValidationResult validationResult) {
    addAll(warnings, validationResult.getWarnings());
    addAll(errors, validationResult.getErrors());
  }

  private void addAll(Map<SystemValidationType, List<String>> current,
      Map<SystemValidationType, List<String>> add) {
    for (SystemValidationType svt : add.keySet()) {
      List<String> list;
      if ((list = current.get(svt)) != null) {
        list.addAll(add.get(svt));
      } else {
        current.put(svt, add.get(svt));
      }
    }
  }

  /**
   * Adds a warning to the result for a specific validation type.
   * 
   * @param validationType
   *          the type of warning
   * @param warning
   *          the message itself
   */
  public void addWarning(SystemValidationType validationType, String warning) {
    addToResult(warnings, validationType, warning);
  }

  /**
   * Adds an error message to the result.
   * 
   * @param validationType
   *          the type of message
   * @param error
   *          the message text
   */
  public void addError(SystemValidationType validationType, String error) {
    addToResult(errors, validationType, error);
  }

  private void addToResult(Map<SystemValidationType, List<String>> result,
      SystemValidationType validationType, String msg) {

    List<String> msgList = result.get(validationType);
    if (msgList == null) {
      msgList = new ArrayList<String>();
      result.put(validationType, msgList);
    }
    msgList.add(msg);
  }

  /**
   * @return Returns the list of error messages by validationType.
   */
  public Map<SystemValidationType, List<String>> getErrors() {
    return errors;
  }

  /**
   * @return Returns the list of warning messages by validationType.
   */
  public Map<SystemValidationType, List<String>> getWarnings() {
    return warnings;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
}
