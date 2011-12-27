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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.geography.Country;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonRestServlet;
import org.openbravo.service.json.JsonToDataConverter;

/**
 * Test update, delete and insert request on the {@link JsonRestServlet}.
 * 
 * @author mtaal
 */

public class JsonUpdateRestTest extends JsonRestTest {

  private static final String URL_PART = "/org.openbravo.service.json.jsonrest";

  /**
   * Test update of description of Country
   */
  public void testUpdate() throws Exception {
    String description = "";
    {
      final JSONObject jsonCountryObject = doRequest(URL_PART + "/Country/100", "_identifier",
          "GET", 200);
      description = jsonCountryObject.getString("description");
      jsonCountryObject.put("description", description + "T");

      final JSONObject jsonResult = new JSONObject();
      jsonResult.put(JsonConstants.DATA, jsonCountryObject);
      final JSONObject resultJsonObject = doContentRequest(URL_PART + "/Country",
          jsonResult.toString(), 200, "_identifier", "PUT");
      assertEquals(
          JsonConstants.RPCREQUEST_STATUS_SUCCESS,
          resultJsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE).getInt(
              JsonConstants.RESPONSE_STATUS));
      final JSONArray jsonResultArray = resultJsonObject.getJSONObject(
          JsonConstants.RESPONSE_RESPONSE).getJSONArray(JsonConstants.DATA);
      assertEquals(1, jsonResultArray.length());
    }
    {
      final JSONObject jsonCountryObject = doRequest(URL_PART + "/Country/100", "_identifier",
          "GET", 200);
      assertEquals(description + "T", jsonCountryObject.getString("description"));
    }
  }

  /**
   * Test insert and delete of Country
   */
  public void testInsertDelete() throws Exception {
    String newId = null;
    setSystemAdministratorContext();
    {
      final Country country = OBDal.getInstance().get(Country.class, "100");
      final Country newCountry = (Country) DalUtil.copy(country);
      newCountry.setName(country.getName() + "T");
      newCountry.setDescription(country.getDescription() + "T");
      newCountry.setISOCountryCode("ZZ");

      final DataToJsonConverter converter = new DataToJsonConverter();
      final JSONObject jsonCountryObject = converter.toJsonObject(newCountry,
          DataResolvingMode.FULL);
      final JSONObject jsonResult = new JSONObject();
      jsonResult.put(JsonConstants.DATA, jsonCountryObject);
      final JSONObject resultJsonObject = doContentRequest(URL_PART + "/Country",
          jsonResult.toString(), 200, "_identifier", "POST");

      assertEquals(
          JsonConstants.RPCREQUEST_STATUS_SUCCESS,
          resultJsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE).getInt(
              JsonConstants.RESPONSE_STATUS));
      final JSONArray jsonArray = resultJsonObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE)
          .getJSONArray(JsonConstants.DATA);
      assertEquals(1, jsonArray.length());
      final JSONObject jsonNewCountryObject = jsonArray.getJSONObject(0);
      final JsonToDataConverter jsonToDataConverter = new JsonToDataConverter();
      final Country theNewCountry = (Country) jsonToDataConverter
          .toBaseOBObject(jsonNewCountryObject);
      assertTrue(!theNewCountry.isNewOBObject());
      newId = theNewCountry.getId();
    }
    {
      final JSONObject jsonResultDeleteObject = doRequest(URL_PART + "/Country/" + newId,
          "_identifier", "DELETE", 200);
      assertEquals(
          JsonConstants.RPCREQUEST_STATUS_SUCCESS,
          jsonResultDeleteObject.getJSONObject(JsonConstants.RESPONSE_RESPONSE).getInt(
              JsonConstants.RESPONSE_STATUS));
      assertEquals(null, doRequest(URL_PART + "/Country/" + newId, "_identifier", "GET", 404));
    }
  }
}