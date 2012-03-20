/*
 * ************************************************************************ The
 * contents of this file are subject to the Openbravo Public License Version 1.1
 * (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2012 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 * ***********************************************************************
 */
package org.openbravo.erpCommon.utility.poc;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.utils.FormatUtilities;

public class EmailManager {
  private static Logger log4j = Logger.getLogger(EmailManager.class);

  /*
	 * 
	 */
  public static Session newMailSession(ConnectionProvider connectionProvider, String clientId,
      String adOrgId) throws PocException, ServletException {
    PocConfigurationData configurations[];
    try {
      configurations = PocConfigurationData.getSmtpDetails(connectionProvider, clientId, adOrgId);
    } catch (ServletException exception) {
      throw new PocException(exception);
    }

    PocConfigurationData configuration = null;
    if (configurations.length > 0) {
      configuration = configurations[0];
      if (log4j.isDebugEnabled()) {
        log4j.debug("Crm configuration, smtp server: " + configuration.smtpserver);
        log4j.debug("Crm configuration, smtp server auth: " + configuration.issmtpauthorization);
        log4j.debug("Crm configuration, smtp server account: " + configuration.smtpserveraccount);
        log4j.debug("Crm configuration, smtp server password: " + configuration.smtpserverpassword);
        log4j.debug("Crm configuration, smtp server connection security: "
            + configuration.smtpconnectionsecurity);
        log4j.debug("Crm configuration, smtp server port: " + configuration.smtpport);
      }
    } else {
      throw new ServletException("No Poc configuration found for this client.");
    }

    Properties props = new Properties();

    if (log4j.isDebugEnabled()) {
      props.put("mail.debug", "true");
    }
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.host", configuration.smtpserver);
    props.put("mail.smtp.auth", (configuration.issmtpauthorization.equals("Y") ? "true" : "false"));
    props.put("mail.smtp.mail.sender", "email_admin@openbravo.com");
    props.put("mail.smtp.port", configuration.smtpport);
    if (configuration.smtpconnectionsecurity.equals("STARTTLS")) {
      props.put("mail.smtp.starttls.enable", "true");
    } else if (configuration.smtpconnectionsecurity.equals("SSL")) {
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.put("mail.smtp.socketFactory.fallback", "false");
      props.put("mail.smtp.socketFactory.port", configuration.smtpport);
    }

    ClientAuthenticator authenticator = null;
    if (configuration.smtpserveraccount != null) {
      authenticator = new ClientAuthenticator(configuration.smtpserveraccount,
          FormatUtilities.encryptDecrypt(configuration.smtpserverpassword, false));
    }

    return Session.getInstance(props, authenticator);
  }

  /*
	 * 
	 */
  public void sendSimpleEmail(Session session, String from, String to, String bcc, String subject,
      String body, String attachmentFileLocations) throws PocException {
    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));

      message.setRecipients(Message.RecipientType.TO, getAddressesFrom(to.split(",")));

      if (bcc != null)
        message.setRecipients(Message.RecipientType.BCC, getAddressesFrom(bcc.split(",")));

      message.setSubject(subject);

      // Content consists of 2 parts, the message body and the attachment
      // We therefore use a multipart message
      Multipart multipart = new MimeMultipart();

      // Create the message part
      MimeBodyPart messageBodyPart = new MimeBodyPart();
      messageBodyPart.setText(body);
      multipart.addBodyPart(messageBodyPart);

      // Create the attachment parts
      if (attachmentFileLocations != null) {
        String attachments[] = attachmentFileLocations.split(",");

        for (String attachment : attachments) {
          messageBodyPart = new MimeBodyPart();
          DataSource source = new FileDataSource(attachment);
          messageBodyPart.setDataHandler(new DataHandler(source));
          messageBodyPart.setFileName(attachment.substring(attachment.lastIndexOf("/") + 1));
          multipart.addBodyPart(messageBodyPart);
        }
      }

      message.setContent(multipart);

      // Send the email
      Transport.send(message);
    } catch (AddressException exception) {
      throw new PocException(exception);
    } catch (MessagingException exception) {
      throw new PocException(exception);
    }
  }

  private InternetAddress[] getAddressesFrom(String[] textualAddresses) {
    InternetAddress internetAddresses[] = new InternetAddress[textualAddresses.length];
    for (int index = 0; index < textualAddresses.length; index++) {
      try {
        internetAddresses[index] = new InternetAddress(textualAddresses[index]);
      } catch (AddressException e) {
        if (log4j.isDebugEnabled())
          log4j.debug("Could not create a valid email for: " + textualAddresses[index]
              + ". Address ignored");
      }
    }
    return internetAddresses;
  }

}
