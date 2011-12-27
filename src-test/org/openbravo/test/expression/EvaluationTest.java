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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.expression;

import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;
import org.openbravo.base.expression.Evaluator;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.test.base.BaseTest;

/**
 * Test the expression processor used in datasets: {@link Evaluator}.
 * 
 * @author mtaal
 */

public class EvaluationTest extends BaseTest {
  private static final Logger log = Logger.getLogger(EvaluationTest.class);

  /**
   * Tests the evaluation of a simple java script expression executed on a set of objects.
   */
  public void testEvaluation() {
    setSystemAdministratorContext();

    // as a test print scripting language names
    final ScriptEngineManager manager = new ScriptEngineManager();
    for (final ScriptEngineFactory sef : manager.getEngineFactories()) {
      log.debug(sef.getEngineName());
    }

    final List<Table> tables = OBDal.getInstance().createCriteria(Table.class).list();
    boolean found = false;
    for (final Table t : tables) {
      final String script = Table.PROPERTY_CLIENT + "." + Client.PROPERTY_ID + " == '0' && "
          + Table.PROPERTY_DBTABLENAME + "== 'AD_Client' && " + Table.PROPERTY_DATAACCESSLEVEL
          + " > 5";
      final Boolean result = Evaluator.getInstance().evaluateBoolean(t, script);
      log.debug(t.getName() + " : " + result);
      found = found || result;
      if (found) {
        break;
      }
    }
    assertTrue(found);
  }

  /**
   * Tests https://issues.openbravo.com/view.php?id=12575 It is neccessary to create a testcase for
   * Transient Condition
   */
  public void testOrderEvaluation() {
    setTestUserContext();
    addReadWriteAccess(Order.class);
    addReadWriteAccess(OrderLine.class);

    final String script = "var orderLines = orderLineList.toArray();"
        + "function compute() {for (var i = 0; i < orderLines.length; i++) {return orderLines[i].getLineNo() > 0;}; return false;};"
        + "compute();";

    final List<Order> orders = OBDal.getInstance().createCriteria(Order.class).list();
    int cnt = 0;
    for (final Order o : orders) {
      if ((cnt % 2) == 0) {
        o.getOrderLineList().clear();
      }
      final Boolean result = Evaluator.getInstance().evaluateBoolean(o, script);
      Boolean check = false;
      for (OrderLine ol : o.getOrderLineList()) {
        check |= ol.getLineNo() > 0;
      }
      assertEquals(result, check);
      cnt++;
    }
    assertTrue(cnt > 0);
    // prevent orders really to be updated
    OBDal.getInstance().rollbackAndClose();
  }
}