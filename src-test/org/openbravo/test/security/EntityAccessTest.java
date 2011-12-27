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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.EntityAccessChecker;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.currency.CurrencyTrl;
import org.openbravo.test.base.BaseTest;

/**
 * Tests access on the basis of window and table definitions. Also tests derived read access.
 * 
 * @see EntityAccessChecker
 * 
 * @author mtaal
 */

public class EntityAccessTest extends BaseTest {

  private static final Logger log = Logger.getLogger(EntityAccessTest.class);

  /**
   * Creates test data, a {@link Currency}.
   */
  public void testCreateCurrency() {
    setTestAdminContext();
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "TE2"));
    final List<Currency> cs = obc.list();
    if (cs.size() == 0) {
      final Currency c = OBProvider.getInstance().get(Currency.class);
      c.setSymbol("TE2");
      c.setDescription("test currency");
      c.setISOCode("TE2");
      c.setPricePrecision((long) 5);
      c.setStandardPrecision((long) 6);
      c.setCostingPrecision((long) 4);
      OBDal.getInstance().save(c);
    }
  }

  /**
   * Test tries to remove the {@link Currency}. Which should fail as it is not deletable.
   * 
   * After fixing issue #0010139, all entities are deletable. Therefore this test case is not going
   * to be executed.
   */
  public void doNotExecutetestNonDeletable() {
    setTestUserContext();
    addReadWriteAccess(Currency.class);
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "TE2"));
    final List<Currency> cs = obc.list();
    assertEquals(1, cs.size());
    final Currency c = cs.get(0);
    try {
      OBDal.getInstance().remove(c);
      OBDal.getInstance().flush();
      fail("Currency should be non-deletable");
    } catch (final OBSecurityException e) {
      assertTrue("Wrong exception thrown:  " + e.getMessage(),
          e.getMessage().indexOf("is not deletable") != -1);
    }
  }

  /**
   * Checks the derived readable concept, only identifier fields of a derived readable object may be
   * read. Also checks the allowRead concept of a BaseOBObject (
   * {@link BaseOBObject#setAllowRead(boolean)})
   */
  public void testCheckDerivedReadableCurrency() {
    setUserContext(TEST2_USER_ID);
    final Currency c = OBDal.getInstance().get(Currency.class, "100");
    log.debug(c.getIdentifier());
    log.debug(c.getId());
    try {
      log.debug(c.getCostingPrecision());
      fail("Derived readable not applied");
    } catch (final OBSecurityException e) {
      assertTrue("Wrong exception thrown:  " + e.getMessage(),
          e.getMessage().indexOf("is not directly readable") != -1);

      try {
        c.setAllowRead(true);
        fail("Allow read my only be called in adminmode");
      } catch (OBSecurityException x) {
        OBContext.setAdminMode();
        try {
          c.setAllowRead(true);
        } finally {
          OBContext.restorePreviousMode();
        }
        // this should be allowed
        log.debug(c.getCostingPrecision());
        // set back
        OBContext.setAdminMode();
        try {
          c.setAllowRead(false);
        } finally {
          OBContext.restorePreviousMode();
        }
        try {
          c.setAllowRead(true);
          fail("Allow read my only be called in adminmode");
        } catch (OBSecurityException y) {
          // okay
        }
      }
    }
  }

  /**
   * Test derived readable on a set method, also there this check must be done.
   */
  public void testUpdateCurrencyDerivedRead() {
    setUserContext(TEST2_USER_ID);
    final Currency c = OBDal.getInstance().get(Currency.class, "100");
    try {
      c.setCostingPrecision((long) 5);
      fail("Derived readable not checked on set");
    } catch (final OBSecurityException e) {
      assertTrue("Wrong exception thrown:  " + e.getMessage(),
          e.getMessage().indexOf("is not directly readable") != -1);
    }
    try {
      OBDal.getInstance().save(c);
      fail("No security check");
    } catch (final OBSecurityException e) {
      // successfull check
      assertTrue("Wrong exception thrown:  " + e.getMessage(),
          e.getMessage().indexOf("is not writable by this user") != -1);
    }
  }

  /**
   * Checks non-readable, if an object/entity is not readable then it may not be read through the
   * {@link OBDal}.
   */
  public void testNonReadable() {
    assertTrue(true);
    // FIXME: find a test case for this!

    // setUserContext(getRandomUserId());
    // try {
    // final OBCriteria<Costing> obc = OBDal.getInstance().createCriteria(Costing.class);
    // obc.add(Restrictions.eq(Costing.PROPERTY_ID, "FE8370A36E91432688A323A07D606622"));
    // final List<Costing> cs = obc.list();
    // assertTrue(cs.size() > 0);
    // fail("Non readable check not enforced");
    // } catch (final OBSecurityException e) {
    // assertTrue("Wrong exception thrown:  " + e.getMessage(), e.getMessage().indexOf(
    // "is not readable") != -1);
    // }
  }

  /**
   * Removes the test data by using the administrator account.
   */
  public void testZDeleteTestData() {
    setTestUserContext();
    addReadWriteAccess(Currency.class);
    addReadWriteAccess(CurrencyTrl.class);
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "TE2"));
    final List<Currency> cs = obc.list();
    assertEquals(1, cs.size());
    OBDal.getInstance().remove(cs.get(0));
  }
}