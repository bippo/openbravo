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

package org.openbravo.erpCommon.ad_process;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.EMail;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.utils.FormatUtilities;
import org.quartz.JobExecutionException;

public class AlertProcess implements Process {

  private static int counter = 0;

  private ConnectionProvider connection;
  private ProcessLogger logger;
  private static final String SYSTEM_CLIENT_ID = "0";

  public void execute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    connection = bundle.getConnection();

    logger.log("Starting Alert Backgrouond Process. Loop " + counter + "\n");

    try {
      AlertProcessData[] alertRule = null;
      final String adClientId = bundle.getContext().getClient();

      if (adClientId.equals(SYSTEM_CLIENT_ID)) {
        // Process all clients
        alertRule = AlertProcessData.selectSQL(connection);
      } else {
        // Filter by Process Request's client
        alertRule = AlertProcessData.selectSQL(connection, adClientId);
      }

      if (alertRule != null && alertRule.length != 0) {

        for (int i = 0; i < alertRule.length; i++) {
          processAlert(alertRule[i], connection);
        }
      }
    } catch (Exception e) {
      throw new JobExecutionException(e.getMessage(), e);
    }
  }

  /**
   * @param alertRule
   * @param conn
   * @throws Exception
   */
  private void processAlert(AlertProcessData alertRule, ConnectionProvider conn) throws Exception {
    logger.log("Processing rule " + alertRule.name + "\n");

    AlertProcessData[] alert = null;

    if (!alertRule.sql.equals("")) {
      try {
        alert = AlertProcessData.selectAlert(conn, alertRule.sql);
      } catch (Exception ex) {
        logger.log("Error processing: " + ex.getMessage() + "\n");
        return;
      }
    }
    // Insert
    if (alert != null && alert.length != 0) {
      int insertions = 0;
      StringBuilder msg = new StringBuilder();
      ;

      for (int i = 0; i < alert.length; i++) {
        if (AlertProcessData
            .existsReference(conn, alertRule.adAlertruleId, alert[i].referencekeyId).equals("0")) {

          String adAlertId = SequenceIdData.getUUID();

          logger.log("Inserting alert " + adAlertId + " org:" + alert[i].adOrgId + " client:"
              + alert[i].adClientId + " reference key: " + alert[i].referencekeyId + " created"
              + alert[i].created + "\n");

          AlertProcessData.InsertAlert(conn, adAlertId, alert[i].adClientId, alert[i].adOrgId,
              alert[i].created, alert[i].createdby, alertRule.adAlertruleId, alert[i].recordId,
              alert[i].referencekeyId, alert[i].description, alert[i].adUserId, alert[i].adRoleId);
          insertions++;

          msg.append("\n\nAlert: " + alert[i].description + "\nRecord: " + alert[i].recordId);
        }
      }

      if (insertions > 0) {
        // Send mail
        AlertProcessData[] mail = AlertProcessData.prepareMails(conn, alertRule.adAlertruleId);

        if (mail != null) {
          for (int i = 0; i < mail.length; i++) {
            String head = Utility.messageBD(conn, "AlertMailHead", mail[i].adLanguage) + "\n";
            EMail email = new EMail(null, mail[i].smtphost, mail[i].mailfrom, mail[i].mailto,
                "[OB Alert] " + alertRule.name, head + msg);
            String pwd = "";
            try {
              pwd = FormatUtilities.encryptDecrypt(mail[i].requestuserpw, false);
            } catch (Exception e) {
              logger.log("Error getting user password to send the mail: " + e.getMessage() + "\n");
              logger.log("Check email password settings in Client configuration.\n");
              continue;
            }
            if (!pwd.equals("")) {
              email.setEMailUser(mail[i].requestuser, pwd);
              if ("OK".equals(email.send())) {
                logger.log("Mail sent ok.");
              } else {
                logger.log("Error sending mail.");
              }
            } else {
              logger
                  .log("Sending email skipped. Check email password settings in Client configuration.\n");
            }
          }
        }
      }
    }

    // Update
    if (!alertRule.sql.equals("")) {
      try {
        Integer count = AlertProcessData.updateAlert(conn, alertRule.adAlertruleId, alertRule.sql);
        logger.log("updated alerts: " + count + "\n");

      } catch (Exception ex) {
        logger.log("Error updating: " + ex.toString() + "\n");
      }
    }
  }
}