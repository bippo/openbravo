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
package org.openbravo.service.datasource;

import java.util.List;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.client.kernel.Template;

/**
 * Represents a data source as it is present on the client. It provides both the javascript to
 * create the datasource on the client as well as the runtime handling code of the client datasource
 * calls to the server.
 * 
 * Is the base class which can both query and return data as json as well as handle update, delete
 * and insert requests with json data as an input.
 * 
 * This is the base class for implementations of this service. Several methods have specific
 * parameters or the content of the request (normally a json string).
 * 
 * This class is cached so one instance is shared by multiple threads. Local caching of information
 * should take this into account! NOTE: another (not-yet-implemented) thought is to cache
 * datasources in session-scope. This allows for additional caching of information.
 * 
 * @author mtaal
 */
public interface DataSourceService {

  /**
   * @return the Template used to render the data source
   */
  public Template getTemplate();

  /**
   * Create the properties used in the template.
   * 
   * @param parameters
   *          parameters used for this request
   * @return the list of DataSourceProperty instances to use in the template
   */
  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters);

  /**
   * Execute a query request and return the result as a json string. For a query request the content
   * ({@link #getPath()}) is normally empty, the query parameters can be found in the parameters map
   * ({@link #getParameters()}).
   * 
   * @param parameters
   *          the parameters often coming from the HTTP request
   * @return the json result string
   */
  public String fetch(Map<String, String> parameters);

  /**
   * Execute a delete action. The id of the deleted record is present in the parameters.
   * 
   * @param parameters
   *          the parameters often coming from the HTTP request
   * @return the result message as a json string
   */
  public String remove(Map<String, String> parameters);

  /**
   * Execute an insert action. There can be parameters in the parameter map (
   * {@link #getParameters()}) but often the data to insert is present in the content (
   * {@link #getPath()}).
   * 
   * @param parameters
   *          the parameters often coming from the HTTP request
   * @param content
   *          , the request content, is assumed to be a json string
   * @return the result message as a json string
   */
  public String add(Map<String, String> parameters, String content);

  /**
   * Execute an update action. There can be parameters in the parameter map (
   * {@link #getParameters()}) but often the data to insert is present in the content (
   * {@link #getPath()}).
   * 
   * @param parameters
   *          the parameters often coming from the HTTP request
   * @param content
   *          , the request content, is assumed to be a json string
   * @return the result message as a json string
   */
  public String update(Map<String, String> parameters, String content);

  /**
   * The name of this service, in the default implementation this is the entityname of the
   * {@link Entity} to get/insert/update/delete.
   * 
   * @return the name of the service
   */
  public String getName();

  public void setName(String name);

  public String getDataUrl();

  public void setDataUrl(String dataUrl);

  public Entity getEntity();

  public void setEntity(Entity entity);

  public String getWhereClause();

  public void setWhereClause(String whereClause);

  /**
   * The data source read from the database. Note care must be taken when using referenced objects
   * in the returned DataSource instance. The DataSource is read often in a different thread so the
   * Hibernate session is not available anymore.
   * 
   * @return the data source read from the database. Note: can be null for data sources which are
   *         created in-memory on request.
   */
  public DataSource getDataSource();

  /**
   * Passes in the data source read from the database. Is the place to initialize the data in the
   * service class
   * 
   * @param dataSource
   *          the data source read from the database.
   */
  public void setDataSource(DataSource dataSource);
}
