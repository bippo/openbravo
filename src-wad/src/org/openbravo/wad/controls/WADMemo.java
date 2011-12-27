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

import org.openbravo.wad.EditionFieldsData;
import org.openbravo.wad.FieldsData;
import org.openbravo.wad.WadUtility;
import org.openbravo.xmlEngine.XmlDocument;

public class WADMemo extends WADControl {

  public WADMemo() {
  }

  public WADMemo(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
  }

  private void generateJSCode() {
    addImport("ValidationTextArea", "../../../../../web/js/default/ValidationTextArea.js");
    if (getData("IsMandatory").equals("Y")) {
      XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
          "org/openbravo/wad/controls/WADMemoJSValidation").createXmlDocument();

      xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
      setValidation(replaceHTML(xmlDocument.print()));
    }
    setCalloutJS();
  }

  public String getType() {
    return "TextArea";
  }

  public String editMode() {
    double rowLength = ((Integer.valueOf(getData("FieldLength")).intValue() * 20) / 4000);
    if (rowLength < 3.0)
      rowLength = 3.0;
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADMemo").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", getData("CssSize"));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));

    boolean isDisabled = (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y") || getData(
        "IsUpdateable").equals("N"));
    xmlDocument.setParameter("disabled", (isDisabled ? "Y" : "N"));
    if (!isDisabled && getData("IsMandatory").equals("Y")) {
      xmlDocument.setParameter("required", "true");
      xmlDocument.setParameter("requiredClass", " required");
    } else {
      xmlDocument.setParameter("required", "false");
      xmlDocument.setParameter("requiredClass", (isDisabled ? " readonly" : ""));
    }

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    double rowLength = ((Integer.valueOf(getData("FieldLength")).intValue() * 20) / 4000);
    if (rowLength < 3.0)
      rowLength = 3.0;
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADMemo").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", getData("CssSize"));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));

    boolean isDisabled = (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y"));
    xmlDocument.setParameter("disabled", (isDisabled ? "Y" : "N"));
    if (!isDisabled && getData("IsMandatory").equals("Y")) {
      xmlDocument.setParameter("required", "true");
      xmlDocument.setParameter("requiredClass", " required");
    } else {
      xmlDocument.setParameter("required", "false");
      xmlDocument.setParameter("requiredClass", (isDisabled ? " readonly" : ""));
    }

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String toXml() {
    String[] discard = { "xx_PARAM", "xx_PARAMHIDDEN", "xx_HIDDEN" };
    if (getData("IsDisplayed").equals("N")) {
      if (getData("IsParameter").equals("Y"))
        discard[1] = "xx";
      else
        discard[2] = "xx";
    } else {
      if (getData("IsParameter").equals("Y"))
        discard[0] = "xx";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADMemoXML", discard).createXmlDocument();
    xmlDocument.setParameter("columnName", getData("ColumnName"));
    return replaceHTML(xmlDocument.print());
  }

  public String toJava() {
    return "";
  }

  public void processTable(String strTab, Vector<Object> vecFields, Vector<Object> vecTables,
      Vector<Object> vecWhere, Vector<Object> vecOrder, Vector<Object> vecParameters,
      String tableName, Vector<Object> vecTableParameters, FieldsData field,
      Vector<String> vecFieldParameters, Vector<Object> vecCounters) throws ServletException,
      IOException {
    // Override this to do nothing
  }

  public void processSelCol(String tableName, EditionFieldsData selCol, Vector<Object> vecAuxSelCol) {
    selCol.xmltext = " + ((strParam" + selCol.columnname + ".equals(\"\") || strParam"
        + selCol.columnname + ".equals(\"%\"))?\"\":\" AND ";
    if (!WadUtility.isSearchValueColumn(selCol.realcolumnname)) {
      selCol.xmltext += "C_IGNORE_ACCENT";
    }
    selCol.xmltext += "(" + tableName + "." + selCol.realcolumnname + ")";
    if (!WadUtility.isSearchValueColumn(selCol.realcolumnname)) {
      selCol.xmltext += " LIKE C_IGNORE_ACCENT('";
    } else {
      selCol.xmltext += " LIKE ('";
    }

    selCol.xmltext += "\" + strParam" + selCol.columnname + " + \"";
    selCol.xmltext += "'";
    selCol.xmltext += ") \")";

    selCol.xsqltext = "";
    if (!WadUtility.isSearchValueColumn(selCol.realcolumnname)) {
      selCol.xsqltext = "C_IGNORE_ACCENT";
    }
    selCol.xsqltext += "(" + tableName + "." + selCol.realcolumnname + ")";
    if (!WadUtility.isSearchValueColumn(selCol.realcolumnname)) {
      selCol.xsqltext += " LIKE C_IGNORE_ACCENT";
    } else {
      selCol.xsqltext += " LIKE ";
    }
    selCol.xsqltext += "(?)";
  }
}
