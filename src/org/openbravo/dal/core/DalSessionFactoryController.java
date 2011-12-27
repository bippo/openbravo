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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.core;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Initializes and provides the session factory for the runtime dal layer. This
 * SessionFactoryController is initialized after the model has been read in-memory. The
 * {@link DalMappingGenerator DalMappingGenerator} is used to generated the Hibernate mapping for
 * the runtime model (see {@link ModelProvider ModelProvider}.
 * 
 * @author mtaal
 */

public class DalSessionFactoryController extends SessionFactoryController {
  private static final Logger log = Logger.getLogger(DalSessionFactoryController.class);

  @Override
  protected void mapModel(Configuration configuration) {
    final String mapping = DalMappingGenerator.getInstance().generateMapping();
    log.debug("Generated mapping: ");
    log.debug(mapping);
    configuration.addXML(mapping);
  }

  @Override
  protected void setInterceptor(Configuration configuration) {
    configuration.setInterceptor(new OBInterceptor());
  }
}
