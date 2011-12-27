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

public class SL_Order_Tax extends HttpSecureAppServlet {
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
      String strMProductID = vars.getStringParameter("inpmProductId");
      String strCBPartnerLocationID = vars.getStringParameter("inpcBpartnerLocationId");
      String strDateOrdered = vars.getStringParameter("inpdateordered");
      String strADOrgID = vars.getStringParameter("inpadOrgId");
      String strMWarehouseID = vars.getStringParameter("inpmWarehouseId");
      String strCOrderId = vars.getStringParameter("inpcOrderId");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strChanged, strMProductID, strCBPartnerLocationID,
            strDateOrdered, strADOrgID, strMWarehouseID, strCOrderId, strIsSOTrx, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strMProductID, String strCBPartnerLocationID, String strDateOrdered,
      String strADOrgID, String strMWarehouseID, String strCOrderId, String strIsSOTrx,
      String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Order_Tax';\n\n");
    resultado.append("var respuesta = new Array(");

    String strCTaxID = null;
    String orgLocationID = SLOrderProductData.getOrgLocationId(this,
        Utility.getContext(this, vars, "#User_Client", "SLOrderProduct"), "'" + strADOrgID + "'");
    if (orgLocationID.equals("")) {
      resultado.append("new Array('MESSAGE', \""
          + FormatUtilities.replaceJS(Utility.messageBD(this,
              "Tax can not be calculated, because the organization has not a location defined",
              vars.getLanguage())) + "\"),\n");
    } else {
      SLOrderTaxData[] data = SLOrderTaxData.select(this, strCOrderId);
      if (data != null && data.length > 0) {
        strCTaxID = Tax.get(this, strMProductID, data[0].dateordered, strADOrgID, strMWarehouseID,
            (data[0].billtoId.equals("") ? strCBPartnerLocationID : data[0].billtoId),
            strCBPartnerLocationID, data[0].cProjectId, strIsSOTrx.equals("Y"));
      }
    }
    if (strCTaxID != null && !strCTaxID.equals(""))
      resultado.append("new Array(\"inpcTaxId\", \"" + strCTaxID + "\")\n");

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
