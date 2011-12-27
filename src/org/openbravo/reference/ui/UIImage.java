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

public class UIImage extends UIReference {
  public UIImage(String reference, String subreference) {
    super(reference, subreference);
  }

  protected void identifier(TableSQLData tableSql, String parentTableName, Properties field,
      String identifierName, String realName, boolean tableRef) throws Exception {
    if (field == null)
      return;

    int myIndex = tableSql.index++;
    tableSql.addSelectField("((CASE td" + myIndex + ".isActive WHEN 'N' THEN '"
        + TableSQLData.INACTIVE_DATA + "' ELSE '' END) || td" + myIndex + ".imageURL)",
        identifierName);
    String tables = "(select IsActive, AD_Image_ID, ImageURL from AD_Image) td" + myIndex;
    tables += " on " + parentTableName + "." + field.getProperty("ColumnNameSearch");
    tables += " = td" + myIndex + ".AD_Image_ID";
    tableSql.addFromField(tables, "td" + myIndex, realName);
  }

  public String getGridType() {
    return "img";
  }

}
