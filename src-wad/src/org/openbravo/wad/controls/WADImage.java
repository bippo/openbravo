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
import org.openbravo.xmlEngine.XmlDocument;

public class WADImage extends WADControl {
  private static final String IMAGE_DEFAULT = "blank.gif";

  public WADImage() {
  }

  public WADImage(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    addImport("searchs", "../../../../../web/js/searchs.js");
    generateJSCode();
  }

  private void generateJSCode() {
    if (getData("IsMandatory").equals("Y")) {
      XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
          "org/openbravo/wad/controls/WADImageJSValidation").createXmlDocument();

      xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
      setValidation(replaceHTML(xmlDocument.print()));
    }
    setCalloutJS();
  }

  public String getType() {
    return "Image";
  }

  public String editMode() {
    String[] discard = { "buttonxx" };
    if (!getData("IsReadOnly").equals("Y") && !getData("IsReadOnlyTab").equals("Y")
        && !getData("IsUpdateable").equals("N"))
      discard[0] = "paramInactive";
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADImage", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("name", getData("Name"));

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    String[] discard = { "buttonxx" };
    if (!getData("IsReadOnly").equals("Y") && !getData("IsReadOnlyTab").equals("Y"))
      discard[0] = "paramInactive";
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADImage", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("name", getData("Name"));

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String toXml() {
    String[] discard = { "xx_PARAM", "xx_PARAM_R" };
    if (getData("IsParameter").equals("Y")) {
      discard[0] = "xx";
      discard[1] = "xx_R";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADImageXML", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    return replaceHTML(xmlDocument.print());
  }

  public String toJava() {
    StringBuffer text = new StringBuffer();
    if (getData("IsDisplayed").equals("Y")) {
      text.append("String strCurrentImageURL = (dataField==null?data[0].getField(\"");
      text.append(getData("ColumnNameInp")).append("\"):dataField.getField(\"");
      text.append(getData("ColumnNameInp")).append("\"));\n");
      text.append("if (strCurrentImageURL==null || strCurrentImageURL.equals(\"\")){\n");
      text.append("  xmlDocument.setParameter(\"").append(getData("ColumnName"))
          .append("Class\", \"Image_NotAvailable_medium\");\n");
      text.append("  if (dataField==null) data[0].adImageIdr=\"blank.gif\";\n");
      text.append("}\n");
    }
    return text.toString();
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

    ilist++;
    if (tableName != null && tableName.length() != 0) {
      vecTable.addElement("left join (select AD_Image_ID, ImageURL from AD_Image) list" + ilist
          + " on (" + tableName + "." + fields.name + " = list" + ilist + ".AD_Image_ID) ");
    } else {
      vecTable.addElement("AD_Image list" + ilist);
    }
    texto.append("list").append(ilist).append(".ImageURL");
    vecFields.addElement(texto.toString());
    vecCounters.set(0, Integer.toString(itable));
    vecCounters.set(1, Integer.toString(ilist));
    return texto.toString();
  }

  public int addAdditionDefaulSQLFields(Vector<Object> v, FieldsData fieldsDef, int itable) {
    if (fieldsDef.isdisplayed.equals("Y")) { // Image
      final FieldsData fd = new FieldsData();
      fd.adcolumnid = fieldsDef.adcolumnid + "_" + (itable++);
      fd.name = fieldsDef.columnname + "R";
      final String tableN = "AD_Image";
      fieldsDef.name = fieldsDef.name;
      final Vector<Object> vecTables2 = new Vector<Object>();
      final Vector<Object> vecWhere2 = new Vector<Object>();
      vecTables2.addElement(tableN + " table1");
      final StringBuffer strFields2 = new StringBuffer();
      strFields2.append(" ( table1.ImageURL ) AS ").append(fieldsDef.columnname);
      final StringBuffer fields = new StringBuffer();
      fields.append("SELECT ").append(strFields2);
      fields.append(" FROM ");
      for (int j = 0; j < vecTables2.size(); j++) {
        fields.append(vecTables2.elementAt(j));
      }
      fields.append(" WHERE table1.isActive='Y'");
      for (int j = 0; j < vecWhere2.size(); j++) {
        fields.append(vecWhere2.elementAt(j));
      }
      fields.append(" AND table1." + fieldsDef.name + " = ? ");
      fd.defaultvalue = fields.toString();
      fd.whereclause = "<Parameter name=\"" + fd.name + "\"/>";
      v.addElement(fd);
    }
    return itable;
  }

  public int addAdditionDefaulJavaFields(StringBuffer strDefaultValues, FieldsData fieldsDef,
      String tabName, int itable) {
    if (fieldsDef.isdisplayed.equals("Y")) {
      strDefaultValues.append(", " + tabName + "Data.selectDef" + fieldsDef.adcolumnid + "_"
          + (itable++) + "(this, " + fieldsDef.defaultvalue + ")");
    }
    return itable;
  }

  public void processTable(String strTab, Vector<Object> vecFields, Vector<Object> vecTables,
      Vector<Object> vecWhere, Vector<Object> vecOrder, Vector<Object> vecParameters,
      String tableName, Vector<Object> vecTableParameters, FieldsData field,
      Vector<String> vecFieldParameters, Vector<Object> vecCounters) throws ServletException,
      IOException {
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
      vecFields.addElement("(CASE WHEN " + tableName + "." + field.name + " IS NULL THEN '"
          + IMAGE_DEFAULT + "' ELSE " + strFields.toString() + ") END) AS " + field.name + "R");
    }
  }
}
