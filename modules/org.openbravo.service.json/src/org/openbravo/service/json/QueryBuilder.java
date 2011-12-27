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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.IdentifierProvider;
import org.openbravo.base.util.Check;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Encapsulates the logic to translate filter properties and values received from the client to a
 * where clauses in the query itself.
 * 
 * @author mtaal
 */
public class QueryBuilder {

  private static final String PARAM_DELIMITER = "@";
  private static final String ALIAS_PREFIX = "alias_";
  private static final char ESCAPE_CHAR = '|';

  public static enum TextMatching {
    startsWith, exact, substring
  }

  private static final Logger log = Logger.getLogger(QueryBuilder.class);

  private static final long serialVersionUID = 1L;

  private Map<String, String> filterParameters = new HashMap<String, String>();
  private List<Object> typedParameters = new ArrayList<Object>();
  private Entity entity;
  private boolean doOr = false;
  private String mainAlias = null;
  private int aliasIndex = 0;
  private List<JoinDefinition> joinDefinitions = new ArrayList<JoinDefinition>();
  private String orderBy;

  private String orderByClause = null;
  private String whereClause = null;
  private String joinClause = null;

  private TextMatching textMatching = TextMatching.exact;

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

    // add some default filter parameters
    filterParameters
        .put(JsonConstants.QUERY_PARAM_USER, OBContext.getOBContext().getUser().getId());
    if (!filterParameters.containsKey(JsonConstants.QUERY_PARAM_CLIENT)) {
      filterParameters.put(JsonConstants.QUERY_PARAM_CLIENT, OBContext.getOBContext().getUser()
          .getId());
    }

    final SimpleDateFormat simpleDateFormat = JsonUtils.createDateFormat();

    Check.isNotNull(entity, "Entity must be set");

