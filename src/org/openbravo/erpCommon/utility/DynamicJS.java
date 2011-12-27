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

package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;

/**
 * @author Fernando Iriazabal
 * 
 *         Servlet that prints a javascript with dynamic functions such as the confirmation messages
 *         for the check javascript of the window.
 */
public class DynamicJS extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    printPageDataSheet(response, vars);
  }

  /**
   * Prints the javascript with the messages.
   * 
   * @param response
   *          Handler for the response.
   * @param vars
   *          Handler for the session info.
   * @throws IOException
   * @throws ServletException
   */
  @SuppressWarnings("unchecked")
  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page");
    OBError myError = null;
    DynamicJSData[] data = null;
    try {
      data = DynamicJSData.select(this, vars.getLanguage());
    } catch (ServletException ex) {
      myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      log4j.error("Error in DynamicJS.printPageDataSheet(): " + myError.getTitle() + " - "
          + myError.getMessage());
      return;
    }
    StringBuffer arrayType = new StringBuffer();
    StringBuffer array = new StringBuffer();
    arrayType.append("function messageType(_messageID, _messageType) {\n");
    arrayType.append("  this.id = _messageID;\n");
    arrayType.append("  this.type = _messageType;\n");
    arrayType.append("}\n");
    arrayType.append("function messagesTexts(_language, _message, _text, _defaultText) {\n");
    arrayType.append("  this.language = _language;\n");
    arrayType.append("  this.message = _message;\n");
    arrayType.append("  this.text = _text;\n");
    arrayType.append("  this.defaultText = _defaultText;\n");
    arrayType.append("}\n");
    arrayType.append("var arrTypes = new Array(\n");
    array.append("var arrMessages = new Array(\n");
    if (data != null && data.length != 0) {
      for (int i = 0; i < data.length; i++) {
        String num = data[i].value.replace("JS", "");
        if (i > 0) {
          arrayType.append(",\n");
          array.append(",\n");
        }
        arrayType.append("new messageType(\"").append(num).append("\",")
            .append(data[i].msgtype.equals("C") ? "1" : "0").append(")");
        array.append("new messagesTexts(\"").append(vars.getLanguage()).append("\", \"");
        array.append(num).append("\", \"").append(FormatUtilities.replaceJS(data[i].msgtext))
            .append("\", null)");
      }

    }
    arrayType.append(");\n");
    array.append(");\n");

    response.setContentType("text/javascript; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(arrayType.toString() + array.toString());
    out.println(arrayType.toString() + array.toString());

    String globals = "";

    globals += "var decSeparator_global = '" + vars.getSessionValue("#DECIMALSEPARATOR|QTYEDITION")
        + "';\n";
    globals += "var groupSeparator_global = '" + vars.getSessionValue("#GROUPSEPARATOR|QTYEDITION")
        + "';\n";
    globals += "var groupInterval_global = '3';\n";
    globals += "var maskNumeric_default = '" + vars.getSessionValue("#FORMATOUTPUT|QTYEDITION")
        + "';\n";

    out.print(globals);

    final HashMap<String, String> formatMap = (HashMap<String, String>) vars
        .getSessionObject("#formatMap");

    out.print("var F = {\"formats\": [");
    boolean first = true;
    for (String key : formatMap.keySet()) {
      if (!first) {
        out.print(",");
      } else {
        first = false;
      }
      out.print("{\"name\":\"" + key + "\",");
      out.print("\"output\":\"" + formatMap.get(key) + "\"}");
    }
    out.println("]};");
    out.print("F.getFormat=function(name)");
    out.print("{if(!name)");
    out.print("{return F.getFormat('qtyEdition');}");
    out.print("for(var i=0;i<this.formats.length;i++)");
    out.print("{if(this.formats[i].name===name)");
    out.println("{return this.formats[i].output;}} return F.getFormat('qtyEdtion');};");

    out.close();
  }
}
