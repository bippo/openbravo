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
package org.openbravo.erpCommon.ad_actionButton;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.AttributeUse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.model.manufacturing.processplan.OperationProduct;
import org.openbravo.model.manufacturing.processplan.OperationProductAttribute;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

public class SequenceProductCreate implements Process {

  private static final String lotSearchKey = "LOT";
  private static final String serialNoSearchKey = "SNO";
  private static final String expirationDateSearchKey = "EXD";
  private static final Logger log4j = Logger.getLogger(SequenceProductCreate.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    final String sequenceProductId = (String) bundle.getParams().get("MA_Sequenceproduct_ID");
    final String value = (String) bundle.getParams().get("value");
    final String name = (String) bundle.getParams().get("name");
    final String productionType = (String) bundle.getParams().get("productiontype");
    final String qty = (String) bundle.getParams().get("qty");
    final ConnectionProvider conn = bundle.getConnection();
    final String copyAttribute = (String) bundle.getParams().get("copyattribute");
    final String productCategoryId = (String) bundle.getParams().get("mProductCategoryId");

    try {
      OBContext.setAdminMode(true);

      // Create new product copy of selected
      OperationProduct opProduct = OBDal.getInstance().get(OperationProduct.class,
          sequenceProductId);

      Product originalProduct = opProduct.getProduct();
      Product newProduct = (Product) DalUtil.copy(originalProduct);

      // Modifies values
      newProduct.setSearchKey(value);
      newProduct.setName(name);
      newProduct.setUPCEAN(null);

      // Delete the ProcessPlan for new product
      newProduct.setProcessPlan(null);

      // Empty values copied and filled by m_product_trg
      newProduct.setProductAccountsList(null);
      newProduct.setProductTrlList(null);

      // Delete Purchasing Tab
      newProduct.setApprovedVendorList(null);

      // Product Category
      ProductCategory pcategory = OBDal.getInstance().get(ProductCategory.class, productCategoryId);
      if (pcategory != null)
        newProduct.setProductCategory(pcategory);

      // Save product
      OBDal.getInstance().save(newProduct);

      OBDal.getInstance().flush();

      // Create Operation Product line

      OperationProduct newOpProduct = OBProvider.getInstance().get(OperationProduct.class);

      newOpProduct.setMASequence(opProduct.getMASequence());
      newOpProduct.setClient(opProduct.getClient());
      newOpProduct.setOrganization(opProduct.getOrganization());
      newOpProduct.setLineNo(getLineNum(opProduct.getMASequence().getId()));
      newOpProduct.setProduct(newProduct);
      newOpProduct.setQuantity(new BigDecimal(qty));
      newOpProduct.setUOM(newProduct.getUOM());
      newOpProduct.setProductionType(productionType);

      // Save Operation Product line
      OBDal.getInstance().save(newOpProduct);

      OBDal.getInstance().flush();

      // Copy Attributes

      if (copyAttribute.equals("Y") && newProduct.getAttributeSet() != null
          && productionType.equals("+") && opProduct.getProductionType().equals("-")) {
        // Special Attribute
        if (newProduct.getAttributeSet().isLot())
          copyAtt(newOpProduct, opProduct, true, lotSearchKey, null);
        if (newProduct.getAttributeSet().isSerialNo())
          copyAtt(newOpProduct, opProduct, true, serialNoSearchKey, null);
        if (newProduct.getAttributeSet().isExpirationDate())
          copyAtt(newOpProduct, opProduct, true, expirationDateSearchKey, null);
        // Normal Attribute
        for (AttributeUse attributeuse : newProduct.getAttributeSet().getAttributeUseList()) {
          copyAtt(newOpProduct, opProduct, false, "", attributeuse);
        }
      }

      OBDal.getInstance().flush();

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle(Utility.messageBD(conn, "Success", bundle.getContext().getLanguage()));
      String message = Utility.messageBD(conn, "SequenceProductCreated", bundle.getContext()
          .getLanguage())
          + newProduct.getName() + " " + qty + " P" + productionType;
      if (copyAttribute.equals("Y")
          && (productionType.equals("-") || opProduct.getProductionType().equals("+"))) {
        message = message
            + ". "
            + Utility.messageBD(conn, "SequenceProductAttNotCopied", bundle.getContext()
                .getLanguage());
      }
      msg.setMessage(message);
      bundle.setResult(msg);

    } catch (final Exception e) {

      OBDal.getInstance().rollbackAndClose();
      log4j.error("Error creating copy of product in sequence", e);
      final OBError msg = new OBError();
      msg.setType("Error");
      if (e instanceof org.hibernate.exception.GenericJDBCException) {
        msg.setMessage(((org.hibernate.exception.GenericJDBCException) e).getSQLException()
            .getNextException().getMessage());
      } else if (e instanceof org.hibernate.exception.ConstraintViolationException) {
        msg.setMessage(((org.hibernate.exception.ConstraintViolationException) e).getSQLException()
            .getNextException().getMessage());
      } else {
        msg.setMessage(e.getMessage());
      }
      msg.setTitle(Utility.messageBD(conn, "Error", bundle.getContext().getLanguage()));
      bundle.setResult(msg);

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void copyAtt(OperationProduct newOpProduct, OperationProduct fromOpProduct,
      boolean isSpecial, String specialValue, AttributeUse attributeuse) throws Exception {

    OperationProductAttribute opProductAtt = OBProvider.getInstance().get(
        OperationProductAttribute.class);
    opProductAtt.setSequenceproduct(newOpProduct);
    opProductAtt.setClient(newOpProduct.getClient());
    opProductAtt.setOrganization(newOpProduct.getOrganization());
    opProductAtt.setProductFrom(fromOpProduct);
    opProductAtt.setSpecialatt(isSpecial);
    if (isSpecial) {
      opProductAtt.setSpecialatt(specialValue);
    } else {
      opProductAtt.setAttributeUse(attributeuse);
      opProductAtt.setAttributeuseto(attributeuse);
    }

    OBDal.getInstance().save(opProductAtt);

  }

  private static Long getLineNum(String SequenceId) throws Exception {
    String hql = "  SELECT COALESCE(MAX(l.lineNo),0)+10 AS DefaultValue FROM ManufacturingOperationProduct l WHERE l.mASequence.id= '"
        + SequenceId + "'";
    Query q = OBDal.getInstance().getSession().createQuery(hql);
    try {
      Long result = (Long) q.uniqueResult();
      return result == null ? 0L : result;
    } catch (Exception e) {
      // Unique result throws exception if more than one line is returned.
      return 0L;
    }
  }
}
