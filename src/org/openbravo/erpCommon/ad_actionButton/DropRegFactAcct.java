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
import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class DropRegFactAcct extends HttpSecureAppServlet {
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
      String stradOrgId = vars.getStringParameter("inpadOrgId", "");
      String strKey = vars.getRequiredGlobalVariable("inpcYearId", strWindow + "|C_Year_ID");
      printPage(response, vars, strKey, stradOrgId, strWindow, strTab, strProcessId);
    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String stradOrgId = vars.getStringParameter("inpadOrgId", "");
      String strKey = vars.getRequiredGlobalVariable("inpcYearId", strWindow + "|C_Year_ID");
      String strTab = vars.getStringParameter("inpTabId");

      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myError = processButton(vars, stradOrgId, strKey);
      vars.setMessage(strTab, myError);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String stradOrgId, String strKey) {
    Connection conn = null;
    OBError myError = null;
    try {
      conn = this.getTransactionConnection();
      String strRegFactAcctGroupId = "";
      String strCloseFactAcctGroupId = "";
      String strDivideUpFactAcctGroupId = "";
      String strOpenUpFactAcctGroupId = "";
      String strOrgClosingId = "";
      try {
        DropRegFactAcctData[] data = DropRegFactAcctData.selectFactAcctGroupId(this, stradOrgId,
            strKey);
        if (data != null && data.length != 0) {
          for (int i = 0; i < data.length; i++) {
            strRegFactAcctGroupId = data[i].regFactAcctGroupId;
            strCloseFactAcctGroupId = data[i].closeFactAcctGroupId;
            strDivideUpFactAcctGroupId = data[i].divideupFactAcctGroupId;
            strOpenUpFactAcctGroupId = data[i].openFactAcctGroupId;
            strOrgClosingId = data[i].adOrgClosingId;
            String strResult = processButtonClose(conn, vars, strKey, stradOrgId,
                strRegFactAcctGroupId, strCloseFactAcctGroupId, strDivideUpFactAcctGroupId,
                strOpenUpFactAcctGroupId, strOrgClosingId);
            if (!"ProcessOK".equals(strResult)) {
              myError = new OBError();
              myError.setType("Error");
              myError.setTitle("");
              myError.setMessage(Utility.messageBD(this, "Error", vars.getLanguage()));
              releaseRollbackConnection(conn);
              return myError;
            }
          }
          DropRegFactAcctData.updatePeriodsOpen(conn, this, vars.getUser(), strKey, stradOrgId);
        }
      } catch (ServletException ex) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        releaseRollbackConnection(conn);
        return myError;
      }

      releaseCommitConnection(conn);
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle("");
      myError.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (Exception e) {
      log4j.warn(e);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      myError = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myError;
  }

  private String processButtonClose(Connection conn, VariablesSecureApp vars, String strKey,
      String stradOrgId, String strRegFactAcctGroupId, String strCloseFactAcctGroupId,
      String strDivideUpFactAcctGroupId, String strOpenUpFactAcctGroupId, String strOrgClosingId)
      throws ServletException {
    DropRegFactAcctData.deleteOrgClosing(conn, this, strOrgClosingId);
    DropRegFactAcctData.deleteFactAcctClose(conn, this, strRegFactAcctGroupId,
        strCloseFactAcctGroupId, strDivideUpFactAcctGroupId, strOpenUpFactAcctGroupId, stradOrgId);
    return "ProcessOK";
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String stradOrgId, String windowId, String strTab, String strProcessId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Create Close Fact Acct");

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
        "org/openbravo/erpCommon/ad_actionButton/DropRegFactAcct", discard).createXmlDocument();
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

    xmlDocument.setData("reportadOrgId", "liststructure",
        DropRegFactAcctData.select(this, vars.getLanguage(), strKey));

    xmlDocument.setParameter("adOrgId", stradOrgId);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Drop reg fact acct";
  } // end of getServletInfo() method
}
