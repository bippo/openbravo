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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.validation;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;

/**
 * Is thrown when an entity or property value is invalid. This Exception is not logged. Instead it
 * allows messages to be added and stored by {@link Property Property}.
 * 
 * @author mtaal
 */
public class ValidationException extends OBException {

  private static final long serialVersionUID = 1L;

  private Map<Property, String> msgs = new HashMap<Property, String>();

  public ValidationException() {
    super();
  }

  public void addMessage(Property p, String msg) {
    msgs.put(p, msg);
  }

  public boolean hasMessages() {
    return !msgs.isEmpty();
  }

  @Override
  public String getMessage() {
    if (msgs == null) {
      // during construction
      return "";
    }
    final StringBuffer sb = new StringBuffer();
    for (final Property p : msgs.keySet()) {
      final String msg = msgs.get(p);
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(p.getName() + ": " + msg);
    }
    return sb.toString();
  }
}
