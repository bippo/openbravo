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
 * Thrown to indicate a pool does not exist.
 * 
 * @author <b>Ismael Ciordia</b>, Copyright &#169; 2001
 * @version 1.0, 15/10/2001
 */
public class PoolNotFoundException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new PoolNotFoundException with no detail message.
   */
  public PoolNotFoundException() {
    super();
  }

  /**
   * Constructs a new PoolNotFoundException with the specified detail message.
   * 
   * @param s
   *          the detail message
   */
  public PoolNotFoundException(String s) {
    super(s);
  }

  /**
   * Constructs a new PoolNotFoundException with the specified detail message.
   * 
   * @param s
   *          the detail message
   * @param t
   *          the exception cause
   */
  public PoolNotFoundException(String s, Throwable t) {
    super(s, t);
  }
}
