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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;
import javax.servlet.ServletException;

public class SL_Project_Service extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    BigDecimal serviceSerCost = info.getBigDecimalParameter("inpservsercost");
    BigDecimal serviceOutCost = info.getBigDecimalParameter("inpservoutcost");

    BigDecimal serviceTotalCost = serviceSerCost.add(serviceOutCost);

    info.addResult("inpservcost", serviceTotalCost);
  }
}
