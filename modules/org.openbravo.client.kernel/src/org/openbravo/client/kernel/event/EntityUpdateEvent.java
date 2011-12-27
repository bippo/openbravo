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

import org.openbravo.base.model.Property;

/**
 * The event object send out when an entity gets updated.
 * 
 * To receive this event, create a class with a method which has this signature:
 * 
 * public void onEvent(@Observes EntityUpdateEvent event) {
 * 
 * Note, the method name is unimportant, the @Observes EntityUpdateEvent specifies that this method
 * will be called before persisting a new instance.
 * 
 * @author mtaal
 */
public class EntityUpdateEvent extends EntityPersistenceEvent {
  private Object[] previousState;

  /**
   * Returns the state of a property which was there when reading from the database.
   * 
   * @param property
   *          the property to get the old state
   * @return the old/previous state/value of the property
   */
  public Object getPreviousState(Property property) {
    int index = 0;
    for (String propName : getPropertyNames()) {
      if (propName.equals(property.getName())) {
        return previousState[index];
      }
      index++;
    }
    throw new IllegalArgumentException("Property " + property + " not found for entity "
        + getTargetInstance());
  }

  /**
   * Get the complete array of previous states. To see what value corresponds to what property use
   * the {@link #getPropertyNames()} array.
   */
  public Object[] getPreviousState() {
    return previousState;
  }

  void setPreviousState(Object[] previousState) {
    this.previousState = previousState;
  }

}
