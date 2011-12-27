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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;

/**
 * Obtains the current instance version. In case Template 3.0 is installed, it is used to calculate
 * version. If it is not installed, core is used for this purpose.
 * 
 */
public class OBVersion {
  private static String TEMPLATE_3_0 = "0138E7A89B5E4DC3932462252801FFBC";
  private static String CORE = "0";
  private Module versionModule;
  private Module core;

  private static final OBVersion instance = new OBVersion();

  public static OBVersion getInstance() {
    return instance;
  }

  private OBVersion() {
    OBContext.setAdminMode();
    try {
      core = OBDal.getInstance().get(Module.class, CORE);
      versionModule = OBDal.getInstance().get(Module.class, TEMPLATE_3_0);
      if (versionModule == null) {
        versionModule = core;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getMP() {
    return versionModule.getVersionLabel() == null ? "" : versionModule.getVersionLabel();
  }

  public String getVersionId() {
    return core.getVersionID();
  }

  public String getVersionNumber() {
    return versionModule.getVersion();
  }

  public String getMajorVersion() {
    String ver = versionModule.getVersion();
    return ver.substring(0, ver.lastIndexOf("."));
  }

  public boolean is30() {
    return getMajorVersion().startsWith("3.0");
  }
}
