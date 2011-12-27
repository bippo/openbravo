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
package org.openbravo.client.application;

import org.codehaus.jettison.json.JSONObject;

/**
 * 
 * @author iperdomo
 */

public class ApplicationConstants {
  public static final String TAB_TITLE = "tabTitle";
  public static final String VIEWID = "viewId";

  public static final String COMMAND = "command";
  public static final String SAVE_COMMAND = "save";
  public static final String DATA_COMMAND = "data";
  public static final String CHANGE_PWD_COMMAND = "changePwd";

  public static final String START_PAGE_PROPERTY = "StartPage";
  public static final String LOGIN_PAGE_PROPERTY = "LoginPage";

  public static final JSONObject ACTION_RESULT_SUCCESS;
  public static final String COMPONENT_TYPE = "OBUIAPP_MainLayout";
  public static final String MAIN_LAYOUT_ID = "Application";
  public static final String MAIN_LAYOUT_VIEW_COMPONENT_ID = "View";
  public static final String MAIN_LAYOUT_TEMPLATE_ID = "9E97FF309FE44C61A761F50801F79349";

  public static final String PROPERTIES_TEMPLATE_ID = "DA488BBACB294198AA36A93F03F9561B";
  public static final String PROPERTIES_COMPONENT_ID = "Properties";
  public static final String PROPERTY_PARAMETER = "property";

  // Processes constants
  public static final String WINDOW_REFERENCE_ID = "FF80818132D8F0F30132D9BC395D0038";
  public static final String BUTTON_LIST_REFERENCE_ID = "FF80818132F94B500132F9575619000A";

  // Identifier of the key holding the selected rows in a grid
  public static final String SELECTION_PROPERTY = "_selection";

  // Identifier of the parameter key holding all rows in the grid
  public static final String ALL_ROWS_PARAM = "_allRows";

  // Identifier of the key holding the value of the button clicked
  public static final String BUTTON_VALUE = "_buttonValue";

  static {
    try {
      ACTION_RESULT_SUCCESS = new JSONObject("{result: 'success'}");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
