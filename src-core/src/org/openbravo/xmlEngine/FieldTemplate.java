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

class FieldTemplate implements XmlComponentTemplate, IDComponent {
  private String fieldName;
  int type = FIELD;
  DecimalFormat formatOutput;
  DecimalFormat formatSimple;
  Vector<ReplaceElement> vecReplace;

  static Logger log4jFieldTemplate = Logger.getLogger(FieldTemplate.class);

  public FieldTemplate(String fieldName, DecimalFormat formatOutput, DecimalFormat formatSimple,
      Vector<ReplaceElement> vecReplace) { // ,
    // StructureSQL
    // structureSQL)
    // {
    this.fieldName = fieldName;
    this.formatOutput = formatOutput;
    this.formatSimple = formatSimple;
    this.vecReplace = vecReplace;
  }

  public int type() {
    return type;
  }

  public String name() {
    return fieldName;
  }

  /*
   * CHX public FieldValue createFieldValue(XmlDocument xmlDocument) { for (Enumeration e1 =
   * xmlDocument.hasDataValue.elements() ; e1.hasMoreElements();) { DataValue dataValue =
   * (DataValue)e1.nextElement(); for (Enumeration e2 = dataValue.vecFieldValue.elements() ;
   * e2.hasMoreElements();) { FieldValue fieldValue = (FieldValue)e2.nextElement();
   * log4jFieldTemplate.debug("Comparing: " + fieldName + " & " + fieldValue.fieldTemplate.name());
   * if (fieldValue.fieldTemplate.name().equals(fieldName)) { return fieldValue; } } }
   * log4jFieldTemplate.info("New FieldValue: " + fieldName); FieldValue fieldValue = new
   * FieldValue(this, xmlDocument); return fieldValue; }
   */
  // CHX
  public FieldValue createFieldValue(XmlDocument xmlDocument) {
    FieldValue fieldValue = (FieldValue) xmlDocument.hasXmlComponentValue.get(this);
    if (fieldValue == null) {
      fieldValue = new FieldValue(this, xmlDocument);
      xmlDocument.hasXmlComponentValue.put(this, fieldValue);
    }
    return fieldValue;
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    return createFieldValue(xmlDocument);
  }
}
