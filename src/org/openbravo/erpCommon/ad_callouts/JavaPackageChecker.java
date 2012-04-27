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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import javax.lang.model.SourceVersion;
import javax.servlet.ServletException;

import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;

/**
 * This class implements the logic to validate the java package of a module.
 * 
 * @author guilleaer
 * 
 */
public class JavaPackageChecker extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String strPackage = info.getStringParameter("inpjavapackage", null);
    if (!isValidName(strPackage)) {
      info.addResult(
          "WARNING",
          Utility.messageBD(this, "javaPackageError", OBContext.getOBContext().getLanguage()
              .getLanguage()));
    }
  }

  /**
   * Returns a boolean which indicates if the java package name is valid. The method tries to verify
   * that the java package name is valid, also looks for java reserved words which are not accepted
   * for the java package name.
   * 
   * @param javaPackageString
   *          String with the java package inserted by the user.
   * @return true if the java package is correct.
   */
  private boolean isValidName(String javaPackageString) {
    for (String s : javaPackageString.split("\\.", -1)) {
      if (!SourceVersion.isName(s)) {
        return false;
      }
    }
    return true;
  }

}
