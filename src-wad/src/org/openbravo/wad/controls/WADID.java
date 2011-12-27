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
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.wad.FieldsData;
import org.openbravo.wad.WadUtility;

public class WADID extends WADControl {

  public WADID() {
  }

  public WADID(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public String columnIdentifier(String tableName, FieldsData fields, Vector<Object> vecCounters,
      Vector<Object> vecFields, Vector<Object> vecTable, Vector<Object> vecWhere,
      Vector<Object> vecParameters, Vector<Object> vecTableParameters) throws ServletException {
    if (fields == null)
      return "";
    StringBuffer texto = new StringBuffer();
    int ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
    int itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();

    FieldsData fdi[] = FieldsData.identifierColumns(conn, tableName);
    for (int i = 0; i < fdi.length; i++) {
      if (i > 0)
        texto.append(" || ' - ' || ");
      vecCounters.set(0, Integer.toString(itable));
      vecCounters.set(1, Integer.toString(ilist));

      WADControl control = WadUtility.getWadControlClass(conn, fdi[i].reference,
          fdi[i].adReferenceValueId);

      texto.append(control.columnIdentifier(tableName, fdi[i], vecCounters, vecFields, vecTable,
          vecWhere, vecParameters, vecTableParameters));
      ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
      itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();
    }
    if (texto.toString().equals("")) {
      vecFields
          .addElement(((tableName != null && tableName.length() != 0) ? (tableName + ".") : "")
              + fields.name);
      texto.append(((tableName != null && tableName.length() != 0) ? (tableName + ".") : "")
          + fields.name);
    }
    vecCounters.set(0, Integer.toString(itable));
    vecCounters.set(1, Integer.toString(ilist));
    return texto.toString();
  }

  public void processTable(String strTab, Vector<Object> vecFields, Vector<Object> vecTables,
      Vector<Object> vecWhere, Vector<Object> vecOrder, Vector<Object> vecParameters,
      String tableName, Vector<Object> vecTableParameters, FieldsData field,
      Vector<String> vecFieldParameters, Vector<Object> vecCounters) throws ServletException,
      IOException {
    // Override this to do nothing
  }
}
