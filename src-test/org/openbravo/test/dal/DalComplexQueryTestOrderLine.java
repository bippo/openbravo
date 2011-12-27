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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.dal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.test.base.BaseTest;

/**
 * Tests a complex query using the DAL, is used in the developers guide.
 * 
 * @author mtaal
 */

public class DalComplexQueryTestOrderLine extends BaseTest {

  private static final Logger log = Logger.getLogger(DalComplexQueryTestOrderLine.class);

  // SELECT C_ORDERLINE.C_ORDERLINE_ID AS ID, C_ORDER.C_ORDER_ID AS C_ORDER_ID, C_ORDER.DOCUMENTNO
  // AS DOCUMENTNO, C_ORDER.DATEORDERED AS DATEORDERED,
  // C_BPARTNER.C_BPARTNER_ID AS C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER_NAME,
  // AD_COLUMN_IDENTIFIER(TO_CHAR('M_Product'), TO_CHAR(C_ORDERLINE.M_PRODUCT_ID), TO_CHAR(?)) AS
  // PRODUCT_NAME, M_ATTRIBUTESETINSTANCE.DESCRIPTION AS DESCRIPTION, C_ORDERLINE.QTYORDERED AS
  // TOTAL_QTY,
  // C_ORDERLINE.QTYORDERED-SUM(COALESCE(M_MATCHPO.QTY,0)) AS QTYORDERED, '-1' AS ISACTIVE
  // FROM C_ORDERLINE left join M_MATCHPO on C_ORDERLINE.C_ORDERLINE_ID = M_MATCHPO.C_ORDERLINE_ID
  // and M_MATCHPO.M_INOUTLINE_ID IS NOT NULL
  // left join M_ATTRIBUTESETINSTANCE on C_ORDERLINE.M_ATTRIBUTESETINSTANCE_ID =
  // M_ATTRIBUTESETINSTANCE.M_ATTRIBUTESETINSTANCE_ID,
  // C_ORDER, C_BPARTNER
  // WHERE C_ORDER.C_BPARTNER_ID = C_BPARTNER.C_BPARTNER_ID
  // AND C_ORDER.C_ORDER_ID = C_ORDERLINE.C_ORDER_ID
  // AND C_ORDER.AD_CLIENT_ID IN ('1')
  // AND C_ORDER.AD_ORG_ID IN ('1')
  // AND C_ORDER.ISSOTRX='N'
  // AND C_ORDER.DOCSTATUS = 'CO'
  // GROUP BY C_ORDERLINE.C_ORDERLINE_ID, C_ORDER.C_ORDER_ID, C_ORDER.DOCUMENTNO,
  // C_ORDER.DATEORDERED, C_BPARTNER.C_BPARTNER_ID,
  // C_BPARTNER.NAME, C_ORDERLINE.M_PRODUCT_ID, M_ATTRIBUTESETINSTANCE.DESCRIPTION,
  // C_ORDERLINE.QTYORDERED
  // ORDER BY PARTNER_NAME, DOCUMENTNO, DATEORDERED

