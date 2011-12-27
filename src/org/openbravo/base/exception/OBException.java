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

package org.openbravo.base.exception;

import org.apache.log4j.Logger;

/**
 * This is the base exception for all exceptions in Openbravo. It is an unchecked exception which
 * also logs itself.
 * 
 * @author mtaal
 */
public class OBException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public OBException() {
    super();
    getLogger().error("Exception", this);
  }

  public OBException(String message, Throwable cause) {
    super(message, cause);
    getLogger().error(message, cause);
  }

  public OBException(String message) {
    super(message);
    getLogger().error(message, this);
  }

  public OBException(Throwable cause) {
    super(cause);
    getLogger().error(cause.getMessage(), cause);
  }

  /**
   * This method returns a logger which can be used by a subclass. The logger is specific for the
   * instance of the Exception (the subclass).
   * 
   * @return the class-specific Logger
   */
  protected Logger getLogger() {
    return Logger.getLogger(this.getClass());
  }
}
