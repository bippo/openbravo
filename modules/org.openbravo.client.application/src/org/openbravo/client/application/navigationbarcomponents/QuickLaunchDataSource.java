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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.application.MenuManager;
import org.openbravo.client.application.MenuManager.MenuOption;
import org.openbravo.client.application.MenuParameter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.JsonConstants;

/**
 * Reads the tabs which the user is allowed to see.
 * 
 * @author mtaal
 */
public class QuickLaunchDataSource extends ReadOnlyDataSourceService {

  public static final String OPTION_TYPE = "optionType";
  public static final String OPTION_TYPE_EXTERNAL = "external";
  public static final String OPTION_TYPE_URL = "url";
  public static final String OPTION_TYPE_PROCESS = "process";
  public static final String OPTION_TYPE_TAB = "tab";
  public static final String OPTION_SINGLE_RECORD = "singleRecord";
  public static final String OPTION_READ_ONLY = "readOnly";
  public static final String PROCESS_ID = "processId";
  public static final String WINDOW_ID = "windowId";
  public static final String FORM_ID = "formId";

  @Inject
  private MenuManager menuManager;

  /**
   * Returns the count of objects based on the passed parameters.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @return the total number of objects
   */
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, Integer.MAX_VALUE).size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.ReadOnlyDataSourceService#getData(java.util.Map, int,
   * int)
   */
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    OBContext.setAdminMode();
    try {
      final List<MenuOption> menuOptions = menuManager.getSelectableMenuOptions();
      final List<MenuOption> filteredMenuOptions = new ArrayList<MenuOption>();
      String filterOn = parameters.get(JsonConstants.IDENTIFIER);
      if (filterOn != null) {
        filterOn = filterOn.toLowerCase().trim();
      }
      for (MenuOption menuOption : menuOptions) {
        if (filterOn == null || menuOption.getLabel().toLowerCase().contains(filterOn)) {
          filteredMenuOptions.add(menuOption);
        }
      }
      List<MenuOption> returnList = filteredMenuOptions;
      if (startRow > -1 && endRow > -1) {
        if (startRow >= returnList.size()) {
          returnList.clear();
        } else if (endRow >= returnList.size()) {
          returnList = returnList.subList(startRow, returnList.size());
        } else {
          returnList = returnList.subList(startRow, endRow);
        }
      }
      final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
      for (MenuOption menuOption : returnList) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put(JsonConstants.IDENTIFIER, menuOption.getLabel());
        if (menuOption.isWindow()) {
          data.put(OPTION_TYPE, OPTION_TYPE_TAB);
          data.put(WINDOW_ID, menuOption.getMenu().getWindow().getId());
          data.put(OPTION_SINGLE_RECORD, menuOption.isSingleRecord());
          data.put(OPTION_READ_ONLY, menuOption.isReadOnly());
        } else if (menuOption.isProcess()) {
          data.put(OPTION_TYPE, "process");
          data.put(PROCESS_ID, menuOption.getMenu().getProcess().getId());
        } else if (menuOption.isProcessManual()) {
          data.put(OPTION_TYPE, "processManual");
          data.put(PROCESS_ID, menuOption.getMenu().getProcess().getId());
        } else if (menuOption.isExternal()) {
          data.put(OPTION_TYPE, OPTION_TYPE_EXTERNAL);
        } else if (menuOption.isForm()) {
          data.put(OPTION_TYPE, OPTION_TYPE_URL);
          data.put(FORM_ID, menuOption.getFormId());
        } else if (menuOption.isReport()) {
          data.put(OPTION_TYPE, OPTION_TYPE_URL);
          data.put(PROCESS_ID, menuOption.getMenu().getProcess().getId());
        } else {
          data.put(OPTION_TYPE, OPTION_TYPE_URL);
        }

        final String icon;
        if (menuOption.isForm()) {
          icon = "Form";
        } else if (menuOption.isReport()) {
          icon = "Report";
        } else if (menuOption.isProcess() || menuOption.isProcessManual()) {
          icon = "Process";
        } else {
          icon = "Window";
        }
        data.put("icon", icon);

        // use dbid to be sure that it is unique
        data.put(JsonConstants.ID, menuOption.getDbId());
        data.put("viewValue", menuOption.getId());
        data.put("modal", menuOption.isModal());
        for (MenuParameter parameter : menuOption.getMenu().getOBUIAPPMenuParametersList()) {
          if (parameter.isActive()) {
            data.put(parameter.getName(), parameter.getParameterValue());
          }
        }
        // set the view id and tab title
        if (menuOption.getMenu().getObuiappView() != null) {
          data.put(ApplicationConstants.VIEWID, menuOption.getMenu().getObuiappView().getName());
          data.put(ApplicationConstants.TAB_TITLE, menuOption.getLabel());
        }
        result.add(data);
      }
      sort(JsonConstants.IDENTIFIER, result);
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
