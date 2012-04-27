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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.json;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.SearchDomainType;
import org.openbravo.base.model.domaintype.TableDomainType;
import org.openbravo.base.structure.IdentifierProvider;
import org.openbravo.base.util.Check;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.domain.ReferencedTable;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Translates an advanced criteria/filter object into a HQL query. Also takes into account session
 * and other parameters.
 * 
 * @author mtaal
 */
public class AdvancedQueryBuilder {

  private static final String ALIAS_PREFIX = "alias_";
  private static final char ESCAPE_CHAR = '|';

  private static final String OPERATOR_AND = "and";
  static final String OPERATOR_OR = "or";

  private static final String OPERATOR_EQUALS = "equals";
  private static final String OPERATOR_NOTEQUAL = "notEqual";
  private static final String OPERATOR_IEQUALS = "iEquals";
  private static final String OPERATOR_INOTEQUAL = "iNotEqual";
  private static final String OPERATOR_GREATERTHAN = "greaterThan";
  private static final String OPERATOR_LESSTHAN = "lessThan";
  private static final String OPERATOR_GREATEROREQUAL = "greaterOrEqual";
  private static final String OPERATOR_LESSOREQUAL = "lessOrEqual";
  private static final String OPERATOR_IGREATERTHAN = "iGreaterThan";
  private static final String OPERATOR_ILESSTHAN = "iLessThan";
  private static final String OPERATOR_IGREATEROREQUAL = "iGreaterOrEqual";
  private static final String OPERATOR_ILESSOREQUAL = "iLessOrEqual";
  private static final String OPERATOR_CONTAINS = "contains";
  private static final String OPERATOR_STARTSWITH = "startsWith";
  private static final String OPERATOR_ENDSWITH = "endsWith";
  private static final String OPERATOR_ICONTAINS = "iContains";
  private static final String OPERATOR_ISTARTSWITH = "iStartsWith";
  private static final String OPERATOR_IENDSWITH = "iEndsWith";
  private static final String OPERATOR_NOTCONTAINS = "notContains";
  private static final String OPERATOR_NOTSTARTSWITH = "notStartsWith";
  private static final String OPERATOR_NOTENDSWITH = "notEndsWith";
  private static final String OPERATOR_INOTCONTAINS = "iNotContains";
  private static final String OPERATOR_INOTSTARTSWITH = "iNotStartsWith";
  private static final String OPERATOR_INOTENDSWITH = "iNotEndsWith";
  // private static final String OPERATOR_REGEXP = "regexp";
  // private static final String OPERATOR_IREGEXP = "iregexp";
  private static final String OPERATOR_ISNULL = "isNull";
  private static final String OPERATOR_NOTNULL = "notNull";
  private static final String OPERATOR_INSET = "inSet";
  private static final String OPERATOR_NOTINSET = "notInSet";
  private static final String OPERATOR_EQUALSFIELD = "equalsField";
  private static final String OPERATOR_NOTEQUALFIELD = "notEqualField";
  private static final String OPERATOR_GREATERTHANFIElD = "greaterThanField";
  private static final String OPERATOR_LESSTHANFIELD = "lessThanField";
  private static final String OPERATOR_GREATEROREQUALFIELD = "greaterOrEqualField";
  private static final String OPERATOR_LESSOREQUALFIElD = "lessOrEqualField";
  private static final String OPERATOR_CONTAINSFIELD = "containsField";
  private static final String OPERATOR_STARTSWITHFIELD = "startsWithField";
  private static final String OPERATOR_ENDSWITHFIELD = "endsWithField";
  private static final String OPERATOR_NOT = "not";
  private static final String OPERATOR_BETWEEN = "between";
  private static final String OPERATOR_BETWEENINCLUSIVE = "betweenInclusive";
  private static final String OPERATOR_IBETWEEN = "iBetween";
  private static final String OPERATOR_IBETWEENINCLUSIVE = "iBetweenInclusive";

  private static final long serialVersionUID = 1L;

  private JSONObject criteria = null;

  private Map<String, String> filterParameters = new HashMap<String, String>();
  private List<Object> typedParameters = new ArrayList<Object>();
  private Entity entity;
  private String mainAlias = null;
  private int aliasIndex = 0;
  private List<JoinDefinition> joinDefinitions = new ArrayList<JoinDefinition>();
  private String orderBy;

  private String orderByClause = null;
  private String whereClause = null;
  private String joinClause = null;

  // keeps track if during parsing the criteria one or more or's are encountered.
  private int orNesting = 0;

