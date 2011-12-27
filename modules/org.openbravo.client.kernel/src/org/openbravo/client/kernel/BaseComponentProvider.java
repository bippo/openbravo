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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.model.ad.module.Module;

/**
 * Base implementation, can be extended.
 * 
 * @author mtaal
 */
public abstract class BaseComponentProvider implements ComponentProvider {

  private Module module;

  @Inject
  @Any
  private Instance<Component> components;

  /**
   * Return a component of the correct implementation using Weld.
   * 
   * @param clz
   *          an instance of this class will be returned
   * @return an instance of clz
   */
  protected <U extends Component> U getComponent(Class<U> clz) {
    return (U) components.select(clz).get();
  }

  public Module getModule() {
    if (module != null) {
      return module;
    }
    module = KernelUtils.getInstance().getModule(getModulePackageName());
    return module;
  }

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
   * @see KernelUtils#getVersionParameters(Module)
   */
  public String getVersionParameters(String resource) {
    return KernelUtils.getInstance().getVersionParameters(getModule());
  }

  /**
   * Override this method if the component is in a different package than the module.
   * 
   * @return
   */
  protected String getModulePackageName() {
    return this.getClass().getPackage().getName();
  }

  public List<String> getTestResources() {
    return null;
  }

  protected ComponentResource createStaticResource(String path, boolean includeAlsoInClassicMode,
      boolean includeInNewUIMode) {
    final ComponentResource componentResource = new ComponentResource();
    componentResource.setType(ComponentResourceType.Static);
    componentResource.setPath(path);
    componentResource.setIncludeAlsoInClassicMode(includeAlsoInClassicMode);
    componentResource.setIncludeInNewUIMode(includeInNewUIMode);
    return componentResource;
  }

  protected ComponentResource createStaticResource(String path, boolean includeAlsoInClassicMode) {
    final ComponentResource componentResource = new ComponentResource();
    componentResource.setType(ComponentResourceType.Static);
    componentResource.setPath(path);
    componentResource.setIncludeAlsoInClassicMode(includeAlsoInClassicMode);
    return componentResource;
  }

  protected ComponentResource createStyleSheetResource(String path, boolean includeAlsoInClassicMode) {
    final ComponentResource componentResource = new ComponentResource();
    componentResource.setType(ComponentResourceType.Stylesheet);
    componentResource.setPath(path);
    componentResource.setIncludeAlsoInClassicMode(includeAlsoInClassicMode);
    return componentResource;
  }

  protected ComponentResource createDynamicResource(String path) {
    final ComponentResource componentResource = new ComponentResource();
    componentResource.setType(ComponentResourceType.Dynamic);
    componentResource.setPath(path);
    componentResource.setIncludeAlsoInClassicMode(false);
    return componentResource;
  }

  /**
   * Implemented here for backward compatibility, calls the {@link #getGlobalResources()}
   */
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    for (String globalResource : getGlobalResources()) {
      globalResources.add(createStaticResource(globalResource, true));
    }
    return globalResources;
  }

  // Implemented for backward compatibility
  @Deprecated
  @Override
  public List<String> getGlobalResources() {
    return Collections.emptyList();
  }

  public static class ComponentResource {

    public enum ComponentResourceType {
      Static, Dynamic, Stylesheet
    }

    private ComponentResourceType type;
    private String path;
    private boolean includeAlsoInClassicMode = false;
    private boolean includeInNewUIMode = true;

    public ComponentResourceType getType() {
      return type;
    }

    public void setType(ComponentResourceType componentResourceType) {
      this.type = componentResourceType;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public String toString() {
      return type + " " + path;
    }

    public boolean isIncludeAlsoInClassicMode() {
      return includeAlsoInClassicMode;
    }

    public void setIncludeAlsoInClassicMode(boolean includeAlsoInClassicMode) {
      this.includeAlsoInClassicMode = includeAlsoInClassicMode;
    }

    public boolean isIncludeInNewUIMode() {
      return includeInNewUIMode;
    }

    public void setIncludeInNewUIMode(boolean includeInNewUIMode) {
      this.includeInNewUIMode = includeInNewUIMode;
    }

  }
}
