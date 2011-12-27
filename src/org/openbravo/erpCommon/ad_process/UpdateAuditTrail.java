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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallProcess;

public class UpdateAuditTrail implements Process {

  private static final Logger log4j = Logger.getLogger(UpdateAuditTrail.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();

    // Check whether it is a OBPS instance
    if (!ActivationKey.isActiveInstance()) {
      String msgTxt = "@FEATURE_OBPS_ONLY@<br/><a class=\"MessageBox_TextLink\" href=\"http://www.openbravo.com/product/erp/get-basic\" target=\"_blank\">@LEARN_HOW@</a>&nbsp;@ACTIVATE_INSTANCE@";

      msgTxt = Utility.parseTranslation(
          bundle.getConnection(),
          new VariablesSecureApp(bundle.getContext().getUser(), bundle.getContext().getClient(),
              bundle.getContext().getOrganization()), bundle.getContext().getLanguage(), msgTxt)
          .replace(
              "@ProfessionalEditionType@",
              Utility.messageBD(bundle.getConnection(), "OBPSAnyEdition", bundle.getContext()
                  .getLanguage()));

      msg.setType("Info");
      msg.setTitle("");

      msg.setMessage(msgTxt);
      bundle.setResult(msg);
      return;
    }

    // Stop scheduler to prevent deadlocks id DB
    boolean schedulerInitiallyStarted = OBScheduler.getInstance().getScheduler().isStarted()
        && !OBScheduler.getInstance().getScheduler().isInStandbyMode();
    try {
      if (schedulerInitiallyStarted) {
        OBScheduler.getInstance().getScheduler().standby();
      }
    } catch (Exception e) {
      log4j.error("Error stopping scheduler", e);

      msg.setType("Error");
      msg.setTitle("@Error@");
      msg.setMessage("@ErrorStoppingScheduler@ " + e.getMessage());
      bundle.setResult(msg);
      return;
    }

    try {
      // Execute the PL process to re-generate audit triggers
      org.openbravo.model.ad.ui.Process process = OBDal.getInstance().get(
          org.openbravo.model.ad.ui.Process.class, "65D1E895C7FD47B48F3D18BC9E28BE9F");
      CallProcess cp = CallProcess.getInstance();
      ProcessInstance pi = cp.call(process, null, null);
      Long result = pi.getResult();
      msg.setMessage(pi.getErrorMsg());
      if (result == 0) {
        msg.setType("Error");
        msg.setTitle("@Error@");
      } else {
        msg.setType("Success");
        msg.setTitle("@Success@");
      }
    } catch (Exception e) {
      log4j.error("Error executing audit process", e);

      msg.setType("Error");
      msg.setTitle("@Error@");
      msg.setMessage(e.getMessage());
      bundle.setResult(msg);
    } finally {
      // Restart scheduler if it was previously started
      if (schedulerInitiallyStarted) {
        try {
          if (schedulerInitiallyStarted) {
            OBScheduler.getInstance().getScheduler().start();
          }
        } catch (Exception e) {
          log4j.error("Error starting scheduler", e);

          if (msg.getType().equals("Success")) {
            msg.setType("Warning");
            msg.setTitle("@Warning@");
          }
          msg.setMessage(msg.getMessage() + " @ErrorStartingScheduler@ " + e.getMessage());
        }
      }
      bundle.setResult(msg);
    }

  }
}
