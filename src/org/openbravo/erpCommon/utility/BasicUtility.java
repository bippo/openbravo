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
package org.openbravo.erpCommon.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.utils.Replace;

/**
 * Basic utilities needed by the 'ApplyModule' module class.
 * 
 * Before the 'ant apply.module' target is run a mini compilation is done to compile all classes
 * needed by that code (and not more). This BasicUtility class was split out of the main 'Utility'
 * class to not pull in the big chain of transitive compile-time dependencies into ApplyModule.
 * 
 * Nothing should be added here without reviewing the list of files compiled by the 'ant
 * compile.apply.module' target to check that it did not grow.
 * 
 */
public class BasicUtility {
  private static final Logger log4j = Logger.getLogger(BasicUtility.class);

  /**
   * @see Utility#messageBD(ConnectionProvider, String, String, boolean)
   */
  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage) {
    return messageBD(conn, strCode, strLanguage, true);
  }

  /**
   * Translate the given code into some message from the application dictionary.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param strCode
   *          String with the code to search.
   * @param strLanguage
   *          String with the translation language.
   * @param escape
   *          Escape \n and " characters
   * @return String with the translated message.
   */
  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage,
      boolean escape) {
    String strMessage = "";
    if (strLanguage == null || strLanguage.equals(""))
      strLanguage = "en_US";

    try {
      log4j.debug("Utility.messageBD - Message Code: " + strCode);
      strMessage = MessageBDData.message(conn, strLanguage, strCode);
    } catch (final Exception ignore) {
      log4j.error("Error getting message", ignore);
    }
    log4j.debug("Utility.messageBD - Message description: " + strMessage);
    if (strMessage == null || strMessage.equals("")) {
      try {
        strMessage = MessageBDData.columnname(conn, strLanguage, strCode);
      } catch (final Exception e) {
        log4j.error("Error getting message", e);
        strMessage = strCode;
      }
    }
    if (strMessage == null || strMessage.equals("")) {
      strMessage = strCode;
    }
    if (escape) {
      strMessage = Replace.replace(Replace.replace(strMessage, "\n", "\\n"), "\"", "&quot;");
    }
    return strMessage;
  }

  /**
   * 
   * Formats a message String into a String for html presentation. Escapes the &, <, >, " and ®, and
   * replace the \n by <br/>
   * and \r for space.
   * 
   * IMPORTANT! : this method is designed to transform the output of Utility.messageBD method, and
   * this method replaces \n by \\n and \" by &quote. Because of that, the first replacements revert
   * this previous replacements.
   * 
   * @param message
   *          message with java formating
   * @return html format message
   */
  public static String formatMessageBDToHtml(String message) {
    return Replace.replace(Replace.replace(Replace.replace(Replace.replace(Replace.replace(Replace
        .replace(Replace.replace(Replace.replace(Replace.replace(
            Replace.replace(Replace.replace(message, "\\n", "\n"), "&quot", "\""), "&", "&amp;"),
            "\"", "&quot;"), "<", "&lt;"), ">", "&gt;"), "\n", "<br/>"), "\r", " "), "®", "&reg;"),
        "&lt;![CDATA[", "<![CDATA["), "]]&gt;", "]]>");
  }

  /**
   * Generates a String representing the file in a path
   * 
   * @param strPath
   * @return file to a String
   */
  public static String fileToString(String strPath) throws FileNotFoundException {
    StringBuffer strMyFile = new StringBuffer("");
    try {
      File f = new File(strPath);
      FileInputStream fis = new FileInputStream(f);
      InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

      final BufferedReader mybr = new BufferedReader(isr);

      String strTemp = mybr.readLine();
      strMyFile.append(strTemp);
      while (strTemp != null) {
        strTemp = mybr.readLine();
        if (strTemp != null)
          strMyFile.append("\n").append(strTemp);
        else {
          mybr.close();
          fis.close();
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return strMyFile.toString();
  }

  /**
   * Generates a String representing the wikified name from source
   * 
   * @param strSource
   * @return strTarget: wikified name
   */
  public static String wikifiedName(String strSource) throws FileNotFoundException {
    if (strSource == null || strSource.equals(""))
      return strSource;
    strSource = strSource.trim();
    if (strSource.equals(""))
      return strSource;
    final StringTokenizer source = new StringTokenizer(strSource, " ", false);
    String strTarget = "";
    String strTemp = "";
    int i = 0;
    while (source.hasMoreTokens()) {
      strTemp = source.nextToken();
      if (i != 0)
        strTarget = strTarget + "_" + strTemp;
      else {
        final String strFirstChar = strTemp.substring(0, 1);
        strTemp = strFirstChar.toUpperCase() + strTemp.substring(1, strTemp.length());
        strTarget = strTarget + strTemp;
      }
      i++;
    }
    return strTarget;
  }

}
