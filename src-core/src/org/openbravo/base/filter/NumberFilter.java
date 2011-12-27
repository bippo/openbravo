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

import java.math.BigDecimal;

/**
 * Filter to check if a value can be parsed into a BigDecimal.
 * 
 * @author iperdomo
 * 
 */
public class NumberFilter implements RequestFilter {

  public static final NumberFilter instance = new NumberFilter();

  @Override
  public boolean accept(String value) {
    try {
      new BigDecimal(value);
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}
