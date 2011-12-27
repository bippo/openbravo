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
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Payment_No_Verify extends HttpSecureAppServlet {
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
      String strcCreditCard = vars.getStringParameter("inpcreditcardnumber");
      String strcCreditCardType = vars.getStringParameter("inpcreditcardtype");
      String strcRoutingNo = vars.getStringParameter("inproutingno");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strChanged, strcCreditCard, strcCreditCardType, strcRoutingNo,
            strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strcCreditCard, String strcCreditCardType, String strcRoutingNo, String strTabId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Payment_DocType';\n\n");
    resultado.append("var respuesta = new Array(");

    if (strChanged.equals("inpcreditcardnumber")) {
      if (strcCreditCard == null || strcCreditCard.length() == 0) {
      } else {
        String strcvalidateCc = Tax.validateCreditCardNumber(strcCreditCard, strcCreditCardType);
        resultado
            .append("new Array('MESSAGE', \""
                + FormatUtilities.replaceJS(Utility.messageBD(this, strcvalidateCc,
                    vars.getLanguage())) + "\")");
      }
    } else if (strChanged.equals("inproutingno")) {
      if (strcRoutingNo == null || strcRoutingNo.length() == 0) {
      } else {
        String strcvalidateRo = Tax.validateRoutingNo(strcRoutingNo);
        resultado
            .append("new Array('MESSAGE', \""
                + FormatUtilities.replaceJS(Utility.messageBD(this, strcvalidateRo,
                    vars.getLanguage())) + "\")");
      }
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
