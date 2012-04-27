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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.reference.Reference;
import org.openbravo.reference.ui.UIReference;

/**
 * @author Fernando Iriazabal
 * 
 *         Handler to manage all the process for the tab's query building.
 */
public class TableSQLData implements Serializable {
  private static Logger log4j = Logger.getLogger(TableSQLData.class);
  private final String internalPrefix = "@@";
  private static final String FIELD_CONCAT = " || ' - ' || ";
  public static final String INACTIVE_DATA = "**";
  private VariablesSecureApp vars;
  private ConnectionProvider pool;
  private Hashtable<String, String> parameters = new Hashtable<String, String>();
  private Vector<Properties> structure = new Vector<Properties>();
  private Vector<QueryParameterStructure> paramSelect = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramFrom = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramSubquery = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramWhere = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramOrderBy = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramFilter = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramWrapper = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramInternalFilter = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramInternalOrderBy = new Vector<QueryParameterStructure>();
  private Vector<QueryFieldStructure> select = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> from = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> where = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> orderBy = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> orderBySimple = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> filter = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> internalFilter = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> internalOrderBy = new Vector<QueryFieldStructure>();
  private Vector<String> orderByPosition = new Vector<String>();
  private Vector<String> orderByDirection = new Vector<String>();
  public int index = 0;
  private boolean isSameTable = false;

  /**
   * Defines how many rows will be shown at maximum in any datagrid inside the scrollable area. If
   * there are more rows in the source table multiple pages of this size will be used.
   */
  public static final int maxRowsPerGridPage = 10000;

  /**
   * Constructor
   */
  public TableSQLData() {
  }

  /**
   * Constructor
   * 
   * @param _vars
   *          Handler for the session info.
   * @param _conn
   *          Handler for the database connection.
   * @param _adTabId
   *          Id of the tab.
   * @param _orgList
   *          List of granted organizations.
   * @param _clientList
   *          List of granted clients.
   * @throws Exception
   */
  public TableSQLData(VariablesSecureApp _vars, ConnectionProvider _conn, String _adTabId,
      String _orgList, String _clientList, boolean showAudit) throws Exception {
    if (_vars != null)
      setVars(_vars);
    setPool(_conn);
    setTabID(_adTabId);
    setOrgList(_orgList);
    setClientList(_clientList);
    setWindowDefinition();
    generateStructure(showAudit);
    generateSQL();
  }

  public TableSQLData(VariablesSecureApp _vars, ConnectionProvider _conn, String _adTabId,
      String _orgList, String _clientList) throws Exception {
    this(_vars, _conn, _adTabId, _orgList, _clientList, _vars.getSessionValue("#ShowAudit").equals(
        "Y"));
  }

  /**
   * Setter for the parameters.
   * 
   * @param name
   *          String with the name.
   * @param value
   *          String with the value.
   * @throws Exception
   */
  public void setParameter(String name, String value) throws Exception {
    if (name == null || name.equals(""))
      throw new Exception("Invalid parameter name");
    if (this.parameters == null)
      this.parameters = new Hashtable<String, String>();
    if (value == null || value.equals(""))
      this.parameters.remove(name.toUpperCase());
    else
      this.parameters.put(name.toUpperCase(), value);
  }

  /**
   * Getter for the parameters.
   * 
   * @param name
   *          String with the name of the parameter to get.
   * @return String with the value of the parameter.
   */
  String getParameter(String name) {
    if (name == null || name.equals(""))
      return "";
    else if (this.parameters == null)
      return "";
    else
      return this.parameters.get(name.toUpperCase());
  }

