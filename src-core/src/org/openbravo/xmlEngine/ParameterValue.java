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

import org.apache.log4j.Logger;
import org.openbravo.utils.Replace;

class ParameterValue implements XmlComponentValue {
  private static final Logger log4j = Logger.getLogger(ParameterValue.class);
  String strValue = null;
  ParameterTemplate parameterTemplate;
  XmlComponentValue xmlComponentValue = null;

  public ParameterValue(ParameterTemplate ParameterTemplate, XmlDocument xmlDocument) {
    this.parameterTemplate = ParameterTemplate;
  }

  public void setXmlComponentValue(XmlDocument xmlDocument) {
    if (parameterTemplate.xmlComponentTemplate != null) {
      xmlComponentValue = parameterTemplate.xmlComponentTemplate
          .createXmlComponentValue(xmlDocument.parentXmlDocument);
    }
  }

  private String replace(String strIni) {
    if (parameterTemplate.vecReplace != null) {
      String strFin = strIni;
      for (ReplaceElement replaceElement : parameterTemplate.vecReplace) {
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
    if (xmlComponentValue != null) {
      return xmlComponentValue.print();
    }

    if (parameterTemplate.formatOutput == null) {
      return strValue;
    }

    if (strValue == null || strValue.equals("") || strValue.equalsIgnoreCase("NULL")) {
      return "";
    }

    // format value for output
    BigDecimal total = BigDecimal.ZERO;
    try {
      total = new BigDecimal(strValue);
    } catch (Exception e) {
      log4j.error("print() - Could not parse to string to BigDecimal: " + strValue, e);
    }
    return parameterTemplate.formatOutput.format(total);
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
