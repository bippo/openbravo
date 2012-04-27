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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DatetimeDomainType;
import org.openbravo.base.model.domaintype.EnumerateDomainType;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.DefaultJsonDataService;
import org.openbravo.service.json.DefaultJsonDataService.QueryResultWriter;
import org.openbravo.service.json.JsonConstants;

/**
 * The default implementation of the {@link DataSourceService}. Supports data retrieval, update
 * operations as well as creation of the datasource in javascript.
 * 
 * Makes extensive use of the {@link DefaultJsonDataService}. Check the javadoc on that class for
 * more information.
 * 
 * @author mtaal
 */
public class DefaultDataSourceService extends BaseDataSourceService {
  private static final long serialVersionUID = 1L;
  private static final Logger log4j = Logger.getLogger(DefaultDataSourceService.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#fetch(java.util.Map)
   */
  public String fetch(Map<String, String> parameters) {
    OBContext.setAdminMode(true);
    try {
      addFetchParameters(parameters);
      return DefaultJsonDataService.getInstance().fetch(parameters);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void fetch(Map<String, String> parameters, QueryResultWriter writer) {
    OBContext.setAdminMode(true);
    try {
      addFetchParameters(parameters);
      DefaultJsonDataService.getInstance().fetch(parameters, writer);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void addFetchParameters(Map<String, String> parameters) {

    if (getEntity() != null) {
      parameters.put(JsonConstants.ENTITYNAME, getEntity().getName());
    }

    if (getWhereClause() != null) {
      if (parameters.get(JsonConstants.WHERE_PARAMETER) != null) {
        final String currentWhere = parameters.get(JsonConstants.WHERE_PARAMETER);
        parameters.put(JsonConstants.WHERE_PARAMETER, "(" + currentWhere + ") and ("
            + getWhereClause() + ")");
      } else {
        parameters.put(JsonConstants.WHERE_PARAMETER, getWhereClause());
      }
    }

    // add a filter on the parent of the entity
    if (parameters.get(JsonConstants.FILTERBYPARENTPROPERTY_PARAMETER) != null
        && parameters.containsKey(JsonConstants.TARGETRECORDID_PARAMETER)) {
      final String parentProperty = parameters.get(JsonConstants.FILTERBYPARENTPROPERTY_PARAMETER);
      final BaseOBObject bob = OBDal.getInstance().get(getEntity().getName(),
          (String) parameters.get(JsonConstants.TARGETRECORDID_PARAMETER));

      // a special case, a child tab actually displays the parent record
      // but a different set of information of that record
      final String parentId;
      if (bob.getId().equals(bob.get(parentProperty))) {
        parentId = (String) bob.getId();
      } else {
        parentId = (String) DalUtil.getId((BaseOBObject) bob.get(parentProperty));
      }

      final String whereClause;
      if (parameters.get(JsonConstants.WHERE_PARAMETER) != null
          && !parameters.get(JsonConstants.WHERE_PARAMETER).equals("null")) {
        whereClause = parameters.get(JsonConstants.WHERE_PARAMETER) + " and (";
      } else {
        whereClause = " (";
      }
      parameters.put(JsonConstants.WHERE_PARAMETER, whereClause + JsonConstants.MAIN_ALIAS + "."
          + parentProperty + ".id='" + parentId + "')");
    }

    parameters.put(JsonConstants.USE_ALIAS, "true");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#remove(java.util.Map)
   */
  @Override
  public String remove(Map<String, String> parameters) {
    OBContext.setAdminMode(true);
    try {
      parameters.put(JsonConstants.ENTITYNAME, getEntity().getName());
      return DefaultJsonDataService.getInstance().remove(parameters);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#add(java.util.Map, java.lang.String)
   */
  @Override
  public String add(Map<String, String> parameters, String content) {
    OBContext.setAdminMode(true);
    try {
      parameters.put(JsonConstants.ENTITYNAME, getEntity().getName());
      testAccessPermissions(parameters, content);
      return DefaultJsonDataService.getInstance().add(parameters, content);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#update(java.util.Map, java.lang.String)
   */
  @Override
  public String update(Map<String, String> parameters, String content) {
    OBContext.setAdminMode(true);
    try {
      parameters.put(JsonConstants.ENTITYNAME, getEntity().getName());
      testAccessPermissions(parameters, content);
      return DefaultJsonDataService.getInstance().update(parameters, content);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void testAccessPermissions(Map<String, String> parameters, String content) {
    try {
      if (parameters.get("tabId") == null) {
        return;
      }
      final Tab tab = OBDal.getInstance().get(Tab.class, parameters.get("tabId"));
      if (tab == null) {
        return;
      }
      final String roleId = OBContext.getOBContext().getRole().getId();

      final JSONObject jsonObject = new JSONObject(content);
      if (content == null) {
        return;
      }

      final JSONObject data = jsonObject.getJSONObject("data");
      String id = null;
      if (data.has(JsonConstants.ID)) {
        id = data.getString(JsonConstants.ID);
      }
      // if there is a new indicator then nullify the id again to treat the object has new
      final boolean isNew = data.has(JsonConstants.NEW_INDICATOR)
          && data.getBoolean(JsonConstants.NEW_INDICATOR);
      if (isNew) {
        id = null;
      }

      String entityName = null;
      if (!data.has(JsonConstants.ENTITYNAME) && parameters.containsKey(JsonConstants.ENTITYNAME)) {
        data.put(JsonConstants.ENTITYNAME, parameters.get(JsonConstants.ENTITYNAME));
      }
      if (data.has(JsonConstants.ENTITYNAME)) {
        entityName = data.getString(JsonConstants.ENTITYNAME);
      }
      if (entityName == null) {
        throw new IllegalArgumentException("Entity name not defined in jsonobject " + data);
      }
      final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
          DataToJsonConverter.class);
      final BaseOBObject oldDataObject = id == null ? null : OBDal.getInstance()
          .get(entityName, id);
      final JSONObject oldData = oldDataObject == null ? null : toJsonConverter.toJsonObject(
          oldDataObject, DataResolvingMode.FULL);
      final OBQuery<Field> fieldQuery = OBDal
          .getInstance()
          .createQuery(
              Field.class,
              "as f where f.tab.id = :tabId"
                  + " and (exists (from f.aDFieldAccessList fa where fa.tabAccess.windowAccess.role.id = :roleId and fa.editableField = false and fa.ischeckonsave = true)"
                  + "      or (not exists (from f.aDFieldAccessList fa where fa.tabAccess.windowAccess.role.id = :roleId)"
                  + "          and exists (from f.tab.aDTabAccessList ta where ta.windowAccess.role.id = :roleId and ta.editableField = false)"
                  + "          or not exists (from f.tab.aDTabAccessList  ta where  ta.windowAccess.role.id = :roleId)"
                  + "          and exists (from ADWindowAccess wa where f.tab.window = wa.window and wa.role.id = :roleId and wa.editableField = false)))");
      fieldQuery.setNamedParameter("tabId", tab.getId());
      fieldQuery.setNamedParameter("roleId", roleId);
      final Entity entity = ModelProvider.getInstance().getEntity(entityName);
      for (Field f : fieldQuery.list()) {
        String key = entity.getPropertyByColumnName(f.getColumn().getDBColumnName().toLowerCase())
            .getName();
        if (data.has(key)) {
          String newValue = getValue(data, key);
          String oldValue = getValue(oldData, key);
          if (oldValue == null && newValue != null || oldValue != null
              && !oldValue.equals(newValue)) {
            throw new RuntimeException(KernelUtils.getInstance().getI18N(
                "OBSERDS_RoleHasNoFieldAccess",
                new String[] { OBContext.getOBContext().getRole().getName(), f.getName() }));
          }
        }
      }

    } catch (JSONException e) {
      log4j.error("Unable to test access", e);
      throw new RuntimeException("Unable to test access", e);
    }
  }

  private static final String getValue(JSONObject jsonObject, String prop) throws JSONException {
    if (jsonObject == null)
      return null;
    if (!jsonObject.has(prop))
      return null;
    Object val = jsonObject.get(prop);
    if (JSONObject.NULL.equals(val) || val == null || val instanceof String
        && ((String) val).trim().equals(""))
      return null;
    else
      return val.toString();
  }

  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters) {
    final Entity entity = getEntity();
    final List<DataSourceProperty> dsProperties;
    if (entity == null) {
      dsProperties = super.getDataSourceProperties(parameters);
    } else {
      dsProperties = getInitialProperties(entity,
          parameters.containsKey(DataSourceConstants.MINIMAL_PROPERTY_OUTPUT));
    }

    // now see if there are additional properties, these are often property paths
    final String additionalPropParameter = (String) parameters
        .get(JsonConstants.ADDITIONAL_PROPERTIES_PARAMETER);
    final StringBuilder additionalProperties = new StringBuilder();
    if (additionalPropParameter != null) {
      additionalProperties.append(additionalPropParameter);
    }

    // get the additionalproperties from the properties
    for (DataSourceProperty dsProp : dsProperties) {
      final Map<String, Object> params = dsProp.getUIDefinition().getDataSourceParameters();
      String additionalProps = (String) params.get(JsonConstants.ADDITIONAL_PROPERTIES_PARAMETER);
      if (additionalProps != null) {
        final String[] additionalPropValues = additionalProps.toString().split(",");
        for (String addProp : additionalPropValues) {
          if (additionalProperties.length() > 0) {
            additionalProperties.append(",");
          }
          additionalProperties.append(dsProp.getName() + "." + addProp);
        }
      }
    }

    if (additionalProperties.length() > 0 && getEntity() != null) {
      final String[] additionalProps = additionalProperties.toString().split(",");

      // the additional properties are passed back using a different name
      // than the original property
      for (String additionalProp : additionalProps) {
        final Property property = DalUtil.getPropertyFromPath(entity, additionalProp);
        final DataSourceProperty dsProperty = DataSourceProperty.createFromProperty(property);
        dsProperty.setAdditional(true);
        dsProperty.setName(additionalProp);
        dsProperties.add(dsProperty);
      }
    }
    return dsProperties;
  }

  protected List<DataSourceProperty> getInitialProperties(Entity entity, boolean minimalProperties) {
    if (entity == null) {
      return Collections.emptyList();
    }

    final List<DataSourceProperty> result = new ArrayList<DataSourceProperty>();
    for (Property prop : entity.getProperties()) {
      if (prop.isOneToMany()) {
        continue;
      }

      // if minimal then only generate date properties
      // and the id itself
      if (!prop.isId()
          && minimalProperties
          && !(prop.getDomainType() instanceof EnumerateDomainType)
          && !(prop.getDomainType() instanceof DateDomainType || prop.getDomainType() instanceof DatetimeDomainType)) {
        continue;
      }

      result.add(DataSourceProperty.createFromProperty(prop));
    }
    return result;
  }
}