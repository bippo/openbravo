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
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.DalInitializingTask;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.ui.Window;

public class WindowsInClassicModeTask extends DalInitializingTask {
  private static final Logger log = Logger.getLogger(WindowsInClassicModeTask.class);

  @Override
  protected void doExecute() {
    OBQuery<Module> modules = OBDal.getInstance().createQuery(Module.class, "");
    for (Module module : modules.list()) {
      List<String> classicWindowMessages = new ArrayList<String>();
      OBCriteria<Window> windowsOfModule = OBDao.getFilteredCriteria(Window.class,
          Restrictions.eq(Window.PROPERTY_MODULE, module));
      for (Window window : windowsOfModule.list()) {
        ApplicationUtils.showWindowInClassicMode(window, classicWindowMessages);
      }
      if (classicWindowMessages.size() > 0) {
        log.info("Module: " + module.getName());
        log.info("The following windows will be shown in classic mode:");
        for (String message : classicWindowMessages) {
          log.info("  " + message);
        }
      }
    }
    log.info("The rest of the windows will be shown in new mode.");
  }
}
