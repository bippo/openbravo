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

import org.apache.log4j.Logger;

/**
 * class for represent the structure of the value of an attribute. It is used because part of the
 * value of an attribute can be modified with the value of an XmlComponent. The value is represented
 * by a three with nodes of three branchs the fist and the third branchs are another
 * XmlThreeTemplate, a String or null the second is an XmlComponent
 */
class XmlThreeTemplate implements XmlComponentTemplate {
  protected XmlThreeTemplate xmlBegin = null;
  protected boolean isBeginXmlThreeTemplate = false;
  protected String stringBegin;
  protected XmlComponentTemplate xmlMiddle = null;
  protected XmlThreeTemplate xmlEnd = null;
  protected boolean isEndXmlThreeTemplate = false;
  protected String stringEnd;
  static Logger log4jXmlThreeTemplate = Logger.getLogger(XmlThreeTemplate.class);

  public XmlThreeTemplate() {
  }

  public XmlThreeTemplate(String character) {
    log4jXmlThreeTemplate.debug("constructor with parameter:" + character);
    stringBegin = character;
  }

  public XmlThreeTemplate(XmlComponentTemplate xmlComponentTemplate) {
    xmlMiddle = xmlComponentTemplate;
  }

  public XmlThreeTemplate(String characterBegin, XmlComponentTemplate xmlComponentTemplate,
      String characterEnd) {
    stringBegin = characterBegin;
    stringEnd = characterEnd;
    xmlMiddle = xmlComponentTemplate;
    log4jXmlThreeTemplate.debug("constructor with stringBegin: " + stringBegin + " stringEnd: "
        + stringEnd);
  }

  public String printStringBegin() {
    return stringBegin;
  }

  public String printStringEnd() {
    return stringEnd;
  }

  public void replace(AttributeComponentTemplate attributeComponentTemplate) {
    if (isBeginXmlThreeTemplate) {
      xmlBegin.replace(attributeComponentTemplate);
    } else {
      if (stringBegin != null) {
        int index = stringBegin.indexOf(attributeComponentTemplate.replace());
        if (index != -1) {
          xmlBegin = new XmlThreeTemplate(stringBegin.substring(0, index),
              attributeComponentTemplate.xmlComponentTemplate(), stringBegin.substring(index
                  + attributeComponentTemplate.replace().length()));
          isBeginXmlThreeTemplate = true;
        }
      }
    }
    if (isEndXmlThreeTemplate) {
      xmlEnd.replace(attributeComponentTemplate);
    } else {
      if (stringEnd != null) {
        int index = stringEnd.indexOf(attributeComponentTemplate.replace());
        if (index != -1) {
          xmlEnd = new XmlThreeTemplate(stringEnd.substring(0, index),
              attributeComponentTemplate.xmlComponentTemplate(), stringEnd.substring(index
                  + attributeComponentTemplate.replace().length()));
          isEndXmlThreeTemplate = true;
        }
      }
    }
  }

  public XmlThreeValue createXmlThreeValue(XmlDocument xmlDocument) {
    return new XmlThreeValue(this, xmlDocument);
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    return new XmlThreeValue(this, xmlDocument);
  }

}
