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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

public class EmailConfiguration_Port extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String strSmtpConnectionSecurity = info.getStringParameter("inpsmtpconnectionsecurity", null);

    info.addResult("inpsmtpport", getSuggestedPort(strSmtpConnectionSecurity));
  }

  private String getSuggestedPort(String strSmtpConnectionSecurity) {
    String recommendedPort = "";
    if (strSmtpConnectionSecurity.equals("N")) {
      recommendedPort = "25";
    } else if (strSmtpConnectionSecurity.equals("STARTTLS")) {
      recommendedPort = "587";
    } else if (strSmtpConnectionSecurity.equals("SSL")) {
      recommendedPort = "465";
    }
    return recommendedPort;
  }
}