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
package org.openbravo.base;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class HttpBaseUtils {
  public static Logger log4j = Logger.getLogger(HttpBaseUtils.class);

  /** Creates a new instance of LoginUtils */
  private HttpBaseUtils() {
  }

  public static String getLocalHostAddress(HttpServletRequest request) {
    return getLocalHostAddress(request, false);
  }

  // This comment is for testing purposes
  public static String getLocalHostAddress(HttpServletRequest request, boolean includePort) {
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    String port = "";
    if (includePort) {
      int p = request.getServerPort();
      port = (p == 80) ? "" : ":" + p;
    }
    return scheme + "://" + serverName + port;
  }

  public static String getLocalAddress(HttpServletRequest request) {
    // reads local address
    String host = getLocalHostAddress(request, true);
    return host + request.getContextPath();
  }

  public static String getRelativeUrl(HttpServletRequest request, String url) {
    if (!url.startsWith(getLocalHostAddress(request, true) + "/"))
      return url;
    url = url.replace((getLocalHostAddress(request, true) + "/"), "");
    String actualServlet = request.getContextPath() + request.getServletPath();
    if (actualServlet.startsWith("/"))
      actualServlet = actualServlet.substring(1);
    int i = actualServlet.indexOf("/");
    while (i != -1) {
      if (url.startsWith(actualServlet.substring(0, i)))
        url = url.replace(actualServlet.substring(0, i + 1), "");
      else
        url = "../" + url;
      actualServlet = actualServlet.substring(i + 1);
      i = actualServlet.indexOf("/");
    }
    return url;
  }
}
