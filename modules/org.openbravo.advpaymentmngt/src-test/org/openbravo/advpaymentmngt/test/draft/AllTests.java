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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.advpaymentmngt.test.draft;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Test for org.openbravo.advpaymentmngt");
    // $JUnit-BEGIN$

    // Master Data Configuration
    suite.addTestSuite(FinancialAccountTest.class);
    suite.addTestSuite(PaymentMethodTest.class);

    // Payment scenarios
    suite.addTestSuite(PaymentTest_01.class);
    suite.addTestSuite(PaymentTest_02.class);
    suite.addTestSuite(PaymentTest_03.class);
    suite.addTestSuite(PaymentTest_04.class);
    suite.addTestSuite(PaymentTest_05.class);
    suite.addTestSuite(PaymentTest_06.class);
    suite.addTestSuite(PaymentTest_07.class);
    suite.addTestSuite(PaymentTest_08.class);
    suite.addTestSuite(PaymentTest_09.class);
    suite.addTestSuite(PaymentTest_10.class);
    suite.addTestSuite(PaymentTest_11.class);

    // $JUnit-END$
    return suite;
  }

}
