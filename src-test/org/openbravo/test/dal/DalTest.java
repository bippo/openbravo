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
 * All portions are Copyright (C) 2008, 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):
 *   Martin Taal <martin.taal@openbravo.com>,
 *   Ivan Perdomo <ivan.perdomo@openbravo.com>,
 *   Leo Arias <leo.arias@openbravo.com>.
 ************************************************************************
 */

package org.openbravo.test.dal;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.CategoryAccounts;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.cashmgmt.CashBook;
import org.openbravo.model.financialmgmt.cashmgmt.CashBookAccounts;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.test.base.BaseTest;

/**
 * Test different parts of the dal api: {@link OBDal} and {@link OBCriteria}.
 * 
 * Note the testcases assume that they are run in the order defined in this class.
 * 
 * @author mtaal
 */

public class DalTest extends BaseTest {
  private static final Logger log = Logger.getLogger(DalTest.class);

  /**
   * Test to assert save false in a null char(1) column - Part I
   */
  public void testSaveBooleanValue1() {
    setSystemAdministratorContext();
    SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
    if (sysInfo.isEnableHeartbeat() == null) {
      sysInfo.setEnableHeartbeat(false);
    }
    OBDal.getInstance().save(sysInfo);
  }

  /**
   * Test to assert save false in a null char(1) column - Part II
   */
  public void testSaveBooleanValue2() {
    setSystemAdministratorContext();
    SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
    assertTrue(sysInfo.isEnableHeartbeat() != null);
  }

  /**
   * Test creates a {@link Category}, test simple save through {@link OBDal}. The new object is
   * removed in a later test.
   */
  public void testCreateBPGroup() {
    setTestUserContext();
    addReadWriteAccess(Category.class);
    final Category bpg = OBProvider.getInstance().get(Category.class);
    bpg.setDefault(true);
    bpg.setDescription("testdescription");
    bpg.setName("testname");
    bpg.setSearchKey("testvalue");
    bpg.setActive(true);
    OBDal.getInstance().save(bpg);
  }

  /**
   * Test queries for the {@link Category} created in the previous step and removes it.
   */
  public void testRemoveBPGroup() {
    setTestUserContext();
    addReadWriteAccess(Category.class);
    addReadWriteAccess(CategoryAccounts.class);
    final OBCriteria<Category> obCriteria = OBDal.getInstance().createCriteria(Category.class);
    obCriteria.add(Restrictions.eq(Category.PROPERTY_NAME, "testname"));
    final List<Category> bpgs = obCriteria.list();
    assertEquals(1, bpgs.size());
    final Category bpg = bpgs.get(0);
    final OBContext obContext = OBContext.getOBContext();
    assertEquals(obContext.getUser().getId(), bpg.getCreatedBy().getId());
    assertEquals(obContext.getUser().getId(), bpg.getUpdatedBy().getId());
    // update and create have occured less than one second ago
    // note that if the delete fails for other reasons that you will have a
    // Category in the database which has for sure a created/updated time
    // longer in the past, You need to manually delete the currency record
    if (false) {
      assertTrue("Created time not updated", (System.currentTimeMillis() - bpg.getCreationDate()
          .getTime()) < 3000);
      assertTrue("Updated time not updated", (System.currentTimeMillis() - bpg.getUpdated()
          .getTime()) < 3000);
    }

    // first delete the related accounts
    final OBCriteria<CategoryAccounts> obc2 = OBDal.getInstance().createCriteria(
        CategoryAccounts.class);
    obc2.add(Restrictions.eq(CategoryAccounts.PROPERTY_BUSINESSPARTNERCATEGORY, bpgs.get(0)));
    final List<CategoryAccounts> bpgas = obc2.list();
    for (final CategoryAccounts bga : bpgas) {
      OBDal.getInstance().remove(bga);
    }
    OBDal.getInstance().remove(bpgs.get(0));
  }

  /**
   * This test checks if the {@link Category} was removed in the previous step.
   */
  public void testCheckBPGroupRemoved() {
    setTestUserContext();
    addReadWriteAccess(Category.class);
    final OBCriteria<Category> obc = OBDal.getInstance().createCriteria(Category.class);
    obc.add(Restrictions.eq(Category.PROPERTY_NAME, "testname"));
    final List<Category> bpgs = obc.list();
    assertEquals(0, bpgs.size());
  }

