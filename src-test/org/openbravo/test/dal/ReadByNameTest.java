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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.dal;

import java.util.UUID;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.test.base.BaseTest;

/**
 * Tests creating a location and then reading it again using the {@link OBCriteria} api.
 * 
 * @author iperdomo
 */
public class ReadByNameTest extends BaseTest {

  // Will hold Ids for next tests
  private static String bpId; // Business Partner Id
  private static String locId; // Location Id
  private static String locName; // Location name

  public void testCreateBP() {

    setTestUserContext();
    addReadWriteAccess(BusinessPartner.class);
    addReadWriteAccess(Location.class);
    addReadWriteAccess(Category.class);

    BusinessPartner bp = OBProvider.getInstance().get(BusinessPartner.class);

    // Generating random strings for testing
    UUID name = UUID.randomUUID();
    UUID key = UUID.randomUUID();

    bp.setName(name.toString());
    bp.setSearchKey(key.toString());

    Category c = OBDal.getInstance().get(Category.class, TEST_BP_CATEGORY_ID); // Standard

    bp.setBusinessPartnerCategory(c);

    OBDal.getInstance().save(bp);
    OBDal.getInstance().flush();
    bpId = bp.getId();
  }

  public void testAddLocation() {

    setTestUserContext();
    addReadWriteAccess(BusinessPartner.class);
    addReadWriteAccess(org.openbravo.model.common.geography.Location.class);
    addReadWriteAccess(org.openbravo.model.common.businesspartner.Location.class);

    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bpId);

    assertNotNull(bp);

    // Getting an existent Location
    org.openbravo.model.common.geography.Location loc = OBDal.getInstance().get(
        org.openbravo.model.common.geography.Location.class, TEST_LOCATION_ID);

    org.openbravo.model.common.businesspartner.Location bpLoc = OBProvider.getInstance().get(
        org.openbravo.model.common.businesspartner.Location.class);

    // Generating a location name
    locName = UUID.randomUUID().toString();

    bpLoc.setName(locName);
    bpLoc.setLocationAddress(loc);
    bpLoc.setBusinessPartner(bp);
    bpLoc.setInvoiceToAddress(false);
    bpLoc.setPayFromAddress(false);
    bpLoc.setRemitToAddress(false);
    bpLoc.setShipToAddress(false);

    OBDal.getInstance().save(bpLoc);
    OBDal.getInstance().flush();

    locId = bpLoc.getId();
  }

  public void testFindLocation() {
    // tests have run in the correct order
    assertNotNull(locName);
    assertNotNull(locId);

    // Search for a BPLocation if you don't have the Id
    setTestUserContext();
    addReadWriteAccess(BusinessPartner.class);
    addReadWriteAccess(org.openbravo.model.common.geography.Location.class);
    addReadWriteAccess(org.openbravo.model.common.businesspartner.Location.class);

    OBCriteria<org.openbravo.model.common.businesspartner.Location> obc = OBDal.getInstance()
        .createCriteria(org.openbravo.model.common.businesspartner.Location.class);

    obc.add(Restrictions.eq(org.openbravo.model.common.businesspartner.Location.PROPERTY_NAME,
        locName));

    assertFalse(obc.list().isEmpty());

    final org.openbravo.model.common.businesspartner.Location tmpLoc = obc.list().get(0);

    assertEquals(locId, tmpLoc.getId());
    assertEquals(locName, tmpLoc.getName());
  }

  public void testPBData() {

    setTestUserContext();
    addReadWriteAccess(BusinessPartner.class);
    addReadWriteAccess(org.openbravo.model.common.businesspartner.Location.class);

    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bpId);
    assertTrue(bp.getBusinessPartnerLocationList().size() > 0);
    for (org.openbravo.model.common.businesspartner.Location loc : bp
        .getBusinessPartnerLocationList()) {
      System.out.println(loc);
      System.out.println(loc.getLocationAddress());
    }
    setTestAdminContext();
    OBDal.getInstance().remove(bp);

    assertNull(OBDal.getInstance().get(BusinessPartner.class, bpId));
    assertNull(OBDal.getInstance().get(Location.class, locId));
  }
}
