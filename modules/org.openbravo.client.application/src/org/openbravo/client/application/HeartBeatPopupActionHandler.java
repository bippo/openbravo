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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.io.File;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.ddlutils.util.DBSMOBUtil;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess.HeartBeatOrRegistration;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.module.Module;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Action handler determines if the heartbeat or registration handler should be displayed.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class HeartBeatPopupActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(HeartBeatPopupActionHandler.class);
  private static final String APRM_MIGRATION_TOOL_ID = "4BD3D4B262B048518FE62496EF09D549";

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    try {
      final JSONObject result = new JSONObject();
      boolean sysAdmin = false;
      boolean isUpgrading = false;
      try {
        isUpgrading = "Y".equals(Preferences.getPreferenceValue("isUpgrading", true, "0", "0",
            null, null, null));
      } catch (PropertyException e) {
        isUpgrading = false;
      }

      boolean usingAprm = true;
      boolean exportConfigScript = false;
      if (isUpgrading) {
        OBContext.setAdminMode();
        try {
          sysAdmin = "S".equals(OBContext.getOBContext().getRole().getUserLevel());
          if (sysAdmin) {
            if (OBDal.getInstance().exists(Module.ENTITY_NAME, APRM_MIGRATION_TOOL_ID)) {
              usingAprm = new AdvPaymentMngtDao().existsAPRMReadyPreference();
            }

            // Check all applied configuration scripts are exported in 3.0
            OBCriteria<Module> qMod = OBDal.getInstance().createCriteria(Module.class);
            qMod.add(Restrictions.eq(Module.PROPERTY_TYPE, "T"));
            qMod.add(Restrictions.eq(Module.PROPERTY_ENABLED, true));
            qMod.add(Restrictions.eq(Module.PROPERTY_APPLYCONFIGURATIONSCRIPT, true));
            String obDir = OBPropertiesProvider.getInstance().getOpenbravoProperties()
                .getProperty("source.path");
            String oldScripts = "";
            for (Module mod : qMod.list()) {
              File cfScript = new File(obDir + "/modules/" + mod.getJavaPackage()
                  + "/src-db/database", "configScript.xml");
              if (cfScript.exists() && DBSMOBUtil.isOldConfigScript(cfScript)) {
                if (!oldScripts.isEmpty()) {
                  oldScripts += ", ";
                }
                oldScripts += mod.getName();
                log.info(mod.getName() + " config script is not exported in 3.0");
              }
            }

            exportConfigScript = !oldScripts.isEmpty();

            if (exportConfigScript) {
              result.put("oldConfigScripts", oldScripts);
            }
          }
        } finally {
          OBContext.restorePreviousMode();
        }
      }

      final HeartBeatOrRegistration showHeartBeatOrRegistration = HeartbeatProcess
          .isLoginPopupRequired(
              new VariablesSecureApp((HttpServletRequest) parameters
                  .get(KernelConstants.HTTP_REQUEST)), new DalConnectionProvider());

      result.put("upgradingNoAdmin", isUpgrading && !sysAdmin);
      result.put("showAPRM", isUpgrading && !usingAprm);
      result.put("showExportScripts", isUpgrading && exportConfigScript);
      result.put("showSuccessUpgrade", isUpgrading && usingAprm && !exportConfigScript);
      result.put("showInstancePurpose",
          showHeartBeatOrRegistration == HeartbeatProcess.HeartBeatOrRegistration.InstancePurpose);
      result.put("showHeartBeat",
          showHeartBeatOrRegistration == HeartbeatProcess.HeartBeatOrRegistration.HeartBeat);
      result.put("showRegistration",
          showHeartBeatOrRegistration == HeartbeatProcess.HeartBeatOrRegistration.Registration);
      return result;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}
