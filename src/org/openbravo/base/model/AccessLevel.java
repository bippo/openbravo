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

package org.openbravo.base.model;

import org.openbravo.base.exception.OBException;

/**
 * Defines the available accesslevels used for an entity.
 * 
 * @author Martin Taal
 */
public enum AccessLevel {
  SYSTEM, CLIENT, ORGANIZATION, CLIENT_ORGANIZATION, SYSTEM_CLIENT, ALL;

  /**
   * Returns raw value for the access level as its stored in the database
   */
  public int getDbValue() {
    switch (this) {
    case SYSTEM:
      return 4;
    case ORGANIZATION:
      return 1;
    case CLIENT_ORGANIZATION:
      return 3;
    case SYSTEM_CLIENT:
      return 6;
    case ALL:
      return 7;
      // client is not implemented..
    case CLIENT:
    default:
      throw new OBException("getDbValue called with illegal value: " + name());
    }
  }
}
