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
 * All portions are Copyright (C) 2012 Openbravo SLU 
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
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Requisition_BPartner_PriceList extends HttpSecureAppServlet {
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
      log4j.debug("CHANGED: " + strChanged);
      String strPriceList = vars.getStringParameter("inpmPricelistId");
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strWindowId = vars.getStringParameter("inpwindowId");
      try {
        printPage(response, vars, strChanged, strWindowId, strPriceList, strBPartner);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strWindowId, String strPriceList, String strBPartner) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer strResult = new StringBuffer();
    strResult.append("var calloutName='SL_Requisition_BPartner';\n\n");
    strResult.append("var respuesta = new Array(");

    if (strChanged.equals("inpcBpartnerId")) {
      if (strBPartner.equals("")) {
        vars.removeSessionValue(strWindowId + "|C_BPartner_ID");
      } else {
        OBContext.setAdminMode(true);
        try {
          BusinessPartner bPartner = OBDal.getInstance().get(BusinessPartner.class, strBPartner);
          if (bPartner.getPurchasePricelist() != null) {
            strResult.append("new Array(\"inpmPricelistId\", \""
                + bPartner.getPurchasePricelist().getId() + "\"),");
            strResult.append("new Array(\"inpcCurrencyId\", \""
                + bPartner.getPurchasePricelist().getCurrency().getId() + "\")");
          }
        } finally {
          OBContext.restorePreviousMode();
        }
      }
    } else { // strChanged.equals("inpmPricelistId")
      if (strPriceList.equals("")) {
        vars.removeSessionValue(strWindowId + "|M_PriceList_ID");
      } else {
        OBContext.setAdminMode(true);
        try {
          PriceList priceList = OBDal.getInstance().get(PriceList.class, strPriceList);
          strResult.append("new Array(\"inpcCurrencyId\", \"" + priceList.getCurrency().getId()
              + "\")");
        } finally {
          OBContext.restorePreviousMode();
        }
      }
    }

    strResult.append(");");
    xmlDocument.setParameter("array", strResult.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}
