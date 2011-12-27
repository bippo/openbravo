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

package org.openbravo.dal.xml;

import org.openbravo.base.exception.OBException;

/**
 * Is thrown when an Exception situation occurs in the XML to Entity or the Entity to XML code.
 * 
 * @see EntityXMLConverter
 * @see XMLEntityConverter
 * @author mtaal
 */
public class EntityXMLException extends OBException {

  private static final long serialVersionUID = 1L;

  public EntityXMLException() {
    super();
  }

  public EntityXMLException(String message, Throwable cause) {
    super(message, cause);
  }

  public EntityXMLException(String message) {
    super(message);
  }

  public EntityXMLException(Throwable cause) {
    super(cause);
  }
}
