package org.openbravo.userinterface.smartclient;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.StaticResourceComponent;

/**
 * Is used to provide the global resources needed for smartclient.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
@ComponentProvider.Qualifier(SmartClientComponentProvider.QUALIFIER)
public class SmartClientComponentProvider extends BaseComponentProvider {
  public static final String SC_COMPONENT_TYPE = "OBUISC_Smartclient";
  public static final String QUALIFIER = SC_COMPONENT_TYPE;

  /**
   * @return the {@link TypesComponent}.
   * @throws IllegalArgumentException
   */
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(TypesComponent.SC_TYPES_COMPONENT_ID)) {
      final TypesComponent component = getComponent(TypesComponent.class);
      component.setId(TypesComponent.SC_TYPES_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  /**
   * @return a set of global resources
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalComponentResources()
   */
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER + "/smartclient/skin_styles.css", true));

    // note ISC_Combined.js is only added for the classic mode
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/isomorphic/ISC_Combined.js", true, false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER + "/smartclient/load_skin.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/js/ob-smartclient-labels.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/js/ob-smartclient.js", true));
    globalResources.add(createStaticResource("org.openbravo.client.kernel/" + SC_COMPONENT_TYPE
        + "/" + TypesComponent.SC_TYPES_COMPONENT_ID, true));
    return globalResources;
  }

  /**
   * @return the package name of the module to which this provider belongs
   */
  public String getModulePackageName() {
    return this.getClass().getPackage().getName();
  }

}
