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

public class WADDate extends WADControl {
  private WADControl button;

  public WADDate() {
  }

  public WADDate(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
    this.button = new WADFieldButton("Calendar", getData("ColumnName"), getData("ColumnNameInp"),
        getData("Name"), "showCalendar('frmMain.inp" + getData("ColumnNameInp")
            + "', document.frmMain.inp" + getData("ColumnNameInp") + ".value, false);");
  }

  private void generateJSCode() {
    addImport("DateTextBox", "../../../../../web/js/default/DateTextBox.js");
    addImport("calendar", "../../../../../web/js/jscalendar/calendar.js");
    addImport("calendarLang", "../../../../../web/js/jscalendar/lang/calendar-lang.js");
    generateValidation();
    setCalloutJS();
  }

  private void generateValidation() {
    String[] discard = { "", "", "", "" };
    String join = "";
    if (!getData("IsMandatory").equals("Y"))
      discard[0] = "isMandatory";
    if (getData("ValueMin").trim().equals("") && getData("ValueMax").trim().equals(""))
      discard[1] = "isValueCheck";
    else {
      boolean valmin = false;
      if (getData("ValueMin").equals(""))
        discard[2] = "isValueMin";
      else
        valmin = true;
      if (getData("ValueMax").equals(""))
        discard[3] = "isValueMax";
      else if (valmin)
        join = " || ";
    }

    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADDateJSValidation", discard).createXmlDocument();
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("valueMin", getData("ValueMin"));
    xmlDocument.setParameter("valueMax", getData("ValueMax"));
    xmlDocument.setParameter("join", join);
    setValidation(replaceHTML(xmlDocument.print()));
  }

  public String getType() {
    return "TextBox_btn";
  }

  public String editMode() {
    String textButton = "";
    String buttonClass = "";
    if (getData("IsReadOnly").equals("N") && getData("IsReadOnlyTab").equals("N")
        && getData("IsUpdateable").equals("Y")) {
      this.button.setReportEngine(getReportEngine());
      textButton = this.button.toString();
      buttonClass = this.button.getType();
    }
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADDate", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
    xmlDocument.setParameter("hasButton", (textButton.equals("") ? "TextButton_ContentCell" : ""));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));
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
    if (getData("IsReadOnly").equals("N") && getData("IsReadOnlyTab").equals("N")) {
      this.button.setReportEngine(getReportEngine());
      textButton = this.button.toString();
      buttonClass = this.button.getType();
    }
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADDate", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
    xmlDocument.setParameter("hasButton", (textButton.equals("") ? "TextButton_ContentCell" : ""));
    xmlDocument.setParameter("maxlength", getData("FieldLength"));
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
    String[] discard = { "xx_PARAM" };
    if (getData("IsParameter").equals("Y"))
      discard[0] = "xx";
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADDateXML", discard).createXmlDocument();
    xmlDocument.setParameter("columnName", getData("ColumnName"));
    return replaceHTML(xmlDocument.print());
  }

  public String toJava() {
    StringBuffer text = new StringBuffer();
    text.append("xmlDocument.setParameter(\"").append(getData("ColumnName"))
        .append("_Format\", vars.getSessionValue(\"#AD_SqlDateFormat\"));");
    return text.toString();
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

    selCol.xmltext += "(" + tableName + "." + selCol.realcolumnname + ") >= ";
    selCol.xsqltext = tableName + "." + selCol.realcolumnname + " >= ";

    selCol.xmltext += "TO_TIMESTAMP('";
    selCol.xsqltext += "TO_TIMESTAMP";

    selCol.xmltext += "\" + strParam" + selCol.columnname + " + \"";

    selCol.xmltext += "')";
    selCol.xmltext += " \")";
    selCol.xsqltext += "(?" + ") ";
    aux.columnnameinp = FormatUtilities.replace(selCol.columnname) + "_f";
    aux.xmltext = " + ((strParam" + aux.columnname + ".equals(\"\") || strParam" + aux.columnname
        + ".equals(\"%\"))?\"\":\" AND";

    aux.xmltext += "(" + tableName + "." + aux.realcolumnname + ") < ";
    aux.xsqltext = tableName + "." + aux.realcolumnname + " < ";

    aux.xmltext += "TO_TIMESTAMP('";
    aux.xsqltext += "TO_TIMESTAMP";
    aux.xmltext += "\" + strParam" + aux.columnname + " + \"";

    aux.xmltext += "')";

    aux.xmltext += " + 1 \")";
    aux.xsqltext += "(?" + ") + 1 ";
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

  public boolean isText() {
    return true;
  }

  public boolean isDate() {
    return true;
  }
}
