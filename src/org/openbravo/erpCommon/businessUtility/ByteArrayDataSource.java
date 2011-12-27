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
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

class ByteArrayDataSource implements DataSource {
  private byte[] g_data = null;
  private String g_type = "text/plain";
  private String g_name = null;

  public ByteArrayDataSource(InputStream is, String type) {
    g_type = type;
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      int ch;

      while ((ch = is.read()) != -1) {
        os.write(ch);
      }
      g_data = os.toByteArray();
    } catch (IOException ioex) {
      System.err.println("ByteArrayDataSource - " + ioex);
    }
  }

  public ByteArrayDataSource(byte[] data, String type) {
    g_data = data;
    g_type = type;
  }

  public ByteArrayDataSource(String asciiData, String type) {
    try {
      g_data = asciiData.getBytes("UTF-8");
    } catch (UnsupportedEncodingException uex) {
      System.err.println("ByteArrayDataSource - " + uex);
    }
    g_type = type;
  }

  public InputStream getInputStream() throws IOException {
    if (g_data == null)
      throw new IOException("no data");
    return new ByteArrayInputStream(g_data);
  }

  public OutputStream getOutputStream() throws IOException {
    throw new IOException("cannot do this");
  }

  public String getContentType() {
    return g_type;
  }

  public ByteArrayDataSource setName(String name) {
    g_name = name;
    return this;
  }

  public String getName() {
    if (g_name != null)
      return g_name;
    return "ByteArrayDataStream " + g_type;
  }
}
