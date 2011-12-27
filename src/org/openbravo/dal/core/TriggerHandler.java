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

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.SessionStatus;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Supports disabling and again enabling of database triggers.
 * 
 * The user of this class should call disable() after beginning the transaction and enable at the
 * end, before committing.
 * 
 * @author martintaal
 */

public class TriggerHandler {
  private static final Logger log = Logger.getLogger(TriggerHandler.class);

  private static TriggerHandler instance;

  public static TriggerHandler getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(TriggerHandler.class);
    }
    return instance;
  }

  private ThreadLocal<SessionStatus> sessionStatus = new ThreadLocal<SessionStatus>();

  /**
   * Disabled all triggers in the database. This is done by creating an ADSessionStatus object and
   * storing it in the AD_SESSION_STATUS table. Note: this method will also call
   * {@link OBDal#flush() OBDal.flush()}.
   */
  public void disable() {
    log.debug("Disabling triggers");
    Check.isNull(sessionStatus.get(), "There is already a ADSessionStatus present in this thread, "
        + "call enable before calling disable again");
    OBContext.setAdminMode();
    try {
      final SessionStatus localSessionStatus = OBProvider.getInstance().get(SessionStatus.class);
      localSessionStatus.setImporting(true);
      localSessionStatus.setClient(OBDal.getInstance().get(Client.class, "0"));
      localSessionStatus.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
      OBDal.getInstance().save(localSessionStatus);
      OBDal.getInstance().flush();
      Check.isNotNull(localSessionStatus.getId(), "The id is not set after insert");
      sessionStatus.set(localSessionStatus);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * @return true if the database triggers are disabled, false in other cases.
   */
  public boolean isDisabled() {
    return sessionStatus.get() != null;
  }

  /**
   * Clears the SessionStatus from the threadlocal, must be done in case of rollback
   */
  public void clear() {
    sessionStatus.set(null);
  }

  /**
   * Enables triggers in the database. It does this by removing the ADSessionStatus from the
   * database.
   */
  public void enable() {
    log.debug("Enabling triggers");
    Check.isNotNull(sessionStatus.get(), "SessionStatus not set, call disable "
        + "before calling this method");
    OBContext.setAdminMode();
    try {
      OBDal.getInstance().remove(sessionStatus.get());
      OBDal.getInstance().flush();
    } finally {
      sessionStatus.set(null);
      OBContext.restorePreviousMode();
    }
  }
}