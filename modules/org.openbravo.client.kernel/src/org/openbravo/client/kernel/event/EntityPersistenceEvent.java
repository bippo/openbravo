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
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.event;

import org.hibernate.type.Type;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;

/**
 * The base event object send out to reflect a persistence event on an entity (save, update, delete,
 * etc.).
 * 
 * Note: it is possible to directly access/retrieve the object which is being deleted or persisted.
 * To update the object's state one should NOT call setters directly on that object. Instead use the
 * {@link #setCurrentState(Property, Object)} method.
 * 
 * @author mtaal
 */
public class EntityPersistenceEvent {
  private BaseOBObject targetInstance;

  private String id;
  private Object[] currentState;
  private String[] propertyNames;
  private Type[] types;
  private boolean stateUpdated = false;

  /**
   * Get the current value (which will be persisted) for a certain property.
   */
  public Object getCurrentState(Property property) {
    int index = 0;
    for (String propName : propertyNames) {
      if (propName.equals(property.getName())) {
        return currentState[index];
      }
      index++;
    }
    throw new IllegalArgumentException("Property " + property + " not found for entity "
        + getTargetInstance());
  }

  /**
   * The current values of the properties of the entity, these values are persisted. Use the
   * {@link EntityPersistenceEvent#getPropertyNames()} to see which properties are located where in
   * this array.
   * 
   * @see #getPropertyNames()
   */
  public Object[] getCurrentState() {
    return currentState;
  }

  void setCurrentState(Object[] currentState) {
    this.currentState = currentState;
  }

  /**
   * The names of the properties of the entity which are persisted. The array corresponds to the
   * types and state arrays.
   * 
   * @see #getTypes()
   * @see #getCurrentState()
   */
  public String[] getPropertyNames() {
    return propertyNames;
  }

  void setPropertyNames(String[] propertyNames) {
    this.propertyNames = propertyNames;
  }

  /**
   * @return the target instance of the event, note that depending on the event it it not correct to
   *         directly update the instance, use the api's offered by the specific subclass of the
   *         EntityPersistenceEvent class.
   */
  public BaseOBObject getTargetInstance() {
    return targetInstance;
  }

  void setTargetInstance(BaseOBObject targetInstance) {
    this.targetInstance = targetInstance;
  }

  /**
   * Change the value/state of a property. The change will also be passed on to the entity and to
   * the database (in case of update and save events).
   * 
   * @param property
   *          the property to change
   * @param value
   *          its new value
   */
  public void setCurrentState(Property property, Object value) {
    int index = 0;
    for (String propName : propertyNames) {
      if (propName.equals(property.getName())) {
        currentState[index] = value;
        stateUpdated = true;
        return;
      }
      index++;
    }
    throw new IllegalArgumentException("Property " + property + " not found for entity "
        + getTargetInstance());
  }

  /**
   * @return true if the state of the entity was updated/changed, false otherwise.
   */
  public boolean isStateUpdated() {
    return stateUpdated;
  }

  /**
   * The id of the entity being persisted, can be null for new entities.
   */
  public String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }

  /**
   * The type definition of the properties of the entity to be persisted.
   * 
   * @see #getPropertyNames()
   */
  public Type[] getTypes() {
    return types;
  }

  void setTypes(Type[] types) {
    this.types = types;
  }

}
