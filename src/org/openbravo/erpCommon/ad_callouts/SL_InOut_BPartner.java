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
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_InOut_BPartner extends HttpSecureAppServlet {
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
      String strProjectId = vars.getStringParameter("inpcProjectId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strBPartner, strLocation, strContact, strWindowId, strProjectId,
            strIsSOTrx, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner,
      String strLocation, String strContact, String strWindowId, String strProjectId,
      String strIsSOTrx, String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);

    String strUserRep = "";
    if (data != null && data.length > 0)
      strUserRep = SEOrderBPartnerData.userIdSalesRep(this, data[0].salesrepId);

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_InOut_BPartner';\n\n");
    resultado.append("var respuesta = new Array(");

    FieldProvider[] tdv = null;
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
            + FormatUtilities.replaceJS(Replace.replace(tdv[i].getField("name"), "\"", "\\\""))
            + "\", \"" + (tdv[i].getField("id").equalsIgnoreCase(strLocation) ? "true" : "false")
            + "\")");
        if (i < tdv.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n),");
    resultado.append("new Array(\"inpsalesrepId\", ");
    FieldProvider[] tld = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "SalesRep_ID",
          "AD_User SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "SEOrderBPartner"),
          Utility.getContext(this, vars, "#User_Client", "SEOrderBPartner"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SEOrderBPartner", "");
      tld = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (tld != null && tld.length > 0) {
      resultado.append("new Array(");
      for (int i = 0; i < tld.length; i++) {
        resultado.append("new Array(\"" + tld[i].getField("id") + "\", \""
            + FormatUtilities.replaceJS(tld[i].getField("name")) + "\", \""
            + (tld[i].getField("id").equalsIgnoreCase(strUserRep) ? "true" : "false") + "\")");
        if (i < tld.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n),");
    resultado.append("new Array(\"inpcProjectId\", \"\"),");
    resultado.append("new Array(\"inpcProjectId_R\", \"\"),");
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
            + FormatUtilities.replaceJS(Replace.replace(tdv[i].getField("name"), "\"", "\\\""))
            + "\", \"" + (tdv[i].getField("id").equalsIgnoreCase(strContact) ? "true" : "false")
            + "\")");
        if (i < tdv.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n)");
    if (data != null && data.length > 0
        && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) < 0
        && strIsSOTrx.equals("Y")) {
      String creditLimitExceed = "" + Double.parseDouble(data[0].creditavailable) * -1;
      resultado.append(", new Array('MESSAGE', \""
          + Utility.messageBD(this, "CreditLimitOver", vars.getLanguage()) + creditLimitExceed
          + "\")");
    } else {
      resultado.append(", new Array('MESSAGE', \"\")");
    }
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
