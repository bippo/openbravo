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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;

/**
 * A base data source service which can be extended. It combines the common parts for data sources
 * which are based on an entity and full-computed data sources.
 * 
 * @author mtaal
 */
public abstract class BaseDataSourceService implements DataSourceService {
  private static final Logger log = Logger.getLogger(BaseDataSourceService.class);

  private static final long serialVersionUID = 1L;

  private String name;
  private Template template;

  // TODO: move this to a config parameter
  private String dataUrl = DataSourceServlet.getServletPathPart() + "/";

  private String whereClause = null;
  private Entity entity;
  private DataSource dataSource;
  private List<DataSourceProperty> dataSourceProperties = new ArrayList<DataSourceProperty>();

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSourceService#getTemplate()
   */
  public Template getTemplate() {
    if (template == null) {
      template = OBDal.getInstance().get(Template.class, DataSourceConstants.DS_TEMPLATE_ID);
      if (template == null) {
        log.error("The default data source template with id " + DataSourceConstants.DS_TEMPLATE_ID
            + " is not present in the database. This is an error!");
      }
    }
    return template;
  }

  /**
   * @deprecated returned class {@link DataSourceJavaScriptCreator} is deprecated
   */
  @Deprecated
  protected DataSourceJavaScriptCreator getJavaScriptCreator() {
    return new DataSourceJavaScriptCreator();
  }

  public String getName() {
    return name;
  }

  public String getDataUrl() {
    return dataUrl;
  }

  public void setDataUrl(String dataUrl) {
    this.dataUrl = dataUrl;
  }

  public String getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(String whereClause) {
    this.whereClause = whereClause;
  }

  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters) {
    return dataSourceProperties;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    setName(dataSource.getId());
    dataSourceProperties = new ArrayList<DataSourceProperty>();
    for (DatasourceField dsField : dataSource.getOBSERDSDatasourceFieldList()) {
      if (dsField.isActive()) {
        dataSourceProperties.add(DataSourceProperty.createFromDataSourceField(dsField));
      }
    }
    if (dataSource.getTable() != null) {
      setEntity(ModelProvider.getInstance().getEntity(dataSource.getTable().getName()));
    }
    setWhereClause(dataSource.getHQLWhereClause());
  }

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public void setName(String name) {
    this.name = name;
  }
}
