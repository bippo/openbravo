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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.validation;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Table;

/**
 * Provides checking logic that enforces that certain tables (== Entities) in the system only
 * contain records (== Objects) with the correct client and organization. The
 * {@link Table#getAccessLevel() accessLevel} of the table is used for this.
 * <p/>
 * System tables may only contain objects with Client id '0' and organization id '0' (=*
 * organization)
 * <p/>
 * System/Client tables may contain objects from any client but only organizations with id '0'
 * <p/>
 * Organization may not contain objects with client '0' or organization '0' (client != '0' and org
 * != '0')
 * <p/>
 * Client/Organization may not contain objects with client '0', any organization is allowed
 * <p/>
 * All this allows all client/organizations.
 * 
 * @author mtaal
 */

public class AccessLevelChecker {

  public static final AccessLevelChecker ALL = new AccessLevelChecker();

  /**
   * SYSTEM access level, only allows client with id '0' and organization with id '0'.
   */
  public static final AccessLevelChecker SYSTEM = new AccessLevelChecker() {
    @Override
    public void checkAccessLevel(String entity, String clientId, String orgId) {
      failOnNonZeroClient(entity, clientId);
      failOnNonZeroOrg(entity, orgId);
    }
  };

  /**
   * SYSTEM_CLIENT access level, allows any client but only allows an organization with id '0'.
   */
  public static final AccessLevelChecker SYSTEM_CLIENT = new AccessLevelChecker() {
    @Override
    public void checkAccessLevel(String entity, String clientId, String orgId) {
      failOnNonZeroOrg(entity, orgId);
    }
  };

  /**
   * ORGANIZATION access level, only allows client and organization with id both unequal to id '0'.
   */
  public static final AccessLevelChecker ORGANIZATION = new AccessLevelChecker() {
    @Override
    public void checkAccessLevel(String entity, String clientId, String orgId) {
      failOnZeroClient(entity, clientId);
      failOnZeroOrg(entity, orgId);
    }
  };

  /**
   * CLIENT_ORGANIZATION access level, only allows client with id unequal to id '0' and any
   * organization.
   */
  public static final AccessLevelChecker CLIENT_ORGANIZATION = new AccessLevelChecker() {
    @Override
    public void checkAccessLevel(String entity, String clientId, String orgId) {
      failOnZeroClient(entity, clientId);
    }
  };

  // default allways all
  public void checkAccessLevel(String entity, String clientId, String orgId) {
  }

  protected void failOnZeroClient(String entity, String clientId) {
    // cliendId == null is by definition unequal to 0
    if (clientId != null && clientId.equals("0")) {
      throw new OBSecurityException("Entity " + entity + " may not have instances with client 0");
    }
  }

  protected void failOnNonZeroClient(String entity, String clientId) {
    if (clientId == null || !clientId.equals("0")) {
      throw new OBSecurityException("Entity " + entity + " may only have instances with client 0");
    }
  }

  protected void failOnZeroOrg(String entity, String orgId) {
    // orgId can be null for a new Organization which by default is not
    // the zero organization
    if (orgId != null && orgId.equals("0")) {
      throw new OBSecurityException("Entity " + entity
          + " may not have instances with organization *");
    }
  }

  protected void failOnNonZeroOrg(String entity, String orgId) {
    // orgId can be null for a new Organization which by default is not
    // the zero organization
    if (orgId == null || !orgId.equals("0")) {
      throw new OBSecurityException("Entity " + entity
          + " may only have instances with organization *");
    }
  }
}