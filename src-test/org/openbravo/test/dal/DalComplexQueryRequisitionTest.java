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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.procurement.RequisitionLine;
import org.openbravo.test.base.BaseTest;

/**
 * Tests a complex query using the DAL. Is used in the Developers Guide HowTo.
 * 
 * @author mtaal
 */

public class DalComplexQueryRequisitionTest extends BaseTest {

  private static final Logger log = Logger.getLogger(DalComplexQueryRequisitionTest.class);

  // SELECT M_REQUISITIONLINE_ID, M_REQUISITIONLINE.NEEDBYDATE,
  // M_REQUISITIONLINE.QTY - M_REQUISITIONLINE.ORDEREDQTY AS QTYTOORDER,
  // M_REQUISITIONLINE.PRICEACTUAL AS PRICE,
  // AD_COLUMN_IDENTIFIER(to_char('C_BPartner'),
  // to_char(COALESCE(M_REQUISITIONLINE.C_BPARTNER_ID, M_REQUISITION.C_BPARTNER_ID)), ?) AS VENDOR,
  // AD_COLUMN_IDENTIFIER(to_char('M_PriceList'), to_char(COALESCE(M_REQUISITIONLINE.M_PRICELIST_ID,
  // M_REQUISITION.M_PRICELIST_ID)), ?) AS PRICELISTID,
  // AD_COLUMN_IDENTIFIER(to_char('M_Product'),
  // to_char(M_REQUISITIONLINE.M_PRODUCT_ID), ?) AS PRODUCT,
  // AD_COLUMN_IDENTIFIER(to_char('M_AttributeSetInstance'),
  // to_char(M_REQUISITIONLINE.M_ATTRIBUTESETINSTANCE_ID), ?) AS ATTRIBUTE,
  // AD_COLUMN_IDENTIFIER(to_char('AD_User'), to_char(M_REQUISITION.AD_USER_ID), ?) AS REQUESTER
  // FROM M_REQUISITIONLINE, M_REQUISITION, C_BPARTNER
  // WHERE M_REQUISITIONLINE.M_REQUISITION_ID = M_REQUISITION.M_REQUISITION_ID
  // AND COALESCE(M_REQUISITIONLINE.C_BPARTNER_ID,M_REQUISITION.C_BPARTNER_ID) =
  // C_BPARTNER.C_BPARTNER_ID
  // AND C_BPARTNER.PO_PAYMENTTERM_ID IS NOT NULL
  // AND M_REQUISITION.ISACTIVE = 'Y'
  // AND M_REQUISITIONLINE.ISACTIVE = 'Y'
  // AND M_REQUISITION.DOCSTATUS = 'CO'
  // AND M_REQUISITIONLINE.REQSTATUS = 'O'
  // AND (M_REQUISITIONLINE.LOCKEDBY IS NULL OR
  // COALESCE (M_REQUISITIONLINE.LOCKDATE, TO_DATE('01-01-1900', 'DD-MM-YYYY')) < (now()-3))
  // AND M_REQUISITION.AD_CLIENT_ID IN ?
  // AND M_REQUISITIONLINE.AD_ORG_ID IN ?
  // AND M_REQUISITIONLINE.NEEDBYDATE >= ?
  // AND AND M_REQUISITIONLINE.NEEDBYDATE < ?
  // AND M_REQUISITIONLINE.M_PRODUCT_ID = ?
  // AND M_REQUISITION.AD_USER_ID = TO_CHAR(?)
  // AND ((M_REQUISITIONLINE.C_BPARTNER_ID = ? OR M_REQUISITION.C_BPARTNER_ID = ?) OR
  // (M_REQUISITIONLINE.C_BPARTNER_ID IS NULL AND M_REQUISITION.C_BPARTNER_ID IS NULL))
  // ORDER BY M_REQUISITIONLINE.NEEDBYDATE, M_REQUISITIONLINE.M_PRODUCT_ID,
  // M_REQUISITIONLINE.M_ATTRIBUTESETINSTANCE_ID

