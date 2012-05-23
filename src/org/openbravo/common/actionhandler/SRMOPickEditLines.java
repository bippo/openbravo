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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.ReturnReason;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;

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
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();

      try {
        jsonRequest = new JSONObject();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text",
            Utility.messageBD(new DalConnectionProvider(), e.getMessage(), vars.getLanguage()));

        jsonRequest.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }

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

  private void createOrderLines(JSONObject jsonRequest) throws JSONException, OBException {
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
      HashMap<String, BigDecimal> prices = null;

      if (shipmentLine.getSalesOrderLine() == null) {
        prices = getPrices(strOrderId, shipmentLine.getProduct().getId(), order.getOrderDate());
      }

      if (prices != null) {
        newOrderLine.setUnitPrice(prices.get("unitPrice"));
        newOrderLine.setListPrice(prices.get("listPrice"));
        newOrderLine.setPriceLimit(prices.get("priceLimit"));
        newOrderLine.setStandardPrice(prices.get("standardPrice"));

        // tax
        List<Object> parameters = new ArrayList<Object>();

        parameters.add(shipmentLine.getProduct().getId());
        parameters.add(order.getOrderDate());
        parameters.add(order.getOrganization().getId());
        parameters.add(order.getWarehouse().getId());
        parameters.add(order.getPartnerAddress().getId());
        parameters.add(order.getInvoiceAddress().getId());
        if (order.getProject() != null) {
          parameters.add(order.getProject().getId());
        } else {
          parameters.add(null);
        }
        parameters.add("Y");

        String taxId = (String) CallStoredProcedure.getInstance()
            .call("C_Gettax", parameters, null);
        TaxRate tax = OBDal.getInstance().get(TaxRate.class, taxId);

        newOrderLine.setTax(tax);
      } else {
        newOrderLine.setUnitPrice(new BigDecimal(selectedLine.getString("unitPrice")));
        newOrderLine.setListPrice(shipmentLine.getSalesOrderLine().getListPrice());
        newOrderLine.setPriceLimit(shipmentLine.getSalesOrderLine().getPriceLimit());
        newOrderLine.setStandardPrice(shipmentLine.getSalesOrderLine().getStandardPrice());

        newOrderLine.setTax(shipmentLine.getSalesOrderLine().getTax());
      }

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

  HashMap<String, BigDecimal> getPrices(String strOrderId, String strProductId, Date orderDate)
      throws OBException {
    HashMap<String, BigDecimal> prices = new HashMap<String, BigDecimal>();
    try {
      OBContext.setAdminMode(true);
      Order o = OBDal.getInstance().get(Order.class, strOrderId);
      PriceList pl = o.getBusinessPartner().getPurchasePricelist();
      PriceListVersion earliestPlv = null;
      boolean isDefultPriceList = false;
      // There is no price list in the Busines Partner so default sales price list is taken.
      if (pl == null) {
        pl = getDefaultSalesPriceList(o.getClient().getId(), o.getOrganization().getId());
        isDefultPriceList = true;
      }

      // Get a fit price list version.
      if (pl != null) {
        earliestPlv = getEarliestPriceListVersion(pl, orderDate);
      } else {
        // If pl was the default price list, there is no default price list.
        if (isDefultPriceList) {
          throw new OBException("NoDefaultPriceList");
        } else {
          // If pl was the Business Partner's price list, there was no fit price list for it so take
          // the default price list.
          pl = getDefaultSalesPriceList(o.getClient().getId(), o.getOrganization().getId());
          earliestPlv = getEarliestPriceListVersion(pl, orderDate);
          // There is no fit price list version.
          if (earliestPlv == null) {
            throw new OBException("NoDefaultPriceList");
          }
        }
      }

      if (earliestPlv != null) {
        prices = getProductPricesFromPLV(strProductId, earliestPlv);
        if (prices == null && isDefultPriceList) {
          throw new OBException("NoProductInDefaultPriceList");
        } else {
          pl = getDefaultSalesPriceList(o.getClient().getId(), o.getOrganization().getId());
          earliestPlv = getEarliestPriceListVersion(pl, orderDate);
          if (earliestPlv == null) {
            throw new OBException("NoDefaultPriceList");
          } else {
            prices = getProductPricesFromPLV(strProductId, earliestPlv);
            if (prices == null) {
              throw new OBException("NoProductInDefaultPriceList");
            } else {
              return prices;
            }
          }
        }
      }
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  PriceList getDefaultSalesPriceList(String clientId, String orgId) throws OBException {
    List<PriceList> pll = null;
    final OBCriteria<PriceList> obc = OBDal.getInstance().createCriteria(PriceList.class);

    obc.add(Restrictions.eq("default", true));
    obc.add(Restrictions.eq("salesPriceList", true));
    ArrayList<Organization> orgs = new ArrayList<Organization>();
    for (String orgauxId : OBContext.getOBContext().getOrganizationStructureProvider(clientId)
        .getNaturalTree(orgId)) {
      orgs.add(OBDal.getInstance().get(Organization.class, orgauxId));
    }
    obc.add(Restrictions.in("organization", orgs));
    pll = obc.list();
    if (pll.size() > 0) {
      return obc.list().get(0);
    }
    return null;
  }

  PriceListVersion getEarliestPriceListVersion(PriceList pl, Date orderDate) {
    PriceListVersion plv, earliestPlv = null;

    for (int j = 0; j < pl.getPricingPriceListVersionList().size(); j++) {
      plv = pl.getPricingPriceListVersionList().get(j);

      if (plv != null && plv.getValidFromDate().before(orderDate)) {
        if (earliestPlv == null) {
          earliestPlv = plv;
        }
        if (plv.getValidFromDate().before(earliestPlv.getValidFromDate())) {
          earliestPlv = plv;
        }
      }
    }
    return earliestPlv;
  }

  HashMap<String, BigDecimal> getProductPricesFromPLV(String strProductId,
      PriceListVersion earliestPlv) {
    List<ProductPrice> ppl = earliestPlv.getPricingProductPriceList();
    HashMap<String, BigDecimal> prices = new HashMap<String, BigDecimal>();

    for (int j = 0; j < ppl.size(); j++) {
      if (ppl.get(j).getProduct().getId().equalsIgnoreCase(strProductId)) {
        prices.put("unitPrice", ppl.get(j).getStandardPrice());
        prices.put("standardPrice", ppl.get(j).getStandardPrice());
        prices.put("listPrice", ppl.get(j).getListPrice());
        prices.put("priceLimit", ppl.get(j).getPriceLimit());
        return prices;
      }
    }

    return null;
  }
}
