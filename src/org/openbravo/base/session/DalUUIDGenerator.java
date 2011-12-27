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

package org.openbravo.base.session;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.UUIDGenerator;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.BaseOBObjectDef;

/**
 * Extends the standard Hibernate UUIDGenerator. This is needed because:
 * <ul>
 * <li>the standard Hibernate UUIDGenerator will overwrite the id even if the object already has
 * one. The goal is to try to keep an id if it has been assigned to an object. This is important in
 * case of imports.</li>
 * <li>the standard uuidgenerator will generate uuid strings of length 36 with the - as a separator,
 * the length should be 32</li>
 * </ul>
 * 
 * @author mtaal
 */
public class DalUUIDGenerator extends UUIDGenerator {

  @Override
  public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
    final BaseOBObjectDef bob = (BaseOBObjectDef) obj;
    if (bob.getId() != null) {
      return ((String) bob.getId()).toUpperCase();
    }
    String result = ((String) super.generate(session, obj)).toUpperCase();
    result = result.replace("-", "");
    if (result.length() != 32) {
      throw new OBException("Generating UUID of wrong length: " + result);
    }
    return result;
  }
}