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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.window.StandardWindowComponent;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.OBUserException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.FeatureRestriction;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;

/**
 * Reads the view and generates it.
 * 
 * @author mtaal
 */
@RequestScoped
public class ViewComponent extends BaseComponent {
  @Inject
  private StandardWindowComponent standardWindowComponent;

  @Inject
  private WeldUtils weldUtils;

  @Override
  public String generate() {

    final String viewId = getParameter("viewId");
    if (viewId == null) {
      throw new IllegalArgumentException("viewId parameter not found, it is mandatory");
    }

    try {
      OBContext.setAdminMode();

      final Window window = OBDal.getInstance().get(Window.class, correctViewId(viewId));

      if (window != null) {
        FeatureRestriction featureRestriction = ActivationKey.getInstance().hasLicenseAccess("MW",
            window.getId());
        if (featureRestriction != FeatureRestriction.NO_RESTRICTION) {
          throw new OBUserException(featureRestriction.toString());
        }
        return generateWindow(window);
      } else {
        return generateView(viewId);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  protected String generateWindow(Window window) {
    standardWindowComponent.setWindow(window);
    standardWindowComponent.setParameters(getParameters());
    final String jsCode = standardWindowComponent.generate();
    return jsCode;
  }

  protected String generateView(String viewName) {
    OBUIAPPViewImplementation viewImpDef = getView(viewName);

    final BaseTemplateComponent component;
    if (viewImpDef.getJavaClassName() != null) {
      try {
        @SuppressWarnings("unchecked")
        final Class<BaseTemplateComponent> clz = (Class<BaseTemplateComponent>) OBClassLoader
            .getInstance().loadClass(viewImpDef.getJavaClassName());
        component = weldUtils.getInstance(clz);
      } catch (Exception e) {
        throw new OBException(e);
      }
    } else {
      component = weldUtils.getInstance(BaseTemplateComponent.class);
      if (viewImpDef.getTemplate() == null) {
        throw new IllegalStateException("No class and no template defined for view " + viewName);
      }
    }
    component.setId(viewImpDef.getId());
    component.setComponentTemplate(viewImpDef.getTemplate());
    component.setParameters(getParameters());

    final String jsCode = component.generate();
    return jsCode;
  }

  private OBUIAPPViewImplementation getView(String viewName) {
    OBCriteria<OBUIAPPViewImplementation> obc = OBDal.getInstance().createCriteria(
        OBUIAPPViewImplementation.class);
    obc.add(Restrictions.or(Restrictions.eq(OBUIAPPViewImplementation.PROPERTY_NAME, viewName),
        Restrictions.eq(OBUIAPPViewImplementation.PROPERTY_ID, viewName)));

    if (obc.list().size() > 0) {
      return obc.list().get(0);
    } else {
      throw new IllegalArgumentException("No view found using id/name " + viewName);
    }
  }

  @Override
  public Module getModule() {
    final String id = getParameter("viewId");
    final Window window = OBDal.getInstance().get(Window.class, correctViewId(id));
    if (window != null) {
      return window.getModule();
    } else {
      OBUIAPPViewImplementation view = getView(id);
      if (view != null) {
        return view.getModule();
      } else {
        return super.getModule();
      }
    }
  }

  protected String correctViewId(String viewId) {
    // the case if a window is in development and has a unique making postfix
    // see the StandardWindowComponent.getWindowClientClassName method
    // changes made here should also be done there
    String correctedViewId = (viewId.startsWith(KernelConstants.ID_PREFIX) ? viewId.substring(1)
        : viewId);
    // if in consultants mode, do another conversion
    if (correctedViewId.contains(KernelConstants.ID_PREFIX)) {
      final int index = correctedViewId.indexOf(KernelConstants.ID_PREFIX);
      correctedViewId = correctedViewId.substring(0, index);
    }
    return correctedViewId;
  }

  @Override
  public Object getData() {
    return this;
  }

  @Override
  public String getETag() {
    String etag = super.getETag();

    return etag + "_" + getViewVersionHash();
  }

  private synchronized String getViewVersionHash() {
    String viewVersionHash = "";
    String viewVersions = "";
    final String viewId = getParameter("viewId");
    OBContext.setAdminMode();
    try {
      Window window = OBDal.getInstance().get(Window.class, correctViewId(viewId));
      if (window == null) {
        return viewVersionHash;
      }
      for (Tab t : window.getADTabList()) {
        viewVersions += t.getTable().isFullyAudited() + "|";
      }
      viewVersionHash = DigestUtils.md5Hex(viewVersions);
    } finally {
      OBContext.restorePreviousMode();
    }
    return viewVersionHash;
  }
}
