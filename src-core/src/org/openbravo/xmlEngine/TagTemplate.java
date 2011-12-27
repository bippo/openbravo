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

import org.xml.sax.Attributes;

class TagTemplate implements XmlComponentTemplate {
  protected String tag;
  protected Vector<AttributeItemTemplate> attributeVectorTemplate; // vector

  // of
  // AttributeItemTemplate

  public TagTemplate() {
  }

  public TagTemplate(String tag) {
    this.tag = tag;
  }

  public String tag() {
    return tag;
  }

  public TagTemplate(String name, Attributes amap, String replaceWhat, String replaceWith,
      String prefix, String uri) {
    this.tag = name;
    attributeVectorTemplate = new Vector<AttributeItemTemplate>();
    for (int i = 0; i < amap.getLength(); i++) {
      AttributeItemTemplate attribute = new AttributeItemTemplate();
      attribute.name = amap.getQName(i);
      String value = replace(amap.getValue(i), replaceWhat, replaceWith);
      attribute.valueTemplate = new XmlThreeTemplate(value);
      attributeVectorTemplate.addElement(attribute);
    }
    if (prefix != null) {
      AttributeItemTemplate attribute = new AttributeItemTemplate();
      attribute.name = "xmlns:" + prefix;
      attribute.valueTemplate = new XmlThreeTemplate(uri);
      attributeVectorTemplate.addElement(attribute);
    }
  }

  public String replace(String initValue, String replaceWhat, String replaceWith) {
    if (initValue != null && replaceWhat != null) {
      int index = initValue.indexOf(replaceWhat);
      if (index != -1) {
        String finalValue = initValue.substring(0, index) + replaceWith
            + initValue.substring(index + replaceWhat.length());
        return finalValue;
      }
    }
    return initValue;
  }

  public void setAttribute(AttributeComponentTemplate attributeComponentTemplate) {
    // attribute for value or replace
    if (attributeComponentTemplate.attributeName() != null) {
      for (AttributeItemTemplate attribute : attributeVectorTemplate) {
        if (attribute.name.equals(attributeComponentTemplate.attributeName())) {
          if (attributeComponentTemplate.replace() != null) {
            attribute.valueTemplate.replace(attributeComponentTemplate);
          } else {
            attribute.valueTemplate = new XmlThreeTemplate(
                attributeComponentTemplate.xmlComponentTemplate());
          }
        }
      }
    }
    // attribute boolean
    if (attributeComponentTemplate.attributeBooleanName() != null) {
      for (AttributeItemTemplate attribute : attributeVectorTemplate) {
        if (attribute.name.equals(attributeComponentTemplate.attributeBooleanName())) {
          attribute.valueTemplate = new XmlThreeTemplate(
              attributeComponentTemplate.xmlComponentTemplate());
          attribute.attributeBoolean = true;
          attribute.valueToCompareTemplate = attributeComponentTemplate.booleanWithId();
          return;
        }
      }
      // if it arrive here, then the attribute is not present, we create
      // it
      AttributeItemTemplate attribute = new AttributeItemTemplate();
      attribute.name = attributeComponentTemplate.attributeBooleanName();
      attribute.valueTemplate = new XmlThreeTemplate(
          attributeComponentTemplate.xmlComponentTemplate());
      attribute.attributeBoolean = true;
      attribute.valueToCompareTemplate = attributeComponentTemplate.booleanWithId();
      attributeVectorTemplate.addElement(attribute);
    }
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    return new TagValue(this, xmlDocument);
  }

}
