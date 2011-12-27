/* 
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.userinterface.skin.s250to300Comp.modulescript;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class DefaultSkin extends ModuleScript {

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      // Change skin to this one in case of installing (not updating) and the
      // used skin is 'Default' one.

      if (!DefaultSkinData.isUpdating(cp)) {
        String currentSkin = DefaultSkinData.selectCurrentSkin(cp);
        if ("Default".equals(currentSkin)) {
          DefaultSkinData.setSkin(cp, "org.openbravo.userinterface.skin.250to300Comp/250to300Comp");
        }
      }
    } catch (Exception e) {
      handleError(e);
    }
  }
}
