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
package org.openbravo.userinterface.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;

/**
 * Provides Selector Components.
 * 
 * @author mtaal
 */
@ApplicationScoped
@ComponentProvider.Qualifier(SelectorConstants.SELECTOR_COMPONENT_TYPE)
public class SelectorComponentProvider extends BaseComponentProvider {

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponent(java.lang.String,
   * java.lang.String, java.util.Map)
   */
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    final SelectorComponent selectorComponent = getComponent(SelectorComponent.class);
    selectorComponent.setId(componentId);
    selectorComponent.setParameters(parameters);
    return selectorComponent;
  }

  /**
   * @return an empty String (no global resources)
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalComponentResources()
   */
  public List<ComponentResource> getGlobalComponentResources() {
    final ArrayList<ComponentResource> resources = new ArrayList<ComponentResource>();
    resources.add(createStaticResource(
        "web/org.openbravo.userinterface.selector/js/ob-selector-widget.js", true, false));
    resources.add(createStaticResource(
        "web/org.openbravo.userinterface.selector/js/ob-selector-link-widget.js", true, false));
    resources.add(createStaticResource(
        "web/org.openbravo.userinterface.selector/js/ob-selector-item.js", false, true));
    resources.add(createStaticResource(
        "web/org.openbravo.userinterface.selector/js/ob-selector-filter-select-item.js", false,
        true));

    resources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.userinterface.selector/ob-selector-item-styles.js", false, true));

    return resources;
  }

  /**
   * @return the package name of the module to which this provider belongs
   */
  public String getModulePackageName() {
    return this.getClass().getPackage().getName();
  }

}
