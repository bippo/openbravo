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

package org.openbravo.service.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.system.Client;
import org.openbravo.scheduling.ProcessBundle;

/**
 * The export client process is called from the ui. It exports all the data from one client using a
 * specific dataset. It again calls the {@link DataExportService} for the actual export.
 * 
 * @author mtaal
 */

public class ExportClientProcess implements org.openbravo.scheduling.Process {

  /** The filename of the export file with client data. */
  public static final String CLIENT_DATA_FILE_NAME = "client_data.xml";

  /** The directory within WEB-INF in which the export file is placed. */
  public static final String EXPORT_DIR_NAME = "referencedata";

  /**
   * Returns the export file into which the xml is written or from the export can be read.
   */
  public static File getExportDir() {

    // determine the location where to place the file
    final String sourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path");
    Check.isNotNull(sourcePath, "The source.path property is not defined in the "
        + "Openbravo.properties file or the Openbravo.properties " + "file can not be found.");
    final File exportDir = new File(sourcePath, EXPORT_DIR_NAME);
    if (!exportDir.exists()) {
      log.debug("Exportdir " + exportDir.getAbsolutePath() + " does not exist, creating it");
      exportDir.mkdirs();
    }

    return exportDir;
  }

  private static final Logger log = Logger.getLogger(ExportClientProcess.class);

  /**
   * Executes the export process. The expected parameters in the bundle are clientId (denoting the
   * client) and fileLocation giving the full path location of the file in which the data for the
   * export should go.
   */
  public void execute(ProcessBundle bundle) throws Exception {
    try {
      final String clientId = (String) bundle.getParams().get("adClientId");
      final String exportAuditInfoStr = (String) bundle.getParams().get("exportauditinfo");
      final boolean exportAuditInfo = exportAuditInfoStr != null
          && exportAuditInfoStr.equalsIgnoreCase("Y");
      if (clientId == null) {
        throw new OBException(
            "Parameter adClientId not present, is the Client combo displayed in the window?");
      }

      log.debug("Exporting client " + clientId);

      // setting parameter for querying
      final Map<String, Object> params = new HashMap<String, Object>();
      params.put(DataExportService.CLIENT_ID_PARAMETER_NAME, clientId);
      log.debug("Reading data from database into in-mem xml string");

      final File exportFile = new File(getExportDir(), CLIENT_DATA_FILE_NAME);
      final OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(exportFile),
          "UTF-8");
      // write the xml to a file in WEB-INF
      log.debug("Writing export file " + exportFile.getAbsolutePath());
      DataExportService.getInstance().exportClientToXML(params, exportAuditInfo, fw);
      fw.close();

      final Client client = OBDal.getInstance().get(Client.class, clientId);

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setMessage("Client " + client.getName() + " has been exported to "
          + exportFile.getAbsolutePath());
      msg.setTitle("Done");
      bundle.setResult(msg);

    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Done with Errors");
      bundle.setResult(msg);
    }
  }
}