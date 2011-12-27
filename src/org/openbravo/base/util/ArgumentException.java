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

package org.openbravo.base.util;

import org.openbravo.base.exception.OBException;

/**
 * Exception which can be used to indicate that arguments of a method have illegal values. Is an
 * unchecked exception which also logs itself.
 * 
 * @author mtaal
 */
public class ArgumentException extends OBException {

  private static final long serialVersionUID = 1L;

  public ArgumentException() {
    super();
  }

  public ArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public ArgumentException(String message) {
    super(message);
  }

  public ArgumentException(Throwable cause) {
    super(cause);
  }
}
