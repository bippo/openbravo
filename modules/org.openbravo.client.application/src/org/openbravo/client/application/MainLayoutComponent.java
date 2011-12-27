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
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.service.datasource.DataSourceConstants;

/**
 * 
 * @author iperdomo
 */
public class MainLayoutComponent extends BaseTemplateComponent {

  @Inject
  private WeldUtils weldUtils;

  @Inject
  @ComponentProvider.Qualifier(DataSourceConstants.DS_COMPONENT_TYPE)
  private ComponentProvider dsComponentProvider;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseTemplateComponent#getComponentTemplate()
   */
  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, ApplicationConstants.MAIN_LAYOUT_TEMPLATE_ID);
  }

  @SuppressWarnings("unchecked")
  public Collection<NBComponent> getNavigationBarComponents() {
    final List<NBComponent> nbComponents = new ArrayList<NBComponent>();

    OBCriteria<NavBarComponent> obc = OBDal.getInstance().createCriteria(NavBarComponent.class);

    obc.addOrderBy(NavBarComponent.PROPERTY_RECORDSORTNO, true);
    for (NavBarComponent nbc : obc.list()) {

      if (!isAccessible(nbc)) {
        continue;
      }

      final NBComponent nbComponent = new NBComponent();

      String jsCode = "";
      try {
        final Class<BaseTemplateComponent> clz = (Class<BaseTemplateComponent>) OBClassLoader
            .getInstance().loadClass(nbc.getJavaClassName());
        final BaseComponent component = weldUtils.getInstance(clz);
        component.setId(nbc.getId());
        if (component instanceof BaseTemplateComponent && nbc.getTemplate() != null) {
          ((BaseTemplateComponent) component).setComponentTemplate(nbc.getTemplate());
        }
        component.setParameters(getParameters());

        jsCode = component.generate();
        nbComponent.setJscode(jsCode);
      } catch (Exception e) {
        throw new IllegalStateException("Exception when creating component " + nbc.getId(), e);
      }
      nbComponents.add(nbComponent);
    }
    return nbComponents;
  }

  private boolean isAccessible(NavBarComponent navBarComponent) {
    if (OBContext.getOBContext().getRole().getId().equals("0")) {
      return true;
    }
    if (navBarComponent.isAllroles()) {
      return true;
    }
    final String currentRoleId = OBContext.getOBContext().getRole().getId();
    for (NavbarRoleaccess roleAccess : navBarComponent.getOBUIAPPNavbarRoleaccessList()) {
      if (currentRoleId.equals(roleAccess.getRole().getId())) {
        return true;
      }
    }
    return false;
  }

  public boolean isAddProfessionalLink() {
    if (SessionFactoryController.isRunningInWebContainer()) {
      return !ActivationKey.isActiveInstance();
    }
    return true;
  }

  public String getCompanyImageLogoWidth() {
    Image img = null;
    img = Utility.getImageLogoObject("yourcompanymenu", "");
    Long imageWidthLong = null;
    if (img != null) {
      imageWidthLong = img.getWidth();
    }
    String imageWidthString = "122";
    if (imageWidthLong != null) {
      imageWidthString = String.valueOf(imageWidthLong.intValue());
    }
    return imageWidthString;
  }

  public String getCompanyImageLogoHeight() {
    Image img = null;
    img = Utility.getImageLogoObject("yourcompanymenu", "");
    Long imageHeightLong = null;
    if (img != null) {
      imageHeightLong = img.getHeight();
    }
    String imageHeightString = "34";
    if (imageHeightLong != null) {
      imageHeightString = String.valueOf(imageHeightLong.intValue());
    }
    return imageHeightString;
  }

  public String getStartPage() {
    try {
      return Preferences.getPreferenceValue(getContextUrl()
          + ApplicationConstants.START_PAGE_PROPERTY, true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    } catch (PropertyException e) {
      return getContextUrl() + "/default/Menu.html";
    }
  }

  public String getVersion() {
    return getETag();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.Component#getETag()
   */
  public String getETag() {
    // also encodes the role id in the etag
    if (getModule().isInDevelopment() != null && getModule().isInDevelopment()) {
      return super.getETag();
    } else {
      return OBContext.getOBContext().getLanguage().getId() + "_"
          + OBContext.getOBContext().getRole().getId() + "_" + getModule().getVersion();
    }
  }

  public String getNotesDataSource() {
    final String dsId = "090A37D22E61FE94012E621729090048";
    final Map<String, Object> dsParameters = new HashMap<String, Object>(getParameters());
    dsParameters.put(DataSourceConstants.DS_CREATE, true);
    final Component component = dsComponentProvider.getComponent(dsId, dsParameters);
    return component.generate();
  }

  public static class NBComponent {
    // NB stands for: Navigation Bar
    private String jscode;

    public void setJscode(String jscode) {
      this.jscode = jscode;
    }

    public String getJscode() {
      return jscode;
    }

    public String toString() {
      return jscode;
    }
  }
}
