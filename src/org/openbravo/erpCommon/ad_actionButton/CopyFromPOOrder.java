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

public class CopyFromPOOrder extends HttpSecureAppServlet {
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
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strKey = vars.getGlobalVariable("inpcOrderId", strWindow + "|C_Order_ID");
      printPage(response, vars, strKey, strWindow, strTab, strProcessId);
    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strOrder = vars.getStringParameter("inpcOrderId");
      String strKey = vars.getRequestGlobalVariable("inpKey", strWindow + "|C_Order_ID");
      String strTab = vars.getStringParameter("inpTabId");

      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myError = processButton(vars, strKey, strOrder, strWindow);
      if (log4j.isDebugEnabled())
        log4j.debug(myError.getMessage());
      vars.setMessage(strTab, myError);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strKey, String strOrder,
      String windowId) {
    OBError myError = null;
    int i = 0;
    String priceactual = "";
    String pricelist = "";
    String pricelimit = "";
    String strPrecision = "0";
    String strPricePrecision = "0";
    String strDiscount = "";
    Connection conn = null;
    try {
      conn = getTransactionConnection();
      CopyFromPOOrderData[] data = CopyFromPOOrderData.selectLines(this, strOrder);
      CopyFromPOOrderData[] order = CopyFromPOOrderData.select(this, strKey);
      for (i = 0; data != null && i < data.length; i++) {
        CopyFromPOOrderData[] data3 = CopyFromPOOrderData.selectPriceForProduct(this,
            data[i].mProductId,
            order[0].mPricelistId.equals("") ? CopyFromPOOrderData.defaultPriceList(this)
                : order[0].mPricelistId);
        for (int j = 0; data3 != null && j < data3.length; j++) {
          if (data3[j].validfrom == null
              || data3[j].validfrom.equals("")
              || !DateTimeData.compare(this, DateTimeData.today(this), data3[j].validfrom).equals(
                  "-1")) {
            priceactual = data3[j].pricestd;
            pricelist = data3[j].pricelist;
            pricelimit = data3[j].pricelimit;
            CopyFromPOOrderData[] data4 = CopyFromPOOrderData.selectOrderPricelist(this, strKey);
            if (data4 != null && data4.length > 0) {
              strPrecision = data4[0].stdprecision.equals("") ? "0" : data4[0].stdprecision;
              strPricePrecision = data4[0].priceprecision.equals("") ? "0"
                  : data4[0].priceprecision;
            }
            int StdPrecision = Integer.valueOf(strPrecision).intValue();
            int PricePrecision = Integer.valueOf(strPricePrecision).intValue();

            BigDecimal priceActual, priceList, discount;

            priceActual = (priceactual.equals("") ? ZERO : (new BigDecimal(priceactual))).setScale(
                PricePrecision, BigDecimal.ROUND_HALF_UP);
            priceList = (pricelist.equals("") ? ZERO : new BigDecimal(pricelist));
            if (priceList.compareTo(ZERO) == 0)
              discount = ZERO;
            else
              discount = ((priceList.subtract(priceActual)).divide(priceList, 12,
                  BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100")); // ((PL-PA)/PL)*100
            if (discount.scale() > StdPrecision)
              discount = discount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
            strDiscount = discount.toString();
            priceactual = priceActual.toString();
            pricelist = priceList.toString();
          }
        }
        if (priceactual.equals(""))
          priceactual = "0";
        if (pricelist.equals(""))
          pricelist = "0";
        if (pricelimit.equals(""))
          pricelimit = "0";
        int line = 0;
        String strCTaxID = Tax.get(this, data[i].mProductId, order[0].datepromised,
            order[0].adOrgId, order[0].mWarehouseId.equals("") ? vars.getWarehouse()
                : order[0].mWarehouseId, CopyFromPOOrderData.cBPartnerLocationId(this,
                order[0].cBpartnerId), CopyFromPOOrderData.cBPartnerLocationId(this,
                order[0].cBpartnerId), order[0].cProjectId, order[0].issotrx.equals("Y") ? true
                : false);
        if (strCTaxID.equals("")) {
          myError = Utility.translateError(this, vars, vars.getLanguage(),
              Utility.messageBD(this, "TaxNotFound", vars.getLanguage()));
          return myError;
        }
        line = Integer.valueOf(order[0].line.equals("") ? "0" : order[0].line).intValue()
            + ((i + 1) * 10);
        String strCOrderlineID = SequenceIdData.getUUID();
        try {
          String isInstance = CopyFromPOOrderData.getIsInstanceValue(conn, this,
              data[i].mAttributesetinstanceId);
          if (isInstance != null && isInstance.equalsIgnoreCase("Y")) {
            String strMAttributesetinstanceID = SequenceIdData.getUUID();
            CopyFromPOOrderData.copyAttributes(conn, this, strMAttributesetinstanceID,
                vars.getUser(), vars.getUser(), data[i].mAttributesetinstanceId);
            CopyFromPOOrderData.copyInstances(conn, this, strMAttributesetinstanceID,
                vars.getUser(), vars.getUser(), data[i].mAttributesetinstanceId);
            data[i].mAttributesetinstanceId = strMAttributesetinstanceID;
          }
          CopyFromPOOrderData.insertCOrderline(
              conn,
              this,
              strCOrderlineID,
              order[0].adClientId,
              order[0].adOrgId,
              vars.getUser(),
              strKey,
              Integer.toString(line),
              order[0].cBpartnerId,
              order[0].cBpartnerLocationId.equals("") ? ExpenseSOrderData.cBPartnerLocationId(this,
                  order[0].cBpartnerId) : order[0].cBpartnerLocationId, order[0].dateordered,
              order[0].datepromised, data[i].description, data[i].mProductId, order[0].mWarehouseId
                  .equals("") ? vars.getWarehouse() : order[0].mWarehouseId, data[i].cUomId,
              data[i].qtyordered, data[i].quantityorder, data[i].cCurrencyId, pricelist,
              priceactual, pricelimit, strCTaxID, strDiscount, data[i].mProductUomId,
              data[i].orderline, data[i].mAttributesetinstanceId);
        } catch (ServletException ex) {
          myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myError;
        }

      }

      releaseCommitConnection(conn);
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(this, "RecordsCopied", vars.getLanguage()) + i);
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

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String strTab, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Copy lines");
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
        "org/openbravo/erpCommon/ad_actionButton/CopyFromPOOrder", discard).createXmlDocument();
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
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of the getServletInfo() method
}
