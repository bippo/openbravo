/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.base.filter;

/**
 * Request filter which checks if the input is contained in a fixed list of allowed input. The
 * comparison is made case in-sensitively.
 * 
 * @author huehner
 * 
 */
public class ValueListFilter implements RequestFilter {
  private final String[] allowedValues;

  public ValueListFilter(String... allowedValues) {
    this.allowedValues = allowedValues;
  }

  @Override
  public boolean accept(String value) {
    for (String allowedValue : allowedValues) {
      if (allowedValue.equalsIgnoreCase(value)) {
        return true;
      }
    }
    return false;
  }

}
