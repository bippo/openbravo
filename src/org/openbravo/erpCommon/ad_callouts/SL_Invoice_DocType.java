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
import org.openbravo.erpCommon.reference.ListData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Invoice_DocType extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strDocTypeTarget = vars.getStringParameter("inpcDoctypetargetId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strCInvoiceId = vars.getStringParameter("inpcInvoiceId");

      try {
        printPage(response, vars, strDocTypeTarget, strTabId, strCInvoiceId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strDocTypeTarget, String strTabId, String strCInvoiceId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    SEInOutDocTypeData[] data = SEInOutDocTypeData.select(this, strDocTypeTarget);

    StringBuffer resultado = new StringBuffer();
    if (data == null || data.length == 0)
      resultado.append("var respuesta = null;");
    else {
      resultado.append("var calloutName='SL_Invoice_DocType';\n\n");
      resultado.append("var respuesta = new Array(");

      // check if doc type target is different, in this case assing new
      // documentno otherwise matain the previous one
      String strDoctypetargetinvoice = SEInOutDocTypeData.selectDoctypetargetinvoice(this,
          strCInvoiceId);

      if (strDoctypetargetinvoice == null || strDoctypetargetinvoice.equals("")
          || !strDoctypetargetinvoice.equals(strDocTypeTarget)) {
        String strDocumentNo = Utility.getDocumentNo(this, vars.getClient(), "C_Invoice", false);
        if (data[0].isdocnocontrolled.equals("Y"))
          strDocumentNo = data[0].currentnext;
        resultado.append("new Array(\"inpdocumentno\", \"<" + strDocumentNo + ">\"),");
      } else if (strDoctypetargetinvoice != null && !strDoctypetargetinvoice.equals("")
          && strDoctypetargetinvoice.equals(strDocTypeTarget))
        resultado.append("new Array(\"inpdocumentno\", \""
            + SEInOutDocTypeData.selectActualinvoicedocumentno(this, strCInvoiceId) + "\"),");
      // ------

      resultado.append("new Array(\"inpdocbasetype\", \"" + data[0].docbasetype + "\")");
      String strPaymentRule = "";
      if (data[0].docbasetype.endsWith("C")) {
        strPaymentRule = "P";
        resultado.append(", new Array(\"inppaymentrule\", \"" + strPaymentRule + "\"),");
        String strNamePaymentRule = ListData.selectName(this, "195", "P");
        resultado.append("new Array(\"PaymentRule_BTN\", \"" + strNamePaymentRule + "\")");
      }
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
