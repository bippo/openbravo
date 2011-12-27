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

public class WADYesNo extends WADControl {

  public WADYesNo() {
  }

  public WADYesNo(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
  }

  private void generateJSCode() {
    setValidation("");
    setCalloutJS();
  }

  public String getType() {
    return "Radio_Check";
  }

  public String editMode() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADYesNo").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));

    if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y")
        || getData("IsUpdateable").equals("N")) {
      xmlDocument.setParameter("disabled", "Y");
      xmlDocument.setParameter("logChanges", "");
      xmlDocument.setParameter("disabledFalse", "return false;");
    } else {
      xmlDocument.setParameter("disabled", "N");
      xmlDocument.setParameter("callout", getOnChangeCode());
    }
    if (getData("IsMandatory").equals("Y"))
      xmlDocument.setParameter("required", "true");
    else
      xmlDocument.setParameter("required", "false");

    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADYesNo").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));

    if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y")) {
      xmlDocument.setParameter("disabled", "Y");
      xmlDocument.setParameter("logChanges", "");
      xmlDocument.setParameter("disabledFalse", "return false;");
    } else {
      xmlDocument.setParameter("disabled", "N");
      xmlDocument.setParameter("callout", getOnChangeCode());
    }
    if (getData("IsMandatory").equals("Y"))
      xmlDocument.setParameter("required", "true");
    else
      xmlDocument.setParameter("required", "false");

    return replaceHTML(xmlDocument.print());
  }

  public String toXml() {
    StringBuffer text = new StringBuffer();
    if (getData("IsParameter").equals("Y")) {
      if (getData("IsDisplayed").equals("N")) {
        text.append("<PARAMETER id=\"").append(getData("ColumnName")).append("\" name=\"")
            .append(getData("ColumnName")).append("\" attribute=\"value\"/>");
      } else {
        text.append("<PARAMETER id=\"").append(getData("ColumnName")).append("\" name=\"")
            .append(getData("ColumnName")).append("\" boolean=\"checked\" withId=\"paramCheck\"/>");
      }
    } else {
      if (getData("IsDisplayed").equals("N")) {
        text.append("<FIELD id=\"").append(getData("ColumnName")).append("\" attribute=\"value\">");
        text.append(getData("ColumnName")).append("</FIELD>");
      } else {
        text.append("<FIELD id=\"").append(getData("ColumnName"))
            .append("\" boolean=\"checked\" withId=\"paramCheck\">");
        text.append(getData("ColumnName")).append("</FIELD>");
      }
    }
    return text.toString();
  }

  public String toJava() {
    return "";
  }

  public String getDefaultValue() {
    return "N";
  }

  public boolean isText() {
    return true;
  }
}
