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
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Project_Type extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");

      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);

      try {
        printPage(response, vars);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars)
      throws ServletException, IOException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    ComboTableData comboTableData = null;
    boolean isRelatedProjectType = false;
    FieldProvider[] data = null;

    String strCProjectTypeID = vars.getStringParameter("inpcProjecttypeId");
    String strWindowId = vars.getStringParameter("inpwindowId");

    resultado.append("var calloutName='SL_Project_Type';\n\n");
    resultado.append("var respuesta = new Array(");

    if (!strCProjectTypeID.isEmpty()) {
      try {
        comboTableData = new ComboTableData(vars, this, "19", "C_ProjectType_ID", "", "",
            Utility.getReferenceableOrg(vars, vars.getStringParameter("inpadOrgId")),
            Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
        comboTableData.fillParameters(null, strWindowId, "");
        data = comboTableData.select(false);
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (data != null && data.length != 0) {
        for (FieldProvider fp : data) {
          if (fp.getField("ID").trim().equalsIgnoreCase(strCProjectTypeID)) {
            isRelatedProjectType = true;
            break;
          }
        }
      }

      if (!isRelatedProjectType && "130".equalsIgnoreCase(strWindowId)) {
        resultado.append("new Array('MESSAGE', \""
            + Utility.messageBD(this, "ProjectTypeNull", vars.getLanguage()) + "\"));");
      } else {
        resultado.append(");");
      }
    } else {
      resultado.append(");");
    }

    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}