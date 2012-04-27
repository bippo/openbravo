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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.application.window.WindowDefinitionComponent;
import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.dal.core.OBContext;

/**
 * 
 * @author iperdomo
 */
@ApplicationScoped
@ComponentProvider.Qualifier(ApplicationConstants.COMPONENT_TYPE)
public class ApplicationComponentProvider extends BaseComponentProvider {
  public static final String QUALIFIER = ApplicationConstants.COMPONENT_TYPE;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponent(java.lang.String,
   * java.util.Map)
   */
  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(WindowDefinitionComponent.WINDOW_DEF_COMPONENT)) {
      final WindowDefinitionComponent component = getComponent(WindowDefinitionComponent.class);
      component.setId(WindowDefinitionComponent.WINDOW_DEF_COMPONENT);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(ApplicationConstants.MAIN_LAYOUT_ID)) {
      final MainLayoutComponent component = getComponent(MainLayoutComponent.class);
      component.setId(ApplicationConstants.MAIN_LAYOUT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(ApplicationConstants.MAIN_LAYOUT_VIEW_COMPONENT_ID)) {
      final ViewComponent component = getComponent(ViewComponent.class);
      component.setId(ApplicationConstants.MAIN_LAYOUT_VIEW_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(ApplicationConstants.PROPERTIES_COMPONENT_ID)) {
      final PropertiesComponent component = getComponent(PropertiesComponent.class);
      component.setId(ApplicationConstants.PROPERTIES_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalResources()
   */
  @Override
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-utilities.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-utilities-date.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-utilities-number.js", true));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-popup.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/ob-form-button.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-canvas.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-checkbox.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-datechooser.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-date.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-datetime.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-encrypted.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-linktitle.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-text.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-link.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-linkbutton.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-list.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-list-filter.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-fk.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-fk-filter.js", true));
    globalResources
        .add(createStaticResource(
            "web/org.openbravo.client.application/js/form/formitem/ob-formitem-minidaterange.js",
            true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-number.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-search.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-search-attribute.js",
        true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-section.js", true));
    globalResources
        .add(createStaticResource(
            "web/org.openbravo.client.application/js/form/formitem/ob-formitem-section-audit.js",
            true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-textarea.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-time.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-yesno.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-spinner.js", true));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/toolbar/ob-toolbar.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/formitem/ob-formitem-image.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/navbar/ob-application-menu.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/main/ob-tab.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/grid/ob-grid.js", false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/navbar/ob-quickrun-widget.js", false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-property-store.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-test-registry.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-remote-call-manager.js", true));
    globalResources
        .add(createDynamicResource("org.openbravo.client.kernel/"
            + ApplicationConstants.COMPONENT_TYPE + "/"
            + ApplicationConstants.PROPERTIES_COMPONENT_ID));
    globalResources.add(createDynamicResource("org.openbravo.client.kernel/"
        + ApplicationConstants.COMPONENT_TYPE + "/"
        + WindowDefinitionComponent.WINDOW_DEF_COMPONENT));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/classic/ob-classic-window.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/classic/ob-classic-help.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-external-page.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/main/ob-standard-window.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/main/ob-standard-view-datasource.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/main/ob-standard-view.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/main/ob-base-view.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/ob-view-form-linked-items.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/ob-view-form-notes.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/ob-view-form-attachments.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/ob-view-form.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/grid/ob-view-grid.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/ob-onchange-registry.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-keyboard-manager.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/classic/ob-classic-popup.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/main/ob-messagebar.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/form/ob-statusbar.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-recent-utilities.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/navbar/ob-user-profile-widget.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/navbar/ob-logout-widget.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/navbar/ob-help-about-widget.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/toolbar/ob-action-button.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-eventhandler.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-section-stack.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/toolbar/ob-clone-order.js", false));

    // Alert Management
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/alert-management/ob-alert-manager.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/alert-management/ob-alert-grid.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/alert-management/ob-alert-management-view.js",
        false));

    // personalization
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/personalization/ob-personalization.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/personalization/ob-personalization-treegrid.js",
        false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/personalization/ob-personalize-form.js", false));
    globalResources
        .add(createStaticResource(
            "web/org.openbravo.client.application/js/personalization/ob-personalize-form-toolbar-button.js",
            false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/personalization/ob-manage-views.js", false));
    globalResources
        .add(createStaticResource(
            "web/org.openbravo.client.application/js/personalization/ob-manage-views-popups.js",
            false));
    globalResources
        .add(createStaticResource(
            "web/org.openbravo.client.application/js/personalization/ob-manage-views-toolbar.js",
            false));

    // Process
    globalResources
        .add(createStaticResource(
            "web/org.openbravo.client.application/js/process/ob-pick-and-execute-datasource.js",
            false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/process/ob-pick-and-execute-grid.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/process/ob-pick-and-execute-view.js", false));

    // Return Material
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/return-material/ob-return-material.js", false));

    // Styling
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-application-menu-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-tab-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-form-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-grid-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-navigation-bar-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-popup-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-dialog-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-toolbar-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-messagebar-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-statusbar-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-application-styles.css", false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-application-menu-styles.js", false));
    globalResources
        .add(createStaticResource(
            "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
                + KernelConstants.SKIN_PARAMETER
                + "/org.openbravo.client.application/ob-tab-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-form-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-grid-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-toolbar-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-messagebar-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-statusbar-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-popup-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-process-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-dialog-styles.js", false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/navbar/ob-quick-launch.js", false));

    // before the main layout
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-application-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-navigation-bar-styles.js", false));

    // Application - dynamic as it contains the generated menu also (which is user/role dependant)
    globalResources.add(createDynamicResource("org.openbravo.client.kernel/"
        + ApplicationConstants.COMPONENT_TYPE + "/" + ApplicationConstants.MAIN_LAYOUT_ID));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-view-manager.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/classic/ob-classic-compatibility.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/utilities/ob-history-manager.js", false));

    // personalization
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-personalization-styles.css", false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/org.openbravo.client.application/ob-personalization-styles.js", false));

    // RTL files should be added at the end. Don't add more files after them
    if (OBContext.getOBContext().isRTL()) {
      globalResources.add(createStyleSheetResource(
          "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
              + KernelConstants.SKIN_PARAMETER
              + "/org.openbravo.client.application/ob-rtl-styles.css", false));

      globalResources.add(createStaticResource(
          "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
              + KernelConstants.SKIN_PARAMETER
              + "/org.openbravo.client.application/ob-rtl-styles.js", false));
    }

    return globalResources;
  }

  @Override
  public List<String> getTestResources() {
    final List<String> testResources = new ArrayList<String>();
    testResources.add("web/org.openbravo.client.application/js/test/ob-ui-test.js");
    testResources.add("web/org.openbravo.client.application/js/test/ob-property-store-test.js");
    testResources.add("web/org.openbravo.client.application/js/test/ob-utilities-date-test.js");
    testResources.add("web/org.openbravo.client.application/js/test/ob-navbar-test.js");
    return testResources;
  }
}
