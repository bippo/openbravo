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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.database.SessionInfo;
import org.openbravo.model.ad.system.SystemInformation;

/**
 * Observes changes in SystemInformation to cache value of usage audit check.
 * 
 * @author alostale
 * 
 */
public class UsageAuditHandler extends EntityPersistenceEventObserver {
  private static final String SYSTEM_INFO_TABLE_ID = "1005400005";
  private static final Entity[] entities = { ModelProvider.getInstance().getEntityByTableId(
      SYSTEM_INFO_TABLE_ID) };
  private static final Property usageAuditProperty = entities[0]
      .getProperty(SystemInformation.PROPERTY_ISUSAGEAUDITENABLED);
  private static final Logger log = Logger.getLogger(UsageAuditHandler.class);

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      SessionInfo.setUsageAuditActive((Boolean) event.getCurrentState(usageAuditProperty));
    } catch (Exception e) {
      log.error("Error setting usage audit", e);
    }
  }

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }
}
