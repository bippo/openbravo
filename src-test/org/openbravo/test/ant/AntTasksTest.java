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

package org.openbravo.test.ant;

import org.openbravo.dal.service.OBDal;

/**
 * Several calls to ant tasks present in the build.xml file. Enable the one relevant for the test.
 * 
 * NOTE: this test case class is used to debug ant task calls.
 * 
 * @author mtaal
 */

public class AntTasksTest extends BaseAntTest {

  public void testCompileComplete() {
    doTest("compile.complete");
  }

  public void testUpdateDatabase() {
    doTest("update.database");
  }

  public void testWad() {
    doTest("wad", "src");
  }

  public void testImportReferenceData() {
    doTest("import.reference.data");
    OBDal.getInstance().commitAndClose();
  }

  public void testCreateDatabase() {
    doTest("create.database");
  }

}