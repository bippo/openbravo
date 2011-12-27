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
package org.openbravo.wad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.Sqlc;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.wad.controls.WADControl;
import org.openbravo.xmlEngine.XmlEngine;

public class WadUtility {
  private static final Logger log4j = Logger.getLogger(WadUtility.class);
  private static String[][] comparations = { { "==", " == " }, { "=", " == " }, { "!", " != " },
      { "^", " != " }, { "-", " != " } };
  private static String[][] unions = { { "|", " || " }, { "&", " && " } };

  // small cache to store mapping of <subRef + "-" + parentRef,classname>
  private static Map<String, String> referenceClassnameCache = new HashMap<String, String>();

  public WadUtility() {
    PropertyConfigurator.configure("log4j.lcf");
  }

  public static String applyFormat(String text, String reference, String sqlDateFormat) {
    // used from WADControl column identifier, keep hardcoded for core references
    if (reference.equals("15"))
      return "TO_CHAR(" + text + ", '" + sqlDateFormat + "')";
    else if (reference.equals("24"))
      return "TO_CHAR(" + text + ", 'HH24:MM:SS')";
    else if (reference.equals("16"))
      return "TO_CHAR(" + text + ", '" + sqlDateFormat + " HH24:MM:SS')";
    else
      text = "TO_CHAR(COALESCE(TO_CHAR(" + text + "), ''))";
    return text;
  }

  public static String columnIdentifier(ConnectionProvider conn, String tableName,
      boolean required, FieldsData fields, Vector<Object> vecCounters, boolean translated,
      Vector<Object> vecFields, Vector<Object> vecTable, Vector<Object> vecWhere,
      Vector<Object> vecParameters, Vector<Object> vecTableParameters, String sqlDateFormat)
      throws ServletException {
    if (fields == null)
      return "";

    if (fields.reference.equals("19") || fields.reference.equals("30")
        || fields.reference.equals("31") || fields.reference.equals("35")
        || fields.reference.equals("25") || fields.reference.equals("800011")) {
      // TableDir, Search and Locator
      // Maintain this old code for convenience, rest of code moved to WADControl subclasses

      StringBuffer texto = new StringBuffer();
      int ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
      int itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();

      itable++;
      EditionFieldsData[] dataSearchs = null;
      if (fields.reference.equals("30"))
        dataSearchs = EditionFieldsData.selectSearchs(conn, "", fields.referencevalue);
      String tableDirName = "", fieldId = "";
      if (dataSearchs == null || dataSearchs.length == 0) {
        int ilength = fields.name.length();
        if (fields.reference.equals("25"))
          tableDirName = "C_ValidCombination";
        else if (fields.reference.equals("31"))
          tableDirName = "M_Locator";
        else if (fields.reference.equals("35"))
          tableDirName = "M_AttributeSetInstance";
        else if (fields.reference.equals("800011"))
          tableDirName = "M_Product";
        else if (fields.name.equalsIgnoreCase("C_SETTLEMENT_CANCEL_ID"))
          tableDirName = "C_Settlement";
        else if (fields.name.equalsIgnoreCase("SUBSTITUTE_ID"))
          tableDirName = "M_Product";
        else
          tableDirName = fields.name.substring(0, ilength - 3);
        if (fields.reference.equals("25"))
          fieldId = "C_ValidCombination_ID";
        else if (fields.reference.equals("31"))
          fieldId = "M_Locator_ID";
        else if (fields.reference.equals("35"))
          fieldId = "M_AttributeSetInstance_ID";
        else if (fields.reference.equals("800011"))
          fieldId = "M_Product_ID";
        else if (fields.name.equalsIgnoreCase("C_SETTLEMENT_CANCEL_ID"))
          fieldId = "C_Settlement_ID";
        else if (fields.name.equalsIgnoreCase("SUBSTITUTE_ID"))
          fieldId = "M_Product_ID";
        else
          fieldId = fields.name;
      } else {
        tableDirName = dataSearchs[0].reference;
        fieldId = dataSearchs[0].columnname;
      }
      FieldsData fdi[] = FieldsData.identifierColumns(conn, tableDirName);
      if (tableName != null && tableName.length() != 0) {
        StringBuffer fieldsAux = new StringBuffer();
        for (int i = 0; i < fdi.length; i++) {
          if (!fdi[i].name.equalsIgnoreCase(fieldId))
            fieldsAux.append(", ").append(fdi[i].name);
        }
        vecTable.addElement("left join (select " + fieldId + fieldsAux.toString() + " from "
            + tableDirName + ") table" + itable + " on (" + tableName + "." + fields.name
            + " = table" + itable + "." + fieldId + ")");
      } else {
        vecTable.addElement(tableDirName + " table" + itable);
      }
      int tableId = itable;
      for (int i = 0; i < fdi.length; i++) {
        if (i > 0)
          texto.append(" || ' - ' || ");
        vecCounters.set(0, Integer.toString(itable));
        vecCounters.set(1, Integer.toString(ilist));
        texto.append(columnIdentifier(conn, "table" + tableId, required, fdi[i], vecCounters,
            translated, vecFields, vecTable, vecWhere, vecParameters, vecTableParameters,
            sqlDateFormat));
        ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
        itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();
      }
      vecCounters.set(0, Integer.toString(itable));
      vecCounters.set(1, Integer.toString(ilist));
      return texto.toString();
    } else {
      WADControl control = WadUtility.getWadControlClass(conn, fields.reference,
          fields.adReferenceValueId);
      return control.columnIdentifier(tableName, fields, vecCounters, vecFields, vecTable,
          vecWhere, vecParameters, vecTableParameters);
    }

  }

