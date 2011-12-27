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
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class CopyFromSettlement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      vars.getGlobalVariable("inpProcessId", "CopyFromSettlement|AD_Process_ID");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "CopyFromSettlement|Window_ID");
      vars.getGlobalVariable("inpTabId", "CopyFromSettlement|Tab_ID");
      String strSettlement = vars.getGlobalVariable("inpcSettlementId", strWindowId + "|"
          + "C_Settlement_ID");
      log4j.warn("***************  strSettlement - " + strSettlement);
      printPage(response, vars);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strDocumentNo = vars.getStringParameter("inpDocumentNo");
      String strDescription = vars.getStringParameter("inpDescription");
      String strWindow = vars.getGlobalVariable("inpwindowId", "CopyFromSettlement|Window_ID");
      String strSettlement = vars.getGlobalVariable("inpcSettlementId", strWindow + "|"
          + "C_Settlement_ID");
      printPage(response, vars, strDescription, strDocumentNo, strDateFrom, strDateTo,
          strSettlement, strWindow);
    } else if (vars.commandIn("FIND2")) {
      String strSettlement = vars.getGlobalVariable("inpcSettlementId",
          "CopyFromSettlement|C_Settlement_ID");
      String strSettlementFrom = vars.getStringParameter("inpcSettlementFromFrame4");
      String strWindow = vars.getStringParameter("inpwindowId");
      printPage(response, vars, strSettlement, strSettlementFrom, strWindow);
    } else if (vars.commandIn("SAVE")) {
      vars.getGlobalVariable("inpProcessId", "CopyFromSettlement|AD_Process_ID");
      vars.getGlobalVariable("inpwindowId", "CopyFromSettlement|Window_ID");
      String strTab = vars.getGlobalVariable("inpTabId", "CopyFromSettlement|Tab_ID");
      String strSettlement = vars.getRequestGlobalVariable("inpcSettlementId",
          "CopyFromSettlement|C_Settlement_ID");
      String strKey = vars.getRequestGlobalVariable("inpcSettlementFromId",
          "CopyFromSettlement|C_SettlementFrom_ID");

      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myError = processButton(vars, strSettlement, strKey);
      if (log4j.isDebugEnabled())
        log4j.debug(myError.getMessage());
      vars.setMessage(strTab, myError);
      log4j.warn("********** strWindowPath - " + strWindowPath);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strSettlement, String strKey) {
    OBError myError = null;
    int i = 0;
    Connection conn = null;

    String strDebtPayment = "";
    String strDebtPaymentBalancing = "";
    String strCBPartnerId = "";
    String strDate = "";
    String strDebe = "";
    String strHaber = "";
    String strImporte = "";
    String strCBPartnerId_D = "";
    String spChars = "";
    boolean inpEvent = false;
    try {
      conn = getTransactionConnection();
      CopyFromSettlementData[] data = CopyFromSettlementData.select(this, strKey);
      CopyFromSettlementData[] to = CopyFromSettlementData.selectSettlement(this, strSettlement);
      for (i = 0; data != null && i < data.length; i++) {
        CopyFromSettlementData[] data1 = CopyFromSettlementData.selectDebtPaymentBalancing(this,
            data[i].cDebtPaymentId);
        strDebtPayment = SequenceIdData.getUUID();
        strCBPartnerId = vars.getStringParameter("inpcBpartnerId" + data[i].cDebtPaymentId);
        strCBPartnerId_D = vars.getStringParameter("inpcBpartnerId_D" + data[i].cDebtPaymentId);
        if (strCBPartnerId_D.equals("")) {
          strCBPartnerId = "";
        }
        strDate = vars.getStringParameter("inpDate" + data[i].cDebtPaymentId);
        if (strDate.equals("")) {
          myError = Utility.translateError(this, vars, vars.getLanguage(),
              "The field DatePlanned is mandatory and cannot be left empty");
          return myError;
        }
        strImporte = vars.getStringParameter("inpAmount" + data[i].cDebtPaymentId);
        if (strImporte.equals("")) {
          myError = Utility.translateError(this, vars, vars.getLanguage(),
              "The field Amount is mandatory and cannot be left empty");
          return myError;
        }
        spChars = " `()\\~!@^&*+\"|:=,< >$%/;'";
        int pos;
        for (int a = 1; a <= 24; a++) {
          pos = strImporte.indexOf(spChars.charAt(a));
          if (pos != -1) {
            inpEvent = true;
          }
        }
        if (inpEvent == true) {
          myError = Utility.translateError(this, vars, vars.getLanguage(),
              "The number is not correct.You may check the data");
          return myError;
        }
        try {
          CopyFromSettlementData.insertDebtPayment(conn, this, strDebtPayment, to[0].client,
              to[0].org, vars.getUser(), data[i].isreceipt, strSettlement, data[i].description,
              strCBPartnerId, data[i].cCurrencyId, data[i].cBankaccountId, data[i].cCashbookId,
              data[i].paymentrule, strImporte, data[i].writeoffamt, strDate, data[i].ismanual,
              data[i].cGlitemId, data[i].isdirectposting, data[i].status);
        } catch (ServletException ex) {
          myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myError;
        }
        for (int j = 0; j < data1.length; j++) {
          strDebe = vars.getStringParameter("inpDebe" + data1[j].cDebtPaymentBalancingId);
          if (strDebe.equals("")) {
            myError = Utility.translateError(this, vars, vars.getLanguage(),
                "The field Debit is mandatory and cannot be left empty");
            return myError;
          }
          strHaber = vars.getStringParameter("inpHaber" + data1[j].cDebtPaymentBalancingId);
          if (strHaber.equals("")) {
            myError = Utility.translateError(this, vars, vars.getLanguage(),
                "The field Credit is mandatory and cannot be left empty");
            return myError;
          }
          strDebtPaymentBalancing = SequenceIdData.getUUID();
          try {
            CopyFromSettlementData.insert(conn, this, strDebtPaymentBalancing, to[0].client,
                to[0].org, vars.getUser(), strDebtPayment, strDebe, strHaber, data1[j].cGlitemId);
          } catch (ServletException ex) {
            myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myError;
          }
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

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Copy from Settlement");

    String[] discard = { "sectionDetail", "sectionDetail2" };

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CopyFromSettlement", discard).createXmlDocument();

    String strDateFormat = vars.getSessionValue("#AD_SqlDateFormat");

    xmlDocument.setParameter("dateFromdisplayFormat", strDateFormat);
    xmlDocument.setParameter("dateFromsaveFormat", strDateFormat);
    xmlDocument.setParameter("dateTodisplayFormat", strDateFormat);
    xmlDocument.setParameter("dateTosaveFormat", strDateFormat);
    xmlDocument.setParameter("dateFrom", "");
    xmlDocument.setParameter("dateTo", "");
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strSetDescription, String strDocumentNo, String strDateFrom, String strDateTo,
      String strSettlement, String strWindow) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Copy from Settlement");

    String[] discard = { "", "" };

    String myStrDocumentNo = (strDocumentNo == null || strDocumentNo.equals("")) ? "%"
        : strDocumentNo;
    String myStrSetDescription = (strSetDescription == null || strSetDescription.equals("")) ? "%"
        : strSetDescription;

    CopyFromSettlementData[] data = CopyFromSettlementData.selectRelation(this,
        myStrSetDescription, myStrDocumentNo,
        Utility.getContext(this, vars, "#User_Org", strWindow),
        Utility.getContext(this, vars, "#User_Client", strWindow), strDateFrom, strDateTo);

    if (data == null || data.length == 0)
      discard[0] = new String("sectionDetail");
    discard[1] = new String("sectionDetail2");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CopyFromSettlement", discard).createXmlDocument();
    String strDateFormat = vars.getSessionValue("#AD_SqlDateFormat");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", strDateFormat);
    xmlDocument.setParameter("dateFromsaveFormat", strDateFormat);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", strDateFormat);
    xmlDocument.setParameter("dateTosaveFormat", strDateFormat);
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("paramSettlement", strSettlement);
    xmlDocument.setParameter("documentNo", strDocumentNo);
    xmlDocument.setParameter("description", strSetDescription);
    xmlDocument.setData("structure", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strSettlement, String strSettlementFrom, String strWindow) throws IOException,
      ServletException {

    String strDateFrom = vars.getStringParameter("inpDateFrom");
    String strDateTo = vars.getStringParameter("inpDateTo");
    String strDocumentNo = vars.getStringParameter("inpDocumentNo");
    String strDescription = vars.getStringParameter("inpDescription");
    String strDateFormat = vars.getSessionValue("#AD_SqlDateFormat");

    String[] discard = { "", "" };

    String myStrDocumentNo = (strDocumentNo == null || strDocumentNo.equals("")) ? "%"
        : strDocumentNo;
    String myStrDescription = (strDescription == null || strDescription.equals("")) ? "%"
        : strDescription;

    CopyFromSettlementData[] data = CopyFromSettlementData.selectRelation(this, myStrDescription,
        myStrDocumentNo, Utility.getContext(this, vars, "#User_Org", strWindow),
        Utility.getContext(this, vars, "#User_Client", strWindow), strDateFrom, strDateTo);

    if (data == null || data.length == 0)
      discard[0] = new String("sectionDetail");

    CopyFromSettlementData[] data2 = CopyFromSettlementData.selectDebtPaymentBalancingF4(this,
        strDateFormat, vars.getLanguage(), strSettlementFrom);

    if (data2 == null || data2.length == 0)
      discard[1] = new String("sectionDetail2");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CopyFromSettlement", discard).createXmlDocument();

    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", strDateFormat);
    xmlDocument.setParameter("dateFromsaveFormat", strDateFormat);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", strDateFormat);
    xmlDocument.setParameter("dateTosaveFormat", strDateFormat);
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("paramSettlement", strSettlement);
    xmlDocument.setParameter("paramSettlementId", strSettlement);
    xmlDocument.setParameter("paramSettlementFromId", strSettlementFrom);
    xmlDocument.setParameter("documentNo", strDocumentNo);
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setData("structure", data);
    xmlDocument.setData("structure2", data2);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from settlement";
  } // end of getServletInfo() method
}
