/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
 ******************************************************************************/
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * Class for import/export languages.
 * 
 * The tree of languages is:
 * 
 * {attachmentsDir} {laguageFolder} {moduleFolder}
 * 
 * Example: /opt/attachments/ en_US/ <trl tables from core> module1/ <trl tables from module1>
 * 
 */
public class Translation extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static ConnectionProvider cp;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    System.setProperty("javax.xml.transform.TransformerFactory",
        "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"); // added
    // for
    // JDK1.5
    if (vars.commandIn("DEFAULT")) {
      printPageDataSheet(response, vars);
    } else if (vars.commandIn("EXPORT")) {

      final String strLang = vars.getRequestGlobalVariable("language", "translation.lang");
      // import/export translation is currently always on system level
      final String strClient = "0";
      if (log4j.isDebugEnabled())
        log4j.debug("Lang " + strLang + " Client " + strClient);

      // New message system
      final OBError myMessage = TranslationManager.exportTrl(this,
          globalParameters.strFTPDirectory, strLang, strClient, vars.getLanguage());

      if (log4j.isDebugEnabled())
        log4j.debug("message:" + myMessage.getMessage());
      vars.setMessage("Translation", myMessage);
      response.sendRedirect(strDireccion + request.getServletPath());

    } else {
      final String strLang = vars.getRequestGlobalVariable("language", "translation.lang");
      // import/export translation is currently always on system level
      final String strClient = "0";
      if (log4j.isDebugEnabled())
        log4j.debug("Lang " + strLang + " Client " + strClient);

      final String directory = globalParameters.strFTPDirectory + "/lang/" + strLang + "/";
      final OBError myMessage = TranslationManager.importTrlDirectory(this, directory, strLang,
          strClient, vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("message:" + myMessage.getMessage());
      vars.setMessage("Translation", myMessage);
      response.sendRedirect(strDireccion + request.getServletPath());

    }
  }

  @Deprecated
  public static void setConnectionProvicer(ConnectionProvider conn) {
    cp = conn;
  }

  @Deprecated
  public static void setLog4j(Logger logger) {
    // Note: This method does not do anything anymore, however is kept to keep the API stable.
    // Logging of the import/export code is not always done using the same static logger as the rest
    // of the openbravo code instead of using the callers logger like before.
  }

  /**
   * 
   * The import process insert in database all the translations found in the folder of the defined
   * language RECURSIVELY. It don't take into account if a module is marked o no as isInDevelopment.
   * Only search for trl's xml files corresponding with trl's tables in database.
   * 
   * @deprecated Use TranslationManager.importTrlDirectory instead
   * @param directory
   *          Directory for trl's xml files
   * @param strLang
   *          Language to import
   * @param strClient
   *          Client to import
   * @param vars
   *          Handler for the session info.
   * @return Message with the error or with the success
   */
  @Deprecated
  public static OBError importTrlDirectory(String directory, String strLang, String strClient,
      VariablesSecureApp vars) {
    return TranslationManager.importTrlDirectory(cp, directory, strLang, strClient,
        vars.getLanguage());
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/Translation")
        .createXmlDocument();
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "Translation", false, "", "", "",
        false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.Translation");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "Translation.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "Translation.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    {
      final OBError myMessage = vars.getMessage("Translation");
      vars.removeMessage("Translation");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      if (log4j.isDebugEnabled() && myMessage != null)
        log4j.debug("datasheet message:" + myMessage.getMessage());

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("paramSelLanguage", vars.getSessionValue("translation.lang"));
      xmlDocument.setData("structure1", LanguageComboData.select(this));

      out.println(xmlDocument.print());
      out.close();
    }
  }
} // Translation
