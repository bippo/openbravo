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
 * All portions are Copyright (C) 2006-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.utils.FormatUtilities;

public class HttpsUtils {

  private static Logger log4j = Logger.getLogger(HttpsUtils.class);

  static String sendSecure(HttpsURLConnection conn, String data) throws IOException {
    String result = null;
    BufferedReader br = null;
    try {
      String s = null;
      StringBuilder sb = new StringBuilder();
      br = new BufferedReader(new InputStreamReader(sendSecureHttpsConnection(conn, data)
          .getInputStream()));
      while ((s = br.readLine()) != null) {
        sb.append(s + "\n");
      }
      br.close();
      result = sb.toString();
    } catch (IOException e) {
      log4j.error(e.getMessage(), e);
      throw e;
    }
    return result;
  }

  private static HttpsURLConnection sendSecureHttpsConnection(HttpsURLConnection conn, String data)
      throws IOException {
    BufferedWriter bw = null;
    try {
      conn.setDoOutput(true);

      bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
      bw.write(data);
      bw.flush();
      bw.close();

      return conn;
    } catch (IOException e) {
      log4j.error(e.getMessage(), e);
      throw e;
    }
  }

  public static String sendSecure(URL url, String data) throws GeneralSecurityException,
      IOException {
    HttpsURLConnection conn = getHttpsConn(url);
    return sendSecure(conn, data);
  }

  public static HttpURLConnection sendHttpsRequest(URL url, String data)
      throws GeneralSecurityException, IOException {

    HttpsURLConnection conn = getHttpsConn(url);
    return sendSecureHttpsConnection(conn, data);

  }

  public static HttpsURLConnection getHttpsConn(URL url) throws KeyStoreException,
      GeneralSecurityException, IOException {
    return (HttpsURLConnection) url.openConnection();
  }

  /**
   * @deprecated use {@link #sendSecure(URL, String)}
   */
  public static String sendSecure(URL url, String data, String alias, String passphrase)
      throws GeneralSecurityException, IOException {
    return sendSecure(url, data);
  }

  /**
   * @deprecated use {@link #sendHttpsRequest(URL, String)}
   */
  public static HttpURLConnection sendHttpsRequest(URL url, String data, String alias,
      String passphrase) throws GeneralSecurityException, IOException {
    return sendHttpsRequest(url, data);
  }

  /**
   * @deprecated use {@link #getHttpsConn(URL)}
   */
  public static HttpsURLConnection getHttpsConn(URL url, String alias, String passphrase)
      throws KeyStoreException, GeneralSecurityException, IOException {
    return getHttpsConn(url);
  }

  /**
   * @deprecated
   * 
   *             This method tries to URLEncode a queryString. It splits the query in chunks
   *             separated by & assuming & symbol is not part of the values. But in case a value
   *             contains this symbol it does not work (issue #18405).
   * 
   *             Do not use this method, instead encode each of the values in the query.
   */
  public static String encode(String queryStr, String encoding) {
    StringBuilder sb = new StringBuilder();
    String[] ss = queryStr.split("&");
    for (String s : ss) {
      String key = s.split("=")[0];
      String value = "";
      try {
        value = s.split("=")[1];
      } catch (IndexOutOfBoundsException e) {
        // Do nothing - value is an empty string
      }
      try {
        value = URLEncoder.encode(value, encoding);
      } catch (UnsupportedEncodingException e) {
        log4j.error(e.getMessage(), e);
        // Shouldn't happen. Openbravo only using UTF-8
      }
      sb.append(key + "=" + value + "&");
    }
    return sb.toString();
  }

  /**
   * Checks Internet availability. In case system information is defined to use proxy, proxy is set.
   * Therefore this method should be invoked before each Internet connection.
   * 
   * @return true in case Internet (https://butler.openbravo.com) is reachable.
   */
  public static boolean isInternetAvailable() {
    return isInternetAvailable(null, 0);
  }

  /**
   * Checks the Internet availability and sets the proxy in case it is needed.
   * 
   * @deprecated Proxy settings should not be passed as parameter, but obtained from system
   *             information. Use instead {@link HttpsUtils#isInternetAvailable()}
   * @param proxyHost
   * @param proxyPort
   */
  public static boolean isInternetAvailable(String proxyHost, int proxyPort) {
    OBContext.setAdminMode();
    try {
      final SystemInformation sys = OBDal.getInstance().get(SystemInformation.class, "0");
      if (sys.isProxyRequired() || (proxyHost != null && !proxyHost.isEmpty())) {
        // Proxy is required for connection.
        String host;
        int port;
        if (proxyHost == null || proxyHost.isEmpty()) {
          // to maintain backwards compatibility, set host in case it is provided as parameter (it
          // shouldn't be)
          host = sys.getProxyServer();
          port = sys.getProxyPort().intValue();
        } else {
          host = proxyHost;
          port = proxyPort;
        }
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("http.proxyHost", host);
        System.getProperties().put("https.proxyHost", host);
        System.getProperties().put("http.proxyPort", String.valueOf(port));
        System.getProperties().put("https.proxyPort", String.valueOf(port));

        System.setProperty("java.net.useSystemProxies", "true");

        if (sys.isRequiresProxyAuthentication()) {
          final String user = sys.getProxyUser();
          String pass = "";
          try {
            pass = FormatUtilities.encryptDecrypt(sys.getProxyPassword(), false);
          } catch (ServletException e) {
            log4j.error("Error setting proxy authenticator", e);
          }
          final String password = pass;

          // Used for standard http and https connections
          Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(user, password.toCharArray());
            }
          });

          // Used for SOAP webservices
          System.getProperties().setProperty("http.proxyUser", user);
          System.getProperties().setProperty("http.proxyPassword", password);
        }
      } else {
        System.getProperties().put("proxySet", false);
        System.getProperties().remove("http.proxyHost");
        System.getProperties().remove("http.proxyPort");
        System.getProperties().remove("https.proxyHost");
        System.getProperties().remove("https.proxyPort");
        System.getProperties().remove("http.proxyUser");
        System.getProperties().remove("http.proxyPassword");
        System.setProperty("java.net.useSystemProxies", "false");
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    try {
      // Double check.
      URL url = new URL("https://butler.openbravo.com");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(3000);
      conn.connect();
      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        return false;
      }
    } catch (Exception e) {
      log4j.info("Unable to reach butler.openbravo.com");
      return false;
    }
    return true;
  }

}