    final StringBuilder sb = new StringBuilder();
    boolean addAnd = false;
    final StringBuilder orgPart = new StringBuilder();
    final List<Property> propertyDone = new ArrayList<Property>();
    String whereParameterValue = null;
    for (String key : filterParameters.keySet()) {
      String value = filterParameters.get(key);

      if (key.equals(JsonConstants.WHERE_PARAMETER)) {
        // there are cases where null is set as a string
        // handle this
        if (value.equals("null") || value.length() == 0) {
          continue;
        }
        whereParameterValue = value;
        continue;
      }

      // handle the case that we should filter on the accessible organizations
      if (key.equals(JsonConstants.ORG_PARAMETER)) {
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
        continue;
      }

      // determine the property
      final List<Property> properties = JsonUtils.getPropertiesOnPath(getEntity(), key);
      if (properties.isEmpty()) {
        continue;
      }
      final Property property = properties.get(properties.size() - 1);
      // invalid propname, ignore this one
      // TODO: possibly warn about it
      if (property == null || propertyDone.contains(property)) {
        continue;
      }
      propertyDone.add(property);

      // we know the property and the string representation of the value...
      // do the conversion

      if (addAnd) {
        if (doOr) {
          sb.append(" or ");
        } else {
          sb.append(" and ");
        }
      }

      String leftWherePart = null;
      if (isDoOr()) {
        leftWherePart = resolveJoins(properties, key);
      } else if (getMainAlias() != null) {
        leftWherePart = getMainAlias() + "." + key.trim();
      } else {
        leftWherePart = key;
      }

      // get rid of the identifier and replace it with the real property name
      // or with the concatenation if there are multiple parts
      // NOTE: the if and else check against the key variable and not the leftwherepart
      // because the key contains the original string (with the _identifier part).
      // Within the if the leftWherePart is used because it contains the join aliases
      if (key.equals(JsonConstants.IDENTIFIER) || key.endsWith("." + JsonConstants.IDENTIFIER)) {
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
        leftWherePart = createIdentifierLeftClause(identifierProperties, prefix);

        // if the value consists of multiple parts then filtering won't work
        // only search on the first part then, is pragmatic but very workable
        if (value != null && value.contains(IdentifierProvider.SEPARATOR)) {
          final int separatorIndex = value.indexOf(IdentifierProvider.SEPARATOR);
          value = value.substring(0, separatorIndex);
        }
      }

      // NOTE: If you change this part, make sure that you sync the changes with the
      // SelectorDataSourceFilter. Check issue https://issues.openbravo.com/view.php?id=14239

      // NOTE the typedParameters.add call must be done after the call to
      // getTypedParameterAlias, this to get the correct alias codes
      if (key.equals(JsonConstants.IDENTIFIER)) {
        if (textMatching == TextMatching.exact) {
          sb.append(leftWherePart + " = " + getTypedParameterAlias());
          typedParameters.add(value);
        } else if (textMatching == TextMatching.startsWith) {
          sb.append("upper(" + leftWherePart + ") like " + getTypedParameterAlias() + " escape '"
              + ESCAPE_CHAR + "' ");
          typedParameters.add(escapeLike(value.toUpperCase()) + "%");
        } else {
          sb.append("upper(" + leftWherePart + ") like " + getTypedParameterAlias() + " escape '"
              + ESCAPE_CHAR + "' ");
          typedParameters.add("%" + escapeLike(value.toUpperCase()).replaceAll(" ", "%") + "%");
        }
      } else if (!property.isPrimitive()) {
        // an in parameter use it...
        if (value.contains(JsonConstants.IN_PARAMETER_SEPARATOR)) {
          final List<String> values = new ArrayList<String>();
          final String[] separatedValues = value.split(JsonConstants.IN_PARAMETER_SEPARATOR);
          for (String separatedValue : separatedValues) {
            values.add(separatedValue);
          }
          sb.append(leftWherePart + ".id in (" + getTypedParameterAlias() + ")");
          typedParameters.add(values);
        } else {
          sb.append(leftWherePart + ".id = " + getTypedParameterAlias());
          typedParameters.add(value);
        }
      } else if (String.class == property.getPrimitiveObjectType()) {
        if (textMatching == TextMatching.exact) {
          sb.append(leftWherePart + " = " + getTypedParameterAlias());
          typedParameters.add(value);
        } else if (textMatching == TextMatching.startsWith) {
          sb.append("upper(" + leftWherePart + ") like " + getTypedParameterAlias() + " escape '"
              + ESCAPE_CHAR + "' ");
          typedParameters.add(escapeLike(value.toUpperCase()) + "%");
        } else {
          sb.append("upper(" + leftWherePart + ") like " + getTypedParameterAlias() + " escape '"
              + ESCAPE_CHAR + "' ");
          typedParameters.add("%" + escapeLike(value.toUpperCase()).replaceAll(" ", "%") + "%");
        }
      } else if (Boolean.class == property.getPrimitiveObjectType()) {
        final String alias = getTypedParameterAlias();
        typedParameters.add(new Boolean(value));
        sb.append(leftWherePart + " = " + alias);
      } else if (property.isNumericType()) {
        try {
          final String alias = getTypedParameterAlias();
          final BigDecimal bdValue = new BigDecimal(value);
          if (Long.class == property.getPrimitiveObjectType()) {
            typedParameters.add(bdValue.longValue());
          } else if (Integer.class == property.getPrimitiveObjectType()) {
            typedParameters.add(bdValue.intValue());
          } else {
            typedParameters.add(bdValue);
          }
          sb.append(leftWherePart + " = " + alias);
        } catch (NumberFormatException e) {
          // ignore on purpose, incorrect value entered by user
          // add a dummy whereclause to make the query format correct
          sb.append(" 1=1 ");
        }
      } else if (Date.class.isAssignableFrom(property.getPrimitiveObjectType())) {
        try {
          final Calendar cal = Calendar.getInstance();
          cal.setTime(simpleDateFormat.parse(value));
          final String alias1 = getTypedParameterAlias();
          typedParameters.add(cal.get(Calendar.DATE));
          final String alias2 = getTypedParameterAlias();
          typedParameters.add(cal.get(Calendar.MONTH) + 1);
          final String alias3 = getTypedParameterAlias();
          typedParameters.add(cal.get(Calendar.YEAR));
          sb.append(" (day(" + leftWherePart + ") = " + alias1 + " and month(" + leftWherePart
              + ") = " + alias2 + " and year(" + leftWherePart + ") = " + alias3 + ") ");
        } catch (Exception e) {
          // ignore these errors, just don't filter then
          // add a dummy whereclause to make the query format correct
          sb.append(" 1=1 ");
        }

        // } else if (property.isDate() || property.isDatetime()) {
        // NOTE: dates arrive in the format of the user....
        // sb.append(leftWherePart + " = ?");
        // typedParameters.add(value);
      } else {
        // TODO: support this....
        throw new UnsupportedOperationException("Type " + property.getPrimitiveObjectType()
            + " not yet supported for parameter " + key);
      }
      addAnd = true;
    }

