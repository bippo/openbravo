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

package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.module.Module;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_ModuleCallout extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Prevent navigation history in the callout
   */
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
      String strADModuleID = vars.getStringParameter("inpadModuleId");

      try {
        String moduleType = vars.getStringParameter("inptype");
        String isInDev = vars.getStringParameter("inpisindevelopment");
        if (isInDev.equals("Y") && moduleType.equals("T")) {
          templateInDev(response, vars, strADModuleID);
        } else {
          printPageResult(response, "");
        }
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  /**
   * Checks if there are multiple templates in development. If so, unsets the is indevelopment
   * property from the current record.
   */
  private void templateInDev(HttpServletResponse response, VariablesSecureApp vars,
      String strADModuleID) throws IOException, ServletException {
    // Check whether there are more templates in development
    OBCriteria<Module> obc = OBDal.getInstance().createCriteria(Module.class);
    obc.add(Restrictions.eq(Module.PROPERTY_TYPE, "T"));
    obc.add(Restrictions.eq(Module.PROPERTY_INDEVELOPMENT, true));
    if (strADModuleID != null && !strADModuleID.equals("")) {
      obc.add(Restrictions.ne(Module.PROPERTY_ID, strADModuleID));
    }
    String devTemplates = "";
    for (Module template : obc.list()) {
      devTemplates += template.getName() + " ";
    }

    StringBuffer result = new StringBuffer();

    if (!devTemplates.equals("")) {
      // There are other template(s) in dev
      result.append("new Array(\"MESSAGE\", \"" + devTemplates + " "
          + Utility.messageBD(this, "MultipleDevelopmentTemplates", vars.getLanguage()) + "\"),\n");
      result.append("new Array(\"inpisindevelopment\", \"N\")");
    }

    printPageResult(response, result.toString());
  }

  /**
   * Composes the standard callout response
   */
  private void printPageResult(HttpServletResponse response, String result) throws IOException {
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_ModuleCallout';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append(result);
    resultado.append(");");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}
