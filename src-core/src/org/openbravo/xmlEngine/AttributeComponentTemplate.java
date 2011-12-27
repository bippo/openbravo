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

import org.apache.log4j.Logger;

/**
 * Class that store the information of the attributes of the configuration file
 */
class AttributeComponentTemplate implements XmlComponentTemplate, IDComponent {
  // private FieldComponent fieldComponent = null;
  private FunctionTemplate functionTemplate = null;
  private XmlComponentTemplate xmlComponentTemplate = null;
  private String attributeName;
  private String strReplace;
  private String strBooleanAttribute;
  private XmlComponentTemplate xmlComponentBooleanWithId;

  static Logger log4jAttributeComponentTemplate = Logger
      .getLogger(AttributeComponentTemplate.class);

  public AttributeComponentTemplate(FunctionTemplate functionTemplate, String attributeName,
      String strReplace, String strBooleanAttribute, XmlComponentTemplate xmlComponentBooleanWithId) {
    this.functionTemplate = functionTemplate;
    this.xmlComponentTemplate = functionTemplate;
    this.attributeName = attributeName;
    this.strReplace = strReplace;
    this.strBooleanAttribute = strBooleanAttribute;
    this.xmlComponentBooleanWithId = xmlComponentBooleanWithId;
  }

  public AttributeComponentTemplate(XmlComponentTemplate xmlComponent, String attributeName,
      String strReplace, String strBooleanAttribute, XmlComponentTemplate xmlComponentBooleanWithId) {
    this.xmlComponentTemplate = xmlComponent;
    this.attributeName = attributeName;
    this.strReplace = strReplace;
    this.strBooleanAttribute = strBooleanAttribute;
    this.xmlComponentBooleanWithId = xmlComponentBooleanWithId;
  }

  public int type() {
    return ATTRIBUTE;
  }

  public FunctionTemplate functionTemplate() {
    return functionTemplate;
  }

  public XmlComponentTemplate xmlComponentTemplate() {
    return xmlComponentTemplate;
  }

  public String attributeName() {
    return attributeName;
  }

  public String replace() {
    return strReplace;
  }

  public String attributeBooleanName() {
    return strBooleanAttribute;
  }

  public XmlComponentTemplate booleanWithId() {
    return xmlComponentBooleanWithId;
  }

  /*
   * replaced 21/04/2002 public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
   * return new AttributeComponentValue(this, xmlDocument); }
   */

  // XmlEngineNP: XmlComponentTemplate is used as FieldTemplate. It must be
  // made based in the original Id's
  public FieldValue createAttributeComponentValue(XmlDocument xmlDocument) {
    for (DataValue dataValue : xmlDocument.hasDataValue.values()) {
      for (Enumeration<Object> e2 = dataValue.vecFieldValue.elements(); e2.hasMoreElements();) {
        FieldValue fieldValue = (FieldValue) e2.nextElement();
        log4jAttributeComponentTemplate.debug("Comparing: "
            + ((FieldTemplate) xmlComponentTemplate).name() + " & "
            + fieldValue.fieldTemplate.name());
        if (fieldValue.fieldTemplate.name().equals(((FieldTemplate) xmlComponentTemplate).name())) {
          return fieldValue;
        }
      }
    }
    if (log4jAttributeComponentTemplate.isDebugEnabled())
      log4jAttributeComponentTemplate.debug("New FieldValue: "
          + ((FieldTemplate) xmlComponentTemplate).name());
    FieldValue fieldValue = new FieldValue((FieldTemplate) (this.xmlComponentTemplate), xmlDocument);
    return fieldValue;
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    // log4jAttributeComponentTemplate.debug("Instanceof: " +
    // (xmlComponentTemplate instanceof FieldTemplate) +
    // " Function: " + (xmlComponentTemplate instanceof FunctionTemplate)+
    // " Function subtract: " + (xmlComponentTemplate instanceof
    // FunctionSubtractTemplate));
    if (xmlComponentTemplate instanceof FunctionTemplate) {
      return ((FunctionTemplate) xmlComponentTemplate).createFunctionValue(xmlDocument);
    } else if (xmlComponentTemplate instanceof ParameterTemplate) {
      return ((ParameterTemplate) xmlComponentTemplate).createParameterValue(xmlDocument);
    } else if (xmlComponentTemplate instanceof FieldTemplate) {
      return createAttributeComponentValue(xmlDocument);
    } else {
      log4jAttributeComponentTemplate.warn("Instance type not found");
      return null;
    }
  }

}
