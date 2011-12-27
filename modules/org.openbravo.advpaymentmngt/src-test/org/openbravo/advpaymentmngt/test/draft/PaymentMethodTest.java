/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.test.draft;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.test.base.BaseTest;

public class PaymentMethodTest extends BaseTest {

  private static final String AUTOMATIC_EXECUTION = "A";
  private static final String MANUAL_EXECUTION = "M";
  private static final String CLEARED_ACCOUNT = "CLE";
  private static final String IN_TRANSIT_ACCOUNT = "INT";
  private static final String WITHDRAWN_ACCOUNT = "WIT";
  private static final String DEPOSIT_ACCOUNT = "DEP";
  private static final String STANDARD_DESCRIPTION = "JUnit Test";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TestUtility.setTestContext();
  }

  public void testAddPaymentMethodValid1() {
    TestUtility.insertPaymentMethod("APRM_PAYMENT_METHOD_1", STANDARD_DESCRIPTION, true, false,
        false, MANUAL_EXECUTION, null, false, IN_TRANSIT_ACCOUNT, DEPOSIT_ACCOUNT, CLEARED_ACCOUNT,
        true, false, false, MANUAL_EXECUTION, null, false, IN_TRANSIT_ACCOUNT, WITHDRAWN_ACCOUNT,
        CLEARED_ACCOUNT, true, false);
  }

  public void testAddPaymentMethodValid2() {
    TestUtility.insertPaymentMethod("APRM_PAYMENT_METHOD_2", STANDARD_DESCRIPTION, true, false,
        false, AUTOMATIC_EXECUTION, null, false, IN_TRANSIT_ACCOUNT, DEPOSIT_ACCOUNT,
        CLEARED_ACCOUNT, true, false, false, AUTOMATIC_EXECUTION, null, false, IN_TRANSIT_ACCOUNT,
        WITHDRAWN_ACCOUNT, CLEARED_ACCOUNT, true, false);
  }

  public void testAddPaymentMethodValid3() {
    TestUtility.insertPaymentMethod("APRM_PAYMENT_METHOD_3", STANDARD_DESCRIPTION, true, false,
        false, MANUAL_EXECUTION, null, false, null, null, null, true, false, false,
        MANUAL_EXECUTION, null, false, null, null, null, true, false);
  }

  public void testAddPaymentMethodValid4() {
    TestUtility.insertPaymentMethod("APRM_PAYMENT_METHOD_4", STANDARD_DESCRIPTION, true, true,
        true, MANUAL_EXECUTION, null, false, null, null, null, true, true, true, MANUAL_EXECUTION,
        null, false, null, null, null, true, false);
  }

  // Requisite: at least one Execution Process created
  public void testAddPaymentMethodValid5() {
    TestUtility.insertPaymentMethod("APRM_PAYMENT_METHOD_5", STANDARD_DESCRIPTION, true, false,
        false, AUTOMATIC_EXECUTION, /* getOneInstance(PaymentExecutionProcess.class) */null, false,
        IN_TRANSIT_ACCOUNT, DEPOSIT_ACCOUNT, CLEARED_ACCOUNT, true, false, false,
        AUTOMATIC_EXECUTION,
        /* getOneInstance(PaymentExecutionProcess.class) */null, false, IN_TRANSIT_ACCOUNT,
        WITHDRAWN_ACCOUNT, CLEARED_ACCOUNT, true, false);
  }

  /**
   * Deletes all the Payment Methods created for testing
   */
  public void testDeletePaymentMethod() {
    final OBCriteria<FIN_PaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
        FIN_PaymentMethod.class);
    obCriteria.add(Restrictions.eq(FIN_PaymentMethod.PROPERTY_DESCRIPTION, STANDARD_DESCRIPTION));
    final List<FIN_PaymentMethod> paymentMethods = obCriteria.list();
    for (FIN_PaymentMethod pm : paymentMethods) {
      OBDal.getInstance().remove(pm);
    }
  }

}