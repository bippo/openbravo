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

import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.Location;
import org.openbravo.test.base.BaseTest;

/**
 * Generates many {@link Location} objects to use for testing performance.
 * 
 * @author mtaal
 */

public class PerformanceGenerateInstanceTest extends BaseTest {

  private static final int START = 1001;
  private static final long NUMBER = 1000000;
  private static final long COMMIT_COUNT = NUMBER / 10;

  public void testLocationInstanceCreation() {
    setTestAdminContext();
    final Location location = OBDal.getInstance().get(Location.class, "1000000");
    long time = System.currentTimeMillis();
    Client zeroClient = OBDal.getInstance().get(Client.class, "0");
    Organization zeroOrg = OBDal.getInstance().get(Organization.class, "0");
    for (int i = START; i < NUMBER; i++) {
      final Location copy = (Location) DalUtil.copy(location);
      copy.setId(null);
      copy.setNewOBObject(true);
      copy.setCityName("CITY " + i);
      copy.setRegionName("REGION " + i);
      copy.setPostalAdd("PA" + i);
      copy.setPostalCode("PC" + i);
      copy.setClient(zeroClient);
      copy.setOrganization(zeroOrg);
      OBDal.getInstance().save(copy);
      if ((i % COMMIT_COUNT) == 0) {
        OBDal.getInstance().commitAndClose();
        System.err.println(i + ", commit and flush, time " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        zeroClient = OBDal.getInstance().get(Client.class, "0");
        zeroOrg = OBDal.getInstance().get(Organization.class, "0");
      }
    }
  }

}