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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.userinterface.selector;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.domaintype.BigDecimalDomainType;
import org.openbravo.base.model.domaintype.BooleanDomainType;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.ForeignKeyDomainType;
import org.openbravo.base.model.domaintype.LongDomainType;
import org.openbravo.base.model.domaintype.UniqueIdDomainType;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

public class CustomQuerySelectorDatasource extends ReadOnlyDataSourceService {

  private static Logger log = Logger.getLogger(SelectorDataSourceFilter.class);
  private static final String ADDITIONAL_FILTERS = "@additional_filters@";
  private static final String NEW_FILTER_CLAUSE = "\n AND ";
  private static final String NEW_OR_FILTER_CLAUSE = "\n OR ";

  @Override
  protected int getCount(Map<String, String> parameters) {
    // we return -1, so that the super class calculates a valid count
    return -1;
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    // creation of formats is done here because they are not thread safe
    final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
    final SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();
    final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

    String selectorId = parameters.get(SelectorConstants.DS_REQUEST_SELECTOR_ID_PARAMETER);

    if (StringUtils.isEmpty(selectorId)) {
      return result;
    }

    OBContext.setAdminMode();
    try {

      Selector sel = OBDal.getInstance().get(Selector.class, selectorId);
      List<SelectorField> fields = OBDao.getActiveOBObjectList(sel,
          Selector.PROPERTY_OBUISELSELECTORFIELDLIST);

      // Parse the HQL in case that optional filters are required
      String HQL = parseOptionalFilters(parameters, sel, xmlDateFormat);

      String sortBy = parameters.get("_sortBy");
      HQL += getSortClause(sortBy, sel);

      Query selQuery = OBDal.getInstance().getSession().createQuery(HQL);
      String[] queryAliases = selQuery.getReturnAliases();

      if (startRow > 0) {
        selQuery.setFirstResult(startRow);
      }
      if (endRow > startRow) {
        selQuery.setMaxResults(endRow - startRow + 1);
      }

      for (Object objResult : selQuery.list()) {
        final Map<String, Object> data = new LinkedHashMap<String, Object>();
        Object[] resultList = new Object[1];
        if (objResult instanceof Object[]) {
          resultList = (Object[]) objResult;
        } else {
          resultList[0] = objResult;
        }

        for (SelectorField field : fields) {
          // TODO: throw an exception if the display expression doesn't match any returned alias.
          for (int i = 0; i < queryAliases.length; i++) {
            if (queryAliases[i].equals(field.getDisplayColumnAlias())) {
              Object value = resultList[i];
              if (value instanceof Date) {
                value = xmlDateFormat.format(value);
              }
              if (value instanceof Timestamp) {
                value = xmlDateTimeFormat.format(value);
                value = JsonUtils.convertToCorrectXSDFormat((String) value);
              }
              data.put(queryAliases[i], value);
            }
          }
        }
        result.add(data);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  /**
   * Returns the selectors HQL query. In case that it contains the '@additional_filters@' String it
   * is replaced by a set of filter clauses.
   * 
   * These include a filter clause:
   * <ul>
   * <li>for the main entity's client by the context's client.</li>
   * <li>for the main entity's organization by an organization list see {@link #getOrgs(String)}</li>
   * <li>with Selector's default filter expression.</li>
   * <li>for each default expression defined on the selector fields.</li>
   * <li>for each selector field in case exists a value for it on the parameters param.</li>
   * </ul>
   * 
   * @param parameters
   *          Map of String values with the request parameters.
   * @param sel
   *          the selector that it is being retrieved the data.
   * @param xmlDateFormat
   *          SimpleDataFormat to be used to parse date Strings.
   * @return a String with the HQL to be executed.
   */

  private String parseOptionalFilters(Map<String, String> parameters, Selector sel,
      SimpleDateFormat xmlDateFormat) {
    String HQL = sel.getHQL();
    if (!HQL.contains(ADDITIONAL_FILTERS)) {
      return HQL;
    }
    final String requestType = parameters.get(SelectorConstants.DS_REQUEST_TYPE_PARAMETER);
    StringBuffer additionalFilter = new StringBuffer();
    final String entityAlias = sel.getEntityAlias();
    // Client filter
    additionalFilter.append(entityAlias + ".client.id ='")
        .append(OBContext.getOBContext().getCurrentClient().getId()).append("'");

    // Organization filter
    final String orgs = getOrgs(parameters.get(JsonConstants.ORG_PARAMETER));
    if (StringUtils.isNotEmpty(orgs)) {
      additionalFilter.append(NEW_FILTER_CLAUSE);
      additionalFilter.append(entityAlias + ".organization in (" + orgs + ")");
    }
    additionalFilter.append(getDefaultFilterExpression(sel, parameters));

    StringBuffer defaultExpressionsFilter = new StringBuffer();
    boolean hasFilter = false;
    List<SelectorField> fields = OBDao.getActiveOBObjectList(sel,
        Selector.PROPERTY_OBUISELSELECTORFIELDLIST);
    HashMap<String, String[]> criteria = getCriteria(parameters);
    for (SelectorField field : fields) {
      if (StringUtils.isEmpty(field.getClauseLeftPart())) {
        continue;
      }
      String operator = null;
      String value = null;
      if (criteria != null) {
        String[] operatorvalue = criteria.get(field.getDisplayColumnAlias());
        if (operatorvalue != null) {
          operator = operatorvalue[0];
          value = operatorvalue[1];
        }
      }
      if (StringUtils.isEmpty(value)) {
        value = parameters.get(field.getDisplayColumnAlias());
      }
      // Add field default expression on picklist if it is not already filtered. Default expressions
      // on selector popup are already evaluated and their values came in the parameters object.
      if (field.getDefaultExpression() != null && !"Window".equals(requestType)
          && StringUtils.isEmpty(value)) {
        try {
          String defaultValue = "";
          Object defaultValueObject = ParameterUtils.getJSExpressionResult(parameters,
              RequestContext.get().getSession(), field.getDefaultExpression());
          if (defaultValueObject != null) {
            defaultValue = defaultValueObject.toString();
          }
          if (StringUtils.isNotEmpty(defaultValue)) {
            defaultExpressionsFilter.append(NEW_FILTER_CLAUSE);
            defaultExpressionsFilter.append(getWhereClause(operator, defaultValue, field,
                xmlDateFormat));
          }
        } catch (Exception e) {
          log.error("Error evaluating filter expression: " + e.getMessage(), e);
        }
      }
      if (field.isFilterable() && StringUtils.isNotEmpty(value)) {
        String whereClause = getWhereClause(operator, value, field, xmlDateFormat);
        if (!hasFilter) {
          additionalFilter.append(NEW_FILTER_CLAUSE);
          additionalFilter.append(" (");
          hasFilter = true;
        } else {
          if ("Window".equals(requestType)) {
            additionalFilter.append(NEW_FILTER_CLAUSE);
          } else {
            additionalFilter.append(NEW_OR_FILTER_CLAUSE);
          }
        }
        additionalFilter.append(whereClause);
      }
    }
    if (hasFilter) {
      additionalFilter.append(")");
    }
    if (defaultExpressionsFilter.length() > 0) {
      additionalFilter.append(defaultExpressionsFilter);
    }
    HQL = HQL.replace(ADDITIONAL_FILTERS, additionalFilter.toString());
    return HQL;
  }

  /**
   * Returns a comma separated list of organization ids to filter the HQL. If an organization id is
   * provided its natural tree is returned. If no organization is provided or the given value is
   * invalid the readable organizations are returned.
   */
  private String getOrgs(String orgId) {
    StringBuffer orgPart = new StringBuffer();
    if (StringUtils.isNotEmpty(orgId)) {
      final Set<String> orgSet = OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(orgId);
      if (orgSet.size() > 0) {
        boolean addComma = false;
        for (String org : orgSet) {
          if (addComma) {
            orgPart.append(",");
          }
          orgPart.append("'" + org + "'");
          addComma = true;
        }
      }
    }
    if (orgPart.length() == 0) {
      String[] orgs = OBContext.getOBContext().getReadableOrganizations();
      boolean addComma = false;
      for (int i = 0; i < orgs.length; i++) {
        if (addComma) {
          orgPart.append(",");
        }
        orgPart.append("'" + orgs[i] + "'");
        addComma = true;
      }
    }
    return orgPart.toString();
  }

  /**
   * Returns the where clause of a selector's field based on the given value.
   * 
   * This method based on the DomainType of the selector field returns the filter clause using the
   * clause left part defined on the selector field.
   * <ul>
   * <li>Numeric Domain Type: Returns an equals clause <i>field.clauseLeftPart = value</i></li>
   * <li>Date Domain Type: Returns a multiple clause comparing separately value's day, month and
   * year.</li>
   * <li>Boolean Domain Type: Returns an equals clause <i>field.clauseLeftPart = value</i></li>
   * <li>Foreign Key Domain Type: Returns an equals clause <i>field.clauseLeftPart.id = value</i></li>
   * <li>Unique Id Domain Type: Returns an equals clause <i>field.clauseLeftPart = value</i></li>
   * <li>String Domain Type: Compares the clause left part with the value using the lower database
   * function which to make comparison case insensitive.
   * </ul>
   * 
   * @param value
   *          String with the value that the selector field's column is filtered by.
   * @param field
   *          The SelectorField that is filtered.
   * @param xmlDateFormat
   *          SimpleDateFormat to parse the value in case the field is a Date field.
   * @return a String with the HQL where clause to filter the field by the given value.
   */
  private String getWhereClause(String operator, String value, SelectorField field,
      SimpleDateFormat xmlDateFormat) {
    String whereClause = "";
    DomainType domainType = ModelProvider.getInstance().getReference(field.getReference().getId())
        .getDomainType();
    if (domainType.getClass().getSuperclass().equals(BigDecimalDomainType.class)
        || domainType.getClass().equals(LongDomainType.class)) {
      whereClause = field.getClauseLeftPart() + " = " + value;
    } else if (domainType.getClass().equals(DateDomainType.class)) {
      try {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(xmlDateFormat.parse(value));
        whereClause = " (day(" + field.getClauseLeftPart() + ") = " + cal.get(Calendar.DATE);
        whereClause += "\n and month(" + field.getClauseLeftPart() + ") = "
            + (cal.get(Calendar.MONTH) + 1);
        whereClause += "\n and year(" + field.getClauseLeftPart() + ") = " + cal.get(Calendar.YEAR)
            + ") ";
      } catch (Exception e) {
        // ignore these errors, just don't filter then
        // add a dummy whereclause to make the query format correct
        whereClause = "1 = 1";
      }
    } else if (domainType instanceof BooleanDomainType) {
      whereClause = field.getClauseLeftPart() + " = " + value;
    } else if (domainType instanceof UniqueIdDomainType) {
      whereClause = field.getClauseLeftPart() + " = '" + value + "'";
    } else if (domainType instanceof ForeignKeyDomainType) {
      // Assume left part definition is full object reference from HQL select
      whereClause = field.getClauseLeftPart() + ".id = '" + value + "'";
    } else {

      if ("iStartsWith".equals(operator)) {
        whereClause = "lower(" + field.getClauseLeftPart() + ") LIKE '"
            + value.toLowerCase().replaceAll(" ", "%") + "%'";
      } else {
        whereClause = "lower(" + field.getClauseLeftPart() + ") LIKE '%"
            + value.toLowerCase().replaceAll(" ", "%") + "%'";
      }
    }
    return whereClause;
  }

  /**
   * Generates the HQL Sort By Clause to append to the query being executed. If no sort options is
   * set on the sortBy parameter the result is ordered by the first shown grid's column.
   * 
   * @param sortBy
   *          String of grid's field names concatenated by JsonConstants.IN_PARAMETER_SEPARATOR.
   * @param sel
   *          the selector that it is being displayed.
   * @return a String with the HQL Sort By clause.
   */
  private String getSortClause(String sortBy, Selector sel) {
    StringBuffer sortByClause = new StringBuffer();
    // If grid is manually filtered sortBy is not empty
    if (StringUtils.isNotEmpty(sortBy)) {
      if (sortBy.contains(JsonConstants.IN_PARAMETER_SEPARATOR)) {
        final String[] fieldNames = sortBy.split(JsonConstants.IN_PARAMETER_SEPARATOR);
        for (String fieldName : fieldNames) {
          int fieldSortIndex = getFieldSortIndex(fieldName, sel);
          if (fieldSortIndex > 0) {
            if (sortByClause.length() > 0) {
              sortByClause.append(", ");
            }
            sortByClause.append(fieldSortIndex);
          }
        }
      } else {
        int fieldSortIndex = getFieldSortIndex(sortBy, sel);
        if (fieldSortIndex > 0) {
          sortByClause.append(fieldSortIndex);
        }
      }
    }

    // If sortByClause is empty set default sort options.
    if (sortByClause.length() == 0) {
      OBCriteria<SelectorField> selFieldsCrit = OBDao.getFilteredCriteria(SelectorField.class,
          Restrictions.eq(SelectorField.PROPERTY_OBUISELSELECTOR, sel),
          Restrictions.eq(SelectorField.PROPERTY_SHOWINGRID, true));
      selFieldsCrit.addOrderBy(SelectorField.PROPERTY_SORTNO, true);
      for (SelectorField selField : selFieldsCrit.list()) {
        int fieldSortIndex = getFieldSortIndex(selField.getDisplayColumnAlias(), sel);
        if (fieldSortIndex > 0) {
          sortByClause.append(fieldSortIndex + ", ");
        }
      }
      // Delete last 2 characters: ", "
      if (sortByClause.length() > 0) {
        sortByClause.delete(sortByClause.length() - 3, sortByClause.length() - 1);
      }
    }
    String result = "";
    if (sortByClause.length() > 0) {
      result = "\n ORDER BY " + sortByClause.toString();
    }

    return result;
  }

  /**
   * Given a Selector object and the request parameters it evaluates the Filter Expression in case
   * that it is defined and returns the result.
   * 
   * @param sel
   *          The Selector that it is being used.
   * @param parameters
   *          parameters used for this request.
   * @return a String with the evaluated JavaScript filter expression in case it is defined.
   */
  private String getDefaultFilterExpression(Selector sel, Map<String, String> parameters) {
    if ((sel.getFilterExpression() == null || sel.getFilterExpression().equals(""))) {
      // Nothing to filter
      return "";
    }

    Object result = null;
    try {
      result = ParameterUtils.getJSExpressionResult(parameters, RequestContext.get().getSession(),
          sel.getFilterExpression());
    } catch (Exception e) {
      log.error("Error evaluating filter expression: " + e.getMessage(), e);
    }
    if (result != null && !result.toString().equals("")) {
      return NEW_FILTER_CLAUSE + "(" + result.toString() + ")";
    }

    return "";
  }

  /**
   * Based on the given field name it gets the HQL query column related to it and returns its index.
   * 
   * @param fieldName
   *          Grid's field name or display alias of the related selector field it is desired to
   *          order by.
   * @param sel
   *          The Selector that it is being used.
   * @return The index of the query column related to the field.
   */
  private int getFieldSortIndex(String fieldName, Selector sel) {
    final String[] queryAliases = OBDal.getInstance().getSession()
        .createQuery(sel.getHQL().replace(ADDITIONAL_FILTERS, "1=1")).getReturnAliases();
    for (int i = 0; i < queryAliases.length; i++) {
      if (queryAliases[i].equals(fieldName)) {
        return i + 1;
      }
    }
    return 0;
  }

  private HashMap<String, String[]> getCriteria(Map<String, String> parameters) {
    if (!"AdvancedCriteria".equals(parameters.get("_constructor"))) {
      return null;
    }
    HashMap<String, String[]> criteriaValues = new HashMap<String, String[]>();
    try {
      JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
      for (int i = 0; i < criterias.length(); i++) {
        final JSONObject criteria = criterias.getJSONObject(i);
        criteriaValues.put(criteria.getString("fieldName"),
            new String[] { criteria.getString("operator"), criteria.getString("value") });
      }
    } catch (JSONException e) {
      // Ignore exception.
    }
    if (criteriaValues.isEmpty()) {
      return null;
    }
    return criteriaValues;
  }
}
