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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.json.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.service.json.JsonRestServlet;
import org.openbravo.test.base.BaseTest;

/**
 * Base class for Json test Rest http requests. To test the {@link JsonRestServlet}. This test class
 * requires a running Openbravo instance on http://localhost:8080/openbravo.
 * 
 * @author mtaal
 */
public class JsonRestTest extends BaseTest {
  private static final Logger log = Logger.getLogger(JsonRestTest.class);

  private static final String OB_URL = "http://localhost:8080/openbravo";
  private static final String LOGIN = "Openbravo";
  private static final String PWD = "openbravo";

  protected JSONObject doRequest(String wsPart, String testContent, String method, int responseCode) {
    try {
      final HttpURLConnection hc = createConnection(wsPart, method);
      hc.connect();
      assertEquals(responseCode, hc.getResponseCode());
      if (hc.getResponseCode() != 200) {
        return null;
      }

      final InputStream is = hc.getInputStream();
      final String content = convertToString(is);
      final JSONObject jsonObject = new JSONObject(content);
      is.close();
      return jsonObject;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  protected HttpURLConnection createConnection(String wsPart, String method) throws Exception {
    Authenticator.setDefault(new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(LOGIN, PWD.toCharArray());
      }
    });
    log.debug(method + ": " + getOpenbravoURL() + wsPart);
    final URL url = new URL(getOpenbravoURL() + wsPart);
    final HttpURLConnection hc = (HttpURLConnection) url.openConnection();
    hc.setRequestMethod(method);
    hc.setAllowUserInteraction(false);
    hc.setDefaultUseCaches(false);
    hc.setDoOutput(true);
    hc.setDoInput(true);
    hc.setInstanceFollowRedirects(true);
    hc.setUseCaches(false);
    hc.setRequestProperty("Content-Type", "application/json");
    return hc;
  }

  protected JSONObject doContentRequest(String wsPart, String content, int expectedResponse,
      String expectedContent, String method) {
    try {
      final HttpURLConnection hc = createConnection(wsPart, method);
      final OutputStream os = hc.getOutputStream();
      os.write(content.getBytes("UTF-8"));
      os.flush();
      os.close();
      hc.connect();

      assertEquals(expectedResponse, hc.getResponseCode());

      if (expectedResponse == 500) {
        // no content available anyway
        return null;
      }
      final InputStream is = hc.getInputStream();
      final String outputContent = convertToString(is);
      final JSONObject jsonObject = new JSONObject(outputContent);
      assertEquals(expectedResponse, hc.getResponseCode());
      is.close();
      return jsonObject;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Returns the url of the Openbravo instance. The default value is: {@link #OB_URL}
   * 
   * @return the url of the Openbravo instance.
   */
  protected String getOpenbravoURL() {
    return OB_URL;
  }

  protected String convertToString(InputStream is) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();

    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    } finally {
      try {
        is.close();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }

    return sb.toString();
  }

}