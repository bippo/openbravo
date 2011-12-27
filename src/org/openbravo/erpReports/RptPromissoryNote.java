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
package org.openbravo.erpReports;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class RptPromissoryNote extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcDebtPaymentId = vars.getSessionValue("RptPromissoryNote.inpcDebtPaymentId_R");
      if (strcDebtPaymentId.equals(""))
        strcDebtPaymentId = vars.getSessionValue("RptPromissoryNote.inpcDebtPaymentId");
      printPagePDF(response, vars, strcDebtPaymentId);
    } else {
      pageError(response);
    }

  }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strcDebtPaymentId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");
    if (!strcDebtPaymentId.equals("")) {
      RptPromissoryNoteData[] data = RptPromissoryNoteData.select(this,
          Utility.getContext(this, vars, "#AccessibleOrgTree", "RptPromissoryNote"),
          Utility.getContext(this, vars, "#User_Client", "RptPromissoryNote"), strcDebtPaymentId);
      RptPromissoryNoteHeaderData[][] pdfPromissoryNoteHeaderData = null;
      RptPromissoryNoteAfterData[][] pdfPromissoryNoteAfterData = null;
      RptPromissoryNoteErrorData[][] pdfPromissoryNoteErrorData = null;

      if (data == null || data.length == 0) {
        data = RptPromissoryNoteData.selectDebtPayment(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "RptPromissoryNote"),
            Utility.getContext(this, vars, "#User_Client", "RptPromissoryNote"), strcDebtPaymentId);
        pdfPromissoryNoteHeaderData = new RptPromissoryNoteHeaderData[data.length][];
        pdfPromissoryNoteAfterData = new RptPromissoryNoteAfterData[data.length][];
        pdfPromissoryNoteErrorData = new RptPromissoryNoteErrorData[data.length][];
        for (int i = 0; i < data.length; i++) {
          String strcBankaccountId = RptPromissoryNoteAfterData.selectDebtPaymentBank(this,
              data[i].cDebtPaymentId);
          if (!strcBankaccountId.equals("")) {
            String strcPromissoryFormatId = RptPromissoryNoteAfterData.selectPromissoryformat(this,
                strcBankaccountId);

            if (strcPromissoryFormatId != null && !strcPromissoryFormatId.equals("")) {
              pdfPromissoryNoteHeaderData[i] = RptPromissoryNoteHeaderData.selectDebtPayment(this,
                  data[i].cDebtPaymentId);

              if (pdfPromissoryNoteHeaderData[i] == null
                  || pdfPromissoryNoteHeaderData[i].length == 0)
                pdfPromissoryNoteHeaderData[i] = RptPromissoryNoteHeaderData.set();

              pdfPromissoryNoteAfterData[i] = RptPromissoryNoteAfterData.selectDebtPayment(this,
                  data[i].cDebtPaymentId);
              if (pdfPromissoryNoteAfterData[i] == null
                  || pdfPromissoryNoteAfterData[i].length == 0)
                pdfPromissoryNoteAfterData[i] = RptPromissoryNoteAfterData.set();

              pdfPromissoryNoteErrorData[i] = new RptPromissoryNoteErrorData[0];
              String printbank = RptPromissoryNoteAfterData.selectBanklocation(this,
                  strcBankaccountId);
              if (!printbank.equals("Y"))
                pdfPromissoryNoteAfterData[i][0].banklocation = "";
            } else {
              pdfPromissoryNoteErrorData[i] = RptPromissoryNoteErrorData.select(this,
                  "PromissoryNoteFormat");
              pdfPromissoryNoteHeaderData[i] = new RptPromissoryNoteHeaderData[0];
              pdfPromissoryNoteAfterData[i] = new RptPromissoryNoteAfterData[0];
            }
          } else {
            pdfPromissoryNoteErrorData[i] = RptPromissoryNoteErrorData.select(this,
                "PromissoryNoteBank");
            pdfPromissoryNoteHeaderData[i] = new RptPromissoryNoteHeaderData[0];
            pdfPromissoryNoteAfterData[i] = new RptPromissoryNoteAfterData[0];
          }

        }
      } else {
        pdfPromissoryNoteHeaderData = new RptPromissoryNoteHeaderData[data.length][];
        pdfPromissoryNoteAfterData = new RptPromissoryNoteAfterData[data.length][];
        pdfPromissoryNoteErrorData = new RptPromissoryNoteErrorData[data.length][];
        for (int i = 0; i < data.length; i++) {
          String strcBankaccountId = RptPromissoryNoteAfterData.selectDebtPaymentBank(this,
              data[i].cDebtPaymentId);
          if (!strcBankaccountId.equals("")) {
            String strcPromissoryFormatId = RptPromissoryNoteAfterData.selectPromissoryformat(this,
                strcBankaccountId);

            if (strcPromissoryFormatId != null && !strcPromissoryFormatId.equals("")) {
              pdfPromissoryNoteHeaderData[i] = RptPromissoryNoteHeaderData.select(this,
                  data[i].cDebtPaymentId);

              if (pdfPromissoryNoteHeaderData[i] == null
                  || pdfPromissoryNoteHeaderData[i].length == 0) {
                pdfPromissoryNoteHeaderData[i] = RptPromissoryNoteHeaderData.set();
              } else {
                // we have to cover the whole debt-payments tree
                // until we find the cancelled invoices
                pdfPromissoryNoteHeaderData[i][0].documentno = debtPaymentTree(data[i].cDebtPaymentId);
              }

              pdfPromissoryNoteAfterData[i] = RptPromissoryNoteAfterData.select(this,
                  data[i].cDebtPaymentId);

              if (pdfPromissoryNoteAfterData[i] == null
                  || pdfPromissoryNoteAfterData[i].length == 0)
                pdfPromissoryNoteAfterData[i] = RptPromissoryNoteAfterData.set();

              pdfPromissoryNoteErrorData[i] = new RptPromissoryNoteErrorData[0];
              String printbank = RptPromissoryNoteAfterData.selectBanklocation(this,
                  strcBankaccountId);
              if (!printbank.equals("Y"))
                pdfPromissoryNoteAfterData[i][0].banklocation = "";
            } else {

              pdfPromissoryNoteErrorData[i] = RptPromissoryNoteErrorData.select(this,
                  Utility.messageBD(this, "PromissoryNoteFormat", vars.getLanguage()));
              pdfPromissoryNoteHeaderData[i] = new RptPromissoryNoteHeaderData[0];
              pdfPromissoryNoteAfterData[i] = new RptPromissoryNoteAfterData[0];
            }
          } else {
            pdfPromissoryNoteErrorData[i] = RptPromissoryNoteErrorData.select(this,
                Utility.messageBD(this, "PromissoryNoteBank", vars.getLanguage()));
            pdfPromissoryNoteHeaderData[i] = new RptPromissoryNoteHeaderData[0];
            pdfPromissoryNoteAfterData[i] = new RptPromissoryNoteAfterData[0];
          }

        }
      }

      XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpReports/RptPromissoryNote").createXmlDocument();

      xmlDocument.setData("structure1", data);
      xmlDocument.setDataArray("reportPromissoryNoteHeader", "structureHeader",
          pdfPromissoryNoteHeaderData);
      xmlDocument.setDataArray("reportPromissoryNoteAfter", "structureAfter",
          pdfPromissoryNoteAfterData);
      xmlDocument.setDataArray("reportPromissoryNoteError", "structureError",
          pdfPromissoryNoteErrorData);

      String strResult = xmlDocument.print();
      renderFO(strResult, response);
    }
  }

  private String debtPaymentTree(String strcDebtPaymentId) throws IOException, ServletException {
    String strDocumentno = "";
    // strMark=(sale, buy);
    // boolean[] strMark = {true, true};
    strcDebtPaymentId = "(" + strcDebtPaymentId + ")";
    while (true) {
      RptPromissoryNoteTreeData[] data = RptPromissoryNoteTreeData.select(this, strcDebtPaymentId);
      strcDebtPaymentId = "(";
      if (data == null || data.length == 0)
        break;
      for (int i = 0; i < data.length; i++) {
        if (!data[i].cInvoiceId.equals("")) {
          if (data[i].issotrx.equals("Y"))
            strDocumentno += "Nuestra factura nº: " + data[i].documentno + "   * "
                + data[i].grandtotal + " *";
          else
            strDocumentno += "Pago su fra. nº: " + data[i].poreference + "   * "
                + data[i].grandtotal + " *";
          strDocumentno += "\n";
        }
        strcDebtPaymentId += data[i].cDebtPaymentId;
        if (i != data.length - 1)
          strcDebtPaymentId += ",";
      }
      strcDebtPaymentId += ")";
    }
    return strDocumentno;
  }

  public String getServletInfo() {
    return "Servlet ReportMInout. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
