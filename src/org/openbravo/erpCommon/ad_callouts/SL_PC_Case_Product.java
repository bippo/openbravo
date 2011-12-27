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
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_PC_Case_Product extends HttpSecureAppServlet {
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
    result.append("var calloutName='SL_PC_Case_Product';\n\n");
    result.append("var respuesta = new Array(");
    if (strPAttr.startsWith("\""))
      strPAttr = strPAttr.substring(1, strPAttr.length() - 1);
    result.append("new Array(\"inpmAttributesetinstanceId\", \"" + strPAttr + "\"),\n");
    String strattrsetvaluesdescr = "";
    final AttributeSetInstance attributesetinstance = OBDal.getInstance().get(
        AttributeSetInstance.class, strPAttr);
    if (attributesetinstance != null) {
      strattrsetvaluesdescr = attributesetinstance.getDescription();
    }
    result.append("new Array(\"inpmAttributesetinstanceId_R\", \""
        + FormatUtilities.replaceJS(strattrsetvaluesdescr) + "\"),\n");
    String strAttrSet, strAttrSetValueType;
    strAttrSet = strAttrSetValueType = "";
    OBContext.setAdminMode();
    try {
      final Product product = OBDal.getInstance().get(Product.class, strMProductID);
      if (product != null) {
        AttributeSet attributeset = product.getAttributeSet();
        if (attributeset != null)
          strAttrSet = product.getAttributeSet().toString();
        strAttrSetValueType = product.getUseAttributeSetValueAs();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    result.append("new Array(\"inpattributeset\", \"" + FormatUtilities.replaceJS(strAttrSet)
        + "\"),\n");
    result.append("new Array(\"inpattrsetvaluetype\", \""
        + FormatUtilities.replaceJS(strAttrSetValueType) + "\"),\n");
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
