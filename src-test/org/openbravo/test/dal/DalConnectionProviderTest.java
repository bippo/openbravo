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

package org.openbravo.test.dal;

import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the {@link DalConnectionProvider}.
 * 
 * @author mtaal
 */

public class DalConnectionProviderTest extends BaseTest {
  // private static final Logger log = Logger.getLogger(DalConnectionProviderTest.class);

  /**
   * Tests calling database procedures using the dal connection provider
   */
  public void testDalConnectionProvider() throws Exception {
    setSystemAdministratorContext();

    final DalConnectionProvider connectionProvider = new DalConnectionProvider();
    // get the current date, this already uses the connectionprovider
    final String currentDateStr = DateTimeData.today(connectionProvider);

    // now compute a tax for a certain business partner using the date string
    // by passing the date str the check is done that the dbSessionConfig has been
    // executed. See this issue:
    // https://issues.openbravo.com/view.php?id=10330
    String productId = "1000004";
    String orgId = "1000002";
    String whId = "1000000";
    String bpLocId = "1000001";
    String shipPartnLocId = "1000001";
    String projectId = null;

    // just call, the return value is not important, it should not fail.
    Tax.get(connectionProvider, productId, currentDateStr, orgId, whId, bpLocId, shipPartnLocId,
        projectId, true);
  }
}