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

package org.openbravo.dal.core;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.tuple.Instantiator;
import org.openbravo.base.model.Entity;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.DynamicOBObject;
import org.openbravo.base.structure.Identifiable;
import org.openbravo.base.util.Check;

/**
 * This class is used by Hibernate. Instantiates a Openbravo business object and sets the
 * {@link Entity} in this new instance. There is one OBInstantiator instance per {@link Entity} in
 * the system.
 * 
 * Its main use is to support dynamic business objects which can handle runtime model changes.
 * 
 * @author mtaal
 */
// TODO: support dynamic subclassing, this is currently not supported, see
// hibernate DynamicMapInstantiator for ideas on how to accomplish this.
public class OBInstantiator implements Instantiator {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(OBInstantiator.class);

  private String entityName;
  private Class<?> mappedClass;

  public OBInstantiator() {
    this.entityName = null;
  }

  public OBInstantiator(PersistentClass mappingInfo) {
    this.entityName = mappingInfo.getEntityName();
    mappedClass = mappingInfo.getMappedClass();
    log.debug("Creating dynamic instantiator for " + entityName);
  }

  /** Instantiate a new instance of the entity. */
  public Object instantiate() {
    return OBProvider.getInstance().get(entityName);
  }

  /**
   * Instantiate an instance and set its id using the parameter. Used by Hibernate when loading
   * existing instances from the database.
   * 
   * @param id
   *          the id to set in the instance
   */
  public Object instantiate(Serializable id) {
    if (mappedClass != null) {
      final Identifiable obObject = (Identifiable) OBProvider.getInstance().get(mappedClass);
      obObject.setId(id);
      Check.isTrue(obObject.getEntityName().equals(entityName),
          "Entityname of instantiated object " + obObject.getEntityName()
              + " and expected entityName: " + entityName + " is different.");
      return obObject;
    } else {
      final DynamicOBObject dob = new DynamicOBObject();
      dob.setEntityName(entityName);
      dob.setId((String) id);
      return dob;
    }
  }

  /**
   * Returns true if the object is an instance of the Entity handled by the OBInstantiator.
   * 
   * @param object
   *          the object to compare with the Entity managed here
   * @return true if the object is an Entity managed by this class
   */
  public boolean isInstance(Object object) {
    if (object instanceof Identifiable) {
      return entityName.equals(((Identifiable) object).getEntityName());
    }
    return false;
  }
}
