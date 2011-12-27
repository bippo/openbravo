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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;

/**
 * @author Fernando Iriazabal
 * 
 *         Abstract Class Handler for the Error management in the application.
 */
abstract class ErrorTextParser {
  private static final Logger log4j = Logger.getLogger(ErrorTextParser.class);
  private ConnectionProvider conn;
  private String language = "";
  private String message = "";
  private VariablesSecureApp vars;

  /**
   * Constructor.
   */
  public ErrorTextParser() {
  }

  /**
   * Constructor
   * 
   * @param _data
   *          Object with the database connection handler.
   */
  public void setConnection(ConnectionProvider _data) {
    this.conn = _data;
  }

  /**
   * Getter for the database connection handler.
   * 
   * @return Object with the database connection handler.
   */
  public ConnectionProvider getConnection() {
    return this.conn;
  }

  /**
   * Setter for the language.
   * 
   * @param _data
   *          String with the language.
   */
  public void setLanguage(String _data) {
    if (_data == null)
      _data = "";
    this.language = _data;
  }

  /**
   * Getter for the language.
   * 
   * @return String with the language.
   */
  public String getLanguage() {
    return ((this.language == null) ? "" : this.language);
  }

  /**
   * Setter for the message text.
   * 
   * @param _data
   *          String with the new message text.
   */
  public void setMessage(String _data) {
    if (_data == null)
      _data = "";
    this.message = _data;
  }

  /**
   * Getter for the message text.
   * 
   * @return String with the message text.
   */
  public String getMessage() {
    return ((this.message == null) ? "" : this.message);
  }

  /**
   * Setter for the session info handler.
   * 
   * @param _data
   *          Object with the session info handler.
   */
  public void setVars(VariablesSecureApp _data) {
    this.vars = _data;
  }

  /**
   * Getter for the session info handler.
   * 
   * @return Object with the session info handler.
   */
  public VariablesSecureApp getVars() {
    return this.vars;
  }

  /**
   * Abstract method to implement the specific error parsing for each database type.
   * 
   * @return Object with the error message parsed.
   * @throws Exception
   */
  public abstract OBError parse() throws Exception;

  /**
   * Abstract helper method to get the list of column names for the specified database constraint.
   * 
   * @param constraintName
   *          name of a database constraint
   * @return list of column names of the constraint
   */
  abstract String[] getColumnNamesForConstraint(String constraintName);

  /**
   * Helper method to get a (slightly better) human-readable name for a database table based on its
   * name. Method uses AD_TABLE.name for this purpose without doing a translation
   * 
   * @param tableName
   *          name of a database table
   * @return human-readable name
   */
  protected String getTableName(String tableName) {
    try {
      String pkColumnName = tableName + "_ID";
      return ErrorTextParserData.selectColumnName(conn, language, pkColumnName);
    } catch (ServletException e) {
      log4j.error(
          "Error while trying to name for table via ad_element for tablename: " + tableName, e);
    }
    return tableName;
  }

  /**
   * Helper method to get a (slightly better) human-readable name for a database column based on its
   * name. Method uses AD_ELEMENT.name and AD_ELEMENT_TRL.NAME for this purpose
   * 
   * @param tableName
   *          name of a database column
   * @return translated, human-readable name
   */
  protected String getColumnName(String columnName) {
    String res;
    try {
      res = ErrorTextParserData.selectColumnName(conn, language, columnName);
      return res;
    } catch (ServletException e) {
      log4j.error("Error while trying to get name for ad_element.columnname: " + columnName, e);
    }
    return columnName;
  }

