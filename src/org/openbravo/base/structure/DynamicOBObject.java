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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.structure;

import static org.openbravo.model.ad.system.Client.PROPERTY_ORGANIZATION;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CLIENT;

import java.util.Date;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Dynamic OB Object which supports full dynamic mapping without java members.
 * 
 * @author mtaal
 */

@SuppressWarnings("serial")
public class DynamicOBObject extends BaseOBObject implements Traceable, ClientEnabled,
    OrganizationEnabled {

  private String entityName;

  public boolean isNew() {
    return getId() == null;
  }

  public boolean isActive() {
    return (Boolean) get(Organization.PROPERTY_ACTIVE);
  }

  public void setActive(boolean active) {
    set(Organization.PROPERTY_ACTIVE, active);
  }

  @Override
  public String getId() {
    return (String) get(Organization.PROPERTY_ID);
  }

  public void setId(String id) {
    set(Organization.PROPERTY_ID, id);
  }

  public User getUpdatedBy() {
    return (User) get(Organization.PROPERTY_UPDATEDBY);
  }

  public void setUpdatedBy(User updatedby) {
    set(Organization.PROPERTY_UPDATEDBY, updatedby);
  }

  public Date getUpdated() {
    return (Date) get(Organization.PROPERTY_UPDATED);
  }

  public void setUpdated(Date updated) {
    set(Organization.PROPERTY_UPDATED, updated);
  }

  public User getCreatedBy() {
    return (User) get(Organization.PROPERTY_CREATEDBY);
  }

  public void setCreatedBy(User createdby) {
    set(Organization.PROPERTY_CREATEDBY, createdby);
  }

  public Date getCreationDate() {
    return (Date) get(Organization.PROPERTY_CREATIONDATE);
  }

  public void setCreationDate(Date created) {
    set(Organization.PROPERTY_CREATIONDATE, created);
  }

  @Override
  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;

    // set the default values
    final Entity e = getEntity();
    for (final Property p : e.getProperties()) {
      // only do primitive default values
      if (!p.isPrimitive()) {
        continue;
      }
      final Object defaultValue = p.getActualDefaultValue();
      if (defaultValue != null) {
        setValue(p.getName(), defaultValue);
      }
    }
  }

  @Override
  public String getIdentifier() {
    return IdentifierProvider.getInstance().getIdentifier(this);
  }

  public Client getClient() {
    return (Client) get(PROPERTY_CLIENT);
  }

  public void setClient(Client client) {
    set(PROPERTY_CLIENT, client);
  }

  public Organization getOrganization() {
    return (Organization) get(PROPERTY_ORGANIZATION);
  }

  public void setOrganization(Organization organization) {
    set(PROPERTY_ORGANIZATION, organization);
  }
}