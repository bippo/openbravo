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

class FunctionGtValue extends FunctionEvaluationValue {

  static Logger log4jFunctionGtValue = Logger.getLogger(FunctionGtValue.class);

  public FunctionGtValue(FunctionTemplate functionTemplate, XmlDocument xmlDocument) {
    super(functionTemplate, xmlDocument);
  }

  public String print() {
    log4jFunctionGtValue.debug("Arg1: " + arg1Value.printSimple() + " Arg2: "
        + arg2Value.printSimple());
    if (arg1Value.print().equals(XmlEngine.strTextDividedByZero)
        || arg2Value.print().equals(XmlEngine.strTextDividedByZero)) {
      return XmlEngine.strTextDividedByZero;
    } else {
      if (new BigDecimal(arg1Value.printSimple())
          .compareTo(new BigDecimal(arg2Value.printSimple())) > 0) {
        return "1";
      } else {
        return "0";
      }
    }
  }

  public String printSimple() {
    return print();
  }

}
