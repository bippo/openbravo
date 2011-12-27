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

package org.openbravo.erpCommon.info;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.test.base.BaseTest;

/**
 * Tests consistency between select and countRows methods of the (old) selectors in erpCommon/info.
 * 
 * Test-class is in this package, to allow access to non-public selector data classes.
 * 
 * @author huehner
 */
public class ClassicSelectorTest extends BaseTest {

  /**
   * Test BusinessPartnerMultiple selector.
   */
  public void testBpartnerMultiple() throws Exception {

    String client = "'" + TEST_CLIENT_ID + "'";
    String org = "'0','" + TEST_ORG_ID + "'";
    String key = "";
    String name = "";
    String contact = "";
    String postCode = "";
    String province = "";
    String clients = "";
    String vendors = "";
    String ciudad = "";
    String orderBy = "1";
    checkBpartnerMultiple(client, org, key, name, contact, postCode, province, clients, vendors,
        ciudad, orderBy);
  }

  private void checkBpartnerMultiple(String client, String org, String key, String name,
      String contact, String postCode, String province, String clients, String vendors,
      String ciudad, String orderBy) throws ServletException {
    String rownum = "0", oraLimit1 = null, oraLimit2 = null, pgLimit = null;
    long offset = 0;
    if (getConnectionProvider().getRDBMS().equalsIgnoreCase("ORACLE")) {
      oraLimit1 = String.valueOf(offset + TableSQLData.maxRowsPerGridPage);
      oraLimit2 = (offset + 1) + " AND " + oraLimit1;
      rownum = "ROWNUM";
    } else {
      pgLimit = TableSQLData.maxRowsPerGridPage + " OFFSET " + offset;
    }

    BusinessPartnerMultipleData data[] = BusinessPartnerMultipleData.select(
        getConnectionProvider(), rownum, client, org, key, name, contact, postCode, province,
        clients, vendors, ciudad, orderBy, pgLimit, oraLimit1, oraLimit2);
    String strCount = BusinessPartnerMultipleData.countRows(getConnectionProvider(), rownum,
        client, org, key, name, contact, postCode, province, clients, vendors, ciudad, pgLimit,
        oraLimit1, oraLimit2);
    long count = Long.valueOf(strCount);

    // check implicit consistency requirement: both select & selectCount methods must agree on
    // number of records available in the selector
    assertEquals(count, data.length);

  }

}