  public static String buildSQL(String clause, Vector<Object> vecParameters) {
    StringBuffer where = new StringBuffer();
    if (!clause.equals("")) {
      if (clause.indexOf('@') > -1) {
        where.append(getSQLWadContext(clause, vecParameters));
      } else {
        where.append(clause);
      }
    }
    return where.toString();
  }

  public static void setLabel(ConnectionProvider conn, WADControl auxControl, boolean isSOTrx,
      String keyName) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("processing WadUtility.setLabel() - field name: " + auxControl.getData("Name"));
    String strTableID = "", strColumnName = "", strTableName = "";

    boolean linkable = auxControl.isLink();
    if (!linkable) {
      auxControl.setData("IsLinkable", "N");
      return;
    }

    String columnId = auxControl.getLinkColumnId();

    strTableID = TableLinkData.tableId(conn, columnId);
    strColumnName = TableLinkData.columnName(conn, columnId);

    if ((strTableID.equals("") || strColumnName.equals(""))
        && !(auxControl.getData("ColumnName").equalsIgnoreCase("updatedBy") || auxControl.getData(
            "ColumnName").equalsIgnoreCase("createdBy"))) {
      log4j.warn("There're no table name or column name for: " + auxControl.getData("ColumnName")
          + " - TABLE_NAME: " + strTableName + " - COLUMN_NAME: " + strColumnName);
    }

    TableLinkData[] data1 = TableLinkData.selectWindow(conn, strTableID);
    if (data1 == null || data1.length == 0) {
      auxControl.setData("IsLinkable", "N");
      return;
    }

    String strWindowId = data1[0].adWindowId;
    if (!isSOTrx && !data1[0].poWindowId.equals(""))
      strWindowId = data1[0].poWindowId;
    TableLinkData[] data = TableLinkData.select(conn, strWindowId, strTableID);
    if (data == null || data.length == 0) {
      auxControl.setData("IsLinkable", "N");
      return;
    }

