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

import org.openbravo.xmlEngine.XmlDocument;

public class WADLink extends WADControl {
  private WADControl button;

  public WADLink() {
  }

  public WADLink(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
    this.button = new WADFieldButton("Link", getData("ColumnName"), getData("ColumnNameInp"),
        getData("Name"), "window.open(document.getElementById('" + getData("ColumnName")
            + "').value);");
  }

  private void generateJSCode() {
    addImport("UrlTextBox", "../../../../../web/js/default/UrlTextBox.js");
    if (getData("IsMandatory").equals("Y")) {
      XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
          "org/openbravo/wad/controls/WADLinkJSValidation").createXmlDocument();
      xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
      setValidation(replaceHTML(xmlDocument.print()));
    }
    setCalloutJS();
  }

  public String getType() {
    return "TextBox_btn";
  }

  public String editMode() {
    String textButton = "";
    String buttonClass = "";
    this.button.setReportEngine(getReportEngine());
    textButton = this.button.toString();
    buttonClass = this.button.getType();
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADLink", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
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
    this.button.setReportEngine(getReportEngine());
    textButton = this.button.toString();
    buttonClass = this.button.getType();
    String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADLink", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("size", (textButton.equals("") ? "" : "btn_") + getData("CssSize"));
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
        "org/openbravo/wad/controls/WADLinkXML", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    return replaceHTML(xmlDocument.print());
  }

  public String toJava() {
    return "";
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
