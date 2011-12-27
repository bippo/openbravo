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
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.session.SessionFactoryController;

/**
 * This class is responsible for initializing the dal layer. It ensures that the model is read in
 * memory and that the mapping is generated in a two stage process.
 * 
 * @author mtaal
 */

public class DalLayerInitializer implements OBSingleton {
  private static final Logger log = Logger.getLogger(DalLayerInitializer.class);

  private static DalLayerInitializer instance;

  public static DalLayerInitializer getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(DalLayerInitializer.class);
    }
    return instance;
  }

  private boolean initialized = false;

  /**
   * Initializes the in-memory model, registers the entity classes with the {@link OBProvider
   * OBProvider}, initializes the SessionFactory and reads the service config files.
   * 
   * @param rereadConfigFiles
   *          there are cases where it does not make sense to reread the config files with services,
   *          for example after installing a module. The system needs to be restarted for those
   *          cases.
   */
  public void initialize(boolean rereadConfigFiles) {
    if (initialized) {
      return;
    }
    log.info("Initializing in-memory model...");
    ModelProvider.refresh();

    log.debug("Registering entity classes in the OBFactory");
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      if (e.getMappingClass() != null) {
        OBProvider.getInstance().register(e.getMappingClass(), e.getMappingClass(), false);
        OBProvider.getInstance().register(e.getName(), e.getMappingClass(), false);
      }
    }

    log.info("Model read in-memory, generating mapping...");
    SessionFactoryController.setInstance(OBProvider.getInstance().get(
        DalSessionFactoryController.class));
    SessionFactoryController.getInstance().initialize();

    // reset the session
    SessionHandler.deleteSessionHandler();

    // set the configs
    if (rereadConfigFiles) {
      OBConfigFileProvider.getInstance().setConfigInProvider();
    }

    log.info("Dal layer initialized");
    initialized = true;
  }

  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Can be used to set the internal initialized member to false and then call initialize again to
   * re-initialize the Dal layer.
   * 
   * @param initialized
   *          the value of the initialized member
   */
  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

}
