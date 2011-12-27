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

import java.util.Properties;
import java.util.Vector;

import org.openbravo.wad.EditionFieldsData;
import org.openbravo.wad.WadUtility;
import org.openbravo.xmlEngine.XmlDocument;

public class WADString extends WADControl {
  private WADControl button;

  public WADString() {
  }

  public WADString(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
    this.button = new WADFieldButton("Password", getData("ColumnName"), getData("ColumnNameInp"),
        getData("Name"), "openServletNewWindow('BUTTON" + getData("ColumnName") + "', false, '"
            + getData("TabName") + "_Edition.html', 'BUTTON', null, "
            + getData("IsAutosave").equals("Y") + ", 300, 600);");
  }

  private void generateJSCode() {
    addImport("ValidationTextBox", "../../../../../web/js/default/ValidationTextBox.js");
    StringBuffer validation = new StringBuffer();
    if (getData("IsMandatory").equals("Y")) {
      validation.append("  if (inputValue(frm.inp").append(getData("ColumnNameInp"))
          .append(")==null || inputValue(frm.inp").append(getData("ColumnNameInp"))
          .append(")==\"\") {\n");
      if (getData("IsDisplayed").equals("Y"))
        validation.append("    setWindowElementFocus(frm.inp").append(getData("ColumnNameInp"))
            .append(");\n");
      validation.append("    showJSMessage(1);\n");
      validation.append("    return false;\n");
      validation.append("  }\n");
    }
    setValidation(validation.toString());
    setCalloutJS();
  }

  public String getType() {
    return "TextBox_btn";
  }

  public String editMode() {
    XmlDocument xmlDocument = null;
    String textButton = "";
    String buttonClass = "";
    boolean isDisabled = false;
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    if (getData("IsEncrypted").equals("Y")) {
      if (getData("IsReadOnly").equals("N") && getData("IsReadOnlyTab").equals("N")
          && getData("IsUpdateable").equals("Y")) {
        this.button.setReportEngine(getReportEngine());
        textButton = this.button.toString();
        buttonClass = this.button.getType();
      }
      xmlDocument = getReportEngine().readXmlTemplate(
          "org/openbravo/wad/controls/WADStringEncrypted", discard).createXmlDocument();
      xmlDocument
          .setParameter("hasButton", (textButton.equals("") ? "TextButton_ContentCell" : ""));
      xmlDocument.setParameter("hasButton2", (buttonClass.equals("") ? "0" : "1"));
      xmlDocument.setParameter("buttonClass", buttonClass + "_ContentCell");
      xmlDocument.setParameter("button", textButton);
      isDisabled = true;
    } else {
      xmlDocument = getReportEngine().readXmlTemplate("org/openbravo/wad/controls/WADString",
          discard).createXmlDocument();
    }

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));

    if (!isDisabled)
      isDisabled = (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y") || getData(
          "IsUpdateable").equals("N"));
    xmlDocument.setParameter("disabled", (isDisabled ? "Y" : "N"));
    if (isDisabled) {
      xmlDocument.setParameter("required", "false");
      xmlDocument.setParameter("requiredClass", " readonly");
    } else if (getData("IsMandatory").equals("Y")) {
      xmlDocument.setParameter("required", "true");
      xmlDocument.setParameter("requiredClass", " required");
    } else {
      xmlDocument.setParameter("required", "false");
      xmlDocument.setParameter("requiredClass", "");
    }
    xmlDocument.setParameter("textBoxCSS", (isDisabled ? "_ReadOnly" : ""));

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    XmlDocument xmlDocument = null;
    String textButton = "";
    String buttonClass = "";
    boolean isDisabled = false;
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    if (getData("IsEncrypted").equals("Y")) {
      if (getData("IsReadOnly").equals("N") && getData("IsReadOnlyTab").equals("N")) {
        this.button.setReportEngine(getReportEngine());
        textButton = this.button.toString();
        buttonClass = this.button.getType();
      }
      xmlDocument = getReportEngine().readXmlTemplate(
          "org/openbravo/wad/controls/WADStringEncrypted", discard).createXmlDocument();
      xmlDocument
          .setParameter("hasButton", (textButton.equals("") ? "TextButton_ContentCell" : ""));
      xmlDocument.setParameter("hasButton2", (buttonClass.equals("") ? "0" : "1"));
      xmlDocument.setParameter("buttonClass", buttonClass + "_ContentCell");
      xmlDocument.setParameter("button", textButton);
      isDisabled = true;
    } else {
      xmlDocument = getReportEngine().readXmlTemplate("org/openbravo/wad/controls/WADString",
          discard).createXmlDocument();
    }

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));

    if (!isDisabled)
      isDisabled = (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y"));
    xmlDocument.setParameter("disabled", (isDisabled ? "Y" : "N"));
    if (isDisabled) {
      xmlDocument.setParameter("required", "false");
      xmlDocument.setParameter("requiredClass", " readonly");
    } else if (getData("IsMandatory").equals("Y")) {
      xmlDocument.setParameter("required", "true");
      xmlDocument.setParameter("requiredClass", " required");
    } else {
      xmlDocument.setParameter("required", "false");
      xmlDocument.setParameter("requiredClass", "");
    }
    xmlDocument.setParameter("textBoxCSS", (isDisabled ? "_ReadOnly" : ""));

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String toJava() {
    return "";
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
}
