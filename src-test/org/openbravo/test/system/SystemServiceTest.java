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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.system;

import java.util.Date;
import java.util.List;

import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.service.dataset.DataSetService;
import org.openbravo.service.system.SystemService;
import org.openbravo.test.base.BaseTest;

/**
 * Test the {@link SystemService} class.
 * 
 * @author mtaal
 */

public class SystemServiceTest extends BaseTest {
  private static final long ONEDAY = 1000 * 60 * 60 * 24;

  /**
   * Test the {@link DataSetService#hasChanged(DataSet, Date)} method.
   */
  public void testChangedDataSet() {
    setSystemAdministratorContext();

    final List<DataSet> dss = OBDal.getInstance().createCriteria(DataSet.class).list();
    // check one day in the future to prevent date/time rounding issues
    final Date tomorrow = new Date(System.currentTimeMillis() + ONEDAY);
    for (DataSet ds : dss) {
      assertFalse("Fails on dataset " + ds.getName() + " checking date " + tomorrow, DataSetService
          .getInstance().hasChanged(ds, tomorrow));
    }

    // 600 days in the past
    final long manyDays = (long) 600 * ONEDAY;
    final Date past = new Date(System.currentTimeMillis() - manyDays);
    for (DataSet ds : dss) {
      if (!DataSetService.getInstance().hasData(ds)) {
        continue;
      }
      assertTrue("Fails on dataset " + ds.getName() + " checking date " + past, DataSetService
          .getInstance().hasChanged(ds, past));
    }
  }

  /**
   * Tests the {@link SystemService#hasChanged(Class[], Date)} method which is used to check if an
   * object in a specific table have changed since a specific time.
   */
  public void testChangedClasses() {
    setSystemAdministratorContext();
    final Class<?>[] clzs = new Class<?>[] { Table.class, Column.class, Reference.class };

    final Date tomorrow = new Date(System.currentTimeMillis() + ONEDAY);
    assertFalse(SystemService.getInstance().hasChanged(clzs, tomorrow));

    // 600 days in the past
    final Date past = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 600));
    assertTrue(SystemService.getInstance().hasChanged(clzs, past));
  }
}