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
package org.openbravo.base.secureApp;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.HttpBaseServlet;

public class ServletGoBack extends HttpBaseServlet {
  private static final long serialVersionUID = 1L;
  String strServletPorDefecto;

  private class Variables extends VariablesHistory {

    public Variables(HttpServletRequest request) {
      super(request);
      String sufix = getCurrentHistoryIndex();
      removeSessionValue("reqHistory.servlet" + sufix);
      removeSessionValue("reqHistory.path" + sufix);
      removeSessionValue("reqHistory.command" + sufix);
      downCurrentHistoryIndex();
    }
  }

  public void init(ServletConfig config) {
    super.init(config);
    strServletPorDefecto = config.getServletContext().getInitParameter("DefaultServlet");
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    if (log4j.isDebugEnabled()) {
      log4j.info("start doPost");
    }
    Variables vars = new Variables(req);
    String strUrl = strDireccion + vars.getCurrentServletPath(strServletPorDefecto) + "?Command="
        + vars.getCurrentServletCommand();
    res.sendRedirect(res.encodeRedirectURL(strUrl));
  }

  public String getServletInfo() {
    return "Servlet that receives and redirects go back requests, using history information registered in the httpSession";
  } // end of getServletInfo() method
}
