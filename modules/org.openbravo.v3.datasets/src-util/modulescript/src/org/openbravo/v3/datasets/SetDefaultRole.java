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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.v3.datasets;

import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

/**
 * Set the default role to "Finance" for the "Openbravo" user, if the role exists. This is useful
 * for those packaging deliverables targeted for evaluation, such as the Appliances or the Ubuntu
 * package.
 * 
 * @author jpabloae
 */
public class SetDefaultRole extends ModuleScript {

  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      PreparedStatement ps = cp
          .getPreparedStatement("update ad_user set default_ad_role_id='42D0EEB1C66F497A90DD526DC597E6F0' where ad_user_id='100' and default_ad_role_id is NULL and exists (select 1 from ad_role where ad_role_id='42D0EEB1C66F497A90DD526DC597E6F0')");
      ps.executeUpdate();
    } catch (Exception e) {
      handleError(e);
    }
  }
}
