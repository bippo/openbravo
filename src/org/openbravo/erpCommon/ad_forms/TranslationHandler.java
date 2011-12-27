/******************************************************************************
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
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
 ******************************************************************************/
package org.openbravo.erpCommon.ad_forms;

import java.sql.Statement;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX Handler for parsing Translation
 * 
 * @author Jorg Janke
 * @version $Id: TranslationHandler.java,v 1.5 2003/10/04 03:52:36 jjanke Exp $
 */
class TranslationHandler extends DefaultHandler {
  /**
   * Translation Handler
   */

  public TranslationHandler(ConnectionProvider cDB) {
    m_AD_Client_ID = 0;
    DB = cDB;
  }

  public TranslationHandler(int AD_Client_ID, ConnectionProvider cDB) {

    m_AD_Client_ID = AD_Client_ID;
    DB = cDB;

  } // TranslationHandler

  private ConnectionProvider DB;

  /** Client */
  private int m_AD_Client_ID = -1;
  /** Language */
  private String m_AD_Language = null;

  /** Table */
  private String m_TableName = null;
  /** Update SQL */
  private String m_updateSQL = null;
  /** Current ID */
  private String m_curID = null;
  /** Current ColumnName */
  private String m_curColumnName = null;
  /** Current Value */
  private StringBuffer m_curValue = null;
  /** Original Value */
  private String m_oriValue = null;
  /** SQL */
  private StringBuffer m_sql = null;

  private int m_updateCount = 0;

  private String m_Translated = null;

  static Logger log4j = Logger.getLogger(TranslationHandler.class);

  /*************************************************************************/

  /**
   * Receive notification of the start of an element.
   * 
   * @param uri
   *          namespace
   * @param localName
   *          simple name
   * @param qName
   *          qualified name
   * @param attributes
   *          attributes
   * @throws org.xml.sax.SAXException
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws org.xml.sax.SAXException {
    if (qName.equals(TranslationManager.XML_TAG)) {
      m_AD_Language = attributes.getValue(TranslationManager.XML_ATTRIBUTE_LANGUAGE);

      m_TableName = attributes.getValue(TranslationManager.XML_ATTRIBUTE_TABLE);
      m_updateSQL = "UPDATE " + m_TableName;

      m_updateSQL += "_Trl";
      m_updateSQL += " SET ";
      if (log4j.isDebugEnabled())
        log4j.debug("AD_Language=" + m_AD_Language + ", TableName=" + m_TableName);
    } else if (qName.equals(TranslationManager.XML_ROW_TAG)) {
      m_curID = attributes.getValue(TranslationManager.XML_ROW_ATTRIBUTE_ID);
      m_Translated = attributes.getValue(TranslationManager.XML_ROW_ATTRIBUTE_TRANSLATED);
      m_sql = new StringBuffer();
    } else if (qName.equals(TranslationManager.XML_VALUE_TAG)) {
      m_curColumnName = attributes.getValue(TranslationManager.XML_VALUE_ATTRIBUTE_COLUMN);
      m_oriValue = attributes.getValue(TranslationManager.XML_VALUE_ATTRIBUTE_ORIGINAL);
    } else if (qName.equals(TranslationManager.XML_CONTRIB)) {
      m_AD_Language = attributes.getValue(TranslationManager.XML_ATTRIBUTE_LANGUAGE);
    } else
      log4j.error("startElement - UNKNOWN TAG: " + qName);
    m_curValue = new StringBuffer();
  } // startElement

  /**
   * Receive notification of character data inside an element.
   * 
   * @param ch
   *          buffer
   * @param start
   *          start
   * @param length
   *          length
   * @throws SAXException
   */
  public void characters(char ch[], int start, int length) throws SAXException {
    m_curValue.append(ch, start, length);
  } // characters

