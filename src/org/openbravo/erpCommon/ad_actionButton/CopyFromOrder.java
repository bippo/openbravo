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
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsPositiveIntFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class CopyFromOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strKey = vars.getGlobalVariable("inpcOrderId", strWindowId + "|C_Order_ID");
      String strTabId = vars.getStringParameter("inpTabId");
      String strBpartner = vars.getStringParameter("inpcBpartnerId");
      String strmPricelistId = vars.getStringParameter("inpmPricelistId");
      printPageDataSheet(response, vars, strKey, strWindowId, strTabId, strSOTrx, strBpartner,
          strmPricelistId);
    } else if (vars.commandIn("SAVE")) {
      String strRownum = null;
      try {
        strRownum = vars.getRequiredInStringParameter("inpRownumId", IsPositiveIntFilter.instance);
      } catch (ServletException e) {
        log4j.error("Error captured: ", e);
        throw new ServletException(Utility.messageBD(new DalConnectionProvider(), "@JS1@",
            OBContext.getOBContext().getLanguage().getLanguage()));
      }
      String strKey = vars.getRequiredStringParameter("inpcOrderId");
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strSOTrx = vars.getStringParameter("inpissotrx");
      String strTabId = vars.getStringParameter("inpTabId");
      OBError myError = copyLines(vars, strRownum, strKey, strWindowId, strSOTrx);

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      vars.setMessage(strTabId, myError);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError copyLines(VariablesSecureApp vars, String strRownum, String strKey,
      String strWindowId, String strSOTrx) throws IOException, ServletException {

    OBError myError = null;
    int count = 0;

    if (strRownum.equals("")) {
      // return "";
      myError = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
      return myError;
    }
    Connection conn = null;
    try {
      conn = getTransactionConnection();
      if (strRownum.startsWith("("))
        strRownum = strRownum.substring(1, strRownum.length() - 1);
      if (!strRownum.equals("")) {
        strRownum = Replace.replace(strRownum, "'", "");
        StringTokenizer st = new StringTokenizer(strRownum, ",", false);
        BigDecimal discount, priceActual, priceList, priceStd;
        while (st.hasMoreTokens()) {
          strRownum = st.nextToken().trim();
          String strmProductId = vars.getStringParameter("inpmProductId" + strRownum);
          String strmAttributesetinstanceId = vars.getStringParameter("inpmAttributesetinstanceId"
              + strRownum);
          String strLastpriceso = vars.getNumericParameter("inpLastpriceso" + strRownum);
          String strQty = vars.getNumericParameter("inpquantity" + strRownum);
          String strcTaxId = vars.getStringParameter("inpcTaxId" + strRownum);
          String strcUOMId = vars.getStringParameter("inpcUOMId" + strRownum);
          String strPrecision = "0";
          String strPricePrecision = "0";
          String strCOrderlineID = SequenceIdData.getUUID();
          CopyFromOrderRecordData[] order = CopyFromOrderRecordData.select(this, strKey);
          CopyFromOrderData[] orderlineprice = CopyFromOrderData.selectPrices(this,
              order[0].dateordered, strmProductId, order[0].mPricelistId);
          priceStd = new BigDecimal(CopyFromOrderData.getOffersStdPrice(this, order[0].cBpartnerId,
              strLastpriceso, strmProductId, order[0].dateordered, strQty, order[0].mPricelistId,
              strKey));
          if (orderlineprice == null || orderlineprice.length == 0) {
            orderlineprice = CopyFromOrderData.set();
            orderlineprice[0].pricelist = "0";
            orderlineprice[0].pricelimit = "0";
            CopyFromOrderData[] precisions = CopyFromOrderData.selectPrecisions(this, strKey);
            strPrecision = precisions[0].stdprecision.equals("") ? "0" : precisions[0].stdprecision;
            strPricePrecision = precisions[0].priceprecision.equals("") ? "0"
                : precisions[0].priceprecision;
          } else {
            strPrecision = orderlineprice[0].stdprecision.equals("") ? "0"
                : orderlineprice[0].stdprecision;
            strPricePrecision = orderlineprice[0].priceprecision.equals("") ? "0"
                : orderlineprice[0].priceprecision;
          }
          int StdPrecision = Integer.valueOf(strPrecision).intValue();
          int PricePrecision = Integer.valueOf(strPricePrecision).intValue();
          priceList = (orderlineprice[0].pricelist.equals("") ? ZERO : new BigDecimal(
              orderlineprice[0].pricelist));
          priceActual = (strLastpriceso.equals("") ? ZERO : new BigDecimal(strLastpriceso));

          if (priceList.compareTo(BigDecimal.ZERO) == 0)
            discount = ZERO;
          else {
            if (log4j.isDebugEnabled())
              log4j.debug("pricelist:" + Double.toString(priceList.doubleValue()));
            if (log4j.isDebugEnabled())
              log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));
            discount = ((priceList.subtract(priceActual)).divide(priceList, 12,
                BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100")); // (PL-PA)/PL*
            // 100
          }
          if (log4j.isDebugEnabled())
            log4j.debug("Discount: " + discount.toString());
          if (discount.scale() > StdPrecision)
            discount = discount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
          if (priceStd.scale() > PricePrecision)
            priceStd = priceStd.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);

          try {
            CopyFromOrderData.insertCOrderline(conn, this, strCOrderlineID, order[0].adClientId,
                order[0].adOrgId, vars.getUser(), strKey, order[0].cBpartnerId,
                order[0].cBpartnerLocationId, order[0].dateordered, order[0].dateordered,
                strmProductId, order[0].mWarehouseId.equals("") ? vars.getWarehouse()
                    : order[0].mWarehouseId, strcUOMId, strQty, order[0].cCurrencyId,
                orderlineprice[0].pricelist, strLastpriceso, orderlineprice[0].pricelimit, priceStd
                    .toString(), discount.toString(), strcTaxId, strmAttributesetinstanceId);
          } catch (ServletException ex) {
            myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myError;
          }
          count++;
        }
      }
      releaseCommitConnection(conn);
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myError
          .setMessage(Utility.messageBD(this, "RecordsCopied", vars.getLanguage()) + " " + count);
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myError = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myError;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strKey, String strWindowId, String strTabId, String strSOTrx, String strBpartner,
      String strmPricelistId) throws IOException, ServletException {
    log4j.debug("Output: Shipment");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CopyFromOrder").createXmlDocument();
    CopyFromOrderRecordData[] dataOrder = CopyFromOrderRecordData.select(this, strKey);
    CopyFromOrderData[] data = CopyFromOrderData.select(this, strBpartner, strmPricelistId,
        dataOrder[0].dateordered, strSOTrx, dataOrder[0].lastDays.equals("") ? "0"
            : dataOrder[0].lastDays);
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("sotrx", strSOTrx);
    xmlDocument.setParameter("yearactual", DateTimeData.sysdateYear(this));
    xmlDocument.setParameter("lastmonth", dataOrder[0].lastDays.equals("") ? "0"
        : dataOrder[0].lastDays);
    xmlDocument.setParameter(
        "pendingdelivery",
        strSOTrx.equals("Y") ? CopyFromOrderRecordData.pendingDeliverySales(this, strBpartner,
            dataOrder[0].adOrgId, dataOrder[0].adClientId) : CopyFromOrderRecordData
            .materialReceiptPending(this, strBpartner, dataOrder[0].adOrgId,
                dataOrder[0].adClientId));
    xmlDocument.setParameter(
        "pendingInvoice",
        strSOTrx.equals("Y") ? CopyFromOrderRecordData.pendingInvoiceSales(this, strBpartner,
            dataOrder[0].adOrgId, dataOrder[0].adClientId) : CopyFromOrderRecordData
            .purchasePendingInvoice(this, strBpartner, dataOrder[0].adOrgId,
                dataOrder[0].adClientId));
    xmlDocument.setParameter("debtpending", CopyFromOrderRecordData.debtPending(this, strBpartner,
        dataOrder[0].adOrgId, dataOrder[0].adClientId, strSOTrx));
    xmlDocument.setParameter("contact",
        CopyFromOrderRecordData.contact(this, dataOrder[0].adUserId));
    xmlDocument.setParameter("lastOrder", CopyFromOrderRecordData.maxDateordered(this,
        vars.getSqlDateFormat(), strBpartner, strSOTrx, dataOrder[0].adOrgId,
        dataOrder[0].adClientId));
    xmlDocument.setParameter("orgname", dataOrder[0].orgname);
    String strInvoicing = CopyFromOrderRecordData.invoicing(this, strSOTrx, strBpartner,
        dataOrder[0].adOrgId, dataOrder[0].adClientId);
    String strTotal = CopyFromOrderRecordData.invoicingTotal(this, strSOTrx, dataOrder[0].adOrgId,
        dataOrder[0].adClientId);
    xmlDocument.setParameter("invoicing", strInvoicing);
    xmlDocument.setParameter("bpartnername", dataOrder[0].bpartnername);

    BigDecimal invoicing, total, totalAverage;

    invoicing = (strInvoicing.equals("") ? ZERO : (new BigDecimal(strInvoicing)));
    total = (strTotal.equals("") ? ZERO : new BigDecimal(strTotal));
    String strTotalAverage = "";
    if (total == ZERO) {
      totalAverage = (invoicing.divide(total, 12, BigDecimal.ROUND_HALF_EVEN))
          .multiply(new BigDecimal("100"));
      totalAverage = totalAverage.setScale(2, BigDecimal.ROUND_HALF_UP);
      strTotalAverage = totalAverage.toPlainString();
      // int intscale = totalAverage.scale();
    }

    xmlDocument.setParameter("totalAverage", strTotalAverage);

    xmlDocument.setData("structure1", data);
    xmlDocument.setData("structure2", dataOrder);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
