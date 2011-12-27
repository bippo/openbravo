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
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.FeatureRestriction;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;

/**
 * The component which takes care of creating a class for a specific Openbravo window.
 * 
 * @author mtaal
 */
public class StandardWindowComponent extends BaseTemplateComponent {
  private static final Logger log = Logger.getLogger(StandardWindowComponent.class);
  private static final String DEFAULT_TEMPLATE_ID = "ADD5EF45333C458098286D0E639B3290";

  protected static final Map<String, String> TEMPLATE_MAP = new HashMap<String, String>();
  static {
    // WindowType - Template
    TEMPLATE_MAP.put("OBUIAPP_PickAndExecute", "FF80818132F916130132F9357DE10016");
  }

  private Window window;
  private OBViewTab rootTabComponent = null;
  private Boolean inDevelopment = null;
  private String uniqueString = "" + System.currentTimeMillis();
  private List<String> processViews = new ArrayList<String>();

  protected Template getComponentTemplate() {
    if (TEMPLATE_MAP.containsKey(window.getWindowType())) {
      return OBDal.getInstance().get(Template.class, TEMPLATE_MAP.get(window.getWindowType()));
    }
    return OBDal.getInstance().get(Template.class, DEFAULT_TEMPLATE_ID);
  }

  public String getWindowClientClassName() {
    // see the ViewComponent#correctViewId
    // changes made in this if statement should also be done in that method
    if (isIndevelopment()) {
      return KernelConstants.ID_PREFIX + window.getId() + KernelConstants.ID_PREFIX + uniqueString;
    }
    return KernelConstants.ID_PREFIX + getWindowId();
  }

  public void setUniqueString(String uniqueString) {
    this.uniqueString = uniqueString;
  }

  public boolean isIndevelopment() {
    if (inDevelopment != null) {
      return inDevelopment;
    }

    // check window, tabs and fields
    inDevelopment = Boolean.FALSE;
    if (window.getModule().isInDevelopment() && window.getModule().isEnabled()) {
      inDevelopment = Boolean.TRUE;
    } else {
      for (Tab tab : window.getADTabList()) {
        if (tab.isActive() && tab.getModule().isInDevelopment() && tab.getModule().isEnabled()) {
          inDevelopment = Boolean.TRUE;
          break;
        }
        for (Field field : tab.getADFieldList()) {
          if (field.isActive() && field.getModule().isInDevelopment()
              && field.getModule().isEnabled()) {
            inDevelopment = Boolean.TRUE;
            break;
          }
        }
        if (inDevelopment) {
          break;
        }
      }
    }
    return inDevelopment;
  }

  public String generate() {
    final String jsCode = super.generate();
    // System.err.println(jsCode);
    return jsCode;
  }

  public String getTabView() {
    return getRootTabComponent().generate();
  }

  public String getWindowId() {
    return getWindow().getId();
  }

  public String getThreadSafe() {
    final Boolean value = getWindow().isThreadsafe();
    if (value != null) {
      return value.toString();
    }
    return "false";
  }

  public Window getWindow() {
    return window;
  }

  public void setWindow(Window window) {
    this.window = window;
  }

  public OBViewTab getRootTabComponent() {
    if (rootTabComponent != null) {
      return rootTabComponent;
    }

    final List<OBViewTab> tempTabs = new ArrayList<OBViewTab>();
    for (Tab tab : getWindow().getADTabList()) {
      // NOTE: grid sequence and field sequence tabs do not have any fields defined!
      if (!tab.isActive()
          || tab.getADFieldList().isEmpty()
          || ActivationKey.getInstance().hasLicencesTabAccess(tab.getId()) != FeatureRestriction.NO_RESTRICTION) {
        continue;
      }
      final OBViewTab tabComponent = createComponent(OBViewTab.class);
      tabComponent.setTab(tab);
      tabComponent.setUniqueString(uniqueString);
      tempTabs.add(tabComponent);
      final String processView = tabComponent.getProcessViews();
      if (!"".equals(processView)) {
        processViews.add(tabComponent.getProcessViews());
      }
    }

    // compute the correct hierarchical structure of the tabs
    for (OBViewTab tabComponent : tempTabs) {
      OBViewTab parentTabComponent = null;
      for (OBViewTab testTabComponent : tempTabs) {
        if (testTabComponent.getTab().getTabLevel() == (tabComponent.getTab().getTabLevel() - 1)
            && testTabComponent.getTab().getSequenceNumber() < tabComponent.getTab()
                .getSequenceNumber()) {
          if (parentTabComponent != null) {
            // if the new potential parent has a higher sequence number then that one is the correct
            // one
            if (parentTabComponent.getTab().getSequenceNumber() < testTabComponent.getTab()
                .getSequenceNumber()) {
              parentTabComponent = testTabComponent;
            }
          } else {
            parentTabComponent = testTabComponent;
          }
        }
      }
      if (parentTabComponent != null) {
        parentTabComponent.addChildTabComponent(tabComponent);
      }
    }

    // handle a special case, multiple root tab components
    // now get the root tabs
    for (OBViewTab tabComponent : tempTabs) {
      if (tabComponent.getParentTabComponent() == null) {
        if (rootTabComponent != null) {
          // warn for a special case, multiple root tab components
          log.warn("Window " + window.getName() + " " + window.getId()
              + " has more than on tab on level 0, choosing an arbitrary root tab");
          rootTabComponent.addChildTabComponent(tabComponent);
        } else {
          rootTabComponent = tabComponent;
        }
      }
    }
    if (rootTabComponent != null) {
      rootTabComponent.setRootTab(true);
    }
    return rootTabComponent;
  }

  public List<String> getProcessViews() {
    return processViews;
  }
}
