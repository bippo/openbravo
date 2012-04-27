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

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
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

  public static void sendEmail(String host, boolean auth, String username, String password,
      String connSecurity, int port, String senderAddress, String recipientTO, String recipientCC,
      String recipientBCC, String replyTo, String subject, String content, String contentType,
      List<File> attachments, Date sentDate, List<String> headerExtras) throws Exception {
    try {
      Properties props = new Properties();

      if (log4j.isDebugEnabled()) {
        props.put("mail.debug", "true");
      }
      props.put("mail.transport.protocol", "smtp");
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.port", port);

      if (connSecurity != null) {
        connSecurity = connSecurity.replaceAll(", *", ",");
        String[] connSecurityArray = connSecurity.split(",");
        for (int i = 0; i < connSecurityArray.length; i++) {
          if ("STARTTLS".equals(connSecurityArray[i])) {
            props.put("mail.smtp.starttls.enable", "true");
          }
          if ("SSL".equals(connSecurityArray[i])) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.socketFactory.port", port);
          }
        }
      }

      Session mailSession = null;
      if (auth) {
        props.put("mail.smtp.auth", "true");
        Authenticator authentification = new SMTPAuthenticator(username, password);
        mailSession = Session.getInstance(props, authentification);
      } else {
        mailSession = Session.getInstance(props, null);
      }

      Transport transport = mailSession.getTransport();

      MimeMessage message = new MimeMessage(mailSession);

      message.setFrom(new InternetAddress(senderAddress));

      if (recipientTO != null) {
        recipientTO = recipientTO.replaceAll(";", ",");
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientTO));
      }
      if (recipientCC != null) {
        recipientCC = recipientCC.replaceAll(";", ",");
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(recipientCC));
      }
      if (recipientBCC != null) {
        recipientBCC = recipientBCC.replaceAll(";", ",");
        message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(recipientBCC));
      }

      if (replyTo != null) {
        replyTo = replyTo.replaceAll(";", ",");
        replyTo = replyTo.replaceAll(", *", ",");
        String[] replyToArray = replyTo.split(",");

        Address[] replyToAddresses = new InternetAddress[replyToArray.length];
        for (int i = 0; i < replyToArray.length; i++) {
          replyToAddresses[i] = new InternetAddress(replyToArray[i]);
        }

        message.setReplyTo(replyToAddresses);
      }

      if (subject != null) {
        message.setSubject(subject);
      }
      if (sentDate != null) {
        message.setSentDate(sentDate);
      }

      if (headerExtras != null && headerExtras.size() > 0) {
        String[] headerExtrasArray = headerExtras.toArray(new String[headerExtras.size()]);
        for (int i = 0; i < headerExtrasArray.length - 1; i++) {
          message.addHeader(headerExtrasArray[i], headerExtrasArray[i + 1]);
          i++;
        }
      }

      if (attachments != null && attachments.size() > 0) {
        Multipart multipart = new MimeMultipart();

        if (content != null) {
          MimeBodyPart messagePart = new MimeBodyPart();
          if (contentType == null) {
            contentType = "text/plain; charset=utf-8";
          }
          messagePart.setContent(content, contentType);
          multipart.addBodyPart(messagePart);
        }

        MimeBodyPart attachmentPart = null;
        for (File attachmentFile : attachments) {
          attachmentPart = new MimeBodyPart();
          if (attachmentFile.exists() && attachmentFile.canRead()) {
            attachmentPart.attachFile(attachmentFile);
            multipart.addBodyPart(attachmentPart);
          }
        }

        message.setContent(multipart);
      } else {
        if (content != null) {
          if (contentType == null) {
            contentType = "text/plain; charset=utf-8";
          }
          message.setContent(content, contentType);
        }
      }

      transport.connect();
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
    } catch (final AddressException exception) {
      log4j.error(exception);
      throw new ServletException(exception);
    } catch (final MessagingException exception) {
      log4j.error(exception);
      throw new ServletException(exception.getMessage(), exception);
    }
  }

  private static class SMTPAuthenticator extends javax.mail.Authenticator {
    private String _username;
    private String _password;

    public SMTPAuthenticator(String username, String password) {
      _username = username;
      _password = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(_username, _password);
    }
  }

  /**
   * Since Openbravo 3.0MP9 only {@link #sendEmail()} is used for the full email sending cycle
   */
  @Deprecated
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

  /**
   * Since Openbravo 3.0MP9 only {@link #sendEmail()} is used for the full email sending cycle
   */
  @Deprecated
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

  /**
   * Since Openbravo 3.0MP9 only {@link #sendEmail()} is used for the full email sending cycle
   */
  @Deprecated
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
