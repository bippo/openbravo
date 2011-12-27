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
package org.openbravo.client.application.navigationbarcomponents;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.service.datasource.DataSourceComponentProvider;

/**
 * Provides a widget to open a classic view from the database.
 * 
 * @author mtaal
 */
public class QuickLaunchComponent extends BaseTemplateComponent {

  private static final String DATASOURCE_ID = "99B9CC42FDEA4CA7A4EE35BC49D61E0E";

  @Inject
  @Any
  private DataSourceComponentProvider dataSourceComponentProvider;

  public String getDataSourceJavascript() {
    return dataSourceComponentProvider.getDataSourceJavascript(DATASOURCE_ID, getParameters());
  }

  public String getPrefixRecent() {
    return "";
  }

  public String getCommand() {
    return "DEFAULT";
  }

  public String getDataSourceId() {
    return DATASOURCE_ID;
  }

  public String getLabel() {
    return "UINAVBA_QUICK_LAUNCH";
  }

  public String getButtonType() {
    return "quickLaunch";
  }

  public String getRecentPropertyName() {
    return "UINAVBA_RecentLaunchList";
  }

  public String getKeyboardShortcutId() {
    return "NavBar_OBQuickLaunch";
  }

  public boolean isQuickLaunch() {
    return true;
  }
}
