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
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.wad.FieldsData;
import org.openbravo.wad.WadUtility;
import org.openbravo.xmlEngine.XmlDocument;

public class WADList extends WADControl {

  public WADList() {
  }

  public WADList(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
  }

  private void generateJSCode() {
    if (getData("IsMandatory").equals("Y")) {
      StringBuffer text = new StringBuffer();
      text.append("  if (inputValue(frm.inp").append(getData("ColumnNameInp"));
      text.append(")==null || inputValue(frm.inp");
      text.append(getData("ColumnNameInp"));
      text.append(")==\"\") {\n");
      text.append("    setWindowElementFocus(frm.inp").append(getData("ColumnNameInp"))
          .append(");\n");
      text.append("    showJSMessage(1);\n");
      text.append("    return false;\n");
      text.append("  }");
      setValidation(replaceHTML(text.toString()));
    }
    if ("Y".equals(getData("ValidateOnNew"))) {
      setOnLoad("if (inputValue(key)==null || inputValue(key)==\"\") updateOnChange(frm.inp"
          + getData("ColumnNameInp") + ");");
    }
    setCalloutJS();
  }

  public String getType() {
    return "Combo";
  }

  public String editMode() {
    String[] discard = { "" };
    if (getData("IsMandatory").equals("Y"))
      discard[0] = "fieldBlankSection";
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADList", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    String length = getData("DisplayLength");
    if (!length.endsWith("%"))
      length += "px";
    xmlDocument.setParameter("size", getData("CssSize"));

    String auxClassName = "";
    if (getData("IsMandatory").equals("Y"))
      auxClassName += "Key";
    if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y"))
      auxClassName += "ReadOnly";
    else if (getData("IsUpdateable").equals("N"))
      auxClassName += "NoUpdatable";
    xmlDocument.setParameter("myClass", auxClassName);

    if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y")
        || getData("IsUpdateable").equals("N"))
      xmlDocument.setParameter("disabled", "Y");
    else
      xmlDocument.setParameter("disabled", "N");
    // if (getData("IsMandatory").equals("Y"))
    // xmlDocument.setParameter("required", "Y");
    // else xmlDocument.setParameter("required", "N");

