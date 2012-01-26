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
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.NumberFilter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.manufacturing.transaction.WorkRequirementOperation;

public class SL_ProductionPlan_WRPhase_Quantity extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final IsIDFilter idFilter = new IsIDFilter();
  private static final NumberFilter numFilter = new NumberFilter();

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    try {
      OBContext.setAdminMode(true);
      String strmWRPhase = info.getStringParameter("inpmaWrphaseId", idFilter);
      String strQty = info.getStringParameter("inpproductionqty", numFilter);

      WorkRequirementOperation wrPhase = OBDal.getInstance().get(WorkRequirementOperation.class,
          strmWRPhase);
      BigDecimal qty = new BigDecimal(strQty);
      BigDecimal wrPhaseEstTime = wrPhase.getEstimatedTime();
      if (wrPhaseEstTime != null) {
        if (wrPhase.getQuantity() != null && wrPhase.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
          info.addResult("inpestimatedtime",
              wrPhaseEstTime.divide(wrPhase.getQuantity()).multiply(qty).toPlainString());
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
