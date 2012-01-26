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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
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
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_ProductionPlan_WRPhase extends HttpSecureAppServlet {
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
      String strProduction = vars.getStringParameter("inpmProductionId");
      String strWRPhase = vars.getStringParameter("inpmaWrphaseId");
      String strQuantity = vars.getStringParameter("inpproductionqty");
      try {
        printPage(response, vars, strProduction, strWRPhase, strQuantity);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strProduction, String strWRPhase, String strQuantity) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLProductionPlanWRPhaseData[] data = SLProductionPlanWRPhaseData.select(this, strProduction,
        strWRPhase);
    if (data == null || data.length == 0)
      data = SLProductionPlanWRPhaseData.set();
    String strNeededQuantity = data[0].neededqty;
    BigDecimal estimatedTime = BigDecimal.ZERO;
    BigDecimal qtyWRPhase = new BigDecimal(data[0].quantity);

    if (!data[0].estimatedtime.equals("") && qtyWRPhase.compareTo(BigDecimal.ZERO) != 0
        && !strQuantity.equals("")) {
      estimatedTime = new BigDecimal(data[0].estimatedtime).divide(qtyWRPhase).multiply(
          new BigDecimal(strQuantity));
    }

    String strOutsourced = SLProductionPlanWRPhaseData.selectOutsourced(this, strWRPhase);
    if (strOutsourced == null || strOutsourced.equals(""))
      strOutsourced = "N";

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_ProductionPlan_WRPhase';\n\n");
    resultado.append("var respuesta = new Array(");
    if (strNeededQuantity != null && !strNeededQuantity.equals(""))
      resultado.append("new Array(\"inpneededquantity\", " + strNeededQuantity + "),\n");
    resultado.append("new Array(\"inpsecondaryunit\", \""
        + FormatUtilities.replaceJS(data[0].secondaryunit) + "\"),\n");
    if (data[0].conversionrate != null && !data[0].conversionrate.equals(""))
      resultado.append("new Array(\"inpconversionrate\", " + data[0].conversionrate + "),\n");
    resultado.append("new Array(\"inpmaCostcenterVersionId\", \"" + data[0].maCostcenterVersionId
        + "\"), \n");
    resultado.append("new Array(\"inpoutsourced\", \"" + strOutsourced + "\"),\n");
    resultado.append("new Array(\"inpestimatedtime\", \""
        + FormatUtilities.replaceJS(estimatedTime.toPlainString()) + "\")\n");
    resultado.append(");\n");
    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
