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
package org.openbravo.client.application.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

/**
 * The backing bean for a grid example. This backing bean does 2 things: 1) read data from the
 * database to fill the grid and 2) execute an action using the selected data.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class GridExampleActionHandler extends BaseActionHandler {

  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String WINDOWCOUNT = "windowCount";
  private static final String SELECTEDRECORDS = "selectedRecords";
  private static final String MESSAGE = "message";
  private static final String LIST = "list";

  // note the _action parameter is already used by the ActionHandler
  // framework, therefore use the term _command
  private static final String COMMAND_PARAM = "_command";
  private static final String COMMAND_PARAM_DATA = "data";
  private static final String COMMAND_PARAM_EXECUTE = "execute";

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseActionHandler#execute(java.util.Map, java.lang.String)
   */
  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    if (!parameters.containsKey(COMMAND_PARAM)) {
      throw new OBException("The parameter " + COMMAND_PARAM + " is a mandatory parameter");
    }

    if (parameters.get(COMMAND_PARAM).equals(COMMAND_PARAM_DATA)) {
      return doDataCommand(parameters, content);
    } else if (parameters.get(COMMAND_PARAM).equals(COMMAND_PARAM_EXECUTE)) {
      return doExecuteCommand(parameters, content);
    } else {
      throw new OBException("The command " + parameters.get(COMMAND_PARAM) + " is not implemented");
    }
  }

  /**
   * Read the data from the database and return it as a json string.
   * 
   * @param parameters
   *          the request parameters
   * @param content
   *          the request content, a json string
   * @return the data as a json object
   */
  protected JSONObject doDataCommand(Map<String, Object> parameters, String content) {
    // read the modules from the database and return the data as a JSON object
    OBContext.setAdminMode();
    try {
      final String hql = "select module.id, module.name, count(window) from ADModule module, ADWindow window "
          + " where window.module=module group by module.id, module.name";
      Query qry = OBDal.getInstance().getSession().createQuery(hql);

      final List<JSONObject> dataList = new ArrayList<JSONObject>();
      for (Object o : qry.list()) {
        final Object[] os = (Object[]) o;
        final JSONObject data = new JSONObject();
        data.put(ID, os[0]);
        data.put(NAME, os[1]);
        data.put(WINDOWCOUNT, os[2]);
        dataList.add(data);
      }
      final JSONObject result = new JSONObject();
      result.put(LIST, new JSONArray(dataList));
      return result;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Execute an action on selected data
   * 
   * @param parameters
   *          the request parameters
   * @param content
   *          the request content, a json string
   * @return a result as a json object
   */
  protected JSONObject doExecuteCommand(Map<String, Object> parameters, String content) {
    // execute an example action
    OBContext.setAdminMode();
    try {
      // convert the data string to a json object and check its field
      // SELECTEDRECORDS, for each record in the selected records
      // generate a dummy string
      final JSONObject data = new JSONObject(content);
      final JSONArray jsonArray = data.getJSONArray(SELECTEDRECORDS);

      // create some message to report something back
      final StringBuilder sb = new StringBuilder();
      sb.append("Processed " + jsonArray.length() + " records: <ol>");
      for (int i = 0; i < jsonArray.length(); i++) {
        final JSONObject record = (JSONObject) jsonArray.get(i);
        sb.append("<li>" + record.get(NAME) + " (" + record.get(ID) + ")</li>");
      }
      sb.append("</ol>");

      // return the result as json
      final JSONObject result = new JSONObject();
      result.put(MESSAGE, sb.toString());
      return result;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
