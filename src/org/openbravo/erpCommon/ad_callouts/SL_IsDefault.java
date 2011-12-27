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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * This callout checks if this is the only active isDefault checked for the table (with
 * organization) or, in case it is a tab with parent, it is the only checked taking into account its
 * parent.
 * 
 * If another one already exists an error message is raised and the checkbox is unchecked.
 * 
 */
public class SL_IsDefault extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      try {
        printPage(response, vars);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    String strValue = vars.getStringParameter(vars.getStringParameter("inpLastFieldChanged"));
    StringBuffer result = new StringBuffer();
    result.append("var calloutName='SL_IsDefault';\n\n");
    result.append("var respuesta = new Array(");

    if (strValue.equals("Y")) {
      String strTableId = vars.getStringParameter("inpTableId");
      String strOrg = vars.getStringParameter("inpadOrgId");
      String parentColumn = vars.getStringParameter("inpParentKeyColumn");
      String parentValue = vars.getStringParameter("inp"
          + Sqlc.TransformaNombreColumna(parentColumn));
      String currentColumnKey = vars.getStringParameter("inpkeyColumnId");
      String currentKeyValue = vars.getStringParameter(vars.getStringParameter("inpKeyName"));

      SLIsDefaultData[] data = SLIsDefaultData.select(this, strTableId);
      if (data != null && data.length != 0) {
        String parentClause = "";
        String currentClause = "";
        // Include parent column if it exists
        if (!parentColumn.equals("") && !parentValue.equals(""))
          parentClause = "AND " + parentColumn + "='" + parentValue + "'";

        // In case the current record already exists in DB not sum it to
        // the total
        if (!currentKeyValue.equals(""))
          currentClause = "AND " + currentColumnKey + " != '" + currentKeyValue + "'";

        String strTotalDefaults = SLIsDefaultData.selectHasDefaults(this, data[0].tablename,
            parentClause, currentClause, strOrg);
        if (!strTotalDefaults.equals("0")) {
          String msg = Utility.messageBD(this, "DuplicatedDefaults", vars.getLanguage());
          result.append("new Array(\"ERROR\", \"" + msg + "\"), \n");
          result.append("new Array(\"inpisdefault\", \"N\")\n");
        }
      }
    }

    result.append(");");
    xmlDocument.setParameter("array", result.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
