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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;

/**
 * The BaseComponent is the main implementation of the {@link Component} concept which can be
 * sub-classed to provide custom or specialized behavior.
 * 
 * A component can be dependent on another component. This means that in the eventual generated code
 * the dependent components are generated first. The BaseComponent takes care that duplicates in
 * dependent components are handled correctly meaning that a dependency is only included once.
 * 
 * @author mtaal
 */
public abstract class BaseComponent implements Component {

  private static final String HTTP_PREFIX = "http://";
  private static final String HTTPS_PREFIX = "https://";

  // The data is the main object which is the basis for creating this component. The
  // data object can be a grid object or a selector etc.
  private Map<String, Object> parameters = new HashMap<String, Object>();
  private String id;
  private Module module = null;

  private List<Component> dependencies = new ArrayList<Component>();

  // the url of the server and the servlet context
  private static String contextUrl = null;

  private static String moduleVersionHash = null;
  private static Boolean hasModulesInDevelopment = null;

  private static final Logger log4j = Logger.getLogger(BaseComponent.class);

  @Inject
  @Any
  private Instance<Component> components;

  // TODO: add the concept of child components which are generated/rendered before the root
  // component.
  // then also identify if this a rootView
  // only the root needs to generate javascript code for datasources which are global objects

  /**
   * @return the generated javascript which is send back to the client
   */
  public abstract String generate();

  public String getContextUrl() {
    if (contextUrl != null) {
      return stripHost(contextUrl);
    }
    if (hasParameter(KernelConstants.CONTEXT_URL)) {
      contextUrl = getParameter(KernelConstants.CONTEXT_URL);
      if (!contextUrl.endsWith("/")) {
        contextUrl += "/";
      }
      return stripHost(contextUrl);
    }
    return "";
  }

  public void setContextUrl(String contextUrl) {
    BaseComponent.contextUrl = contextUrl;
  }

  protected String stripHost(String url) {
    if (url.toLowerCase().startsWith(HTTP_PREFIX)) {
      int index = url.indexOf("/", HTTP_PREFIX.length());
      if (index != -1) {
        return url.substring(index);
      }
    }
    if (url.toLowerCase().startsWith(HTTPS_PREFIX)) {
      int index = url.indexOf("/", HTTPS_PREFIX.length());
      if (index != -1) {
        return url.substring(index);
      }
    }
    return url;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public abstract Object getData();

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public List<String> getParameterNames() {
    final List<String> params = new ArrayList<String>();
    for (String key : getParameters().keySet()) {
      if (getParameters().get(key) instanceof String) {
        params.add(key);
      }
    }
    return params;
  }

  public String getParameter(String key) {
    if (!getParameters().containsKey(key)) {
      // TODO: should not happen
      return "";
    }
    return getParameters().get(key).toString();
  }

  public boolean hasParameter(String key) {
    return getParameters().containsKey(key);
  }

  public List<Component> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<Component> dependencies) {
    this.dependencies = dependencies;
  }

  /**
   * Return a new component of the correct implementation using Weld.
   * 
   * @param clz
   *          an instance of this class will be returned
   * @return an instance of clz
   */
  protected <U extends Component> U createComponent(Class<U> clz) {
    return (U) components.select(clz).get();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.Component#getETag()
   */
  public String getETag() {
    if (hasModulesInDevelopment()) {
      return OBContext.getOBContext().getLanguage().getId() + "_" + getLastModified().getTime();
    } else {
      return OBContext.getOBContext().getLanguage().getId() + "_" + getModuleVersionHash();
    }
  }

  synchronized private boolean hasModulesInDevelopment() {
    if (hasModulesInDevelopment == null) {
      OBContext.setAdminMode();
      try {
        OBCriteria<Module> qMod = OBDal.getInstance().createCriteria(Module.class);
        qMod.add(Restrictions.eq(Module.PROPERTY_INDEVELOPMENT, true));
        hasModulesInDevelopment = qMod.count() > 0;
        log4j.debug("Calculating whether there are modules in development: "
            + hasModulesInDevelopment);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    return hasModulesInDevelopment;
  }

  synchronized private String getModuleVersionHash() {
    if (moduleVersionHash == null) {
      String moduleVersions = "";
      OBContext.setAdminMode();
      try {
        OBCriteria<Module> qMod = OBDal.getInstance().createCriteria(Module.class);
        qMod.addOrder(Order.asc(Module.PROPERTY_ID));
        for (Module mod : qMod.list()) {
          moduleVersions += mod.getId() + "-" + mod.getVersion() + "-" + mod.isEnabled();
          if ("T".equals(mod.getType())) {
            moduleVersions += "-" + mod.isApplyConfigurationScript();
          }
          moduleVersions += "\n";
        }
        moduleVersionHash = DigestUtils.md5Hex(moduleVersions);
        log4j.debug("New moduleVersionHash. Original: " + moduleVersions + " hash:"
            + moduleVersionHash);

      } finally {
        OBContext.restorePreviousMode();
      }
    }
    return moduleVersionHash;
  }

  synchronized public static void nullifyModuleCache() {
    hasModulesInDevelopment = null;
    moduleVersionHash = null;
    log4j.debug("Module cache for etag is now null");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.Component#getLastModified()
   */
  public Date getLastModified() {
    return new Date();
  }

  public Module getModule() {
    if (module != null) {
      return module;
    }
    module = KernelUtils.getInstance().getModule(getModulePackageName());
    return module;
  }

  /**
   * Override this method if the component is in a different package than the module.
   * 
   * @return
   */
  protected String getModulePackageName() {
    return this.getClass().getPackage().getName();
  }

  /**
   * Translates a null value to a value which can be handled better by a template.
   */
  protected String getSafeValue(Object value) {
    if (value == null) {
      return "";
    }
    return value + "";
  }

  /**
   * @return returns the javascript content type with UTF-8: application/javascript;charset=UTF-8
   */
  public String getContentType() {
    return KernelConstants.JAVASCRIPT_CONTENTTYPE;
  }

  public boolean isJavaScriptComponent() {
    return true;
  }

  public boolean isInDevelopment() {
    return getModule().isInDevelopment();
  }
}
