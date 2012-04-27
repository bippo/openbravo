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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.Product;

public class SL_Inventory_Product extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Locator

    String strLocator = info.vars.getStringParameter("inpmProductId_LOC");

    if (strLocator.startsWith("\"")) {
      strLocator = strLocator.substring(1, strLocator.length() - 1);
    }
    if (!strLocator.equals("")) {
      info.addResult("inpmLocatorId", strLocator);
      info.addResult("inpmLocatorId_R",
          SLInOutLineProductData.locator(this, strLocator, info.vars.getLanguage()));
    }

    // UOM
    String strUOM = info.vars.getStringParameter("inpmProductId_UOM");
    info.addResult("inpcUomId", strUOM);

    // Attributes

    String strAttribute = info.vars.getStringParameter("inpmProductId_ATR");

    if (strAttribute.startsWith("\"")) {
      strAttribute = strAttribute.substring(1, strAttribute.length() - 1);
    }
    info.addResult("inpmAttributesetinstanceId", strAttribute);
    info.addResult("inpmAttributesetinstanceId_R",
        SLInOutLineProductData.attribute(this, strAttribute));

    // Attribute set

    String strMProductID = info.vars.getStringParameter("inpmProductId");
    String strAttrSet = "";
    String strAttrSetValueType = "";

    OBContext.setAdminMode();
    try {
      final Product product = OBDal.getInstance().get(Product.class, strMProductID);
      if (product != null) {
        AttributeSet attributeset = product.getAttributeSet();
        if (attributeset != null) {
          strAttrSet = product.getAttributeSet().toString();
        }
        strAttrSetValueType = product.getUseAttributeSetValueAs();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    info.addResult("inpattributeset", strAttrSet);
    info.addResult("inpattrsetvaluetype", strAttrSetValueType);

    // Secondary UOM

    String strHasSecondaryUOM = SLOrderProductData.hasSecondaryUOM(this, strMProductID);
    info.addResult("inphasseconduom", (Object) strHasSecondaryUOM);

    // Quantity order

    String strQtyOrder = info.vars.getNumericParameter("inpmProductId_PQTY");

    info.addResult("inpquantityorder", StringUtils.isEmpty(strQtyOrder) ? "\"\""
        : (Object) strQtyOrder);
    info.addResult("inpquantityorderbook", StringUtils.isEmpty(strQtyOrder) ? "\"\""
        : (Object) strQtyOrder);

    // Quantity

    String strQty = info.vars.getNumericParameter("inpmProductId_QTY");

    info.addResult("inpqtycount", StringUtils.isEmpty(strQty) ? "\"\"" : (Object) strQty);
    info.addResult("inpqtybook", StringUtils.isEmpty(strQty) ? "\"\"" : (Object) strQty);

    // UOM

    String strPUOM = info.vars.getStringParameter("inpmProductId_PUOM");

    if (strPUOM.startsWith("\"")) {
      strPUOM = strPUOM.substring(1, strPUOM.length() - 1);
    }

    FieldProvider[] tld = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLE", "",
          "M_Product_UOM", "", Utility.getContext(this, info.vars, "#AccessibleOrgTree",
              "SLInventoryProduct"), Utility.getContext(this, info.vars, "#User_Client",
              "SLInventoryProduct"), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SLInventoryProduct", "");
      tld = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (tld != null && tld.length > 0) {
      info.addSelect("inpmProductUomId");
      for (int i = 0; i < tld.length; i++) {
        info.addSelectResult(tld[i].getField("id"), tld[i].getField("name"), tld[i].getField("id")
            .equalsIgnoreCase(strPUOM));
      }
      info.endSelect();
    } else {
      info.addResult("inpmProductUomId", null);
    }

    // displaylogic

    info.addResult("EXECUTE", "displayLogic();");

  }
}