  private SimpleDateFormat simpleDateFormat = JsonUtils.createDateFormat();

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(String entityName) {
    this.entity = ModelProvider.getInstance().getEntity(entityName);
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  /**
   * Translates the filter criteria ({@link #addFilterParameter(String, String)}) to a valid HQL
   * where clause (without the 'where' keyword). After calling this method the method
   * {@link #getNamedParameters()} can be called. Note that currently only filtering on string and
   * boolean properties is supported. Also filtering on the identifier of a referenced business
   * object is supported.
   * 
   * @return a valid where clause or an empty string if not set.
   */
  public String getWhereClause() {

    if (whereClause != null) {
      return whereClause;
    }

    Check.isNotNull(entity, "Entity must be set");

    // parse the criteria themselves
    if (criteria.has("operator")) {
      try {
        whereClause = parseCriteria(criteria);
      } catch (JSONException e) {
        throw new OBException(e);
      }
    }
    if (whereClause == null) {
      whereClause = "";
    }
    whereClause = addWhereOrgParameters(whereClause);
    whereClause = substituteParameters(whereClause);

    if (whereClause.trim().length() > 0) {
      whereClause = " where " + whereClause;
    }

    whereClause += " ";

    return whereClause;
  }

  private String addWhereOrgParameters(String where) {
    String localWhereClause = where;
    // add the organization parameter
    if (filterParameters.containsKey(JsonConstants.ORG_PARAMETER)) {
      final String value = filterParameters.get(JsonConstants.ORG_PARAMETER);
      final StringBuilder orgPart = new StringBuilder();
      if (entity.isOrganizationEnabled() && value != null && value.length() > 0) {
        final Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
            .getNaturalTree(value);
        if (orgs.size() > 0) {
          if (getMainAlias() != null) {
            orgPart.append(" " + getMainAlias() + ".organization in (");
          } else {
            orgPart.append(" organization in (");
          }
          boolean addComma = false;
          for (String org : orgs) {
            if (addComma) {
              orgPart.append(",");
            }
            orgPart.append("'" + org + "'");
            addComma = true;
          }
          orgPart.append(") ");
        }
      }
      if (localWhereClause == null || localWhereClause.length() == 0) {
        localWhereClause = orgPart.length() > 0 ? orgPart.toString() : "";
      } else {
        localWhereClause = "(" + localWhereClause + ")"
            + (orgPart.length() > 0 ? " and " + orgPart.toString() : "");
      }
    }
    // add the special whereParameter
    final String whereParameter = filterParameters.get(JsonConstants.WHERE_PARAMETER);
    if (whereParameter != null && !whereParameter.equals("null") && whereParameter.length() > 0) {
      if (localWhereClause.length() > 0) {
        localWhereClause = " (" + localWhereClause + ") and (" + whereParameter + ") ";
      } else {
        localWhereClause = " " + whereParameter;
      }
    }
    return localWhereClause;
  }

  private String substituteParameters(String where) {

    // add some default filter parameters which are substituted
    filterParameters
        .put(JsonConstants.QUERY_PARAM_USER, OBContext.getOBContext().getUser().getId());
    if (!filterParameters.containsKey(JsonConstants.QUERY_PARAM_CLIENT)) {
      filterParameters.put(JsonConstants.QUERY_PARAM_CLIENT, OBContext.getOBContext()
          .getCurrentClient().getId());
    }

    String localWhereClause = where;
    // handle special transactional range parameter
    if (localWhereClause.contains(JsonConstants.QUERY_PARAM_TRANSACTIONAL_RANGE)) {
      final String alias = getTypedParameterAlias();
      String windowId = RequestContext.get().getRequestParameter("windowId");
      if (windowId == null) {
        windowId = "";
      }
      final String range = Utility.getTransactionalDate(new DalConnectionProvider(false),
          RequestContext.get().getVariablesSecureApp(), windowId);
      final int rangeNum = Integer.parseInt(range);
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_MONTH, -1 * rangeNum);
      localWhereClause = localWhereClause.replace(JsonConstants.QUERY_PARAM_TRANSACTIONAL_RANGE,
          alias);
      typedParameters.add(cal.getTime());
    }

    if (localWhereClause.contains(JsonConstants.QUERY_PARAM_CLIENT)) {
      final String alias = getTypedParameterAlias();
      String clientId = (String) DalUtil.getId(OBContext.getOBContext().getCurrentClient());
      localWhereClause = localWhereClause.replace(JsonConstants.QUERY_PARAM_CLIENT, alias);
      typedParameters.add(clientId);
    }
    localWhereClause = setRequestParameters(localWhereClause);
    return substituteContextParameters(localWhereClause);
  }

  private String parseCriteria(JSONObject jsonCriteria) throws JSONException {
    // a constructor so the content is an advanced criteria
    if (jsonCriteria.has("_constructor") || hasOrAndOperator(jsonCriteria)) {
      return parseAdvancedCriteria(jsonCriteria);
    }
    return parseSingleClause(jsonCriteria);
  }

  private boolean hasOrAndOperator(JSONObject jsonCriteria) throws JSONException {
    if (!jsonCriteria.has("operator")) {
      return false;
    }
    return OPERATOR_OR.equals(jsonCriteria.get("operator"))
        || OPERATOR_AND.equals(jsonCriteria.get("operator"));
  }

  private String parseSingleClause(JSONObject jsonCriteria) throws JSONException {
    String operator = jsonCriteria.getString("operator");

    if (operator.equals(OPERATOR_BETWEEN) || operator.equals(OPERATOR_BETWEENINCLUSIVE)
        || operator.equals(OPERATOR_IBETWEEN) || operator.equals(OPERATOR_IBETWEENINCLUSIVE)) {
      return parseBetween(jsonCriteria, operator, true);
    }

    String fieldName = jsonCriteria.getString("fieldName");
    Object value = jsonCriteria.has("value") ? jsonCriteria.get("value") : null;

    if (operator.equals(OPERATOR_ISNULL) || operator.equals(OPERATOR_NOTNULL)) {
      value = null;
    }

    // if a comparison is done on an equal date then replace
    // with a between start time and end time on that date
    if (operator.equals(OPERATOR_EQUALS) || operator.equals(OPERATOR_EQUALSFIELD)) {
      final List<Property> properties = JsonUtils.getPropertiesOnPath(getEntity(), fieldName);
      if (properties.isEmpty()) {
        return null;
      }
      final Property property = properties.get(properties.size() - 1);
      if (property == null) {
        return null;
      }
      // create the clauses, re-uses the code in parseSimpleClause
      // which translates a lesserthan/greater than to the end/start
      // time of a date
      if (property.isDate() || property.isDatetime()) {
        if (operator.equals(OPERATOR_EQUALS)) {
          return "(" + parseSimpleClause(fieldName, OPERATOR_GREATEROREQUAL, value) + " and "
              + parseSimpleClause(fieldName, OPERATOR_LESSOREQUAL, value) + ")";

        } else {
          return "(" + parseSimpleClause(fieldName, OPERATOR_GREATEROREQUALFIELD, value) + " and "
              + parseSimpleClause(fieldName, OPERATOR_LESSOREQUALFIElD, value) + ")";
        }
      }
    }

    return parseSimpleClause(fieldName, operator, value);
  }

