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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.reference.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;

public class UIGeneralQuantity extends UIReference {
  public UIGeneralQuantity(String reference, String subreference) {
    super(reference, subreference);
  }

  public String formatGridValue(VariablesSecureApp vars, String value) {
    String rt = value;
    try {
      DecimalFormat numberFormat = Utility.getFormat(vars, "generalQtyRelation");
      if (numberFormat != null) {
        rt = numberFormat.format(new BigDecimal(value));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return rt;
  }
}
