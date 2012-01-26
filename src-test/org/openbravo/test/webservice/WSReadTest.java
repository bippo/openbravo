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

package org.openbravo.test.webservice;

import java.net.URLEncoder;

import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;

/**
 * Test the DAL rest webservices in read-mode. The test cases here require that there is a running
 * Openbravo at http://localhost:8080/openbravo
 * 
 * @author mtaal
 */

public class WSReadTest extends BaseWSTest {

  /**
   * Tests retrieval of the XML Schema defining the REST webservice.
   * 
   * @throws Exception
   */
  public void testSchemaWebService() throws Exception {
    doTestGetRequest("/ws/dal/schema", "<xs:element name=\"Openbravo\">", 200, false);
  }

  /**
   * Tests a special web service which lists all Entities (types) in the system.
   * 
   * @throws Exception
   */
  public void testTypesWebService() throws Exception {
    doTestGetRequest("/ws/dal", "<Types>", 200, false);
  }

  /**
   * Queries for a few {@link Table} objects using a REST call with a whereclause.
   * 
   * @throws Exception
   */
  public void testWhereClause() throws Exception {
    String whereClause = "(table.id='104' or table.id='105') and isKey='Y'";
    whereClause = URLEncoder.encode(whereClause, "UTF-8");
    final String content = doTestGetRequest("/ws/dal/ADColumn?where=" + whereClause, "<ADColumn",
        200);

    // there should be two columns
    final int index1 = content.indexOf("<ADColumn");
    assertTrue(index1 != -1);
    final int index2 = content.indexOf("<ADColumn", index1 + 2);
    assertTrue(index2 != -1);
    final int index3 = content.indexOf("<ADColumn", index2 + 2);
    assertTrue(index3 == -1);
  }

  /**
   * Performs a number of paged queries.
   * 
   * @throws Exception
   */
  public void testPagedWhereClause() throws Exception {
    setTestAdminContext();
    requestColumnPage(1, 10);
    requestColumnPage(1, 5);
    requestColumnPage(30, 5);
  }

  private void requestColumnPage(int firstResult, int maxResult) throws Exception {

    String whereClause = "(table.id='104' or table.id='105')";

    final OBQuery<Column> columns = OBDal.getInstance().createQuery(Column.class, whereClause);
    final int columnCnt = columns.count();
    final int expectedCount = ((firstResult + maxResult) < columnCnt ? maxResult
        : (columnCnt - firstResult));

    whereClause = URLEncoder.encode(whereClause, "UTF-8");
    String content = doTestGetRequest("/ws/dal/ADColumn?where=" + whereClause + "&firstResult="
        + firstResult + "&maxResult=" + maxResult, "<ADColumn", 200);

    // count the columns
    int index = content.indexOf("<ADColumn");
    int cnt = 0;
    while (index != -1) {
      cnt++;
      index = content.indexOf("<ADColumn", index + 1);
    }
    assertEquals(expectedCount, cnt);
  }

  /**
   * Calls the webservice for every readable {@link Entity} in the system. The test can take some
   * time to run (about 5 minutes).
   */
  /*
   * public void testAllToXML() { // do not replace this with a call to setUserContext,
   * OBContext.setOBContext("100"); for (Entity entity :
   * OBContext.getOBContext().getEntityAccessChecker().getReadableEntities()) {
   * doTestGetRequest("/ws/dal/" + entity.getName() + "?includeChildren=false", "<ob:Openbravo",
   * 200); } }
   */

}