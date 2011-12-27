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

class ParameterTemplate implements XmlComponentTemplate, IDComponent {
  String strName = null;
  int type;
  String strDefault = null;
  String section = null;
  XmlComponentTemplate xmlComponentTemplate = null;
  Vector<ReplaceElement> vecReplace = null;
  DecimalFormat formatOutput;
  DecimalFormat formatSimple;

  public int type() {
    return PARAMETER;
  }

  public ParameterValue createParameterValue(XmlDocument xmlDocument) {
    ParameterValue parameterValue = xmlDocument.hasParameterValue.get(strName);
    if (parameterValue == null)
      parameterValue = new ParameterValue(this, xmlDocument);
    return parameterValue;
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    return createParameterValue(xmlDocument);
  }

}
