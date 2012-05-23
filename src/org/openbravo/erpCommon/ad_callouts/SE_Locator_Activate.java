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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.onhandquantity.StoragePending;

public class SE_Locator_Activate extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  private final String STORAGEBIN_TAB = "178";
  private final String WAREHOUSE_TAB = "177";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    VariablesSecureApp vars = info.vars;
    String active = vars.getStringParameter("inpisactive");
    String strLocator = vars.getStringParameter("inpmLocatorId");
    String tab = vars.getStringParameter("inpTabId");
    String strWarehouse = vars.getStringParameter("inpmWarehouseId");

    if (active.equals("Y")) {
      return;
    }

    OBContext.setAdminMode(true);
    try {

      if (tab.equals(STORAGEBIN_TAB)) {
        if (storageIsNotEmpty(strLocator)) {
          info.addResult("MESSAGE", FIN_Utility.messageBD("M_STORAGE_ACTIVE_CHECK_FULL"));
        } else {
          Locator locator = OBDal.getInstance().get(Locator.class, strLocator);
          if (numberOfActiveStorageBins(locator.getWarehouse()) == 1 && locator.isActive()) {
            // This means that the warehouse has only one active storage bin and it is this one
            info.addResult("MESSAGE", FIN_Utility.messageBD("M_STORAGE_ACTIVE_CHECK_LAST"));
          }
        }
      } else if (tab.equals(WAREHOUSE_TAB)) {
        Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, strWarehouse);
        if (numberOfActiveStorageBins(warehouse) > 0) {
          info.addResult("MESSAGE", FIN_Utility.messageBD("M_WAREHOUSE_ACTIVE_CHECK_ACTIVES"));
        } else if (warehouseWithPendingReceipts(warehouse.getId())) {
          info.addResult("MESSAGE", FIN_Utility.messageBD("M_WAREHOUSE_ACTIVE_CHECK_ENTRIES"));
        }
      }

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * This method returns true if the storage bin with the id passed as argument has stock inside.
   * This means that the storage bin should not be deactivated.
   */
  private boolean storageIsNotEmpty(String strLocator) {
    final StringBuilder hsqlScript = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();

    hsqlScript.append(" as sd ");
    hsqlScript.append(" where sd." + StorageDetail.PROPERTY_STORAGEBIN + ".id = ? and ");
    parameters.add(strLocator);
    hsqlScript.append(" (coalesce (sd." + StorageDetail.PROPERTY_QUANTITYONHAND + ",0) <> 0)");
    hsqlScript.append(" or coalesce (sd." + StorageDetail.PROPERTY_ONHANDORDERQUANITY + ",0) <> 0");
    hsqlScript.append(" or coalesce (sd." + StorageDetail.PROPERTY_QUANTITYINDRAFTTRANSACTIONS
        + ",0) <> 0");
    hsqlScript.append(" or coalesce (sd." + StorageDetail.PROPERTY_QUANTITYORDERINDRAFTTRANSACTIONS
        + ",0) <> 0) ");

    final OBQuery<StorageDetail> query = OBDal.getInstance().createQuery(StorageDetail.class,
        hsqlScript.toString());
    query.setParameters(parameters);
    return query.list().size() > 0;
  }

  /**
   * This method returns true if the warehouse has pending shipments or receipts.
   */
  private Boolean warehouseWithPendingReceipts(String warehouse) {
    final StringBuilder hsqlScript = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();

    hsqlScript.append(" as sp");
    hsqlScript.append(" left join sp.warehouse as w");
    hsqlScript.append(" where w.id = ? and");
    parameters.add(warehouse);
    hsqlScript.append(" (coalesce (sp." + StoragePending.PROPERTY_ORDEREDQUANTITY + ",0) <> 0");
    hsqlScript.append(" or coalesce (sp." + StoragePending.PROPERTY_ORDEREDQUANTITYORDER
        + ",0) <> 0");
    hsqlScript.append(" or coalesce (sp." + StoragePending.PROPERTY_RESERVEDQUANTITY + ",0) <> 0");
    hsqlScript.append(" or coalesce (sp." + StoragePending.PROPERTY_RESERVEDQUANTITYORDER
        + ",0) <> 0) ");

    final OBQuery<StoragePending> query = OBDal.getInstance().createQuery(StoragePending.class,
        hsqlScript.toString());
    query.setParameters(parameters);
    return query.list().size() > 0;
  }

  /**
   * This method returns the number of Active Storage Bins a Warehouse has.
   */
  private int numberOfActiveStorageBins(Warehouse warehouse) {
    int number = 0;
    for (Locator locator : warehouse.getLocatorList()) {
      if (locator.isActive()) {
        number++;
      }
    }
    return number;
  }

}
