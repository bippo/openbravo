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

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * JavaScript Ant task to check the JavaScript API
 * 
 * @author iperdomo
 */
public class JavaScriptAntTest extends Task {
  private static final Logger log = Logger.getLogger(JavaScriptAntTest.class);
  private JavaScriptAPIChecker jsAPIChecker = null;
  private String apiDetailsPath = null;
  private String jsPath = null;
  private boolean export;

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
   * Gets the folder path of the .details files
   * 
   * @return the string folder
   */
  public String getApiDetailsPath() {
    return apiDetailsPath;
  }

  /**
   * Sets the folder path of the .details files
   * 
   * @param apiDetailsPath
   *          a String folder path
   */
  public void setApiDetailsPath(String apiDetailsPath) {
    this.apiDetailsPath = apiDetailsPath;
  }

  /**
   * Gets the folder path of the .js files
   * 
   * @return the folder path of the .js files
   */
  public String getJsPath() {
    return jsPath;
  }

  /**
   * Sets the folder path of the .js files to check
   * 
   * @param jsPath
   *          a String folder path
   */
  public void setJsPath(String jsPath) {
    this.jsPath = jsPath;
  }

  /**
   * Overloaded execute Task method. Processes all the files in the js folder and checks them
   * against the API one. It fails if the resultant API Map is not empty.
   */
  @Override
  public void execute() throws BuildException {
    log.debug("JS API details folder: " + this.apiDetailsPath);
    log.debug("JS folder:" + this.jsPath);
    log.debug("JS Export procedure:" + this.export);

    jsAPIChecker = new JavaScriptAPIChecker();
    jsAPIChecker.setDetailsFolder(new File(this.apiDetailsPath));
    jsAPIChecker.setJSFolder(new File(this.jsPath));
    jsAPIChecker.setExport(this.export);
    jsAPIChecker.process();

    if (!jsAPIChecker.getAPIMap().isEmpty()) {
      throw new BuildException("API Map must be empty: " + jsAPIChecker.getAPIMap());
    }
  }
}
