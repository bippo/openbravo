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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.db;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;

/**
 * This class implements transaction and user context handling for backend process tasks which need
 * to make use of the DAL. It assumes to be run inside of a web container which has an initialized
 * dal layer.
 * 
 * A subclass needs to implement the doExecute method (or override the execute method).
 * 
 * @author mtaal
 */
public abstract class DalBaseProcess implements Process {
  private static final Logger log = Logger.getLogger(DalBaseProcess.class);

  /**
   * Is called by the process scheduler. The execute method sets the usercontext and does
   * transaction handling.
   * 
   * @param bundle
   *          provides the current user and other context information.
   */
  public void execute(ProcessBundle bundle) throws Exception {
    final ProcessContext processContext = bundle.getContext();

    boolean errorOccured = true;
    final OBContext currentOBContext = OBContext.getOBContext();
    try {
      String userId = processContext.getUser();
      String roleId = processContext.getRole();
      String clientId = processContext.getClient();
      String orgId = processContext.getOrganization();
      String lang = processContext.getLanguage();

      log.debug("Setting user context to user=" + userId + ",roleId=" + roleId + ",client="
          + clientId + ",org=" + orgId + ",lang=" + lang);

      OBContext.setOBContext(userId, roleId, clientId, orgId, lang);
      doExecute(bundle);
      errorOccured = false;
    } finally {
      if (errorOccured) {
        OBDal.getInstance().rollbackAndClose();
      } else {
        OBDal.getInstance().commitAndClose();
      }

      // remove the context at the end, maybe the process scheduler
      // reuses the thread?
      OBContext.setOBContext(currentOBContext);
    }
  }

  protected abstract void doExecute(ProcessBundle bundle) throws Exception;
}
