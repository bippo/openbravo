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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_help;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.xmlEngine.XmlEngine;

public class HelpWindow {

  /**
   * Currently wiki help is generated just for modules in Openbravo 3 distribution.
   * <code>ob3DistroModules</code> maintains the list of modules within it, so link to wiki help is
   * only shown for them.
   */
  private static final List<String> ob3DistroModules = new ArrayList<String>() {
    private static final long serialVersionUID = 1L;
    {
      add("0"); // Core
      add("0138E7A89B5E4DC3932462252801FFBC"); // Openbravo 3.0
      add("0A060B2AF1974E8EAA8DB61388E9AECC"); // Query/List Widget
      add("2758CD25B2704AF6BBAD10365FC82C06"); // Workspace & Widgets
      add("2A5EE903D7974AC298C0504FBC4501A7"); // Payment Report
      add("3A3A943684D64DEF9EC39F588A656848"); // Orders Awaiting Delivery
      add("4B828F4D03264080AA1D2057B13F613C"); // User Interface Client Kernel
      add("5EB4F15C80684ACA904756BDC12ADBE5"); // User Interface Selector
      add("7E48CDD73B7E493A8BED4F7253E7C989"); // Openbravo 3.0 Framework
      add("883B5872CA0548F9AF2BBBE7D2DDFA61"); // Standard Roles
      add("96998CBC42744B3DBEE28AC8095C9335"); // 2.50 to 3.00 Compatibility Skin
      add("9BA0836A3CD74EE4AB48753A47211BCC"); // User Interface Application
      add("A44B9BA75C354D8FB2E3F7D6EB6BFDC4"); // JSON Datasource
      add("A918E3331C404B889D69AA9BFAFB23AC"); // Advanced Payables and Receivables Mngmt
      add("C70732EA90A14EC0916078B85CC33D2D"); // JBoss Weld
      add("D393BE6F22BB44B7B728259B34FC795A"); // HTML Widget
      add("D66395531D1E4364AFCD90FE6A8A5166"); // Openbravo 3 Demo Login Page
      add("EC356CEE3D46416CA1EBEEB9AB82EDB9"); // Smartclient
      add("F8D1B3ECB3474E8DA5C216473C840DF1"); // JSON REST Webservice
      add("FF8080812D842086012D844F3CC0003E"); // Widgets Collection
      add("FF8080813129ADA401312CA1222A0005"); // Integration with Google APIs
      add("FF8080813141B198013141B86DD70003"); // OpenID Service Integration
      add("FF8081812E008C6E012E00A613DC0019"); // Openbravo 3 Demo Sampledata API
    }
  };

  private static Logger log4j = Logger.getLogger(HelpWindow.class);

  public static String generateWindow(ConnectionProvider conn, XmlEngine xmlEngine,
      VariablesSecureApp vars, boolean discardEdit, String strKeyId) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Window");
    Boolean window = true;
    String[] discard = { "", "", "", "", "" };
    String strType = "";
    String strWindowName = "";
    String strWindowHelp = "";
    String strBaseName = "";
    String strCommand = "";
    boolean showWikiLink = false;

    if (discardEdit)
      discard[0] = new String("discardEdit");
    else if (strKeyId.equals("")) {
      strType = vars.getRequiredStringParameter("inpwindowType");
      window = false;
      if (strType.equals("X")) {
        strCommand = "FORM";
        strKeyId = vars.getRequiredStringParameter("inpwindowName");
        DisplayHelpData[] dataForm = DisplayHelpData.selectFormTrl(conn, vars.getLanguage(),
            strKeyId);
        if (dataForm != null && dataForm.length > 0) {
          strWindowName = dataForm[0].name;
          strWindowHelp = dataForm[0].help;
          strBaseName = dataForm[0].basename;
          showWikiLink = ob3DistroModules.contains(dataForm[0].moduleid);
        } else {
          discard[3] = new String("discardEdit");
        }
      } else if (strType.equals("P") || strType.equals("R")) {
        strCommand = "PROCESS";
        strKeyId = vars.getRequiredStringParameter("inpwindowName");
        DisplayHelpData[] dataProcess = DisplayHelpData.selectProcessTrl(conn, vars.getLanguage(),
            strKeyId);
        if (dataProcess != null && dataProcess.length > 0) {
          strWindowName = dataProcess[0].name;
          strWindowHelp = dataProcess[0].help;
          strBaseName = dataProcess[0].basename;
          showWikiLink = ob3DistroModules.contains(dataProcess[0].moduleid);
        } else {
          discard[3] = new String("discardEdit");
        }
      }
      discard[0] = new String("sectionTabsRelation");
      discard[1] = new String("sectionTabsDescription");
      discard[2] = new String("sectionCabeceraFields");
    }

    DisplayHelpData[] data = DisplayHelpData.set();
    if (window) {
      data = DisplayHelpData.selectTrl(conn, vars.getLanguage(), strKeyId);
      if (data != null && data.length > 0) {
        strWindowName = data[0].windowname;
        strBaseName = data[0].basename;
        showWikiLink = ob3DistroModules.contains(data[0].moduleid);
      } else {
        discard[3] = new String("discardEdit");
      }
      strWindowHelp = DisplayHelpData.windowHelpTrl(conn, vars.getLanguage(), strKeyId);
      strCommand = "WINDOW";
    }

    if (!showWikiLink) {
      discard[4] = "showWikiLink";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp", discard).createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + xmlEngine.strReplaceWith
        + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strKeyId);
    xmlDocument.setParameter("windowName", strWindowName);
    xmlDocument.setParameter("windowHelp", strWindowHelp);
    xmlDocument.setParameter("command", strCommand);
    xmlDocument.setParameter("wikiLink", strBaseName);
    xmlDocument.setData("structure1", data);
    xmlDocument.setData("structure2", data);
    xmlDocument.setData("structure3", data);

    return (xmlDocument.print());
  }
}
