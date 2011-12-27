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

package org.openbravo.test.security;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.test.base.BaseTest;

/**
 * Iterates over all the data in the database and checks the organization of the referenced objects.
 * 
 * @see OrganizationStructureProvider
 * @see OBContext#getReadableOrganizations()
 * 
 * @author mtaal
 */

public class AllDataAllowedOrganizationsTest extends BaseTest {

  /**
   * Test all data for correct organization settings.
   */
  public void testReferencedOrganizations() {
    setSystemAdministratorContext();

    // create an organizationstructureprovider for each client
    Map<String, OrganizationStructureProvider> orgStructureProviders = new HashMap<String, OrganizationStructureProvider>();
    for (Client client : OBDal.getInstance().createQuery(Client.class, null).list()) {
      final OrganizationStructureProvider orgProvider = new OrganizationStructureProvider();
      orgProvider.setClientId(client.getId());
      orgStructureProviders.put(client.getId(), orgProvider);
    }

    final StringBuilder sb = new StringBuilder();
    for (Entity entity : ModelProvider.getInstance().getModel()) {
      System.err.println("Checking " + entity);
      final OBCriteria<BaseOBObject> criteria = OBDal.getInstance()
          .createCriteria(entity.getName());
      criteria.setFilterOnActive(false);
      criteria.setFilterOnReadableClients(false);
      criteria.setFilterOnReadableOrganization(false);
      for (BaseOBObject o : criteria.list()) {
        if (!(o instanceof ClientEnabled) || !(o instanceof OrganizationEnabled)) {
          continue;
        }
        final OrganizationEnabled orgEnabled = (OrganizationEnabled) o;
        final ClientEnabled clientEnabled = (ClientEnabled) o;
        final OrganizationStructureProvider orgProvider = orgStructureProviders.get(clientEnabled
            .getClient().getId());
        for (Property property : entity.getProperties()) {
          // a many-to-one
          if (!property.isPrimitive() && !property.isOneToMany()) {
            final BaseOBObject value = (BaseOBObject) o.get(property.getName());
            if (value != null && value instanceof OrganizationEnabled) {
              if (!orgProvider.isInNaturalTree(orgEnabled.getOrganization(),
                  ((OrganizationEnabled) value).getOrganization())) {
                sb.append("Object " + o + " has organization " + orgEnabled.getOrganization()
                    + " but references another object " + value + " with an organization "
                    + ((OrganizationEnabled) value).getOrganization()
                    + " which is not in the natural tree of the first org\n");
              }
            }
          }
        }
      }
      OBDal.getInstance().commitAndClose();
    }
    System.err.println(sb.toString());
  }
}