    log.debug("Whereclause for entity " + entity.getName());
    log.debug(sb.toString());
    for (Object param : typedParameters) {
      log.debug(param);
    }
    log.debug("Textmatching " + textMatching);

    if (sb.length() == 0) {
      whereClause = orgPart.length() > 0 ? orgPart.toString() : "";
    } else {
      whereClause = "(" + sb.toString() + ")"
          + (orgPart.length() > 0 ? " and " + orgPart.toString() : "");
    }
    if (whereParameterValue != null) {
      if (whereClause.length() > 0) {
        whereClause = " (" + whereClause + ") and (" + whereParameterValue + ") ";
      } else {
        whereClause = " " + whereParameterValue;
      }
    }
    if (whereClause.trim().length() > 0) {
      whereClause = " where " + whereClause;
    }

    // handle special transactional range parameter
    if (whereClause.contains(JsonConstants.QUERY_PARAM_TRANSACTIONAL_RANGE)) {
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
      whereClause = whereClause.replace(JsonConstants.QUERY_PARAM_TRANSACTIONAL_RANGE, alias);
      typedParameters.add(cal.getTime());
    }

    if (whereClause.contains(JsonConstants.QUERY_PARAM_CLIENT)) {
      final String alias = getTypedParameterAlias();
      String clientId = (String) DalUtil.getId(OBContext.getOBContext().getCurrentClient());
      whereClause = whereClause.replace(JsonConstants.QUERY_PARAM_CLIENT, alias);
      typedParameters.add(clientId);
    }
    whereClause = setRequestParameters(whereClause);
    whereClause = substituteContextParameters(whereClause);

    return whereClause;
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
          .getVariablesSecureApp(), param, RequestContext.get().getRequestParameter("windowId"));
      localWhereClause = prefix + getTypedParameterAlias() + suffix;
      typedParameters.add(paramValue);
    }
    return localWhereClause;
  }

  private String setRequestParameters(String currentWhereClause) {
    // no parameters
    if (!currentWhereClause.contains(PARAM_DELIMITER)) {
      return currentWhereClause;
    }
    String localWhereClause = currentWhereClause;
    for (String key : filterParameters.keySet()) {
      if (!key.startsWith(PARAM_DELIMITER) || !key.endsWith(PARAM_DELIMITER)) {
        continue;
      }
      final int index = localWhereClause.toLowerCase().indexOf(key.toLowerCase());
      if (index != -1) {
        localWhereClause = localWhereClause.substring(0, index) + getTypedParameterAlias() + " "
            + localWhereClause.substring(index + key.length());
        typedParameters.add(filterParameters.get(key));
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
    final StringBuilder sb = new StringBuilder(" order by ");
    boolean firstElement = true;
    for (String localOrderBy : orderBy.split(",")) {
      if (!firstElement) {
        sb.append(",");
      }
      sb.append(getOrderByClausePart(localOrderBy.trim()));
      firstElement = false;
    }
    orderByClause = sb.toString();
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
    boolean isIdenfitier = localOrderBy.equals(JsonConstants.IDENTIFIER)
        || localOrderBy.endsWith("." + JsonConstants.IDENTIFIER);
    if (isIdenfitier) {
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
        sb.append("COALESCE(to_char(" + prefix + prop.getName() + "),'')");
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

  public TextMatching getTextMatching() {
    return textMatching;
  }

  /**
   * The text matching strategy used. See here for a description:
   * http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#attr..ComboBoxItem.textMatchStyle
   * 
   * @param matchStyle
   *          the following values are allowed: startsWith, substring, exact
   */
  public void setTextMatching(TextMatching matchStyle) {
    whereClause = null;
    typedParameters.clear();
    this.textMatching = matchStyle;
  }

  public boolean isDoOr() {
    return doOr;
  }

  public void setDoOr(boolean doOr) {
    this.doOr = doOr;
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
      return " left outer join " + ownerAlias + "." + property.getName() + " as " + joinAlias;
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
}
