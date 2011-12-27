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
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_WRPhase_Sequence extends HttpSecureAppServlet {
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
      String strTabId = vars.getStringParameter("inpTabId");

      String strMASequenceID = vars.getStringParameter("inpmaSequenceId");
      String strMAWReqID = vars.getStringParameter("inpmaWorkrequirementId");
      try {
        printPage(response, vars, strTabId, strMASequenceID, strMAWReqID);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTabId,
      String strMASequenceID, String strMAWReqID) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Sequence_Process';\n\n");
    resultado.append("var respuesta = new Array(");
    if (!(strMASequenceID == null || strMASequenceID.equals(""))) {
      SLWRPhaseSequenceData[] data = SLWRPhaseSequenceData.select(this, strMASequenceID);
      String strQuantity = SLWRPhaseSequenceData.selectQuantity(this, strMASequenceID, strMAWReqID);
      resultado.append("new Array(\"inpmaProcessId\", \""
          + FormatUtilities.replaceJS((data[0].process.equals("") ? "\"\"" : data[0].process))
          + "\"),\n");
      resultado.append("new Array(\"inpquantity\", "
          + FormatUtilities.replaceJS((strQuantity.equals("") ? "\"\"" : strQuantity)) + "),\n");
      resultado.append("new Array(\"inpcostcenteruse\", \""
          + FormatUtilities.replaceJS((data[0].ccuse.equals("") ? "\"\"" : data[0].ccuse))
          + "\"),\n");
      resultado.append("new Array(\"inppreptime\", \""
          + FormatUtilities.replaceJS((data[0].preptime.equals("") ? "\"\"" : data[0].preptime))
          + "\"),\n");
      resultado
          .append("new Array(\"inpoutsourced\", \""
              + FormatUtilities.replaceJS((data[0].outsourced.equals("") ? "\"\""
                  : data[0].outsourced)) + "\")\n");
    }

    resultado.append(");\n");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName",
        (Utility.isTreeTab(this, strTabId) ? "appFrame.frameWindowTreeTab" : "appFrame"));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
