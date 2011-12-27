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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.kernel;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.client.kernel.event.PersistenceEventOBInterceptor;
import org.openbravo.dal.core.OBInterceptor;

/**
 * Class responsible for initializing the kernel layer. Can be used in a servlet as well as a
 * non-servlet environment.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class KernelInitializer {

  @Inject
  private PersistenceEventOBInterceptor persistenceEventOBInterceptor;

  @Inject
  @Any
  private Instance<ApplicationInitializer> applicationInitializers;

  public void initialize() {
    setInterceptor();

    for (ApplicationInitializer initializer : applicationInitializers) {
      initializer.initialize();
    }

  }

  public synchronized void setInterceptor() {
    final OBInterceptor interceptor = (OBInterceptor) SessionFactoryController.getInstance()
        .getConfiguration().getInterceptor();
    if (interceptor.getInterceptorListener() == null) {
      interceptor.setInterceptorListener(persistenceEventOBInterceptor);
    }
  }

}
