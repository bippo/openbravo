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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.security;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.plm.Product;
import org.openbravo.test.base.BaseTest;

/**
 * Tests check of writable organization and allowed client.
 * 
 * @see OBContext#getWritableOrganizations()
 * @see OBContext#getReadableClients()
 * @see OBContext#getReadableOrganizations()
 * 
 * @author mtaal
 */

public class WritableReadableOrganizationClientTest extends BaseTest {

  /**
   * Checks for two users that each writable organization also occurs in the readable organizations
   * list.
   */
  public void testAccessLevelCO() {
    setSystemAdministratorContext();
    doCheckUser();
    setTestUserContext();
    doCheckUser();
  }

  private void doCheckUser() {
    final OBContext obContext = OBContext.getOBContext();
    final Set<String> writOrgs = obContext.getWritableOrganizations();
    final String[] readOrgs = obContext.getReadableOrganizations();
    final StringBuilder sb = new StringBuilder();
    for (final String s : readOrgs) {
      sb.append("," + s);
    }

    for (final String wo : writOrgs) {
      boolean found = false;
      for (final String s : readOrgs) {
        found = s.equals(wo);
        if (found) {
          break;
        }
      }
      assertTrue("Org " + wo + " not present in readableOrglist " + sb.toString(), found);
    }
  }

  /**
   * Checks that the current client is present in the set of readable clients.
   * 
   * @see OBContext#getReadableClients()
   * @see OBContext#getCurrentClient()
   */
  public void testClient() {
    final OBContext obContext = OBContext.getOBContext();
    final String[] cs = obContext.getReadableClients();
    final String cid = obContext.getCurrentClient().getId();
    boolean found = false;
    final StringBuilder sb = new StringBuilder();
    for (final String s : cs) {
      sb.append("," + s);
    }
    for (final String s : cs) {
      found = s.equals(cid);
      if (found) {
        break;
      }
    }
    assertTrue("Current client " + cid + " not found in clienttlist " + sb.toString(), found);
  }

  /**
   * Checks that writable organization is checked when an invalid update is attempted.
   */
  public void testUpdateNotAllowed() {
    setTestUserContext();
    addReadWriteAccess(Product.class);
    final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(Product.class);
    obc.add(Restrictions.eq("id", TEST_PRODUCT_ID));
    final List<Product> ps = obc.list();
    assertEquals(1, ps.size());
    final Product p = ps.get(0);
    p.setCapacity(new BigDecimal(1));

    // switch usercontext to force exception
    setUserContext(TEST2_USER_ID);
    try {
      commitTransaction();
      fail("Writable organizations not checked");
    } catch (final OBException e) {
      rollback();
      // FIXME: The Message should be checked
      // assertTrue("Invalid exception " + e.getMessage(), e.getMessage().indexOf(
      // " is not writable by this user") != -1);
    }
  }

  /**
   * Test if a check is done that an update in an invalid client is not allowed.
   */
  public void testCheckInvalidClient() {
    setTestUserContext();
    addReadWriteAccess(Category.class);
    final OBCriteria<Category> obc = OBDal.getInstance().createCriteria(Category.class);
    obc.add(Restrictions.eq("name", "Supplier"));
    final List<Category> bogs = obc.list();
    assertEquals(1, bogs.size());
    final Category bp = bogs.get(0);
    bp.setDescription(bp.getDescription() + "A");
    // switch usercontext to force exception
    setUserContext(QA_TEST_ADMIN_USER_ID);
    try {
      commitTransaction();
    } catch (final OBException e) {
      rollback();
      assertTrue("Invalid exception " + e.getMessage(),
          e.getMessage().indexOf("is not present in ClientList") != -1);
    }
  }
}