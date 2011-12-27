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

import org.openbravo.dal.core.OBContext;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the usage of the {@link OBContext#setAdminMode()} and
 * {@link OBContext#restorePreviousMode()}.
 * 
 * See these issues:
 * 
 * - https://issues.openbravo.com/view.php?id=12594: Make setting of administrator mode less
 * vulnerable for wrong usage
 * 
 * - https://issues.openbravo.com/view.php?id=12660: OBContext enableAsAdminContext -
 * resetAsAdminContext should use an stack
 * 
 * @author mtaal
 */

public class AdminContextTest extends BaseTest {

  /**
   * Test a single call to the admin context setting.
   */
  public void testSingleAdminContextCall() {
    setTestUserContext();
    assertFalse(OBContext.getOBContext().isInAdministratorMode());
    OBContext.setAdminMode();
    assertTrue(OBContext.getOBContext().isInAdministratorMode());
    OBContext.restorePreviousMode();
    assertFalse(OBContext.getOBContext().isInAdministratorMode());
  }

  /**
   * Test multiple nested calls to setting and reseting admin context.
   */
  public void testMultipleAdminContextCall() {
    setTestUserContext();
    assertFalse(OBContext.getOBContext().isInAdministratorMode());
    OBContext.setAdminMode();
    assertTrue(OBContext.getOBContext().isInAdministratorMode());

    {
      OBContext.setAdminMode();
      assertTrue(OBContext.getOBContext().isInAdministratorMode());

      {
        OBContext.setAdminMode();
        assertTrue(OBContext.getOBContext().isInAdministratorMode());

        OBContext.restorePreviousMode();
        assertTrue(OBContext.getOBContext().isInAdministratorMode());
      }

      OBContext.restorePreviousMode();
      assertTrue(OBContext.getOBContext().isInAdministratorMode());
    }

    OBContext.restorePreviousMode();
    assertFalse(OBContext.getOBContext().isInAdministratorMode());
  }
}