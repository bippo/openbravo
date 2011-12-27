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
import org.openbravo.erpCommon.businessUtility.BuscadorData;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;

public class UIPAttribute extends UITableDir {
  public UIPAttribute(String reference, String subreference) {
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

    field.setProperty("ColumnNameSearch", "M_AttributeSetInstance_ID");
    super.identifier(tableSql, parentTableName, field, identifierName, realName, tableRef);
  }

  public void getFilter(SQLReturnObject result, boolean isNewFilter, VariablesSecureApp vars,
      TableSQLData tableSQL, Vector<String> filter, Vector<String> filterParams, Properties prop)
      throws ServletException {
    String aux;
    if (isNewFilter) {
      aux = vars.getRequestGlobalVariable("inpParam" + prop.getProperty("ColumnName"),
          tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName"));
    } else {
      aux = vars.getSessionValue(tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName"));
    }
    // The filter is not applied if the parameter value is null or
    // parameter value is '%' for string references.
    if (!aux.equals("")) {
      if (!aux.equals("%")) {
        UIReferenceUtility.addFilter(filter, filterParams, result, tableSQL,
            prop.getProperty("ColumnName"), prop.getProperty("ColumnName"), reference, true, aux);
      } else {
        filter.addElement("1=1");
      }
    }
  }

  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars,
      BuscadorData fields, String strTab, String strWindow, ArrayList<String> vecScript,
      Vector<Object> vecKeys) throws IOException, ServletException {
    UIString stringRef = new UIString(reference, subReference);
    stringRef.generateFilterHtml(strHtml, vars, fields, strTab, strWindow, vecScript, vecKeys);
  }

  public void generateFilterAcceptScript(BuscadorData field, StringBuffer params,
      StringBuffer paramsData) {
    UIString stringRef = new UIString(reference, subReference);
    stringRef.generateFilterAcceptScript(field, params, paramsData);
  }

  @Override
  public boolean canBeCached() {
    return true;
  }
}
