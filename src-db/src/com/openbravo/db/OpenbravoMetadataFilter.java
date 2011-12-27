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
public class OpenbravoMetadataFilter extends AbstractDatabaseFilter {

  /** Creates a new instance of OpenbravoMetadataFilter */
  public OpenbravoMetadataFilter() {
  }

  public void init(Database database) {

    addTable("AD_CLIENT", "AD_CLIENT_ID = '0'");
    addTable("AD_CLIENTINFO", "AD_CLIENT_ID = '0'");
    addTable("AD_ORG", "AD_CLIENT_ID = '0'");
    addTable("AD_ORGTYPE", "AD_CLIENT_ID = '0'");
    addTable("AD_ORGINFO", "AD_CLIENT_ID = '0'");
    addTable("AD_ROLE", "AD_CLIENT_ID = '0'");
    addTable("AD_MODEL_OBJECT_PARA", "AD_CLIENT_ID = '0'");
    addTable("AD_USER", "AD_USER_ID IN ('0','100')");

    // addTable("AD_WORKBENCH", "AD_CLIENT_ID = '0'");

    addTable("AD_ALERTRULE", "AD_CLIENT_ID = '0'");
    addTable("AD_ALERTRULE_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_AUXILIARINPUT", "AD_CLIENT_ID = '0'");
    addTable("AD_CALLOUT", "AD_CLIENT_ID = '0'");
    addTable("AD_COLUMN", "AD_CLIENT_ID = '0'");
    addTable("AD_DATATYPE", "AD_CLIENT_ID = '0'");
    addTable("AD_DIMENSION", "AD_CLIENT_ID = '0'");
    addTable("AD_ELEMENT", "AD_CLIENT_ID = '0'");
    addTable("AD_ELEMENT_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_FIELD", "AD_CLIENT_ID = '0'");
    addTable("AD_FIELD_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_FIELDGROUP", "AD_CLIENT_ID = '0'");
    addTable("AD_FIELDGROUP_TRL", "AD_CLIENT_ID = '0' ");
    addTable("AD_FORM", "AD_CLIENT_ID = '0'");
    addTable("AD_FORM_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_IMAGE", "AD_CLIENT_ID = '0'");
    addTable("AD_LANGUAGE", "AD_CLIENT_ID = '0'");
    addTable("AD_MENU", "AD_CLIENT_ID = '0'");
    addTable("AD_MENU_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_MESSAGE", "AD_CLIENT_ID = '0'");
    addTable("AD_MESSAGE_TRL", "AD_CLIENT_ID = '0' ");
    addTable("AD_MODEL_OBJECT", "AD_CLIENT_ID = '0'");
    addTable("AD_MODEL_OBJECT_MAPPING", "AD_CLIENT_ID = '0'");
    addTable("AD_MODULE", "AD_MODULE_ID = '0'");
    addTable("AD_MONTH", "AD_CLIENT_ID = '0'");
    addTable("AD_PREFERENCE", "AD_CLIENT_ID = '0'");
    addTable("AD_PROCESS", "AD_CLIENT_ID = '0'");
    addTable("AD_PROCESS_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_PROCESS_PARA", "AD_CLIENT_ID = '0'");
    addTable("AD_PROCESS_PARA_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_REFERENCE", "AD_CLIENT_ID = '0'");
    addTable("AD_REFERENCE_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_REF_LIST", "AD_CLIENT_ID = '0'");
    addTable("AD_REF_LIST_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_REF_SEARCH", "AD_CLIENT_ID = '0'");
    addTable("AD_REF_SEARCH_COLUMN", "AD_CLIENT_ID = '0'");
    addTable("AD_REF_TABLE", "AD_CLIENT_ID = '0'");
    // addTable("AD_SEQUENCE", "AD_CLIENT_ID = '0'");
    addTable("AD_TAB", "AD_CLIENT_ID = '0'");
    addTable("AD_TAB_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_TABLE", "AD_CLIENT_ID = '0'");
    addTable("AD_TEXTINTERFACES", "AD_CLIENT_ID = '0'");
    addTable("AD_TEXTINTERFACES_TRL", "AD_CLIENT_ID = '0'");
    addTable("AD_TREE", "AD_CLIENT_ID = '0'");
    addTable("AD_TREENODE", "AD_CLIENT_ID = '0'");
    addTable("AD_VAL_RULE", "AD_CLIENT_ID = '0'");
    addTable("AD_WINDOW", "AD_CLIENT_ID = '0'");
    addTable("AD_WINDOW_TRL", "AD_CLIENT_ID = '0'");
    /*
     * addTable("AD_ACCOUNTINGRPT_ELEMENT", "AD_CLIENT_ID = '0'");// addTable("C_COUNTRY",
     * "AD_CLIENT_ID = '0'");// addTable("C_COUNTRY_TRL",
     * "AD_CLIENT_ID = '0' AND AD_LANGUAGE = 'es_ES'");// addTable("C_CURRENCY",
     * "AD_CLIENT_ID = '0'");// addTable("C_CURRENCY_TRL",
     * "AD_CLIENT_ID = '0' AND AD_LANGUAGE = 'es_ES'");//
     */
    /*
     * addTable("C_DOCTYPE", "AD_CLIENT_ID = '0'"); addTable("C_DOCTYPE_TRL",
     * "AD_CLIENT_ID = '0' AND AD_LANGUAGE = 'es_ES'");
     */
    /*
     * addTable("C_REGION", "AD_CLIENT_ID = '0'");// addTable("C_UOM", "AD_CLIENT_ID = '0'");//
     * addTable("C_UOM_TRL", "AD_CLIENT_ID = '0' AND AD_LANGUAGE = 'es_ES'");//
     * addTable("GL_CATEGORY", "AD_CLIENT_ID = '0'");//
     */
    addTable("M_ATTRIBUTESET", "AD_CLIENT_ID = '0'");
    addTable("M_ATTRIBUTESETINSTANCE", "AD_CLIENT_ID = '0'");
    addTable("AD_MODULE_DEPENDENCY", "AD_CLIENT_ID = '0'");
    addTable("AD_MODULE_DBPREFIX", "AD_CLIENT_ID = '0'");
    addTable("AD_DATASET", "AD_CLIENT_ID = '0'");
    addTable("AD_DATASET_TABLE", "AD_CLIENT_ID = '0'");
    addTable("AD_DATASET_COLUMN", "AD_CLIENT_ID = '0'");
    addTable("AD_PACKAGE", "AD_CLIENT_ID = '0'");
    addTable("AD_MODULE", "AD_CLIENT_ID = '0'");
  }
}
