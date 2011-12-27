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

class FunctionModuleValue extends FunctionEvaluationValue {

  static Logger log4jFunctionModuleValue = Logger.getLogger(FunctionModuleValue.class);

  public FunctionModuleValue(FunctionTemplate functionTemplate, XmlDocument xmlDocument) {
    super(functionTemplate, xmlDocument);
  }

  public String print() {
    log4jFunctionModuleValue.debug("Arg2: " + arg2Value.printSimple());
    log4jFunctionModuleValue.debug("Arg1: " + arg1Value.printSimple());

    if (arg1Value.print().equals(XmlEngine.strTextDividedByZero)
        || arg2Value.print().equals(XmlEngine.strTextDividedByZero)) {
      return XmlEngine.strTextDividedByZero;
    } else {
      try {
        // divisionFloor = Math.floor( arg1Value / arg2Value )
        // printSimple may return empty string in some of the occasion.
        // If that is the case, create the BigDecimal without validate will throws
        // NumberFormatException.
        // We need to initiate the argument value to avoid the problem
        String valueOfArg1 = arg1Value.printSimple();
        String valueOfArg2 = arg2Value.printSimple();
        if (valueOfArg1.trim().equalsIgnoreCase("")) {
          valueOfArg1 = BigDecimal.ONE.toString();
        }
        if (valueOfArg2.trim().equalsIgnoreCase("")) {
          valueOfArg2 = BigDecimal.ONE.toString();
        }
        BigDecimal divisionFloor = new BigDecimal(valueOfArg1).divide(new BigDecimal(valueOfArg2),
            0, RoundingMode.FLOOR);
        return functionTemplate.printFormatOutput(new BigDecimal(valueOfArg1)
            .subtract(new BigDecimal(valueOfArg2).multiply(divisionFloor)));
      } catch (ArithmeticException a) {
        return XmlEngine.strTextDividedByZero;
      }
    }
  }

  public String printSimple() {
    log4jFunctionModuleValue.debug("Arg2: " + arg2Value.printSimple());
    log4jFunctionModuleValue.debug("Arg1: " + arg1Value.printSimple());

    if (arg1Value.print().equals(XmlEngine.strTextDividedByZero)
        || arg2Value.print().equals(XmlEngine.strTextDividedByZero)) {
      return XmlEngine.strTextDividedByZero;
    } else {
      try {
        // divisionFloor = Math.floor( arg1Value / arg2Value )
        // printSimple may return empty string in some of the occasion.
        // If that is the case, create the BigDecimal without validate will throws
        // NumberFormatException.
        // We need to initiate the argument value to avoid the problem
        String valueOfArg1 = arg1Value.printSimple();
        String valueOfArg2 = arg2Value.printSimple();
        if (valueOfArg1.trim().equalsIgnoreCase("")) {
          valueOfArg1 = BigDecimal.ONE.toString();
        }
        if (valueOfArg2.trim().equalsIgnoreCase("")) {
          valueOfArg2 = BigDecimal.ONE.toString();
        }
        BigDecimal divisionFloor = new BigDecimal(valueOfArg1).divide(new BigDecimal(valueOfArg2),
            0, RoundingMode.FLOOR);
        return functionTemplate.printFormatSimple(new BigDecimal(valueOfArg1)
            .subtract(new BigDecimal(valueOfArg2).multiply(divisionFloor)));
      } catch (ArithmeticException a) {
        return XmlEngine.strTextDividedByZero;
      }
    }
  }
}
