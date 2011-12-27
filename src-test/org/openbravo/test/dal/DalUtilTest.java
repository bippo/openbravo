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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.dal;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the {@link DalUtil} class.
 * 
 * @author mtaal
 * @author iperdomo
 */

public class DalUtilTest extends BaseTest {

  /**
   * Tests {@link DalUtil#getPropertyFromPath(org.openbravo.base.model.Entity, String)}.
   */
  public void testGetProperty() {
    setSystemAdministratorContext();
    final Entity bpEntity = ModelProvider.getInstance().getEntity(BusinessPartner.ENTITY_NAME);
    final Property property = DalUtil.getPropertyFromPath(bpEntity, "bankAccount.bank.name");
    assertTrue(property != null);
    assertEquals("Bank", property.getEntity().getName());
    assertEquals("name", property.getName());
  }

  /**
   * Tests {@link DalUtil#getValueFromPath(org.openbravo.base.structure.BaseOBObject, String)}.
   */
  public void testGetValue() {
    setSystemAdministratorContext();
    final Organization org = OBDal.getInstance().get(Organization.class, "0");
    final Object value = DalUtil.getValueFromPath(org, "client.organization.name");
    assertEquals(org.getName(), value);
  }
}