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
package org.openbravo.client.querylist;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.domaintype.BigDecimalDomainType;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.LongDomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.kernel.reference.EnumUIDefinition;
import org.openbravo.client.kernel.reference.ForeignKeyUIDefinition;
import org.openbravo.client.kernel.reference.NumberUIDefinition;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.client.kernel.reference.YesNoUIDefinition;
import org.openbravo.client.myob.WidgetClass;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.service.datasource.DataSourceProperty;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

/**
 * Reads the tabs which the user is allowed to see.
 * 
 * @author gorkaion
 */
public class QueryListDataSource extends ReadOnlyDataSourceService {
  private static final String OPTIONAL_FILTERS = "@optional_filters@";
  private static final Logger log = Logger.getLogger(QueryListDataSource.class);

  /**
   * Returns the count of objects based on the passed parameters.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @return the total number of objects
   */
  @Override
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, -1).size();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    // creation of formats is done here because they are not thread safe
    final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
    final SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();

    OBContext.setAdminMode();
    try {
      WidgetClass widgetClass = OBDal.getInstance().get(WidgetClass.class,
          parameters.get("widgetId"));
      boolean isExport = "true".equals(parameters.get("exportToFile"));
      boolean showAll = "true".equals(parameters.get("showAll"));
      String viewMode = parameters.get("viewMode");
      List<OBCQL_QueryColumn> columns = QueryListUtils.getColumns(widgetClass
          .getOBCQLWidgetQueryList().get(0));

      // handle complex criteria
      try {
        JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
        for (int i = 0; i < criterias.length(); i++) {
          final JSONObject criteria = criterias.getJSONObject(i);
          parameters.put(criteria.getString("fieldName"), criteria.getString("value"));
        }
      } catch (JSONException e) {
        // Ignore exception.
      }

      String HQL = widgetClass.getOBCQLWidgetQueryList().get(0).getHQL();
      // Parse the HQL in case that optional filters are required
      HQL = parseOptionalFilters(HQL, viewMode, parameters, columns, xmlDateFormat);

      Query widgetQuery = OBDal.getInstance().getSession().createQuery(HQL);
      String[] queryAliases = widgetQuery.getReturnAliases();

      if (!isExport && "widget".equals(viewMode) && !showAll) {
        int rowsNumber = Integer.valueOf((parameters.get("rowsNumber") != null && !parameters.get(
            "rowsNumber").equals("null")) ? parameters.get("rowsNumber") : "10");
        widgetQuery.setMaxResults(rowsNumber);
      } else if (!isExport) {
        if (startRow > 0) {
          widgetQuery.setFirstResult(startRow);
        }
        if (endRow > startRow) {
          widgetQuery.setMaxResults(endRow - startRow + 1);
        }
      }

      String[] params = widgetQuery.getNamedParameters();
      if (params.length > 0) {
        HashMap<String, Object> parameterValues = getParameterValues(parameters, widgetClass);

        for (int i = 0; i < params.length; i++) {
          String namedParam = params[i];
          boolean isParamSet = false;
          if (parameterValues.containsKey(namedParam)) {
            Object value = parameterValues.get(namedParam);
            if (value instanceof Collection<?>) {
              widgetQuery.setParameterList(namedParam, (Collection<?>) value);
            } else if (value instanceof Object[]) {
              widgetQuery.setParameterList(namedParam, (Object[]) value);
            } else {
              widgetQuery.setParameter(namedParam, value);
            }
            isParamSet = true;
          }
          if (!isParamSet) {
            // TODO: throw an exception
          }
        }
      }

      final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
      for (Object objResult : widgetQuery.list()) {
        final Map<String, Object> data = new LinkedHashMap<String, Object>();
        Object[] resultList = new Object[1];
        if (objResult instanceof Object[]) {
          resultList = (Object[]) objResult;
        } else {
          resultList[0] = objResult;
        }

        for (OBCQL_QueryColumn column : columns) {
          // TODO: throw an exception if the display expression doesn't match any returned alias.
          for (int i = 0; i < queryAliases.length; i++) {
            if (queryAliases[i].equals(column.getDisplayExpression())
                || (!isExport && queryAliases[i].equals(column.getLinkExpression()))) {
              Object value = resultList[i];
              if (value instanceof Timestamp) {
                value = xmlDateTimeFormat.format(value);
                value = JsonUtils.convertToCorrectXSDFormat((String) value);
              }
              if (value instanceof Date) {
                value = xmlDateFormat.format(value);
              }

              data.put(queryAliases[i], value);
            }
          }
        }
        result.add(data);
      }
      String sortBy = parameters.get("_sortBy");
      if (StringUtils.isNotEmpty(sortBy)) {
        sort(sortBy, result);
      }
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a HashMap with the values of the parameters included on the given widget instance.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @param widgetClass
   *          the widget class to which the parameters belong to
   * @return a HashMap<String, Object> with the value of each parameter mapped by the DBColumnName
   *         of the parameter.
   */
  private HashMap<String, Object> getParameterValues(Map<String, String> parameters,
      WidgetClass widgetClass) {
    HashMap<String, Object> parameterValues = new HashMap<String, Object>();

    // get serializedValues from request (if present)
    String serializedParams = parameters.get("serializedParameters");
    if (serializedParams != null) {
      try {
        JSONObject json = new JSONObject(serializedParams);
        for (Parameter parameter : widgetClass.getOBUIAPPParameterEMObkmoWidgetClassIDList()) {
          if (parameter.isFixed()) {
            parameterValues.put(parameter.getDBColumnName(),
                ParameterUtils.getParameterFixedValue(parameters, parameter));
          } else {
            if (json.has(parameter.getDBColumnName())) {
              parameterValues.put(parameter.getDBColumnName(),
                  json.get(parameter.getDBColumnName()));
            } else {
              // TODO: not fixed & value missing -> error (prepared to be handled in caller, but not
              // yet implemented)
            }
          }
        }
      } catch (JSONException e) {
        log.error("Error processing client parameters", e);
      }
    } else {
      // data send without serializedParams (should not happen)
      throw new OBException("Missing serializedParameters value in request");
    }

    return parameterValues;
  }

  private String parseOptionalFilters(String _HQL, String viewMode, Map<String, String> parameters,
      List<OBCQL_QueryColumn> columns, SimpleDateFormat xmlDateFormat) {
    StringBuffer optionalFilter = new StringBuffer(" 1=1 ");
    String HQL = _HQL;

    // Parse for columns filtered by grid's filter row on maximized view. If we are not on maximized
    // view return the HQL without parsing.
    if ("maximized".equals(viewMode)) {
      for (OBCQL_QueryColumn column : columns) {
        if (column.isCanBeFiltered()) {
          String value = parameters.get(column.getDisplayExpression());
          String whereClause = " 1=1 ";
          if (value != null) {
            whereClause = getWhereClause(value, column, xmlDateFormat);
          }

          if (HQL.contains("@" + column.getDisplayExpression() + "@")) {
            HQL = HQL.replace("@" + column.getDisplayExpression() + "@", whereClause);
          } else {
            optionalFilter.append(" and " + whereClause);
          }
        }
      }
    }
    HQL = HQL.replace(OPTIONAL_FILTERS, optionalFilter.toString());
    return HQL;
  }

  private String getWhereClause(String value, OBCQL_QueryColumn column,
      SimpleDateFormat xmlDateFormat) {
    String whereClause = "";
    DomainType domainType = ModelProvider.getInstance().getReference(column.getReference().getId())
        .getDomainType();
    if (domainType.getClass().getSuperclass().equals(BigDecimalDomainType.class)
        || domainType.getClass().equals(LongDomainType.class)) {
      whereClause = column.getWhereClauseLeftPart() + " = " + value;
    } else if (domainType.getClass().equals(DateDomainType.class)) {
      try {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(xmlDateFormat.parse(value));
        whereClause = " (day(" + column.getWhereClauseLeftPart() + ") = " + cal.get(Calendar.DATE);
        whereClause += "\n and month(" + column.getWhereClauseLeftPart() + ") = "
            + (cal.get(Calendar.MONTH) + 1);
        whereClause += "\n and year(" + column.getWhereClauseLeftPart() + ") = "
            + cal.get(Calendar.YEAR) + ") ";
      } catch (Exception e) {
        // ignore these errors, just don't filter then
        // add a dummy whereclause to make the query format correct
        whereClause = " 1=1 ";
      }
    } else {
      whereClause = "upper(" + column.getWhereClauseLeftPart() + ")";
      whereClause += " LIKE ";
      whereClause += "'%" + value.toUpperCase().replaceAll(" ", "%") + "%'";
    }
    return whereClause;
  }

  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters) {
    // note datasource properties are not cached as the component is
    // re-used within one request thread
    final List<DataSourceProperty> dsProperties = new ArrayList<DataSourceProperty>();
    OBContext.setAdminMode();
    try {
      WidgetClass widgetClass = (WidgetClass) parameters
          .get(QueryListWidgetProvider.WIDGETCLASS_PARAMETER);

      if (!widgetClass.getOBCQLWidgetQueryList().isEmpty()) {
        for (OBCQL_QueryColumn column : QueryListUtils.getColumns(widgetClass
            .getOBCQLWidgetQueryList().get(0))) {
          Reference reference = column.getReference();
          if (column.getReferenceSearchKey() != null) {
            reference = column.getReferenceSearchKey();
          }

          final DataSourceProperty dsProperty = new DataSourceProperty();
          dsProperty.setName(column.getDisplayExpression());
          dsProperty.setId(false);
          dsProperty.setMandatory(false);
          dsProperty.setAuditInfo(false);
          dsProperty.setUpdatable(false);
          final UIDefinition uiDefinition = UIDefinitionController.getInstance().getUIDefinition(
              reference);
          dsProperty.setBoolean(uiDefinition instanceof YesNoUIDefinition);
          dsProperty.setPrimitive(!(uiDefinition instanceof ForeignKeyUIDefinition));
          dsProperty.setUIDefinition(uiDefinition);
          if (dsProperty.isPrimitive()) {
            dsProperty.setPrimitiveObjectType(((PrimitiveDomainType) uiDefinition.getDomainType())
                .getPrimitiveType());
            dsProperty.setNumericType(uiDefinition instanceof NumberUIDefinition);

            if (uiDefinition instanceof EnumUIDefinition) {
              if (column.getReferenceSearchKey() == null) {
                log.warn("In widget " + column.getWidgetQuery().getWidgetClass().getWidgetTitle()
                    + " column " + column.getDisplayExpression()
                    + " is of enum type but does not define sub reference.");
              } else {
                Set<String> allowedValues = new HashSet<String>();

                final String hql = "select al.searchKey from ADList al"
                    + " where al.reference=:ref";
                final Query qry = OBDal.getInstance().getSession().createQuery(hql);
                qry.setParameter("ref", column.getReferenceSearchKey());
                for (Object o : qry.list()) {
                  final String value = (String) o;
                  allowedValues.add(value);
                }

                dsProperty.setAllowedValues(allowedValues);
                dsProperty.setValueMap(DataSourceProperty.createValueMap(allowedValues, column
                    .getReferenceSearchKey().getId()));
              }
            }
          } else {
          }
          dsProperties.add(dsProperty);
        }
      }
      return dsProperties;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  protected void sort(String sortBy, List<Map<String, Object>> data) {
    Collections.sort(data, new DataComparator(sortBy));
  }

  // can only be used if the comparedBy is a string
  private static class DataComparator implements Comparator<Map<String, Object>> {
    private ArrayList<String> compareByArray;

    public DataComparator(String compareBy) {
      this.compareByArray = new ArrayList<String>();
      if (compareBy.contains(JsonConstants.IN_PARAMETER_SEPARATOR)) {
        final String[] separatedValues = compareBy.split(JsonConstants.IN_PARAMETER_SEPARATOR);
        for (String separatedValue : separatedValues) {
          this.compareByArray.add(separatedValue);
        }
      } else {
        this.compareByArray.add(compareBy);
      }
    }

    @Override
    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
      for (String compareBy : compareByArray) {
        int ascending = 1;
        if (compareBy.startsWith("-")) {
          ascending = -1;
          compareBy = compareBy.substring(1);
        }
        final Object v1 = o1.get(compareBy);
        final Object v2 = o2.get(compareBy);
        if (v1 == null) {
          return -1 * ascending;
        } else if (v2 == null) {
          return 1 * ascending;
        }
        int returnValue = 0;
        if (v1 instanceof Date && v2 instanceof Date) {
          returnValue = ((Date) v1).compareTo((Date) v2) * ascending;
        } else if (v1 instanceof Timestamp && v2 instanceof Timestamp) {
          returnValue = ((Timestamp) v1).compareTo((Timestamp) v2) * ascending;
        } else if (v1 instanceof Long && v2 instanceof Long) {
          returnValue = ((Long) v1).compareTo((Long) v2) * ascending;
        } else if (v1 instanceof BigDecimal && v2 instanceof BigDecimal) {
          returnValue = ((BigDecimal) v1).compareTo((BigDecimal) v2) * ascending;
        } else if (v1 instanceof String && v2 instanceof String) {
          returnValue = ((String) v1).compareTo((String) v2) * ascending;
        } else {
          log.warn("Comparing on property " + compareBy + " for objects " + v1 + "/" + v2 + ". "
              + "But value is are of different classes or an instance of a not supported class. "
              + "Returning default compare value.");
          returnValue = 0;
        }
        if (returnValue != 0) {
          return returnValue;
        }
      }
      return 0;
    }
  }

}
