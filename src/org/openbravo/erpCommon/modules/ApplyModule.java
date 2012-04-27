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

package org.openbravo.erpCommon.modules;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.TranslationManager;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.BasicUtility;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * ApplyModule processes all modules that are in status (I)Installed or (P)Pending but not
 * (A)Applied yet (or all of them, if the property forceRefData is set to true). This process is
 * done by the execute method.
 * 
 * 
 */
public class ApplyModule {
  private static ConnectionProvider pool;
  static Logger log4j = Logger.getLogger(ApplyModule.class);
  private String obDir;
  private boolean forceRefData = false;

  public ApplyModule(ConnectionProvider cp, String dir) {
    pool = cp;
    obDir = dir;
    PropertyConfigurator.configure(obDir + "/src/log4j.lcf");
    log4j = Logger.getLogger(ApplyModule.class);
  }

  public ApplyModule(ConnectionProvider cp, String dir, boolean forceRefData) {
    pool = cp;
    obDir = dir;
    PropertyConfigurator.configure(obDir + "/src/log4j.lcf");
    log4j = Logger.getLogger(ApplyModule.class);
    this.forceRefData = forceRefData;
  }

  /**
   * Process the Installed but not applied modules, the treatement for these modules is: Translation
   * modules In case the module contains translations the process will: <br/>
   * -Sets the module language as system -Populates the trl tables calling the verify language
   * process (this is done just once for all the modules with translations.<br/>
   * -Imports the xml files with translations into trl tables.
   * 
   * Reference data modules for client system Loads the reference data in client system
   * 
   * All modules Sets them as installed
   * 
   * Uninstalled modules Deletes them
   */
  public void execute() {
    PropertyConfigurator.configure(obDir + "/src/log4j.lcf");
    if (log4j.getLevel() == null || log4j.getLevel().isGreaterOrEqual(Level.INFO)) {
      log4j.setLevel(Level.INFO);
    }
    try {
      // **************** Translation modules ************************
      // Check whether modules to install are translations
      log4j.info("Looking for translation modules");
      final ApplyModuleData[] data;
      if (!forceRefData) {
        data = ApplyModuleData.selectTranslationModules(pool);
      } else {
        data = ApplyModuleData.selectAllTranslationModules(pool);
      }

      if (data != null && data.length > 0) {
        log4j.info(data.length + " translation modules found");
        // Set language as system in case it is not already
        for (int i = 0; i < data.length; i++) {
          if (data[i].issystemlanguage.equals("N")) {
            ApplyModuleData.setSystemLanguage(pool, data[i].adLanguage);
          }
        }

        // Populate trl tables (execute verify languages)
        try {
          log4j.info("Executing verify language process");
          final String pinstance = SequenceIdData.getUUID();
          PInstanceProcessData.insertPInstance(pool, pinstance, "179", "0", "N", "0", "0", "0");

          ApplyModuleData.process179(pool, pinstance);

          final OBError myMessage = getProcessInstanceMessageSimple(pool, pinstance);
          if (myMessage.getType().equals("Error"))
            log4j.error(myMessage.getMessage());
          else
            log4j.info(myMessage.getMessage());
        } catch (final ServletException ex) {
          log4j.error("Error running verify language process", ex);
        }

        // Import language modules
        for (int i = 0; i < data.length; i++) {
          log4j.info("Importing language " + data[i].adLanguage + " from module " + data[i].name);
          TranslationManager.importTrlDirectory(pool, obDir + "/modules/" + data[i].javapackage
              + "/referencedata/translation", data[i].adLanguage, "0", null);
        }
      }
      // ************ Reference data for system client modules ************

      log4j.info("Looking for reference data modules");

      final ApplyModuleData[] ds;
      if (!forceRefData) {
        ds = ApplyModuleData.selectClientReferenceModules(pool);
      } else {
        ds = ApplyModuleData.selectAllClientReferenceModules(pool);
      }

      if (ds != null && ds.length > 0) {
        ModuleUtility.orderModuleByDependency(ds);
        // build list of reference data modules which have files to import
        List<ApplyModuleData> dsToImport = new ArrayList<ApplyModuleData>();
        for (ApplyModuleData amd : ds) {
          // Obtain dataset xml-file and check whether it is present
          String strImportFile = dataSet2ImportFilename(amd);
          File datasetFile = new File(strImportFile);
          if (datasetFile.exists()) {
            dsToImport.add(amd);
          }
        }

        log4j.info(dsToImport.size() + " reference data modules have data files");
        for (ApplyModuleData amd : dsToImport) {

          String strImportFile = dataSet2ImportFilename(amd);

          File datasetFile = new File(strImportFile);
          log4j.info("Importing data from " + amd.name + " module. Dataset: "
              + BasicUtility.wikifiedName(amd.dsName) + ".xml");

          // Import data from the xml file
          final String strXml = BasicUtility.fileToString(datasetFile.getPath());
          final DataImportService importService = DataImportService.getInstance();
          final ImportResult result = importService.importDataFromXML(
              OBDal.getInstance().get(Client.class, "0"),
              OBDal.getInstance().get(Organization.class, "0"), strXml,
              OBDal.getInstance().get(Module.class, amd.adModuleId));
          if (result.hasErrorOccured()) {
            log4j.error(result.getErrorMessages());
            if (result.getException() != null) {
              throw new OBException(result.getException());
            } else {
              throw new OBException(result.getErrorMessages());
            }
          }

          String msg = result.getWarningMessages();
          if (msg != null && msg.length() > 0)
            log4j.warn(msg);

          msg = result.getLogMessages();
          if (msg != null && msg.length() > 0)
            log4j.debug(msg);
        }
        OBDal.getInstance().commitAndClose();
      }

    } catch (final OBException e) {
      throw e;
    } catch (final Exception e) {
      e.printStackTrace();
      throw new OBException(e);
    }
  }

  /**
   * Gets untranslated result of the 'ad_language_create' pl-function call.
   * 
   * This is a very much simplified version of the 'Utility.getProcesInstanceMessage' function
   * suitable for the simple usecase as needed inside ApplyModule.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param pinstance
   *          ad_pinstance_id to look at
   * @return Object with the message.
   * @throws ServletException
   */
  private static OBError getProcessInstanceMessageSimple(ConnectionProvider conn, String pinstance)
      throws ServletException {
    PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(pool, pinstance);

    OBError myMessage = new OBError();
    if (pinstanceData != null && pinstanceData.length > 0) {
      String message = pinstanceData[0].errormsg;

      String type = "Error";
      if (pinstanceData[0].result.equals("1")) {
        type = "Success";
      }

      int errorPos = message.indexOf("@ERROR=");
      if (errorPos != -1) {
        // skip the @ERROR= marker in output
        myMessage.setMessage(message.substring(errorPos + 7));
      }
      myMessage.setType(type);
    }
    return myMessage;
  }

  /**
   * Helper function to construct the data-file name for a dataset.
   */
  private String dataSet2ImportFilename(ApplyModuleData ds) throws FileNotFoundException {
    String strPath;
    if (ds.adModuleId.equals("0"))
      strPath = obDir + "/referencedata/standard";
    else
      strPath = obDir + "/modules/" + ds.javapackage + "/referencedata/standard";

    strPath = strPath + "/" + BasicUtility.wikifiedName(ds.dsName) + ".xml";
    return strPath;
  }

  public static void main(String[] args) {
    final ApplyModule am = new ApplyModule(new CPStandAlone(
        "/ws/modularity/openbravo/config/Openbravo.properties"), "/ws/modularity/openbravo");
    am.execute();
  }
}
