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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class validates several aspects of the application dictionary. The application dictionary
 * itself is validated as well as the match between the application dictionary and the database.
 * 
 * @author mtaal
 */
public class ApplicationDictionaryValidator {
  private static final Logger log = Logger.getLogger(ApplicationDictionaryValidator.class);

  private List<SystemValidator> validators = new ArrayList<SystemValidator>();

  public ApplicationDictionaryValidator() {
    validators.add(new DatabaseValidator());
  }

  /**
   * Performs the validation using different validators. Returns the validation results grouped by
   * type of validation.
   * 
   * @return the validation result.
   */
  public Map<String, SystemValidationResult> validate() {
    final Map<String, SystemValidationResult> result = new HashMap<String, SystemValidationResult>();

    for (SystemValidator validator : validators) {
      result.put(validator.getCategory(), validator.validate());
    }
    return result;
  }
}
