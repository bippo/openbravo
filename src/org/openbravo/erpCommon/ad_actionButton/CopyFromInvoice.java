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
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class CopyFromInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strKey = vars.getGlobalVariable("inpcInvoiceId", strWindow + "|C_Invoice_ID");
      printPage(response, vars, strKey, strWindow, strTab, strProcessId);
    } else if (vars.commandIn("SAVE")) {
      String strKey = vars.getStringParameter("inpcInvoiceId");
      String strInvoice = vars.getStringParameter("inpNewcInvoiceId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strPriceListCheck = vars.getStringParameter("inpPriceList");
      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myError = processButton(vars, strKey, strInvoice, strWindow, strPriceListCheck);
      vars.setMessage(strTab, myError);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strKey, String strInvoice,
      String windowId, String strPriceListCheck) {
    int i = 0;
    OBError myError = null;
    Connection conn = null;
    try {
      conn = getTransactionConnection();
      CopyFromInvoiceData[] data = CopyFromInvoiceData.select(conn, this, strInvoice,
          Utility.getContext(this, vars, "#User_Client", windowId),
          Utility.getContext(this, vars, "#User_Org", windowId));
      CopyFromInvoiceData[] dataInvoice = CopyFromInvoiceData.selectInvoice(conn, this, strKey);
      if (data != null && data.length != 0) {
        for (i = 0; i < data.length; i++) {
          String strSequence = SequenceIdData.getUUID();
          try {
            String strDateInvoiced = "";
            String strInvPriceList = "";
            String strBPartnerId = "";
            String strPricePrecision = "0";
            String strmProductId = "";
            String strQty = "";
            String priceactual = "";
            String pricestd = "";
            String pricelist = "";
            String pricelimit = "";
            String linenetamt = "";
            strDateInvoiced = dataInvoice[0].dateinvoiced;
            strInvPriceList = dataInvoice[0].mPricelistId;
            strBPartnerId = dataInvoice[0].mPricelistId;
            strmProductId = data[i].productId;
            strQty = data[i].qtyinvoiced;
            String strWindowId = vars.getStringParameter("inpwindowId");
            String strWharehouse = Utility.getContext(this, vars, "#M_Warehouse_ID", strWindowId);
            String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);

            String strCTaxID = Tax.get(this, data[i].productId, dataInvoice[0].dateinvoiced,
                dataInvoice[0].adOrgId, strWharehouse, dataInvoice[0].cBpartnerLocationId,
                dataInvoice[0].cBpartnerLocationId, dataInvoice[0].cProjectId,
                strIsSOTrx.equals("Y"), data[i].accountId);

            if ("Y".equals(strPriceListCheck)) {

              CopyFromInvoiceData[] invoicelineprice = CopyFromInvoiceData.selectPriceForProduct(
                  this, strmProductId, strInvPriceList);
              for (int j = 0; invoicelineprice != null && j < invoicelineprice.length; j++) {
                if (invoicelineprice[j].validfrom == null
                    || invoicelineprice[j].validfrom.equals("")
                    || !DateTimeData.compare(this, DateTimeData.today(this),
                        invoicelineprice[j].validfrom).equals("-1")) {
                  pricestd = invoicelineprice[j].pricestd;
                  pricelist = invoicelineprice[j].pricelist;
                  pricelimit = invoicelineprice[j].pricelimit;
                  CopyFromInvoiceData[] invoicePriceList = CopyFromInvoiceData
                      .selectInvoicePricelist(this, strKey);
                  if (invoicePriceList != null && invoicePriceList.length > 0) {
                    strPricePrecision = invoicePriceList[0].priceprecision.equals("") ? "0"
                        : invoicePriceList[0].priceprecision;
                  }
                  int PricePrecision = Integer.valueOf(strPricePrecision).intValue();

                  BigDecimal priceStd, priceActual, qtyInvoiced, lineNetAmt;

                  priceStd = (pricestd.equals("") ? BigDecimal.ZERO : (new BigDecimal(pricestd)))
                      .setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
                  qtyInvoiced = (data[i].qtyinvoiced.equals("") ? BigDecimal.ZERO : new BigDecimal(
                      data[i].qtyinvoiced));
                  // Calculate price adjustments (offers)
                  priceActual = new BigDecimal(CopyFromInvoiceData.getOffersStdPrice(this,
                      strBPartnerId, pricestd, strmProductId, strDateInvoiced, strQty,
                      strInvPriceList, strKey));
                  if (priceActual.scale() > PricePrecision)
                    priceActual = priceActual.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
                  // Calculate line net amount
                  lineNetAmt = qtyInvoiced.multiply(priceActual);
                  if (lineNetAmt.scale() > PricePrecision)
                    lineNetAmt = lineNetAmt.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
                  pricestd = priceStd.toString();
                  priceactual = priceActual.toString();
                  linenetamt = lineNetAmt.toString();
                }
              }
              if (pricestd.equals(""))
                pricestd = "0";
              if (pricelist.equals(""))
                pricelist = "0";
              if (pricelimit.equals(""))
                pricelimit = "0";
              if (priceactual.equals(""))
                priceactual = "0";
            } else {
              pricelist = data[i].pricelist;
              pricelimit = data[i].pricelimit;
              priceactual = data[i].priceactual;
              linenetamt = data[i].linenetamt;
            }
            CopyFromInvoiceData.insert(conn, this, strSequence, strKey, dataInvoice[0].adClientId,
                dataInvoice[0].adOrgId, vars.getUser(), pricelist, priceactual, pricelimit,
                linenetamt, strCTaxID, data[i].cInvoicelineId);

            // Copy accounting dimensions
            CopyFromInvoiceData.insertAcctDimension(conn, this, dataInvoice[0].adClientId,
                dataInvoice[0].adOrgId, vars.getUser(), strSequence, data[i].cInvoicelineId);
          } catch (ServletException ex) {
            myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
          }
        }
      }
      releaseCommitConnection(conn);
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      log4j.warn("Rollback in transaction", e);
      myError = new OBError();
      myError.setType("Error");
      myError.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myError;
    }
    myError = new OBError();
    myError.setType("Success");
    myError.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
    myError.setMessage(Utility.messageBD(this, "RecordsCopied", vars.getLanguage()) + " " + i);
    return myError;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String strTab, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Copy from Invoice");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);

    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CopyFromInvoice", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("question",
        Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from invoice";
  } // end of getServletInfo() method
}
