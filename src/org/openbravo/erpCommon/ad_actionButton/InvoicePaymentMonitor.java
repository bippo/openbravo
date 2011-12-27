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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_process.PaymentMonitor;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.xmlEngine.XmlDocument;

public class InvoicePaymentMonitor extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    log4j.debug("InvoicePaymentMonitor: doPost");

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strKey = vars.getGlobalVariable("inpcInvoiceId", strWindow + "|C_Invoice_ID");
      String strTabId = vars.getGlobalVariable("inpTabId", "InvoicePaymentMonitor|tabId");
      String strProcessId = vars.getGlobalVariable("inpProcessId",
          "InvoicePaymentMonitor|processId", "");
      String strMappingName = vars.getGlobalVariable("mappingName",
          "InvoicePaymentMonitor|mappingName", "");
      String strPath = strDireccion + strMappingName;
      vars.setSessionValue("InvoicePaymentMonitor|path", strPath);
      String strWindowId = vars.getGlobalVariable("inpwindowId", "InvoicePaymentMonitor|windowId",
          "");
      String strTabName = vars.getGlobalVariable("inpTabName", "InvoicePaymentMonitor|tabName", "");

      printPage(response, vars, strKey, strWindowId, strTabId, strProcessId, strPath, strTabName);
    } else if (vars.commandIn("SAVE")) {

      String strKey = vars.getStringParameter("inpKey");
      String strTabId = vars.getRequestGlobalVariable("inpTabId", "InvoicePaymentMonitor|tabId");
      vars.getRequestGlobalVariable("inpProcessId", "InvoicePaymentMonitor|processId");
      String strPath = vars.getRequestGlobalVariable("inpPath", "InvoicePaymentMonitor|path");
      vars.getRequestGlobalVariable("inpWindowId", "InvoicePaymentMonitor|windowId");
      vars.getRequestGlobalVariable("inpTabName", "InvoicePaymentMonitor|tabName");
      OBError messageResult = process(vars, strKey);
      vars.setMessage(strTabId, messageResult);
      printPageClosePopUp(response, vars, strPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError process(VariablesSecureApp vars, String strKey) throws ServletException {
    OBError myError = null;
    try {
      Invoice invoice = OBDal.getInstance().get(Invoice.class, strKey);
      // Extra check for PaymentMonitor-disabling switch, to build correct message for users
      // Use Utility.getPropertyValue for backward compatibility
      try {
        Preferences.getPreferenceValue("PaymentMonitor", true, invoice.getClient(), invoice
            .getOrganization(), OBContext.getOBContext().getUser(), OBContext.getOBContext()
            .getRole(), null);
      } catch (PropertyNotFoundException e) {
        if (invoice.isProcessed())
          PaymentMonitor.updateInvoice(invoice);
      }

      myError = new OBError();
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (PropertyException e) {
      myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
    } catch (Exception e) {
      log4j.error("Rollback in transaction", e);
      myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
    }
    return myError;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String strTab, String strProcessId, String strPath, String strTabName)
      throws IOException, ServletException {
    log4j.debug("Output: Button process InvoicePaymentMonitor");

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
    String[] discard = { "", "" };
    if (strHelp.equals(""))
      discard[0] = "helpDiscard";
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/InvoicePaymentMonitor", discard)
        .createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("process", strProcessId);
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("tabname", strTabName);

    {
      OBError myMessage = vars.getMessage("InvoicePaymentMonitor");
      vars.removeMessage("InvoicePaymentMonitor");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
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
    return "Servlet InvoicePaymentMonitor";
  }
}
