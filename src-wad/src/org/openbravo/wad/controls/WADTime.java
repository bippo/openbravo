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

import org.openbravo.utils.FormatUtilities;
import org.openbravo.wad.EditionFieldsData;
import org.openbravo.xmlEngine.XmlDocument;

public class WADTime extends WADControl {

  public WADTime() {
  }

  public WADTime(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
  }

  private void generateJSCode() {
    addImport("TimeTextBox", "../../../../../web/js/default/TimeTextBox.js");
    // addImport("time", "../../../../../web/js/time.js");
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
    if (!getData("ValueMin").equals("") || !getData("ValueMax").equals("")) {
      validation.append("  if (inputValue(frm.inp").append(getData("ColumnNameInp"))
          .append(")!=null && ");
      validation.append("inputValue(frm.inp").append(getData("ColumnNameInp"))
          .append(")!=\"\" && (");
      boolean valmin = false;
      if (!getData("ValueMin").equals("")) {
        validation.append("timecmp(frm.inp").append(getData("ColumnNameInp")).append(".value, '")
            .append(getData("ValueMin")).append("')<0");
        valmin = true;
      }
      if (!getData("ValueMax").equals("")) {
        if (valmin)
          validation.append(" || ");
        validation.append("timecmp(frm.inp").append(getData("ColumnNameInp")).append(".value, '")
            .append(getData("ValueMax")).append("')>0");
      }
      validation.append(")) {\n");
      if (getData("IsDisplayed").equals("Y"))
        validation.append("    setWindowElementFocus(frm.inp").append(getData("ColumnNameInp"))
            .append(");\n");
      validation.append("    showJSMessage(9);\n");
      validation.append("    return false;\n");
      validation.append("  }\n");
    }
    setValidation(validation.toString());
    setCalloutJS();
  }

  public String getType() {
    return "TextBox";
  }

  public String editMode() {
    String textButton = "";
    String buttonClass = "";
    /*
     * if (getData("IsReadOnly").equals("N") && getData("IsReadOnlyTab").equals("N") &&
     * getData("IsUpdateable").equals("Y")) { this.button.setReportEngine(getReportEngine());
     * textButton = this.button.toString(); buttonClass = this.button.getType(); }
     */
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADTime", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));
    xmlDocument.setParameter("hasButton", (textButton.equals("") ? "TextButton_ContentCell" : ""));
    xmlDocument.setParameter("buttonClass", buttonClass + "_ContentCell");
    xmlDocument.setParameter("button", textButton);

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
    xmlDocument.setParameter("textBoxCSS", (isDisabled ? "_ReadOnly" : ""));

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    String textButton = "";
    String buttonClass = "";
    /*
     * if (getData("IsReadOnly").equals("N") && getData("IsReadOnlyTab").equals("N")) {
     * this.button.setReportEngine(getReportEngine()); textButton = this.button.toString();
     * buttonClass = this.button.getType(); }
     */
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADTime", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));
    xmlDocument.setParameter("hasButton", (textButton.equals("") ? "TextButton_ContentCell" : ""));
    xmlDocument.setParameter("buttonClass", buttonClass + "_ContentCell");
    xmlDocument.setParameter("button", textButton);

    boolean isDisabled = (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y"));
    xmlDocument.setParameter("disabled", (isDisabled ? "Y" : "N"));
    if (!isDisabled && getData("IsMandatory").equals("Y")) {
      xmlDocument.setParameter("required", "true");
      xmlDocument.setParameter("requiredClass", " required");
    } else {
      xmlDocument.setParameter("required", "false");
      xmlDocument.setParameter("requiredClass", (isDisabled ? " readonly" : ""));
    }
    xmlDocument.setParameter("textBoxCSS", (isDisabled ? "_ReadOnly" : ""));

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String toXml() {
    StringBuffer text = new StringBuffer();
    if (getData("IsParameter").equals("Y")) {
      text.append("<PARAMETER id=\"").append(getData("ColumnName")).append("\" name=\"")
          .append(getData("ColumnName")).append("\" attribute=\"value\"/>");
    } else {
      text.append("<FIELD id=\"").append(getData("ColumnName")).append("\" attribute=\"value\">");
      text.append(getData("ColumnName")).append("</FIELD>");
    }
    return text.toString();
  }

  public String toJava() {
    return "";
  }

  public String getSQLCasting() {
    return "TO_DATE";
  }

  public void processSelCol(String tableName, EditionFieldsData selCol, Vector<Object> vecAuxSelCol) {
    final EditionFieldsData aux = new EditionFieldsData();
    aux.adColumnId = selCol.adColumnId;
    aux.name = selCol.name;
    aux.reference = selCol.reference;
    aux.referencevalue = selCol.referencevalue;
    aux.adValRuleId = selCol.adValRuleId;
    aux.fieldlength = selCol.fieldlength;
    aux.displaylength = selCol.displaylength;
    aux.columnname = selCol.columnname + "_f";
    aux.realcolumnname = selCol.realcolumnname;
    aux.columnnameinp = selCol.columnnameinp;
    aux.value = selCol.value;
    aux.adWindowId = selCol.adWindowId;
    aux.htmltext = "strParam" + aux.columnname + ".equals(\"\")";
    selCol.xmltext = " + ((strParam" + selCol.columnname + ".equals(\"\") || strParam"
        + selCol.columnname + ".equals(\"%\"))?\"\":\" AND ";
    selCol.xmltext += "TO_CHAR(" + tableName + "." + selCol.realcolumnname + ", 'HH24:MI:SS') >= ";
    selCol.xsqltext = "TO_CHAR(" + tableName + "." + selCol.realcolumnname + ", 'HH24:MI:SS') >= ";

    selCol.xmltext += "TO_TIMESTAMP('";
    selCol.xsqltext += "TO_TIMESTAMP";

    selCol.xmltext += "\" + strParam" + selCol.columnname + " + \"";
    selCol.xmltext += "', 'HH24:MI:SS')";

    selCol.xmltext += " \")";
    selCol.xsqltext += "(?" + ", 'HH24:MI:SS'" + ") ";
    aux.columnnameinp = FormatUtilities.replace(selCol.columnname) + "_f";
    aux.xmltext = " + ((strParam" + aux.columnname + ".equals(\"\") || strParam" + aux.columnname
        + ".equals(\"%\"))?\"\":\" AND";

    aux.xmltext += "TO_CHAR(" + tableName + "." + aux.realcolumnname + ", 'HH24:MI:SS') < ";
    aux.xsqltext = "TO_CHAR(" + tableName + "." + aux.realcolumnname + ", 'HH24:MI:SS') < ";

    aux.xmltext += "TO_TIMESTAMP('";
    aux.xsqltext += "TO_TIMESTAMP";

    aux.xmltext += "\" + strParam" + aux.columnname + " + \"";

    aux.xmltext += "', 'HH24:MI:SS')";
    aux.xmltext += " + 1 \")";
    aux.xsqltext += "(?" + ", 'HH24:MI:SS'" + ") + 1 ";
    vecAuxSelCol.addElement(aux);

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

  public boolean isTime() {
    return true;
  }
}
