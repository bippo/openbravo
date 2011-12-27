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
 * value of an attribute can be modified with the value of an HtmlComponent. The value is
 * represented by a three with nodes of three branchs the fist and the third branchs are another
 * node or null the second is an HtmlComponent
 */

// XmlEngineNP: it will be replaced by XmlVectorValue in the future
class XmlThreeValue implements XmlComponentValue {
  protected XmlThreeTemplate xmlThreeTemplate;
  protected XmlComponentValue xmlBegin = null;
  protected XmlComponentValue xmlMiddle = null;
  protected XmlComponentValue xmlEnd = null;
  static Logger log4jXmlThreeValue = Logger.getLogger(XmlThreeValue.class);

  public XmlThreeValue(XmlThreeTemplate xmlThreeTemplate, XmlDocument xmlDocument) {
    this.xmlThreeTemplate = xmlThreeTemplate;
    // New to create the Value:
    if (xmlThreeTemplate.isBeginXmlThreeTemplate) {
      xmlBegin = new XmlThreeValue(xmlThreeTemplate.xmlBegin, xmlDocument);
    }
    if (xmlThreeTemplate.xmlMiddle != null) {
      log4jXmlThreeValue.debug("created Middle element");
      // XmlEngineNP: remove if only it used in the log
      // log4jXmlThreeValue.debug("value:" +
      // ((ParameterTemplate)(xmlThreeTemplate.xmlMiddle)).strName);
      xmlMiddle = xmlThreeTemplate.xmlMiddle.createXmlComponentValue(xmlDocument);
    }
    if (xmlThreeTemplate.isEndXmlThreeTemplate) {
      xmlEnd = new XmlThreeValue(xmlThreeTemplate.xmlEnd, xmlDocument);
    }

  }

  public String print() {
    String strReturn = "";
    log4jXmlThreeValue.debug("print");
    if (xmlBegin != null)
      strReturn = strReturn.concat(xmlBegin.print());
    else if (xmlThreeTemplate.printStringBegin() != null)
      strReturn = strReturn.concat(xmlThreeTemplate.printStringBegin());
    if (xmlMiddle != null) {
      String xmlMiddlePrint = xmlMiddle.print();
      if (xmlMiddlePrint != null)
        strReturn = strReturn.concat(xmlMiddlePrint);
    }
    if (xmlEnd != null)
      strReturn = strReturn.concat(xmlEnd.print());
    else if (xmlThreeTemplate.printStringEnd() != null)
      strReturn = strReturn.concat(xmlThreeTemplate.printStringEnd());
    log4jXmlThreeValue.debug("print return: " + strReturn);
    return strReturn;
  }

  public String printPrevious() {
    String strReturn = "";
    log4jXmlThreeValue.debug("printPrevious");
    if (xmlBegin != null)
      strReturn = strReturn.concat(xmlBegin.printPrevious());
    else if (xmlThreeTemplate.printStringBegin() != null)
      strReturn = strReturn.concat(xmlThreeTemplate.printStringBegin());
    if (xmlMiddle != null) {
      String strMiddle = xmlMiddle.printPrevious();
      log4jXmlThreeValue.debug("xmlMiddle: " + strReturn);
      if (strMiddle != null)
        strReturn = strReturn.concat(strMiddle);
    }
    if (xmlEnd != null)
      strReturn = strReturn.concat(xmlEnd.printPrevious());
    else if (xmlThreeTemplate.printStringEnd() != null)
      strReturn = strReturn.concat(xmlThreeTemplate.printStringEnd());
    log4jXmlThreeValue.debug("print previous return: " + strReturn);
    return strReturn;
  }

  public String printSimple() {
    return print();
  }

  public String printPreviousSimple() {
    return printPrevious();
  }

}