  /**
   * Tests a complex query related to requisition lines.
   */
  public void testComplexQueryOne() {
    setTestAdminContext();

    // the query parameters are added to this list
    final List<Object> parameters = new ArrayList<Object>();
    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as rl");

    // do a left outer join on business partner
    whereClause.append(" left join fetch rl.product");
    whereClause.append(" left join fetch rl.businessPartner rlbp");
    whereClause.append(" left join fetch rlbp.language");
    whereClause.append(" left join fetch rl.requisition rlr");
    whereClause.append(" left join fetch rl.priceList");
    whereClause.append(" left join fetch rlr.businessPartner rlrbp");
    whereClause.append(" left join fetch rlrbp.language");

    // AND COALESCE(M_REQUISITIONLINE.C_BPARTNER_ID,M_REQUISITION.C_BPARTNER_ID) =
    // C_BPARTNER.C_BPARTNER_ID
    // AND C_BPARTNER.PO_PAYMENTTERM_ID IS NOT NULL
    whereClause
        .append(" where (rl.businessPartner.pOPaymentTerms != null or rl.requisition.businessPartner.pOPaymentTerms != null)");

    // AND M_REQUISITION.ISACTIVE = 'Y'
    whereClause.append(" and rl.requisition.active=true");

    // AND M_REQUISITIONLINE.ISACTIVE = 'Y' <-- is done by the DAL Layer

    // AND M_REQUISITION.DOCSTATUS = 'CO'
    whereClause.append(" and rl.requisition.documentStatus='CO'");

    // AND M_REQUISITIONLINE.REQSTATUS = 'O'
    whereClause.append(" and rl.requisitionLineStatus='O'");

    // AND (M_REQUISITIONLINE.LOCKEDBY IS NULL OR
    // COALESCE (M_REQUISITIONLINE.LOCKDATE, TO_DATE('01-01-1900', 'DD-MM-YYYY')) < (now()-3))
    whereClause.append(" and (rl.lockedBy = null or rl.lockDate<? or rl.lockDate = null)");
    final long threeDays = 1000 * 3600 * 24 * 3;
    parameters.add(new Date(System.currentTimeMillis() - threeDays));

    // AND M_REQUISITION.AD_CLIENT_ID IN ? <-- Done by the DAL
    // AND M_REQUISITIONLINE.AD_ORG_ID IN ? <-- Done by the DAL

    // AND M_REQUISITIONLINE.NEEDBYDATE >= ?
    whereClause.append(" and rl.needByDate>=?");
    // needByDate from, set at 30 days back
    final long thirtyDays = threeDays * 10;
    parameters.add(new Date(System.currentTimeMillis() - thirtyDays));

    // AND AND M_REQUISITIONLINE.NEEDBYDATE < ?
    whereClause.append(" and rl.needByDate<?");
    // needByDate to, set at 30 days in the future
    parameters.add(new Date(System.currentTimeMillis() + thirtyDays));

    // AND M_REQUISITIONLINE.M_PRODUCT_ID = ?
    whereClause.append(" and rl.product.id=?");
    parameters.add("1000010");

    // AND M_REQUISITION.AD_USER_ID = TO_CHAR(?)
    whereClause.append(" and rl.requisition.userContact.id=?");
    parameters.add("100");

    // AND ((M_REQUISITIONLINE.C_BPARTNER_ID = ? OR M_REQUISITION.C_BPARTNER_ID = ?) OR
    // (M_REQUISITIONLINE.C_BPARTNER_ID IS NULL AND M_REQUISITION.C_BPARTNER_ID IS NULL))
    // ORDER BY M_REQUISITIONLINE.NEEDBYDATE, M_REQUISITIONLINE.M_PRODUCT_ID,
    // M_REQUISITIONLINE.M_ATTRIBUTESETINSTANCE_ID
    whereClause
        .append(" and ((rl.businessPartner.id = ? or rl.requisition.businessPartner.id = ?) or "
            + "(rl.businessPartner = null and rl.requisition.businessPartner = null))");
    parameters.add("1000011");
    parameters.add("1000011");

    // ORDER BY M_REQUISITIONLINE.NEEDBYDATE, M_REQUISITIONLINE.M_PRODUCT_ID,
    // M_REQUISITIONLINE.M_ATTRIBUTESETINSTANCE_ID
    whereClause.append(" order by rl.needByDate, rl.product.id, rl.attributeSetValue.id");

    final OBQuery<RequisitionLine> obQuery = OBDal.getInstance().createQuery(RequisitionLine.class,
        whereClause.toString());

    obQuery.setParameters(parameters);

    // now print the select clause parts
    for (RequisitionLine requisitionLine : obQuery.list()) {
      // now print the information from the select clause
      // SELECT M_REQUISITIONLINE_ID, M_REQUISITIONLINE.NEEDBYDATE,
      log.debug(requisitionLine.getId());
      log.debug(requisitionLine.getNeedByDate());
      // M_REQUISITIONLINE.QTY - M_REQUISITIONLINE.ORDEREDQTY AS QTYTOORDER,
      if (requisitionLine.getOrderQuantity() != null) {
        log.debug(requisitionLine.getQuantity().min(requisitionLine.getOrderQuantity()));
      }
      // M_REQUISITIONLINE.PRICEACTUAL AS PRICE,
      log.debug(requisitionLine.getUnitPrice());

      // AD_COLUMN_IDENTIFIER(to_char('C_BPartner'),
      // to_char(COALESCE(M_REQUISITIONLINE.C_BPARTNER_ID, M_REQUISITION.C_BPARTNER_ID)), ?) AS
      // VENDOR,
      if (requisitionLine.getBusinessPartner() != null) {
        log.debug(requisitionLine.getBusinessPartner().getIdentifier());
      } else if (requisitionLine.getRequisition().getBusinessPartner() != null) {
        log.debug(requisitionLine.getRequisition().getBusinessPartner().getIdentifier());
      }

      // AD_COLUMN_IDENTIFIER(to_char('M_PriceList'),
      // to_char(COALESCE(M_REQUISITIONLINE.M_PRICELIST_ID,
      // M_REQUISITION.M_PRICELIST_ID)), ?) AS PRICELISTID,
      if (requisitionLine.getPriceList() != null) {
        log.debug(requisitionLine.getPriceList().getIdentifier());
      } else if (requisitionLine.getRequisition().getPriceList() != null) {
        log.debug(requisitionLine.getRequisition().getPriceList().getIdentifier());
      }

      // AD_COLUMN_IDENTIFIER(to_char('M_Product'),
      // to_char(M_REQUISITIONLINE.M_PRODUCT_ID), ?) AS PRODUCT,
      log.debug(requisitionLine.getProduct().getIdentifier());

      // AD_COLUMN_IDENTIFIER(to_char('M_AttributeSetInstance'),
      // to_char(M_REQUISITIONLINE.M_ATTRIBUTESETINSTANCE_ID), ?) AS ATTRIBUTE,
      if (requisitionLine.getAttributeSetValue() != null) {
        log.debug(requisitionLine.getAttributeSetValue().getIdentifier());
      }

      // AD_COLUMN_IDENTIFIER(to_char('AD_User'), to_char(M_REQUISITION.AD_USER_ID), ?) AS REQUESTER
      log.debug(requisitionLine.getRequisition().getUserContact().getIdentifier());

      log.debug(requisitionLine.getIdentifier());
    }
  }

