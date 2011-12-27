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

package org.openbravo.service.json.test;

import java.net.URLEncoder;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonRestServlet;
import org.openbravo.service.json.JsonToDataConverter;

/**
 * Test the {@link JsonRestServlet} using read requests.
 * 
 * @author mtaal
 */

public class JsonReadRestTest extends JsonRestTest {

  private static final String URL_PART = "/org.openbravo.service.json.jsonrest";

  /**
   * Queries for a few {@link Column} objects using a Json REST call with a whereclause.
   */
  public void testWhereClause() throws Exception {
    String whereClause = "(table.id='104' or table.id='105') and isKey='Y'";
    whereClause = URLEncoder.encode(whereClause, "UTF-8");
    final JSONObject jsonObject = doRequest(URL_PART + "/ADColumn?" + JsonConstants.WHERE_PARAMETER
        + "=" + whereClause, "_identifier", "GET", 200);
    final JSONArray jsonArray = jsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE)
        .getJSONArray(JsonConstants.DATA);
    assertEquals(2, jsonArray.length());
    final JSONObject jsonColumn1 = jsonArray.getJSONObject(0);
    final JSONObject jsonColumn2 = jsonArray.getJSONObject(1);

    setSystemAdministratorContext();
    final JsonToDataConverter converter = new JsonToDataConverter();
    final Column column1 = (Column) converter.toBaseOBObject(jsonColumn1);
    assertTrue(column1.getTable().getId().equals("104") || column1.getTable().getId().equals("105"));
    assertFalse(converter.hasErrors());
    converter.clearState();
    final Column column2 = (Column) converter.toBaseOBObject(jsonColumn2);
    assertTrue(column2.getTable().getId().equals("104") || column2.getTable().getId().equals("105"));
    assertFalse(converter.hasErrors());

    assertTrue(!column1.isNewOBObject());
    assertTrue(!column2.isNewOBObject());
  }

  /**
   * Query for one {@link Table}.
   */
  public void testRestOneObject() throws Exception {
    final JSONObject jsonObject = doRequest(URL_PART + "/ADTable/104", "_identifier", "GET", 200);

    setSystemAdministratorContext();
    final JsonToDataConverter converter = new JsonToDataConverter();
    final Table table = (Table) converter.toBaseOBObject(jsonObject);
    assertFalse(converter.hasErrors());
    assertTrue(table.getId().equals("104"));
    assertTrue(!table.isNewOBObject());
  }

  /**
   * Test filter.
   */
  public void testFilter() throws Exception {
    String filterClause = "Reference List";
    filterClause = URLEncoder.encode(filterClause, "UTF-8");
    final JSONObject jsonObject = doRequest(URL_PART + "/ADTable?description=" + filterClause,
        "_identifier", "GET", 200);
    final JSONArray jsonArray = jsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE)
        .getJSONArray(JsonConstants.DATA);
    assertEquals(1, jsonArray.length());
    final JSONObject jsonTable = jsonArray.getJSONObject(0);

    setSystemAdministratorContext();
    final JsonToDataConverter converter = new JsonToDataConverter();
    final Table table = (Table) converter.toBaseOBObject(jsonTable);
    assertFalse(converter.hasErrors());
    assertTrue(table.getId().equals("104"));
    assertTrue(table.getDescription().equals("Reference List"));
    assertTrue(!table.isNewOBObject());
  }

  /**
   * Paged query.
   */
  @SuppressWarnings("deprecation")
  public void testPageRest() throws Exception {
    setSystemAdministratorContext();
    final OBCriteria<Column> colCriteria = OBDal.getInstance().createCriteria(Column.class);
    colCriteria.setFilterOnActive(false);
    final int columnCnt = colCriteria.count();
    final JSONObject jsonObject = doRequest(URL_PART + "/ADColumn?_startRow=10&_endRow=17",
        "_identifier", "GET", 200);
    assertEquals(
        columnCnt,
        jsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE).getInt(
            JsonConstants.RESPONSE_TOTALROWS));
    assertEquals(
        17,
        jsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE).getInt(
            JsonConstants.RESPONSE_ENDROW));
    assertEquals(
        10,
        jsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE).getInt(
            JsonConstants.RESPONSE_STARTROWS));
    final JSONArray jsonArray = jsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE)
        .getJSONArray(JsonConstants.DATA);

    for (int i = 0; i < jsonArray.length(); i++) {
      final JSONObject jsonColumnObject = jsonArray.getJSONObject(i);
      final JsonToDataConverter converter = new JsonToDataConverter();
      converter.toBaseOBObject(jsonColumnObject);
      assertFalse(converter.hasErrors());
    }
  }

  /**
   * Test sortby and filter.
   */
  public void testSortbyAndFilter() throws Exception {
    doTestSortByAndFilter(true);
    doTestSortByAndFilter(false);
  }

  private void doTestSortByAndFilter(boolean ascending) throws Exception {
    String filterClause = "Reference List";
    filterClause = URLEncoder.encode(filterClause, "UTF-8");
    final JSONObject jsonObject = doRequest(URL_PART + "/ADRole?clientList=1000000&_sortBy="
        + (ascending ? "" : "-") + "name", "_identifier", "GET", 200);
    final JSONArray jsonArray = jsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE)
        .getJSONArray(JsonConstants.DATA);
    assertEquals(8, jsonArray.length());

    setSystemAdministratorContext();
    String prevName = (ascending ? "AAAAAAAAAAAAAAAAAAAA" : "zzzzzzzzzzzzzzzzzzzzzzzzzzzz");
    for (int i = 0; i < jsonArray.length(); i++) {
      final JSONObject jsonRoleObject = jsonArray.getJSONObject(i);
      final JsonToDataConverter converter = new JsonToDataConverter();
      assertFalse(converter.hasErrors());
      final Role role = (Role) converter.toBaseOBObject(jsonRoleObject);
      if (ascending) {
        assertTrue(role.getName().compareTo(prevName) > 0);
      } else {
        assertTrue(role.getName().compareTo(prevName) < 0);
      }
      assertEquals("1000000", role.getClient().getId());
      prevName = role.getName();
    }
  }
}