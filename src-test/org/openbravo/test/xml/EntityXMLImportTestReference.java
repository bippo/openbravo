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

package org.openbravo.test.xml;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.ReferenceDataStore;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.geography.Location;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * Test import of data for a business object (in this case {@link Warehouse} and its referenced
 * information. A warehouse refers to a {@link Location}, the export exports both the warehouses as
 * their locations. Then the tests check that both are inserted and then when the warehouses are
 * reimported that the locations previously imported are re-used. Note that this test also tests the
 * use of the {@link ReferenceDataStore} which is used to map id's used in the xml to the eventual
 * id of the imported object in the database.
 * 
 * @author mtaal
 */

public class EntityXMLImportTestReference extends XMLBaseTest {

  /**
   * Number of Warehouse in client in test client
   */
  private int numberOfWarehouses = 0;

  /**
   * Number of Locations associated to warehouses
   */
  private int numberOfLocations = 0;

  private List<String> warehouseNames = new ArrayList<String>();
  private List<String> locationAddresses = new ArrayList<String>();

  @Override
  protected void setUp() throws Exception {
    Location l = null;
    super.setUp();
    setTestUserContext();
    OBCriteria<Warehouse> obc = OBDal.getInstance().createCriteria(Warehouse.class);
    obc.add(Restrictions.eq(Warehouse.PROPERTY_ORGANIZATION,
        OBDal.getInstance().get(Organization.class, TEST_US_ORG_ID)));
    numberOfWarehouses = obc.count();
    for (Warehouse w : obc.list()) {
      warehouseNames.add(w.getName());
      if (!w.getLocationAddress().equals(l)) {
        l = w.getLocationAddress();
        locationAddresses.add(w.getLocationAddress().getAddressLine1());
        numberOfLocations++;
      }
    }
  }

  /**
   * Exports {@link Warehouse} objects from client/org {@link #TEST_CLIENT_ID} and imports them in
   * {@link #QA_TEST_CLIENT_ID}. Also the referenced {@link Location} objects are imported.
   */
  public void test1Warehouse() {
    // This test is currently disabled because it didn't work with the new Openbravo demo data
    // More info can be found here: https://issues.openbravo.com/view.php?id=20264
    if (1 == 1)
      return;
    cleanRefDataLoaded();
    setTestUserContext();
    addReadWriteAccess(Warehouse.class);

    final String xml = getXML(Warehouse.class,
        OBDal.getInstance().get(Organization.class, TEST_US_ORG_ID));

    setUserContext(QA_TEST_ADMIN_USER_ID);
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, QA_TEST_CLIENT_ID),
        OBDal.getInstance().get(Organization.class, QA_TEST_ORG_ID), xml);
    if (ir.getException() != null) {
      ir.getException().printStackTrace(System.err);
      fail(ir.getException().getMessage());
    } else if (ir.getErrorMessages() != null) {
      fail(ir.getErrorMessages());
    } else {
      assertEquals(numberOfWarehouses + numberOfLocations, ir.getInsertedObjects().size());
      assertEquals(0, ir.getUpdatedObjects().size());
    }
    if (ir.hasErrorOccured()) {
      fail(ir.getErrorMessages());
    }
  }

  /**
   * Remove the imported {@link Warehouse} objects from the {@link #QA_TEST_CLIENT_ID} client. The
   * imported {@link Location} objects should still remain.
   */
  public void test2Warehouse() {
    // This test is currently disabled because it didn't work with the new Openbravo demo data
    // More info can be found here: https://issues.openbravo.com/view.php?id=20264
    if (1 == 1)
      return;
    setUserContext(QA_TEST_ADMIN_USER_ID);
    // a warehouse is not deletable, but as we are cleaning up, they should be
    // deleted, force this by being admin
    OBContext.setAdminMode();
    try {
      removeAll(Warehouse.class, numberOfWarehouses,
          Restrictions.in(Warehouse.PROPERTY_NAME, warehouseNames));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Repeat the action of importing the {@link Warehouse} objects from {@link #TEST_CLIENT_ID}. Now
   * the {@link Location} objects are also exported but not imported as they already exist in
   * {@link #QA_TEST_CLIENT_ID}.
   */
  public void test3Warehouse() {
    // This test is currently disabled because it didn't work with the new Openbravo demo data
    // More info can be found here: https://issues.openbravo.com/view.php?id=20264
    if (1 == 1)
      return;
    setTestUserContext();
    addReadWriteAccess(Warehouse.class);
    final String xml = getXML(Warehouse.class,
        OBDal.getInstance().get(Organization.class, TEST_US_ORG_ID));
    setUserContext(QA_TEST_ADMIN_USER_ID);
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, QA_TEST_CLIENT_ID),
        OBDal.getInstance().get(Organization.class, QA_TEST_ORG_ID), xml);
    if (ir.getException() != null) {
      ir.getException().printStackTrace(System.err);
      fail(ir.getException().getMessage());
    } else if (ir.getErrorMessages() != null) {
      fail(ir.getErrorMessages());
    }
    assertEquals(numberOfWarehouses, ir.getInsertedObjects().size());
    for (final BaseOBObject bob : ir.getInsertedObjects()) {
      assertTrue(bob instanceof Warehouse);
    }
    assertEquals(0, ir.getUpdatedObjects().size());
    if (ir.hasErrorOccured()) {
      fail(ir.getErrorMessages());
    }
  }

  /**
   * Clean up by removing both the imported {@link Warehouse} and {@link Location} objects from
   * {@link #QA_TEST_CLIENT_ID} client.
   */
  public void test4Warehouse() {
    // This test is currently disabled because it didn't work with the new Openbravo demo data
    // More info can be found here: https://issues.openbravo.com/view.php?id=20264
    if (1 == 1)
      return;
    setUserContext(QA_TEST_ADMIN_USER_ID);
    // a warehouse is not deletable, but as we are cleaning up, they should be
    // deleted, force this by being admin
    OBContext.setAdminMode();
    try {
      removeAll(Warehouse.class, numberOfWarehouses,
          Restrictions.in(Warehouse.PROPERTY_NAME, warehouseNames));
      removeAll(Location.class, numberOfLocations,
          Restrictions.in(Location.PROPERTY_ADDRESSLINE1, locationAddresses));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private <T extends BaseOBObject> void removeAll(Class<T> clz, int expectCount, Criterion c) {
    final Criteria criteria = SessionHandler.getInstance().getSession().createCriteria(clz);
    if (c != null) {
      criteria.add(c);
    }
    criteria.add(Restrictions.eq("client.id", QA_TEST_CLIENT_ID));

    @SuppressWarnings("unchecked")
    final List<T> list = criteria.list();
    if (expectCount != -1) {
      assertEquals(expectCount, list.size());
    }
    for (final T t : list) {
      SessionHandler.getInstance().getSession().delete(t);
    }
  }
}