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
 * All portions are Copyright (C) 2007-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.openbravo.data.FieldProvider;

public class JRFieldProviderDataSource implements JRDataSource {

  /**
   *
   */
  private FieldProvider[] fieldProvider = null;
  private static String strDateFormat;
  private int index = -1;

  /**
   *
   */
  public JRFieldProviderDataSource(FieldProvider[] fp, String strDateFormat) {
    fieldProvider = fp;
    JRFieldProviderDataSource.strDateFormat = strDateFormat;
  }

  /**
   *
   */
  public boolean next() throws JRException {
    boolean hasNext = false;

    if (fieldProvider != null) {
      if (index < fieldProvider.length - 1) {
        index++;
        hasNext = true;
      }
      // else throw new JRException("Unable to get the FieldProvider.");
    }

    return hasNext;
  }

  /**
   *
   */
  public Object getFieldValue(JRField field) throws JRException {
    Object objValue = null;

    if (field != null && fieldProvider != null) {
      String value = fieldProvider[index].getField(field.getName());
      Class<?> clazz = field.getValueClass();
      try {
        if (clazz.equals(java.lang.Boolean.class)) {
          if (value != null && !value.equals("")) {
            objValue = value.equals("Y") ? Boolean.TRUE : Boolean.FALSE;
          } else
            objValue = null;
        } else if (clazz.equals(java.lang.Byte.class)) {
          if (value != null && !value.equals("")) {
            objValue = new Byte(value);
          } else
            objValue = null;
        } else if (clazz.equals(java.util.Date.class)) {
          if (value != null && !value.equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
            try {
              objValue = dateFormat.parse(value);
            } catch (Exception e) {
              throw new JRException("Unable to parse the date", e);
            }
          } else
            objValue = null;
        }
        /*
         * else if (clazz.equals(java.sql.Timestamp.class)) { objValue =
         * resultSet.getTimestamp(columnIndex.intValue()); if(resultSet.wasNull()) { objValue =
         * null; } }
         */
        /*
         * else if (clazz.equals(java.sql.Time.class)) { objValue =
         * resultSet.getTime(columnIndex.intValue()); if(resultSet.wasNull()) { objValue = null; } }
         */
        else if (clazz.equals(java.lang.Double.class)) {
          if (value != null && !value.equals("")) {
            objValue = new Double(value);
          } else {
            objValue = null;
          }
        } else if (clazz.equals(java.lang.Float.class)) {
          if (value != null && !value.equals("")) {
            objValue = new Float(value);
          } else {
            objValue = null;
          }
        } else if (clazz.equals(java.lang.Integer.class)) {
          if (value != null && !value.equals("")) {
            objValue = new Integer(value);
          } else {
            objValue = null;
          }
        }
        /*
         * else if (clazz.equals(java.io.InputStream.class)) { byte[] bytes =
         * readBytes(columnIndex);
         * 
         * if(bytes == null) { objValue = null; } else { objValue = new ByteArrayInputStream(bytes);
         * } }
         */
        /*
         * else if (clazz.equals(java.lang.Long.class)) { objValue = new
         * Long(resultSet.getLong(columnIndex.intValue())); if(resultSet.wasNull()) { objValue =
         * null; } }
         */
        /*
         * else if (clazz.equals(java.lang.Short.class)) { objValue = new
         * Short(resultSet.getShort(columnIndex.intValue())); if(resultSet.wasNull()) { objValue =
         * null; } }
         */
        else if (clazz.equals(java.math.BigDecimal.class)) {
          if (value != null && !value.equals("")) {
            objValue = new BigDecimal(value);
          } else
            objValue = null;
        } else if (clazz.equals(java.lang.String.class)) {
          if (value != null && !value.equals("")) {
            objValue = new String(value);
          } else
            objValue = null;
        }
        /*
         * else if (clazz.equals(Clob.class)) { objValue =
         * resultSet.getClob(columnIndex.intValue()); if(resultSet.wasNull()) { objValue = null; } }
         */
        /*
         * else if (clazz.equals(Reader.class)) { Reader reader = null; long size = -1;
         * 
         * int columnType = resultSet.getMetaData().getColumnType(columnIndex .intValue()); switch
         * (columnType) { case Types.CLOB: Clob clob = resultSet.getClob(columnIndex.intValue()); if
         * (!resultSet.wasNull()) { reader = clob.getCharacterStream(); size = clob.length(); }
         * break;
         * 
         * default: reader = resultSet.getCharacterStream(columnIndex.intValue()); if
         * (resultSet.wasNull()) { reader = null; } }
         * 
         * if (reader == null) { objValue = null; } else { objValue = getArrayReader(reader, size);
         * } }
         */
        /*
         * else if (clazz.equals(Blob.class)) { objValue =
         * resultSet.getBlob(columnIndex.intValue()); if(resultSet.wasNull()) { objValue = null; } }
         */
        /*
         * else if (clazz.equals(Image.class)) { byte[] bytes = readBytes(columnIndex);
         * 
         * if(bytes == null) { objValue = null; } else { objValue = JRImageLoader.loadImage(bytes);
         * } }
         */
        else {
          if (value != null && !value.equals("")) {
            objValue = new String(value);
          } else
            objValue = null;
        }
      } catch (Exception e) {
        e.printStackTrace();
        throw new JRException("Unable to get value for field '" + field.getName() + "' of class '"
            + clazz.getName() + "' for value: " + value, e);
      }
    }

    return objValue;
  }
}
