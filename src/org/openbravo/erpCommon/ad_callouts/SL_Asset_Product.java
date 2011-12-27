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
 * All portions are Copyright (C) 2010 Openbravo SLU 
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
import org.openbravo.erpCommon.businessUtility.PAttributeSet;
import org.openbravo.erpCommon.businessUtility.PAttributeSetData;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Asset_Product extends HttpSecureAppServlet {
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
      String strTabId = vars.getStringParameter("inpTabId");

      String strMProductID = vars.getStringParameter("inpmProductId");
      String strPAttr = vars.getStringParameter("inpmProductId_ATR");

      try {
        printPage(response, vars, strTabId, strMProductID, strPAttr);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTabId,
      String strMProductID, String strPAttr) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer result = new StringBuffer();
    result.append("var calloutName='SL_Asset_Product';\n\n");
    result.append("var respuesta = new Array(");
    PAttributeSetData[] dataPAttr = PAttributeSetData.selectProductAttr(this, strMProductID);
    if (dataPAttr != null && dataPAttr.length > 0 && dataPAttr[0].attrsetvaluetype.equals("D")) {
      PAttributeSetData[] data2 = PAttributeSetData.select(this, dataPAttr[0].mAttributesetId);
      if (PAttributeSet.isInstanceAttributeSet(data2)) {
        result.append("new Array(\"inpmAttributesetinstanceId\", \"\"),");
        result.append("new Array(\"inpmAttributesetinstanceId_R\", \"\"),");
      } else {
        result.append("new Array(\"inpmAttributesetinstanceId\", \""
            + dataPAttr[0].mAttributesetinstanceId + "\"),");
        result.append("new Array(\"inpmAttributesetinstanceId_R\", \""
            + FormatUtilities.replaceJS(dataPAttr[0].description) + "\"),");
      }
    } else {
      result.append("new Array(\"inpmAttributesetinstanceId\", \"\"),");
      result.append("new Array(\"inpmAttributesetinstanceId_R\", \"\"),");
    }
    result.append("new Array(\"inpattributeset\", \""
        + FormatUtilities.replaceJS(dataPAttr[0].mAttributesetId) + "\"),\n");
    result.append("new Array(\"inpattrsetvaluetype\", \""
        + FormatUtilities.replaceJS(dataPAttr[0].attrsetvaluetype) + "\"),\n");
    result.append("new Array(\"EXECUTE\", \"displayLogic();\")\n");
    result.append(");\n");

    xmlDocument.setParameter("array", result.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
