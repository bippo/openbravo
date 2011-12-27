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

package org.openbravo.test.model;

import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.UniqueConstraint;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.geography.Country;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the api which allows to query for objects which would match a certain object based on the
 * unique constraints defined: {@link OBDal#findUniqueConstrainedObjects(BaseOBObject)}.
 * 
 * @author mtaal
 */

public class UniqueConstraintTest extends BaseTest {

  private static final Logger log = Logger.getLogger(UniqueConstraintTest.class);

  /**
   * Check that the unique constraints are loaded ([@link {@link Entity#getUniqueConstraints()}).
   */
  public void testUniqueConstraintLoad() {
    final Entity entity = ModelProvider.getInstance().getEntityByTableName("C_Country_Trl");
    assertEquals(1, entity.getUniqueConstraints().size());
    dumpUniqueConstraints();
  }

  /**
   * Tests the {@link OBDal#findUniqueConstrainedObjects(BaseOBObject)} method.
   */
  public void testUniqueConstraintQuerying() {
    setUserContext(getRandomUser().getId());
    addReadWriteAccess(Country.class);
    final List<Country> countries = OBDal.getInstance().createCriteria(Country.class).list();
    assertTrue(countries.size() > 0);
    for (final Country c : countries) {
      // make copy to not interfere with hibernate's auto update mechanism
      final Country copy = (Country) DalUtil.copy(c);
      copy.setId("test");
      final List<BaseOBObject> queried = OBDal.getInstance().findUniqueConstrainedObjects(copy);
      assertEquals(1, queried.size());
      assertEquals(c.getId(), queried.get(0).getId());
    }
  }

  // dump uniqueconstraints
  private void dumpUniqueConstraints() {
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      if (e.getUniqueConstraints().size() > 0) {
        for (final UniqueConstraint uc : e.getUniqueConstraints()) {
          log.debug(">>> Entity " + e);
          log.debug("UniqueConstraint " + uc.getName());
          for (final Property p : uc.getProperties()) {
            log.debug(p.getName() + " ");
          }
        }
        log.debug("");
      }
    }
  }

}