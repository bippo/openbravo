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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SE_Project_BPartner extends HttpSecureAppServlet {
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
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strLocation = vars.getStringParameter("inpcBpartnerId_LOC");
      String strContact = vars.getStringParameter("inpcBpartnerId_CON");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);

      try {
        printPage(response, vars, strBPartner, strIsSOTrx, strWindowId, strLocation, strContact);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner,
      String strIsSOTrx, String strWindowId, String strLocation, String strContact)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    String strPaymentrule, strPaymentterm, strPricelist, strPaymentMethod;
    strPaymentrule = strPaymentterm = strPricelist = strPaymentMethod = "";
    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);
    if (data != null && data.length > 0) {
      strPaymentrule = (strIsSOTrx.equals("Y") ? data[0].paymentrule : data[0].paymentrulepo);
      strPaymentterm = (strIsSOTrx.equals("Y") ? data[0].cPaymenttermId : data[0].poPaymenttermId);
      strPricelist = (strIsSOTrx.equals("Y") ? data[0].mPricelistId : data[0].poPricelistId);
      strPaymentMethod = (strIsSOTrx.equals("Y") ? data[0].finPaymentmethodId
          : data[0].poPaymentmethodId);
    }

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_Project_BPartner';\n\n");
    resultado.append("var respuesta = new Array(");
    FieldProvider[] tdv = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_PriceList_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      tdv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "C_BPartner_Location_ID", "", "C_BPartner Location - Ship To", Utility.getContext(this,
              vars, "#AccessibleOrgTree", strWindowId), Utility.getContext(this, vars,
              "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      tdv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    resultado.append("new Array(\"inpcBpartnerLocationId\", ");
    if (tdv != null && tdv.length > 0) {
      resultado.append("new Array(");
      for (int i = 0; i < tdv.length; i++) {
        resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
            + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \""
            + (tdv[i].getField("id").equalsIgnoreCase(strLocation) ? "true" : "false") + "\")");
        if (i < tdv.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n),");
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_User_ID", "",
          "AD_User C_BPartner User/Contacts", Utility.getContext(this, vars, "#AccessibleOrgTree",
              strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      tdv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    resultado.append("new Array(\"inpadUserId\", ");
    if (tdv != null && tdv.length > 0) {
      resultado.append("new Array(");
      for (int i = 0; i < tdv.length; i++) {
        resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
            + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \""
            + (tdv[i].getField("id").equalsIgnoreCase(strContact) ? "true" : "false") + "\")");
        if (i < tdv.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n),");
    FieldProvider[] tlv = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
          "C_BPartner Location", "C_BPartner Location - Bill To", Utility.getContext(this, vars,
              "#AccessibleOrgTree", strWindowId), Utility.getContext(this, vars, "#User_Client",
              strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      tlv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    resultado.append("new Array(\"inpbilltoId\", ");
    if (tlv != null && tlv.length > 0) {
      resultado.append("new Array(");
      for (int i = 0; i < tlv.length; i++) {
        resultado.append("new Array(\"" + tlv[i].getField("id") + "\", \""
            + FormatUtilities.replaceJS(tlv[i].getField("name")) + "\", \""
            + (tlv[i].getField("id").equalsIgnoreCase(strLocation) ? "true" : "false") + "\")");
        if (i < tlv.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n),");
    resultado.append("new Array(\"inppaymentrule\", \"" + strPaymentrule + "\"),");
    resultado.append("new Array(\"inpcPaymenttermId\", \"" + strPaymentterm + "\"),");
    resultado.append("new Array(\"inpmPricelistId\", \"" + strPricelist + "\"),");
    resultado.append("new Array(\"inpfinPaymentmethodId\", \"" + strPaymentMethod + "\")");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
