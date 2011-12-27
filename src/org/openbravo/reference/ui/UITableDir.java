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
package org.openbravo.reference.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.BuscadorData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.ComboTableQueryData;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.reference.Reference;
import org.openbravo.utils.FormatUtilities;

public class UITableDir extends UIReference {

  public UITableDir(String reference, String subreference) {
    super(reference, subreference);
  }

  public void generateSQL(TableSQLData table, Properties prop) throws Exception {
    table.addSelectField(table.getTableName() + "." + prop.getProperty("ColumnName"),
        prop.getProperty("ColumnName"));
    identifier(table, table.getTableName(), prop, prop.getProperty("ColumnName") + "_R",
        table.getTableName() + "." + prop.getProperty("ColumnName"), false);
  }

  public void identifier(TableSQLData tableSql, String parentTableName, Properties field,
      String identifierName, String realName, boolean tableRef) throws Exception {
    if (field == null)
      return;

    int myIndex = tableSql.index++;
    String name = field.getProperty("ColumnNameSearch");
    String tableDirName;
    if (field.containsKey("tableDirName")) {
      tableDirName = field.getProperty("tableDirName");
    } else {
      tableDirName = name.substring(0, name.length() - 3);
    }
    if (subReference != null && !subReference.equals("")) {
      TableSQLQueryData[] search = TableSQLQueryData.searchInfo(tableSql.getPool(), subReference);
      if (search != null && search.length != 0) {
        name = search[0].columnname;
        tableDirName = search[0].tablename;
      }
    } else {
      if (name.equalsIgnoreCase("CreatedBy") || name.equalsIgnoreCase("UpdatedBy")) {
        tableDirName = "AD_User";
        name = "AD_User_ID";
      }
    }
    ComboTableQueryData trd[] = ComboTableQueryData.identifierColumns(tableSql.getPool(),
        tableDirName);
    String tables = "(SELECT " + name;
    for (int i = 0; i < trd.length; i++) {
      // exclude tabledir pk-column as it has already been added in the line above
      if (!trd[i].name.equals(name)) {
        tables += ", " + trd[i].name;
      }
    }
    tables += " FROM ";
    tables += tableDirName + ") td" + myIndex;
    tables += " on " + parentTableName + "." + field.getProperty("ColumnName") + " = td" + myIndex
        + "." + name + "\n";
    tableSql.addFromField(tables, "td" + myIndex, realName);
    for (int i = 0; i < trd.length; i++) {
      Properties linkedRefProp = UIReferenceUtility.fieldToProperties(trd[i]);
      UIReference linkedReference = Reference.getUIReference(
          linkedRefProp.getProperty("AD_Reference_ID"),
          linkedRefProp.getProperty("AD_Reference_Value_ID"));
      linkedReference.identifier(tableSql, "td" + myIndex, linkedRefProp, identifierName, realName,
          false);
    }
  }