    StringBuffer text = new StringBuffer();
    if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y")
        || getData("IsUpdateable").equals("N")) {
      text.append("selectCombo(this, 'xx');return true; tmp_water_mark; ");
    }
    text.append(getOnChangeCode());
    xmlDocument.setParameter("callout", text.toString());

    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    String[] discard = { "" };
    if (getData("IsMandatory").equals("Y"))
      discard[0] = "fieldBlankSection";
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/wad/controls/WADList", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    String length = getData("DisplayLength");
    if (!length.endsWith("%"))
      length += "px";
    xmlDocument.setParameter("size", getData("CssSize"));

    String auxClassName = "";
    if (getData("IsMandatory").equals("Y"))
      auxClassName += "Key";
    if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y"))
      auxClassName += "ReadOnly";
    xmlDocument.setParameter("myClass", auxClassName);

    if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y"))
      xmlDocument.setParameter("disabled", "Y");
    else
      xmlDocument.setParameter("disabled", "N");
    // if (getData("IsMandatory").equals("Y"))
    // xmlDocument.setParameter("required", "Y");
    // else xmlDocument.setParameter("required", "N");

    StringBuffer text = new StringBuffer();
    if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y")) {
      text.append("selectCombo(this, 'xx');return true; tmp_water_mark; ");
    }
    text.append(getOnChangeCode());
    xmlDocument.setParameter("callout", text.toString());

    return replaceHTML(xmlDocument.print());
  }

  public String toXml() {
    StringBuffer text = new StringBuffer();
    if (getData("IsParameter").equals("Y")) {
      text.append("<PARAMETER id=\"").append(getData("ColumnName"));
      text.append("\" name=\"").append(getData("ColumnName")).append("\" attribute=\"value\"/>");
      if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y")
          || getData("IsUpdateable").equals("N")) {
        text.append("\n<PARAMETER id=\"report").append(getData("ColumnName"));
        text.append("_S\" name=\"report").append(getData("ColumnName"))
            .append("_S\" attribute=\"onchange\" replace=\"xx\"/>");
      }
    } else {
      text.append("<FIELD id=\"").append(getData("ColumnName"));
      text.append("\" attribute=\"value\">");
      text.append(getData("ColumnName")).append("</FIELD>");
      if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y")
          || getData("IsUpdateable").equals("N")) {
        text.append("\n<FIELD id=\"report").append(getData("ColumnName"));
        text.append("_S\" attribute=\"onchange\" replace=\"xx\">");
        text.append(getData("ColumnName")).append("</FIELD>");
      }
    }
    if (getData("IsDisplayed").equals("Y")) {
      text.append("\n<SUBREPORT id=\"report").append(getData("ColumnName"));
      text.append("\" name=\"report").append(getData("ColumnName"));
      text.append("\" report=\"org/openbravo/erpCommon/reference/List\">\n");
      text.append("  <ARGUMENT name=\"parameterListSelected\" withId=\"")
          .append(getData("ColumnName")).append("\"/>\n");
      text.append("</SUBREPORT>");
    }
    return text.toString();
  }

  public String toJava() {
    StringBuffer text = new StringBuffer();
    if (getData("IsDisplayed").equals("Y")) {
      if (getData("ColumnName").equalsIgnoreCase("AD_Org_ID")) {
        text.append("String userOrgList = \"\";\n");
        text.append("if (editableTab) \n");
        if (getData("hasParentsFields").equals("N"))
          text.append("  userOrgList=Utility.getContext(this, vars, \"#User_Org\", windowId, accesslevel); //editable record \n");
        else
          text.append("  userOrgList= Utility.getReferenceableOrg(this, vars, currentPOrg, windowId, accesslevel); //referenceable from parent org, only the writeable orgs\n");
        text.append("else \n");
        text.append("  userOrgList=currentOrg;\n");
      } else if (getData("ColumnName").equalsIgnoreCase("AD_Client_ID")) {
        text.append("String userClientList = \"\";\n");
        text.append("if (editableTab) \n");
        text.append("  userClientList=Utility.getContext(this, vars, \"#User_Client\", windowId, accesslevel); //editable record \n");
        text.append("else \n");
        text.append("  userClientList=currentClient;\n");
      }
      text.append("comboTableData = new ComboTableData(vars, this, \"")
          .append(getData("AD_Reference_ID")).append("\", ");
      text.append("\"").append(getData("ColumnName")).append("\", \"");
      text.append(getData("AD_Reference_Value_ID")).append("\", ");
      text.append("\"").append(getData("AD_Val_Rule_ID")).append("\", ");

      if (getData("ColumnName").equalsIgnoreCase("AD_Org_ID"))
        text.append("userOrgList, ");
      else if (getData("ColumnName").equalsIgnoreCase("AD_Client_ID"))
        text.append("null, ");
      else
        text.append("Utility.getReferenceableOrg(vars, (dataField!=null?dataField.getField(\"adOrgId\"):data[0].getField(\"adOrgId\").equals(\"\")?vars.getOrg():data[0].getField(\"adOrgId\"))), ");

      if (getData("ColumnName").equalsIgnoreCase("AD_Client_ID"))
        text.append("userClientList, 0);\n");
      else
        text.append("Utility.getContext(this, vars, \"#User_Client\", windowId), 0);\n");

      text.append("Utility.fillSQLParameters(this, vars, (dataField==null?data[0]:dataField), comboTableData, windowId, (dataField==null?data[0].getField(\"");
      text.append(getData("ColumnNameInp")).append("\"):dataField.getField(\"");
      text.append(getData("ColumnNameInp")).append("\")));\n");
      text.append("xmlDocument.setData(\"report").append(getData("ColumnName"))
          .append("\",\"liststructure\", ");
      text.append("comboTableData.select(!strCommand.equals(\"NEW\")));\n");
      text.append("comboTableData = null;");
    }
    return text.toString();
  }

  public boolean has2UIFields() {
    return true;
  }

  public String columnIdentifier(String tableName, FieldsData fields, Vector<Object> vecCounters,
      Vector<Object> vecFields, Vector<Object> vecTable, Vector<Object> vecWhere,
      Vector<Object> vecParameters, Vector<Object> vecTableParameters) throws ServletException {
    if (fields == null)
      return "";
    StringBuffer texto = new StringBuffer();
    int ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
    int itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();

    ilist++;
    if (tableName != null && tableName.length() != 0) {
      vecTable.addElement("left join ad_ref_list_v list" + ilist + " on (" + tableName + "."
          + fields.name + " = list" + ilist + ".value and list" + ilist + ".ad_reference_id = '"
          + fields.referencevalue + "' and list" + ilist + ".ad_language = ?) ");
      vecTableParameters.addElement("<Parameter name=\"paramLanguage\"/>");
    } else {
      vecTable.addElement("ad_ref_list_v list" + ilist);
      vecWhere.addElement(fields.referencevalue + " = " + "list" + ilist + ".ad_reference_id ");
      vecWhere.addElement("list" + ilist + ".ad_language = ? ");
      vecParameters.addElement("<Parameter name=\"paramLanguage\"/>");
    }
    texto.append("list").append(ilist).append(".name");
    vecFields.addElement(texto.toString());
    vecCounters.set(0, Integer.toString(itable));
    vecCounters.set(1, Integer.toString(ilist));

    return texto.toString();
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
      strFields.append(" ( ");
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

  public boolean isText() {
    return true;
  }

  public String getReadOnlyLogicColumn() {
    return "report" + getData("columnName") + "_S";
  }
}
