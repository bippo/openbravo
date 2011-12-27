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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.javascript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * Checks the API of all .js files on a specific folder. This check verifies that your current
 * JavaScript API matches the ones specified in the .details files
 * 
 * @author iperdomo
 */
public class JavaScriptAPIChecker {
  private static final Logger log = Logger.getLogger(JavaScriptAPIChecker.class);
  private HashMap<String, String> apiMap = new HashMap<String, String>();
  private File apiDetailsFolder = null;
  private File jsFolder = null;
  private boolean export = false;

  /**
   * Returns if the export of .details file should be made
   * 
   * @return true if the export procedure should be made, false if not
   */
  public boolean isExport() {
    return export;
  }

  /**
   * Sets the flag to export the .details file
   * 
   * @param export
   *          boolean that sets that export procedure should be made
   */
  public void setExport(boolean export) {
    this.export = export;
  }

  /**
   * Sets the folder where the .details file are located
   * 
   * @param details
   */
  public void setDetailsFolder(File details) {
    if (!details.isDirectory()) {
      throw new RuntimeException("A API (.details) folder path must be passed as parameter");
    }
    apiDetailsFolder = details;
  }

  /**
   * Sets the folder that contains the .js files we want to check
   * 
   * @param jsFolder
   */
  public void setJSFolder(File jsFolder) {
    if (!jsFolder.isDirectory()) {
      throw new RuntimeException("A JavaScript folder path must be passed as parameter");
    }
    this.jsFolder = jsFolder;
  }

  /**
   * Returns a Map with all the broken entries on the API. This map must be empty after running the
   * check process
   * 
   * @return a Map containing the broken API
   */
  public HashMap<String, String> getAPIMap() {
    return apiMap;
  }

  /**
   * Process all the files defined in the js folder, and checks them against the files in the API
   * (.details) folder
   */
  public void process() {
    if (apiDetailsFolder == null) {
      throw new RuntimeException("A JavaScript API details folder must be set");
    }

    if (jsFolder == null) {
      throw new RuntimeException("A folder containing the JavaScript files must be set");
    }

    if (isExport()) { // export
      // Parsing the .js files and checking it agains the api map

      FilenameFilter jsFilter = new FilenameFilter() {
        public boolean accept(File dir, String fileName) {
          return fileName.endsWith(".js");
        }
      };

      final JavaScriptParser jsp = new JavaScriptParser();

      final String[] jsFiles = jsFolder.list(jsFilter);
      for (int j = 0; j < jsFiles.length; j++) {
        final File jsFile = new File(jsFolder, jsFiles[j]);
        jsp.setFile(jsFile);
        try {
          final File detailFile = new File(apiDetailsFolder, jsFiles[j] + ".details");
          jsp.toFile(detailFile);
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
      log.info("Export done!");
    } else { // check
      FilenameFilter detailsFilter = new FilenameFilter() {
        public boolean accept(File dir, String fileName) {
          return fileName.endsWith(".details");
        }
      };

      // Building a map with the current API details
      final String[] detailFiles = apiDetailsFolder.list(detailsFilter);
      for (int i = 0; i < detailFiles.length; i++) {
        final File dFile = new File(apiDetailsFolder, detailFiles[i]);
        int pos = detailFiles[i].indexOf(".details");
        final String jsFileName = detailFiles[i].substring(0, pos);
        String line;
        int lineNo = 1;
        try {
          BufferedReader br = new BufferedReader(new FileReader(dFile));
          while ((line = br.readLine()) != null) {
            apiMap.put(jsFileName + line, String.valueOf(lineNo));
            lineNo++;
          }
          br.close();
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }
      }

      // Parsing the .js files and checking it agains the api map

      FilenameFilter jsFilter = new FilenameFilter() {
        public boolean accept(File dir, String fileName) {
          return fileName.endsWith(".js");
        }
      };

      final JavaScriptParser jsp = new JavaScriptParser();

      final String[] jsFiles = jsFolder.list(jsFilter);
      for (int j = 0; j < jsFiles.length; j++) {
        final File jsFile = new File(jsFolder, jsFiles[j]);
        jsp.setFile(jsFile);
        try {
          checkJS(jsp, jsFiles[j]);
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Checks the parsed file against the API Map.
   * 
   * @param p
   *          an instance of {@link JavaScriptParser} to get the tree representation of it
   * @param jsFileName
   *          the JavaScript file name that is being checked
   * @throws IOException
   */
  private void checkJS(JavaScriptParser p, String jsFileName) throws IOException {
    ScriptOrFnNode nodeTree = p.parse();
    for (Node cursor = nodeTree.getFirstChild(); cursor != null; cursor = cursor.getNext()) {
      StringBuffer sb = new StringBuffer();
      if (cursor.getType() == Token.FUNCTION) {
        int fnIndex = cursor.getExistingIntProp(Node.FUNCTION_PROP);
        FunctionNode fn = nodeTree.getFunctionNode(fnIndex);
        sb.append("FUNCTION: " + fn.getFunctionName());
      } else if (cursor.getType() == Token.VAR) {
        Node vn = cursor.getFirstChild();
        sb.append("VAR: " + vn.getString());

      }
      apiMap.remove(jsFileName + sb);
    }
  }
}
