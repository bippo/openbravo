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

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalInitializingTask;

/**
 * Ant task which is the basis for the import and export of sample data during installation of
 * Openbravo. The files are read and exported from and to the src-db/database/referencedata
 * directory.
 * 
 * @author mtaal
 */
public class ReferenceDataTask extends DalInitializingTask {
  public static final String REFERENCE_DATA_DIRECTORY = "/referencedata/sampledata";
  private String clients;

  protected File getReferenceDataDir() {
    final File mainDir = new File(getProject().getBaseDir().toString()).getParentFile();
    final File referenceDir = new File(mainDir, REFERENCE_DATA_DIRECTORY);
    if (!referenceDir.exists()) {
      referenceDir.mkdirs();
    }
    return referenceDir;
  }

  public String getClients() {
    if (clients == null) {
      throw new OBException(
          "No clients defined to export, is the clients attribute set in the task definition");
    }
    return clients;
  }

  public void setClients(String clients) {
    this.clients = clients;
  }
}
