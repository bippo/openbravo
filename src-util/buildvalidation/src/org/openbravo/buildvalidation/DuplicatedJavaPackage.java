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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.database.ConnectionProvider;

/**
 * This validation is related to this issue 14143. A unique constraint has been added to
 * ad_module.javapackage column, this validation checks there are no multiple modules with the same
 * javapackage.
 */
public class DuplicatedJavaPackage extends BuildValidation {

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      String msg = "";
      boolean error = false;
      for (DuplicatedJavaPackageData err : DuplicatedJavaPackageData.duplicatedPackages(cp)) {
        msg += "\nModule name:" + err.name + " - Java package:" + err.javapackage;
        error = true;
      }
      if (error) {
        errors
            .add("You can not apply this MP because your instance fails in a pre-validation: from Openbravo ERP 2.50MP21 it is not allowed to have more than one module with the same java package. Below you can find the list of modules with duplicated java package. Once they are fixed you should be able to apply this MP."
                + msg);
      }
    } catch (Exception e) {
      return handleError(e);
    }
    return errors;
  }
}