  private String parseBetween(JSONObject jsonCriteria, String operator, boolean inclusive)
      throws JSONException {
    final String fieldName = jsonCriteria.getString("fieldName");
    final Object start = jsonCriteria.get("start");
    final Object end = jsonCriteria.get("end");
    final String leftClause = parseSimpleClause(fieldName, getBetweenOperator(operator, false),
        start);
    final String rightClause = parseSimpleClause(fieldName, getBetweenOperator(operator, true), end);
    if (leftClause != null && rightClause != null) {
      return "(" + leftClause + " and " + rightClause + ")";
    }
    return null;
  }

  private String parseSimpleClause(String fieldName, String operator, Object value)
      throws JSONException {
    // note: code duplicated in parseSingleClause
    final List<Property> properties = JsonUtils.getPropertiesOnPath(getEntity(), fieldName);
    if (properties.isEmpty()) {
      return null;
    }
    final Property property = properties.get(properties.size() - 1);
    if (property == null) {
      return null;
    }

    String leftClause = buildFieldClause(properties, property, fieldName, operator);
    String hqlOperator = getHqlOperator(operator);
    // special case
    if (value != null && value.toString().contains(JsonConstants.IN_PARAMETER_SEPARATOR)) {
      hqlOperator = "in";
    }

    String rightClause = buildRightClause(property, operator, value);

    if (hqlOperator.equals("in")) {
      rightClause = "(" + rightClause + ")";
    }

    if (isNot(operator)) {
      return "not(" + leftClause + " " + hqlOperator + " " + rightClause + ")";
    } else {
      return leftClause + " " + hqlOperator + " " + rightClause;
    }
  }

  private String buildRightClause(Property property, String operator, Object value)
      throws JSONException {
    if (value == null) {
      return null;
    }
    // the right side can be a field
    if (operator.equals(OPERATOR_EQUALSFIELD) || operator.equals(OPERATOR_NOTEQUALFIELD)
        || operator.equals(OPERATOR_GREATERTHANFIElD) || operator.equals(OPERATOR_LESSTHANFIELD)
        || operator.equals(OPERATOR_GREATEROREQUALFIELD)
        || operator.equals(OPERATOR_LESSOREQUALFIElD) || operator.equals(OPERATOR_CONTAINSFIELD)
        || operator.equals(OPERATOR_STARTSWITHFIELD) || operator.equals(OPERATOR_ENDSWITHFIELD)) {
      final List<Property> properties = JsonUtils
          .getPropertiesOnPath(getEntity(), value.toString());
      if (properties.isEmpty()) {
        // invalid property, report it with a listing of allowed names
        final StringBuilder sb = new StringBuilder();
        for (Property prop : getEntity().getProperties()) {
          if (prop.isId() || prop.isOneToMany() || prop.isBoolean() || prop.isDate()
              || prop.isDatetime() || prop.getAllowedValues().size() > 0 || prop.isInactive()
              || prop.isEncrypted() || prop.isOneToOne()) {
            continue;
          }
          if (!prop.isPrimitive()) {
            continue;
          }
          if (sb.length() > 0) {
            sb.append(", ");
          }
          sb.append(prop.getName());
        }
        throw new OBException(OBMessageUtils.getI18NMessage("OBJSON_InvalidProperty",
            new String[] { value.toString(), sb.toString() }));
      }
      final Property fieldProperty = properties.get(properties.size() - 1);
      if (property == null) {
        return null;
      }
      return buildFieldClause(properties, fieldProperty, value.toString(), operator);
    } else {
      return buildValueClause(property, operator, value);
    }
  }

  private String buildFieldClause(List<Property> properties, Property property, String fieldName,
      String operator) {

    // special cases:
    // TableDomainType
    // TableDirDomainType

    // handle a special case the table reference which shows a tablename in a combo
    // or uses the display column to display that in the grid
    Property useProperty = property;
    String useFieldName = fieldName;
    if (properties.size() >= 2) {
      final Property refProperty = properties.get(properties.size() - 2);
      if (refProperty.getDomainType() instanceof TableDomainType) {
        // special case table reference itself
        final boolean isTable = property.getEntity() == ModelProvider.getInstance().getEntity(
            Table.ENTITY_NAME);
        if (isTable) {
          useProperty = property.getEntity().getProperty(Table.PROPERTY_NAME);
          final int index = fieldName.indexOf(".");
          useFieldName = fieldName.substring(0, index + 1) + useProperty.getName();
        } else {
          // read the reference to get the table reference
          final Reference reference = OBDal.getInstance().get(Reference.class,
              refProperty.getDomainType().getReference().getId());
          for (ReferencedTable referencedTable : reference.getADReferencedTableList()) {
            if (referencedTable.isActive() && referencedTable.getDisplayedColumn() != null
                && referencedTable.getDisplayedColumn().isActive()) {
              useProperty = property.getEntity().getPropertyByColumnName(
                  referencedTable.getDisplayedColumn().getDBColumnName());
              final int index = fieldName.indexOf(".");
              useFieldName = fieldName.substring(0, index + 1) + useProperty.getName();
              break;
            }
          }
        }
      }
    }

    String clause = null;
    if (orNesting > 0) {
      clause = resolveJoins(properties, useFieldName);
    } else if (getMainAlias() != null) {
      clause = getMainAlias() + "." + useFieldName.trim();
    } else {
      clause = useFieldName;
    }

    // get rid of the identifier and replace it with the real property name
    // or with the concatenation if there are multiple parts
    // NOTE: the if and else check against the key variable and not the leftwherepart
    // because the key contains the original string (with the _identifier part).
    // Within the if the leftWherePart is used because it contains the join aliases
    if (useFieldName.equals(JsonConstants.IDENTIFIER)
        || useFieldName.endsWith("." + JsonConstants.IDENTIFIER)) {
      if (useFieldName.endsWith("." + JsonConstants.IDENTIFIER)
          && (operator.equals(OPERATOR_ISNULL) || operator.equals(OPERATOR_NOTNULL))) {
        clause = getMainAlias() + "." + useFieldName.replace("." + JsonConstants.IDENTIFIER, "");
      } else {
        clause = computeLeftWhereClauseForIdentifier(useProperty, useFieldName, clause);
      }
    } else if (!useProperty.isPrimitive()) {
      clause = clause + ".id";
    }

    if (ignoreCase(useProperty, operator)) {
      clause = "upper(" + clause + ")";
    }
    return clause;
  }

