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

package org.openbravo.client.application.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.order.OrderTax;

/**
 * Listens to delete events for a list of entities and modifies deletions order of deletes done for
 * child tables to not run into problems with the hibernate cascading delete order and the various
 * triggers attached to those tables.
 * 
 * So far this class has special handling for deletions of c_order & c_invoice. As explained in
 * issue 17199 there are triggers attached to c_orderlinetax which in some cases delete c_ordertax
 * entries attached to same order. As hibernate already scheduled deletion for those also, the
 * following deletion fails as the record is not longer present. This class works around that by
 * explicitely deleting c_ordertax entries on delete of c_order. (Same for c_invoicetax)
 * 
 * @link https://issues.openbravo.com/view.php?id=17199
 * @author shuehner
 */
public class ReorderCascadingChildDeletionsHandler extends EntityPersistenceEventObserver {
  private static final Logger log = Logger.getLogger(ReorderCascadingChildDeletionsHandler.class);

  private static final String ORDER_TABLE_ID = "259";
  private static final String INVOICE_TABLE_ID = "318";
  private static Entity orderEntity = ModelProvider.getInstance()
      .getEntityByTableId(ORDER_TABLE_ID);
  private static Entity invoiceEntity = ModelProvider.getInstance().getEntityByTableId(
      INVOICE_TABLE_ID);
  private static Entity[] entities = { orderEntity, invoiceEntity };

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    Entity targetEntity = event.getTargetInstance().getEntity();

    if (targetEntity == orderEntity) {
      log.debug("Pre-deleting c_ordertax for order: " + event.getId());
      OBCriteria<OrderTax> otc = OBDal.getInstance().createCriteria(OrderTax.class);
      otc.add(Restrictions.eq(OrderTax.PROPERTY_SALESORDER, event.getTargetInstance()));
      List<OrderTax> otList = otc.list();
      for (OrderTax ot : otList) {
        OBDal.getInstance().remove(ot);
      }
    } else if (targetEntity == invoiceEntity) {
      log.debug("Pre-deleting c_invoicetax for invoice: " + event.getId());
      OBCriteria<InvoiceTax> otc = OBDal.getInstance().createCriteria(InvoiceTax.class);
      otc.add(Restrictions.eq(InvoiceTax.PROPERTY_INVOICE, event.getTargetInstance()));
      List<InvoiceTax> otList = otc.list();
      for (InvoiceTax ot : otList) {
        OBDal.getInstance().remove(ot);
      }
    }
  }

  @Override
  protected synchronized Entity[] getObservedEntities() {
    return entities;
  }
}
