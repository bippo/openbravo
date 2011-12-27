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

package org.openbravo.base.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.util.Check;

/**
 * This class implements a central location where the Openbravo.properties are read and made
 * available for the rest of the application.
 * 
 * @author mtaal
 */
public class OBPropertiesProvider {
  private final Logger log = Logger.getLogger(OBPropertiesProvider.class);

  private static OBPropertiesProvider instance = new OBPropertiesProvider();

  private static boolean friendlyWarnings = false;

  public static boolean isFriendlyWarnings() {
    return friendlyWarnings;
  }

  public static void setFriendlyWarnings(boolean doFriendlyWarnings) {
    friendlyWarnings = doFriendlyWarnings;
  }

  private Properties obProperties = null;
  private Document formatXML;

  public static synchronized OBPropertiesProvider getInstance() {
    return instance;
  }

  public static synchronized void setInstance(OBPropertiesProvider instance) {
    OBPropertiesProvider.instance = instance;
  }

  public Properties getOpenbravoProperties() {
    if (obProperties == null) {
      readPropertiesFromDevelopmentProject();
    }
    return obProperties;
  }

  public Document getFormatXMLDocument() {
    if (formatXML == null) {
      final File file = getFileFromDevelopmentPath("Format.xml");
      if (file != null) {
        try {
          SAXReader reader = new SAXReader();
          formatXML = reader.read(new FileReader(file));
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      }
    }
    return formatXML;
  }

  public void setFormatXML(InputStream is) {
    try {
      SAXReader reader = new SAXReader();
      formatXML = reader.read(is);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public void setProperties(InputStream is) {
    if (obProperties != null) {
      log.warn("Openbravo properties have already been set, setting them again");
    }
    log.debug("Setting openbravo.properties through input stream");
    obProperties = new Properties();
    try {
      obProperties.load(is);
      is.close();
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  public void setProperties(Properties props) {
    Check.isNull(obProperties, "Openbravo properties have already been set");
    log.debug("Setting openbravo.properties through properties");
    obProperties = new Properties();
    obProperties.putAll(props);
  }

  public void setProperties(String fileLocation) {
    // Check.isNull(obProperties,
    // "Openbravo properties have already been set");
    log.debug("Setting openbravo.properties through a file");
    obProperties = new Properties();
    try {
      final FileInputStream fis = new FileInputStream(fileLocation);
      obProperties.load(fis);
      fis.close();
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  // tries to read the properties from the openbravo development project
  private void readPropertiesFromDevelopmentProject() {
    final File propertiesFile = getFileFromDevelopmentPath("Openbravo.properties");
    if (propertiesFile == null) {
      return;
    }
    setProperties(propertiesFile.getAbsolutePath());
    OBConfigFileProvider.getInstance().setFileLocation(
        propertiesFile.getParentFile().getAbsolutePath());
  }

  private File getFileFromDevelopmentPath(String fileName) {
    // get the location of the current class file
    final URL url = this.getClass().getResource(getClass().getSimpleName() + ".class");
    File f = new File(url.getPath());
    File propertiesFile = null;
    while (f.getParentFile() != null && f.getParentFile().exists()) {
      f = f.getParentFile();
      final File configDirectory = new File(f, "config");
      if (configDirectory.exists()) {
        propertiesFile = new File(configDirectory, fileName);
        if (propertiesFile.exists()) {
          // found it and break
          break;
        }
      }
    }
    return propertiesFile;
  }
}