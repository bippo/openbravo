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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
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

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_WorkRequirement_ProcessPlan extends HttpSecureAppServlet {
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
      String strProcessPlan = vars.getStringParameter("inpmaProcessplanId");

      try {
        printPage(response, vars, strProcessPlan);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strProcessPlan) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLWorkRequirementProcessPlanData[] data = SLWorkRequirementProcessPlanData.select(this,
        strProcessPlan);
    // String strExplodePhases = data[0].explodephases;

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Workrequirement_ProcessPlan';\n\n");
    resultado.append("var respuesta = new Array(");
    if (data != null && data.length > 0) {
      resultado.append("new Array(\"inpexplodephases\", \""
          + FormatUtilities.replaceJS(data[0].explodephases) + "\"),");

      final String conversionRate = StringUtils.isNotEmpty(data[0].conversionrate) ? FormatUtilities
          .replaceJS(data[0].conversionrate) : "\"\"";
      resultado.append("new Array(\"inpconversionrate\", " + conversionRate + "),");
      resultado.append("new Array(\"inpsecondaryunit\", \""
          + FormatUtilities.replaceJS(data[0].secondaryunit) + "\")");
    } else {
      resultado.append("new Array(\"inpexplodephases\", \"N\"),");
      resultado.append("new Array(\"inpconversionrate\", \"1\"),");
      resultado.append("new Array(\"inpsecondaryunit\", \"\")");
    }
    resultado.append(");");

    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
