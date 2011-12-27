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
package org.openbravo.reference.ui;

import java.util.Properties;

import org.openbravo.erpCommon.utility.TableSQLData;

public class UIButton extends UIReference {
  public UIButton(String reference, String subreference) {
    super(reference, subreference);
  }

  protected void identifier(TableSQLData tableSql, String parentTableName, Properties field,
      String identifierName, String realName, boolean tableRef) throws Exception {
    if (field == null)
      return;

    if (field.getProperty("AD_Reference_Value_ID") != null
        && !field.getProperty("AD_Reference_Value_ID").equals("")) {
      UIList list = new UIList("17", null);
      list.identifier(tableSql, parentTableName, field, identifierName, realName, tableRef);
    } else {
      tableSql.addSelectField(parentTableName + "." + field.getProperty("ColumnName"),
          identifierName);
    }
  }

}
