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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesManager {

  Properties obProperties;

  public PropertiesManager(String propertiesFile) {
    try {
      obProperties = new Properties();
      obProperties.load(new FileInputStream(propertiesFile));
    } catch (final Exception e) {
      // do nothing
    }
  }

  public PropertiesManager() {
    this("src-util/diagnostic/config/diagnostics.properties");
  }

  public String getProperty(String name) {
    return obProperties.getProperty(name);
  }
}
