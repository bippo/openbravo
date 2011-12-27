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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.wad.controls;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.wad.FieldsData;
import org.openbravo.wad.WadUtility;

public class WADTableDir extends WADList {

  public WADTableDir() {
  }

  public WADTableDir(Properties prop) {
    super(prop);
  }

  public boolean has2UIFields() {
    return true;
  }

  public void processTable(String strTab, Vector<Object> vecFields, Vector<Object> vecTables,
      Vector<Object> vecWhere, Vector<Object> vecOrder, Vector<Object> vecParameters,
      String tableName, Vector<Object> vecTableParameters, FieldsData field,
      Vector<String> vecFieldParameters, Vector<Object> vecCounters) throws ServletException,
      IOException {
    String strOrder = "";
    if (field.isdisplayed.equals("Y")) {
      final Vector<Object> vecSubFields = new Vector<Object>();
      WadUtility.columnIdentifier(conn, tableName, field.required.equals("Y"), field, vecCounters,
          false, vecSubFields, vecTables, vecWhere, vecParameters, vecTableParameters,
          sqlDateFormat);

      final StringBuffer strFields = new StringBuffer();
      strFields.append(" (");
      boolean boolFirst = true;
      for (final Enumeration<Object> e = vecSubFields.elements(); e.hasMoreElements();) {
        final String tableField = (String) e.nextElement();
        if (boolFirst) {
          boolFirst = false;
        } else {
          strFields.append(" || ' - ' || ");
        }
        strFields.append("COALESCE(TO_CHAR(").append(tableField).append("),'') ");
      }
      strOrder = strFields.toString() + ")";
      vecFields.addElement("(CASE WHEN " + tableName + "." + field.name + " IS NULL THEN '' ELSE "
          + strFields.toString() + ") END) AS " + field.name + "R");
    } else {
      strOrder = tableName + "." + field.name;
    }

    final String[] aux = { new String(field.name),
        new String(strOrder + (field.name.equalsIgnoreCase("DocumentNo") ? " DESC" : "")) };
    vecOrder.addElement(aux);

  }

  public boolean isLink() {
    return true;
  }

  public String getLinkColumnId() {
    try {

      String strTableName = getData("ColumnNameSearch");
      strTableName = strTableName.substring(0, (strTableName.length() - 3));
      return WADSearchData.getLinkColumn(conn, strTableName);

    } catch (Exception e) {
      return "";
    }
  }
}