  /**
   * Gets all the parameters defined.
   * 
   * @return Vector with all the parameters defined.
   */
  Vector<String> getParameters() {
    Vector<String> result = new Vector<String>();
    if (log4j.isDebugEnabled())
      log4j.debug("Obtaining parameters");
    Vector<QueryParameterStructure> vAux = getSelectParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Select parameters obtained");
    vAux = getFromParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("From parameters obtained");
    vAux = getSubqueryParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("SubQuery parameters obtained");
    vAux = getWhereParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Where parameters obtained");
    vAux = getFilterParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Filter parameters obtained");
    vAux = getWrapperParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
      if (log4j.isDebugEnabled())
        log4j.debug("Wrapper parameters obtained");
    }
    vAux = getOrderByParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Order by parameters obtained");
    result.addElement("#AD_LANGUAGE");
    return result;
  }

  /**
   * Gets all the parameter's values defined.
   * 
   * @return Vector with the values.
   */
  Vector<String> getParameterValues() {
    Vector<String> result = new Vector<String>();
    if (log4j.isDebugEnabled())
      log4j.debug("Obtaining parameters values");
    Vector<QueryParameterStructure> vAux = getSelectParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Select parameters obtained");
    vAux = getFromParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("From parameters obtained");
    vAux = getSubqueryParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Subquery parameters obtained");
    vAux = getWhereParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Where parameters obtained");
    vAux = getFilterParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Filter parameters obtained");
    vAux = getWrapperParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    vAux = getOrderByParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Order by parameters obtained");
    return result;
  }

  /**
   * Gets the parameter's values defined, which are needed for onlyId queries. These correspond to
   * the sql generated by the getSql-function with onlyId=true
   * 
   * @return Vector with the values.
   */
  public Vector<String> getParameterValuesOnlyId() {
    Vector<String> result = new Vector<String>();
    Vector<QueryParameterStructure> vAux;
    // skip the following parameter types for onlyId queries: select, from
    vAux = getSubqueryParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Subquery parameters obtained");
    vAux = getWhereParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Where parameters obtained");

    vAux = getFilterParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Filter parameters obtained");

    vAux = getWrapperParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    vAux = getOrderByParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Order by parameters obtained");

    return result;
  }

  /**
   * Gets all the parameter's value for the total query.
   * 
   * @return Vector with the values.
   */
  Vector<String> getParameterValuesTotalSQL() {
    Vector<String> result = new Vector<String>();
    if (log4j.isDebugEnabled())
      log4j.debug("Obtaining parameters values");
    Vector<QueryParameterStructure> vAux = getFromParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("From parameters obtained");
    vAux = getWhereParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Where parameters obtained");
    vAux = getFilterParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Filter parameters obtained");
    return result;
  }

  /**
   * Setter for the handler of session info.
   * 
   * @param _vars
   *          Handler of session info.
   * @throws Exception
   */
  void setVars(VariablesSecureApp _vars) throws Exception {
    if (_vars == null)
      throw new Exception("The session vars is null");
    this.vars = _vars;
  }

  /**
   * Getter for the handler of session info.
   * 
   * @return Handler of session info.
   */
  public VariablesSecureApp getVars() {
    return this.vars;
  }

  /**
   * Setter for the handler of database connection.
   * 
   * @param _conn
   *          Handler of database connection.
   * @throws Exception
   */
  void setPool(ConnectionProvider _conn) throws Exception {
    if (_conn == null)
      throw new Exception("The pool is null");
    this.pool = _conn;
  }

  /**
   * Getter for the handler of database connection.
   * 
   * @return Handler of database connection.
   */
  public ConnectionProvider getPool() {
    return this.pool;
  }

  /**
   * Setter for the tab id.
   * 
   * @param _data
   *          String with the tab id.
   * @throws Exception
   */
  void setTabID(String _data) throws Exception {
    if (_data == null || _data.equals(""))
      throw new Exception("The Tab ID must be specified");
    setParameter(internalPrefix + "AD_Tab_ID", _data);
  }

  /**
   * Getter for the tab id.
   * 
   * @return String with the tab id.
   */
  public String getTabID() {
    return getParameter(internalPrefix + "AD_Tab_ID");
  }

  /**
   * Indicates if is of the same table or not.
   * 
   * @return Boolean that indicates if is of the same table or not.
   */
  boolean getIsSameTable() {
    return this.isSameTable;
  }

  /**
   * Method to set the parameters for the window information.
   * 
   * @throws Exception
   */
  void setWindowDefinition() throws Exception {
    TableSQLQueryData[] data = TableSQLQueryData.selectWindowDefinition(getPool(), getVars()
        .getLanguage(), getTabID());
    if (data == null || data.length == 0)
      throw new Exception("Couldn't extract window information");
    setParameter(internalPrefix + "AD_Window_ID", data[0].adWindowId);
    setParameter(internalPrefix + "AD_Table_ID", data[0].adTableId);
    setParameter(internalPrefix + "TabLevel", data[0].tablevel);
    setParameter(internalPrefix + "IsReadOnly", data[0].isreadonly);
    setParameter(internalPrefix + "HasTree", data[0].hastree);
    setParameter(internalPrefix + "WhereClause", data[0].whereclause);
    setParameter(internalPrefix + "OrderByClause", data[0].orderbyclause);
    setParameter(internalPrefix + "AD_Process_ID", data[0].adProcessId);
    setParameter(internalPrefix + "AD_ColumnSortOrder_ID", data[0].adColumnsortorderId);
    setParameter(internalPrefix + "AD_ColumnSortYesNo_ID", data[0].adColumnsortyesnoId);
    setParameter(internalPrefix + "IsSortTab", data[0].issorttab);
    setParameter(internalPrefix + "FilterClause", data[0].filterclause);
    setParameter(internalPrefix + "EditReference", data[0].editreference);
    setParameter(internalPrefix + "WindowType", data[0].windowtype);
    setParameter(internalPrefix + "IsSOTrx", data[0].issotrx);
    setParameter(internalPrefix + "WindowName", data[0].windowName);
    setParameter(internalPrefix + "WindowNameTrl", data[0].windowNameTrl);
    setParameter(internalPrefix + "TabName", data[0].tabName);
    setParameter(internalPrefix + "TabNameTrl", data[0].tabNameTrl);
    setParameter(internalPrefix + "TableName", data[0].tablename);
    if (!getTabLevel().equals("0")) {
      TableSQLQueryData[] parentFields = TableSQLQueryData.parentsColumnName(getPool(), getTabID());
      if (parentFields != null && parentFields.length > 0) {
        if (log4j.isDebugEnabled())
          log4j.debug("Parent key: " + parentFields[0].columnname);
        setParameter(internalPrefix + "ParentColumnName", parentFields[0].columnname);
      } else {
        parentFields = TableSQLQueryData.parentsColumnNameKey(getPool(), getTabID());
        if (parentFields != null && parentFields.length > 0) {
          if (log4j.isDebugEnabled())
            log4j.debug("Parent key: " + parentFields[0].columnname);
          setParameter(internalPrefix + "ParentColumnName", parentFields[0].columnname);
          this.isSameTable = true;
        }
      }
    }
  }

  /**
   * Gets the window id.
   * 
   * @return String with the window id.
   */
  String getWindowID() {
    return getParameter(internalPrefix + "AD_Window_ID");
  }

  /**
   * Gets the key column.
   * 
   * @return String with the key column.
   */
  public String getKeyColumn() {
    return getParameter(internalPrefix + "KeyColumn");
  }

  /**
   * Gets all the key columns including the secondary key ones
   * 
   * @param tableName
   *          tableName
   * @return a comma separated string with the list of key columns
   */
  String getKeyColumns(String tableName) {
    String result = "";
    Vector<Properties> headers = getStructure();
    if (headers == null || headers.size() == 0)
      return null;
    for (int i = 0; i < headers.size(); i++) {
      Properties element = headers.elementAt(i);
      if (element.getProperty("IsSecondaryKey").equals("Y")
          || element.getProperty("IsKey").equals("Y")) {
        if (result.length() > 0)
          result += ", ";
        if (!tableName.equals(""))
          result += tableName + ".";
        result += element.getProperty("ColumnName");
      }
    }
    return result;
  }

  /**
   * Gets the table id.
   * 
   * @return String with the table id.
   */
  String getTableID() {
    return getParameter(internalPrefix + "AD_Table_ID");
  }

  /**
   * Gets the table name.
   * 
   * @return String with the table name.
   */
  public String getTableName() {
    return getParameter(internalPrefix + "TableName");
  }

  /**
   * Gets the where clause.
   * 
   * @return String with the where clause.
   */
  String getWhereClause() {
    return getParameter(internalPrefix + "WhereClause");
  }

  /**
   * Gets the order by clause.
   * 
   * @return String with the order by clause.
   */
  String getOrderByClause() {
    return getParameter(internalPrefix + "OrderByClause");
  }

  /**
   * Gets the filter clause.
   * 
   * @return String with the filter clause.
   */
  String getFilterClause() {
    return getParameter(internalPrefix + "FilterClause");
  }

  /**
   * Gets the tab level.
   * 
   * @return String with the tab level.
   */
  String getTabLevel() {
    return getParameter(internalPrefix + "TabLevel");
  }

  /**
   * Gets the window type.
   * 
   * @return String with the window type.
   */
  String getWindowType() {
    return getParameter(internalPrefix + "WindowType");
  }

  /**
   * Gets the parent column name.
   * 
   * @return String with the parent column name.
   */
  String getParentColumnName() {
    return getParameter(internalPrefix + "ParentColumnName");
  }

  /**
   * Setter for the list of granted organizations.
   * 
   * @param _orgList
   *          Granted organizations.
   * @throws Exception
   */
  void setOrgList(String _orgList) throws Exception {
    setParameter(internalPrefix + "orgList", _orgList);
  }

  /**
   * Getter for the list of granted organizations.
   * 
   * @return String with the list.
   */
  String getOrgList() {
    return getParameter(internalPrefix + "orgList");
  }

  /**
   * Setter for the list of granted clients.
   * 
   * @param _clientList
   *          List of granted clients.
   * @throws Exception
   */
  void setClientList(String _clientList) throws Exception {
    setParameter(internalPrefix + "clientList", _clientList);
  }

  /**
   * Getter for the list of granted clients.
   * 
   * @return String with the list.
   */
  String getClientList() {
    return getParameter(internalPrefix + "clientList");
  }

  /**
   * Adds new structure of window information.
   * 
   * @param _prop
   *          Properties with the window information.
   */
  void addStructure(Properties _prop) {
    if (_prop == null)
      return;
    if (this.structure == null)
      this.structure = new Vector<Properties>();
    this.structure.addElement(_prop);
  }

  /**
   * Gets the structure with the window information.
   * 
   * @return Vector with the information.
   */
  Vector<Properties> getStructure() {
    return this.structure;
  }

  /**
   * Adds new order by field by position.
   * 
   * @param _data
   *          String with the order by.
   */
  void addOrderByPosition(String _data) {
    if (_data == null)
      return;
    if (this.orderByPosition == null)
      this.orderByPosition = new Vector<String>();
    this.orderByPosition.addElement(_data);
  }

  /**
   * Gets order by fields by position
   * 
   * @return Vector with the positions of the order by fields.
   */
  Vector<String> getOrderByPosition() {
    return this.orderByPosition;
  }

  /**
   * Adds new order by direction (Asc, Desc)
   * 
   * @param _data
   *          String with the order by direction.
   */
  void addOrderByDirection(String _data) {
    if (_data == null)
      return;
    if (this.orderByDirection == null)
      this.orderByDirection = new Vector<String>();
    this.orderByDirection.addElement(_data);
  }

  /**
   * Gets the list of directions for the order by fields.
   * 
   * @return Vector with the directions.
   */
  Vector<String> getOrderByDirection() {
    return this.orderByDirection;
  }

  /**
   * Gets the window information filtered.
   * 
   * @param propertyName
   *          Name of the property to filter.
   * @param propertyValue
   *          Vaule of the property to filter.
   * @return Vector with the filtered information.
   */
  Vector<Properties> getFilteredStructure(String propertyName, String propertyValue) {
    Vector<Properties> vAux = new Vector<Properties>();
    if (this.structure == null)
      return vAux;
    for (Enumeration<Properties> e = this.structure.elements(); e.hasMoreElements();) {
      Properties prop = e.nextElement();
      if (prop.getProperty(propertyName).equals(propertyValue))
        vAux.addElement(prop);
    }
    return vAux;
  }

  void addAuditFields(Vector<Properties> properties) {
    if (this.structure == null)
      return;
    for (Enumeration<Properties> e = this.structure.elements(); e.hasMoreElements();) {
      Properties prop = e.nextElement();
      if (prop.getProperty("ColumnName").equalsIgnoreCase("Created")
          || prop.getProperty("ColumnName").equalsIgnoreCase("CreatedBy")
          || prop.getProperty("ColumnName").equalsIgnoreCase("Updated")
          || prop.getProperty("ColumnName").equalsIgnoreCase("UpdatedBy"))
        properties.addElement(prop);
    }
  }

  /**
   * Setter for the table alias index.
   * 
   * @param _index
   *          Integer with the new index.
   */
  void setIndex(int _index) {
    this.index = _index;
  }

  /**
   * Getter for the table alias index.
   * 
   * @return Integer with the index.
   */
  int getIndex() {
    return this.index;
  }

  /**
   * Adds new field to the select clause.
   * 
   * @param _field
   *          String with the field.
   * @param _alias
   *          String with the alias.
   */
  public void addSelectField(String _field, String _alias) {
    addSelectField(_field, _alias, false);
  }

  /**
   * Adds new field to the select clause.
   * 
   * @param _field
   *          String with the field.
   * @param _alias
   *          String with the alias.
   * @param _new
   *          Boolean to know if the field is new (not used).
   */
  void addSelectField(String _field, String _alias, boolean _new) {
    QueryFieldStructure p = new QueryFieldStructure(_field, " AS ", _alias, "SELECT");
    if (this.select == null)
      this.select = new Vector<QueryFieldStructure>();
    else {
      QueryFieldStructure pOld = getSelectFieldElement(_alias);
      if (pOld != null) {
        p = new QueryFieldStructure(pOld.getField() + FIELD_CONCAT + _field, " AS ", _alias,
            "SELECT");
        this.select.remove(pOld);
      }
    }
    this.select.addElement(p);
  }

  /**
   * Gets a field of the select clause by the alias name.
   * 
   * @param _alias
   *          String with the alias.
   * @return Object with the field.
   */
  QueryFieldStructure getSelectFieldElement(String _alias) {
    if (this.select == null)
      return null;
    for (int i = 0; i < this.select.size(); i++) {
      QueryFieldStructure p = this.select.elementAt(i);
      if (p.getAlias().equalsIgnoreCase(_alias))
        return p;
    }
    return null;
  }

  /**
   * Gets field of the select clause by the alias name.
   * 
   * @param _alias
   *          String with the alias.
   * @return String with the field.
   */
  String getSelectField(String _alias) {
    if (this.select == null)
      return "";
    for (int i = 0; i < this.select.size(); i++) {
      QueryFieldStructure p = this.select.elementAt(i);
      if (p.getAlias().equalsIgnoreCase(_alias))
        return p.getField();
    }
    return "";
  }

  /**
   * Gets the list of fields of the select clause.
   * 
   * @return Vector with the fields.
   */
  Vector<QueryFieldStructure> getSelectFields() {
    return this.select;
  }

  /**
   * Adds new field to the from clause.
   * 
   * @param _field
   *          String with the field.
   * @param _alias
   *          String with the alias.
   */
  public void addFromField(String _field, String _alias, String _realName) {
    QueryFieldStructure p = new QueryFieldStructure(_field, " ", _alias, "FROM", _realName);
    if (this.from == null)
      this.from = new Vector<QueryFieldStructure>();
    from.addElement(p);
  }

  /**
   * Gets the list of fields of the from clause.
   * 
   * @return Vector with the list of fields.
   */
  Vector<QueryFieldStructure> getFromFields() {
    return this.from;
  }

  /**
   * Adds new field to the where clause.
   * 
   * @param _field
   *          String with the field.
   * @param _type
   *          String with the type of field.
   */
  void addWhereField(String _field, String _type) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", _type);
    if (this.where == null)
      this.where = new Vector<QueryFieldStructure>();
    where.addElement(p);
  }

  /**
   * Gets the list of fields of the where clause.
   * 
   * @return Vector with the list of fields.
   */
  Vector<QueryFieldStructure> getWhereFields() {
    return this.where;
  }

  /**
   * Adds new field to the filter clause.
   * 
   * @param _field
   *          Strin with the field.
   * @param _type
   *          String with the type of field.
   */
  void addFilterField(String _field, String _type) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", _type);
    if (this.filter == null)
      this.filter = new Vector<QueryFieldStructure>();
    filter.addElement(p);
  }

  /**
   * Gets the list of the fields of the filter clause.
   * 
   * @return Vector with the list.
   */
  Vector<QueryFieldStructure> getFilterFields() {
    return this.filter;
  }

  /**
   * Returns true in case the tab has internal filters. Internal filters are added to tabs with
   * filter clause and to transactional windows.
   */
  public boolean hasInternalFilter() {
    return internalFilter.size() > 0;
  }

  /**
   * Adds fields to the internal filter clause. The internal filter clause is the one defined by the
   * tab in the dictionary.
   * 
   * @param _field
   *          String with the field.
   * @param _type
   *          String with the type.
   */
  void addInternalFilterField(String _field, String _type) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", _type);
    if (this.internalFilter == null)
      this.internalFilter = new Vector<QueryFieldStructure>();
    internalFilter.addElement(p);
  }

  /**
   * Gets the list of fields of the internal filter clause.
   * 
   * @return Vector with the list of fields.
   */
  Vector<QueryFieldStructure> getInternalFilterFields() {
    return this.internalFilter;
  }

  /**
   * Adds a field to the order by clause.
   * 
   * @param _field
   *          String with the field.
   */
  void addOrderByField(String _field) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", "ORDERBY");
    if (this.orderBy == null)
      this.orderBy = new Vector<QueryFieldStructure>();
    this.orderBy.addElement(p);
  }

  /**
   * Adds a field to the order by clause.
   * 
   * @param _field
   *          String with the field.
   */
  void addOrderByFieldSimple(String _field) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", "ORDERBY");
    if (this.orderBySimple == null)
      this.orderBySimple = new Vector<QueryFieldStructure>();
    this.orderBySimple.addElement(p);
  }

  /**
   * Gets the list of fields of the order by clause.
   * 
   * @return Vector with the list of fields.
   */
  Vector<QueryFieldStructure> getOrderByFields() {
    return this.orderBy;
  }

  /**
   * Gets the list of fields of the order by clause.
   * 
   * @return Vector with the list of fields.
   */
  Vector<QueryFieldStructure> getOrderBySimpleFields() {
    return this.orderBySimple;
  }

  /**
   * Gets the list of fields of the order by clause.
   * 
   * @return Vector with the list of fields.
   */
  Vector<String> getOrderBySimpleFieldsString() {
    Vector<String> vOrderBy = new Vector<String>();
    if (this.orderBySimple != null) {
      for (int i = 0; i < this.orderBySimple.size(); i++) {
        vOrderBy.add(this.orderBySimple.elementAt(i).getField());
      }
    }
    return vOrderBy;
  }

  /**
   * Tokenizes vector elements and inserts it as new vector elements and removes duplicates from
   * vector
   * 
   * @param v
   * @return a cleaned vector of Strings
   */
  Vector<String> cleanVector(Vector<String> v) {
    Vector<String> result = new Vector<String>();
    Vector<String> aux = new Vector<String>();
    if (v != null) {
      for (int i = 0; i < v.size(); i++) {
        StringTokenizer element = new StringTokenizer(v.elementAt(i), ",", false);
        while (element.hasMoreTokens()) {
          String token = element.nextToken().trim();
          aux.add(token);
        }
      }
    }
    if (aux != null) {
      for (int i = 0; i < aux.size(); i++) {
        if (result.indexOf(aux.elementAt(i)) == -1)
          result.add(aux.elementAt(i));
      }
    }
    return result;
  }

  /**
   * Adds new field to the internal order by clause. The internal order by clause is the one defined
   * in the application dictionary for the tab.
   * 
   * @param _field
   *          String with the field.
   */
  void addInternalOrderByField(String _field) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", "ORDERBY");
    if (this.internalOrderBy == null)
      this.internalOrderBy = new Vector<QueryFieldStructure>();
    this.internalOrderBy.addElement(p);
  }

  /**
   * Gets the list of fields of the internal order by clause.
   * 
   * @return Vector with the list of fields.
   */
  Vector<QueryFieldStructure> getInternalOrderByFields() {
    return this.internalOrderBy;
  }

  /**
   * Adds a new parameter to the select clause.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the name of the field for this parameter.
   */
  void addSelectParameter(String _parameter, String _fieldName) {
    if (this.paramSelect == null)
      this.paramSelect = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "SELECT");
    this.paramSelect.addElement(aux);
  }

  /**
   * Gets the list of parameters of the select clause.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getSelectParameters() {
    return this.paramSelect;
  }

  /**
   * Adds new parameter to the from clause.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the field of this parameter.
   */
  public void addFromParameter(String _parameter, String _fieldName, String _realName) {
    if (this.paramFrom == null)
      this.paramFrom = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "FROM",
        _realName);
    this.paramFrom.addElement(aux);
  }

  /**
   * Adds new parameter to the subquery clause.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the field of this parameter.
   */
  void addSubQueryParameter(String _parameter, String _fieldName, String _realName) {
    if (this.paramSubquery == null)
      this.paramSubquery = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "SUB",
        _realName);
    this.paramSubquery.addElement(aux);
  }

  /**
   * Gets the list of parameters for the from clause.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getFromParameters() {
    return this.paramFrom;
  }

  /**
   * Gets the list of parameters for the from clause.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getSubqueryParameters() {
    return this.paramSubquery;
  }

  /**
   * Adds new parameter to the where clause.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the field of this parameter.
   * @param _type
   *          String with the type of parameter.
   */
  void addWhereParameter(String _parameter, String _fieldName, String _type) {
    if (this.paramWhere == null)
      this.paramWhere = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, _type);
    this.paramWhere.addElement(aux);
  }

  /**
   * Gets the list of parameters for the where clause.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getWhereParameters() {
    return this.paramWhere;
  }

  /**
   * Adds new parameter to the filter clause.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the field for the parameter.
   * @param _type
   *          String with the type of parameter.
   */
  void addFilterParameter(String _parameter, String _fieldName, String _type) {
    if (this.paramFilter == null)
      this.paramFilter = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, _type);
    this.paramFilter.addElement(aux);
  }

  /**
   * Adds new parameter to the wrapper clause.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the field for the parameter.
   * @param _type
   *          String with the type of parameter.
   */
  void addWrapperParameter(String _parameter, String _fieldName, String _type) {
    if (this.paramWrapper == null)
      this.paramWrapper = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, _type);
    this.paramWrapper.addElement(aux);
  }

  /**
   * Gets the list of parameters for the filter clause.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getFilterParameters() {
    return this.paramFilter;
  }

  /**
   * Adds new parameter to the internal filter clause (the one defined in the dictionary).
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the field for the parameter.
   * @param _type
   *          String with the type of parameter.
   */
  void addInternalFilterParameter(String _parameter, String _fieldName, String _type) {
    if (this.paramInternalFilter == null)
      this.paramInternalFilter = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, _type);
    this.paramInternalFilter.addElement(aux);
  }

  /**
   * Gets the list of parameters for the internal filter clause.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getInternalFilterParameters() {
    return this.paramInternalFilter;
  }

  /**
   * Adds new parameter to the order by clause.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the field for the parameter.
   */
  void addOrderByParameter(String _parameter, String _fieldName) {
    if (this.paramOrderBy == null)
      this.paramOrderBy = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "ORDERBY");
    this.paramOrderBy.addElement(aux);
  }

  /**
   * Gets the list of parameters for the order by clause.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getOrderByParameters() {
    return this.paramOrderBy;
  }

  /**
   * Gets the list of parameters for the wrapper.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getWrapperParameters() {
    return this.paramWrapper;
  }

  /**
   * Adds new parameter to the internal order by clause (the one defined in the dictionary).
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the field for the parameter.
   */
  void addInternalOrderByParameter(String _parameter, String _fieldName) {
    if (this.paramInternalOrderBy == null)
      this.paramInternalOrderBy = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "ORDERBY");
    this.paramInternalOrderBy.addElement(aux);
  }

  /**
   * Gets the list of parameters for the internal order by clause.
   * 
   * @return Vector with the list of parameters
   */
  Vector<QueryParameterStructure> getInternalOrderByParameters() {
    return this.paramInternalOrderBy;
  }

  /**
   * Method to parse the text of the clauses, searching the @ parameters.
   * 
   * @param context
   *          Text to parse.
   * @param type
   *          Type of the text (the clause).
   * @return String parsed.
   */
  String parseContext(String context, String type) {
    if (context == null || context.equals(""))
      return "";
    StringBuffer strOut = new StringBuffer();
    String value = new String(context);
    String token, defStr;
    int i = value.indexOf("@");
    while (i != -1) {
      strOut.append(value.substring(0, i));
      value = value.substring(i + 1);
      int j = value.indexOf("@");
      if (j == -1) {
        strOut.append(value);
        return strOut.toString();
      }
      token = value.substring(0, j);
      if (token.equalsIgnoreCase("#User_Client"))
        defStr = getClientList();
      else if (token.equalsIgnoreCase("#User_Org"))
        defStr = getOrgList();
      else
        defStr = "?";

      if (defStr.equals("?")) {
        if (type.equalsIgnoreCase("WHERE"))
          addWhereParameter(token, type.toUpperCase(), type.toUpperCase());
        else if (type.equalsIgnoreCase("FILTER"))
          addInternalFilterParameter(token, type.toUpperCase(), type.toUpperCase());
        else if (type.equalsIgnoreCase("INTERNAL_ORDERBY"))
          addInternalOrderByParameter(token, "ORDERBY");
      }
      strOut.append(defStr);
      value = value.substring(j + 1);
      i = value.indexOf("@");
    }
    strOut.append(value);
    return strOut.toString().replace("'?'", "?");
  }

  private void setProperties(Properties prop, TableSQLQueryData data) {
    prop.setProperty("ColumnName", data.columnname);
    prop.setProperty("AD_Reference_ID", data.adReferenceId);
    prop.setProperty("AD_Reference_Value_ID", data.adReferenceValueId);
    prop.setProperty("AD_Val_Rule_ID", data.adValRuleId);
    prop.setProperty("FieldLength", data.fieldlength);
    prop.setProperty("DefaultValue", data.defaultvalue);
    prop.setProperty("IsKey", data.iskey);
    prop.setProperty("IsParent", data.isparent);
    prop.setProperty("IsMandatory", data.ismandatory);
    prop.setProperty("IsUpdateable", data.isupdateable);
    prop.setProperty("ReadOnlyLogic", data.readonlylogic);
    prop.setProperty("IsIdentifier", data.isidentifier);
    prop.setProperty("SeqNo", data.seqno);
    prop.setProperty("IsTranslated", data.istranslated);
    prop.setProperty("IsEncrypted", data.isencrypted);
    prop.setProperty("VFormat", data.vformat);
    prop.setProperty("ValueMin", data.valuemin);
    prop.setProperty("ValueMax", data.valuemax);
    prop.setProperty("IsSelectionColumn", data.isselectioncolumn);
    prop.setProperty("AD_Process_ID", data.adProcessId);
    prop.setProperty("IsSessionAttr", data.issessionattr);
    prop.setProperty("IsSecondaryKey", data.issecondarykey);
    prop.setProperty("IsDesencryptable", data.isdesencryptable);
    prop.setProperty("AD_CallOut_ID", data.adCalloutId);
    prop.setProperty("Name", data.name);
    prop.setProperty("AD_FieldGroup_ID", data.adFieldgroupId);
    prop.setProperty("IsDisplayed", data.isdisplayed);
    prop.setProperty("DisplayLogic", data.displaylogic);
    prop.setProperty("DisplayLength", data.displaylength);
    prop.setProperty("IsReadOnly", data.isreadonly);
    prop.setProperty("SortNo", data.sortno);
    prop.setProperty("IsSameLine", data.issameline);
    prop.setProperty("IsFieldOnly", data.isfieldonly);
    prop.setProperty("ShowInRelation", data.showinrelation);
    prop.setProperty("ColumnNameSearch", data.columnnameSearch);
  }

  /**
   * Generates the needed information of the tab. The columns, the ids...
   * 
   * @throws Exception
   */
  private void generateStructure(boolean showAudit) throws Exception {
    if (getPool() == null)
      throw new Exception("No pool defined for database connection");
    else if (getTabID().equals(""))
      throw new Exception("No Tab defined");

    TableSQLQueryData[] data = TableSQLQueryData.selectRelationStructure(getPool(), getVars()
        .getLanguage(), getTabID());
    if (data == null || data.length == 0)
      throw new Exception("Couldn't get structure for tab " + getTabID());
    String primaryKey = "";
    String secondaryKey = "";
    for (int i = 0; i < data.length; i++) {
      Properties prop = new Properties();

      setProperties(prop, data[i]);
      addStructure(prop);
      String parentKey = getParentColumnName();
      if (parentKey == null)
        parentKey = "";
      if (primaryKey.equals("") && data[i].iskey.equals("Y")) {
        primaryKey = data[i].columnname;
      }

    }

    if (showAudit) {
      TableSQLQueryData[] dataAudit = TableSQLQueryData.selectRelationStructureAudit(getPool(),
          getVars().getLanguage(), getTabID());
      if (dataAudit != null) {
        for (int i = 0; i < dataAudit.length; i++) {
          Properties prop = new Properties();

          setProperties(prop, dataAudit[i]);
          addStructure(prop);
        }
      }
    }

    if (!primaryKey.equals(""))
      setParameter(internalPrefix + "KeyColumn", primaryKey);
    else if (!secondaryKey.equals(""))
      setParameter(internalPrefix + "KeyColumn", secondaryKey);
    else {
      primaryKey = TableSQLQueryData.columnNameKey(getPool(), getTabID());
      if (!primaryKey.equals(""))
        setParameter(internalPrefix + "KeyColumn", primaryKey);
      else
        throw new Exception("No column key defined for this tab");
    }
  }

  /**
   * Generates the sql with the given information.
   * 
   * @throws Exception
   */
  void generateSQL() throws Exception {
    if (getPool() == null)
      throw new Exception("No pool defined for database connection");
    Vector<Properties> headers = getStructure();
    if (headers == null || headers.size() == 0)
      throw new Exception("No structure defined");
    addFromField(getTableName(), getTableName(), getTableName());
    for (Enumeration<Properties> e = headers.elements(); e.hasMoreElements();) {
      Properties prop = e.nextElement();

      UIReference reference = Reference.getUIReference(prop.getProperty("AD_Reference_ID"),
          prop.getProperty("AD_Reference_Value_ID"));
      reference.generateSQL(this, prop);
    }
    setWindowFilters();
  }

  /**
   * Search for the close char inside a text. This method bear in mind that in the string can be
   * another open chars with their own close char.
   * 
   * @param text
   *          String where is the close char to search.
   * @param pos
   *          Integer with the start position.
   * @param openChar
   *          The open char.
   * @param closeChar
   *          The close char.
   * @return Integer with the position of the close char or -1 if isn't.
   */
  private int findCloseTarget(String text, int pos, String openChar, String closeChar) {
    if (text == null || text.equals(""))
      return -1;
    int nextOpen = text.indexOf(openChar, pos);
    int nextClose = text.indexOf(closeChar, pos);
    if (nextClose != -1) {
      while (pos != -1 && nextClose != -1 && (nextOpen != -1 && nextClose > nextOpen)) {
        pos = findCloseTarget(text, nextOpen + 1, openChar, closeChar);
        nextOpen = text.indexOf(openChar, pos);
        nextClose = text.indexOf(closeChar, pos);
      }
      if (pos == -1)
        nextClose = -1;
    }
    return nextClose;
  }

  /**
   * Return the given order by clause into a structured field Vector.
   * 
   * @param text
   *          order by clause.
   * @return Vector with the fields.
   */
  private Vector<String> getOrdeByIntoFields(String text) {
    Vector<String> result = new Vector<String>();
    if (log4j.isDebugEnabled())
      log4j.debug("Parsing text: " + text);
    if (text != null && !text.equals("")) {
      int lastPos = 0;
      int openPos = text.indexOf("(");
      int actualPos = text.indexOf(",");
      while (actualPos != -1) {
        if (actualPos > openPos)
          openPos = findCloseTarget(text, openPos + 1, "(", ")");
        if (openPos == -1) {
          Vector<String> vAux = new Vector<String>();
          vAux.add(text);
          // Vector seems not to be correct, let's try to clean it
          if (log4j.isDebugEnabled())
            log4j.debug("Clean vector:" + vAux.toString());
          return cleanVector(vAux);
        } else {
          result.addElement(text.substring(lastPos, actualPos));
          lastPos = actualPos + 1;
          openPos = text.indexOf("(", lastPos);
          actualPos = text.indexOf(",", lastPos);
        }
      }
      if (lastPos != -1 && lastPos < text.length())
        result.addElement(text.substring(lastPos));
    }
    return result;
  }

  /**
   * Sets the filters for the window, including the filters by granted clients and organizations.
   * 
   * @throws Exception
   */
  private void setWindowFilters() throws Exception {
    String strWhereClause = getWhereClause();
    if (strWhereClause != null && !strWhereClause.equals("")) {
      if (strWhereClause.indexOf("@") != -1)
        strWhereClause = parseContext(strWhereClause, "WHERE");
      addWhereField(strWhereClause, "WHERE");
    }
    if (!getTableName().toUpperCase().endsWith("_ACCESS")) {
      addWhereField(getTableName() + ".AD_Client_ID IN (" + getClientList() + ")", "WHERE");
      addWhereField(getTableName() + ".AD_Org_ID IN (" + getOrgList() + ")", "WHERE");
    }
    String parentKey = getParentColumnName();
    if (parentKey != null && !parentKey.equals("")) {
      addWhereField(getTableName() + "." + parentKey + " = ?", "PARENT");
      addWhereParameter("PARENT", "PARENT", "PARENT");
    }
    String strOrderByClause = getOrderByClause();
    if (strOrderByClause == null || strOrderByClause.equals(""))
      getFieldsOrderByClause();
    else {
      if (strOrderByClause.indexOf("@") != -1)
        strOrderByClause = parseContext(strOrderByClause, "INTERNAL_ORDERBY");
      Vector<String> vecOrdersAux = getOrdeByIntoFields(strOrderByClause);
      if (vecOrdersAux != null && vecOrdersAux.size() > 0) {
        for (int i = 0; i < vecOrdersAux.size(); i++)
          addInternalOrderByField(getRealOrderByColumn(vecOrdersAux.elementAt(i)));
      } else
        addInternalOrderByField(getRealOrderByColumn(strOrderByClause));
    }
    String strFilterClause = getFilterClause();
    if (strFilterClause != null && !strFilterClause.equals("")) {
      if (strFilterClause.indexOf("@") != -1)
        strFilterClause = parseContext(strFilterClause, "FILTER");
      addInternalFilterField(strFilterClause, "FILTER");
    }
    if (getWindowType().equalsIgnoreCase("T") && getTabLevel().equals("0")) {
      /*
       * using now() as a function on oracle so that it is evaluated many times in a query is very
       * inefficient. Special case it for oracle/postgres
       */
      if (getPool().getRDBMS().equalsIgnoreCase("ORACLE")) {
        addInternalFilterField("(COALESCE(" + getTableName() + ".Processed, 'N')='N' OR "
            + getTableName() + ".Updated > sysdate-TO_NUMBER(?))", "FILTER");
      } else {
        addInternalFilterField("(COALESCE(" + getTableName() + ".Processed, 'N')='N' OR "
            + getTableName() + ".Updated > now()-TO_NUMBER(?))", "FILTER");
      }
      addInternalFilterParameter("#Transactional$Range", "FILTER", "FILTER");
    }
  }

  /**
   * Returns the order by clause of the application dictionary into the handler structure.
   * 
   * @throws Exception
   */
  private void getFieldsOrderByClause() throws Exception {
    TableSQLQueryData[] orderByData = TableSQLQueryData.selectOrderByFields(getPool(), getTabID());
    if (orderByData == null)
      return;
    for (int i = 0; i < orderByData.length; i++) {
      addInternalOrderByField(getTableName() + "." + orderByData[i].columnname + " "
          + (orderByData[i].columnname.equalsIgnoreCase("DocumentNo") ? "DESC" : "ASC"));
    }
  }

  /**
   * Returns the actual order by columns. The columns names inside the handler.
   * 
   * @param data
   *          String with the order by clause.
   * @return String with the correct columns names.
   */
  private String getRealOrderByColumn(String data) {
    if (data == null || data.equals(""))
      return "";
    data = data.trim();
    String orderDirection = "ASC";
    String orderField = data;
    int pos = data.toUpperCase().lastIndexOf(" DESC");
    if (pos == -1 || pos != (data.length() - 5)) {
      pos = data.toUpperCase().lastIndexOf(" ASC");
      if (pos != -1 && pos == (data.length() - 4)) {
        orderField = data.substring(0, pos);
      } else
        return data;
    } else {
      orderField = data.substring(0, pos);
      orderDirection = "DESC";
    }
    String _alias = getSelectFieldAlias(orderField);
    if (!_alias.equals("") && this.structure != null) {
      boolean isTranslated = false;
      if (_alias.endsWith("_R")) {
        isTranslated = true;
        _alias = _alias.substring(0, _alias.length() - 2);
      }
      int position = -1;
      UIReference reference = null;
      Properties myProps = getColumnPosition(_alias);
      if (myProps != null) {
        position = Integer.valueOf(myProps.getProperty("Position")).intValue();

        reference = Reference.getUIReference(myProps.getProperty("AD_Reference_ID"),
            myProps.getProperty("AD_Reference_Value_ID"));
      }
      if (position != -1) {
        addOrderByPosition(Integer.toString(position));
        addOrderByDirection(orderDirection);
      }
      if (!isTranslated) {
        String aux = getSelectField(_alias + "_R");
        if (aux != null && !aux.equals(""))
          orderField = aux;
      }
    }
    return orderField + " " + orderDirection;
  }

  /**
   * Returns the columns defined for this tab, including the ones derived from the references.
   * 
   * @return Array with the columns.
   */
  SQLReturnObject[] getHeaders() {
    return getHeaders(false, false);
  }

  SQLReturnObject[] getHeaders(boolean useFieldLength) {
    return getHeaders(false, useFieldLength);
  }

  /**
   * Returns the columns defined for this tab, including the ones derived from the references.
   * 
   * @param withoutIdentifiers
   *          Boolean to indicate if the identifier columns must be removed from the list.
   * @param useFieldLength
   *          Boolean to indicate if the column width should be calculated based on the FieldLength
   *          instead of DisplayLength.
   * @return Array with the columns.
   */
  SQLReturnObject[] getHeaders(boolean withoutIdentifiers, boolean useFieldLength) {
    SQLReturnObject[] data = null;
    Vector<SQLReturnObject> vAux = new Vector<SQLReturnObject>();
    Vector<Properties> structure = getStructure();
    for (Enumeration<Properties> e = structure.elements(); e.hasMoreElements();) {
      Properties prop = e.nextElement();
      if (prop.getProperty("IsKey").equals("Y")
          || prop.getProperty("IsSecondaryKey").equals("Y")
          || (prop.getProperty("IsDisplayed").equals("Y")
              && prop.getProperty("ShowInRelation").equals("Y")
              && prop.getProperty("IsEncrypted").equals("N") && !prop.getProperty("ColumnName")
              .equals(getParentColumnName()))) {
        SQLReturnObject dataAux = new SQLReturnObject();

        boolean cloneRecord = (!withoutIdentifiers
            && (prop.getProperty("IsDisplayed").equals("Y") && prop.getProperty("ShowInRelation")
                .equals("Y")) && prop.getProperty("ColumnName").equals(getKeyColumn()) && !getSelectField(
            prop.getProperty("ColumnName") + "_R").equals(""));

        dataAux.setData("columnname", prop.getProperty("ColumnName"));
        dataAux.setData("gridcolumnname", prop.getProperty("ColumnName"));
        dataAux.setData("adReferenceId", prop.getProperty("AD_Reference_ID"));
        dataAux.setData("adReferenceValueId", prop.getProperty("AD_Reference_Value_ID"));
        dataAux.setData("isidentifier", (prop.getProperty("IsIdentifier").equals("Y") ? "true"
            : "false"));

        boolean isKey = (!getTabID().equals("445")
            && prop.getProperty("ColumnName").equals(getKeyColumn()) && !cloneRecord)
            || (getTabID().equals("445") && prop.getProperty("ColumnName").equals("AD_Language"));

        dataAux.setData("iskey", isKey ? "true" : "false");
        dataAux.setData("isvisible", ((prop.getProperty("IsDisplayed").equals("Y") && prop
            .getProperty("ShowInRelation").equals("Y")) ? "true" : "false"));
        dataAux.setData("name", prop.getProperty("Name"));

        UIReference reference = Reference.getUIReference(prop.getProperty("AD_Reference_ID"),
            prop.getProperty("AD_Reference_Value_ID"));
        String type = reference.getGridType();

        dataAux.setData("type", type);
        String strWidth = "";

        if (useFieldLength)
          strWidth = prop.getProperty("FieldLength");
        else
          strWidth = prop.getProperty("DisplayLength");

        if (strWidth == null || strWidth.equals(""))
          strWidth = "0";
        int width = Integer.valueOf(strWidth).intValue();
        width = width * 6;

        if (!useFieldLength) {
          if (width < 23)
            width = 23;
          else if (width > 300)
            width = 300;
        }
        dataAux.setData("width", Integer.toString(width));

        if (cloneRecord) {
          dataAux.setData("isvisible", "true");
          dataAux.setData("gridcolumnname", prop.getProperty("ColumnName"));
        }
        vAux.addElement(dataAux);
        if (cloneRecord)
          vAux.addElement(getClone(dataAux));
      }
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  /**
   * Auxiliar method to clone the column object. It's used to add key column when the key column
   * must be a reference one.
   * 
   * @param data
   *          Column object to clone.
   * @return New cloned column object.
   */
  SQLReturnObject getClone(SQLReturnObject data) {
    SQLReturnObject dataAux = new SQLReturnObject();
    dataAux.setData("columnname", data.getData("columnname"));
    dataAux.setData("gridcolumnname", "keyname");
    dataAux.setData("adReferenceId", data.getData("adReferenceId"));
    dataAux.setData("adReferenceValueId", data.getData("adReferenceValueId"));
    dataAux.setData("isidentifier", data.getData("isidentifier"));
    dataAux.setData("iskey", "true");
    dataAux.setData("isvisible", "false");
    dataAux.setData("name", data.getData("name"));
    dataAux.setData("type", data.getData("type"));
    dataAux.setData("width", data.getData("width"));
    return dataAux;
  }

  /**
   * Gets the position of a column.
   * 
   * @param _name
   *          String with the column name.
   * @return Properties with the position and other information.
   */
  Properties getColumnPosition(String _name) {
    SQLReturnObject[] vAux = getHeaders();
    Properties _prop = new Properties();
    for (int i = 0; i < vAux.length; i++) {
      if (vAux[i].getData("columnname").equalsIgnoreCase(_name)
          || (getTableName() + "." + vAux[i].getData("columnname")).equalsIgnoreCase(_name)) {
        _prop.setProperty("AD_Reference_ID", vAux[i].getData("adReferenceId"));
        _prop.setProperty("Position", Integer.toString(i));
        return _prop;
      }
    }
    return null;
  }

  /**
   * Gets the alias of the selected field.
   * 
   * @param _data
   *          String with the field.
   * @return String with the alias.
   */
  String getSelectFieldAlias(String _data) {
    if (this.select == null)
      return "";
    for (int i = 0; i < this.select.size(); i++) {
      QueryFieldStructure p = this.select.elementAt(i);
      if (p.getField().equalsIgnoreCase(_data))
        return p.getAlias();
      else if (p.getAlias().equalsIgnoreCase(_data))
        return p.getAlias();
      else if ((getTableName() + "." + p.getAlias()).equalsIgnoreCase(_data.trim()))
        return p.getAlias();
    }
    return "";
  }

  /**
   * Sets the order by clause.
   * 
   * @param _fields
   *          Vector with the order by fields.
   * @param _params
   *          Vector with the order by parameters.
   */
  void setOrderBy(Vector<String> _fields, Vector<String> _params) {
    this.orderBy = new Vector<QueryFieldStructure>();
    this.paramOrderBy = new Vector<QueryParameterStructure>();
    if (_fields == null || _fields.size() == 0) {
      if (this.internalOrderBy != null) {
        for (int i = 0; i < this.internalOrderBy.size(); i++) {
          QueryFieldStructure aux = this.internalOrderBy.elementAt(i);
          addOrderByField(getRealOrderByColumn(aux.getField()));
          addOrderByFieldSimple(aux.getField());
        }
        if (this.paramInternalOrderBy != null) {
          for (int i = 0; i < this.paramInternalOrderBy.size(); i++) {
            QueryParameterStructure aux = this.paramInternalOrderBy.elementAt(i);
            addOrderByParameter(aux.getName(), aux.getField());
          }
        }
      }
    } else {
      for (int i = 0; i < _fields.size(); i++) {
        String strOrderByClause = _fields.elementAt(i);
        Vector<String> vecOrdersAux = getOrdeByIntoFields(strOrderByClause);
        if (vecOrdersAux != null && vecOrdersAux.size() > 0) {
          for (int j = 0; j < vecOrdersAux.size(); j++) {
            addOrderByField(getRealOrderByColumn(vecOrdersAux.elementAt(j)));
            addOrderByFieldSimple(vecOrdersAux.elementAt(j));
          }
        } else {
          addOrderByField(getRealOrderByColumn(strOrderByClause));
          addOrderByFieldSimple(strOrderByClause);
        }
      }
      if (_params != null)
        for (int i = 0; i < _params.size(); i++)
          addOrderByParameter(_params.elementAt(i), _params.elementAt(i));
    }
  }

  /**
   * Sets the filter clause.
   * 
   * @param _fields
   *          Vector with the fields for the filter clause.
   * @param _params
   *          Vector with the paramters.
   */
  void setFilter(Vector<String> _fields, Vector<String> _params) {
    this.filter = new Vector<QueryFieldStructure>();
    this.paramFilter = new Vector<QueryParameterStructure>();
    if (_fields == null || _fields.size() == 0) {
      if (this.internalFilter != null) {
        for (int i = 0; i < this.internalFilter.size(); i++) {
          QueryFieldStructure aux = this.internalFilter.elementAt(i);
          addFilterField(aux.getField(), aux.getType());
        }
        if (this.paramInternalFilter != null) {
          for (int i = 0; i < this.paramInternalFilter.size(); i++) {
            QueryParameterStructure aux = this.paramInternalFilter.elementAt(i);
            addFilterParameter(aux.getName(), aux.getField(), aux.getType());
          }
        }
      }
    } else {
      for (int i = 0; i < _fields.size(); i++)
        addFilterField(_fields.elementAt(i), "FILTER");
      if (_params != null)
        for (int i = 0; i < _params.size(); i++)
          addFilterParameter(_params.elementAt(i), _params.elementAt(i), "FILTER");
    }
  }

  /**
   * Gets the sql generated adding the filters
   * 
   * @param _FilterFields
   *          Vector with specific filter fields.
   * @param _FilterParams
   *          Vector with parameters for the specific filter fields.
   * @param _OrderFields
   *          Vector with specific order by fields. It can contain tablename.field or SQL clause
   * @param _OrderParams
   *          Vector with parameters for the specific order by fields.
   * @param selectFields
   *          String with the fields for the select clause.
   * @param _OrderSimple
   *          Vector with specific order by fields. It always contains tablename.field, never SQL
   *          clause
   * @param startPosition
   *          int with the first row to be shown
   * @param rangeLength
   *          int with the number of rows to be shown
   * @return String with the generated sql.
   */

  String getSQL(Vector<String> _FilterFields, Vector<String> _FilterParams,
      Vector<String> _OrderFields, Vector<String> _OrderParams, String selectFields,
      Vector<String> _OrderSimple, int startPosition, int rangeLength, boolean sorted,
      boolean onlyId) {
    StringBuffer text = new StringBuffer();
    boolean hasWhere = false;
    Vector<QueryFieldStructure> aux = null;
    if (selectFields == null || selectFields.equals("")) {
      aux = getSelectFields();
      if (aux != null) {
        text.append("SELECT ");
        for (int i = 0; i < aux.size(); i++) {
          QueryFieldStructure auxStructure = aux.elementAt(i);
          if (i > 0)
            text.append(", ");
          text.append(auxStructure.toString(true)).append(" ");
        }
        text.append(" \n");
      }
    } else {
      text.append("SELECT ").append(selectFields).append("\n");
    }

    aux = getFromFields();
    if (aux != null) {
      StringBuffer txtAux = new StringBuffer();
      text.append("FROM ");
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!txtAux.toString().equals(""))
          txtAux.append("left join ");
        txtAux.append(auxStructure.toString()).append(" \n");
      }
      text.append(txtAux.toString());
    }

    boolean hasRange = !(startPosition == 0 && rangeLength == 0);
    boolean hasRangeLimit = !(rangeLength == 0);

    StringBuffer txtAuxWhere = new StringBuffer();
    hasWhere = true;

    String keyColumns = getKeyColumns(getPool().getRDBMS().equalsIgnoreCase("ORACLE") ? ""
        : getTableName());
    String keyColumnsTable = getKeyColumns(getTableName());
    txtAuxWhere.append("(").append(keyColumnsTable).append(") IN (SELECT ").append(keyColumns)
        .append("\n");

    if (hasRange && getPool().getRDBMS().equalsIgnoreCase("ORACLE")) { // range
      // wrapper
      txtAuxWhere.append(" FROM (SELECT ROWNUM AS rn1, A.* FROM (\n");
      txtAuxWhere.append(" SELECT ").append(keyColumnsTable).append("\n");
    }
    txtAuxWhere.append(" FROM ").append(getTableName()).append("\n");

    Vector<QueryFieldStructure> auxFrom = getFromFields();
    Vector<QueryParameterStructure> auxParam = getFromParameters();

    if ((_OrderSimple.size() == 0) && (_OrderFields.size() == 0)) {// Order
      // by
      // defined
      // as
      // internal,
      // not as
      // param
      setOrderBy(null, null);
      _OrderSimple = getOrderBySimpleFieldsString();
    }
    _OrderSimple = cleanVector(_OrderSimple);

    if (_OrderSimple != null) {
      // where clause for order by fields
      for (int i = 0; i < _OrderSimple.size(); i++) {
        String auxStructure = _OrderSimple.elementAt(i);
        String orderField = auxStructure;
        if (auxStructure.indexOf(" ") != -1)
          orderField = auxStructure.substring(0, auxStructure.indexOf(" ")); // tablename.fieldname
        if (auxFrom != null) {
          for (int j = 0; j < auxFrom.size(); j++) {
            QueryFieldStructure auxStructureFrom = auxFrom.elementAt(j);
            if ((!auxStructureFrom.getRealName().equals(getTableName()))
                && (auxStructureFrom.getRealName().equals(orderField) || (keyColumnsTable
                    .indexOf(auxStructureFrom.getRealName()) != -1))) {
              txtAuxWhere.append("left join ");
              txtAuxWhere.append(auxStructureFrom.toString()).append(" \n");
            }
          }
        }
        if (auxParam != null) {
          for (int j = 0; j < auxParam.size(); j++) {
            QueryParameterStructure auxParamFrom = auxParam.elementAt(j);
            if ((!auxParamFrom.getRealName().equals(getTableName()))
                && (auxParamFrom.getRealName().equals(orderField) || (keyColumnsTable
                    .indexOf(auxParamFrom.getRealName()) != -1))) {
              addSubQueryParameter(auxParamFrom.getName(), auxParamFrom.getField(), "JOIN2"
                  + auxParamFrom.getName());
            }
          }
        }
      }
    }
    // Standard where
    aux = getWhereFields();
    boolean hasStandardWhere = false;
    if (aux != null) {
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!hasStandardWhere)
          txtAuxWhere.append(" WHERE ");
        else
          txtAuxWhere.append(" AND ");
        hasStandardWhere = true;
        txtAuxWhere.append(auxStructure.toString()).append(" \n");
      }
    }

    // filters where
    setFilter(_FilterFields, _FilterParams);
    aux = getFilterFields();
    if (aux != null) {
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!hasStandardWhere)
          txtAuxWhere.append(" WHERE ");
        else
          txtAuxWhere.append(" AND ");
        hasStandardWhere = true;
        txtAuxWhere.append(auxStructure.toString()).append(" \n");
      }
    }

    // Order by

    setOrderBy(_OrderSimple, _OrderParams);
    aux = getOrderByFields();
    boolean hasOrder = false;
    StringBuffer txtAuxOrderBy = new StringBuffer();
    if (sorted) {
      if (aux != null) {
        for (int i = 0; i < aux.size(); i++) {
          QueryFieldStructure auxStructure = aux.elementAt(i);
          if (!hasOrder)
            txtAuxOrderBy.append("ORDER BY ");
          else
            txtAuxOrderBy.append(", ");
          hasOrder = true;
          txtAuxOrderBy.append(auxStructure.toString());
        }
        if (!hasOrder)
          txtAuxOrderBy.append("ORDER BY ");
        else
          txtAuxOrderBy.append(", ");
        hasOrder = true;
        txtAuxOrderBy.append(getTableName()).append(".").append(getKeyColumn());
      }
    }
    if (hasWhere) {
      // for onlyId include the inner order by always, not only when
      // having and limit part
      if (hasRange || onlyId)
        txtAuxWhere.append(txtAuxOrderBy.toString()); // Internal order
      // by only for
      // ranges, or
      // onlyId case
      if ((!hasRange) || (getPool().getRDBMS().equalsIgnoreCase("ORACLE")))
        txtAuxWhere.append(")\n");
      if (hasRange) {
        // wrap end SQL
        // calc positions
        String rangeStart = Integer.toString(startPosition + 1);
        String rangeEnd = Integer.toString(startPosition + rangeLength);
        if (getPool().getRDBMS().equalsIgnoreCase("ORACLE")) {
          txtAuxWhere.append("A");
          if (hasRangeLimit) {
            txtAuxWhere.append(" WHERE ROWNUM <= ?");
            addWrapperParameter("oraRangeEnd", "RANGE", "");
            try {
              setParameter("oraRangeEnd", rangeEnd);
            } catch (Exception e) {
              // This shouldn't happen as name parameter is not null nor empty
              log4j.error("Error setting parameters ", e);
            }
          }
          txtAuxWhere.append(")\n");
          txtAuxWhere.append("  WHERE rn1 ");
          if (hasRangeLimit) {
            txtAuxWhere.append("BETWEEN ? AND ?");
            addWrapperParameter("oraRangeBtw1", "RANGE", "");
            addWrapperParameter("oraRangeBtw2", "RANGE", "");

            try {
              setParameter("oraRangeBtw1", rangeStart);
              setParameter("oraRangeBtw2", rangeEnd);
            } catch (Exception e) {
              // This shouldn't happen as name parameter is not null nor empty
              log4j.error("Error setting parameters ", e);
            }
          } else {
            txtAuxWhere.append(">= ?");
          }
          txtAuxWhere.append(")\n");
        } else {
          if (hasRangeLimit)
            txtAuxWhere.append(" LIMIT " + Integer.toString(rangeLength));
          txtAuxWhere.append(" OFFSET " + Integer.toString(startPosition) + ")\n");
        }
      }

      /*
       * When only interested in ordered sequence of id values we need only the inner subselect of
       * the previously assembled query. This block massages the inner select into an complete
       * executable sql statement and uses this instead of the bigger one.
       */
      if (onlyId) {
        if (getPool().getRDBMS().equalsIgnoreCase("ORACLE")) {
          txtAuxWhere.deleteCharAt(txtAuxWhere.length() - 1);
          txtAuxWhere.deleteCharAt(txtAuxWhere.length() - 1);
          int pos = txtAuxWhere.indexOf("FROM");
          txtAuxWhere.delete(0, pos);
          // replace the previously built sql-text with our version
          // completely

          text = txtAuxWhere;

          if (hasRange) {
            // don't add alias as it is already done in the subquery
            text.insert(0, "SELECT " + keyColumns + " \n");
          } else {
            // add alias
            text.insert(0, "SELECT " + keyColumnsTable + " \n");
          }
        } else {
          txtAuxWhere.deleteCharAt(txtAuxWhere.length() - 1);
          txtAuxWhere.deleteCharAt(txtAuxWhere.length() - 1);
          int pos = txtAuxWhere.indexOf("SELECT");
          txtAuxWhere.delete(0, pos);
          // replace the previously built sql-text with our version
          // completely
          text = txtAuxWhere;
        }
      } else {
        // old behavior for !onlyId case
        text.append("WHERE ").append(txtAuxWhere.toString());
      }
    }

    // don't append second (outer) order in onlyId case
    if (!onlyId) {
      text.append(txtAuxOrderBy.toString());
    }
    return text.toString();
  }

  /**
   * Gets the sql for the total queries.
   * 
   * @param page
   *          current backend page
   * @return String with the sql.
   */
  String getTotalSQL(int page) {
    StringBuffer text = new StringBuffer();
    Vector<QueryFieldStructure> aux = null;
    boolean hasWhere = false;

    text.append(" SELECT count(*) AS TOTAL FROM (\n");
    if (getPool().getRDBMS().equalsIgnoreCase("ORACLE")) {
      text.append("SELECT ROWNUM AS rn1, B.* FROM (\n");
    } else {
    }

    text.append("SELECT 1 ");

    aux = getFromFields();
    if (aux != null) {
      StringBuffer txtAux = new StringBuffer();
      text.append("FROM ");
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!txtAux.toString().equals(""))
          txtAux.append("left join ");
        txtAux.append(auxStructure.toString()).append(" \n");
      }
      text.append(txtAux.toString());
    }

    aux = getWhereFields();
    StringBuffer txtAuxWhere = new StringBuffer();
    if (aux != null) {
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        hasWhere = true;
        if (!txtAuxWhere.toString().equals(""))
          txtAuxWhere.append("AND ");
        txtAuxWhere.append(auxStructure.toString()).append(" \n");
      }
    }
    aux = getFilterFields();
    if (aux != null) {
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        hasWhere = true;
        if (!txtAuxWhere.toString().equals(""))
          txtAuxWhere.append("AND ");
        txtAuxWhere.append(auxStructure.toString()).append(" \n");
      }
    }
    if (hasWhere)
      text.append("WHERE ").append(txtAuxWhere.toString());

    if (getPool().getRDBMS().equalsIgnoreCase("ORACLE")) {
      text.append(" ) B WHERE ROWNUM <= " + ((page + 1) * maxRowsPerGridPage) + "\n");
      text.append(" ) A WHERE RN1 BETWEEN " + ((page * maxRowsPerGridPage) + 1) + " AND "
          + ((page + 1) * maxRowsPerGridPage) + "\n");
    } else {
      text.append(" LIMIT " + maxRowsPerGridPage + " OFFSET " + (page * maxRowsPerGridPage) + "\n");
      text.append(" ) B");
    }

    return text.toString();
  }

  /**
   * This function retrieves the stored backend page number for the given key from the session,
   * adjust it if needed based on the movePage request parameter, stored the new value into the
   * session and return it to the caller.
   * 
   * @param vars
   * @param currPageKey
   * @throws ServletException
   */
  public static int calcAndGetBackendPage(VariablesSecureApp vars, String currPageKey)
      throws ServletException {

    String movePage = vars.getStringParameter("movePage", "");
    log4j.debug("movePage action: " + movePage);

    // get current page
    String strPage = vars.getSessionValue(currPageKey, "0");
    int page = Integer.valueOf(strPage);

    // reset page on filter change
    if (vars.getStringParameter("newFilter", "0").equals("1")) {
      page = 0;
      vars.setSessionValue(currPageKey, String.valueOf(page));
    }

    // need to change page?
    if (movePage.length() > 0) {
      if (movePage.equals("FIRSTPAGE")) {
        page = 0;
      } else if (movePage.equals("PREVIOUSPAGE")) {
        page = Math.max(--page, 0);
        log4j.debug("page-- newPage=" + page);
      } else if (movePage.equals("NEXTPAGE")) {
        page++;
        log4j.debug("page++ newPage=" + page);
      } else {
        throw new ServletException("Unknown action for movePage: " + movePage);
      }
      vars.setSessionValue(currPageKey, String.valueOf(page));
    }

    return page;
  }

}
