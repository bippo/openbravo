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
package org.openbravo.service.json;

import java.util.Map;

import org.openbravo.base.structure.BaseOBObject;

/**
 * Implements generic data operations which have parameters and json as an input and return results
 * as json strings.
 * 
 * Note the parameters, json input and generated json follow the Smartclient specs. See the
 * Smartclient <a href="http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#class..RestDataSource">
 * RestDataSource</a> for more information.
 * 
 * @author mtaal
 */
public interface JsonDataService {

  /**
   * Do a query for {@link BaseOBObject} objects and return the result as a json string.
   * 
   * One parameter is mandatory: {@link JsonConstants.ENTITYNAME}. The possible query parameters can
   * be found in {@link JsonConstants}, see the constants ending with _PARAMETER. All other request
   * parameters which correspond to properties of the queried objects are considered to be filter
   * properties.
   * 
   * The result is a json string which contains paging information (startrow, endrow, rowcount) and
   * the data itself. See the Smartclient <a
   * href="http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#class..RestDataSource">
   * RestDataSource</a> for more information.
   * 
   * @param parameters
   *          the parameters driving the query, one parameter is mandatory:
   *          {@link JsonConstants.ENTITYNAME}
   * @return a json result string
   * @see QueryBuilder
   */
  public abstract String fetch(Map<String, String> parameters);

  /**
   * Remove an object, this method expects two parameters: {@link JsonConstants.ID} and
   * {@link JsonConstants.ENTITYNAME}. If they are not present an error is returned.
   * 
   * @param parameters
   *          two parameters should be present: id and entityName
   * @return the id and entityname of the removed object is returned.
   */
  public abstract String remove(Map<String, String> parameters);

  /**
   * Adds data passed as json. Note that this method currently forwards to the
   * {@link #update(Map, String)} method. The system follows this logic: if the json defines an id
   * for an object and it exists in the database then it is updated, in all other cases an insert
   * takes place.
   * 
   * @param parameters
   *          contains extra parameters, not used by this implementation but can be convenient for
   *          extenders of this service.
   * @param content
   *          the json string containing the data, the expected format is the same as defined by the
   *          Smartclient <a
   *          href="http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#class..RestDataSource">
   *          RestDataSource</a>
   * @return the inserted objects as json or the an error json string
   */
  public abstract String add(Map<String, String> parameters, String content);

  /**
   * Updates data passed as json. Note that this method currently both allows additions as well as
   * updated. The system follows this logic: if the json defines an id for an object and it exists
   * in the database then it is updated, in all other cases an insert takes place.
   * 
   * @param parameters
   *          contains extra parameters, not used by this implementation but can be convenient for
   *          extenders of this service.
   * @param content
   *          the json string containing the data, the expected format is the same as defined by the
   *          Smartclient <a
   *          href="http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#class..RestDataSource">
   *          RestDataSource</a>
   * @return the inserted objects as json or the an error json string
   */
  public abstract String update(Map<String, String> parameters, String content);

}