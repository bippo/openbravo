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
import org.openbravo.xmlEngine.XmlDocument;

public class SE_Order_BPartner extends HttpSecureAppServlet {
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
      String strOrderType = vars.getStringParameter("inpordertype");
      String strLocation = vars.getStringParameter("inpcBpartnerId_LOC");
      String strContact = vars.getStringParameter("inpcBpartnerId_CON");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strProjectId = vars.getStringParameter("inpcProjectId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");
      String strDeliveryRule = vars.getStringParameter("inpdeliveryrule");
      String strUserRep = vars.getStringParameter("inpsalesrepId");
      String strPaymentrule = vars.getStringParameter("inppaymentrule");
      String strPaymentterm = vars.getStringParameter("inpcPaymenttermId");
      String strInvoiceRule = vars.getStringParameter("inpinvoicerule");
      String strPriceList = vars.getStringParameter("inpmPricelistId");
      String strDeliveryViaRule = vars.getStringParameter("inpdeliveryviarule");
      String strOrgId = vars.getStringParameter("inpadOrgId");

      try {
        printPage(response, vars, strBPartner, strOrderType, strIsSOTrx, strWindowId, strLocation,
            strContact, strProjectId, strTabId, strDeliveryRule, strUserRep, strPaymentrule,
            strPaymentterm, strInvoiceRule, strPriceList, strDeliveryViaRule, strOrgId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner,
      String strOrderType, String strIsSOTrx, String strWindowId, String strLocation,
      String strContact, String strProjectId, String strTabId, String strDeliveryRule0,
      String strUserRep0, String strPaymentrule0, String strPaymentterm0, String strInvoiceRule0,
      String strPriceList0, String strDeliveryViaRule0, String strOrgId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    if (strBPartner.equals(""))
      vars.removeSessionValue(strWindowId + "|C_BPartner_ID");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    String strDeliveryRule, strPaymentrule, strPaymentterm, strMwarehouse, strInvoiceRule, strPriceList, strUserRep, strDeliveryViaRule, strFinPaymentMethodId;
    strDeliveryRule = strPaymentrule = strPaymentterm = strMwarehouse = strInvoiceRule = strPriceList = strUserRep = strDeliveryViaRule = strFinPaymentMethodId = "";
    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);
    if (data != null && data.length > 0) {
      strDeliveryRule = data[0].deliveryrule.equals("") ? strDeliveryRule0 : data[0].deliveryrule;
      strUserRep = SEOrderBPartnerData.userIdSalesRep(this, data[0].salesrepId);
      strUserRep = strUserRep.equals("") ? strUserRep0 : strUserRep;
      strPaymentrule = (strIsSOTrx.equals("Y") ? data[0].paymentrule : data[0].paymentrulepo);
      strPaymentrule = strPaymentrule.equals("") ? strPaymentrule0 : strPaymentrule;

      strPaymentterm = (strIsSOTrx.equals("Y") ? data[0].cPaymenttermId : data[0].poPaymenttermId);
      if (strPaymentterm.equalsIgnoreCase("")) {
        BpartnerMiscData[] paymentTerm = BpartnerMiscData.selectPaymentTerm(this, strOrgId,
            vars.getClient());
        if (paymentTerm.length != 0) {
          strPaymentterm = strPaymentterm.equals("") ? paymentTerm[0].cPaymenttermId
              : strPaymentterm;
        }
      }

      strPaymentterm = strPaymentterm.equals("") ? strPaymentterm0 : strPaymentterm;

      strInvoiceRule = data[0].invoicerule.equals("") ? strInvoiceRule0 : data[0].invoicerule;
      strFinPaymentMethodId = (strIsSOTrx.equals("Y") ? data[0].finPaymentmethodId
          : data[0].poPaymentmethodId);
      strPriceList = (strIsSOTrx.equals("Y") ? data[0].mPricelistId : data[0].poPricelistId);
      if (strPriceList.equalsIgnoreCase("")) {
        strPriceList = SEOrderBPartnerData.defaultPriceList(this, strIsSOTrx, vars.getClient());
      }
      strPriceList = strPriceList.equals("") ? strPriceList0 : strPriceList;
      strDeliveryViaRule = data[0].deliveryviarule.equals("") ? strDeliveryViaRule0
          : data[0].deliveryviarule;
    }
    strMwarehouse = SEOrderBPartnerData.mWarehouse(this, strBPartner);

    if (strMwarehouse.equals(""))
      strMwarehouse = vars.getWarehouse();

    StringBuffer message = new StringBuffer();
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_Order_BPartner';\n\n");
    resultado.append("var respuesta = new Array(");

    resultado.append("new Array(\"inpmPricelistId\", \""
        + (strPriceList.equals("") ? Utility.getContext(this, vars, "#M_PriceList_ID", strWindowId)
            : strPriceList) + "\"),");

    if (strLocation.equals("")) {
      message.append(Utility.messageBD(this, "NoBPLocation", vars.getLanguage()));
    }

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
            + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \""
            + (tdv[i].getField("id").equalsIgnoreCase(strLocation) ? "true" : "false") + "\")");
        if (i < tdv.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n),");
    FieldProvider[] td = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "18", "M_Warehouse_ID", "197",
          "", Utility.getReferenceableOrg(vars, vars.getStringParameter("inpadOrgId")),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      td = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    resultado.append("new Array(\"inpmWarehouseId\", ");
    if (td != null && td.length > 0) {
      resultado.append("new Array(");
      for (int i = 0; i < td.length; i++) {
        resultado.append("new Array(\"" + td[i].getField("id") + "\", \""
            + FormatUtilities.replaceJS(td[i].getField("name")) + "\", \""
            + (td[i].getField("id").equalsIgnoreCase(strMwarehouse) ? "true" : "false") + "\")");
        if (i < td.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n),");
    resultado.append("new Array(\"inpsalesrepId\", ");
    FieldProvider[] tld = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
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
    FieldProvider[] l = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_Order InvoiceRule", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "SEOrderBPartner"),
          Utility.getContext(this, vars, "#User_Client", "SEOrderBPartner"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SEOrderBPartner", "");
      l = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    resultado.append("new Array(\"inpinvoicerule\", ");
    if (l != null && l.length > 0) {
      resultado.append("new Array(");
      for (int i = 0; i < l.length; i++) {
        resultado.append("new Array(\"" + l[i].getField("id") + "\", \""
            + FormatUtilities.replaceJS(l[i].getField("name")) + "\", \""
            + (l[i].getField("id").equalsIgnoreCase(strInvoiceRule) ? "true" : "false") + "\")");
        if (i < l.length - 1)
          resultado.append(",\n");
      }
      resultado.append("\n)");
    } else
      resultado.append("null");
    resultado.append("\n),");
    resultado.append("new Array(\"inpcProjectId\", \"\"),");
    resultado.append("new Array(\"inpcProjectId_R\", \"\"),");
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
    if (!"".equals(strFinPaymentMethodId))
      resultado.append("new Array(\"inpfinPaymentmethodId\", \"" + strFinPaymentMethodId + "\"),");
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
    resultado.append("new Array(\"inpdeliveryviarule\", \"" + strDeliveryViaRule + "\"),");
    resultado.append("new Array(\"inpisdiscountprinted\", \""
        + SEOrderBPartnerData.getIsDicountPrinted(this, strBPartner) + "\"),");
    resultado.append("new Array(\"inpcPaymenttermId\", \"" + strPaymentterm + "\"),");
    resultado.append("new Array(\"inpdeliveryrule\", ");
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_Order DeliveryRule", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "SEOrderBPartner"),
          Utility.getContext(this, vars, "#User_Client", "SEOrderBPartner"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SEOrderBPartner", "");
      l = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (strIsSOTrx.equals("N")) {
      resultado
          .append("\"")
          .append(
              (strDeliveryRule.equals("") ? ((l != null && l.length > 0) ? l[0].getField("id")
                  : "null") : strDeliveryRule)).append("\"");
    } else {
      if (l != null && l.length > 0) {
        resultado.append("new Array(");
        for (int i = 0; i < l.length; i++) {
          resultado.append("new Array(\"" + l[i].getField("id") + "\", \""
              + FormatUtilities.replaceJS(l[i].getField("name")) + "\", \""
              + (l[i].getField("id").equalsIgnoreCase(strDeliveryRule) ? "true" : "false") + "\")");
          if (i < l.length - 1)
            resultado.append(",\n");
        }
        resultado.append("\n)");
      } else
        resultado.append("null");
    }
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
    resultado.append("\n)");
    if (data != null && data.length > 0
        && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) < 0
        && strIsSOTrx.equals("Y")) {
      if (message.length() > 0)
        message.append("<br>");
      String creditLimitExceed = "" + Double.parseDouble(data[0].creditavailable) * -1;
      message.append(Utility.messageBD(this, "CreditLimitOver", vars.getLanguage())
          + creditLimitExceed);
    }

    if (message != null) {
      resultado.append(", new Array('MESSAGE', \"" + message + "\")");
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
