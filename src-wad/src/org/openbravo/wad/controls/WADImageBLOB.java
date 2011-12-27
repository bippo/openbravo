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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.wad.controls;

import java.util.Properties;

import org.openbravo.xmlEngine.XmlDocument;

public class WADImageBLOB extends WADControl {

  public WADImageBLOB() {
  }

  public WADImageBLOB(Properties prop) {
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
        "org/openbravo/wad/controls/WADImageBLOB", discard).createXmlDocument();

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
        "org/openbravo/wad/controls/WADImageBLOB", discard).createXmlDocument();

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
        "org/openbravo/wad/controls/WADImageBLOBXML", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    return replaceHTML(xmlDocument.print());
  }

  public String toJava() {
    StringBuffer text = new StringBuffer();
    if (getData("IsDisplayed").equals("Y")) {
      text.append("String strCurrentImageURL" + getData("ColumnName")
          + " = (dataField==null?data[0].getField(\"");
      text.append(getData("ColumnNameInp")).append("\"):dataField.getField(\"");
      text.append(getData("ColumnNameInp")).append("\"));\n");
      text.append("if (strCurrentImageURL" + getData("ColumnName") + "==null || strCurrentImageURL"
          + getData("ColumnName") + ".equals(\"\")){\n");
      text.append("  xmlDocument.setParameter(\"").append(getData("ColumnName"))
          .append("Class\", \"Image_NotAvailable_medium\");\n");
      text.append("}\n");
    }
    return text.toString();
  }
}