  // test querying for a specific currency and then updating it
  // should fail for a user
  public void testUpdateCurrencyByUser() {
    setUserContext("E12DC7B3FF8C4F64924A98195223B1F8");
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "USD"));
    final List<Currency> cs = obc.list();
    assertEquals(1, cs.size());
    final Currency c = cs.get(0);
    // Call getValue and setValue directly to work around security checks on the description
    // that are not the objective of this test.
    c.setValue(Currency.PROPERTY_DESCRIPTION, c.getValue(Currency.PROPERTY_DESCRIPTION) + " a test");
    try {
      OBDal.getInstance().save(c);
      fail("No security check");
    } catch (final OBSecurityException e) {
      // successful check
      rollback();
    }
  }

  /**
   * Test updates the description of {@link Currency} by the admin user.
   */
  public void testUpdateCurrencyByAdmin() {
    setTestAdminContext();
    Currency c = null;
    String prevDescription = null;
    String newDescription = null;
    {
      final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
      obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "USD"));
      final List<Currency> cs = obc.list();
      assertEquals(1, cs.size());
      c = cs.get(0);
      prevDescription = c.getDescription();
      c.setDescription(c.getDescription() + " a test");
      newDescription = c.getDescription();
      OBDal.getInstance().save(c);
      commitTransaction();
    }

    // roll back the change, while doing some checks
    {
      final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
      obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "USD"));
      final List<Currency> cs = obc.list();
      assertEquals(1, cs.size());
      final Currency newC = cs.get(0);
      assertTrue(c != newC);
      assertEquals(newDescription, newC.getDescription());
      newC.setDescription(prevDescription);
      commitTransaction();
    }
  }

  /**
   * Tests the toString method of the BaseOBObject ({@link BaseOBObject#toString()}).
   */
  public void testToString() {
    setTestAdminContext();
    final List<Product> products = OBDal.getInstance().createCriteria(Product.class).list();
    final StringBuilder sb = new StringBuilder();
    for (final Product p : products) {
      sb.append(p.toString());
    }
  }

  /**
   * Tests a paged read of {@link MaterialTransaction} objects and print of the identifier. The
   * identifier of a transaction has been implemented such that it reads all the references (which
   * are non-null) and uses their identifier to create the identifier of the transaction. Also tests
   * sorting on the name of a related entity (in this case {@link MaterialTransaction#getProduct()
   * #getName()}.
   */
  public void testTransaction25PageRead() {
    setTestUserContext();
    addReadWriteAccess(MaterialTransaction.class);
    final OBCriteria<MaterialTransaction> countObc = OBDal.getInstance().createCriteria(
        MaterialTransaction.class);
    final int count = countObc.count();
    final int pageSize = 25;
    int pageCount = 1 + (count / pageSize);
    if (pageCount > 25) {
      pageCount = 25;
    }
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < pageCount; i++) {
      final OBCriteria<MaterialTransaction> obc = OBDal.getInstance().createCriteria(
          MaterialTransaction.class);
      obc.addOrderBy(MaterialTransaction.PROPERTY_PRODUCT + "." + Product.PROPERTY_NAME, false);
      obc.setMaxResults(pageSize);
      obc.setFirstResult(i * pageSize);

      sb.append("\nPAGE>>> " + (1 + i));
      for (final MaterialTransaction t : obc.list()) {
        sb.append("\n" + t.getIdentifier());
      }
    }
  }

  /**
   * Test reads 500 pages of the {@link MaterialTransaction} table and then prints how many
   * milliseconds one page took to retrieve.
   */
  public void testTransactionAllPagesTime() {
    setSystemAdministratorContext();
    final OBCriteria<MaterialTransaction> countObc = OBDal.getInstance().createCriteria(
        MaterialTransaction.class);
    final int count = countObc.count();
    long time = System.currentTimeMillis();
    final int pageSize = 25;
    int pageCount = 1 + (count / pageSize);
    pageCount = 500;
    long avg = 0;
    for (int i = 0; i < pageCount; i++) {
      final OBCriteria<MaterialTransaction> obc = OBDal.getInstance().createCriteria(
          MaterialTransaction.class);
      obc.addOrderBy(MaterialTransaction.PROPERTY_PRODUCT + "." + Product.PROPERTY_NAME, false);
      obc.setMaxResults(pageSize);
      obc.setFirstResult(i * pageSize);
      for (final MaterialTransaction t : obc.list()) {
        log.debug(t.getIdentifier());
        // System.err.println(t.getIdentifier() +
        // " client/organization " +
        // t.getClient().getName() + "/" +
        // t.getOrganization().getName());
      }
      if (avg == 0) {
        avg = System.currentTimeMillis() - time;
      } else {
        avg = (avg + System.currentTimeMillis() - time) / 2;
      }
      time = System.currentTimeMillis();
      SessionHandler.getInstance().commitAndClose();
    }

    log.debug("Read " + pageCount + " pages with average " + avg + " milliSeconds per page");
  }

  /**
   * Tests paged read of {@link Currency} objects.
   */
  public void testCurrencyPageRead() {
    setSystemAdministratorContext();
    final int count = OBDal.getInstance().createCriteria(Currency.class).count();
    final int pageSize = 5;
    final int pageCount = 1 + (count / 5);
    for (int i = 0; i < pageCount; i++) {
      final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
      obc.addOrderBy(Currency.PROPERTY_ISOCODE, false);
      obc.setMaxResults(pageSize);
      obc.setFirstResult(i * pageSize);

      log.debug("PAGE>>> " + (1 + i));
      for (final Currency c : obc.list()) {
        log.debug(c.getISOCode() + " " + c.getSymbol());
      }
    }
  }

  /**
   * Tests paged read of {@link CashBook} objects.
   */
  public void testCashBookPageRead() {
    setSystemAdministratorContext();
    final int count = OBDal.getInstance().createCriteria(CashBook.ENTITY_NAME).count();
    final int pageSize = 5;
    final int pageCount = 1 + (count / 5);
    for (int i = 0; i < pageCount; i++) {
      final OBCriteria<CashBook> obc = OBDal.getInstance().createCriteria(CashBook.ENTITY_NAME);
      obc.setFirstResult(i * pageSize);
      obc.setMaxResults(pageSize);

      log.debug("CashBook PAGE>>> " + (1 + i));
      for (final CashBook c : obc.list()) {
        log.debug(c.getName() + " " + c.getDescription());
      }
    }

  }

  /**
   * Tests if a database trigger is fired on creation of a {@link CashBook}.
   */
  public void testCashBookTrigger() {
    setTestUserContext();
    OBContext.setAdminMode(true);
    try {
      String cashBookId = "";
      {
        final OBCriteria<Currency> cc = OBDal.getInstance().createCriteria(Currency.class);
        cc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "USD"));
        final List<Currency> cs = cc.list();
        final Currency currency = cs.get(0);
        final CashBook c = OBProvider.getInstance().get(CashBook.class);
        c.setName("c_" + System.currentTimeMillis());
        c.setDescription("test");
        c.setDefault(false);
        c.set(CashBook.PROPERTY_CURRENCY, currency);

        OBDal.getInstance().save(c);
        cashBookId = c.getId();
        SessionHandler.getInstance().commitAndClose();
      }

      // now check if the save indeed worked out by seeing if there is a
      // cashbook account
      final OBCriteria<CashBookAccounts> cbc = OBDal.getInstance().createCriteria(
          CashBookAccounts.ENTITY_NAME);
      cbc.add(Restrictions.eq(CashBookAccounts.PROPERTY_CASHBOOK + "." + CashBook.PROPERTY_ID,
          cashBookId));
      final List<?> cbas = cbc.list();
      assertTrue(cbas.size() > 0);
      for (final Object co : cbas) {
        final CashBookAccounts cba = (CashBookAccounts) co;
        log.debug(cba.getUpdated() + " " + cba.getCashbook().getName());
        OBDal.getInstance().remove(cba);
      }
      OBDal.getInstance().remove(OBDal.getInstance().get(CashBook.class, cashBookId));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void testGetPropertyFromColumnName() {
    final Property property = DalUtil.getProperty("AD_COLUMN", "AD_COLUMN_ID");
    assertNotNull(property);
  }
}
