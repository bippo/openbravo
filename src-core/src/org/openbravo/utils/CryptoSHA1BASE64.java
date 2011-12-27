/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;

public final class CryptoSHA1BASE64 {
  public static String hash(String plaintext) throws ServletException {
    MessageDigest md = null;

    try {
      md = MessageDigest.getInstance("SHA"); // SHA-1 generator instance
    } catch (NoSuchAlgorithmException e) {
      throw new ServletException(e.getMessage());
    }

    try {
      md.update(plaintext.getBytes("UTF-8")); // Message summary
      // generation
    } catch (UnsupportedEncodingException e) {
      throw new ServletException(e.getMessage());
    }

    byte raw[] = md.digest(); // Message summary reception
    try {
      String hash = new String(org.apache.commons.codec.binary.Base64.encodeBase64(raw), "UTF-8");
      return hash;
    } catch (UnsupportedEncodingException use) {
      throw new ServletException(use);
    }
  }
}
