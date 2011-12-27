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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;

/**
 * Provides {@link DataSourceComponent}. The component is initialized by reading the
 * {@link DataSourceService} instance through the {@link DataSourceServiceProvider}.
 * 
 * @author mtaal
 */
@ApplicationScoped
@ComponentProvider.Qualifier(DataSourceConstants.DS_COMPONENT_TYPE)
public class DataSourceComponentProvider extends BaseComponentProvider {
  public final static String QUALIFIER = DataSourceConstants.DS_COMPONENT_TYPE;

  @Inject
  private DataSourceServiceProvider dataSourceServiceProvider;

  /**
   * Generate the javascript for a specific data source
   * 
   * @param dataSourceId
   * @param parameters
   *          the parameters map, is internally copied to prevent issues with changing the map etc.
   * @return the javascript for creating a datasource on the client.
   */
  public String getDataSourceJavascript(String dataSourceId, Map<String, Object> parameters) {
    // copy the parameters so that they are not accidentally changed...
    final Map<String, Object> dsParameters = new HashMap<String, Object>(parameters);
    dsParameters.put(DataSourceConstants.DS_ONLY_GENERATE_CREATESTATEMENT, true);
    dsParameters.putAll(dsParameters);
    final Component component = getComponent(dataSourceId, dsParameters);
    return component.generate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponent(java.lang.String,
   * java.lang.String, java.util.Map)
   */
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    final DataSourceComponent dataSourceComponent = getComponent(DataSourceComponent.class);
    dataSourceComponent.setParameters(parameters);
    dataSourceComponent.setId(componentId);
    dataSourceComponent.setDataSourceService(dataSourceServiceProvider.getDataSource(componentId));
    return dataSourceComponent;
  }

  /**
   * @return an empty String (no global resources)
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalComponentResources()
   */
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStaticResource(
        "web/org.openbravo.service.datasource/js/ob-datasource-utilities.js", true));
    return globalResources;
  }

  /**
   * @return the package name of the module to which this provider belongs
   */
  public String getModulePackageName() {
    return this.getClass().getPackage().getName();
  }

}
