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

package org.openbravo.dal.security;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;

/**
 * This class combines all security checks which are performed on entity level:
 * <ul>
 * <li>Delete: is the entity deletable (@see {@link Entity#isDeletable()}) and does the user have
 * write access to the entity.</li>
 * <li>Write: is done in case of create and update actions. The following checks are performed: is
 * the organization writable, is the client of the object the same as is the entity writable (@see
 * EntityAccessChecker#isWritable(Entity))
 * 
 * @author mtaal
 */

public class SecurityChecker implements OBSingleton {

  private static SecurityChecker instance;

  public static SecurityChecker getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(SecurityChecker.class);
    }
    return instance;
  }

  public void checkDeleteAllowed(Object o) {
    if (!OBContext.getOBContext().isInAdministratorMode() && o instanceof BaseOBObject) {
      final BaseOBObject bob = (BaseOBObject) o;
      final Entity entity = ModelProvider.getInstance().getEntity(bob.getEntityName());
      if (!entity.isDeletable()) {
        throw new OBSecurityException("Entity " + entity.getName() + " is not deletable");
      }
    }
    checkWriteAccess(o);
  }

  /**
   * Performs several write access checks when an object is created or updated:
   * <ul>
   * <li>is the organization writable (@see OBContext#getWritableOrganizations())</li>
   * <li>is the client of the object the same as the client of the user (@see
   * OBContext#getCurrentClient())</li>
   * <li>is the Entity writable for this user (@see EntityAccessChecker#isWritable(Entity))
   * <li>are the client and organization correct from an access level perspective (@see
   * AccessLevelChecker).</lo>
   * 
   * @param obj
   *          the object to check
   * @return true if writable, false otherwise
   * @see Entity
   */
  // NOTE: this method needs to be kept insync with the checkWritable method
  public boolean isWritable(Object obj) {

    // check that the client id and organization id are resp. in the list of
    // user_client and user_org
    // TODO: throw specific and translated exception, for more info:
    // Utility.translateError(this, vars, vars.getLanguage(),
    // Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()))

    final OBContext obContext = OBContext.getOBContext();

    String clientId = "";
    if (obj instanceof ClientEnabled && ((ClientEnabled) obj).getClient() != null) {
      clientId = (String) DalUtil.getId(((ClientEnabled) obj).getClient());
    }
    String orgId = "";
    if (obj instanceof OrganizationEnabled && ((OrganizationEnabled) obj).getOrganization() != null) {
      orgId = (String) DalUtil.getId(((OrganizationEnabled) obj).getOrganization());
    }

    final Entity entity = ((BaseOBObject) obj).getEntity();
    if (!obContext.isInAdministratorMode() && clientId.length() > 0) {
      if (obj instanceof ClientEnabled) {
        if (!obContext.getCurrentClient().getId().equals(clientId)) {
          return false;
        }
      }

      // todo can be improved by only checking if the client or
      // organization
      // actually changed...
      if (!obContext.getEntityAccessChecker().isWritable(entity)) {
        return false;
      }

      if (obj instanceof OrganizationEnabled && orgId.length() > 0) {
        if (!obContext.getWritableOrganizations().contains(orgId)) {
          return false;
        }
      }
    }

    // accesslevel check must also be done for administrators
    try {
      entity.checkAccessLevel(clientId, orgId);
    } catch (final OBSecurityException e) {
      return false;
    }
    return true;
  }

  /**
   * Performs the same checks as {@link #isWritable(Object)}. Does not return true/false but throws
   * a OBSecurityException if the object is not writable.
   * 
   * @param obj
   *          the object to check
   * @throws OBSecurityException
   */
  // NOTE: this method needs to be kept insync with the isWritable method
  public void checkWriteAccess(Object obj) {

    // check that the client id and organization id are resp. in the list of
    // user_client and user_org
    // TODO: throw specific and translated exception, for more info:
    // Utility.translateError(this, vars, vars.getLanguage(),
    // Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()))
    final OBContext obContext = OBContext.getOBContext();

    String clientId = "";
    if (obj instanceof ClientEnabled && ((ClientEnabled) obj).getClient() != null) {
      clientId = (String) DalUtil.getId(((ClientEnabled) obj).getClient());
    }
    String orgId = "";
    if (obj instanceof OrganizationEnabled && ((OrganizationEnabled) obj).getOrganization() != null) {
      orgId = (String) DalUtil.getId(((OrganizationEnabled) obj).getOrganization());
    }

    final Entity entity = ((BaseOBObject) obj).getEntity();
    if ((!obContext.isInAdministratorMode() || obContext.doOrgClientAccessCheck())
        && clientId.length() > 0) {
      if (obj instanceof ClientEnabled) {
        if (!obContext.getCurrentClient().getId().equals(clientId)) {
          // TODO: maybe move rollback to exception throwing
          SessionHandler.getInstance().setDoRollback(true);
          throw new OBSecurityException("Client (" + clientId + ") of object (" + obj
              + ") is not present in ClientList " + obContext.getCurrentClient().getId());
        }
      }

      // todo can be improved by only checking if the client or
      // organization
      // actually changed...
      obContext.getEntityAccessChecker().checkWritable(entity);

      if (obj instanceof OrganizationEnabled && orgId != null && orgId.length() > 0) {
        // todo as only the id is required this can be made much more
        // efficient
        // by
        // not loading the hibernate proxy
        if (!obContext.getWritableOrganizations().contains(orgId)) {
          // TODO: maybe move rollback to exception throwing
          SessionHandler.getInstance().setDoRollback(true);
          throw new OBSecurityException("Organization " + orgId + " of object (" + obj
              + ") is not present in OrganizationList " + obContext.getWritableOrganizations());
        }
      }
    }

    // accesslevel check must also be done for administrators
    entity.checkAccessLevel(clientId, orgId);
  }
}