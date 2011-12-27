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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class CacheFilter implements Filter {
  private String[][] replyHeaders = { {} };

  public void init(FilterConfig config) {
    Enumeration<?> names = config.getInitParameterNames();
    ArrayList<String[]> tmp = new ArrayList<String[]>();
    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();
      String value = config.getInitParameter(name);
      String[] pair = { name, value };
      tmp.add(pair);
    }
    replyHeaders = new String[tmp.size()][2];
    tmp.toArray(replyHeaders);
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    for (int n = 0; n < replyHeaders.length; n++) {
      String name = replyHeaders[n][0];
      String value = replyHeaders[n][1];
      httpResponse.addHeader(name, value);
    }
    chain.doFilter(request, response);
  }

  public void destroy() {
  }

}
