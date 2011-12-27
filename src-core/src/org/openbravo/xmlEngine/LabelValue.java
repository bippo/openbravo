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
import org.openbravo.utils.Replace;

class LabelValue implements XmlComponentValue {

  static Logger log4j = Logger.getLogger(LabelValue.class);

  String strValue = null;
  LabelTemplate labelTemplate;
  XmlComponentValue xmlComponentValue = null;

  public LabelValue(LabelTemplate labelTemplate, XmlDocument xmlDocument) {
    this.labelTemplate = labelTemplate;
  }

  public void setXmlComponentValue(XmlDocument xmlDocument) {
    if (labelTemplate.xmlComponentTemplate != null) {
      xmlComponentValue = labelTemplate.xmlComponentTemplate
          .createXmlComponentValue(xmlDocument.parentXmlDocument);
    }
  }

  private String replace(String strIni) {
    log4j.debug("running replace() method in LabelValue class with input string: " + strIni);
    if (labelTemplate.vecReplace != null) {
      String strFin = strIni;
      for (ReplaceElement replaceElement : labelTemplate.vecReplace) {
        strFin = Replace.replace(strFin, replaceElement.replaceWhat, replaceElement.replaceWith);
      }
      return strFin;
    } else {
      return strIni;
    }
  }

  public void setValue(String strValue) {
    this.strValue = replace(strValue);
  }

  public String print() {
    log4j.debug("running print() in LabelValue class");
    if (xmlComponentValue != null) {
      return xmlComponentValue.print();
    } else {
      return strValue;
    }
  }

  public String printPrevious() {
    if (xmlComponentValue != null) {
      return xmlComponentValue.printPrevious();
    } else {
      return strValue;
    }
  }

  public String printSimple() {
    if (xmlComponentValue != null) {
      return xmlComponentValue.printSimple();
    } else {
      return strValue;
    }
  }

  public String printPreviousSimple() {
    if (xmlComponentValue != null) {
      return xmlComponentValue.printPreviousSimple();
    } else {
      return strValue;
    }
  }
}
