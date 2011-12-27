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
package org.openbravo.xmlEngine;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

class FunctionSumValue extends FunctionValue {
  BigDecimal sum = BigDecimal.ZERO;

  static Logger log4jFunctionSumValue = Logger.getLogger(FunctionSumValue.class);

  public FunctionSumValue(FunctionTemplate functionTemplate, XmlDocument xmlDocument) {
    super(functionTemplate, xmlDocument);
  }

  public String print() {
    if (functionTemplate.formatOutput != null) {
      return functionTemplate.formatOutput.format(sum);
    } else {
      return sum.toPlainString();
    }
  }

  public String printSimple() {
    if (functionTemplate.formatSimple != null) {
      return functionTemplate.formatSimple.format(sum);
    } else {
      return sum.toPlainString();
    }
  }

  public void acumulate() {
    log4jFunctionSumValue.debug("Accumulate: " + fieldValue.print());
    if (fieldValue.print() != "") {
      sum = sum.add(new BigDecimal(fieldValue.printSimple()));
    }
  }

  public void init() {
    sum = BigDecimal.ZERO;
  }

}
