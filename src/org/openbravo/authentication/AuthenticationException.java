/*
 ************************************************************************************
 * Copyright (C) 2001-2011 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.authentication;

import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.OBError;

/**
 * 
 * @author adrianromero
 * @author iperdomo
 */
public class AuthenticationException extends OBException {
  private static final long serialVersionUID = 1L;
  private OBError error;

  public AuthenticationException(String msg) {
    super(msg);
    this.error = null;
  }

  public AuthenticationException(String msg, Throwable cause) {
    super(msg, cause);
    this.error = null;
  }

  public AuthenticationException(String msg, OBError error) {
    super(msg);
    this.error = error;
  }

  public OBError getOBError() {
    return error;
  }
}
