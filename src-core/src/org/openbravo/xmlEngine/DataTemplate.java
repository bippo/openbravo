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
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * A piece of a XmlTemplate with a defined data. This class manages the connection an the query if
 * there is not a FieldProvider[]
 **/
class DataTemplate implements XmlComponentTemplate {
  String strName; // the name should be initialized

  Vector<SectionTemplate> vecSectionTemplate;
  XmlVectorTemplate vecDetailTemplate; // vector of XmlComponents
  Vector<Object> vecFieldTemplate; // vector of FieldComponent's
  Vector<Object> vecParameterTemplate; // vector of ParameterTemplates for the
  // query
  Vector<Object> vecFunctionTemplateData; // vector of functions of structure
  Vector<Object> vecFunctionTemplateOutSection; // vector of functions out of
  // a Section, therefore out of
  // a Data, but need to
  // initialize and accumulte in
  // the data
  SectionTemplate firstSectionTemplate;
  int intTotalLevels;
  // field if there are a connection
  String strDriver;
  String strURL;
  String strSQL;

  static Logger log4jDataTemplate = Logger.getLogger(DataTemplate.class);

  public DataTemplate() {
    intTotalLevels = -1;
    log4jDataTemplate.debug("Initial value: " + intTotalLevels);
    vecSectionTemplate = new Vector<SectionTemplate>();
    vecDetailTemplate = new XmlVectorTemplate();
    vecFieldTemplate = new Vector<Object>();
    vecParameterTemplate = new Vector<Object>();
    firstSectionTemplate = null;
    vecFunctionTemplateData = new Vector<Object>();
    vecFunctionTemplateOutSection = new Vector<Object>();
  }

  public FieldTemplate addField(String name) {
    return addField(name, null, null, null);
  }

  public FieldTemplate addField(String name, DecimalFormat formatOutput,
      DecimalFormat formatSimple, Vector<ReplaceElement> vecReplace) {
    log4jDataTemplate.debug("(addField) Crear FieldTemplate:" + name);
    // FieldComponent fieldComponent = new
    // FieldComponent(name.trim().toUpperCase(), this);
    // FieldTemplate field = new FieldTemplate(name.trim().toUpperCase(),
    // format, vecReplace); //, this); // XmlEngineNP: .trim().toUpperCase()
    // has been used again
    FieldTemplate field = new FieldTemplate(name, formatOutput, formatSimple, vecReplace); // ,
                                                                                           // this);
                                                                                           // //
                                                                                           // XmlEngineNP:
    // .trim().toUpperCase() has been
    // used again
    log4jDataTemplate.debug("(addField) call a addElement:" + name);
    vecFieldTemplate.addElement(field);
    log4jDataTemplate.debug("VECTOR addField, created:" + name);
    return field;
  }

  public FunctionTemplate addFunction(String strFunction, String name, DecimalFormat formatOutput,
      DecimalFormat formatSimple) {
    return addFunction(strFunction, name, formatOutput, formatSimple, null, null, null);
  }

  public FunctionTemplate addFunction(String strFunction, String name, DecimalFormat formatOutput,
      DecimalFormat formatSimple, XmlComponentTemplate arg1, XmlComponentTemplate arg2, String id) {
    FieldTemplate field = null;
    if (!name.equals("*") && arg1 == null) { // if it's * or an evaluation
      // function then the field is
      // not used
      log4jDataTemplate.debug("Call to addField: " + name + " " + strFunction);
      field = addField(name);
      log4jDataTemplate.debug("addField returns: " + name + " " + strFunction);
    }
    FunctionTemplate functionInstance = null;
    if (strFunction.equals("COUNT")) {
      functionInstance = new FunctionCountTemplate(name, field, null, this);
    } else if (strFunction.equals("ORDER")) {
      functionInstance = new FunctionOrderTemplate(name, field, null, this);
    } else if (strFunction.equals("SUM")) {
      functionInstance = new FunctionSumTemplate(name, field, formatOutput, formatSimple, this);
    } else if (strFunction.equals("MED")) {
      functionInstance = new FunctionMedTemplate(name, field, formatOutput, formatSimple, this);
    } else if (strFunction.equals("EQUAL")) {
      functionInstance = new FunctionEqualTemplate(id, formatOutput, formatSimple, this, arg1);
    } else if (strFunction.equals("ADD")) {
      functionInstance = new FunctionAddTemplate(id, formatOutput, formatSimple, this, arg1, arg2);
    } else if (strFunction.equals("SUBTRACT")) {
      functionInstance = new FunctionSubtractTemplate(id, formatOutput, formatSimple, this, arg1,
          arg2);
    } else if (strFunction.equals("MULTIPLY")) {
      functionInstance = new FunctionMultiplyTemplate(id, formatOutput, formatSimple, this, arg1,
          arg2);
    } else if (strFunction.equals("DIVIDE")) {
      functionInstance = new FunctionDivideTemplate(id, formatOutput, formatSimple, this, arg1,
          arg2);
    } else if (strFunction.equals("MODULE")) {
      functionInstance = new FunctionModuleTemplate(id, formatOutput, formatSimple, this, arg1,
          arg2);
    } else if (strFunction.equals("GT")) {
      functionInstance = new FunctionGtTemplate(id, formatOutput, formatSimple, this, arg1, arg2);
    } else if (strFunction.equals("LT")) {
      functionInstance = new FunctionLtTemplate(id, formatOutput, formatSimple, this, arg1, arg2);
    } else if (strFunction.equals("MAX")) {
      functionInstance = new FunctionMaxTemplate(id, formatOutput, formatSimple, this, arg1, arg2);
    } else if (strFunction.equals("MIN")) {
      functionInstance = new FunctionMinTemplate(id, formatOutput, formatSimple, this, arg1, arg2);
    } else {
      log4jDataTemplate.warn("Function " + strFunction + " not exists");
    }
    vecFunctionTemplateData.addElement(functionInstance);
    return functionInstance;
  }

  public DataValue createDataValue(XmlDocument xmlDocument) {
    DataValue dataValue = new DataValue(this, xmlDocument);
    return dataValue;
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    DataValue dataValue = xmlDocument.hasDataValue.get(strName);
    if (dataValue == null) {
      dataValue = new DataValue(this, xmlDocument);
      dataValue.initialize();
    }
    return dataValue;
  }

}
