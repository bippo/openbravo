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
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Journal_Period extends HttpSecureAppServlet {
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
      String strWindowId = vars.getStringParameter("inpWindowId");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strDateAcct = vars.getStringParameter("inpdateacct");
      String strDateDoc = vars.getStringParameter("inpdatedoc");
      String strcPeriodId = vars.getStringParameter("inpcPeriodId");
      String strTabId = vars.getStringParameter("inpTabId");
      try {
        printPage(response, vars, strDateAcct, strDateDoc, strcPeriodId, strWindowId, strChanged,
            strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strDateAcctNew, String strDateDocNew, String strcPeriodIdNew, String strWindowId,
      String strChanged, String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    String stradClientId = vars.getClient();
    final String stradOrgId = vars.getGlobalVariable("inpadOrgId", "SL_Journal_Period|adOrgId", "");
    ;
    String strDateAcct = strDateAcctNew;
    String strcPeriodId = strcPeriodIdNew;
    // When DateDoc is changed, update DateAcct
    if (strChanged.equals("inpdatedoc")) {
      strDateAcct = strDateDocNew;
      strChanged = "inpdateacct";
    }
    // When DateAcct is changed, set C_Period_ID
    if (strChanged.equals("inpdateacct")) {
      strcPeriodId = SLJournalPeriodData.period(this, stradClientId, stradOrgId, strDateAcct);
      if (strcPeriodId.equals(""))
        strcPeriodId = strcPeriodIdNew;
    }
    if (strChanged.equals("inpcPeriodId") && !strcPeriodId.equals("")) {
      // When C_Period_ID is changed, check if in DateAcct range and set
      // to end date if not
      SLJournalPeriodData[] data = SLJournalPeriodData.select(this, strcPeriodId);
      String PeriodType = data[0].periodtype;
      String StartDate = data[0].startdate;
      String EndDate = data[0].enddate;
      if (PeriodType.equals("S")) { // Standard Periods
        // out of range - set to last day
        if (DateTimeData.compare(this, StartDate, strDateAcct).equals("1")
            || DateTimeData.compare(this, EndDate, strDateAcct).equals("-1"))
          strDateAcct = EndDate;
      }
    }
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Journal_Period';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpdateacct\", \"" + strDateAcct + "\"),");
    resultado.append("new Array(\"inpcPeriodId\", \"" + strcPeriodId + "\")");

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
