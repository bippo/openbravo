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

package org.openbravo.test.model;

import java.io.FileWriter;
import java.util.List;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.EntityAccessChecker;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.test.base.BaseTest;

/**
 * Not to be used in practice, contains some utility methods.
 * 
 * @author mtaal
 */

public class UtilsTest extends BaseTest {

  // don't initialize dal layer for model tests
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void _testDumpModelToFile() throws Exception {
    final StringBuilder sb = new StringBuilder();
    for (Entity e : ModelProvider.getInstance().getModel()) {
      for (Property p : e.getProperties()) {
        if (p.getColumnName() != null && p.getTargetEntity() != null) {
          sb.append("\n" + e.getName() + "." + p.getColumnName() + " = "
              + p.getTargetEntity().getName());
        }
      }
    }
    final FileWriter fw = new FileWriter("/home/mtaal/mytmp/fk_referenced_tables.properties");
    fw.write(sb.toString());
    fw.close();
  }

  public void testDumpAuthorisations() {
    setSystemAdministratorContext();
    final OBCriteria<User> obc = OBDal.getInstance().createCriteria(User.class);
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    final List<User> l = obc.list();
    for (User u : l) {
      try {
        OBContext.setOBContext(u.getId());
      } catch (OBSecurityException e) {
        // handle some non-configured unimportant users
        continue;
      }
      final OBContext obContext = OBContext.getOBContext();
      EntityAccessChecker eac = obContext.getEntityAccessChecker();
      System.err.println("++++++++++++ user " + u.getId() + " ++++++++++++++");
      eac.dump();
    }
  }

  public void testWriteAll() throws Exception {
    // final StringWriter sw = new StringWriter();
    final FileWriter writer = new FileWriter("/home/mtaal/mytmp/test.csv");

    final String SEPARATOR = "||";

    // as we read all entities, be an administrator to prevent
    // security exceptions
    OBContext.setAdminMode();

    // iterate over all entities
    for (Entity entity : ModelProvider.getInstance().getModel()) {

      // query for all objects of the entity and iterate over them
      final List<BaseOBObject> businessObjects = OBDal.getInstance()
          .createCriteria(entity.getName()).list();
      for (BaseOBObject businessObject : businessObjects) {

        final StringBuilder line = new StringBuilder();

        // place the entity name so for each line it is known what type is exported there
        line.append(entity.getName());

        // and iterate over all the properties of the entity
        for (Property property : entity.getProperties()) {
          // ignore these type of properties, as the children are exported separately
          if (property.isOneToMany()) {
            continue;
          }

          line.append(SEPARATOR);

          // get the current value
          final Object value = businessObject.get(property.getName());
          // handle null
          if (value == null) {
            continue;
          }
          // export primitives in the same way as xml primitives
          if (property.isPrimitive()) {
            line.append(((PrimitiveDomainType) property.getDomainType()).convertToString(value));
          } else {
            // export the id of a referenced business object
            line.append(((BaseOBObject) value).getId());
          }
        }
        writer.append(line + "\n");
      }
    }
    writer.close();
  }
}
