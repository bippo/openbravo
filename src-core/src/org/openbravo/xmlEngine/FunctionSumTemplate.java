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

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

class FunctionSumTemplate extends FunctionTemplate {

  static Logger log4jFunctionSumTemplate = Logger.getLogger(FunctionSumTemplate.class);

  public FunctionSumTemplate(String fieldName, FieldTemplate field, DecimalFormat formatOutput,
      DecimalFormat formatSimple, DataTemplate dataTemplate) {
    super(fieldName, field, formatOutput, formatSimple, dataTemplate);
  }

  public FunctionValue createFunctionValue(XmlDocument xmlDocument) {
    FunctionValue functionValue = searchFunction(xmlDocument);
    if (functionValue == null) {
      if (log4jFunctionSumTemplate.isDebugEnabled())
        log4jFunctionSumTemplate.debug("New FunctionSumValue: " + fieldName);
      functionValue = new FunctionSumValue(this, xmlDocument);
    }
    return functionValue;
  }

}
