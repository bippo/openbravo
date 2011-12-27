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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openbravo.model.ad.module.Module;

/**
 * 
 * @author iperdomo
 * 
 */
public class TestComponent extends BaseComponent {

  @Inject
  @Any
  private Instance<ComponentProvider> componentProviders;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseComponent#generate()
   */
  @Override
  public String generate() {
    final StringBuilder sb = new StringBuilder();
    for (String resource : getTestResources()) {
      String testResource = resource;
      if (testResource.startsWith("/") && getContextUrl().length() > 0) {
        testResource = getContextUrl() + testResource.substring(1);
      } else {
        testResource = getContextUrl() + testResource;
      }
      sb.append("document.write(\"<\" + \"script src='" + testResource + "'><\" + \"/script>\");\n");
    }
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseComponent#getData()
   */
  @Override
  public Object getData() {
    return this;
  }

  /**
   * Get the list of static test resources ordered by module dependency
   * 
   * @return all static test components to be used by the ui test suite
   */
  public List<String> getTestResources() {
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    final List<String> result = new ArrayList<String>();
    for (Module mod : modules) {
      for (ComponentProvider provider : componentProviders) {
        if (provider.getTestResources() == null) {
          continue;
        }
        if (provider.getModule().getId().equals(mod.getId())) {
          for (String resource : provider.getTestResources()) {
            if (!"".equals(resource)) {
              result.add(resource);
            }
          }
        }
      }
    }
    return result;
  }
}
