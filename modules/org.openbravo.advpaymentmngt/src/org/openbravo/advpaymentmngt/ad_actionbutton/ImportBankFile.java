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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.utility.FIN_BankStatementImport;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.BankFileFormat;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.xmlEngine.XmlDocument;

public class ImportBankFile extends HttpSecureAppServlet {
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
      String strKey = vars.getStringParameter("inpfinFinancialAccountId");

      String strTabId = vars.getStringParameter("inpTabId");
      String strMessage = "";
      printPage(response, vars, strKey, strWindow, strTabId, strProcessId, strMessage, true);
    } else if (vars.commandIn("IMPORT")) {
      String strKey = vars.getStringParameter("inpfinFinancialAccountId");
      String strBankFileFormat = vars.getStringParameter("inpfinBankFileFormatId");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strWindowPath = Utility.getTabURL(strTabId, "E", true);
      BankFileFormat bff = OBDal.getInstance().get(BankFileFormat.class, strBankFileFormat);
      FIN_BankStatementImport bsi = null;
      try {
        bsi = (FIN_BankStatementImport) Class.forName(bff.getJavaClassName()).newInstance();
      } catch (Exception e) {
        log4j.error("Error while creating new instance for FIN_BankStatementImport - " + e, e);
      }
      OBError message = null;
      if (bsi != null) {
        bsi.init(OBDal.getInstance().get(FIN_FinancialAccount.class, strKey));
        message = bsi.importFile(this, vars);
      } else {
        message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        message.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
            "@APRM_WrongBankFileFormat@") + ": " + bff.getJavaClassName());
      }

      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      vars.setMessage(strTabId, message);
      printPageClosePopUp(response, vars, strWindowPath);

    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String tabId, String strProcessId, String strMessage, boolean isDefault)
      throws IOException, ServletException {
    log4j.debug("Output: Button import bank file msg:" + strMessage);

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
        "org/openbravo/advpaymentmngt/ad_actionbutton/ImportBankFile", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("tabId", tabId);
    // Bank File Formats
    boolean isAnyFileFormatInstalled = false;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "FIN_BANKFILE_FORMAT_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ImportBankFile"), Utility.getContext(this, vars, "#User_Client", "ImportBankFile"),
          0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ImportBankFile", "");
      FieldProvider[] fileFormatCombo = comboTableData.select(false);
      isAnyFileFormatInstalled = fileFormatCombo.length > 0;
      xmlDocument.setData("reportfinBankFileFormatId", "liststructure", fileFormatCombo);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("finBankFileFormatId", "");

    if (isDefault) {
      if (!isAnyFileFormatInstalled) {
        String strWindowPath = Utility.getTabURL(tabId, "R", true);
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        OBError message = new OBError();
        message.setType("Warning");
        message.setTitle(Utility.messageBD(this, "APRM_NoBankFileAvailable", vars.getLanguage()));
        message.setMessage(Utility.messageBD(this, "APRM_NoBankFileAvailableInfo",
            vars.getLanguage()));
        vars.setMessage(tabId, message);
        printPageClosePopUp(response, vars, strWindowPath);
        return;
      } else {
        xmlDocument.setParameter("messageType", "");
        xmlDocument.setParameter("messageTitle", "");
        xmlDocument.setParameter("messageMessage", "");
      }
    } else {
      OBError myMessage = new OBError();
      myMessage.setTitle("");
      log4j.debug("ImportBankFile - before setMessage");
      if (strMessage == null || strMessage.equals(""))
        myMessage.setType("Success");
      else
        myMessage.setType("Error");
      if (strMessage != null && !strMessage.equals("")) {
        myMessage.setMessage(strMessage);
      } else
        Utility.translateError(this, vars, vars.getLanguage(), "Success");
      log4j.debug("ImportBankFile - Message Type: " + myMessage.getType());
      vars.setMessage("ImportBankFile", myMessage);
      log4j.debug("ImportBankFile - after setMessage");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet for the importation of files from banks";
  } // end of getServletInfo() method
}
