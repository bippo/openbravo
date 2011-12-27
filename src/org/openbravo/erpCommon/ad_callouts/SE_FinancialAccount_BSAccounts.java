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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.xmlEngine.XmlDocument;

public class SE_FinancialAccount_BSAccounts extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      final String strChanged = vars.getStringParameter("inpLastFieldChanged");
      log4j.debug("CHANGED: " + strChanged);
      final String strfinFinancialAccountId = vars.getStringParameter("inpfinFinancialAccountId",
          IsIDFilter.instance);

      try {
        printPage(response, vars, strChanged, strfinFinancialAccountId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else if (vars.commandIn("EXECUTE")) {
      final String strfinTransitoryAcct = vars.getStringParameter("inpfinTransitoryAcct",
          IsIDFilter.instance);
      final String strfinFinancialAccountId = vars.getStringParameter("inpfinFinancialAccountId",
          IsIDFilter.instance);
      try {
        updatePaymentMethodConfiguration(strfinFinancialAccountId);
        printPageResponse(response, strfinTransitoryAcct);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strFinancialAccountId) throws IOException, ServletException {
    log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    String strOB3UIMode = vars.getStringParameter("inpOB3UIMode", "N");
    String strfinTransitoryAcct = vars.getStringParameter("inpfinTransitoryAcct",
        IsIDFilter.instance);
    StringBuffer resultado = new StringBuffer();

    if ("inpfinTransitoryAcct".equals(strChanged) && !"".equals(strfinTransitoryAcct)) {
      resultado.append("var calloutName='SE_FinancialAccount_BSAccounts';\n\n");
      resultado.append("var respuesta = new Array(");

      String strConfirmMessage = Utility.messageBD(this, "BankStatementAccountWarning",
          vars.getLanguage());
      if ("Y".equals(strOB3UIMode)) {
        resultado.append("new Array(\"JSEXECUTE\","
            + " \"OB.APRM.bankTransitoryAccountCalloutResponse(this, '"
            + strConfirmMessage.replaceAll("\\\\n", "<br>") + "', '" + strFinancialAccountId
            + "')\")");
      } else {
        String strScript = "(function(){var confirmation = confirm(\'"
            + strConfirmMessage.replaceAll("\\\\n", "\\\\\\\\n")
            + "\'); if(confirmation){submitCommandFormParameter(\'EXECUTE\', frmMain.inpLastFieldChanged, \'"
            + strChanged
            + "\', false, null, \'../ad_callouts/SE_FinancialAccount_BSAccounts.html\', \'hiddenFrame\', null, null, true);}})();";
        resultado.append("new Array(\"EXECUTE\", \"" + strScript + "\")");
      }
      resultado.append(");");
    } else {
      resultado.append("var calloutName='SE_FinancialAccount_BSAccounts';\n\n");
      resultado.append("var respuesta = null;");
    }
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageResponse(HttpServletResponse response, String strfinTransitoryAcct)
      throws IOException, ServletException {
    log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    StringBuffer resultado = new StringBuffer();
    AccountingCombination transitoryAccount = OBDal.getInstance().get(AccountingCombination.class,
        strfinTransitoryAcct);
    resultado.append("var calloutName='SE_FinancialAccount_BSAccounts';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpfinInClearAcct\", \"" + strfinTransitoryAcct + "\"),");
    resultado.append("new Array(\"inpfinOutClearAcct\", \"" + strfinTransitoryAcct + "\"),");
    resultado.append("new Array(\"inpfinOutClearAcct_R\", \"" + transitoryAccount.getCombination()
        + "\"),");
    resultado.append("new Array(\"inpfinInClearAcct_R\", \"" + transitoryAccount.getCombination()
        + "\"));");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void updatePaymentMethodConfiguration(String strfinFinancialAccountId) {
    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strfinFinancialAccountId);

    // Configure clearing account for all payment methods upon clearing event
    for (FinAccPaymentMethod paymentMethod : account.getFinancialMgmtFinAccPaymentMethodList()) {
      paymentMethod.setOUTUponClearingUse("CLE");
      paymentMethod.setINUponClearingUse("CLE");
      OBDal.getInstance().save(paymentMethod);
      OBDal.getInstance().flush();
    }
  }
}
