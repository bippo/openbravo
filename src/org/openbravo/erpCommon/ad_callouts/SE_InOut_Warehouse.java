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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Warehouse;

public class SE_InOut_Warehouse extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    final String strWarehouseId = info.getStringParameter("inpmWarehouseId", IsIDFilter.instance);
    final String strIsSOTrx = info.getStringParameter("inpissotrx", null);
    final String strIsReturnWindow = info.vars.getStringParameter("isReturnMaterial", "N");

    if ("Y".equals(strIsSOTrx) && "Y".equals(strIsReturnWindow)) {
      OBContext.setAdminMode();
      try {
        Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, strWarehouseId);
        if (warehouse.getReturnlocator().getId() != null) {
          info.addResult("ReturnLocator", warehouse.getReturnlocator().getId());
        }
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }
}