  /**
   * Tests a complexer query related to order lines.
   */
  public void testComplexOBQuery() {
    setTestAdminContext();

    // create the where clause
    final StringBuilder whereClause = new StringBuilder();
    // set the alias, the OrderLine will be added by the DAL
    whereClause.append(" as ol ");

    // whereClause.append(" left join fetch ol.product ");
    // whereClause.append(" left join fetch ol.salesOrder ");
    // whereClause.append(" left join fetch ol.businessPartner ");
    // whereClause.append(" left join fetch ol.businessPartner.language ");

    // the subselect to filter on the matched invoices, only orders with a
    // different order quantity are returned.
    whereClause.append(" where ol.orderedQuantity <> ");
    whereClause.append(" (select sum(quantity) from ProcurementPOInvoiceMatch "
        + "where goodsShipmentLine is not null and salesOrderLine=ol)");

    // Other filtering options:
    // AND C_ORDER.AD_CLIENT_ID IN ('1') <-- these are done automatically by the dal
    // AND C_ORDER.AD_ORG_ID IN ('1') <-- these are done automatically by the dal

    // AND C_ORDER.ISSOTRX='N'
    whereClause.append(" and ol.salesOrder.salesTransaction=false ");

    // AND C_BPARTNER.C_BPARTNER_ID = ?
    // note the value of the parameter is set below
    whereClause.append(" and ol.salesOrder.businessPartner.id=? ");

    // AND C_ORDER.DOCSTATUS = 'CO'
    whereClause.append(" and ol.salesOrder.documentStatus='CO' ");

    // ORDER BY C_BPARTNER_ID, ID
    whereClause.append(" order by ol.salesOrder.businessPartner.id, ol.id");

    // final Session session = OBDal.getInstance().getSession();
    // session.createQuery(hql.toString());
    final OBQuery<OrderLine> qry = OBDal.getInstance().createQuery(OrderLine.class,
        whereClause.toString());

    // set the business partner parameter
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add("1000017");
    qry.setParameters(parameters);

    for (OrderLine ol : qry.list()) {

      // C_ORDERLINE.C_ORDERLINE_ID AS ID, C_ORDER.C_ORDER_ID AS C_ORDER_ID
      log.debug(ol.getId());
      log.debug(ol.getSalesOrder().getId());

      // C_ORDER.DOCUMENTNO AS DOCUMENTNO, C_ORDER.DATEORDERED AS DATEORDERED,
      log.debug(ol.getSalesOrder().getDocumentNo());
      log.debug(ol.getSalesOrder().getOrderDate());

      // C_BPARTNER.C_BPARTNER_ID AS C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER_NAME,
      log.debug(ol.getSalesOrder().getBusinessPartner().getId());
      log.debug(ol.getSalesOrder().getBusinessPartner().getName());

      // AD_COLUMN_IDENTIFIER(TO_CHAR('M_Product'), TO_CHAR(C_ORDERLINE.M_PRODUCT_ID),
      // TO_CHAR(?)) AS PRODUCT_NAME,
      log.debug(ol.getProduct().getIdentifier());
      log.debug(ol.getProduct().getId());
      log.debug(ol.getProduct().getName());

      // M_ATTRIBUTESETINSTANCE.DESCRIPTION AS DESCRIPTION,
      if (ol.getAttributeSetValue() != null) {
        log.debug(ol.getAttributeSetValue().getDescription());
      }

      // C_ORDERLINE.QTYORDERED AS TOTAL_QTY,
      log.debug(ol.getOrderedQuantity());

      // C_ORDERLINE.QTYORDERED-SUM(COALESCE(M_MATCHPO.QTY,0)) AS QTYORDERED, '-1' AS ISACTIVE
      // todo this we have to repeat the sum query, we use direct hql for this
      final String hql = "select sum(quantity) from ProcurementPOInvoiceMatch "
          + "where goodsShipmentLine is not null and salesOrderLine=?";
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hql);
      query.setParameter(0, ol);
      final BigDecimal sum = (BigDecimal) query.uniqueResult();
      log.debug(sum);
    }
  }

  public void testComplexQueryTwoHQL() {
    setTestAdminContext();

    final StringBuilder selectClause = new StringBuilder();

    // SELECT ID, C_ORDER_ID, DOCUMENTNO, DATEORDERED, C_BPARTNER_ID, PARTNER_NAME, PRODUCT_NAME,
    // DESCRIPTION, TOTAL_QTY,
    // QTYORDERED, ISACTIVE, ? AS DATE_FORMAT
    selectClause
        .append("select ol.id, ol.salesOrder.id, ol.salesOrder.documentNo, ol.salesOrder.orderDate, ");
    selectClause
        .append(" ol.salesOrder.businessPartner.id, ol.salesOrder.businessPartner.name, ol.product.name, ");
    selectClause.append(" ol.orderedQuantity, sum(matchPO.quantity) as totalQty ");

    final StringBuilder fromClause = new StringBuilder();
    fromClause.append("from OrderLine ol, ProcurementPOInvoiceMatch matchPO ");

    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" where ol.orderedQuantity <> ");
    whereClause.append(" (select sum(quantity) from ProcurementPOInvoiceMatch "
        + "where goodsShipmentLine is not null and salesOrderLine=ol)");

    whereClause.append(" and ol = matchPO.salesOrderLine ");

    // Other filtering options:
    // AND C_ORDER.AD_CLIENT_ID IN ('1') <-- these are done automatically by the dal
    // AND C_ORDER.AD_ORG_ID IN ('1') <-- these are done automatically by the dal

    // AND C_ORDER.ISSOTRX='N'
    whereClause.append(" and ol.salesOrder.salesTransaction=false ");

    // AND C_BPARTNER.C_BPARTNER_ID = ?
    // note the value of the parameter is set below
    whereClause.append(" and ol.salesOrder.businessPartner.id=? ");

    // AND C_ORDER.DOCSTATUS = 'CO'
    whereClause.append(" and ol.salesOrder.documentStatus='CO' ");

    // Add the readable organization and client clauses
    whereClause.append(" and ol.organization "
        + OBDal.getInstance().getReadableOrganizationsInClause());
    whereClause.append(" and ol.client " + OBDal.getInstance().getReadableClientsInClause());

    // append active
    whereClause.append(" and ol.active=true");

    // ORDER BY C_BPARTNER_ID, ID
    final StringBuilder orderByClause = new StringBuilder();
    orderByClause.append(" order by ol.salesOrder.businessPartner.id, ol.id");

    final StringBuilder groupClause = new StringBuilder();
    groupClause
        .append(" group by ol.id, ol.salesOrder.id, ol.salesOrder.documentNo, ol.salesOrder.orderDate, ");
    groupClause
        .append(" ol.salesOrder.businessPartner.id, ol.salesOrder.businessPartner.name, ol.product.name, ");
    groupClause.append(" ol.orderedQuantity");

    final String hql = selectClause.toString() + fromClause.toString() + whereClause.toString()
        + groupClause.toString() + orderByClause.toString();

    log.debug(hql);

    // final Session session = OBDal.getInstance().getSession();
    // session.createQuery(hql.toString());
    final Query query = OBDal.getInstance().getSession().createQuery(hql);
    query.setParameter(0, "1000017");

    for (Object o : query.list()) {
      final Object[] os = (Object[]) o;
      for (Object result : os) {
        log.debug(result);
      }
    }
  }
}