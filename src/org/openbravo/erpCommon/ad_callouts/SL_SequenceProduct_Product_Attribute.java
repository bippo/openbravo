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

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.List;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.common.plm.AttributeUse;
import org.openbravo.model.manufacturing.processplan.OperationProduct;

public class SL_SequenceProduct_Product_Attribute extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final IsIDFilter idFilter = new IsIDFilter();
  private static final String specialAttListId = "FF808181322476640132249E3417002F";
  private static final String lotSearchKey = "LOT";
  private static final String serialNoSearchKey = "SNO";
  private static final String expirationDateSearchKey = "EXD";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    try {
      OBContext.setAdminMode(true);
      String strmSequenceProductId = info
          .getStringParameter("inpmaSequenceproductfromId", idFilter);
      String strmProductSequenceId = info.getStringParameter("inpmaSequenceproductId", idFilter);

      OperationProduct fromOpProduct = OBDal.getInstance().get(OperationProduct.class,
          strmSequenceProductId);
      if (fromOpProduct.getProduct().getAttributeSet() != null) {
        // fill normal attributes
        OperationProduct opProduct = OBDal.getInstance().get(OperationProduct.class,
            strmProductSequenceId);

        OBCriteria<AttributeUse> attributeUseCriteria = OBDal.getInstance().createCriteria(
            AttributeUse.class);
        attributeUseCriteria.add(Restrictions.eq(AttributeUse.PROPERTY_ATTRIBUTESET, fromOpProduct
            .getProduct().getAttributeSet()));
        attributeUseCriteria.addOrderBy(AttributeUse.PROPERTY_SEQUENCENUMBER, true);
        java.util.List<AttributeUse> attUseList = attributeUseCriteria.list();

        info.addSelect("inpmAttributeuseId");
        for (AttributeUse attUse : attUseList) {
          info.addSelectResult(attUse.getId(), attUse.getAttribute().getIdentifier());
        }
        info.endSelect();

        // fill special attributes
        if (opProduct.getProduct().getAttributeSet() != null) {
          info.addSelect("inpspecialatt");
          // lot
          if (fromOpProduct.getProduct().getAttributeSet().isLot()
              && opProduct.getProduct().getAttributeSet().isLot()) {
            org.openbravo.model.ad.domain.List lot = SpecialAttListValue(lotSearchKey);
            if (lot != null)
              info.addSelectResult(lot.getSearchKey(), lot.getName());
          }

          // serial no.
          if (fromOpProduct.getProduct().getAttributeSet().isSerialNo()
              && opProduct.getProduct().getAttributeSet().isSerialNo()) {
            org.openbravo.model.ad.domain.List sn = SpecialAttListValue(serialNoSearchKey);
            if (sn != null)
              info.addSelectResult(sn.getSearchKey(), sn.getName());
          }

          // expirationDate
          if (fromOpProduct.getProduct().getAttributeSet().isExpirationDate()
              && opProduct.getProduct().getAttributeSet().isExpirationDate()) {
            org.openbravo.model.ad.domain.List ed = SpecialAttListValue(expirationDateSearchKey);
            if (ed != null)
              info.addSelectResult(ed.getSearchKey(), ed.getName());
          }
          info.endSelect();
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private org.openbravo.model.ad.domain.List SpecialAttListValue(String Value)
      throws ServletException {
    Reference specialAttList = OBDal.getInstance().get(Reference.class, specialAttListId);
    OBCriteria<List> specialAttListValuesCriteria = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.domain.List.class);
    specialAttListValuesCriteria.add(Restrictions.eq(
        org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE, specialAttList));
    specialAttListValuesCriteria.add(Restrictions.eq(
        org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY, Value));
    java.util.List<org.openbravo.model.ad.domain.List> specialAttListValues = (java.util.List<List>) specialAttListValuesCriteria
        .list();
    if (specialAttListValues.isEmpty()) {
      return null;
    } else {
      return specialAttListValues.get(0);
    }
  }
}
