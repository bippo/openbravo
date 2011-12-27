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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.wad.controls;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.utils.FormatUtilities;
import org.openbravo.wad.FieldsData;
import org.openbravo.xmlEngine.XmlDocument;

public class WADButton extends WADControl {
  public WADButton() {
  }

  public WADButton(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void setShortcuts(HashMap<String, String> sc) {
    setData("nameButton", "");
  }

  public void initialize() {
    generateJSCode();
  }

  private void generateJSCode() {
    setValidation("");
    setCalloutJS();
  }

  public String getType() {
    return "Button_CenterAlign";
  }

  private StringBuffer getAction() {
    final String logClickCode = "logClick(document.getElementById('" + getData("ColumnName")
        + "'));";
    final boolean triggersAutosave = getData("IsAutosave").equalsIgnoreCase("Y");

    StringBuffer text = new StringBuffer();
    boolean isDisabled = (getData("IsReadOnly").equals("Y")
        || (getData("IsReadOnlyTab").equals("Y") && getData("isReadOnlyDefinedTab").equals("N")) || getData(
        "IsUpdateable").equals("N"));

    if (isDisabled) {
      text.append("return true;");
    } else {
      if (getData("MappingName").equals("")) {
        if (triggersAutosave) {
          text.append(logClickCode);
        }
        text.append("openServletNewWindow('BUTTON")
            .append(FormatUtilities.replace(getData("ColumnName")))
            .append(getData("AD_Process_ID"));
        text.append("', true, '").append(getData("TabName"))
            .append("_Edition.html', 'BUTTON', null, ").append(triggersAutosave);

        text.append(", 600, 900, null, null, null, null, zz);");
      } else {
        if (triggersAutosave) {
          text.append(logClickCode);
        }
        text.append("openServletNewWindow('DEFAULT', true, '..");
        if (!getData("MappingName").startsWith("/"))
          text.append('/');
        text.append(getData("MappingName")).append("', 'BUTTON', '")
            .append(getData("AD_Process_ID")).append("', ").append(triggersAutosave);
        text.append(",600, 900, null, null, null, null, zz);");
      }
    }
    return text;
  }

  public String editMode() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADButton").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("nameHTML", getData("nameButton"));
    xmlDocument.setParameter("name", getData("Name"));

    xmlDocument.setParameter("callout", getOnChangeCode());
    xmlDocument.setParameter("action", getAction().toString());

    boolean isDisabled = (getData("IsReadOnly").equals("Y")
        || (getData("IsReadOnlyTab").equals("Y") && getData("isReadOnlyDefinedTab").equals("N")) || getData(
        "IsUpdateable").equals("N"));
    if (isDisabled) {
      xmlDocument.setParameter("disabled", "_disabled");
    }
    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADButton").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("nameHTML", getData("nameButton"));
    xmlDocument.setParameter("name", getData("Name"));

    xmlDocument.setParameter("callout", getOnChangeCode());

    xmlDocument.setParameter("inputId", getData("ColumnName"));
    xmlDocument.setParameter("action", getAction().toString());

    boolean isDisabled = (getData("IsReadOnly").equals("Y")
        || (getData("IsReadOnlyTab").equals("Y") && getData("isReadOnlyDefinedTab").equals("N")) || getData(
        "IsUpdateable").equals("N"));

    if (isDisabled) {
      xmlDocument.setParameter("disabled", "_disabled");
    }
    return replaceHTML(xmlDocument.print());
  }

  public String toXml() {
    StringBuffer text = new StringBuffer();

    boolean isDisabled = getData("IsReadOnly").equals("Y") || getData("IsUpdateable").equals("N");

    if (getData("IsParameter").equals("Y")) {
      text.append("<PARAMETER id=\"").append(getData("ColumnName"));
      text.append("\" name=\"").append(getData("ColumnName"));
      text.append("\" attribute=\"value\"/>");
      if (getData("IsDisplayed").equals("Y")
          && !getData("ColumnName").equalsIgnoreCase("ChangeProjectStatus")) {
        text.append("\n<PARAMETER id=\"").append(getData("ColumnName")).append("_BTN\" name=\"");
        text.append(getData("ColumnName"));
        text.append("_BTN\" replaceCharacters=\"htmlPreformated\"/>");
      }
    } else {
      if (getData("IsDisplayed").equals("Y")) {
        text.append("<PARAMETER id=\"").append(getData("ColumnName")).append("_BTNname\" name=\"")
            .append(getData("ColumnName")).append("_BTNname\" default=\"\"/>\n");
      }
      text.append("<FIELD id=\"").append(getData("ColumnName"));
      text.append("\" attribute=\"value\">").append(getData("ColumnName")).append("</FIELD>");
      if (getData("IsDisplayed").equals("Y")
          && !getData("ColumnName").equalsIgnoreCase("ChangeProjectStatus")) {
        text.append("\n<FIELD id=\"").append(getData("ColumnName"))
            .append("_BTN\" replaceCharacters=\"htmlPreformated\">");
        text.append(getData("ColumnName")).append("_BTN</FIELD>");
      }
    }

    if (!isDisabled) {
      text.append("<PARAMETER id=\"").append(getData("ColumnName"));
      text.append("_linkBTN\" name=\"").append(getData("ColumnName"));
      text.append("_Modal\" attribute=\"onclick\" replace=\"zz\" default=\"false\"/>");
    }
    return text.toString();
  }

