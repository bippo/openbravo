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

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.ReturnReason;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

/**
 * 
 * @author gorkaion
 * 
 */
public class SRMOPickEditLines extends BaseProcessActionHandler {
  private static Logger log = Logger.getLogger(SRMOPickEditLines.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    try {
      jsonRequest = new JSONObject(content);
      log.debug(jsonRequest);
      final String strOrderId = jsonRequest.getString("inpcOrderId");
      Order order = OBDal.getInstance().get(Order.class, strOrderId);
      if (cleanOrderLines(order)) {
        createOrderLines(jsonRequest);
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private boolean cleanOrderLines(Order order) {
    if (order.getOrderLineList().isEmpty()) {
      // nothing to delete.
      return true;
    }
    try {
      order.getOrderLineList().clear();
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
    return true;
  }

  private void createOrderLines(JSONObject jsonRequest) throws JSONException {
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    // if no lines selected don't do anything.
    if (selectedLines.length() == 0) {
      return;
    }
    final String strOrderId = jsonRequest.getString("inpcOrderId");
    Order order = OBDal.getInstance().get(Order.class, strOrderId);
    for (long i = 0; i < selectedLines.length(); i++) {
      JSONObject selectedLine = selectedLines.getJSONObject((int) i);
      log.debug(selectedLine);
      OrderLine newOrderLine = OBProvider.getInstance().get(OrderLine.class);
      newOrderLine.setSalesOrder(order);
      newOrderLine.setOrganization(order.getOrganization());
      newOrderLine.setLineNo((i + 1L) * 10L);
      newOrderLine.setOrderDate(order.getOrderDate());
      newOrderLine.setWarehouse(order.getWarehouse());
      newOrderLine.setCurrency(order.getCurrency());

      ShipmentInOutLine shipmentLine = OBDal.getInstance().get(ShipmentInOutLine.class,
          selectedLine.getString("goodsShipmentLine"));
      newOrderLine.setGoodsShipmentLine(shipmentLine);
      newOrderLine.setProduct(shipmentLine.getProduct());
      newOrderLine.setAttributeSetValue(shipmentLine.getAttributeSetValue());
      newOrderLine.setUOM(shipmentLine.getUOM());
      // Ordered Quantity = returned quantity.
      BigDecimal qtyReturned = new BigDecimal(selectedLine.getString("returned"));
      newOrderLine.setOrderedQuantity(qtyReturned.negate());
      // Price
      BigDecimal price = new BigDecimal(selectedLine.getString("unitPrice"));
      newOrderLine.setUnitPrice(price);
      newOrderLine.setListPrice(shipmentLine.getSalesOrderLine().getListPrice());
      newOrderLine.setPriceLimit(shipmentLine.getSalesOrderLine().getPriceLimit());
      newOrderLine.setStandardPrice(shipmentLine.getSalesOrderLine().getStandardPrice());

      newOrderLine.setTax(shipmentLine.getSalesOrderLine().getTax());

      if (selectedLine.getString("returnReason") != null
          && !selectedLine.getString("returnReason").equals("null")) {
        newOrderLine.setReturnReason(OBDal.getInstance().get(ReturnReason.class,
            selectedLine.getString("returnReason")));
      } else {
        newOrderLine.setReturnReason(order.getReturnReason());
      }

      List<OrderLine> orderLines = order.getOrderLineList();
      orderLines.add(newOrderLine);
      order.setOrderLineList(orderLines);

      OBDal.getInstance().save(newOrderLine);
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();
    }
  }
}
