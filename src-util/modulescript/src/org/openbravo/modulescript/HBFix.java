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
package org.openbravo.modulescript;

import org.openbravo.database.ConnectionProvider;

/**
 * ModuleScript that fixes the Process Request for Heartbeat. In some cases the process request is
 * in status "misfired" (MIS) and the so it won't be schedule by OBScheduler. This scripts fixes the
 * data for instances that have Hearbeat enabled.
 * 
 */
public class HBFix extends ModuleScript {

  private static String HB_PROCESS_ID = "1005800000";
  private static String OB_CONTEXT = "{\"org.openbravo.scheduling.ProcessContext\":{\"user\":100,\"role\":0,"
      + "\"language\":\"en_US\",\"theme\":\"ltr\\/Default\",\"client\":0,"
      + "\"organization\":0,\"warehouse\":\"\",\"command\":\"DEFAULT\",\"userClient\":\"''0''\","
      + "\"userOrganization\":\"''0''\",\"dbSessionID\":\"\",\"javaDateFormat\":\"MM-dd-yyyy\","
      + "\"javaDateTimeFormat\":\"MM-dd-yyyy HH:mm:ss\",\"jsDateFormat\":\"%m-%d-%Y\","
      + "\"sqlDateFormat\":\"MM-DD-YYYY\",\"accessLevel\":4,\"roleSecurity\":true}}";

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      String adProcessRequestId = "";

      boolean isHeartbeatActive = HBFixData.select(cp).equalsIgnoreCase("Y");

      if (!isHeartbeatActive) {
        return;
      }

      HBFixData scheduled[] = HBFixData.selectScheduled(cp, HB_PROCESS_ID);

      if (scheduled.length > 0) {
        return;
      }

      HBFixData unsheduled[] = HBFixData.selectUnscheduled(cp, HB_PROCESS_ID);

      if (unsheduled.length > 0) {

        adProcessRequestId = unsheduled[0].adProcessRequestId;

        HBFixData.updateToScheduled(cp, adProcessRequestId);

        HBFixData.deleteDuplicated(cp, adProcessRequestId, HB_PROCESS_ID);

        return;

      }

      HBFixData misfired[] = HBFixData.selectMisfired(cp, HB_PROCESS_ID);

      if (misfired.length > 0) {
        adProcessRequestId = misfired[0].adProcessRequestId;

        HBFixData.updateToScheduled(cp, adProcessRequestId);

        HBFixData.deleteDuplicated(cp, adProcessRequestId, HB_PROCESS_ID);

        return;
      }

      HBFixData.insert(cp, OB_CONTEXT);

    } catch (Exception e) {
      handleError(e);
    }
  }

}
