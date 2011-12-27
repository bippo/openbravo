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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.utils.FormatUtilities;

public abstract class CalloutHelper extends HttpSecureAppServlet {

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  abstract void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTabId,
      String windowId) throws IOException, ServletException;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (!vars.commandIn("DEFAULT")) {
      String strTabId = vars.getStringParameter("inpTabId");
      String strWindowId = vars.getStringParameter("inpwindowId");
      printPage(response, vars, strTabId, strWindowId);
    } else
      pageError(response);
  }

  String generateArray(FieldProvider[] data) {
    return generateArray(data, "");
  }

  String generateArray(FieldProvider[] data, String selected) {
    StringBuffer strArray = new StringBuffer();
    if (data == null || data.length == 0)
      strArray.append("null");
    else {
      strArray.append("new Array(");
      for (int i = 0; i < data.length; i++) {
        strArray.append("\nnew Array(\"").append(data[i].getField("id")).append("\", \"")
            .append(FormatUtilities.replaceJS(data[i].getField("name"))).append("\",")
            .append(data[i].getField("id").equals(selected) ? "\"true\"" : "\"false\"").append(")");
        if (i < data.length - 1)
          strArray.append(", \n");
      }
      strArray.append(")");
    }
    return strArray.toString();
  }

  static boolean commandInCommandList(String inCommand, String... commandList) {
    for (String command : commandList) {
      if (inCommand.equals(command)) {
        return true;
      }
    }
    return false;
  }

}
