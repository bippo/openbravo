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
package org.openbravo.common.actionhandler;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

/**
 * 
 * @author gorkaion
 * 
 */
public class RMShipmentPickEditLines extends BaseProcessActionHandler {
  private static Logger log = Logger.getLogger(RMShipmentPickEditLines.class);
  private boolean setRefNo = false;

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    try {
      jsonRequest = new JSONObject(content);
      log.debug(jsonRequest);
      final String strInOutId = jsonRequest.getString("inpmInoutId");
      ShipmentInOut inOut = OBDal.getInstance().get(ShipmentInOut.class, strInOutId);
      if (cleanInOutLines(inOut)) {
        createInOutLines(jsonRequest);
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private boolean cleanInOutLines(ShipmentInOut inOut) {
    if (inOut.getMaterialMgmtShipmentInOutLineList().isEmpty()) {
      if (inOut.getOrderReference() == null) {
        setRefNo = true;
      }
      // nothing to delete.
      return true;
    }
    try {
      inOut.getMaterialMgmtShipmentInOutLineList().clear();
      OBDal.getInstance().save(inOut);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private void createInOutLines(JSONObject jsonRequest) throws JSONException {
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    // if no lines selected don't do anything.
    if (selectedLines.length() == 0) {
      return;
    }
    final String strInOutId = jsonRequest.getString("inpmInoutId");
    ShipmentInOut inOut = OBDal.getInstance().get(ShipmentInOut.class, strInOutId);
    TreeSet<String> rmVendorRefs = new TreeSet<String>();
    for (long i = 0; i < selectedLines.length(); i++) {
      JSONObject selectedLine = selectedLines.getJSONObject((int) i);
      log.debug(selectedLine);
      ShipmentInOutLine newInOutLine = OBProvider.getInstance().get(ShipmentInOutLine.class);
      newInOutLine.setShipmentReceipt(inOut);
      newInOutLine.setOrganization(inOut.getOrganization());
      newInOutLine.setLineNo((i + 1L) * 10L);

      OrderLine orderLine = OBDal.getInstance().get(OrderLine.class,
          selectedLine.getString("orderLine"));
      if (orderLine.getSalesOrder().getOrderReference() != null) {
        rmVendorRefs.add(orderLine.getSalesOrder().getOrderReference());
      }
      newInOutLine.setSalesOrderLine(orderLine);
      newInOutLine.setStorageBin(OBDal.getInstance().get(Locator.class,
          selectedLine.getString("storageBin")));
      newInOutLine.setProduct(orderLine.getProduct());
      newInOutLine.setAttributeSetValue(orderLine.getAttributeSetValue());
      newInOutLine.setUOM(orderLine.getUOM());
      // Ordered Quantity = returned quantity.
      BigDecimal qtyReceived = new BigDecimal(selectedLine.getString("movementQuantity"));
      newInOutLine.setMovementQuantity(qtyReceived.negate());

      List<ShipmentInOutLine> inOutLines = inOut.getMaterialMgmtShipmentInOutLineList();
      inOutLines.add(newInOutLine);
      inOut.setMaterialMgmtShipmentInOutLineList(inOutLines);

      OBDal.getInstance().save(newInOutLine);
      OBDal.getInstance().save(inOut);
      OBDal.getInstance().flush();
    }
    if (setRefNo && rmVendorRefs.size() == 1) {
      inOut.setOrderReference(rmVendorRefs.first());
      OBDal.getInstance().save(inOut);
      OBDal.getInstance().flush();
    }
  }
}
