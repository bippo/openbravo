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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.reference.ui;

import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;

public class UIString extends UIReference {
  public UIString(String reference, String subreference) {
    super(reference, subreference);
  }

  public void getFilter(SQLReturnObject result, boolean isNewFilter, VariablesSecureApp vars,
      TableSQLData tableSQL, Vector<String> filter, Vector<String> filterParams, Properties prop)
      throws ServletException {
    String aux;
    if (isNewFilter) {
      aux = vars.getRequestGlobalVariable("inpParam" + prop.getProperty("ColumnName"),
          tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName"));
    } else {
      aux = vars.getSessionValue(tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName"));
    }
    // The filter is not applied if the parameter value is null or
    // parameter value is '%' for string references.
    if (!aux.equals("")) {
      if (!aux.equals("%")) {
        UIReferenceUtility.addFilter(filter, filterParams, result, tableSQL,
            prop.getProperty("ColumnName"), prop.getProperty("ColumnName"), reference, true, aux);
      } else {
        filter.addElement("1=1");
      }
    }
  }
}
