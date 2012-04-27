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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
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
import org.openbravo.erpCommon.reference.ListData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Order_DocType extends HttpSecureAppServlet {
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
      String strDocTypeTarget = vars.getStringParameter("inpcDoctypetargetId");
      String strDocType = vars.getStringParameter("inpcDoctypeId");
      String docNo = vars.getStringParameter("inpdocumentno");
      String strOrder = vars.getStringParameter("inpcOrderId");
      String strDescription = vars.getStringParameter("inpdescription");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strBPartner, strDocTypeTarget, strDocType, docNo, strOrder,
            strDescription, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner,
      String strDocTypeTarget, String strDocType, String docNo, String strOrder,
      String strDescription, String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    if (strDocTypeTarget.equals(""))
      resultado.append("var respuesta = null;");
    else {
      resultado.append("var calloutName='SL_Order_DocType';\n\n");
      resultado.append("var respuesta = new Array(");
      String PaymentRule = "P";
      String InvoiceRule = "D";
      String DeliveryRule = "A";
      boolean newDocNo = docNo.equals("");
      if (!newDocNo && docNo.startsWith("<") && docNo.endsWith(">"))
        newDocNo = true;
      String AD_Sequence_ID = "0";
      SLOrderDocTypeData[] data = null;

      if (!newDocNo && !"0".equals(strDocType)) {
        data = SLOrderDocTypeData.select(this, strDocType);
        if (data != null && data.length > 0) {
          AD_Sequence_ID = data[0].adSequenceId;
        }
      }
      String DocSubTypeSO = "";
      boolean IsSOTrx = true;
      SLOrderDocTypeData[] dataNew = SLOrderDocTypeData.select(this, strDocTypeTarget);
      if (dataNew != null && dataNew.length > 0) {
        DocSubTypeSO = dataNew[0].docsubtypeso;
        if (DocSubTypeSO == null)
          DocSubTypeSO = "--";
        String strOldDocTypeTarget = SLOrderDocTypeData.selectOldDocSubType(this, strOrder);
        if (!DocSubTypeSO.equals("OB") && strOldDocTypeTarget.equals("OB")) {
          String strOldDocNo = SLOrderDocTypeData.selectOldDocNo(this, strOrder);
          resultado.append("new Array(\"inpdescription\", \""
              + FormatUtilities.replaceJS(Utility.messageBD(this, "Quotation", vars.getLanguage())
                  + " " + strOldDocNo + ". " + strDescription) + "\"),\n");
        }
        resultado.append("new Array(\"inpordertype\", \"" + DocSubTypeSO + "\")\n");
        PaymentRule = "P";
        InvoiceRule = (DocSubTypeSO.equals("PR") || DocSubTypeSO.equals("WI") ? "I" : "D");
        DeliveryRule = "A";
        if (dataNew[0].isdocnocontrolled.equals("Y")) {
          if (!newDocNo
              && !AD_Sequence_ID.equals(dataNew[0].adSequenceId)
              && !SLOrderDocTypeData.selectOldDocTypeTargetId(this, strOrder).equalsIgnoreCase(
                  strDocTypeTarget))
            newDocNo = true;
          if (newDocNo) {
            if (vars.getRole().equalsIgnoreCase("System")
                && new BigDecimal(vars.getClient()).compareTo(new BigDecimal("1000000.0")) < 0)
              resultado.append(", new Array(\"inpdocumentno\", \"<" + dataNew[0].currentnextsys
                  + ">\")\n");
            else
              resultado.append(", new Array(\"inpdocumentno\", \"<" + dataNew[0].currentnext
                  + ">\")\n");
          }
        }
        if (dataNew[0].issotrx.equals("N"))
          IsSOTrx = false;
      }

      if (!DocSubTypeSO.equalsIgnoreCase("WR")) {
        SLOrderDocTypeData[] dataBP = SLOrderDocTypeData.BPartner(this, strBPartner);
        if (dataBP != null && dataBP.length > 0) {
          String s = (IsSOTrx ? dataBP[0].paymentrule : dataBP[0].paymentrulepo);
          if (s != null && s.length() != 0) {
            if (s.equals("B"))
              s = "P";
            if (IsSOTrx && (s.equals("S") || s.equals("U")))
              s = "P";
            if (!s.equals(""))
              PaymentRule = s;
          }
          InvoiceRule = (DocSubTypeSO.equals("PR") || DocSubTypeSO.equals("WI") ? "I"
              : dataBP[0].invoicerule);
          DeliveryRule = dataBP[0].deliveryrule;
          if (!dataBP[0].deliveryviarule.equals(""))
            resultado.append(", new Array(\"inpdeliveryviarule\", \"" + dataBP[0].deliveryviarule
                + "\")\n");
        }
        // Added by gorkaion remove when feature request 4350 is done
        FieldProvider[] l = null;
        try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
              "C_Order InvoiceRule", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "SLOrderDocType"), Utility.getContext(this, vars, "#User_Client",
                  "SLOrderDocType"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "SLOrderDocType", "");
          l = comboTableData.select(false);
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
        resultado.append(", new Array(\"inpinvoicerule\", ");
        if (l != null && l.length > 0) {
          resultado.append("new Array(");
          for (int i = 0; i < l.length; i++) {
            resultado.append("new Array(\"" + l[i].getField("id") + "\", \""
                + FormatUtilities.replaceJS(l[i].getField("name")) + "\", \""
                + (l[i].getField("id").equalsIgnoreCase(InvoiceRule) ? "true" : "false") + "\")");
            if (i < l.length - 1)
              resultado.append(",\n");
          }
          resultado.append(")");
        } else
          resultado.append("null");
        resultado.append(")");
        InvoiceRule = "";
      } else {
        resultado.append(", new Array(\"inpinvoicerule\", new Array(");
        resultado.append("new Array(\"D\", \"")
            .append(FormatUtilities.replaceJS(ListData.selectName(this, "150", "D")))
            .append("\", ").append(InvoiceRule.equals("D") ? "true" : "false").append("),");
        resultado.append("new Array(\"I\", \"")
            .append(FormatUtilities.replaceJS(ListData.selectName(this, "150", "I")))
            .append("\", ").append(InvoiceRule.equals("I") ? "true" : "false").append("),");
        resultado.append("new Array(\"O\", \"")
            .append(FormatUtilities.replaceJS(ListData.selectName(this, "150", "O")))
            .append("\", ").append(InvoiceRule.equals("O") ? "true" : "false").append(")))\n");
        InvoiceRule = "";
        // End of add
      }
      if (!PaymentRule.equals(""))
        resultado.append(", new Array(\"inppaymentrule\", \"" + PaymentRule + "\")\n");
      if (!InvoiceRule.equals(""))
        resultado.append(", new Array(\"inpinvoicerule\", \"" + InvoiceRule + "\")\n");
      if (!DeliveryRule.equals(""))
        resultado.append(", new Array(\"inpdeliveryrule\", \"" + DeliveryRule + "\")\n");
      resultado.append(", new Array(\"EXECUTE\", \"displayLogic();\")\n");
      resultado.append(");\n");
    }
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
