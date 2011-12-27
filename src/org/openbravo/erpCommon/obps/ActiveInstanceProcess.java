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

package org.openbravo.erpCommon.obps;

import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.MaturityLevel;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SystemInfo;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.HeartbeatLog;
import org.openbravo.model.ad.system.System;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

public class ActiveInstanceProcess implements Process {

  private static final Logger log = Logger.getLogger(ActiveInstanceProcess.class);
  private static final String BUTLER_URL = "https://butler.openbravo.com:443/heartbeat-server/activate";
  private static final String EVALUATION_PURPOSE = "E";

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    String publicKey = (String) bundle.getParams().get("publicKey");
    String purpose = (String) bundle.getParams().get("purpose");
    String localActivationKey = (String) bundle.getParams().get("activationKey");
    Boolean activate = (Boolean) bundle.getParams().get("activate");

    OBError msg = new OBError();

    bundle.setResult(msg);

    String[] result = null;

    System sys = OBDal.getInstance().get(System.class, "0");

    boolean localActivation = localActivationKey != null && !localActivationKey.isEmpty();

    if (!localActivation) {
      String instanceNo = (String) bundle.getParams().get("instanceNo");
      if (!HttpsUtils.isInternetAvailable()) {
        msg.setType("Error");
        msg.setMessage("@WSError@");
        return;
      } else {
        if (!publicKey.equals(sys.getInstanceKey())) {
          // Changing license, do not send instance number to get a new one
          instanceNo = null;
        }
        result = send(publicKey, purpose, instanceNo, activate);
      }
    }

    if (localActivation
        || (result.length == 2 && result[0] != null && result[1] != null && result[0]
            .equals("@Success@"))) {
      // now we have the activation key, lets save it
      String activationKey;
      if (localActivation) {
        activationKey = localActivationKey;
      } else {
        activationKey = result[1];
      }

      ActivationKey ak = new ActivationKey(publicKey, activationKey);
      String nonAllowedMods = ak.verifyInstalledModules(false);
      if (!nonAllowedMods.isEmpty()) {
        msg.setType("Error");
        msg.setMessage("@LicenseWithoutAccessTo@ " + nonAllowedMods);
      } else {

        sys.setActivationKey(activationKey);
        sys.setInstanceKey(publicKey);
        ActivationKey.setInstance(ak);
        if (ak.isActive()) {
          SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
          sysInfo.setInstancePurpose(ak.getProperty("purpose"));

          sysInfo.setMaturitySearch(Integer.toString(MaturityLevel.CS_MATURITY));
          sysInfo.setMaturityUpdate(Integer.toString(MaturityLevel.CS_MATURITY));

          updateShowProductionFields("Y");

          if (ak.isTrial() && !EVALUATION_PURPOSE.equals(purpose)) {
            sysInfo.setInstancePurpose(EVALUATION_PURPOSE);
            msg.setType("Warning");
            msg.setMessage("@TrialLicenseForcedEvaluation@");
          } else {
            msg.setType("Success");
            msg.setMessage("@Success@");
          }

          // When reactivating a cloned instance insert a dummy heartbeat log so it is not detected
          // as a cloned instance anymore.
          if (HeartbeatProcess.isClonedInstance()) {
            insertDummyHBLog();
          }
        } else {
          msg.setType("Error");
          msg.setMessage(ak.getErrorMessage());
        }
      }
    } else {
      // If there is error do not save keys, thus we maintain previous ones in case they were valid
      msg.setType("Error");
      msg.setMessage(result[0]);
      log.error(result[0]);
    }

  }

  public static void updateShowProductionFields(String value) {
    String hql = "update ADPreference set searchKey = :value where property = 'showMRPandProductionFields' and module.id is null";
    Query q = OBDal.getInstance().getSession().createQuery(hql);
    q.setParameter("value", value);
    int numRows = q.executeUpdate();
    if ("Y".equals(value) && numRows == 0) {
      Preference pref = OBProvider.getInstance().get(Preference.class);
      pref.setProperty("showMRPandProductionFields");
      pref.setSearchKey(value);
      OBDal.getInstance().save(pref);
    }
  }

  /**
   * Sends the request for the activation key.
   * 
   * @param publickey
   *          Instance's public key
   * @param purpose
   *          Instance's purpose
   * @param instanceNo
   *          current instance number (for reactivation purposes)
   * @param activate
   *          activate (true) or cancel (false)
   * @return returns a String[] with 2 elements, the first one in the message (@Success@ in case of
   *         success) and the second one the activation key
   * @throws Exception
   */
  private String[] send(String publickey, String purpose, String instanceNo, boolean activate)
      throws Exception {
    log.debug("Sending request");
    String content = "publickey=" + URLEncoder.encode(publickey, "utf-8");
    content += "&purpose=" + purpose;
    if (!activate) {
      content += "&cancel=Y";
    }
    if (instanceNo != null && !instanceNo.equals(""))
      content += "&instanceNo=" + instanceNo;

    try {
      OBContext.setAdminMode();
      Module core = OBDal.getInstance().get(Module.class, "0");
      content += "&erpversion=" + core.getVersion();
    } finally {
      OBContext.restorePreviousMode();
    }

    content += "&sysId=" + URLEncoder.encode(SystemInfo.getSystemIdentifier(), "utf-8");
    content += "&dbId=" + URLEncoder.encode(SystemInfo.getDBIdentifier(), "utf-8");
    content += "&macId=" + URLEncoder.encode(SystemInfo.getMacAddress(), "utf-8");

    URL url = new URL(BUTLER_URL);
    try {
      String result = HttpsUtils.sendSecure(url, content);
      log.debug("Activation key response:" + result);

      return result.split("\n");
    } catch (Exception e) {
      String result[] = { "@HB_SECURE_CONNECTION_ERROR@", "" };
      log.error("Error connecting server", e);
      return result;
    }

  }

  public static void insertDummyHBLog() throws ServletException {
    HeartbeatLog hbLog = OBProvider.getInstance().get(HeartbeatLog.class);
    hbLog.setClient(OBDal.getInstance().get(Client.class, "0"));
    hbLog.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
    hbLog.setSystemIdentifier(SystemInfo.getSystemIdentifier());
    hbLog.setDatabaseIdentifier(SystemInfo.getDBIdentifier());
    hbLog.setMacIdentifier(SystemInfo.getMacAddress());
    OBDal.getInstance().save(hbLog);
  }
}
