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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;

/**
 * Utility class to detect MIME type based on data array. This class is based on Apache Tika part of
 * the Lucene project. http://lucene.apache.org/tika/
 * 
 * @author iperdomo
 * 
 */
public class MimeTypeUtil {
  private static MimeTypeUtil instance = new MimeTypeUtil();
  private static Tika tika;

  private static void init() {
    tika = new Tika();
  }

  /**
   * Returns the instance of the MimeTypeUtil class
   * 
   * @return MimeTypeUtil instance
   */
  public static MimeTypeUtil getInstance() {
    if (tika == null) {
      init();
    }
    return instance;
  }

  public static void setInstace(MimeTypeUtil ins) {
    MimeTypeUtil.instance = ins;
  }

  /**
   * Returns a MimeType object based on the byte array provided as parameter
   * 
   * @param data
   *          byte array from which we want to detect the MIME type
   * @return MimeType representation
   * @deprecated This method will always return <b>null</b>
   */
  @Deprecated
  public MimeType getMimeType(byte[] data) {
    return null;
  }

  /**
   * Returns the MIME type name, e.g. image/png based on the byte array passed as parameter. Returns
   * application/octet-stream if no better match is found.
   * 
   * @param data
   *          byte array from which we want to detect the MIME type
   * @return A MIME type name, e.g. "image/png"
   */
  public String getMimeTypeName(byte[] data) {
    return tika.detect(data);
  }

}
