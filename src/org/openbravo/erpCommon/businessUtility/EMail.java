/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2012 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;

import com.sun.mail.smtp.SMTPMessage;

/**
 * Since Openbravo 3.0MP9, related to @deprecated code in AlertProcess.java
 */
@Deprecated
public class EMail {
  static Logger log4j = Logger.getLogger(EMail.class);
  private String g_from;
  private ArrayList<InternetAddress> g_to;
  private ArrayList<InternetAddress> g_cc;
  private ArrayList<InternetAddress> g_bcc;
  private ArrayList<Object> g_attachments;
  private InternetAddress g_replyTo;
  private String g_subject;
  private String g_message;
  private String g_smtpHost;
  private VariablesSecureApp g_vars;
  private EMailAuthenticator g_auth;
  private SMTPMessage g_smtpMsg;
  private String g_messageHTML;
  private boolean g_valid;

  public EMail(VariablesSecureApp vars, String smtpHost, String from, String to) {
    g_from = from;
    addTo(to);
    g_smtpHost = smtpHost;
    g_vars = vars;
  }

  public EMail(VariablesSecureApp vars, String smtpHost, String from, String to, String subject,
      String message) {
    g_from = from;
    addTo(to);
    g_subject = subject;
    g_message = message;
    g_smtpHost = smtpHost;
    g_vars = vars;
    g_valid = isValid(true);
  }

  /*
   * never used private void dumpMessage() { if (g_smtpMsg == null) return; try { Enumeration<?> e =
   * g_smtpMsg.getAllHeaderLines (); while (e.hasMoreElements ()) if (log4j.isDebugEnabled())
   * log4j.debug("- " + e.nextElement ()); } catch (MessagingException ex) { if
   * (log4j.isDebugEnabled()) log4j.error("dumpMessage" + ex); } }
   */

  protected MimeMessage getMimeMessage() {
    return g_smtpMsg;
  }

  public void setEMailUser(String username, String password) {
    if (username == null || password == null)
      log4j.warn("setEMailUser ignored - " + username + "/" + password);
    else {
      g_auth = new EMailAuthenticator(username, password);
      if (g_auth == null)
        g_valid = false;
    }
  }

  private void setEMailUser() {
    if (g_auth != null)
      return;
    String from = g_from;
    String email = g_vars.getSessionValue("#User_EMail");
    String usr = g_vars.getSessionValue("#User_EMailUser");
    String pwd = g_vars.getSessionValue("#User_EMailUserPw");
    if (from.equals(email) && usr.length() > 0 && pwd.length() > 0) {
      setEMailUser(usr, pwd);
      return;
    }
    email = g_vars.getSessionValue("#Request_EMail");
    usr = g_vars.getSessionValue("#Request_EMailUser");
    pwd = g_vars.getSessionValue("#Request_EMailUserPw");
    if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
      setEMailUser(usr, pwd);
  }

