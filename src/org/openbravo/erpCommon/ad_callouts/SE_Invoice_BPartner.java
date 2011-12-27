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
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SE_Invoice_BPartner extends HttpSecureAppServlet {
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
      String strDocType = vars.getStringParameter("inpcDoctypetargetId");
      String strLocation = vars.getStringParameter("inpcBpartnerId_LOC");
      String strContact = vars.getStringParameter("inpcBpartnerId_CON");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strProjectId = vars.getStringParameter("inpcProjectId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");
      String strfinPaymentmethodId = vars.getStringParameter("inpfinPaymentmethodId");
      String strOrgId = vars.getStringParameter("inpadOrgId");

      try {
        if ("inpfinPaymentmethodId".equals(strChanged)) { // Payment Method changed
          printPagePaymentMethod(response, vars, strBPartner, strIsSOTrx, strfinPaymentmethodId,
              strOrgId);
        } else {
          printPage(response, vars, strBPartner, strDocType, strIsSOTrx, strWindowId, strLocation,
              strContact, strProjectId, strTabId, strOrgId);
        }
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner,
      String strDocType, String strIsSOTrx, String strWindowId, String strLocation,
      String strContact, String strProjectId, String strTabId, String strOrgId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    if (strBPartner.equals(""))
      vars.removeSessionValue(strWindowId + "|C_BPartner_ID");

    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);
    String strUserRep;
    String DocBaseType = SEInvoiceBPartnerData.docBaseType(this, strDocType);
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_Invoice_BPartner';\n\n");
    if (data == null || data.length == 0)
      resultado.append("var respuesta = new Array(new Array(\"inpcBpartnerLocationId\", null));");

    else {
      resultado.append("var respuesta = new Array(");
      strUserRep = SEOrderBPartnerData.userIdSalesRep(this, data[0].salesrepId);
      String strPriceList = (strIsSOTrx.equals("Y") ? data[0].mPricelistId : data[0].poPricelistId);
      if (strPriceList.equalsIgnoreCase("")) {
        strPriceList = SEOrderBPartnerData.defaultPriceList(this, strIsSOTrx, vars.getClient());
      }
      resultado.append("new Array(\"inpmPricelistId\", \""
          + (strPriceList.equals("") ? Utility.getContext(this, vars, "#M_PriceList_ID",
              strWindowId) : strPriceList) + "\"),");

      String strPaymentRule = (strIsSOTrx.equals("Y") ? data[0].paymentrule : data[0].paymentrulepo);
      if (strPaymentRule.equals("") && DocBaseType.endsWith("C"))
        strPaymentRule = "P";
      else if (strPaymentRule.equals("S") || strPaymentRule.equals("U") && strIsSOTrx.equals("Y"))
        strPaymentRule = "P";
      resultado.append("new Array(\"inppaymentrule\", \"" + strPaymentRule + "\"),");
      String strFinPaymentMethodId = (strIsSOTrx.equals("Y") ? data[0].finPaymentmethodId
          : data[0].poPaymentmethodId);
      resultado.append("new Array(\"inpfinPaymentmethodId\", \"" + strFinPaymentMethodId + "\"),");
      String PaymentTerm = (strIsSOTrx.equals("Y") ? data[0].cPaymenttermId
          : data[0].poPaymenttermId);
      if (PaymentTerm.equalsIgnoreCase("")) {
        BpartnerMiscData[] paymentTerm = BpartnerMiscData.selectPaymentTerm(this, strOrgId,
            vars.getClient());
        if (paymentTerm.length != 0) {
          PaymentTerm = PaymentTerm.equals("") ? paymentTerm[0].cPaymenttermId : PaymentTerm;
        }
      }
      resultado.append("new Array(\"inpcPaymenttermId\", \"" + PaymentTerm + "\"),");
      FieldProvider[] tdv = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "C_BPartner_Location_ID", "", "C_BPartner Location - Bill To", Utility.getContext(this,
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
      resultado.append("new Array(\"inpsalesrepId\", ");
      FieldProvider[] tld = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "AD_User SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "SEInvoiceBPartner"), Utility.getContext(this, vars, "#User_Client",
                "SEInvoiceBPartner"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "SEInvoiceBPartner", "");
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
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_User_ID",
            "", "AD_User C_BPartner User/Contacts", Utility.getContext(this, vars,
                "#AccessibleOrgTree", strWindowId), Utility.getContext(this, vars, "#User_Client",
                strWindowId), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
        tdv = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      resultado.append("new Array(\"inpcBpartnerContactId\", ");
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

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_User_ID",
            "", "AD_User C_BPartner User/Contacts", Utility.getContext(this, vars,
                "#AccessibleOrgTree", strWindowId), Utility.getContext(this, vars, "#User_Client",
                strWindowId), 0);
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
      String strWithHolding = SEInvoiceBPartnerData.WithHolding(this, strBPartner);
      resultado.append("new Array(\"inpcWithholdingId\", \"" + strWithHolding + "\"),");
      resultado
          .append("new Array(\"inpisdiscountprinted\", \"" + data[0].isdiscountprinted + "\")");
      if (data != null && data.length > 0
          && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) < 0
          && strIsSOTrx.equals("Y")) {
        String creditLimitExceed = "" + Double.parseDouble(data[0].creditavailable) * -1;
        String automationPaymentMethod = isAutomaticCombination(vars, strBPartner, strIsSOTrx,
            strFinPaymentMethodId, strOrgId);
        resultado.append(", new Array('MESSAGE', \""
            + Utility.messageBD(this, "CreditLimitOver", vars.getLanguage()) + creditLimitExceed
            + "<br/>" + automationPaymentMethod + "\")");
      } else if (strIsSOTrx.equals("Y")) {
        resultado.append(", new Array('MESSAGE', \"\")");
      }
      resultado.append(");");
    }
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePaymentMethod(HttpServletResponse response, VariablesSecureApp vars,
      String strBPartnerId, String strIsSOTrx, String strfinPaymentmethodId, String strOrgId)
      throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    StringBuilder result = new StringBuilder();

    String message = isAutomaticCombination(vars, strBPartnerId, strIsSOTrx, strfinPaymentmethodId,
        strOrgId);

    result.append("var calloutName='SE_Invoice_BPartner';\n\n");
    result.append("var respuesta = new Array(new Array(\"MESSAGE\", ");
    result.append("\"" + message + "\"));");

    xmlDocument.setParameter("array", result.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Verifies if the given payment method belongs to the default financial account of the given
   * business partner.
   * 
   * @param vars
   *          VariablesSecureApp.
   * @param strBPartnerId
   *          Business Partner id.
   * @param strIsSOTrx
   *          Sales ('Y') or purchase ('N') transaction.
   * @param strfinPaymentmethodId
   *          Payment Method id.
   * @return Message to be displayed in the application warning the user that automatic actions
   *         could not be performed because given payment method does not belong to the default
   *         financial account of the given business partner.
   */
  private String isAutomaticCombination(VariablesSecureApp vars, String strBPartnerId,
      String strIsSOTrx, String strfinPaymentmethodId, String strOrgId) {
    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strBPartnerId);
    FIN_PaymentMethod selectedPaymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        strfinPaymentmethodId);
    OBContext.setAdminMode(true);
    try {
      boolean isSales = "Y".equals(strIsSOTrx);
      FIN_FinancialAccount account = null;
      String message = "";

      if (bpartner != null && selectedPaymentMethod != null && !"".equals(strOrgId)) {
        account = (isSales) ? bpartner.getAccount() : bpartner.getPOFinancialAccount();
        if (account != null) {
          OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance().createCriteria(
              FinAccPaymentMethod.class);
          obc.setFilterOnReadableOrganization(false);
          obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, account));
          obc.add(Restrictions
              .eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, selectedPaymentMethod));
          obc.add(Restrictions.in(FinAccPaymentMethod.PROPERTY_ORGANIZATION + ".id", OBContext
              .getOBContext().getOrganizationStructureProvider().getNaturalTree(strOrgId)));

          if (obc.list() == null || obc.list().size() == 0) {
            message = Utility.messageBD(this, "PaymentmethodNotbelongsFinAccount",
                vars.getLanguage());
          }
        } else {
          message = Utility
              .messageBD(this, "PaymentmethodNotbelongsFinAccount", vars.getLanguage());
        }
      }
      return message;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
