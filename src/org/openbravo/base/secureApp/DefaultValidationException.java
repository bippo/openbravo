/*
 ************************************************************************************
 * Copyright (C) 2008-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.base.secureApp;

/**
 * Class used to catch invalid settings during the login process
 * 
 * @author Openbravo
 * 
 */
public class DefaultValidationException extends Exception {

  private static final long serialVersionUID = 1L;
  private String defaultField;

  public DefaultValidationException(String message, String fieldName) {
    super(message);
    setDefaultField(fieldName);
  }

  /**
   * Method used to determine the field for which the default setting failed
   * 
   * @return the field for which the default setting failed.
   */
  public String getDefaultField() {
    return defaultField;
  }

  private void setDefaultField(String fieldName) {
    defaultField = fieldName;
  }
}
