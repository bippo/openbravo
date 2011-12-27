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

import org.apache.tools.ant.BuildException;
import org.openbravo.dal.core.DalInitializingTask;

/**
 * Ant task to extract modules in obx format, it calls {@link ExtractModule}
 * 
 */
public class ExtractModuleTask extends DalInitializingTask {
  private String moduleName;
  private String moduleID;

  private String propertiesFile;
  private String destDir;
  private String obDir;
  private boolean exportRD = false;

  /**
   * Initialize DAL only in case RD exportation is active
   */
  public void execute() {
    if (exportRD)
      super.execute();
    else
      doExecute();
  }

  @Override
  public void doExecute() {
    try {
      if (destDir == null || destDir.equals(""))
        destDir = getProject().getBaseDir().toString();
      if (obDir == null || obDir.equals(""))
        obDir = getProject().getBaseDir().toString();
      if (propertiesFile == null || propertiesFile.equals(""))
        propertiesFile = obDir + "/config/Openbravo.properties";

      ExtractModule em = new ExtractModule(propertiesFile, obDir);
      em.setDestDir(destDir);
      em.setExportReferenceData(exportRD);

      if (moduleID != null && !moduleID.equals(""))
        em.extract(moduleID);
      else
        em.extractName(moduleName);
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }

  @Override
  public String getPropertiesFile() {
    return propertiesFile;
  }

  @Override
  public void setPropertiesFile(String propertiesFile) {
    this.propertiesFile = propertiesFile;
  }

  public void setDestDir(String destFile) {
    this.destDir = destFile;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public void setModuleID(String moduleID) {
    this.moduleID = moduleID;
  }

  public void setObDir(String obDir) {
    this.obDir = obDir;
  }

  public void setExportRD(boolean exportRD) {
    this.exportRD = exportRD;
  }
}
