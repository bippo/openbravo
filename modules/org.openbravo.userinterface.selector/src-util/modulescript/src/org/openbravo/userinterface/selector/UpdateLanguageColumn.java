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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.userinterface.selector;

import java.sql.Statement;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class UpdateLanguageColumn extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(UpdateLanguageColumn.class);

  @Override
  // obuisel_selector_field_trl table have & must use ad_language column instead of ad_language_id
  // https://issues.openbravo.com/view.php?id=14226
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      Statement fieldStatement = cp.getStatement();
      fieldStatement
          .execute("update obuisel_selector_field_trl trl set ad_language = (select lan.ad_language from ad_language lan where lan.ad_language_id=trl.ad_language_id) where trl.ad_language is null");
      try {
        if (!fieldStatement.isClosed()) {
          fieldStatement.close();
        }
      } catch (Throwable t) {
        // ignore on purpose
      }

      Statement selectorStatement = cp.getStatement();
      selectorStatement
          .execute("update obuisel_selector_trl trl set ad_language = (select lan.ad_language from ad_language lan where lan.ad_language_id=trl.ad_language_id) where trl.ad_language is null");
      try {
        if (!selectorStatement.isClosed()) {
          selectorStatement.close();
        }
      } catch (Throwable t) {
        // ignore on purpose
      }
    } catch (Throwable t) {
      log4j.error("Error executing moduleScript: " + t.getMessage());
    }

  }
}