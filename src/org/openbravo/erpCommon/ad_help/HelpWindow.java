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
package org.openbravo.erpCommon.ad_help;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.xmlEngine.XmlEngine;

public class HelpWindow {
  private static Logger log4j = Logger.getLogger(HelpWindow.class);

  public static String generateWindow(ConnectionProvider conn, XmlEngine xmlEngine,
      VariablesSecureApp vars, boolean discardEdit, String strKeyId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Window");
    Boolean window = true;
    String[] discard = { "", "", "", "" };
    String strType = "";
    String strWindowName = "";
    String strWindowHelp = "";
    String strCommand = "";
    if (discardEdit)
      discard[0] = new String("discardEdit");
    else if (strKeyId.equals("")) {
      strType = vars.getRequiredStringParameter("inpwindowType");
      window = false;
      if (strType.equals("X")) {
        strCommand = "FORM";
        strKeyId = vars.getRequiredStringParameter("inpwindowName");
        DisplayHelpData[] dataForm = (vars.getLanguage().equals("en_US") ? DisplayHelpData
            .selectForm(conn, strKeyId) : DisplayHelpData.selectFormTrl(conn, vars.getLanguage(),
            strKeyId));
        if (dataForm != null && dataForm.length > 0) {
          strWindowName = dataForm[0].name;
          strWindowHelp = dataForm[0].help;
        } else {
          discard[3] = new String("discardEdit");
        }
      } else if (strType.equals("P") || strType.equals("R")) {
        strCommand = "PROCESS";
        strKeyId = vars.getRequiredStringParameter("inpwindowName");
        DisplayHelpData[] dataProcess = (vars.getLanguage().equals("en_US") ? DisplayHelpData
            .selectProcess(conn, strKeyId) : DisplayHelpData.selectProcessTrl(conn,
            vars.getLanguage(), strKeyId));
        if (dataProcess != null && dataProcess.length > 0) {
          strWindowName = dataProcess[0].name;
          strWindowHelp = dataProcess[0].help;
        } else {
          discard[3] = new String("discardEdit");
        }
      }
      discard[0] = new String("sectionTabsRelation");
      discard[1] = new String("sectionTabsDescription");
      discard[2] = new String("sectionCabeceraFields");
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp", discard).createXmlDocument();

    DisplayHelpData[] data = DisplayHelpData.set();
    if (window) {
      data = (vars.getLanguage().equals("en_US") ? DisplayHelpData.select(conn, strKeyId)
          : DisplayHelpData.selectTrl(conn, vars.getLanguage(), strKeyId));
      strWindowName = vars.getLanguage().equals("en_US") ? DisplayHelpData.windowName(conn,
          strKeyId) : DisplayHelpData.windowNameTrl(conn, vars.getLanguage(), strKeyId);
      strWindowHelp = vars.getLanguage().equals("en_US") ? DisplayHelpData.windowHelp(conn,
          strKeyId) : DisplayHelpData.windowHelpTrl(conn, vars.getLanguage(), strKeyId);
      strCommand = "WINDOW";
    }
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + xmlEngine.strReplaceWith
        + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strKeyId);
    xmlDocument.setParameter("windowName", strWindowName);
    xmlDocument.setParameter("windowHelp", strWindowHelp);
    xmlDocument.setParameter("command", strCommand);
    xmlDocument.setData("structure1", data);
    xmlDocument.setData("structure2", data);
    xmlDocument.setData("structure3", data);
    return (xmlDocument.print());
  }
}
