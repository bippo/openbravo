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

import java.util.List;

import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;

/**
 * Represents a datasource to be rendered/created on the client.
 * 
 * @author mtaal
 */
public class DataSourceComponent extends BaseTemplateComponent {

  private DataSourceService dataSourceService;

  @Override
  protected Template getComponentTemplate() {
    return dataSourceService.getTemplate();
  }

  public String getDataSourceClassName() {
    final String clzName = getParameter(DataSourceConstants.DS_CLASS_NAME);
    if (clzName != null) {
      return clzName;
    }
    return "OBRestDataSource";
  }

  public void setDataSourceService(DataSourceService data) {
    dataSourceService = data;
  }

  public DataSourceService getDataSourceService() {
    return dataSourceService;
  }

  public String getName() {
    return dataSourceService.getName();
  }

  public String getId() {
    String postFix = "";
    // create a new datasource id
    if (getParameters().containsKey(DataSourceConstants.NEW_PARAM)) {
      postFix = "_" + System.currentTimeMillis();
    }

    if (getParameters().containsKey(DataSourceConstants.DS_ID)) {
      return getParameter(DataSourceConstants.DS_ID) + postFix;
    }
    return dataSourceService.getName() + postFix;
  }

  public String getDataUrl() {
    return getContextUrl() + dataSourceService.getDataUrl();
  }

  public List<DataSourceProperty> getDataSourceProperties() {
    return dataSourceService.getDataSourceProperties(getParameters());
  }
}
