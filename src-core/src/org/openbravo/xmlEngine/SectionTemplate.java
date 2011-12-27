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

import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Each of the breaks in a DataTemplate
 **/
class SectionTemplate implements IDComponent {
  String name;
  DataTemplate dataTemplate;
  int intLevel;
  XmlVectorTemplate vecHeadTemplate;
  XmlVectorTemplate vecFootTemplate;
  Vector<Object> vecFunctionTemplate;
  FieldTemplate breakFieldTemplate;

  static Logger log4jSectionTemplate = Logger.getLogger(SectionTemplate.class);

  public SectionTemplate(String id, DataTemplate dataTemplate) {
    name = id;
    this.dataTemplate = dataTemplate;
    dataTemplate.intTotalLevels++;
    intLevel = dataTemplate.intTotalLevels; // the first level is 0
    vecHeadTemplate = new XmlVectorTemplate();
    vecFootTemplate = new XmlVectorTemplate();
    vecFunctionTemplate = new Vector<Object>();
  }

  public int type() {
    return SECTION;
  }

  public DataTemplate dataTemplate() {
    return dataTemplate;
  }

  public void addFunction(FunctionTemplate function) {
    vecFunctionTemplate.addElement(function);
  }

  public SectionValue createSectionValue(XmlDocument xmlDocument, DataValue dataValue) {
    SectionValue sectionValue = new SectionValue(this, xmlDocument, dataValue);
    return sectionValue;
  }
}
