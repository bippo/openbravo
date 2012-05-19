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
 * All portions are Copyright (C) 2008-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.alert.AlertRecipient;
import org.openbravo.model.ad.alert.AlertRule;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.utils.FormatUtilities;
import org.quartz.JobExecutionException;

public class AlertProcess implements Process {

  private static final Logger log4j = Logger.getLogger(AlertProcess.class);

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
    } finally {
      OBDal.getInstance().commitAndClose();
    }
  }

  /**
   * @param alertRule
   * @param conn
   * @throws Exception
   */
  @SuppressWarnings({ "unchecked", "deprecation" })
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

        // There are two ways of sending the email, depending if the SMTP server is configured in
        // the 'Client' tab or in the 'Email Configuration' tab.
        // The SMTP server configured in 'Client' tab way is @Deprecated in 3.0

        final String adClientId = alertRule.adClientId;
        final String adOrgId = alertRule.adOrgId;
        final String deprecatedMailHost = OBDal.getInstance().get(Client.class, adClientId)
            .getMailHost();
        boolean isDeprecatedMode = false;
        if (deprecatedMailHost != null && !"".equals(deprecatedMailHost)) {
          isDeprecatedMode = true;
        }

        if (!isDeprecatedMode) {
          // Since it is a background process and each email sending takes some time (may vary
          // depending on the server), they are sent at the end, once all data is recollected, in
          // order to minimize problems/inconsistencies/NPE if the 'Alerts', 'AlertRecipient',
          // 'User' or 'UserRoles' columns change in the middle of the process.
          final List<Object[]> emailsToSendList = new ArrayList<Object[]>();
          OBContext.setAdminMode();
          try {
            // Getting the SMTP server parameters
            OBCriteria<EmailServerConfiguration> mailConfigCriteria = OBDal.getInstance()
                .createCriteria(EmailServerConfiguration.class);
            mailConfigCriteria.add(Restrictions.eq(EmailServerConfiguration.PROPERTY_CLIENT, OBDal
                .getInstance().get(Client.class, adClientId)));
            mailConfigCriteria.setFilterOnReadableClients(false);
            mailConfigCriteria.setFilterOnReadableOrganization(false);
            final List<EmailServerConfiguration> mailConfigList = mailConfigCriteria.list();

            if (mailConfigList.size() > 0) {
              // TODO: There should be a mechanism to select the desired Email server configuration
              // for alerts, until then, first search for the current organization (and use the
              // first returned one), then for organization '0' (and use the first returned one) and
              // then for any other of the organization tree where current organization belongs to
              // (and use the first returned one).
              EmailServerConfiguration mailConfig = null;

              for (EmailServerConfiguration currentOrgConfig : mailConfigList) {
                if (adOrgId.equals(currentOrgConfig.getOrganization().getId())) {
                  mailConfig = currentOrgConfig;
                  break;
                }
              }
              if (mailConfig == null) {
                for (EmailServerConfiguration zeroOrgConfig : mailConfigList) {
                  if ("0".equals(zeroOrgConfig.getOrganization().getId())) {
                    mailConfig = zeroOrgConfig;
                    break;
                  }
                }
              }
              if (mailConfig == null) {
                mailConfig = mailConfigList.get(0);
              }

              OBCriteria<AlertRecipient> alertRecipientsCriteria = OBDal.getInstance()
                  .createCriteria(AlertRecipient.class);
              alertRecipientsCriteria.add(Restrictions.eq(AlertRecipient.PROPERTY_ALERTRULE, OBDal
                  .getInstance().get(AlertRule.class, alertRule.adAlertruleId)));
              alertRecipientsCriteria.setFilterOnReadableClients(false);
              alertRecipientsCriteria.setFilterOnReadableOrganization(false);

              final List<AlertRecipient> alertRecipientsList = alertRecipientsCriteria.list();

              // Mechanism to avoid several mails are sent to the same email address for the same
              // alert
              List<String> alreadySentToList = new ArrayList<String>();
              for (AlertRecipient currentAlertRecipient : alertRecipientsList) {
                // If 'Send EMail' option is not checked, we are done for this alert recipient
                if (!currentAlertRecipient.isSendEMail()) {
                  continue;
                }

                final List<User> usersList = new ArrayList<User>();
                // If there is a 'Contact' established, take it, if not, take all users for the
                // selected 'Role'
                if (currentAlertRecipient.getUserContact() != null) {
                  usersList.add(currentAlertRecipient.getUserContact());
                } else {
                  OBCriteria<UserRoles> userRolesCriteria = OBDal.getInstance().createCriteria(
                      UserRoles.class);
                  userRolesCriteria.add(Restrictions.eq(AlertRecipient.PROPERTY_ROLE,
                      currentAlertRecipient.getRole()));
                  userRolesCriteria.add(Restrictions.eq(AlertRecipient.PROPERTY_CLIENT,
                      currentAlertRecipient.getClient()));
                  final List<UserRoles> userRolesList = userRolesCriteria.list();
                  for (UserRoles currenUserRole : userRolesList) {
                    usersList.add(currenUserRole.getUserContact());
                  }
                }

                // If there are no 'Contact' for send the email, we are done for this alert
                // recipient
                if (usersList.size() == 0) {
                  continue;
                }

                // For each 'User', get the email parameters (to, subject, body, ...) and store them
                // to send the email at the end
                for (User targetUser : usersList) {
                  if (targetUser == null) {
                    continue;
                  }
                  final Client targetUserClient = targetUser.getClient();
                  final String targetUserClientLanguage = (targetUserClient.getLanguage() != null ? targetUserClient
                      .getLanguage().getLanguage() : null);
                  final String targetUserEmail = targetUser.getEmail();
                  if (targetUserEmail == null) {
                    continue;
                  }

                  boolean repeatedEmail = false;
                  for (String alreadySentTo : alreadySentToList) {
                    if (targetUserEmail.equals(alreadySentTo)) {
                      repeatedEmail = true;
                      break;
                    }
                  }
                  if (repeatedEmail) {
                    continue;
                  }
                  alreadySentToList.add(targetUserEmail);

                  final String host = mailConfig.getSmtpServer();
                  final Boolean auth = mailConfig.isSMTPAuthentification();
                  final String username = mailConfig.getSmtpServerAccount();
                  final String password = FormatUtilities.encryptDecrypt(
                      mailConfig.getSmtpServerPassword(), false);
                  final String connSecurity = mailConfig.getSmtpConnectionSecurity();
                  final int port = mailConfig.getSmtpPort().intValue();
                  final String senderAddress = mailConfig.getSmtpServerSenderAddress();
                  final String recipientTO = targetUserEmail;
                  final String recipientCC = null;
                  final String recipientBCC = null;
                  final String replyTo = null;
                  final String subject = "[OB Alert] " + alertRule.name;
                  final String content = Utility.messageBD(conn, "AlertMailHead",
                      targetUserClientLanguage) + "\n" + msg;
                  final String contentType = "text/plain; charset=utf-8";
                  final List<File> attachments = null;
                  final Date sentDate = null;
                  final List<String> headerExtras = null;

                  final Object[] email = { host, auth, username, password, connSecurity, port,
                      senderAddress, recipientTO, recipientCC, recipientBCC, replyTo, subject,
                      content, contentType, attachments, sentDate, headerExtras };
                  emailsToSendList.add(email);
                }
              }
            }
          } catch (Exception e) {
            throw new JobExecutionException(e.getMessage(), e);
          } finally {
            OBContext.restorePreviousMode();
          }
          // Send all the stored emails
          for (Object[] emailToSend : emailsToSendList) {
            try {
              EmailManager.sendEmail((String) emailToSend[0],
                  ((Boolean) emailToSend[1]).booleanValue(), (String) emailToSend[2],
                  (String) emailToSend[3], (String) emailToSend[4],
                  ((Number) emailToSend[5]).intValue(), (String) emailToSend[6],
                  (String) emailToSend[7], (String) emailToSend[8], (String) emailToSend[9],
                  (String) emailToSend[10], (String) emailToSend[11], (String) emailToSend[12],
                  (String) emailToSend[13], (List<File>) emailToSend[14], (Date) emailToSend[15],
                  (List<String>) emailToSend[16]);
            } catch (Exception exception) {
              log4j.error(exception);
              final String exceptionClass = exception.getClass().toString().replace("class ", "");
              String exceptionString = "Problems while sending the email" + exception;
              exceptionString = exceptionString.replace(exceptionClass, "");
              throw new ServletException(exceptionString);
            }
          }
        } else {
          // @Deprecated : This full "else" statement is deprecated from OB 3.0MP9. It happens only
          // when there is an email configured directly in the AD_CLIENT (new way is configure it in
          // C_POC_CONFIGURATION)
          AlertProcessData[] mail = AlertProcessData.prepareMails(conn, alertRule.adAlertruleId);

          if (mail != null) {
            for (int i = 0; i < mail.length; i++) {
              String head = Utility.messageBD(conn, "AlertMailHead", mail[i].adLanguage) + "\n";
              org.openbravo.erpCommon.businessUtility.EMail email = new org.openbravo.erpCommon.businessUtility.EMail(
                  null, mail[i].smtphost, mail[i].mailfrom, mail[i].mailto, "[OB Alert] "
                      + alertRule.name, head + msg);
              String pwd = "";
              try {
                pwd = FormatUtilities.encryptDecrypt(mail[i].requestuserpw, false);
              } catch (Exception e) {
                logger
                    .log("Error getting user password to send the mail: " + e.getMessage() + "\n");
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