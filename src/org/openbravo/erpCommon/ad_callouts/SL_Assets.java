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
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Assets extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {

      String strAssetvalue = vars.getNumericParameter("inpassetvalueamt");
      String strResidualvalue = vars.getNumericParameter("inpresidualassetvalueamt");
      String strAmortizationvalue = vars.getNumericParameter("inpamortizationvalueamt");
      String strLastChanged = vars.getStringParameter("inpLastFieldChanged");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strTabId, strAssetvalue, strResidualvalue, strAmortizationvalue,
            strLastChanged);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTabId,
      String strAssetvalue, String strResidualvalue, String strAmortizationvalue,
      String strLastChanged) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    if (strAssetvalue.equals(""))
      strAssetvalue = "0";
    if (strResidualvalue.equals(""))
      strResidualvalue = "0";
    if (strAmortizationvalue.equals(""))
      strAmortizationvalue = "0";

    BigDecimal fAssetvalue = new BigDecimal(strAssetvalue);
    BigDecimal fResidualvalue = new BigDecimal(strResidualvalue);
    BigDecimal fAmortizationvalue = new BigDecimal(strAmortizationvalue);
    // Float fAssetvalue = Float.valueOf(strAssetvalue);
    // Float fResidualvalue = Float.valueOf(strResidualvalue);
    // Float fAmortizationvalue = Float.valueOf(strAmortizationvalue);

    if (strLastChanged.equals("inpassetvalueamt")) {
      if (!fAmortizationvalue.equals(BigDecimal.ZERO))
        fResidualvalue = fAssetvalue.subtract(fAmortizationvalue);
      fAmortizationvalue = fAssetvalue.subtract(fResidualvalue);
    }

    if (strLastChanged.equals("inpresidualassetvalueamt")) {
      // if (fAmortizationvalue != 0) fAssetvalue = fResidualvalue +
      // fAmortizationvalue;
      fAmortizationvalue = fAssetvalue.subtract(fResidualvalue);
    }

    if (strLastChanged.equals("inpamortizationvalueamt")) {
      // if (fResidualvalue != 0 ) fAssetvalue = fResidualvalue +
      // fAmortizationvalue;
      fResidualvalue = fAssetvalue.subtract(fAmortizationvalue);
    }

    strAssetvalue = fAssetvalue.toString();
    strResidualvalue = fResidualvalue.toString();
    strAmortizationvalue = fAmortizationvalue.toString();

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Assets';\n\n");
    resultado.append("var respuesta = new Array(new Array(\"inpassetvalueamt\","
        + fAssetvalue.toString() + "), new Array(\"inpresidualassetvalueamt\","
        + fResidualvalue.toString() + "), new Array(\"inpamortizationvalueamt\","
        + fAmortizationvalue.toString() + "));");
    resultado.append("\n\n//" + strLastChanged);

    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
