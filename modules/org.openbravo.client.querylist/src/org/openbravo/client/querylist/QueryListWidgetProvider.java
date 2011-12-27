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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.querylist;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.myob.WidgetInstance;
import org.openbravo.client.myob.WidgetProvider;
import org.openbravo.service.datasource.DataSourceComponentProvider;
import org.openbravo.service.datasource.DataSourceConstants;

/**
 * Responsible for creating the Query/List Widgets.
 * 
 * @author gorkaion
 */
public class QueryListWidgetProvider extends WidgetProvider {
  private static final String DATASOURCEID = "DD17275427E94026AD721067C3C91C18";
  public static final String WIDGETCLASS_PARAMETER = "WIDGET_CLASS";

  private static final String GRID_PROPERTIES_REFERENCE = "B36DF126DF5F4077A37F1E5B963AA636";
  private static final Logger log = Logger.getLogger(QueryListWidgetProvider.class);

  @Inject
  @ComponentProvider.Qualifier(DataSourceConstants.DS_COMPONENT_TYPE)
  private DataSourceComponentProvider dataSourceComponentProvider;

  @Override
  public String generate() {
    JSONObject gridPropertiesObject = null;
    for (Parameter parameter : getWidgetClass().getOBUIAPPParameterEMObkmoWidgetClassIDList()) {
      // fixed parameters are not part of the fielddefinitions
      if (parameter.getReferenceSearchKey() != null
          && parameter.getReferenceSearchKey().getId().equals(GRID_PROPERTIES_REFERENCE)) {
        try {
          gridPropertiesObject = new JSONObject(parameter.getFixedValue());
        } catch (Exception e) {
          // ignore, invalid grid properties
          log.error(
              "Grid properties parameter " + parameter + " has an illegal format " + e.getMessage(),
              e);
        }
      }
    }
    String gridProperties = (gridPropertiesObject == null ? "" : ", gridProperties: "
        + gridPropertiesObject.toString());
    final String result = "isc.defineClass('"
        + KernelConstants.ID_PREFIX
        + getWidgetClass().getId()
        + "', isc.OBQueryListWidget).addProperties({widgetId: '"
        + getWidgetClass().getId()
        + "', "
        + "gridDataSource: null,"
        + "createGridDataSource: function() {"
        + "return "
        + getDataSourceJavaScript()
        + ";}, fields:"
        + QueryListUtils
            .getWidgetClassFields(getWidgetClass(), QueryListUtils.IncludeIn.WidgetView)
        + ", maximizedFields:"
        + QueryListUtils.getWidgetClassFields(getWidgetClass(),
            QueryListUtils.IncludeIn.MaximizedView) + gridProperties + "});";
    return result;
  }

  public String getDataSourceJavaScript() {
    final Map<String, Object> localParameters = new HashMap<String, Object>();
    localParameters.putAll(getParameters());
    localParameters.put(DataSourceConstants.DS_ONLY_GENERATE_CREATESTATEMENT, true);
    localParameters.put(WIDGETCLASS_PARAMETER, getWidgetClass());
    final Component dsComponent = dataSourceComponentProvider.getComponent(DATASOURCEID,
        localParameters);
    final String dsJavaScript = dsComponent.generate();
    return dsJavaScript;
  }

  public JSONObject getWidgetInstanceDefinition(WidgetInstance widgetInstance) {
    try {
      final JSONObject jsonObject = super.getWidgetInstanceDefinition(widgetInstance);
      jsonObject.put("widgetInstanceId", widgetInstance.getId());

      return jsonObject;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}
