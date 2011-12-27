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
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.wad.EditionFieldsData;
import org.openbravo.wad.FieldsData;
import org.openbravo.wad.TableRelationData;
import org.openbravo.wad.WadUtility;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.xmlEngine.XmlEngine;

public class WADControl {
  private Hashtable<String, String> data = new Hashtable<String, String>();
  private Vector<String[]> css = new Vector<String[]>();
  private Vector<String[]> imports = new Vector<String[]>();
  private Vector<String[]> jsCode = new Vector<String[]>();
  XmlEngine xmlEngine;
  private String validation = "";
  private String onload = "";
  protected static ConnectionProvider conn = null;
  protected static String sqlDateFormat;
  protected String reference;
  protected String subreference;

  private WADLabelControl label;

  public WADControl() {
  }

  public void setData(String name, String value) {
    if (name == null)
      return;
    if (this.data == null)
      this.data = new Hashtable<String, String>();
    if (value == null || value.equals(""))
      this.data.remove(name.toUpperCase());
    else
      this.data.put(name.toUpperCase(), value);
  }

  public String getData(String name) {
    String aux = data.get(name.toUpperCase());
    if (aux == null)
      aux = "";
    return aux;
  }

  public void setInfo(Properties prop) {
    if (prop == null)
      return;
    for (Enumeration<?> e = prop.propertyNames(); e.hasMoreElements();) {
      String _name = (String) e.nextElement();
      setData(_name, prop.getProperty(_name));
    }
  }

  public static void setConnection(ConnectionProvider _conn) {
    conn = _conn;
  }

  public ConnectionProvider getConnection() {
    return conn;
  }

  public void setReportEngine(XmlEngine _xmlEngine) {
    this.xmlEngine = _xmlEngine;
  }

  public XmlEngine getReportEngine() {
    return this.xmlEngine;
  }

  public void initialize() {
    generateJSCode();
  }

  public void addCSSImport(String name, String _data) {
    if (css == null)
      css = new Vector<String[]>();
    String[] aux = new String[2];
    aux[0] = name;
    aux[1] = _data;
    css.addElement(aux);
  }

  public void addImport(String name, String _data) {
    if (imports == null)
      imports = new Vector<String[]>();
    String[] aux = new String[2];
    aux[0] = name;
    aux[1] = _data;
    imports.addElement(aux);
  }

  public void addJSCode(String name, String _code) {
    if (jsCode == null)
      jsCode = new Vector<String[]>();
    String[] aux = new String[2];
    aux[0] = name;
    aux[1] = _code;
    jsCode.addElement(aux);
  }

  public void setValidation(String _code) {
    validation = _code;
  }

  public String getValidation() {
    return validation;
  }

  public void setOnLoad(String _code) {
    onload = _code;
  }

  public String getOnLoad() {
    return onload;
  }

  public Vector<String[]> getJSCode() {
    return jsCode;
  }

  public Vector<String[]> getImport() {
    return imports;
  }

  public Vector<String[]> getCSSImport() {
    return css;
  }

  protected String replaceHTML(String text) {
    text = text.replace("<HTML>", "");
    text = text.replace("<HEAD>", "");
    text = text.replace("<BODY>", "");
    text = text.replace("</BODY>", "");
    text = text.replace("</HTML>", "");
    text = text.replace("</HEAD>", "");
    text = text.replace("<html>", "");
    text = text.replace("<head>", "");
    text = text.replace("<body>", "");
    text = text.replace("</body>", "");
    text = text.replace("</html>", "");
    text = text.replace("</head>", "");
    return text;
  }

