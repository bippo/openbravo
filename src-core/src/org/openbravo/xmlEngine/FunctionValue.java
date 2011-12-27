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

class FunctionValue implements XmlComponentValue {
  FunctionTemplate functionTemplate;
  FieldValue fieldValue;

  public FunctionValue() {
  }

  public FunctionValue(FunctionTemplate functionTemplate, XmlDocument xmlDocument) {
    this.functionTemplate = functionTemplate;
    fieldValue = functionTemplate.fieldTemplate.createFieldValue(xmlDocument);
    xmlDocument.hasXmlComponentValue.put(functionTemplate, this); // CHX
  }

  public String print() {
    return null;
  }

  public void acumulate() {
  }

  public void init() {
  }

  public String printPrevious() {
    return print();
  }

  public String printSimple() {
    return print();
  }

  public String printPreviousSimple() {
    return printPrevious();
  }

}
