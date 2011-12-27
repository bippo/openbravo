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

package org.openbravo.erpCommon.utility;

import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.reference.Reference;
import org.openbravo.reference.ui.UIReference;

/**
 * @author Fernando Iriazabal
 * 
 *         Implements the common tasks needed to build the query of the windows.
 */
public class ModelSQLGeneration {
  static Logger log4j = Logger.getLogger(ModelSQLGeneration.class);

  private static final RequestFilter columnNameFilter = new RequestFilter() {
    @Override
    public boolean accept(String value) {
      for (int i = 0; i < value.length(); i++) {
        int c = value.codePointAt(i);
        if (Character.isLetter(c) || Character.isDigit(c) || value.charAt(i) == '_') {
          return true;
        }
      }
      return false;
    }
  };

  private static final RequestFilter directionFilter = new ValueListFilter("asc", "desc");

  /**
   * Constructor
   */
  public ModelSQLGeneration() {
  }

  /**
   * Gets the order by clause.
   * 
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Handler for the query builder.
   * @return Vector with the list of fields in the order by clause.
   * @throws Exception
   */
  private static Vector<String> getOrderBy(VariablesSecureApp vars, TableSQLData tableSQL)
      throws Exception {
    Vector<String> vOrderBy = new Vector<String>();
    StringBuffer orderBy = new StringBuffer();
    if (tableSQL == null)
      return vOrderBy;
    String sortCols = vars.getInStringParameter("sort_cols", columnNameFilter);
    String sortDirs = vars.getInStringParameter("sort_dirs", directionFilter);

    if (log4j.isDebugEnabled())
      log4j.debug("sort_cols: " + sortCols);
    if (log4j.isDebugEnabled())
      log4j.debug("sort_dirs: " + sortDirs);

    if (sortCols != null && sortCols.length() > 0) {
      if (sortCols.startsWith("("))
        sortCols = sortCols.substring(1, sortCols.length() - 1);
      if (sortDirs.startsWith("("))
        sortDirs = sortDirs.substring(1, sortDirs.length() - 1);
      StringTokenizer datas = new StringTokenizer(sortCols, " ,", false);
      StringTokenizer dirs = new StringTokenizer(sortDirs, " ,", false);
      while (datas.hasMoreTokens()) {
        String token = datas.nextToken();
        String tokenDir = dirs.nextToken();
        if (token.startsWith("'"))
          token = token.substring(1, token.length() - 1);
        if (tokenDir.startsWith("'"))
          tokenDir = tokenDir.substring(1, tokenDir.length() - 1);
        token = token.trim();
        tokenDir = tokenDir.trim();
        if (!token.equals("")) {
          vOrderBy.addElement(tableSQL.getTableName() + "." + token + " " + tokenDir);
          if (!orderBy.toString().equals(""))
            orderBy.append(", ");
          orderBy.append(tableSQL.getTableName()).append(".").append(token).append(" ")
              .append(tokenDir);
        }
      }
    }
    vars.setSessionValue(tableSQL.getTabID() + "|orderby", orderBy.toString());
    vars.setSessionValue(tableSQL.getTabID() + "|orderbySimple", orderBy.toString());
    return vOrderBy;
  }

  /**
   * Returns the filter clause to apply to the query.
   * 
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Handler for the query builder.
   * @param filter
   *          Vector with specific filters.
   * @param filterParams
   *          Vector with the parameters for the specific filters.
   * @return Object with the filters defined.
   * @throws Exception
   */
  private static SQLReturnObject getFilter(VariablesSecureApp vars, TableSQLData tableSQL,
      Vector<String> filter, Vector<String> filterParams) throws Exception {
    SQLReturnObject result = new SQLReturnObject();
    if (tableSQL == null) {
      return result;
    }
    boolean isNewFilter = !vars.getStringParameter("newFilter").equals("");
    Vector<Properties> filters = tableSQL.getFilteredStructure("IsSelectionColumn", "Y");
    if (filters == null || filters.size() == 0) {
      filters = tableSQL.getFilteredStructure("IsIdentifier", "Y");
    }
    if (filters == null || filters.size() == 0) {
      return result;
    }
    tableSQL.addAuditFields(filters);

    for (Enumeration<Properties> e = filters.elements(); e.hasMoreElements();) {
      Properties prop = e.nextElement();
      UIReference reference = Reference.getUIReference(prop.getProperty("AD_Reference_ID"),
          prop.getProperty("AD_Reference_Value_ID"));
      reference.getFilter(result, isNewFilter, vars, tableSQL, filter, filterParams, prop);
    }
    return result;
  }

