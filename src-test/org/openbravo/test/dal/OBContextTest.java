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

package org.openbravo.test.dal;

import org.openbravo.dal.core.OBContext;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the {@link OBContext} class.
 * 
 * @author mtaal
 */

public class OBContextTest extends BaseTest {

  /**
   * Tests if the warehouse is set correctly in the OBContext.
   */
  public void testWarehouseInContext() {
    OBContext.setOBContext("100", "0", TEST_CLIENT_ID, TEST_ORG_ID, null, TEST_WAREHOUSE_ID);
    assertTrue(OBContext.getOBContext().getWarehouse().getId().equals(TEST_WAREHOUSE_ID));
  }

  /**
   * Tests if the language is set correctly in the OBContext.
   */
  public void testLanguageInContext() {
    OBContext.setOBContext("100", "0", TEST_CLIENT_ID, TEST_ORG_ID, "en_US");
    assertTrue(OBContext.getOBContext().getLanguage().getId().equals("192"));
  }

  /**
   * Tests if the {@link OBContext#setAdminMode()} and {@link OBContext#restorePreviousMode()} work
   * correctly if the same OBContext is used by multiple threads. This is possible in case of
   * simultaneous ajax requests.
   * 
   * See: https://issues.openbravo.com/view.php?id=8853
   */
  public void testMultiThreadedOBContext() throws Exception {
    setTestUserContext();
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    final LocalThread t1 = new LocalThread();
    t1.setName("t1");
    final LocalThread t2 = new LocalThread();
    t2.setName("t2");
    t1.setPriority(Thread.MAX_PRIORITY);
    t2.setPriority(Thread.MAX_PRIORITY);

    // they all share the same obcontext
    t1.setLocalOBContext(OBContext.getOBContext());
    t2.setLocalOBContext(OBContext.getOBContext());

    // also tests if this thread influences the other two!
    // main thread is true
    OBContext.setAdminMode();

    try {
      t1.start();
      t2.start();

      // subthreads should have false
      assertFalse(t1.isAdminMode());
      assertFalse(t2.isAdminMode());

      assertTrue(OBContext.getOBContext().isInAdministratorMode());

      // t1 moves to the next phase
      long cnt = 0;
      t1.setFirstStep(true);
      while (!t1.isFirstStepDone()) {
        cnt++;
        Thread.sleep(5);
        // some thing to prevent infinite loops
        assertTrue(cnt < 1000000000);
      }
      // t1 in admin mode, t2 not
      assertFalse(t1.isPrevMode());
      assertTrue(t1.isAdminMode());
      assertFalse(t2.isAdminMode());

      assertTrue(OBContext.getOBContext().isInAdministratorMode());

      // let t2 do the first step
      t2.setFirstStep(true);
      cnt = 0;
      while (!t2.isFirstStepDone()) {
        cnt++;
        Thread.sleep(5);
        // some thing to prevent infinite loops
        assertTrue(cnt < 1000000000);
      }
      // second one should encounter adminmode = false as it is a different thread;
      // both t1 and t2 in admin mode
      assertFalse(t2.isPrevMode());
      assertTrue(t2.isAdminMode());
      assertTrue(t1.isAdminMode());

      assertTrue(OBContext.getOBContext().isInAdministratorMode());

      // move t1 to the next step
      t1.setNextStep(true);
      cnt = 0;
      while (!t1.isNextStepDone()) {
        cnt++;
        Thread.sleep(5);
        // some thing to prevent infinite loops
        assertTrue(cnt < 1000000000);
      }
      // t1 not in admin mode, t2 in admin mode still
      assertFalse(t1.isAdminMode());
      assertTrue(t2.isAdminMode());

      assertTrue(OBContext.getOBContext().isInAdministratorMode());

      // now move t2
      t2.setNextStep(true);
      cnt = 0;
      while (!t2.isNextStepDone()) {
        cnt++;
        Thread.sleep(5);
        // some thing to prevent infinite loops
        assertTrue(cnt < 1000000000);
      }
      // t2 not anymore in admin mode
      assertFalse(t2.isAdminMode());
      assertTrue(OBContext.getOBContext().isInAdministratorMode());
    } finally {
      // ensure that the threads stop
      t1.setFirstStep(true);
      t2.setFirstStep(true);
      t1.setNextStep(true);
      t2.setNextStep(true);
      OBContext.restorePreviousMode();
    }
  }

  /**
   * See issue: https://issues.openbravo.com/view.php?id=13572 Maintain and print stacktraces when
   * calls to setAdminMode and restoreAdminMode are unbalanced
   * 
   * To test this issue set the OBContext.ADMIN_TRACE_SIZE to a higher value than 0
   */
  public void testUnbalancedCallsToAdminMode() {
    OBContext.setAdminMode();
    OBContext.setAdminMode();
    OBContext.setAdminMode();
    OBContext.restorePreviousMode();
    OBContext.restorePreviousMode();
    OBContext.restorePreviousMode();
    OBContext.restorePreviousMode();
  }

  // the scenario:
  // thread1 T1
  // thread2 T2
  // T2: setInAdminMode(true)
  // T1: setInAdminMode(true)
  // T2: restorePrevAdminMode --> sets admin mode to false
  // T1: fails because adminmode is false

  private class LocalThread extends Thread {

    private boolean firstStep = false;
    private boolean firstStepDone = false;
    private boolean nextStep = false;
    private boolean nextStepDone = false;
    private boolean adminMode = false; // start with true
    private boolean prevMode = false;

    private OBContext localOBContext;

    @Override
    public void run() {
      OBContext.setOBContext(getLocalOBContext());

      try {
        while (!firstStep) {
          adminMode = OBContext.getOBContext().isInAdministratorMode();
        }
        OBContext.setAdminMode();
        adminMode = OBContext.getOBContext().isInAdministratorMode();
        firstStepDone = true;
        while (!nextStep) {
          adminMode = OBContext.getOBContext().isInAdministratorMode();
        }
        OBContext.restorePreviousMode();
        adminMode = OBContext.getOBContext().isInAdministratorMode();
        nextStepDone = true;
      } catch (Exception e) {
        e.printStackTrace(System.err);
        throw new IllegalStateException(e);
      }
    }

    public void setNextStep(boolean nextStep) {
      this.nextStep = nextStep;
    }

    public boolean isAdminMode() {
      return adminMode;
    }

    public void setFirstStep(boolean firstStep) {
      this.firstStep = firstStep;
    }

    public boolean isPrevMode() {
      return prevMode;
    }

    public boolean isFirstStepDone() {
      return firstStepDone;
    }

    public boolean isNextStepDone() {
      return nextStepDone;
    }

    public OBContext getLocalOBContext() {
      return localOBContext;
    }

    public void setLocalOBContext(OBContext localOBContext) {
      this.localOBContext = localOBContext;
    }

  }
}