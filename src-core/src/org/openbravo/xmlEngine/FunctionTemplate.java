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
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

class FunctionTemplate implements XmlComponentTemplate, IDComponent {
  protected String fieldName;
  protected FieldTemplate fieldTemplate;
  DecimalFormat formatOutput;
  DecimalFormat formatSimple;
  public DataTemplate dataTemplate;
  protected XmlComponentTemplate arg1Template;
  protected XmlComponentTemplate arg2Template;

  static Logger log4jFunctionTemplate = Logger.getLogger(FunctionTemplate.class);

  public FunctionTemplate() {
  }

  public FunctionTemplate(String fieldName, FieldTemplate field, DecimalFormat formatOutput,
      DecimalFormat formatSimple, DataTemplate dataTemplate) {
    this.fieldName = fieldName;
    this.fieldTemplate = field;
    this.formatOutput = formatOutput;
    this.formatSimple = formatSimple;
    this.dataTemplate = dataTemplate;
  }

  public FunctionTemplate(String fieldName, DecimalFormat formatOutput, DecimalFormat formatSimple,
      DataTemplate dataTemplate, XmlComponentTemplate arg1, XmlComponentTemplate arg2) {
    this.fieldName = fieldName; // XmlEngineNP: in fact its a function name
    this.formatOutput = formatOutput;
    this.formatSimple = formatSimple;
    this.dataTemplate = dataTemplate;
    this.arg1Template = arg1;
    this.arg2Template = arg2;
  }

  public int type() {
    return FUNCTION;
  }

  public String printFormatOutput(BigDecimal value) {
    if (formatOutput != null) {
      return formatOutput.format(value);
    } else {
      return value.toPlainString();
    }
  }

  public String printFormatSimple(BigDecimal value) {
    if (formatSimple != null) {
      return formatSimple.format(value);
    } else {
      return value.toPlainString();
    }
  }

  public FunctionValue createFunctionValue(XmlDocument xmlDocument) {
    return null;
  }

  /*
   * CHX public FunctionValue searchFunction(XmlDocument xmlDocument) {
   * log4jFunctionTemplate.debug("Searching : " + fieldName); for (Enumeration e1 =
   * xmlDocument.hasDataValue.elements() ; e1.hasMoreElements();) { DataValue dataValue =
   * (DataValue)e1.nextElement(); for (Enumeration e2 = dataValue.vecFunctionValueData.elements() ;
   * e2.hasMoreElements();) { FunctionValue functionValue = (FunctionValue)e2.nextElement();
   * log4jFunctionTemplate.debug("Comparing in vecFunctionValueData: " + fieldName + " & " +
   * functionValue.functionTemplate.fieldName); if
   * (functionValue.functionTemplate.fieldName.equals(fieldName)) {
   * log4jFunctionTemplate.debug("Found in vecFunctionValueData: " + fieldName); return
   * functionValue; } } for (Enumeration e3 = dataValue.vecFunctionValueOutSection.elements() ;
   * e3.hasMoreElements();) { FunctionValue functionValue = (FunctionValue)e3.nextElement();
   * log4jFunctionTemplate.debug("Comparing in vecFunctionOutSection: " + fieldName + " & " +
   * functionValue.functionTemplate.fieldName); if
   * (functionValue.functionTemplate.fieldName.equals(fieldName)) {
   * log4jFunctionTemplate.debug("Found in vecFunctionOutSection: " + fieldName); return
   * functionValue; } } } log4jFunctionTemplate.debug("Function: " + fieldName + " not found");
   * return null; }
   */
  // CHX
  public FunctionValue searchFunction(XmlDocument xmlDocument) {
    FunctionValue functionValue = (FunctionValue) xmlDocument.hasXmlComponentValue.get(this);
    return functionValue;
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    return createFunctionValue(xmlDocument);
  }

}
