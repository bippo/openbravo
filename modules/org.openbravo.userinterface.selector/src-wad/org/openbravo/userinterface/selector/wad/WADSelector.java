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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.userinterface.selector.wad;

import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.wad.FieldsData;
import org.openbravo.wad.WadUtility;
import org.openbravo.wad.controls.WADControl;
import org.openbravo.xmlEngine.XmlDocument;

public class WADSelector extends WADControl {

  private final static String DS_FILTER_CLASS = "org.openbravo.userinterface.selector.SelectorDataSourceFilter";

  public WADSelector() {
  }

  public WADSelector(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    addImport("StaticResources",
        "../../../../../web/../org.openbravo.client.kernel/OBCLKER_Kernel/StaticResources");
    generateJSCode();
  }

  private void generateJSCode() {
    StringBuffer validation = new StringBuffer();
    if (getData("IsMandatory").equals("Y")) {
      final String inpName = "inp" + getData("ColumnNameInp");

      validation.append("  if (inputValue(frm." + inpName + ") === null || inputValue(frm."
          + inpName + ")===\"\") {\n");
      // validation.append("  if (sc_").append(getData("ColumnName")).append(
      // ".selectorField.getSelectedRecord()===null) {\n");
      if (getData("IsDisplayed").equals("Y"))
        validation.append("    setWindowElementFocus(sc_").append(getData("ColumnName"))
            .append(".selectorField").append(");\n");
      validation.append("    showJSMessage(1);\n");
      validation.append("    return false;\n");
      validation.append("  }\n");
    }

    setValidation(validation.toString());
    setCalloutJS();
  }

  private String generateSelectorLink(boolean disabled) {
    final StringBuilder sb = new StringBuilder();
    sb.append("../org.openbravo.client.kernel/OBUISEL_Selector/" + getSelectorID());
    sb.append("?columnName=" + getData("ColumnName"));

    if (disabled) {
      sb.append("&disabled=true");
    } else {
      sb.append("&disabled=false");
    }

    if (getData("CssSize") != null) {
      sb.append("&CssSize=" + getData("CssSize"));
    }

    if (getData("DisplayLength") != null) {
      sb.append("&DisplayLength=" + getData("DisplayLength"));
    }

    final String callOut = getData("CallOutName");
    if (callOut != null && callOut.trim().length() > 0) {
      sb.append("&callOut=callout" + callOut);
    }

    if (getData("IsMandatory").equals("Y")) {
      sb.append("&required=true");
    } else {
      sb.append("&required=false");
    }

    sb.append("&adTabId=" + getData("AD_Tab_ID"));

    sb.append("&filterClass=" + DS_FILTER_CLASS);

    // do the comboreload
    String isComboReload = getData("IsComboReload");
    if (isComboReload == null || isComboReload.equals("")) {
      isComboReload = "N";
    }
    if (isComboReload.equals("Y")) {
      sb.append("&comboReload=true");
    }

    return sb.toString();
  }

  private String getSelectorID() {
    try {
      return WADSelectorData.getSelectorID(getConnection(), getData("AD_Reference_ID"),
          getData("AD_Reference_Value_ID")).obuiselSelectorId;
    } catch (ServletException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  public String getType() {
    return "TextBox_btn";
  }

  public String editMode() {
    final String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }

    final boolean isDisabled = (getData("IsReadOnly").equals("Y")
        || getData("IsReadOnlyTab").equals("Y") || getData("IsUpdateable").equals("N"));

    final XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/userinterface/selector/wad/WADSelector", discard).createXmlDocument();

    xmlDocument.setParameter("disabled", (isDisabled ? "Y" : "N"));
    xmlDocument.setParameter("selectorLink", generateSelectorLink(isDisabled));
    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("selectorVariable", "<script>var sc_" + getData("ColumnName")
        + " = null;</script>");

    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    final String[] discard = { "" };
    if (!getData("IsMandatory").equals("Y")) {
      // if field is not mandatory, discard it
      discard[0] = "xxmissingSpan";
    }

    final boolean isDisabled = getData("IsReadOnly").equals("Y")
        || getData("IsReadOnlyTab").equals("Y");

    final XmlDocument xmlDocument = getReportEngine().readXmlTemplate(
        "org/openbravo/userinterface/selector/wad/WADSelector", discard).createXmlDocument();

    xmlDocument.setParameter("disabled", (isDisabled ? "Y" : "N"));
    xmlDocument.setParameter("selectorLink", generateSelectorLink(isDisabled));
    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("selectorVariable", "<script>var sc_" + getData("ColumnName")
        + " = null;</script>");

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
      displayLogic.append("_inp_td', ").append(display ? "true" : "false").append(");\n");
    }
    return displayLogic.toString();
  }

  public boolean isLink() {
    return true;
  }

  public String getLinkColumnId() {
    try {
      return WADSelectorData.getLinkedColumnId(getConnection(), subreference);
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  public String columnIdentifier(String tableName, FieldsData fields, Vector<Object> vecCounters,
      Vector<Object> vecFields, Vector<Object> vecTable, Vector<Object> vecWhere,
      Vector<Object> vecParameters, Vector<Object> vecTableParameters) throws ServletException {
    if (fields == null)
      return "";

    StringBuffer texto = new StringBuffer();
    int ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
    int itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();

    itable++;
    String fieldId = "";
    String tableDirName = "";

    WADSelectorData wadSelectorData = WADSelectorData.getTableName(conn, fields.referencevalue);
    if (wadSelectorData != null && wadSelectorData.tablename != null
        && wadSelectorData.tablename.length() > 0) {
      tableDirName = wadSelectorData.tablename;
      fieldId = tableDirName + "_ID";
    } else {
      tableDirName = fields.name.substring(0, fields.name.length() - 3);
      fieldId = fields.name;
    }

    FieldsData fdi[] = FieldsData.identifierColumns(conn, tableDirName);
    if (tableName != null && tableName.length() != 0) {
      StringBuffer fieldsAux = new StringBuffer();
      for (int i = 0; i < fdi.length; i++) {
        if (!fdi[i].name.equalsIgnoreCase(fieldId)) {
          fieldsAux.append(", ");
          fieldsAux.append(fdi[i].name);
        }
      }
      vecTable.addElement("left join (select " + fieldId + fieldsAux.toString() + " from "
          + tableDirName + ") table" + itable + " on (" + tableName + "." + fields.name
          + " = table" + itable + "." + fieldId + ")");
    } else {
      vecTable.addElement(tableDirName + " table" + itable);
    }
    for (int i = 0; i < fdi.length; i++) {
      if (i > 0)
        texto.append(" || ' - ' || ");
      vecCounters.set(0, Integer.toString(itable));
      vecCounters.set(1, Integer.toString(ilist));

      WADControl control = WadUtility.getWadControlClass(conn, fdi[i].reference,
          fdi[i].adReferenceValueId);
      texto.append(control.columnIdentifier("table" + itable, fdi[i], vecCounters, vecFields,
          vecTable, vecWhere, vecParameters, vecTableParameters));
      ilist = Integer.valueOf(vecCounters.elementAt(1).toString()).intValue();
      itable = Integer.valueOf(vecCounters.elementAt(0).toString()).intValue();
    }
    return texto.toString();
  }
}