  private String buildValueClause(Property property, String operator, Object value)
      throws JSONException {
    Object localValue = value;

    // if the value consists of multiple parts then filtering won't work
    // only search on the first part then, is pragmatic but very workable
    if (localValue != null && localValue.toString().contains(IdentifierProvider.SEPARATOR)) {
      final int separatorIndex = localValue.toString().indexOf(IdentifierProvider.SEPARATOR);
      localValue = localValue.toString().substring(0, separatorIndex);
    }

    if (ignoreCase(property, operator)) {
      localValue = localValue.toString().toUpperCase();
    }

    final String alias = getTypedParameterAlias();
    String clause;
    if (isLike(operator)) {
      clause = alias + " escape '" + ESCAPE_CHAR + "' ";
    } else {
      clause = alias;
    }

    if (!property.isPrimitive()) {
      // an in parameter use it...
      if (localValue.toString().contains(JsonConstants.IN_PARAMETER_SEPARATOR)) {
        final List<String> values = new ArrayList<String>();
        final String[] separatedValues = localValue.toString().split(
            JsonConstants.IN_PARAMETER_SEPARATOR);
        for (String separatedValue : separatedValues) {
          values.add(separatedValue);
        }
        clause = "(" + clause + ")";
        localValue = values;
      }
    }

    try {
      localValue = getTypeSafeValue(operator, property, localValue);
    } catch (IllegalArgumentException e) {
      throw new OBException(OBMessageUtils.getI18NMessage("OBJSON_InvalidFilterValue",
          new String[] { value != null ? value.toString() : "" }));
    }
    typedParameters.add(localValue);
    return clause;
  }

