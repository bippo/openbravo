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
public class OpenbravoMasterdataFilter extends AbstractDatabaseFilter {

  /** Creates a new instance of OpenbravoMetadataFilter */
  public OpenbravoMasterdataFilter() {
  }

  public void init(Database database) {

    addTable("AD_ACCOUNTINGRPT_ELEMENT", "AD_CLIENT_ID = '0'");//
    addTable("C_COUNTRY", "AD_CLIENT_ID = '0'");//
    addTable("C_COUNTRY_TRL", "AD_CLIENT_ID = '0'");//
    addTable("C_CURRENCY", "AD_CLIENT_ID = '0'");//
    addTable("C_CURRENCY_TRL", "AD_CLIENT_ID = '0'");//
    addTable("C_DOCTYPE", "AD_CLIENT_ID = '0'");
    addTable("C_DOCTYPE_TRL", "AD_CLIENT_ID = '0'");
    addTable("C_REGION", "AD_CLIENT_ID = '0'");//
    addTable("C_UOM", "AD_CLIENT_ID = '0'");//
    addTable("C_UOM_TRL", "AD_CLIENT_ID = '0'");//
    addTable("GL_CATEGORY", "AD_CLIENT_ID = '0'");//
  }
}
