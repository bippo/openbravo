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

public class SL_User_Name extends HttpSecureAppServlet {
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
      String strFirstname = vars.getStringParameter("inpfirstname");
      String strLastname = vars.getStringParameter("inplastname");
      String strName = vars.getStringParameter("inpname");
      String strTabId = vars.getStringParameter("inpTabId");
      try {
        printPage(response, vars, strChanged, strFirstname, strLastname, strName, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strFirstname, String strLastname, String strName, String strTabId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    if (!strLastname.equals(""))
      strLastname = " " + strLastname;

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_User_Name';\n\n");
    resultado.append("var respuesta = new Array(");
    // limits name and username to a maximum number of characters
    int maxChar = 60;
    // do not change the name field, if the user just left it
    if (!strChanged.equals("inpname")) {
      if (FormatUtilities.replaceJS(strFirstname + strLastname).length() > maxChar) {
        strName = FormatUtilities.replaceJS(strFirstname + strLastname).substring(0, maxChar);
      } else {
        strName = FormatUtilities.replaceJS(strFirstname + strLastname);
      }
      resultado.append("new Array(\"inpname\", \"" + strName + "\"),");
    }
    // if we have a name filled in use that for the username
    if (!strName.equals("")) {
      if (FormatUtilities.replaceJS(strName).length() > maxChar) {
        resultado.append("new Array(\"inpusername\", \""
            + FormatUtilities.replaceJS(strName).substring(0, maxChar) + "\")");
      } else {
        resultado.append("new Array(\"inpusername\", \"" + FormatUtilities.replaceJS(strName)
            + "\")");
      }
    } else {
      // else concatenate first- and lastname
      if (FormatUtilities.replaceJS(strFirstname + strLastname).length() > maxChar) {
        resultado.append("new Array(\"inpusername\", \""
            + FormatUtilities.replaceJS(strFirstname + strLastname).substring(0, maxChar) + "\")");
      } else {
        resultado.append("new Array(\"inpusername\", \""
            + FormatUtilities.replaceJS(strFirstname + strLastname) + "\")");
      }
    }
    // informs about characters cut
    if (FormatUtilities.replaceJS(strFirstname + strLastname).length() > maxChar) {
      resultado.append(", new Array('MESSAGE', \""
          + FormatUtilities.replaceJS(Utility.messageBD(this, "NameUsernameLengthCut",
              vars.getLanguage())) + "\")");
    } else {
      resultado.append(", new Array('MESSAGE', \"\")");
    }
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