  /**
   * Handle the common cases of constraint violation. Methods returns an OBError objects if it can
   * handle (parse/translate) the error, otherwise null is returned.
   * 
   * @param constraintData
   * @return OBError object with translated message, or null if couldn't be handled
   */
  protected OBError handleConstraintViolation(ErrorTextParserData[] constraintData) {
    FieldProvider fldMessage;
    OBError myError;

    // search for AD_MESSAGE.value with exact constraint name
    fldMessage = Utility.locateMessage(getConnection(), constraintData[0].constraintName,
        getLanguage());
    if (fldMessage != null) {
      myError = new OBError();
      myError.setType((fldMessage.getField("msgtype").equals("E") ? "Error" : "Warning"));
      myError.setMessage(fldMessage.getField("msgtext"));
      return myError;
    }

    // handle duplicate primary key violations
    if (constraintData[0].constraintType.equals("P")) {
      FieldProvider msgText = Utility.locateMessage(getConnection(), "DuplicatePrimaryKey",
          getLanguage());
      if (msgText != null) {
        String msgTemplate = msgText.getField("msgText");
        String tableName = getTableName(constraintData[0].tableName);
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("TABLE_NAME", tableName);
        String res = Utility.parseTranslation(getConnection(), getVars(), replaceMap,
            getLanguage(), msgTemplate);

        myError = new OBError();
        myError.setType("Error");
        myError.setMessage(res);
        return myError;
      }
    }

    // handle unique constraint violations
    if (constraintData[0].constraintType.equals("U")) {
      FieldProvider msgText = Utility.locateMessage(getConnection(), "UniqueConstraintViolation",
          getLanguage());
      if (msgText != null) {
        String msgTemplate = msgText.getField("msgText");
        String tableName = getTableName(constraintData[0].tableName);
        String[] columnList = getColumnNamesForConstraint(constraintData[0].constraintName);
        StringBuilder columns = new StringBuilder();
        for (String column : columnList) {
          if (columns.length() > 0) {
            columns.append(", ");
          }
          columns.append(getColumnName(column));
        }
        String columnName;
        if (columnList.length > 1) {
          columnName = '(' + columns.toString() + ')';
        } else {
          columnName = columns.toString();
        }
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("TABLE_NAME", tableName);
        replaceMap.put("COLUMN_NAMES", columnName);
        String res = Utility.parseTranslation(getConnection(), getVars(), replaceMap,
            getLanguage(), msgTemplate);

        myError = new OBError();
        myError.setType("Error");
        myError.setMessage(res);
        return myError;
      }
    }

    // handle foreign key violations
    if (constraintData[0].constraintType.equals("R")) {
      // Note: Text is always 'You cannot delete this record...' as we do not have enough
      // context information to distinguish insert/update or delete here
      FieldProvider msgText = Utility.locateMessage(getConnection(), "ForeignKeyViolation",
          getLanguage());
      if (msgText != null) {
        String msgTemplate = msgText.getField("msgText");
        Map<String, String> replaceMap = new HashMap<String, String>();
        String res = Utility.parseTranslation(getConnection(), getVars(), replaceMap,
            getLanguage(), msgTemplate);

        myError = new OBError();
        myError.setType("Error");
        myError.setMessage(res);
        return myError;
      }
    }

    // handle check constraint violations
    if (constraintData[0].constraintType.equalsIgnoreCase("C")
        && !constraintData[0].searchCondition.equals("")) {
      // BEGIN Search message by constraint search condition
      fldMessage = Utility.locateMessage(getConnection(), constraintData[0].searchCondition,
          getLanguage());
      if (fldMessage != null) {
        myError = new OBError();
        myError.setType((fldMessage.getField("msgtype").equals("E") ? "Error" : "Warning"));
        myError.setMessage(fldMessage.getField("msgtext"));
        return myError;
      } else if (!constraintData[0].searchCondition.trim().equals("")) {
        String searchCond = constraintData[0].searchCondition.trim().toUpperCase();
        if (searchCond.endsWith(" IS NOT NULL")) {
          String columnName = searchCond.substring(0, searchCond.lastIndexOf(" IS NOT NULL"))
              .trim();
          myError = new OBError();
          myError.setType("Error");

          FieldProvider msgText = Utility.locateMessage(getConnection(), "NotNullError",
              getLanguage());
          if (msgText != null) {
            String msgTemplate = msgText.getField("msgText");
            String tableName = getTableName(constraintData[0].tableName);
            columnName = getColumnName(columnName);
            Map<String, String> replaceMap = new HashMap<String, String>();
            replaceMap.put("TABLE_NAME", tableName);
            replaceMap.put("COLUMN_NAME", columnName);
            String res = Utility.parseTranslation(getConnection(), getVars(), replaceMap,
                getLanguage(), msgTemplate);
            myError.setMessage(res);
            return myError;
          }
        } else if (searchCond.endsWith(" IN ('Y','N')") || searchCond.endsWith(" IN ('Y', 'N')")
            || searchCond.endsWith(" IN ('N','Y')") || searchCond.endsWith(" IN ('N', 'Y')")) {
          String columnName = searchCond.substring(0, searchCond.lastIndexOf(" IN (")).trim();
          columnName = getColumnName(columnName);

          FieldProvider msgText = Utility.locateMessage(getConnection(), "NotYNError",
              getLanguage());
          if (msgText != null) {
            String msgTemplate = msgText.getField("msgText");
            String tableName = getTableName(constraintData[0].tableName);
            Map<String, String> replaceMap = new HashMap<String, String>();
            replaceMap.put("TABLE_NAME", tableName);
            replaceMap.put("COLUMN_NAME", columnName);
            String res = Utility.parseTranslation(getConnection(), getVars(), replaceMap,
                getLanguage(), msgTemplate);
            myError = new OBError();
            myError.setType("Error");
            myError.setMessage(res);
            return myError;
          }
        }
      }
      // it is a constraint violation but it is not handled so far (no explicit message, and no
      // auto-generated one use generic constraint violation message
      String msgText = Utility.messageBD(getConnection(), "UnspecifiedConstraintViolation",
          getLanguage());
      myError = new OBError();
      myError.setType("Error");
      myError.setMessage(msgText);
      return myError;
      // END Search message by constraint search condition
    }

    // fallback for unhandled cases
    return null;
  }
}
