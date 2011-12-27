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
import org.openbravo.wad.TableRelationData;
import org.openbravo.wad.WadUtility;

public class WADTable extends WADList {

  public WADTable() {
  }

  public WADTable(Properties prop) {
    super(prop);
  }

  public boolean has2UIFields() {
    return true;
  }

  public String columnIdentifier(String tableName, FieldsData fields, Vector<Object> vecCounters,
      Vector<Object> vecFields, Vector<Object> vecTable, Vector<Object> vecWhere,
      Vector<Object> vecParameters, Vector<Object> vecTableParameters) throws ServletException {
    if (fields == null)
      return "";
    StringBuffer texto = new StringBuffer();
    int ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
    int itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();

    itable++;
    TableRelationData trd[] = TableRelationData.selectRefTable(conn, fields.referencevalue);

    if (tableName != null && tableName.length() != 0) {
      vecTable
          .addElement("left join (select "
              + trd[0].keyname
              + ((trd[0].isvaluedisplayed.equals("Y") && !trd[0].keyname.equalsIgnoreCase("value")) ? ", value"
                  : "")
              + (!trd[0].keyname.equalsIgnoreCase(trd[0].name) ? (", " + trd[0].name) : "")
              + " from " + trd[0].tablename + ") table" + itable + " on (" + tableName + "."
              + fields.name + " = " + " table" + itable + "." + trd[0].keyname + ")");
    } else {
      vecTable.addElement(trd[0].tablename + " table" + itable);
    }
    FieldsData fieldsAux = new FieldsData();
    fieldsAux.name = trd[0].name;
    fieldsAux.tablename = trd[0].tablename;
    fieldsAux.reference = trd[0].reference;
    fieldsAux.referencevalue = trd[0].referencevalue;
    fieldsAux.required = trd[0].required;
    fieldsAux.istranslated = trd[0].istranslated;
    vecCounters.set(0, Integer.toString(itable));
    vecCounters.set(1, Integer.toString(ilist));
    if (trd[0].isvaluedisplayed.equals("Y")) {
      texto.append("table" + itable + ".value || ' - ' || ");
    }

    WADControl control = WadUtility.getWadControlClass(conn, fieldsAux.reference,
        fieldsAux.adReferenceValueId);
    texto.append(control.columnIdentifier("table" + itable, fieldsAux, vecCounters, vecFields,
        vecTable, vecWhere, vecParameters, vecTableParameters));
    ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
    itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();
    return texto.toString();
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
      strFields.append(" ( ");
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
      return WADTableData.getLinkColumn(conn, getData("AD_Reference_Value_ID"));
    } catch (Exception e) {
      return "";
    }
  }
}
