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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Element;
import org.openbravo.model.ad.ui.ElementTrl;
import org.openbravo.model.ad.ui.Message;
import org.openbravo.model.ad.ui.MessageTrl;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.utils.Replace;

public class OBMessageUtils {
  static Logger log4j = Logger.getLogger(OBMessageUtils.class);

  /**
   * Translate the given code into some message from the application dictionary. It searches first
   * in AD_Message table and if there are not matchings then in AD_Element table.
   * 
   * @param strCode
   *          String with the search key to search.
   * @return String with the translated message.
   */
  public static String messageBD(String strCode) {
    String strMessage = "";
    final String strLanguageId = OBContext.getOBContext().getLanguage().getId();

    // Search strCode in AD_Message table.
    try {
      OBContext.setAdminMode(false);
      log4j.debug("messageBD - Message Code: " + strCode);
      OBCriteria<Message> obcMessage = OBDal.getInstance().createCriteria(Message.class);
      obcMessage.add(Restrictions.eq(Message.PROPERTY_SEARCHKEY, strCode).ignoreCase());
      if (obcMessage.count() > 0) {
        Message msg = obcMessage.list().get(0);
        strMessage = msg.getMessageText();
        for (MessageTrl msgTrl : msg.getADMessageTrlList()) {
          if (DalUtil.getId(msgTrl.getLanguage()).equals(strLanguageId)) {
            strMessage = msgTrl.getMessageText();
            break;
          }
        }
      }
    } catch (final Exception ignore) {
      log4j.error("Error getting message", ignore);
    } finally {
      OBContext.restorePreviousMode();
    }
    log4j.debug("messageBD - Message description: " + strMessage);
    // if message is still empty search in AD_Element
    if ("".equals(strMessage)) {
      try {
        OBContext.setAdminMode(false);
        OBCriteria<Element> obcElement = OBDal.getInstance().createCriteria(Element.class);
        obcElement.add(Restrictions.eq(Element.PROPERTY_DBCOLUMNNAME, strCode).ignoreCase());
        if (obcElement.count() > 0) {
          Element element = obcElement.list().get(0);
          strMessage = element.getName();
          for (ElementTrl elementTrl : element.getADElementTrlList()) {
            if (DalUtil.getId(elementTrl.getLanguage()).equals(strLanguageId)) {
              strMessage = elementTrl.getName();
            }
          }
        }
      } catch (final Exception e) {
        log4j.error("Error getting message", e);
        strMessage = strCode;
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    if ("".equals(strMessage)) {
      strMessage = strCode;
    }
    strMessage = Replace.replace(Replace.replace(strMessage, "\n", "\\n"), "\"", "&quot;");
    return strMessage;
  }

  /**
   * @see OBMessageUtils#messageBD(ConnectionProvider, String, String, boolean)
   */
  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage) {
    return BasicUtility.messageBD(conn, strCode, strLanguage, true);
  }

  /**
   * Translate the given code into some message from the application dictionary.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param strCode
   *          String with the code to search.
   * @param strLanguage
   *          String with the translation language.
   * @param escape
   *          Escape \n and " characters
   * @return String with the translated message.
   */
  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage,
      boolean escape) {
    return BasicUtility.messageBD(conn, strCode, strLanguage, escape);
  }

  /**
   * 
   * Formats a message String into a String for html presentation. Escapes the &, <, >, " and ®, and
   * replace the \n by <br/>
   * and \r for space.
   * 
   * IMPORTANT! : this method is designed to transform the output of Utility.messageBD method, and
   * this method replaces \n by \\n and \" by &quote. Because of that, the first replacements revert
   * this previous replacements.
   * 
   * @param message
   *          message with java formating
   * @return html format message
   */
  public static String formatMessageBDToHtml(String message) {
    return BasicUtility.formatMessageBDToHtml(message);
  }

  /**
   * Parse the text searching @ parameters to translate.
   * 
   * @param text
   *          String with the text to translate.
   * @return String translated.
   */
  public static String parseTranslation(String text) {
    final VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    final String language = OBContext.getOBContext().getLanguage().getLanguage();
    return parseTranslation(new DalConnectionProvider(false), vars, null, language, text);
  }

  /**
   * Parse the text searching @ parameters to translate.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param language
   *          String with the language to translate.
   * @param text
   *          String with the text to translate.
   * @return String translated.
   */
  public static String parseTranslation(ConnectionProvider conn, VariablesSecureApp vars,
      String language, String text) {
    return parseTranslation(conn, vars, null, language, text);
  }

