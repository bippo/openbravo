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

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Each of the breaks in a DataValue
 **/
class SectionValue {
  SectionTemplate sectionTemplate;
  DataValue dataValue;
  StringBuffer strSection;
  XmlVectorValue vecHeadValue;
  XmlVectorValue vecFootValue;
  Vector<FunctionValue> vecFunctionValue;
  FieldValue breakFieldValue;
  static Logger log4jSectionValue = Logger.getLogger(SectionValue.class);

  public SectionValue(SectionTemplate sectionTemplate, XmlDocument xmlDocument, DataValue dataValue) {
    this.sectionTemplate = sectionTemplate;
    this.dataValue = dataValue;

    vecHeadValue = new XmlVectorValue(sectionTemplate.vecHeadTemplate, xmlDocument);
    vecFootValue = new XmlVectorValue(sectionTemplate.vecFootTemplate, xmlDocument);

    if (sectionTemplate.breakFieldTemplate == null) {
      breakFieldValue = null;
    } else {
      breakFieldValue = sectionTemplate.breakFieldTemplate.createFieldValue(xmlDocument);
    }

    // vector of Functions
    vecFunctionValue = new Vector<FunctionValue>();
    for (Enumeration<Object> e1 = sectionTemplate.vecFunctionTemplate.elements(); e1
        .hasMoreElements();) {
      FunctionTemplate functionTemplate = (FunctionTemplate) e1.nextElement();
      FunctionValue functionValue = functionTemplate.createFunctionValue(xmlDocument);
      vecFunctionValue.addElement(functionValue);
      log4jSectionValue.debug("Function: " + functionValue.functionTemplate.fieldName);
    }
  }

  public void init() {
    log4jSectionValue.debug("(Init) Levels: " + sectionTemplate.dataTemplate.intTotalLevels
        + " actual level: " + sectionTemplate.intLevel);
    if (breakFieldValue != null) {
      breakFieldValue.savePrevious();
    }
    for (FunctionValue functionInstance : vecFunctionValue) {
      functionInstance.init();
    }
    if (sectionTemplate.intLevel != sectionTemplate.dataTemplate.intTotalLevels) {
      SectionValue section = dataValue.vecSectionValue.elementAt(sectionTemplate.intLevel + 1);
      section.strSection = new StringBuffer();
    } else {
      dataValue.strDetailValue = new StringBuffer();
    }
  }

  public void close() {
    log4jSectionValue.debug("(Close) Levels:" + sectionTemplate.dataTemplate.intTotalLevels
        + " actual level: " + sectionTemplate.intLevel);
    if (sectionTemplate.intLevel != sectionTemplate.dataTemplate.intTotalLevels) {
      SectionValue section = dataValue.vecSectionValue.elementAt(sectionTemplate.intLevel + 1);
      section.close();
    }
    // Head of the section
    strSection.append(vecHeadValue.printPreviousStringBuffer());

    // add next section
    if (sectionTemplate.intLevel != sectionTemplate.dataTemplate.intTotalLevels) {
      SectionValue section = dataValue.vecSectionValue.elementAt(sectionTemplate.intLevel + 1);
      strSection.append(section.strSection);
    } else {
      // log4jSectionValue.debug("Adding strDetail length detail: " +
      // dataValue.strDetailValue.length() + " section length: " +
      // strSection.length());
      // System.gc();
      strSection.append(dataValue.strDetailValue);
      // log4jSectionValue.debug("Añadido strDetail"+ " section length: "
      // + strSection.length());
    }
    // Foot of the section
    strSection.append(vecFootValue.printPreviousStringBuffer());

    init();
    log4jSectionValue.debug("End of close");

  }

  public boolean check() {
    if (breakFieldValue != null) {
      return breakFieldValue.check();
    } else {
      return true;
    }
  }

  public void acumulate() {
    for (FunctionValue functionInstance : vecFunctionValue) {
      functionInstance.acumulate();
    }
  }
}
