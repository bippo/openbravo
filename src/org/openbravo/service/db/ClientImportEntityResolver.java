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

package org.openbravo.service.db;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityResolver;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * This entity resolver is used in complete Client imports. With complete Client imports all the
 * data on Client/Organization level is present in the xml String. This means that the
 * EntityResolver only needs to search for existing objects on System level. This class overrides
 * some methods from the {@link EntityResolver} to accomplish this. New objects are assumed to have
 * the client/organization set through the xml.
 * <p/>
 * This resolver does not query the AD_REF_DATA_LOADED table.
 * 
 * @author mtaal
 */

public class ClientImportEntityResolver extends EntityResolver {

  public static ClientImportEntityResolver getInstance() {
    return OBProvider.getInstance().get(ClientImportEntityResolver.class);
  }

  /**
   * Searches for a existing entity with the same id. Only searches on system level.
   * 
   * @param entityName
   *          the name of the entity to resolve
   * @param id
   *          the id
   * @param referenced
   *          is true if the entity needs to be resolved because it is referenced from imported
   *          data, is false if the entity is part of the main imported dataset
   * 
   * @see BaseOBObject#getEntityName()
   * @see EntityResolver#resolve(String, String, boolean)
   */
  @Override
  public BaseOBObject resolve(String entityName, String id, boolean referenced) {

    final Entity entity = ModelProvider.getInstance().getEntity(entityName);

    BaseOBObject result = null;

    // The zero organization is in fact a system level concept, return it
    // always
    if (entityName.equals(Organization.ENTITY_NAME) && id != null && id.equals("0")) {
      return getOrganizationZero();
    }

    if (entityName.equals(Client.ENTITY_NAME) && id != null && id.equals("0")) {
      return getClientZero();
    }

    if (id != null) {
      result = getData().get(getKey(entityName, id));
      if (result != null) {
        return result;
      }
    }

    // note id can be null if someone did not care to add it in a manual
    // xml file
    // In case of client import the client is always created new
    if (!entityName.equals(Client.ENTITY_NAME) && id != null) {
      result = searchInstance(entity, id);
    }

    // search using the id if it is a view, note can be wrong as there can
    // be duplicates in id for older id values, but is the best we can do
    // at the moment
    if (result == null && entity.isView()) {
      result = getObjectUsingOriginalId(id);
    }

    if (result != null) {
      // found, cache it for future use
      addObjectToCaches(id, entityName, result);
    } else {
      // not found create a new one
      result = (BaseOBObject) OBProvider.getInstance().get(entityName);

      if (id != null) {

        // force new
        result.setNewOBObject(true);

        // check if we can keep the id for this one
        if (!OBDal.getInstance().exists(entityName, id)) {
          result.setId(id);
        }

        // keep it here so it can be found later
        addObjectToCaches(id, entityName, result);
      }
      setClientOrganization(result);
    }
    return result;
  }

  // search on the basis of the access level of the entity
  @Override
  public BaseOBObject searchInstance(Entity entity, String id) {
    return searchSystem(id, entity);
  }

  @Override
  protected BaseOBObject findUniqueConstrainedObject(BaseOBObject obObject) {
    return null;
  }

  @Override
  protected void setClient(Client client) {
  }

  @Override
  protected void setOrganization(Organization organization) {
  }
}