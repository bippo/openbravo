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

package org.openbravo.erpCommon.utility;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * OBPrintStream class is a PrintStream, it allows to obtain a log (using the getLog() method) as a
 * String. Each time this method is called the log is emptied. Its purpose is to be used from an
 * HTML using AJAX to fill a log textarea in real time.
 * 
 * @see org.openbravo.erpCommon.ad_process.ApplyModules
 * @see AntExecutor
 * 
 */
@Deprecated
class OBPrintStream extends PrintStream {
  private StringBuffer log;
  private boolean finished;
  private PrintWriter out;
  private PrintStream psout;
  public static final int TEXT_HTML = 1;
  public static final int TEXT_PLAIN = 2;
  private File logFile;
  private PrintWriter logWriter;

  /**
   * Crates a new OBPrintStream object
   */
  public OBPrintStream(PrintWriter p) {

    super(System.out); // It is needed to call a super constructor, though
    // it is not going to be used
    setPrintWritter(p);
    log = new StringBuffer();
    finished = false;
  }

  public OBPrintStream(PrintStream p) {

    super(System.out); // It is needed to call a super constructor, though
    // it is not going to be used
    psout = p;
    log = new StringBuffer();
    finished = false;
  }

  public void setPrintWritter(PrintWriter p) {
    out = p;
  }

  public void setLogFile(File f) {
    logFile = f;
    try {
      logWriter = new PrintWriter(f);

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Writes a byte array to the internal PrintStream, replaces line breaks with the <br/>
   * html tag.
   */
  @Override
  public void write(byte[] buf) {
    write(buf, 0, buf.length);
  }

  /**
   * Writes the log in a StringBuffer, if the PrintWritter is set if also writes there and flushes
   * it.
   */
  @Override
  public void write(byte[] buf, int off, int len) {
    final String s = new String(buf, off, len);
    if (psout != null) {
      psout.println(encodeHtml(s));
      psout.flush();
    } else if (out != null) {
      out.println(encodeHtml(s));
      out.flush();
    }
    if (logWriter != null) {
      logWriter.print(s);
      logWriter.flush();
    }
    log.append(s);
  }

  // simple encoding
  private String encodeHtml(String s) {
    String value = s.replace("&", "&amp;");
    value = value.replace(">", "&gt;");
    value = value.replace("<", "&lt;");
    value = value.replace("\n", "<br/>");
    return value;
  }

  /**
   * Returns a String with the piece of log generated after the last call to this method. In case no
   * log has been generated and the finished property is set to true, it returns and END String to
   * be used in case the AJAX call has timed out.
   * 
   * @param showType
   *          - Defines the format to display the text
   * @return - The newly generated log
   */
  public String getLog(int showType) {
    String rt = "";
    if (log != null) {
      rt = log.toString();
      log = new StringBuffer();
    }
    if (rt.equals("") && finished) {
      rt = "@END@"; // to force end
    } else {
      switch (showType) {
      case TEXT_HTML:
        rt = rt.replace("\n", "<br/>");
      }
    }
    return rt;
  }

  /**
   * Sets the finished property to the passed value.
   * 
   * @param v
   *          - boolean value to set the finished property.
   */
  public void setFinished(boolean v) {
    finished = v;
  }
}
