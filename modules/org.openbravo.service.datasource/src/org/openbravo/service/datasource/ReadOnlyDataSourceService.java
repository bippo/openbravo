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
package org.openbravo.service.datasource;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.DefaultJsonDataService.QueryResultWriter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

/**
 * The SimpleDataSourceService provides a simple way of returning data in the correct format for a
 * client side component using a datasource.
 * 
 * @author mtaal
 */
public abstract class ReadOnlyDataSourceService extends DefaultDataSourceService {
  private static final Logger log = Logger.getLogger(ReadOnlyDataSourceService.class);

  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#fetch(java.util.Map)
   */
  public String fetch(Map<String, String> parameters) {

    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    final String endRowStr = parameters.get(JsonConstants.ENDROW_PARAMETER);
    int startRow = 0;
    boolean doCount = false;
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
      doCount = true;
    }
    if (endRowStr != null) {
      doCount = true;
    }
    final List<JSONObject> jsonObjects = fetchJSONObject(parameters);

    // now jsonfy the data
    try {
      final JSONObject jsonResult = new JSONObject();
      final JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, jsonObjects.size() + startRow - 1);
      if (doCount) {
        int num = getCount(parameters);
        if (num == -1) {
          int endRow = Integer.parseInt(endRowStr);
          num = (endRow + 2);
          if ((endRow - startRow) > jsonObjects.size()) {
            num = startRow + jsonObjects.size();
          }
        }
        jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, num);
      }
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

      // if (jsonObjects.size() > 0) {
      // System.err.println(jsonObjects.get(0));
      // }
      return jsonResult.toString();
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  public void fetch(Map<String, String> parameters, QueryResultWriter writer) {
    for (JSONObject jsonObject : fetchJSONObject(parameters)) {
      writer.write(jsonObject);
    }
  }

  private List<JSONObject> fetchJSONObject(Map<String, String> parameters) {
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    final String endRowStr = parameters.get(JsonConstants.ENDROW_PARAMETER);
    int startRow = -1;
    int endRow = -1;
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
    }
    if (endRowStr != null) {
      endRow = Integer.parseInt(endRowStr);
    }
    final List<Map<String, Object>> data = getData(parameters, startRow, endRow);
    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);
    toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
    return toJsonConverter.convertToJsonObjects(data);
  }

  /**
   * Returns the count of objects based on the passed parameters.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @return the total number of objects
   */
  protected abstract int getCount(Map<String, String> parameters);

  /**
   * Read/create the set of data as a list of Maps with key-value pairs.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @param startRow
   *          the first row to read (maybe -1 to indicate no startrow)
   * @param endRow
   *          the last row to read (maybe -1 to indicate no endrow
   * @return the number of objects read, note that this maybe more than endRow - startRow + 1. The
   *         startRow parameter should be strictly followed though.
   */
  protected abstract List<Map<String, Object>> getData(Map<String, String> parameters,
      int startRow, int endRow);

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#remove(java.util.Map)
   */
  @Override
  public String remove(Map<String, String> parameters) {
    throw new UnsupportedOperationException("Only fetch is supported");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#add(java.util.Map, java.lang.String)
   */
  @Override
  public String add(Map<String, String> parameters, String content) {
    throw new UnsupportedOperationException("Only fetch is supported");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#update(java.util.Map, java.lang.String)
   */
  @Override
  public String update(Map<String, String> parameters, String content) {
    throw new UnsupportedOperationException("Only fetch is supported");
  }

  /**
   * Sorts the data list by the value of the passed in sortBy key
   * 
   * @param sortBy
   *          the
   * @param data
   */
  protected void sort(String sortBy, List<Map<String, Object>> data) {
    Collections.sort(data, new DataComparator(sortBy));
  }

  // can only be used if the comparedBy is a string
  private static class DataComparator implements Comparator<Map<String, Object>> {
    private String compareBy;

    public DataComparator(String compareBy) {
      this.compareBy = compareBy;
    }

    @Override
    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
      final Object v1 = o1.get(compareBy);
      final Object v2 = o2.get(compareBy);
      if (v1 == v2) {
        return 0;
      } else if (v1 == null) {
        return -1;
      } else if (v2 == null) {
        return 1;
      }
      if (!(v1 instanceof String && v2 instanceof String)) {
        log.warn("Comparing on property " + compareBy + " for objects " + v1 + "/" + v2 + ". "
            + "But value is not a string but another class, only string is supported, values " + v1
            + "/" + v2 + ". Returning default compare value.");
        return 0;
      }
      return ((String) v1).compareTo((String) v2);
    }
  }

}
