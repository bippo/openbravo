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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class SQLExecutor extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;

    if (vars.commandIn("DEFAULT")) {
      String strSQL = vars.getGlobalVariable("inpSQL", "SQLExecutor|sql", "");
      SQLExecutor_Query[] data = null;
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "SQLExecutor");
      int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
      String strInitRecord = vars.getSessionValue("SQLExecutor|initRecordNumber");
      int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      try {
        if (!strSQL.toUpperCase().trim().startsWith("SELECT ")) {
          vars.removeSessionValue("SQLExecutor|sql");
          throw new ServletException("Invalid SQL statement");
        }
        data = SQLExecutor_Query.select(this, strSQL, initRecordNumber, intRecordRange);
      } catch (Exception ignored) {
      }
      printPage(response, vars, strSQL, data, strInitRecord, initRecordNumber, intRecordRange);
    } else if (vars.commandIn("FIND")) {
      String strSQL = vars.getRequestGlobalVariable("inpSQL", "SQLExecutor|sql");
      SQLExecutor_Query[] data = null;
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "SQLExecutor");
      int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
      vars.setSessionValue("SQLExecutor|initRecordNumber", "0");
      int initRecordNumber = 0;
      try {
        if (!strSQL.toUpperCase().trim().startsWith("SELECT ")) {
          vars.removeSessionValue("SQLExecutor|sql");
          throw new ServletException("Invalid SQL statement");
        }
        data = SQLExecutor_Query.select(this, strSQL, initRecordNumber, intRecordRange);
      } catch (Exception ex) {
        ex.printStackTrace();
        // new message system
        myMessage = new OBError();
        myMessage.setType("Error");
        myMessage.setTitle("");
        myMessage.setMessage(Utility.messageBD(this, "Error", vars.getLanguage()));
        vars.setMessage("SQLExecutor", myMessage);
        // vars.setSessionValue("SQLExecutor|message",
        // Utility.messageBD(this, "Error", vars.getLanguage()));
      }
      log4j.debug("sql: " + strSQL);
      printPage(response, vars, strSQL, data, "0", initRecordNumber, intRecordRange);
    } else if (vars.commandIn("RELATION_XLS")) {
      String strSQL = vars.getGlobalVariable("inpSQL", "SQLExecutor|sql", "");
      vars.setSessionValue("SQLExecutor|sql", strSQL);
      SQLExecutor_Query[] data = null;
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "SQLExecutor");
      int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
      vars.setSessionValue("SQLExecutor|initRecordNumber", "0");
      int initRecordNumber = 0;
      try {
        if (!strSQL.toUpperCase().trim().startsWith("SELECT ")) {
          vars.removeSessionValue("SQLExecutor|sql");
          throw new ServletException("Invalid SQL statement");
        }
        data = SQLExecutor_Query.select(this, strSQL, initRecordNumber, intRecordRange);
      } catch (Exception ex) {
        ex.printStackTrace();
        // new message system
        myMessage = new OBError();
        myMessage.setType("Error");
        myMessage.setTitle("");
        myMessage.setMessage(Utility.messageBD(this, "Error", vars.getLanguage()));
        vars.setMessage("SQLExecutor", myMessage);
        // vars.setSessionValue("SQLExecutor|message",
        // Utility.messageBD(this, "Error", vars.getLanguage()));
      }
      log4j.debug("sql: " + strSQL);
      printExcel(response, vars, strSQL, data);
    } else if (vars.commandIn("FIRST_RELATION")) {
      vars.setSessionValue("SQLExecutor|initRecordNumber", "0");
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("SQLExecutor|initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "SQLExecutor");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) {
        vars.setSessionValue("SQLExecutor|initRecordNumber", "0");
      } else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("SQLExecutor|initRecordNumber", strInitRecord);
      }
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("SQLExecutor|initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "SQLExecutor");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      if (initRecord == 0)
        initRecord = 1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("SQLExecutor|initRecordNumber", strInitRecord);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
    } else if (vars.commandIn("LAST_RELATION")) {
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "SQLExecutor");
      String strSQL = vars.getGlobalVariable("inpSQL", "SQLExecutor|sql", "");
      String strInitRecord = lastRange(vars, strSQL, strRecordRange);
      vars.setSessionValue("SQLExecutor|initRecordNumber", strInitRecord);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
    } else
      pageError(response);
  }

  private String lastRange(VariablesSecureApp vars, String strSQL, String strRecordRange) {
    SQLExecutor_Query[] data = null;
    int initRecord = 0;
    try {
      data = SQLExecutor_Query.select(this, strSQL);
    } catch (Exception ex) {
      ex.printStackTrace();
      return "0";
    }
    while (initRecord < data.length) {
      initRecord += Integer.parseInt(strRecordRange);
    }
    initRecord -= Integer.parseInt(strRecordRange);
    if (initRecord < 0)
      initRecord = 0;
    return Integer.toString(initRecord);

  }

  private void printExcel(HttpServletResponse response, VariablesSecureApp vars, String strSQL,
      SQLExecutor_Query[] data) throws IOException, ServletException {
    log4j.info("print page");
    if (log4j.isDebugEnabled())
      log4j.debug("printPage - Reading xml\n");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/SQLExecutor_Excel").createXmlDocument();

    SQLExecutorData[] dataHeader = null;
    StringBuffer dataBuffer = new StringBuffer();
    if (data != null && data.length != 0) {
      log4j.debug("data != null || data.length != 0");
      dataHeader = new SQLExecutorData[data[0].name.size()];
      for (int i = 0; i < data[0].name.size(); i++) {
        log4j.debug("data[0].name: " + data[0].name);
        log4j.debug(data[0].name.elementAt(i));
        dataHeader[i] = new SQLExecutorData();
        dataHeader[i].header = data[0].name.elementAt(i);
      }
      dataBuffer.append("<tr>\n");
      for (int j = 0; j < data.length; j++) {
        if (j != 0) {
          dataBuffer.append("<tr>\n");
          for (int k = 0; k < data[0].name.size(); k++) {
            dataBuffer.append("<td>");
            dataBuffer.append(data[j].getField(Integer.toString(k)));
            dataBuffer.append("</td>\n");
          }
          dataBuffer.append("</tr>\n");
        }
      }
    }
    log4j.debug("dataBuffer: " + dataBuffer.toString());
    xmlDocument.setParameter("data", dataBuffer.toString());
    xmlDocument.setData("structureHeader", dataHeader);

    response.setContentType("text/xls; charset=UTF-8");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug("printPage - Printing document\n");
    out.println(xmlDocument.print());
    out.close();
    if (log4j.isDebugEnabled())
      log4j.debug("printPage - End printing document\n");
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strSQL,
      SQLExecutor_Query[] data, String strInitRecord, int initRecordNumber, int intRecordRange)
      throws IOException, ServletException {
    log4j.info("print page");
    if (log4j.isDebugEnabled())
      log4j.debug("printPage - Reading xml\n");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/SQLExecutor").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");

    // new message system
    // String strMessage = vars.getSessionValue("SQLExecutor|message");;
    // if (!strMessage.equals("")) strMessage = "alert('" + strMessage +
    // "');";
    // vars.removeSessionValue("SQLExecutor|message");
    // xmlDocument.setParameter("buscador", strMessage);
    //

    xmlDocument.setParameter("sql", strSQL);
    log4j.debug("sql");
    SQLExecutorData[] dataHeader = null;
    StringBuffer dataBuffer = new StringBuffer();
    if (data != null && data.length != 0) {
      log4j.debug("data != null || data.length != 0");
      dataHeader = new SQLExecutorData[data[0].name.size()];
      for (int i = 0; i < data[0].name.size(); i++) {
        log4j.debug("data[0].name: " + data[0].name);
        log4j.debug(data[0].name.elementAt(i));
        dataHeader[i] = new SQLExecutorData();
        dataHeader[i].header = data[0].name.elementAt(i);
      }
      for (int j = 0; j < data.length; j++) {
        int evenOdd = j % 2;
        dataBuffer.append("<tr class=\"TableDetailRow" + String.valueOf(evenOdd) + "\">\n");
        for (int k = 0; k < data[0].name.size(); k++) {
          dataBuffer.append("<td>");
          dataBuffer.append(data[j].getField(Integer.toString(k)));
          dataBuffer.append("</td>\n");
        }
        dataBuffer.append("</tr>\n");
      }
    }
    /*
     * StringBuffer strData = new StringBuffer("var myData = new Array(\n"); StringBuffer strHeader
     * = new StringBuffer(); if (log4j.isDebugEnabled())
     * log4j.debug("printPage - Making grid data\n"); int selectedIndex = 0; if (data!=null &&
     * data.length!=0) { for (int countHeads=0;countHeads<data[0].type.size();countHeads++) {
     * strHeader. append("obj.addHeader(new Header(\"").append(data[0].name.elementAt (countHeads
     * )).append("\", \"").append(data[0].type.elementAt(countHeads
     * )).append("\", 100, 20, true));\n"); } for (int
     * contadorData=0;contadorData<data.length;contadorData++) { strData.append("new Array("); for
     * (int countHeads=0;countHeads<data[0].data.size();countHeads++) { if (countHeads>0)
     * strData.append(","); strData.append("\"").append(Replace
     * .replace(Replace.replace(Replace.replace
     * (data[contadorData].getField(Integer.toString(countHeads)), "\r", ""), "\n", "<br>"), "\"",
     * "\\\"")).append("\""); } strData.append(")"); if (contadorData < (data.length-1))
     * strData.append(","); strData.append("\n"); } } else { strInitRecord = "0"; }
     * strData.append(");"); String strGrid = "obj.setSelectedRow(" +
     * Integer.toString(selectedIndex) + ");\n"; strGrid += "obj.setInitRowNum(" +
     * (strInitRecord.equals("0")?"1":strInitRecord) + ");\n"; xmlDocument.setParameter("grid",
     * strGrid); xmlDocument.setParameter("header", strHeader.toString());
     * xmlDocument.setParameter("data", strData.toString());
     */
    xmlDocument.setData("structureHeader", dataHeader);
    xmlDocument.setParameter("data", dataBuffer.toString());
    // xmlDocument.setData("reportLinea", "structure1", dataLinea);
    if (log4j.isDebugEnabled())
      log4j.debug("printPage - Making toolbar\n");
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "SQLExecutor", false,
        "document.frmMain.inpKey", "myGrid", null, false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareQueryTemplate((initRecordNumber > 1),
        (data != null && data.length != 0 && data.length >= intRecordRange),
        vars.getSessionValue("#ShowTest", "N").equals("Y"));
    xmlDocument.setParameter("toolbar", toolbar.toString());

    log4j.debug("toolbar");
    log4j.debug("keymap");
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.SQLExecutor");
      log4j.debug("tabs");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      log4j.debug("parentTabContainer");
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      log4j.debug("mainTabContainer");
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      log4j.debug("childTabContainer");
      xmlDocument.setParameter("theme", vars.getTheme());
      log4j.debug("theme");
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "SQLExecutor.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      log4j.debug("navigationBar");
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "SQLExecutor.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      log4j.debug("leftTabs");
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("SQLExecutor");
      vars.removeMessage("SQLExecutor");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    log4j.debug("calendar");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug("printPage - Printing document\n");
    out.println(xmlDocument.print());
    out.close();
    if (log4j.isDebugEnabled())
      log4j.debug("printPage - End printing document\n");
  }

  public String getServletInfo() {
    return "Servlet for the standard SQL execution";
  } // end of getServletInfo() method
}
