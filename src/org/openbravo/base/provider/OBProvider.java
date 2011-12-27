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

package org.openbravo.base.provider;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The OBProvider provides the runtime instances of model entities as well as service instances.
 * Classes are registered by their class type and it is identified if the class should be considered
 * to be a singleton or not.
 * <p/>
 * The OBProvider is an implementation of the servicelocator pattern discussed in Martin Fowler's
 * article here: http://martinfowler.com/articles/injection.html
 * 
 * @author mtaal
 */

public class OBProvider {
  private static final Logger log = Logger.getLogger(OBProvider.class);

  public static final String CONFIG_FILE_NAME = "provider-config.xml";

  private static OBProvider instance = new OBProvider();

  public static OBProvider getInstance() {
    return instance;
  }

  public static void setInstance(OBProvider instance) {
    OBProvider.instance = instance;
  }

  private Map<String, Registration> registrations = new Hashtable<String, Registration>();

  /**
   * Returns true if the clz is registered.
   * 
   * @param clz
   *          the name of this class is used to check if it is already registered
   * @return true if the clz is registered
   */
  public boolean isRegistered(Class<?> clz) {
    return isRegistered(clz.getName());
  }

  /**
   * Checks if a service is registered under the name passed as a parameter.
   * 
   * @param name
   *          is used to search the registry
   * @return true if a registration exists
   */
  public boolean isRegistered(String name) {
    return registrations.get(name) != null;
  }

  // Used by OBConfigFileProvider
  protected void register(String prefix, InputStream is) {
    final OBProviderConfigReader reader = new OBProviderConfigReader();
    reader.read(prefix, is);
  }

  // Used by OBConfigFileProvider
  protected void register(String prefix, String configFile) {
    final OBProviderConfigReader reader = new OBProviderConfigReader();
    reader.read(prefix, configFile);
  }

  /**
   * Register an actual instance for an internal Openbravo class (the registrationClass).
   * 
   * @param registrationClass
   *          the original Openbravo class
   * @param instanceObj
   *          the instance to use when the class is requested.
   * @param overwrite
   *          true overwrite a current registration, false a current registration is not overwritten
   */
  public void registerInstance(Class<?> registrationClass, Object instanceObj, boolean overwrite) {
    register(registrationClass.getName(), instanceObj.getClass(), overwrite);
    final Registration reg = registrations.get(registrationClass.getName());
    reg.setInstance(instanceObj);
  }

  /**
   * Register an instance for an internal Openbravo class (the registrationClass).
   * 
   * @param registrationClass
   *          the original Openbravo class
   * @param instanceClass
   *          the implementation class
   * @param overwrite
   *          true overwrite a current registration, false a current registration is not overwritten
   */
  public void register(Class<?> registrationClass, Class<?> instanceClass, boolean overwrite) {
    register(registrationClass.getName(), instanceClass, overwrite);
  }

  /**
   * Register an instance for an internal Openbravo class or service (the name).
   * 
   * @param name
   *          the name of the Openbravo class or service
   * @param instanceClass
   *          the implementation class
   * @param overwrite
   *          true overwrite a current registration, false a current registration is not overwritten
   */
  public void register(String name, Class<?> instanceClass, boolean overwrite) {
    final Registration reg = new Registration();
    reg.setSingleton(OBSingleton.class.isAssignableFrom(instanceClass));
    reg.setInstanceClass(instanceClass);
    reg.setName(name);
    // a registration which overwrites others is not overwritable
    reg.setOverwritable(!overwrite);
    final Registration currentReg = registrations.get(name);
    if (currentReg != null) {
      if (!overwrite || !currentReg.isOverwritable()) {
        log.debug("A different registration: " + currentReg
            + " already exists under this name, NOT overwriting it by " + reg);
        return;
      } else {
        log.debug(currentReg + " will be replaced by " + reg);
      }
    } else {
      log.debug("Registering " + reg);
    }
    registrations.put(name, reg);
  }

  /**
   * Checks the registry for which class should be used for the passed clz. If no registration is
   * found a new registration is created using the passed clz.
   * 
   * @param clz
   *          the class for which an instance is requested
   * @return an instance of the clz
   */
  @SuppressWarnings("unchecked")
  public <T extends Object> T get(Class<T> clz) {
    Registration reg = registrations.get(clz.getName());
    if (reg == null) {
      // register it
      log.debug("Registration for class " + clz.getName()
          + " not found, creating a registration automatically");
      register(clz, clz, false);

      reg = registrations.get(clz.getName());
      return (T) reg.getInstance();
    }
    return (T) reg.getInstance();
  }

  /**
   * Removes the singleton instance of the clz (if any) from the internal registry. It will be
   * recreated at next request.
   * 
   * @param clz
   *          the instance of this class is removed.
   */
  public void removeInstance(Class<?> clz) {
    log.debug("Removing instance " + clz.getName());
    final Registration reg = registrations.get(clz.getName());
    if (reg == null) {
      log.debug("Removing instance " + clz.getName() + " but it was not registered, doing nothing");
      return;
    }
    reg.setInstance(null);
  }

  /**
   * Returns an instance of the requested service. If no registration is found an
   * OBProviderException is thrown
   * 
   * @param name
   *          the name of the service
   * @return an instance of the service
   * @throws OBProviderException
   */
  public Object get(String name) {
    final Registration reg = registrations.get(name);
    if (reg == null) {
      throw new OBProviderException("No registration for name " + name);
    }
    return reg.getInstance();
  }

  class Registration {
    private String name;
    private Class<?> instanceClass;
    private boolean singleton;
    private Object theInstance;
    // custom bean mappings are not overwritable
    // by the system
    private boolean overwritable;

    public void setName(String name) {
      this.name = name;
    }

    public Class<?> getInstanceClass() {
      return instanceClass;
    }

    public void setInstanceClass(Class<?> instanceClass) {
      this.instanceClass = instanceClass;
    }

    public void setSingleton(boolean singleton) {
      this.singleton = singleton;
    }

    public Object getInstance() {
      // there are cases that the instance is set explicitly in that
      // case always that instance should be returned so
      // no singleton check!
      if (theInstance != null) {
        return theInstance;
      }

      // instantiate the class
      try {
        final Object value = instanceClass.newInstance();
        if (singleton) {
          theInstance = value;
        }
        return value;
      } catch (final Exception e) {
        throw new OBProviderException("Exception when instantiating class "
            + instanceClass.getName() + " for registration " + name, e);

      }
    }

    public void setInstance(Object instance) {
      this.theInstance = instance;
    }

    @Override
    public String toString() {
      return "Class Registration " + name + " instanceClass: " + instanceClass.getName()
          + ", singleton: " + singleton;
    }

    public boolean isOverwritable() {
      return overwritable;
    }

    public void setOverwritable(boolean overwritable) {
      this.overwritable = overwritable;
    }
  }
}