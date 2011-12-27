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

package org.openbravo.erpCommon.obps;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.dal.core.OBContext;
import org.openbravo.utils.FileUtility;

/**
 * Servlet which returns the openbravo logo image with version number. Depending on instance
 * activation status either the ops image or the normal powered by openbravo image is returned.
 * 
 * @author huehner
 * 
 */
public class GetOpenbravoLogo extends HttpBaseServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    String communityLogo = "PoweredByOpenbravo.png";
    String opsLogo = "PoweredByOpenbravoOPS.png";

    // get instance active status from db
    boolean active = false;
    OBContext.setAdminMode();
    try {
      active = ActivationKey.getInstance().isActive();
      log4j.debug("GetOpsLogo: activated: " + active);
    } finally {
      OBContext.restorePreviousMode();
    }

    String activeLogo;
    if (active) {
      activeLogo = opsLogo;
    } else {
      activeLogo = communityLogo;
    }

    FileUtility f = new FileUtility(this.globalParameters.prefix + "web/images", activeLogo, false,
        true);
    response.setContentType("image/png");
    // mark response as cache-forever
    response.addHeader("Expires", "Sun, 17 Jan 2038 19:14:07 GMT");
    response.addHeader("Cache-Control", "public");

    f.dumpFile(response.getOutputStream());
    response.getOutputStream().flush();
    response.getOutputStream().close();

  }

}
