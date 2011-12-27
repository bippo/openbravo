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
package org.openbravo.userinterface.selector;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.kernel.reference.StringUIDefinition;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.datasource.DataSourceFilter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.json.QueryBuilder.TextMatching;

/**
 * Implements the a datasource filter request for the selectors. Used to generates Hibernate where
 * clauses based on dynamic expressions (JavaScript)
 * 
 * @author iperdomo
 */
public class SelectorDataSourceFilter implements DataSourceFilter {

  private static Logger log = Logger.getLogger(SelectorDataSourceFilter.class);
  private String dateFormat = null;
  private DateFormat systemDateFormat = null;
  private TextMatching textMatching = TextMatching.exact;

  public SelectorDataSourceFilter() {
  }

  @Override
  public void doFilter(Map<String, String> parameters, HttpServletRequest request) {

    final long t1 = System.currentTimeMillis();

    try {

      OBContext.setAdminMode();

      String selectorId = parameters.get(SelectorConstants.DS_REQUEST_SELECTOR_ID_PARAMETER);
      String requestType = parameters.get(SelectorConstants.DS_REQUEST_TYPE_PARAMETER);

      if (selectorId == null || selectorId.equals("")) {
        return;
      }

      Selector sel = OBDal.getInstance().get(Selector.class, selectorId);

      OBCriteria<SelectorField> sfc = OBDal.getInstance().createCriteria(SelectorField.class);
      sfc.add(Restrictions.isNotNull(SelectorField.PROPERTY_DEFAULTEXPRESSION));
      sfc.add(Restrictions.eq(SelectorField.PROPERTY_OBUISELSELECTOR, sel));

      if ((sel.getFilterExpression() == null || sel.getFilterExpression().equals(""))
          && sfc.count() == 0) { // Nothing to filter
        return;
      }

      // Applying filter expression
      applyFilterExpression(sel, parameters, request);

      // Applying default expression for selector fields when is not a selector window request
      if (!"Window".equals(requestType)) {
        applyDefaultExpressions(sel, parameters, sfc, request);
        verifyPropertyTypes(sel, parameters);
      }

    } catch (Exception e) {
      log.error("Error executing filter: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
      log.debug("doFilter took: " + (System.currentTimeMillis() - t1) + "ms");
    }
  }

  /**
   * This method verifies that in the parameters, there are not numeric or date values. In case that
   * it finds numeric or date parameter, these are deleted
   * 
   * @author jecharri
   */
  private void verifyPropertyTypes(Selector sel, Map<String, String> parameters) {
    String value = parameters.get("criteria");
    if (value == null) {
      return;
    }
    boolean isCustomQuerySelector = sel.getHQL() != null;
    String filteredCriteria = "";
    String fieldName;
    Entity entity = ModelProvider.getInstance().getEntityByTableName(
        sel.getTable().getDBTableName());
    Entity cEntity = null;
    try {
      OBContext.setAdminMode(true);
      if (value.contains(JsonConstants.IN_PARAMETER_SEPARATOR)) {
        final String[] separatedValues = value.split(JsonConstants.IN_PARAMETER_SEPARATOR);
        for (String separatedValue : separatedValues) {
          cEntity = entity;
          JSONObject jSONObject = new JSONObject(separatedValue);
          fieldName = (String) jSONObject.get("fieldName");
          if (fieldName.contains("_dummy") || fieldName.contains("_identifier")
              || fieldName.contains("searchKey")) {
            filteredCriteria += jSONObject.toString() + JsonConstants.IN_PARAMETER_SEPARATOR;
            continue;
          }
          boolean filterParameter = false;
          if (isCustomQuerySelector) {
            // This is a custom query selector. We cannot filter parameters by linking them to
            // entity properties
            // Instead, we will do it by checking the references of the fields
            for (SelectorField field : sel.getOBUISELSelectorFieldList()) {
              if (field.isSearchinsuggestionbox()) {
                if (field.getDisplayColumnAlias().equals(fieldName)) {
                  UIDefinition uiDef = UIDefinitionController.getInstance().getUIDefinition(
                      field.getReference());
                  if (!(uiDef instanceof StringUIDefinition)) {
                    filterParameter = true;
                  }
                }
              }
            }
          } else {
            // A property in the entity is searched for this fieldName
            // If the property is numeric or date, then it is filtered
            String[] fieldNameSplit = fieldName.split("\\.");
            Property fProp = null;
            if (fieldNameSplit.length == 1) {
              fProp = entity.getProperty(fieldName);
            } else {
              for (int i = 0; i < fieldNameSplit.length; i++) {
                fProp = cEntity.getProperty(fieldNameSplit[i]);
                if (i != fieldNameSplit.length - 1) {
                  cEntity = fProp.getReferencedProperty().getEntity();
                }
              }
            }
            if (fProp.isNumericType() || fProp.isDate()) {
              filterParameter = true;
            }
          }
          if (filterParameter) {
            try {
              jSONObject.put("operator", "equals");
              BigDecimal valueJSONObject = new BigDecimal(jSONObject.get("value").toString());
              jSONObject.put("value", valueJSONObject);
              filteredCriteria += jSONObject.toString() + JsonConstants.IN_PARAMETER_SEPARATOR;
            } catch (Exception ex) {
              // do nothing
            }
          } else {
            filteredCriteria += jSONObject.toString() + JsonConstants.IN_PARAMETER_SEPARATOR;
          }
        }
        parameters.put("criteria", filteredCriteria.substring(0, (filteredCriteria.length() - 5)));
      }
    } catch (Exception ex) {
      log.error("Error converting to JSON object: " + ex.getMessage(), ex);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Evaluates the Selector filter expression and modifies the parameters map for data filtering
   */
  private void applyFilterExpression(Selector sel, Map<String, String> parameters,
      HttpServletRequest request) {

    if (sel.getFilterExpression() == null) {
      return;
    }

    Object result = null;
    String dynamicWhere = "";

    try {
      result = ParameterUtils.getJSExpressionResult(parameters, request.getSession(),
          sel.getFilterExpression());
      if (result != null && !result.toString().equals("")) {
        dynamicWhere = result.toString();
      }
    } catch (Exception e) {
      log.error("Error evaluating filter expression: " + e.getMessage(), e);
    }

    if (!dynamicWhere.equals("")) {
      log.debug("Adding to where clause (based on filter expression): " + dynamicWhere);

      String currentWhere = parameters.get(JsonConstants.WHERE_PARAMETER);

      if (currentWhere == null || currentWhere.equals("null") || currentWhere.equals("")) {
        parameters.put(JsonConstants.WHERE_PARAMETER, dynamicWhere);
      } else {
        parameters.put(JsonConstants.WHERE_PARAMETER, currentWhere + " and " + dynamicWhere);
      }
    }
  }

  /**
   * Evaluates the default expressions and modifies the parameters map for data filtering
   */
  private void applyDefaultExpressions(Selector sel, Map<String, String> parameters,
      OBCriteria<SelectorField> sfc, HttpServletRequest request) {

    if (sfc.count() == 0) {
      return;
    }

    Object result = null;
    StringBuffer sb = new StringBuffer();
    String textMatchingName = null;

    if (parameters.containsKey(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE)) {
      textMatchingName = parameters.get(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE);
    } else {
      textMatchingName = parameters.get(JsonConstants.TEXTMATCH_PARAMETER);
    }

    if (textMatchingName != null) {
      for (TextMatching txtMatching : TextMatching.values()) {
        if (txtMatching.name().equals(textMatchingName)) {
          textMatching = txtMatching;
          break;
        }
      }
    }

    Entity entity = ModelProvider.getInstance().getEntityByTableId(sel.getTable().getId());

    for (SelectorField sf : sfc.list()) {
      // skip selector fields which do not have a property defined (needed for selector definitions
      // using a custom query
      if (sf.getProperty() == null) {
        continue;
      }

      // Skip values from the request
      if (parameters.get(sf.getProperty()) != null) {
        log.debug("Skipping the default value evaluation for property: " + sf.getProperty()
            + " - value from request: " + parameters.get(sf.getProperty()));
        continue;
      }

      final List<Property> properties = JsonUtils.getPropertiesOnPath(entity, sf.getProperty());

      if (properties.isEmpty()) {
        continue;
      }

      final Property property = properties.get(properties.size() - 1);

      try {
        result = ParameterUtils.getJSExpressionResult(parameters, request.getSession(),
            sf.getDefaultExpression());

        if (result == null || result.toString().equals("")) {
          continue;
        }

        if (sb.length() > 0) {
          sb.append(" and ");
        }

        // Code duplicated from org.openbravo.service.json.QueryBuilder
        // Used to identify the type of property and modify the _where parameter
        // If the this code change, make sure you check the getWhereClause method of the
        // QueryBuilder. Check issue https://issues.openbravo.com/view.php?id=14239

        if (!property.isPrimitive()) {
          sb.append("e." + sf.getProperty() + ".id = '" + result.toString() + "'");
        } else if (String.class == property.getPrimitiveObjectType()) {
          if (textMatching == TextMatching.exact) {
            sb.append("e." + sf.getProperty() + " = '" + result.toString() + "'");
          } else if (textMatching == TextMatching.startsWith) {
            sb.append("upper(" + "e." + sf.getProperty() + ") like '"
                + result.toString().toUpperCase() + "%'");
          } else {
            sb.append("upper(" + "e." + sf.getProperty() + ") like '%"
                + result.toString().toUpperCase().replaceAll(" ", "%") + "%'");
          }
        } else if (Boolean.class == property.getPrimitiveObjectType() || property.isNumericType()) {
          sb.append("e." + sf.getProperty() + " = " + result.toString());
        } else if (Date.class.isAssignableFrom(property.getPrimitiveObjectType())) {

          if (dateFormat == null || systemDateFormat == null) {
            dateFormat = (String) request.getSession(false).getAttribute("#AD_JAVADATEFORMAT");
            systemDateFormat = new SimpleDateFormat(dateFormat);
          }

          try {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(systemDateFormat.parse(result.toString()));
            sb.append("(day(" + "e." + sf.getProperty() + ") = " + cal.get(Calendar.DATE)
                + " and month(" + "e." + sf.getProperty() + ") = " + (cal.get(Calendar.MONTH) + 1)
                + " and year(" + "e." + sf.getProperty() + ") = " + cal.get(Calendar.YEAR) + ")");
          } catch (Exception e) {
            log.error("Error trying to parse date for property " + sf.getProperty(), e);
          }

        }
      } catch (Exception e) {
        log.error("Error evaluating filter expression: " + sf.getDefaultExpression(), e);
      }
    }

    if (sb.length() == 0) {
      return;
    }

    log.debug("Adding to where clause (based on fields default expression): " + sb.toString());

    String currentWhere = parameters.get(JsonConstants.WHERE_PARAMETER);

    if (currentWhere == null || currentWhere.equals("null") || currentWhere.equals("")) {
      parameters.put(JsonConstants.WHERE_PARAMETER, sb.toString());
    } else {
      parameters.put(JsonConstants.WHERE_PARAMETER, currentWhere + " and " + sb.toString());
    }
  }
}
