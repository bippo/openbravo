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
package org.openbravo.client.kernel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import javax.enterprise.util.AnnotationLiteral;

import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource;
import org.openbravo.model.ad.module.Module;

/**
 * A ComponentProvider is responsible for generating a component on the basis of a request and its
 * parameters. A {@link Component} can be anything from a visualization of a single field to a full
 * ui with forms and grids. The component provider is responsible for creating the component on the
 * basis of the component type, its id and additional parameters.
 * 
 * One application can have several component providers. There is at most one component provider for
 * a component type.
 * 
 * @author mtaal
 */
public interface ComponentProvider {

  /**
   * @return the JavaScript code to create this datasource on the client
   */
  public Component getComponent(String componentId, Map<String, Object> parameters);

  /**
   * @return the global resources which are needed to be present on every page.
   */
  public List<ComponentResource> getGlobalComponentResources();

  /**
   * @return the global resources which are needed to be present on every page.
   * @deprecated implement getGlobalComponentResources
   */
  @Deprecated
  public List<String> getGlobalResources();

  /**
   * @return the Module to which this provider belongs
   */
  public Module getModule();

  /**
   * 
   * @return the list of JavaScript files that implement unit tests
   */
  public List<String> getTestResources();

  /**
   * Computes parameters to add to a link of a resource. The parameters include the version and
   * language of the user.
   * 
   * The version computation logic depends on if the module is in development (
   * {@link Module#isInDevelopment()}. If in developers mode then the
   * {@link System#currentTimeMillis()} is used. If not in developers mode then the
   * {@link Module#getVersion()} is used. These values are prepended with the language id of the
   * user. This makes it possible to generate language specific components on the server.
   * 
   * @param resource
   *          , the resource to compute the version string for, is typically a resource provided by
   *          the getGlobalResources method
   * @return the version parameter string, a concatenation of the version and language with
   *         parameter names
   * @see KernelConstants#RESOURCE_VERSION_PARAMETER
   * @see KernelConstants#RESOURCE_LANGUAGE_PARAMETER
   */
  public String getVersionParameters(String resource);

  /**
   * Defines the qualifier used to register a component provider.
   * 
   * @author mtaal
   */
  @javax.inject.Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
  public @interface Qualifier {
    String value();
  }

  /**
   * A class used to select the correct component provider.
   * 
   * @author mtaal
   */
  @SuppressWarnings("all")
  public static class Selector extends AnnotationLiteral<ComponentProvider.Qualifier> implements
      ComponentProvider.Qualifier {
    private static final long serialVersionUID = 1L;

    final String value;

    public Selector(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }
  }

}