  /**
   * Parse the text searching @ parameters to translate. If replaceMap is not null and contains a
   * replacement value for a token then it will be used, otherwise the return value of the translate
   * method will be used for the translation.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param replaceMap
   *          optional Map containing replacement values for the tokens
   * @param language
   *          String with the language to translate.
   * @param text
   *          String with the text to translate.
   * @return String translated.
   */
  public static String parseTranslation(ConnectionProvider conn, VariablesSecureApp vars,
      Map<String, String> replaceMap, String language, String text) {
    if (text == null || text.length() == 0) {
      return text;
    }

    String inStr = text;
    String token;
    final StringBuffer outStr = new StringBuffer();

    int i = inStr.indexOf("@");
    while (i != -1) {
      outStr.append(inStr.substring(0, i));
      inStr = inStr.substring(i + 1, inStr.length());

      final int j = inStr.indexOf("@");
      if (j < 0) {
        inStr = "@" + inStr;
        break;
      }

      token = inStr.substring(0, j);
      if (replaceMap != null && replaceMap.containsKey(token)) {
        outStr.append(replaceMap.get(token));
      } else {
        outStr.append(translate(conn, vars, token, language));
      }

      inStr = inStr.substring(j + 1, inStr.length());
      i = inStr.indexOf("@");
    }

    outStr.append(inStr);
    return outStr.toString();
  }

  /**
   * For each token found in the parseTranslation method, this method is called to find the correct
   * translation.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param token
   *          String with the token to translate.
   * @param language
   *          String with the language to translate.
   * @return String with the token translated.
   */
  public static String translate(ConnectionProvider conn, VariablesSecureApp vars, String token,
      String language) {
    String strTranslate = vars.getSessionValue(token);
    if (!strTranslate.equals("")) {
      return strTranslate;
    }
    strTranslate = messageBD(conn, token, language);
    if (strTranslate.equals("")) {
      return token;
    }
    return strTranslate;
  }

  /**
   * Gets the Message for the instance of the processes.
   * 
   * @param pInstance
   *          ProcessInstance object
   * @return Object with the message.
   * @throws ServletException
   */
  public static OBError getProcessInstanceMessage(ProcessInstance pInstance) {
    OBError myMessage = new OBError();
    String message = pInstance.getErrorMsg();
    String title = "";
    String type = "";
    if (pInstance.getResult() == 1L) {
      type = "Success";
      title = messageBD("Success");
    } else if (pInstance.getResult() == 0L) {
      type = "Error";
      title = messageBD("Error");
    } else {
      type = "Warning";
      title = messageBD("Warning");
    }

    final int errorPos = message.indexOf("@ERROR=");
    if (errorPos != -1) {
      myMessage = translateError("@CODE=@" + message.substring(errorPos + 7));
      log4j.debug("Error Message returned: " + myMessage.getMessage());
      if (message.substring(errorPos + 7).equals(myMessage.getMessage())) {
        myMessage.setMessage(parseTranslation(myMessage.getMessage()));
      }
      if (errorPos > 0) {
        message = message.substring(0, errorPos);
      } else {
        message = "";
      }
    }
    if (!message.equals("") && message.indexOf("@") != -1) {
      message = parseTranslation(message);
    }
    myMessage.setType(type);
    myMessage.setTitle(title);
    myMessage.setMessage(message + ((!message.equals("") && errorPos != -1) ? " <br> " : "")
        + myMessage.getMessage());

    return myMessage;
  }

  /**
   * Translate the message, searching the @ parameters, and making use of the ErrorTextParser class
   * to get the appropriated message.
   * 
   * @param message
   *          String with the message to translate.
   * @return
   */
  public static OBError translateError(String message) {
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    final String strLanguage = OBContext.getOBContext().getLanguage().getLanguage();
    return translateError(new DalConnectionProvider(false), vars, strLanguage, message);
  }

