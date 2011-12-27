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
package org.openbravo.client.application.test;

import javax.inject.Inject;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.ApplicationComponent;
import org.openbravo.client.kernel.JSCompressor;
import org.openbravo.client.kernel.JSLintChecker;

/**
 * Tests the generating of the menu through a template
 * 
 * @author mtaal
 */
public class MenuTemplateTest extends WeldBaseTest {

  @Inject
  private ApplicationComponent appMenuComponent;

  @Test
  public void testApplication() throws Exception {
    setTestAdminContext();
    final String javascript = appMenuComponent.generate();
    System.err.println(javascript);

    // do jslint check
    JSLintChecker.getInstance().check(appMenuComponent.getId(), javascript);

    // compress
    final String compressed = JSCompressor.getInstance().compress(javascript);

    // should have compressed something
    assertTrue(compressed.length() < javascript.length());
    assertTrue(!compressed.equals(javascript));
    assertTrue(compressed.length() > 0);
    System.err.println(compressed);
  }
}
