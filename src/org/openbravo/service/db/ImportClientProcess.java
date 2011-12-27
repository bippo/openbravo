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
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.scheduling.ProcessBundle;

/**
 * The import client process is called from the ui. It imports the data of a new client (including
 * the client itself). It again calls the {@link DataImportService} for the actual import.
 * 
 * @author mtaal
 */

public class ImportClientProcess implements org.openbravo.scheduling.Process {

  private static final Logger log = Logger.getLogger(ExportClientProcess.class);

  /**
   * Executes the import process. The expected parameters in the bundle are clientId (denoting the
   * client) and fileLocation giving the full path location of the file with the data to import.
   */
  public void execute(ProcessBundle bundle) throws Exception {

    try {
      final String newName = (String) bundle.getParams().get("name");
      final String importAuditInfoStr = (String) bundle.getParams().get("importauditinfo");
      final boolean importAuditInfo = importAuditInfoStr != null
          && importAuditInfoStr.equalsIgnoreCase("Y");
      log.debug("Importing file using name " + newName);

      final ClientImportProcessor importProcessor = new ClientImportProcessor();
      importProcessor.setNewName((newName != null ? newName.trim() : newName));
      final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(
          getImportFile()), "UTF-8");
      final ImportResult ir = DataImportService.getInstance().importClientData(importProcessor,
          importAuditInfo, inputStreamReader);
      inputStreamReader.close();
      if (ir.hasErrorOccured()) {
        final StringBuilder sb = new StringBuilder();
        if (ir.getException() != null) {
          log.error(ir.getException().getMessage(), ir.getException());
          sb.append(ir.getException().getMessage());
        }
        if (ir.getErrorMessages() != null) {
          log.debug(ir.getErrorMessages());
          if (sb.length() > 0) {
            sb.append("\n");
          }
          sb.append(ir.getErrorMessages());
        }
        final OBError msg = new OBError();
        msg.setType("Error");
        msg.setMessage(sb.toString());
        msg.setTitle("Errors occured");
        bundle.setResult(msg);
        return;
      }
      final OBError msg = new OBError();
      msg.setType("Success");

      if (ir.getWarningMessages() != null) {
        msg.setTitle("Done with messages");
        log.debug(ir.getWarningMessages());
        msg.setMessage("Imported client data with the following messages:<br/><ul><li>"
            + ir.getWarningMessages().replaceAll("\n", "</li><li>") + "</li></ul>");
      } else {
        msg.setTitle("Done");
        msg.setMessage("Imported client data");
      }
      bundle.setResult(msg);
    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Error occurred");
      bundle.setResult(msg);
    }
  }

  private File getImportFile() {
    final File exportDir = ExportClientProcess.getExportDir();
    final File importDir = new File(exportDir, "importclient");
    if (!importDir.exists()) {
      importDir.mkdirs();
    }
    if (importDir.listFiles().length > 1) {
      throw new OBException("There is more than one file in the " + "importdirectory: "
          + importDir.getAbsolutePath() + ". Only one file is allowed to be there.");
    }
    if (importDir.listFiles().length == 0) {
      throw new OBException("There is no file (to import from) present "
          + "in the importdirectory: " + importDir.getAbsolutePath());
    }
    return importDir.listFiles()[0];
  }
}