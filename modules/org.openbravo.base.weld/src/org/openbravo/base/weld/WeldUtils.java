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
package org.openbravo.base.weld;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.jboss.weld.environment.servlet.Listener;
import org.openbravo.dal.core.DalContextListener;

/**
 * Provides weld utilities.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class WeldUtils {

  private static BeanManager staticBeanManager = null;

  public static BeanManager getStaticInstanceBeanManager() {
    if (staticBeanManager == null) {
      staticBeanManager = (BeanManager) DalContextListener.getServletContext().getAttribute(
          Listener.BEAN_MANAGER_ATTRIBUTE_NAME);
    }
    return staticBeanManager;
  }

  public static void setStaticInstanceBeanManager(BeanManager theBeanManager) {
    staticBeanManager = theBeanManager;
  }

  @SuppressWarnings("serial")
  public static final AnnotationLiteral<Any> ANY_LITERAL = new AnnotationLiteral<Any>() {
  };

  /**
   * Method which uses the static instance of the bean manager cached in this class. This method
   * should only be used by objects which are not created by Weld. Objects created by Weld should
   * preferably use the @Inject annotation to get an instance of the WeldUtils injected.
   * 
   * @see WeldUtils#getInstance(Class)
   */
  @SuppressWarnings("unchecked")
  public static <T> T getInstanceFromStaticBeanManager(Class<T> type) {
    final BeanManager theBeanManager = getStaticInstanceBeanManager();
    final Set<Bean<?>> beans = theBeanManager.getBeans(type, ANY_LITERAL);
    for (Bean<?> bean : beans) {
      if (bean.getBeanClass() == type) {
        return (T) theBeanManager.getReference(bean, type,
            theBeanManager.createCreationalContext(bean));
      }
    }
    throw new IllegalArgumentException("No bean found for type " + type);
  }

  @Inject
  private BeanManager beanManager;

  /**
   * Return an instance which has the requested class. Ignores inheritance, so if a class A extends
   * a class B, if B is requested then an instance of B is returned and not an instance of A.
   * 
   * @param <T>
   *          the expected class
   * @param type
   *          the type to search, this type and all its subtypes are searched
   * @return an instance of the requested type
   */
  @SuppressWarnings("unchecked")
  public <T> T getInstance(Class<T> type) {
    final Set<Bean<?>> beans = beanManager.getBeans(type, ANY_LITERAL);
    for (Bean<?> bean : beans) {
      if (bean.getBeanClass() == type) {
        return (T) beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
      }
    }
    throw new IllegalArgumentException("No bean found for type " + type);
  }
}
