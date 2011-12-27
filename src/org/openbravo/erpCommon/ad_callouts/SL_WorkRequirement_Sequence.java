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
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_WorkRequirement_Sequence extends HttpSecureAppServlet {
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
      String strSequence = vars.getStringParameter("inpmaSequenceId");
      String strWorkRequirement = vars.getStringParameter("inpmaWorkrequirementId");

      try {
        printPage(response, vars, strWorkRequirement, strSequence);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strWorkRequirement, String strSequence) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLWorkRequirementSequence[] data = SLWorkRequirementSequence.select(this, strSequence);

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_WorkRequirement_Sequence';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpmaProcessId\", \"" + data[0].maProcessId + "\"),");
    resultado.append("new Array(\"inpcostcenteruse\", \""
        + FormatUtilities.replaceJS(data[0].costcenteruse) + "\"),");
    resultado.append("new Array(\"inppreptime\", \"" + FormatUtilities.replaceJS(data[0].preptime)
        + "\"),");
    resultado.append("new Array(\"inpnoqty\", " + FormatUtilities.replaceJS(data[0].noqty) + "),");
    resultado.append("new Array(\"inpgroupuse\", \"" + FormatUtilities.replaceJS(data[0].groupuse)
        + "\")");
    resultado.append(");");

    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
