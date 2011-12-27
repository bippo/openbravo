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
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SE_ProjectLine_Value extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strmProductId = vars.getStringParameter("inpmProductId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strProjectId = vars.getStringParameter("inpcProjectId");
      String strPhaseId = vars.getStringParameter("inpcProjectphaseId");
      String strADOrgID = vars.getStringParameter("inpadOrgId");
      String strPriceListVersion = vars.getGlobalVariable("inpPriceListVersion",
          "Product.priceListVersion", "");
      try {
        printPage(response, vars, strmProductId, strPriceListVersion, strTabId, strProjectId,
            strPhaseId, strADOrgID);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strmProductId, String strPriceListVersion, String strTabId, String strProjectId,
      String strPhaseId, String strADOrgID) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    String strMessage = "";
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_ProjectLine_Value';\n\n");
    resultado.append("var respuesta = new Array(\n");

    SEProjectLineValueData[] data = null;
    SEProjectLineValueData[] data1 = null;

    if (strmProductId != null && !strmProductId.equals("")) {
      data = SEProjectLineValueData.select(this, strmProductId);
      String strDate = DateTimeData.today(this);
      if (strPhaseId != null && !strPhaseId.equals("")) {
        if (strProjectId == null || strProjectId.equals("")) {
          strProjectId = SEProjectLineValueData.selectPhaseProject(this, strPhaseId);
        }
      }
      data1 = SEProjectLineValueData.selectProject(this, strProjectId);
      String strCBPartnerLocationID = data1[0].bplocation;
      String strMWarehouseID = data1[0].warehouse;
      String strProjCat = data1[0].projcat;

      if (!strPriceListVersion.equals("")) {
        String plannedprice = SEProjectLineValueData.selectPlannedPrice(this, strPriceListVersion,
            strmProductId, strProjectId);
        if (plannedprice != null && !plannedprice.equals("")) {
          if (!strProjCat.equals("S")) {
            resultado.append("new Array(\"inpplannedprice\", "
                + (plannedprice.equals("") ? "\"\"" : plannedprice) + "),\n");
          } else {
            resultado.append("new Array(\"inppriceactual\", "
                + (plannedprice.equals("") ? "\"\"" : plannedprice) + ")\n");
          }
        } else
          strMessage = "PriceNotFound";
      }
      if (!strProjCat.equals("S")) {
        if (strCBPartnerLocationID != null && !strCBPartnerLocationID.equals("")
            && strMWarehouseID != null && !strMWarehouseID.equals("")) {
          String strIsSOTrx = "Y";
          String strCTaxID = Tax.get(this, strmProductId, strDate, strADOrgID, strMWarehouseID,
              strCBPartnerLocationID, strCBPartnerLocationID, strProjectId, strIsSOTrx.equals("Y"));
          if (strCTaxID != null && !strCTaxID.equals("")) {
            resultado.append("new Array(\"inpcTaxId\", \""
                + (strCTaxID.equals("") ? "\"\"" : strCTaxID) + "\"),\n");
          } else
            strMessage = "TaxNotFound";
        }
        resultado.append("new Array(\"inpproductValue\", \"" + data[0].value + "\"),\n");
        resultado.append("new Array(\"inpproductName\", \""
            + FormatUtilities.replaceJS(data[0].name) + "\"),\n");
        resultado.append("new Array(\"inpproductDescription\", \""
            + FormatUtilities.replaceJS(data[0].description) + "\")\n");
      }
    } else {
      data = SEProjectLineValueData.set();
    }
    if (!strMessage.equals(""))
      resultado.append(", new Array('MESSAGE', \""
          + FormatUtilities.replaceJS(Utility.messageBD(this, strMessage, vars.getLanguage()))
          + "\")\n");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
