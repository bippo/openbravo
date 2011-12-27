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

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.UUIDHexGenerator;
import org.hibernate.type.Type;
import org.openbravo.base.model.BaseOBObjectDef;
import org.openbravo.base.session.DalUUIDGenerator;

/**
 * Extends the standard Hibernate UUIDHexGenerator. This is needed because the standard Hibernate
 * UUIDHexGenerator will overwrite the id even if the object already has one. The goal is to try to
 * keep an id if it has been assigned to an object. This is important in case of imports.
 * 
 * @deprecated replaced by the {@link DalUUIDGenerator}.
 * 
 * @author mtaal
 */
public class DalUUIDHexGenerator extends UUIDHexGenerator {
  @Override
  public void configure(Type type, Properties params, Dialect d) throws MappingException {
    super.configure(type, params, d);
  }

  @Override
  public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
    final BaseOBObjectDef bob = (BaseOBObjectDef) obj;
    if (bob.getId() != null) {
      return ((String) bob.getId()).toUpperCase();
    }
    return ((String) super.generate(session, obj)).toUpperCase();
  }
}