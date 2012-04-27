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
package org.openbravo.wad;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.Sqlc;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.wad.controls.WADButton;
import org.openbravo.wad.controls.WADControl;
import org.openbravo.wad.controls.WADGrid;
import org.openbravo.wad.controls.WADLabelControl;
import org.openbravo.wad.controls.WadControlLabelBuilder;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.xmlEngine.XmlEngine;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Main class of WAD project. This class manage all the process to generate the sources from the
 * model.
 * 
 * @author Fernando Iriazabal
 */
public class Wad extends DefaultHandler {
  private static final int COLUMN_1_OF_1 = 11;
  private static final int COLUMN_1_OF_2 = 12;
  private static final int COLUMN_2_OF_2 = 22;
  private static final int NUM_TABS = 8;
  private static final int INCR_TABS = 8;
  private static final int HEIGHT_TABS = 38;
  private static final int MAX_SIZE_EDITION_1_COLUMNS = 90;
  private static final int MAX_SIZE_EDITION_2_COLUMNS = 45;
  private static final int MAX_TEXTBOX_LENGTH = 110;
  private static final double PIXEL_TO_LENGTH = 5.6;
  private XmlEngine xmlEngine;
  private WadConnection pool;
  private String strSystemSeparator;
  private static String jsDateFormat;
  private static String sqlDateFormat;

  private static final Logger log4j = Logger.getLogger(Wad.class);

  /**
   * Main function, entrusted to launch the process of generation of sources. The list of arguments
   * that it can receive are the following ones:<br>
   * <ol>
   * <li>Path to XmlPool.xml</li>
   * <li>Name of the window to generate (% for all)</li>
   * <li>Path to generate the code</li>
   * <li>Path to generate common objects (reference)</li>
   * <li>Path to generate the web.xml</li>
   * <li>Parameter:
   * <ul>
   * <li>tabs (To generate only the windows and action buttons)</li>
   * <li>web.xml (To generate only the web.xml)</li>
   * <li>all (To generate everything)</li>
   * </ul>
   * <li>Path to generate the action buttons</li>
   * <li>Path to generate the translated objects</li>
   * <li>Base package for the translation objects</li>
   * <li>Path to find the client web.xml file</li>
   * <li>Path to the project root</li>
   * <li>Path to the attached files</li>
   * <li>Url to the static web contents</li>
   * <li>Path to the src</li>
   * <li>Boolean to indicate if it's gonna be made a complete generation or not</li>
   * </ol>
   * 
   * @param argv
   *          Arguments array
   * @throws Exception
   */
  public static void main(String argv[]) throws Exception {
    PropertyConfigurator.configure("log4j.lcf");
    String strWindowName;
    String module;
    String dirFin;
    String dirReference;
    String dirWebXml;
    String dirActionButton;
    boolean generateWebXml;
    boolean generateTabs;
    String attachPath;
    String webPath;
    boolean complete;
    boolean quick;
    if (argv.length < 1) {
      log4j.error("Usage: java Wad connection.xml [{% || Window} [destinyDir]]");
      return;
    }
    final String strFileConnection = argv[0];
    final Wad wad = new Wad();
    wad.strSystemSeparator = System.getProperty("file.separator");
    wad.createPool(strFileConnection + "/Openbravo.properties");
    wad.createXmlEngine(strFileConnection);
    wad.readProperties(strFileConnection + "/Openbravo.properties");
    try {
      // the second parameter is the tab to be generated
      // if there is none it's * then all them are read
      strWindowName = argv[1];

      // the third parameter is the directory where the tab files are
      // created
      if (argv.length <= 2)
        dirFin = ".";
      else
        dirFin = argv[2];

      // the fourth paramenter is the directory where the references are
      // created
      // (TableList_data.xsql y TableDir_data.xsql)
      if (argv.length <= 3)
        dirReference = dirFin;
      else
        dirReference = argv[3];

      // the fifth parameter is the directory where web.xml is created
      if (argv.length <= 4)
        dirWebXml = dirFin;
      else
        dirWebXml = argv[4];

      // the sixth parementer indicates whether web.xml has to be
      // generated or not
      if (argv.length <= 5) {
        generateWebXml = true;
        generateTabs = true;
      } else if (argv[5].equals("web.xml")) {
        generateWebXml = true;
        generateTabs = false;
      } else if (argv[5].equals("tabs")) {
        generateWebXml = false;
        generateTabs = true;
      } else {
        generateWebXml = true;
        generateTabs = true;
      }

      // Path to generate the action button
      if (argv.length <= 6)
        dirActionButton = dirFin;
      else
        dirActionButton = argv[6];

      // Path to base translation generation
      // was argv[7] no longer used

      // Translate base structure
      // was argv[8] no longer used

      // Path to find the client's web.xml file
      // was argv[9] no longer used

      // Path of the root project
      // was argv[10] no longer used

      // Path of the attach files
      if (argv.length <= 11)
        attachPath = dirFin;
      else
        attachPath = argv[11];

      // Url to the static content
      if (argv.length <= 12)
        webPath = dirFin;
      else
        webPath = argv[12];

      // Path to the src folder
      // was argv[13] no longer used

      // Boolean to indicate if we are doing a complete generation
      if (argv.length <= 14)
        complete = false;
      else
        complete = ((argv[14].equals("true")) ? true : false);

      // Module to compile
      if (argv.length <= 15)
        module = "%";
      else
        module = argv[15].equals("%") ? "%" : "'"
            + argv[15].replace(", ", ",").replace(",", "', '") + "'";

      // Check for quick build
      if (argv.length <= 16)
        quick = false;
      else
        quick = argv[16].equals("quick");

      if (quick) {
        module = "%";
        strWindowName = "xx";
      }

      log4j.info("File connection: " + strFileConnection);
      log4j.info("window: " + strWindowName);
      log4j.info("module: " + module);
      log4j.info("directory destiny: " + dirFin);
      log4j.info("directory reference: " + dirReference + wad.strSystemSeparator + "reference");
      log4j.info("directory web.xml: " + dirWebXml);
      log4j.info("directory ActionButtons: " + dirActionButton);
      log4j.info("generate web.xml: " + generateWebXml);
      log4j.info("generate tabs: " + generateTabs);
      log4j.info("File separator: " + wad.strSystemSeparator);
      log4j.info("Attach path: " + attachPath);
      log4j.info("Web path: " + webPath);
      log4j.info("Quick mode: " + quick);

      final File fileFin = new File(dirFin);
      if (!fileFin.exists()) {
        log4j.error("No such directory: " + fileFin.getAbsoluteFile());

        return;
      }

      final File fileFinReloads = new File(dirReference + wad.strSystemSeparator + "ad_callouts");
      if (!fileFinReloads.exists()) {
        log4j.error("No such directory: " + fileFinReloads.getAbsoluteFile());

        return;
      }

      final File fileReference = new File(dirReference + wad.strSystemSeparator + "reference");
      if (!fileReference.exists()) {
        log4j.error("No such directory: " + fileReference.getAbsoluteFile());

        return;
      }

      final File fileWebXml = new File(dirWebXml);
      if (!fileWebXml.exists()) {
        log4j.error("No such directory: " + fileWebXml.getAbsoluteFile());

        return;
      }

      final File fileActionButton = new File(dirActionButton);
      if (!fileActionButton.exists()) {
        log4j.error("No such directory: " + fileActionButton.getAbsoluteFile());

        return;
      }

      // Calculate windows to generate
      String strCurrentWindow;
      final StringTokenizer st = new StringTokenizer(strWindowName, ",", false);
      ArrayList<TabsData> td = new ArrayList<TabsData>();
      while (st.hasMoreTokens()) {
        strCurrentWindow = st.nextToken().trim();
        TabsData tabsDataAux[];
        if (quick)
          tabsDataAux = TabsData.selectQuick(wad.pool);
        else if (module.equals("%") || complete)
          tabsDataAux = TabsData.selectTabs(wad.pool, strCurrentWindow);
        else
          tabsDataAux = TabsData.selectTabsinModules(wad.pool, strCurrentWindow, module);
        td.addAll(Arrays.asList(tabsDataAux));
      }
      TabsData[] tabsData = td.toArray(new TabsData[0]);
      log4j.info(tabsData.length + " tabs to compile.");

      // Call to update the table identifiers
      log4j.info("Updating table identifiers");
      WadData.updateIdentifiers(wad.pool, quick ? "Y" : "N");

      // Call to generate audit trail infrastructure
      log4j.info("Re-generating audit trail infrastructure");
      WadData.updateAuditTrail(wad.pool);

      // If generateTabs parameter is true, the action buttons must be
      // generated
      if (generateTabs) {
        if (!quick || ProcessRelationData.generateActionButton(wad.pool)) {
          wad.processProcessComboReloads(fileFinReloads);
          wad.processActionButton(fileReference);
        } else {
          log4j.info("No changes in ActionButton_data.xml");
        }
        if (!quick || FieldsData.buildActionButton(wad.pool)) {
          wad.processActionButtonXml(fileActionButton);
          wad.processActionButtonHtml(fileActionButton);
        } else
          log4j.info("No changes in Action button for columns");
        if (!quick || ActionButtonRelationData.buildGenerics(wad.pool)) {
          wad.processActionButtonGenerics(fileActionButton);
          wad.processActionButtonXmlGenerics(fileActionButton);
          wad.processActionButtonHtmlGenerics(fileActionButton);
          wad.processActionButtonSQLDefaultGenerics(fileActionButton);
        } else
          log4j.info("No changes in generic action button responser");

      }

      // If generateWebXml parameter is true, the web.xml file should be
      // generated
      if (generateWebXml) {

        if (!quick || WadData.genereteWebXml(wad.pool))
          wad.processWebXml(fileWebXml, attachPath, webPath);
        else
          log4j.info("No changes in web.xml");
      }

      if (tabsData.length == 0)
        log4j.info("No windows to compile");

      if (generateTabs) {
        for (int i = 0; i < tabsData.length; i++) {
          // don't compile if it is in an unactive branch
          if (wad.allTabParentsActive(tabsData[i].tabid)) {
            log4j.info("Processing Window: " + tabsData[i].windowname + " - Tab: "
                + tabsData[i].tabname + " - id: " + tabsData[i].tabid);
            log4j.debug("Processing: " + tabsData[i].tabid);
            wad.processTab(fileFin, fileFinReloads, tabsData[i]);
          }
        }
      }

    } catch (final Exception e) {

      throw new Exception(e);
    } finally {
      wad.pool.destroy();
    }
  }

