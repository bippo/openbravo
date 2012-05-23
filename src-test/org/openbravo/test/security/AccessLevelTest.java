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

package org.openbravo.test.security;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.AccessLevel;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.validation.AccessLevelChecker;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.order.Order;
import org.openbravo.test.base.BaseTest;

/**
 * Tests/checks the accesslevel of an entity. See the {@link AccessLevelChecker}.
 * 
 * @see AccessLevelChecker
 * @see AccessLevel
 * 
 * @author mtaal
 */

public class AccessLevelTest extends BaseTest {

  /**
   * Test that the access level is tested correctly
   */
  public void testUserDataAccessLevel() {
    // Table Access Level:
    // "6";"System/Client"
    // "1";"Organization"
    // "3";"Client/Organization"
    // "4";"System only"
    // "7";"All"

    // User level:
    // "S";"System"
    // " C";"Client"
    // "  O";"Organization"
    // " CO";"Client+Organization"

    setSystemAdministratorContext();
    final List<Table> tables = OBDal.getInstance().createCriteria(Table.class).list();

    final Role role = OBDal.getInstance().get(Role.class, TEST_ROLE_ID);
    setTestAdminContext();
    OBContext.getOBContext().setRole(role);
    final String userLevel = OBContext.getOBContext().getUserLevel();
    assertEquals("CO", userLevel.trim());
    final OBContext tmpContext = OBContext.getOBContext();

    boolean testDone = false;
    for (Table t : tables) {

      setSystemAdministratorContext(); // reset to sysadmin

      final Entity entity = ModelProvider.getInstance().getEntityByTableName(t.getDBTableName());
      if (t.getDataAccessLevel().contains("6") || t.getDataAccessLevel().contains("4")) {
        try {

          OBContext.setOBContext(tmpContext); // set to user context

          // ignore these
          if (OBContext.getOBContext().getEntityAccessChecker().isDerivedReadable(entity)) {
            continue;
          }
          testDone = true;
          OBContext.getOBContext().getEntityAccessChecker().checkReadable(entity);
          fail("Incorrect access level check for entity " + entity + " and userlevel " + userLevel
              + " data access level " + t.getDataAccessLevel());
        } catch (OBSecurityException e) {
          // correct
        }
      }
    }
    assertTrue(testDone);
  }

  /**
   * Tests/checks if the current client/org of the all objects in the database is valid for the
   * access level defined for that entity.
   */
  public void testADataAccessLevel() {
    setSystemAdministratorContext();
    final List<Entity> entities = ModelProvider.getInstance().getModel();
    final StringBuilder sb = new StringBuilder();
    final Client clientZero = OBDal.getInstance().get(Client.class, "0");
    final Organization orgZero = OBDal.getInstance().get(Organization.class, "0");
    for (Entity e : entities) {
      final StringBuilder where = new StringBuilder();
      final List<Object> params = new ArrayList<Object>();
      if (e.getAccessLevel() == AccessLevel.ALL) {
        // anything allowed continue
        continue;
      } else if (e.getAccessLevel() == AccessLevel.CLIENT) {
        sb.append("Access Level CLIENT encountered for entity " + e.getName() + "/"
            + e.getTableName() + ", this AccessLevel is not supported.\n");
      } else if (e.getAccessLevel() == AccessLevel.CLIENT_ORGANIZATION) {
        if (!e.isClientEnabled()) {
          continue;
        }
        where.append("where client = ?");
        params.add(clientZero);
      } else if (e.getAccessLevel() == AccessLevel.ORGANIZATION) {
        if (!e.isOrganizationEnabled() || !e.isClientEnabled()) {
          // ignore these
          continue;
        }
        where.append("where client = ? or organization = ?");
        params.add(clientZero);
        params.add(orgZero);
      } else if (e.getAccessLevel() == AccessLevel.SYSTEM) {
        if (!e.isOrganizationEnabled()) {
          where.append("where client != ?");
          params.add(clientZero);
        } else {
          where.append("where client != ? or organization != ?");
          params.add(clientZero);
          params.add(orgZero);
        }
        if (!e.isClientEnabled()) {
          // special case happens for AD_SQL_SCRIPT
          continue;
        }
      } else if (e.getAccessLevel() == AccessLevel.SYSTEM_CLIENT) {
        if (!e.isOrganizationEnabled()) {
          // ignore these
          continue;
        }
        where.append("where organization != ?");
        params.add(orgZero);
      }
      final OBQuery<BaseOBObject> obq = OBDal.getInstance().createQuery(e.getName(),
          where.toString());
      obq.setParameters(params);
      for (BaseOBObject bob : obq.list()) {
        String clientId = null;
        if (bob instanceof ClientEnabled) {
          clientId = ((ClientEnabled) bob).getClient().getId();
        }
        String orgId = null;
        if (bob instanceof OrganizationEnabled) {
          orgId = ((OrganizationEnabled) bob).getOrganization().getId();
        }
        sb.append("Object " + bob.getIdentifier() + " (" + bob.getEntityName()
            + ") has an invalid client/org " + clientId + "/" + orgId
            + " for the accesslevel of the entity/table: " + e.getAccessLevel().name() + ".\n");
      }
    }
    if (sb.length() > 0) {
      fail(sb.toString());
    }
  }

  /**
   * Tests the Client Organization access level.
   */
  public void testAccessLevelCO() {
    setTestAdminContext();
    final Client c = OBDal.getInstance().get(Client.class, "0");

    final BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class,
        "2C4C71BC828B47A0AF2A79855FD3BA7A");
    bp.setClient(c);
    try {
      commitTransaction();
      fail();
    } catch (final OBException e) {
      // no fail!
      assertTrue(e.getMessage().indexOf("may not have instances with client 0") != -1);
      rollback();
    }
  }

  /**
   * Test the System access level.
   */
  public void testAccessLevelSystem() {
    setSystemAdministratorContext();
    final Organization o = OBDal.getInstance().get(Organization.class, "1000002");
    final Table t = OBDal.getInstance().get(Table.class, "100");
    t.setOrganization(o);

    try {
      commitTransaction();
      fail();
    } catch (final OBException e) {
      // no fail!
      assertTrue("Invalid exception: " + e.getMessage(),
          e.getMessage().indexOf(" may only have instances with organization *") != -1);
      rollback();
    }
  }

  /**
   * Tests the Organization Access Level.
   */
  public void testAccessLevelOrganization() {
    setSystemAdministratorContext();
    final Organization o = OBDal.getInstance().get(Organization.class, "0");
    final Order c = OBDal.getInstance().get(Order.class, "F8492493E92C4EE5B5251AC4574778B7");
    c.setOrganization(o);

    try {
      commitTransaction();
      fail();
    } catch (final OBException e) {
      // no fail!
      assertTrue("Invalid exception " + e.getMessage(),
          e.getMessage().indexOf(" may not have instances with organization *") != -1);
      rollback();
    }
  }

  /**
   * Tests Access Level System Client.
   */
  public void testAccessLevelSC() {
    setSystemAdministratorContext();
    final Organization o = OBDal.getInstance().get(Organization.class, "1000001");
    final Country c = OBDal.getInstance().get(Country.class, "100");
    c.setOrganization(o);

    try {
      commitTransaction();
      fail("The organization of a system client may not be set to a non-zero org.");
    } catch (final OBException e) {
      // no fail!
      assertTrue("Invalid exception " + e.getMessage(),
          e.getMessage().indexOf("may only have instances with organization *") != -1);
      rollback();
    }
  }

}