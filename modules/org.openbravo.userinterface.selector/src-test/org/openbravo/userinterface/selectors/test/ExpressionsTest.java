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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.userinterface.selectors.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;
import org.openbravo.client.application.OBBindings;
import org.openbravo.dal.core.OBContext;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the current API exposed to JavaScript expression through OBBindings class
 * 
 * @author iperdomo
 */
public class ExpressionsTest extends BaseTest {

  private ScriptEngineManager manager;
  private ScriptEngine engine;
  private Object result = null;
  private Logger log = Logger.getLogger(ExpressionsTest.class);

  private HashMap<String, String> expr = new HashMap<String, String>();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Everything runs as System Admin user
    setSystemAdministratorContext();

    manager = new ScriptEngineManager();
    engine = manager.getEngineByName("js");
    engine.put("OB", new OBBindings(OBContext.getOBContext()));

    // Initialize expressions
    expr.put("Get current user's name", "OB.getContext().getUser().name");

    expr.put("Get current language", "OB.getContext().getLanguage().language");

    expr.put("Format today's date", "OB.formatDate(new Date(), 'yyyy-MM-dd')");

    expr.put("Get current client id", "OB.getContext().getCurrentClient().id");

    expr.put("Parse date with fixed format", "OB.parseDate('1979-04-24','yyyy-MM-dd')");

    expr.put("Format a parsed date",
        "OB.formatDate(OB.parseDate('1979-04-24', 'yyyy-MM-dd'), 'MM-dd-yyyy')");

    expr.put("Filter by vendor/customer", "if(OB.isSalesTransaction()===null){'';}"
        + "else if(OB.isSalesTransaction()==true){'e.customer = true';}"
        + "else{'e.vendor = true';}");

    expr.put("Complex expression from Java",
        "OB.getFilterExpression('org.openbravo.userinterface.selectors.test.SampleFilterExpression');");

  }

  public void testUserName() {
    final String s = expr.get("Get current user's name");
    try {
      result = engine.eval(s);
    } catch (Exception e) {
      log.error("Error evaluating expression: " + s, e);
    }
    assertEquals(result.toString(), OBContext.getOBContext().getUser().getName());
  }

  public void testLanguage() {
    final String s = expr.get("Get current language");
    try {
      result = engine.eval(s);
    } catch (Exception e) {
      log.error("Error evaluating expression: " + s, e);
    }
    assertEquals(result.toString(), OBContext.getOBContext().getLanguage().getLanguage());
  }

  public void testFormatDate() {
    final String s = expr.get("Format today's date");
    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    try {
      result = engine.eval(s);
    } catch (Exception e) {
      log.error("Error evaluating expression: " + s, e);
    }
    assertEquals(df.format(Calendar.getInstance().getTime()), result);
  }

  public void testCurrentClientId() {
    final String s = expr.get("Get current client id");
    try {
      result = engine.eval(s);
    } catch (Exception e) {
      log.error("Error evaluating expression: " + s, e);
    }
    assertEquals(OBContext.getOBContext().getCurrentClient().getId(), result);
  }

  public void testParseDate() {
    final String s = expr.get("Parse date with fixed format");
    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    try {
      result = engine.eval(s);
      assertEquals(df.parse("1979-04-24"), result);
    } catch (Exception e) {
      log.error("Error evaluating expression: " + s, e);
    }
  }

  public void testFormatParsedDate() {
    final String s = expr.get("Format a parsed date");
    try {
      result = engine.eval(s);
      assertEquals("04-24-1979", result);
    } catch (Exception e) {
      log.error("Error evaluating expression: " + s, e);
    }
  }

  public void testCustomerVendorFilter() {
    final String s = expr.get("Filter by vendor/customer");
    try {
      result = engine.eval(s);
    } catch (Exception e) {
      log.error("Error evaluating expression: " + s, e);
    }
    assertEquals("", result);
  }

  public void testGetFilterExpression() {
    final String s = expr.get("Complex expression from Java");
    try {
      result = engine.eval(s);
    } catch (Exception e) {
      log.error("Error evaluating expression: " + s, e);
    }
    assertEquals("This is a complex expression", result);
  }
}
