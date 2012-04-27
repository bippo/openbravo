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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.test;

import org.junit.Test;
import org.openbravo.client.application.DynamicExpressionParser;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.test.base.BaseTest;

public class DynamicExpressionParserTest extends BaseTest {

  @Test
  public void testRegularExpression() {
    setSystemAdministratorContext();
    String displayLogic = "((@Financial_Invoice_Line@='N'))";
    String expectedResult = "((OB.Utilities.getValue(currentValues,'financialInvoiceLine') === false))";
    Tab tab = OBDal.getInstance().get(Tab.class, "270");

    DynamicExpressionParser parser = new DynamicExpressionParser(displayLogic, tab);
    assertTrue(expectedResult.equals(parser.getJSExpression()));

    displayLogic = "@Financial_Invoice_Line@='Y'";
    expectedResult = "OB.Utilities.getValue(currentValues,'financialInvoiceLine') === true";

    parser = new DynamicExpressionParser(displayLogic, tab);
    assertTrue(expectedResult.equals(parser.getJSExpression()));

  }

}
