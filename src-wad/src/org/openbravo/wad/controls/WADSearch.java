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
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.data.Sqlc;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.wad.EditionFieldsData;
import org.openbravo.wad.FieldsData;
import org.openbravo.wad.WadUtility;
import org.openbravo.xmlEngine.XmlDocument;

public class WADSearch extends WADControl {
  public WADControl button;
  public String command = "";
  public String hiddenFields = "";
  public String imageName = "";
  public String searchName = "";
  public boolean isFieldEditable = true;

  public WADSearch() {
  }

  public WADSearch(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    addImport("ValidationTextBox", "../../../../../web/js/default/ValidationTextBox.js");
    addImport("searchs", "../../../../../web/js/searchs.js");
    generateJSCode();
    this.button = new WADFieldButton(this.imageName, getData("ColumnName"),
        getData("ColumnNameInp"), this.searchName, this.command);
    if (getData("AD_Reference_Value_ID").equals("21"))
      this.isFieldEditable = false;
  }

  private void generateJSCode() {
    StringBuffer validation = new StringBuffer();
    if (getData("IsMandatory").equals("Y")) {
      validation.append("  if (inputValue(frm.inp").append(getData("ColumnNameInp"))
          .append(")==null || inputValue(frm.inp").append(getData("ColumnNameInp"))
          .append(")==\"\") {\n");
      if (getData("IsDisplayed").equals("Y"))
        validation.append("    setWindowElementFocus(frm.inp").append(getData("ColumnNameInp"))
            .append("_R);\n");
      validation.append("    showJSMessage(1);\n");
      validation.append("    return false;\n");
      validation.append("  }\n");
    }
    setValidation(validation.toString());
    setCalloutJS();
    {
      String text = "function debugSearch(key, text, keyField) {\n" + "  return true;\n" + "}";
      addJSCode("debugSearch", text);
    }
    if (!getData("IsReadOnly").equals("Y") && !getData("IsReadOnlyTab").equals("Y")) {
      StringBuffer columnsScript = new StringBuffer();
      StringBuffer commandScript = new StringBuffer();
      StringBuffer hiddenScript = new StringBuffer();
      StringBuffer text = new StringBuffer();
      if (this.imageName == null || this.imageName.equals(""))
        this.imageName = FormatUtilities.replace(getData("searchName"));
      if (this.searchName == null || this.searchName.equals(""))
        this.searchName = getData("Name");
      String servletName = "/info/" + this.imageName + ".html";
      try {
        if (!getData("AD_Reference_Value_ID").equals("")) {
          WADSearchData[] data = WADSearchData.select(getConnection(), getData("AD_Language"),
              getData("AD_Reference_Value_ID"));
          if (data != null && data.length > 0) {
            servletName = data[0].mappingname;
            this.searchName = data[0].referenceNameTrl;
            // this.imageName =
            // FormatUtilities.replace(data[0].referenceName) +
            // ".gif";
            if (!servletName.startsWith("/"))
              servletName = '/' + servletName;
            for (int i = 0; i < data.length; i++) {
              if (data[i].columntype.equals("I")) {
                columnsScript.append(", 'inp").append(data[i].name).append("'");
                columnsScript.append(", inputValue(document.frmMain.inp")
                    .append(Sqlc.TransformaNombreColumna(data[i].columnname)).append(')');
              } else {
                hiddenScript.append("<input type=\"hidden\" name=\"inp").append(
                    Sqlc.TransformaNombreColumna(data[i].name));
                hiddenScript.append(data[i].columnSuffix).append("\" value=\"\" ");
                hiddenScript.append("id=\"").append(data[i].columnname)
                    .append(data[i].columnSuffix).append("\"/>\n");
              }
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      commandScript.append("openSearch(null, null, '..").append(servletName).append("', ");
      commandScript.append("null, false, 'frmMain', 'inp").append(getData("ColumnNameInp"))
          .append("', ");
      commandScript.append("'inp").append(getData("ColumnNameInp")).append("_R', ");
      commandScript.append("inputValue(document.frmMain.inp").append(getData("ColumnNameInp"))
          .append("_R), ");
      commandScript.append("'inpIDValue', inputValue(document.frmMain.inp")
          .append(getData("ColumnNameInp")).append("), ");
      commandScript.append("'WindowID', inputValue(document.frmMain.inpwindowId)");
      commandScript.append(columnsScript);
      text.append(commandScript).append(", 'Command', 'KEY'");
      commandScript.append(");");
      text.append(");");
      setOnLoad("keyArray[keyArray.length] = new keyArrayItem(\"ENTER\", \"" + text.toString()
          + "\", \"inp" + getData("ColumnNameInp") + "_R\", \"null\");");
      this.command = commandScript.toString();
      this.hiddenFields = hiddenScript.toString();
    }
  }

  public String getType() {
    return "TextBox_btn";
  }

  public String editMode() {
    String textButton = "";
    String buttonClass = "";
    String tabIndex = "";

    if (getData("IsReadOnly").equals("N") && getData("IsReadOnlyTab").equals("N")
        && getData("IsUpdateable").equals("Y")) {
      this.button.setReportEngine(getReportEngine());
      textButton = this.button.toString();
      buttonClass = this.button.getType();
      if (!this.isFieldEditable) {
        tabIndex = "1";
      }
    }
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }

    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADSearch", discard).createXmlDocument();
    xmlDocument.setParameter("tabindex", tabIndex);
    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));
    xmlDocument.setParameter("hiddens", this.hiddenFields);
    xmlDocument.setParameter("hasButton", (textButton.equals("") ? "TextButton_ContentCell" : ""));
    xmlDocument.setParameter("buttonClass", buttonClass + "_ContentCell");
    xmlDocument.setParameter("button", textButton);
    String className = "";
    boolean isDisabled = (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y") || getData(
        "IsUpdateable").equals("N"));
    if (!isDisabled && !this.isFieldEditable && getData("IsMandatory").equals("Y"))
      className += " readonly_required";
    else if (isDisabled || !this.isFieldEditable)
      className += " readonly";
    else if (getData("IsMandatory").equals("Y"))
      className += " required";
    xmlDocument.setParameter("className", className);

    if (isDisabled || !this.isFieldEditable)
      xmlDocument.setParameter("disabled", "Y");
    else
      xmlDocument.setParameter("disabled", "N");
    if (getData("IsMandatory").equals("Y"))
      xmlDocument.setParameter("required", "true");
    else
      xmlDocument.setParameter("required", "false");
    xmlDocument.setParameter("textBoxCSS", (isDisabled ? "_ReadOnly" : ""));

    xmlDocument.setParameter("callout", getOnChangeCode());
    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    String textButton = "";
    String buttonClass = "";
    String tabIndex = "";
    if (getData("IsReadOnly").equals("N") && getData("IsReadOnlyTab").equals("N")) {
      this.button.setReportEngine(getReportEngine());
      textButton = this.button.toString();
      buttonClass = this.button.getType();
      if (!this.isFieldEditable) {
        tabIndex = "1";
      }
    }
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADSearch", discard).createXmlDocument();
    xmlDocument.setParameter("tabindex", tabIndex);
    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));
    xmlDocument.setParameter("hiddens", this.hiddenFields);
    xmlDocument.setParameter("hasButton", (textButton.equals("") ? "TextButton_ContentCell" : ""));
    xmlDocument.setParameter("buttonClass", buttonClass + "_ContentCell");
    xmlDocument.setParameter("button", textButton);
    String className = "";
    boolean isDisabled = (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y"));
    if (isDisabled || !this.isFieldEditable)
      className += " readonly";
    else if (getData("IsMandatory").equals("Y"))
      className += " required";
    xmlDocument.setParameter("className", className);

    if (isDisabled || !this.isFieldEditable)
      xmlDocument.setParameter("disabled", "Y");
    else
      xmlDocument.setParameter("disabled", "N");
    if (getData("IsMandatory").equals("Y"))
      xmlDocument.setParameter("required", "true");
    else
      xmlDocument.setParameter("required", "false");
    xmlDocument.setParameter("textBoxCSS", (isDisabled ? "_ReadOnly" : ""));

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
        "org/openbravo/wad/controls/WADSearchXML", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    return replaceHTML(xmlDocument.print());
  }

  public String toJava() {
    return "";
  }

  public boolean has2UIFields() {
    return true;
  }

  public int addAdditionDefaulSQLFields(Vector<Object> v, FieldsData fieldsDef, int itable) {
    if (fieldsDef.isdisplayed.equals("Y")) {
      final FieldsData fd = new FieldsData();
      fd.adcolumnid = fieldsDef.adcolumnid + "_" + (itable++);
      fd.name = fieldsDef.columnname + "R";
      String tableN = "";

      HashMap<String, String> tableColumnName = getTableColumnName(fieldsDef);
      tableN = tableColumnName.get("table");
      fieldsDef.name = tableColumnName.get("column");

      final Vector<Object> vecFields2 = new Vector<Object>();
      final Vector<Object> vecTables2 = new Vector<Object>();
      final Vector<Object> vecWhere2 = new Vector<Object>();

      final Vector<Object> vecCounters = new Vector<Object>();
      vecCounters.addElement("1");
      vecCounters.addElement("0");
      final Vector<Object> vecParameters = new Vector<Object>();
      final Vector<Object> vecTableParameters = new Vector<Object>();
      try {
        WadUtility.columnIdentifier(conn, tableN, false, fieldsDef, vecCounters, false, vecFields2,
            vecTables2, vecWhere2, vecParameters, vecTableParameters, sqlDateFormat);
      } catch (ServletException e2) {
        e2.printStackTrace();
      }

      final StringBuffer strFields2 = new StringBuffer();
      strFields2.append(" ( ");
      boolean boolFirst = true;
      for (final Enumeration<Object> e = vecFields2.elements(); e.hasMoreElements();) {
        final String tableField = (String) e.nextElement();
        if (boolFirst) {
          boolFirst = false;
        } else {
          strFields2.append(" || ' - ' || ");
        }
        strFields2.append("COALESCE(TO_CHAR(").append(tableField).append("), '') ");
      }
      strFields2.append(") as ").append(fieldsDef.columnname);
      final StringBuffer fields = new StringBuffer();
      fields.append("SELECT ").append(strFields2);
      fields.append(" FROM " + tableN + " ");
      for (int j = 0; j < vecTables2.size(); j++) {
        fields.append(vecTables2.elementAt(j));
      }
      fields.append(" WHERE " + tableN + ".isActive='Y'");
      for (int j = 0; j < vecWhere2.size(); j++) {
        fields.append(vecWhere2.elementAt(j));
      }
      fields.append(" AND " + tableN + "." + fieldsDef.name + " = ? ");
      fd.defaultvalue = fields.toString();

      fd.whereclause = "";
      for (Object param : vecTableParameters) {
        fd.whereclause += ((String) param) + "\n";
      }

      fd.whereclause += "<Parameter name=\"" + fd.name + "\"/>";
      v.addElement(fd);
    }
    return itable;
  }

  private HashMap<String, String> getTableColumnName(FieldsData fieldsDef) {
    EditionFieldsData[] dataSearchs = null;
    HashMap<String, String> ret = new HashMap<String, String>();
    if (fieldsDef.name == null) {
      fieldsDef.name = "";
    }

    if (fieldsDef.reference.equals("30")) {
      try {
        dataSearchs = EditionFieldsData.selectSearchs(conn, "", fieldsDef.referencevalue);
      } catch (ServletException e2) {
        e2.printStackTrace();
      }
    }
    if (dataSearchs == null || dataSearchs.length == 0) {
      // set table name
      if (fieldsDef.reference.equals("25")) {
        ret.put("table", "C_ValidCombination");
      } else if (fieldsDef.reference.equals("31")) {
        ret.put("table", "M_Locator");
      } else if (fieldsDef.reference.equals("35")) {
        ret.put("table", "M_AttributeSetInstance");
      } else if (fieldsDef.reference.equals("800011")) {
        ret.put("table", "M_Product");
      } else if (fieldsDef.name.equalsIgnoreCase("createdBy")
          || fieldsDef.name.equalsIgnoreCase("updatedBy")) {
        ret.put("table", "AD_User");
      } else {
        ret.put("table", fieldsDef.name.substring(0, fieldsDef.name.length() - 3));
      }

      // set column
      if (fieldsDef.reference.equals("25")) {
        ret.put("column", "C_ValidCombination_ID");

      } else if (fieldsDef.reference.equals("31")) {
        ret.put("column", "M_Locator_ID");
      } else if (fieldsDef.reference.equals("35")) {
        ret.put("column", "M_AttributeSetInstance_ID");
      } else if (fieldsDef.reference.equals("800011")) {
        ret.put("column", "M_Product_ID");
      } else if (fieldsDef.name.equalsIgnoreCase("createdBy")
          || fieldsDef.name.equalsIgnoreCase("updatedBy")) {
        ret.put("column", "AD_User_ID");
      }
    } else {
      ret.put("table", dataSearchs[0].reference);
      fieldsDef.name = dataSearchs[0].columnname;
    }
    if (ret.get("table") == null) {
      ret.put("table", "");
    }
    if (ret.get("column") == null) {
      ret.put("column", ret.get("table") + "_ID");
    }
    return ret;
  }

  public int addAdditionDefaulJavaFields(StringBuffer strDefaultValues, FieldsData fieldsDef,
      String tabName, int itable) {
    if (fieldsDef.isdisplayed.equals("Y")) {

      HashMap<String, String> tableColumnName = getTableColumnName(fieldsDef);
      String tableN = tableColumnName.get("table");
      fieldsDef.name = tableColumnName.get("column");

      final Vector<Object> vecFields2 = new Vector<Object>();
      final Vector<Object> vecTables2 = new Vector<Object>();
      final Vector<Object> vecWhere2 = new Vector<Object>();

      final Vector<Object> vecCounters = new Vector<Object>();
      vecCounters.addElement("1");
      vecCounters.addElement("0");
      final Vector<Object> vecParameters = new Vector<Object>();
      final Vector<Object> vecTableParameters = new Vector<Object>();
      try {
        WadUtility.columnIdentifier(conn, tableN, false, fieldsDef, vecCounters, false, vecFields2,
            vecTables2, vecWhere2, vecParameters, vecTableParameters, sqlDateFormat);
      } catch (ServletException e2) {
        e2.printStackTrace();
      }

      boolean hasLangParam = vecTableParameters.size() > 0;

      strDefaultValues.append(", " + tabName + "Data.selectDef" + fieldsDef.adcolumnid + "_"
          + (itable++) + "(this, " + (hasLangParam ? " vars.getLanguage(), " : "")
          + fieldsDef.defaultvalue + ")");
    }
    return itable;
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
      EditionFieldsData[] dataSearchs = EditionFieldsData.selectSearchs(conn, "",
          getData("AD_Reference_Value_ID"));
      if (dataSearchs != null && dataSearchs.length != 0) {
        return dataSearchs[0].adColumnId;
      } else {
        String strTableName = getData("ColumnNameSearch");
        strTableName = strTableName.substring(0, (strTableName.length() - 3));
        return WADSearchData.getLinkColumn(conn, strTableName);
      }
    } catch (Exception e) {
      return "";
    }
  }

  public String getDisplayLogic(boolean display, boolean isreadonly) {
    StringBuffer displayLogic = new StringBuffer();

    displayLogic.append(super.getDisplayLogic(display, isreadonly));

    if (!getData("IsReadOnly").equals("Y") && !isreadonly) {
      displayLogic.append("displayLogicElement('");
      displayLogic.append(getData("ColumnName"));
      displayLogic.append("_btt', ").append(display ? "true" : "false").append(");\n");
    }
    return displayLogic.toString();
  }

  public String getReadOnlyLogicColumn() {
    return getData("columnName") + "_R";
  }
}
