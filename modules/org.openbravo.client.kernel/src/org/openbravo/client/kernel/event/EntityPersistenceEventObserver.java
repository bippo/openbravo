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

import org.openbravo.base.model.Entity;

/**
 * A base listener which can be extended to perform specific actions when persisting entities.
 * 
 * @author mtaal
 */
public abstract class EntityPersistenceEventObserver {

  /**
   * Must be implemented by subclass to signal which {@link Entity} types are observed.
   * 
   * @return the array of entity instances to observe.
   * @see #isValidEvent(EntityPersistenceEvent)
   */
  protected abstract Entity[] getObservedEntities();

  /**
   * Convenience method which can be used by subclass to check if a certain event is indeed targeted
   * for this observer.
   * 
   * @param event
   *          the persistence event which is being handled by this observer.
   * @return true if the event applies to one of the observed entities.
   * @see #getObservedEntities()
   */
  protected boolean isValidEvent(EntityPersistenceEvent event) {
    final Entity targetEntity = event.getTargetInstance().getEntity();
    for (Entity entity : getObservedEntities()) {
      if (entity == targetEntity) {
        return true;
      }
    }
    return false;
  }
}
