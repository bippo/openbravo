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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.modulescript;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;

public class AdminFlagOnCreateDefaults extends ModuleScript {
  @Override
  // Updates new column values with deprecated ones
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      // CLIENT ADMIN
      // Client admin are all roles that have write access to the "Initial organization
      // setup" form and have the * organization assigned.
      AdminFlagOnCreateDefaultsData.updateClientAdmin(cp);

      // ORGANIZATION ADMIN
      // Organization admin flag is set on the AD_Role_OrgAccess table. It is set to true
      // to all organizations assigned to a role that doesn't have the * organization
      // assigned and that has write access to the "Initial organization setup" form.
      AdminFlagOnCreateDefaultsData.updateOrgAdmin(cp);

      // ROLE ADMIN
      // Role admin flag is set on the AD_User_Roles table. It is set to true to all the
      // roles assigned to a user that has access at least to a role that has write access
      // to the "Role" window.
      AdminFlagOnCreateDefaultsData.updateRoleAdmin(cp);
    } catch (Exception e) {
      handleError(e);
    }
  }
}