  public void _testComplexQueryOne() {
    setTestAdminContext();

    final StringBuilder whereClause = new StringBuilder();

    whereClause.append(" left outer join businessPartner as bp where ");

    // AND COALESCE(M_REQUISITIONLINE.C_BPARTNER_ID,M_REQUISITION.C_BPARTNER_ID) =
    // C_BPARTNER.C_BPARTNER_ID
    // AND C_BPARTNER.PO_PAYMENTTERM_ID IS NOT NULL
    whereClause
        .append(" (bp.pOPaymentTerms != null or requisition.businessPartner.pOPaymentTerms != null)");

    // AND M_REQUISITION.ISACTIVE = 'Y'
    whereClause.append(" and requisition.active=true");

    // AND M_REQUISITIONLINE.ISACTIVE = 'Y' <-- is done by the DAL Layer

    // AND M_REQUISITION.DOCSTATUS = 'CO'
    whereClause.append(" and requisition.documentStatus='CO'");

    // AND M_REQUISITIONLINE.REQSTATUS = 'O'
    whereClause.append(" and requisitionLineStatus='O'");

    // AND (M_REQUISITIONLINE.LOCKEDBY IS NULL OR
    // COALESCE (M_REQUISITIONLINE.LOCKDATE, TO_DATE('01-01-1900', 'DD-MM-YYYY')) < (now()-3))
    whereClause.append(" and (lockedBy = null or lockDate<? or lockDate = null)");

    // AND M_REQUISITION.AD_CLIENT_ID IN ? <-- Done by the DAL
    // AND M_REQUISITIONLINE.AD_ORG_ID IN ? <-- Done by the DAL

    // AND M_REQUISITIONLINE.NEEDBYDATE >= ?
    whereClause.append(" and needByDate>=?");

    // AND AND M_REQUISITIONLINE.NEEDBYDATE < ?
    whereClause.append(" and needByDate<?");

    // AND M_REQUISITIONLINE.M_PRODUCT_ID = ?
    whereClause.append(" and product.id=?");

    // AND M_REQUISITION.AD_USER_ID = TO_CHAR(?)
    whereClause.append(" and requisition.userContact.id=?");

    // AND ((M_REQUISITIONLINE.C_BPARTNER_ID = ? OR M_REQUISITION.C_BPARTNER_ID = ?) OR
    // (M_REQUISITIONLINE.C_BPARTNER_ID IS NULL AND M_REQUISITION.C_BPARTNER_ID IS NULL))
    // ORDER BY M_REQUISITIONLINE.NEEDBYDATE, M_REQUISITIONLINE.M_PRODUCT_ID,
    // M_REQUISITIONLINE.M_ATTRIBUTESETINSTANCE_ID
    whereClause.append(" and ((businessPartner.id = ? or requisition.businessPartner.id = ?) or "
        + "(businessPartner = null and requisition.businessPartner = null))");

    // ORDER BY M_REQUISITIONLINE.NEEDBYDATE, M_REQUISITIONLINE.M_PRODUCT_ID,
    // M_REQUISITIONLINE.M_ATTRIBUTESETINSTANCE_ID
    whereClause.append(" order by needByDate, product.id, attributeSetValue.id");

    final OBQuery<RequisitionLine> obQuery = OBDal.getInstance().createQuery(RequisitionLine.class,
        whereClause.toString());

    // now set the parameters
    final List<Object> parameters = new ArrayList<Object>();

    // lockDate
    final long threeDays = 1000 * 3600 * 24 * 3;
    parameters.add(new Date(System.currentTimeMillis() - threeDays));

    // needByDate from, set at 30 days back
    final long thirtyDays = threeDays * 10;
    parameters.add(new Date(System.currentTimeMillis() - thirtyDays));

    // needByDate to, set at 30 days in the future
    parameters.add(new Date(System.currentTimeMillis() + thirtyDays));

    // product.id
    parameters.add("1000010");

    // userContact.id
    parameters.add("100");

    // businessPartner.id
    parameters.add("1000011");
    parameters.add("1000011");

    obQuery.setParameters(parameters);

    // now print the select clause parts
    for (RequisitionLine requisitionLine : obQuery.list()) {
      // now print the information from the select clause
      // SELECT M_REQUISITIONLINE_ID, M_REQUISITIONLINE.NEEDBYDATE,
      log.debug(requisitionLine.getId());
      log.debug(requisitionLine.getNeedByDate());
      // M_REQUISITIONLINE.QTY - M_REQUISITIONLINE.ORDEREDQTY AS QTYTOORDER,
      log.debug(requisitionLine.getQuantity().min(requisitionLine.getOrderQuantity()));
      // M_REQUISITIONLINE.PRICEACTUAL AS PRICE,
      log.debug(requisitionLine.getUnitPrice());

      // AD_COLUMN_IDENTIFIER(to_char('C_BPartner'),
      // to_char(COALESCE(M_REQUISITIONLINE.C_BPARTNER_ID, M_REQUISITION.C_BPARTNER_ID)), ?) AS
      // VENDOR,
      if (requisitionLine.getBusinessPartner() != null) {
        log.debug(requisitionLine.getBusinessPartner().getIdentifier());
      } else if (requisitionLine.getRequisition().getBusinessPartner() != null) {
        log.debug(requisitionLine.getRequisition().getBusinessPartner().getIdentifier());
      }

      // AD_COLUMN_IDENTIFIER(to_char('M_PriceList'),
      // to_char(COALESCE(M_REQUISITIONLINE.M_PRICELIST_ID,
      // M_REQUISITION.M_PRICELIST_ID)), ?) AS PRICELISTID,
      if (requisitionLine.getPriceList() != null) {
        log.debug(requisitionLine.getPriceList().getIdentifier());
      } else if (requisitionLine.getRequisition().getPriceList() != null) {
        log.debug(requisitionLine.getRequisition().getPriceList().getIdentifier());
      }

      // AD_COLUMN_IDENTIFIER(to_char('M_Product'),
      // to_char(M_REQUISITIONLINE.M_PRODUCT_ID), ?) AS PRODUCT,
      log.debug(requisitionLine.getProduct().getIdentifier());

      // AD_COLUMN_IDENTIFIER(to_char('M_AttributeSetInstance'),
      // to_char(M_REQUISITIONLINE.M_ATTRIBUTESETINSTANCE_ID), ?) AS ATTRIBUTE,
      if (requisitionLine.getAttributeSetValue() != null) {
        log.debug(requisitionLine.getAttributeSetValue().getIdentifier());
      }

      // AD_COLUMN_IDENTIFIER(to_char('AD_User'), to_char(M_REQUISITION.AD_USER_ID), ?) AS REQUESTER
      log.debug(requisitionLine.getRequisition().getUserContact().getIdentifier());

      log.debug(requisitionLine.getIdentifier());
    }
    log.debug("done");
  }
}