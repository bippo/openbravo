/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package com.openbravo.db;

import org.apache.ddlutils.platform.ExcludeFilter;

@Deprecated
/**
 * @Deprecated This class has been replaced by the new excludeFilter functionality. If you need to extend the list, you can check this document: http://wiki.openbravo.com/wiki/ERP/2.50/Developers_Guide/How_To_Exclude_Database_Physical_Objects_From_Model
 */
public class OpenbravoExcludeFilter extends ExcludeFilter {

  @Override
  public String[] getExcludedTables() {
    return new String[] { "PLAN_TABLE", "AD_SYSTEM_MODEL", "C_TEMP_SELECTION", "C_TEMP_SELECTION2", "AD_CONTEXT_INFO" };
  }

  public String[] getExcludedViews() {
    return new String[] { "DUAL", "USER_CONS_COLUMNS", "USER_TABLES", "USER_CONSTRAINTS",
        "USER_INDEXES", "USER_IND_COLUMNS", "USER_OBJECTS", "USER_TAB_COLUMNS", "USER_TRIGGERS",
        "V$VERSION", "AD_INTEGER" };
  }

  public String[] getExcludedFunctions() {
    return new String[] { "EXIST_LANGUAGE", "INSERT_PG_LANGUAGE", "CREATE_LANGUAGE", "DATEFORMAT",
        "TO_NUMBER", "TO_DATE", "TO_TIMESTAMP", "TO_CHAR", "ROUND", "RPAD", "SUBSTR",
        "TO_INTERVAL", "ADD_MONTHS", "ADD_DAYS", "TYPE_OID", "SUBSTRACT_DAYS", "TRUNC", "INSTR",
        "LAST_DAY", "IS_TRIGGER_ENABLED", "DROP_VIEW", "MONTHS_BETWEEN",
        "AD_SCRIPT_DISABLE_TRIGGERS", "AD_SCRIPT_DISABLE_CONSTRAINTS", "AD_SCRIPT_ENABLE_TRIGGERS",
        "AD_SCRIPT_ENABLE_CONSTRAINTS", "AD_SCRIPT_DROP_RECREATE_INDEXES",
        "AD_SCRIPT_DROP_RECREATE_INDEX", "AD_SCRIPT_EXECUTE", "DBA_GETATTNUMPOS",
        "DBA_GETSTANDARD_SEARCH_TEXT", "DUMP", "NEGATION", "EQUAL", "GREATEREQUAL", "LOWEREQUAL",
        "LOWEREQUALNUMERIC", "LOWEREQUALTIMESTAMP", "DBA_RECOMPILE", "DBA_AFTERIMPORT", "NOW",
        "UPDATE_DATEFORMAT", "GET_UUID", "HEX_TO_INT", "UUID_GENERATE_V4", "AD_ORG_CHK_DOCUMENTS",
        "AD_ORG_CHK_DOC_PAYMENTS", "C_CREATE_TEMPORARY_TABLES", "AD_DB_MODIFIED", "AD_GET_DOC_LE_BU",
        "AD_GET_RDBMS", "AD_CREATE_AUDIT_TRIGGERS"};
  }

}