  private Object getTypeSafeValue(String operator, Property property, Object value)
      throws JSONException {
    if (value == null) {
      return value;
    }

    // a FK. Old selectors is an special key, though they are not primitive they should be treated
    // as text
    if (!property.isPrimitive() && !(property.getDomainType() instanceof SearchDomainType)) {
      return value;
    }

    if (isLike(operator)) {
      if (operator.equals(OPERATOR_CONTAINS) || operator.equals(OPERATOR_NOTCONTAINS)
          || operator.equals(OPERATOR_INOTCONTAINS) || operator.equals(OPERATOR_ICONTAINS)
          || operator.equals(OPERATOR_CONTAINSFIELD)) {
        return "%" + escapeLike(value.toString().toUpperCase()).replaceAll(" ", "%") + "%";
      } else if (operator.equals(OPERATOR_NOTSTARTSWITH)
          || operator.equals(OPERATOR_INOTSTARTSWITH) || operator.equals(OPERATOR_STARTSWITH)
          || operator.equals(OPERATOR_ISTARTSWITH) || operator.equals(OPERATOR_STARTSWITHFIELD)) {
        return escapeLike(value.toString().toUpperCase()).replaceAll(" ", "%") + "%";
      } else {
        return "%" + escapeLike(value.toString());
      }
    }

    if (operator.equals(OPERATOR_INSET) || operator.equals(OPERATOR_NOTINSET)) {
      final List<Object> typedValues = new ArrayList<Object>();
      final JSONArray values = (JSONArray) value;
      for (int i = 0; i < values.length(); i++) {
        typedValues.add(getTypeSafeValue(OPERATOR_EQUALS, property, values.get(i)));
      }
      return typedValues;
    }

    if (property.getDomainType() instanceof SearchDomainType) {
      return value;
    }

    if (Boolean.class == property.getPrimitiveObjectType()) {
      return new Boolean(value.toString());
    } else if (property.isNumericType()) {
      try {
        final BigDecimal bdValue = new BigDecimal(value.toString());
        if (Long.class == property.getPrimitiveObjectType()) {
          return bdValue.longValue();
        } else if (Integer.class == property.getPrimitiveObjectType()) {
          return bdValue.intValue();
        } else {
          return bdValue;
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(e);
      }
    } else if (Date.class.isAssignableFrom(property.getPrimitiveObjectType())) {
      try {
        final Date date = simpleDateFormat.parse(value.toString());
        // move the date to the beginning of the day
        if (isGreaterOperator(operator)) {
          final Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.set(Calendar.HOUR, 0);
          calendar.set(Calendar.MINUTE, 0);
          calendar.set(Calendar.SECOND, 0);
          calendar.set(Calendar.MILLISECOND, 0);
          return calendar.getTime();
        } else if (isLesserOperator(operator)) {
          // move the data to the end of the day
          final Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.set(Calendar.HOUR, 23);
          calendar.set(Calendar.MINUTE, 59);
          calendar.set(Calendar.SECOND, 59);
          calendar.set(Calendar.MILLISECOND, 999);
          return calendar.getTime();
        } else {
          return date;
        }
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }
    return value;
  }

  private boolean isGreaterOperator(String operator) {
    return operator != null
        && (operator.equals(OPERATOR_GREATERTHAN) || operator.equals(OPERATOR_GREATEROREQUAL)
            || operator.equals(OPERATOR_IGREATERTHAN) || operator.equals(OPERATOR_IGREATEROREQUAL)
            || operator.equals(OPERATOR_GREATERTHANFIElD) || operator
            .equals(OPERATOR_GREATEROREQUALFIELD));
  }

  private boolean isLesserOperator(String operator) {
    return operator != null
        && (operator.equals(OPERATOR_LESSTHAN) || operator.equals(OPERATOR_LESSOREQUAL)
            || operator.equals(OPERATOR_ILESSTHAN) || operator.equals(OPERATOR_ILESSOREQUAL)
            || operator.equals(OPERATOR_LESSTHANFIELD) || operator
            .equals(OPERATOR_LESSOREQUALFIElD));
  }

  private String computeLeftWhereClauseForIdentifier(Property property, String key,
      String leftWherePart) {

    // the identifierProperties are read from the owning entity of the
    // property, that should work fine, as this last property is always part of the
    // identifier
    final List<Property> identifierProperties = property.getEntity().getIdentifierProperties();
    Check.isTrue(identifierProperties.contains(property), "Property " + property
        + " not part of identifier of " + property.getEntity());
    final String prefix;
    final int index = leftWherePart.lastIndexOf(".");
    if (key.equals(JsonConstants.IDENTIFIER)) {
      prefix = getMainAlias() + ".";
    } else if (index == -1) {
      prefix = "";
    } else {
      // the + 1 makes sure that the dot is included
      prefix = leftWherePart.substring(0, index + 1);
    }
    return createIdentifierLeftClause(identifierProperties, prefix);
  }

  private String parseAdvancedCriteria(JSONObject advancedCriteria) throws JSONException {
    final String operator = advancedCriteria.getString("operator");
    if (operator.equals(OPERATOR_NOT)) {
      final String clause = parseStructuredClause(advancedCriteria.getJSONArray("criteria"), "or");
      if (clause != null) {
        return " not(" + clause + ")";
      }
      return null;
    }
    if (operator.equals(OPERATOR_AND)) {
      return parseStructuredClause(advancedCriteria.getJSONArray("criteria"), "and");
    }
    if (operator.equals(OPERATOR_OR)) {
      orNesting++;
      final String value = parseStructuredClause(advancedCriteria.getJSONArray("criteria"), "or");
      orNesting--;
      return value;
    }
    return parseSingleClause(advancedCriteria);
  }

  private String parseStructuredClause(JSONArray clauses, String hqlOperator) throws JSONException {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < clauses.length(); i++) {
      final JSONObject clause = clauses.getJSONObject(i);
      if (clause.has("value") && clause.get("value") != null
          && clause.getString("value").equals("")) {
        continue;
      }
      final String clauseString = parseCriteria(clause);
      if (clauseString != null) {
        if (sb.length() > 0) {
          sb.append(" " + hqlOperator + " ");
        }
        sb.append(" " + clauseString + " ");
      }
    }
    if (sb.length() > 0) {
      return "(" + sb.toString() + ")";
    }
    return null;
  }

  private boolean isLike(String operator) {
    return operator.equals(OPERATOR_ICONTAINS) || operator.equals(OPERATOR_IENDSWITH)
        || operator.equals(OPERATOR_ISTARTSWITH) || operator.equals(OPERATOR_CONTAINS)
        || operator.equals(OPERATOR_ENDSWITH) || operator.equals(OPERATOR_STARTSWITH)
        || operator.equals(OPERATOR_NOTCONTAINS) || operator.equals(OPERATOR_INOTCONTAINS)
        || operator.equals(OPERATOR_NOTENDSWITH) || operator.equals(OPERATOR_NOTSTARTSWITH)
        || operator.equals(OPERATOR_INOTENDSWITH) || operator.equals(OPERATOR_INOTSTARTSWITH)
        || operator.equals(OPERATOR_CONTAINSFIELD) || operator.equals(OPERATOR_ENDSWITHFIELD)
        || operator.equals(OPERATOR_STARTSWITHFIELD);
  }

  private String getBetweenOperator(String operator, boolean rightClause) {
    if (operator.equals(OPERATOR_IBETWEEN)) {
      if (rightClause) {
        return OPERATOR_ILESSTHAN;
      } else {
        return OPERATOR_IGREATERTHAN;
      }
    }
    if (operator.equals(OPERATOR_BETWEEN)) {
      if (rightClause) {
        return OPERATOR_LESSTHAN;
      } else {
        return OPERATOR_GREATERTHAN;
      }
    }
    if (operator.equals(OPERATOR_IBETWEENINCLUSIVE)) {
      if (rightClause) {
        return OPERATOR_ILESSOREQUAL;
      } else {
        return OPERATOR_IGREATEROREQUAL;
      }
    }
    if (operator.equals(OPERATOR_BETWEENINCLUSIVE)) {
      if (rightClause) {
        return OPERATOR_LESSOREQUAL;
      } else {
        return OPERATOR_GREATEROREQUAL;
      }
    }
    throw new IllegalArgumentException("Operator not supported " + operator);
  }

  private boolean ignoreCase(Property property, String operator) {
    if (property.isPrimitive()
        && (property.isNumericType() || property.isDate() || property.isDatetime())) {
      return false;
    }
    return operator.equals(OPERATOR_IEQUALS) || operator.equals(OPERATOR_INOTEQUAL)
        || operator.equals(OPERATOR_CONTAINS) || operator.equals(OPERATOR_ENDSWITH)
        || operator.equals(OPERATOR_STARTSWITH) || operator.equals(OPERATOR_ICONTAINS)
        || operator.equals(OPERATOR_INOTSTARTSWITH) || operator.equals(OPERATOR_INOTENDSWITH)
        || operator.equals(OPERATOR_NOTSTARTSWITH) || operator.equals(OPERATOR_NOTCONTAINS)
        || operator.equals(OPERATOR_INOTCONTAINS) || operator.equals(OPERATOR_NOTENDSWITH)
        || operator.equals(OPERATOR_IENDSWITH) || operator.equals(OPERATOR_ISTARTSWITH)
        || operator.equals(OPERATOR_IBETWEEN) || operator.equals(OPERATOR_IGREATEROREQUAL)
        || operator.equals(OPERATOR_ILESSOREQUAL) || operator.equals(OPERATOR_IGREATERTHAN)
        || operator.equals(OPERATOR_ILESSTHAN) || operator.equals(OPERATOR_IBETWEENINCLUSIVE);
  }

  private boolean isNot(String operator) {
    return operator.equals(OPERATOR_NOTCONTAINS) || operator.equals(OPERATOR_NOTENDSWITH)
        || operator.equals(OPERATOR_NOTSTARTSWITH) || operator.equals(OPERATOR_INOTCONTAINS)
        || operator.equals(OPERATOR_INOTENDSWITH) || operator.equals(OPERATOR_INOTSTARTSWITH)
        || operator.equals(OPERATOR_NOT) || operator.equals(OPERATOR_NOTINSET);
  }

  private String getHqlOperator(String operator) {
    if (operator.equals(OPERATOR_EQUALS)) {
      return "=";
    } else if (operator.equals(OPERATOR_INSET)) {
      return "in";
    } else if (operator.equals(OPERATOR_NOTINSET)) {
      return "in";
    } else if (operator.equals(OPERATOR_NOTEQUAL)) {
      return "!=";
    } else if (operator.equals(OPERATOR_IEQUALS)) {
      return "=";
    } else if (operator.equals(OPERATOR_INOTEQUAL)) {
      return "!=";
    } else if (operator.equals(OPERATOR_GREATERTHAN)) {
      return ">";
    } else if (operator.equals(OPERATOR_LESSTHAN)) {
      return "<";
    } else if (operator.equals(OPERATOR_GREATEROREQUAL)) {
      return ">=";
    } else if (operator.equals(OPERATOR_LESSOREQUAL)) {
      return "<=";
    } else if (operator.equals(OPERATOR_IGREATERTHAN)) {
      return ">";
    } else if (operator.equals(OPERATOR_ILESSTHAN)) {
      return "<";
    } else if (operator.equals(OPERATOR_IGREATEROREQUAL)) {
      return ">=";
    } else if (operator.equals(OPERATOR_ILESSOREQUAL)) {
      return "<=";
    } else if (operator.equals(OPERATOR_CONTAINS)) {
      return "like";
    } else if (operator.equals(OPERATOR_STARTSWITH)) {
      return "like";
    } else if (operator.equals(OPERATOR_ENDSWITH)) {
      return "like";
    } else if (operator.equals(OPERATOR_ICONTAINS)) {
      return "like";
    } else if (operator.equals(OPERATOR_ISTARTSWITH)) {
      return "like";
    } else if (operator.equals(OPERATOR_IENDSWITH)) {
      return "like";
    } else if (operator.equals(OPERATOR_NOTCONTAINS)) {
      return "like";
    } else if (operator.equals(OPERATOR_NOTSTARTSWITH)) {
      return "like";
    } else if (operator.equals(OPERATOR_NOTENDSWITH)) {
      return "like";
    } else if (operator.equals(OPERATOR_INOTCONTAINS)) {
      return "like";
    } else if (operator.equals(OPERATOR_INOTSTARTSWITH)) {
      return "like";
    } else if (operator.equals(OPERATOR_INOTENDSWITH)) {
      return "like";
    } else if (operator.equals(OPERATOR_EQUALSFIELD)) {
      return "=";
    } else if (operator.equals(OPERATOR_NOTEQUALFIELD)) {
      return "!=";
    } else if (operator.equals(OPERATOR_GREATERTHANFIElD)) {
      return ">";
    } else if (operator.equals(OPERATOR_LESSTHANFIELD)) {
      return "<";
    } else if (operator.equals(OPERATOR_GREATEROREQUALFIELD)) {
      return ">=";
    } else if (operator.equals(OPERATOR_LESSOREQUALFIElD)) {
      return "<=";
    } else if (operator.equals(OPERATOR_CONTAINSFIELD)) {
      return "like";
    } else if (operator.equals(OPERATOR_STARTSWITHFIELD)) {
      return "like";
    } else if (operator.equals(OPERATOR_ENDSWITHFIELD)) {
      return "like";
    } else if (operator.equals(OPERATOR_ISNULL)) {
      return "is";
    } else if (operator.equals(OPERATOR_NOTNULL)) {
      return "is not";
    }
    // todo throw exception
    return null;
  }

  private String substituteContextParameters(String currentWhereClause) {
    // This method will check for any remaining @param@s
    // If there are still some in the whereclause, they will be resolved by calling the getContext()
    // method
    if (!currentWhereClause.contains("@")) {
      return currentWhereClause;
    }
    String localWhereClause = currentWhereClause;
    while (localWhereClause.contains("@")) {
      int firstAtIndex = localWhereClause.indexOf("@");
      String prefix = localWhereClause.substring(0, firstAtIndex);
      String restOfClause = localWhereClause.substring(firstAtIndex + 1);
      int secondAtIndex = restOfClause.indexOf("@");
      if (secondAtIndex == -1) {
        // No second @. We return the clause as it is
        return localWhereClause;
      }
      String suffix = restOfClause.substring(secondAtIndex + 1);
      String param = restOfClause.substring(0, secondAtIndex);
      String paramValue = Utility.getContext(new DalConnectionProvider(false), RequestContext.get()
          .getVariablesSecureApp(), param,
          RequestContext.get().getRequestParameter("windowId") != null ? RequestContext.get()
              .getRequestParameter("windowId") : "");
      localWhereClause = prefix + getTypedParameterAlias() + suffix;
      typedParameters.add(paramValue);
    }
    return localWhereClause;
  }

  private String setRequestParameters(String currentWhereClause) {
    // no parameters
    if (!currentWhereClause.contains(DataEntityQueryService.PARAM_DELIMITER)) {
      return currentWhereClause;
    }
    String localWhereClause = currentWhereClause;
    for (String key : filterParameters.keySet()) {
      if (!key.startsWith(DataEntityQueryService.PARAM_DELIMITER)
          || !key.endsWith(DataEntityQueryService.PARAM_DELIMITER)) {
        continue;
      }
      int index = localWhereClause.toLowerCase().indexOf(key.toLowerCase());
      if (index != -1) {
        while (index != -1) {
          // substitute all occurrences of paramater
          localWhereClause = localWhereClause.substring(0, index) + getTypedParameterAlias() + " "
              + localWhereClause.substring(index + key.length());
          typedParameters.add(filterParameters.get(key));
          index = localWhereClause.toLowerCase().indexOf(key.toLowerCase());
        }
      }
    }

    return localWhereClause;
  }

  private String getTypedParameterAlias() {
    return ":" + ALIAS_PREFIX + typedParameters.size();
  }

  /**
   * @return an empty String if there is no join clause, in other cases a String like the following
   *         is returned " as e left join e.bank as alias_1"
   */
  public String getJoinClause() {
    if (joinClause != null) {
      return joinClause;
    }

    // make sure that the join clauses are computed
    getWhereClause();
    getOrderByClause();

    if (getMainAlias() == null) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    sb.append(" as " + getMainAlias() + " ");
    for (JoinDefinition joinDefinition : joinDefinitions) {
      sb.append(joinDefinition.getJoinStatement());
    }
    sb.append(" ");
    joinClause = sb.toString();
    return joinClause;
  }

  /**
   * Converts the value of the sortBy member into a valid order by clause in a HQL query. The method
   * handles special cases as sorting by the identifier properties and descending which is
   * controlled with a minus sign before the property name.
   * 
   * @return a valid order by clause (or an empty string if no sorting)
   */
  protected String getOrderByClause() {
    if (orderByClause != null) {
      return orderByClause;
    }
    if (orderBy == null || orderBy.trim().length() == 0) {
      orderByClause = "";
      return orderByClause;
    }
    final StringBuilder sb = new StringBuilder();
    boolean firstElement = true;
    for (String localOrderBy : orderBy.split(",")) {
      if (!firstElement) {
        sb.append(",");
      }
      sb.append(getOrderByClausePart(localOrderBy.trim()));
      firstElement = false;
    }

    // no order by elements, just use empty string
    if (sb.toString().trim().length() == 0) {
      orderByClause = "";
    } else {
      orderByClause = " order by " + sb.toString();
    }
    return orderByClause;
  }

  protected String getOrderByClausePart(String orderByParam) {
    String localOrderBy = orderByParam;
    final boolean asc = !localOrderBy.startsWith("-");
    String direction = "";
    if (!asc) {
      localOrderBy = localOrderBy.substring(1);
      direction = " desc ";
    }

    final List<String> paths = new ArrayList<String>();

    // handle the following case:
    // table.window.identifier as the sort string
    boolean isIdentifier = localOrderBy.equals(JsonConstants.IDENTIFIER)
        || localOrderBy.endsWith("." + JsonConstants.IDENTIFIER);
    if (isIdentifier) {
      Entity searchEntity = getEntity();
      // a path to an entity, find the last entity
      final String prefix;
      if (!localOrderBy.equals(JsonConstants.IDENTIFIER)) {
        // be lazy get the last property, it belongs to the last entity
        final Property prop = DalUtil.getPropertyFromPath(searchEntity, localOrderBy);
        Check.isNotNull(prop, "Property path " + localOrderBy + " is not valid for entity "
            + searchEntity);
        searchEntity = prop.getEntity();
        prefix = localOrderBy.substring(0, localOrderBy.lastIndexOf(".") + 1);
      } else {
        prefix = "";
      }
      for (Property prop : searchEntity.getIdentifierProperties()) {
        if (prop.isOneToMany()) {
          // not supported ignoring it
          continue;
        }
        if (!prop.isPrimitive()) {
          // get identifier properties from target entity
          // TODO: currently only supports one level, recursive
          // calls have the danger of infinite loops in case of
          // wrong identifier definitions in the AD
          final Entity targetEntity = prop.getTargetEntity();
          for (Property targetEntityProperty : targetEntity.getIdentifierProperties()) {
            paths.add(prefix + prop.getName() + "." + targetEntityProperty.getName());
          }
        } else {
          paths.add(prefix + prop.getName());
        }
      }
    } else {
      paths.add(localOrderBy);
    }

    final StringBuilder sb = new StringBuilder();
    boolean addComma = false;
    for (String path : paths) {
      if (addComma) {
        sb.append(", ");
      }
      addComma = true;

      final String[] orderByExpression = path.split(" "); // e.property DESC
      if (orderByExpression.length > 1) {
        path = orderByExpression[0];
        direction = " " + orderByExpression[1] + " ";
      }

      final String resolvedPath = resolveJoins(JsonUtils.getPropertiesOnPath(getEntity(), path),
          path);
      sb.append(resolvedPath);
      sb.append(direction);
    }
    return sb.toString();
  }

  // Creates a Hibernate concatenation if there are multiple identifierproperties
  // note prefix includes the dot at the end
  private String createIdentifierLeftClause(List<Property> identifierProperties, String prefix) {
    final StringBuilder sb = new StringBuilder();
    for (Property prop : identifierProperties) {
      if (sb.length() > 0) {
        sb.append(" || '" + IdentifierProvider.SEPARATOR + "' || ");
      }
      // note to_char is added to handle null values correctly
      if (prop.getReferencedProperty() == null) {
        if (prop.isTranslatable() && OBContext.hasTranslationInstalled()) {
          // HQL for trl properties. Doing it as a select because it cannot be done as left join.
          // Example:
          //
          // select coalesce(w.name, t.name)
          // from ADWindow w left join w.aDWindowTrlList as t with t.language = :lang
          // where w.id=:window
          //
          // raises: with clause can only reference columns in the driving table

          sb.append("COALESCE(to_char((select " + prop.getTranslationProperty().getName()
              + " from " + prop.getTranslationProperty().getEntity().getName() + " as t where t."
              + prop.getTrlParentProperty().getName() + " = "
              + prefix.substring(0, prefix.lastIndexOf('.')) + " and t.language.language='"
              + OBContext.getOBContext().getLanguage().getLanguage() + "')), to_char(" + prefix
              + prop.getName() + "), '')");

        } else {
          sb.append("COALESCE(to_char(" + prefix + prop.getName() + "),'')");
        }

      } else {
        final List<Property> newIdentifierProperties = prop.getReferencedProperty().getEntity()
            .getIdentifierProperties();
        sb.append(createIdentifierLeftClause(newIdentifierProperties, prefix + prop.getName() + "."));
      }
    }

    return "(" + sb.toString() + ")";
  }

  /**
   * @return true if one of the filter parameters is the {@link JsonConstants#ORG_PARAMETER}.
   */
  public boolean hasOrganizationParameter() {
    final String value = filterParameters.get(JsonConstants.ORG_PARAMETER);
    return value != null && value.trim().length() > 0;
  }

  /**
   * Add a filter parameter, the method {@link #getWhereClause()} will try to convert the String
   * value to a typed parameter.
   * 
   * @param key
   *          the filter key, can be direct property or a referenced property.
   * @param value
   *          the value as a String
   */
  public void addFilterParameter(String key, String value) {
    // ignore these
    if (value == null) {
      return;
    }
    whereClause = null;
    typedParameters.clear();
    filterParameters.put(key, value);
  }

  public Map<String, Object> getNamedParameters() {
    final Map<String, Object> parameters = new HashMap<String, Object>();
    for (int i = 0; i < typedParameters.size(); i++) {
      parameters.put(ALIAS_PREFIX + Integer.toString(i), typedParameters.get(i));
    }
    return parameters;
  }

  public void setDoOr(boolean doOr) {
    if (doOr) {
      orNesting++;
    }
    // in case of join always do outer joining
    setMainAlias(JsonConstants.MAIN_ALIAS);
  }

  // Resolves the list of properties against existing join definitions
  // creates new join definitions when necessary
  private String resolveJoins(List<Property> props, String originalPath) {
    String alias = getMainAlias();
    if (alias == null) {
      return originalPath;
    }
    int index = 0;
    int joinedPropertyIndex = -1;
    for (Property prop : props) {
      boolean found = false;
      for (JoinDefinition joinDefinition : joinDefinitions) {
        if (joinDefinition.appliesTo(alias, prop)) {
          alias = joinDefinition.getJoinAlias();
          joinedPropertyIndex = index;
          found = true;
          break;
        }
      }
      if (!found) {
        // no more joins, leave
        break;
      }
      index++;
    }
    // check if any new JoinDefinitions should be created
    for (int i = (joinedPropertyIndex + 1); i < props.size(); i++) {
      final Property prop = props.get(i);
      if (prop.isPrimitive()) {
        break;
      }
      // a joinable property
      final JoinDefinition joinDefinition = new JoinDefinition();
      joinDefinition.setOwnerAlias(alias);
      joinDefinition.setJoinAlias(getNewUniqueAlias());
      joinDefinition.setProperty(prop);
      joinDefinitions.add(joinDefinition);

      // move the result up to use the new JoinDefinition
      alias = joinDefinition.getJoinAlias();
      joinedPropertyIndex = i;
    }
    if (joinedPropertyIndex == (props.size() - 1)) {
      return alias;
    }
    return alias + "." + props.get(props.size() - 1).getName();
  }

  private String getNewUniqueAlias() {
    return ALIAS_PREFIX + (aliasIndex++);
  }

  private class JoinDefinition {
    private Property property;
    private String joinAlias;
    private String ownerAlias;

    public boolean appliesTo(String checkAlias, Property checkProperty) {
      return checkAlias.equals(ownerAlias) && checkProperty == property;
    }

    public String getJoinStatement() {
      if (orNesting > 0) {
        return " left outer join " + ownerAlias + "." + property.getName() + " as " + joinAlias;
      } else {
        return " left join " + ownerAlias + "." + property.getName() + " as " + joinAlias;
      }
    }

    public void setProperty(Property property) {
      this.property = property;
    }

    public String getJoinAlias() {
      return joinAlias;
    }

    public void setJoinAlias(String joinAlias) {
      this.joinAlias = joinAlias;
    }

    public void setOwnerAlias(String ownerAlias) {
      this.ownerAlias = ownerAlias;
    }
  }

  public String getMainAlias() {
    return mainAlias;
  }

  public void setMainAlias(String mainAlias) {
    this.mainAlias = mainAlias;
  }

  public String getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
    // do outer joining if the order by has more than 1 dot
    if (orderBy.indexOf(".") != -1 && orderBy.indexOf(".") != orderBy.lastIndexOf(".")) {
      setMainAlias(JsonConstants.MAIN_ALIAS);
    }
  }

  private String escapeLike(String value) {
    if (value == null || value.trim().length() == 0) {
      return value;
    }
    String localValue = value.replace(ESCAPE_CHAR + "", ESCAPE_CHAR + ESCAPE_CHAR + "");
    localValue = localValue.replace("_", ESCAPE_CHAR + "_");
    localValue = localValue.replace("%", ESCAPE_CHAR + "%");
    return localValue;
  }

  public JSONObject getCriteria() {
    return criteria;
  }

  public void setCriteria(JSONObject criteria) {
    this.criteria = criteria;
  }
}
