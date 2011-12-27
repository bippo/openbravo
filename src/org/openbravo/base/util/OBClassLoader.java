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

package org.openbravo.base.util;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;

/**
 * The OBClassLoader is used to support different classloading scenarios. As a default two
 * classloaders are present: the context (the default, used in Tomcat) and the class classloader.
 * The class classloader is used in Ant tasks.
 * <p/>
 * Use the {@link OBProvider OBProvider} to define which classloader in a specific environment.
 * 
 * @author mtaal
 */

public class OBClassLoader implements OBSingleton {

  private static OBClassLoader instance;

  public static OBClassLoader getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(OBClassLoader.class);
    }
    return instance;
  }

  /**
   * Load a class using the classloader. This method will throw an OBException if the class is not
   * found. This exception is logged.
   * 
   * @param className
   *          the name of the class to load
   * @throws ClassNotFoundException
   */
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    return Thread.currentThread().getContextClassLoader().loadClass(className);
  }

  /**
   * A class loader which uses the classloader of the Class class.
   * 
   * To use this classloader do the following:
   * OBProvider.getInstance().register(OBClassLoader.class, OBClassLoader.ClassOBClassLoader.class,
   * false);
   */
  public static class ClassOBClassLoader extends OBClassLoader {

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
      return Class.forName(className);
    }
  }
}