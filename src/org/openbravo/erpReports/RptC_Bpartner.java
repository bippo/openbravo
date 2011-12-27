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

package org.openbravo.erpReports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.ad.ui.WindowTrl;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.xmlEngine.XmlDocument;

public class RptC_Bpartner extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcBpartnerId = vars.getSessionValue("RptC_Bpartner.inpcBpartnerId_R");
      if (strcBpartnerId.equals(""))
        strcBpartnerId = vars.getSessionValue("RptC_Bpartner.inpcBpartnerId");
      printPageDataSheet(response, vars, strcBpartnerId);
    } else if (vars.commandIn("OPEN")) {
      String strcBpartnerId = vars.getRequiredStringParameter("inpcBpartnerId");
      String strmProductTemplate = vars.getRequiredStringParameter("inpProductTemplate");
      printPageAjaxResponse(response, vars, strcBpartnerId, strmProductTemplate);
    } else if (vars.commandIn("OPENDOCUMENT")) {
      String strcBpartnerId = vars.getRequiredStringParameter("inpcBpartnerId");
      String strmTypeDocument = vars.getRequiredStringParameter("inpTypeDocument");
      printPageAjaxDocumentResponse(response, vars, strcBpartnerId, strmTypeDocument);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strcBpartnerId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "", "", "", "", "", "", "", "", "", "", "", "", "" };
    XmlDocument xmlDocument = null;

    try {
      OBContext.setAdminMode(true);
      RptCBpartnerData[] dataPartner = RptCBpartnerData.select(this, vars.getLanguage(),
          strcBpartnerId);
      RptCBpartnerData[] dataAccount = RptCBpartnerData.selectAccount(this, strcBpartnerId);
      RptCBpartnerData[] dataShipper = RptCBpartnerData.selectShipper(this, strcBpartnerId);
      RptCBpartnerData[] dataTemplate = RptCBpartnerData.selectTemplate(this, vars.getLanguage(),
          strcBpartnerId);
      RptCBpartnerData[] dataDiscount = RptCBpartnerData.selectDiscount(this, strcBpartnerId);

      Client c = OBDal.getInstance().get(Client.class,
          OBContext.getOBContext().getCurrentClient().getId());

      RptCBpartnerSalesData[] dataPaymentsIn = RptCBpartnerSalesData.selectPayments(this,
          "PAYMENTIN", c.getCurrency().getId(), "Y", strcBpartnerId);
      RptCBpartnerSalesData[] dataPaymentsOut = RptCBpartnerSalesData.selectPayments(this,
          "PAYMENTOUT", c.getCurrency().getId(), "N", strcBpartnerId);

      RptCBpartnerCustomerData[] dataCustomer = RptCBpartnerCustomerData.select(this,
          vars.getLanguage(), strcBpartnerId);
      RptCBpartnerVendorData[] dataVendor = RptCBpartnerVendorData.select(this, vars.getLanguage(),
          strcBpartnerId);
      RptCBpartnerlocationData[] dataLocation = RptCBpartnerlocationData.select(this,
          strcBpartnerId);
      RptCBpartnercontactData[] dataContact = RptCBpartnercontactData.select(this, strcBpartnerId);
      RptCBpartnerSalesData[] dataSales = RptCBpartnerSalesData.selectOrder(this, strcBpartnerId);
      RptCBpartnerSalesData[] dataInvoice = RptCBpartnerSalesData.select(this, strcBpartnerId);
      RptCBpartnerSalesData[] dataInout = RptCBpartnerSalesData.selectinout(this, strcBpartnerId);
      RptCBpartnerSalesData[] dataABC = RptCBpartnerSalesData.selectABC(this,
          DateTimeData.sysdateYear(this), DateTimeData.lastYear(this), strcBpartnerId);

      if (dataAccount == null || dataAccount.length == 0) {
        dataAccount = RptCBpartnerData.set();
        discard[0] = "selDelete1";
      }
      if (dataShipper == null || dataShipper.length == 0) {
        dataShipper = RptCBpartnerData.set();
        discard[1] = "selDelete2";
      }
      if (dataTemplate == null || dataTemplate.length == 0) {
        dataTemplate = RptCBpartnerData.set();
        discard[2] = "selDelete4";
      }
      if (dataDiscount == null || dataDiscount.length == 0) {
        dataDiscount = RptCBpartnerData.set();
        discard[3] = "selDelete3";
      }
      if (dataCustomer == null || dataCustomer.length == 0) {
        dataCustomer = RptCBpartnerCustomerData.set();
        discard[4] = "selDelete5";
      } else {
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strcBpartnerId);
        dataCustomer[0].availablecredit = getCustomerCredit(bp, true).toString();
      }
      if (dataVendor == null || dataVendor.length == 0) {
        dataVendor = RptCBpartnerVendorData.set();
        discard[5] = "selDelete6";
      }
      if (dataLocation == null || dataLocation.length == 0) {
        dataLocation = RptCBpartnerlocationData.set();
        discard[6] = "selDelete7";
      }
      if (dataContact == null || dataContact.length == 0) {
        dataContact = RptCBpartnercontactData.set();
        discard[7] = "selDelete8";
      }
      if (dataSales == null || dataSales.length == 0) {
        dataSales = RptCBpartnerSalesData.set();
        discard[8] = "selDelete9";
      }
      if (dataSales == null || dataSales.length == 0) {
        dataInvoice = RptCBpartnerSalesData.set();
        discard[9] = "selDelete10";
      }
      if (dataSales == null || dataSales.length == 0) {
        dataInout = RptCBpartnerSalesData.set();
        discard[10] = "selDelete11";
      }
      if ((dataPaymentsIn == null || dataPaymentsIn.length == 0)
          && (dataPaymentsOut == null || dataPaymentsOut.length == 0)) {
        discard[11] = "selDeleteOpenItems";
      } else if (dataPaymentsIn == null || dataPaymentsIn.length == 0) {
        dataPaymentsIn = RptCBpartnerSalesData.set();
        discard[11] = "sectionPaymentin";
      } else if (dataPaymentsOut == null || dataPaymentsOut.length == 0) {
        dataPaymentsOut = RptCBpartnerSalesData.set();
        discard[11] = "sectionPaymentout";
      }

      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_Bpartner", discard)
          .createXmlDocument();
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("paramBpartner", dataPartner[0].cBpartnerId);
      xmlDocument.setParameter("paramSysdate", DateTimeData.today(this));

      xmlDocument.setData("structureAccount", dataAccount);
      xmlDocument.setData("structureShipper", dataShipper);
      xmlDocument.setData("structureTemplate", dataTemplate);
      xmlDocument.setData("structureDiscount", dataDiscount);
      xmlDocument.setData("structureCustomer", dataCustomer);
      xmlDocument.setData("structureVendor", dataVendor);
      xmlDocument.setData("structureLocation", dataLocation);
      xmlDocument.setData("structureContact", dataContact);
      xmlDocument.setData("structureSalesorder", dataSales);
      xmlDocument.setData("structureSalesinvoice", dataInvoice);
      xmlDocument.setData("structureSalesinout", dataInout);
      xmlDocument.setData("structureABC", dataABC);
      xmlDocument.setData("structure1", dataPartner);
      xmlDocument.setData("structurePaymentsin", dataPaymentsIn);
      xmlDocument.setData("structurePaymentsout", dataPaymentsOut);

      out.println(xmlDocument.print());
      out.close();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void printPageAjaxResponse(HttpServletResponse response, VariablesSecureApp vars,
      String strcBpartnerId, String strmProductTemplate) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajaxreponse");
    XmlDocument xmlDocument = null;

    RptCBpartnerData[] data = RptCBpartnerData.selectTemplateDetail(this, strcBpartnerId,
        strmProductTemplate);

    if (data == null || data.length == 0)
      data = RptCBpartnerData.set();

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerTemplateLines")
        .createXmlDocument();

    response.setContentType("text/plain; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();

    xmlDocument.setData("structure", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageAjaxDocumentResponse(HttpServletResponse response, VariablesSecureApp vars,
      String strcBpartnerId, String strmTypeDocument) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajaxreponse");

    try {
      OBContext.setAdminMode(true);
      XmlDocument xmlDocument = null;

      RptCBpartnerSalesData[] data = null;
      RptCBpartnerSalesData[] dataPeriod = RptCBpartnerSalesData.selectperiod(this);

      HashMap<String, String> orderHM = new HashMap<String, String>();
      HashMap<String, String> invoiceHM = new HashMap<String, String>();

      if (strmTypeDocument.equals("INVOICE")) {
        data = RptCBpartnerSalesData.selectInvoiceperiod(this, strcBpartnerId);
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpReports/RptC_BpartnerPeriodInvoice").createXmlDocument();
        xmlDocument.setData("structurePeriod", dataPeriod);
        if (data == null || data.length == 0)
          data = RptCBpartnerSalesData.set();
      }
      if (strmTypeDocument.equals("ORDER")) {
        data = RptCBpartnerSalesData.selectOrderperiod(this, strcBpartnerId);
        xmlDocument = xmlEngine
            .readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerPeriodSales")
            .createXmlDocument();
        xmlDocument.setData("structurePeriod", dataPeriod);
        if (data == null || data.length == 0)
          data = RptCBpartnerSalesData.set();
      }
      if (strmTypeDocument.equals("INOUT")) {
        data = RptCBpartnerSalesData.selectInoutperiod(this, strcBpartnerId);
        xmlDocument = xmlEngine
            .readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerPeriodInout")
            .createXmlDocument();
        xmlDocument.setData("structurePeriod", dataPeriod);
        if (data == null || data.length == 0)
          data = RptCBpartnerSalesData.set();
      }
      if (strmTypeDocument.equals("ABC")) {
        data = RptCBpartnerSalesData.selectABCactualdetail(this, DateTimeData.sysdateYear(this),
            strcBpartnerId);
        xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerABC")
            .createXmlDocument();
      }
      if (strmTypeDocument.equals("ABCREF")) {
        data = RptCBpartnerSalesData.selectABCrefdetail(this, DateTimeData.lastYear(this),
            strcBpartnerId);
        xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerABCref")
            .createXmlDocument();
      }

      Client c = OBDal.getInstance().get(Client.class,
          OBContext.getOBContext().getCurrentClient().getId());

      if (strmTypeDocument.equals("PAYMENTIN")) {
        data = RptCBpartnerSalesData.selectPaymentsdetail(this, vars.getLanguage(), c.getCurrency()
            .getId(), "Y", strcBpartnerId);
        xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerPaymentsIn")
            .createXmlDocument();
        orderHM = getLinkParameters("70E57DEA195843729FF303C9A71EBCA3", "Y");
        xmlDocument.setParameter("tabIdSO", orderHM.get("tabId"));
        xmlDocument.setParameter("windowIdSO", orderHM.get("windowId"));
        xmlDocument.setParameter("tabTitleSO", orderHM.get("tabTitle"));
        xmlDocument.setParameter("mappingNameSO", orderHM.get("mappingName"));
        xmlDocument.setParameter("keyParameterSO", orderHM.get("keyParameter"));
        invoiceHM = getLinkParameters("77182DC88AA842D499C01FB0BAE39561", "Y");
        xmlDocument.setParameter("tabIdSI", invoiceHM.get("tabId"));
        xmlDocument.setParameter("windowIdSI", invoiceHM.get("windowId"));
        xmlDocument.setParameter("tabTitleSI", invoiceHM.get("tabTitle"));
        xmlDocument.setParameter("mappingNameSI", invoiceHM.get("mappingName"));
        xmlDocument.setParameter("keyParameterSI", invoiceHM.get("keyParameter"));
      }
      if (strmTypeDocument.equals("PAYMENTOUT")) {
        data = RptCBpartnerSalesData.selectPaymentsdetail(this, vars.getLanguage(), c.getCurrency()
            .getId(), "N", strcBpartnerId);
        xmlDocument = xmlEngine
            .readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerPaymentsOut")
            .createXmlDocument();
        orderHM = getLinkParameters("70E57DEA195843729FF303C9A71EBCA3", "N");
        xmlDocument.setParameter("tabIdPO", orderHM.get("tabId"));
        xmlDocument.setParameter("windowIdPO", orderHM.get("windowId"));
        xmlDocument.setParameter("tabTitlePO", orderHM.get("tabTitle"));
        xmlDocument.setParameter("mappingNamePO", orderHM.get("mappingName"));
        xmlDocument.setParameter("keyParameterPO", orderHM.get("keyParameter"));
        invoiceHM = getLinkParameters("77182DC88AA842D499C01FB0BAE39561", "N");
        xmlDocument.setParameter("tabIdPI", invoiceHM.get("tabId"));
        xmlDocument.setParameter("windowIdPI", invoiceHM.get("windowId"));
        xmlDocument.setParameter("tabTitlePI", invoiceHM.get("tabTitle"));
        xmlDocument.setParameter("mappingNamePI", invoiceHM.get("mappingName"));
        xmlDocument.setParameter("keyParameterPI", invoiceHM.get("keyParameter"));
      }

      response.setContentType("text/plain; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      xmlDocument.setData("structure", data);
      out.println(xmlDocument.print());
      out.close();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public BigDecimal getCustomerCredit(BusinessPartner bp, boolean isReceipt) {
    BigDecimal creditAmount = BigDecimal.ZERO;
    for (FIN_Payment payment : getCustomerPaymentsWithCredit(bp, isReceipt))
      creditAmount = creditAmount.add(payment.getGeneratedCredit()).subtract(
          payment.getUsedCredit());
    return creditAmount;
  }

  public List<FIN_Payment> getCustomerPaymentsWithCredit(BusinessPartner bp, boolean isReceipt) {
    OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
    obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_BUSINESSPARTNER, bp));
    obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
    obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_GENERATEDCREDIT, BigDecimal.ZERO));
    obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_STATUS, "RPAP"));
    obcPayment.add(Restrictions.neProperty(FIN_Payment.PROPERTY_GENERATEDCREDIT,
        FIN_Payment.PROPERTY_USEDCREDIT));
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, true);
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_DOCUMENTNO, true);
    return obcPayment.list();
  }

  public static HashMap<String, String> getLinkParameters(String adTableId, String isSOtrx) {
    HashMap<String, String> hmValues = new HashMap<String, String>();

    OBContext.setAdminMode();
    try {
      Table adTable = OBDal.getInstance().get(Table.class, adTableId);

      Window adWindow = null;
      if (isSOtrx.equalsIgnoreCase("Y")) {
        adWindow = adTable.getWindow();
      } else {
        adWindow = adTable.getPOWindow();
      }
      hmValues.put("windowId", adWindow.getId());

      java.util.List<Tab> adTabList = adWindow.getADTabList();
      Tab tab = null;
      for (int i = 0; i < adTabList.size(); i++) {
        if (adTabList.get(i).getTable().getId().equalsIgnoreCase(adTableId)) {
          hmValues.put("tabId", adTabList.get(i).getId());
          tab = adTabList.get(i);
        }
      }

      String mappingName = Utility.getTabURL(tab.getId(), "E", false);
      hmValues.put("mappingName", mappingName);

      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      String tabTitle = null;
      for (WindowTrl windowTrl : tab.getWindow().getADWindowTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(windowTrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          tabTitle = windowTrl.getName();
        }
      }
      if (tabTitle == null) {
        tabTitle = tab.getWindow().getName();
      }

      hmValues.put("tabTitle", tabTitle);

      final Entity entity = ModelProvider.getInstance().getEntity(tab.getTable().getName());
      hmValues.put("keyParameter",
          "inp" + Sqlc.TransformaNombreColumna(entity.getIdProperties().get(0).getColumnName()));

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return hmValues;
  }

  public String getServletInfo() {
    return "Servlet RptC_Bpartner. This Servlet was made by Pablo Sarobe";
  } // End of getServletInfo() method
}