  private void generateJSCode() {
    addImport("ValidationTextBox", "../../../../../web/js/default/ValidationTextBox.js");
    if (getData("IsMandatory").equals("Y")) {
      XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
          "org/openbravo/wad/controls/WADControlJSValidation").createXmlDocument();
      xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
      setValidation(replaceHTML(xmlDocument.print()));
    }
    setCalloutJS();
  }

  public void setCalloutJS() {
    String callout = getData("CallOutName");
    if (callout != null && !callout.equals("")) {
      XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
          "org/openbravo/wad/controls/WADControlJS").createXmlDocument();
      xmlDocument.setParameter("calloutName", callout);
      xmlDocument.setParameter("calloutMapping", getData("CallOutMapping"));
      addJSCode("callout" + callout, replaceHTML(xmlDocument.print()));
    }
  }

  public String getOnChangeCode() {
    StringBuffer text = new StringBuffer();
    if (getData("IsDisplayLogic").equals("Y"))
      text.append("displayLogic();");
    if (getData("IsReadOnlyLogic").equals("Y"))
      text.append("readOnlyLogic();");
    String callout = getData("CallOutName");
    String isComboReload = getData("IsComboReload");
    if (isComboReload == null || isComboReload.equals(""))
      isComboReload = "N";
    if (callout != null && !callout.equals(""))
      text.append("callout").append(callout).append("(this.name);");
    if (isComboReload.equals("Y"))
      text.append("reloadComboReloads").append(getData("AD_Tab_ID")).append("(this.name);");
    return text.toString();
  }

  public String editMode() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADControl").createXmlDocument();

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
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADControl").createXmlDocument();

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

  public String getType() {
    if (getData("IsDisplayed").equals("N"))
      return "Hidden";
    else
      return "TextBox";
  }

  public String toString() {
    StringBuffer text = new StringBuffer();
    if (getData("IsDisplayed").equals("N")) {
      text.append(getHiddenHTML());
    } else {
      text.append("<div id=\"editDiscard\">");
      text.append(editMode()).append("");
      text.append("</div>");
      text.append("<div id=\"newDiscard\">");
      text.append(newMode()).append("");
      text.append("</div>");
    }
    return text.toString();
  }

  public String toLabel() {
    if (getData("AD_Reference_ID").equals("28"))
      return "";
    String[] discard = { "isNotLinkable" };
    String isLinkable = getData("IsLinkable");
    if (isLinkable == null || !isLinkable.equals("Y"))
      discard[0] = "isLinkable";

    createWADLabelControl();
    WadControlLabelBuilder builder = new WadControlLabelBuilder(label);
    builder.buildLabelControl();
    return builder.getLabelString();
  }

  public String toJava() {
    return "";
  }

  public String toXml() {
    StringBuffer text = new StringBuffer();
    if (getData("IsParameter").equals("Y")) {
      text.append("<PARAMETER id=\"").append(getData("ColumnName"));
      text.append("\" name=\"").append(getData("ColumnName"));
      text.append("\" attribute=\"value\" default=\"N\" replaceCharacters=\"htmlPreformated\"/>");
    } else {
      text.append("<FIELD id=\"").append(getData("ColumnName"));
      text.append("\" attribute=\"value\" replaceCharacters=\"htmlPreformated\" default=\"N\">");
      text.append(getData("ColumnName")).append("</FIELD>");
    }
    return text.toString();
  }

  public String toLabelXML() {
    StringBuffer labelText = new StringBuffer();
    createWADLabelControl();
    if (label.getLabelId() != null && !label.getLabelId().equals("")
        && label.getLabelPlaceHolderText() != null && !label.getLabelPlaceHolderText().equals("")) {
      labelText.append("<LABEL id=\"").append(label.getLabelId());
      labelText.append("\" name=\"").append(label.getLabelId());
      labelText.append("\" replace=\"" + label.getLabelPlaceHolderText() + "\">");
      labelText.append(label.getColumnName()).append("lbl");
      labelText.append("</LABEL>");
    } else {
      labelText.append("");
    }

    return labelText.toString();
  }

  private void createWADLabelControl() {
    String column = getData("ColumnName");
    String labelText = getData("ColumnLabelText");
    if (labelText.trim().equals(""))
      labelText = column;
    String columnId = getData("AdColumnId");
    String columnLink = getData("ColumnNameLabel");
    if (columnLink == null || columnLink.equals(""))
      columnLink = getData("ColumnName");
    label = new WADLabelControl(WADLabelControl.FIELD_LABEL, null, null, columnId, column, null,
        null, getData("IsLinkable"), getData("KeyColumnName"), getData("ColumnNameInp"),
        getData("AD_Table_ID"), columnLink);
  }

  /**
   * Checks whether the reference is a numeric value
   * 
   * @return true in case the reference is numeric
   */
  public boolean isNumericType() {
    return false;
  }

  /**
   * Checks whether there are two ui fields for the control. These 2 fields are used in case the
   * displayed value is different than the actual one in database, for example for list or table
   * references.
   * 
   * @return true in case there are 2 ui fields
   */
  public boolean has2UIFields() {
    return false;
  }

  /**
   * Generates SQL identifier
   */
  public String columnIdentifier(String tableName, FieldsData field, Vector<Object> vecCounters,
      Vector<Object> vecFields, Vector<Object> vecTable, Vector<Object> vecWhere,
      Vector<Object> vecParameters, Vector<Object> vecTableParameters) throws ServletException {
    if (field == null)
      return "";
    StringBuffer texto = new StringBuffer();
    int ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
    int itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();
    if ("Y".equals(field.istranslated)
        && TableRelationData.existsTableColumn(conn, field.tablename + "_TRL", field.name)) {
      FieldsData fdi[] = FieldsData.tableKeyColumnName(conn, field.tablename);
      if (fdi == null || fdi.length == 0) {
        vecFields.addElement(WadUtility.applyFormat(
            ((tableName != null && tableName.length() != 0) ? (tableName + ".") : "") + field.name,
            field.reference, sqlDateFormat));
        texto.append(WadUtility.applyFormat(
            ((tableName != null && tableName.length() != 0) ? (tableName + ".") : "") + field.name,
            field.reference, sqlDateFormat));
      } else {
        vecTable.addElement("left join (select " + fdi[0].name + ",AD_Language"
            + (!fdi[0].name.equalsIgnoreCase(field.name) ? (", " + field.name) : "") + " from "
            + field.tablename + "_TRL) tableTRL" + itable + " on (" + tableName + "." + fdi[0].name
            + " = tableTRL" + itable + "." + fdi[0].name + " and tableTRL" + itable
            + ".AD_Language = ?) ");
        vecTableParameters.addElement("<Parameter name=\"paramLanguage\"/>");
        vecFields.addElement(WadUtility.applyFormat("(CASE WHEN tableTRL" + itable + "."
            + field.name + " IS NULL THEN TO_CHAR(" + tableName + "." + field.name
            + ") ELSE TO_CHAR(tableTRL" + itable + "." + field.name + ") END)", field.reference,
            sqlDateFormat));
        texto.append(WadUtility.applyFormat("(CASE WHEN tableTRL" + itable + "." + field.name
            + " IS NULL THEN TO_CHAR(" + tableName + "." + field.name + ") ELSE TO_CHAR(tableTRL"
            + itable + "." + field.name + ") END)", field.reference, sqlDateFormat));
      }
    } else {
      vecFields.addElement(WadUtility.applyFormat(
          ((tableName != null && tableName.length() != 0) ? (tableName + ".") : "") + field.name,
          field.reference, sqlDateFormat));
      texto.append(WadUtility.applyFormat(
          ((tableName != null && tableName.length() != 0) ? (tableName + ".") : "") + field.name,
          field.reference, sqlDateFormat));
    }
    vecCounters.set(0, Integer.toString(itable));
    vecCounters.set(1, Integer.toString(ilist));
    return texto.toString();
  }

  /**
   * Adds to the vector the additional SQL default fields. This is used for search element which
   * have a calculated addition to UI field. Whenever this method is overridden it should also be
   * overridden the {@link #addAdditionDefaulJavaFields(StringBuffer, FieldsData, String, int)}
   * method
   */
  public int addAdditionDefaulSQLFields(Vector<Object> v, FieldsData fieldsDef, int itable) {
    return itable;
  }

  /**
   * Adds to the vector the additional Java logic default fields. This is used for search element
   * which have a calculated addition to UI field. Whenever this method is overridden it should also
   * be overridden the {@link #addAdditionDefaulSQLFields(Vector, FieldsData, int)} method
   */
  public int addAdditionDefaulJavaFields(StringBuffer strDefaultValues, FieldsData fieldsDef,
      String tabName, int itable) {
    return itable;
  }

  /**
   * Obtains the SQL casting depending on the data type
   * 
   */
  public String getSQLCasting() {
    return "";
  }

  /**
   * Prepares SQL query calculating all fields and parameters required
   * 
   * @param vecCounters
   */
  public void processTable(String strTab, Vector<Object> vecFields, Vector<Object> vecTables,
      Vector<Object> vecWhere, Vector<Object> vecOrder, Vector<Object> vecParameters,
      String tableName, Vector<Object> vecTableParameters, FieldsData field,
      Vector<String> vecFieldParameters, Vector<Object> vecCounters) throws ServletException,
      IOException {
    String strOrder = tableName + "." + field.name;

    final String[] aux = { new String(field.name),
        new String(strOrder + (field.name.equalsIgnoreCase("DocumentNo") ? " DESC" : "")) };
    vecOrder.addElement(aux);
  }

  public static void setDateFormat(String dateFormat) {
    sqlDateFormat = dateFormat;
  }

  /**
   * Processes selection columns.
   */
  public void processSelCol(String tableName, EditionFieldsData selCol, Vector<Object> vecAuxSelCol) {
    selCol.xmltext = " + ((strParam" + selCol.columnname + ".equals(\"\") || strParam"
        + selCol.columnname + ".equals(\"%\"))?\"\":\" AND ";

    selCol.xmltext += "(" + tableName + "." + selCol.realcolumnname + ")";

    selCol.xmltext += " = (";
    if (isText())
      selCol.xmltext += "'";

    selCol.xmltext += "\" + strParam" + selCol.columnname + " + \"";
    if (isText()) {
      selCol.xmltext += "'";
    }
    selCol.xmltext += ") \")";
    selCol.xsqltext = "";

    selCol.xsqltext += "(" + tableName + "." + selCol.realcolumnname + ")";

    selCol.xsqltext += " = ";

    selCol.xsqltext += "(?)";

  }

  /**
   * Determines whether the reference is a foreign key to another table. Used to generate links
   * 
   */
  public boolean isLink() {
    return false;
  }

  /**
   * Determines whether the reference is text
   * 
   */
  public boolean isText() {
    return false;
  }

  /**
   * In case the control is a link this method should return the id for the column the link is for
   */
  public String getLinkColumnId() {
    return "";
  }

  /**
   * Returns the display javascript logic
   * 
   */
  public String getDisplayLogic(boolean display, boolean isreadonly) {
    StringBuffer displayLogic = new StringBuffer();

    displayLogic.append("displayLogicElement('");
    displayLogic.append(getData("ColumnName"));
    displayLogic.append("_lbl_td', ").append(display ? "true" : "false").append(");\n");
    displayLogic.append("displayLogicElement('");
    displayLogic.append(getData("ColumnName"));
    displayLogic.append("_lbl', ").append(display ? "true" : "false").append(");\n");

    return displayLogic.toString();
  }

  /**
   * Obtains default value for the field
   */
  public String getDefaultValue() {
    return "";
  }

  public boolean isDate() {
    return false;
  }

  public boolean isTime() {
    return false;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getSubreference() {
    return subreference;
  }

  public void setSubreference(String subreference) {
    this.subreference = subreference;
  }

  /**
   * Returns HTML needed for hidden fields
   */
  public String getHiddenHTML() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADHidden").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));

    return replaceHTML(xmlDocument.print());
  }

  /**
   * Returns XML needed for hidden fields
   */
  public String getHiddenXML() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADHiddenXML").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    return replaceHTML(xmlDocument.print());
  }

  /**
   * Returns the HTML element to set in the read only logic
   */
  public String getReadOnlyLogicColumn() {
    return getData("columnName");
  }
}