  public String send() {
    StringBuffer sb = new StringBuffer();
    Properties props = new Properties();
    props.put("mail.debug", "true");
    props.put("mail.store.protocol", "smtp");
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.host", g_smtpHost);

    setEMailUser();
    if (g_auth != null)
      log4j.debug("g_auth is not null, setting prop auth to true");
    if (g_auth != null)
      props.put("mail.smtp.auth", "true");

    Session session = Session.getInstance(props, g_auth);

    if (log4j.isDebugEnabled()) {
      session.setDebug(true);
    }

    try {
      g_smtpMsg = new SMTPMessage(session);
      g_smtpMsg.setFrom(new InternetAddress(g_from));

      InternetAddress[] address = getTos();
      if (address.length == 1)
        g_smtpMsg.setRecipient(Message.RecipientType.TO, address[0]);
      else
        g_smtpMsg.setRecipients(Message.RecipientType.TO, address);
      address = getCcs();
      if (address != null && address.length > 0)
        g_smtpMsg.setRecipients(Message.RecipientType.CC, address);
      address = getBccs();
      if (address != null && address.length > 0)
        g_smtpMsg.setRecipients(Message.RecipientType.BCC, address);
      if (g_replyTo != null)
        g_smtpMsg.setReplyTo(new Address[] { g_replyTo });
      g_smtpMsg.setSentDate(new java.util.Date());
      g_smtpMsg.setHeader("Comments", "CompiereMail");
      g_smtpMsg.setAllow8bitMIME(true);
      g_smtpMsg.setNotifyOptions(SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
      g_smtpMsg.setReturnOption(SMTPMessage.RETURN_HDRS);
      setContent();
      Transport t = session.getTransport("smtp");
      g_smtpMsg.saveChanges();
      if (log4j.isDebugEnabled())
        log4j.debug("send() - transport obtained: " + t.toString());
      String username = g_auth.getPasswordAuthentication().getUserName();
      String password = g_auth.getPasswordAuthentication().getPassword();
      if (log4j.isDebugEnabled())
        log4j.debug("send() - username and password set, username: " + username + ", password: "
            + password);
      t.connect(g_smtpHost, username, password);
      if (log4j.isDebugEnabled())
        log4j.debug("send() - transport connected: " + t.isConnected()
            + ", now to try and send....");
      t.sendMessage(g_smtpMsg, g_smtpMsg.getAllRecipients());
      t.close();
    } catch (MessagingException mex) {
      log4j.error("Exception handling in EMail.java: " + mex.toString());
      sb.append("Exception handling in EMail.java\n");

      mex.printStackTrace();
      Exception ex = mex;
      do {
        if (ex instanceof SendFailedException) {
          SendFailedException sfex = (SendFailedException) ex;
          Address[] invalid = sfex.getInvalidAddresses();
          if (invalid != null) {
            log4j.error("    ** Invalid Addresses");
            sb.append("    ** Invalid Addresses\n");
            if (invalid != null) {
              for (int i = 0; i < invalid.length; i++) {
                log4j.error("         " + invalid[i]);
                sb.append("         " + invalid[i] + "\n");
              }
            }
          }
          Address[] validUnsent = sfex.getValidUnsentAddresses();
          if (validUnsent != null) {
            log4j.error("    ** ValidUnsent Addresses");
            sb.append("    ** ValidUnsent Addresses\n");
            if (validUnsent != null) {
              for (int i = 0; i < validUnsent.length; i++) {
                log4j.error("         " + validUnsent[i]);
                sb.append("         " + validUnsent[i] + "\n");
              }
            }
          }
          Address[] validSent = sfex.getValidSentAddresses();
          if (validSent != null) {
            log4j.error("    ** ValidSent Addresses");
            sb.append("    ** ValidSent Addresses\n");
            if (validSent != null) {
              for (int i = 0; i < validSent.length; i++) {
                log4j.error("         " + validSent[i]);
                sb.append("         " + validSent[i] + "\n");
              }
            }
          }
        }
        if (ex instanceof MessagingException)
          ex = ((MessagingException) ex).getNextException();
        else
          ex = null;
      } while (ex != null);
    } catch (Exception e) {
      log4j.error("send" + e);
      return "EMail.send: " + e.getLocalizedMessage();
    }
    return (sb.toString().equals("") ? "OK" : sb.toString());
  }

  public boolean isValid() {
    return g_valid;
  }

  public boolean isValid(boolean recheck) {
    if (g_from == null || g_from.equals("")) {
      log4j.warn("EMail.isValid - From is invalid=" + g_from);
      return false;
    }
    InternetAddress ia = getTo();
    if (ia == null || ia.getAddress().length() == 0) {
      log4j.warn("EMail.isValid - To is invalid=" + g_to);
      return false;
    }
    if (g_smtpHost == null || g_smtpHost.equals("")) {
      log4j.warn("EMail.isValid - SMTP Host is invalid" + g_smtpHost);
      return false;
    }
    if (g_subject == null || g_subject.equals("")) {
      log4j.warn("EMail.isValid - Subject is invalid=" + g_subject);
      return false;
    } else {
      return true;
    }
  }

  private void setContent() throws MessagingException, IOException {
    g_smtpMsg.setSubject(getSubject());

    // Simple Message
    if (g_attachments == null || g_attachments.size() == 0) {
      if (g_messageHTML == null || g_messageHTML.length() == 0)
        g_smtpMsg.setContent(getMessageCRLF(), "text/plain; charset=UTF-8");
      else
        g_smtpMsg.setDataHandler(new DataHandler(new ByteArrayDataSource(g_messageHTML,
            "text/html; charset=UTF-8")));

      if (log4j.isDebugEnabled())
        log4j.debug("setContent(simple) " + getSubject());
    } else { // Multi part message
      // First Part - Message
      MimeBodyPart mbp_1 = new MimeBodyPart();
      mbp_1.setText("");
      if (g_messageHTML == null || g_messageHTML.length() == 0)
        mbp_1.setContent(getMessageCRLF(), "text/plain; charset=UTF-8");
      else
        mbp_1.setDataHandler(new DataHandler(new ByteArrayDataSource(g_messageHTML,
            "text/html; charset=UTF-8")));

      // Create Multipart and its parts to it
      Multipart mp = new MimeMultipart();
      mp.addBodyPart(mbp_1);
      if (log4j.isDebugEnabled())
        log4j.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

      // for all attachments
      for (int i = 0; i < g_attachments.size(); i++) {
        Object attachment = g_attachments.get(i);
        DataSource ds = null;
        if (attachment instanceof File) {
          File file = (File) attachment;
          if (file.exists())
            ds = new FileDataSource(file);
          else {
            log4j.error("setContent - File does not exist: " + file);
            continue;
          }
        } else if (attachment instanceof URL) {
          URL url = (URL) attachment;
          ds = new URLDataSource(url);
        } else if (attachment instanceof DataSource)
          ds = (DataSource) attachment;
        else {
          log4j.error("setContent - Attachement type unknown: " + attachment);
          continue;
        }
        // Attachment Part
        MimeBodyPart mbp_2 = new MimeBodyPart();
        mbp_2.setDataHandler(new DataHandler(ds));
        mbp_2.setFileName(ds.getName());
        if (log4j.isDebugEnabled())
          log4j.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
        mp.addBodyPart(mbp_2);
      }

      // Add to Message
      g_smtpMsg.setContent(mp);
    } // multi=part
  } // setContent*/

  public void setMessageHTML(String html) {
    if (html == null || html.length() == 0)
      g_valid = false;
    else {
      g_messageHTML = html;
      if (!g_messageHTML.endsWith("\n"))
        g_messageHTML += "\n";
    }
  }

  public void setMessageHTML(String subject, String message) {
    g_subject = subject;
    StringBuffer sb = new StringBuffer("<html>\n").append("<head>\n").append("<title>\n")
        .append(subject + "\n").append("</title>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");
    sb.append("<H2>").append(subject).append("</H2>\n");
    sb.append(message);
    sb.append("\n");
    sb.append("</body>\n");
    sb.append("</html>\n");
    g_messageHTML = sb.toString();
  }

  public String getMessageHTML() {
    return g_messageHTML;
  }

  public void setFrom(String newFrom) {
    if (newFrom == null || newFrom.equals("")) {
      g_valid = false;
      return;
    }
    g_from = newFrom;
  }

  public String getFrom() {
    return g_from;
  }

  public boolean addTo(String newTo) {
    if (newTo == null || newTo.length() == 0) {
      g_valid = false;
      return false;
    }
    try {
      InternetAddress ia = new InternetAddress(newTo);
      if (g_to == null)
        g_to = new ArrayList<InternetAddress>();
      g_to.add(ia);
    } catch (Exception e) {
      log4j.error("addTo - " + e.toString());
      g_valid = false;
      return false;
    }
    return true;
  }

  public InternetAddress getTo() {
    if (g_to == null || g_to.size() == 0)
      return null;
    InternetAddress ia = g_to.get(0);
    return ia;
  }

  public InternetAddress[] getTos() {
    if (g_to == null || g_to.size() == 0)
      return null;
    InternetAddress[] ias = new InternetAddress[g_to.size()];
    g_to.toArray(ias);
    return ias;
  }

  public boolean addCc(String newCc) {
    if (newCc == null || newCc.length() == 0)
      return false;
    InternetAddress ia = null;
    try {
      ia = new InternetAddress(newCc);
    } catch (Exception e) {
      log4j.error("addCc" + e);
      return false;
    }
    if (g_cc == null)
      g_cc = new ArrayList<InternetAddress>();
    g_cc.add(ia);
    return true;
  }

  public InternetAddress[] getCcs() {
    if (g_cc == null || g_cc.size() == 0)
      return null;
    InternetAddress[] ias = new InternetAddress[g_cc.size()];
    g_cc.toArray(ias);
    return ias;
  }

  public boolean addBcc(String newBcc) {
    if (newBcc == null || newBcc.length() == 0)
      return false;
    InternetAddress ia = null;
    try {
      ia = new InternetAddress(newBcc);
    } catch (Exception e) {
      log4j.error("addBcc" + e);
      return false;
    }
    if (g_bcc == null)
      g_bcc = new ArrayList<InternetAddress>();
    g_bcc.add(ia);
    return true;
  }

  public InternetAddress[] getBccs() {
    if (g_bcc == null || g_bcc.size() == 0)
      return null;
    InternetAddress[] ias = new InternetAddress[g_bcc.size()];
    g_bcc.toArray(ias);
    return ias;
  }

  public boolean setReplyTo(String newTo) {
    if (newTo == null || newTo.length() == 0)
      return false;
    InternetAddress ia = null;
    try {
      ia = new InternetAddress(newTo);
    } catch (Exception e) {
      log4j.error("setReplyTo" + e);
      return false;
    }
    g_replyTo = ia;
    return true;
  }

  public InternetAddress getReplyTo() {
    return g_replyTo;
  }

  public void setSubject(String newSubject) {
    if (newSubject == null || newSubject.length() == 0)
      g_valid = false;
    else
      g_subject = newSubject;
  }

  public String getSubject() {
    return g_subject;
  }

  public void setMessage(String newMessage) {
    if (newMessage == null || newMessage.length() == 0)
      g_valid = false;
    else {
      g_message = newMessage;
      if (!g_message.endsWith("\n"))
        g_message += "\n";
    }
  }

  public String getMessage() {
    return g_message;
  }

  public String getMessageCRLF() {
    if (g_message == null)
      return "";
    char[] chars = g_message.toCharArray();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (c == '\n') {
        int previous = i - 1;
        if (previous >= 0 && chars[previous] == '\r')
          sb.append(c);
        else
          sb.append("\r\n");
      } else
        sb.append(c);
    }
    return sb.toString();
  }

  public void setSmtpHost(String newSmtpHost) {
    if (newSmtpHost == null || newSmtpHost.length() == 0)
      g_valid = false;
    else
      g_smtpHost = newSmtpHost;
  }

  public String getSmtpHost() {
    return g_smtpHost;
  }

  public void setSmtpMessage(SMTPMessage newSmtpMsg) {
    g_smtpMsg = newSmtpMsg;
  }

  public SMTPMessage getSmtpMessage() {
    return g_smtpMsg;
  }

  public void addAttachment(File file) {
    if (file == null)
      return;
    if (g_attachments == null)
      g_attachments = new ArrayList<Object>();
    g_attachments.add(file);
  }

  public void addAttachment(URL url) {
    if (url == null)
      return;
    if (g_attachments == null)
      g_attachments = new ArrayList<Object>();
    g_attachments.add(url);
  }

  public void addAttachment(byte[] data, String type, String name) {
    ByteArrayDataSource byteArray = new ByteArrayDataSource(data, type).setName(name);
    addAttachment(byteArray);
  }

  public void addAttachment(DataSource dataSource) {
    if (dataSource == null)
      return;
    if (g_attachments == null)
      g_attachments = new ArrayList<Object>();
    g_attachments.add(dataSource);
  }
}
