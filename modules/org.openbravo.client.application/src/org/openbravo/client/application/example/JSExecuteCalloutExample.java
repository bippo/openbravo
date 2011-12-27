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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.example;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

/**
 * 
 * @author iperdomo
 */
public class JSExecuteCalloutExample extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final String JSEXECUTE = "JSEXECUTE";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // info.addResult(JSEXECUTE, "debugger;isc.say('hello world');");
    info.addResult(JSEXECUTE, "isc.say('hello world');");
    // info.addResult("MESSAGE", "a message");
    // info.addResult("WARNING", "a warning message");
    // info.addResult("ERROR", "an error message");
    // info.addResult("INFO", "an info message");
  }

}
