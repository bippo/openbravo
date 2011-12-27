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

class FunctionEvaluationValue extends FunctionValue {
  protected XmlComponentValue arg1Value;
  protected XmlComponentValue arg2Value;

  public FunctionEvaluationValue(FunctionTemplate functionTemplate, XmlDocument xmlDocument) {
    this.functionTemplate = functionTemplate;
    arg1Value = functionTemplate.arg1Template.createXmlComponentValue(xmlDocument);
    if (functionTemplate.arg2Template != null) {
      arg2Value = functionTemplate.arg2Template.createXmlComponentValue(xmlDocument);
    }
    xmlDocument.hasXmlComponentValue.put(functionTemplate, this); // CHX
  }

}
