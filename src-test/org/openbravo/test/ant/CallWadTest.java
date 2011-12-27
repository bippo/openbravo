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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.ant;

import org.apache.log4j.PropertyConfigurator;
import org.openbravo.base.exception.OBException;
import org.openbravo.wad.Wad;

/**
 * An example test of calling Wad directly with command line arguments.
 * 
 * @author mtaal
 */
public class CallWadTest extends BaseAntTest {

  /**
   * Calls Wad.main(String[]) with a number of commandline arguments.
   */
  public void testWad() {
    PropertyConfigurator.configure(this.getClass().getResource("/log4j.properties"));

    final String[] args = new String[5];
    args[0] = "config"; // ${base.config}'
    args[1] = "%";// '${tab}'
    args[2] = "srcAD/org/openbravo/erpWindows"; // '${build.AD}/org/openbravo/erpWindows'
    args[3] = "srcAD/org/openbravo/erpCommon"; //
    args[4] = "build/javasqlc/src"; // '${build.sqlc}/src'
    // args[5] = '${webTab}'
    // '${build.AD}/org/openbravo/erpCommon/ad_actionButton'
    // '${base.design}' '${base.translate.structure}' '${client.web.xml}'
    // '..' '${attach.path}' '${web.url}' '${base.src}' '${complete}'
    // '${module}'
    try {
      Wad.main(args);
    } catch (final Exception e) {
      throw new OBException(e);
    }

    // doTest("compile");
  }
}