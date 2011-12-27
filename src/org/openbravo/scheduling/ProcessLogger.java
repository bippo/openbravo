package org.openbravo.scheduling;

import java.sql.Timestamp;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;

public class ProcessLogger {

  private StringBuilder log;

  private ConnectionProvider connection;

  public ProcessLogger(ConnectionProvider conn) {
    this.connection = conn;
    log = new StringBuilder();
  }

  public String getLog() {
    return log.toString();
  }

  /**
   * Returns an i18n-ed message String from the database.
   * 
   * @param msgKey
   *          the message id
   * @param language
   *          the language to be used to query for the message
   * @return the message retrieved from the db, using the msgKey and language
   */
  public String messageDb(String msgKey, String language) {
    return Utility.messageBD(connection, msgKey, language);
  }

  /**
   * Log a message.
   * 
   * @param msg
   *          the message to log
   * 
   * @see #getLog()
   */
  public void log(String msg) {
    log.append(new Timestamp(System.currentTimeMillis()).toString() + " - " + msg);
  }

  /**
   * Log a message with an additional newline character.
   * 
   * @param msg
   *          the message to log
   * 
   * @see #getLog()
   */
  public void logln(String msg) {
    log(msg + "\n");
  }

}
