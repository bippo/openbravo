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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.obps;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.ui.Form;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;

/**
 * This class maintains a list of all elements that are disabled because a module is disabled. It is
 * kept in this way to prevent DB queries for each request.
 * 
 */
public class DisabledModules {
  enum Artifacts {
    MODULE, TAB, PROCESS, FORM, WINDOW
  };

  private static List<String> disabledModules = new ArrayList<String>();
  private static List<String> disabledWindows = new ArrayList<String>();
  private static List<String> disabledTabs = new ArrayList<String>();
  private static List<String> disabledProcesses = new ArrayList<String>();
  private static List<String> disabledForms = new ArrayList<String>();

  private static final Logger log4j = Logger.getLogger(DisabledModules.class);

  /**
   * Reloads information about disabled elements reading from database.
   */
  public static synchronized void reload() {
    log4j.info("Loading disabled modules...");
    disabledModules = new ArrayList<String>();
    disabledWindows = new ArrayList<String>();
    disabledTabs = new ArrayList<String>();
    disabledProcesses = new ArrayList<String>();
    disabledForms = new ArrayList<String>();

    OBContext.setAdminMode();
    try {
      OBCriteria<Module> qMods = OBDal.getInstance().createCriteria(Module.class);
      qMods.add(Restrictions.eq(Module.PROPERTY_ENABLED, false));
      for (Module disabledModule : qMods.list()) {
        disabledModules.add(disabledModule.getId());
        log4j.debug(disabledModule.getName() + " module is disabled");

        OBCriteria<Window> qWindows = OBDal.getInstance().createCriteria(Window.class);
        qWindows.add(Restrictions.eq(Window.PROPERTY_MODULE, disabledModule));
        for (Window window : qWindows.list()) {
          disabledTabs.add(window.getId());
          log4j.debug("Disabled tab: " + window.getIdentifier());
        }

        OBCriteria<Tab> qTabs = OBDal.getInstance().createCriteria(Tab.class);
        qTabs.add(Restrictions.eq(Tab.PROPERTY_MODULE, disabledModule));
        for (Tab tab : qTabs.list()) {
          disabledTabs.add(tab.getId());
          log4j.debug("Disabled tab: " + tab.getIdentifier());
        }

        OBCriteria<Process> qProcess = OBDal.getInstance().createCriteria(Process.class);
        qProcess.add(Restrictions.eq(Process.PROPERTY_MODULE, disabledModule));
        for (Process process : qProcess.list()) {
          disabledProcesses.add(process.getId());
          log4j.debug("Disabled process: " + process.getIdentifier());
        }

        OBCriteria<Form> qForm = OBDal.getInstance().createCriteria(Form.class);
        qForm.add(Restrictions.eq(Form.PROPERTY_MODULE, disabledModule));
        for (Form form : qForm.list()) {
          disabledForms.add(form.getId());
          log4j.debug("Disabled form: " + form.getIdentifier());
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Checks if an artifact is enabled
   * 
   * @param artifactType
   *          Type of artifact
   * @param id
   *          artifact id
   * @return true in case it is enabled
   */
  static boolean isEnabled(Artifacts artifactType, String id) {
    switch (artifactType) {
    case MODULE:
      return !disabledModules.contains(id);
    case FORM:
      return !disabledForms.contains(id);
    case PROCESS:
      return !disabledProcesses.contains(id);
    case TAB:
      return !disabledTabs.contains(id);
    case WINDOW:
      return !disabledWindows.contains(id);
    }
    return true;
  }
}