  /**
   * Translate the message, searching the @ parameters, and making use of the ErrorTextParser class
   * to get the appropriated message.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param strLanguage
   *          Language to translate.
   * @param message
   *          String with the message to translate.
   * @return Object with the message.
   */
  public static OBError translateError(ConnectionProvider conn, VariablesSecureApp vars,
      String strLanguage, String _message) {
    String message = _message;
    final OBError myError = new OBError();
    myError.setType("Error");
    myError.setMessage(message);
    if (message == null || message.equals("")) {
      return myError;
    }
    String code = "";
    log4j.debug("translateError - message: " + message);
    if (message.startsWith("@CODE=@")) {
      message = message.substring(7);
    } else if (message.startsWith("@CODE=")) {
      message = message.substring(6);
      final int pos = message.indexOf("@");
      if (pos == -1) {
        code = message;
        message = "";
      } else {
        code = message.substring(0, pos);
        message = message.substring(pos + 1);
      }
    }
    myError.setMessage(message);
    log4j.debug("translateError - code: " + code + " - message: " + message);

    // BEGIN Checking if is a pool problem
    if (code != null && code.equals("NoConnectionAvailable")) {
      myError.setType("Error");
      myError.setTitle("Critical Error");
      myError.setConnectionAvailable(false);
      myError.setMessage("No database connection available");
      return myError;
    }
    // END Checking if is a pool problem

    // BEGIN Parsing message text
    if (message != null && !message.equals("")) {
      final String rdbms = conn.getRDBMS();
      ErrorTextParser myParser = null;
      try {
        final Class<?> c = Class.forName("org.openbravo.erpCommon.utility.ErrorTextParser"
            + rdbms.toUpperCase());
        myParser = (ErrorTextParser) c.newInstance();
      } catch (final ClassNotFoundException ex) {
        log4j.warn("Couldn´t find class: org.openbravo.erpCommon.utility.ErrorTextParser"
            + rdbms.toUpperCase());
        myParser = null;
      } catch (final Exception ex1) {
        log4j.warn("Couldn´t initialize class: org.openbravo.erpCommon.utility.ErrorTextParser"
            + rdbms.toUpperCase());
        myParser = null;
      }
      if (myParser != null) {
        myParser.setConnection(conn);
        myParser.setLanguage(strLanguage);
        myParser.setMessage(message);
        myParser.setVars(vars);
        try {
          final OBError myErrorAux = myParser.parse();
          if (myErrorAux != null
              && !myErrorAux.getMessage().equals("")
              && (code == null || code.equals("") || code.equals("0") || !myErrorAux.getMessage()
                  .equalsIgnoreCase(message)))
            return myErrorAux;
        } catch (final Exception ex) {
          log4j.error("Error while parsing text: " + ex);
        }
      }
    } else {
      myError.setMessage(code);
    }
    // END Parsing message text

    // BEGIN Looking for error code in AD_Message
    if (code != null && !code.equals("")) {
      final FieldProvider fldMessage = locateMessage(conn, code, strLanguage);
      if (fldMessage != null) {
        myError.setType((fldMessage.getField("msgtype").equals("E") ? "Error" : (fldMessage
            .getField("msgtype").equals("I") ? "Info"
            : (fldMessage.getField("msgtype").equals("S") ? "Success" : "Warning"))));
        myError.setMessage(fldMessage.getField("msgtext"));
        return myError;
      }
    }
    // END Looking for error code in AD_Message

    return myError;
  }

  /**
   * Search a message in the database.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param strCode
   *          Message to search.
   * @param strLanguage
   *          Language to translate.
   * @return FieldProvider with the message info.
   */
  public static FieldProvider locateMessage(ConnectionProvider conn, String strCode,
      String strLanguage) {
    FieldProvider[] fldMessage = null;

    try {
      log4j.debug("locateMessage - Message Code: " + strCode);
      fldMessage = MessageBDData.messageInfo(conn, strLanguage, strCode);
    } catch (final Exception ignore) {
    }
    if (fldMessage != null && fldMessage.length > 0) {
      return fldMessage[0];
    } else {
      return null;
    }
  }

  /**
   * Returns a message in the right language with parameter substitution. Each occurence of a %
   * parameter (%0, %1 etc) is replaced with the corresponding parameter value. in the params array.
   * 
   * @param key
   *          the key of the message
   * @param params
   *          the parameters to substitute in the message
   * @return the translated message with the parameters substituted
   */
  public static String getI18NMessage(String key, String[] params) {
    OBContext.setAdminMode();
    try {

      // first read the labels from the base table
      final OBQuery<Message> messages = OBDal.getInstance().createQuery(Message.class,
          Message.PROPERTY_SEARCHKEY + "=:key");
      messages.setNamedParameter("key", key);
      if (messages.list().isEmpty()) {
        return null;
      }

      if (messages.list().size() > 1) {
        log4j.warn("More than one message found using key " + key);
      }

      // pick the first one
      final Message message = messages.list().get(0);
      String label = message.getMessageText();
      final String languageId = OBContext.getOBContext().getLanguage().getId();
      for (MessageTrl messageTrl : message.getADMessageTrlList()) {
        if (DalUtil.getId(messageTrl.getLanguage()).equals(languageId)) {
          label = messageTrl.getMessageText();
          break;
        }
      }
      // parameter substitution
      if (params != null && params.length > 0) {
        int cnt = 0;
        for (String param : params) {
          label = label.replace("%" + cnt++, param);
        }
      }
      return label;
    } catch (Exception e) {
      throw new OBException("Exception when getting message for key: " + key, e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
