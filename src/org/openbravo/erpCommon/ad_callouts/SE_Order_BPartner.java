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

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;

public class SE_Order_BPartner extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // General data

    String strBPartner = info.vars.getStringParameter("inpcBpartnerId");
    String strIsSOTrx = Utility.getContext(this, info.vars, "isSOTrx", info.getWindowId());
    String strOrgId = info.vars.getStringParameter("inpadOrgId");
    String strPriceList = "";
    String strUserRep = "";
    String strInvoiceRule = "";
    String strFinPaymentMethodId = "";
    String strPaymentrule = "";
    String strDeliveryViaRule = "";
    String strPaymentterm = "";
    String strDeliveryRule = "";
    String strDocTypeTarget = info.vars.getStringParameter("inpcDoctypetargetId");

    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);
    if (data != null && data.length > 0) {
      strDeliveryRule = data[0].deliveryrule.equals("") ? info.vars
          .getStringParameter("inpdeliveryrule") : data[0].deliveryrule;
      strUserRep = SEOrderBPartnerData.userIdSalesRep(this, data[0].salesrepId);
      strUserRep = strUserRep.equals("") ? info.vars.getStringParameter("inpsalesrepId")
          : strUserRep;
      String docSubTypeSO = "";
      SLOrderDocTypeData[] docTypeData = SLOrderDocTypeData.select(this, strDocTypeTarget);
      if (docTypeData != null && docTypeData.length > 0) {
        docSubTypeSO = docTypeData[0].docsubtypeso;
      }
      strInvoiceRule = (docSubTypeSO.equals("PR") || docSubTypeSO.equals("WI")
          || data[0].invoicerule.equals("") ? info.vars.getStringParameter("inpinvoicerule")
          : data[0].invoicerule);
      strPaymentrule = (strIsSOTrx.equals("Y") ? data[0].paymentrule : data[0].paymentrulepo);
      strPaymentrule = strPaymentrule.equals("") ? info.vars.getStringParameter("inppaymentrule")
          : strPaymentrule;

      strPaymentterm = (strIsSOTrx.equals("Y") ? data[0].cPaymenttermId : data[0].poPaymenttermId);
      if (strPaymentterm.equalsIgnoreCase("")) {
        BpartnerMiscData[] paymentTerm = BpartnerMiscData.selectPaymentTerm(this, strOrgId,
            info.vars.getClient());
        if (paymentTerm.length != 0) {
          strPaymentterm = strPaymentterm.equals("") ? paymentTerm[0].cPaymenttermId
              : strPaymentterm;
        }
      }
      strPaymentterm = strPaymentterm.equals("") ? info.vars
          .getStringParameter("inpcPaymenttermId") : strPaymentterm;

      strFinPaymentMethodId = (strIsSOTrx.equals("Y") ? data[0].finPaymentmethodId
          : data[0].poPaymentmethodId);

      strPriceList = (strIsSOTrx.equals("Y") ? data[0].mPricelistId : data[0].poPricelistId);
      if (strPriceList.equalsIgnoreCase("")) {
        strPriceList = SEOrderBPartnerData
            .defaultPriceList(this, strIsSOTrx, info.vars.getClient());
      }
      strPriceList = strPriceList.equals("") ? info.vars.getStringParameter("inpmPricelistId")
          : strPriceList;
      strDeliveryViaRule = data[0].deliveryviarule.equals("") ? info.vars
          .getStringParameter("inpdeliveryviarule") : data[0].deliveryviarule;
    }

    // Price list

    info.addResult(
        "inpmPricelistId",
        strPriceList.equals("") ? Utility.getContext(this, info.vars, "#M_PriceList_ID",
            info.getWindowId()) : strPriceList);

    // BPartner Location

    FieldProvider[] tdv = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLEDIR",
          "C_BPartner_Location_ID", "", "C_BPartner Location - Ship To", Utility.getContext(this,
              info.vars, "#AccessibleOrgTree", info.getWindowId()), Utility.getContext(this,
              info.vars, "#User_Client", info.getWindowId()), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, info.getWindowId(), "");
      tdv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    String strLocation = info.vars.getStringParameter("inpcBpartnerId_LOC");

    if (tdv != null && tdv.length > 0) {
      info.addSelect("inpcBpartnerLocationId");

      for (int i = 0; i < tdv.length; i++) {
        info.addSelectResult(tdv[i].getField("id"), tdv[i].getField("name"), tdv[i].getField("id")
            .equalsIgnoreCase(strLocation));
      }
      info.endSelect();
    } else {
      info.addResult("inpcBpartnerLocationId", null);
    }
    // Warehouses

    FieldProvider[] td = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "18", "M_Warehouse_ID",
          "197", "", Utility.getReferenceableOrg(info.vars,
              info.vars.getStringParameter("inpadOrgId")), Utility.getContext(this, info.vars,
              "#User_Client", info.getWindowId()), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, info.getWindowId(), "");
      td = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (td != null && td.length > 0) {
      info.addSelect("inpmWarehouseId");
      String strMwarehouse = SEOrderBPartnerData.mWarehouse(this, strBPartner);

      if (strMwarehouse.equals("")) {
        strMwarehouse = info.vars.getWarehouse();
      }

      for (int i = 0; i < td.length; i++) {
        info.addSelectResult(td[i].getField("id"), td[i].getField("name"), td[i].getField("id")
            .equalsIgnoreCase(strMwarehouse));
      }
      info.endSelect();
    } else {
      info.addResult("inpmWarehouseId", null);
    }
    // Sales Representative

    FieldProvider[] tld = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLE", "",
          "AD_User SalesRep", "", Utility.getContext(this, info.vars, "#AccessibleOrgTree",
              "SEOrderBPartner"), Utility.getContext(this, info.vars, "#User_Client",
              "SEOrderBPartner"), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SEOrderBPartner", "");
      tld = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (tld != null && tld.length > 0) {
      info.addSelect("inpsalesrepId");
      for (int i = 0; i < tld.length; i++) {
        info.addSelectResult(tld[i].getField("id"), tld[i].getField("name"), tld[i].getField("id")
            .equalsIgnoreCase(strUserRep));
      }

      info.endSelect();

    } else {
      info.addResult("inpsalesrepId", null);
    }

    // Invoice Rule

    FieldProvider[] l = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "LIST", "",
          "C_Order InvoiceRule", "", Utility.getContext(this, info.vars, "#AccessibleOrgTree",
              "SEOrderBPartner"), Utility.getContext(this, info.vars, "#User_Client",
              "SEOrderBPartner"), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SEOrderBPartner", "");
      l = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (l != null && l.length > 0) {
      info.addSelect("inpinvoicerule");
      for (int i = 0; i < l.length; i++) {
        info.addSelectResult(l[i].getField("id"), l[i].getField("name"), l[i].getField("id")
            .equalsIgnoreCase(strInvoiceRule));
      }

      info.endSelect();

    } else {
      info.addResult("inpinvoicerule", null);
    }
    // Project

    info.addResult("inpcProjectId", "");

    // Project R

    info.addResult("inpcProjectId_R", "");

    // Financial Payment

    if (!"".equals(strFinPaymentMethodId)) {
      info.addResult("inpfinPaymentmethodId", strFinPaymentMethodId);
    }

    // Bill to
    FieldProvider[] tlv = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLE", "",
          "C_BPartner Location", "C_BPartner Location - Bill To", Utility.getContext(this,
              info.vars, "#AccessibleOrgTree", info.getWindowId()), Utility.getContext(this,
              info.vars, "#User_Client", info.getWindowId()), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, info.getWindowId(), "");
      tlv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (tlv != null && tlv.length > 0) {
      info.addSelect("inpbilltoId");
      for (int i = 0; i < tlv.length; i++) {
        info.addSelectResult(tlv[i].getField("id"), tlv[i].getField("name"), tlv[i].getField("id")
            .equalsIgnoreCase(strLocation));
      }

      info.endSelect();
    } else {
      info.addResult("inpbilltoId", null);
    }
    // Payment rule

    info.addResult("inppaymentrule", strPaymentrule);

    // Delivery via rule

    info.addResult("inpdeliveryviarule", strDeliveryViaRule);

    // Discount printed

    info.addResult("inpisdiscountprinted",
        SEOrderBPartnerData.getIsDicountPrinted(this, strBPartner));

    // Payment term

    info.addResult("inpcPaymenttermId", strPaymentterm);

    // Delivery rule

    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "LIST", "",
          "C_Order DeliveryRule", "", Utility.getContext(this, info.vars, "#AccessibleOrgTree",
              "SEOrderBPartner"), Utility.getContext(this, info.vars, "#User_Client",
              "SEOrderBPartner"), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SEOrderBPartner", "");
      l = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (strIsSOTrx.equals("N")) {
      info.addResult("inpdeliveryrule",
          strDeliveryRule.equals("") ? ((l != null && l.length > 0) ? l[0].getField("id") : "null")
              : strDeliveryRule);
    } else {

      if (l != null && l.length > 0) {
        info.addSelect("inpdeliveryrule");

        for (int i = 0; i < l.length; i++) {
          info.addSelectResult(l[i].getField("id"), l[i].getField("name"), l[i].getField("id")
              .equalsIgnoreCase(strDeliveryRule));
        }

        info.endSelect();
      } else {
        info.addResult("inpdeliveryrule", null);
      }
    }

    // Ad User

    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLEDIR", "AD_User_ID",
          "", "AD_User C_BPartner User/Contacts", Utility.getContext(this, info.vars,
              "#AccessibleOrgTree", info.getWindowId()), Utility.getContext(this, info.vars,
              "#User_Client", info.getWindowId()), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, info.getWindowId(), "");
      tdv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (tdv != null && tdv.length > 0) {
      info.addSelect("inpadUserId");

      for (int i = 0; i < tdv.length; i++) {
        info.addSelectResult(tdv[i].getField("id"), tdv[i].getField("name"), tdv[i].getField("id")
            .equalsIgnoreCase(info.vars.getStringParameter("inpcBpartnerId_CON")));
      }

      info.endSelect();

    } else {
      info.addResult("inpadUserId", null);
    }

    // Message

    StringBuilder message = new StringBuilder();

    if (strLocation.equals("")) {
      message.append(Utility.messageBD(this, "NoBPLocation", info.vars.getLanguage()));
    }

    if (data != null && data.length > 0
        && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) < 0
        && strIsSOTrx.equals("Y")) {
      if (message.length() > 0) {
        message.append("<br>");
      }
      String creditLimitExceed = "" + Double.parseDouble(data[0].creditavailable) * -1;
      message.append(Utility.messageBD(this, "CreditLimitOver", info.vars.getLanguage())
          + creditLimitExceed);
    }

    info.addResult("MESSAGE", message.toString());
  }
}
