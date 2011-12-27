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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openbravo.test.webservice.PerformanceTest;
import org.openbravo.test.webservice.WSReadTest;
import org.openbravo.test.webservice.WSUpdateTest;

/**
 * This test suite should only contain test cases which are to run the weservices included in core.
 * 
 * 
 */
public class AllWebserviceTests {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Test for org.openbravo.test.dal");
    // $JUnit-BEGIN$

    suite.addTestSuite(WSReadTest.class);
    suite.addTestSuite(WSUpdateTest.class);
    suite.addTestSuite(PerformanceTest.class);

    // $JUnit-END$
    return suite;
  }

}
