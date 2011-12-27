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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.Session;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

/**
 * This process kills the session passed in the AD_Session_ID parameter.
 * 
 */
public class KillSession implements Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    String sessionId = (String) bundle.getParams().get("AD_Session_ID");
    OBError msg = new OBError();
    if (bundle.getContext().getDbSessionID().equals(sessionId)) {
      // do not kill current session
      msg.setType("Error");
      msg.setMessage("@NotAllowedToKillCurrentSession@");
      bundle.setResult(msg);
    } else {
      Session session = OBDal.getInstance().get(Session.class, sessionId);
      session.setSessionActive(false);
      msg.setType("Success");
      msg.setMessage("@Success@");
    }
    bundle.setResult(msg);

  }
}
