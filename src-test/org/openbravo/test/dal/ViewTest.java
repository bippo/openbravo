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

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.test.base.BaseTest;

/**
 * Test if views work properly
 * 
 * @author mtaal
 */
public class ViewTest extends BaseTest {

  /**
   * Iterates over all views
   */
  public void testViews() {
    setTestAdminContext();
    int cnt = 0;
    for (Entity entity : ModelProvider.getInstance().getModel()) {
      if (entity.isView()) {
        for (BaseOBObject bob : OBDal.getInstance().createQuery(entity.getName(), "").list()) {
          assertTrue(bob.getEntity() == entity);
          cnt++;
        }
      }
    }
    assertTrue(cnt > 0);
    System.err.println(cnt);
  }

  /**
   * Tests issue https://issues.openbravo.com/view.php?id=14914 that view objects are not copied.
   */
  public void test14914() {
    setTestUserContext();
    OBContext.setAdminMode();
    boolean testDone = false;
    try {
      for (Invoice o : OBDal.getInstance().createQuery(Invoice.class, "").list()) {
        if (!o.getFINPaymentSchedInvVList().isEmpty()) {
          final Invoice copied = (Invoice) DalUtil.copy(o);
          assertTrue(copied.getFINPaymentSchedInvVList().isEmpty());
          testDone = true;
        }
      }
      assertTrue(testDone);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}