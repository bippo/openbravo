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
 * All portions are Copyright (C) 2001-2010 Openbravo S.L.U.
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;

class ShowSessionVariablesStructureData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ShowSessionVariablesStructureData.class);
  boolean isPreference = false;
  boolean isAccounting = false;
  boolean isGlobal = false;
  String window;
  String windowName;
  String completeName;
  String name;
  String value;
  String rownum;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("isPreference"))
      return (isPreference ? "Yes" : "NO");
    else if (fieldName.equalsIgnoreCase("isAccounting"))
      return (isAccounting ? "Yes" : "NO");
    else if (fieldName.equalsIgnoreCase("isGlobal"))
      return (isGlobal ? "Yes" : "NO");
    else if (fieldName.equalsIgnoreCase("window"))
      return ((window == null) ? "" : window);
    else if (fieldName.equalsIgnoreCase("windowName"))
      return ((windowName == null) ? "" : windowName);
    else if (fieldName.equalsIgnoreCase("completeName"))
      return ((completeName == null) ? "" : completeName);
    else if (fieldName.equalsIgnoreCase("name"))
      return ((name == null) ? "" : name);
    else if (fieldName.equalsIgnoreCase("value"))
      return ((value == null) ? "" : value);
    else if (fieldName.equalsIgnoreCase("rownum"))
      return ((rownum == null) ? "0" : rownum);
    else {
      if (log4j.isDebugEnabled())
        log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }
}