  public String getGridType() {
    return "dynamicEnum";
  }

  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars,
      BuscadorData fields, String strTab, String strWindow, ArrayList<String> vecScript,
      Vector<Object> vecKeys) throws IOException, ServletException {
    // Table,
    // TableDir, Yes/No, direct search
    strHtml.append("<td class=\"Combo_ContentCell\" colspan=\"3\">");
    strHtml.append("<select ");
    strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields.columnname))
        .append("\" ");
    // attach comboReload call if needed
    Vector<String> comboReloadFields = getComboReloadFields(conn, strTab);
    if (isInVector(comboReloadFields, fields.columnname)) {
      strHtml.append("onchange=\"reloadComboReloads(this.name);return true; \" id=\"idParam")
          .append(FormatUtilities.replace(fields.columnname)).append("\" ");
    } else {
      strHtml.append("onchange=\"return true; \" id=\"idParam")
          .append(FormatUtilities.replace(fields.columnname)).append("\" ");
    }
    if (Integer.valueOf(fields.fieldlength).intValue() < (UIReferenceUtility.MAX_TEXTBOX_LENGTH / 4)) {
      strHtml.append("class=\"Combo Combo_OneCell_width\"");
    } else if (Integer.valueOf(fields.fieldlength).intValue() < (UIReferenceUtility.MAX_TEXTBOX_LENGTH / 2)) {
      strHtml.append("class=\"Combo Combo_TwoCells_width\"");
    } else {
      strHtml.append("class=\"Combo Combo_ThreeCells_width\"");
    }
    strHtml.append(">");
    strHtml.append("<option value=\"\"></option>\n");
    try {

      ComboTableData comboTableData = new ComboTableData(vars, conn, reference, fields.columnname,
          subReference, fields.adValRuleId, Utility.getContext(conn, vars, "#AccessibleOrgTree",
              strWindow), Utility.getContext(conn, vars, "#User_Client", strWindow), 0);
      comboTableData.fillParametersFromSearch(strTab, strWindow);
      FieldProvider[] data = comboTableData.select(false);
      comboTableData = null;
      for (int j = 0; j < data.length; j++) {
        strHtml.append("<option value=\"");
        strHtml.append(data[j].getField("ID"));
        strHtml.append("\" ");
        if (data[j].getField("ID").equalsIgnoreCase(fields.value))
          strHtml.append("selected");
        strHtml.append(">");
        strHtml.append(data[j].getField("NAME"));
        strHtml.append("</option>\n");
      }
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    strHtml.append("</select>\n");
  }

  /**
   * Gets list of fields which have a comboReload associated to trigger reloads of dependent combos.
   * Change in logic needs to be synchronized with copy in wad
   * 
   * @return List of columnnames of fields with have a comboReload associated
   */
  private static Vector<String> getComboReloadFields(ConnectionProvider pool, String strTab)
      throws ServletException {
    final BuscadorData[] dataReload = BuscadorData.selectValidationTab(pool, strTab);

    final Vector<String> vecReloads = new Vector<String>();
    if (dataReload != null && dataReload.length > 0) {
      for (int z = 0; z < dataReload.length; z++) {
        String code = dataReload[z].whereclause
            + ((!dataReload[z].whereclause.equals("") && !dataReload[z].referencevalue.equals("")) ? " AND "
                : "") + dataReload[z].referencevalue;

        if (code.equals("") && dataReload[z].type.equals("R"))
          code = "@AD_Org_ID@";
        getComboReloadText(code, vecReloads, dataReload[z].columnname);
      }
    }
    return vecReloads;
  }

  private static boolean isInVector(Vector<String> vec, String field) {
    for (String aux : vec) {
      if (aux.equalsIgnoreCase(field))
        return true;
    }
    return false;
  }

  private static void getComboReloadText(String token, Vector<String> vecComboReload,
      String columnname) {
    int i = token.indexOf("@");
    while (i != -1) {
      token = token.substring(i + 1);
      if (!token.startsWith("SQL")) {
        i = token.indexOf("@");
        if (i != -1) {
          String strAux = token.substring(0, i);
          token = token.substring(i + 1);
          getComboReloadTextTranslate(strAux, vecComboReload, columnname);
        }
      }
      i = token.indexOf("@");
    }
  }

  private static void getComboReloadTextTranslate(String token, Vector<String> vecComboReload,
      String columnname) {
    if (token == null || token.trim().equals(""))
      return;
    if (!token.equalsIgnoreCase(columnname))
      if (!isInVector(vecComboReload, token))
        vecComboReload.addElement(token);
  }

  public void generateFilterAcceptScript(BuscadorData field, StringBuffer params,
      StringBuffer paramsData) {
    paramsData.append("paramsData[count++] = new Array(\"inpParam")
        .append(FormatUtilities.replace(field.columnname)).append("\" , ");
    params.append(", \"inpParam").append(FormatUtilities.replace(field.columnname)).append("\",");
    params.append(" escape(");
    paramsData.append("((frm.inpParam").append(FormatUtilities.replace(field.columnname))
        .append(".selectedIndex!=-1)?");
    paramsData.append("frm.inpParam").append(FormatUtilities.replace(field.columnname))
        .append(".options[");
    paramsData.append("frm.inpParam").append(FormatUtilities.replace(field.columnname))
        .append(".selectedIndex].value:");
    paramsData.append("\"\"));\n");
    params.append("((frm.inpParam").append(FormatUtilities.replace(field.columnname))
        .append(".selectedIndex!=-1)?");
    params.append("frm.inpParam").append(FormatUtilities.replace(field.columnname))
        .append(".options[");
    params.append("frm.inpParam").append(FormatUtilities.replace(field.columnname))
        .append(".selectedIndex].value:");
    params.append("\"\")");
    params.append(")");
  }

  public void setComboTableDataIdentifier(ComboTableData comboTableData, String tableName,
      FieldProvider field) throws Exception {
    String fieldName = field == null ? "" : field.getField("name");
    String parentFieldName = fieldName;

    int myIndex = comboTableData.index++;
    String name = ((fieldName != null && !fieldName.equals("")) ? fieldName : comboTableData
        .getObjectName());

    String tableDirName = null;
    if (name.equalsIgnoreCase("createdby") || name.equalsIgnoreCase("updatedby")) {
      tableDirName = "AD_User";
      name = "AD_User_ID";
    } else {
      // Try to obtain the referenced table from reference. Note it is possible not to be a TableDir
      // reference, but another one inheriting from this (search).
      if (subReference != null && !subReference.equals("")) {
        TableSQLQueryData[] search = TableSQLQueryData.searchInfo(comboTableData.getPool(),
            subReference);
        if (search != null && search.length != 0) {
          name = search[0].columnname;
          tableDirName = search[0].tablename;
        }
      }
      // If not possible, use the columnname
      if (tableDirName == null) {
        tableDirName = name.substring(0, name.length() - 3);
      }
    }
    ComboTableQueryData trd[] = ComboTableQueryData.identifierColumns(comboTableData.getPool(),
        tableDirName);
    comboTableData.addSelectField("td" + myIndex + "." + name, "ID");

    String tables = tableDirName + " td" + myIndex;
    if (tableName != null && !tableName.equals("") && parentFieldName != null
        && !parentFieldName.equals("")) {
      tables += " on " + tableName + "." + parentFieldName + " = td" + myIndex + "." + name + "\n";
      tables += "AND td" + myIndex + ".AD_Client_ID IN (" + comboTableData.getClientList() + ") \n";
      tables += "AND td" + myIndex + ".AD_Org_ID IN (" + comboTableData.getOrgList() + ")";
    } else {
      comboTableData.addWhereField(
          "td" + myIndex + ".AD_Client_ID IN (" + comboTableData.getClientList() + ")",
          "CLIENT_LIST");
      if (comboTableData.getOrgList() != null)
        comboTableData.addWhereField(
            "td" + myIndex + ".AD_Org_ID IN (" + comboTableData.getOrgList() + ")", "ORG_LIST");
    }
    comboTableData.addFromField(tables, "td" + myIndex);
    if (tableName == null || tableName.equals("")) {
      comboTableData.parseValidation();
      comboTableData.addWhereField("(td" + myIndex + ".isActive = 'Y' OR td" + myIndex + "." + name
          + " = (?) )", "ISACTIVE");
      comboTableData.addWhereParameter("@ACTUAL_VALUE@", "ACTUAL_VALUE", "ISACTIVE");
    }
    for (int i = 0; i < trd.length; i++)
      comboTableData.identifier("td" + myIndex, trd[i]);
    comboTableData.addOrderByField("2");
  }

  @Override
  public boolean canBeCached() {
    return true;
  }
}
