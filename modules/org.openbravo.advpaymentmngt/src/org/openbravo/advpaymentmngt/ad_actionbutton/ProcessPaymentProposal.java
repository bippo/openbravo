/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.xmlEngine.XmlDocument;

public class ProcessPaymentProposal extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strTabId = vars.getGlobalVariable("inpTabId", "");
      String strFinPaymentProposalId = vars.getGlobalVariable("inpfinPaymentProposalId", "");
      printPage(response, vars, strTabId, strFinPaymentProposalId);
    } else if (vars.commandIn("SAVE")) {

      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strProcessProposalAction = vars.getRequiredStringParameter("inpProcessProposalAction");
      String strFinPaymentProposalId = vars.getRequiredStringParameter("inpfinPaymentProposalId");
      OBError message = null;
      OBContext.setAdminMode();
      try {

        if (strProcessProposalAction.equals("GSP")) {
          try {
            List<FIN_PaymentPropDetail> ppd = new AdvPaymentMngtDao().getObject(
                FIN_PaymentProposal.class, strFinPaymentProposalId).getFINPaymentPropDetailList();
            if (ppd.isEmpty() || ppd == null) {
              throw new OBException(Utility.messageBD(this,
                  "It is not possible to process a Payment Proposal without line.",
                  vars.getLanguage()));
            } else {
              message = FIN_AddPayment.processPaymentProposal(vars, this, strProcessProposalAction,
                  strFinPaymentProposalId);
            }
          } catch (Exception ex) {
            message = Utility.translateError(this, vars, vars.getLanguage(),
                FIN_Utility.getExceptionMessage(ex));
            log4j.error(ex);
            if (!message.isConnectionAvailable()) {
              bdErrorConnection(response);
              return;
            }
          }

        } else if (strProcessProposalAction.equals("RE")) {
          try {
            message = FIN_AddPayment.processPaymentProposal(vars, this, strProcessProposalAction,
                strFinPaymentProposalId);
          } catch (Exception ex) {
            message = Utility.translateError(this, vars, vars.getLanguage(),
                FIN_Utility.getExceptionMessage(ex));
            log4j.error(ex);
            if (!message.isConnectionAvailable()) {
              bdErrorConnection(response);
              return;
            }
          }
        }
      } finally {
        OBContext.restorePreviousMode();
      }

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      vars.setMessage(strTabId, message);
      printPageClosePopUp(response, vars, strWindowPath);

    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTabId,
      String strFinPaymentProposalId) throws IOException, ServletException {

    log4j.debug("Output: Add Payment button pressed on Sales Invoice window");

    Map<String, String> filterActions = new HashMap<String, String>();
    FIN_PaymentProposal fpp = OBDal.getInstance().get(FIN_PaymentProposal.class,
        strFinPaymentProposalId);

    if (fpp == null) {
      String strMessage = "@APRM_PaymentNoLines@";
      OBError msg = new OBError();
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
      vars.setMessage(strTabId, msg);
      printPageClosePopUpAndRefreshParent(response, vars);
    }

    String discard[] = { "discard" };
    if (fpp.getAPRMProcessProposal().equals("RE")) {
      discard[0] = "displayCheckBox";
      filterActions.put(fpp.getAPRMProcessProposal(), fpp.getAPRMProcessProposal());
    } else {
      filterActions.put("GSP", "GSP");
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/ProcessPaymentProposal", discard)
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("finPaymentProposalId", strFinPaymentProposalId);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST",
          "PROCESS_PROPOSAL_ACTION", "79FDE7805FC84C2BB251EE57E96C0AEE",
          "Process Proposal Window Reference Validation", Utility.getContext(this, vars,
              "#AccessibleOrgTree", "ProcessProposalWindow"), Utility.getContext(this, vars,
              "#User_Client", "ProcessProposalWindow"), 0);

      FieldProvider[] filterApplied = filterComboTableData(comboTableData, filterActions, false);
      xmlDocument.setData("reportPROCESS_PROPOSAL_ACTION", "liststructure", filterApplied);
      // comboTableData = null;

    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    // Action Regarding Document
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * 
   * @param ctd
   *          ComboTableData
   * @param filter
   *          Map with the identifiers to be filtered
   * @param includeActual
   *          Boolean that indicates if the actual selected value must be included in the result,
   *          even if it does not exists in the new query.
   * @return FieldProvider[] structure with the filter applied
   * @throws Exception
   */
  private FieldProvider[] filterComboTableData(ComboTableData ctd, Map<String, String> filter,
      boolean includeActual) throws Exception {
    FieldProvider[] fp = ctd.select(includeActual);
    Vector<FieldProvider> aux = new Vector<FieldProvider>();
    if (fp != null && fp.length > 0) {
      for (int i = 0; i < fp.length; i++) {
        if (filter.get(fp[i].getField("ID")) != null) {
          aux.addElement(fp[i]);
        }
      }
      if (aux.size() == 0)
        return fp;
    }
    FieldProvider[] clone = new FieldProvider[aux.size()];
    aux.copyInto(clone);
    return clone;
  }
}
