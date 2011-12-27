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
 * Contributor(s):
 *   Martin Taal <martin.taal@openbravo.com>,
 ************************************************************************
 */

package org.openbravo.test.dal;

import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.test.base.BaseTest;

/**
 * Test the filters added to the hibernate mapping.
 * 
 * @author mtaal
 */

public class DalFilterTest extends BaseTest {
  public void testActiveFilterDisabled() {
    doTest(true);
  }

  public void testActiveFilterEnabled() {
    OBDal.getInstance().commitAndClose();
    OBDal.getInstance().enableActiveFilter();
    doTest(false);
  }

  public void testActiveFilterEnDisabled() {
    OBDal.getInstance().commitAndClose();
    OBDal.getInstance().enableActiveFilter();
    OBDal.getInstance().disableActiveFilter();
    doTest(true);
  }

  /**
   * test active filter both querying as well as collection
   */
  private void doTest(boolean present) {
    setSystemAdministratorContext();
    final Table table = OBDal.getInstance().get(Table.class, "111");
    boolean inActiveColPresent = false;
    for (Column col : table.getADColumnList()) {
      if (!col.isActive()) {
        inActiveColPresent = true;
        break;
      }
    }
    assertTrue(inActiveColPresent == present);
    final OBQuery<Column> columns = OBDal.getInstance().createQuery(Column.class, " table =:table");
    columns.setNamedParameter("table", table);
    columns.setFilterOnActive(false);
    assertEquals(table.getADColumnList().size(), columns.list().size());
  }

}
