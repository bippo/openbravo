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
package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DatetimeDomainType;
import org.openbravo.base.model.domaintype.EnumerateDomainType;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
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
      return DefaultJsonDataService.getInstance().update(parameters, content);
    } finally {
      OBContext.restorePreviousMode();
    }
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