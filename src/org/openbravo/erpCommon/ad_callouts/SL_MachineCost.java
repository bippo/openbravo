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

public class SL_MachineCost extends HttpSecureAppServlet {
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
      String strPurchaseAmt = vars.getNumericParameter("inppurchaseamt");
      String strToolsetAmt = vars.getNumericParameter("inptoolsetamt");
      String strYearValue = vars.getNumericParameter("inpyearvalue");
      String strAmortization = vars.getNumericParameter("inpamortization");
      String strDaysYear = vars.getNumericParameter("inpdaysyear");
      String strDayHours = vars.getNumericParameter("inpdayhours");
      String strImproductiveHoursYear = vars.getNumericParameter("inpimproductivehoursyear");
      String strCostUomYear = vars.getNumericParameter("inpcostuomyear");
      String strCost = vars.getNumericParameter("inpcost");
      String strCostUom = vars.getStringParameter("inpcostuom");
      try {
        printPage(response, vars, strChanged, strPurchaseAmt, strToolsetAmt, strYearValue,
            strAmortization, strDaysYear, strDayHours, strImproductiveHoursYear, strCostUomYear,
            strCost, strCostUom);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strPurchaseAmt, String strToolsetAmt, String strYearValue, String strAmortization,
      String strDaysYear, String strDayHours, String strImproductiveHoursYear,
      String strCostUomYear, String strCost, String strCostUom) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    if (strChanged.equals("inppurchaseamt") || strChanged.equals("inptoolsetamt")
        || strChanged.equals("inpyearvalue")) {
      if (strPurchaseAmt != null && !strPurchaseAmt.equals("") && strToolsetAmt != null
          && !strToolsetAmt.equals("") && strYearValue != null && !strYearValue.equals("")) {
        BigDecimal fPurchaseAmt = new BigDecimal(strPurchaseAmt);
        BigDecimal fToolsetAmt = new BigDecimal(strToolsetAmt);
        BigDecimal fYearValue = new BigDecimal(strYearValue);
        BigDecimal fAmortization = (fPurchaseAmt.add(fToolsetAmt)).divide(fYearValue, 12,
            BigDecimal.ROUND_HALF_EVEN);
        strAmortization = fAmortization.toString();

        if (strCostUomYear != null && !strCostUomYear.equals("")) {
          BigDecimal fCostUomYear = new BigDecimal(strCostUomYear);
          BigDecimal fCost = fYearValue.divide(fCostUomYear, 12, BigDecimal.ROUND_HALF_EVEN);
          strCost = fCost.toPlainString();
        }
      }
    } else if (strChanged.equals("inpamortization")) {
      if (strPurchaseAmt != null && !strPurchaseAmt.equals("") && strToolsetAmt != null
          && !strToolsetAmt.equals("") && strAmortization != null && !strAmortization.equals("")) {
        BigDecimal fPurchaseAmt = new BigDecimal(strPurchaseAmt);
        BigDecimal fToolsetAmt = new BigDecimal(strToolsetAmt);
        BigDecimal fAmortization = new BigDecimal(strAmortization);
        BigDecimal fYearValue = (fPurchaseAmt.add(fToolsetAmt)).divide(fAmortization, 12,
            BigDecimal.ROUND_HALF_EVEN);
        strYearValue = fYearValue.toPlainString();

        if (strCostUomYear != null && !strCostUomYear.equals("")) {
          BigDecimal fCostUomYear = new BigDecimal(strCostUomYear);
          BigDecimal fCost = fYearValue.divide(fCostUomYear, 12, BigDecimal.ROUND_HALF_EVEN);
          strCost = fCost.toPlainString();
        }
      }
    } else if (strChanged.equals("inpdaysyear") || strChanged.equals("inpdayhours")
        || strChanged.equals("inpimproductivehoursyear")) {
      if (strDaysYear != null && !strDaysYear.equals("") && strDayHours != null
          && !strDayHours.equals("") && strImproductiveHoursYear != null
          && !strImproductiveHoursYear.equals("")) {
        BigDecimal fDaysYear = new BigDecimal(strDaysYear);
        BigDecimal fDayHours = new BigDecimal(strDayHours);
        BigDecimal fImproductiveHoursYear = new BigDecimal(strImproductiveHoursYear);
        BigDecimal fCostUomYear = (fDaysYear.multiply(fDayHours)).subtract(fImproductiveHoursYear);
        strCostUomYear = fCostUomYear.toPlainString();

        if (strYearValue != null && !strYearValue.equals("")) {
          BigDecimal fYearValue = new BigDecimal(strYearValue);
          BigDecimal fCost = fYearValue.divide(fCostUomYear, 12, BigDecimal.ROUND_HALF_EVEN);
          strCost = fCost.toPlainString();
        }
      }
    } else if (strChanged.equals("inpcostuomyear")) {
      if (strCostUom.equals("H"))
        if (strDaysYear != null && !strDaysYear.equals("") && strDayHours != null
            && !strDayHours.equals("") && strCostUomYear != null && !strCostUomYear.equals("")) {
          BigDecimal fDaysYear = new BigDecimal(strDaysYear);
          BigDecimal fDayHours = new BigDecimal(strDayHours);
          BigDecimal fCostUomYear = new BigDecimal(strCostUomYear);
          BigDecimal fImproductiveHoursYear = (fDaysYear.multiply(fDayHours))
              .subtract(fCostUomYear);
          strImproductiveHoursYear = fImproductiveHoursYear.toPlainString();
        }
      if (strYearValue != null && !strYearValue.equals("") && strCostUomYear != null
          && !strCostUomYear.equals("")) {
        BigDecimal fYearValue = new BigDecimal(strYearValue);
        BigDecimal fCostUomYear = new BigDecimal(strCostUomYear);
        BigDecimal fCost = fYearValue.divide(fCostUomYear, 12, BigDecimal.ROUND_HALF_EVEN);
        strCost = fCost.toPlainString();
      }
    } else if (strChanged.equals("inpcost")) {
      if (strCost != null && !strCost.equals("") && strCostUomYear != null
          && !strCostUomYear.equals("")) {
        BigDecimal fCostUomYear = new BigDecimal(strCostUomYear);
        BigDecimal fCost = new BigDecimal(strCost);
        BigDecimal fYearValue = fCost.multiply(fCostUomYear);
        strYearValue = fYearValue.toPlainString();

        if (strPurchaseAmt != null && !strPurchaseAmt.equals("") && strToolsetAmt != null
            && !strToolsetAmt.equals("")) {
          BigDecimal fPurchaseAmt = new BigDecimal(strPurchaseAmt);
          BigDecimal fToolsetAmt = new BigDecimal(strToolsetAmt);
          BigDecimal fAmortization = (fPurchaseAmt.add(fToolsetAmt)).divide(fYearValue, 12,
              BigDecimal.ROUND_HALF_EVEN);
          strAmortization = fAmortization.toPlainString();
        }
      }
    }

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_MachineCost';\n\n");
    resultado.append("var respuesta = new Array(");
    if (!"".equals(strPurchaseAmt) && strPurchaseAmt != null) {
      resultado.append("new Array(\"inppurchaseamt\", " + strPurchaseAmt + "),\n");
    }
    if (!"".equals(strToolsetAmt) && strToolsetAmt != null) {
      resultado.append("new Array(\"inptoolsetamt\", " + strToolsetAmt + "),\n");
    }
    if (!"".equals(strYearValue) && strYearValue != null) {
      resultado.append("new Array(\"inpyearvalue\", " + strYearValue + "),\n");
    }
    if (!"".equals(strAmortization) && strAmortization != null) {
      resultado.append("new Array(\"inpamortization\", " + strAmortization + "), \n");
    }
    if (!"".equals(strDaysYear) && strDaysYear != null) {
      resultado.append("new Array(\"inpdaysyear\", " + strDaysYear + "),\n");
    }
    if (!"".equals(strDayHours) && strDayHours != null) {
      resultado.append("new Array(\"inpdayhours\", " + strDayHours + "),\n");
    }
    if (!"".equals(strImproductiveHoursYear) && strImproductiveHoursYear != null) {
      resultado.append("new Array(\"inpimproductivehoursyear\", " + strImproductiveHoursYear
          + "),\n");
    }
    if (!"".equals(strCostUomYear) && strCostUomYear != null) {
      resultado.append("new Array(\"inpcostuomyear\", " + strCostUomYear + "),\n");
    }
    if (!"".equals(strCost) && strCost != null) {
      resultado.append("new Array(\"inpcost\", " + strCost + ") \n");
    }
    resultado.append(");\n");
    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
