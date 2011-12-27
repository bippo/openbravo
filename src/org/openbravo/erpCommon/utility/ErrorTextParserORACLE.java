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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;

//examples for the types of postgres messages to be parsed by this class

// example for unique constraint
// ORA-00001: unique constraint (TADPI.AD_USER_UN_USERNAME) violated

// example for raise_application_error(-20000), from inside a trigger
// ORA-20000: @HCMC_OneDefaultRecord@
// ORA-06512: at "TADPI.HCMC_DEFAULT_TRG", line 35
// ORA-04088: error during execution of trigger 'TADPI.HCMC_DEFAULT_TRG'

// example for oracle raised error from inside/about a trigger
// ORA-04091: table TADPI.AD_MODULE is mutating, trigger/function may not see it
// ORA-06512: at "TADPI.AD_MODULE_DEPENDENCY_MOD_TRG", line 30
// ORA-04088: error during execution of trigger 'TADPI.AD_MODULE_DEPENDENCY_MOD_TRG'

/**
 * @author Fernando Iriazabal
 * 
 *         Instance of the Abstract class, ErrorTextParser, that implements the error parsing for
 *         ORACLE RDBMS.
 */
class ErrorTextParserORACLE extends ErrorTextParser {
  private static final Logger log4j = Logger.getLogger(ErrorTextParserORACLE.class);

  String[] getColumnNamesForConstraint(String constraintName) {
    try {
      String query = "column_name as columnname from user_cons_columns where upper(constraint_name) = upper(?)";
      ErrorTextParserData[] cols = ErrorTextParserData.selectColumnNamesForConstraint(
          getConnection(), query, constraintName);
      String[] res = new String[cols.length];
      for (int i = 0; i < cols.length; i++) {
        res[i] = cols[i].columnname;
      }

      // assure same column order across (oracle,postgresql) by sorting results in java
      Arrays.sort(res);

      return res;
    } catch (ServletException se) {
      log4j.error("Error reading list of columns for constraint: " + constraintName, se);
      return new String[0];
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.erpCommon.utility.ErrorTextParser#parse()
   */
  public OBError parse() throws Exception {
    if (getMessage().equals(""))
      return null;
    else if (getConnection() == null)
      return null;
    OBError myError = null;
    OBError myCodeError = null;
    int errorCode = 0;
    String errorCodeText = "";
    String myMessage = getMessage();
    if (log4j.isDebugEnabled())
      log4j.debug("Message: " + myMessage);

    // BEGIN Checking if it's a DB error
    int pos = myMessage.indexOf("ORA-");
    if (pos != -1) {
      // BEGIN Getting error code
      try {
        errorCode = Integer.valueOf(myMessage.substring(pos + 4, myMessage.indexOf(":", pos + 4)))
            .intValue();
        errorCodeText = myMessage.substring(pos, myMessage.indexOf(":", pos));
      } catch (Exception ignored) {
        errorCode = 0;
        errorCodeText = "";
      }
      if (log4j.isDebugEnabled())
        log4j.debug("Error code: " + Integer.toString(errorCode));
      if (errorCode != 0) {
        FieldProvider fldMessage = Utility.locateMessage(getConnection(),
            Integer.toString(errorCode), getLanguage());
        if (fldMessage != null) {
          myCodeError = new OBError();
          myCodeError.setType((fldMessage.getField("msgtype").equals("E") ? "Error" : "Warning"));
          myCodeError.setMessage(fldMessage.getField("msgtext"));
          // return myError;
        } else if (errorCode >= 20000 && errorCode <= 30000) {
          myError = new OBError();
          myError.setType("Error");

          String toTranslate = myMessage.replace(errorCodeText + ": ", "");
          // assumption incoming error message useful part is completely contained in the first line
          pos = toTranslate.indexOf("\n");
          if (pos != -1) {
            toTranslate = toTranslate.substring(0, pos);
          }

          String messageAux = Utility.parseTranslation(getConnection(), getVars(), getLanguage(),
              toTranslate);
          if (log4j.isDebugEnabled())
            log4j.debug("Message parsed: " + messageAux);
          myError.setMessage(messageAux);
          return myError;
        }
      }
      // END Getting error code
      // BEGIN Getting DB object name
      pos = myMessage.indexOf("(", pos + 4);
      if (pos != -1) {
        int finalPos = myMessage.indexOf(")", pos + 1);
        if (finalPos == -1)
          finalPos = myMessage.length();
        String objectName = myMessage.substring(pos + 1, finalPos);
        if (log4j.isDebugEnabled())
          log4j.debug("Object name: " + objectName);
        pos = objectName.indexOf(".");
        if (pos != -1)
          objectName = objectName.substring(pos + 1);
        if (log4j.isDebugEnabled())
          log4j.debug("Object real name: " + objectName);
        ErrorTextParserData[] constraintData = ErrorTextParserData.select(getConnection(),
            objectName);
        // BEGIN Specific parse for CONSTRAINT DB objects
        if (constraintData != null && constraintData.length > 0) {

          // handle common cases (shared across different dbms)
          OBError cError = handleConstraintViolation(constraintData);
          if (cError != null) {
            return cError;
          }

        } else if (ErrorTextParserORACLEData.isTrigger(getConnection(), objectName)) {
          FieldProvider fldMessage = Utility.locateMessage(getConnection(),
              myMessage.substring(0, myMessage.indexOf("ORA-")), getLanguage());
          if (fldMessage != null) {
            myError = new OBError();
            myError.setType((fldMessage.getField("msgtype").equals("E") ? "Error" : "Warning"));
            myError.setMessage(fldMessage.getField("msgtext"));
          }
        } else if (errorCode == 1400 || errorCode == 1407) {
          pos = objectName.indexOf(".");
          String tableName = objectName.substring(0, pos);
          if (tableName.startsWith("\""))
            tableName = tableName.substring(1, tableName.length() - 1);
          String columnName = objectName.substring(pos + 1);
          if (columnName.startsWith("\""))
            columnName = columnName.substring(1, columnName.length() - 1);
          myError = new OBError();
          myError.setType("Error");

          FieldProvider msgText = Utility.locateMessage(getConnection(), "NotNullError",
              getLanguage());
          if (msgText != null) {
            String msgTemplate = msgText.getField("msgText");
            tableName = getTableName(tableName);
            columnName = getColumnName(columnName);
            Map<String, String> replaceMap = new HashMap<String, String>();
            replaceMap.put("TABLE_NAME", tableName);
            replaceMap.put("COLUMN_NAME", columnName);
            String res = Utility.parseTranslation(getConnection(), getVars(), replaceMap,
                getLanguage(), msgTemplate);
            myError.setMessage(res);
            return myError;
          }
        }
        // END Specific parse for CONSTRAINT DB objects
      }
      // END Getting DB object name
    }
    // END Checking if it's a DB error
    if (myCodeError != null)
      return myCodeError;
    else
      return myError;
  }
}