  private boolean allTabParentsActive(String tabId) {
    try {
      if (!TabsData.isTabActive(pool, tabId))
        return false;
      else {
        String parentTabId = TabsData.selectParentTab(pool, tabId);
        if (parentTabId != null && !parentTabId.equals(""))
          return allTabParentsActive(parentTabId);
      }
      return true;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Generates the action button's xsql files
   * 
   * @param fileReference
   *          The path where to create the files.
   */
  private void processActionButton(File fileReference) {
    try {
      log4j.info("Processing ActionButton_data.xml");
      final XmlDocument xmlDocumentData = xmlEngine.readXmlTemplate(
          "org/openbravo/wad/ActionButton_data").createXmlDocument();
      final ProcessRelationData ard[] = ProcessRelationData.select(pool);

      xmlDocumentData.setData("structure1", ard);
      WadUtility.writeFile(fileReference, "ActionButton_data.xsql",
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xmlDocumentData.print());
    } catch (final ServletException e) {
      e.printStackTrace();
      log4j.error("Problem of ServletExceptio in process of ActionButtonData");
    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOExceptio in process of ActionButtonData");
    }
  }

  /**
   * Generates the action button's xml files
   * 
   * @param fileReference
   *          The path where to create the files.
   */
  private void processActionButtonXml(File fileReference) {
    try {
      log4j.info("Processing ActionButtonXml");
      final FieldsData fd[] = FieldsData.selectActionButton(pool);
      if (fd != null) {
        for (int i = 0; i < fd.length; i++) {
          final Vector<Object> vecFields = new Vector<Object>();
          WadActionButton.buildXml(pool, xmlEngine, fileReference, fd[i], vecFields,
              MAX_TEXTBOX_LENGTH);
        }
      }
    } catch (final ServletException e) {
      e.printStackTrace();
      log4j.error("Problem of ServletExceptio in process of ActionButtonXml");
    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOExceptio in process of ActionButtonXml");
    }
  }

  /**
   * Generates the action button's html files
   * 
   * @param fileReference
   *          The path where to create the files.
   */
  private void processActionButtonHtml(File fileReference) {
    try {
      log4j.info("Processing ActionButtonHtml");
      final FieldsData fd[] = FieldsData.selectActionButton(pool);
      if (fd != null) {
        for (int i = 0; i < fd.length; i++) {
          final Vector<Object> vecFields = new Vector<Object>();

          // calculate fields that need combo reload
          final FieldsData[] dataReload = FieldsData.selectValidationProcess(pool, fd[i].reference);

          final Vector<Object> vecReloads = new Vector<Object>();
          if (dataReload != null && dataReload.length > 0) {
            for (int z = 0; z < dataReload.length; z++) {
              String code = dataReload[z].whereclause
                  + ((!dataReload[z].whereclause.equals("") && !dataReload[z].referencevalue
                      .equals("")) ? " AND " : "") + dataReload[z].referencevalue;

              if (code.equals("") && dataReload[z].type.equals("R"))
                code = "@AD_Org_ID@";
              WadUtility.getComboReloadText(code, vecFields, null, vecReloads, "",
                  dataReload[z].columnname);
            }
          }

          // build the html template
          WadActionButton.buildHtml(pool, xmlEngine, fileReference, fd[i], vecFields,
              MAX_TEXTBOX_LENGTH, MAX_SIZE_EDITION_1_COLUMNS, "", false, jsDateFormat, vecReloads);
        }
      }
    } catch (final ServletException e) {
      e.printStackTrace();
      log4j.error("Problem of ServletExceptio in process of ActionButtonHtml");
    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOExceptio in process of ActionButtonHtml");
    }
  }

  /**
   * Generates the main file to manage the action buttons (ActionButton_Responser.java). These are
   * the menu's action buttons.
   * 
   * @param fileReference
   *          The path where to create the files.
   */
  private void processActionButtonGenerics(File fileReference) {
    try {
      // Generic action button for jasper and PL
      log4j.info("Processing ActionButton_Responser.xml");
      XmlDocument xmlDocumentData = xmlEngine.readXmlTemplate(
          "org/openbravo/wad/ActionButton_Responser").createXmlDocument();

      ActionButtonRelationData[] abrd = WadActionButton.buildActionButtonCallGenerics(pool);
      xmlDocumentData.setData("structure1", abrd);
      xmlDocumentData.setData("structure2", abrd);
      xmlDocumentData.setData("structure3", abrd);
      xmlDocumentData.setData("structure4", abrd);

      WadUtility.writeFile(fileReference, "ActionButton_Responser.java", xmlDocumentData.print());

      // Generic action button for java
      log4j.info("Processing ActionButton_ResponserJava.xml");
      xmlDocumentData = xmlEngine.readXmlTemplate("org/openbravo/wad/ActionButtonJava_Responser")
          .createXmlDocument();
      abrd = WadActionButton.buildActionButtonCallGenericsJava(pool);

      xmlDocumentData.setData("structure1", abrd);
      xmlDocumentData.setData("structure2", abrd);
      xmlDocumentData.setData("structure3", abrd);
      xmlDocumentData.setData("structure4", abrd);

      WadUtility.writeFile(fileReference, "ActionButtonJava_Responser.java",
          xmlDocumentData.print());

    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOExceptio in process of ActionButton_Responser");
    }
  }

  /**
   * Generates the action button's xsql file for the action buttons called directly from menu. This
   * xsql file contains all the queries needed for SQL default values in generated parameters.
   * 
   * @param fileReference
   *          The path where to create the files.
   */
  private void processActionButtonSQLDefaultGenerics(File fileReference) {
    try {
      log4j.info("Processing ActionButtonDefault_data.xsql");

      ProcessRelationData defaults[] = ProcessRelationData.selectXSQLGenericsParams(pool);
      if (defaults != null && defaults.length > 0) {
        for (int i = 0; i < defaults.length; i++) {
          final Vector<Object> vecParametros = new Vector<Object>();
          defaults[i].reference = defaults[i].adProcessId + "_"
              + FormatUtilities.replace(defaults[i].columnname);
          defaults[i].defaultvalue = WadUtility.getSQLWadContext(defaults[i].defaultvalue,
              vecParametros);
          final StringBuffer parametros = new StringBuffer();
          for (final Enumeration<Object> e = vecParametros.elements(); e.hasMoreElements();) {
            final String paramsElement = WadUtility.getWhereParameter(e.nextElement(), true);
            parametros.append("\n" + paramsElement);
          }
          defaults[i].whereclause = parametros.toString();
        }
        XmlDocument xmlDocumentData = xmlEngine.readXmlTemplate(
            "org/openbravo/wad/ActionButtonDefault_data").createXmlDocument();
        xmlDocumentData.setData("structure16", defaults);

        WadUtility.writeFile(fileReference, "ActionButtonSQLDefault_data.xsql",
            xmlDocumentData.print());
      }
    } catch (final Exception e) {
      log4j.error(e);
    }
  }

  /**
   * Generates the action button's xml files. These are the menu's action buttons.
   * 
   * @param fileReference
   *          The path where to create the files.
   */
  private void processActionButtonXmlGenerics(File fileReference) {
    try {
      log4j.info("Processing ActionButtonXml Generics");
      final FieldsData fd[] = FieldsData.selectActionButtonGenerics(pool);
      if (fd != null) {
        for (int i = 0; i < fd.length; i++) {
          final Vector<Object> vecFields = new Vector<Object>();
          WadActionButton.buildXml(pool, xmlEngine, fileReference, fd[i], vecFields,
              MAX_TEXTBOX_LENGTH);
        }
      }
    } catch (final ServletException e) {
      e.printStackTrace();
      log4j.error("Problem of ServletExceptio in process of ActionButtonXml Generics");
    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOExceptio in process of ActionButtonXml Generics");
    }
  }

  /**
   * Generates the action button's html files. These are the menu's action button
   * 
   * @param fileReference
   *          The path where to create the files.
   */
  private void processActionButtonHtmlGenerics(File fileReference) {
    try {
      log4j.info("Processing ActionButtonHtml for generics");
      final FieldsData fd[] = FieldsData.selectActionButtonGenerics(pool);
      if (fd != null) {
        for (int i = 0; i < fd.length; i++) {
          final Vector<Object> vecFields = new Vector<Object>();

          // calculate fields that need combo reload
          final FieldsData[] dataReload = FieldsData.selectValidationProcess(pool, fd[i].reference);

          final Vector<Object> vecReloads = new Vector<Object>();
          if (dataReload != null && dataReload.length > 0) {
            for (int z = 0; z < dataReload.length; z++) {
              String code = dataReload[z].whereclause
                  + ((!dataReload[z].whereclause.equals("") && !dataReload[z].referencevalue
                      .equals("")) ? " AND " : "") + dataReload[z].referencevalue;

              if (code.equals("") && dataReload[z].type.equals("R"))
                code = "@AD_Org_ID@";
              WadUtility.getComboReloadText(code, vecFields, null, vecReloads, "",
                  dataReload[z].columnname);
            }
          }

          // build the html template
          WadActionButton.buildHtml(pool, xmlEngine, fileReference, fd[i], vecFields,
              MAX_TEXTBOX_LENGTH, MAX_SIZE_EDITION_1_COLUMNS, "", true, jsDateFormat, vecReloads);
        }
      }
    } catch (final ServletException e) {
      e.printStackTrace();
      log4j.error("Problem of ServletExceptio in process of ActionButtonHtml Generics");
    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOExceptio in process of ActionButtonHtml Generics");
    }
  }

  /**
   * Generates the web.xml file
   * 
   * @param fileWebXml
   *          path to generate the new web.xml file.
   * @param attachPath
   *          The path where are the attached files.
   * @param webPath
   *          The url where are the static web content.
   * @throws ServletException
   * @throws IOException
   */
  private void processWebXml(File fileWebXml, String attachPath, String webPath)
      throws ServletException, IOException {
    try {
      log4j.info("Processing web.xml");
      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/wad/webConf")
          .createXmlDocument();

      xmlDocument.setParameter("webPath", webPath);
      xmlDocument.setParameter("attachPath", attachPath);
      xmlDocument.setData("structureListener", WadData.selectListener(pool));
      xmlDocument.setData("structureResource", WadData.selectResource(pool));
      final WadData[] filters = WadData.selectFilter(pool);
      WadData[][] filterParams = null;
      if (filters != null && filters.length > 0) {
        filterParams = new WadData[filters.length][];
        for (int i = 0; i < filters.length; i++) {
          filterParams[i] = WadData.selectParams(pool, "F", filters[i].classname, filters[i].id);
        }
      } else
        filterParams = new WadData[0][0];
      xmlDocument.setData("structureFilter", filters);
      xmlDocument.setDataArray("reportFilterParams", "structure1", filterParams);

      WadData[] contextParams = WadData.selectContextParams(pool);
      xmlDocument.setData("structureContextParams", contextParams);

      WadData[] allTabs = WadData.selectAllTabs(pool);
      xmlDocument.setData("structureServletTab", getTabServlets(allTabs));
      xmlDocument.setData("structureMappingTab", getTabMappings(allTabs));

      final WadData[] servlets = WadData.select(pool);
      WadData[][] servletParams = null;
      if (servlets != null && servlets.length > 0) {
        servletParams = new WadData[servlets.length][];
        for (int i = 0; i < servlets.length; i++) {
          if (servlets[i].loadonstartup != null && !servlets[i].loadonstartup.equals(""))
            servlets[i].loadonstartup = "<load-on-startup>" + servlets[i].loadonstartup
                + "</load-on-startup>";
          servletParams[i] = WadData.selectParams(pool, "S", servlets[i].classname, servlets[i].id);
        }
      } else
        servletParams = new WadData[0][0];

      WadData[] timeout = WadData.selectSessionTimeOut(pool);
      if (timeout.length == 0) {
        log4j.info("No session timeout found, setting default 60min");
      } else if (timeout.length > 1) {
        log4j.error("Multiple session timeout config found (" + timeout.length
            + "), setting default 60min");
      } else {
        xmlDocument.setParameter("fieldSessionTimeOut", timeout[0].value);
      }

      xmlDocument.setData("structure1", servlets);
      xmlDocument.setDataArray("reportServletParams", "structure1", servletParams);
      xmlDocument.setData("structureFilterMapping", WadData.selectFilterMapping(pool));
      xmlDocument.setData("structure2", WadData.selectMapping(pool));

      String webXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xmlDocument.print();
      webXml = webXml.replace("${attachPath}", attachPath);
      webXml = webXml.replace("${webPath}", webPath);

      WadUtility.writeFile(fileWebXml, "web.xml", webXml);
    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOException in process of Web.xml");
    }
  }

  private WadData[] getTabServlets(WadData[] allTabs) {
    ArrayList<WadData> servlets = new ArrayList<WadData>();
    for (WadData tab : allTabs) {
      String tabClassName = "org.openbravo.erpWindows."
          + ("0".equals(tab.windowmodule) ? "" : tab.windowpackage + ".") + tab.windowname + "."
          + tab.tabname + ("0".equals(tab.tabmodule) ? "" : tab.adTabId);

      WadData servlet = new WadData();
      servlet.displayname = tabClassName;
      servlet.name = "W" + tab.adTabId;
      servlet.classname = tabClassName;
      servlets.add(servlet);

      String comboReloadClassName = "org.openbravo.erpCommon.ad_callouts.ComboReloads"
          + tab.adTabId;
      WadData servletCombo = new WadData();
      servletCombo.displayname = comboReloadClassName;
      servletCombo.name = "WR" + tab.adTabId;
      servletCombo.classname = comboReloadClassName;
      servlets.add(servletCombo);
    }
    return servlets.toArray(new WadData[servlets.size()]);
  }

  private FieldProvider[] getTabMappings(WadData[] allTabs) {
    ArrayList<WadData> mappings = new ArrayList<WadData>();
    for (WadData tab : allTabs) {
      String prefix = "/" + ("0".equals(tab.windowmodule) ? "" : tab.windowpackage)
          + tab.windowname + "/" + tab.tabname + ("0".equals(tab.tabmodule) ? "" : tab.adTabId);

      WadData mapping = new WadData();
      mapping.name = "W" + tab.adTabId;
      mapping.classname = prefix + "_Relation.html";
      mappings.add(mapping);

      WadData mapping2 = new WadData();
      mapping2.name = "W" + tab.adTabId;
      mapping2.classname = prefix + "_Edition.html";
      mappings.add(mapping2);

      WadData mapping3 = new WadData();
      mapping3.name = "W" + tab.adTabId;
      mapping3.classname = prefix + "_Excel.xls";
      mappings.add(mapping3);

      WadData mapping4 = new WadData();
      mapping4.name = "WR" + tab.adTabId;
      mapping4.classname = "/ad_callouts/ComboReloads" + tab.adTabId + ".html";
      mappings.add(mapping4);
    }
    return mappings.toArray(new WadData[mappings.size()]);
  }

  /**
   * Generates all the windows defined in the dictionary. Also generates the translated files for
   * the defineds languages.
   * 
   * @param fileFin
   *          Path where are gonna be created the sources.
   * @param fileFinReloads
   *          Path where are gonna be created the reloads sources.
   * @param tabsData
   *          An object containing the tabs info.
   * @throws Exception
   */
  private void processTab(File fileFin, File fileFinReloads, TabsData tabsData) throws Exception {
    try {
      final String tabNamePresentation = tabsData.realtabname;
      // tabName contains tab's UUID for non core tabs
      final String tabName = FormatUtilities.replace(tabNamePresentation)
          + (tabsData.tabmodule.equals("0") ? "" : tabsData.tabid);
      final String windowName = FormatUtilities.replace(tabsData.windowname);
      final String tableName = FieldsData.tableName(pool, tabsData.tabid);
      final String isSOTrx = FieldsData.isSOTrx(pool, tabsData.tabid);
      final TabsData[] allTabs = getPrimaryTabs(tabsData.key, tabsData.tabid,
          Integer.valueOf(tabsData.tablevel).intValue(), HEIGHT_TABS, INCR_TABS);
      final FieldsData[] fieldsData = FieldsData.select(pool, tabsData.tabid);
      final EditionFieldsData efd[] = EditionFieldsData.select(pool, tabsData.tabid);
      final EditionFieldsData efdauxiliar[] = EditionFieldsData
          .selectAuxiliar(pool, tabsData.tabid);

      /************************************************
       * The 2 tab lines generation
       *************************************************/
      if (allTabs == null || allTabs.length == 0)
        throw new Exception("No tabs found for AD_Tab_ID: " + tabsData.tabid + " - key: "
            + tabsData.key + " - level: " + tabsData.tablevel);
      final TabsData[] tab1 = new TabsData[(allTabs.length > NUM_TABS) ? NUM_TABS : allTabs.length];
      final TabsData[] tab2 = new TabsData[(allTabs.length > NUM_TABS) ? NUM_TABS : 0];
      for (int i = 0; i < NUM_TABS && i < allTabs.length; i++) {
        tab1[i] = allTabs[i];
      }
      if (allTabs.length > NUM_TABS) {
        int j = 0;
        for (int i = allTabs.length - NUM_TABS; i < allTabs.length; i++)
          tab2[j++] = allTabs[i];
      }

      int parentTabIndex = -1;
      boolean sinParent = false;
      String grandfatherField = "";
      if (allTabs != null && allTabs.length > 0)
        parentTabIndex = parentTabId(allTabs, tabsData.tabid);
      FieldsData[] parentsFieldsData = null;

      if (tabsData.issorttab.equals("Y")) {
        parentsFieldsData = FieldsData.parentsColumnNameSortTab(pool,
            (parentTabIndex != -1 ? allTabs[parentTabIndex].tabid : ""), tabsData.tableId);
      } else {
        parentsFieldsData = FieldsData.parentsColumnName(pool,
            (parentTabIndex != -1 ? allTabs[parentTabIndex].tabid : ""), tabsData.tabid);
      }

      if (parentTabIndex != -1 && (parentsFieldsData == null || parentsFieldsData.length == 0)) {
        parentsFieldsData = FieldsData.parentsColumnReal(pool, allTabs[parentTabIndex].tabid,
            tabsData.tabid);
        sinParent = true;
        if (parentsFieldsData == null || parentsFieldsData.length == 0) {
          log4j.warn("No key found in parent tab: " + allTabs[parentTabIndex].tabname);
        }
      }

      final Vector<Object> vecFields = new Vector<Object>();
      final Vector<Object> vecTables = new Vector<Object>();
      final Vector<Object> vecWhere = new Vector<Object>();
      final Vector<Object> vecOrder = new Vector<Object>();
      final Vector<Object> vecParameters = new Vector<Object>();
      final Vector<Object> vecTableParameters = new Vector<Object>();
      final Vector<Object> vecTotalParameters = new Vector<Object>();
      final Vector<String> vecFieldParameters = new Vector<String>();
      processTable(parentsFieldsData, tabsData.tabid, vecFields, vecTables, vecWhere, vecOrder,
          vecParameters, tableName, tabsData.windowtype, tabsData.tablevel, vecTableParameters,
          fieldsData, vecFieldParameters);
      final StringBuffer strFields = new StringBuffer();
      log4j.debug("Executing de select conformation");
      for (int i = 0; i < vecTableParameters.size(); i++) {
        vecTotalParameters.addElement(vecTableParameters.elementAt(i));
      }
      for (int i = 0; i < vecParameters.size(); i++) {
        vecTotalParameters.addElement(vecParameters.elementAt(i));
      }
      for (final Enumeration<Object> e = vecFields.elements(); e.hasMoreElements();) {
        final String fieldElement = (String) e.nextElement();
        strFields.append(fieldElement + ", \n");
      }
      log4j.debug("Fields of select: " + strFields.toString());
      final StringBuffer strTables = new StringBuffer();
      for (final Enumeration<Object> e = vecTables.elements(); e.hasMoreElements();) {
        final String tableElement = (String) e.nextElement();
        strTables.append((tableElement.trim().toLowerCase().startsWith("left join") ? " " : ", ")
            + tableElement);
      }
      log4j.debug("Tables of select: " + strTables.toString());
      final StringBuffer strWhere = new StringBuffer();
      for (final Enumeration<Object> e = vecWhere.elements(); e.hasMoreElements();) {
        final String whereElement = (String) e.nextElement();
        strWhere.append((!whereElement.startsWith(" AND") ? " AND " : "") + whereElement);
      }
      String whereClauseParams = "";
      if (!tabsData.whereclause.equals("")) {
        final int totalParameters = vecTotalParameters.size();
        strWhere.append(" AND " + WadUtility.buildSQL(tabsData.whereclause, vecTotalParameters));
        if (totalParameters < vecTotalParameters.size()) {
          ArrayList<String> usedParameters = new ArrayList<String>();
          for (int h = totalParameters; h < vecTotalParameters.size(); h++) {
            String strParam = WadUtility.getWhereParameter(vecTotalParameters.elementAt(h), false);
            vecParameters.addElement(WadUtility.getWhereParameter(vecTotalParameters.elementAt(h),
                true));
            if (!usedParameters.contains(strParam)) {
              usedParameters.add(strParam);
              whereClauseParams += ", Utility.getContext(this, vars, \"" + strParam
                  + "\", windowId)";
            }
          }
        }
      }
      log4j.debug("Where of select: " + strWhere.toString());
      final StringBuffer strOrder = new StringBuffer();
      log4j.debug("Order Vector's Size: " + vecOrder.size());
      if (tabsData.orderbyclause.equals("")) {
        if (vecOrder.size() > 0)
          strOrder.append(" ORDER BY ");
        boolean first = true;
        for (final Enumeration<Object> e = vecOrder.elements(); e.hasMoreElements();) {
          final String orderElement = (String) e.nextElement();
          log4j.debug("Order element: " + orderElement);
          strOrder.append(((!first) ? ", " : "") + orderElement);
          if (first)
            first = false;
        }
      } else {
        strOrder.append(" ORDER BY " + tabsData.orderbyclause);
      }
      log4j.debug("Order of select: " + strOrder.toString());
      if (strOrder.toString().equals(""))
        strOrder.append(" ORDER BY 1");

      EditionFieldsData[] selCol = EditionFieldsData.selectSerchFieldsSelection(pool, "",
          tabsData.tabid);
      if (selCol == null || selCol.length == 0)
        selCol = EditionFieldsData.selectSerchFields(pool, "", tabsData.tabid);
      selCol = processSelCol(selCol, tableName);

      final String javaPackage = (!tabsData.javapackage.equals("") ? tabsData.javapackage.replace(
          ".", "/") + "/" : "")
          + windowName; // Take into account java packages for modules
      final File fileDir = new File(fileFin, javaPackage);

      int grandfatherTabIndex = -1;
      String parentwhereclause = "";
      FieldsData auxFieldsData[] = null;
      if (parentTabIndex != -1 && allTabs != null && allTabs.length > 0) {
        parentwhereclause = FieldsData.selectParentWhereClause(pool, allTabs[parentTabIndex].tabid);
        final Vector<Object> vecParametersParent = new Vector<Object>();
        WadUtility.buildSQL(parentwhereclause, vecParametersParent);
        parentwhereclause = "";
        if (vecParametersParent.size() > 0) {
          ArrayList<String> usedParameters = new ArrayList<String>();
          for (int h = 0; h < vecParametersParent.size(); h++) {
            String strParam = WadUtility.getWhereParameter(vecParametersParent.get(h), false);

            if (!usedParameters.contains(strParam)) {
              usedParameters.add(strParam);
              parentwhereclause += ", Utility.getContext(this, vars, \"" + strParam
                  + "\", windowId)";
            }
          }
        }
        grandfatherTabIndex = parentTabId(allTabs, allTabs[parentTabIndex].tabid);
        auxFieldsData = FieldsData.parentsColumnName(pool,
            (grandfatherTabIndex != -1 ? allTabs[grandfatherTabIndex].tabid : ""),
            allTabs[parentTabIndex].tabid);
        if (grandfatherTabIndex != -1 && (auxFieldsData == null || auxFieldsData.length == 0)) {
          auxFieldsData = FieldsData.parentsColumnReal(pool, allTabs[grandfatherTabIndex].tabid,
              allTabs[parentTabIndex].tabid);
        }
      }
      if (auxFieldsData != null && auxFieldsData.length > 0)
        grandfatherField = auxFieldsData[0].name;
      auxFieldsData = null;
      String keyColumnName = "";
      boolean isSecondaryKey = false;
      final FieldsData[] dataKey = FieldsData.keyColumnName(pool, tabsData.tabid);
      if (dataKey != null && dataKey.length > 0) {
        keyColumnName = dataKey[0].name;
        isSecondaryKey = dataKey[0].issecondarykey.equals("Y");
      }
      log4j.debug("KeyColumnName: " + keyColumnName);
      String strProcess = "", strDirectPrint = "";
      if (!tabsData.adProcessId.equals("")) {
        strProcess = TabsData.processName(pool, tabsData.adProcessId);
        if (strProcess.indexOf("/") == -1)
          strProcess = "/" + FormatUtilities.replace(strProcess);
        strDirectPrint = TabsData.directPrint(pool, tabsData.adProcessId);
      }
      WADGrid gridControl = null;
      {
        final Properties gridProps = new Properties();
        gridProps.setProperty("id", "grid");
        gridProps.setProperty("NumRows", "20");
        gridProps.setProperty("width", "99%");
        gridProps.setProperty("ShowLineNumbers", "true");
        gridProps.setProperty("editable", "false");
        gridProps.setProperty("sortable", "true");
        gridProps.setProperty("deleteable", (tabsData.uipattern.equals("STD") ? "true" : "false"));
        gridProps.setProperty("onScrollFunction", "updateHeader");
        gridProps.setProperty("onLoadFunction", "onGridLoadDo");
        gridProps.setProperty("AD_Window_ID", tabsData.key);
        gridProps.setProperty("AD_Tab_ID", tabsData.tabid);
        gridProps.setProperty("ColumnName", keyColumnName);
        gridProps.setProperty("inpKeyName", "inp" + Sqlc.TransformaNombreColumna(keyColumnName));
        gridControl = new WADGrid(gridProps);
      }
      if (tabsData.issorttab.equals("Y")) {
        /************************************************
         * XSQL of the SORT TAB
         *************************************************/
        processTabXSQLSortTab(parentsFieldsData, fileDir, tabsData.tabid, tabName, tableName,
            windowName, keyColumnName, tabsData.adColumnsortorderId, tabsData.adColumnsortyesnoId,
            vecParameters, vecTableParameters, tabsData.javapackage, strWhere.toString(),
            strOrder.toString());
        /************************************************
         * JAVA of the SORT TAB
         *************************************************/
        processTabJavaSortTab(parentsFieldsData, fileDir, tabsData.tabid, tabName, tableName,
            windowName, keyColumnName, strTables.toString(), strOrder.toString(),
            strWhere.toString(), vecFields, isSOTrx, allTabs, tabsData.key, tabsData.accesslevel,
            selCol, isSecondaryKey, grandfatherField, tabsData.tablevel, tabsData.tableId,
            tabsData.windowtype, tabsData.adColumnsortorderId, whereClauseParams,
            parentwhereclause, strProcess, strDirectPrint, !tabsData.uipattern.equals("STD"),
            vecParameters, vecTableParameters, tabsData.javapackage, tabsData.tabmodule);
        /************************************************
         * XML of the SORT TAB
         *************************************************/
        processTabXmlSortTab(parentsFieldsData, fileDir, tabsData.tabid, tabName, keyColumnName);
        /************************************************
         * HTML of the SORT TAB
         *************************************************/
        processTabHtmlSortTab(parentsFieldsData, fileDir, tabsData.tabid, tabName,
            tabsData.realwindowname, keyColumnName, tabNamePresentation, allTabs, strProcess,
            strDirectPrint, windowName, "");
      } else {
        /************************************************
         * JAVA
         *************************************************/
        processTabJava(efd, efdauxiliar, parentsFieldsData, fileDir, tabsData.tabid, tabName,
            tableName, windowName, keyColumnName, strTables.toString(), strOrder.toString(),
            strWhere.toString(), tabsData.filterclause, vecFields, vecParameters, isSOTrx, allTabs,
            tabsData.key, tabsData.accesslevel, selCol, isSecondaryKey, grandfatherField,
            tabsData.tablevel, tabsData.tableId, tabsData.windowtype, tabsData.uipattern,
            whereClauseParams, parentwhereclause, tabsData.editreference, strProcess,
            strDirectPrint, vecTableParameters, fieldsData, gridControl, tabsData.javapackage,
            "Y".equals(tabsData.isdeleteable), tabsData.tabmodule);

        /************************************************
         * XSQL
         *************************************************/
        processTabXSQL(parentsFieldsData, fileDir, tabsData.tabid, tabName, tableName, windowName,
            keyColumnName, strFields.toString(), strTables.toString(), strOrder.toString(),
            strWhere.toString(), vecParameters, tabsData.filterclause, selCol, tabsData.tablevel,
            tabsData.windowtype, vecTableParameters, fieldsData, isSecondaryKey,
            tabsData.javapackage, vecFieldParameters);

        /************************************************
         * JAVA of the combo reloads
         *************************************************/
        processTabComboReloads(fileFinReloads, tabsData.tabid, parentsFieldsData, vecFields,
            isSOTrx, tabsData.accesslevel);

        /************************************************
         * XML in Relation view
         *************************************************/
        processTabXmlRelation(parentsFieldsData, fileDir, tabsData.tabid, tabName, keyColumnName,
            gridControl);

        /************************************************
         * HTML in Relation view
         *************************************************/
        processTabHtmlRelation(parentsFieldsData, fileDir, tabsData.tabid, tabName, keyColumnName,
            tabsData.uipattern.equals("RO"), gridControl, false, "", tabNamePresentation,
            tabsData.tableId, tabsData.accesslevel);

        /************************************************
         * XML in Edition view
         *************************************************/
        processTabXmlEdition(fileDir, tabsData.tabid, tabName, tabsData.key,
            tabsData.uipattern.equals("RO"), efd, efdauxiliar, isSecondaryKey);

        /************************************************
         * HTML in Edition view
         *************************************************/
        processTabHtmlEdition(efd, efdauxiliar, fileDir, tabsData.tabid, tabName, keyColumnName,
            tabNamePresentation, tabsData.key, parentsFieldsData, vecFields,
            tabsData.uipattern.equals("RO"), isSOTrx, tabsData.tableId, PIXEL_TO_LENGTH, "", true,
            isSecondaryKey);
        processTabHtmlEdition(efd, efdauxiliar, fileDir, tabsData.tabid, tabName, keyColumnName,
            tabNamePresentation, tabsData.key, parentsFieldsData, vecFields,
            tabsData.uipattern.equals("RO"), isSOTrx, tabsData.tableId, PIXEL_TO_LENGTH, "", false,
            isSecondaryKey);
      }

    } catch (final ServletException e) {
      e.printStackTrace();
      log4j.error("Problem of ServletException in the file: " + tabsData.tabid);
    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem at close of the file: " + tabsData.tabid);
    } catch (final Exception e) {
      e.printStackTrace();
      log4j.error("Problem at close of the file: " + tabsData.tabid);
    }
  }

  /**
   * Generates all the info to build the selection columns structure.
   * 
   * @param selCol
   *          The array with the info of the selection columns.
   * @param tableName
   *          The name of the selection column's table.
   * @return Array with the selection columns info.
   */
  private EditionFieldsData[] processSelCol(EditionFieldsData[] selCol, String tableName) {
    final Vector<Object> vecAuxSelCol = new Vector<Object>(0);
    final Vector<Object> vecSelCol = new Vector<Object>(0);
    if (selCol != null) {
      for (int i = 0; i < selCol.length; i++) {

        selCol[i].htmltext = "strParam" + selCol[i].columnname + ".equals(\"\")";
        selCol[i].columnnameinp = FormatUtilities.replace(selCol[i].columnname);
        WADControl control = WadUtility.getWadControlClass(pool, selCol[i].reference,
            selCol[i].referencevalue);
        control.processSelCol(tableName, selCol[i], vecAuxSelCol);

        vecSelCol.addElement(selCol[i]);
      }
      for (int i = 0; i < vecAuxSelCol.size(); i++)
        vecSelCol.addElement(vecAuxSelCol.elementAt(i));
      selCol = new EditionFieldsData[vecSelCol.size()];
      vecSelCol.copyInto(selCol);
    }
    return selCol;
  }

  /**
   * Generates the structure for the query fields.
   * 
   * @param parentsFieldsData
   *          Array with the parents fields.
   * @param strTab
   *          The id of the tab.
   * @param vecFields
   *          Vector of query fields (select fields).
   * @param vecTables
   *          Vector of query tables (from tables).
   * @param vecWhere
   *          Vector of where clauses.
   * @param vecOrder
   *          Vector of order clauses.
   * @param vecParameters
   *          Vector of query parameters.
   * @param tableName
   *          The name of the table.
   * @param windowType
   *          The type of window.
   * @param tablevel
   *          The tab level.
   * @param vecTableParameters
   *          Vector of the from clause parameters.
   * @param fieldsDataSelectAux
   *          Array with the fields of the tab.
   * @param vecFieldParameters
   * @throws ServletException
   * @throws IOException
   */
  private void processTable(FieldsData[] parentsFieldsData, String strTab,
      Vector<Object> vecFields, Vector<Object> vecTables, Vector<Object> vecWhere,
      Vector<Object> vecOrder, Vector<Object> vecParameters, String tableName, String windowType,
      String tablevel, Vector<Object> vecTableParameters, FieldsData[] fieldsDataSelectAux,
      Vector<String> vecFieldParameters) throws ServletException, IOException {
    int ilist = 0;
    final int itable = 0;
    final Vector<Object> vecCounters = new Vector<Object>();
    final Vector<Object> vecOrderAux = new Vector<Object>();
    vecCounters.addElement(Integer.toString(itable));
    vecCounters.addElement(Integer.toString(ilist));
    FieldsData[] fieldsData = null;
    fieldsData = copyarray(fieldsDataSelectAux);
    for (int i = 0; i < fieldsData.length; i++) {
      if (!fieldsData[i].columnname.equalsIgnoreCase("Created")
          && !fieldsData[i].columnname.equalsIgnoreCase("CreatedBy")
          && !fieldsData[i].columnname.equalsIgnoreCase("Updated")
          && !fieldsData[i].columnname.equalsIgnoreCase("UpdatedBy")) {

        // hardcoded these special cases
        if (fieldsData[i].reference.equals("24")) {
          vecFields.addElement("TO_CHAR(" + tableName + "." + fieldsData[i].name
              + ", 'HH24:MI:SS') AS " + fieldsData[i].name);
        } else if (fieldsData[i].reference.equals("20")) {
          vecFields.addElement("COALESCE(" + tableName + "." + fieldsData[i].name + ", 'N') AS "
              + fieldsData[i].name);
        } else if (fieldsData[i].reference.equals("16")) { // datetime
          vecFields.addElement("TO_CHAR(" + tableName + "." + fieldsData[i].name + ", ?) AS "
              + fieldsData[i].name);
          vecFieldParameters.addElement("<Parameter name=\"dateTimeFormat\"/>");
        } else {
          vecFields.addElement(tableName + "." + fieldsData[i].name);
        }

        WADControl control = WadUtility.getWadControlClass(pool, fieldsData[i].reference,
            fieldsData[i].referencevalue);
        control.processTable(strTab, vecFields, vecTables, vecWhere, vecOrderAux, vecParameters,
            tableName, vecTableParameters, fieldsData[i], vecFieldParameters, vecCounters);
      }
    }
    final FieldsData sfd1[] = FieldsData.selectSequence(pool, strTab);
    if (sfd1 != null && sfd1.length > 0) {
      for (int i = 0; i < sfd1.length; i++) {
        final String aux = findOrderVector(vecOrderAux, sfd1[i].name);
        if (aux != null && aux.length() > 0)
          vecOrder.addElement(aux);
      }
    }
  }

  /**
   * Searchs a field in the order vector and returns the column name.
   * 
   * @param vecOrder
   *          Vector with the order fields
   * @param name
   *          The name of the field to find
   * @return String with the name of the column.
   */
  private String findOrderVector(Vector<Object> vecOrder, String name) {
    if (vecOrder.size() == 0 || name.equals(""))
      return "";
    for (int i = 0; i < vecOrder.size(); i++) {
      final String[] aux = (String[]) vecOrder.elementAt(i);
      if (aux[0].equalsIgnoreCase(name))
        return aux[1];
    }
    return "";
  }

  /**
   * Generates the java files for a sort tab type.
   * 
   * @param parentsFieldsData
   *          Array with the parents fields.
   * @param fileDir
   *          Path where is gonna be created the file.
   * @param strTab
   *          The id of the tab.
   * @param tabName
   *          The name of the tab.
   * @param tableName
   *          The name of the tab's table.
   * @param windowName
   *          The name of the window.
   * @param keyColumnName
   *          The name of the key column.
   * @param strTables
   *          String with from clause of the query.
   * @param strOrder
   *          String with the order clause of the query.
   * @param strWhere
   *          String with the where clause of the query.
   * @param vecFields
   *          Vector with the fields of the query.
   * @param isSOTr
   *          String that indicates if is a Sales Order tab or not (Y | N).
   * @param allTabs
   *          Array with the tabs.
   * @param strWindow
   *          The window id.
   * @param accesslevel
   *          The access level defined for this tab.
   * @param selCol
   *          Array with selection columns.
   * @param isSecondaryKey
   *          Boolean that indicates if the tab key is a secondary key.
   * @param grandfatherField
   *          The grnadfather field of this tab.
   * @param tablevel
   *          The tab level.
   * @param tableId
   *          The id of the tab's table.
   * @param windowType
   *          The type of window.
   * @param strColumnSortOrderId
   *          The id of the column defined for the sort order.
   * @param whereClauseParams
   *          Array with the where clause's parameters.
   * @param parentwhereclause
   *          The where clause of the parent tab.
   * @param strProcess
   *          The id of the process associated to the tab.
   * @param strDirectPrint
   *          If is a direct print process (Y | N).
   * @param strReadOnly
   *          If is a readonly tab (Y | N)
   * @param vecParametersTop
   *          Array of query's parameters for the where clause.
   * @param vecTableParametersTop
   *          Array of query's parameters for from clause.
   * @param tabmodule
   * @throws ServletException
   * @throws IOException
   */
  private void processTabJavaSortTab(FieldsData[] parentsFieldsData, File fileDir, String strTab,
      String tabName, String tableName, String windowName, String keyColumnName, String strTables,
      String strOrder, String strWhere, Vector<Object> vecFields, String isSOTrx,
      TabsData[] allTabs, String strWindow, String accesslevel, EditionFieldsData[] selCol,
      boolean isSecondaryKey, String grandfatherField, String tablevel, String tableId,
      String windowType, String strColumnSortOrderId, String whereClauseParams,
      String parentwhereclause, String strProcess, String strDirectPrint, boolean strReadOnly,
      Vector<Object> vecParametersTop, Vector<Object> vecTableParametersTop, String javaPackage,
      String tabmodule) throws ServletException, IOException {
    log4j.debug("Processing Sort Tab java: " + strTab + ", " + tabName);
    XmlDocument xmlDocument;
    final int parentTab = parentTabId(allTabs, strTab);
    final String hasTree = TableLinkData.hasTree(pool, strTab);

    final String[] discard = { "", "", "" };
    if (parentsFieldsData == null || parentsFieldsData.length == 0) {
      discard[0] = "parent"; // remove the parent tags
    }
    // if (tableName.toUpperCase().startsWith("M_PRODUCT") ||
    // tableName.toUpperCase().startsWith("C_BP") ||
    // tableName.toUpperCase().startsWith("AD_ORG")) discard[1] = "org";
    if (grandfatherField.equals(""))
      discard[2] = "grandfather";
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/wad/javasourceSortTab", discard)
        .createXmlDocument();

    fileDir.mkdirs();
    xmlDocument.setParameter("class", tabName);
    xmlDocument.setParameter("package", (!javaPackage.equals("") ? javaPackage + "." : "")
        + windowName);
    xmlDocument.setParameter("path", (!javaPackage.equals("") ? javaPackage + "/" : "")
        + windowName);
    xmlDocument.setParameter("windowName", windowName);
    xmlDocument.setParameter("key", keyColumnName);
    xmlDocument.setParameter("grandfatherName", grandfatherField);
    xmlDocument.setParameter("ShowName", FieldsData.columnName(pool, strColumnSortOrderId));
    xmlDocument.setParameter("accessLevel", accesslevel);
    xmlDocument.setParameter("moduleId", tabmodule);
    if (parentsFieldsData.length > 0) {
      xmlDocument.setParameter("keyParent", parentsFieldsData[0].name);
      xmlDocument.setParameter("keyParentINP",
          Sqlc.TransformaNombreColumna(parentsFieldsData[0].name));
    }
    xmlDocument.setParameter("keyData", Sqlc.TransformaNombreColumna(keyColumnName));
    xmlDocument.setParameter("windowId", strWindow);
    xmlDocument.setParameter("tabId", strTab);
    xmlDocument.setParameter("whereClauseParams", whereClauseParams);
    xmlDocument.setParameter("parentwhereclause", parentwhereclause);
    xmlDocument.setParameter("reportPDF", strProcess);
    xmlDocument.setParameter("reportDirectPrint", strDirectPrint);
    xmlDocument.setParameter("hasTree", hasTree);
    xmlDocument.setParameter("isReadOnly", (strReadOnly ? "Y" : "N"));
    if (WadUtility.findField(vecFields, "adClientId"))
      xmlDocument.setParameter("clientId", "data.adClientId");
    else
      xmlDocument.setParameter("clientId",
          "Utility.getContext(this, vars, \"#AD_Client_ID\", windowId)");

    if (WadUtility.findField(vecFields, "adOrgId"))
      xmlDocument.setParameter("orgId", "data.adOrgId");
    else
      xmlDocument.setParameter("orgId", "Utility.getContext(this, vars, \"#AD_Org_ID\", windowId)");

    // Parent field language
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      final Vector<Object> vecCounters2 = new Vector<Object>();
      final Vector<Object> vecFields2 = new Vector<Object>();
      final Vector<Object> vecTable2 = new Vector<Object>();
      final Vector<Object> vecWhere2 = new Vector<Object>();
      final Vector<Object> vecParameters2 = new Vector<Object>();
      final Vector<Object> vecTableParameters2 = new Vector<Object>();
      vecCounters2.addElement("0");
      vecCounters2.addElement("0");
      WadUtility.columnIdentifier(pool, parentsFieldsData[0].tablename, true, parentsFieldsData[0],
          vecCounters2, true, vecFields2, vecTable2, vecWhere2, vecParameters2,
          vecTableParameters2, sqlDateFormat);

      xmlDocument.setParameter("parentLanguage", (vecParameters2.size() > 0 || vecTableParameters2
          .size() > 0) ? ", vars.getLanguage()" : "");
    }
    // Fields of the parent Session
    FieldsData[] fieldsParentSession = null;
    FieldsData[] auxiliarPFields = null;
    if (parentTab != -1) {
      xmlDocument.setParameter("parentClass", FormatUtilities.replace(allTabs[parentTab].tabname)
          + (allTabs[parentTab].tabmodule.equals("0") ? "" : allTabs[parentTab].tabid));
      fieldsParentSession = FieldsData.selectSession(pool, allTabs[parentTab].tabid);
      for (int i = 0; i < fieldsParentSession.length; i++) {
        fieldsParentSession[i].name = Sqlc.TransformaNombreColumna(fieldsParentSession[i].name);
        if (fieldsParentSession[i].reference.equals("20")) {
          fieldsParentSession[i].xmltext = ", \"N\"";
        } else {
          fieldsParentSession[i].xmltext = "";
        }
      }
      // Auxiliary fields of the parent
      final Vector<Object> vecAuxiliarPFields = new Vector<Object>();
      auxiliarPFields = FieldsData.selectAuxiliar(pool, "", allTabs[parentTab].tabid);
      if (auxiliarPFields != null) {
        for (int i = 0; i < auxiliarPFields.length; i++) {
          auxiliarPFields[i].columnname = Sqlc
              .TransformaNombreColumna(auxiliarPFields[i].columnname);
          if (auxiliarPFields[i].defaultvalue.toUpperCase().startsWith("@SQL=")) {
            auxiliarPFields[i].defaultvalue = FormatUtilities.replace(allTabs[parentTab].tabname)
                + "Data.selectAux"
                + auxiliarPFields[i].reference
                + "(this"
                + WadUtility.getWadContext(auxiliarPFields[i].defaultvalue, vecFields,
                    vecAuxiliarPFields, parentsFieldsData, false, isSOTrx, strWindow) + ")";
          } else if (auxiliarPFields[i].defaultvalue.indexOf("@") != -1) {
            auxiliarPFields[i].defaultvalue = WadUtility.getTextWadContext(
                auxiliarPFields[i].defaultvalue, vecFields, vecAuxiliarPFields, parentsFieldsData,
                false, isSOTrx, strWindow);
          } else {
            auxiliarPFields[i].defaultvalue = "\"" + auxiliarPFields[i].defaultvalue + "\"";
          }
          vecAuxiliarPFields.addElement(auxiliarPFields[i].name);
        }
      }
    } else {
      fieldsParentSession = FieldsData.set();
      auxiliarPFields = FieldsData.set();
    }

    xmlDocument.setData("structure8", fieldsParentSession);
    xmlDocument.setData("structure11", auxiliarPFields);
    WadUtility.writeFile(fileDir, tabName + ".java", xmlDocument.print());
  }

  /**
   * Generates the java files for a normal tab type.
   * 
   * @param allfields
   *          Array with the fields of the tab.
   * @param auxiliarsData
   *          Array with the auxiliar inputs for this tab.
   * @param parentsFieldsData
   *          Array with the parents fields for the tab.
   * @param fileDir
   *          Path where to build the file.
   * @param strTab
   *          The id of the tab.
   * @param tabName
   *          The name of the tab.
   * @param tableName
   *          The name of the tab's table.
   * @param windowName
   *          The name of the window.
   * @param keyColumnName
   *          The name of the key column.
   * @param strTables
   *          String with the from clause.
   * @param strOrder
   *          String with the order clause.
   * @param strWhere
   *          String with the where clause.
   * @param strFilter
   *          String with the filter clause.
   * @param vecFields
   *          Vector with the fields of the tab.
   * @param vecParametersTop
   *          Vector with parameters for the query.
   * @param isSOTrx
   *          String that indicates if is a Sales Order tab or not (Y | N).
   * @param allTabs
   *          Array with all the tabs.
   * @param strWindow
   *          The id of the window.
   * @param accesslevel
   *          The access level.
   * @param selCol
   *          Array with the selection columns.
   * @param isSecondaryKey
   *          Boolean that identifies if the key column is a secondary key.
   * @param grandfatherField
   *          The grandfather column of the tab.
   * @param tablevel
   *          The tab level.
   * @param tableId
   *          The id of the tab's table.
   * @param windowType
   *          The tab's window type.
   * @param uiPattern
   *          The patter for the tab.
   * @param whereClauseParams
   *          Array of where clause's parameters.
   * @param parentwhereclause
   *          The where clause for the parent tab.
   * @param editReference
   *          The id of the manual tab for the edition mode.
   * @param strProcess
   *          The id of the tab's process.
   * @param strDirectPrint
   *          If is a direct printing type process (Y | N).
   * @param vecTableParametersTop
   *          Vector with parameters for the from clause of the query.
   * @param fieldsDataSelectAux
   *          Array with the auxiliar inputs info
   * @param relationControl
   *          Object with the WADGrid control
   * @param tabmodule
   * @throws ServletException
   * @throws IOException
   */
  private void processTabJava(EditionFieldsData[] allfields, EditionFieldsData[] auxiliarsData,
      FieldsData[] parentsFieldsData, File fileDir, String strTab, String tabName,
      String tableName, String windowName, String keyColumnName, String strTables, String strOrder,
      String strWhere, String strFilter, Vector<Object> vecFields, Vector<Object> vecParametersTop,
      String isSOTrx, TabsData[] allTabs, String strWindow, String accesslevel,
      EditionFieldsData[] selCol, boolean isSecondaryKey, String grandfatherField, String tablevel,
      String tableId, String windowType, String uiPattern, String whereClauseParams,
      String parentwhereclause, String editReference, String strProcess, String strDirectPrint,
      Vector<Object> vecTableParametersTop, FieldsData[] fieldsDataSelectAux,
      WADControl relationControl, String javaPackage, boolean deleteable, String tabmodule)
      throws ServletException, IOException {
    log4j.debug("Processing java: " + strTab + ", " + tabName);
    XmlDocument xmlDocument;
    final boolean isHighVolumen = (FieldsData.isHighVolume(pool, strTab).equals("Y"));
    boolean hasParentsFields = true;
    final String createFromProcess = FieldsData.hasCreateFromButton(pool, strTab);
    final boolean hasCreateFrom = !createFromProcess.equals("0");
    final String postedProcess = FieldsData.hasPostedButton(pool, strTab);
    final boolean hasPosted = !postedProcess.equals("0");
    final String strhasEncryption = FieldsData.hasEncryptionFields(pool, strTab);
    final boolean hasEncryption = (strhasEncryption != null && !strhasEncryption.equals("0"));
    final int parentTab = parentTabId(allTabs, strTab);
    final String hasTree = TableLinkData.hasTree(pool, strTab);
    final boolean noPInstance = (ActionButtonRelationData.select(pool, strTab).length == 0);
    final boolean noActionButton = FieldsData.hasActionButton(pool, strTab).equals("0");
    final HashMap<String, String> shortcuts = new HashMap<String, String>();
    final StringBuffer dl = new StringBuffer();
    final StringBuffer readOnlyLogic = new StringBuffer();
    // Auxiliary fields of the window
    final Vector<Object> vecAuxiliarFields = new Vector<Object>();
    final FieldsData[] auxiliarFields = FieldsData.selectAuxiliar(pool, "", strTab);
    if (auxiliarFields != null) {
      for (int i = 0; i < auxiliarFields.length; i++) {
        auxiliarFields[i].columnname = Sqlc.TransformaNombreColumna(auxiliarFields[i].columnname);
        if (auxiliarFields[i].defaultvalue.toUpperCase().startsWith("@SQL=")) {
          auxiliarFields[i].defaultvalue = tabName
              + "Data.selectAux"
              + auxiliarFields[i].reference
              + "(this"
              + WadUtility.getWadContext(auxiliarFields[i].defaultvalue, vecFields,
                  vecAuxiliarFields, parentsFieldsData, false, isSOTrx, strWindow) + ")";
        } else if (auxiliarFields[i].defaultvalue.indexOf("@") != -1) {
          auxiliarFields[i].defaultvalue = WadUtility.getTextWadContext(
              auxiliarFields[i].defaultvalue, vecFields, vecAuxiliarFields, parentsFieldsData,
              false, isSOTrx, strWindow);
        } else {
          auxiliarFields[i].defaultvalue = "\"" + auxiliarFields[i].defaultvalue + "\"";
        }
        vecAuxiliarFields.addElement(auxiliarFields[i].name);
      }
    }

    {
      final Vector<Object> vecContext = new Vector<Object>();
      final Vector<Object> vecDL = new Vector<Object>();
      final EditionFieldsData[] efd = EditionFieldsData.selectDisplayLogic(pool, strTab);
      if (efd != null) {
        for (int i = 0; i < efd.length; i++)
          WadUtility.displayLogic(efd[i].displaylogic, vecDL, parentsFieldsData, vecAuxiliarFields,
              vecFields, strWindow, vecContext);
      }
      for (int i = 0; i < vecContext.size(); i++) {
        dl.append("var str");
        dl.append(FormatUtilities.replace(vecContext.elementAt(i).toString()));

        dl.append("=\\\"\" +");
        if (vecContext.elementAt(i).toString().equals("ShowAudit"))
          dl.append("(isNew?\"N\":");
        dl.append("Utility.getContext(this, vars, \"").append(vecContext.elementAt(i).toString())
            .append("\", windowId)");
        if (vecContext.elementAt(i).toString().equals("ShowAudit"))
          dl.append(")");

        dl.append(" + \"\\\";\\n");
      }
    }

    {
      final Vector<Object> vecContext = new Vector<Object>();
      final Vector<Object> vecDL = new Vector<Object>();
      final EditionFieldsData[] efd = EditionFieldsData.selectReadOnlyLogic(pool, strTab);
      if (efd != null) {
        for (int i = 0; i < efd.length; i++)
          WadUtility.displayLogic(efd[i].readonlylogic, vecDL, parentsFieldsData,
              vecAuxiliarFields, vecFields, strWindow, vecContext);
      }
      for (int i = 0; i < vecContext.size(); i++) {
        readOnlyLogic.append("var str");
        readOnlyLogic.append(FormatUtilities.replace(vecContext.elementAt(i).toString()));
        readOnlyLogic.append("=\\\"\" + Utility.getContext(this, vars, \"");
        readOnlyLogic.append(vecContext.elementAt(i).toString());
        readOnlyLogic.append("\", windowId) + \"\\\";\\n");
      }
    }

    final String[] discard = { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "hasReference", "", "", "", "", "", "", "", "hasOrgKey", "", "", "", "" };

    if (parentsFieldsData == null || parentsFieldsData.length == 0) {
      discard[0] = "parent"; // remove the parent tags
      hasParentsFields = false;
    } else if (!"Y".equals(parentsFieldsData[0].issecondarykey)) {
      discard[32] = "parentSecondaryKey";
    }

    if (tableName.toUpperCase().endsWith("_ACCESS")) {
      discard[18] = "client";
      discard[1] = "org";
    } // else if (tableName.toUpperCase().startsWith("M_PRODUCT") ||
    // tableName.toUpperCase().startsWith("C_BP") ||
    // tableName.toUpperCase().startsWith("AD_ORG")) discard[1] = "org";
    if (dl.toString().equals(""))
      discard[2] = "selDisplayLogic";
    if (!isHighVolumen || !tablevel.equals("0")) {
      discard[3] = "sectionIsHighVolume";
    }
    if (selCol == null || selCol.length == 0) {
      discard[4] = "sectionIsHighVolume2";
      discard[5] = "sectionIsHighVolume3";
      discard[6] = "sectionIsHighVolume5";
      discard[7] = "sectionIsHighVolumeEdit";
      discard[8] = "sectionIsHighVolume2Edit";
      discard[9] = "sectionIsHighVolume3Edit";
      discard[14] = "sectionIsHighVolume4";
    }
    if (isHighVolumen)
      discard[10] = "sectionNotIsHighVolume";
    if (isSecondaryKey)
      discard[11] = "keySequence";
    else
      discard[24] = "withSecondaryKey";
    if (grandfatherField.equals(""))
      discard[12] = "grandfather";
    if (!hasCreateFrom)
      discard[13] = "sectionCreateFrom";
    if (!hasPosted)
      discard[19] = "sectionPosted";
    if (!(windowType.equalsIgnoreCase("T") && tablevel.equals("0")))
      discard[15] = "isTransactional";
    if (strFilter.trim().equals(""))
      discard[16] = "sectionFilter";
    if (uiPattern.equals("STD"))
      discard[17] = "sectionReadOnly";
    if (!hasEncryption)
      discard[20] = "encryptionsFields";
    if (!editReference.equals(""))
      discard[21] = "NothasReference";
    if ((noPInstance) && (noActionButton))
      discard[22] = "hasAdPInstance";
    if (noActionButton)
      discard[23] = "hasAdActionButton";

    if (FieldsData.hasButtonFixed(pool, strTab).equals("0"))
      discard[26] = "buttonFixed";
    if (strWindow.equals("110"))
      discard[27] = "sectionOrganizationCheck";
    discard[28] = "sameParent";
    if (!(parentsFieldsData == null || parentsFieldsData.length == 0)
        && (keyColumnName.equals(parentsFieldsData[0].name)))
      discard[28] = "";
    if (isSecondaryKey && !EditionFieldsData.isOrgKey(pool, strTab).equals("0")
        && !strTab.equals("170"))
      discard[29] = "";

    if (strWindow.equals("250"))
      discard[30] = "refreshTabParentSession"; // TODO: This fixes
    // [1879633] and shoudn't
    // be necessary in r2.5x
    // because of new PKs

    // Obtain action buttons processes to be called from tab trough buttons
    final ActionButtonRelationData[] actBtns = WadActionButton.buildActionButtonCall(pool, strTab,
        tabName, keyColumnName, isSOTrx, strWindow);
    final ActionButtonRelationData[] actBtnsJava = WadActionButton.buildActionButtonCallJava(pool,
        strTab, tabName, keyColumnName, isSOTrx, strWindow);

    if ((actBtns == null || actBtns.length == 0)
        && (actBtnsJava == null || actBtnsJava.length == 0)) {
      // No action buttons, service method is not neccessary
      discard[31] = "discardService";
    }

    if (parentsFieldsData.length > 0) {
      String parentTableName = WadData.tabTableName(pool, parentsFieldsData[0].adTabId);
      if (parentTableName != null && parentTableName.toUpperCase().endsWith("_ACCESS")) {
        discard[33] = "parentAccess";
      }
    }

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/wad/javasource", discard)
        .createXmlDocument();

    fileDir.mkdirs();
    xmlDocument.setParameter("class", tabName);
    xmlDocument.setParameter("package", (!javaPackage.equals("") ? javaPackage + "." : "")
        + windowName);
    xmlDocument.setParameter("path", (!javaPackage.equals("") ? javaPackage.replace(".", "/") + "/"
        : "") + windowName);
    xmlDocument.setParameter("windowName", windowName);
    xmlDocument.setParameter("key", keyColumnName);
    xmlDocument.setParameter("from", generateStaticWhere(strTables, vecTableParametersTop));
    xmlDocument.setParameter("order", (!strOrder.equals("") ? strOrder.substring(9) : strOrder));
    final Vector<Object> vecTotalParameters = new Vector<Object>();
    for (int i = 0; i < vecTableParametersTop.size(); i++) {
      vecTotalParameters.addElement(vecTableParametersTop.elementAt(i));
    }
    for (int i = 0; i < vecParametersTop.size(); i++) {
      vecTotalParameters.addElement(vecParametersTop.elementAt(i));
    }
    xmlDocument.setParameter("where", generateStaticWhere(strWhere, vecParametersTop));
    xmlDocument.setParameter("filter", strFilter);
    xmlDocument.setParameter("displayLogic", dl.toString());
    xmlDocument.setParameter("readOnlyLogic", readOnlyLogic.toString());
    xmlDocument.setParameter("grandfatherName", grandfatherField);
    xmlDocument.setParameter("defaultView",
        (FieldsData.isSingleRow(pool, strTab).equals("Y") ? "EDIT" : "RELATION"));
    xmlDocument.setParameter("whereClauseParams", whereClauseParams);
    xmlDocument.setParameter("parentwhereclause", parentwhereclause);
    xmlDocument.setParameter("reportPDF", strProcess);
    xmlDocument.setParameter("reportDirectPrint", strDirectPrint);
    xmlDocument.setParameter("relationControl", relationControl.toJava());

    xmlDocument.setParameter("deleteable", deleteable ? "true" : "false");

    if (parentsFieldsData.length > 0) {

      xmlDocument.setParameter("keyParent", parentsFieldsData[0].name);
      xmlDocument.setParameter("keyParentColName",
          "Y".equals(parentsFieldsData[0].issecondarykey) ? "arentId" : parentsFieldsData[0].name);

      xmlDocument.setParameter("keyParentSimple", WadUtility.columnName(parentsFieldsData[0].name,
          parentsFieldsData[0].tablemodule, parentsFieldsData[0].columnmodule));
      xmlDocument.setParameter("keyParentT",
          Sqlc.TransformaNombreColumna(parentsFieldsData[0].name));

      String keyTabName;
      if ("Y".equals(parentsFieldsData[0].issecondarykey)) {
        keyTabName = parentsFieldsData[0].tablename + "_ID";
      } else {
        keyTabName = parentsFieldsData[0].name;
      }

      xmlDocument.setParameter("keyParentTabINP", Sqlc.TransformaNombreColumna(WadUtility
          .columnName(keyTabName, parentsFieldsData[0].tablemodule,
              parentsFieldsData[0].columnmodule)));
      xmlDocument.setParameter("keyParentINP", Sqlc.TransformaNombreColumna(WadUtility.columnName(
          parentsFieldsData[0].name, parentsFieldsData[0].tablemodule,
          parentsFieldsData[0].columnmodule)));

      xmlDocument.setParameter("parentTab", parentsFieldsData[0].adTabId);
      xmlDocument.setParameter("parentTabName", parentsFieldsData[0].parentTabName);
      xmlDocument.setParameter("parentFieldID", parentsFieldsData[0].adFieldId);
    }
    xmlDocument.setParameter("keyData", Sqlc.TransformaNombreColumna(keyColumnName));
    xmlDocument.setParameter("table", tableName);
    xmlDocument.setParameter("windowId", strWindow);
    xmlDocument.setParameter("accessLevel", accesslevel);
    xmlDocument.setParameter("moduleId", tabmodule);
    xmlDocument.setParameter("tabId", strTab);
    xmlDocument.setParameter("tableId", tableId);
    xmlDocument.setParameter("createFromProcessId",
        ((Integer.valueOf(createFromProcess).intValue() > 0) ? createFromProcess : ""));
    xmlDocument.setParameter("postedProcessId",
        ((Integer.valueOf(postedProcess).intValue() > 0) ? postedProcess : ""));
    xmlDocument.setParameter("editReference", TabsData.formClassName(pool, editReference));
    xmlDocument.setParameter("hasTree", hasTree);
    // read only for relation toolbar: it is the same for Single Record and Read Only
    xmlDocument.setParameter("isReadOnly", uiPattern.equals("STD") ? "false" : "true");

    // UI Patter for edition toolbar
    xmlDocument.setParameter("uiPattern", uiPattern);

    String strParamHighVolume = "", strHighVolumeComp = "";

    String internalFilter = "(tableSQL.hasInternalFilter()";
    String filter1 = "";
    String filter2 = "";

    if (selCol != null) {
      for (int i = 0; i < selCol.length; i++) {
        if (filter1.isEmpty()) {
          internalFilter += " && ";
        } else {
          filter1 += " && ";
        }
        filter1 += "(\"\").equals(strParam" + selCol[i].columnname + ")";
        filter2 += " || !((\"\").equals(strParam" + selCol[i].columnname
            + ") || (\"%\").equals(strParam" + selCol[i].columnname + ")) ";

        strParamHighVolume += "String strParam" + selCol[i].columnname

        + " = vars.getSessionValue(tabId + \"|param" + selCol[i].columnname + "\");\n";
        strHighVolumeComp += selCol[i].xmltext;
      }
    }

    xmlDocument.setParameter("searchName", internalFilter + filter1 + ")" + filter2);
    xmlDocument.setParameter("searchNameHighVolume", filter1);
    xmlDocument.setParameter("searchVariables", strParamHighVolume);
    xmlDocument.setParameter("searchComparations", strHighVolumeComp);

    if (WadUtility.findField(vecFields, "adClientId"))
      xmlDocument.setParameter("clientId", "data.adClientId");
    else
      xmlDocument.setParameter("clientId",
          "Utility.getContext(this, vars, \"#AD_Client_ID\", windowId)");

    if (WadUtility.findField(vecFields, "adOrgId"))
      xmlDocument.setParameter("orgId", "data.adOrgId");
    else
      xmlDocument.setParameter("orgId", "Utility.getContext(this, vars, \"#AD_Org_ID\", windowId)");

    // Parent field language
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      final Vector<Object> vecCounters2 = new Vector<Object>();
      final Vector<Object> vecFields2 = new Vector<Object>();
      final Vector<Object> vecTable2 = new Vector<Object>();
      final Vector<Object> vecWhere2 = new Vector<Object>();
      final Vector<Object> vecParameters2 = new Vector<Object>();
      final Vector<Object> vecTableParameters2 = new Vector<Object>();
      vecCounters2.addElement("0");
      vecCounters2.addElement("0");
      WadUtility.columnIdentifier(pool, parentsFieldsData[0].tablename, true, parentsFieldsData[0],
          vecCounters2, true, vecFields2, vecTable2, vecWhere2, vecParameters2,
          vecTableParameters2, sqlDateFormat);

      xmlDocument.setParameter("parentLanguage", (vecParameters2.size() > 0 || vecTableParameters2
          .size() > 0) ? ", vars.getLanguage()" : "");
    }
    FieldsData[] fieldsData = null;
    boolean defaultValue;
    {
      final Vector<Object> vecFieldsSelect = new Vector<Object>();
      FieldsData[] fieldsData1 = null;
      fieldsData1 = copyarray(fieldsDataSelectAux);
      for (int i = 0; i < fieldsData1.length; i++) {
        if (!fieldsData1[i].name.equalsIgnoreCase("Created")
            && !fieldsData1[i].name.equalsIgnoreCase("CreatedBy")
            && !fieldsData1[i].name.equalsIgnoreCase("Updated")
            && !fieldsData1[i].name.equalsIgnoreCase("UpdatedBy")) {
          fieldsData1[i].name = Sqlc.TransformaNombreColumna(fieldsData1[i].name);
          fieldsData1[i].columnname = fieldsData1[i].name;
          defaultValue = false;

          WADControl control = WadUtility.getWadControlClass(pool, fieldsData1[i].reference,
              fieldsData1[i].adReferenceValueId);

          if (fieldsData1[i].reference.equals("20")) {
            fieldsData1[i].xmltext = ", \"N\"";
            defaultValue = true;
          } else {
            fieldsData1[i].xmltext = "";
          }

          if (fieldsData1[i].iskey.equals("Y")) {
            fieldsData1[i].xmltext = ", windowId + \"|" + fieldsData1[i].realname + "\"";
            fieldsData1[i].type = "RequestGlobalVariable";
          } else if (fieldsData1[i].issessionattr.equals("Y")) {
            if (control.isNumericType()) {
              fieldsData1[i].xmltext = ", vars.getSessionValue(windowId + \"|"
                  + fieldsData1[i].realname + "\")";
            } else {
              fieldsData1[i].xmltext = ", windowId + \"|" + fieldsData1[i].realname + "\"";
            }
            if (fieldsData1[i].reference.equals("20"))
              fieldsData1[i].xmltext += ", \"N\"";
            if (fieldsData1[i].required.equals("Y")
                && !fieldsData1[i].columnname.equalsIgnoreCase("Value") && !defaultValue) {
              if (fieldsData1[i].reference.equals("20"))
                fieldsData1[i].type = "RequiredInputGlobalVariable";
              else
                fieldsData1[i].type = "RequiredGlobalVariable";
            } else {
              if (fieldsData1[i].reference.equals("20"))
                fieldsData1[i].type = "RequiredInputGlobalVariable";
              else
                fieldsData1[i].type = "RequestGlobalVariable";
            }
          } else if (fieldsData1[i].required.equals("Y")
              && !fieldsData1[i].columnname.equalsIgnoreCase("Value") && !defaultValue) {
            fieldsData1[i].type = "RequiredStringParameter";
          }

          if (control.isNumericType()) {
            if (fieldsData1[i].required.equals("Y")) {
              fieldsData1[i].type = "RequiredNumericParameter";
            } else {
              fieldsData1[i].type = "NumericParameter";
            }
          }

          if (control.isNumericType()) {
            fieldsData1[i].trytext = " try { ";
            fieldsData1[i].catchtext = " } catch (ServletException paramEx) { ex = paramEx; } ";
          } else {
            fieldsData1[i].trytext = "";
            fieldsData1[i].catchtext = "";
          }

          if (fieldsData1[i].iscolumnencrypted.equals("Y")
              && fieldsData1[i].isdesencryptable.equals("Y")) {
            fieldsData1[i].htmltext = "FormatUtilities.encryptDecrypt(";
            fieldsData1[i].htmltexttrl = ", true)";
          } else if (fieldsData1[i].iscolumnencrypted.equals("Y")
              && fieldsData1[i].isdesencryptable.equals("N")) {
            fieldsData1[i].htmltext = "FormatUtilities.sha1Base64(";
            fieldsData1[i].htmltexttrl = ")";
          }
          vecFieldsSelect.addElement(fieldsData1[i]);
          if (control.has2UIFields() && fieldsData1[i].isdisplayed.equals("Y")) {
            FieldsData fieldsData2 = null;
            fieldsData2 = copyarrayElement(fieldsData1[i]);
            fieldsData2.name += "r";// (WadUtility.isSearchType(fieldsData1[i].reference)?"D":"r");
            fieldsData2.columnname += "_R";
            fieldsData2.type = "StringParameter";
            fieldsData2.xmltext = "";
            vecFieldsSelect.addElement(fieldsData2);
          }
        }
      }
      fieldsData = new FieldsData[vecFieldsSelect.size()];
      vecFieldsSelect.copyInto(fieldsData);
    }

    // Campos del Session actual
    // Fields of the current Session
    final FieldsData[] fieldsSession = FieldsData.selectSession(pool, strTab);
    if (fieldsSession != null) {
      for (int i = 0; i < fieldsSession.length; i++) {
        fieldsSession[i].name = Sqlc.TransformaNombreColumna(fieldsSession[i].name);
        if (fieldsSession[i].reference.equals("20")) {
          fieldsSession[i].xmltext = ", \"N\"";
        } else {
          fieldsSession[i].xmltext = "";
        }
      }
    }

    // Fields of the parent Session
    FieldsData[] fieldsParentSession = null;
    FieldsData[] auxiliarPFields = null;
    if (parentTab != -1) {
      xmlDocument.setParameter("parentClass", FormatUtilities.replace(allTabs[parentTab].tabname)
          + (allTabs[parentTab].tabmodule.equals("0") ? "" : allTabs[parentTab].tabid));
      fieldsParentSession = FieldsData.selectSession(pool, allTabs[parentTab].tabid);
      for (int i = 0; i < fieldsParentSession.length; i++) {
        fieldsParentSession[i].name = Sqlc.TransformaNombreColumna(fieldsParentSession[i].name);
        if (fieldsParentSession[i].reference.equals("20")) {
          fieldsParentSession[i].xmltext = ", \"N\"";
        } else {
          fieldsParentSession[i].xmltext = "";
        }
      }
      // Auxiliary fields of the parent
      final Vector<Object> vecAuxiliarPFields = new Vector<Object>();
      auxiliarPFields = FieldsData.selectAuxiliar(pool, "", allTabs[parentTab].tabid);
      if (auxiliarPFields != null) {
        for (int i = 0; i < auxiliarPFields.length; i++) {
          auxiliarPFields[i].columnname = Sqlc
              .TransformaNombreColumna(auxiliarPFields[i].columnname);
          if (auxiliarPFields[i].defaultvalue.toUpperCase().startsWith("@SQL=")) {
            auxiliarPFields[i].defaultvalue = FormatUtilities.replace(allTabs[parentTab].tabname)
                + (allTabs[parentTab].tabmodule.equals("0") ? "" : allTabs[parentTab].tabid)
                + "Data.selectAux"
                + auxiliarPFields[i].reference
                + "(this"
                + WadUtility.getWadContext(auxiliarPFields[i].defaultvalue, vecFields,
                    vecAuxiliarPFields, parentsFieldsData, false, isSOTrx, strWindow) + ")";
          } else if (auxiliarPFields[i].defaultvalue.indexOf("@") != -1) {
            auxiliarPFields[i].defaultvalue = WadUtility.getTextWadContext(
                auxiliarPFields[i].defaultvalue, vecFields, vecAuxiliarPFields, parentsFieldsData,
                false, isSOTrx, strWindow);
          } else {
            auxiliarPFields[i].defaultvalue = "\"" + auxiliarPFields[i].defaultvalue + "\"";
          }
          vecAuxiliarPFields.addElement(auxiliarPFields[i].name);
        }
      }
    } else {
      fieldsParentSession = FieldsData.set();
      auxiliarPFields = FieldsData.set();
    }

    if (fieldsSession != null) {
      for (int i = 0; i < fieldsSession.length; i++) {
        if (!fieldsSession[i].columnname.equals("")) {
          fieldsSession[i].referencevalue += "_" + fieldsSession[i].columnname;
          fieldsSession[i].tablename = "TableDirValData";
          fieldsSession[i].whereclause = ", Utility.getContext(this, vars, \"#User_Org\", windowId), Utility.getContext(this, vars, \"#User_Client\", windowId)";
          fieldsSession[i].whereclause += WadUtility.getWadContext(fieldsSession[i].defaultvalue,
              vecFields, vecAuxiliarFields, parentsFieldsData, false, isSOTrx, strWindow);
          fieldsSession[i].whereclause += ", data[0]."
              + Sqlc.TransformaNombreColumna(fieldsSession[i].name);
        }
      }
    }

    {
      final FieldsData[] docsNoFields = FieldsData.selectDocumentsNo(pool, strTab);
      if (docsNoFields != null) {
        String field = "";
        for (int i = 0; i < docsNoFields.length; i++) {
          docsNoFields[i].columnname = Sqlc.TransformaNombreColumna(docsNoFields[i].columnname);
          docsNoFields[i].defaultvalue = "Utility.getDocumentNo(con, this, vars, windowId, \""
              + docsNoFields[i].nameref + "\", ";
          docsNoFields[i].realname = "Utility.getDocumentNo(con, this, vars, windowId, \""
              + docsNoFields[i].nameref + "\", ";
          field = WadUtility.findField(pool, allfields, auxiliarsData, "C_DocTypeTarget_ID");
          if (!field.equals("")) {
            docsNoFields[i].defaultvalue += "data[0]." + Sqlc.TransformaNombreColumna(field);
            docsNoFields[i].realname += "data." + Sqlc.TransformaNombreColumna(field);
          } else {
            docsNoFields[i].defaultvalue += "\"\"";
            docsNoFields[i].realname += "\"\"";
          }
          docsNoFields[i].defaultvalue += ", ";
          docsNoFields[i].realname += ", ";
          field = WadUtility.findField(pool, allfields, auxiliarsData, "C_DocType_ID");
          if (!field.equals("")) {
            docsNoFields[i].defaultvalue += "data[0]." + Sqlc.TransformaNombreColumna(field);
            docsNoFields[i].realname += "data." + Sqlc.TransformaNombreColumna(field);
          } else {
            docsNoFields[i].defaultvalue += "\"\"";
            docsNoFields[i].realname += "\"\"";
          }
          docsNoFields[i].defaultvalue += ", false, false)";
          docsNoFields[i].realname += ", false, true)";
        }
      }
      xmlDocument.setData("structure13", docsNoFields);

      final FieldsData[] docNoNoConnFileds = new FieldsData[docsNoFields.length];
      for (int i = 0; i < docsNoFields.length; i++) {
        docNoNoConnFileds[i] = new FieldsData();
        docNoNoConnFileds[i].columnname = docsNoFields[i].columnname;
        docNoNoConnFileds[i].realname = docsNoFields[i].realname;

        docNoNoConnFileds[i].defaultvalue = docsNoFields[i].defaultvalue.replace("(con,", "(");
      }

      xmlDocument.setData("structure12", docNoNoConnFileds);
      {
        final FieldsData[] docsIdentify = FieldsData.selectIdentify(pool, strTab);
        if (docsIdentify != null) {
          for (int i = 0; i < docsIdentify.length; i++) {
            if (docsNoFields == null || docsNoFields.length == 0) {
              docsIdentify[i].realname = "Utility.getDocumentNoConnection(con, this, vars.getClient(), \""
                  + docsIdentify[i].nameref + "\", true)";
              if (docsIdentify[i].issessionattr.equals("Y"))
                docsIdentify[i].realname += ";\nvars.setSessionValue(windowId + \"|"
                    + docsIdentify[i].columnname + "\", data."
                    + Sqlc.TransformaNombreColumna(docsIdentify[i].columnname) + ")";
            } else {
              docsIdentify[i].realname = "data." + docsNoFields[0].columnname;
            }
            docsIdentify[i].columnname = Sqlc.TransformaNombreColumna(docsIdentify[i].columnname);
          }
        }
        xmlDocument.setData("structure23", docsIdentify);
      }
    }

    xmlDocument.setData("structure1", fieldsData);

    xmlDocument.setData("structure7", auxiliarFields);
    xmlDocument.setData("structure27", auxiliarFields);
    xmlDocument.setData("structure8", fieldsParentSession);
    xmlDocument.setData("structure9", fieldsSession);
    xmlDocument.setData("structure10", auxiliarFields);
    xmlDocument.setData("structure11", auxiliarPFields);

    // process action buttons
    xmlDocument.setData("structure14", actBtns);
    xmlDocument.setData("structure15", actBtns);
    xmlDocument.setData("structure16", actBtns);
    xmlDocument.setData("structureActionBtnService", actBtns);

    // process standard UI java implemented buttons
    xmlDocument.setData("structure14java", actBtnsJava);
    xmlDocument.setData("structure15java", actBtnsJava);
    xmlDocument.setData("structure16java", actBtnsJava);
    xmlDocument.setData("structureActionBtnServiceJava", actBtnsJava);

    xmlDocument.setData("structure18", selCol);
    xmlDocument.setData("structure20", selCol);
    xmlDocument.setData("structure22", selCol);
    xmlDocument.setData("structure24", selCol);
    xmlDocument.setData("structure25", selCol);
    xmlDocument.setData("structure26", selCol);

    // Encrypted Fields
    {
      final FieldsData[] encryptedData = FieldsData.selectEncrypted(pool, strTab);
      if (encryptedData != null && encryptedData.length > 0) {
        for (int g = 0; g < encryptedData.length; g++) {
          encryptedData[g].realname = Sqlc.TransformaNombreColumna(encryptedData[g].realname);
          encryptedData[g].name = FormatUtilities.replace(encryptedData[g].name);
          if (encryptedData[g].isdesencryptable.equals("Y")) {
            encryptedData[g].xmlFormat = "encryptDecrypt";
            encryptedData[g].htmltext = ", true";
          } else {
            encryptedData[g].xmlFormat = "sha1Base64";
            encryptedData[g].htmltext = "";
          }
        }
      }
      xmlDocument.setData("structure32", encryptedData);
      xmlDocument.setData("structure33", encryptedData);
      xmlDocument.setData("structure34", encryptedData);
      xmlDocument.setData("structure35", encryptedData);
    }

    // Button Fields
    {
      final FieldsData[] buttonData = FieldsData.selectButton(pool, strTab);
      if (buttonData != null && buttonData.length > 0) {
        for (int g = 0; g < buttonData.length; g++) {
          buttonData[g].realname = Sqlc.TransformaNombreColumna(buttonData[g].realname);
        }
      }
      xmlDocument.setData("structure36", buttonData);
    }

    final StringBuffer strDefaultValues = new StringBuffer();
    final FieldsData sfd[] = FieldsData.selectDefaultValue(pool, "", strTab);
    int isSelect = 0;
    for (int i = 0; i < sfd.length; i++) {
      if (!hasParentsFields || !parentsFieldsData[0].name.equalsIgnoreCase(sfd[i].columnname)) {
        if (sfd[i].defaultvalue.toUpperCase().startsWith("@SQL=")) {
          sfd[i].defaultvalue = tabName
              + "Data.selectDef"
              + sfd[i].adcolumnid
              + "(this"
              + WadUtility.getWadContext(sfd[i].defaultvalue, vecFields, vecAuxiliarFields,
                  parentsFieldsData, true, isSOTrx, strWindow) + ")";
        } else if (sfd[i].columnname.equalsIgnoreCase("isActive")) {
          sfd[i].defaultvalue = "\"Y\"";
        } else if (sfd[i].accesslevel.equals("4")
            && (sfd[i].columnname.equalsIgnoreCase("AD_CLIENT_ID") || sfd[i].columnname
                .equalsIgnoreCase("AD_ORG_ID"))) {
          sfd[i].defaultvalue = "\"0\"";
        } else if (sfd[i].accesslevel.equals("6")
            && sfd[i].columnname.equalsIgnoreCase("AD_ORG_ID")) {
          sfd[i].defaultvalue = "\"0\"";
        } else if (!sfd[i].reference.equals("13")) { // ID
          sfd[i].defaultvalue = "Utility.getDefault(this, vars, \"" + sfd[i].columnname + "\", \""
              + WadUtility.toJavaString(sfd[i].defaultvalue) + "\", \"" + strWindow + "\", \""
              + WadUtility.getWadDefaultValue(pool, sfd[i]) + "\", dataField)";
        } else {
          sfd[i].defaultvalue = "\"\"";
        }
        if (!strDefaultValues.toString().equals("") || hasParentsFields)
          strDefaultValues.append(", ");
        strDefaultValues.append(sfd[i].defaultvalue);
      } else {
        sfd[i].defaultvalue = "strP" + sfd[i].columnname;
      }
      WADControl control = WadUtility.getWadControlClass(pool, sfd[i].reference,
          sfd[i].referencevalue);
      isSelect = control.addAdditionDefaulJavaFields(strDefaultValues, sfd[i], tabName, isSelect);
    }

    final StringBuffer controlsJavaSource = new StringBuffer();
    boolean needsComboTableData = false;
    for (int i = 0; i < allfields.length; i++) {
      WADControl auxControl = null;
      try {
        auxControl = WadUtility.getControl(pool, allfields[i], uiPattern.equals("RO"), tabName, "",
            xmlEngine, false, false, false, hasParentsFields);
      } catch (final Exception ex) {
        throw new ServletException(ex);
      }
      if ((!auxControl.toJava().equals("")) && (!needsComboTableData)) {
        needsComboTableData = true;
        controlsJavaSource.append("    try {\n      ComboTableData comboTableData = null;\n");
      }
      controlsJavaSource.append(auxControl.toJava()).append(
          (auxControl.toJava().equals("") ? "" : "\n"));

      if ((auxControl instanceof WADButton) && (auxControl.getData("IsDisplayed").equals("Y"))) {
        ((WADButton) auxControl).setShortcuts(shortcuts);
      }
    }

    // Shorcuts for buttons
    final FieldsData[] shortcutsAux = new FieldsData[shortcuts.size()];
    final Iterator<String> ik = shortcuts.keySet().iterator();
    for (int i = 0; i < shortcuts.size(); i++) {
      shortcutsAux[i] = new FieldsData();
      if (ik.hasNext())
        shortcutsAux[i].name = ik.next();
    }
    xmlDocument.setData("structure37", shortcutsAux);

    if (needsComboTableData)
      controlsJavaSource
          .append("    } catch (Exception ex) {\n      ex.printStackTrace();\n      throw new ServletException(ex);\n    }\n");
    xmlDocument.setParameter("controlsJavaCode", controlsJavaSource.toString());
    xmlDocument.setParameter("defaultValues", strDefaultValues.toString());
    WadUtility.writeFile(fileDir, tabName + ".java", xmlDocument.print());
  }