  public String toJava() {

    boolean isDisabled = getData("IsReadOnly").equals("Y") || getData("IsUpdateable").equals("N");

    String javaCode = "";

    if (getData("IsDisplayed").equals("Y")) {
      if (!getData("AD_Reference_Value_ID").equals("")
          && !getData("ColumnName").equalsIgnoreCase("ChangeProjectStatus")) {
        javaCode = "xmlDocument.setParameter(\"" + getData("ColumnName")
            + "_BTNname\", Utility.getButtonName(this, vars, \"" + getData("AD_Reference_Value_ID")
            + "\", (dataField==null?data[0].getField(\"" + getData("ColumnNameInp")
            + "\"):dataField.getField(\"" + getData("ColumnNameInp") + "\")), \""
            + getData("ColumnName") + "_linkBTN\", usedButtonShortCuts, reservedButtonShortCuts));";
      } else {
        javaCode = "xmlDocument.setParameter(\"" + getData("ColumnName")
            + "_BTNname\", Utility.getButtonName(this, vars, \"" + getData("AD_Field_ID")
            + "\", \"" + getData("ColumnName")
            + "_linkBTN\", usedButtonShortCuts, reservedButtonShortCuts));";
      }
      if (!isDisabled) {
        String varName = "modal" + FormatUtilities.replace(getData("ColumnName"));
        javaCode += "boolean " + varName
            + " = org.openbravo.erpCommon.utility.Utility.isModalProcess(\""
            + getData("AD_Process_ID") + "\"); \n";
        javaCode += "xmlDocument.setParameter(\"" + getData("ColumnName") + "_Modal\", " + varName
            + "?\"true\":\"false\");";
      }
    } else {
      javaCode = "";
    }
    return javaCode;
  }

  public int addAdditionDefaulJavaFields(StringBuffer strDefaultValues, FieldsData fieldsDef,
      String tabName, int itable) {
    // not need to implement sql method as itable is not modified
    if (fieldsDef.isdisplayed.equals("Y") && !fieldsDef.referencevalue.equals("")) {
      strDefaultValues
          .append(", (vars.getLanguage().equals(\"en_US\")?ListData.selectName(this, \"")
          .append(fieldsDef.referencevalue).append("\", ").append(fieldsDef.defaultvalue)
          .append("):ListData.selectNameTrl(this, vars.getLanguage(), \"")
          .append(fieldsDef.referencevalue).append("\", ").append(fieldsDef.defaultvalue)
          .append("))");
    }
    return itable;
  }

  public void processTable(String strTab, Vector<Object> vecFields, Vector<Object> vecTables,
      Vector<Object> vecWhere, Vector<Object> vecOrder, Vector<Object> vecParameters,
      String tableName, Vector<Object> vecTableParameters, FieldsData field,
      Vector<String> vecFieldParameters, Vector<Object> vecCounters) throws ServletException,
      IOException {

    String strOrder = "";
    if (field.isdisplayed.equals("Y") && !field.referencevalue.equals("")
        && !field.name.equalsIgnoreCase("ChangeProjectStatus")) {
      int ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
      ilist++;
      vecFields.addElement("list" + ilist + ".name as " + field.name + "_BTN");
      strOrder = "list" + ilist + ".name";
      final StringBuffer strWhere = new StringBuffer();
      if (field.name.equalsIgnoreCase("DocAction")) {
        strWhere.append(" AND (CASE " + tableName + "." + field.name
            + " WHEN '--' THEN 'CL' ELSE TO_CHAR(" + tableName + "." + field.name + ") END) = "
            + "list" + ilist + ".value");
      } else {
        strWhere.append(" AND " + tableName + "." + field.name + " = TO_CHAR(list" + ilist
            + ".value)");
      }
      vecTables.addElement("left join ad_ref_list_v list" + ilist + " on (" + "list" + ilist
          + ".ad_reference_id = '" + field.referencevalue + "' and list" + ilist
          + ".ad_language = ? " + strWhere.toString() + ")");
      vecTableParameters.addElement("<Parameter name=\"paramLanguage\"/>");
      vecCounters.set(1, Integer.toString(ilist));
    } else {
      strOrder = tableName + "." + field.name;
    }

    final String[] aux = { new String(field.name),
        new String(strOrder + (field.name.equalsIgnoreCase("DocumentNo") ? " DESC" : "")) };
    vecOrder.addElement(aux);
  }

  public String getDisplayLogic(boolean display, boolean isreadonly) {
    return "";
  }

  public String getDefaultValue() {
    if (!getData("name").endsWith("_ID")) {
      return "N";
    } else {
      return "";
    }
  }

}
