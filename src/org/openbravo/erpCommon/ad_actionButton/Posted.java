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
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.reference.ActionButtonData;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.xmlEngine.XmlDocument;

public class Posted extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Posted: doPost");

    final String generalLedgerJournalReport_ID = "800000";
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strKey = vars.getGlobalVariable("inpKey", "Posted|key");
      String strTableId = vars.getGlobalVariable("inpTableId", "Posted|tableId");
      String strTabId = vars.getGlobalVariable("inpTabId", "Posted|tabId");
      String strPosted = vars.getGlobalVariable("inpPosted", "Posted|posted");
      String strProcessId = vars.getGlobalVariable("inpProcessId", "Posted|processId", "");
      String strPath = vars.getGlobalVariable("inpPath", "Posted|path",
          strDireccion + request.getServletPath());
      String strWindowId = vars.getGlobalVariable("inpWindowId", "Posted|windowId", "");
      String strForcedTableId = vars.getGlobalVariable("inpforcedTableId", strWindowId
          + "|FORCED_TABLE_ID", "");
      String strTabName = vars.getGlobalVariable("inpTabName", "Posted|tabName", "");

      printPage(response, vars, strKey, strWindowId, strTabId, strProcessId, strTableId,
          strForcedTableId, strPath, strTabName, strPosted);
    } else if (vars.commandIn("SAVE")) {
      String strKey = vars.getRequiredGlobalVariable("inpKey", "Posted|key");
      String strTableId = vars.getRequiredGlobalVariable("inpTableId", "Posted|tableId");
      String strTabId = vars.getRequestGlobalVariable("inpTabId", "Posted|tabId");
      String strPosted = vars.getRequiredGlobalVariable("inpPosted", "Posted|posted");
      vars.getRequestGlobalVariable("inpProcessId", "Posted|processId");
      vars.getRequestGlobalVariable("inpPath", "Posted|path");
      vars.getRequestGlobalVariable("inpWindowId", "Posted|windowId");
      vars.getRequestGlobalVariable("inpTabName", "Posted|tabName");
      String strEliminar = vars.getStringParameter("inpEliminar", "N");

      if (log4j.isDebugEnabled())
        log4j.debug("SAVE, strPosted: " + strPosted + " Elim " + strEliminar);

      if (!"Y".equals(strPosted)) {
        OBError messageResult = processButton(vars, strKey, strTableId);
        if (!"Success".equals(messageResult.getType())) {
          vars.setMessage(strTabId, messageResult);
          printPageClosePopUp(response, vars);
        } else {
          PostedData[] data = PostedData.select(this, strKey, strTableId);
          if (data == null || data.length == 0 || data[0].id.equals("")) {
            // vars.setSessionValue(strWindowId + "|" + strTabName +
            // ".message", messageResult);
            vars.setMessage(strTabId, messageResult);
            printPageClosePopUp(response, vars);
          } else {
            String title;
            OBContext.setAdminMode();
            Process genLedJour = OBDal.getInstance().get(Process.class,
                generalLedgerJournalReport_ID);
            if (genLedJour != null) {
              title = genLedJour.getIdentifier();
            } else {
              title = "POST";
            }
            printPageClosePopUp(response, vars, strDireccion
                + "/ad_reports/ReportGeneralLedgerJournal.html?Command=DIRECT&inpTable="
                + strTableId + "&inpRecord=" + strKey + "&inpOrg=" + data[0].org, title);
          }
        }
      } else {
        if (strEliminar.equals("N")) {
          PostedData[] data = PostedData.select(this, strKey, strTableId);
          if (data == null || data.length == 0 || data[0].id.equals("")) {
            // if (log4j.isDebugEnabled())
            // log4j.debug("***********************" + strWindowId +
            // "|" + strTabName + ".message");
            // vars.setSessionValue(strWindowId + "|" + strTabName +
            // ".message", Utility.messageBD(this, "NoFactAcct",
            // vars.getLanguage()));
            vars.setMessage(strTabId,
                Utility.translateError(this, vars, vars.getLanguage(), "NoFactAcct"));
            printPageClosePopUp(response, vars);
          } else {
            String title;
            OBContext.setAdminMode();
            Process genLedJour = OBDal.getInstance().get(Process.class,
                generalLedgerJournalReport_ID);
            if (genLedJour != null) {
              title = genLedJour.getIdentifier();
            } else {
              title = "POST";
            }
            printPageClosePopUp(response, vars, strDireccion
                + "/ad_reports/ReportGeneralLedgerJournal.html?Command=DIRECT&inpTable="
                + strTableId + "&inpRecord=" + strKey + "&inpOrg=" + data[0].org, title);
          }
        } else {
          if (log4j.isDebugEnabled())
            log4j.debug("SAVE, delete");
          OBError myMessage = processButtonDelete(vars, strKey, strTableId);
          vars.setMessage(strTabId, myMessage);
          printPageClosePopUp(response, vars);
        }
      }
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strKey, String strTableId)
      throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("ProcessButton strKey: " + strKey + "strTableId: " + strTableId);
    String strOrg;
    Connection con = null;
    OBError myMessage = null;

    strOrg = PostedData.selectDocOrg(this, PostedData.selectTableName(this, strTableId), strKey);
    if (strOrg == null)
      strOrg = "0";

    try {
      con = getTransactionConnection();
      AcctServer acct = AcctServer.get(strTableId, vars.getClient(), strOrg, this.myPool);
      if (acct == null) {
        releaseRollbackConnection(con);
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
        return myMessage;
      } else if (!acct.post(strKey, false, vars, this, con) || acct.errors != 0) {
        releaseRollbackConnection(con);
        myMessage = acct.getMessageResult();
        return myMessage;
      }
      releaseCommitConnection(con);
    } catch (Exception e) {
      log4j.error(e);
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      try {
        releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
    }

    if (myMessage == null) {
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    }
    return myMessage;
  }

  private OBError processButtonDelete(VariablesSecureApp vars, String strKey, String strTableId)
      throws ServletException {
    OBError myMessage = null;

    try {

      String strClient = PostedData.selectClient(this,
          PostedData.selectTableName(this, strTableId), strKey);
      String pinstance = SequenceIdData.getUUID();
      PInstanceProcessData.insertPInstance(this, pinstance, "176", strKey, "N", vars.getUser(),
          vars.getClient(), vars.getOrg());
      PInstanceProcessData.insertPInstanceParam(this, pinstance, "10", "AD_Client_ID", strClient,
          vars.getClient(), vars.getOrg(), vars.getUser());
      PInstanceProcessData.insertPInstanceParam(this, pinstance, "20", "AD_Table_ID", strTableId,
          vars.getClient(), vars.getOrg(), vars.getUser());
      PInstanceProcessData.insertPInstanceParam(this, pinstance, "30", "DeletePosting", "Y",
          vars.getClient(), vars.getOrg(), vars.getUser());
      if (log4j.isDebugEnabled())
        log4j.debug("delete, pinstance " + pinstance);
      ActionButtonData.process176(this, pinstance);

      PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
      myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    if (myMessage == null) {
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    }
    return myMessage;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String strTab, String strProcessId, String strTableId,
      String strForcedTableId, String strPath, String strTabName, String strPosted)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Posted");

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
      discard[0] = new String("helpDiscard");
    if (!"Y".equals(strPosted))
      discard[1] = new String("selEliminar");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/Posted", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("process", strProcessId);
    if ("".equals(strForcedTableId) || strForcedTableId == null)
      xmlDocument.setParameter("table", strTableId);
    else
      xmlDocument.setParameter("table", strForcedTableId);
    xmlDocument.setParameter("posted", strPosted);
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("tabname", strTabName);

    {
      OBError myMessage = vars.getMessage("Posted");
      vars.removeMessage("Posted");
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
    return "Servlet Posted";
  } // end of getServletInfo() method
}
