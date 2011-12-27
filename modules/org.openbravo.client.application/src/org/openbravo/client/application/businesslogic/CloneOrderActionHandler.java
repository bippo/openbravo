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
 * Contributor(s):  Mallikarjun M
 ************************************************************************
 */
package org.openbravo.client.application.businesslogic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;

/**
 * When user on the Sales Order window and have a Sales Order displayed / selected, you then click a
 * button on the toolbar (where the 'new' order button is, among other buttons) called 'Clone
 * Order'. The process would then create a new order, and copy the information from the old order to
 * the new one.
 * 
 * @author Mallikarjun M
 * 
 */
public class CloneOrderActionHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    final DataToJsonConverter jsonConverter = new DataToJsonConverter();
    JSONObject json = null;
    try {
      String orderId = (String) parameters.get("orderId");
      Order objOrder = OBDal.getInstance().get(Order.class, orderId);
      Order objCloneOrder = (Order) DalUtil.copy(objOrder, false);
      BigDecimal bLineNetAmt = getLineNetAmt(orderId);

      objCloneOrder.setDocumentAction("CO");
      objCloneOrder.setDocumentStatus("DR");
      objCloneOrder.setPosted("N");
      objCloneOrder.setProcessed(false);
      objCloneOrder.setSalesTransaction(true);
      objCloneOrder.setDocumentNo(null);
      // save the cloned order object
      OBDal.getInstance().save(objCloneOrder);

      objCloneOrder.setSummedLineAmount(objCloneOrder.getSummedLineAmount().subtract(bLineNetAmt));
      objCloneOrder.setGrandTotalAmount(objCloneOrder.getGrandTotalAmount().subtract(bLineNetAmt));

      // get the lines associated with the order and clone them to the new
      // order line.
      for (OrderLine ordLine : objOrder.getOrderLineList()) {
        String strPriceVersionId = getPriceListVersion(objOrder.getPriceList().getId(), objOrder
            .getClient().getId());
        BigDecimal bdPriceList = getPriceList(ordLine.getProduct().getId(), strPriceVersionId);
        OrderLine objCloneOrdLine = (OrderLine) DalUtil.copy(ordLine, false);
        objCloneOrdLine.setReservedQuantity(new BigDecimal("0"));
        objCloneOrdLine.setDeliveredQuantity(new BigDecimal("0"));
        objCloneOrdLine.setInvoicedQuantity(new BigDecimal("0"));
        objCloneOrdLine.setListPrice(bdPriceList);
        objCloneOrder.getOrderLineList().add(objCloneOrdLine);
        objCloneOrdLine.setSalesOrder(objCloneOrder);
      }

      OBDal.getInstance().save(objCloneOrder);

      OBDal.getInstance().flush();
      OBDal.getInstance().refresh(objCloneOrder);
      json = jsonConverter.toJsonObject(objCloneOrder, DataResolvingMode.FULL);
      OBDal.getInstance().commitAndClose();
      return json;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  private String getPriceListVersion(String priceList, String clientId) {
    try {
      String whereClause = " as plv , PricingPriceList pl where pl.id=plv.id and plv.active='Y' and "
          + " pl.id = :priceList and plv.client.id = :clientId order by plv.validFromDate desc";

      OBQuery<PriceListVersion> ppriceListVersion = OBDal.getInstance().createQuery(
          PriceListVersion.class, whereClause);
      ppriceListVersion.setNamedParameter("priceList", priceList);
      ppriceListVersion.setNamedParameter("clientId", clientId);

      if (!ppriceListVersion.list().isEmpty()) {
        return ppriceListVersion.list().get(0).getId();
      } else {
        return "0";
      }
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  private BigDecimal getPriceList(String strProductID, String strPriceVersionId) {
    BigDecimal bdPriceList = null;
    try {
      final List<Object> parameters = new ArrayList<Object>();
      parameters.add(strProductID);
      parameters.add(strPriceVersionId);
      final String procedureName = "M_BOM_PriceList";
      bdPriceList = (BigDecimal) CallStoredProcedure.getInstance().call(procedureName, parameters,
          null);
    } catch (Exception e) {
      throw new OBException(e);
    }

    return (bdPriceList);
  }

  public static BigDecimal getLineNetAmt(String strOrderId) {

    BigDecimal bdLineNetAmt = new BigDecimal("0");
    final String readLineNetAmtHql = " select (ol.lineNetAmount + ol.freightAmount + ol.chargeAmount) as LineNetAmt from OrderLine ol where ol.salesOrder.id=?";
    final Query readLineNetAmtQry = OBDal.getInstance().getSession().createQuery(readLineNetAmtHql);
    readLineNetAmtQry.setString(0, strOrderId);

    for (int i = 0; i < readLineNetAmtQry.list().size(); i++) {
      bdLineNetAmt = bdLineNetAmt.add(((BigDecimal) readLineNetAmtQry.list().get(i)));
    }

    return bdLineNetAmt;
  }

}