    auxControl.setData("IsLinkable", "Y");
    auxControl.setData("ColumnNameLabel", strColumnName);
    auxControl.setData("KeyColumnName", keyName);
    auxControl.setData("AD_Table_ID", strTableID);
    auxControl.setData("ColumnLabelText", strColumnName);
  }

  public static String findField(ConnectionProvider conn, EditionFieldsData[] fields,
      EditionFieldsData[] auxiliars, String fieldName) {
    if (fields == null)
      return "";
    for (int i = 0; i < fields.length; i++)
      if (fields[i].columnname.equalsIgnoreCase(fieldName))
        return fields[i].columnnameinp;
    if (auxiliars == null)
      return "";
    for (int i = 0; i < auxiliars.length; i++)
      if (auxiliars[i].columnname.equalsIgnoreCase(fieldName))
        return auxiliars[i].columnnameinp;
    return "";
  }

  public static String getSQLWadContext(String code, Vector<Object> vecParameters) {
    if (code == null || code.trim().equals(""))
      return "";
    String token;
    String strValue = code;
    StringBuffer strOut = new StringBuffer();

    int i = strValue.indexOf("@");
    String strAux, strAux1;
    while (i != -1) {
      if (strValue.length() > (i + 5) && strValue.substring(i + 1, i + 5).equalsIgnoreCase("SQL=")) {
        strValue = strValue.substring(i + 5, strValue.length());
      } else {
        // Delete the chain symbol
        strAux = strValue.substring(0, i).trim();
        if (strAux.substring(strAux.length() - 1).equals("'")) {
          strAux = strAux.substring(0, strAux.length() - 1);
          strOut.append(strAux);
        } else
          strOut.append(strValue.substring(0, i));
        strAux1 = strAux;
        if (strAux.substring(strAux.length() - 1).equals("("))
          strAux = strAux.substring(0, strAux.length() - 1).toUpperCase().trim();
        if (strAux.length() > 3
            && strAux.substring(strAux.length() - 3, strAux.length()).equals(" IN")) {
          strAux = " type=\"replace\" optional=\"true\" after=\"" + strAux1 + "\" text=\"'" + i
              + "'\"";
        } else {
          strAux = "";
        }
        strValue = strValue.substring(i + 1, strValue.length());

        int j = strValue.indexOf("@");
        if (j < 0)
          return "";

        token = strValue.substring(0, j);

        String modifier = ""; // holds the modifier (# or $) for the session value
        if (token.substring(0, 1).indexOf("#") > -1 || token.substring(0, 1).indexOf("$") > -1) {
          modifier = token.substring(0, 1);
          token = token.substring(1, token.length());
        }
        if (strAux.equals(""))
          strOut.append("?");
        else
          strOut.append("'" + i + "'");
        String parameter = "<Parameter name=\"" + token + "\"" + strAux + "/>";
        String paramElement[] = { parameter, modifier };
        vecParameters.addElement(paramElement);
        strValue = strValue.substring(j + 1, strValue.length());
        strAux = strValue.trim();
        if (strAux.length() > 0 && strAux.substring(0, 1).indexOf("'") > -1)
          strValue = strAux.substring(1, strValue.length());
      }
      i = strValue.indexOf("@");
    }
    strOut.append(strValue);
    return strOut.toString();
  }

  public static String getWadContext(String code, Vector<Object> vecFields,
      Vector<Object> vecAuxiliarFields, FieldsData[] parentsFieldsData, boolean isDefaultValue,
      String isSOTrx, String windowId) {
    if (code == null || code.trim().equals(""))
      return "";
    String token;
    String strValue = code;
    StringBuffer strOut = new StringBuffer();

    int i = strValue.indexOf("@");
    String strAux;
    while (i != -1) {
      if (strValue.length() > (i + 5) && strValue.substring(i + 1, i + 5).equalsIgnoreCase("SQL=")) {
        strValue = strValue.substring(i + 5, strValue.length());
      } else {
        strValue = strValue.substring(i + 1, strValue.length());

        int j = strValue.indexOf("@");
        if (j < 0)
          return "";

        token = strValue.substring(0, j);
        strAux = getWadContextTranslate(token, vecFields, vecAuxiliarFields, parentsFieldsData,
            isDefaultValue, isSOTrx, windowId, true);
        if (!strAux.trim().equals("") && strOut.toString().indexOf(strAux) == -1)
          strOut.append(", " + strAux);

        strValue = strValue.substring(j + 1, strValue.length());
      }
      i = strValue.indexOf("@");
    }
    return strOut.toString();
  }

  public static String getWadComboReloadContext(String code, String isSOTrx) {
    if (code == null || code.trim().equals(""))
      return "";
    String token;
    String strValue = code;
    StringBuffer strOut = new StringBuffer();

    int i = strValue.indexOf("@");
    String strAux;
    while (i != -1) {
      if (strValue.length() > (i + 5) && strValue.substring(i + 1, i + 5).equalsIgnoreCase("SQL=")) {
        strValue = strValue.substring(i + 5, strValue.length());
      } else {
        strValue = strValue.substring(i + 1, strValue.length());

        int j = strValue.indexOf("@");
        if (j < 0)
          return "";

        token = strValue.substring(0, j);
        strAux = getWadComboReloadContextTranslate(token, isSOTrx);
        if (!strAux.trim().equals("") && strOut.toString().indexOf(strAux) == -1)
          strOut.append(", " + strAux);

        strValue = strValue.substring(j + 1, strValue.length());
      }
      i = strValue.indexOf("@");
    }
    return strOut.toString();
  }

  public static String getWadComboReloadContextTranslate(String token, String isSOTrx) {
    String result = "";
    if (token.substring(0, 1).indexOf("#") > -1 || token.substring(0, 1).indexOf("$") > -1) {
      if (token.equalsIgnoreCase("#DATE"))
        result = "DateTimeData.today(this)";
      // else result = "vars.getSessionValue(\"" + token + "\")";
      else
        result = "Utility.getContext(this, vars, \"" + token + "\", windowId)";
    } else {
      String aux = Sqlc.TransformaNombreColumna(token);
      if (token.equalsIgnoreCase("ISSOTRX"))
        result = ("\"" + isSOTrx + "\"");
      else
        result = "vars.getStringParameter(\"inp" + aux + "\")";
    }
    return result;
  }

  public static String getTextWadContext(String code, Vector<Object> vecFields,
      Vector<Object> vecAuxiliarFields, FieldsData[] parentsFieldsData, boolean isDefaultValue,
      String isSOTrx, String windowId) {
    if (code == null || code.trim().equals(""))
      return "";
    String token;
    String strValue = code;
    StringBuffer strOut = new StringBuffer();

    int h = strValue.indexOf(";");
    if (h != -1) {
      StringBuffer total = new StringBuffer();
      String strFirstElement = getTextWadContext(strValue.substring(0, h), vecFields,
          vecAuxiliarFields, parentsFieldsData, isDefaultValue, isSOTrx, windowId);
      total.append("(");
      if (strValue.substring(0, h).indexOf("@") == -1)
        total.append("(\"");
      total.append(strFirstElement);
      if (strValue.substring(0, h).indexOf("@") == -1)
        total.append("\")");
      total.append(".equals(\"\")?(");
      if (strValue.substring(h + 1).indexOf("@") == -1)
        total.append("\"");
      total.append(getTextWadContext(strValue.substring(h + 1), vecFields, vecAuxiliarFields,
          parentsFieldsData, isDefaultValue, isSOTrx, windowId));
      if (strValue.substring(h + 1).indexOf("@") == -1)
        total.append("\"");
      total.append("):(");
      if (strValue.substring(0, h).indexOf("@") == -1)
        total.append("\"");
      total.append(strFirstElement);
      if (strValue.substring(0, h).indexOf("@") == -1)
        total.append("\"");
      total.append("))");
      return total.toString();
    }

    int i = strValue.indexOf("@");
    while (i != -1) {
      strOut.append(strValue.substring(0, i));
      strValue = strValue.substring(i + 1, strValue.length());

      int j = strValue.indexOf("@");
      if (j < 0)
        return "";

      token = strValue.substring(0, j);
      strOut.append(getWadContextTranslate(token, vecFields, vecAuxiliarFields, parentsFieldsData,
          isDefaultValue, isSOTrx, windowId, true));

      strValue = strValue.substring(j + 1, strValue.length());

      i = strValue.indexOf("@");
    }
    strOut.append(strValue);
    return strOut.toString();
  }

  public static String transformFieldName(String field) {
    if (field == null || field.trim().equals(""))
      return "";
    int aux = field.toUpperCase().indexOf(" AS ");
    if (aux != -1)
      return field.substring(aux + 3).trim();
    aux = field.lastIndexOf(".");
    if (aux != -1)
      return field.substring(aux + 1).trim();

    return field.trim();
  }

  public static boolean findField(Vector<Object> vecFields, String field) {
    String strAux;
    for (int i = 0; i < vecFields.size(); i++) {
      strAux = transformFieldName((String) vecFields.elementAt(i));
      if (strAux.equalsIgnoreCase(field))
        return true;
    }
    return false;
  }

  public static String getWadContextTranslate(String token, Vector<Object> vecFields,
      Vector<Object> vecAuxiliarFields, FieldsData[] parentsFieldsData, boolean isDefaultValue,
      String isSOTrx, String windowId, boolean dataMultiple) {
    if (token.substring(0, 1).indexOf("#") > -1 || token.substring(0, 1).indexOf("$") > -1) {
      if (token.equalsIgnoreCase("#DATE"))
        return "DateTimeData.today(this)";
      // else return "vars.getSessionValue(\"" + token + "\")";
      else
        return "Utility.getContext(this, vars, \"" + token + "\", windowId)";
    } else {
      String aux = Sqlc.TransformaNombreColumna(token);
      if (token.equalsIgnoreCase("ISSOTRX"))
        return ("\"" + isSOTrx + "\"");
      if (parentsFieldsData != null) {
        for (int i = 0; i < parentsFieldsData.length; i++) {
          if (parentsFieldsData[i].name.equalsIgnoreCase(token))
            return "strP" + parentsFieldsData[i].name;
        }
      }
      if (!isDefaultValue) {
        if (vecFields != null && findField(vecFields, token)) {
          return (dataMultiple ? "((dataField!=null)?dataField.getField(\"" + aux
              + "\"):((data==null || data.length==0)?\"\":data[0]." : "((data==null)?\"\":data.")
              + aux + "))";
        } else if (vecAuxiliarFields != null && findField(vecAuxiliarFields, token)) {
          return "str" + token;
        }
      }
      return "Utility.getContext(this, vars, \"" + token + "\", \"" + windowId + "\")";
    }
  }

  public static String getWadDefaultValue(ConnectionProvider pool, FieldsData fd) {
    if (fd == null)
      return "";
    WADControl control = getWadControlClass(pool, fd.reference, fd.referencevalue);
    control.setData("name", fd.name.toUpperCase());
    control.setData("required", fd.required);
    return control.getDefaultValue();
  }

  public static String displayLogic(String code, Vector<Object> vecDL,
      FieldsData[] parentsFieldsData, Vector<Object> vecAuxiliar, Vector<Object> vecFields,
      String windowId, Vector<Object> vecContext) {
    if (code == null || code.trim().equals(""))
      return "";
    String token, token2;
    String strValue = code;
    StringBuffer strOut = new StringBuffer();

    String strAux;
    StringTokenizer st = new StringTokenizer(strValue, "|&", true);
    while (st.hasMoreTokens()) {
      strAux = st.nextToken().trim();
      int i[] = getFirstElement(unions, strAux);
      if (i[0] != -1) {
        strAux = strAux.substring(0, i[0]) + unions[i[1]][1]
            + strAux.substring(i[0] + unions[i[1]][0].length());
      }

      int pos[] = getFirstElement(comparations, strAux);
      token = strAux;
      token2 = "";
      if (pos[0] >= 0) {
        token = strAux.substring(0, pos[0]);
        token2 = strAux.substring(pos[0] + comparations[pos[1]][0].length(), strAux.length());
        strAux = strAux.substring(0, pos[0]) + comparations[pos[1]][1]
            + strAux.substring(pos[0] + comparations[pos[1]][0].length(), strAux.length());
      }

      strOut.append(getDisplayLogicText(token, vecFields, parentsFieldsData, vecAuxiliar, vecDL,
          windowId, vecContext, true));
      if (pos[0] >= 0)
        strOut.append(comparations[pos[1]][1]);
      strOut.append(getDisplayLogicText(token2, vecFields, parentsFieldsData, vecAuxiliar, vecDL,
          windowId, vecContext, false));
    }
    return strOut.toString();
  }

  public static int[] getFirstElement(String[][] array, String token) {
    int min[] = { -1, -1 }, aux;
    for (int i = 0; i < array.length; i++) {
      aux = token.indexOf(array[i][0]);
      if (aux != -1 && (aux < min[0] || min[0] == -1)) {
        min[0] = aux;
        min[1] = i;
      }
    }
    return min;
  }

  public static boolean isInVector(Vector<Object> vec, String field) {
    if (field == null || field.trim().equals(""))
      return false;
    for (int i = 0; i < vec.size(); i++) {
      String aux = (String) vec.elementAt(i);
      if (aux.equalsIgnoreCase(field))
        return true;
    }
    return false;
  }

  public static void saveVectorField(Vector<Object> vec, String field) {
    if (field == null || field.trim().equals(""))
      return;
    if (!isInVector(vec, field))
      vec.addElement(field);
  }

  public static String getComboReloadText(String token, Vector<Object> vecFields,
      FieldsData[] parentsFieldsData, Vector<Object> vecComboReload, String prefix) {
    return getComboReloadText(token, vecFields, parentsFieldsData, vecComboReload, prefix, "");
  }

  public static String getComboReloadText(String token, Vector<Object> vecFields,
      FieldsData[] parentsFieldsData, Vector<Object> vecComboReload, String prefix,
      String columnname) {
    StringBuffer strOut = new StringBuffer();
    int i = token.indexOf("@");
    while (i != -1) {
      // strOut.append(token.substring(0,i));
      token = token.substring(i + 1);
      if (!token.startsWith("SQL")) {
        i = token.indexOf("@");
        if (i != -1) {
          String strAux = token.substring(0, i);
          token = token.substring(i + 1);
          if (!strOut.toString().trim().equals(""))
            strOut.append(", ");
          strOut.append(getComboReloadTextTranslate(strAux, vecFields, parentsFieldsData,
              vecComboReload, prefix, columnname));
        }
      }
      i = token.indexOf("@");
    }
    // strOut.append(token);
    return strOut.toString();
  }

  public static String getComboReloadTextTranslate(String token, Vector<Object> vecFields,
      FieldsData[] parentsFieldsData, Vector<Object> vecComboReload, String prefix,
      String columnname) {
    if (token == null || token.trim().equals(""))
      return "";
    if (!token.equalsIgnoreCase(columnname))
      saveVectorField(vecComboReload, token);
    if (parentsFieldsData != null) {
      for (int i = 0; i < parentsFieldsData.length; i++) {
        if (parentsFieldsData[i].name.equalsIgnoreCase(token))
          return ((prefix.equals("")) ? ("\"" + parentsFieldsData[i].name + "\"") : ("\"" + prefix
              + Sqlc.TransformaNombreColumna(parentsFieldsData[i].name) + "\""));
      }
    }
    if (vecFields != null && findField(vecFields, token)) {
      return ((prefix.equals("")) ? ("\"" + token + "\"") : ("\"" + prefix
          + Sqlc.TransformaNombreColumna(token) + "\""));
    }
    return ((prefix.equals("")) ? ("\"" + FormatUtilities.replace(token) + "\"") : ("\"" + prefix
        + Sqlc.TransformaNombreColumna(token) + "\""));
  }

  public static String getDisplayLogicText(String token, Vector<Object> vecFields,
      FieldsData[] parentsFieldsData, Vector<Object> vecAuxiliar, Vector<Object> vecDisplayLogic,
      String windowId, Vector<Object> vecContext, boolean save) {
    StringBuffer strOut = new StringBuffer();
    int i = token.indexOf("@");
    while (i != -1) {
      strOut.append(token.substring(0, i));
      token = token.substring(i + 1);
      i = token.indexOf("@");
      if (i != -1) {
        String strAux = token.substring(0, i);
        token = token.substring(i + 1);
        strOut.append(getDisplayLogicTextTranslate(strAux, vecFields, parentsFieldsData,
            vecAuxiliar, vecDisplayLogic, windowId, vecContext, save));
      }
      i = token.indexOf("@");
    }
    strOut.append(token);
    return strOut.toString();
  }

  public static String getDisplayLogicTextTranslate(String token, Vector<Object> vecFields,
      FieldsData[] parentsFieldsData, Vector<Object> vecAuxiliar, Vector<Object> vecDisplayLogic,
      String windowId, Vector<Object> vecContext, boolean save) {
    if (token == null || token.trim().equals(""))
      return "";
    String aux = Sqlc.TransformaNombreColumna(token);
    if (save)
      saveVectorField(vecDisplayLogic, token);
    if (parentsFieldsData != null) {
      for (int i = 0; i < parentsFieldsData.length; i++) {
        if (parentsFieldsData[i].name.equalsIgnoreCase(token))
          return "inputValue(document.frmMain.inp"
              + Sqlc.TransformaNombreColumna(parentsFieldsData[i].name) + ")";
      }
    }
    if (vecAuxiliar != null && findField(vecAuxiliar, token)) {
      return ("inputValue(document.frmMain.inp" + aux + ")");
    }
    if (vecFields != null && findField(vecFields, token)) {
      return ("inputValue(document.frmMain.inp" + aux + ")");
    }
    saveVectorField(vecContext, token);
    return "str" + FormatUtilities.replace(token);
  }

  public static String getDisplayLogicComparation(String token) {
    String aux = token.trim();
    for (int i = 0; i < comparations.length; i++) {
      if (comparations[i][0].equals(aux))
        return comparations[i][1];
    }
    return aux;
  }

  public static boolean isInFieldList(FieldsData[] fields, String columnName) {
    if (fields == null || fields.length == 0)
      return false;
    for (int i = 0; i < fields.length; i++) {
      if (fields[i].name.equalsIgnoreCase(columnName))
        return true;
    }
    return false;
  }

  public static boolean isSearchValueColumn(String name) {
    if (name == null || name.equals(""))
      return false;
    return (name.equalsIgnoreCase("Value") || name.equalsIgnoreCase("DocumentNo"));
  }

  public static String sqlCasting(ConnectionProvider conn, String reference, String referencevalue) {
    if (reference == null || reference.equals(""))
      return "";
    WADControl control = WadUtility.getWadControlClass(conn, reference, referencevalue);
    return control.getSQLCasting();
  }

  public static void setPropertyValue(Properties _prop, FieldProvider _field, String _name,
      String _fieldName, String _defaultValue) throws Exception {
    String aux = "";
    try {
      aux = _field.getField(_fieldName);
      if (aux == null || aux.equals(""))
        aux = _defaultValue;
    } catch (Exception ex) {
      if (_defaultValue == null)
        throw new Exception("Inexistent field: " + _fieldName);
      else
        aux = _defaultValue;
    }
    if (aux != null)
      _prop.setProperty(_name, aux);
  }

  public static WADControl getControl(ConnectionProvider conn, FieldProvider field,
      boolean isreadonly, String tabName, String adLanguage, XmlEngine xmlEngine,
      boolean isDisplayLogic, boolean isReloadObject, boolean isReadOnlyLogic,
      boolean hasParentsFields) throws Exception {
    return getControl(conn, field, isreadonly, tabName, adLanguage, xmlEngine, isDisplayLogic,
        isReloadObject, isReadOnlyLogic, hasParentsFields, false);
  }

  public static WADControl getControl(ConnectionProvider conn, FieldProvider field,
      boolean isreadonly, String tabName, String adLanguage, XmlEngine xmlEngine,
      boolean isDisplayLogic, boolean isReloadObject, boolean isReadOnlyLogic,
      boolean hasParentsFields, boolean isReadOnlyDefinedTab) throws Exception {
    if (field == null)
      return null;
    Properties prop = new Properties();
    setPropertyValue(prop, field, "ColumnName", "columnname", null);
    prop.setProperty("ColumnNameInp", Sqlc.TransformaNombreColumna(field.getField("columnname")));
    setPropertyValue(prop, field, "Name", "name", null);
    setPropertyValue(prop, field, "AD_Field_ID", "adFieldId", null);
    setPropertyValue(prop, field, "IsMandatory", "required", "N");
    setPropertyValue(prop, field, "AD_Reference_ID", "reference", null);
    setPropertyValue(prop, field, "ReferenceName", "referenceName", null);
    setPropertyValue(prop, field, "ReferenceNameTrl", "referenceNameTrl", "");
    setPropertyValue(prop, field, "AD_Reference_Value_ID", "referencevalue", "");
    setPropertyValue(prop, field, "AD_Val_Rule_ID", "adValRuleId", "");
    setPropertyValue(prop, field, "DisplayLength", "displaysize", "0");
    setPropertyValue(prop, field, "IsSameLine", "issameline", "N");
    setPropertyValue(prop, field, "IsDisplayed", "isdisplayed", "N");
    setPropertyValue(prop, field, "IsUpdateable", "isupdateable", "N");
    setPropertyValue(prop, field, "IsParent", "isparent", "N");
    setPropertyValue(prop, field, "FieldLength", "fieldlength", "0");
    setPropertyValue(prop, field, "AD_Column_ID", "adColumnId", "null");
    setPropertyValue(prop, field, "ColumnNameSearch", "realname", "");
    setPropertyValue(prop, field, "SearchName", "searchname", "");
    setPropertyValue(prop, field, "AD_CallOut_ID", "adCalloutId", "");
    setPropertyValue(prop, field, "ValidateOnNew", "validateonnew", "Y");
    setPropertyValue(prop, field, "CallOutName", "calloutname", "");
    setPropertyValue(prop, field, "CallOutMapping", "mappingnameCallout", "");
    setPropertyValue(prop, field, "CallOutClassName", "classnameCallout", "");
    setPropertyValue(prop, field, "AD_Process_ID", "adProcessId", "");
    setPropertyValue(prop, field, "IsReadOnly", "isreadonly", "N");
    setPropertyValue(prop, field, "DisplayLogic", "displaylogic", "");
    setPropertyValue(prop, field, "IsEncrypted", "isencrypted", "N");
    setPropertyValue(prop, field, "AD_FieldGroup_ID", "fieldgroup", "");
    setPropertyValue(prop, field, "AD_Tab_ID", "tabid", null);
    setPropertyValue(prop, field, "ValueMin", "valuemin", "");
    setPropertyValue(prop, field, "ValueMax", "valuemax", "");
    setPropertyValue(prop, field, "MappingName", "javaClassName", "");
    setPropertyValue(prop, field, "IsColumnEncrypted", "iscolumnencrypted", "");
    setPropertyValue(prop, field, "IsDesencryptable", "isdesencryptable", "");
    setPropertyValue(prop, field, "ReadOnlyLogic", "readonlylogic", "");
    setPropertyValue(prop, field, "IsAutosave", "isautosave", "Y");
    prop.setProperty("TabName", tabName);
    prop.setProperty("IsReadOnlyTab", (isreadonly ? "Y" : "N"));
    prop.setProperty("AD_Language", adLanguage);
    prop.setProperty("IsDisplayLogic", (isDisplayLogic ? "Y" : "N"));
    prop.setProperty("IsReadOnlyLogic", (isReadOnlyLogic ? "Y" : "N"));
    prop.setProperty("IsComboReload", (isReloadObject ? "Y" : "N"));
    prop.setProperty("isReadOnlyDefinedTab", (isReadOnlyDefinedTab ? "Y" : "N"));
    prop.setProperty("hasParentsFields", (hasParentsFields ? "Y" : "N"));

    WADControl _myClass = getWadControlClass(conn, field.getField("AD_Reference_ID"),
        field.getField("AD_Reference_Value_ID"));

    _myClass.setReportEngine(xmlEngine);
    _myClass.setInfo(prop);
    _myClass.initialize();

    return _myClass;
  }

  /**
   * Obtains an instance of the WAD implementator for the reference passed as parameter
   */
  public static WADControl getWadControlClass(ConnectionProvider conn, String parentRef,
      String subRef) {
    String classname;
    WADControl control;

    try {
      // lookup value from cache, if not found, search in db and put into cache
      String cacheKey = subRef + "-" + parentRef;
      classname = referenceClassnameCache.get(cacheKey);
      if (classname == null) {
        classname = WadUtilityData.getReferenceClassName(conn, subRef, parentRef);
        referenceClassnameCache.put(cacheKey, classname);
      }
    } catch (ServletException e1) {
      log4j.warn("Couldn't find reference classname ref " + parentRef + ", subRef " + subRef, e1);
      return new WADControl();
    }

    try {
      Class<?> c = Class.forName(classname);
      control = (WADControl) c.newInstance();
      control.setReference(parentRef);
      control.setSubreference(subRef);
    } catch (ClassNotFoundException ex) {
      log4j.warn("Couldn't find class: " + classname);
      control = new WADControl();
    } catch (InstantiationException e) {
      log4j.warn("Couldn't instanciate class: " + classname);
      control = new WADControl();
    } catch (IllegalAccessException e) {
      log4j.warn("Illegal access class: " + classname);
      control = new WADControl();
    }
    return control;
  }

  public static boolean isNewGroup(WADControl control, String strFieldGroup) {
    if (control == null)
      return false;
    String fieldgroup = control.getData("AD_FieldGroup_ID");
    return (control.getData("IsDisplayed").equals("Y") && fieldgroup != null
        && !fieldgroup.equals("") && !fieldgroup.equals(strFieldGroup));
  }

  public static String getReadOnlyLogic(WADControl auxControl, Vector<Object> vecDL,
      FieldsData[] parentsFieldsData, Vector<Object> vecAuxiliar, Vector<Object> vecFields,
      String windowId, Vector<Object> vecContext, boolean isreadonly) {
    String code = auxControl.getData("ReadOnlyLogic");
    if (code == null || code.equals("") || auxControl.getData("IsUpdateable").equals("N")
        || auxControl.getData("IsReadOnly").equals("Y")) {
      return "";
    }
    StringBuffer _displayLogic = new StringBuffer();
    String element = auxControl.getReadOnlyLogicColumn();

    _displayLogic
        .append("  readOnlyLogicElement('")
        .append(element)
        .append("', (")
        .append(
            displayLogic(code, vecDL, parentsFieldsData, vecAuxiliar, vecFields, windowId,
                vecContext)).append("));\n");

    return _displayLogic.toString();
  }

  public static String getbuttonShortcuts(HashMap<String, String> sc) {
    StringBuffer shortcuts = new StringBuffer();
    Iterator<String> ik = sc.keySet().iterator();
    Iterator<String> iv = sc.values().iterator();
    while (ik.hasNext() && iv.hasNext()) {
      // shortcuts.append("keyArray[keyArray.length] = new keyArrayItem(\"").append(ik.next()).append("\", \"").append(iv.next()).append("\", null, \"altKey\", false, \"onkeydown\");\n");
      shortcuts.append("keyArray[keyArray.length] = new keyArrayItem(\"").append(ik.next())
          .append("\", \"").append(iv.next())
          .append("\", null, \"altKey\", false, \"onkeydown\");\n");
    }
    return shortcuts.toString();
  }

  public static String getDisplayLogicForGroups(String strFieldGroup, StringBuffer code) {
    if ((code == null) || (code.length() == 0))
      return "";
    StringBuffer _displayLogic = new StringBuffer();
    _displayLogic.append("if ").append(code).append("{\n");
    _displayLogic.append("  displayLogicElement('fldgrp").append(strFieldGroup)
        .append("', true);\n");
    _displayLogic.append("} else {\n");
    _displayLogic.append("  displayLogicElement('fldgrp").append(strFieldGroup)
        .append("', false);\n");
    _displayLogic.append("}\n");
    return _displayLogic.toString();
  }

  public static String getDisplayLogic(WADControl auxControl, Vector<Object> vecDL,
      FieldsData[] parentsFieldsData, Vector<Object> vecAuxiliar, Vector<Object> vecFields,
      String windowId, Vector<Object> vecContext, boolean isreadonly) {
    String code = auxControl.getData("DisplayLogic");
    if (code == null || code.equals(""))
      return "";
    StringBuffer _displayLogic = new StringBuffer();
    _displayLogic.append("if (");
    _displayLogic.append(displayLogic(code, vecDL, parentsFieldsData, vecAuxiliar, vecFields,
        windowId, vecContext));
    _displayLogic.append(") {\n");
    _displayLogic.append("displayLogicElement('");
    _displayLogic.append(auxControl.getData("ColumnName"));
    _displayLogic.append("_inp_td', true);\n");
    _displayLogic.append("displayLogicElement('");
    _displayLogic.append(auxControl.getData("ColumnName"));
    _displayLogic.append("_inp', true);\n");

    _displayLogic.append(auxControl.getDisplayLogic(true, isreadonly));

    _displayLogic.append("} else {\n");
    _displayLogic.append("displayLogicElement('");
    _displayLogic.append(auxControl.getData("ColumnName"));
    _displayLogic.append("_inp_td', false);\n");
    _displayLogic.append("displayLogicElement('");
    _displayLogic.append(auxControl.getData("ColumnName"));
    _displayLogic.append("_inp', false);\n");

    _displayLogic.append(auxControl.getDisplayLogic(false, isreadonly));

    _displayLogic.append("}\n");
    return _displayLogic.toString();
  }

  public static void writeFile(File path, String filename, String text) throws IOException {
    File fileData = new File(path, filename);
    FileOutputStream fileWriterData = new FileOutputStream(fileData);
    OutputStreamWriter printWriterData = new OutputStreamWriter(fileWriterData, "UTF-8");
    printWriterData.write(text);
    printWriterData.flush();
    fileWriterData.close();
  }

  /**
   * Replaces special characters in str to make it a valid java string
   * 
   * @param str
   * @return String with special characters replaced
   */
  public static String toJavaString(String str) {
    return (str.replace("\n", "\\n").replace("\"", "\\\""));
  }

  /**
   * Returns a where parameter, this parameter can contain a modifier to decide which level of
   * session value is (# or $).
   * <p>
   * This method returns the parameter applying the modifier if exists. It can return the complete
   * parameter to be used in xsql files or just the name for the parameter with the modifier.
   * 
   * @param parameter
   *          parameter for the where clause to parse
   * @param complete
   *          return the complete parameter or just the name
   * @return the paresed parameter
   */
  public static String getWhereParameter(Object parameter, boolean complete) {
    String strParam = "";
    if (parameter instanceof String) {
      // regular parameter without modifier
      strParam = (String) parameter;
      if (!complete) {
        strParam = strParam.substring(17, strParam.lastIndexOf("\""));
      }
    } else {
      // parameter with modifier, used for session values (#, $)
      String paramElement[] = (String[]) parameter;
      if (complete) {
        strParam = paramElement[0];
      } else {
        strParam = paramElement[1]
            + paramElement[0].substring(17, paramElement[0].lastIndexOf("\""));
      }
    }
    return strParam;
  }

  public static String columnName(String name, String tableModule, String columnModule) {
    // If the column is in a different module than the table it will start with EM_
    String columnName;
    if (tableModule != null && columnModule != null && !tableModule.equals(columnModule)
        && name.toLowerCase().startsWith("em_")) {
      columnName = name.substring(3);
    } else {
      columnName = name;
    }
    return columnName;
  }
}
