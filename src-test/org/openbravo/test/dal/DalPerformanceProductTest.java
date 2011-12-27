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

package org.openbravo.test.dal;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Product;
import org.openbravo.test.base.BaseTest;

/**
 * Does some simple performance tests by reading and updating of all {@link Product} objects, either
 * directly or in paged mode.
 * 
 * @author mtaal
 */

public class DalPerformanceProductTest extends BaseTest {
  private static final Logger log = Logger.getLogger(DalPerformanceProductTest.class);

  /**
   * Tests a paged read of products and print of the identifier. The timing is reported in the log.
   */
  public void testProduct25PageRead() {
    setUserContext(getRandomUser().getId());
    final OBCriteria<Product> countObc = OBDal.getInstance().createCriteria(Product.class);
    final int count = countObc.count();
    log.debug("Number of products " + count);
    final int pageSize = 25;
    final int pageCount = 1 + (count / pageSize);
    long time = System.currentTimeMillis();
    long avg = 0;
    for (int i = 0; i < pageCount; i++) {
      final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(Product.class);
      obc.setFilterOnReadableOrganization(false);
      obc.addOrderBy(Product.PROPERTY_NAME, true);
      obc.setMaxResults(pageSize);
      obc.setFirstResult(i * pageSize);

      log.debug("PAGE>>> " + (1 + i));
      for (final Product t : obc.list()) {
        log.debug(t.getIdentifier());
      }
      if (avg == 0) {
        avg = System.currentTimeMillis() - time;
      } else {
        avg = (avg + System.currentTimeMillis() - time) / 2;
      }
      time = System.currentTimeMillis();
      commitTransaction();
    }

    log.debug("Read " + pageCount + " pages with average " + avg + " milliSeconds per page");
  }

  /**
   * Tests a paged read of products and print of the identifier. In addition extra information is
   * read for the {@link Product}, nl. the {@link Product#getProductCategory()} and the
   * {@link Product#getTaxCategory()}. The timing is reported in the log.
   */
  public void testProduct25PageReadGetExtra() {
    setUserContext(getRandomUser().getId());
    final OBCriteria<Product> countObc = OBDal.getInstance().createCriteria(Product.class);
    final int count = countObc.count();
    log.debug("Number of products " + count);
    final int pageSize = 25;
    final int pageCount = 1 + (count / pageSize);
    long time = System.currentTimeMillis();
    long avg = 0;
    for (int i = 0; i < pageCount; i++) {
      final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(Product.class);
      obc.setFilterOnReadableOrganization(false);
      obc.addOrderBy(Product.PROPERTY_NAME, true);
      obc.setMaxResults(pageSize);
      obc.setFirstResult(i * pageSize);

      log.debug("PAGE>>> " + (1 + i));
      for (final Product t : obc.list()) {
        log.debug(t.toString() + " Product Category "
            + (t.getProductCategory() != null ? t.getProductCategory().getIdentifier() : "NULL")
            + " Tax Category "
            + (t.getTaxCategory() != null ? t.getTaxCategory().getIdentifier() : "NULL"));
        log.debug(t.toString());
      }
      if (avg == 0) {
        avg = System.currentTimeMillis() - time;
      } else {
        avg = (avg + System.currentTimeMillis() - time) / 2;
      }
      time = System.currentTimeMillis();
      commitTransaction();
    }

    log.debug("Read " + pageCount + " pages with average " + avg
        + " milliSeconds per page (read extra info)");
  }

  /**
   * Reads all {@link Product} objects sorted by name and also prints related information:
   * {@link Product#getProductCategory()} and {@link Product#getTaxCategory()}.
   */
  public void testReadProducts() {
    setUserContext(getRandomUser().getId());
    final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(Product.class);
    obc.setFilterOnReadableOrganization(false);
    obc.addOrderBy(Product.PROPERTY_NAME, true);

    final long time = System.currentTimeMillis();
    for (final Product t : obc.list()) {
      final String rs = t.toString() + " Product Category "
          + (t.getProductCategory() != null ? t.getProductCategory().getIdentifier() : "NULL")
          + " Tax Category "
          + (t.getTaxCategory() != null ? t.getTaxCategory().getIdentifier() : "NULL");
      log.debug(rs);
    }

    log.debug("Read 75000 products in " + (System.currentTimeMillis() - time)
        + " milliSeconds (reading extra info)");
  }

  /**
   * Reads all {@link Product} objects and updates the name.
   */
  public void testUpdateAllProducts() {
    setTestUserContext();
    final OBCriteria<Product> countObc = OBDal.getInstance().createCriteria(Product.class);
    final int count = countObc.count();
    log.debug("Number of products " + count);
    final long time = System.currentTimeMillis();
    final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(Product.class);
    obc.setFilterOnReadableOrganization(false);
    obc.addOrderBy(Product.PROPERTY_NAME, true);

    // don't be bothered by security exceptions
    addReadWriteAccess(Product.class);
    for (final Product t : obc.list()) {
      t.setName(t.getName() + "_t");
      OBDal.getInstance().save(t);
    }
    OBDal.getInstance().flush();
    commitTransaction();
    log.debug("Updated " + count + " products in " + (System.currentTimeMillis() - time)
        + " milliseconds ");
  }

  /**
   * Reads all products in a paged manner and updates the name.
   */
  public void testUpdateAllProductsByPage() {
    setTestUserContext();
    addReadWriteAccess(Product.class);
    final OBCriteria<Product> countObc = OBDal.getInstance().createCriteria(Product.class);
    final int count = countObc.count();
    log.debug("Number of products " + count);
    final int pageSize = 25;
    final int pageCount = 1 + (count / pageSize);
    long time = System.currentTimeMillis();
    long avg = 0;
    for (int i = 0; i < pageCount; i++) {
      final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(Product.class);
      obc.setFilterOnReadableOrganization(false);
      obc.addOrderBy(Product.PROPERTY_NAME, true);
      obc.setMaxResults(pageSize);
      obc.setFirstResult(i * pageSize);

      // log.debug("PAGE>>> " + (1 + i));
      for (final Product t : obc.list()) {
        if (t.getName().endsWith("_t")) {
          t.setName(t.getName().substring(0, t.getName().length() - 2));
        }
        OBDal.getInstance().save(t);
      }
      if (avg == 0) {
        avg = System.currentTimeMillis() - time;
      } else {
        avg = (avg + System.currentTimeMillis() - time) / 2;
      }
      time = System.currentTimeMillis();
      commitTransaction();
    }

    log.debug("Updated " + pageCount + " pages of products with average " + avg
        + " milliSeconds per page and 25 products per page");
  }
}