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

package org.openbravo.client.application.event;

import javax.enterprise.event.Observes;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.model.ad.module.Module;

/**
 * This class observes modifications/insertions in ADModule in order to invalidate modules cache
 * used for obtaining views ETags.
 * 
 * @author alostale
 * 
 */
public class ModuleHandler extends EntityPersistenceEventObserver {
  private static final String MODULE_TABLE_ID = "9D36D488605044F5A0264D7C8B916657";
  private static Entity[] entities = { ModelProvider.getInstance().getEntityByTableId(
      MODULE_TABLE_ID) };

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    if (!event.getPreviousState(getVersionProperty()).equals(
        event.getCurrentState(getVersionProperty()))
        || !event.getPreviousState(getEnabledProperty()).equals(
            event.getCurrentState(getEnabledProperty()))
        || !event.getPreviousState(getInDevelopmentProperty()).equals(
            event.getCurrentState(getInDevelopmentProperty()))) {
      BaseComponent.nullifyModuleCache();
    }
  }

  public void onNew(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    BaseComponent.nullifyModuleCache();
  }

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Property getInDevelopmentProperty() {
    return entities[0].getProperty(Module.PROPERTY_INDEVELOPMENT);
  }

  private Property getVersionProperty() {
    return entities[0].getProperty(Module.PROPERTY_VERSION);
  }

  private Property getEnabledProperty() {
    return entities[0].getProperty(Module.PROPERTY_ENABLED);
  }
}
