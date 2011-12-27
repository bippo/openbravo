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
import java.net.URL;
import java.util.Properties;

import junit.framework.TestCase;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;

/**
 * Test case for JavaScript API check
 * 
 * @author iperdomo
 * 
 */
public class JavaScriptAPITest extends TestCase {

  /**
   * Sets up the Openbravo.properties file
   */
  @Override
  protected void setUp() throws Exception {
    setConfigPropertyFiles();
    super.setUp();
  }

  /**
   * Helper function to get the Openbravo.properties file path
   */
  protected void setConfigPropertyFiles() {
    // get the location of the current class file
    final URL url = this.getClass().getResource(getClass().getSimpleName() + ".class");
    File f = new File(url.getPath());
    // go up 7 levels
    for (int i = 0; i < 7; i++) {
      f = f.getParentFile();
    }
    final File configDirectory = new File(f, "config");
    f = new File(configDirectory, "Openbravo.properties");
    if (!f.exists()) {
      throw new OBException("The testrun assumes that it is run from "
          + "within eclipse and that the Openbravo.properties "
          + "file is located as a grandchild of the 7th ancestor " + "of this class");
    }
    OBPropertiesProvider.getInstance().setProperties(f.getAbsolutePath());
  }

  /**
   * Checks the JavaScript API It fails if the resultant API Map is not empty
   */
  public void testJavaScriptAPI() {
    JavaScriptAPIChecker apiCheck = new JavaScriptAPIChecker();
    final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    final String sourcePath = (String) props.get("source.path");
    apiCheck.setDetailsFolder(new File(sourcePath + File.separator + "config"));
    apiCheck.setJSFolder(new File(sourcePath + File.separator + "web" + File.separator + "js"));
    apiCheck.process();
    assertTrue("API Map should be empty: " + apiCheck.getAPIMap(), apiCheck.getAPIMap().isEmpty());
  }

  /*
   * public void testJSDetailFiles() { FilenameFilter jsFilter = new FilenameFilter() { public
   * boolean accept(File dir, String fileName) { return fileName.endsWith(".js"); } }; Properties
   * props = OBPropertiesProvider.getInstance().getOpenbravoProperties(); final String sourcePath =
   * (String) props.get("source.path"); final String tmpPath = System.getProperty("java.io.tmpdir");
   * 
   * File jsFolder = new File(sourcePath + File.separator + "web" + File.separator + "js"); String[]
   * jsFiles = jsFolder.list(jsFilter); JavaScriptParser jsp = new JavaScriptParser(); for (int i =
   * 0; i < jsFiles.length; i++) { File js = new File(jsFolder, jsFiles[i]); System.out.println(js);
   * jsp.setFile(js); File jsDetils = new File(tmpPath, jsFiles[i] + ".details"); try {
   * jsp.toFile(jsDetils); } catch (IOException e) { e.printStackTrace(); } } }
   */
}
