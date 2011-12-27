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
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.dal.core.OBContext;

/**
 * Provides Kernel Components.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
@ComponentProvider.Qualifier(KernelConstants.KERNEL_COMPONENT_TYPE)
public class KernelComponentProvider extends BaseComponentProvider {
  public static final String QUALIFIER = KernelConstants.KERNEL_COMPONENT_TYPE;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponent(java.lang.String,
   * java.lang.String, java.util.Map)
   */
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    final BaseComponent component = createComponent(componentId, parameters);
    component.setParameters(parameters);
    return component;
  }

  protected BaseComponent createComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(KernelConstants.STYLE_SHEET_COMPONENT_ID)) {
      return getComponent(StyleSheetResourceComponent.class);
    } else if (componentId.equals(KernelConstants.RESOURCE_COMPONENT_ID)) {
      return getComponent(StaticResourceComponent.class);
    } else if (componentId.equals(KernelConstants.APPLICATION_COMPONENT_ID)) {
      return getComponent(ApplicationComponent.class);
    } else if (componentId.equals(KernelConstants.APPLICATION_DYNAMIC_COMPONENT_ID)) {
      return getComponent(ApplicationDynamicComponent.class);
    } else if (componentId.equals(KernelConstants.TEST_COMPONENT_ID)) {
      return getComponent(TestComponent.class);
    } else if (componentId.equals(KernelConstants.DOCUMENT_COMPONENT_ID)) {
      return getComponent(DocumentationComponent.class);
    } else if (componentId.equals(KernelConstants.LABELS_COMPONENT_ID)) {
      return getComponent(I18NComponent.class);
    }
    throw new IllegalArgumentException("Component " + componentId + " not supported here");
  }

  // in case of the application component also make it role/org dependent, this
  // also covers client dependency
  public String getVersionParameters(String resource) {
    final String versionParam = super.getVersionParameters(resource);
    if (resource.contains(KernelConstants.APPLICATION_COMPONENT_ID)
        || resource.contains(KernelConstants.APPLICATION_DYNAMIC_COMPONENT_ID)) {
      return versionParam + "&_role=" + OBContext.getOBContext().getRole().getId() + "&_org="
          + OBContext.getOBContext().getCurrentOrganization().getId();
    }
    return versionParam;
  }

  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStaticResource("org.openbravo.client.kernel/"
        + KernelConstants.KERNEL_COMPONENT_TYPE + "/" + KernelConstants.APPLICATION_COMPONENT_ID,
        true));
    globalResources.add(createDynamicResource("org.openbravo.client.kernel/"
        + KernelConstants.KERNEL_COMPONENT_TYPE + "/"
        + KernelConstants.APPLICATION_DYNAMIC_COMPONENT_ID));
    globalResources.add(createStaticResource("org.openbravo.client.kernel/"
        + KernelConstants.KERNEL_COMPONENT_TYPE + "/" + KernelConstants.LABELS_COMPONENT_ID, true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.kernel/js/ob-kernel-utilities.js", true));

    return globalResources;
  }
}