  /**
   * Receive notification of the end of an element.
   * 
   * @param uri
   *          namespace
   * @param localName
   *          simple name
   * @param qName
   *          qualified name
   * @throws SAXException
   */
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // Log.trace(Log.l6_Database+1, "TranslationHandler.endElement", qName);
    if (log4j.isDebugEnabled())
      log4j.debug("endelement " + qName);
    if (qName.equals(TranslationManager.XML_TAG)) {
    } else if (qName.equals(TranslationManager.XML_ROW_TAG)) {
      // Set section
      if (m_sql.length() > 0)
        m_sql.append(",");
      m_sql.append("Updated=now()"); // .append(DB.TO_DATE(m_time,
      // false));
      m_sql.append(",IsTranslated='" + m_Translated + "'");
      // Where section
      m_sql.append(" WHERE ").append(m_TableName).append("_ID='").append(m_curID).append("'");
      m_sql.append(" AND AD_Language='").append(m_AD_Language).append("'");
      if (m_AD_Client_ID >= 0)
        m_sql.append(" AND AD_Client_ID='").append(m_AD_Client_ID).append("'");
      // Update section
      m_sql.insert(0, m_updateSQL);
      if (log4j.isDebugEnabled())
        log4j.debug(m_sql.toString());
      // Execute
      int no = 0;
      //
      Statement st = null;
      try {
        st = DB.getStatement();
        no = st.executeUpdate(m_sql.toString());
      } catch (Exception e) {
        log4j.error("183:" + m_sql.toString() + e.toString());
      } finally {
        try {
          if (st != null)
            DB.releaseStatement(st);
        } catch (Exception ignored) {
        }
      }

      if (no == 1) {
        if (log4j.isDebugEnabled())
          log4j.debug(m_sql.toString());
        m_updateCount++;
      } else if (no == 0)
        log4j.info("Not Found - " + m_sql.toString());
      else
        log4j.error("Update Rows=" + no + " (Should be 1) - " + m_sql.toString());
    } else if (qName.equals(TranslationManager.XML_VALUE_TAG)) {
      String value = "";
      if (m_curValue != null && !m_curValue.toString().equals("")) {
        value = TO_STRING(m_curValue.toString());
      } else if (m_oriValue != null && !m_oriValue.toString().equals("")) {
        value = TO_STRING(m_oriValue.toString());
      }
      if (!value.equals("")) {
        if (m_sql.length() > 0)
          m_sql.append(",");
        m_sql.append(m_curColumnName).append("=").append(value);
      }
    } else if (qName.equals(TranslationManager.XML_CONTRIB)) {
      if (log4j.isDebugEnabled())
        log4j.debug("Contibutors:" + TO_STRING(m_curValue.toString()));
      try {
        TranslationData.insertContrib(DB, m_curValue.toString(), m_AD_Language);
      } catch (Exception e) {
        log4j.error(e.toString());
      }
    }
  } // endElement

  /**
   * Get Number of updates
   * 
   * @return update count
   */
  public int getUpdateCount() {
    return m_updateCount;
  } // getUpdateCount

  private String TO_STRING(String txt) {
    return TO_STRING(txt, 0);
  } // TO_STRING

  /**
   * Package Strings for SQL command.
   * 
   * <pre>
   * 	-	include in ' (single quotes)
   * 	-	replace ' with ''
   * </pre>
   * 
   * @param txt
   *          String with text
   * @param maxLength
   *          Maximum Length of content or 0 to ignore
   * @return escaped string for insert statement (NULL if null or empty)
   */
  private String TO_STRING(String txt, int maxLength) {
    if (txt == null || txt.isEmpty())
      return "NULL";

    // Length
    String text = txt;
    if (maxLength != 0 && text.length() > maxLength)
      text = txt.substring(0, maxLength);

    char quote = '\'';
    // copy characters (wee need to look through anyway)
    StringBuffer out = new StringBuffer();
    out.append(quote); // '
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == quote)
        out.append("''");
      else
        out.append(c);
    }
    out.append(quote); // '
    //
    return out.toString();
  } // TO_STRING

} // TranslationHandler
