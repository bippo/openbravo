/*
 ************************************************************************************
 * Copyright (C) 2'0''0'1-2'0''0'8 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.'0'
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.'0'
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package com.openbravo.db;

import org.apache.ddlutils.io.AbstractDatabaseFilter;
import org.apache.ddlutils.model.Database;

/**
 * 
 * @author adrian
 */
public final class OpenbravoDataFilter extends AbstractDatabaseFilter {

  /** Creates a new instance of OpenbravoDataFilter */
  public OpenbravoDataFilter() {
  }

  public void init(Database database) {

    addAllTables(database);

    removeTable("AD_DEPENDENCIES");
    removeTable("AD_ID_TRANSLATION");
    removeTable("AD_PINSTANCE");
    removeTable("AD_PINSTANCE_PARA");
    removeTable("AD_REPLICATION");
    removeTable("AD_REPLICATION_LOG");
    removeTable("AD_REPLICATION_RUN");
    removeTable("AD_REPLICATIONSTRATEGY");
    removeTable("AD_REPLICATIONTABLE");
    removeTable("AD_SCRIPT_SQL");
    removeTable("AD_SEQUENCE_AUDIT");
    removeTable("AD_SEQUENCE_NO");
    removeTable("AD_SESSION");
    removeTable("AD_SYSTEM");
    removeTable("AD_SYSTEM_INFO");
    removeTable("TIME_DIMENSION");
    removeTable("AD_DEVELOPER");

    addTable("AD_CLIENT", "AD_CLIENT_ID <> '0'");
    addTable("AD_CLIENTINFO", "AD_CLIENT_ID <> '0'");
    addTable("AD_ORG", "AD_CLIENT_ID <> '0'");
    addTable("AD_ORGTYPE", "AD_CLIENT_ID <> '0'");
    addTable("AD_ORGINFO", "AD_CLIENT_ID <> '0'");
    addTable("AD_ROLE", "AD_CLIENT_ID <> '0'");
    addTable("AD_ROLE_ORGACCESS", "AD_CLIENT_ID <> '0'");
    addTable("AD_USER", "AD_USER_ID NOT IN ('0','100')");
    addTable("AD_USER_ROLES", "AD_CLIENT_ID <> '0'");

    addTable("AD_ALERTRULE", "AD_CLIENT_ID <> '0'");
    addTable("AD_ALERTRULE_TRL", "AD_CLIENT_ID <> '0' OR AD_LANGUAGE <> 'es_ES'");
    removeTable("AD_AUXILIARINPUT");
    removeTable("AD_CALLOUT");
    removeTable("AD_COLUMN");
    removeTable("AD_DATATYPE");
    removeTable("AD_DIMENSION");
    removeTable("AD_ELEMENT");
    removeTable("AD_ELEMENT_TRL");
    removeTable("AD_FIELD");
    removeTable("AD_FIELD_TRL");
    removeTable("AD_FIELDGROUP");
    removeTable("AD_FIELDGROUP_TRL");
    removeTable("AD_FORM");
    removeTable("AD_FORM_TRL");
    addTable("AD_IMAGE", "AD_CLIENT_ID <> '0'");
    removeTable("AD_LANGUAGE");
    removeTable("AD_MENU");
    removeTable("AD_MENU_TRL");
    removeTable("AD_MESSAGE");
    removeTable("AD_MESSAGE_TRL");
    removeTable("AD_MODEL_OBJECT");
    removeTable("AD_MODEL_OBJECT_MAPPING");
    removeTable("AD_MONTH");
    addTable("AD_PREFERENCE", "AD_CLIENT_ID <> '0'");
    removeTable("AD_PROCESS");
    removeTable("AD_PROCESS_TRL");
    removeTable("AD_PROCESS_PARA");
    removeTable("AD_PROCESS_PARA_TRL");
    removeTable("AD_REFERENCE");
    removeTable("AD_REFERENCE_TRL");
    removeTable("AD_REF_LIST");
    removeTable("AD_REF_LIST_TRL");
    removeTable("AD_REF_SEARCH");
    removeTable("AD_REF_SEARCH_COLUMN");
    removeTable("AD_REF_TABLE");
    addTable("AD_SEQUENCE", "AD_CLIENT_ID <> '0'");
    removeTable("AD_TAB");
    removeTable("AD_TAB_TRL");
    removeTable("AD_TABLE");
    removeTable("AD_TEXTINTERFACES");
    removeTable("AD_TEXTINTERFACES_TRL");
    addTable("AD_TREE", "AD_CLIENT_ID <> '0'");
    addTable("AD_TREENODE", "AD_CLIENT_ID <> '0'");
    removeTable("AD_VAL_RULE");
    removeTable("AD_WINDOW");
    removeTable("AD_WINDOW_TRL");

    addTable("AD_ACCOUNTINGRPT_ELEMENT", "AD_CLIENT_ID <> '0'");//
    addTable("C_COUNTRY", "AD_CLIENT_ID <> '0'");//
    addTable("C_COUNTRY_TRL", "AD_CLIENT_ID <> '0'");//
    addTable("C_CURRENCY", "AD_CLIENT_ID <> '0'");//
    addTable("C_CURRENCY_TRL", "AD_CLIENT_ID <> '0'");//
    addTable("C_DOCTYPE", "AD_CLIENT_ID <> '0'");
    addTable("C_DOCTYPE_TRL", "AD_CLIENT_ID <> '0'");
    addTable("C_REGION", "AD_CLIENT_ID <> '0'");//
    addTable("C_UOM", "AD_CLIENT_ID <> '0'");//
    addTable("C_UOM_TRL", "AD_CLIENT_ID <> '0'");//
    addTable("GL_CATEGORY", "AD_CLIENT_ID <> '0'");//
  }
}
