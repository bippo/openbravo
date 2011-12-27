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

public class EditCCPMeasureValues extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {

      String strWindowId = vars.getStringParameter("inpwindowId");
      String strKey = vars.getRequiredStringParameter("inpmaMeasureShiftId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strProcessId = "800062";
      String strMeasureDate = vars.getStringParameter("inpmeasuredate");
      String strShift = vars.getStringParameter("inpshift");

      printPageDataSheet(response, vars, strKey, strWindowId, strTabId, strProcessId,
          strMeasureDate, strShift);
    } else if (vars.commandIn("SAVE")) {
      String strKey = vars.getStringParameter("inpmaMeasureShiftId");
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strTabId = vars.getStringParameter("inpTabId");
      String[] strValueId = request.getParameterValues("strKey");
      String[] strGroupId = request.getParameterValues("strGroup");
      ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTabId);

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myMessage = updateValues(vars, strValueId, strGroupId, strKey, strWindowId);
      vars.setMessage(strTabId, myMessage);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strKey, String strWindowId, String strTabId, String strProcessId,
      String strMeasureDate, String strShift) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: values ");
    String[] discard = { "" };
    EditCCPMeasureValuesData[] data = null;
    data = EditCCPMeasureValuesData.select(this, strKey);

    String shift = "";

    if (vars.getLanguage().equals("en_US"))
      shift = EditCCPMeasureValuesData.selectShift(this, strShift);
    else
      shift = EditCCPMeasureValuesData.selectShiftTrl(this, strShift, vars.getLanguage());
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/EditCCPMeasureValues", discard)
        .createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);

    xmlDocument.setParameter("measureDate", strMeasureDate);
    xmlDocument.setParameter("shift", shift);

    if (log4j.isDebugEnabled())
      log4j.debug("Param: " + vars.getLanguage() + " " + strReplaceWith + " " + strKey + " "
          + strWindowId + " " + strTabId + " " + strMeasureDate + " shift: " + shift + " "
          + strShift);
    EditCCPMeasureValuesHoursData[][] dataHours = new EditCCPMeasureValuesHoursData[data.length][10];
    EditCCPMeasureValuesValuesData[][] dataValues = new EditCCPMeasureValuesValuesData[data.length][];

    for (int i = 0; i < data.length; i++) {
      dataHours[i] = EditCCPMeasureValuesHoursData.select(this, data[i].groupid);
      if (dataHours[i] == null || dataHours[i].length == 0)
        dataHours[i] = new EditCCPMeasureValuesHoursData[0];

      dataValues[i] = EditCCPMeasureValuesValuesData.select(this, data[i].groupid);
      if (dataValues[i] == null || dataValues[i].length == 0)
        dataValues[i] = new EditCCPMeasureValuesValuesData[0];
    }

    OBError myMessage = vars.getMessage("EditCCPMeasureValues");
    vars.removeMessage("EditCCPMeasureValues");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    xmlDocument.setData("structure1", data);
    xmlDocument.setDataArray("reportHours", "structureHours", dataHours);
    xmlDocument.setDataArray("reportValues", "structureValues", dataValues);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
    if (log4j.isDebugEnabled())
      log4j.debug("Output: values - out");
  }

  private OBError updateValues(VariablesSecureApp vars, String[] strValueId, String[] strGroupId,
      String strKey, String strWindowId) {

    if (log4j.isDebugEnabled())
      log4j.debug("Update: values");

    OBError myError = null;
    int total = 0;
    Boolean error = false;

    if (log4j.isDebugEnabled())
      log4j.debug("Update: values after strValueID");
    if (strValueId == null || strValueId.length == 0) {
      myError = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
      return myError;
    }
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      for (int i = 0; i < strValueId.length; i++) {
        String numeric = "";
        String text = "";
        String check = "N";
        if (log4j.isDebugEnabled())
          log4j.debug("*****strValueId[i]=" + strValueId[i]);
        if (!strValueId[i].equals("0")) {
          String type = EditCCPMeasureValuesData.selectType(this, strValueId[i]);
          if (type.equals("N"))
            numeric = vars.getStringParameter("strValue" + strValueId[i]);
          else if (type.equals("T"))
            text = vars.getStringParameter("strValue" + strValueId[i]);
          else
            check = vars.getStringParameter("strValue" + strValueId[i]);
          if (log4j.isDebugEnabled())
            log4j.debug("Values to update: " + strValueId[i] + ", " + numeric + ", " + text + ", "
                + check);
          try {
            total = EditCCPMeasureValuesData.update(conn, this, numeric, text,
                check.equals("Y") ? "Y" : "N", strValueId[i]);
          } catch (ServletException ex) {
            myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myError;
          }
          if (total == 0)
            error = true;
        }
      }

      if (error) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=DBExecuteError");
        return myError;
      }

      releaseCommitConnection(conn);
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle("");
      myError.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
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

  public String getServletInfo() {
    return "Servlet that presents the button of Create From Multiple";
  } // end of getServletInfo() method
}
