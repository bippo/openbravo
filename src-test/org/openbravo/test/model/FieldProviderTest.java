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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.model;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.OBObjectFieldProvider;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the {@link OBObjectFieldProvider}.
 * 
 * @author mtaal
 */

public class FieldProviderTest extends BaseTest {

  /**
   * Read all data from the database and create field providers then iterate over all columns and
   * get the value.
   */
  public void testAll() {
    super.setTestAdminContext();
    for (Entity e : ModelProvider.getInstance().getModel()) {
      final OBCriteria<BaseOBObject> criteria = OBDal.getInstance().createCriteria(e.getName());
      if (criteria.list().size() > 100) {
        // make the test a bit smaller...
        continue;
      }
      for (BaseOBObject bob : criteria.list()) {
        final FieldProvider fp = OBObjectFieldProvider.createOBObjectFieldProvider(bob);
        for (Property p : e.getProperties()) {
          if (p.getColumnName() == null) {
            continue;
          }
          final String convertedName = p.getColumnName().replaceAll("_", "");
          final String strValue = fp.getField(convertedName);
          final Object value = bob.get(p.getName());
          if (value instanceof BaseOBObject) {
            assertTrue(((BaseOBObject) value).getIdentifier().equals(strValue));
          } else if (value instanceof String) {
            assertTrue(value.equals(strValue));
          } else {
            assertTrue((value != null && strValue != null)
                || (value == null && strValue.equals("")));
          }
        }
      }
    }
  }

}