  /**
   * Generates the where with the params as java vars, to put it in the java file to be used in all
   * the internal searchs, like gotoFirstRow...
   * 
   * @param strWhere
   *          The tab's where clause
   * @param vecParameters
   *          Vector with the parameters for the where clause.
   * @return String with the new static where clause.
   */
  private String generateStaticWhere(String strWhere, Vector<Object> vecParameters) {
    final StringBuffer result = new StringBuffer();
    if (strWhere == null || strWhere.equals(""))
      return strWhere;
    int pos = strWhere.indexOf("?");
    int questNumber = 0;
    while (pos != -1) {
      result.append(strWhere.substring(0, pos));
      strWhere = strWhere.substring(pos + 1);
      String strParam = WadUtility.getWhereParameter(vecParameters.elementAt(questNumber), false);
      questNumber++;
      if (strParam.equalsIgnoreCase("paramLanguage"))
        result.append(" '\" + vars.getLanguage() + \"' ");
      else
        result.append(" '\" + Utility.getContext(this, vars, \"" + strParam
            + "\", windowId) + \"' ");
      pos = strWhere.indexOf("?");
    }
    ;
    result.append(strWhere);
    return result.toString();
  }

  /**
   * Generates the xsql file for a sort tab type.
   * 
   * @param parentsFieldsData
   *          Array with the parents fields.
   * @param fileDir
   *          Path where to generate the file
   * @param strTab
   *          Id of the tab.
   * @param tabName
   *          Name of the tab.
   * @param tableName
   *          Name of the tab's table.
   * @param windowName
   *          Name of the window.
   * @param keyColumnName
   *          Name of the key column.
   * @param strColumnSortOrderId
   *          Column that makes the sorting.
   * @param strColumnSortYNId
   *          Column to know if has to be in shown listbox.
   * @param vecParametersTop
   *          Vector with the where clause parameters.
   * @param vecTableParametersTop
   *          Vector with the from clause parameters.
   * @param whereClause
   * @param orderBy
   * @throws ServletException
   * @throws IOException
   */
  private void processTabXSQLSortTab(FieldsData[] parentsFieldsData, File fileDir, String strTab,
      String tabName, String tableName, String windowName, String keyColumnName,
      String strColumnSortOrderId, String strColumnSortYNId, Vector<Object> vecParametersTop,
      Vector<Object> vecTableParametersTop, String javaPackage, String whereClause, String orderBy)
      throws ServletException, IOException {
    log4j.debug("Processing Sort Tab xsql: " + strTab + ", " + tabName);
    XmlDocument xmlDocumentXsql;
    final String[] discard = { "", "", "hasOrgKey" };
    if (parentsFieldsData == null || parentsFieldsData.length == 0)
      discard[0] = "parent"; // remove the parent tags
    xmlDocumentXsql = xmlEngine.readXmlTemplate("org/openbravo/wad/datasourceSortTab", discard)
        .createXmlDocument();

    xmlDocumentXsql.ignoreTranslation = true;
    xmlDocumentXsql.setParameter("class", tabName + "Data");
    xmlDocumentXsql.setParameter("package", "org.openbravo.erpWindows." + windowName);
    xmlDocumentXsql.setParameter("package", "org.openbravo.erpWindows."
        + (!javaPackage.equals("") ? javaPackage + "." : "") + windowName);
    xmlDocumentXsql.setParameter("table", tableName);
    xmlDocumentXsql.setParameter("key", tableName + "." + keyColumnName);
    xmlDocumentXsql.setParameter("SortConditionField",
        FieldsData.columnName(pool, strColumnSortYNId));
    final String strSortField = FieldsData.columnName(pool, strColumnSortOrderId);
    xmlDocumentXsql.setParameter("SortField", strSortField);
    xmlDocumentXsql.setParameter("SortFieldInp", Sqlc.TransformaNombreColumna(strSortField));
    if (parentsFieldsData.length > 0) {
      xmlDocumentXsql.setParameter("keyParent", tableName + "." + parentsFieldsData[0].name);
    }
    xmlDocumentXsql.setParameter("paramKey", Sqlc.TransformaNombreColumna(keyColumnName));
    if (parentsFieldsData.length > 0) {
      xmlDocumentXsql.setParameter("paramKeyParent",
          Sqlc.TransformaNombreColumna(parentsFieldsData[0].name));
    }
    String strOrder = " ORDER BY " + tableName + "." + strSortField;

    orderBy = orderBy.replace("ORDER BY 1", "").trim();
    orderBy = orderBy.replace("ORDER BY", "");
    if (!orderBy.isEmpty()) {
      strOrder += ", " + orderBy;
    }

    String strFields = "";
    String strTables = "";
    String strWhere = "";
    {
      final Vector<Object> vecCounters = new Vector<Object>();
      final Vector<Object> vecFields = new Vector<Object>();
      final Vector<Object> vecTable = new Vector<Object>();
      final Vector<Object> vecWhere = new Vector<Object>();
      final FieldsData[] data = FieldsData.identifierColumns(pool, tableName);
      log4j.debug("Total Identifiers for " + tableName + ": " + data.length);
      if (data == null)
        strFields = "''";
      vecCounters.addElement("0");
      vecCounters.addElement("0");
      for (int i = 0; i < data.length; i++) {
        if (i != 0)
          strFields += " || ' - ' || ";
        strFields += WadUtility.columnIdentifier(pool, tableName, true, data[i], vecCounters,
            false, vecFields, vecTable, vecWhere, vecParametersTop, vecTableParametersTop,
            sqlDateFormat);
      }
      for (int i = 0; i < vecTable.size(); i++) {
        final String strAux = (String) vecTable.elementAt(i);
        strTables += (strAux.trim().toLowerCase().startsWith("left join") ? " " : ", ") + strAux;
      }
      for (int i = 0; i < vecWhere.size(); i++) {
        strWhere += "\n AND " + vecWhere.elementAt(i).toString();
      }

      if (!strWhere.isEmpty()) {
        strWhere += "\n AND ";
      }
      strWhere += whereClause;

    }

    xmlDocumentXsql.setParameter("fields", strFields);

    // Relation select
    xmlDocumentXsql.setParameter("tables", strTables);
    xmlDocumentXsql.setParameter("where", strWhere);
    xmlDocumentXsql.setParameter("order", strOrder);
    final StringBuffer strParameters = new StringBuffer();
    for (int i = 0; i < vecTableParametersTop.size(); i++) {
      strParameters.append(vecTableParametersTop.elementAt(i).toString()).append("\n");
    }
    for (int i = 0; i < vecParametersTop.size(); i++) {
      strParameters.append(vecParametersTop.elementAt(i).toString()).append("\n");
    }
    xmlDocumentXsql.setParameter("parameters", strParameters.toString());
    // Parent field
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      final Vector<Object> vecCounters = new Vector<Object>();
      final Vector<Object> vecFields = new Vector<Object>();
      final Vector<Object> vecTable = new Vector<Object>();
      final Vector<Object> vecWhere = new Vector<Object>();
      final Vector<Object> vecParameters = new Vector<Object>();
      final Vector<Object> vecTableParameters = new Vector<Object>();
      vecCounters.addElement("0");
      vecCounters.addElement("0");
      final String strText = WadUtility.columnIdentifier(pool, parentsFieldsData[0].tablename,
          true, parentsFieldsData[0], vecCounters, false, vecFields, vecTable, vecWhere,
          vecParameters, vecTableParameters, sqlDateFormat);
      final FieldsData[] fieldsParent = new FieldsData[1];
      fieldsParent[0] = new FieldsData();
      fieldsParent[0].defaultvalue = "SELECT (" + strText + ") AS NAME FROM ";
      fieldsParent[0].defaultvalue += parentsFieldsData[0].tablename;
      for (int s = 0; s < vecTable.size(); s++) {
        final String strAux = (String) vecTable.elementAt(s);
        fieldsParent[0].defaultvalue += (strAux.trim().toLowerCase().startsWith("left join") ? " "
            : ", ") + strAux;
      }
      fieldsParent[0].defaultvalue += " WHERE " + parentsFieldsData[0].tablename + "."
          + parentsFieldsData[0].name + " = ? ";
      for (int s = 0; s < vecWhere.size(); s++) {
        fieldsParent[0].defaultvalue += " AND " + vecWhere.elementAt(s).toString();
      }
      fieldsParent[0].whereclause = "";
      for (int s = 0; s < vecTableParameters.size(); s++) {
        fieldsParent[0].whereclause += vecTableParameters.elementAt(s).toString() + "\n";
      }
      fieldsParent[0].whereclause += "<Parameter name=\""
          + Sqlc.TransformaNombreColumna(parentsFieldsData[0].name) + "\"/>\n";
      for (int s = 0; s < vecParameters.size(); s++) {
        fieldsParent[0].whereclause += vecParameters.elementAt(s).toString() + "\n";
      }
      xmlDocumentXsql.setData("structure14", fieldsParent);
    } else {
      xmlDocumentXsql.setData("structure14", null);
    }
    // Parent field translated
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      final Vector<Object> vecCounters = new Vector<Object>();
      final Vector<Object> vecFields = new Vector<Object>();
      final Vector<Object> vecTable = new Vector<Object>();
      final Vector<Object> vecWhere = new Vector<Object>();
      final Vector<Object> vecParameters = new Vector<Object>();
      final Vector<Object> vecTableParameters = new Vector<Object>();
      vecCounters.addElement("0");
      vecCounters.addElement("0");
      final String strText = WadUtility.columnIdentifier(pool, parentsFieldsData[0].tablename,
          true, parentsFieldsData[0], vecCounters, true, vecFields, vecTable, vecWhere,
          vecParameters, vecTableParameters, sqlDateFormat);
      final FieldsData[] fieldsParent = new FieldsData[1];
      fieldsParent[0] = new FieldsData();
      fieldsParent[0].defaultvalue = "SELECT (" + strText + ") AS NAME FROM "
          + parentsFieldsData[0].tablename;
      for (int s = 0; s < vecTable.size(); s++) {
        final String strAux = (String) vecTable.elementAt(s);
        fieldsParent[0].defaultvalue += (strAux.trim().toLowerCase().startsWith("left join") ? " "
            : ", ") + strAux;
      }
      fieldsParent[0].defaultvalue += " WHERE " + parentsFieldsData[0].tablename + "."
          + parentsFieldsData[0].name + " = ? ";
      for (int s = 0; s < vecWhere.size(); s++) {
        fieldsParent[0].defaultvalue += " AND " + vecWhere.elementAt(s).toString();
      }
      fieldsParent[0].whereclause = "";
      for (int s = 0; s < vecTableParameters.size(); s++) {
        fieldsParent[0].whereclause += vecTableParameters.elementAt(s).toString() + "\n";
      }
      fieldsParent[0].whereclause += "<Parameter name=\""
          + Sqlc.TransformaNombreColumna(parentsFieldsData[0].name) + "\"/>\n";
      for (int s = 0; s < vecParameters.size(); s++) {
        fieldsParent[0].whereclause += vecParameters.elementAt(s).toString() + "\n";
      }
      xmlDocumentXsql.setData("structure15", fieldsParent);
    } else {
      xmlDocumentXsql.setData("structure15", null);
    }
    WadUtility.writeFile(fileDir, tabName + "_data.xsql",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xmlDocumentXsql.print());
  }

  /**
   * Generates the xsql file for the tab
   * 
   * @param parentsFieldsData
   *          Array with the parent fields of the tab
   * @param fileDir
   *          Path where the file is gonna be created.
   * @param strTab
   *          Id of the tab.
   * @param tabName
   *          Name of the tab.
   * @param tableName
   *          Tab's table name.
   * @param windowName
   *          Window name.
   * @param keyColumnName
   *          Name of the key column.
   * @param strFields
   *          Select clause for the tab.
   * @param strTables
   *          From clause for the tab.
   * @param strOrder
   *          Order clause for the tab.
   * @param strWhere
   *          Where clause for the tab.
   * @param vecParametersTop
   *          Vector of where clause parameters.
   * @param strFilter
   *          Filter clause for the tab.
   * @param selCol
   *          Array with the selection columns.
   * @param tablevel
   *          Tab level.
   * @param windowType
   *          Type of window.
   * @param vecTableParametersTop
   *          Array of from clause parameters.
   * @param fieldsDataSelectAux
   *          Array with the tab's fields.
   * @throws ServletException
   * @throws IOException
   */
  private void processTabXSQL(FieldsData[] parentsFieldsData, File fileDir, String strTab,
      String tabName, String tableName, String windowName, String keyColumnName, String strFields,
      String strTables, String strOrder, String strWhere, Vector<Object> vecParametersTop,
      String strFilter, EditionFieldsData[] selCol, String tablevel, String windowType,
      Vector<Object> vecTableParametersTop, FieldsData[] fieldsDataSelectAux,
      boolean isSecondaryKey, String javaPackage, Vector<String> vecFieldParameters)
      throws ServletException, IOException {
    log4j.debug("Procesig xsql: " + strTab + ", " + tabName);
    XmlDocument xmlDocumentXsql;
    final String[] discard = { "", "", "", "", "", "", "", "", "", "", "" };

    if (parentsFieldsData == null || parentsFieldsData.length == 0) {
      discard[0] = "parent"; // remove the parent tags
    } else if (!"Y".equals(parentsFieldsData[0].issecondarykey)) {
      discard[10] = "parentSecondaryKey";
    }

    if (tableName.toUpperCase().endsWith("_ACCESS")) {
      discard[6] = "client";
      discard[1] = "org";
    } // else if (tableName.toUpperCase().startsWith("M_PRODUCT") ||
    // tableName.toUpperCase().startsWith("C_BP") ||
    // tableName.toUpperCase().startsWith("AD_ORG")) discard[1] = "org";

    boolean isHighVolumen = (FieldsData.isHighVolume(pool, strTab).equals("Y"));
    if (!isHighVolumen || !tablevel.equals("0")) {
      discard[8] = "sectionIsHighVolume";
    }

    if (selCol == null || selCol.length == 0) {
      discard[2] = "sectionHighVolume";
      discard[3] = "sectionHighVolume1";
      discard[9] = "sectionIsHighVolume4";
    }
    if (!(windowType.equalsIgnoreCase("T") && tablevel.equals("0")))
      discard[4] = "sectionTransactional";
    if (strFilter.trim().equals(""))
      discard[5] = "sectionFilter";
    if ((!(isSecondaryKey && !EditionFieldsData.isOrgKey(pool, strTab).equals("0")))
        || strTab.equals("170"))
      discard[7] = "hasOrgKey";
    else {
      discard[7] = "hasNoOrgKey";
    }

    xmlDocumentXsql = xmlEngine.readXmlTemplate("org/openbravo/wad/datasource", discard)
        .createXmlDocument();

    xmlDocumentXsql.ignoreTranslation = true;
    xmlDocumentXsql.setParameter("class", tabName + "Data");
    xmlDocumentXsql.setParameter("package", "org.openbravo.erpWindows."
        + (!javaPackage.equals("") ? javaPackage + "." : "") + windowName);
    xmlDocumentXsql.setParameter("table", tableName);
    xmlDocumentXsql.setParameter("key", tableName + "." + keyColumnName);
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {

      xmlDocumentXsql.setParameter("keyParent", tableName + "." + parentsFieldsData[0].name);
    }
    xmlDocumentXsql.setParameter("paramKey", Sqlc.TransformaNombreColumna(keyColumnName));
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      xmlDocumentXsql.setParameter("paramKeyParent",
          Sqlc.TransformaNombreColumna(parentsFieldsData[0].name));
      if (isSecondaryKey && (!EditionFieldsData.isOrgKey(pool, strTab).equals("0"))) {
        xmlDocumentXsql.setParameter("paramKeyParentOrg", "currentAdOrgId");
      }
      parentsFieldsData[0].name = WadUtility.columnName(parentsFieldsData[0].name,
          parentsFieldsData[0].tablemodule, parentsFieldsData[0].columnmodule);
    }
    xmlDocumentXsql.setParameter("fields", strFields);

    // Relation select
    xmlDocumentXsql.setParameter("tables", strTables);
    xmlDocumentXsql.setParameter("where", strWhere);
    xmlDocumentXsql.setParameter("filter", strFilter);
    xmlDocumentXsql.setParameter("order", strOrder);
    final StringBuffer strParameters = new StringBuffer();
    final StringBuffer strParametersFields = new StringBuffer();

    for (String param : vecFieldParameters) {
      strParametersFields.append(param);
    }

    for (int i = 0; i < vecTableParametersTop.size(); i++) {
      strParameters.append(vecTableParametersTop.elementAt(i).toString()).append("\n");
    }
    for (int i = 0; i < vecParametersTop.size(); i++) {
      strParameters.append(vecParametersTop.elementAt(i).toString()).append("\n");
    }
    xmlDocumentXsql.setParameter("parameterFields", strParametersFields.toString());
    xmlDocumentXsql.setParameter("parameters", strParameters.toString());

    // Parent field
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      final Vector<Object> vecCounters = new Vector<Object>();
      final Vector<Object> vecFields = new Vector<Object>();
      final Vector<Object> vecTable = new Vector<Object>();
      final Vector<Object> vecWhere = new Vector<Object>();
      final Vector<Object> vecParameters = new Vector<Object>();
      final Vector<Object> vecTableParameters = new Vector<Object>();
      vecCounters.addElement("0");
      vecCounters.addElement("0");
      final String strText = WadUtility.columnIdentifier(pool, parentsFieldsData[0].tablename,
          true, parentsFieldsData[0], vecCounters, false, vecFields, vecTable, vecWhere,
          vecParameters, vecTableParameters, sqlDateFormat);
      final FieldsData[] fieldsParent = new FieldsData[1];
      fieldsParent[0] = new FieldsData();
      fieldsParent[0].defaultvalue = "SELECT (" + strText + ") AS NAME FROM ";
      fieldsParent[0].defaultvalue += parentsFieldsData[0].tablename;
      for (int s = 0; s < vecTable.size(); s++) {
        final String strAux = (String) vecTable.elementAt(s);
        fieldsParent[0].defaultvalue += (strAux.trim().toLowerCase().startsWith("left join") ? " "
            : ", ") + strAux;
      }

      fieldsParent[0].defaultvalue += " WHERE " + parentsFieldsData[0].tablename + "."
          + parentsFieldsData[0].name + " = ? ";
      for (int s = 0; s < vecWhere.size(); s++) {
        fieldsParent[0].defaultvalue += " AND " + vecWhere.elementAt(s).toString();
      }
      fieldsParent[0].whereclause = "";
      for (int s = 0; s < vecTableParameters.size(); s++) {
        fieldsParent[0].whereclause += vecTableParameters.elementAt(s).toString() + "\n";
      }
      fieldsParent[0].whereclause += "<Parameter name=\""
          + Sqlc.TransformaNombreColumna(parentsFieldsData[0].name) + "\"/>\n";
      for (int s = 0; s < vecParameters.size(); s++) {
        fieldsParent[0].whereclause += vecParameters.elementAt(s).toString() + "\n";
      }

      if ("Y".equals(parentsFieldsData[0].issecondarykey)) {
        xmlDocumentXsql.setParameter("parentTableName", parentsFieldsData[0].tablename);
        xmlDocumentXsql.setParameter("keySecondary", parentsFieldsData[0].name);
      }

      xmlDocumentXsql.setData("structure14", fieldsParent);
    } else {
      xmlDocumentXsql.setData("structure14", null);
    }
    // Parent field translated
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      final Vector<Object> vecCounters = new Vector<Object>();
      final Vector<Object> vecFields = new Vector<Object>();
      final Vector<Object> vecTable = new Vector<Object>();
      final Vector<Object> vecWhere = new Vector<Object>();
      final Vector<Object> vecParameters = new Vector<Object>();
      final Vector<Object> vecTableParameters = new Vector<Object>();
      vecCounters.addElement("0");
      vecCounters.addElement("0");
      final String strText = WadUtility.columnIdentifier(pool, parentsFieldsData[0].tablename,
          true, parentsFieldsData[0], vecCounters, true, vecFields, vecTable, vecWhere,
          vecParameters, vecTableParameters, sqlDateFormat);
      final FieldsData[] fieldsParent = new FieldsData[1];
      fieldsParent[0] = new FieldsData();
      fieldsParent[0].defaultvalue = "SELECT (" + strText + ") AS NAME FROM "
          + parentsFieldsData[0].tablename;
      for (int s = 0; s < vecTable.size(); s++) {
        final String strAux = (String) vecTable.elementAt(s);
        fieldsParent[0].defaultvalue += (strAux.trim().toLowerCase().startsWith("left join") ? " "
            : ", ") + strAux;
      }

      fieldsParent[0].defaultvalue += " WHERE " + parentsFieldsData[0].tablename + "."
          + parentsFieldsData[0].name + " = ? ";
      for (int s = 0; s < vecWhere.size(); s++) {
        fieldsParent[0].defaultvalue += " AND " + vecWhere.elementAt(s).toString();
      }
      fieldsParent[0].whereclause = "";
      for (int s = 0; s < vecTableParameters.size(); s++) {
        fieldsParent[0].whereclause += vecTableParameters.elementAt(s).toString() + "\n";
      }
      fieldsParent[0].whereclause += "<Parameter name=\""
          + Sqlc.TransformaNombreColumna(parentsFieldsData[0].name) + "\"/>\n";
      for (int s = 0; s < vecParameters.size(); s++) {
        fieldsParent[0].whereclause += vecParameters.elementAt(s).toString() + "\n";
      }
      xmlDocumentXsql.setData("structure15", fieldsParent);
    } else {
      xmlDocumentXsql.setData("structure15", null);
    }

    // Auxiliar Fields
    {
      final FieldsData fieldsAux[] = FieldsData.selectAuxiliar(pool, "@SQL=", strTab);
      for (int i = 0; i < fieldsAux.length; i++) {
        final Vector<Object> vecParametros = new Vector<Object>();
        fieldsAux[i].defaultvalue = WadUtility.getSQLWadContext(fieldsAux[i].defaultvalue,
            vecParametros);
        final StringBuffer parametros = new StringBuffer();
        for (final Enumeration<Object> e = vecParametros.elements(); e.hasMoreElements();) {
          String paramsElement = WadUtility.getWhereParameter(e.nextElement(), true);
          parametros.append("\n" + paramsElement);
        }
        fieldsAux[i].whereclause = parametros.toString();
      }
      xmlDocumentXsql.setData("structure9", fieldsAux);
    }
    // Default Fields
    {
      final FieldsData fieldsDef[] = FieldsData.selectDefaultValue(pool, "", strTab);
      final Vector<Object> v = new Vector<Object>();
      int itable = 0;
      for (int i = 0; i < fieldsDef.length; i++) {
        final Vector<Object> vecParametros = new Vector<Object>();
        if (fieldsDef[i].defaultvalue.startsWith("@SQL=")) {
          fieldsDef[i].defaultvalue = WadUtility.getSQLWadContext(fieldsDef[i].defaultvalue,
              vecParametros);
          final StringBuffer parametros = new StringBuffer();
          for (final Enumeration<Object> e = vecParametros.elements(); e.hasMoreElements();) {
            String paramsElement = WadUtility.getWhereParameter(e.nextElement(), true);
            parametros.append("\n" + paramsElement);
          }
          fieldsDef[i].whereclause = parametros.toString();
          v.addElement(fieldsDef[i]);
        }

        // Calculate additional defaults
        WADControl control = WadUtility.getWadControlClass(pool, fieldsDef[i].reference,
            fieldsDef[i].referencevalue);
        itable = control.addAdditionDefaulSQLFields(v, fieldsDef[i], itable);
      }
      final FieldsData[] fd = new FieldsData[v.size()];
      v.copyInto(fd);
      xmlDocumentXsql.setData("structure10", fd);
    }
    {
      // default values for search references in parameter windows for action buttons
      // keep it hardcoded by now
      final ProcessRelationData[] data = ProcessRelationData.selectXSQL(pool, strTab);
      if (data != null) {
        for (int i = 0; i < data.length; i++) {
          String tableN = "";
          if (data[i].adReferenceId.equals("28"))
            tableN = "C_ValidCombination";
          else if (data[i].adReferenceId.equals("31"))
            tableN = "M_Locator";
          else
            tableN = data[i].name.substring(0, data[i].searchname.length() - 3);
          String strName = "";
          if (data[i].adReferenceId.equals("28"))
            strName = "C_ValidCombination_ID";
          else if (data[i].adReferenceId.equals("31"))
            strName = "M_Locator_ID";
          else
            strName = data[i].searchname;
          final String strColumnName = FieldsData.columnIdentifier(pool, tableN);
          final StringBuffer fields = new StringBuffer();
          fields.append("SELECT " + strColumnName);
          fields.append(" FROM " + tableN);
          fields.append(" WHERE isActive='Y'");
          fields.append(" AND " + strName + " = ? ");
          data[i].whereclause = fields.toString();
          data[i].name = FormatUtilities.replace(data[i].name);
        }
      }
      xmlDocumentXsql.setData("structure12", data);
    }
    // SQLs of the defaultvalue of the parameter of the tab-associated
    // processes
    {
      final ProcessRelationData fieldsAux[] = ProcessRelationData.selectXSQLParams(pool, strTab);
      if (fieldsAux != null && fieldsAux.length > 0) {
        for (int i = 0; i < fieldsAux.length; i++) {
          final Vector<Object> vecParametros = new Vector<Object>();
          fieldsAux[i].reference = fieldsAux[i].adProcessId + "_"
              + FormatUtilities.replace(fieldsAux[i].columnname);
          fieldsAux[i].defaultvalue = WadUtility.getSQLWadContext(fieldsAux[i].defaultvalue,
              vecParametros);
          final StringBuffer parametros = new StringBuffer();
          for (final Enumeration<Object> e = vecParametros.elements(); e.hasMoreElements();) {
            final String paramsElement = WadUtility.getWhereParameter(e.nextElement(), true);
            parametros.append("\n" + paramsElement);
          }
          fieldsAux[i].whereclause = parametros.toString();
        }
      }
      xmlDocumentXsql.setData("structure16", fieldsAux);
    }
    // Update
    {
      final FieldsData fieldsDataUpdate[] = FieldsData.selectUpdatables(pool, strTab);
      for (int i = 0; i < fieldsDataUpdate.length; i++) { // *** i=1?
        fieldsDataUpdate[i].name = ((i > 0) ? ", " : "") + fieldsDataUpdate[i].name;
        if (fieldsDataUpdate[i].reference.equals("24"))
          fieldsDataUpdate[i].xmlFormat = "TO_TIMESTAMP(?,'HH24:MI:SS')";
        else if (fieldsDataUpdate[i].reference.equals("16")) {
          // datetime, use time stamp: it works for ORA date and for PG. In pg, to_date(text, text)
          // does not save time
          fieldsDataUpdate[i].xmlFormat = "TO_TIMESTAMP(?, ?)";
        } else
          fieldsDataUpdate[i].xmlFormat = WadUtility.sqlCasting(pool,
              fieldsDataUpdate[i].reference, fieldsDataUpdate[i].referencevalue) + "(?)";
      }
      xmlDocumentXsql.setData("structure3", fieldsDataUpdate);
    }
    {
      final FieldsData fieldsDataParameterTmp[] = FieldsData.selectUpdatables(pool, strTab);
      ArrayList<String> fieldParam = new ArrayList<String>();
      for (int i = 0; i < fieldsDataParameterTmp.length; i++) {
        fieldParam.add(Sqlc.TransformaNombreColumna(fieldsDataParameterTmp[i].name));
        if (fieldsDataParameterTmp[i].reference.equals("16")) {
          // add extra sqldatetime to datetime reference
          fieldParam.add("dateTimeFormat");
        }
      }
      FieldsData fieldsDataParameter[] = new FieldsData[fieldParam.size()];
      int i = 0;
      for (String paramName : fieldParam) {
        fieldsDataParameter[i] = new FieldsData();
        fieldsDataParameter[i].name = paramName;
        i++;
      }

      xmlDocumentXsql.setData("structure4", fieldsDataParameter);
    }
    {
      // Insert
      FieldsData[] fieldsDataInsert = null;
      fieldsDataInsert = copyarray(fieldsDataSelectAux);
      for (int i = 1; i < fieldsDataInsert.length; i++) {
        if (!fieldsDataInsert[i].name.equalsIgnoreCase("Created")
            && !fieldsDataInsert[i].name.equalsIgnoreCase("CreatedBy")
            && !fieldsDataInsert[i].name.equalsIgnoreCase("Updated")
            && !fieldsDataInsert[i].name.equalsIgnoreCase("UpdatedBy"))
          fieldsDataInsert[i].name = ", " + fieldsDataInsert[i].name;
        else
          fieldsDataInsert[i].name = "";
      }
      xmlDocumentXsql.setData("structure5", fieldsDataInsert);
    }
    {
      FieldsData[] fieldsDataValue = null;
      fieldsDataValue = copyarray(fieldsDataSelectAux);
      for (int i = 0; i < fieldsDataValue.length; i++) {
        if (!fieldsDataValue[i].name.equalsIgnoreCase("Created")
            && !fieldsDataValue[i].name.equalsIgnoreCase("CreatedBy")
            && !fieldsDataValue[i].name.equalsIgnoreCase("Updated")
            && !fieldsDataValue[i].name.equalsIgnoreCase("UpdatedBy")) {
          if (fieldsDataValue[i].reference.equals("24")) {
            fieldsDataValue[i].name = ((i > 0) ? ", " : "") + "TO_TIMESTAMP(?, 'HH24:MI:SS')";
          } else if (fieldsDataValue[i].reference.equals("16")) {
            // datetime, use time stamp: it works for ORA date and for PG. In pg, to_date(text,
            // text) does not save time
            fieldsDataValue[i].name = ((i > 0) ? ", " : "") + "TO_TIMESTAMP(?, ?)";
          } else {
            fieldsDataValue[i].name = ((i > 0) ? ", " : "")
                + WadUtility.sqlCasting(pool, fieldsDataValue[i].reference,
                    fieldsDataValue[i].referencevalue) + "(?)";
          }
        } else
          fieldsDataValue[i].name = "";
      }
      xmlDocumentXsql.setData("structure6", fieldsDataValue);
    }
    {
      final Vector<Object> vecAux = new Vector<Object>();
      FieldsData[] fieldsDataParameterInsert = null;
      fieldsDataParameterInsert = copyarray(fieldsDataSelectAux);
      for (int i = 0; i < fieldsDataParameterInsert.length; i++) {
        if (!fieldsDataParameterInsert[i].name.equalsIgnoreCase("Created")
            && !fieldsDataParameterInsert[i].name.equalsIgnoreCase("CreatedBy")
            && !fieldsDataParameterInsert[i].name.equalsIgnoreCase("Updated")
            && !fieldsDataParameterInsert[i].name.equalsIgnoreCase("UpdatedBy")) {
          fieldsDataParameterInsert[i].name = Sqlc
              .TransformaNombreColumna(fieldsDataParameterInsert[i].name);
          vecAux.addElement(fieldsDataParameterInsert[i]);
          if (fieldsDataParameterInsert[i].reference.equals("16")) {
            FieldsData paramDateTime = new FieldsData();
            paramDateTime.name = "dateTimeFormat";
            vecAux.addElement(paramDateTime);
          }
        }
      }
      FieldsData[] fieldsDataParameterInsert1 = null;
      if (vecAux.size() > 0) {
        fieldsDataParameterInsert1 = new FieldsData[vecAux.size()];
        vecAux.copyInto(fieldsDataParameterInsert1);
      }
      xmlDocumentXsql.setData("structure7", fieldsDataParameterInsert1);
    }
    {
      // generate set() method
      final FieldsData fieldsDataDefaults[] = FieldsData.selectDefaultValue(pool, "", strTab);
      final Vector<Object> vecDDef = new Vector<Object>();
      for (int i = 0; i < fieldsDataDefaults.length; i++) {
        boolean modified = false;
        if (parentsFieldsData == null || parentsFieldsData.length == 0
            || !parentsFieldsData[0].name.equalsIgnoreCase(fieldsDataDefaults[i].columnname)) {
          fieldsDataDefaults[i].name = Sqlc.TransformaNombreColumna(fieldsDataDefaults[i].name);
          fieldsDataDefaults[i].columnname = Sqlc
              .TransformaNombreColumna(fieldsDataDefaults[i].columnname);
          vecDDef.addElement(fieldsDataDefaults[i]);
          modified = true;
        }

        // add another field for default special cases with more than 1 field
        WADControl control = WadUtility.getWadControlClass(pool, fieldsDataDefaults[i].reference,
            fieldsDataDefaults[i].referencevalue);
        if (fieldsDataDefaults[i].isdisplayed.equals("Y")
            && control.addAdditionDefaulJavaFields(new StringBuffer(), fieldsDataDefaults[i],
                tabName, 0) != 0) {

          final FieldsData f = new FieldsData();
          f.name = (modified ? fieldsDataDefaults[i].name : Sqlc
              .TransformaNombreColumna(fieldsDataDefaults[i].name)) + "r";
          f.columnname = (modified ? fieldsDataDefaults[i].columnname : Sqlc
              .TransformaNombreColumna(fieldsDataDefaults[i].columnname)) + "r";
          vecDDef.addElement(f);
        } else if (fieldsDataDefaults[i].reference.equals("28")
            && fieldsDataDefaults[i].isdisplayed.equals("Y")
            && !fieldsDataDefaults[i].referencevalue.equals("")) {
          // Button special case
          final FieldsData f = new FieldsData();
          f.name = (modified ? fieldsDataDefaults[i].name : Sqlc
              .TransformaNombreColumna(fieldsDataDefaults[i].name)) + "Btn";
          f.columnname = (modified ? fieldsDataDefaults[i].columnname : Sqlc
              .TransformaNombreColumna(fieldsDataDefaults[i].columnname)) + "Btn";
          vecDDef.addElement(f);
        }
      }
      final FieldsData[] f1 = new FieldsData[vecDDef.size()];
      vecDDef.copyInto(f1);
      xmlDocumentXsql.setData("structure8", f1);
    }

    {
      final ActionButtonRelationData[] abrd = WadActionButton.buildActionButtonSQL(pool, strTab);
      xmlDocumentXsql.setData("structure11", abrd);
    }

    {
      final FieldsData[] encryptedData = FieldsData.selectEncrypted(pool, strTab);
      if (encryptedData != null && encryptedData.length > 0) {
        for (int g = 0; g < encryptedData.length; g++) {
          encryptedData[g].realname = Sqlc.TransformaNombreColumna(encryptedData[g].realname);
          encryptedData[g].name = FormatUtilities.replace(encryptedData[g].name);
        }
      }
      xmlDocumentXsql.setData("structure17", encryptedData);
    }

    xmlDocumentXsql.setData("structure13", selCol);

    WadUtility.writeFile(fileDir, tabName + "_data.xsql",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xmlDocumentXsql.print());
  }

  /**
   * Generates the combo reloads for the tab. Combo reloads are the callouts to reloads the values
   * of the subordinated combos in the tab.
   * 
   * By now combo reloads are created only for core list, table and table dir references
   * 
   * @param fileDir
   *          Path where to generate the file.
   * @param strTab
   *          Id of the tab.
   * @param parentsFieldsData
   *          Array with the parents fields.
   * @param vecFields
   *          Vector with the tab's fields.
   * @param isSOTrx
   *          Indicates if the tab of sales or not (Y | N).
   * @throws ServletException
   * @throws IOException
   */
  private void processTabComboReloads(File fileDir, String strTab, FieldsData[] parentsFieldsData,
      Vector<Object> vecFields, String isSOTrx, String accesslevel) throws ServletException,
      IOException {
    log4j.debug("Procesig combo reloads java for tab: " + strTab);
    final FieldsData[] data = FieldsData.selectValidationTab(pool, strTab);
    if (data == null || data.length == 0)
      return;

    final Vector<Object> vecReloads = new Vector<Object>();
    final Vector<Object> vecTotal = new Vector<Object>();
    final Vector<Object> vecCounters = new Vector<Object>();
    vecCounters.addElement("0");
    vecCounters.addElement("0");
    FieldsData[] result = null;

    for (int i = 0; i < data.length; i++) {
      // Do not reload parent combo, it shouldn't be changed preventing inactive elements removal
      if (parentsFieldsData == null || parentsFieldsData.length == 0
          || !data[i].columnname.equalsIgnoreCase(parentsFieldsData[0].name)) {
        final String code = data[i].whereclause
            + ((!data[i].whereclause.equals("") && !data[i].referencevalue.equals("")) ? " AND "
                : "") + data[i].referencevalue;
        data[i].columnname = "inp" + Sqlc.TransformaNombreColumna(data[i].columnname);
        data[i].whereclause = WadUtility.getComboReloadText(code, vecFields, parentsFieldsData,
            vecReloads, "inp");

        // Always add combo reload for organization
        data[i].whereclause += (!data[i].whereclause.isEmpty() ? ", " : "") + "\"inpadOrgId\"";

        if (data[i].reference.equals("17") && data[i].whereclause.equals(""))
          data[i].whereclause = "\"inp" + data[i].columnname + "\"";
        if (!data[i].whereclause.equals("")
            && data[i].isdisplayed.equals("Y")
            && (data[i].reference.equals("17") || data[i].reference.equals("18") || data[i].reference
                .equals("19"))) {
          if (data[i].name.equalsIgnoreCase("AD_Org_ID")) {
            if (parentsFieldsData == null || parentsFieldsData.length == 0)
              data[i].orgcode = "Utility.getContext(this, vars, \"#User_Org\", windowId, "
                  + accesslevel + ")";
            else
              data[i].orgcode = "Utility.getReferenceableOrg(this, vars, parentOrg, windowId, "
                  + accesslevel + ")";
          } else {
            data[i].orgcode = "Utility.getReferenceableOrg(vars, vars.getStringParameter(\"inpadOrgId\"))";
          }

          // Do not add client 0 for client combo, but do it for the rest of combos
          if (data[i].name.equalsIgnoreCase("AD_Client_ID")) {
            data[i].clientcode = "Utility.getContext(this, vars, \"#User_Client\", windowId, "
                + accesslevel + ")";
          } else {
            data[i].clientcode = "Utility.getContext(this, vars, \"#User_Client\", windowId)";
          }

          if (data[i].reference.equals("17")) { // List
            data[i].tablename = "List";
            data[i].tablenametrl = "List";
            data[i].htmltext = "select";
            data[i].htmltexttrl = "selectLanguage";
            data[i].xmltext = ", \"" + data[i].nameref + "\"";
            data[i].xmltexttrl = data[i].xmltext + ", vars.getLanguage()";
            data[i].xmltext += ", \"\"";
            data[i].xmltexttrl += ", \"\"";
          } else if (data[i].reference.equals("18")) { // Table
            final FieldsData[] tables = FieldsData.selectColumnTable(pool, strTab, data[i].id);
            if (tables == null || tables.length == 0)
              throw new ServletException("No se ha encontrado la Table para la columnId: "
                  + data[i].id);
            final StringBuffer where = new StringBuffer();
            final Vector<Object> vecFields1 = new Vector<Object>();
            final Vector<Object> vecTables = new Vector<Object>();
            final Vector<Object> vecWhere = new Vector<Object>();
            final Vector<Object> vecParameters = new Vector<Object>();
            final Vector<Object> vecTableParameters = new Vector<Object>();

            WADControl control = WadUtility.getWadControlClass(pool, data[i].reference,
                data[i].referencevalue);
            control.columnIdentifier(tables[0].tablename, tables[0], vecCounters, vecFields1,
                vecTables, vecWhere, vecParameters, vecTableParameters);

            where.append(tables[0].whereclause);
            data[i].tablename = "TableList";
            data[i].htmltext = "select" + tables[0].referencevalue;
            if (!tables[0].columnname.equals("")) {
              data[i].htmltext += "_" + tables[0].columnname;
              data[i].tablename = "TableListVal";
              if (!where.toString().equals(""))
                where.append(" AND ");
              where.append(tables[0].defaultvalue);
            }
            data[i].tablenametrl = data[i].tablename + "Trl";
            data[i].htmltexttrl = data[i].htmltext;
            data[i].xmltext = "";
            if (vecTableParameters.size() > 0) {
              data[i].xmltext = ", vars.getLanguage()";
            }
            data[i].xmltext += ", Utility.getContext(this, vars, \"#User_Org\", windowId), Utility.getContext(this, vars, \"#User_Client\", windowId)";
            data[i].xmltext += WadUtility.getWadComboReloadContext(where.toString(), isSOTrx);
            data[i].xmltexttrl = data[i].xmltext;
            if (vecParameters.size() > 0 && vecTableParameters.size() == 0) {
              data[i].xmltext += ", vars.getLanguage()";
              data[i].xmltexttrl += ", vars.getLanguage()";
            }
            data[i].xmltext += ", \"\"";
            data[i].xmltexttrl += ", \"\"";
          } else if (data[i].reference.equals("19")) { // TableDir
            final FieldsData[] tableDir = FieldsData.selectColumnTableDir(pool, strTab, data[i].id);
            if (tableDir == null || tableDir.length == 0)
              throw new ServletException("No se ha encontrado la TableDir para la columnId: "
                  + data[i].id);
            data[i].tablename = "TableDir";
            data[i].htmltext = "select" + tableDir[0].referencevalue;
            final String table_Name = tableDir[0].name.substring(0, tableDir[0].name.length() - 3);
            final Vector<Object> vecFields1 = new Vector<Object>();
            final Vector<Object> vecTables = new Vector<Object>();
            final Vector<Object> vecWhere = new Vector<Object>();
            final Vector<Object> vecParameters = new Vector<Object>();
            final Vector<Object> vecTableParameters = new Vector<Object>();

            WADControl control = WadUtility.getWadControlClass(pool, data[i].reference,
                data[i].referencevalue);
            control.columnIdentifier(table_Name, data[i], vecCounters, vecFields1, vecTables,
                vecWhere, vecParameters, vecTableParameters);

            data[i].xmltext = "";
            if (vecTableParameters.size() > 0) {
              data[i].xmltext = ", vars.getLanguage()";
            }
            data[i].xmltext += ", Utility.getContext(this, vars, \"#User_Org\", windowId), Utility.getContext(this, vars, \"#User_Client\", windowId)";
            if (!tableDir[0].columnname.equals("")) {
              data[i].htmltext += "_" + tableDir[0].columnname;
              data[i].tablename = "TableDirVal";
              data[i].xmltext += WadUtility.getWadComboReloadContext(tableDir[0].defaultvalue,
                  isSOTrx);
            } else {
              data[i].tablename = "TableDir";
            }
            data[i].tablenametrl = data[i].tablename + "Trl";
            data[i].htmltexttrl = data[i].htmltext;
            data[i].xmltexttrl = data[i].xmltext;
            if (vecParameters.size() > 0 && vecTableParameters.size() == 0) {
              data[i].xmltext += ", vars.getLanguage()";
              data[i].xmltexttrl += ", vars.getLanguage()";
            }
            data[i].xmltext += ", \"\"";
            data[i].xmltexttrl += ", \"\"";
          }

          // Do not create combo reload for the same column that is being modified
          if (!data[i].whereclause.replace("\"", "").equals(data[i].columnname)) {
            vecTotal.addElement(data[i]);
          }
        }
      }
    }
    if (vecTotal != null && vecTotal.size() > 0) {
      result = new FieldsData[vecTotal.size()];
      vecTotal.copyInto(result);
    }

    // Generate always callout, even there's no columns to check
    String discard[] = { "" };
    if (vecTotal == null || vecTotal.size() == 0) {
      discard[0] = "discardTry";
    }

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/wad/ComboReloads",
        discard).createXmlDocument();
    xmlDocument.setParameter("tabId", strTab);
    xmlDocument.setData("structure1", result);

    WadUtility.writeFile(fileDir, "ComboReloads" + strTab + ".java", xmlDocument.print());
  }

  /**
   * Generates combo reloads for all action buttons
   * 
   * @param fileDir
   *          Directory to save the generated java
   * @throws ServletException
   * @throws IOException
   */
  private void processProcessComboReloads(File fileDir) throws ServletException, IOException {
    log4j.info("Processig combo reloads for action buttons ");
    Vector<FieldsData> generatedProcesses = new Vector<FieldsData>();
    Vector<FieldsData[]> processCode = new Vector<FieldsData[]>();
    FieldsData[] processes = FieldsData.selectProcessesWithReloads(pool);

    for (FieldsData process : processes) {

      String processId = process.id;

      final FieldsData[] data = FieldsData.selectValidationProcess(pool, processId);
      if (data == null || data.length == 0)
        return;

      final boolean hasOrg = FieldsData.processHasOrgParam(pool, processId);

      final Vector<Object> vecReloads = new Vector<Object>();
      final Vector<Object> vecTotal = new Vector<Object>();
      final Vector<Object> vecCounters = new Vector<Object>();
      vecCounters.addElement("0");
      vecCounters.addElement("0");

      FieldsData[] result = null;

      for (int i = 0; i < data.length; i++) {

        final String code = data[i].whereclause
            + ((!data[i].whereclause.equals("") && !data[i].referencevalue.equals("")) ? " AND "
                : "") + data[i].referencevalue;
        data[i].columnname = "inp" + Sqlc.TransformaNombreColumna(data[i].columnname);
        data[i].whereclause = WadUtility.getComboReloadText(code, null, null, vecReloads, "inp");
        if (data[i].whereclause.equals("") && data[i].type.equals("R")) {
          // Add combo reloads for all combo references in case there is a ad_org parameter, if not
          // only for the params with validation rule
          if (!hasOrg) {
            continue;
          }
          data[i].whereclause = "\"inpadOrgId\"";
        }
        if (data[i].reference.equals("17") && data[i].whereclause.equals(""))
          data[i].whereclause = "\"inp" + data[i].columnname + "\"";
        if (!data[i].whereclause.equals("")
            && (data[i].reference.equals("17") || data[i].reference.equals("18") || data[i].reference
                .equals("19"))) {

          data[i].orgcode = "Utility.getReferenceableOrg(vars, vars.getStringParameter(\"inpadOrgId\"))";

          if (data[i].reference.equals("17")) { // List
            data[i].tablename = "List";
            data[i].tablenametrl = "List";
            data[i].htmltext = "select";
            data[i].htmltexttrl = "selectLanguage";
            data[i].xmltext = ", \"" + data[i].nameref + "\"";
            data[i].xmltexttrl = data[i].xmltext + ", vars.getLanguage()";
            data[i].xmltext += ", \"\"";
            data[i].xmltexttrl += ", \"\"";
          } else if (data[i].reference.equals("18")) { // Table
            final FieldsData[] tables = FieldsData.selectColumnTableProcess(pool, data[i].id);
            if (tables == null || tables.length == 0)
              throw new ServletException("No se ha encontrado la Table para la columnId: "
                  + data[i].id);
            final StringBuffer where = new StringBuffer();
            final Vector<Object> vecFields1 = new Vector<Object>();
            final Vector<Object> vecTables = new Vector<Object>();
            final Vector<Object> vecWhere = new Vector<Object>();
            final Vector<Object> vecParameters = new Vector<Object>();
            final Vector<Object> vecTableParameters = new Vector<Object>();

            WADControl control = WadUtility.getWadControlClass(pool, data[i].reference,
                data[i].referencevalue);
            control.columnIdentifier(tables[0].tablename, tables[0], vecCounters, vecFields1,
                vecTables, vecWhere, vecParameters, vecTableParameters);

            where.append(tables[0].whereclause);

            data[i].tablename = "TableList";
            data[i].htmltext = "select" + tables[0].referencevalue;
            if (!tables[0].columnname.equals("")) {
              data[i].htmltext += "_" + tables[0].columnname;
              data[i].tablename = "TableListVal";
              if (!where.toString().equals(""))
                where.append(" AND ");
              where.append(tables[0].defaultvalue);
            }
            data[i].tablenametrl = data[i].tablename + "Trl";
            data[i].htmltexttrl = data[i].htmltext;
            data[i].xmltext = "";
            if (vecTableParameters.size() > 0) {
              data[i].xmltext = ", vars.getLanguage()";
            }
            data[i].xmltext += ", Utility.getContext(this, vars, \"#User_Org\", windowId), Utility.getContext(this, vars, \"#User_Client\", windowId)";
            data[i].xmltext += WadUtility.getWadComboReloadContext(where.toString(), "N");
            data[i].xmltexttrl = data[i].xmltext;
            if (vecParameters.size() > 0 && vecTableParameters.size() == 0) {
              data[i].xmltext += ", vars.getLanguage()";
              data[i].xmltexttrl += ", vars.getLanguage()";
            }
            data[i].xmltext += ", \"\"";
            data[i].xmltexttrl += ", \"\"";
          } else if (data[i].reference.equals("19")) { // TableDir
            final FieldsData[] tableDir = FieldsData.selectColumnTableDirProcess(pool, data[i].id);
            if (tableDir == null || tableDir.length == 0)
              throw new ServletException("No se ha encontrado la TableDir para la columnId: "
                  + data[i].id);
            data[i].tablename = "TableDir";
            data[i].htmltext = "select" + tableDir[0].referencevalue;
            final String table_Name = tableDir[0].name.substring(0, tableDir[0].name.length() - 3);
            final Vector<Object> vecFields1 = new Vector<Object>();
            final Vector<Object> vecTables = new Vector<Object>();
            final Vector<Object> vecWhere = new Vector<Object>();
            final Vector<Object> vecParameters = new Vector<Object>();
            final Vector<Object> vecTableParameters = new Vector<Object>();

            WADControl control = WadUtility.getWadControlClass(pool, data[i].reference,
                data[i].referencevalue);
            control.columnIdentifier(table_Name, data[i], vecCounters, vecFields1, vecTables,
                vecWhere, vecParameters, vecTableParameters);

            data[i].xmltext = "";
            if (vecTableParameters.size() > 0) {
              data[i].xmltext = ", vars.getLanguage()";
            }
            data[i].xmltext += ", Utility.getContext(this, vars, \"#User_Org\", windowId), Utility.getContext(this, vars, \"#User_Client\", windowId)";
            if (!tableDir[0].columnname.equals("")) {
              data[i].htmltext += "_" + tableDir[0].columnname;
              data[i].tablename = "TableDirVal";
              data[i].xmltext += WadUtility.getWadComboReloadContext(tableDir[0].defaultvalue, "N");
            } else {
              data[i].tablename = "TableDir";
            }
            data[i].tablenametrl = data[i].tablename + "Trl";
            data[i].htmltexttrl = data[i].htmltext;
            data[i].xmltexttrl = data[i].xmltext;
            if (vecParameters.size() > 0 && vecTableParameters.size() == 0) {
              data[i].xmltext += ", vars.getLanguage()";
              data[i].xmltexttrl += ", vars.getLanguage()";
            }
            data[i].xmltext += ", \"\"";
            data[i].xmltexttrl += ", \"\"";
          }
          vecTotal.addElement(data[i]);
        }
      }
      if (vecTotal != null && vecTotal.size() > 0) {
        result = new FieldsData[vecTotal.size()];
        vecTotal.copyInto(result);
        processCode.add(result);
        generatedProcesses.add(process);
      }

    }
    if (generatedProcesses.size() > 0) {
      // create the helper class, it is a servlet that manages all combo reloads
      XmlDocument xmlDocumentHelper = xmlEngine.readXmlTemplate(
          "org/openbravo/wad/ComboReloadsProcessHelper").createXmlDocument();
      FieldsData[] processesGenerated = new FieldsData[generatedProcesses.size()];
      generatedProcesses.copyInto(processesGenerated);
      FieldsData[][] processData = new FieldsData[generatedProcesses.size()][];
      for (int i = 0; i < generatedProcesses.size(); i++) {
        processData[i] = processCode.get(i);
      }

      xmlDocumentHelper.setData("structure1", processesGenerated);
      xmlDocumentHelper.setData("structure2", processesGenerated);
      xmlDocumentHelper.setDataArray("reportComboReloadsProcess", "structure1", processData);
      WadUtility.writeFile(fileDir, "ComboReloadsProcessHelper.java", xmlDocumentHelper.print());
      log4j.debug("created :" + fileDir + "/ComboReloadsProcessHelper.java");
    }
  }

  /**
   * Generates the xml file for a sort type tab.
   * 
   * @param parentsFieldsData
   *          Array with the parent fields.
   * @param fileDir
   *          Path where is gonna be created the file.
   * @param strTab
   *          Id of the tab.
   * @param tabName
   *          The name of the tab.
   * @param keyColumnName
   *          The name of the tab's key column.
   * @throws ServletException
   * @throws IOException
   */
  private void processTabXmlSortTab(FieldsData[] parentsFieldsData, File fileDir, String strTab,
      String tabName, String keyColumnName) throws ServletException, IOException {
    log4j.debug("Procesig relation sort tab xml: " + strTab + ", " + tabName);
    final String[] discard = { "" };
    if (parentsFieldsData == null || parentsFieldsData.length == 0)
      discard[0] = new String("sectionParent");
    final XmlDocument xmlDocumentRXml = xmlEngine.readXmlTemplate(
        "org/openbravo/wad/ConfigurationSortTab_Relation", discard).createXmlDocument();
    xmlDocumentRXml.setParameter("class", tabName + "_Relation.html");
    xmlDocumentRXml.setParameter("key", keyColumnName);
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      xmlDocumentRXml.setParameter("parent", parentsFieldsData[0].name);
    }
    WadUtility.writeFile(fileDir, tabName + "_Relation.xml",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xmlDocumentRXml.print());
  }

  /**
   * Generates the html file for a sort type tab.
   * 
   * @param parentsFieldsData
   *          Array with the parent fields.
   * @param fileDir
   *          Path where is gonna be created the file.
   * @param strTab
   *          Id of the tab.
   * @param tabName
   *          The name of the tab.
   * @param windowName
   *          The name of the window.
   * @param keyColumnName
   *          The name of the key column.
   * @param tabNamePresentation
   *          The human name of the tab.
   * @param allTabs
   *          Array with all the tabs of the window.
   * @param strProcess
   *          Id of the process associated to the tab.
   * @param strDirectPrint
   *          Indicate if the process is a direct print or has a preview mode.
   * @param strParentNameDescription
   *          The human description of the parent tab.
   * @param WindowPathName
   *          The name of the window for the path.
   * @param strLanguage
   *          The language for which is gonna be created the file.
   * @throws ServletException
   * @throws IOException
   */
  private void processTabHtmlSortTab(FieldsData[] parentsFieldsData, File fileDir, String strTab,
      String tabName, String windowName, String keyColumnName, String tabNamePresentation,
      TabsData[] allTabs, String strProcess, String strDirectPrint, String WindowPathName,
      String strLanguage) throws ServletException, IOException {
    log4j.debug("Procesig relation sort tab html: " + strTab + ", " + tabName);
    XmlDocument xmlDocumentRHtml;
    final String[] discard = new String[3];
    if (strProcess.equals("")) {
      discard[0] = new String("printButton");
    } else {
      discard[0] = new String("");
    }
    if (allTabs.length <= NUM_TABS)
      discard[1] = new String("tabButton");
    else
      discard[1] = new String("");
    if (parentsFieldsData.length == 0)
      discard[2] = new String("parent");
    else
      discard[2] = new String("");

    xmlDocumentRHtml = xmlEngine.readXmlTemplate("org/openbravo/wad/TemplateSortTab_Relation",
        discard).createXmlDocument();
    xmlDocumentRHtml.setParameter("tab", tabNamePresentation);
    xmlDocumentRHtml.setParameter("form", tabName + "_Relation.html");
    xmlDocumentRHtml.setParameter("key", "inp" + Sqlc.TransformaNombreColumna(keyColumnName));
    xmlDocumentRHtml.setParameter("tabId", strTab);
    if (parentsFieldsData.length > 0) {
      xmlDocumentRHtml.setParameter("keyParent",
          "inp" + Sqlc.TransformaNombreColumna(parentsFieldsData[0].name));
      xmlDocumentRHtml.setParameter("parentKeyName", parentsFieldsData[0].name);
    }

    xmlDocumentRHtml.setParameter("subtabKey", tabName);
    WadUtility.writeFile(fileDir, tabName + "_Relation.html", xmlDocumentRHtml.print());
  }

  /**
   * Generates the xml for the relation window of the tab.
   * 
   * @param parentsFieldsData
   *          Array with the parent fields.
   * @param fileDir
   *          Path where is gonna be created the file.
   * @param strTab
   *          Id of the tab.
   * @param tabName
   *          Name of the tab.
   * @param keyColumnName
   *          Name of the tab's key column.
   * @param relationControl
   *          Object with the WADGrid control.
   * @throws ServletException
   * @throws IOException
   */
  private void processTabXmlRelation(FieldsData[] parentsFieldsData, File fileDir, String strTab,
      String tabName, String keyColumnName, WADControl relationControl) throws ServletException,
      IOException {
    log4j.debug("Procesig relation xml: " + strTab + ", " + tabName);
    final String[] discard = { "" };
    if (parentsFieldsData == null || parentsFieldsData.length == 0)
      discard[0] = new String("sectionParent");
    final XmlDocument xmlDocumentRXml = xmlEngine.readXmlTemplate(
        "org/openbravo/wad/Configuration_Relation", discard).createXmlDocument();
    xmlDocumentRXml.setParameter("class", tabName + "_Relation.html");
    xmlDocumentRXml.setParameter("key", keyColumnName);
    if (parentsFieldsData != null && parentsFieldsData.length > 0) {
      xmlDocumentRXml.setParameter("parent", parentsFieldsData[0].name);
    }
    xmlDocumentRXml.setParameter("relationControl", relationControl.toXml());
    WadUtility.writeFile(fileDir, tabName + "_Relation.xml",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xmlDocumentRXml.print());
  }

  /**
   * Generates the html file for the relation windows of the tab.
   * 
   * @param parentsFieldsData
   *          Array with the parent fields
   * @param fileDir
   *          Path where is gonna be created the file.
   * @param strTab
   *          Id of the tab.
   * @param tabName
   *          Name of the tab.
   * @param keyColumnName
   *          Name of the tab's key column.
   * @param isreadonly
   *          Boolean that means if is a read only tab or not.
   * @param control
   *          Object of type WADGrid control.
   * @param isTranslated
   *          Boolean that indicates if is a translated file (non-english).
   * @param adLanguage
   *          Language to translate.
   * @param tabNamePresentation
   *          The human name of the tab.
   * @param strTable
   *          The id of the tab's table.
   * @throws ServletException
   * @throws IOException
   */
  private void processTabHtmlRelation(FieldsData[] parentsFieldsData, File fileDir, String strTab,
      String tabName, String keyColumnName, boolean isreadonly, WADControl control,
      boolean isTranslated, String adLanguage, String tabNamePresentation, String strTable,
      String accessLevel) throws ServletException, IOException {
    log4j.debug("Procesig relation html" + (isTranslated ? " translated" : "") + ": " + strTab
        + ", " + tabName);
    final String[] discard = new String[1];
    if (parentsFieldsData.length == 0)
      discard[0] = new String("parent");
    else
      discard[0] = new String("");

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/wad/Template_Relation", discard).createXmlDocument();
    xmlDocument.setParameter("form", tabName + "_Edition.html");
    xmlDocument.setParameter("tab", tabNamePresentation);
    if (strTab.equals("445"))
      keyColumnName = "AD_Language";
    xmlDocument.setParameter("key", "inp" + Sqlc.TransformaNombreColumna(keyColumnName));
    xmlDocument.setParameter("keyId", keyColumnName);
    xmlDocument.setParameter("tabId", strTab);
    xmlDocument.setParameter("tableId", strTable);
    xmlDocument.setParameter("accesslevel", accessLevel);
    if (parentsFieldsData.length > 0) {
      xmlDocument.setParameter("keyParent",
          "inp" + Sqlc.TransformaNombreColumna(parentsFieldsData[0].name));
      xmlDocument.setParameter("parentKeyName", parentsFieldsData[0].name);
    }
    xmlDocument.setParameter("importCSS",
        getVectorElementsNotRepeated(control.getCSSImport(), new Vector<String>(), 1));
    xmlDocument.setParameter("importJS",
        getVectorElementsNotRepeated(control.getImport(), new Vector<String>(), 2));
    final StringBuffer script = new StringBuffer();
    script.append(getVectorElementsNotRepeated(control.getJSCode(), new Vector<String>(), 0));
    script.append("function validateClient(action, form, value) {\n");
    script.append("  var frm=document.frmMain;\n");
    script.append(control.getValidation()).append("\n");
    script.append("  return true;\n");
    script.append("}\n");
    xmlDocument.setParameter("script", script.toString());
    control.setData("accesslevel", accessLevel);
    xmlDocument.setParameter("controlDesign", control.toString());
    WadUtility.writeFile(fileDir, tabName + "_Relation.html", xmlDocument.print());
  }

  /**
   * Manage the Vector of imports for the html files. Is in charged of keep them with unique values.
   * 
   * @param data
   *          The main Vector.
   * @param addedElements
   *          Vector with the new values to add.
   * @param type
   *          Indicates the type of import (1=CSS, 2=JS).
   * @return String with the list of imports in html format.
   */
  private String getVectorElementsNotRepeated(Vector<String[]> data, Vector<String> addedElements,
      int type) {
    if (addedElements == null)
      addedElements = new Vector<String>();
    if (data == null)
      return "";
    final StringBuffer text = new StringBuffer();
    for (int i = 0; i < data.size(); i++) {
      final String[] aux = data.elementAt(i);
      if (!isInVector(addedElements, aux[0])) {
        addedElements.addElement(aux[0]);
        if (type == 1)
          text.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        else if (type == 2)
          text.append("<script language=\"JavaScript\" src=\"");
        text.append(aux[1]);
        if (type == 1)
          text.append("\"/>");
        else if (type == 2)
          text.append("\" type=\"text/javascript\"></script>");
        text.append("\n");
      }
    }
    return text.toString();
  }

  /**
   * Checks if a value exists inside a Vector.
   * 
   * @param data
   *          Vector with the data.
   * @param value
   *          The value to search.
   * @return Boolean to indicate if the value exists in the Vector.
   */
  private boolean isInVector(Vector<String> data, String value) {
    if (data == null)
      return false;
    if (value == null || value.equals(""))
      return false;
    for (int i = 0; i < data.size(); i++) {
      final String aux = data.elementAt(i);
      if (aux.equalsIgnoreCase(value))
        return true;
    }
    return false;
  }

  /**
   * Generates the xml file for the edition window of the tab.
   * 
   * @param fileDir
   *          Path where is gonna be created the file.
   * @param strTab
   *          Id of the tab.
   * @param tabName
   *          Name of the tab.
   * @param windowId
   *          Id of the window.
   * @param isreadonly
   *          Boolean that indicates if is a read only tab or not.
   * @param efd
   *          Array with the fields of the tab.
   * @param efdauxiliar
   *          Array with the auxiliar inputs of the tab.
   * @throws ServletException
   * @throws IOException
   */
  private void processTabXmlEdition(File fileDir, String strTab, String tabName, String windowId,
      boolean isreadonly, FieldProvider[] efd, FieldProvider[] efdauxiliar, boolean isSecondaryKey)
      throws ServletException, IOException {
    if (log4j.isDebugEnabled())
      log4j.debug("Processing edition xml: " + strTab + ", " + tabName);
    final String[] discard = { "hasOrgKey" };
    if (isSecondaryKey && !EditionFieldsData.isOrgKey(pool, strTab).equals("0")
        && !strTab.equals("170"))
      discard[0] = "";
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/wad/Configuration_Edition").createXmlDocument();

    // auxiliary inputs
    final StringBuffer htmlHidden = new StringBuffer();
    if (efdauxiliar != null) {
      for (FieldProvider auxiliaryInput : efdauxiliar) {
        final WADControl auxControl = new WADControl();
        auxControl.setData("ColumnName", auxiliaryInput.getField("columnname"));
        auxControl.setData("ColumnNameInp",
            Sqlc.TransformaNombreColumna(auxiliaryInput.getField("columnname")));
        auxControl.setReportEngine(xmlEngine);
        htmlHidden.append(auxControl.getHiddenXML()).append("\n");
      }
    }
    xmlDocument.setParameter("hiddens", htmlHidden.toString());

    final StringBuffer html = new StringBuffer();
    final StringBuffer labelsHTML = new StringBuffer();
    ArrayList<String> labels = new ArrayList<String>();

    String strFieldGroup = "";

    for (int i = 0; i < efd.length; i++) {
      WADControl auxControl = null;
      try {
        auxControl = WadUtility.getControl(pool, efd[i], isreadonly, tabName, "", xmlEngine, false,
            false, false, false, false);

      } catch (final Exception ex) {
        throw new ServletException(ex);
      }
      html.append(auxControl.toXml()).append("\n");
      auxControl.setData("AdColumnId", efd[i].getField("adColumnId"));
      final String labelXML = auxControl.toLabelXML();
      if (!labelXML.trim().equals("")) {
        String xml = auxControl.toLabelXML();
        if (!labels.contains(xml)) {
          labels.add(xml);
        }
      }

      // FieldGroups
      if (WadUtility.isNewGroup(auxControl, strFieldGroup)) {
        strFieldGroup = auxControl.getData("AD_FieldGroup_ID");
        final WADLabelControl fieldLabel = new WADLabelControl();
        fieldLabel.setLabelType(WADLabelControl.FIELD_GROUP_LABEL);
        fieldLabel.setFieldGroupId(strFieldGroup);
        fieldLabel.setColumnName(EditionFieldsData.fieldGroupName(pool, strFieldGroup));
        fieldLabel.setLinkable(false);
        final String labelXMLfg = fieldLabel.toLabelXML();
        labelsHTML.append(labelXMLfg).append("\n");
      }
    }

    for (String label : labels) {
      labelsHTML.append(label).append("\n");
    }

    xmlDocument.setParameter("fields", html.toString());

    xmlDocument.setParameter("labels", labelsHTML.toString());

    xmlDocument.setParameter("class", tabName + "_Edition.html");
    WadUtility.writeFile(fileDir, tabName + "_Edition.xml",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xmlDocument.print());

    xmlDocument.setParameter("class", tabName + "_NonEditable.html");
    WadUtility.writeFile(fileDir, tabName + "_NonEditable.xml",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xmlDocument.print());

  }

  /**
   * Generates html file for edition window of the tab.
   * 
   * @param efd
   *          Array of fields.
   * @param efdauxiliar
   *          Array of auxiliar inputs.
   * @param fileDir
   *          Path where is gonna be created the file.
   * @param strTab
   *          Id of the tab.
   * @param tabName
   *          Name of the tab.
   * @param keyColumnName
   *          Name of the tab's column name.
   * @param tabNamePresentation
   *          The human name of the tab.
   * @param windowId
   *          The id of the window.
   * @param parentsFieldsData
   *          Array of parent fields.
   * @param vecFields
   *          Vector with the select clause.
   * @param isreadonly
   *          Boolean that indicates if the tab is read only or not.
   * @param isSOTrx
   *          Indicates if the tab is a sales tab or not (Y | N).
   * @param strTable
   *          The id of the tab's table.
   * @param pixelSize
   *          The size of one pixel. To use it as a multiplier.
   * @param strLanguage
   *          The language of translation.
   * @throws ServletException
   * @throws IOException
   */
  private void processTabHtmlEdition(FieldProvider[] efd, FieldProvider[] efdauxiliar,
      File fileDir, String strTab, String tabName, String keyColumnName,
      String tabNamePresentation, String windowId, FieldsData[] parentsFieldsData,
      Vector<Object> vecFields, boolean isreadonly, String isSOTrx, String strTable,
      double pixelSize, String strLanguage, boolean editable, boolean isSecondaryKey)
      throws ServletException, IOException {
    if (log4j.isDebugEnabled())
      log4j.debug("Procesig edition html" + (strLanguage.equals("") ? "" : " translated") + ": "
          + strTab + ", " + tabName);

    final boolean isReadOnlyDefinedTab = (isreadonly && editable); // isReadOnlyDefinedTab:
    // the
    // tab is
    // defined
    // as
    // read-only
    // but
    // not
    // for
    // security
    if (!editable)
      isreadonly = true; // isreadonly: because it is not editable (for
    // security reasons) or because it is defined as
    // read-only

    final HashMap<String, String> shortcuts = new HashMap<String, String>();

    final String[] discard = { "hasOrgKey", "" };

    if (isSecondaryKey && (!EditionFieldsData.isOrgKey(pool, strTab).equals("0")))
      discard[0] = "";
    if (parentsFieldsData == null || parentsFieldsData.length == 0)
      discard[1] = "hasParent";

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/wad/Template_Edition",
        discard).createXmlDocument();
    ;
    xmlDocument.setParameter("tab", tabNamePresentation);
    xmlDocument.setParameter("form", tabName + "_Relation.html");
    xmlDocument.setParameter("key", "inp" + Sqlc.TransformaNombreColumna(keyColumnName));
    xmlDocument.setParameter("keyId", keyColumnName);
    xmlDocument.setParameter("tabId", strTab);
    xmlDocument.setParameter("tableId", strTable);

    if (parentsFieldsData != null && parentsFieldsData.length > 0)
      xmlDocument.setParameter("keyParent", parentsFieldsData[0].name);

    final Vector<Object> vecDisplayLogic = new Vector<Object>();
    final EditionFieldsData efdDl[] = EditionFieldsData.selectDisplayLogic(pool, strTab);
    if (efdDl != null) {
      for (int i = 0; i < efdDl.length; i++) {
        WadUtility.displayLogic(efdDl[i].displaylogic, vecDisplayLogic, parentsFieldsData,
            new Vector<Object>(), vecFields, windowId, new Vector<Object>());
      }
    }

    // ReadOnly logic
    final Vector<Object> vecReadOnlyLogic = new Vector<Object>();
    if (!isreadonly) {
      final EditionFieldsData efdReadOnlyLogic[] = EditionFieldsData.selectReadOnlyLogic(pool,
          strTab);
      if (efdReadOnlyLogic != null) {
        for (int i = 0; i < efdReadOnlyLogic.length; i++) {
          WadUtility.displayLogic(efdReadOnlyLogic[i].readonlylogic, vecReadOnlyLogic,
              parentsFieldsData, new Vector<Object>(), vecFields, windowId, new Vector<Object>());
        }
      }
    }

    // auxiliary inputs
    final Vector<Object> vecAuxiliar = new Vector<Object>();
    final StringBuffer htmlHidden = new StringBuffer();
    if (efdauxiliar != null) {
      for (FieldProvider auxiliaryInput : efdauxiliar) {
        final WADControl auxControl = new WADControl();
        auxControl.setData("ColumnName", auxiliaryInput.getField("columnname"));
        auxControl.setData("ColumnNameInp",
            Sqlc.TransformaNombreColumna(auxiliaryInput.getField("columnname")));
        auxControl.setReportEngine(xmlEngine);
        htmlHidden.append(auxControl.getHiddenHTML()).append("\n");
        vecAuxiliar.addElement(FormatUtilities.replace(auxiliaryInput.getField("columnname")));
      }
    }

    final FieldsData[] dataReload = FieldsData.selectValidationTab(pool, strTab);

    final Vector<Object> vecReloads = new Vector<Object>();
    if (dataReload != null && dataReload.length > 0) {
      for (int z = 0; z < dataReload.length; z++) {
        String code = dataReload[z].whereclause
            + ((!dataReload[z].whereclause.equals("") && !dataReload[z].referencevalue.equals("")) ? " AND "
                : "") + dataReload[z].referencevalue;

        if (code.equals("") && dataReload[z].type.equals("R"))
          code = "@AD_Org_ID@";
        WadUtility.getComboReloadText(code, vecFields, parentsFieldsData, vecReloads, "",
            dataReload[z].columnname);
      }
    }

    final ArrayList<String> importsCSS = new ArrayList<String>();
    ArrayList<String> usedCSSImports = new ArrayList<String>();
    final ArrayList<String> importsJS = new ArrayList<String>();
    ArrayList<String> usedJSImports = new ArrayList<String>();
    final ArrayList<String> javaScriptFunctions = new ArrayList<String>();
    ArrayList<String> usedScripts = new ArrayList<String>();
    final ArrayList<String> calendarScripts = new ArrayList<String>();

    boolean hasCalendar = false;

    final StringBuffer displayLogicFunction = new StringBuffer();
    final StringBuffer readOnlyLogicFunction = new StringBuffer();
    final StringBuffer validations = new StringBuffer();
    final StringBuffer onload = new StringBuffer();
    final StringBuffer html = new StringBuffer();
    String strFieldGroup = "";
    int fieldGroupElements = 0;
    boolean fieldGroupHasDisplayLogic = false;

    StringBuffer strGroupDisplayLogic = new StringBuffer();
    int columnType = 0;
    html.append("<tr>\n");
    html.append("  <td class=\"TableEdition_OneCell_width\"></td>\n");
    html.append("  <td class=\"TableEdition_TwoCells_width\"></td>\n");
    html.append("  <td class=\"TableEdition_OneCell_width\"></td>\n");
    html.append("  <td class=\"TableEdition_TwoCells_width\"></td>\n");
    html.append("</tr>\n");
    for (int i = 0; i < efd.length; i++) {
      WADControl auxControl = null;

      try {
        auxControl = WadUtility.getControl(pool, efd[i], isreadonly, tabName, strLanguage,
            xmlEngine, (WadUtility.isInVector(vecDisplayLogic, efd[i].getField("columnname"))),
            WadUtility.isInVector(vecReloads, efd[i].getField("columnname")),
            WadUtility.isInVector(vecReadOnlyLogic, efd[i].getField("columnname")), false,
            isReadOnlyDefinedTab);
      } catch (final Exception ex) {
        throw new ServletException(ex);
      }

      if (WadUtility.isNewGroup(auxControl, strFieldGroup)) {
        if (fieldGroupHasDisplayLogic && (fieldGroupElements != 0)) {
          strGroupDisplayLogic.append(")");
          displayLogicFunction.append(WadUtility.getDisplayLogicForGroups(strFieldGroup,
              strGroupDisplayLogic));
        }
        strGroupDisplayLogic = new StringBuffer("(");
        fieldGroupElements = 0;
        fieldGroupHasDisplayLogic = true;

        strFieldGroup = auxControl.getData("AD_FieldGroup_ID");
        html.append("      <tr><td colspan=\"4\">\n");
        html.append("      <table border=0 cellspacing=0 cellpadding=0 class=\"FieldGroup\" id=\"fldgrp"
            + strFieldGroup + "\"><tr class=\"FieldGroup_TopMargin\"></tr>\n<tr>\n");
        html.append("<td class=\"FieldGroupTitle_Left\"><img src=\"../../../../../web/images/blank.gif\" class=\"FieldGroupTitle_Left_bg\" border=0/></td>");
        String strText = "";
        strText = EditionFieldsData.fieldGroupName(pool, strFieldGroup);

        final WADLabelControl fieldLabel = new WADLabelControl();
        fieldLabel.setLabelType(WADLabelControl.FIELD_GROUP_LABEL);
        fieldLabel.setFieldGroupId(strFieldGroup);
        fieldLabel.setColumnName(strText);
        fieldLabel.setLinkable(false);

        final WadControlLabelBuilder fieldGroupLabelBuilder = new WadControlLabelBuilder(fieldLabel);
        fieldGroupLabelBuilder.buildLabelControl();
        final String labelText = fieldGroupLabelBuilder.getBasicLabelText();

        html.append("<td class=\"FieldGroupTitle\">").append(labelText).append("</td>");
        html.append("<td class=\"FieldGroupTitle_Right\"><img src=\"../../../../../web/images/blank.gif\" class=\"FieldGroupTitle_Right_bg\" border=0/></td>");
        html.append("<td class=\"FieldGroupContent\">&nbsp;</td></tr>\n<tr class=\"FieldGroup_BottomMargin\"></tr></table>");
        html.append("      </td></tr>\n");

      }

      // insert display logic for field groups
      if (auxControl.getData("IsDisplayed").equals("Y") && fieldGroupHasDisplayLogic
          && !strFieldGroup.equals("")) {
        String displ = WadUtility.displayLogic(auxControl.getData("DisplayLogic"),
            new Vector<Object>(), parentsFieldsData, vecAuxiliar, vecFields, windowId,
            new Vector<Object>());
        if (displ.length() != 0) {
          displ = "(" + displ + ")";
          if (!strGroupDisplayLogic.toString().contains(displ)) {
            strGroupDisplayLogic.append((fieldGroupElements == 0 ? "" : " || ") + displ);
            fieldGroupElements++;
          }
        } else {
          fieldGroupHasDisplayLogic = false;
        }
      }

      if (log4j.isDebugEnabled())
        log4j.debug("Column: " + auxControl.getData("ColumnName") + ", col:  " + columnType);
      // hidden inputs
      if (auxControl.getData("IsDisplayed").equals("N")) {
        htmlHidden.append(auxControl.toString()).append("\n");
      } else {
        if (auxControl instanceof WADButton) {
          ((WADButton) auxControl).setShortcuts(shortcuts);
        }
        if (auxControl.getData("IsSameLine").equals("Y")) {
          columnType = COLUMN_2_OF_2;
          html.append("<td");
        } else if (i < efd.length - 1 && efd[i + 1].getField("issameline").equals("Y")
            && efd[i + 1].getField("isdisplayed").equals("Y")) {
          columnType = COLUMN_1_OF_2;
          html.append("<tr><td");
        } else {
          columnType = COLUMN_1_OF_1;
          html.append("<tr><td");
        }
        auxControl.setData("ColumnNameLabel", auxControl.getData("ColumnName"));
        auxControl.setData("AdColumnId", efd[i].getField("adColumnId"));
        WadUtility.setLabel(pool, auxControl, isSOTrx.equals("Y"),
            "inp" + Sqlc.TransformaNombreColumna(keyColumnName));
        final String label = auxControl.toLabel();
        if (!label.equals("")) {
          html.append(" class=\"TitleCell\" id=\"").append(auxControl.getData("ColumnName"))
              .append("_lbl_td\">").append(label.replace("\n", ""));
          if (columnType == COLUMN_1_OF_1)
            html.append("</td>\n<td colspan=\"3\" class=\"").append(auxControl.getType())
                .append("_ContentCell\" id=\"").append(auxControl.getData("ColumnName"))
                .append("_inp_td\">");
          else
            html.append("</td>\n<td class=\"").append(auxControl.getType())
                .append("_ContentCell\" id=\"").append(auxControl.getData("ColumnName"))
                .append("_inp_td\">");
        } else {
          html.append(" class=\"").append(auxControl.getType()).append("_ContentCell\" id=\"")
              .append(auxControl.getData("ColumnName")).append("_inp_td\"");
          if (columnType == COLUMN_1_OF_1)
            html.append(" colspan=\"4\">");
          else
            html.append(" colspan=\"2\">");
        }

        final boolean isCombo = (auxControl.getData("AD_Reference_ID").equals("17")
            || auxControl.getData("AD_Reference_ID").equals("18") || auxControl.getData(
            "AD_Reference_ID").equals("19"));
        if (columnType == COLUMN_1_OF_1) {
          if (Integer.valueOf(auxControl.getData("DisplayLength")).intValue() < (MAX_SIZE_EDITION_1_COLUMNS / 4)) {
            auxControl.setData("DisplayLength",
                Double.toString(MAX_SIZE_EDITION_1_COLUMNS * (isCombo ? 7.5 : 1)));
            auxControl.setData("CssSize", "OneCell");
          } else if (Integer.valueOf(auxControl.getData("DisplayLength")).intValue() > (MAX_SIZE_EDITION_1_COLUMNS / 2)) {
            auxControl.setData("DisplayLength",
                Double.toString(MAX_SIZE_EDITION_1_COLUMNS * (isCombo ? 7.5 : 1)));
            auxControl.setData("CssSize", "FiveCells");
          } else {
            auxControl.setData("DisplayLength",
                Double.toString((MAX_SIZE_EDITION_1_COLUMNS / 2) * (isCombo ? 7.5 : 1)));
            auxControl.setData("CssSize", "TwoCells");
          }
        } else {
          if (Integer.valueOf(auxControl.getData("DisplayLength")).intValue() > (MAX_SIZE_EDITION_2_COLUMNS / 2)) {
            auxControl.setData("DisplayLength",
                Double.toString(MAX_SIZE_EDITION_2_COLUMNS * (isCombo ? 7.5 : 1)));
            auxControl.setData("CssSize", "TwoCells");
          } else {
            auxControl.setData("DisplayLength",
                Double.toString((MAX_SIZE_EDITION_2_COLUMNS / 2) * (isCombo ? 7.5 : 1)));
            auxControl.setData("CssSize", "OneCell");
          }
        }
        html.append(auxControl.toString());
        // end of input tag
        html.append("</td>\n");
        if (columnType == COLUMN_1_OF_1 || columnType == COLUMN_2_OF_2)
          html.append("</tr>\n");
        // Getting JavaScript
        {
          final Vector<String[]> auxJavaScript = auxControl.getJSCode();
          if (auxJavaScript != null) {
            for (String[] auxObj : auxJavaScript) {
              // Use only one import per key
              if (!usedJSImports.contains(auxObj[0])) {
                usedJSImports.add(auxObj[0]);
                javaScriptFunctions.add(auxObj[1]);
              }
            }
          }
        } // End getting JavaScript
        // Getting css imports
        {
          final Vector<String[]> auxCss = auxControl.getCSSImport();
          if (auxCss != null) {
            for (String[] auxObj : auxCss) {
              // Use only one import per key
              if (!usedCSSImports.contains(auxObj[0])) {
                usedCSSImports.add(auxObj[0]);
                importsCSS.add(auxObj[1]);
              }
            }
          }
        } // End getting css imports
        // Getting js imports
        {
          final Vector<String[]> auxJs = auxControl.getImport();
          if (auxJs != null) {
            for (String[] auxObj : auxJs) {
              if (!usedScripts.contains(auxObj[0])) {
                if (auxObj[0].startsWith("calendar")) {
                  // calendar scripts must be appended after all scripts, keep them in
                  // calendarScripts Array
                  hasCalendar = true;
                  calendarScripts.add(auxObj[1]);
                } else {
                  importsJS.add(auxObj[1]);
                }
                usedScripts.add(auxObj[0]);
              }
            }
          }
        } // End getting js imports
        if (!auxControl.getValidation().equals(""))
          validations.append(auxControl.getValidation()).append("\n");
        if (!auxControl.getOnLoad().equals(""))
          onload.append(auxControl.getOnLoad()).append("\n");
        displayLogicFunction.append(WadUtility.getDisplayLogic(auxControl, new Vector<Object>(),
            parentsFieldsData, vecAuxiliar, vecFields, windowId, new Vector<Object>(), isreadonly));
        if (!isreadonly)
          readOnlyLogicFunction.append(WadUtility.getReadOnlyLogic(auxControl,
              new Vector<Object>(), parentsFieldsData, vecAuxiliar, vecFields, windowId,
              new Vector<Object>(), isreadonly));
      }
    }

    // display logic for last group
    if (fieldGroupHasDisplayLogic && (fieldGroupElements != 0)) {
      strGroupDisplayLogic.append(")");
      displayLogicFunction.append(WadUtility.getDisplayLogicForGroups(strFieldGroup,
          strGroupDisplayLogic));
    }

    xmlDocument.setParameter("hiddenControlDesign", htmlHidden.toString());
    xmlDocument.setParameter("controlDesign", html.toString());

    final StringBuffer sbImportCSS = new StringBuffer();
    for (String imp : importsCSS) {
      sbImportCSS.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(imp)
          .append("\"/>\n");
    }
    xmlDocument.setParameter("importCSS", sbImportCSS.toString());

    final StringBuffer sbImportJS = new StringBuffer();
    if (hasCalendar) {
      // Add calendar scripts at the end
      importsJS.addAll(calendarScripts);
    }
    for (String imp : importsJS) {
      sbImportJS.append("<script language=\"JavaScript\" src=\"").append(imp)
          .append("\" type=\"text/javascript\"></script>\n");
    }
    xmlDocument.setParameter("importJS", sbImportJS.toString());

    final StringBuffer script = new StringBuffer();
    for (String imp : javaScriptFunctions) {
      script.append(imp).append("\n");
    }

    // First focused element
    String firstFocus;
    final EditionFieldsData[] ff = EditionFieldsData.selectFirstFocused(pool, strTab);
    if (ff == null || ff.length == 0) {
      firstFocus = "'firstElement'";
    } else {
      String id = ff[0].columnname;
      String reference = ff[0].reference;
      if (reference.equals("21") || reference.equals("25") || reference.equals("30")
          || reference.equals("31") || reference.equals("32") || reference.equals("35")
          || reference.equals("800013") || reference.equals("800011"))
        id += "_R";
      if (reference.equals("17") || reference.equals("18") || reference.equals("19"))
        id = "report" + id + "_S";
      firstFocus = "'" + id + "', 'id'";
    }

    final String buttonShorcuts = WadUtility.getbuttonShortcuts(shortcuts);
    script.append("\nfunction reloadComboReloads").append(strTab).append("(changedField) {\n");
    script
        .append("  submitCommandForm(changedField, false, null, '../ad_callouts/ComboReloads' + document.frmMain.inpTabId.value + '.html', 'hiddenFrame', null, null, true);\n");
    script.append("  return true;\n");
    script.append("}\n");
    script.append("\nfunction validateClient(action, form, value) {\n");
    script.append("  var frm=document.frmMain;\n");
    script.append(validations);
    script.append("  return true;\n");
    script.append("}\n");

    script.append("\nfunction displayLogic() {\n");
    script.append(displayLogicFunction);
    script.append("  return true;\n");
    script.append("}\n");

    script.append("\nfunction readOnlyLogic() {\n");
    script.append(readOnlyLogicFunction);
    script.append("  return true;\n");
    script.append("}\n");

    script.append("\nfunction onloadClient() {\n");
    script.append("  var frm=document.frmMain;\n");
    script.append("  var key = eval(\"document.frmMain.\" + frm.inpKeyName.value);");
    script.append(onload);
    script.append("  displayLogic();\n");
    script.append("  readOnlyLogic();\n");
    script.append(buttonShorcuts);
    // script.append("  setInputValue(frm.inpLastFieldChanged, \"\");\n");
    script.append("  return true;\n");
    script.append("}\n");

    script.append("\nfunction setFocusFirstElement() {\n");
    script.append("  setWindowElementFocus(").append(firstFocus).append(");\n");
    script.append("}\n");

    xmlDocument.setParameter("script", script.toString());
    if (editable)
      WadUtility.writeFile(fileDir, tabName + "_Edition.html", xmlDocument.print());
    else
      WadUtility.writeFile(fileDir, tabName + "_NonEditable.html", xmlDocument.print());
  }

  /*
   * ##########################################################################
   * ################################################### # Utilities # ########
   * ##################################################################
   * #####################################################
   */
  /**
   * Returns the subtabs for a given parent tab id. Also marks as selected one of them.
   * 
   * @param vec
   *          Vector with the subtabs.
   * @param strTabParent
   *          Id of the parent tab.
   * @param strTabSelected
   *          Id of the selected tab.
   * @param strLanguage
   *          Language to translate the names of the subtabs.
   * @throws IOException
   * @throws ServletException
   */
  private void getSubTabs(Vector<Object> vec, String strTabParent, String strTabSelected)
      throws IOException, ServletException {
    TabsData[] aux = null;
    aux = TabsData.selectSubtabs(pool, strTabParent);
    if (aux == null || aux.length <= 0)
      return;
    for (int i = 0; i < aux.length; i++) {
      vec.addElement(aux[i]);
      getSubTabs(vec, aux[i].tabid, strTabSelected);
    }
  }

  /**
   * Returns the primary tabs of a given window.
   * 
   * @param strWindowId
   *          Id of the window.
   * @param strTabSelected
   *          The selected tab.
   * @param level
   *          The level of the tab to return.
   * @param heightTabs
   *          The default height for the tabs.
   * @param incrTabs
   *          The increment over the height.
   * @return Array with the primary tabs.
   * @throws IOException
   * @throws ServletException
   */
  private TabsData[] getPrimaryTabs(String strWindowId, String strTabSelected, int level,
      int heightTabs, int incrTabs) throws IOException, ServletException {
    TabsData[] aux = null;
    TabsData[] aux1 = null;
    int mayor = 0;
    final Vector<Object> vec = new Vector<Object>();
    aux1 = TabsData.selectTabParent(pool, strWindowId);
    if (aux1 == null || aux1.length == 0)
      return null;
    for (int i = 0; i < aux1.length; i++) {
      vec.addElement(aux1[i]);
      getSubTabs(vec, aux1[i].tabid, strTabSelected);
    }
    aux = new TabsData[vec.size()];
    vec.copyInto(aux);
    for (int i = 0; i < aux.length; i++)
      if (mayor < Integer.valueOf(aux[i].tablevel).intValue())
        mayor = Integer.valueOf(aux[i].tablevel).intValue();
    for (int i = 0; i < aux.length; i++)
      debugTab(aux[i], strTabSelected, level, heightTabs, incrTabs, mayor);
    return aux;
  }

  /**
   * Assigns the correct command to the given tab.
   * 
   * @param tab
   *          Tab to manipulate.
   * @param strTab
   *          The id of the actual tab.
   * @param level
   *          The level of the actual tab.
   * @param heightTabs
   *          The height of the tab.
   * @param incrTabs
   *          The increment for the height.
   * @param mayor
   *          operand to calculate the height.
   * @throws ServletException
   */
  private void debugTab(TabsData tab, String strTab, int level, int heightTabs, int incrTabs,
      int mayor) throws ServletException {
    final String tabName = FormatUtilities.replace(tab.tabname)
        + (tab.tabmodule.equals("0") ? "" : tab.tabid);
    if (strTab.equals(tab.tabid)) {
      tab.tdClass = "";
      tab.href = "return false;";
    } else {
      tab.tdClass = "";
      tab.href = "submitCommandForm('DEFAULT', false, null, '" + tabName
          + "_Relation.html', 'appFrame');return false;";
      if ((level + 1) >= Integer.valueOf(tab.tablevel).intValue())
        tab.href = "submitCommandForm('"
            + ((level > Integer.valueOf(tab.tablevel).intValue()) ? "DEFAULT" : "TAB") + "', "
            + ((level >= Integer.valueOf(tab.tablevel).intValue()) ? "false" : "true")
            + ", null, '" + tabName + "_Relation.html', 'appFrame');return false;";
      else
        tab.href = "return false;";
    }

    final int height = ((mayor - Integer.valueOf(tab.tablevel).intValue()) * incrTabs + heightTabs);
    tab.tdHeight = Integer.toString(height);
  }

  /**
   * Returns the index of the parent tab in the given array.
   * 
   * @param allTabs
   *          Array of tabs.
   * @param tabId
   *          The id of the actual tab.
   * @return Int with the index of the parent tab or -1 if there is no parent.
   * @throws ServletException
   * @throws IOException
   */
  private int parentTabId(TabsData[] allTabs, String tabId) throws ServletException, IOException {
    if (allTabs == null || allTabs.length == 0)
      return -1;
    else if (tabId == null || tabId.equals(""))
      return -1;
    else if (tabId.equals(allTabs[0].tabid))
      return -1;
    String parentTab = "";
    for (int i = 1; i < allTabs.length; i++) {
      if (allTabs[i].tabid.equals(tabId)) {
        parentTab = allTabs[i].parentKey;
        break;
      }
    }
    if (!parentTab.equals("-1")) {
      for (int i = 0; i < allTabs.length; i++) {
        if (allTabs[i].tabid.equals(parentTab))
          return i;
      }
    }
    return -1;
  }

  /**
   * Method to prepare the XmlEngine object, which is the one in charged of the templates.
   * 
   * @param fileConnection
   *          The path to the connection file.
   */
  private void createXmlEngine(String fileConnection) {
    // pass null as connection to running the translation at compile time
    xmlEngine = new XmlEngine(null);
    xmlEngine.isResource = true;
    xmlEngine.fileBaseLocation = new File(".");
    xmlEngine.strReplaceWhat = null;
    xmlEngine.strReplaceWith = null;
    XmlEngine.strTextDividedByZero = "TextDividedByZero";
    xmlEngine.fileXmlEngineFormat = new File(fileConnection, "Format.xml");
    log4j.debug("xmlEngine format file: " + xmlEngine.fileXmlEngineFormat.getAbsoluteFile());
    xmlEngine.initialize();
  }

  /**
   * Creates an instance of the connection's pool.
   * 
   * @param strFileConnection
   *          Path where is allocated the connection file.
   */
  private void createPool(String strFileConnection) {
    pool = new WadConnection(strFileConnection);
    WADControl.setConnection(pool);
  }

  /**
   * Auxiliar method to make a copy of a FieldsData element.
   * 
   * @param from
   *          The FieldsData object to copy.
   * @return The new copy of the given FieldsData object.
   */
  private FieldsData copyarrayElement(FieldsData from) {
    final FieldsData toAux = new FieldsData();
    toAux.realname = from.realname;
    toAux.name = from.name;
    toAux.nameref = from.nameref;
    toAux.xmltext = from.xmltext;
    toAux.reference = from.reference;
    toAux.referencevalue = from.referencevalue;
    toAux.required = from.required;
    toAux.isdisplayed = from.isdisplayed;
    toAux.isupdateable = from.isupdateable;
    toAux.defaultvalue = from.defaultvalue;
    toAux.fieldlength = from.fieldlength;
    toAux.textAlign = from.textAlign;
    toAux.xmlFormat = from.xmlFormat;
    toAux.displaylength = from.displaylength;
    toAux.columnname = from.columnname;
    toAux.whereclause = from.whereclause;
    toAux.tablename = from.tablename;
    toAux.type = from.type;
    toAux.issessionattr = from.issessionattr;
    toAux.iskey = from.iskey;
    toAux.isparent = from.isparent;
    toAux.accesslevel = from.accesslevel;
    toAux.isreadonly = from.isreadonly;
    toAux.issecondarykey = from.issecondarykey;
    toAux.showinrelation = from.showinrelation;
    toAux.isencrypted = from.isencrypted;
    toAux.sortno = from.sortno;
    toAux.istranslated = from.istranslated;
    toAux.id = from.id;
    toAux.htmltext = from.htmltext;
    toAux.htmltexttrl = from.htmltexttrl;
    toAux.xmltexttrl = from.xmltexttrl;
    toAux.tablenametrl = from.tablenametrl;
    toAux.nowrap = from.nowrap;
    toAux.iscolumnencrypted = from.iscolumnencrypted;
    toAux.isdesencryptable = from.isdesencryptable;
    toAux.adReferenceValueId = from.adReferenceValueId;
    return toAux;
  }

  /**
   * Auxiliar method to copy an array of FieldsData objects.
   * 
   * @param from
   *          The array of FieldsData objects to copy.
   * @return The copy array of FieldsData objects.
   */
  private FieldsData[] copyarray(FieldsData[] from) {
    log4j.debug("Starting copyarray: " + from.length);
    if (from == null)
      return null;
    final FieldsData[] to = new FieldsData[from.length];
    for (int i = 0; i < from.length; i++) {
      log4j.debug("For copyarray");
      to[i] = copyarrayElement(from[i]);
    }
    return to;
  }

  /**
   * Method to read the Openbravo.properties file.
   * 
   * @param strFileProperties
   *          The path of the property file to read.
   */
  private void readProperties(String strFileProperties) {
    // Read properties file.
    final Properties properties = new Properties();
    try {
      log4j.info("strFileProperties: " + strFileProperties);
      properties.load(new FileInputStream(strFileProperties));
      jsDateFormat = properties.getProperty("dateFormat.js");
      log4j.info("jsDateFormat: " + jsDateFormat);
      sqlDateFormat = properties.getProperty("dateFormat.sql");
      WADControl.setDateFormat(sqlDateFormat);
      log4j.info("sqlDateFormat: " + sqlDateFormat);
    } catch (final IOException e) {
      // catch possible io errors from readLine()
      e.printStackTrace();
    }
  }

}
