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

import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

/**
 * Implements a service which can handle different types of query and paging options. This class
 * supports standard parameters for paging and flexible parameters for filtering queries. Sorting is
 * supported on one property.
 * 
 * This service class only supports querying for one {@link Entity}.
 * 
 * This service class can not be used as a singleton.
 * 
 * It makes use of the {@link QueryBuilder} helper class to manage filter information.
 * 
 * @author mtaal
 */
public class DataEntityQueryService {
  private static final Logger log = Logger.getLogger(DataEntityQueryService.class);

  public static final String PARAM_DELIMITER = "@";

  private static final long serialVersionUID = 1L;

  private String entityName;
  private Integer firstResult = null;
  private Integer maxResults = null;

  private boolean filterOnActive = true;
  private AdvancedQueryBuilder queryBuilder = new AdvancedQueryBuilder();

  /**
   * Count the records which fit in the filter criteria.
   * 
   * @return the number of records in the filter.
   */
  public int count() {
    Check.isNotNull(entityName, "entityName must be set");
    final OBQuery<BaseOBObject> obq = OBDal.getInstance().createQuery(entityName,
        queryBuilder.getJoinClause() + queryBuilder.getWhereClause());

    if (queryBuilder.hasOrganizationParameter()) {
      obq.setFilterOnReadableOrganization(false);
    }
    if (!filterOnActive) {
      obq.setFilterOnActive(false);
    }
    obq.setNamedParameters(queryBuilder.getNamedParameters());

    return obq.count();
  }

  /**
   * Return the list of {@link BaseOBObject} objects retrieved by querying using the filter
   * criteria.
   * 
   * @return the list of retrieved objects from the db.
   */
  public List<BaseOBObject> list() {
    return buildOBQuery().list();
  }

  /**
   * @return a result which can be scrolled forward only and the results are not cached
   */
  public ScrollableResults scroll() {
    final Query qry = buildOBQuery().createQuery();
    qry.setFetchSize(1000);
    qry.setCacheable(false);
    return qry.scroll(ScrollMode.FORWARD_ONLY);
  }

  private OBQuery<BaseOBObject> buildOBQuery() {
    final String whereOrderBy = queryBuilder.getJoinClause() + queryBuilder.getWhereClause()
        + queryBuilder.getOrderByClause();

    log.debug("Querying for " + entityName + " " + whereOrderBy);

    // System.err.println("Querying for " + entityName + " " + whereOrderBy);

    final OBQuery<BaseOBObject> obq = OBDal.getInstance().createQuery(entityName, whereOrderBy);
    if (getFirstResult() != null) {
      obq.setFirstResult(getFirstResult());
      log.debug("Firstresult " + getFirstResult());
    }
    if (getMaxResults() != null) {
      obq.setMaxResult(getMaxResults());
      log.debug("Maxresult " + getMaxResults());
    }

    if (queryBuilder.hasOrganizationParameter()) {
      obq.setFilterOnReadableOrganization(false);
    }
    obq.setFilterOnActive(isFilterOnActive());

    obq.setNamedParameters(queryBuilder.getNamedParameters());

    return obq;
  }

  public int getRowNumber(String targetRecordId) {
    final String whereOrderBy = queryBuilder.getJoinClause() + queryBuilder.getWhereClause()
        + queryBuilder.getOrderByClause();

    log.debug("Querying for " + entityName + " " + whereOrderBy);

    // System.err.println("Querying for " + entityName + " " + whereOrderBy);

    final OBQuery<BaseOBObject> obq = OBDal.getInstance().createQuery(entityName, whereOrderBy);

    if (queryBuilder.hasOrganizationParameter()) {
      obq.setFilterOnReadableOrganization(false);
    }
    obq.setFilterOnActive(isFilterOnActive());

    obq.setNamedParameters(queryBuilder.getNamedParameters());

    return obq.getRowNumber(targetRecordId);

  }

  public Integer getFirstResult() {
    return firstResult;
  }

  public void setFirstResult(Integer firstResult) {
    this.firstResult = firstResult;
  }

  public Integer getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  public void setOrderBy(String orderBy) {
    queryBuilder.setOrderBy(orderBy);
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
    queryBuilder.setEntity(entityName);
  }

  /**
   * The text matching strategy used. See here for a description:
   * http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#attr..ComboBoxItem.textMatchStyle
   * 
   * @param textMatchingName
   *          the following values are allowed: startsWith, substring, exact
   */
  public void setTextMatching(String textMatchingName) {
  }

  public void setCriteria(JSONObject criteria) {
    try {
      queryBuilder.setCriteria(criteria);
      if (criteria.has("operator")
          && AdvancedQueryBuilder.OPERATOR_OR.equals(criteria.getString("operator"))) {
        setDoOrExpression();
      }
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  public void addFilterParameter(String key, String value) {
    queryBuilder.addFilterParameter(key, value);
  }

  /**
   * If called then the where clause filters will be considered to be an or expression.
   */
  public void setDoOrExpression() {
    queryBuilder.setDoOr(true);
  }

  /**
   * Tells the {@link QueryBuilder} to use the {@link JsonConstants#MAIN_ALIAS} as the alias for
   * prefixing all properties in the where clause and order by.
   */
  public void setUseAlias() {
    queryBuilder.setMainAlias(JsonConstants.MAIN_ALIAS);
  }

  public boolean isFilterOnActive() {
    return filterOnActive;
  }

  public void setFilterOnActive(boolean filterOnActive) {
    this.filterOnActive = filterOnActive;
  }
}
