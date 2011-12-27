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

package org.openbravo.base.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.util.Check;

/**
 * A UniqueConstraint defines for an entity a set of properties, which combined are unique for that
 * entity.
 * 
 * @author mtaal
 */

public class UniqueConstraint {
  private static final Logger log = Logger.getLogger(UniqueConstraint.class);

  private List<Property> properties = new ArrayList<Property>();
  private String name;
  private Entity entity;
  private boolean invalid = false;

  protected void addPropertyForColumn(String columnName) {
    if (isInvalid()) {
      return;
    }
    for (final Property property : entity.getProperties()) {
      // one-to-many properties have a null columnname
      if (property.getColumnName() != null && property.getColumnName().equalsIgnoreCase(columnName)) {
        Check.isFalse(properties.contains(property), "Column " + columnName
            + " occurs twice in uniqueconstraint " + name + " in entity " + entity + " table "
            + entity.getTableName());
        properties.add(property);
        log.debug("Adding property " + property + " to uniqueconstraint " + name);
        return;
      }
    }

    setInvalid(true);
    log.error("Fail when setting uniqueconstraint " + getName() + " columnname " + columnName
        + " not present in entity " + entity + " table " + entity.getTableName() + ". Ignoring "
        + "this unique constraint");
    entity.getUniqueConstraints().remove(this);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    log.debug("Created unique constraint " + name);
    this.name = name;
  }

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public boolean isInvalid() {
    return invalid;
  }

  public void setInvalid(boolean invalid) {
    this.invalid = invalid;
  }
}
