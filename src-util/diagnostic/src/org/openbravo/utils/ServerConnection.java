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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerConnection {

  private URL getUrl(String action, String additionalParameters) throws MalformedURLException {
    String serverUrl = new PropertiesManager("config/Openbravo.properties")
        .getProperty("context.url");
    serverUrl = serverUrl.substring(0, serverUrl.lastIndexOf('/'));
    return new URL(serverUrl + "/OpenbravoDiagnostics/Check.html?Command=" + action
        + additionalParameters);
  }

  public String getCheck(String action, String additionalParameters) {
    BufferedReader br = null;
    BufferedWriter bw = null;
    try {
      final HttpURLConnection conn = (HttpURLConnection) getUrl(action, additionalParameters)
          .openConnection();

      conn.setDoOutput(true);

      bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
      bw.flush();
      bw.close();

      String s = null;
      final StringBuilder sb = new StringBuilder();
      br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      while ((s = br.readLine()) != null) {
        sb.append(s);
      }
      br.close();
      return sb.toString();
    } catch (final IOException e) {
      // log4j.error(e.getMessage(), e);
      // throw e;
    }
    return "";
  }

  public String getCheck(String action) {
    return getCheck(action, "");
  }
}
