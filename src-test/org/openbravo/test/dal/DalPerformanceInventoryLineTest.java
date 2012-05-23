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

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.test.base.BaseTest;

/**
 * Does some simple performance tests by creating sets of {@link InventoryCount} and
 * {@link InventoryCountLine} objects and then reading and updating them.
 * 
 * @author mtaal
 */

public class DalPerformanceInventoryLineTest extends BaseTest {

  private static final Logger log = Logger.getLogger(DalPerformanceInventoryLineTest.class);

  // increase this number to make it a real performance test
  private static final int NO_HEADER = 50;
  private static final int NO_LINE = 10;
  private static String NAME_PREFIX = "" + System.currentTimeMillis();

  /**
   * Creates {@link #NO_HEADER} {@link InventoryCount} objects and for each of them {@link #NO_LINE}
   * {@link InventoryCountLine} objects. These objects are stored in the database and the timing is
   * reported.
   */
  public void testACreateInventoryLine() {
    // This test is currently disabled because it didn't work with the new Openbravo demo data
    // More info can be found here: https://issues.openbravo.com/view.php?id=20264
    if (1 == 1)
      return;
    setTestUserContext();

    // make sure our user can do this addReadWriteAccess(InventoryCount.class);
    addReadWriteAccess(InventoryCountLine.class);

    final OBCriteria<InventoryCount> icObc = OBDal.getInstance().createCriteria(
        InventoryCount.class);
    icObc.setFirstResult(1);
    icObc.setMaxResults(1);
    icObc.addOrderBy("id", false);
    final InventoryCount baseIc = (InventoryCount) DalUtil.copy(icObc.list().get(0), false);

    final OBCriteria<InventoryCountLine> iclObc = OBDal.getInstance().createCriteria(
        InventoryCountLine.class);
    iclObc.setFirstResult(1);
    iclObc.setMaxResults(1);
    final InventoryCountLine baseLine = (InventoryCountLine) DalUtil.copy(iclObc.list().get(0),
        false);
    final long time = System.currentTimeMillis();
    commitTransaction();
    for (int i = 0; i < NO_HEADER; i++) {
      final InventoryCount ic = (InventoryCount) DalUtil.copy(baseIc, false);
      ic.setPosted("N");
      ic.setProcessed(false);
      ic.setName(NAME_PREFIX + "_" + i);
      for (int j = 0; j < NO_LINE; j++) {
        final InventoryCountLine icl = (InventoryCountLine) DalUtil.copy(baseLine, false);
        icl.setPhysInventory(ic);
        icl.setLineNo((long) j);
        ic.getMaterialMgmtInventoryCountLineList().add(icl);
      }
      OBDal.getInstance().save(ic);
    }
    commitTransaction();
    log.debug("Created " + NO_HEADER + " inventorycounts and " + (NO_HEADER * NO_LINE)
        + " inventory lines" + " in " + (System.currentTimeMillis() - time) + " milliseconds");
  }

  /**
   * Reads the {@link InventoryCountLine} objects created in the above tests and adds one new line
   * and updates one line. The timings are reported in the log.
   */

  public void testBReadAndAddLine() {
    // This test is currently disabled because it didn't work with the new Openbravo demo data
    // More info can be found here: https://issues.openbravo.com/view.php?id=20264
    if (1 == 1)
      return;
    setTestUserContext(); // make sure our user can do this
    addReadWriteAccess(InventoryCount.class);
    addReadWriteAccess(InventoryCountLine.class);

    final OBCriteria<InventoryCountLine> iclObc = OBDal.getInstance().createCriteria(
        InventoryCountLine.class);
    iclObc.setFirstResult(1);
    iclObc.setMaxResults(1);
    final InventoryCountLine baseLine = (InventoryCountLine) DalUtil.copy(iclObc.list().get(0),
        false);
    final long time = System.currentTimeMillis();
    Hibernate.initialize(baseLine.getUpdatedBy());
    Hibernate.initialize(baseLine.getCreatedBy());
    commitTransaction();

    final OBCriteria<InventoryCount> icObc = OBDal.getInstance().createCriteria(
        InventoryCount.class);
    icObc.add(Restrictions.like("name", NAME_PREFIX + "%"));
    int cnt = 0;
    int cntLine = 0;
    for (final InventoryCount ic : icObc.list()) {
      cnt++;
      final InventoryCountLine icl = (InventoryCountLine) DalUtil.copy(baseLine, false);
      icl.setPhysInventory(ic);
      icl.setLineNo((long) (ic.getMaterialMgmtInventoryCountLineList().size() + 1));
      ic.getMaterialMgmtInventoryCountLineList().add(icl);

      cntLine = ic.getMaterialMgmtInventoryCountLineList().size();

      icl.setDescription("desc " + ic.getName());
      final InventoryCountLine icl2 = ic.getMaterialMgmtInventoryCountLineList().get(0);
      icl2.setQuantityOrderBook(new BigDecimal((icl2.getQuantityOrderBook() == null ? 0f : icl2
          .getQuantityOrderBook().floatValue() + 1f)));
      OBDal.getInstance().save(ic);
    }
    commitTransaction();
    log.debug("Read " + cnt + " inventorycounts with each " + cntLine
        + " inventory lines and added one new line and updated one line in "
        + (System.currentTimeMillis() - time) + " milliseconds");
  }

  /**
   * Removes the created {@link InventoryCount} and {@link InventoryCountLine} records.
   */
  public void testZCleanUp() {
    addReadWriteAccess(InventoryCount.class);
    addReadWriteAccess(InventoryCountLine.class);
    final OBCriteria<InventoryCount> icObc = OBDal.getInstance().createCriteria(
        InventoryCount.class);
    icObc.add(Restrictions.like("name", NAME_PREFIX + "%"));
    for (InventoryCount ic : icObc.list()) {
      OBDal.getInstance().remove(ic);
    }
  }
}