  /**
   * Sets the order by in the session.
   * 
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Handler for the query builder.
   * @throws Exception
   */
  private static void setSessionOrderBy(VariablesSecureApp vars, TableSQLData tableSQL)
      throws Exception {
    Vector<QueryFieldStructure> vOrderBy = tableSQL.getOrderByFields();
    StringBuffer txtAux = new StringBuffer();
    if (vOrderBy != null) {
      for (int i = 0; i < vOrderBy.size(); i++) {
        QueryFieldStructure auxStructure = vOrderBy.elementAt(i);
        if (!txtAux.toString().equals(""))
          txtAux.append(", ");
        txtAux.append(auxStructure.toString());
      }
    }
    vars.setSessionValue(tableSQL.getTabID() + "|orderby", txtAux.toString());

    vOrderBy = tableSQL.getOrderBySimpleFields();
    StringBuffer txtAuxSimple = new StringBuffer();
    if (vOrderBy != null) {
      for (int i = 0; i < vOrderBy.size(); i++) {
        QueryFieldStructure auxStructure = vOrderBy.elementAt(i);
        if (!txtAuxSimple.toString().equals(""))
          txtAuxSimple.append(", ");
        txtAuxSimple.append(auxStructure.toString());
      }
    }
    if (txtAuxSimple.toString().equals(""))
      vars.setSessionValue(tableSQL.getTabID() + "|orderbySimple", txtAuxSimple.toString());

    Vector<String> positions = tableSQL.getOrderByPosition();
    txtAux = new StringBuffer();
    if (positions != null) {
      for (int i = 0; i < positions.size(); i++) {
        String auxStructure = positions.elementAt(i);
        auxStructure = Integer.toString((Integer.valueOf(auxStructure).intValue() + 1));
        if (!txtAux.toString().equals(""))
          txtAux.append(",");
        txtAux.append(auxStructure);
      }
    }
    vars.setSessionValue(tableSQL.getTabID() + "|orderbyPositions", txtAux.toString());

    Vector<String> directions = tableSQL.getOrderByDirection();
    txtAux = new StringBuffer();
    if (directions != null) {
      for (int i = 0; i < directions.size(); i++) {
        String auxStructure = directions.elementAt(i);
        if (!txtAux.toString().equals(""))
          txtAux.append(",");
        txtAux.append(auxStructure);
      }
    }
    vars.setSessionValue(tableSQL.getTabID() + "|orderbyDirections", txtAux.toString());
  }

  /**
   * Overloaded method with sorted columns by default
   */
  static String generateSQL(ConnectionProvider conn, VariablesSecureApp vars,
      TableSQLData tableSQL, String selectFields, Vector<String> filter,
      Vector<String> filterParams, int offset, int pageSize) throws Exception {
    return generateSQL(conn, vars, tableSQL, selectFields, filter, filterParams, offset, pageSize,
        true, false);
  }

  /**
   * Special case of the method which specifies that only the id column of the base table should be
   * returned. No extra left joins (besides possible sort columns) are needed.
   */
  public static String generateSQLonlyId(ConnectionProvider conn, VariablesSecureApp vars,
      TableSQLData tableSQL, String selectFields, Vector<String> filter,
      Vector<String> filterParams, int offset, int pageSize) throws Exception {
    return generateSQL(conn, vars, tableSQL, selectFields, filter, filterParams, offset, pageSize,
        true, true);
  }

  /**
   * Generates the query for this tab. This method adds to the standard query defined in the
   * TableSQLData (from dictionary) the user filter parameters and order by defined by UI
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Handler for the query builder.
   * @param selectFields
   *          String with the fields of the select clause.
   * @param filter
   *          Vector with the specific filter fields.
   * @param filterParams
   *          Vector with the parameters for the specific filter fields.
   * @param offset
   *          int, offset of rows to be displayed
   * @param pageSize
   *          int, number of rows to be displayed
   * @param onlyId
   *          only the id of the base table should be returned
   * @return String with the sql.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  static String generateSQL(ConnectionProvider conn, VariablesSecureApp vars,
      TableSQLData tableSQL, String selectFields, Vector<String> filter,
      Vector<String> filterParams, int offset, int pageSize, boolean sorted, boolean onlyId)
      throws Exception {
    Vector<String> orderBy = new Vector<String>(); // Maintains orderby
    // clause with SQL clause
    Vector<String> orderBySimple = new Vector<String>(); // Maintains
    // orderby clause
    // just with column
    // names
    String loadSessionOrder = vars.getSessionValue(tableSQL.getTabID() + "|newOrder");
    if (loadSessionOrder == null || loadSessionOrder.equals("")
        || vars.getSessionValue(tableSQL.getTabID() + "|orderbySimple").equals("")) {
      orderBy = getOrderBy(vars, tableSQL);
      orderBySimple = (Vector<String>) orderBy.clone();
    } else {
      String auxOrder = vars.getSessionValue(tableSQL.getTabID() + "|orderby");
      String auxOrderSimple = vars.getSessionValue(tableSQL.getTabID() + "|orderbySimple");
      if (!auxOrder.equals(""))
        orderBy.addElement(auxOrder);
      if (!auxOrderSimple.equals(""))
        orderBySimple.addElement(auxOrderSimple);
    }
    if (filter == null)
      filter = new Vector<String>();
    if (filterParams == null)
      filterParams = new Vector<String>();
    SQLReturnObject parametersData = getFilter(vars, tableSQL, filter, filterParams);
    String parentKey = tableSQL.getParentColumnName();
    if (parentKey != null && !parentKey.equals("")) {
      String aux = vars.getGlobalVariable("inpParentKey", tableSQL.getWindowID() + "|" + parentKey);
      if (!aux.equals("")) {
        if (parametersData == null)
          parametersData = new SQLReturnObject();
        parametersData.setData("PARENT", aux);
      }
    }
    String strSQL = tableSQL.getSQL(filter, filterParams, orderBy, null, selectFields,
        orderBySimple, offset, pageSize, sorted, onlyId);
    setSessionOrderBy(vars, tableSQL);
    Utility.fillTableSQLParameters(conn, vars, parametersData, tableSQL, tableSQL.getWindowID());
    return strSQL;
  }
}
