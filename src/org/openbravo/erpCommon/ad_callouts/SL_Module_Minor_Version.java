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
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * A callout used in the module dependency form. When the dependent module changes then the minor
 * version is set to the version of the dependent module.
 * 
 * @author mtaal
 */
public class SL_Module_Minor_Version extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String DEPENDENT_MODULE_FIELD = "inpadDependentModuleId";
  private static final String MINOR_VERSION_FIELD = "inpstartversion";
  private static final String LAST_CHANGED_FIELD = "inpLastFieldChanged";

  /**
   * Initializes the servlet.
   */
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  /**
   * Receives the request to compute the version.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter(LAST_CHANGED_FIELD);
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strModule = vars.getStringParameter(DEPENDENT_MODULE_FIELD);
      String strFirstVersion = vars.getStringParameter(MINOR_VERSION_FIELD);
      try {
        printPage(response, vars, strChanged, strFirstVersion, strModule);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String firstVersion, String strModule) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer result = new StringBuffer();
    result.append("var calloutName='SL_Module_Minor_version';\n\n");
    result.append("var respuesta = new Array(");
    // do not change the name field, if the user just left it
    if (strChanged.equals(DEPENDENT_MODULE_FIELD)) {
      // get the minor version
      final Module dependsOnModule = OBDal.getInstance().get(Module.class, strModule);
      if (dependsOnModule.getVersion() != null) {
        result.append("new Array(\"" + MINOR_VERSION_FIELD + "\", \""
            + dependsOnModule.getVersion() + "\")");
      } else {
        result.append("new Array(\"" + MINOR_VERSION_FIELD + "\", \""
            + dependsOnModule.getVersion() + "\")");
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
