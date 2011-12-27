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

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Initializes the dal layer when the servlet container starts.
 * 
 * @see DalLayerInitializer
 * 
 * @author mtaal
 */
public class DalContextListener implements ServletContextListener {
  private static Properties obProperties = null;
  private static ServletContext servletContext = null;

  public static ServletContext getServletContext() {
    return servletContext;
  }

  public static void setServletContext(ServletContext context) {
    DalContextListener.servletContext = context;
  }

  public static Properties getOpenBravoProperties() {
    return obProperties;
  }

  /**
   * Reads the Openbravo.properties file, initializes the Dal layer and flags that the Dal layer is
   * running in a web container.
   * 
   * @see DalLayerInitializer
   * @see OBPropertiesProvider
   */
  public void contextInitialized(ServletContextEvent event) {
    // this allows the sessionfactory controller to use jndi
    SessionFactoryController.setRunningInWebContainer(true);

    final ServletContext context = event.getServletContext();
    setServletContext(context);
    final InputStream is = context.getResourceAsStream("/WEB-INF/Openbravo.properties");
    if (is != null) {
      OBPropertiesProvider.getInstance().setProperties(is);
    }
    final InputStream formatInputStream = context.getResourceAsStream("/WEB-INF/Format.xml");
    if (formatInputStream != null) {
      OBPropertiesProvider.getInstance().setFormatXML(formatInputStream);
    }

    // set our own config file provider which uses the servletcontext
    OBConfigFileProvider.getInstance().setServletContext(context);
    OBConfigFileProvider.getInstance().setClassPathLocation("/WEB-INF");

    // initialize the dal layer
    DalLayerInitializer.getInstance().initialize(true);
  }

  public void contextDestroyed(ServletContextEvent event) {
    ModelProvider.setInstance(null);
    SessionFactoryController.setInstance(null);
  }
}
