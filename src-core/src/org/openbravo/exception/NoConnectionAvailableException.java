/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.exception;

/**
 * Thrown to indicate that database connection does not exist.
 * 
 */
public class NoConnectionAvailableException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new NoConnectionAvailableException with no detail message.
   */
  public NoConnectionAvailableException() {
    super();
  }

  /**
   * Constructs a new NoConnectionAvailableException with the specified detail message.
   * 
   * @param s
   *          the detail message
   */
  public NoConnectionAvailableException(String s) {
    super(s);
  }
}
