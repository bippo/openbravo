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
import java.math.RoundingMode;

import org.apache.log4j.Logger;

class FunctionMedValue extends FunctionValue {
  int count;
  BigDecimal sum;

  static Logger log4jFunctionMedValue = Logger.getLogger(FunctionMedValue.class);

  public FunctionMedValue(FunctionTemplate functionTemplate, XmlDocument xmlDocument) {
    super(functionTemplate, xmlDocument);
  }

  public String print() {
    try {
      return functionTemplate.printFormatOutput(sum.divide(new BigDecimal(count), 12,
          RoundingMode.HALF_UP));
    } catch (ArithmeticException a) {
      return XmlEngine.strTextDividedByZero;
    }
  }

  public String printSimple() {
    try {
      return functionTemplate.printFormatSimple(sum.divide(new BigDecimal(count), 12,
          RoundingMode.HALF_UP));
    } catch (ArithmeticException a) {
      return XmlEngine.strTextDividedByZero;
    }
  }

  public void acumulate() {
    count++;
    if (fieldValue.print() != "") {
      sum = sum.add(new BigDecimal(fieldValue.printSimple()));
    }
  }

  public void init() {
    sum = BigDecimal.ZERO;
    count = 0;
  }

}
