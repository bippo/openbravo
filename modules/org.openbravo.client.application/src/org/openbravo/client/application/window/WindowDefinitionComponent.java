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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.ApplicationComponentProvider;
import org.openbravo.client.application.MenuManager;
import org.openbravo.client.application.MenuManager.MenuOption;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.dal.core.OBContext;

/**
 * A component which generates javascript directly without a template
 * 
 * @author mtaal
 */
@RequestScoped
public class WindowDefinitionComponent extends BaseComponent {
  public static final String WINDOW_DEF_COMPONENT = "WindowDefinitionComponent";

  @Inject
  private MenuManager menuManager;

  @Override
  public String generate() {
    try {
      final JSONObject result = new JSONObject();
      for (MenuOption menuOption : menuManager.getSelectableMenuOptions()) {
        if (menuOption.getTab() != null) {
          final JSONObject windowDef = new JSONObject();
          result.put(menuOption.getTab().getWindow().getId(), windowDef);
          if (menuOption.isShowInClassicMode()) {
            windowDef.put("showInClassicMode", true);
          }
        }
      }
      return "OB.WindowDefinitions = " + result.toString() + ";";
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  @Override
  public Object getData() {
    return this;
  }

  protected String getModulePackageName() {
    return ApplicationComponentProvider.class.getPackage().getName();
  }

  @Override
  public String getETag() {
    return super.getETag() + "_" + OBContext.getOBContext().getRole().getId();
  }
}
